package cn.vsx.vc.service;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import org.apache.log4j.Logger;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.CallMode;
import cn.vsx.hamster.common.MemberChangeType;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.SignalServerErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCallingCannotClickHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCeaseGroupCallConformationHander;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallCeasedIndicationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyMemberChangeHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRequestGroupCallConformationHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.receiveHandle.ReceiverPTTButtonEventHandler;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.audio.CheckMyPermission;

import static cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState.IDLE;

/**
 * @author martian on 2018/12/11.
 */
public class PTTButtonEventService extends Service {

    private Logger logger = Logger.getLogger(getClass());
    private PTTButtonEventBinder pttButtonEventBinder = new PTTButtonEventBinder();
    private static final int HANDLE_CODE_GROUNP_TIME = 1;

    //组呼--记录时间
    private int timeProgress;
//  private ButtonEventReceiver buttonEventReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        logger.info("PTTButtonEventService = onCreate");
        initListener();
    }

    private void initListener() {
        MyTerminalFactory.getSDK().registReceiveHandler(receiverPTTButtonEventHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyMemberChangeHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveCallingCannotClickHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveRequestGroupCallConformationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveCeaseGroupCallConformationHander);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String pttAction = intent.getStringExtra(Constants.PTTEVEVT_ACTION);
        if (!TextUtils.isEmpty(pttAction)) {
            //做相应的处理
        }
        initListener();
        return super.onStartCommand(intent, flags, startId);
    }

    public Handler myHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLE_CODE_GROUNP_TIME:
                    timeProgress--;
                    if (timeProgress <= 0) {
                        myHandler.removeMessages(1);
                        pttUpDoThing();
                    } else {
                        //                    talkback_time_progress.setText(String.valueOf(timeProgress));
                        myHandler.sendEmptyMessageDelayed(1, 1000);
                    }
                    break;
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return pttButtonEventBinder;
    }

    public class PTTButtonEventBinder extends Binder {
        //释放资源
        public void pttButtonEventService() {
            if (MyApplication.instance.isPttPress && !TerminalFactory.getSDK().isExit()) {
                MyTerminalFactory.getSDK().getGroupCallManager().ceaseGroupCall();
            }
        }
    }

    /**
     * 通过广播收到PTT按键的事件
     */
    private ReceiverPTTButtonEventHandler receiverPTTButtonEventHandler = new ReceiverPTTButtonEventHandler() {
        @Override
        public void handler(String action) {
            logger.info("ReceiverPTTButtonEventHandler = " + action);
            if (Constants.PTTEVEVT_ACTION_DOWN.equals(action)) {
                //PTT实体按键-按下
                if (!MyApplication.instance.folatWindowPress && !MyApplication.instance.isPttPress) {
                    MyApplication.instance.isClickVolumeToCall = true;
                    onPTTVolumeBtnStatusChange(MyApplication.instance.getGroupSpeakState());
                    MyApplication.instance.volumePress = true;
                }
            } else if (Constants.PTTEVEVT_ACTION_UP.equals(action)) {
                //PTT实体按键-抬起
                if (MyApplication.instance.volumePress) {
                    MyApplication.instance.isClickVolumeToCall = false;
                    onPTTVolumeBtnStatusChange(GroupCallSpeakState.END);
                    MyApplication.instance.volumePress = false;
                }
            }
        }
    };

    private void onPTTVolumeBtnStatusChange(GroupCallSpeakState groupCallSpeakState) {
        if (groupCallSpeakState == IDLE) {
            pttDownDoThing();
        } else {
            myHandler.removeMessages(HANDLE_CODE_GROUNP_TIME);
            pttUpDoThing();
        }
    }

    /**
     * PTT按下以后
     */
    private void pttDownDoThing() {
        logger.info("ptt.pttDownDoThing执行了 isPttPress：" + MyApplication.instance.isPttPress);

        if (!CheckMyPermission.selfPermissionGranted(this, Manifest.permission.RECORD_AUDIO)) {//没有录音权限

            return;
        }
        //没有组呼权限
        if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_TALK.name())) {
            ToastUtil.showToast(this, "没有组呼权限");
            return;
        }
        int resultCode = MyTerminalFactory.getSDK().getGroupCallManager().requestCurrentGroupCall("");
        logger.info("PTT按下以后resultCode:" + resultCode);
        if (resultCode == BaseCommonCode.SUCCESS_CODE) {//允许组呼了
            if (!MyTerminalFactory.getSDK().getAudioProxy().isSpeakerphoneOn()) {
                MyTerminalFactory.getSDK().getAudioProxy().setSpeakerphoneOn(true);
            }
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCallingCannotClickHandler.class, true);
            //------------------------------------------
            //            change2PreSpeaking();
        } else if (resultCode == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode()) {
            //------------------------------------------
            //            change2Waiting();
        } else {//组呼失败的提示
            ToastUtil.groupCallFailToast(this, resultCode);
        }

    }

    /**
     * PTT抬起以后
     */
    private void pttUpDoThing() {
        logger.info("ptt.pttUpDoThing执行了 isPttPress：" + MyApplication.instance.isPttPress);
        TerminalFactory.getSDK().getAudioProxy().volumeCancelQuiet();
        if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_TALK.name())) {
            return;
        }

        if (MyApplication.instance.getGroupListenenState() == GroupCallListenState.LISTENING) {
            //---------------------------------------------
            //            isScanGroupCall = false;
            //            change2Listening();
        } else {
            //---------------------------------------------
            //            change2Silence();

        }
        MyTerminalFactory.getSDK().getGroupCallManager().ceaseGroupCall();
        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCallingCannotClickHandler.class, false);

    }

    /**
     * 成员信息改变
     */
    private ReceiveNotifyMemberChangeHandler receiveNotifyMemberChangeHandler = new ReceiveNotifyMemberChangeHandler() {

        @Override
        public void handler(MemberChangeType memberChangeType) {
            logger.info("触发了receiveNotifyMemberChangeHandler：" + memberChangeType);
        }
    };

    /**
     * PTT按下时不可切换
     */
    private ReceiveCallingCannotClickHandler receiveCallingCannotClickHandler = new ReceiveCallingCannotClickHandler() {
        @Override
        public void handler(final boolean isCannotCheck) {
            logger.info("change_group_show_area被禁了 ？ isCannotCheck：" + isCannotCheck);

        }
    };

    /**
     * 被动方组呼来了
     */
    private ReceiveGroupCallIncommingHandler receiveGroupCallIncommingHandler = new ReceiveGroupCallIncommingHandler() {
        @Override
        public void handler(int memberId, final String memberName, final int groupId, String version, CallMode currentCallMode, long uniqueNo) {
            logger.info("触发了被动方组呼来了receiveGroupCallIncommingHandler:" + "curreneCallMode " + currentCallMode + "-----" + MyApplication.instance.getGroupSpeakState());
            if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_LISTEN.name())) {
                //ToastUtil.showToast(activity, "没有组呼听的权限");
            }
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    MyTerminalFactory.getSDK().putParam(Params.CURRENT_SPEAKER, memberName);
                }
            });
        }
    };

    /**
     * 被动方组呼停止
     */
    private ReceiveGroupCallCeasedIndicationHandler receiveGroupCallCeasedIndicationHandler = new ReceiveGroupCallCeasedIndicationHandler() {

        @Override
        public void handler(int reasonCode) {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    MyTerminalFactory.getSDK().putParam(Params.CURRENT_SPEAKER, "");
                    logger.info("被动方停止组呼" + "/" + MyApplication.instance.getGroupListenenState() + MyApplication.instance.isPttPress);
                }
            });
        }
    };

    /**
     * 主动方请求组呼的消息
     */
    private ReceiveRequestGroupCallConformationHandler receiveRequestGroupCallConformationHandler = new ReceiveRequestGroupCallConformationHandler() {
        @Override
        public void handler(int methodResult, String resultDesc, int groupId) {
            logger.info("主动方请求组呼的消息：" + methodResult + "-------" + resultDesc);
            logger.info("主动方请求组呼的消息：" + MyTerminalFactory.getSDK().getGroupCallManager().getCurrentCallMode());

            if (MyTerminalFactory.getSDK().getGroupCallManager().getCurrentCallMode() == CallMode.GENERAL_CALL_MODE) {
                if (methodResult == BaseCommonCode.SUCCESS_CODE) {//请求成功，开始组呼
                    myHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            myHandler.removeMessages(HANDLE_CODE_GROUNP_TIME);
                            timeProgress = 60;
                            myHandler.sendEmptyMessageDelayed(HANDLE_CODE_GROUNP_TIME, 1000);

                            MyTerminalFactory.getSDK().putParam(Params.CURRENT_SPEAKER, "");
                        }
                    });
                } else if (methodResult == SignalServerErrorCode.CANT_SPEAK_IN_GROUP.getErrorCode()) {//只听组
                } else if (methodResult == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode()) {//请求等待中
                } else {
                }
            } else {
            }
        }
    };

    /**
     * 主动方停止组呼的消息
     */
    private ReceiveCeaseGroupCallConformationHander receiveCeaseGroupCallConformationHander = new ReceiveCeaseGroupCallConformationHander() {
        @Override
        public void handler(int resultCode, String resultDesc) {
            logger.info("主动方停止组呼的消息ReceiveCeaseGroupCallConformationHander" + "/" + MyApplication.instance.getGroupListenenState());
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (MyTerminalFactory.getSDK().getGroupCallManager().getCurrentCallMode() == CallMode.GENERAL_CALL_MODE) {
                        if (MyApplication.instance.getGroupListenenState() == GroupCallListenState.LISTENING) {
                        } else {
                            //如果是停止组呼
                            MyApplication.instance.isPttPress = false;
                            myHandler.removeMessages(HANDLE_CODE_GROUNP_TIME);
                            timeProgress = 60;
                        }
                    }
                }
            });
        }
    };


    @Override
    public boolean onUnbind(Intent intent) {
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiverPTTButtonEventHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyMemberChangeHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveCallingCannotClickHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveRequestGroupCallConformationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveCeaseGroupCallConformationHander);
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
//    unregisterReceiver(buttonEventReceiver);
        super.onDestroy();
    }
}

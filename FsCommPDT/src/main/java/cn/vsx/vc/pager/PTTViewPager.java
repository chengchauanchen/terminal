package cn.vsx.vc.pager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import org.apache.log4j.Logger;

import butterknife.Bind;
import butterknife.ButterKnife;
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
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivePTTDownHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivePTTUpHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRequestGroupCallConformationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveServerConnectionEstablishedHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.BaseActivity;
import cn.vsx.vc.activity.GroupCallNewsActivity;
import cn.vsx.vc.activity.NewMainActivity;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.fragment.TalkbackFragment;
import cn.vsx.vc.utils.DataUtil;
import ptt.terminalsdk.tools.ToastUtil;
import cn.vsx.vc.view.BaseViewPager;
import cn.vsx.vc.view.LazyViewPager;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.audio.CheckMyPermission;

import static cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState.LISTENING;
import static cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState.IDLE;

public class PTTViewPager extends BaseViewPager {

    private ReceivePTTUpHandler receivePTTUpHandler = new ReceivePTTUpHandler() {
        @Override
        public void handler() {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    MyApplication.instance.isClickVolumeToCall = false;
                    if (MyApplication.instance.getGroupListenenState() == LISTENING) {
                        change2Listening();
                    }
                    else {
                        change2Silence();
                    }
                }
            });
        }
    };
    private ReceivePTTDownHandler receivePTTDownHandler = new ReceivePTTDownHandler() {
        @Override
        public void handler(int requestGroupCall) {
            if (requestGroupCall == BaseCommonCode.SUCCESS_CODE) {
                myHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!CheckMyPermission.selfPermissionGranted((NewMainActivity) context, Manifest.permission.RECORD_AUDIO)) {//没有录音权限
                            CheckMyPermission.permissionPrompt((NewMainActivity) context, Manifest.permission.RECORD_AUDIO);
                            return;
                        }
                        change2PreSpeaking();
                        MyApplication.instance.isClickVolumeToCall = true;
                    }
                });
            }else if(requestGroupCall == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode()){
                change2Waiting();
            }else {//组呼失败的提示
                ToastUtil.groupCallFailToast(context, requestGroupCall);
            }
        }
    };

    /**
     * 转组的时候不能点击Ptt按钮的消息
     */
//    private ReceiveChangeGroupingCannotClickHandler receiveChangeGroupingCannotClickHandler = new ReceiveChangeGroupingCannotClickHandler() {
//        @Override
//        public void handler(final boolean clickable) {
//            logger.info("执行了锁定");
//            myHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    ptt.setEnabled(clickable);
//                }
//            });
//        }
//    };



    /**
     * 主动方停止组呼的消息
     */
    private ReceiveCeaseGroupCallConformationHander receiveCeaseGroupCallConformationHander = new ReceiveCeaseGroupCallConformationHander() {
        @Override
        public void handler(final int resultCode, String resultDesc) {
            logger.info("主动方停止组呼的消息ReceiveCeaseGroupCallConformationHander");
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (MyTerminalFactory.getSDK().getGroupCallManager().getCurrentCallMode() == CallMode.GENERAL_CALL_MODE) {
                        if (MyApplication.instance.getGroupListenenState() == GroupCallListenState.LISTENING) {
                            change2Listening();
                        } else {
                            change2Silence();
                        }
                    }
                    setViewEnable(true);
                }
            });
        }
    };

    /**
     * 主动方请求组呼的消息
     */
    private ReceiveRequestGroupCallConformationHandler receiveRequestGroupCallConformationHandler = new ReceiveRequestGroupCallConformationHandler() {

        @Override
        public void handler(int methodResult, String resultDesc) {
            logger.info("PTTViewPager触发了请求组呼的响应methodResult:"+methodResult);
            if (MyTerminalFactory.getSDK().getGroupCallManager().getCurrentCallMode() == CallMode.GENERAL_CALL_MODE) {
                logger.error("isPttPress值为" + MyApplication.instance.isPttPress);
                if (MyApplication.instance.isPttPress) {
                    if (methodResult == 0) {//请求成功，开始组呼
                        myHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                change2Speaking();
                                MyTerminalFactory.getSDK().putParam(Params.CURRENT_SPEAKER, "");
                                setViewEnable(false);
                            }
                        });
                    } else if (methodResult == SignalServerErrorCode.CANT_SPEAK_IN_GROUP.getErrorCode()) {//只听组
                        myHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showToast(context, "当前组是只听组，不能发起组呼");
                            }
                        });
                    } else if (methodResult == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode()) {//请求等待中
                        myHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                change2Waiting();
                            }
                        });
                    } else {//请求失败
                        myHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (MyApplication.instance.getGroupListenenState() != GroupCallListenState.LISTENING) {
                                    change2Silence();
                                } else {
                                    change2Listening();
                                }
                            }
                        });
                    }
                }
            }
        }
    };

    /**
     * 被动方组呼停止
     */
    private ReceiveGroupCallCeasedIndicationHandler receiveGroupCallCeasedIndicationHandler = new ReceiveGroupCallCeasedIndicationHandler() {
        @Override
        public void handler(int reasonCode) {
            logger.info("触发了被动方组呼停止receiveGroupCallCeasedIndicationHandler");
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (MyTerminalFactory.getSDK().getGroupCallManager().getCurrentCallMode() == CallMode.GENERAL_CALL_MODE) {
                        if (MyApplication.instance.getGroupSpeakState() != GroupCallSpeakState.GRANTING && MyApplication.instance.getGroupSpeakState() != GroupCallSpeakState.WAITING && MyApplication.instance.getGroupSpeakState() != GroupCallSpeakState.GRANTED) {
                            change2Silence();
                        }
                    }
                }
            });
        }
    };

    /**
     * 被动方组呼来了
     */
    private ReceiveGroupCallIncommingHandler receiveGroupCallIncommingHandler = new ReceiveGroupCallIncommingHandler() {

        @Override
        public void handler(int memberId, final String memberName, int groupId,
                            String version, CallMode currentCallMode) {

            speakingId = groupId;
            speakingName = memberName;

            logger.info("触发了被动方组呼来了receiveGroupCallIncommingHandler");

            if (currentCallMode == CallMode.GENERAL_CALL_MODE&&MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_LISTEN.name())) {
                myHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.GRANTING
                                || MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.WAITING){
                            change2Waiting();
                        }else if(MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.GRANTED){
                            //什么都不用做
                        }else {
                            change2Listening();
                        }
                    }
                });
            }
        }
    };
    private int speakingId;
    private String speakingName;
    /**
     * 是否禁止组呼
     */
    private ReceiveNotifyMemberChangeHandler receiveNotifyMemberChangeHandler = new ReceiveNotifyMemberChangeHandler() {

        @Override
        public void handler(MemberChangeType memberChangeType) {
            logger.info("触发了receiveNotifyMemberChangeHandler");
            if (memberChangeType == MemberChangeType.MEMBER_ACTIVE_GROUP_CALL) {
                myHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        change2Silence();
                    }
                });

            } else if (memberChangeType == MemberChangeType.MEMBER_PROHIBIT_GROUP_CALL) {
                myHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        change2Forbid();
                    }
                });
            }
        }
    };

    /**
     * 设置音量键为ptt键时的监听
     */
    private final class OnPTTVolumeBtnStatusChangedListenerImp
            implements BaseActivity.OnPTTVolumeBtnStatusChangedListener {
        @Override
        public void onPTTVolumeBtnStatusChange(GroupCallSpeakState groupCallSpeakState) {
            if (groupCallSpeakState == IDLE) {
                pttDownDoThing();
            } else {
                pttUpDoThing();
            }
        }
    }

    /**
     * 网络连接状态
     */
    private ReceiveServerConnectionEstablishedHandler receiveServerConnectionEstablishedHandler = new ReceiveServerConnectionEstablishedHandler() {

        @Override
        public void handler(final boolean connected) {
            logger.info("pttViewPager收到服务是否连接的通知------>" + connected);
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    stateView();
                }
            });
        }
    };

    /**
     * 点击切换到历史记录
     */
    private final class OnClickListenerImplementationHistoryVoice implements
            View.OnClickListener {
        @Override
        public void onClick(View v) {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    view_pager.setCurrentItem(2);
                }
            });
        }
    }

    /**
     * 点击切换到在线成员
     */
    private final class OnClickListenerImplementationOnlineMember implements
            View.OnClickListener {
        @Override
        public void onClick(View v) {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    view_pager.setCurrentItem(0);
                }
            });
        }
    }
    //    Handler mHandler;
//    RoundProgressBarWidthNumber groupCallTimeProgress;
    private final class OnTouchListenerImplementation implements
            View.OnTouchListener {
        private float pushX;
        private float pushY;
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    logger.info("ACTION_DOWN，ptt按钮按下，开始组呼："+MyApplication.instance.folatWindowPress+MyApplication.instance.volumePress);
                    if(!MyApplication.instance.folatWindowPress && !MyApplication.instance.volumePress){
                        pttDownDoThing();
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(event.getX() + v.getWidth()/4 < 0 || event.getX() - v.getWidth()*1.25 > 0 ||
                            event.getY() + v.getHeight()/8 < 0 || event.getY() - v.getHeight()*1.125 > 0){
                        logger.info("ACTION_MOVE，ptt按钮移动，停止组呼："+MyApplication.instance.isPttPress);

                        if(MyApplication.instance.isPttPress){
                            pttUpDoThing();
                            canScroll = false;
                        }
                    }
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    canScroll = true;
                    logger.info("ACTION_UP，ACTION_CANCEL，ptt按钮抬起，停止组呼："+MyApplication.instance.isPttPress);
//                    mHandler.removeMessages(1);
//                    groupCallTimeProgress.setProgress(0);
                    if(MyApplication.instance.isPttPress){
                        pttUpDoThing();
                    }
                    break;

                default:
                    break;
            }
            return true;
        }
    }

    private void pttUpDoThing() {
        logger.info("pttUpDoThing执行了 isPttPress："+MyApplication.instance.isPttPress);
//        talkback_change_session.setEnabled(true);
        if (MyApplication.instance.isPttPress){
            MyApplication.instance.isPttPress = false;
            canScroll = true;
            if (MyApplication.instance.getGroupListenenState() == GroupCallListenState.LISTENING) {
                change2Listening();
            } else {
                change2Silence();
            }
            MyTerminalFactory.getSDK().getGroupCallManager().ceaseGroupCall();
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCallingCannotClickHandler.class, false);

        }
    }

    private void pttDownDoThing() {
        logger.info("pttDownDoThing执行了 isPttPress："+MyApplication.instance.isPttPress);
//        talkback_change_session.setEnabled(false);
        if (!CheckMyPermission.selfPermissionGranted((NewMainActivity) context, Manifest.permission.RECORD_AUDIO)) {//没有录音权限
            CheckMyPermission.permissionPrompt((NewMainActivity) context, Manifest.permission.RECORD_AUDIO);
            return;
        }

        int resultCode = MyTerminalFactory.getSDK().getGroupCallManager().requestGroupCall("");
        if (resultCode == BaseCommonCode.SUCCESS_CODE){//允许组呼了
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCallingCannotClickHandler.class, true);
            MyApplication.instance.isPttPress = true;
            canScroll = false;
            change2PreSpeaking();
        }else if(resultCode == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode()){
            change2Waiting();
        }else {//组呼失败的提示
            ToastUtil.groupCallFailToast(context, resultCode);
        }

    }




    @Override
    public boolean canScroll() {
        return canScroll;
    }

    public PTTViewPager(Context context, TalkbackFragment takebackFragment, LazyViewPager view_pager) {
        super(context, null);
        this.takebackFragment = takebackFragment;
        this.view_pager = view_pager;
    }
    @Bind(R.id.vp_ptt_rl)
    RelativeLayout talkback_change_session;
    @Bind(R.id.ptt)
    Button ptt;
    @Bind(R.id.tv_ptt)
    TextView tv_ptt;
//    @Bind(R.id.talkback_change_session)
//    ImageView talkback_change_session;

    private TalkbackFragment takebackFragment;
    public static Handler myHandler = new Handler();
    private Logger logger = Logger.getLogger(getClass());
    private boolean canScroll = true;
    private LazyViewPager view_pager;
    private GroupCallSpeakState currentPttStatus = GroupCallSpeakState.IDLE;

    public void setViewEnable (boolean isEanble) {
        talkback_change_session.setEnabled(isEanble);
    }

    @Override
    public View initView() {
        View view = View.inflate(context, R.layout.view_ptt, null);
        ButterKnife.bind(this, view);

        stateView();
        //电源键监听
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        MyApplication.instance.registerReceiver(mBatInfoReceiver, filter);
        return view;
    }

    private void stateView() {
        switch (TerminalFactory.getSDK().getGroupCallManager().getGroupCallSpeakStateMachine().getCurrentState()) {
            case IDLE:
                change2Silence();
                break;
            case GRANTING:
                change2PreSpeaking();
                break;
            case GRANTED:
                change2Speaking();
                break;
            case WAITING:
                change2Waiting();
                break;
            default:
                break;
        }
        switch (TerminalFactory.getSDK().getGroupCallManager().getGroupCallListenStateMachine().getCurrentState()) {
            case IDLE:
                change2Silence();
                break;
            case LISTENING:
                change2Listening();
                break;
            default:
                break;
        }
    }

    @Override
    public void initListener() {
        ptt.setOnTouchListener(new OnTouchListenerImplementation());
        talkback_change_session.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                GroupCallNewsActivity.startCurrentActivity(context, MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0),
                        DataUtil.getGroupByGroupNo(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)).name, speakingId, speakingName);
            }
        });
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyMemberChangeHandler);

        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveRequestGroupCallConformationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveCeaseGroupCallConformationHander);

        MyTerminalFactory.getSDK().registReceiveHandler(receiveServerConnectionEstablishedHandler );

        MyTerminalFactory.getSDK().registReceiveHandler(receivePTTDownHandler );
        MyTerminalFactory.getSDK().registReceiveHandler(receivePTTUpHandler );

        ((BaseActivity) context).setOnPTTVolumeBtnStatusChangedListener(new OnPTTVolumeBtnStatusChangedListenerImp());
    }

    @Override
    public void doDestroy() {
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyMemberChangeHandler);

        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveRequestGroupCallConformationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveCeaseGroupCallConformationHander);

        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveServerConnectionEstablishedHandler );

        MyTerminalFactory.getSDK().unregistReceiveHandler(receivePTTDownHandler );
        MyTerminalFactory.getSDK().unregistReceiveHandler(receivePTTUpHandler );

        try {
            MyApplication.instance.unregisterReceiver(mBatInfoReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initData() {
        // TODO Auto-generated method stub
    }


    private void change2Silence() {
        currentPttStatus = GroupCallSpeakState.IDLE;
        allViewDefault();
        tv_ptt.setText("按住 说话");
        tv_ptt.setTextColor(Color.parseColor("#a9a9a9"));
        ptt.setBackgroundResource(R.drawable.ptt_silence1);
        ptt.setEnabled(true);
    }

    private void change2Waiting() {
        currentPttStatus = GroupCallSpeakState.WAITING;
        allViewDefault();

        ptt.setBackgroundResource(R.drawable.ptt_pre_speaking);
        tv_ptt.setText("准备说话");
        tv_ptt.setTextColor(Color.parseColor("#fdb852"));
        ptt.setEnabled(true);
    }

    private void change2PreSpeaking() {
        currentPttStatus = GroupCallSpeakState.GRANTING;
        allViewDefault();

        ptt.setBackgroundResource(R.drawable.ptt_pre_speaking);
        tv_ptt.setText("准备说话");
        tv_ptt.setTextColor(Color.parseColor("#fdb852"));

        ptt.setEnabled(true);
    }

    private void change2Speaking() {
        currentPttStatus = GroupCallSpeakState.GRANTED;
        allViewDefault();

        ptt.setBackgroundResource(R.drawable.ptt_speaking);
        tv_ptt.setText("松开结束");
        tv_ptt.setTextColor(Color.parseColor("#a9a9a9"));
        logger.info("主界面，ptt被禁 ？  isClickVolumeToCall："+MyApplication.instance.isClickVolumeToCall);
        ptt.setEnabled(!MyApplication.instance.isClickVolumeToCall);
    }

    @Override
    public void setOnTouchListener(View.OnTouchListener l) {

        super.setOnTouchListener(l);
    }

    private void change2Listening() {
        allViewDefault();
        tv_ptt.setText("按住 排队");
        tv_ptt.setTextColor(Color.parseColor("#a9a9a9"));
        ptt.setBackgroundResource(R.drawable.ptt_listening3);
        logger.info("主界面，ptt被禁了  isPttPress："+MyApplication.instance.isPttPress);
        if(MyApplication.instance.isPttPress){
            pttUpDoThing();
        }
    }

    private void change2Forbid() {//禁止组呼，不是遥毙
        allViewDefault();

        tv_ptt.setText("按住 排队");
        tv_ptt.setTextColor(Color.parseColor("#a9a9a9"));
        ptt.setBackgroundResource(R.drawable.ptt_listening3);
        logger.info("主界面，ptt被禁了  isPttPress："+MyApplication.instance.isPttPress);
        ptt.setEnabled(false);
        if(MyApplication.instance.isPttPress){
            pttUpDoThing();
        }
    }

    private void allViewDefault() {
//        talk_time.stop();
//        press_to_talk.setVisibility(View.GONE);
//        pre_to_talk.setVisibility(View.GONE);
//        other_is_talking.setVisibility(View.GONE);
//        wait_in_line.setVisibility(View.GONE);
//        ll_speaking_time.setVisibility(View.GONE);
//        not_listen_and_speak.setVisibility(View.GONE);
    }

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if(Intent.ACTION_SCREEN_OFF.equals(action)) {
                logger.info("Intent.ACTION_SCREEN_OFF，ptt按钮抬起，停止组呼："+MyApplication.instance.isPttPress);
                if(MyApplication.instance.isPttPress){
                    canScroll = true;
                    pttUpDoThing();
                }
            }
        }
    };
}

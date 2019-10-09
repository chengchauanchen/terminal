package cn.vsx.vc.activity;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.TextViewCompat;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import org.apache.http.util.TextUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.TimerTask;

import cn.vsx.SpecificSDK.SpecificSDK;
import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.CallMode;
import cn.vsx.hamster.common.MemberChangeType;
import cn.vsx.hamster.common.ResponseGroupType;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.SignalServerErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallState;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCallingCannotClickHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCeaseGroupCallConformationHander;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveChangeGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveForceChangeGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallCeasedIndicationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyMemberChangeHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRequestGroupCallConformationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseChangeTempGroupProcessingStateHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseGroupActiveHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUnreadMessageAdd1Handler;
import cn.vsx.hamster.terminalsdk.tools.GroupUtils;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.service.LockScreenService;
import cn.vsx.vc.utils.CommonGroupUtil;
import cn.vsx.vc.utils.SystemUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.audio.CheckMyPermission;
import ptt.terminalsdk.tools.ToastUtil;

import static cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState.LISTENING;

/**
 * Created by Administrator on 2017/3/16 0016.
 */

public class GroupCallCenterActivity extends BaseActivity
//        ,NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback
{

    /**
     * 60s倒计时
     */
    @SuppressWarnings("HandlerLeak")
    private Handler myHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
//            if (msg.what == 1) {
//                timeProgress--;
//                if (timeProgress <= 0) {
//                    myHandler.removeMessages(1);
//                    pttUpDoThing();
//                } else {
//                    talkback_time_progress.setText(String.valueOf(timeProgress));
//                    myHandler.sendEmptyMessageDelayed(1, 1000);
//                }
//            }
        }
    };

    /**
     * 主动方停止组呼的消息
     */
    private ReceiveCeaseGroupCallConformationHander receiveCeaseGroupCallConformationHander = new ReceiveCeaseGroupCallConformationHander() {
        @Override
        public void handler(int resultCode, String resultDesc) {
            logger.info("主动方停止组呼的消息ReceiveCeaseGroupCallConformationHander" + "/" + MyApplication.instance.getGroupListenenState());
            myHandler.post(() -> {
                if (MyTerminalFactory.getSDK().getGroupCallManager().getCurrentCallMode() == CallMode.GENERAL_CALL_MODE) {
                    if (MyApplication.instance.getGroupListenenState() == GroupCallListenState.LISTENING) {
                        isScanGroupCall = false;
                        change2Listening();
                    } else {
                        //如果是停止组呼
                        MyApplication.instance.isPttPress = false;
                        myHandler.removeMessages(1);
                        timeProgress = 60;
                        change2Silence();
                    }
//                    int currentGroupId = TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);
//                    setCurrentGroupScanView(currentGroupId);
//                    tx_ptt_group_name.setText("");
                }
//                setViewEnable(true);
            });
        }
    };

    private ReceiveResponseGroupActiveHandler receiveResponseGroupActiveHandler = new ReceiveResponseGroupActiveHandler() {
        @Override
        public void handler(boolean isActive, int responseGroupId) {
            //如果时间到了，还在响应组会话界面，将PTT禁止
            Group groupByGroupNo = TerminalFactory.getSDK().getGroupByGroupNo(responseGroupId);
            if (TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0) == responseGroupId && !isActive &&
                    !groupByGroupNo.isHighUser()) {
                myHandler.post(() -> change2Forbid());
            }
        }
    };

    private ReceiveUnreadMessageAdd1Handler receiveUnreadMessageAdd1Handler = isAdd -> {

    };
    private boolean soundOff;


    /**
     * 主动方请求组呼的消息
     */
    private ReceiveRequestGroupCallConformationHandler receiveRequestGroupCallConformationHandler = new ReceiveRequestGroupCallConformationHandler() {
        @Override
        public void handler(final int methodResult, final String resultDesc, int groupId) {
            logger.info("主动方请求组呼的消息：" + methodResult + "-------" + resultDesc);
            logger.info("主动方请求组呼的消息：" + MyTerminalFactory.getSDK().getGroupCallManager().getCurrentCallMode());

//            speechSynthesizer.pause();//停止读出组名

            if (MyTerminalFactory.getSDK().getGroupCallManager().getCurrentCallMode() == CallMode.GENERAL_CALL_MODE) {

                if (methodResult == BaseCommonCode.SUCCESS_CODE) {//请求成功，开始组呼
                    myHandler.post(() -> {
                        myHandler.removeMessages(1);
                        timeProgress = 60;
//                        talkback_time_progress.setText(String.valueOf(timeProgress));
                        myHandler.sendEmptyMessageDelayed(1, 1000);
//                        if (PhoneAdapter.isF25()) {
//                            allViewDefault();
//                            TextViewCompat.setTextAppearance(tv_speak_text_me, R.style.red);
//                        }
//                        if (MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.GRANTED) {
//                            ll_pre_speaking.setVisibility(View.GONE);
//                        }
                        change2Speaking();
                        MyTerminalFactory.getSDK().putParam(Params.CURRENT_SPEAKER, "");
//                        setViewEnable(false);
//                        setCurrentGroupScanView(groupId);
//                        tx_ptt_group_name.setText(DataUtil.getGroupName(groupId));
                    });
                } else if (methodResult == SignalServerErrorCode.RESPONSE_GROUP_IS_DISABLED.getErrorCode()) {//响应组为禁用状态，低级用户无法组呼

                    ToastUtil.showToast(GroupCallCenterActivity.this, resultDesc);
                    int currentGroupId = TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);
                    Group groupByGroupNo = TerminalFactory.getSDK().getGroupByGroupNo(currentGroupId);
                    if (!groupByGroupNo.isHighUser()) {
                        myHandler.post(() -> change2Forbid());
                    } else {
                        myHandler.post(() -> change2Silence());
                    }
                } else if (methodResult == SignalServerErrorCode.CANT_SPEAK_IN_GROUP.getErrorCode()) {//只听组
                    myHandler.post(() -> {
                        ToastUtil.showToast(GroupCallCenterActivity.this, getString(R.string.cannot_talk));
                        change2Silence();
                    });
                } else if (methodResult == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode()) {//请求等待中
                    myHandler.post(() -> change2Waiting());
                } else {
                    myHandler.post(() -> {
                        if (MyApplication.instance.getGroupListenenState() != LISTENING) {
                            change2Silence();
                        } else {
                            isScanGroupCall = false;
                            change2Listening();
                        }
                    });
                }
            } else {
                ToastUtil.toast( GroupCallCenterActivity.this, resultDesc);
                myHandler.post(() -> {
                    if (MyApplication.instance.getGroupListenenState() != GroupCallListenState.LISTENING) {
                        change2Silence();
                    } else {
                        change2Listening();
                    }
                });

            }

        }
    };

    /**
     * 强制切组
     */
    private ReceiveForceChangeGroupHandler receiveForceChangeGroupHandler = new ReceiveForceChangeGroupHandler() {

        @Override
        public void handler(int memberId, int toGroupId, boolean forceSwitchGroup, String tempGroupType) {
            if (!forceSwitchGroup) {
                return;
            }
            logger.info("TalkbackFragment收到强制切组： toGroupId：" + toGroupId);
            myHandler.post(() -> {
//                setCurrentGroupView();
                if ((!MyTerminalFactory.getSDK().getParam(Params.GROUP_SCAN, false) && !MyTerminalFactory.getSDK().getParam(Params.GUARD_MAIN_GROUP, false)) || MyApplication.instance.getGroupListenenState() != LISTENING) {
                    change2Silence();
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
            myHandler.post(() -> {
//                groupScanId = 0;
                MyTerminalFactory.getSDK().putParam(Params.CURRENT_SPEAKER, "");
//                setCurrentGroupView();
                logger.info("被动方停止组呼" + "/" + MyApplication.instance.getGroupListenenState() + MyApplication.instance.isPttPress);
                if (MyTerminalFactory.getSDK().getGroupCallManager().getCurrentCallMode() == CallMode.GENERAL_CALL_MODE) {
                    if (MyApplication.instance.isPttPress && MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.IDLE) {
                        //别人停止组呼，如果自己还是在按着，重新请求组呼
                        change2Speaking();
                    } else {
                        change2Silence();
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
        public void handler(int memberId, String memberName, int groupId, String groupName, CallMode currentCallMode, long uniqueNo) {
            logger.info("触发了被动方组呼来了receiveGroupCallIncommingHandler:" + "curreneCallMode " + currentCallMode + "-----" + MyApplication.instance.getGroupSpeakState());
            if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_LISTEN.name())) {
                ToastUtil.showToast(GroupCallCenterActivity.this, getString(R.string.text_has_no_group_call_listener_authority));
            }
            myHandler.post(() -> {
                //是组扫描的组呼,且当前组没人说话，变文件夹和组名字
                if (groupId != MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)) {
                    isScanGroupCall = true;
//                    groupScanId = groupId;
//                    setCurrentGroupScanView(groupId, groupName);
                }
                //是当前组的组呼,且扫描组有人说话，变文件夹和组名字
                if (groupId == MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0) && MyApplication.instance.getGroupListenenState() == LISTENING) {
                    isScanGroupCall = false;
//                    setCurrentGroupScanView(groupId, groupName);
                }
                MyTerminalFactory.getSDK().putParam(Params.CURRENT_SPEAKER, memberName);
            });

            speakingId = groupId;
            speakingName = memberName;

            if (currentCallMode == CallMode.GENERAL_CALL_MODE) {
                myHandler.post(() -> {
                    if (MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.GRANTING || MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.WAITING) {
                        change2Waiting();
                    } else if (MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.GRANTED) {
                        //什么都不用做
                    } else {
                        change2Listening();
                    }
                });
            }
        }
    };
    private int speakingId;
    private String speakingName;


    public void setPttCurrentState(GroupCallSpeakState groupCallSpeakState) {
        change2CurrentPttStatus(groupCallSpeakState);
    }

    private synchronized void change2CurrentPttStatus(GroupCallSpeakState groupCallSpeakState) {
        if (MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_TALK.name())) {
            if (groupCallSpeakState == GroupCallSpeakState.IDLE) {
                change2Silence();
            } else if (groupCallSpeakState == GroupCallSpeakState.GRANTING) {
                change2PreSpeaking();
            } else if (groupCallSpeakState == GroupCallSpeakState.GRANTED) {
                change2Speaking();
//                setCurrentGroupView();
            } else if (groupCallSpeakState == GroupCallSpeakState.WAITING) {
                isScanGroupCall = false;
                change2Listening();//等待状态时，上方仍然显示某人说话的文字
            }
        }

    }


    private final class OnPttTouchListenerImplementation implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    MyApplication.instance.isPttPress = true;
                    logger.error("ACTION_DOWN，ptt按钮按下，开始组呼：" + MyApplication.instance.folatWindowPress + MyApplication.instance.volumePress);
                    if (!MyApplication.instance.folatWindowPress && !MyApplication.instance.volumePress) {
                        pttDownDoThing(true);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.performClick();
                    MyApplication.instance.isPttPress = false;
                    logger.error("ACTION_UP，ACTION_CANCEL，ptt按钮抬起，停止组呼：" + MyApplication.instance.isPttPress);
                    pttUpDoThing();
                    break;
                default:
                    break;
            }
            return false;
        }
    }

    /**
     * 成员信息改变
     */
    private ReceiveNotifyMemberChangeHandler receiveNotifyMemberChangeHandler = new ReceiveNotifyMemberChangeHandler() {

        @Override
        public void handler(MemberChangeType memberChangeType) {
            logger.info("触发了receiveNotifyMemberChangeHandler：" + memberChangeType);
//            online_number = MyTerminalFactory.getSDK().getConfigManager().getCurrentGroupMembers().size();
            if (memberChangeType == MemberChangeType.MEMBER_ACTIVE_GROUP_CALL) {
                myHandler.post(() -> change2Silence());

            } else if (memberChangeType == MemberChangeType.MEMBER_PROHIBIT_GROUP_CALL) {
                myHandler.post(() -> change2Forbid());
            }
//            myHandler.post(() -> tv_current_online.setText(String.format(getResources().getString(R.string.current_group_members), online_number)));


        }
    };

    /**
     * 转组消息
     */
    private ReceiveChangeGroupHandler receiveChangeGroupHandler = new ReceiveChangeGroupHandler() {

        @Override
        public void handler(final int errorCode, final String errorDesc) {
            logger.info("转组消息：" + "errorCode:" + errorCode + "/" + errorDesc);
            if (errorCode != 0 && errorCode != SignalServerErrorCode.INVALID_SWITCH_GROUP.getErrorCode()) {
                myHandler.post(() -> {
                    if (errorCode == SignalServerErrorCode.RESOURCES_NOT_ENOUGH.getErrorCode()) {
                        ToastUtil.showToast(MyApplication.instance, getString(R.string.text_no_radio_resources_available));
                    } else if (errorCode == SignalServerErrorCode.TEMP_GROUP_LOCKED.getErrorCode()) {
                        ToastUtil.showToast(MyApplication.instance, SignalServerErrorCode.TEMP_GROUP_LOCKED.getErrorDiscribe());
                    } else {
                        ToastUtil.showToast(MyApplication.instance, errorDesc + "");
                    }
                });

            } else {
                int currentGroup = MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);
                CommonGroupUtil.setCatchGroupIdList(currentGroup);

                myHandler.post(() -> {
//                    setCurrentGroupView();
                    setPttText();
                });
            }
        }
    };

    // 警情临时组处理完成，终端需要切到主组，刷新通讯录
    private ReceiveResponseChangeTempGroupProcessingStateHandler receiveResponseChangeTempGroupProcessingStateHandler = (resultCode, resultDesc) -> {
        if (resultCode == BaseCommonCode.SUCCESS_CODE) {
            int mainGroupId = MyTerminalFactory.getSDK().getParam(Params.MAIN_GROUP_ID, 0);
            MyTerminalFactory.getSDK().getGroupManager().changeGroup(mainGroupId);
        }
    };

    /**=====================================================================================================Listener================================================================================================================================**/

    Button ptt;



    private Logger logger = Logger.getLogger(getClass());
    private TimerTask timerTaskLock;

    protected boolean onRecordAudioDenied;
    protected boolean onLocationDenied;
    protected boolean onCameraDenied;

    protected boolean isScanGroupCall = false;

    private int timeProgress;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_group_call_center;
    }

    @Override
    public void initView() {
        //电源键监听
        ptt = findViewById(R.id.ptt);

//        if (MyTerminalFactory.getSDK().isRegisted()) {
//        setCurrentGroupView();
//        } else {
//            waitAndFinish();
//        }

        if (MyApplication.instance.getGroupListenenState() == LISTENING) {
            change2Listening();
        }
    }

    @Override
    public void initListener() {
        MyTerminalFactory.getSDK().registReceiveHandler(receiveChangeGroupHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveForceChangeGroupHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveCeaseGroupCallConformationHander);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseGroupActiveHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveRequestGroupCallConformationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyMemberChangeHandler);
        ptt.setOnTouchListener(new OnPttTouchListenerImplementation());
        MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseChangeTempGroupProcessingStateHandler);
    }


    @Override
    protected void onStart() {
        super.onStart();
        try {
            //文件
            File file = new File(Environment.getExternalStorageDirectory()
                    + File.separator + MyApplication.instance.getApplicationInfo()
                    .loadLabel(MyApplication.instance.getPackageManager()) + File.separator + "logs"
                    + File.separator + "log.txt");
            if (!file.exists()) {
                SpecificSDK.getInstance().configLogger();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (MyApplication.instance.isPttPress && !TerminalFactory.getSDK().isExit()) {
            MyTerminalFactory.getSDK().getGroupCallManager().ceaseGroupCall();
        }
        MyApplication.instance.isPttPress = false;
    }

    @Override
    public void initData() {
        logger.info("NewMainActivity---initData");

        //开启服务，开启锁屏界面
        startService(new Intent(GroupCallCenterActivity.this, LockScreenService.class));
//        SpecificSDK.initVoipSpecificSDK();

//        MyTerminalFactory.getSDK().getVideoProxy().setActivity(this);
        judgePermission();
        setPttText();
//        if(!FloatWindowManager.getInstance().checkPermission(this)){
//            FloatWindowManager.getInstance().applyPermission(this);
//        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        setPttText();
        if (MyApplication.instance.getGroupSpeakState() != GroupCallSpeakState.GRANTED) {
//            ll_groupCall_prompt.setVisibility(View.GONE);
//            ICTV_groupCall_time.stop();
        }

        //清楚所有通知
        NotificationManager notificationManager=(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

//        if (mNfcAdapter != null) {
//            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
//        }
    }

    //PTT按下以后
    private void pttDownDoThing(boolean currentGroup) {
        logger.info("ptt.pttDownDoThing执行了 isPttPress：" + MyApplication.instance.isPttPress);

        if (!CheckMyPermission.selfPermissionGranted(this, Manifest.permission.RECORD_AUDIO)) {//没有录音权限
            CheckMyPermission.permissionPrompt( this, Manifest.permission.RECORD_AUDIO);
            return;
        }
        //没有组呼权限
        if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_TALK.name())) {
            ToastUtil.showToast(this, getString(R.string.text_has_no_group_call_authority));
            return;
        }
        //半双工个呼中在别的组不能组呼、全双工个呼中不能组呼
        if (MyApplication.instance.getIndividualState() != IndividualCallState.IDLE) {

        }
        int resultCode;
        if(currentGroup){
            resultCode = MyTerminalFactory.getSDK().getGroupCallManager().requestCurrentGroupCall("");
        }else {
            int lastGroupId = TerminalFactory.getSDK().getParam(Params.SECOND_GROUP_ID, 0);
            if(lastGroupId != 0){
                resultCode = MyTerminalFactory.getSDK().getGroupCallManager().requestGroupCall("",lastGroupId);
            }else {
                resultCode = MyTerminalFactory.getSDK().getGroupCallManager().requestCurrentGroupCall("");
            }
        }
        logger.info("PTT按下以后resultCode:" + resultCode);
        if (resultCode == BaseCommonCode.SUCCESS_CODE) {//允许组呼了
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCallingCannotClickHandler.class, true);
            change2PreSpeaking();
        } else if (resultCode == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode()) {
            change2Waiting();
        } else {//组呼失败的提示
            ToastUtil.groupCallFailToast(this, resultCode);
        }

    }

    //PTT抬起以后
    private void pttUpDoThing() {
        logger.info("ptt.pttUpDoThing执行了 isPttPress：" + MyApplication.instance.isPttPress);
        MyTerminalFactory.getSDK().getAudioProxy().volumeCancelQuiet();
        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCallingCannotClickHandler.class, false);
        if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_TALK.name())) {
            return;
        }

        if (MyApplication.instance.getGroupListenenState() == GroupCallListenState.LISTENING) {
            isScanGroupCall = false;
            change2Listening();
        } else {
            change2Silence();

        }
        MyTerminalFactory.getSDK().getGroupCallManager().ceaseGroupCall();
    }



    /**
     * 听
     */
    private void change2Listening() {
//        layoutDefault();
//        allViewDefault();
//        MyTerminalFactory.getSDK().getAudioProxy().volumeCancelQuiet();
        String speakMemberName = MyTerminalFactory.getSDK().getParam(Params.CURRENT_SPEAKER, "");
        if (!TextUtils.isEmpty(speakMemberName)) {
            //设置说话人名字,在组呼来的handler中设置
//            ll_listening.setVisibility(View.VISIBLE);
//            incomming_call_current_speaker.setText(speakMemberName);
        }
//        tv_current_online.setText(String.format(getResources().getString(R.string.current_group_members), online_number));

        if (isScanGroupCall) {
            if (GroupUtils.currentIsForbid()) {
                //如果当前组是禁呼的，不需要改变PPT的样式
                return;
            }
            logger.info("扫描组在组呼");
            ptt.setText(R.string.press_blank_space_talk_text);
//            TextViewCompat.setTextAppearance(ptt, R.style.pttSilenceText);
            ptt.setBackgroundResource(R.drawable.bg_group_call_1);
        } else {
            ptt.setText(R.string.button_press_to_line_up);
            TextViewCompat.setTextAppearance(ptt, R.style.pttWaitingText);
            ptt.setBackgroundResource(R.drawable.bg_group_call_4);
            logger.info("主界面，ptt被禁了  isPttPress：" + MyApplication.instance.isPttPress);
        }
    }

    /**
     * Silence 沉默、无声状态
     */
    private void change2Silence() {
        if (MyApplication.instance.getGroupListenenState() == LISTENING) {
            return;
        }
//        online_number = MyTerminalFactory.getSDK().getConfigManager().getCurrentGroupMembers().size();
//        layoutDefault();
//        ll_show_area.setVisibility(View.VISIBLE);
//        allViewDefault();
//        tv_current_online.setText(String.format(getResources().getString(R.string.current_group_members), online_number));
//        change_group_show_area.setVisibility(View.VISIBLE);
//        talkback_time_progress.setVisibility(View.GONE);
        if (!GroupUtils.currentIsForbid()) {
//            ll_silence.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(MyTerminalFactory.getSDK().getParam(Params.CURRENT_SPEAKER))) {
//                ll_listening.setVisibility(View.GONE);
//                incomming_call_current_speaker.setText(MyTerminalFactory.getSDK().getParam(Params.CURRENT_SPEAKER, ""));
            }
            //只有当前组不是禁呼的才恢复PPT的状态
            ptt.setText(R.string.press_blank_space_talk_text);
            TextViewCompat.setTextAppearance(ptt, R.style.pttwWhiteText);
            ptt.setBackgroundResource(R.drawable.bg_group_call_1);
            ptt.setEnabled(true);
//        talkback_add_icon.setEnabled(true);
        }
    }

    /**
     * 禁止组呼
     */
    private void change2Forbid() {
        logger.info("ptt.change2Forbid()按住排队");
//        layoutDefault();
//        ll_show_area.setVisibility(View.VISIBLE);
//        allViewDefault();
//        ll_forbid.setVisibility(View.VISIBLE);
//        tv_current_online.setText(String.format(getResources().getString(R.string.current_group_members), online_number));
        ptt.setText(R.string.text_no_group_calls);
        TextViewCompat.setTextAppearance(ptt, R.style.pttWaitingText);
        ptt.setBackgroundResource(R.drawable.bg_group_call_4);
        logger.info("主界面，ptt被禁了  isPttPress：" + MyApplication.instance.isPttPress);
        ptt.setEnabled(false);
        if (MyApplication.instance.isPttPress) {
            pttUpDoThing();
        }
    }


    /**
     * 开始说话
     */
    private void change2Speaking() {
//        talkback_add_icon.setEnabled(false);
        logger.info("change2Speaking");
//        allViewDefault();
//        ll_speaking.setVisibility(View.VISIBLE);
//        tv_current_online.setText(String.format(getResources().getString(R.string.current_group_members), online_number));
//        change_group_show_area.setVisibility(View.GONE);
//        talkback_time_progress.setVisibility(View.VISIBLE);
        ptt.setBackgroundResource(R.drawable.bg_group_call_3);
        ptt.setText(R.string.button_release_end);
        TextViewCompat.setTextAppearance(ptt, R.style.pttSpeakingText);
        logger.info("主界面，ptt被禁 ？  isClickVolumeToCall：" + MyApplication.instance.isClickVolumeToCall);
        ptt.setEnabled(!MyApplication.instance.isClickVolumeToCall);
        if (!MyTerminalFactory.getSDK().getAudioProxy().isSpeakerphoneOn()) {
            MyTerminalFactory.getSDK().getAudioProxy().setSpeakerphoneOn(true);
        }
    }


    /**
     * 准备说话
     */
    private void change2PreSpeaking() {
        logger.info("ptt.change2PreSpeaking()准备说话");
        if (MyApplication.instance.getGroupListenenState() == LISTENING) {

            return;
        }
//        layoutDefault();
//        ll_show_area.setVisibility(View.VISIBLE);
//        tv_current_online.setText(String.format(getResources().getString(R.string.current_group_members), online_number));
//        allViewDefault();
//        if (MyApplication.instance.getGroupListenenState() != LISTENING) {
//            ll_pre_speaking.setVisibility(View.VISIBLE);
//        } else {
//            ll_pre_speaking.setVisibility(View.GONE);
//        }

        ptt.setBackgroundResource(R.drawable.bg_group_call_2);
        ptt.setText(R.string.text_ready_to_speak);
        TextViewCompat.setTextAppearance(ptt, R.style.pttSilenceText);
        ptt.setEnabled(true);
    }

    /**
     * 等待
     */
    private void change2Waiting() {
//        layoutDefault();
//        ll_show_area.setVisibility(View.VISIBLE);
//        allViewDefault();
//        ll_waiting.setVisibility(View.VISIBLE);
//        tv_current_online.setText(String.format(getResources().getString(R.string.current_group_members), online_number));
        logger.info("ptt.change2Waiting准备说话");
//        ll_pre_speaking.setVisibility(View.VISIBLE);
        ptt.setBackgroundResource(R.drawable.bg_group_call_2);
        ptt.setText(R.string.text_ready_to_speak);
        TextViewCompat.setTextAppearance(ptt, R.style.pttSilenceText);
        ptt.setEnabled(true);
    }

    private void setPttText() {
        int currentGroupId = TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);
        Group groupByGroupNo = TerminalFactory.getSDK().getGroupByGroupNo(currentGroupId);
        //响应组  普通用户  不在响应状态
        if (ResponseGroupType.RESPONSE_TRUE.toString().equals(groupByGroupNo.getResponseGroupType()) &&
                !groupByGroupNo.isHighUser() &&
                !TerminalFactory.getSDK().getGroupCallManager().getActiveResponseGroup().contains(currentGroupId)) {
            change2Forbid();
        } else if (MyApplication.instance.getGroupListenenState() != GroupCallListenState.IDLE) {
            change2Listening();
        } else if (MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.GRANTING) {
            change2PreSpeaking();
        } else if (MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.WAITING) {
            change2Waiting();
        } else if (MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.GRANTED) {
            change2Speaking();
        } else {
            change2Silence();
        }
    }



    @Override
    protected void onStop() {
        super.onStop();
    }

    public static final int OVERLAY_PERMISSION_REQ_CODE = 1234;
    public static final int REQUEST_INSTALL_PACKAGES_CODE = 1235;
    public static final int GET_UNKNOWN_APP_SOURCES = 1236;
    public static final int REQUEST_CODE_SCAN = 1237;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CheckMyPermission.REQUEST_RECORD_AUDIO:
                onRecordAudioDenied = true;
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    judgePermission();
                }else {
                    permissionDenied(requestCode);
                }
                break;
            case CheckMyPermission.REQUEST_CAMERA:
                onCameraDenied = true;
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    judgePermission();
                }else {
                    permissionDenied(requestCode);
                }
                break;
            case CheckMyPermission.REQUEST_LOCATION:
                onLocationDenied = true;
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    judgePermission();
                }else {
                    permissionDenied(requestCode);
                }
                break;
            case REQUEST_INSTALL_PACKAGES_CODE:
                if(grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                    startActivityForResult(intent, GET_UNKNOWN_APP_SOURCES);
                }
            default:
                break;

        }
    }

    private void permissionDenied(int requestCode){
        if(requestCode == CheckMyPermission.REQUEST_RECORD_AUDIO){
            cn.vsx.vc.utils.ToastUtil.showToast(this, getString(R.string.text_audio_frequency_is_not_open_audio_is_not_used));
        }else if(requestCode == CheckMyPermission.REQUEST_CAMERA){
            cn.vsx.vc.utils.ToastUtil.showToast(this, getString(R.string.text_camera_not_open_audio_is_not_used));
        }else if(requestCode == CheckMyPermission.REQUEST_LOCATION){
            cn.vsx.vc.utils.ToastUtil.showToast(this, getString(R.string.text_location_not_open_locat_is_not_used));
        }
        //        judgePermission();
    }

    /**
     * 必须要有录音和相机的权限，APP才能去视频页面
     */
    protected void judgePermission() {

        //6.0以下判断相机权限
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M  ){
            if(!SystemUtil.cameraIsCanUse()){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CheckMyPermission.REQUEST_CAMERA);
            }
        }else {
            if (CheckMyPermission.selfPermissionGranted(this, Manifest.permission.RECORD_AUDIO)){
                if(CheckMyPermission.selfPermissionGranted(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                    if(!CheckMyPermission.selfPermissionGranted(this,Manifest.permission.CAMERA)){
                        CheckMyPermission.permissionPrompt(this, Manifest.permission.CAMERA);
                    }
                    //                    else {
                    //                        CheckMyPermission.permissionPrompt(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    //                    }
                }else {
                    CheckMyPermission.permissionPrompt(this, Manifest.permission.ACCESS_FINE_LOCATION);
                }
            }else {
                //如果权限被拒绝，申请下一个权限
                if(onRecordAudioDenied){
                    if(CheckMyPermission.selfPermissionGranted(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                        if(!CheckMyPermission.selfPermissionGranted(this,Manifest.permission.CAMERA)){
                            CheckMyPermission.permissionPrompt(this, Manifest.permission.CAMERA);
                        }
                    }else {
                        if(onLocationDenied){
                            if(!CheckMyPermission.selfPermissionGranted(this,Manifest.permission.CAMERA)){
                                if(!onCameraDenied){
                                    CheckMyPermission.permissionPrompt(this, Manifest.permission.CAMERA);
                                }
                            }
                        }else {
                            CheckMyPermission.permissionPrompt(this, Manifest.permission.ACCESS_FINE_LOCATION);
                        }
                    }
                }else{
                    CheckMyPermission.permissionPrompt(this, Manifest.permission.RECORD_AUDIO);
                }
            }
        }

    }


    @Override
    public void doOtherDestroy() {
        logger.info("NewMainActivity----doOtherDestroy");
        //退到后台
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveChangeGroupHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveForceChangeGroupHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveRequestGroupCallConformationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveCeaseGroupCallConformationHander);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyMemberChangeHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseGroupActiveHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseChangeTempGroupProcessingStateHandler);

        //外置摄像头是否连接通知
        myHandler.removeCallbacksAndMessages(null);
        PromptManager.getInstance().stopRing();

        stopService(new Intent(GroupCallCenterActivity.this, LockScreenService.class));
        MyApplication.instance.stopHandlerService();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                    exit();
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //记录当前的checkedId
    }


    public void exit(){
            // 判断是否点了一次后退
            if (MyApplication.instance.getIndividualState() != IndividualCallState.SPEAKING) {
                // 在2秒之内点击第二次
                moveTaskToBack(true);//把程序变成后台的
                // finish完成之后当前进程依然在
            }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyTerminalFactory.getSDK().unregistNetworkChangeHandler();
    }
}

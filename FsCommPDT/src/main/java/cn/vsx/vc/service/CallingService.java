package cn.vsx.vc.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.CallMode;
import cn.vsx.hamster.common.IndividualCallType;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.SignalServerErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallState;
import cn.vsx.hamster.terminalsdk.receiveHandler.IndividualCallPttStatusHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCallingCannotClickHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCeaseGroupCallConformationHander;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallCeasedIndicationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyIndividualCallStoppedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRequestGroupCallConformationHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.receiveHandle.ReceiveStopCallingServiceHandler;
import cn.vsx.vc.receiveHandle.ReceiveStopStartReceiveCallServiceHandler;
import cn.vsx.vc.receiveHandle.ReceiverCloseKeyBoardHandler;
import cn.vsx.vc.utils.BitmapUtil;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.HandleIdUtil;
import cn.vsx.vc.utils.SensorUtil;
import cn.vsx.vc.view.IndividualCallTimerView;
import cn.vsx.vc.view.IndividualCallView;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.audio.CheckMyPermission;
import ptt.terminalsdk.receiveHandler.ReceiveHeadSetPlugHandler;
import ptt.terminalsdk.tools.ToastUtil;

import static cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState.LISTENING;
import static cn.vsx.vc.receive.Actions.KILL_ACT_CALL;

/**
 * 个呼通话界面，包括全双工和半双工
 */
public class CallingService extends BaseService{

    //全双工还是半双工
    private int individualCallType;
    //请求界面头像
//    private ImageView mIvMemberPortraitRequest;
    //收到请求界面头像
//    private ImageView mIvMemberPortraitChooice;
    private TextView mTvSpeakingToast;
    private ImageView mLlIndividualCallRetractSpeaking;
    //全双工对讲界面头像
    //    private ImageView mIvMemberPortraitSpeaking;
    private TextView mTvMemberNameSpeaking;
    private TextView mTvMemberIdSpeaking;
    private TextView mTvSpeakingPrompt;
    private IndividualCallView mIctvSpeakingTimeSpeaking;
    private ImageView mIvIndividualCallHangupSpeaking;
    private ImageView mIvIndividualCallMicroMute;
    private ImageView mIvIndividualCallHandFree;
    private TextView mTvWaiting;
    private IndividualCallTimerView mPopupICTVSpeakingTime;
    private LinearLayout mIndividualCallSpeaking;
    private RelativeLayout mPopMinimize;
    private RelativeLayout mIndividualCallHalfDuplex;
    private ImageView mIvIndividualCallRetractHalfDuplex;
    private TextView mTvMemberNameHalfDuplex;
    private TextView mTvMemberIdHalfDuplex;
    private ImageView mIvIndividualCallHangupHalfDuplex;
    private TextView mTvHalfDuplexPrompt;
    private IndividualCallView mIctvHalfDuplexTimeSpeaking;
    private Button mBtnIndividualCallHalfDuplexPtt;

    //网络状态的布局
    private LinearLayout  ll_individual_call_network_state;
    private LinearLayout  ll_individual_call_half_duplex_network_state;

   private float downX = 0;
    private float downY = 0;
    private int oddOffsetX = 0;
    private int oddOffsetY = 0;

    private static final int AUTOHANGUP = 0;
    //半双工个呼临时组id
//    private int tempGroupId;

    public CallingService(){}

    @SuppressLint("InflateParams")
    @Override
    protected void setRootView(){
        rootView = LayoutInflater.from(MyTerminalFactory.getSDK().application).inflate(R.layout.layout_individual_call, null);
    }

    @Override
    protected void initWindow() {
        super.initWindow();
        //如果屏幕宽度小于高度就开启横屏
        layoutParams1.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    }

    @Override
    protected void findView(){
        //正在通话
        mIndividualCallSpeaking = rootView.findViewById(R.id.individual_call_speaking);
        mLlIndividualCallRetractSpeaking = rootView.findViewById(R.id.ll_individual_call_retract_speaking);
        mTvSpeakingToast = rootView.findViewById(R.id.tv_speaking_toast);
        mTvMemberNameSpeaking = rootView.findViewById(R.id.tv_member_name_speaking);
        mTvMemberIdSpeaking = rootView.findViewById(R.id.tv_member_id_speaking);
        mTvSpeakingPrompt = rootView.findViewById(R.id.tv_speaking_prompt);
        mIctvSpeakingTimeSpeaking = rootView.findViewById(R.id.ictv_speaking_time_speaking);
        mIvIndividualCallHangupSpeaking = rootView.findViewById(R.id.iv_individual_call_hangup_speaking);
        mIvIndividualCallMicroMute = rootView.findViewById(R.id.iv_individual_call_micro_mute);
        mIvIndividualCallHandFree = rootView.findViewById(R.id.iv_individual_call_hand_free);
        //半双工
        mIndividualCallHalfDuplex = rootView.findViewById(R.id.individual_call_half_duplex);
        mIvIndividualCallRetractHalfDuplex = rootView.findViewById(R.id.iv_individual_call_retract_half_duplex);
        mTvMemberNameHalfDuplex = rootView.findViewById(R.id.tv_member_name_half_duplex);
        mTvMemberIdHalfDuplex = rootView.findViewById(R.id.tv_member_id_half_duplex);
        mIvIndividualCallHangupHalfDuplex = rootView.findViewById(R.id.iv_individual_call_hangup_half_duplex);
        mTvHalfDuplexPrompt = rootView.findViewById(R.id.tv_half_duplex_prompt);
        mIctvHalfDuplexTimeSpeaking = rootView.findViewById(R.id.ictv_half_duplex_time_speaking);
        mBtnIndividualCallHalfDuplexPtt = rootView.findViewById(R.id.btn_individual_call_half_duplex_ptt);
        //最小化
        mPopMinimize = rootView.findViewById(R.id.pop_minimize);
        mTvWaiting = rootView.findViewById(R.id.tv_waiting);
        mPopupICTVSpeakingTime = rootView.findViewById(R.id.popup_ICTV_speaking_time);

        //头像
        ImageView ivMemberPortraitSpeaking = rootView.findViewById(R.id.iv_member_portrait_speaking);
        ivMemberPortraitSpeaking.setImageResource(BitmapUtil.getUserPhoto());
        ImageView ivMemberPortraitChooice = rootView.findViewById(R.id.iv_member_portrait_half_duplex);
        ivMemberPortraitChooice.setImageResource(BitmapUtil.getUserPhoto());

        //网络状态的布局
        ll_individual_call_network_state = rootView.findViewById(R.id.ll_individual_call_network_state);
        ll_individual_call_half_duplex_network_state = rootView.findViewById(R.id.ll_individual_call_half_duplex_network_state);
        ll_individual_call_network_state.setVisibility(View.GONE);
        ll_individual_call_half_duplex_network_state.setVisibility(View.GONE);
    }

    @Override
    protected void initData(){
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initListener(){
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(individualCallPttStatusHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveCeaseGroupCallConformationHander);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveRequestGroupCallConformationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyIndividualCallStoppedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveStopCallingServiceHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveHeadSetPlugHandler);
        mLlIndividualCallRetractSpeaking.setOnClickListener(retractListener);
        mIvIndividualCallRetractHalfDuplex.setOnClickListener(retractListener);
        mIvIndividualCallHangupSpeaking.setOnClickListener(stopCallListener);
        mIvIndividualCallMicroMute.setOnClickListener(microMuteListener);
        mIvIndividualCallHandFree.setOnClickListener(handFreeListener);
        mIvIndividualCallHangupHalfDuplex.setOnClickListener(stopCallListener);
        mBtnIndividualCallHalfDuplexPtt.setOnTouchListener(halfCallPTTOnTouchListener);
        mPopMinimize.setOnTouchListener(miniPopOnTouchListener);

        //发送通知关闭StartIndividualCallService和ReceiveCallComingService
        TerminalFactory.getSDK().notifyReceiveHandler(ReceiveStopStartReceiveCallServiceHandler.class);
    }

    @Override
    protected void initBroadCastReceiver(){
        IntentFilter mReceivFilter = new IntentFilter();
        mReceivFilter.addAction(KILL_ACT_CALL);
        registerReceiver(mBroadcastReceiv, mReceivFilter);
    }

    @Override
    protected void handleMesage(Message msg){
        switch(msg.what){
            case AUTOHANGUP:
                mHandler.removeMessages(AUTOHANGUP);
                logger.error("执行了半双工超时机制；挂断个呼！！！");
                individualCallStopped();
                break;
            case OFF_LINE:
                //发送通知关闭StartIndividualCallService和ReceiveCallComingService
//                TerminalFactory.getSDK().notifyReceiveHandler(ReceiveStopStartReceiveCallServiceHandler.class);
                mHandler.removeMessages(OFF_LINE);
                stopBusiness();
                break;
        }
    }

    @Override
    protected void onNetworkChanged(boolean connected){
        if(!connected){
            if(!mHandler.hasMessages(OFF_LINE)){
                mPopupICTVSpeakingTime.onPause();
                mIctvSpeakingTimeSpeaking.onPause();
                mIctvHalfDuplexTimeSpeaking.onPause();
                mHandler.sendEmptyMessageDelayed(OFF_LINE,30000);
            }
        }else {
            mHandler.removeMessages(OFF_LINE);
            mPopupICTVSpeakingTime.onContinue();
            mIctvSpeakingTimeSpeaking.onContinue();
            mIctvHalfDuplexTimeSpeaking.onContinue();
        }
        if(ll_individual_call_network_state!=null){
            ll_individual_call_network_state.setVisibility((!connected)?View.VISIBLE:View.GONE);
        }
        if(ll_individual_call_half_duplex_network_state!=null){
            ll_individual_call_half_duplex_network_state.setVisibility((!connected)?View.VISIBLE:View.GONE);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        setMicrophoneMute(mIvIndividualCallMicroMute,false);
        //打开默认听筒说话，
        setSpeakPhoneOn(mIvIndividualCallHandFree,false);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(individualCallPttStatusHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveCeaseGroupCallConformationHander);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveRequestGroupCallConformationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyIndividualCallStoppedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveStopCallingServiceHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveHeadSetPlugHandler);
        unregisterReceiver(mBroadcastReceiv);
    }

    private View.OnClickListener retractListener = v-> retract();

    private View.OnClickListener stopCallListener = v -> stopCall();

    private View.OnClickListener microMuteListener = v -> {
        boolean isMicrophoneMute = MyTerminalFactory.getSDK().getAudioProxy().isMicrophoneMute();
        setMicrophoneMute(mIvIndividualCallMicroMute,!isMicrophoneMute);
        //设置UI
    };

    private View.OnClickListener handFreeListener = v -> {
        if(MyApplication.instance.headset){
            ToastUtil.showToast(this,getString(R.string.text_head_set_can_not_hand_free));
            //设置为耳机模式
            setSpeakPhoneOn(mIvIndividualCallHandFree,false);
        }else{
            boolean isSpeakerphoneOn = MyTerminalFactory.getSDK().getAudioProxy().isSpeakerphoneOn();
            setSpeakPhoneOn(mIvIndividualCallHandFree,!isSpeakerphoneOn);
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private View.OnTouchListener halfCallPTTOnTouchListener = (v, motionEvent) -> {
        switch(motionEvent.getAction()){
            case MotionEvent.ACTION_DOWN:
                logger.info("PTT按下了，开始说话");
                halfPttDownDothing();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                logger.info("PTT松开了，结束说话");
                halfPttUpDothing();
                break;
            default:
                break;
        }
        return true;
    };

    @SuppressLint("ClickableViewAccessibility")
    private View.OnTouchListener miniPopOnTouchListener = (v, event) -> {
        //触摸点到边界屏幕的距离
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                //触摸点到自身边界的距离
                downX = event.getX();
                downY = event.getY();
                oddOffsetX = layoutParams.x;
                oddOffsetY = layoutParams.y;
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();
                //不除以3，拖动的view抖动的有点厉害
                if(Math.abs(downX - moveX) > 5 || Math.abs(downY - moveY) > 5){
                    // 更新浮动窗口位置参数
                    layoutParams.x = (int) (screenWidth - (x + downX));
                    layoutParams.y = (int) (y - downY);
                    windowManager.updateViewLayout(rootView, layoutParams);
                }
                break;
            case MotionEvent.ACTION_UP:
                int newOffsetX = layoutParams.x;
                int newOffsetY = layoutParams.y;
                if (Math.abs(newOffsetX - oddOffsetX) <=30 && Math.abs(newOffsetY - oddOffsetY) <=30){
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverCloseKeyBoardHandler.class);
                    windowManager.removeView(rootView);
                    windowManager.addView(rootView, layoutParams1);
                    MyApplication.instance.isMiniLive = false;
                    hideAllView();
                    if(individualCallType == IndividualCallType.FULL_DUPLEX.getCode()){
                        mIndividualCallSpeaking.setVisibility(View.VISIBLE);
                        //全双工时，才注册检测感应器
                        SensorUtil.getInstance().registSensor();
                    }else if(individualCallType == IndividualCallType.HALF_DUPLEX.getCode()){
                        mIndividualCallHalfDuplex.setVisibility(View.VISIBLE);
                    }
                }
                break;
        }
        return true;
    };

    private ReceiveGroupCallIncommingHandler receiveGroupCallIncommingHandler = (memberId, memberName, groupId, groupName, currentCallMode,uniqueNo) -> {
        //如果在半双工个呼中来组呼，就是对方在说话
        Log.e("IndividualCallService", "收到组呼：callType:" + individualCallType);
        if(individualCallType == IndividualCallType.HALF_DUPLEX.getCode()){
            mHandler.post(() -> {
                logger.info("半双工个呼时来组呼，对方正在说话");
                mBtnIndividualCallHalfDuplexPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_gray);
                mTvHalfDuplexPrompt.setText(getResources().getString(R.string.other_speaking));
                mTvHalfDuplexPrompt.setTextColor(Color.YELLOW);
                mBtnIndividualCallHalfDuplexPtt.setEnabled(false);
                cancelAutoHangUpTimer();//对方按下开始说话，取消时间检测
            });
        }
    };

    //ptt个呼等待
    private IndividualCallPttStatusHandler individualCallPttStatusHandler = (pttIsDown, outerMemberId) -> {
        logger.info("PTT个呼等待" + "pttIsDown:" + pttIsDown);
        mHandler.post(() -> {
            if(outerMemberId != TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)){
                if(pttIsDown){
                    mTvHalfDuplexPrompt.setText(getResources().getString(R.string.other_speaking));
                    mTvHalfDuplexPrompt.setTextColor(Color.YELLOW);
                    mBtnIndividualCallHalfDuplexPtt.setBackgroundResource(R.drawable.ptt_individual_call_wait);
                    mBtnIndividualCallHalfDuplexPtt.setEnabled(false);
                    ptt.terminalsdk.manager.Prompt.PromptManager.getInstance().playPromptCalled();
                    cancelAutoHangUpTimer();//对方按下开始说话，取消时间检测
                }else{
                    mTvHalfDuplexPrompt.setText(getResources().getString(R.string.press_talk));
                    mTvHalfDuplexPrompt.setTextColor(Color.WHITE);
                    mBtnIndividualCallHalfDuplexPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_dodgerblue2);
                    mBtnIndividualCallHalfDuplexPtt.setEnabled(true);
                    startAutoHangUpTimer();//对方抬起，启动时间检测机制
                }
            }
        });
    };

    //主动方停止组呼
    private ReceiveCeaseGroupCallConformationHander receiveCeaseGroupCallConformationHander = (resultCode, resultDesc) -> mHandler.post(() -> {
        if(individualCallType == IndividualCallType.HALF_DUPLEX.getCode()){
            mTvHalfDuplexPrompt.setText(getResources().getString(R.string.press_talk));
            mTvHalfDuplexPrompt.setTextColor(Color.WHITE);
            mBtnIndividualCallHalfDuplexPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_dodgerblue2);
            mBtnIndividualCallHalfDuplexPtt.setEnabled(true);
            logger.info("当前个呼状态：" + MyApplication.instance.getIndividualState());
            if(MyApplication.instance.getIndividualState() == IndividualCallState.SPEAKING){
                //只有在半双工个呼接通了才发送超时检测
                startAutoHangUpTimer();//对方抬起，启动时间检测机制
            }
        }
    });

    private ReceiveGroupCallCeasedIndicationHandler receiveGroupCallCeasedIndicationHandler = (reasonCode) -> {
        logger.info("收到组呼停止");
        mHandler.post(() -> {
            if(individualCallType == IndividualCallType.HALF_DUPLEX.getCode()){
                mTvHalfDuplexPrompt.setText(getResources().getString(R.string.press_talk));
                mTvHalfDuplexPrompt.setTextColor(Color.WHITE);
                mBtnIndividualCallHalfDuplexPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_dodgerblue2);
                mBtnIndividualCallHalfDuplexPtt.setEnabled(true);
                if(MyApplication.instance.getIndividualState() == IndividualCallState.SPEAKING){
                    startAutoHangUpTimer();//对方抬起，启动时间检测机制
                }
            }
        });
    };

    /**
     * 主动方请求组呼的消息
     */
    private ReceiveRequestGroupCallConformationHandler receiveRequestGroupCallConformationHandler = (methodResult, resultDesc,groupId) -> {
        if(MyTerminalFactory.getSDK().getGroupCallManager().getCurrentCallMode() == CallMode.GENERAL_CALL_MODE && MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_TALK.name())){
//            if(MyApplication.instance.isPttPress){
                mHandler.post(() -> {
                    if(individualCallType == IndividualCallType.HALF_DUPLEX.getCode()){
                        cancelAutoHangUpTimer();
                        if(methodResult == 0){
                            mTvHalfDuplexPrompt.setText(getResources().getString(R.string.i_speaking));
                            mBtnIndividualCallHalfDuplexPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_speaking);
                        }else if(methodResult == SignalServerErrorCode.CANT_SPEAK_IN_GROUP.getErrorCode()){
                            ToastUtil.showToast(CallingService.this, getResources().getString(R.string.cannot_talk));
                            mTvHalfDuplexPrompt.setText(getResources().getString(R.string.press_talk));
                            mBtnIndividualCallHalfDuplexPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_dodgerblue2);
                        }else if(methodResult == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode()){
                            mTvHalfDuplexPrompt.setText(getResources().getString(R.string.press_talk));
                            mBtnIndividualCallHalfDuplexPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_yellow);
                        }else{
                            mBtnIndividualCallHalfDuplexPtt.setText(getResources().getString(R.string.press_talk));
                            if(MyApplication.instance.getGroupListenenState() != LISTENING){
                                mBtnIndividualCallHalfDuplexPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_dodgerblue2);
                            }else{
                                mBtnIndividualCallHalfDuplexPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_gray);
                            }
                        }
                    }
                });
//            }
        }
    };

    /**
     * 被动方通知个呼停止，界面---------静默状态
     */
    private ReceiveNotifyIndividualCallStoppedHandler receiveNotifyIndividualCallStoppedHandler = (methodResult, resultDesc) -> mHandler.post(() -> {
        if(SignalServerErrorCode.getInstanceByCode(methodResult) != null){
            mTvSpeakingToast.setText(resultDesc);
            ToastUtil.showToast(MyTerminalFactory.getSDK().application, resultDesc);
        }else{
            mTvSpeakingToast.setText(getResources().getString(R.string.other_stop_call));
            ToastUtil.showToast(MyTerminalFactory.getSDK().application, getResources().getString(R.string.other_stop_call));
        }
        individualCallStopped();
    });

    /**
     * 通知关闭CallingService
     */
    private ReceiveStopCallingServiceHandler receiveStopCallingServiceHandler = () -> mHandler.postDelayed(this::individualCallStopped,500);

    /**
     * 设置是否可以打开扬声器
     */
    private ReceiveHeadSetPlugHandler receiveHeadSetPlugHandler = new ReceiveHeadSetPlugHandler(){
        @Override
        public void handler(boolean headset){
            mHandler.post(()-> {
                if(headset){
                    //设置为耳机模式
                    setSpeakPhoneOn(mIvIndividualCallHandFree,false);
                }
            });
        }
    };


    private void stopCall(){

        MyTerminalFactory.getSDK().getIndividualCallManager().ceaseIndividualCall();
        mPopupICTVSpeakingTime.onStop();
        mIctvHalfDuplexTimeSpeaking.onStop();
        mIctvSpeakingTimeSpeaking.onStop();
        PromptManager.getInstance().stopRing();
        hideAllView();
        stopBusiness();
    }

    private void retract(){
//        //发送通知关闭StartIndividualCallService和ReceiveCallComingService
//        TerminalFactory.getSDK().notifyReceiveHandler(ReceiveStopStartReceiveCallServiceHandler.class);
        windowManager.removeView(rootView);
        windowManager.addView(rootView, layoutParams);
        hideAllView();
        showPopMiniView();
    }

    private void hideAllView(){
        mIndividualCallSpeaking.setVisibility(View.GONE);
        mIndividualCallHalfDuplex.setVisibility(View.GONE);
        mPopMinimize.setVisibility(View.GONE);
    }

    private void individualCallStopped(){
        mPopupICTVSpeakingTime.onStop();
        if(individualCallType == IndividualCallType.FULL_DUPLEX.getCode()){
            mIctvSpeakingTimeSpeaking.onStop();
            mTvSpeakingPrompt.setText(getResources().getString(R.string.stop_talk));
        }else if(individualCallType == IndividualCallType.HALF_DUPLEX.getCode()){
            mIctvHalfDuplexTimeSpeaking.onStop();
            mTvHalfDuplexPrompt.setText(getResources().getString(R.string.stop_talk));
        }

        //发送通知关闭StartIndividualCallService和ReceiveCallComingService
//        TerminalFactory.getSDK().notifyReceiveHandler(ReceiveStopStartReceiveCallServiceHandler.class);
        PromptManager.getInstance().IndividualHangUpRing();
        PromptManager.getInstance().delayedStopRing();
        cancelAutoHangUpTimer();
        stopBusiness();
    }

    private synchronized void startAutoHangUpTimer(){
        if(MyApplication.instance.getIndividualState() == IndividualCallState.SPEAKING || MyApplication.instance.getIndividualState() == IndividualCallState.RINGING){
            logger.info("启动了半双工超时检测机制；10秒后将自动挂断！！！");
            mHandler.removeMessages(AUTOHANGUP);
            mHandler.sendEmptyMessageDelayed(AUTOHANGUP, 10000);
        }
    }


    private synchronized void cancelAutoHangUpTimer(){
        logger.info("取消半双工超时检测");
        if(mHandler.hasMessages(AUTOHANGUP)){
            mHandler.removeMessages(AUTOHANGUP);
        }
    }

    @Override
    protected void showPopMiniView(){
        SensorUtil.getInstance().unregistSensor();
        windowManager.removeView(rootView);
        windowManager.addView(rootView,layoutParams);
        hideAllView();
        mPopMinimize.setVisibility(View.VISIBLE);
        mPopupICTVSpeakingTime.setVisibility(View.VISIBLE);
        mTvWaiting.setVisibility(View.GONE);
        MyApplication.instance.isMiniLive = true;
    }

    @Override
    protected void initView(Intent intent){
        SensorUtil.getInstance().unregistSensor();
        individualCallType = intent.getIntExtra(Constants.CALL_TYPE, 0);
        String memberName = intent.getStringExtra(Constants.MEMBER_NAME);
        int memberId = intent.getIntExtra(Constants.MEMBER_ID, 0);
        hideAllView();
        recoverSpeakingPop();
        mTvWaiting.setVisibility(View.GONE);
        MyTerminalFactory.getSDK().getIndividualCallManager().responseIndividualCall(true);
        PromptManager.getInstance().stopRing();
        MyApplication.instance.isPrivateCallOrVideoLiveHand = true;
        mPopupICTVSpeakingTime.setVisibility(View.VISIBLE);
        if(individualCallType == IndividualCallType.FULL_DUPLEX.getCode()){
            //全双工时，才注册检测感应器
            SensorUtil.getInstance().registSensor();
            mTvMemberNameSpeaking.setText(memberName);
            mTvMemberIdSpeaking.setText(HandleIdUtil.handleId(memberId));
            mTvSpeakingPrompt.setText(getResources().getString(R.string.talking));
            //打开默认听筒说话，
            setSpeakPhoneOn(mIvIndividualCallHandFree,false);
        }else if(individualCallType == IndividualCallType.HALF_DUPLEX.getCode()){
            mTvMemberNameHalfDuplex.setText(memberName);
            mTvMemberIdHalfDuplex.setText(HandleIdUtil.handleId(memberId));
            mTvHalfDuplexPrompt.setText(getResources().getString(R.string.press_talk));
            mBtnIndividualCallHalfDuplexPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_dodgerblue2);
            mBtnIndividualCallHalfDuplexPtt.setEnabled(true);
            startAutoHangUpTimer();//半双工选择接听，开始超时检测
        }
        setMicrophoneMute(mIvIndividualCallMicroMute,false);
    }

    private void recoverSpeakingPop(){
        mPopupICTVSpeakingTime.onStart();
        if(individualCallType == IndividualCallType.FULL_DUPLEX.getCode()){
            mIctvSpeakingTimeSpeaking.onStart();
            mTvSpeakingPrompt.setText(getResources().getString(R.string.talking));
        }else if(individualCallType == IndividualCallType.HALF_DUPLEX.getCode()){
            mIctvHalfDuplexTimeSpeaking.onStart();
            mTvHalfDuplexPrompt.setText(getResources().getString(R.string.press_talk));
        }
        if(mPopMinimize.getVisibility() == View.VISIBLE){
            mTvWaiting.setVisibility(View.GONE);
            mPopupICTVSpeakingTime.setVisibility(View.VISIBLE);
        }else{
            if(individualCallType == IndividualCallType.FULL_DUPLEX.getCode()){
                mIndividualCallSpeaking.setVisibility(View.VISIBLE);
                mIctvSpeakingTimeSpeaking.setVisibility(View.VISIBLE);
            }else{
                mIndividualCallHalfDuplex.setVisibility(View.VISIBLE);
                mIctvHalfDuplexTimeSpeaking.setVisibility(View.VISIBLE);
            }
        }
    }
    private void halfPttDownDothing(){
        logger.info("pttDownDoThing执行了 isPttPress：" + MyApplication.instance.isPttPress);
        if(!CheckMyPermission.selfPermissionGranted(CallingService.this, Manifest.permission.RECORD_AUDIO)){//没有录音权限
            ToastUtil.showToast(MyTerminalFactory.getSDK().application, getResources().getString(R.string.no_record_perssion_for_call));
            return;
        }
        cancelAutoHangUpTimer();
        int tempGroupId = MyTerminalFactory.getSDK().getIndividualCallManager().getTempGroupId();
        if(tempGroupId!=0){
            int resultCode = MyTerminalFactory.getSDK().getGroupCallManager().requestGroupCall("",tempGroupId);
            if(resultCode == BaseCommonCode.SUCCESS_CODE){//允许组呼了
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCallingCannotClickHandler.class, true);
//                MyApplication.instance.isPttPress = true;
                mTvHalfDuplexPrompt.setText(getResources().getString(R.string.i_pre_speaking));
                mTvHalfDuplexPrompt.setTextColor(Color.YELLOW);
                mBtnIndividualCallHalfDuplexPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_yellow);
            }else if(resultCode == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode()){
                mBtnIndividualCallHalfDuplexPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_yellow);
            }else{//组呼失败的提示
                ToastUtil.groupCallFailToast(CallingService.this, resultCode);
            }
        }else{
            logger.error(getString(R.string.no_get_temporary_group_id));
        }

    }

    private void halfPttUpDothing(){
        MyTerminalFactory.getSDK().getAudioProxy().volumeCancelQuiet();
//        if(MyApplication.instance.isPttPress){
//            MyApplication.instance.isPttPress = false;
            if(MyApplication.instance.getGroupListenenState() == LISTENING){
                mBtnIndividualCallHalfDuplexPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_gray);
            }else if(MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.GRANTED){
                startAutoHangUpTimer();
            }
            MyTerminalFactory.getSDK().getGroupCallManager().ceaseGroupCall();
//        }
//        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCallingCannotClickHandler.class, false);
    }

    private BroadcastReceiver mBroadcastReceiv = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent){
            String action = intent.getAction();
            if(null == action){
                return;
            }
            if(KILL_ACT_CALL.equals(intent.getAction())){
                //发送通知关闭StartIndividualCallService和ReceiveCallComingService
//                TerminalFactory.getSDK().notifyReceiveHandler(ReceiveStopStartReceiveCallServiceHandler.class);
                stopBusiness();
            }
        }
    };
}

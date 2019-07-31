package cn.vsx.vc.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import cn.vsx.hamster.errcode.module.SignalServerErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveAnswerIndividualCallTimeoutHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyIndividualCallStoppedHandler;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.receiveHandle.ReceiveStopCallingServiceHandler;
import cn.vsx.vc.receiveHandle.ReceiveStopStartReceiveCallServiceHandler;
import cn.vsx.vc.receiveHandle.ReceiverCloseKeyBoardHandler;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.HandleIdUtil;
import cn.vsx.vc.utils.InputMethodUtil;
import cn.vsx.vc.utils.SensorUtil;
import cn.vsx.vc.view.IndividualCallTimerView;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2018/12/29
 * 描述：
 * 修订历史：
 */
public class ReceiveCallComingService extends BaseService{

    private ImageView mIndividualCallRetractEmergency;
//    private ImageView mIvMemberPortraitChooice;
    private TextView mTvMemberNameChooice;
    private TextView mTvMemberIdChooice;
    private LinearLayout mLlIndividualCallRefuse;
    private LinearLayout mLlIndividualCallAccept;
    private String type;
    private int callType;
    private String memberName;
    private int memberId;
    private RelativeLayout mRlCallChooice;
    private RelativeLayout mPopMinimize;
    private TextView mTvWaiting;
    private IndividualCallTimerView mTimerView;
    private float downX = 0;
    private float downY = 0;
    private int oddOffsetX = 0;
    private int oddOffsetY = 0;

    @SuppressLint("InflateParams")
    @Override
    protected void setRootView(){
        rootView = LayoutInflater.from(MyTerminalFactory.getSDK().application).inflate(R.layout.layout_receive_call_service, null);
    }

    @Override
    protected void initWindow() {
        super.initWindow();
        //如果屏幕宽度小于高度就开启横屏
        layoutParams1.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    }

    @Override
    protected void findView(){
        mRlCallChooice = rootView.findViewById(R.id.rl_call_chooice);
        mIndividualCallRetractEmergency = rootView.findViewById(R.id.individual_call_retract_emergency);
//        mIvMemberPortraitChooice = rootView.findViewById(R.id.iv_member_portrait_chooice);
        mTvMemberNameChooice = rootView.findViewById(R.id.tv_member_name_chooice);
        mTvMemberIdChooice = rootView.findViewById(R.id.tv_member_id_chooice);
        mLlIndividualCallRefuse = rootView.findViewById(R.id.ll_individual_call_refuse);
        mLlIndividualCallAccept = rootView.findViewById(R.id.ll_individual_call_accept);
        //小窗口
        mPopMinimize = rootView.findViewById(R.id.pop_minimize);
        mTvWaiting = rootView.findViewById(R.id.tv_waiting);
        mTimerView = rootView.findViewById(R.id.popup_ICTV_speaking_time);
    }

    @Override
    protected void initData(){
    }

    @Override
    protected void initBroadCastReceiver(){
    }

    @Override
    protected void handleMesage(Message msg){
        switch(msg.what){
            case OFF_LINE:
                stopBusiness();
            break;
        }
    }

    @Override
    protected void onNetworkChanged(boolean connected){
        if(!connected){
            mTimerView.onPause();
            if(!mHandler.hasMessages(OFF_LINE)){
                mHandler.sendEmptyMessageDelayed(OFF_LINE,OFF_LINE_TIME);
            }
        }else {
            mHandler.removeMessages(OFF_LINE);
            mTimerView.onContinue();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initListener(){
        SensorUtil.getInstance().registSensor();
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyIndividualCallStoppedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveAnswerIndividualCallTimeoutHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveStopStartReceiveCallServiceHandler);
        mLlIndividualCallRefuse.setOnClickListener(refuseCallListener);
        mLlIndividualCallAccept.setOnClickListener(acceptCallListener);
        mIndividualCallRetractEmergency.setOnClickListener(retractOnClickListener);
        mPopMinimize.setOnTouchListener(miniPopOnTouchListener);
    }

    @Override
    protected void showPopMiniView(){
        SensorUtil.getInstance().unregistSensor();
        MyApplication.instance.isMiniLive = true;
        mRlCallChooice.setVisibility(View.GONE);
        mPopMinimize.setVisibility(View.VISIBLE);
        mTvWaiting.setVisibility(View.VISIBLE);
    }

    @Override
    protected void initView(Intent intent){
        type = intent.getStringExtra(Constants.TYPE);
        callType = intent.getIntExtra(Constants.CALL_TYPE, 0);
        memberName = intent.getStringExtra(Constants.MEMBER_NAME);
        memberId = intent.getIntExtra(Constants.MEMBER_ID, 0);
        wakeLock.acquire(10 * 1000);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if(null != imm && InputMethodUtil.inputMethodSate(MyTerminalFactory.getSDK().application)){
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverCloseKeyBoardHandler.class);
        mRlCallChooice.setVisibility(View.VISIBLE);
        mPopMinimize.setVisibility(View.GONE);
        mTvMemberIdChooice.setText(HandleIdUtil.handleId(memberId));
        mTvMemberNameChooice.setText(HandleIdUtil.handleName(memberName));
        PromptManager.getInstance().IndividualCallNotifyRing();
        mTimerView.onStart();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveAnswerIndividualCallTimeoutHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyIndividualCallStoppedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveStopStartReceiveCallServiceHandler);
    }

    /**
     * 被动方收到个呼答复超时，关闭界面
     */
    private ReceiveAnswerIndividualCallTimeoutHandler receiveAnswerIndividualCallTimeoutHandler = () -> mHandler.post(() -> {
        ToastUtil.showToast(MyTerminalFactory.getSDK().application, getResources().getString(R.string.no_answer));
        mHandler.post(this::individualCallStopped);
    });

    /**
     * 被动方通知个呼停止，界面---------静默状态
     */
    private ReceiveNotifyIndividualCallStoppedHandler receiveNotifyIndividualCallStoppedHandler = (methodResult, resultDesc) -> mHandler.post(() -> {
        if(SignalServerErrorCode.getInstanceByCode(methodResult) != null){
            ToastUtil.showToast(ReceiveCallComingService.this, SignalServerErrorCode.getInstanceByCode(methodResult).getErrorDiscribe());
        }else{
            ToastUtil.showToast(ReceiveCallComingService.this, getResources().getString(R.string.other_cancel));
        }
        //发送通知关闭CallingService(防止已经跳转到CallingService)
        TerminalFactory.getSDK().notifyReceiveHandler(ReceiveStopCallingServiceHandler.class);
        mHandler.post(() -> {
                mLlIndividualCallAccept.setEnabled(false);
                mIndividualCallRetractEmergency.setEnabled(false);});
        mHandler.postDelayed(this::individualCallStopped,500);
    });

    /**
     * 通知关闭ReceiveCallingService
     */
    private ReceiveStopStartReceiveCallServiceHandler receiveStopStartReceiveCallServiceHandler = () -> mHandler.post(() -> {
        mHandler.postDelayed(this::removeView,500);
    });

    private View.OnClickListener refuseCallListener = v -> refuseCall();

    private View.OnClickListener acceptCallListener = v -> acceptCall();

    private View.OnClickListener retractOnClickListener = v -> retract();

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
                if(Math.abs(newOffsetX - oddOffsetX) <= 30 && Math.abs(newOffsetY - oddOffsetY) <= 30){
                    layoutParams1.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverCloseKeyBoardHandler.class);
                    windowManager.removeView(rootView);
                    windowManager.addView(rootView, layoutParams1);
                    MyApplication.instance.isMiniLive = false;
                    mRlCallChooice.setVisibility(View.VISIBLE);
                    mPopMinimize.setVisibility(View.GONE);
                    SensorUtil.getInstance().registSensor();
                }
                break;
        }
        return true;
    };

    private void refuseCall(){
        MyTerminalFactory.getSDK().getIndividualCallManager().responseIndividualCall(false);
        MyApplication.instance.isPrivateCallOrVideoLiveHand = true;
        mTimerView.onStop();
        stopBusiness();
    }

    private void acceptCall(){
        Intent intent = new Intent(this, CallingService.class);
        intent.putExtra(Constants.CALL_TYPE, callType);
        intent.putExtra(Constants.TYPE, type);
        intent.putExtra(Constants.MEMBER_NAME, memberName);
        intent.putExtra(Constants.MEMBER_ID, memberId);
        startService(intent);
        mTimerView.onStop();
//        mHandler.postDelayed(this::removeView,500);
    }

    private void retract(){
        windowManager.removeView(rootView);
        windowManager.addView(rootView, layoutParams);
        showPopMiniView();
    }

    private void individualCallStopped(){
        mTimerView.onStop();
        MyTerminalFactory.getSDK().getIndividualCallManager().ceaseIndividualCall();
        PromptManager.getInstance().IndividualHangUpRing();
        PromptManager.getInstance().delayedStopRing();
        stopBusiness();
    }
}

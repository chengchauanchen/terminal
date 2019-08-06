package com.vsxin.terminalpad.mvp.ui.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.MvpFragment;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.app.PadApplication;
import com.vsxin.terminalpad.mvp.contract.presenter.HalfDuplexIndividualCallPresenter;
import com.vsxin.terminalpad.mvp.contract.view.IHalfDuplexIndividualCallView;
import com.vsxin.terminalpad.mvp.ui.widget.IndividualCallView;
import com.vsxin.terminalpad.prompt.PromptManager;
import com.vsxin.terminalpad.utils.Constants;
import com.vsxin.terminalpad.utils.SensorUtil;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.SignalServerErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallState;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCallingCannotClickHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.audio.CheckMyPermission;
import ptt.terminalsdk.tools.ToastUtil;

import static cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState.LISTENING;
import static com.vsxin.terminalpad.utils.StateMachineUtils.revertStateMachine;

/**
 * @author qzw
 * <p>
 * 地图气泡点击-成员详情页
 */
public class HalfDuplexIndividualCallFragment extends MvpFragment<IHalfDuplexIndividualCallView, HalfDuplexIndividualCallPresenter> implements IHalfDuplexIndividualCallView {


    @BindView(R.id.iv_member_portrait_half_duplex)
    ImageView ivMemberPortraitHalfDuplex;
    @BindView(R.id.tv_member_name_half_duplex)
    TextView tvMemberNameHalfDuplex;
    @BindView(R.id.tv_member_id_half_duplex)
    TextView tvMemberIdHalfDuplex;
    @BindView(R.id.iv_individual_call_hangup_half_duplex)
    ImageView ivIndividualCallHangupHalfDuplex;
    @BindView(R.id.tv_half_duplex_prompt)
    TextView mTvHalfDuplexPrompt;
    @BindView(R.id.ictv_half_duplex_time_speaking)
    IndividualCallView mIctvHalfDuplexTimeSpeaking;
    @BindView(R.id.btn_individual_call_half_duplex_ptt)
    Button mBtnIndividualCallHalfDuplexPtt;
    Unbinder unbinder;

    private static final int AUTOHANGUP = 0;
    private static final String HDICFragment_TAG = "halfDuplexIndividualCallFragment";
    private boolean isPress = false;


    @SuppressLint("HandlerLeak")
    protected Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case AUTOHANGUP:
                    mHandler.removeMessages(AUTOHANGUP);
                    getLogger().error("执行了半双工超时机制；挂断个呼！！！");
                    individualCallStopped();
                    break;
//                case OFF_LINE:
//                    mHandler.removeMessages(OFF_LINE);
//                    stopBusiness();
//                    break;
            }
        }
    };

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_half_duplex_individual_call;
    }

    @Override
    protected void initViews(View view) {
        getPresenter().registReceiveHandler();

        String name = getArguments().getString(Constants.MEMBER_NAME);
        String no = getArguments().getString(Constants.MEMBER_ID);
        tvMemberNameHalfDuplex.setText(name);
        tvMemberIdHalfDuplex.setText(no);

        ivIndividualCallHangupHalfDuplex.setOnClickListener(v -> {
            //挂断
            MyTerminalFactory.getSDK().getIndividualCallManager().ceaseIndividualCall();
            mIctvHalfDuplexTimeSpeaking.onStop();
            PromptManager.getInstance().stopRing();
            stopBusiness();
        });
        mBtnIndividualCallHalfDuplexPtt.setOnTouchListener(halfCallPTTOnTouchListener);
//        mBtnIndividualCallHalfDuplexPtt.setOnClickListener(v -> {
//            if(isPress){
//                isPress = false;
//                halfPttUpDothing();
//            }else{
//                isPress = true;
//                halfPttDownDothing();
//            }
//        });
        recoverSpeakingPop();
        MyTerminalFactory.getSDK().getIndividualCallManager().responseIndividualCall(true);
        PromptManager.getInstance().stopRing();
        PadApplication.getPadApplication().isPrivateCallOrVideoLiveHand = true;
        mTvHalfDuplexPrompt.setText(getResources().getString(R.string.press_talk));
        mBtnIndividualCallHalfDuplexPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_dodgerblue2);
        mBtnIndividualCallHalfDuplexPtt.setEnabled(true);
        startAutoHangUpTimer();//半双工选择接听，开始超时检测
    }

    @Override
    protected void initData() {
    }

    @Override
    public HalfDuplexIndividualCallPresenter createPresenter() {
        return new HalfDuplexIndividualCallPresenter(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate a fragment view
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void groupCallOtherSpeaking() {
        mHandler.post(() -> {
            mBtnIndividualCallHalfDuplexPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_gray);
            mTvHalfDuplexPrompt.setText(getResources().getString(R.string.other_speaking));
            mTvHalfDuplexPrompt.setTextColor(Color.YELLOW);
            mBtnIndividualCallHalfDuplexPtt.setEnabled(false);
            cancelAutoHangUpTimer();//对方按下开始说话，取消时间检测
        });
    }

    @Override
    public void individualCallPttStatus(boolean pttIsDown, int outerMemberId) {
        mHandler.post(() -> {
            if (outerMemberId != TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)) {
                if (pttIsDown) {
                    mTvHalfDuplexPrompt.setText(getResources().getString(R.string.other_speaking));
                    mTvHalfDuplexPrompt.setTextColor(Color.YELLOW);
                    mBtnIndividualCallHalfDuplexPtt.setBackgroundResource(R.drawable.ptt_individual_call_wait);
                    mBtnIndividualCallHalfDuplexPtt.setEnabled(false);
                    ptt.terminalsdk.manager.Prompt.PromptManager.getInstance().playPromptCalled();
                    cancelAutoHangUpTimer();//对方按下开始说话，取消时间检测
                } else {
                    mTvHalfDuplexPrompt.setText(getResources().getString(R.string.press_talk));
                    mTvHalfDuplexPrompt.setTextColor(Color.WHITE);
                    mBtnIndividualCallHalfDuplexPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_dodgerblue2);
                    mBtnIndividualCallHalfDuplexPtt.setEnabled(true);
                    startAutoHangUpTimer();//对方抬起，启动时间检测机制
                }
            }
        });
    }

    @Override
    public void ceaseGroupCallConformation(int resultCode, String resultDesc) {
        mHandler.post(() -> {
            mTvHalfDuplexPrompt.setText(getResources().getString(R.string.press_talk));
            mTvHalfDuplexPrompt.setTextColor(Color.WHITE);
            mBtnIndividualCallHalfDuplexPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_dodgerblue2);
            mBtnIndividualCallHalfDuplexPtt.setEnabled(true);
            getLogger().info("当前个呼状态：" + PadApplication.getPadApplication().getIndividualState());
            if (PadApplication.getPadApplication().getIndividualState() == IndividualCallState.SPEAKING) {
                //只有在半双工个呼接通了才发送超时检测
                startAutoHangUpTimer();//对方抬起，启动时间检测机制
            }

        });
    }

    @Override
    public void groupCallCeasedIndication(int reasonCode) {
        mHandler.post(() -> {
            mTvHalfDuplexPrompt.setText(getResources().getString(R.string.press_talk));
            mTvHalfDuplexPrompt.setTextColor(Color.WHITE);
            mBtnIndividualCallHalfDuplexPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_dodgerblue2);
            mBtnIndividualCallHalfDuplexPtt.setEnabled(true);
            if(PadApplication.getPadApplication().getIndividualState() == IndividualCallState.SPEAKING){
                startAutoHangUpTimer();//对方抬起，启动时间检测机制
            }
        });

    }

    @Override
    public void requestGroupCallConformation(int methodResult, String resultDesc, int groupId) {
        mHandler.post(() -> {
            if(methodResult == 0){
                mTvHalfDuplexPrompt.setText(getResources().getString(R.string.i_speaking));
                mBtnIndividualCallHalfDuplexPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_speaking);
            }else if(methodResult == SignalServerErrorCode.CANT_SPEAK_IN_GROUP.getErrorCode()){
                startAutoHangUpTimer();
                ToastUtil.showToast(getContext(), getResources().getString(R.string.cannot_talk));
                mTvHalfDuplexPrompt.setText(getResources().getString(R.string.press_talk));
                startAutoHangUpTimer();
                mBtnIndividualCallHalfDuplexPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_dodgerblue2);
            }else if(methodResult == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode()){
                mTvHalfDuplexPrompt.setText(getResources().getString(R.string.press_talk));
                startAutoHangUpTimer();
                mBtnIndividualCallHalfDuplexPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_yellow);
            }else{
                mBtnIndividualCallHalfDuplexPtt.setText(getResources().getString(R.string.press_talk));
                startAutoHangUpTimer();
                if(PadApplication.getPadApplication().getGroupListenenState() != LISTENING){
                    mBtnIndividualCallHalfDuplexPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_dodgerblue2);
                }else{
                    mBtnIndividualCallHalfDuplexPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_gray);
                }
            }
        });
    }

    @Override
    public void notifyIndividualCallStopped(int methodResult, String resultDesc) {
        mHandler.post(() -> {
            if(SignalServerErrorCode.getInstanceByCode(methodResult) != null){
                ToastUtil.showToast(MyTerminalFactory.getSDK().application, resultDesc);
            }else{
                ToastUtil.showToast(MyTerminalFactory.getSDK().application, getResources().getString(R.string.other_stop_call));
            }
            individualCallStopped();
        });
    }


    @SuppressLint("ClickableViewAccessibility")
    private View.OnTouchListener halfCallPTTOnTouchListener = (v, motionEvent) -> {
        switch(motionEvent.getAction()){
            case MotionEvent.ACTION_DOWN:
                getLogger().info("PTT按下了，开始说话");
                halfPttDownDothing();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                halfPttUpDothing();
                break;
            default:
                break;
        }
        return true;
    };


    private void halfPttDownDothing(){
        getLogger().info("pttDownDoThing执行了 isPttPress：" + PadApplication.getPadApplication().isPttPress);
        if(!CheckMyPermission.selfPermissionGranted(getContext(), Manifest.permission.RECORD_AUDIO)){//没有录音权限
            ToastUtil.showToast(MyTerminalFactory.getSDK().application, getResources().getString(R.string.no_recorde_permisson));
            return;
        }
        cancelAutoHangUpTimer();
        int tempGroupId = MyTerminalFactory.getSDK().getIndividualCallManager().getTempGroupId();
        if(tempGroupId!=0){
            int resultCode = MyTerminalFactory.getSDK().getGroupCallManager().requestGroupCall("",tempGroupId);
            if(resultCode == BaseCommonCode.SUCCESS_CODE){//允许组呼了
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCallingCannotClickHandler.class, true);
                PadApplication.getPadApplication().isPttPress = true;
                mTvHalfDuplexPrompt.setText(getResources().getString(R.string.i_pre_speaking));
                mTvHalfDuplexPrompt.setTextColor(Color.YELLOW);
                mBtnIndividualCallHalfDuplexPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_yellow);
            }else if(resultCode == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode()){
                mBtnIndividualCallHalfDuplexPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_yellow);
            }else{//组呼失败的提示
                ToastUtil.groupCallFailToast(getContext(), resultCode);
            }
        }else{
            getLogger().error(getString(R.string.no_get_temporary_group_id));
        }

    }

    private void halfPttUpDothing(){
        MyTerminalFactory.getSDK().getAudioProxy().volumeCancelQuiet();
        if( PadApplication.getPadApplication().isPttPress){
            getLogger().info("PTT松开了，结束说话");
            PadApplication.getPadApplication().isPttPress = false;
            if( PadApplication.getPadApplication().getGroupListenenState() == LISTENING){
                mBtnIndividualCallHalfDuplexPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_gray);
            }
            if( PadApplication.getPadApplication().getGroupSpeakState() == GroupCallSpeakState.GRANTED){
                startAutoHangUpTimer();
            }
            MyTerminalFactory.getSDK().getGroupCallManager().ceaseGroupCall();
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCallingCannotClickHandler.class, false);
        }
    }

    private void individualCallStopped(){
        if(mIctvHalfDuplexTimeSpeaking!=null){
            mIctvHalfDuplexTimeSpeaking.onStop();
        }
        if(mTvHalfDuplexPrompt!=null){
            mTvHalfDuplexPrompt.setText(getResources().getString(R.string.stop_talk));
        }
        //发送通知关闭StartIndividualCallService和ReceiveCallComingService
//        TerminalFactory.getSDK().notifyReceiveHandler(ReceiveStopStartReceiveCallServiceHandler.class);
        PromptManager.getInstance().IndividualHangUpRing();
        PromptManager.getInstance().delayedStopRing();
        cancelAutoHangUpTimer();
        stopBusiness();
    }

    private void startAutoHangUpTimer() {
        if (PadApplication.getPadApplication().getIndividualState() == IndividualCallState.SPEAKING || PadApplication.getPadApplication().getIndividualState() == IndividualCallState.RINGING) {
            getLogger().info("启动了半双工超时检测机制；10秒后将自动挂断！！！");
            mHandler.removeMessages(AUTOHANGUP);
            mHandler.sendEmptyMessageDelayed(AUTOHANGUP, 10000);
        }

    }

    private void cancelAutoHangUpTimer() {
        getLogger().info("取消半双工超时检测");
        mHandler.removeMessages(AUTOHANGUP);
    }

    /**
     * 退出业务状态
     */
    protected void stopBusiness(){
        PromptManager.getInstance().stopRing();
        SensorUtil.getInstance().unregistSensor();
        revertStateMachine();
        mHandler.post(this::closeFragment);
    }


    private void recoverSpeakingPop(){
        mIctvHalfDuplexTimeSpeaking.onStart();
        mTvHalfDuplexPrompt.setText(getResources().getString(R.string.press_talk));
        mIctvHalfDuplexTimeSpeaking.setVisibility(View.VISIBLE);
    }

    /**
     * 关闭Fragment
     */
    private void closeFragment(){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        Fragment memberInfo = fragmentManager.findFragmentByTag(HDICFragment_TAG);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if(memberInfo!=null){
            fragmentTransaction.remove(memberInfo);
        }
        fragmentTransaction.commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getLogger().info("MemberInfoFragment 销毁了");
        mHandler.removeCallbacksAndMessages(null);
        getPresenter().unregistReceiveHandler();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


}

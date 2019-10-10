package com.vsxin.terminalpad.mvp.contract.presenter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.ixiaoma.xiaomabus.architecture.mvp.BasePresenter;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.app.PadApplication;
import com.vsxin.terminalpad.mvp.contract.view.IFullDuplexIndividualCallView;
import com.vsxin.terminalpad.mvp.contract.view.IHalfDuplexIndividualCallView;
import com.vsxin.terminalpad.prompt.PromptManager;
import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.CallMode;
import cn.vsx.hamster.common.IndividualCallType;
import cn.vsx.hamster.errcode.module.SignalServerErrorCode;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallState;
import cn.vsx.hamster.terminalsdk.receiveHandler.IndividualCallPttStatusHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCeaseGroupCallConformationHander;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallCeasedIndicationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyIndividualCallStoppedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRequestGroupCallConformationHandler;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.receiveHandler.ReceiveHeadSetPlugHandler;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * @author qzw
 *
 * 全双工个呼
 */

public class FullDuplexIndividualCallPresenter extends BasePresenter<IFullDuplexIndividualCallView> {

    protected Handler mHandler = new Handler(Looper.getMainLooper());

    public FullDuplexIndividualCallPresenter(Context mContext) {
        super(mContext);
    }

    /**
     * 设置是否免提
     * @param result
     */
    public void setSpeakPhoneOn(ImageView imageView, boolean result){
        if(result){
            if (!MyTerminalFactory.getSDK().getAudioProxy().isSpeakerphoneOn()) {
                MyTerminalFactory.getSDK().getAudioProxy().setSpeakerphoneOn(result);
            }
            if(imageView!=null){
                imageView.setImageResource(R.drawable.ic_hand_free_2);
            }
        }else{
            if (MyTerminalFactory.getSDK().getAudioProxy().isSpeakerphoneOn()) {
                MyTerminalFactory.getSDK().getAudioProxy().setSpeakerphoneOn(result);
            }
            if(imageView!=null){
                imageView.setImageResource(R.drawable.ic_hand_free_1);
            }
        }
    }

    /**
     * 设置是否静音
     * @param result
     */
    public void setMicrophoneMute(ImageView imageView,boolean result){
        MyTerminalFactory.getSDK().getAudioProxy().setMicrophoneMute(result);
        if(imageView!=null){
            imageView.setImageResource(result?R.drawable.ic_micro_mute_2:R.drawable.ic_micro_mute_1);
        }
    }


    /**
     * 被动方通知个呼停止，界面---------静默状态
     */
    private ReceiveNotifyIndividualCallStoppedHandler receiveNotifyIndividualCallStoppedHandler = (methodResult, resultDesc) -> mHandler.post(() -> {
        if(SignalServerErrorCode.getInstanceByCode(methodResult) != null){
            getView().setSpeakingToast(resultDesc);
            ToastUtil.showToast(getContext(), resultDesc);
        }else{
            getView().setSpeakingToast(getContext().getResources().getString(R.string.other_stop_call));
            ToastUtil.showToast(getContext(), getContext().getResources().getString(R.string.other_stop_call));
        }
        stopIndividualCall();
        getView().closeFragment();
    });

    /**
     * 挂断个呼后
     */
    public void stopIndividualCall() {
        stopPromptSound();
        revertStateMachine();
    }

    /**
     * 关闭提示音
     */
    private void stopPromptSound() {
        PromptManager.getInstance().IndividualHangUpRing();
        PromptManager.getInstance().delayedStopRing();
    }


    /**
     * 重置状态机
     */
    private void revertStateMachine() {
        if (PadApplication.getPadApplication().getIndividualState() != IndividualCallState.IDLE) {
            MyTerminalFactory.getSDK().getIndividualCallManager().ceaseIndividualCall();
        }
    }

    /**
     * 注册监听
     */
    public void registerReceiveHandler() {
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyIndividualCallStoppedHandler);
    }

    /**
     * 取消监听
     */
    public void unRegisterReceiveHandler() {
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyIndividualCallStoppedHandler);
    }

}

package com.vsxin.terminalpad.mvp.contract.presenter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.ixiaoma.xiaomabus.architecture.mvp.BasePresenter;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.app.PadApplication;
import com.vsxin.terminalpad.manager.StartCallManager;
import com.vsxin.terminalpad.mvp.contract.view.IIndividualCallView;
import com.vsxin.terminalpad.prompt.PromptManager;
import com.vsxin.terminalpad.utils.OperateReceiveHandlerUtilSync;
import com.vsxin.terminalpad.utils.SensorUtil;

import cn.vsx.hamster.common.IndividualCallType;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePlayingState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePushingState;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveAnswerIndividualCallTimeoutHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCurrentGroupIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseStartIndividualCallHandler;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * @author qzw
 * <p>
 * 地图气泡点击-发起个呼
 */

public class IndividualCallPresenter extends BasePresenter<IIndividualCallView> {

    protected Handler mHandler = new Handler(Looper.getMainLooper());
    private final StartCallManager startCallManager;

    public IndividualCallPresenter(Context mContext) {
        super(mContext);
        startCallManager = new StartCallManager(mContext);
    }

    public StartCallManager getStartCallManager() {
        return startCallManager;
    }

    //    /**
//     * 主动发起个呼
//     */
//    private ReceiveCurrentGroupIndividualCallHandler receiveCurrentGroupIndividualCallHandler = (member) -> mHandler.post(() -> {
//        getView().getLogger().info("当前呼叫对象:" + member);
//
//        if (PadApplication.getPadApplication().getVideoLivePlayingState() != VideoLivePlayingState.IDLE) {
//            ToastUtil.showToast(getContext(), getContext().getString(R.string.text_watching_can_not_private_call));
//            return;
//        }
//        if (PadApplication.getPadApplication().getVideoLivePushingState() != VideoLivePushingState.IDLE) {
//            ToastUtil.showToast(getContext(), getContext().getString(R.string.text_pushing_can_not_private_call));
//            return;
//        }
//        if (PadApplication.getPadApplication().getIndividualState() != IndividualCallState.IDLE) {
//            ToastUtil.showToast(getContext(), getContext().getString(R.string.text_personal_calling_can_not_do_others));
//            return;
//        }
//        SensorUtil.getInstance().registSensor();
//        PromptManager.getInstance().IndividualCallRequestRing();
//        int resultCode = MyTerminalFactory.getSDK().getIndividualCallManager().requestIndividualCall(member.getId(), member.getUniqueNo(), "");
//        if (resultCode == BaseCommonCode.SUCCESS_CODE) {
//            getView().setMemberinfo();//设置头像名称
//        } else {
//            ToastUtil.individualCallFailToast(getContext(), resultCode);
//            stopIndividualCall();
//        }
//    });

    /**
     * 对方 接听/拒绝
     */
    private ReceiveResponseStartIndividualCallHandler receiveResponseStartIndividualCallHandler = (resultCode, resultDesc, individualCallType) -> {
        getView().getLogger().info("ReceiveResponseStartIndividualCallHandler====" + "resultCode:" + resultCode + "=====resultDesc:" + resultDesc);
        if (resultCode == BaseCommonCode.SUCCESS_CODE) {//对方接听
            getView().getLogger().info("对方接受了你的个呼:" + resultCode + resultDesc + "callType;" + individualCallType);
            mHandler.post(() -> callAnswer(individualCallType));
        } else {//对方拒绝
            ToastUtil.showToast(getContext(), resultDesc);
            mHandler.postDelayed(() -> {
                getView().stopAndDestroy();
            }, 500);
        }
    };

    private void callAnswer(int individualCallType) {
        if (individualCallType == IndividualCallType.HALF_DUPLEX.getCode()) {
            getView().startHalfDuplexIndividualCall();
        } else {
            getView().startFullDuplexIndividualCall();
        }
        //关闭提示音
        startCallManager.stopPromptSound();
    }


    /**
     * 呼叫超时
     */
    private ReceiveAnswerIndividualCallTimeoutHandler receiveAnswerIndividualCallTimeoutHandler = () ->{
        ToastUtil.showToast(getContext(),"呼叫超时");
        getView().getLogger().info("NoticePresenter 呼叫超时");
        mHandler.postDelayed(() -> {
            getView().stopAndDestroy();
        }, 500);
    };

    /**
     * 注册监听
     */
    public void registerReceiveHandler() {
        MyTerminalFactory.getSDK().registReceiveHandler(receiveAnswerIndividualCallTimeoutHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseStartIndividualCallHandler);
        //OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiveCurrentGroupIndividualCallHandler);
    }

    /**
     * 取消监听
     */
    public void unRegisterReceiveHandler() {
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveAnswerIndividualCallTimeoutHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseStartIndividualCallHandler);
        //OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiveCurrentGroupIndividualCallHandler);
    }


}

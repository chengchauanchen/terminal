package com.vsxin.terminalpad.mvp.contract.presenter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.ixiaoma.xiaomabus.architecture.mvp.BasePresenter;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.app.App;
import com.vsxin.terminalpad.app.PadApplication;
import com.vsxin.terminalpad.mvp.contract.view.IPttButton;
import com.vsxin.terminalpad.mvp.ui.widget.SendGroupCallListener;
import com.vsxin.terminalpad.utils.OperateReceiveHandlerUtilSync;

import java.lang.ref.WeakReference;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.CallMode;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.SignalServerErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCallingCannotClickHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCeaseGroupCallConformationHander;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallCeasedIndicationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRequestGroupCallConformationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseGroupActiveHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveStartCeaseGroupCallHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveTalkWillTimeoutHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.audio.CheckMyPermission;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * @author qzw
 * 自定义ptt按钮控件
 * <p>
 * 组呼功能 封装
 */
public class PttButtonPresenter extends BasePresenter<IPttButton> {

    protected Handler mHandler = new Handler(Looper.getMainLooper());

    public PttButtonPresenter(Context mContext) {
        super(mContext);
    }

    private WeakReference<SendGroupCallListener> sendGroupCallListener;

    public void startGroupCall(SendGroupCallListener sendGroupCallListener) {
        this.sendGroupCallListener = new WeakReference<SendGroupCallListener>(sendGroupCallListener);
        // FIXME: 2019/4/8 观看视频上报时发起组呼，是在临时组里
        int tempGroupId = MyTerminalFactory.getSDK().getLiveManager().getTempGroupId();
        if(tempGroupId!=0){
            int resultCode = MyTerminalFactory.getSDK().getGroupCallManager().requestGroupCall("",tempGroupId);
            if(resultCode == BaseCommonCode.SUCCESS_CODE){//允许组呼了
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCallingCannotClickHandler.class, true);
                if (getSendGroupCallListener() != null) {
                    getSendGroupCallListener().readySpeak();
                }
                if (!MyTerminalFactory.getSDK().getAudioProxy().isSpeakerphoneOn()) {
                    MyTerminalFactory.getSDK().getAudioProxy().setSpeakerphoneOn(true);
                }
                PadApplication.getPadApplication().isPttPress = true;
            }else if(resultCode == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode()){
                if (getSendGroupCallListener() != null) {
                    getSendGroupCallListener().waite();
                }
            }else{//组呼失败的提示
                ToastUtil.groupCallFailToast(getContext(), resultCode);
            }
            //setViewEnable(false); 设置推送等图标的是否可用
        }else{
            getView().getLogger().error(getContext().getString(R.string.no_get_temporary_group_id));
            if (getSendGroupCallListener() != null) {
                getSendGroupCallListener().fail();
            }
        }
    }

    public SendGroupCallListener getSendGroupCallListener() {
        if (sendGroupCallListener != null && sendGroupCallListener.get() != null) {
            return sendGroupCallListener.get();
        } else {
            return null;
        }
    }

    /**
     * 组呼来了
     */
    ReceiveGroupCallIncommingHandler receiveGroupCallIncommingHandler = (memberId, memberName, groupId, groupName, currentCallMode, uniqueNo) -> mHandler.post(() -> {
        getView().getLogger().info("组呼来了---ReceiveGroupCallIncommingHandler---");

        if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_LISTEN.name())){
            ToastUtil.showToast(getContext(),getContext().getString(R.string.text_has_no_group_call_listener_authority));
        }else{
            //Todo 1显示 当前组呼人
            //change2Listening();//听
            if (getSendGroupCallListener() != null) {
                getSendGroupCallListener().listening(memberName);
            }
        }
    });

    /**
     * 停止组呼
     */
    ReceiveGroupCallCeasedIndicationHandler receiveGroupCallCeasedIndicationHandler = reasonCode -> mHandler.post(() -> {
        getView().getLogger().info("停止组呼 ---ReceiveGroupCallCeasedIndicationHandler---");
        //Todo 1隐藏当前组呼人
        //change2Silence();//沉默、无声状态
        if (getSendGroupCallListener() != null) {
            getSendGroupCallListener().silence();
        }
    });


    /**
     * 请求组呼成功的回调
     */
    ReceiveRequestGroupCallConformationHandler receiveRequestGroupCallConformationHandler = (methodResult, resultDesc, groupId) -> mHandler.post(() -> {
        getView().getLogger().info("请求组呼成功 回调---ReceiveRequestGroupCallConformationHandler---");

        if (MyTerminalFactory.getSDK().getGroupCallManager().getCurrentCallMode() == CallMode.GENERAL_CALL_MODE) {

            if (methodResult == BaseCommonCode.SUCCESS_CODE) {//请求成功，开始组呼
                if (getSendGroupCallListener() != null) {
                    getSendGroupCallListener().speaking();
                }
                MyTerminalFactory.getSDK().putParam(Params.CURRENT_SPEAKER, "");
            } else if (methodResult == SignalServerErrorCode.RESPONSE_GROUP_IS_DISABLED.getErrorCode()) {//响应组为禁用状态，低级用户无法组呼
                int currentGroupId = TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);
                Group groupByGroupNo = TerminalFactory.getSDK().getGroupByGroupNo(currentGroupId);
                if (!groupByGroupNo.isHighUser()) {
                    //change2Forbid();//禁止组呼
                    if (getSendGroupCallListener() != null) {
                        getSendGroupCallListener().forbid();
                    }
                } else {
                    //change2Silence();//沉默、无声状态
                    if (getSendGroupCallListener() != null) {
                        getSendGroupCallListener().silence();
                    }
                }
            } else if (methodResult == SignalServerErrorCode.CANT_SPEAK_IN_GROUP.getErrorCode()) {//只听组
                //change2Silence();//沉默、无声状态
                if (getSendGroupCallListener() != null) {
                    getSendGroupCallListener().silence();
                }
            } else if (methodResult == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode()) {//请求等待中
                //change2Waiting();//等待
                if (getSendGroupCallListener() != null) {
                    getSendGroupCallListener().waite();
                }
            } else {
                if (App.getApp().getGroupListenenState() != GroupCallListenState.LISTENING) {
                    //change2Silence();//沉默、无声状态
                    if (getSendGroupCallListener() != null) {
                        getSendGroupCallListener().silence();
                    }
                } else {
                    //change2Listening();//听
                    if (getSendGroupCallListener() != null) {
                        getSendGroupCallListener().listening(null);
                    }
                }
            }
        } else {
            if (App.getApp().getGroupListenenState() != GroupCallListenState.LISTENING) {
                //change2Silence();//沉默、无声状态
                if (getSendGroupCallListener() != null) {
                    getSendGroupCallListener().silence();
                }
            } else {
                //change2Listening();//听
                if (getSendGroupCallListener() != null) {
                    getSendGroupCallListener().listening(null);
                }
            }
        }
    });

    /**
     * 谁发起，谁结束 组呼
     * <p>
     * 组呼有我发起，由我结束
     */
    ReceiveCeaseGroupCallConformationHander receiveCeaseGroupCallConformationHander = (resultCode, resultDesc) -> mHandler.post(() -> {
        getView().getLogger().info("结束 组呼---ReceiveResponseGroupActiveHandler---");

        MyTerminalFactory.getSDK().getAudioProxy().volumeCancelQuiet();
        //mBtnLiveLookPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_dodgerblue2);
        //mLlLiveGroupCall.setVisibility(View.GONE);
        //change2Silence();//沉默、无声状态
        if (getSendGroupCallListener() != null) {
            getSendGroupCallListener().silence();
        }
    });


    /**
     * 取消组呼
     */
    ReceiveStartCeaseGroupCallHandler receiveStartCeaseGroupCallHandler = isCalled -> mHandler.post(() -> {
        getView().getLogger().info("取消组呼---ReceiveStartCeaseGroupCallHandler---");
    });

    /**
     * 响应组 消息来了
     * 响应组-为群组的一个属性，只有有权限的人才能主动说话，其它人只能在规定的时候才能说
     * 如为非 活动状态 则不能在说话
     */
    ReceiveResponseGroupActiveHandler receiveResponseGroupActiveHandler = (isActive, responseGroupId) -> mHandler.post(() -> {
        getView().getLogger().info("响应组---ReceiveResponseGroupActiveHandler---");
    });

    /**
     * 组呼还由10秒超时
     * 还有10秒超时，振动提醒
     */
    ReceiveTalkWillTimeoutHandler receiveTalkWillTimeoutHandler = () -> mHandler.post(() -> {
        getView().getLogger().info("组呼还由10秒超时---ReceiveTalkWillTimeoutHandler---");
    });



    /********************ptt事件**********************/
    public void pttDownDoThing(SendGroupCallListener sendGroupCallListener) {
        getView().getLogger().info("ptt.pttDownDoThing执行了 isPttPress：" + PadApplication.getPadApplication().isPttPress);

        if (!CheckMyPermission.selfPermissionGranted(getContext(), Manifest.permission.RECORD_AUDIO)) {//没有录音权限
            CheckMyPermission.permissionPrompt((Activity) getContext(), Manifest.permission.RECORD_AUDIO);
            return;
        }
        //没有组呼权限
        if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_TALK.name())) {
            ToastUtil.showToast(getContext(), getContext().getString(R.string.text_has_no_group_call_authority));
            return;
        }
        startGroupCall(sendGroupCallListener);
    }

    public void pttUpDoThing() {
        getView().getLogger().info("ptt.pttUpDoThing执行了 isPttPress：" + PadApplication.getPadApplication().isPttPress);
        MyTerminalFactory.getSDK().getAudioProxy().volumeCancelQuiet();
        if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_TALK.name())) {
            return;
        }
        if (PadApplication.getPadApplication().getGroupListenenState() == GroupCallListenState.LISTENING) {
            //change2Listening();//听
            if (getSendGroupCallListener() != null) {
                getSendGroupCallListener().listening(null);
            }
        } else {
            //change2Silence();//沉默、无声状态
            if (getSendGroupCallListener() != null) {
                getSendGroupCallListener().silence();
            }
        }
        MyTerminalFactory.getSDK().getGroupCallManager().ceaseGroupCall();
        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCallingCannotClickHandler.class, false);
    }


    /***********************注册监听************************/

    /**
     * 注册回调
     */
    public void bindReceiveHandler() {
        registerReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        registerReceiveHandler(receiveGroupCallIncommingHandler);
        registerReceiveHandler(receiveStartCeaseGroupCallHandler);
        registerReceiveHandler(receiveResponseGroupActiveHandler);
        registerReceiveHandler(receiveCeaseGroupCallConformationHander);
        registerReceiveHandler(receiveTalkWillTimeoutHandler);
        registerReceiveHandler(receiveRequestGroupCallConformationHandler);
    }

    /**
     * 解绑 回调
     */
    public void unBindReceiveHandler() {
        unRegisterReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        unRegisterReceiveHandler(receiveGroupCallIncommingHandler);
        unRegisterReceiveHandler(receiveStartCeaseGroupCallHandler);
        unRegisterReceiveHandler(receiveResponseGroupActiveHandler);
        unRegisterReceiveHandler(receiveCeaseGroupCallConformationHander);
        unRegisterReceiveHandler(receiveTalkWillTimeoutHandler);
        unRegisterReceiveHandler(receiveRequestGroupCallConformationHandler);
    }

    /**
     * 注册 回调
     *
     * @param receiveHandler
     */
    private void registerReceiveHandler(ReceiveHandler receiveHandler) {
        MyTerminalFactory.getSDK().registReceiveHandler(receiveHandler);
    }

    /**
     * 取消注册的回调
     *
     * @param receiveHandler
     */
    private void unRegisterReceiveHandler(ReceiveHandler receiveHandler) {
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveHandler);
    }


    @Override
    public void attachView(IPttButton view) {
        super.attachView(view);
        bindReceiveHandler();
    }

    @Override
    public void detachView() {
        super.detachView();
        unBindReceiveHandler();
    }
}

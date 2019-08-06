package com.vsxin.terminalpad.instruction.groupCall;

import android.content.Context;

import com.vsxin.terminalpad.app.App;
import com.vsxin.terminalpad.instruction.BaseInstruction;

import java.lang.ref.WeakReference;

import cn.vsx.hamster.common.CallMode;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.SignalServerErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCeaseGroupCallConformationHander;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallCeasedIndicationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRequestGroupCallConformationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseGroupActiveHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveStartCeaseGroupCallHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveTalkWillTimeoutHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.context.MyTerminalFactory;

import static cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState.LISTENING;

/**
 * 组呼指令集封装
 * <p>
 * 相关所有回调
 * GroupCallListenEndStateHandler:
 * ReceiveGroupCallCeasedIndicationHandler
 * <p>
 * GroupCallListenListeningStateHandler:
 * ReceiveGroupCallIncommingHandler
 * <p>
 * GroupCallListenStoppedStateHandler:
 * ReceiveStartCeaseGroupCallHandler
 * <p>
 * GroupCallManager:
 * ReceiveResponseGroupActiveHandler
 * <p>
 * GroupCallSpeakCeasedStateHandler:
 * ReceiveStartCeaseGroupCallHandler ReceiveCeaseGroupCallConformationHander
 * <p>
 * GroupCallSpeakGrantedStateHandler:
 * ReceiveTalkWillTimeoutHandler ReceiveRequestGroupCallConformationHandler
 * <p>
 * GroupCallSpeakNoGrantedStateHandler:
 * ReceiveRequestGroupCallConformationHandler
 * <p>
 * <p>
 * GroupCallSpeakStateMachine:
 * ReceiveCeaseGroupCallConformationHander
 * <p>
 * <p>
 * GroupCallSpeakWaitingStateHandler:
 * ReceiveRequestGroupCallConformationHandler
 */
public class GroupCallInstruction extends BaseInstruction{

    private WeakReference<SendGroupCallListener> sendGroupCallListener;

    public GroupCallInstruction(Context context) {
        super(context);
    }

    public void startGroupCall(SendGroupCallListener sendGroupCallListener) {
        this.sendGroupCallListener = new WeakReference<SendGroupCallListener>(sendGroupCallListener);
        int resultCode = MyTerminalFactory.getSDK().getGroupCallManager().requestCurrentGroupCall("");
        if (resultCode == BaseCommonCode.SUCCESS_CODE) {//允许组呼了 //准备说话
            if(getSendGroupCallListener()!=null){
                getSendGroupCallListener().readySpeak();
                if (!MyTerminalFactory.getSDK().getAudioProxy().isSpeakerphoneOn()) {
                    MyTerminalFactory.getSDK().getAudioProxy().setSpeakerphoneOn(true);
                }
            }
        } else if (resultCode == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode()) {//PTT排队中
            if(getSendGroupCallListener()!=null){
                getSendGroupCallListener().waite();
            }
        } else {//组呼失败的提示
            //ToastUtil.groupCallFailToast(getContext(), resultCode);

            if(getSendGroupCallListener()!=null){
                getSendGroupCallListener().waite();
            }
        }
    }

    public SendGroupCallListener getSendGroupCallListener() {
        if(sendGroupCallListener!=null && sendGroupCallListener.get()!=null){
            return sendGroupCallListener.get();
        }else{
            return null;
        }
    }

    /**
     * 停止组呼
     */
    ReceiveGroupCallCeasedIndicationHandler receiveGroupCallCeasedIndicationHandler = new ReceiveGroupCallCeasedIndicationHandler() {
        @Override
        public void handler(int reasonCode) {
            getLogger().info("停止组呼 ---ReceiveGroupCallCeasedIndicationHandler---");
        }
    };

    /**
     * 组呼来了
     */
    ReceiveGroupCallIncommingHandler receiveGroupCallIncommingHandler = new ReceiveGroupCallIncommingHandler() {
        @Override
        public void handler(int memberId, String memberName, int groupId, String groupName, CallMode currentCallMode) {
            getLogger().info("组呼来了---ReceiveGroupCallIncommingHandler---");
        }
    };

    /**
     * 取消组呼
     */
    ReceiveStartCeaseGroupCallHandler receiveStartCeaseGroupCallHandler = new ReceiveStartCeaseGroupCallHandler() {
        @Override
        public void handler(boolean isCalled) {
            getLogger().info("取消组呼---ReceiveStartCeaseGroupCallHandler---");
        }
    };

    /**
     * 响应组 消息来了
     * 响应组-为群组的一个属性，只有有权限的人才能主动说话，其它人只能在规定的时候才能说
     * 如为非 活动状态 则不能在说话
     */
    ReceiveResponseGroupActiveHandler receiveResponseGroupActiveHandler = new ReceiveResponseGroupActiveHandler() {
        @Override
        public void handler(boolean isActive, int responseGroupId) {
            getLogger().info("响应组---ReceiveResponseGroupActiveHandler---");
        }
    };

    /**
     * 谁发起，谁结束 组呼
     * <p>
     * 组呼有我发起，由我结束
     */
    ReceiveCeaseGroupCallConformationHander receiveCeaseGroupCallConformationHander = new ReceiveCeaseGroupCallConformationHander() {
        @Override
        public void handler(int resultCode, String resultDesc) {
            getLogger().info("结束 组呼---ReceiveResponseGroupActiveHandler---");

        }
    };


    /**
     * 组呼还由10秒超时
     * 还有10秒超时，振动提醒
     */
    ReceiveTalkWillTimeoutHandler receiveTalkWillTimeoutHandler = new ReceiveTalkWillTimeoutHandler() {
        @Override
        public void handler() {
            getLogger().info("组呼还由10秒超时---ReceiveTalkWillTimeoutHandler---");
        }
    };

    /**
     * 请求组呼成功的回调
     */
    ReceiveRequestGroupCallConformationHandler receiveRequestGroupCallConformationHandler = new ReceiveRequestGroupCallConformationHandler() {
        @Override
        public void handler(int methodResult, String resultDesc, int groupId) {
            getLogger().info("请求组呼成功 回调---ReceiveRequestGroupCallConformationHandler---");

            if (MyTerminalFactory.getSDK().getGroupCallManager().getCurrentCallMode() == CallMode.GENERAL_CALL_MODE) {

                if (methodResult == BaseCommonCode.SUCCESS_CODE) {//请求成功，开始组呼
                    if(getSendGroupCallListener()!=null){
                        getSendGroupCallListener().speaking();
                    }
                    MyTerminalFactory.getSDK().putParam(Params.CURRENT_SPEAKER, "");
                } else if (methodResult == SignalServerErrorCode.RESPONSE_GROUP_IS_DISABLED.getErrorCode()) {//响应组为禁用状态，低级用户无法组呼
                    int currentGroupId = TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);
                    Group groupByGroupNo = TerminalFactory.getSDK().getGroupByGroupNo(currentGroupId);
                    if (!groupByGroupNo.isHighUser()) {
                        //change2Forbid();//禁止组呼
                        if(getSendGroupCallListener()!=null){
                            getSendGroupCallListener().forbid();
                        }
                    } else {
                        //change2Silence();//沉默、无声状态
                        if(getSendGroupCallListener()!=null){
                            getSendGroupCallListener().silence();
                        }
                    }
                } else if (methodResult == SignalServerErrorCode.CANT_SPEAK_IN_GROUP.getErrorCode()) {//只听组
                    //change2Silence();//沉默、无声状态
                    if(getSendGroupCallListener()!=null){
                        getSendGroupCallListener().silence();
                    }
                } else if (methodResult == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode()) {//请求等待中
                    //change2Waiting();//等待
                    if(getSendGroupCallListener()!=null){
                        getSendGroupCallListener().waite();
                    }
                } else {
                    if (App.getApp().getGroupListenenState() != LISTENING) {
                        //change2Silence();//沉默、无声状态
                        if(getSendGroupCallListener()!=null){
                            getSendGroupCallListener().silence();
                        }
                    } else {
                        //change2Listening();//听
                        if(getSendGroupCallListener()!=null){
                            getSendGroupCallListener().listening();
                        }
                    }
                }
            } else {
                if (App.getApp().getGroupListenenState() != GroupCallListenState.LISTENING) {
                    //change2Silence();//沉默、无声状态
                    if(getSendGroupCallListener()!=null){
                        getSendGroupCallListener().silence();
                    }
                } else {
                    //change2Listening();//听
                    if(getSendGroupCallListener()!=null){
                        getSendGroupCallListener().listening();
                    }
                }
            }
        }
    };

    @Override
    public void bindReceiveHandler() {
        registerReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        registerReceiveHandler(receiveGroupCallIncommingHandler);
        registerReceiveHandler(receiveStartCeaseGroupCallHandler);
        registerReceiveHandler(receiveResponseGroupActiveHandler);
        registerReceiveHandler(receiveCeaseGroupCallConformationHander);
        registerReceiveHandler(receiveTalkWillTimeoutHandler);
        registerReceiveHandler(receiveRequestGroupCallConformationHandler);
    }

    @Override
    public void unBindReceiveHandler() {
        unRegisterReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        unRegisterReceiveHandler(receiveGroupCallIncommingHandler);
        unRegisterReceiveHandler(receiveStartCeaseGroupCallHandler);
        unRegisterReceiveHandler(receiveResponseGroupActiveHandler);
        unRegisterReceiveHandler(receiveCeaseGroupCallConformationHander);
        unRegisterReceiveHandler(receiveTalkWillTimeoutHandler);
        unRegisterReceiveHandler(receiveRequestGroupCallConformationHandler);
    }
}

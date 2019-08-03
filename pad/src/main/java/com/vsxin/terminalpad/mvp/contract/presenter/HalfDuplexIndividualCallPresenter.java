package com.vsxin.terminalpad.mvp.contract.presenter;

import android.content.Context;

import com.ixiaoma.xiaomabus.architecture.mvp.BasePresenter;
import com.vsxin.terminalpad.app.PadApplication;
import com.vsxin.terminalpad.mvp.contract.view.IHalfDuplexIndividualCallView;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.CallMode;
import cn.vsx.hamster.common.IndividualCallType;
import cn.vsx.hamster.terminalsdk.receiveHandler.IndividualCallPttStatusHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCeaseGroupCallConformationHander;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallCeasedIndicationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyIndividualCallStoppedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRequestGroupCallConformationHandler;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.receiveHandler.ReceiveHeadSetPlugHandler;

/**
 * Created by PC on 2018/11/1.
 * <p>
 * 地图气泡点击-成员详情页
 */

public class HalfDuplexIndividualCallPresenter extends BasePresenter<IHalfDuplexIndividualCallView> {

    public HalfDuplexIndividualCallPresenter(Context mContext) {
        super(mContext);
    }

    //全双工还是半双工
    private int individualCallType = IndividualCallType.HALF_DUPLEX.getCode();

    private ReceiveGroupCallIncommingHandler receiveGroupCallIncommingHandler = (memberId, memberName, groupId, groupName, currentCallMode) -> {
        //如果在半双工个呼中来组呼，就是对方在说话
        getView().getLogger().info("IndividualCallService: 收到组呼：callType:" + individualCallType);
        if(individualCallType == IndividualCallType.HALF_DUPLEX.getCode()){
            getView().groupCallOtherSpeaking();
        }
    };

    //ptt个呼等待
    private IndividualCallPttStatusHandler individualCallPttStatusHandler = (pttIsDown, outerMemberId) -> {
        getView().getLogger().info("PTT个呼等待" + "pttIsDown:" + pttIsDown);
        getView().individualCallPttStatus(pttIsDown, outerMemberId);
    };

    //主动方停止组呼
    private ReceiveCeaseGroupCallConformationHander receiveCeaseGroupCallConformationHander = (resultCode, resultDesc) -> {
        if(individualCallType == IndividualCallType.HALF_DUPLEX.getCode()){
            getView().ceaseGroupCallConformation(resultCode, resultDesc);
        }
    };


    private ReceiveGroupCallCeasedIndicationHandler receiveGroupCallCeasedIndicationHandler = (reasonCode) -> {
        getView().getLogger().info("收到组呼停止");
        if(individualCallType == IndividualCallType.HALF_DUPLEX.getCode()){
            getView().groupCallCeasedIndication(reasonCode);
        }
    };

    /**
     * 主动方请求组呼的消息
     */
    private ReceiveRequestGroupCallConformationHandler receiveRequestGroupCallConformationHandler = (methodResult, resultDesc, groupId) -> {
        if(MyTerminalFactory.getSDK().getGroupCallManager().getCurrentCallMode() == CallMode.GENERAL_CALL_MODE && MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_TALK.name())){
            if(PadApplication.getPadApplication().isPttPress){
                    if(individualCallType == IndividualCallType.HALF_DUPLEX.getCode()){
                        getView().requestGroupCallConformation(methodResult, resultDesc, groupId);
                    }
            }
        }
    };

    /**
     * 被动方通知个呼停止，界面---------静默状态
     */
    private ReceiveNotifyIndividualCallStoppedHandler receiveNotifyIndividualCallStoppedHandler = (methodResult, resultDesc) ->{
        getView().notifyIndividualCallStopped(methodResult, resultDesc);
    };

    /**
     * 设置是否可以打开扬声器
     */
    private ReceiveHeadSetPlugHandler receiveHeadSetPlugHandler = new ReceiveHeadSetPlugHandler(){
        @Override
        public void handler(boolean headset){
        }
    };
    /**
     * 注册监听
     */
    public void registReceiveHandler(){
       MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(individualCallPttStatusHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveCeaseGroupCallConformationHander);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveRequestGroupCallConformationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyIndividualCallStoppedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveHeadSetPlugHandler);
    }

    /**
     * 取消监听
     */
    public void unregistReceiveHandler(){
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(individualCallPttStatusHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveCeaseGroupCallConformationHander);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveRequestGroupCallConformationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyIndividualCallStoppedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveHeadSetPlugHandler);
    }





}

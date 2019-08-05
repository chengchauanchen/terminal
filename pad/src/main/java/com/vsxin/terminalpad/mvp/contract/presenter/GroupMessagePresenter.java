package com.vsxin.terminalpad.mvp.contract.presenter;

import android.content.Context;

import com.blankj.utilcode.util.ToastUtils;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.app.PadApplication;
import com.vsxin.terminalpad.mvp.contract.view.IGroupMessageView;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.CallMode;
import cn.vsx.hamster.errcode.module.SignalServerErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCeaseGroupCallConformationHander;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallCeasedIndicationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRequestGroupCallConformationHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/8/5
 * 描述：
 * 修订历史：
 */
public class GroupMessagePresenter extends BaseMessagePresenter<IGroupMessageView>{
    public GroupMessagePresenter(Context mContext){
        super(mContext);
    }

    @Override
    public void registReceiveHandler(){
        super.registReceiveHandler();
        TerminalFactory.getSDK().registReceiveHandler(receiveRequestGroupCallConformationHandler);
        TerminalFactory.getSDK().registReceiveHandler(receiveGroupCallIncommingHandler);
        TerminalFactory.getSDK().registReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        TerminalFactory.getSDK().registReceiveHandler(receiveCeaseGroupCallConformationHander);
    }

    @Override
    public void unregistReceiveHandler(){
        super.unregistReceiveHandler();
        TerminalFactory.getSDK().unregistReceiveHandler(receiveRequestGroupCallConformationHandler);
        TerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallIncommingHandler);
        TerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        TerminalFactory.getSDK().unregistReceiveHandler(receiveCeaseGroupCallConformationHander);
    }

    /**
     * 主动方请求组呼的消息
     */
    private ReceiveRequestGroupCallConformationHandler receiveRequestGroupCallConformationHandler = (methodResult, resultDesc, groupId) -> {
        getView().getLogger().info("PTTViewPager触发了请求组呼的响应methodResult:" + methodResult);
        if (MyTerminalFactory.getSDK().getGroupCallManager().getCurrentCallMode() == CallMode.GENERAL_CALL_MODE) {
            if (PadApplication.getPadApplication().isPttPress) {
                if (methodResult == 0) {//请求成功，开始组呼
                    mHandler.post(() -> {
                        getView().change2Speaking();
                        MyTerminalFactory.getSDK().putParam(Params.CURRENT_SPEAKER, "");
                    });
                } else if (methodResult == SignalServerErrorCode.CANT_SPEAK_IN_GROUP.getErrorCode()) {//只听组
                    mHandler.post(() -> ToastUtils.showShort(R.string.text_current_group_only_listener_can_not_speak));
                } else if (methodResult == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode()) {//请求等待中
                    mHandler.post(() -> getView().change2Waiting());
                } else {//请求失败
                    mHandler.post(() -> {
                        if (PadApplication.getPadApplication().getGroupListenenState() != GroupCallListenState.LISTENING) {
                            getView().change2Silence();
                        } else {
                            getView().change2Listening();
                        }
                    });
                }
            }
        }
    };

    /**
     * 被动方组呼来了
     */
    private ReceiveGroupCallIncommingHandler receiveGroupCallIncommingHandler = (memberId, memberName, groupId, version, currentCallMode) -> {
//        speakingId = groupId;
//        speakingName = memberName;
        if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_LISTEN.name())) {
            ToastUtils.showShort(R.string.text_has_no_group_call_listener_authority);
        }

        if (currentCallMode == CallMode.GENERAL_CALL_MODE) {
            mHandler.post(() -> {
                getView().refreshPtt();
                //                if (MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.GRANTING
                //                        || MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.WAITING) {
                //                    change2Waiting();
                //                } else if (MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.GRANTED) {
                //                    //什么都不用做
                //                } else {
                //                    change2Listening();
                //                }
            });
        }
    };

    /**
     * 被动方组呼停止
     */
    private ReceiveGroupCallCeasedIndicationHandler receiveGroupCallCeasedIndicationHandler = reasonCode -> {
        //            groupScanId = 0;
        mHandler.post(() -> {
            if (MyTerminalFactory.getSDK().getGroupCallManager().getCurrentCallMode() == CallMode.GENERAL_CALL_MODE) {
                getView().refreshPtt();
                //                if (MyApplication.instance.isPttPress && MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.IDLE) {
                //                    change2Speaking();
                //                }
                //                if (MyApplication.instance.getGroupSpeakState() != GroupCallSpeakState.GRANTING && MyApplication.instance.getGroupSpeakState() != GroupCallSpeakState.WAITING && MyApplication.instance.getGroupSpeakState() != GroupCallSpeakState.GRANTED) {
                //                    change2Silence();
                //                }
            }
        });
    };

    /**
     * 主动方停止组呼的消息
     */
    private ReceiveCeaseGroupCallConformationHander receiveCeaseGroupCallConformationHander = (resultCode, resultDesc) -> {
        mHandler.post(() -> {
            getView().refreshPtt();
            //            if (MyTerminalFactory.getSDK().getGroupCallManager().getCurrentCallMode() == CallMode.GENERAL_CALL_MODE) {
            //                if (MyApplication.instance.getGroupListenenState() == GroupCallListenState.LISTENING) {
            //                    change2Listening();
            //                } else {
            //                    change2Silence();
            //                }
            //            }
//            setViewEnable(true);
        });
    };
}

package com.vsxin.terminalpad.mvp.contract.presenter;

import android.content.Context;

import com.ixiaoma.xiaomabus.architecture.mvp.refresh.RefreshPresenter;
import com.vsxin.terminalpad.app.PadApplication;
import com.vsxin.terminalpad.mvp.contract.constant.MemberTypeConstans;
import com.vsxin.terminalpad.mvp.contract.constant.MemberTypeEnum;
import com.vsxin.terminalpad.mvp.contract.view.ILayerMapView;
import com.vsxin.terminalpad.mvp.contract.view.INoticeView;
import com.vsxin.terminalpad.mvp.entity.NoticeBean;
import com.vsxin.terminalpad.mvp.ui.activity.MainMapActivity;
import com.vsxin.terminalpad.prompt.PromptManager;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallState;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveAnswerIndividualCallTimeoutHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyIndividualCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyIndividualCallStoppedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyLivingIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseStartIndividualCallHandler;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * @author qzw
 *
 * 右上 图层list
 */
public class LayerMapPresenter extends RefreshPresenter<MemberTypeEnum, ILayerMapView> {

    public LayerMapPresenter(Context mContext) {
        super(mContext);
    }

    public void drawMapLayer(int layerType,boolean isShow){
        ((MainMapActivity)getContext()).drawMapLayer(layerType,isShow);
    }

    /**
     * 全部图层 checkbox 选择 或 取消
     * @param isChecked
     */
    public void notifyDataSetChanged(boolean isChecked){
        List<MemberTypeEnum> memberTypeList = MemberTypeConstans.getMemberTypeList();
        for (MemberTypeEnum typeEnum : memberTypeList){
            typeEnum.setCheck(isChecked);
            drawMapLayer(typeEnum.getType(),typeEnum.isCheck());
        }
        getView().refreshOrLoadMore(memberTypeList);
    }



}

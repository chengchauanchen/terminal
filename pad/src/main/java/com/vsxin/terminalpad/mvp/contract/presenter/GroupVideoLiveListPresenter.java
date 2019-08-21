package com.vsxin.terminalpad.mvp.contract.presenter;

import android.content.Context;

import com.ixiaoma.xiaomabus.architecture.mvp.refresh.RefreshPresenter;
import com.vsxin.terminalpad.mvp.contract.view.IGroupVideoLiveList;

import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetGroupLivingHistoryListHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetGroupLivingListHandler;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * @author qzw
 * <p>
 * app模块-通讯录模块
 */
public class GroupVideoLiveListPresenter extends RefreshPresenter<TerminalMessage, IGroupVideoLiveList> {
    private static final String TAG = "GroupVideoLiveListPresenter";

    public GroupVideoLiveListPresenter(Context mContext) {
        super(mContext);
    }

    /**
     * 加载数据
     */
    public void initData( int groupId,boolean isGroupVideoLiving,int page,int mPageSize) {
        if (isGroupVideoLiving) {
            //正在上报
            long groupUniqueNo = MyTerminalFactory.getSDK().getTerminalMessageManager().getGroupUniqueNo(groupId);
            MyTerminalFactory.getSDK().getGroupManager().getGroupLivingList(String.valueOf(groupUniqueNo), false);
        } else {
            //上报历史
            long groupUniqueNo = MyTerminalFactory.getSDK().getTerminalMessageManager().getGroupUniqueNo(groupId);
            MyTerminalFactory.getSDK().getGroupManager().getGroupHistoryLiveList(String.valueOf(groupUniqueNo), page, mPageSize);
        }
    }

    /**
     * 获取组内正在直播列表
     */
    private ReceiveGetGroupLivingListHandler receiveGetGroupLivingListHandler = (beanList, resultCode, resultDesc, forNumber) -> {
        getView().getGroupLivingList(beanList, resultCode, resultDesc, forNumber);
    };

    /**
     * 获取组内直播历史列表
     */
    private ReceiveGetGroupLivingHistoryListHandler receiveGetGroupLivingHistoryListHandler = (beanList, resultCode, resultDesc) -> {
        getView().getGroupLivingHistoryList(beanList, resultCode, resultDesc);
    };

    public void registReceiveHandler() {
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGetGroupLivingListHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGetGroupLivingHistoryListHandler);
    }

    public void unRegistReceiveHandler() {
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetGroupLivingListHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetGroupLivingHistoryListHandler);
    }
}

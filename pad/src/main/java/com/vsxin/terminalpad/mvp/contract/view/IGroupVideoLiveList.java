package com.vsxin.terminalpad.mvp.contract.view;


import com.ixiaoma.xiaomabus.architecture.mvp.refresh.IRefreshView;
import com.vsxin.terminalpad.mvp.ui.adapter.GroupVideoLiveListAdapter;

import java.util.List;

import cn.vsx.hamster.terminalsdk.model.TerminalMessage;

/**
 * @author qzw
 * <p>
 * app模块-通讯录模块
 */
public interface IGroupVideoLiveList extends IRefreshView<TerminalMessage> {
    void notifyDataSetChanged(List<TerminalMessage> terminalMessages, boolean toTop);
    void showMsg(String msg);
    void showMsg(int resouce);
    GroupVideoLiveListAdapter getAdapter();

    void getGroupLivingList(List<TerminalMessage> memberList, int resultCode, String resultDesc, boolean forNumber);
    void getGroupLivingHistoryList(List<TerminalMessage> memberList, int resultCode, String resultDesc);
}

package com.vsxin.terminalpad.mvp.contract.view;


import com.ixiaoma.xiaomabus.architecture.mvp.refresh.IRefreshView;

import java.util.List;

import cn.vsx.hamster.terminalsdk.model.TerminalMessage;

/**
 * @author qzw
 * <p>
 * app模块-消息模块
 */
public interface IMessageListView extends IRefreshView<TerminalMessage> {

    void notifyDataSetChanged(List<TerminalMessage> terminalMessages);

    void updateGroupName();
}

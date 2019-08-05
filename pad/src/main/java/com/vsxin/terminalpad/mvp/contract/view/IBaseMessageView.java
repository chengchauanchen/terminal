package com.vsxin.terminalpad.mvp.contract.view;

import android.content.Context;

import com.ixiaoma.xiaomabus.architecture.mvp.refresh.IRefreshView;

import java.util.List;

import cn.vsx.hamster.terminalsdk.model.TerminalMessage;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/8/5
 * 描述：
 * 修订历史：
 */
public interface IBaseMessageView extends IRefreshView<TerminalMessage>{
    void notifyDataSetChanged(List<TerminalMessage> terminalMessages);

    void setListSelection(int position);

    void stopReFreshing();

    boolean isGroup();

    void notifyItemRangeInserted(int startPosition, int endPosition);

    void smoothScrollBy(int start, int end);

    Context getContext();

    void scrollMyListViewToBottom();
}

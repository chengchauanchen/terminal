package com.vsxin.terminalpad.mvp.contract.view;

import android.content.Context;

import com.ixiaoma.xiaomabus.architecture.mvp.refresh.IRefreshView;

import java.util.List;

import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Member;
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
    void notifyDataSetChanged();
    void notifyItemChanged(int position);

    void setListSelection(int position);

    void setSmoothScrollToPosition(int position);

    void stopReFreshing();

    boolean isGroup();

    int getUserId();

    void downloadProgress(float percent, TerminalMessage terminalMessage);

    void downloadFinish(TerminalMessage terminalMessage, boolean success);

    void notifyItemRangeInserted(int startPosition, int endPosition);

    void smoothScrollBy(int start, int end);

    Context getContext();

    void scrollMyListViewToBottom();

    void showProgressDialog();

    void dismissProgressDialog();

    void showMsg(String msg);
    void showMsg(int resouce);

    void chooseDevicesDialog(int type, Account account);

    void callPhone(String phone);

    void goToVoIpActivity(Member member);

    void refreshPersonContactsAdapter(int mposition, List<TerminalMessage> terminalMessageList, boolean isPlaying, boolean isSameItem);

    void chatListItemClick(TerminalMessage terminalMessage, boolean isReceiver);

}

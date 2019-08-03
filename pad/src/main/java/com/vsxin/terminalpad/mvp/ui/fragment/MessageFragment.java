package com.vsxin.terminalpad.mvp.ui.fragment;

import android.view.View;

import com.ixiaoma.xiaomabus.architecture.mvp.refresh.adapter.BaseRecycleViewAdapter;
import com.ixiaoma.xiaomabus.architecture.mvp.refresh.fragment.RefreshRecycleViewFragment;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.presenter.MessagePresenter;
import com.vsxin.terminalpad.mvp.contract.view.IMessageView;
import com.vsxin.terminalpad.mvp.ui.adapter.MessageAdapter;

import java.util.List;

import cn.vsx.hamster.terminalsdk.model.TerminalMessage;

/**
 * @author qzw
 * <p>
 * app模块-消息模块
 */
public class MessageFragment extends RefreshRecycleViewFragment<TerminalMessage, IMessageView, MessagePresenter> implements IMessageView {

    private MessageAdapter messageAdapter;

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_message;
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        getPresenter().registReceiveHandler();

        refreshLayout.setEnableRefresh(false);
        refreshLayout.setEnableLoadMore(false);
        getPresenter().setAdapter(recyclerView, mSuperAdapter);
    }

    @Override
    protected void initData() {
        getPresenter().loadMessages();
        getPresenter().getAllMessageFromServer();
    }


    @Override
    public void notifyDataSetChanged(List<TerminalMessage> terminalMessages) {
//        messageAdapter.notifyDataSetChanged();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshOrLoadMore(terminalMessages);
            }
        });
    }

    @Override
    protected void refresh() {

    }

    @Override
    protected void loadMore() {

    }

    @Override
    protected BaseRecycleViewAdapter createAdapter() {
        messageAdapter = new MessageAdapter(getContext());
//        messageAdapter.setDatas(getPresenter().getData());
        return messageAdapter;
    }

    @Override
    public MessagePresenter createPresenter() {
        return new MessagePresenter(getContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPresenter().unRegistReceiveHandler();
    }
}

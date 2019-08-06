package com.vsxin.terminalpad.mvp.ui.fragment;

import android.view.View;
import android.widget.TextView;

import com.ixiaoma.xiaomabus.architecture.mvp.refresh.adapter.BaseRecycleViewAdapter;
import com.ixiaoma.xiaomabus.architecture.mvp.refresh.fragment.RefreshRecycleViewFragment;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.presenter.MessageListPresenter;
import com.vsxin.terminalpad.mvp.contract.view.IMessageListView;
import com.vsxin.terminalpad.mvp.ui.adapter.MessageListAdapter;
import com.vsxin.terminalpad.utils.FragmentManage;

import java.util.List;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.TerminalMessageUtil;

/**
 * @author qzw
 * <p>
 * app模块-消息模块
 */
public class MessageListFragment extends RefreshRecycleViewFragment<TerminalMessage, IMessageListView, MessageListPresenter> implements IMessageListView{

    private TextView tv_group_name;

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_message_list;
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        tv_group_name = view.findViewById(R.id.tv_group_name);
        getPresenter().registReceiveHandler();
        refreshLayout.setEnableRefresh(false);
        refreshLayout.setEnableLoadMore(false);
        getPresenter().setAdapter(recyclerView, mSuperAdapter);
    }

    @Override
    protected void initData() {
        getPresenter().loadMessages();
        getPresenter().getAllMessageFromServer();
        int currentGroupId = TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);
        Group group = TerminalFactory.getSDK().getGroupByGroupNo(currentGroupId);
        tv_group_name.setText(group.getName());
        mSuperAdapter.setItemClickListener(position -> {
            TerminalMessage terminalMessage = getPresenter().getData().get(position);
            if(TerminalMessageUtil.isGroupMessage(terminalMessage)){
                int userId = terminalMessage.messageToId;
                String groupName = terminalMessage.messageToName;
                long uniqueNo = terminalMessage.messageToUniqueNo;
                FragmentManage.startFragment(getActivity(), GroupMessageFragment.newInstance(userId,groupName,uniqueNo));
            }else {
                int userId = TerminalMessageUtil.getNo(terminalMessage);
                String groupName = TerminalMessageUtil.getTitleName(terminalMessage);
                FragmentManage.startFragment(getActivity(), PersonMessageFragment.newInstance(userId,groupName));
            }
        });
    }


    @Override
    public void notifyDataSetChanged(List<TerminalMessage> terminalMessages) {
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
        mSuperAdapter = new MessageListAdapter(getContext());
        return mSuperAdapter;
    }

    @Override
    public MessageListPresenter createPresenter() {
        return new MessageListPresenter(getContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPresenter().unRegistReceiveHandler();
    }
}

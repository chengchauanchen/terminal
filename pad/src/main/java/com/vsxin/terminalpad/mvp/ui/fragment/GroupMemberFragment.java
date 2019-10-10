package com.vsxin.terminalpad.mvp.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.ixiaoma.xiaomabus.architecture.mvp.refresh.adapter.BaseRecycleViewAdapter;
import com.ixiaoma.xiaomabus.architecture.mvp.refresh.fragment.RefreshRecycleViewFragment;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.presenter.GroupMemberPresenter;
import com.vsxin.terminalpad.mvp.contract.view.IGroupMemberView;
import com.vsxin.terminalpad.mvp.ui.adapter.GroupMemberAdapter;
import com.vsxin.terminalpad.utils.FragmentManage;

import butterknife.BindView;
import cn.vsx.hamster.common.GroupType;
import cn.vsx.hamster.common.TerminalMemberStatusEnum;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * @author qzw
 * 组内成员
 */
public class GroupMemberFragment extends RefreshRecycleViewFragment<Member, IGroupMemberView, GroupMemberPresenter> implements IGroupMemberView {

    private static String GROUP_ID = "groupId";//group id
    private static String GROUP_NAME = "groupName";//组名称

    @BindView(R.id.news_bar_return)
    ImageView news_bar_return;//返回键

    @BindView(R.id.tv_chat_name)
    TextView tv_chat_name;//标题

    @BindView(R.id.member_num)
    TextView member_num;//在线成员人数

    private int groupId;
    private String groupName;

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_group_member;
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        refreshLayout.setEnableRefresh(false);
        refreshLayout.setEnableLoadMore(false);
        getPresenter().setAdapter(recyclerView, mSuperAdapter);

        tv_chat_name.setText("组内在线成员");
        //返回
        news_bar_return.setOnClickListener(v -> {
            FragmentManage.finishFragment(getActivity());
        });
    }

    @Override
    protected void initData() {
        super.initData();
        groupId = getArguments().getInt(GROUP_ID,0);
        groupName = getArguments().getString(GROUP_NAME,"");

        getGroupMembers();
    }

    @Override
    public void setMemberNum(String num) {
        tv_chat_name.setText(num);
    }

    @Override
    public void getGroupMembers() {
        getPresenter().getGroupMembers(groupId);
    }

    @Override
    protected void refresh() {

    }

    @Override
    protected void loadMore() {

    }

    @Override
    protected BaseRecycleViewAdapter createAdapter() {
        return new GroupMemberAdapter(getContext());
    }

    @Override
    public GroupMemberPresenter createPresenter() {
        return new GroupMemberPresenter(getContext());
    }

    /**
     * 打开组内在线成员
     * @param context
     * @param groupId
     * @param groupName
     */
    public static void startGroupMemberFragment(FragmentActivity context, int groupId, String groupName){
        Bundle args = new Bundle();
        args.putInt(GROUP_ID, groupId);
        args.putString(GROUP_NAME, groupName);
        GroupMemberFragment fragment = new GroupMemberFragment();
        fragment.setArguments(args);
        FragmentManage.startFragment(context, fragment);
    }
}

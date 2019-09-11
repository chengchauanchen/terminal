package com.vsxin.terminalpad.mvp.ui.fragment;

import android.view.View;
import android.widget.TextView;

import com.ixiaoma.xiaomabus.architecture.mvp.refresh.adapter.BaseRecycleViewAdapter;
import com.ixiaoma.xiaomabus.architecture.mvp.refresh.fragment.RefreshRecycleViewFragment;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.presenter.ContactsPresenter;
import com.vsxin.terminalpad.mvp.contract.view.IContactsView;
import com.vsxin.terminalpad.mvp.ui.adapter.ContactsAdapter;
import com.vsxin.terminalpad.utils.FragmentManage;

import java.util.List;

import butterknife.BindView;
import cn.vsx.hamster.terminalsdk.model.GroupAndDepartment;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * @author qzw
 * <p>
 * app模块-通信录模块
 */
public class ContactsFragment extends RefreshRecycleViewFragment<GroupAndDepartment, IContactsView, ContactsPresenter> implements IContactsView {

    private ContactsAdapter contactsAdapter;

    @BindView(R.id.tv_group_name)
    TextView tv_group_name;

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_contacts;
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        getPresenter().registReceiveHandler();

        refreshLayout.setEnableRefresh(true);
        refreshLayout.setEnableLoadMore(false);
        getPresenter().setAdapter(recyclerView, mSuperAdapter);

        contactsAdapter.setCatalogItemClickListener((v, isTempGroup, position) -> {
           getPresenter().onCatalogItemClick(v, isTempGroup, position);
        });
        contactsAdapter.setFolderClickListener((v, depId, name, isTempGroup) -> {
            getPresenter().onFolderClickListener(v, depId, name, isTempGroup);
        });
        contactsAdapter.setItemOnClickListener(group -> {
            if(group!=null){
                FragmentManage.startFragment(getActivity(), GroupMessageFragment.newInstance(group.getNo(),group.getName(),group.getUniqueNo()));
            }
        });

        //组名
        tv_group_name.setText(DataUtil.getGroupName(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)));
    }

    @Override
    protected void initData() {
        getPresenter().initData();
    }

    @Override
    protected void refresh() {
        getPresenter().onRefresh();
    }

    @Override
    protected void loadMore() {

    }

    @Override
    protected BaseRecycleViewAdapter createAdapter() {
        contactsAdapter = new ContactsAdapter(getContext(),getActivity());
        return contactsAdapter;
    }

    @Override
    public ContactsPresenter createPresenter() {
        return new ContactsPresenter(getContext());
    }


    @Override
    public void notifyDataSetChanged(List<GroupAndDepartment> list, boolean toTop) {
        getActivity().runOnUiThread(() -> {
            refreshOrLoadMore(list);
            if(toTop){
                recyclerView.scrollToPosition(0);
            }
            tv_group_name.setText(DataUtil.getGroupName(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)));
        });
    }

    @Override
    public void showMsg(String msg) {
        ToastUtil.showToast(getActivity(),msg);
    }

    @Override
    public void showMsg(int resouce) {
        ToastUtil.showToast(getActivity(),getString(resouce));
    }

    @Override
    public ContactsAdapter getAdapter() {
        return contactsAdapter;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPresenter().unRegistReceiveHandler();
    }
}

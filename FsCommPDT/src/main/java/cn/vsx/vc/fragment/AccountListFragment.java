package cn.vsx.vc.fragment;

import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Department;
import cn.vsx.hamster.terminalsdk.model.NewMemberResponse;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveAccountSelectedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivegUpdatePoliceMemberHandler;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.AccountListAdapter;
import cn.vsx.vc.model.ContactItemBean;
import cn.vsx.vc.receiveHandle.ReceiveRemoveSelectedMemberHandler;
import cn.vsx.vc.utils.Constants;

/**
 * 显示账号列表
 */
public class AccountListFragment extends BaseFragment{

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerview;

    private List<ContactItemBean> mData = new ArrayList<>();

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private AccountListAdapter accountListAdapter;
    private List<Integer> selectedMemberNos = new ArrayList<>();

    @Override
    public int getContentViewId(){
        return R.layout.member_list_layout;
    }

    @Override
    public void initView(){
        mSwipeRefreshLayout = mRootView.findViewById(R.id.swipeRefreshLayout);
        mRecyclerview = mRootView.findViewById(R.id.recyclerview);
        mRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        accountListAdapter = new AccountListAdapter(getContext(), mData);
        mRecyclerview.setAdapter(accountListAdapter);
    }

    @Override
    public void initListener(){
        TerminalFactory.getSDK().registReceiveHandler(receiveAccountSelectedHandler);
        TerminalFactory.getSDK().registReceiveHandler(receivegUpdatePoliceMemberHandler);
        TerminalFactory.getSDK().registReceiveHandler(receiveRemoveSelectedMemberHandler);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            TerminalFactory.getSDK().getConfigManager().updataPhoneMemberInfo();
            mHandler.postDelayed(()-> mSwipeRefreshLayout.setRefreshing(false),1200);
        });

        accountListAdapter.setItemClickListener((type, position) -> {
            if(type == Constants.TYPE_ACCOUNT){
                Account account = (Account) mData.get(position).getBean();
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiveAccountSelectedHandler.class,account,!account.isChecked());

            }else if(type == Constants.TYPE_DEPARTMENT){
                Department department = (Department) mData.get(position).getBean();
                TerminalFactory.getSDK().getConfigManager().updatePoliceMember(department.getId(),department.getName());
            }
            accountListAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public void initData(){
        NewMemberResponse mMemberResponse = TerminalFactory.getSDK().getConfigManager().getPhoneMemeberInfo();
        if(null != mMemberResponse){
            List<Department> deptList = mMemberResponse.getDeptList();
            List<Account> accountList = mMemberResponse.getAccountDtos();
            clearData(accountList);
            updateData(deptList,accountList);
        }
    }

    /**
     * 设置数据
     */
    private void updateData(List<Department> deptList,List<Account> accountList){
        mData.clear();
        if(null != accountList && !accountList.isEmpty()){
            //添加成员
            for(Account account : accountList){
                ContactItemBean<Account> contactItemBean = new ContactItemBean<>();
                contactItemBean.setType(Constants.TYPE_ACCOUNT);
                contactItemBean.setBean(account);
                mData.add(contactItemBean);
            }
        }
        if(null != deptList && !deptList.isEmpty()){
            for(Department department : deptList){
                ContactItemBean<Department> contactItemBean = new ContactItemBean<>();
                contactItemBean.setType(Constants.TYPE_DEPARTMENT);
                contactItemBean.setBean(department);
                mData.add(contactItemBean);
            }
        }

        //将之前选中的人重新选中
        for(Integer selectedMemberNo : selectedMemberNos){
            for(ContactItemBean contactItemBean : mData){
                if(contactItemBean.getBean() instanceof Account){
                    if(selectedMemberNo == ((Account) contactItemBean.getBean()).getNo()){
                        ((Account) contactItemBean.getBean()).setChecked(true);
                    }
                }
            }
        }
        if(accountListAdapter !=null){
            mHandler.post(() -> accountListAdapter.notifyDataSetChanged());
        }
    }


    private ReceiveRemoveSelectedMemberHandler receiveRemoveSelectedMemberHandler = contactItemBean -> {
        // TODO: 2019/4/22 如果删除的是子部门的，需要更改子部门数据的状态
        if(contactItemBean.getBean() instanceof Account){
            Account bean = (Account) contactItemBean.getBean();
            Iterator<ContactItemBean> iterator = mData.iterator();
            while(iterator.hasNext()){
                ContactItemBean next = iterator.next();
                if(next.getBean() instanceof Account){
                    Account account = (Account) next.getBean();
                    if(bean.getNo() == account.getNo()){
                        account.setChecked(false);
                    }
                }
            }

            if(selectedMemberNos.contains(bean.getNo())){
                selectedMemberNos.remove((Integer)bean.getNo());
            }
        }
        mHandler.post(()->accountListAdapter.notifyDataSetChanged());
    };

    /**
     * 获取到数据
     */
    private ReceivegUpdatePoliceMemberHandler receivegUpdatePoliceMemberHandler = (depId, depName, departments, accounts) -> updateData(departments,accounts);

    /**
     * 选择成员
     */
    private ReceiveAccountSelectedHandler receiveAccountSelectedHandler = (account, selected) -> {
        // TODO: 2019/4/22 如果选中的是子部门的，需要修改子部门数据的状态
        if(selected){
            if(!selectedMemberNos.contains(account.getNo())){
                selectedMemberNos.add(account.getNo());
            }
        }else {
            if(selectedMemberNos.contains(account.getNo())){
                selectedMemberNos.remove((Integer)account.getNo());
            }
        }
        for(ContactItemBean bean : mData){
            if(bean.getBean() instanceof Account){
                if(((Account) bean.getBean()).getNo() == account.getNo()){
                    ((Account) bean.getBean()).setChecked(selected);
                    break;
                }
            }
        }
        mHandler.post(()-> accountListAdapter.notifyDataSetChanged());
    };

    /**
     * 清除选择状态
     * @param accountList
     */
    private void clearData(List<Account> accountList) {
        if(accountList!=null && !accountList.isEmpty()){
            for (Account account: accountList) {
                account.setChecked(false);
            }
        }
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        TerminalFactory.getSDK().unregistReceiveHandler(receiveRemoveSelectedMemberHandler);
        TerminalFactory.getSDK().unregistReceiveHandler(receivegUpdatePoliceMemberHandler);
        TerminalFactory.getSDK().unregistReceiveHandler(receiveAccountSelectedHandler);
    }
}

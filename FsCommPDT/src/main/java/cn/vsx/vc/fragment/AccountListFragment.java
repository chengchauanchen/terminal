package cn.vsx.vc.fragment;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Department;
import cn.vsx.hamster.terminalsdk.model.NewMemberResponse;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveAccountSelectedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivegUpdatePoliceMemberHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.AccountListAdapter;
import cn.vsx.vc.adapter.GroupCatalogAdapter;
import cn.vsx.vc.model.CatalogBean;
import cn.vsx.vc.model.ContactItemBean;
import cn.vsx.vc.receiveHandle.OnSearchListener;
import cn.vsx.vc.receiveHandle.ReceiveRemoveSelectedMemberHandler;
import cn.vsx.vc.receiveHandle.ReceiveTransponBackPressedHandler;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.WuTieUtil;

/**
 * 显示账号列表
 */
public class AccountListFragment extends BaseFragment{

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerview;

    private RecyclerView mRlSearch;
    private ImageView mIvSearch;
    private GroupCatalogAdapter mCatalogAdapter;
    private List<CatalogBean> mCatalogDatas = new ArrayList<>();

    private List<ContactItemBean> mData = new ArrayList<>();

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private AccountListAdapter accountListAdapter;
    private List<Integer> selectedMemberNos = new ArrayList<>();

    private OnSearchListener onSearchListener;

    @Override
    public int getContentViewId(){
        return R.layout.layout_member_list;
    }

    @Override
    public void initView(){
        mSwipeRefreshLayout = mRootView.findViewById(R.id.swipeRefreshLayout);
        mRecyclerview = mRootView.findViewById(R.id.recyclerview);
        mRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        accountListAdapter = new AccountListAdapter(getContext(), mData);
        mRecyclerview.setAdapter(accountListAdapter);

        //搜索布局
        mRlSearch = mRootView.findViewById(R.id.parent_recyclerview);
        mIvSearch = mRootView.findViewById(R.id.iv_search);
        mRlSearch.setLayoutManager(new LinearLayoutManager(getActivity(), OrientationHelper.HORIZONTAL,false));
        mCatalogDatas.clear();
        mCatalogDatas.add(WuTieUtil.addRootDetpCatalogNames());
        mCatalogAdapter=new GroupCatalogAdapter(getActivity(),mCatalogDatas);
        mCatalogAdapter.setOnItemClick(searchItemClickListener);
        mRlSearch.setAdapter(mCatalogAdapter);
    }

    @Override
    public void initListener(){
        TerminalFactory.getSDK().registReceiveHandler(receiveAccountSelectedHandler);
        TerminalFactory.getSDK().registReceiveHandler(receivegUpdatePoliceMemberHandler);
        TerminalFactory.getSDK().registReceiveHandler(receiveRemoveSelectedMemberHandler);
        TerminalFactory.getSDK().registReceiveHandler(receiveTransponBackPressedHandler);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            int deptId = TerminalFactory.getSDK().getParam(Params.DEP_ID,0);
            String deptName = TerminalFactory.getSDK().getParam(Params.DEP_NAME,"");
            updateSearchCatalogView(deptId,deptName,true);
            TerminalFactory.getSDK().getConfigManager().updataPhoneMemberInfo();
            mHandler.postDelayed(()-> mSwipeRefreshLayout.setRefreshing(false),1200);
        });

        accountListAdapter.setItemClickListener((type, position) -> {
            if(type == Constants.TYPE_ACCOUNT){
                Account account = (Account) mData.get(position).getBean();
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiveAccountSelectedHandler.class,account,!account.isChecked());

            }else if(type == Constants.TYPE_DEPARTMENT){
                Department department = (Department) mData.get(position).getBean();
                updateSearchCatalogView(department.getId(), department.getName(),false);
                TerminalFactory.getSDK().getConfigManager().updatePoliceMember(department.getId(),department.getName());
            }
//            accountListAdapter.notifyDataSetChanged();
        });

        mIvSearch.setOnClickListener(v -> {
            if(onSearchListener!=null){
                onSearchListener.onSearch();
            }
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

    /**
     * 点击搜索布局中的部门item
     */
    private  GroupCatalogAdapter.ItemClickListener searchItemClickListener = (view, position) -> {
        if(position>=0 && position < mCatalogDatas.size()){
            List<CatalogBean> catalogBeans = new ArrayList<>(mCatalogDatas.subList(0, position + 1));
            mCatalogDatas.clear();
            mCatalogDatas.addAll(catalogBeans);
            if(mCatalogAdapter!=null){
                mCatalogAdapter.setData(mCatalogDatas);
                mCatalogAdapter.notifyDataSetChanged();
            }
            TerminalFactory.getSDK().getConfigManager().updatePoliceMember(mCatalogDatas.get(position).getId(),mCatalogDatas.get(position).getName());
        }
    };
    /**
     * 返回按键-返回上一级部门
     */
    private ReceiveTransponBackPressedHandler receiveTransponBackPressedHandler = new ReceiveTransponBackPressedHandler(){
        @Override
        public void handler(Activity activity){
            mHandler.post(() -> {
                if(isVisible()){
                    if(mCatalogDatas.size() > 1){
                        //返回到上一级
                        mCatalogDatas.remove(mCatalogDatas.size()-1);
                        if(mCatalogAdapter !=null){
                            mCatalogAdapter.notifyDataSetChanged();
                        }
                        int position = mCatalogDatas.size()-1;
                        TerminalFactory.getSDK().getConfigManager().updatePoliceMember(mCatalogDatas.get(position).getId(),mCatalogDatas.get(position).getName());
                        mRecyclerview.scrollToPosition(0);

                    }else{
                        if(activity!=null){
                            activity.finish();
                        }
                    }
                }
            });
        }
    };

    /**
     * 改变搜素布局中部门导航的显示
     * @param deptId
     * @param deptName
     */
    private void updateSearchCatalogView(int deptId,String deptName,boolean isClear){
        if(isClear){
            mCatalogDatas.clear();
        }
        CatalogBean catalogBean = WuTieUtil.getCatalogBean( deptId, deptName);
        if(!mCatalogDatas.contains(catalogBean)){
            mCatalogDatas.add(catalogBean);
        }
        if(mCatalogAdapter!=null){
            mCatalogAdapter.setData(mCatalogDatas);
            mCatalogAdapter.notifyDataSetChanged();
        }
    }

    public void setOnSearchListener(OnSearchListener onSearchListener){
        this.onSearchListener = onSearchListener;

    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        TerminalFactory.getSDK().unregistReceiveHandler(receiveRemoveSelectedMemberHandler);
        TerminalFactory.getSDK().unregistReceiveHandler(receivegUpdatePoliceMemberHandler);
        TerminalFactory.getSDK().unregistReceiveHandler(receiveAccountSelectedHandler);
        TerminalFactory.getSDK().unregistReceiveHandler(receiveTransponBackPressedHandler);
    }
}

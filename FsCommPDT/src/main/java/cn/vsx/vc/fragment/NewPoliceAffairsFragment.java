package cn.vsx.vc.fragment;

import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Department;
import cn.vsx.hamster.terminalsdk.model.NewMemberResponse;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateFrequentMemberHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivegUpdatePoliceMemberHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.NewMainActivity;
import cn.vsx.vc.adapter.ContactAdapter;
import cn.vsx.vc.model.CatalogBean;
import cn.vsx.vc.model.ContactItemBean;
import cn.vsx.vc.utils.Constants;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * 警务通
 */
public class NewPoliceAffairsFragment extends BaseFragment {

//    @Bind(R.id.catalog_recyclerview)
//    RecyclerView mCatalogRecyclerview;
    @Bind(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.recyclerview)
    RecyclerView mRecyclerview;

    private NewMainActivity mActivity;
    private List<CatalogBean> catalogNames=new ArrayList<>();

    private List<ContactItemBean> mDatas=new ArrayList<>();
    private List<ContactItemBean> accountAndDeps=new ArrayList<>();
    private List<ContactItemBean> allFrequentContacts =new ArrayList<>();
    private List<ContactItemBean> lastGroupDatas=new ArrayList<>();
    private Handler myHandler = new Handler(Looper.getMainLooper());
//    private CatalogAdapter mCatalogAdapter;
    private ContactAdapter mContactAdapter;

    private ReceiveUpdateFrequentMemberHandler receiveUpdateFrequentMemberHandler = new ReceiveUpdateFrequentMemberHandler(){
        @Override
        public void handle(){
            updateFrequentContacts();
        }
    };

    private ReceivegUpdatePoliceMemberHandler receivegUpdatePoliceMemberHandler = new ReceivegUpdatePoliceMemberHandler(){
        @Override
        public void handler(int depId,String depName,List<Department> departments, List<Account> accounts){
            updateData(departments,accounts);
            myHandler.post(()->{
                mDatas.clear();
                if(depId == TerminalFactory.getSDK().getParam(Params.DEP_ID,0)){
                    mDatas.addAll(allFrequentContacts);
                }
                mDatas.addAll(accountAndDeps);
                if(mContactAdapter != null){
                    mContactAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    @Override
    public int getContentViewId() {
        return R.layout.fragment_new_police_affairs;
    }

    @Override
    public void initView() {
//        mCatalogRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity(), OrientationHelper.HORIZONTAL,false));
//        mCatalogAdapter=new CatalogAdapter(getActivity(),catalogNames);
//        mCatalogRecyclerview.setAdapter(mCatalogAdapter);

        mRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));
        mContactAdapter=new ContactAdapter(getActivity(),mDatas,catalogNames,true);
        mRecyclerview.setAdapter(mContactAdapter);

    }

    @Override
    public void initData() {
        mActivity= (NewMainActivity) getActivity();
        NewMemberResponse mMemberResponse = TerminalFactory.getSDK().getConfigManager().getPhoneMemeberInfo();
        if(null != mMemberResponse){
            initFrequentContacts();
            CatalogBean memberCatalogBean = new CatalogBean(TerminalFactory.getSDK().getParam(Params.DEP_NAME,""),TerminalFactory.getSDK().getParam(Params.DEP_ID,0));
            catalogNames.add(memberCatalogBean);
            List<Department> deptList = mMemberResponse.getDeptList();
            List<Account> accountList = mMemberResponse.getAccountDtos();
            updateData(deptList,accountList);
            myHandler.post(()->{
                mDatas.clear();
                mDatas.addAll(allFrequentContacts);
                mDatas.addAll(accountAndDeps);
                if(mContactAdapter != null){
                    mContactAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void updateFrequentContacts(){
        initFrequentContacts();
        myHandler.post(()->{
            mDatas.clear();
            mDatas.addAll(allFrequentContacts);
            mDatas.addAll(accountAndDeps);
            if(mContactAdapter != null){
                mContactAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * 添加常用联系人
     */
    private void initFrequentContacts(){
        myHandler.post(()->{
            allFrequentContacts.clear();
            List<Account> frequentContacts = TerminalFactory.getSDK().getConfigManager().getFrequentContacts();
            logger.error("常用联系人："+frequentContacts);
            if(frequentContacts != null && !frequentContacts.isEmpty()){
                ContactItemBean memberCatalogBean = new ContactItemBean();
                memberCatalogBean.setName("常用联系人");
                memberCatalogBean.setType(Constants.TYPE_FREQUENT);
                allFrequentContacts.add(memberCatalogBean);

                for(Account account : frequentContacts){
                    ContactItemBean<Account> bean = new ContactItemBean<>();
                    bean.setType(Constants.TYPE_ACCOUNT);
                    bean.setBean(account);
                    allFrequentContacts.add(bean);
                }
            }
        });
    }

    /**
     * 设置数据
     */
    private void updateData(List<Department> deptList,List<Account> accountList){
        accountAndDeps.clear();
        //添加标题
        ContactItemBean<Object> Title = new ContactItemBean<>();
        Title.setType(Constants.TYPE_TITLE);
        Title.setBean(new Object());
        accountAndDeps.add(Title);
        if(null != accountList && !accountList.isEmpty()){
            //添加成员
            for(Account account : accountList){
                ContactItemBean<Account> contactItemBean = new ContactItemBean<>();
                contactItemBean.setType(Constants.TYPE_ACCOUNT);
                contactItemBean.setBean(account);
                accountAndDeps.add(contactItemBean);
            }
        }
        if(null != deptList && !deptList.isEmpty()){
            for(Department department : deptList){
                ContactItemBean<Department> contactItemBean = new ContactItemBean<>();
                contactItemBean.setType(Constants.TYPE_DEPARTMENT);
                contactItemBean.setBean(department);
                accountAndDeps.add(contactItemBean);
            }
        }
    }




    @Override
    public void initListener() {

        MyTerminalFactory.getSDK().registReceiveHandler(receivegUpdatePoliceMemberHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateFrequentMemberHandler);

        mContactAdapter.setCatalogItemClickListener((view, position) -> {

//            if(position == catalogNames.size()-2){
//                //返回到上一级
//                catalogNames.remove(catalogNames.size()-1);
//                mDatas.clear();
//                mDatas.addAll(lastGroupDatas);
//                if(mContactAdapter !=null){
//                    mContactAdapter.notifyDataSetChanged();
//                }
//
//                mRecyclerview.scrollToPosition(0);
//            }else {
                List<CatalogBean> catalogBeans = new ArrayList<>(catalogNames.subList(0, position + 1));
                catalogNames.clear();
                catalogNames.addAll(catalogBeans);
                TerminalFactory.getSDK().getConfigManager().updatePoliceMember(catalogNames.get(position).getId(),catalogNames.get(position).getName());
//            }
        });


        mContactAdapter.setOnItemClickListener((view, depId, name, type) -> {
            if (type==Constants.TYPE_DEPARTMENT){
                saveLastGroupData();
                CatalogBean catalogBean = new CatalogBean(name,depId);
                catalogNames.add(catalogBean);
                TerminalFactory.getSDK().getConfigManager().updatePoliceMember(depId,name);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            initFrequentContacts();
            catalogNames.clear();
            CatalogBean memberCatalogBean = new CatalogBean(TerminalFactory.getSDK().getParam(Params.DEP_NAME,""),TerminalFactory.getSDK().getParam(Params.DEP_ID,0));
            catalogNames.add(memberCatalogBean);
            TerminalFactory.getSDK().getConfigManager().updataPhoneMemberInfo();
            myHandler.postDelayed(() -> {
                // 加载完数据设置为不刷新状态，将下拉进度收起来
                swipeRefreshLayout.setRefreshing(false);
                // 加载完数据设置为不刷新状态，将下拉进度收起来

            }, 1200);
        });
    }

    private void saveLastGroupData(){
        lastGroupDatas.clear();
        for(ContactItemBean contactItemBean : mDatas){
            lastGroupDatas.add((ContactItemBean) contactItemBean.clone());
        }
    }


    @Override
    public void onDestroy() {
        MyTerminalFactory.getSDK().unregistReceiveHandler(receivegUpdatePoliceMemberHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateFrequentMemberHandler);
        super.onDestroy();
    }

    /**
     * 返回操作
     */
   public void onBack(){
       if(catalogNames.size() > 1){
           //返回到上一级
           catalogNames.remove(catalogNames.size()-1);
           mDatas.clear();
           mDatas.addAll(lastGroupDatas);
           if(mContactAdapter !=null){
               mContactAdapter.notifyDataSetChanged();
           }
           mRecyclerview.scrollToPosition(0);

       }else{
           mActivity.exit();
       }
   }
}

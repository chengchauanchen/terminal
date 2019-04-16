package cn.vsx.vc.fragment;

import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Department;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.model.NewMemberResponse;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivegUpdatePoliceMemberHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.NewMainActivity;
import cn.vsx.vc.adapter.CatalogAdapter;
import cn.vsx.vc.adapter.ContactAdapter;
import cn.vsx.vc.model.CatalogBean;
import cn.vsx.vc.model.ContactItemBean;
import cn.vsx.vc.receiveHandle.ReceiverShowPersonFragmentHandler;
import cn.vsx.vc.utils.Constants;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * 警务通
 */
public class NewPoliceAffairsFragment extends BaseFragment {

    @Bind(R.id.catalog_recyclerview)
    RecyclerView mCatalogRecyclerview;
    @Bind(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.recyclerview)
    RecyclerView mRecyclerview;

    private NewMainActivity mActivity;
    private List<CatalogBean> catalogNames=new ArrayList<>();

    private List<ContactItemBean> mDatas=new ArrayList<>();
    private List<ContactItemBean> lastGroupDatas=new ArrayList<>();
    private Handler myHandler = new Handler();
    private CatalogAdapter mCatalogAdapter;
    private ContactAdapter mContactAdapter;


    private ReceivegUpdatePoliceMemberHandler receivegUpdatePoliceMemberHandler = new ReceivegUpdatePoliceMemberHandler(){
        @Override
        public void handler(int depId,String depName,List<Department> departments, List<Account> accounts){
            updateData(depId,depName,departments,accounts);
        }
    };

    @Override
    public int getContentViewId() {
        return R.layout.fragment_new_police_affairs;
    }

    @Override
    public void initView() {
        mCatalogRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity(), OrientationHelper.HORIZONTAL,false));
        mCatalogAdapter=new CatalogAdapter(getActivity(),catalogNames);
        mCatalogRecyclerview.setAdapter(mCatalogAdapter);

        mRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));
        mContactAdapter=new ContactAdapter(getActivity(),mDatas,true);
        mRecyclerview.setAdapter(mContactAdapter);

    }

    @Override
    public void initData() {
        mActivity= (NewMainActivity) getActivity();
        NewMemberResponse mMemberResponse = TerminalFactory.getSDK().getConfigManager().getPhoneMemeberInfo();
        if(null != mMemberResponse){
            CatalogBean memberCatalogBean = new CatalogBean(TerminalFactory.getSDK().getParam(Params.DEP_NAME,""),TerminalFactory.getSDK().getParam(Params.DEP_ID,0));
            catalogNames.add(memberCatalogBean);
            List<Department> deptList = mMemberResponse.getDeptList();
            List<Account> accountList = mMemberResponse.getAccountDtos();
            updateData(TerminalFactory.getSDK().getParam(Params.DEP_ID,0),TerminalFactory.getSDK().getParam(Params.DEP_NAME,""),deptList,accountList);
        }
    }


    /**
     * 设置数据
     */
    private void updateData(int depId,String depName,List<Department> deptList,List<Account> accountList){

        mDatas.clear();
        //根部门先设置常用联系人
        if(depId == TerminalFactory.getSDK().getParam(Params.DEP_ID,0)){
            for(Member member : TerminalFactory.getSDK().getConfigManager().getFrequentContacts()){
                ContactItemBean<Member> bean = new ContactItemBean<>();
                bean.setType(Constants.TYPE_USER);
                bean.setBean(member);
                mDatas.add(bean);
            }
        }

        if(null != accountList && !accountList.isEmpty()){
            //添加成员
            for(Account account : accountList){
                ContactItemBean<Account> contactItemBean = new ContactItemBean<>();
                contactItemBean.setType(Constants.TYPE_ACCOUNT);
                contactItemBean.setBean(account);
                mDatas.add(contactItemBean);
            }
        }
        if(null != deptList && !deptList.isEmpty()){
            for(Department department : deptList){
                ContactItemBean<Department> contactItemBean = new ContactItemBean<>();
                contactItemBean.setType(Constants.TYPE_DEPARTMENT);
                contactItemBean.setBean(department);
                mDatas.add(contactItemBean);
            }
        }
        myHandler.post(()->{
            if(null != mContactAdapter){
                mContactAdapter.notifyDataSetChanged();
            }
            if(mCatalogAdapter !=null){
                mCatalogAdapter.notifyDataSetChanged();
            }
        });

    }




    @Override
    public void initListener() {

        MyTerminalFactory.getSDK().registReceiveHandler(receivegUpdatePoliceMemberHandler);

        mCatalogAdapter.setOnItemClick((view, position) -> {

            if(position == catalogNames.size()-2){
                //返回到上一级
                catalogNames.remove(catalogNames.size()-1);
                mDatas.clear();
                mDatas.addAll(lastGroupDatas);
                if(mContactAdapter !=null){
                    mContactAdapter.notifyDataSetChanged();
                }
                if(mCatalogAdapter !=null){
                    mCatalogAdapter.notifyDataSetChanged();
                }
                mRecyclerview.scrollToPosition(0);
            }else {
                List<CatalogBean> catalogBeans = new ArrayList<>(catalogNames.subList(0, position + 1));
                catalogNames.clear();
                catalogNames.addAll(catalogBeans);
                TerminalFactory.getSDK().getConfigManager().updatePoliceMember(catalogNames.get(position).getId(),catalogNames.get(position).getName());
            }
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

    private long lastSearchTime;
    /**
     * 搜索
     */
    @OnClick(R.id.iv_search)
    public void onViewClicked() {
        if(System.currentTimeMillis() - lastSearchTime<1000){
            return;
        }
        List<Member> memberList=TerminalFactory.getSDK().getConfigManager().getPhoneMembers();
        if (memberList!=null) {
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverShowPersonFragmentHandler.class, memberList);
        }else {
            ToastUtil.showToast(getActivity(),getString(R.string.text_no_general_police_users));
        }
        lastSearchTime = System.currentTimeMillis();
    }

    @Override
    public void onDestroy() {
        MyTerminalFactory.getSDK().unregistReceiveHandler(receivegUpdatePoliceMemberHandler);
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
           if(mCatalogAdapter !=null){
               mCatalogAdapter.notifyDataSetChanged();
           }
           mRecyclerview.scrollToPosition(0);

       }else{
           mActivity.exit();
       }
   }
}

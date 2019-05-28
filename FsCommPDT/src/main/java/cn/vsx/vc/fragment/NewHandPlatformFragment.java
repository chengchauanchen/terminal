package cn.vsx.vc.fragment;

import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Department;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdatePDTMemberHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.NewMainActivity;
import cn.vsx.vc.adapter.ContactAdapter;
import cn.vsx.vc.model.CatalogBean;
import cn.vsx.vc.model.ContactItemBean;
import cn.vsx.vc.utils.CommonGroupUtil;
import cn.vsx.vc.utils.Constants;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * 电台
 */
public class NewHandPlatformFragment extends BaseFragment {

//    @Bind(R.id.catalog_recyclerview)
//    RecyclerView mCatalogRecyclerview;

    SwipeRefreshLayout swipeRefreshLayout;

    RecyclerView mRecyclerview;

    private NewMainActivity mActivity;
    //上部分
    private List<CatalogBean> catalogNames = new ArrayList<>();

    //下部分
    private List<ContactItemBean> mDatas = new ArrayList<>();

    private Handler myHandler = new Handler();
//    private CatalogAdapter mCatalogAdapter;//上部分横向
    private ContactAdapter mContactAdapter;//下部门竖直
    private List<ContactItemBean> lastGroupDatas=new ArrayList<>();


    private ReceiveUpdatePDTMemberHandler receiveUpdatePDTMemberHandler = new ReceiveUpdatePDTMemberHandler(){
        @Override
        public void handler(int depId,String depName,List<Department> departments, List<Member> members){
            updateData(depId,depName,departments,members);
        }
    };

    /**
     * 更新配置信息
     */
    private ReceiveUpdateConfigHandler receiveUpdateConfigHandler = () -> {//更新当前组
        CommonGroupUtil.setCatchGroupIdList(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0));
        myHandler.post(() -> {

        });
    };



    @Override
    public int getContentViewId() {
        return R.layout.fragment_new_police_affairs;
    }

    @Override
    public void initView() {
//        mCatalogRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity(), OrientationHelper.HORIZONTAL, false));
//        mCatalogAdapter = new CatalogAdapter(getActivity(), catalogNames);
//        mCatalogRecyclerview.setAdapter(mCatalogAdapter);
        mRecyclerview = (RecyclerView) mRootView.findViewById(R.id.recyclerview);
        swipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipeRefreshLayout);
        mRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));
        mContactAdapter = new ContactAdapter(getActivity(), mDatas,catalogNames,Constants.TYPE_CONTRACT_PDT);
        mRecyclerview.setAdapter(mContactAdapter);

    }

    @Override
    public void initData() {
        mActivity = (NewMainActivity) getActivity();
        TerminalFactory.getSDK().getConfigManager().updataPDTMemberInfo();
        CatalogBean memberCatalogBean = new CatalogBean(TerminalFactory.getSDK().getParam(Params.DEP_NAME,""),TerminalFactory.getSDK().getParam(Params.DEP_ID,0));
        catalogNames.add(memberCatalogBean);
    }


    /**
     * 设置数据
     */
    private void updateData(int depId,String depName,List<Department> deptList,List<Member> memberList){

        mDatas.clear();
        ContactItemBean<Object> Title = new ContactItemBean<>();
        Title.setType(Constants.TYPE_TITLE);
        Title.setBean(new Object());
        mDatas.add(Title);
        if(null != memberList && !memberList.isEmpty()){

            //添加成员
            for(Member member : memberList){
                ContactItemBean<Member> contactItemBean = new ContactItemBean<>();
                contactItemBean.setType(Constants.TYPE_USER);
                contactItemBean.setBean(member);
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
            if(mContactAdapter !=null){
                mContactAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void initListener() {

        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdatePDTMemberHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdatePDTMemberHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateConfigHandler);
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
                TerminalFactory.getSDK().getConfigManager().updatePDTMember(catalogNames.get(position).getId(),catalogNames.get(position).getName());
//            }
        });


        mContactAdapter.setOnItemClickListener((view, depId,depName, type) -> {
            if (type == Constants.TYPE_DEPARTMENT) {
                saveLastGroupData();
                CatalogBean memberCatalogBean = new CatalogBean(depName,depId);
                catalogNames.add(memberCatalogBean);
                TerminalFactory.getSDK().getConfigManager().updatePDTMember(depId,depName);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            catalogNames.clear();
            CatalogBean memberCatalogBean = new CatalogBean(TerminalFactory.getSDK().getParam(Params.DEP_NAME,""),TerminalFactory.getSDK().getParam(Params.DEP_ID,0));
            catalogNames.add(memberCatalogBean);
            TerminalFactory.getSDK().getConfigManager().updataPDTMemberInfo();
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
        super.onDestroy();
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdatePDTMemberHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdatePDTMemberHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateConfigHandler);

    }

    /**
     * 返回操作
     */
    public void onBack(){
        if(catalogNames.size() > 1){
            //返回到上一级
            catalogNames.remove(catalogNames.size()-1);
//            mDatas.clear();
//            mDatas.addAll(lastGroupDatas);
            if(mContactAdapter !=null){
                mContactAdapter.notifyDataSetChanged();
            }
            int position = catalogNames.size()-1;
            TerminalFactory.getSDK().getConfigManager().updatePDTMember(catalogNames.get(position).getId(),catalogNames.get(position).getName());
            mRecyclerview.scrollToPosition(0);

        }else{
            mActivity.exit();
        }
    }
}

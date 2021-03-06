package cn.vsx.vc.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Department;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.NewMainActivity;
import cn.vsx.vc.adapter.ContactAdapter;
import cn.vsx.vc.model.CatalogBean;
import cn.vsx.vc.model.ContactItemBean;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.WuTieUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.receiveHandler.ReceiveGetTerminalDeviceHandler;

/**
 * 通讯录显示设备
 */
public class TerminalFragment extends BaseFragment{
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TYPE = "type";
    private static final String TERMINALMEMBERTYPES = "terminalMemberTypes";

    private ArrayList<String> terminalMemberTypes;

    RecyclerView mCatalogRecyclerview;

    SwipeRefreshLayout swipeRefreshLayout;

    RecyclerView mRecyclerview;

    private NewMainActivity mActivity;
    private List<CatalogBean> catalogNames=new ArrayList<>();

    private List<ContactItemBean> mDatas=new ArrayList<>();
    private List<ContactItemBean> lastGroupDatas=new ArrayList<>();
    private Handler myHandler = new Handler();
    private ContactAdapter mContactAdapter;

    public TerminalFragment(){
    }

    public static TerminalFragment newInstance( ArrayList<String> terminalMemberTypes){
        TerminalFragment fragment = new TerminalFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(TERMINALMEMBERTYPES, terminalMemberTypes);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            terminalMemberTypes = getArguments().getStringArrayList(TERMINALMEMBERTYPES);
        }
    }

    @Override
    public int getContentViewId(){
        return R.layout.fragment_new_police_affairs;
    }

    @Override
    public void initView(){
        mRecyclerview = (RecyclerView) mRootView.findViewById(R.id.recyclerview);
        swipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipeRefreshLayout);
        mCatalogRecyclerview = (RecyclerView) mRootView.findViewById(R.id.catalog_recyclerview);

        mRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));
        mContactAdapter = new ContactAdapter(getActivity(), mDatas,catalogNames,Constants.TYPE_CONTRACT_TERMINAL,terminalMemberTypes);
        mRecyclerview.setAdapter(mContactAdapter);
    }

    @Override
    public void initListener(){
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGetTerminalDeviceHandler);
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
            if(position>=0 && position < catalogNames.size()){
                List<CatalogBean> catalogBeans = new ArrayList<>(catalogNames.subList(0, position+1));
                catalogNames.clear();
                catalogNames.addAll(catalogBeans);
                TerminalFactory.getSDK().getDataManager().getDeptDeviceList(catalogNames.get(position).getId(),terminalMemberTypes);
            }
            //            }
        });

        mContactAdapter.setOnItemClickListener((view, depId,depName, type) -> {
            if (type == Constants.TYPE_DEPARTMENT) {
                saveLastGroupData();
                CatalogBean memberCatalogBean = new CatalogBean(depName,depId);
                if(!catalogNames.contains(memberCatalogBean)){
                    catalogNames.add(memberCatalogBean);
                }
                TerminalFactory.getSDK().getDataManager().getDeptDeviceList(depId,terminalMemberTypes);
            }
        });
        swipeRefreshLayout.setOnRefreshListener(() -> {
            catalogNames.clear();
            catalogNames.add(WuTieUtil.addRootDetpCatalogNames());
            TerminalFactory.getSDK().getDataManager().getDeptDeviceList(TerminalFactory.getSDK().getParam(Params.DEP_ID, 0),terminalMemberTypes);
            myHandler.postDelayed(() -> {
                // 加载完数据设置为不刷新状态，将下拉进度收起来
                swipeRefreshLayout.setRefreshing(false);
                // 加载完数据设置为不刷新状态，将下拉进度收起来

            }, 1200);
        });
    }

    @Override
    public void initData(){
        mActivity= (NewMainActivity) getActivity();
        catalogNames.clear();
        catalogNames.add(WuTieUtil.addRootDetpCatalogNames());
        TerminalFactory.getSDK().getDataManager().getDeptDeviceList(TerminalFactory.getSDK().getParam(Params.DEP_ID, 0),terminalMemberTypes);
    }

    //更新LTE成员信息
    private ReceiveGetTerminalDeviceHandler receiveGetTerminalDeviceHandler = new ReceiveGetTerminalDeviceHandler() {
        @Override
        public void handler(int depId, List<String> type, List<Department> departments, List<Member> members) {

            if(TextUtils.equals(terminalMemberTypes.toString(),type.toString())){
                updateData(departments,members);
            }
        }
    };

    private void updateData(List<Department> deptList,List<Member> memberList){
        mDatas.clear();
        ContactItemBean<Object> Title = new ContactItemBean<>();
        Title.setType(Constants.TYPE_TITLE);
        Title.setBean(new Object());
        mDatas.add(Title);
        if(null != memberList && !memberList.isEmpty()){

            //添加成员
            for(Member member : memberList){
                ContactItemBean<Member> contactItemBean = new ContactItemBean<>();
                contactItemBean.setType(Constants.TYPE_TERMINAL);
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

    private void saveLastGroupData(){
        lastGroupDatas.clear();
        for(ContactItemBean contactItemBean : mDatas){
            lastGroupDatas.add((ContactItemBean) contactItemBean.clone());
        }
    }

    @Override
    public void onDestroy(){
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetTerminalDeviceHandler);
        super.onDestroy();
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
            TerminalFactory.getSDK().getDataManager().getDeptDeviceList(catalogNames.get(position).getId(),terminalMemberTypes);
            mRecyclerview.scrollToPosition(0);

        }else{
            mActivity.exit();
        }
    }
}

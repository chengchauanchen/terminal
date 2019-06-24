package cn.vsx.vc.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Department;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupSelectedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivegUpdateGroupHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.GroupCatalogAdapter;
import cn.vsx.vc.adapter.MemberListAdapter;
import cn.vsx.vc.model.CatalogBean;
import cn.vsx.vc.model.ContactItemBean;
import cn.vsx.vc.receiveHandle.OnSearchListener;
import cn.vsx.vc.receiveHandle.ReceiveRemoveSelectedMemberHandler;
import cn.vsx.vc.receiveHandle.ReceiveTransponBackPressedHandler;
import cn.vsx.vc.utils.Constants;

/**
 * 显示群组列表
 */
public class GroupListFragment extends BaseFragment{

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerview;

    private RecyclerView mRlSearch;
    private ImageView mIvSearch;
    private GroupCatalogAdapter mCatalogAdapter;
    private List<CatalogBean> mCatalogDatas = new ArrayList<>();

    private static final String TYPE = "type";
    private String type = "type";
    private List<ContactItemBean> mData = new ArrayList<>();

    private Handler mHandler = new Handler();
    private MemberListAdapter memberListAdapter;
    private List<Integer> selectedMemberNos = new ArrayList<>();
    private OnSearchListener onSearchListener;

    public static GroupListFragment newInstance(String type){
        GroupListFragment memberListFragment = new GroupListFragment();
        Bundle bundle = new Bundle();
        bundle.putString(TYPE, type);
        memberListFragment.setArguments(bundle);
        return memberListFragment;
    }

    @Override
    public int getContentViewId(){
        return R.layout.layout_member_list;
    }

    @Override
    public void initView(){
        mSwipeRefreshLayout = mRootView.findViewById(R.id.swipeRefreshLayout);
        mRecyclerview = mRootView.findViewById(R.id.recyclerview);
        mRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        memberListAdapter = new MemberListAdapter(getContext(), mData);
        mRecyclerview.setAdapter(memberListAdapter);
        //搜索布局
        mRlSearch = mRootView.findViewById(R.id.parent_recyclerview);
        mIvSearch = mRootView.findViewById(R.id.iv_search);
        CatalogBean memberCatalogBean = new CatalogBean(TerminalFactory.getSDK().getParam(Params.DEP_NAME,""),TerminalFactory.getSDK().getParam(Params.DEP_ID,0));
        mRlSearch.setLayoutManager(new LinearLayoutManager(getActivity(), OrientationHelper.HORIZONTAL,false));
        mCatalogDatas.clear();
        mCatalogDatas.add(memberCatalogBean);
        mCatalogAdapter=new GroupCatalogAdapter(getActivity(),mCatalogDatas);
        mCatalogAdapter.setOnItemClick(searchItemClickListener);
        mRlSearch.setAdapter(mCatalogAdapter);
    }

    @Override
    public void initListener(){
        TerminalFactory.getSDK().registReceiveHandler(receiveGroupSelectedHandler);
        TerminalFactory.getSDK().registReceiveHandler(receivegUpdateGroupHandler);
        TerminalFactory.getSDK().registReceiveHandler(receiveRemoveSelectedMemberHandler);
        TerminalFactory.getSDK().registReceiveHandler(receiveTransponBackPressedHandler);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            int deptId = TerminalFactory.getSDK().getParam(Params.DEP_ID,0);
            String deptName = TerminalFactory.getSDK().getParam(Params.DEP_NAME,"");
            updateSearchCatalogView(deptId,deptName,true);
            TerminalFactory.getSDK().getConfigManager().updateGroup(deptId,deptName);
            mHandler.postDelayed(()-> mSwipeRefreshLayout.setRefreshing(false),1200);
        });

        memberListAdapter.setItemClickListener((type, position) -> {
            if(type == Constants.TYPE_FOLDER){
                Department department = (Department) mData.get(position).getBean();
                updateSearchCatalogView(department.getId(), department.getName(),false);
                TerminalFactory.getSDK().getConfigManager().updateGroup(department.getId(), department.getName());
            }else if(type == Constants.TYPE_GROUP){
                Group group = (Group) mData.get(position).getBean();
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiveGroupSelectedHandler.class,group,!group.isChecked());
            }
//            memberListAdapter.notifyDataSetChanged();
        });
        mIvSearch.setOnClickListener(v -> {
           if(onSearchListener!=null){
               onSearchListener.onSearch();
           }
        });
    }

    @Override
    public void initData(){
        TerminalFactory.getSDK().getConfigManager().updateGroup(TerminalFactory.getSDK().getParam(Params.DEP_ID, 0), TerminalFactory.getSDK().getParam(Params.DEP_NAME, ""));
    }

    private ReceiveRemoveSelectedMemberHandler receiveRemoveSelectedMemberHandler = contactItemBean -> {
        // TODO: 2019/4/22 如果删除的是子部门的，需要更改子部门数据的状态
        if(contactItemBean.getBean() instanceof Group){
            Group bean = (Group) contactItemBean.getBean();
            Iterator<ContactItemBean> iterator = mData.iterator();
            while(iterator.hasNext()){
                ContactItemBean next = iterator.next();
                if(next.getBean() instanceof Group){
                    Group group = (Group) next.getBean();
                    if(bean.getNo() == group.getNo()){
                        group.setChecked(false);
                    }
                }
            }
            //
            if(selectedMemberNos.contains(bean.getNo())){
                selectedMemberNos.remove((Integer)bean.getNo());
            }
        }
        mHandler.post(()->memberListAdapter.notifyDataSetChanged());
    };



    private ReceiveGroupSelectedHandler receiveGroupSelectedHandler = (group, selected) -> {
        // TODO: 2019/4/22 如果选中的是子部门的，需要修改子部门数据的状态
        if(selected){
            if(!selectedMemberNos.contains(group.getNo())){
                selectedMemberNos.add((Integer)group.getNo());
            }
        }else {
            if(selectedMemberNos.contains(group.getNo())){
                selectedMemberNos.remove((Integer)group.getNo());
            }
        }
        for(ContactItemBean bean : mData){
            if(bean.getBean() instanceof Group){
                if(((Group) bean.getBean()).getNo() == group.getNo()){
                    ((Group) bean.getBean()).setChecked(selected);
                    break;
                }
            }
        }
        mHandler.post(()-> memberListAdapter.notifyDataSetChanged());
    };

    private ReceivegUpdateGroupHandler receivegUpdateGroupHandler = new ReceivegUpdateGroupHandler(){
        @Override
        public void handler(int depId, String depName, List<Department> departments, List<Group> groups){
            mHandler.post(() -> updateData( depId, departments, groups));
        }
    };

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
            TerminalFactory.getSDK().getConfigManager().updateGroup(mCatalogDatas.get(position).getId(),mCatalogDatas.get(position).getName());
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
                        TerminalFactory.getSDK().getConfigManager().updateGroup(mCatalogDatas.get(position).getId(),mCatalogDatas.get(position).getName());
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

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        TerminalFactory.getSDK().unregistReceiveHandler(receiveRemoveSelectedMemberHandler);
        TerminalFactory.getSDK().unregistReceiveHandler(receivegUpdateGroupHandler);
        TerminalFactory.getSDK().unregistReceiveHandler(receiveGroupSelectedHandler);
        TerminalFactory.getSDK().unregistReceiveHandler(receiveTransponBackPressedHandler);
    }



    private void updateData(int depId, List<Department> departments, List<Group> groups){
        mData.clear();
        for(Group group : groups){
            ContactItemBean<Group> contactItemBean = new ContactItemBean<>();
            contactItemBean.setBean(group);
            contactItemBean.setType(Constants.TYPE_GROUP);
            mData.add(contactItemBean);
        }
        for(Department department : departments){
            ContactItemBean<Department> contactItemBean = new ContactItemBean<>();
            contactItemBean.setBean(department);
            contactItemBean.setType(Constants.TYPE_FOLDER);
            mData.add(contactItemBean);
        }
        //将之前选中的人重新选中
        for(Integer selectedMemberNo : selectedMemberNos){
            for(ContactItemBean contactItemBean : mData){
                if(contactItemBean.getBean() instanceof Group){
                    if(selectedMemberNo == ((Group) contactItemBean.getBean()).no){
                        ((Group) contactItemBean.getBean()).setChecked(true);
                    }
                }
            }
        }
        if(memberListAdapter !=null){
            memberListAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 改变搜素布局中部门导航的显示
     * @param deptId
     * @param deptName
     */
    private void updateSearchCatalogView(int deptId,String deptName,boolean isClear){
        if(isClear){
            mCatalogDatas.clear();
        }
        CatalogBean catalogBean = new CatalogBean(deptName,deptId);
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

}

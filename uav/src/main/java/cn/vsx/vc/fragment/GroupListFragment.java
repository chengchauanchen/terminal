package cn.vsx.vc.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Department;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetTerminalHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupSelectedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberSelectedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivegUpdateGroupHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.MemberListAdapter;
import cn.vsx.vc.model.ContactItemBean;
import cn.vsx.vc.receiveHandle.ReceiveRemoveSelectedMemberHandler;
import cn.vsx.vc.utils.Constants;

/**
 * 显示群组列表
 */
public class GroupListFragment extends BaseFragment{

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerview;

    private static final String TYPE = "type";
    private String type = "type";
    private List<ContactItemBean> mData = new ArrayList<>();

    private Handler mHandler = new Handler();
    private MemberListAdapter memberListAdapter;
    private List<Integer> selectedMemberNos = new ArrayList<>();

    public static GroupListFragment newInstance(String type){
        GroupListFragment memberListFragment = new GroupListFragment();
        Bundle bundle = new Bundle();
        bundle.putString(TYPE, type);
        memberListFragment.setArguments(bundle);
        return memberListFragment;
    }

    @Override
    public int getContentViewId(){
        return R.layout.member_list_layout;
    }

    @Override
    public void initView(){
        mSwipeRefreshLayout = mRootView.findViewById(R.id.swipeRefreshLayout);
        mRecyclerview = mRootView.findViewById(R.id.recyclerview);
        mRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        memberListAdapter = new MemberListAdapter(getContext(), mData);
        mRecyclerview.setAdapter(memberListAdapter);
    }

    @Override
    public void initListener(){
        TerminalFactory.getSDK().registReceiveHandler(receiveGroupSelectedHandler);
        TerminalFactory.getSDK().registReceiveHandler(receivegUpdateGroupHandler);
        TerminalFactory.getSDK().registReceiveHandler(receiveRemoveSelectedMemberHandler);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            TerminalFactory.getSDK().getConfigManager().updateGroup(TerminalFactory.getSDK().getParam(Params.DEP_ID, 0), TerminalFactory.getSDK().getParam(Params.DEP_NAME, ""));
            mHandler.postDelayed(()-> mSwipeRefreshLayout.setRefreshing(false),1200);
        });

        memberListAdapter.setItemClickListener((type, position) -> {
            if(type == Constants.TYPE_FOLDER){
                Department department = (Department) mData.get(position).getBean();
                TerminalFactory.getSDK().getConfigManager().updateGroup(department.getId(), department.getName());
            }else if(type == Constants.TYPE_GROUP){
                Group group = (Group) mData.get(position).getBean();
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiveGroupSelectedHandler.class,group,!group.isChecked());
            }
            memberListAdapter.notifyDataSetChanged();
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

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        TerminalFactory.getSDK().unregistReceiveHandler(receiveRemoveSelectedMemberHandler);
        TerminalFactory.getSDK().unregistReceiveHandler(receivegUpdateGroupHandler);
        TerminalFactory.getSDK().unregistReceiveHandler(receiveGroupSelectedHandler);
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
}

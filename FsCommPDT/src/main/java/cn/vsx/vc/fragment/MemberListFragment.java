package cn.vsx.vc.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Department;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetTerminalHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberSelectedHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.MemberListAdapter;
import cn.vsx.vc.model.ContactItemBean;
import cn.vsx.vc.utils.Constants;

/**
 * 显示成员列表
 */
public class MemberListFragment extends BaseFragment{

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerview;

    private static final String TYPE = "type";
    private String type = "type";
    private List<ContactItemBean> mData = new ArrayList<>();

    private Handler mHandler = new Handler();
    private MemberListAdapter memberListAdapter;
    private List<Integer> selectedMemberNos = new ArrayList<>();

    public MemberListFragment(){
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            type = getArguments().getString(TYPE);
        }
    }

    public static MemberListFragment newInstance(String type){
        MemberListFragment memberListFragment = new MemberListFragment();
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
        TerminalFactory.getSDK().registReceiveHandler(receiveMemberSelectedHandler);
        TerminalFactory.getSDK().registReceiveHandler(receiveGetTerminalHandler);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            TerminalFactory.getSDK().getConfigManager().getTerminal(TerminalFactory.getSDK().getParam(Params.DEP_ID, 0), type);
            mHandler.postDelayed(()-> mSwipeRefreshLayout.setRefreshing(false),1200);
        });

        memberListAdapter.setItemClickListener(new MemberListAdapter.ItemClickListener(){
            @Override
            public void itemClick(int type, int position){
                if(type == Constants.TYPE_USER){
                    Member member = (Member) mData.get(position).getBean();
                    TerminalFactory.getSDK().notifyReceiveHandler(ReceiveMemberSelectedHandler.class,member,!member.isChecked());

                }else if(type == Constants.TYPE_FOLDER){
                    Department department = (Department) mData.get(position).getBean();
                    TerminalFactory.getSDK().getConfigManager().getTerminal(department.getId(),MemberListFragment.this.type);
                }
                memberListAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void initData(){
        TerminalFactory.getSDK().getConfigManager().getTerminal(TerminalFactory.getSDK().getParam(Params.DEP_ID, 0), type);
    }

    private ReceiveGetTerminalHandler receiveGetTerminalHandler = (depId, type, departments, members) -> {
        if(type.equals(this.type)){
            mHandler.post(()-> updateData(depId, departments, members));
        }
    };

    private ReceiveMemberSelectedHandler receiveMemberSelectedHandler = (member, selected) -> {
        if(selected){
            if(!selectedMemberNos.contains(member.getNo())){
                selectedMemberNos.add(member.getNo());
            }
        }else {
            if(selectedMemberNos.contains(member.getNo())){
                selectedMemberNos.remove((Integer)member.getNo());
            }
        }
        for(ContactItemBean bean : mData){
            if(bean.getBean() instanceof Member){
                if(((Member) bean.getBean()).getNo() == member.getNo()){
                    ((Member) bean.getBean()).setChecked(selected);
                    break;
                }
            }
        }
        mHandler.post(()-> memberListAdapter.notifyDataSetChanged());
    };

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        TerminalFactory.getSDK().unregistReceiveHandler(receiveGetTerminalHandler);
        TerminalFactory.getSDK().unregistReceiveHandler(receiveMemberSelectedHandler);
    }

    private void updateData(int depId, List<Department> departments, List<Member> members){
        mData.clear();
        for(Member member : members){
            ContactItemBean<Member> contactItemBean = new ContactItemBean<>();
            contactItemBean.setBean(member);
            contactItemBean.setType(Constants.TYPE_USER);
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
                if(contactItemBean.getBean() instanceof Member){
                    if(selectedMemberNo == contactItemBean.getBean()){
                        ((Member) contactItemBean.getBean()).setChecked(true);
                    }
                }

            }
        }
        if(memberListAdapter !=null){
            memberListAdapter.notifyDataSetChanged();
        }
    }
}

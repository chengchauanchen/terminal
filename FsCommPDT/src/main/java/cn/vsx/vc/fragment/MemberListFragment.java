package cn.vsx.vc.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Department;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetTerminalHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberSelectedHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.GroupCatalogAdapter;
import cn.vsx.vc.adapter.MemberListAdapter;
import cn.vsx.vc.model.CatalogBean;
import cn.vsx.vc.model.ContactItemBean;
import cn.vsx.vc.receiveHandle.ReceiveRemoveSelectedMemberHandler;
import cn.vsx.vc.receiveHandle.ReceiveShowSearchFragmentHandler;
import cn.vsx.vc.receiveHandle.ReceiverMemberFragmentBackHandler;
import cn.vsx.vc.utils.Constants;

/**
 * 显示成员列表
 */
public class MemberListFragment extends BaseFragment implements GroupCatalogAdapter.ItemClickListener, View.OnClickListener{

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerview;
    private LinearLayout mLl_title;
    private RecyclerView mParentRecyclerview;

    private GroupCatalogAdapter parentRecyclerAdapter;
    private List<CatalogBean> catalogNames=new ArrayList<>();

    private static final String TYPE = "type";
    private String type = "type";
    private List<ContactItemBean> mData = new ArrayList<>();

    private Handler mHandler = new Handler();
    private MemberListAdapter memberListAdapter;
    private List<Member> selectedMember = new ArrayList<>();
    //添加成员时如果成员已经在临时组中了，不在列表显示
    private List<String> uniqueNos = new ArrayList<>();

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
        mLl_title = mRootView.findViewById(R.id.ll_title);
        mParentRecyclerview = mRootView.findViewById(R.id.parent_recyclerview);
        mRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        memberListAdapter = new MemberListAdapter(getContext(), mData);
        mRecyclerview.setAdapter(memberListAdapter);

        mParentRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity(), OrientationHelper.HORIZONTAL, false));
        parentRecyclerAdapter = new GroupCatalogAdapter(getContext(),catalogNames);
        parentRecyclerAdapter.setOnItemClick(this);
        mParentRecyclerview.setAdapter(parentRecyclerAdapter);

        mRootView.findViewById(R.id.iv_search).setOnClickListener(this);
    }

    @Override
    public void initListener(){
        TerminalFactory.getSDK().registReceiveHandler(receiverMemberFragmentBackHandler);
        TerminalFactory.getSDK().registReceiveHandler(receiveMemberSelectedHandler);
        TerminalFactory.getSDK().registReceiveHandler(receiveGetTerminalHandler);
        TerminalFactory.getSDK().registReceiveHandler(receiveRemoveSelectedMemberHandler);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            TerminalFactory.getSDK().getConfigManager().getTerminal(TerminalFactory.getSDK().getParam(Params.DEP_ID, 0), type);
            mHandler.postDelayed(()-> mSwipeRefreshLayout.setRefreshing(false),1200);
        });

        memberListAdapter.setItemClickListener(new MemberListAdapter.ItemClickListener(){
            @Override
            public void itemClick(int type, int position){
                if(type == Constants.TYPE_USER){
                    Member member = (Member) mData.get(position).getBean();
                    TerminalFactory.getSDK().notifyReceiveHandler(ReceiveMemberSelectedHandler.class,member,!member.isChecked(), TerminalMemberType.getInstanceByCode(member.getType()).toString());

                }else if(type == Constants.TYPE_FOLDER){
                    Department department = (Department) mData.get(position).getBean();
                    CatalogBean groupCatalogBean = new CatalogBean(department.getName(),department.getId());
                    if(!catalogNames.contains(groupCatalogBean)){
                        catalogNames.add(groupCatalogBean);
                    }
                    TerminalFactory.getSDK().getConfigManager().getTerminal(department.getId(),MemberListFragment.this.type);
                    parentRecyclerAdapter.notifyDataSetChanged();
                }
                memberListAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void initData(){
        TerminalFactory.getSDK().getConfigManager().getTerminal(TerminalFactory.getSDK().getParam(Params.DEP_ID, 0), type);

        CatalogBean groupCatalogBean = new CatalogBean(TerminalFactory.getSDK().getParam(Params.DEP_NAME,""),TerminalFactory.getSDK().getParam(Params.DEP_ID,0));
        catalogNames.add(groupCatalogBean);
    }

    private ReceiveRemoveSelectedMemberHandler receiveRemoveSelectedMemberHandler = contactItemBean -> {
        // TODO: 2019/4/22 如果删除的是子部门的，需要更改子部门数据的状态
        if(contactItemBean.getBean() instanceof Member){
            Member bean = (Member) contactItemBean.getBean();
            Iterator<ContactItemBean> iterator = mData.iterator();
            while(iterator.hasNext()){
                ContactItemBean next = iterator.next();
                if(next.getBean() instanceof Member){
                    Member member = (Member) next.getBean();
                    if(bean.getNo() == member.getNo() && bean.getType() == member.getType()){
                        member.setChecked(false);
                    }
                }
            }
            selectedMember.remove(bean);
        }
        mHandler.post(()->memberListAdapter.notifyDataSetChanged());
    };

    private ReceiveGetTerminalHandler receiveGetTerminalHandler = (depId, type, departments, members) -> {
        if(type.equals(this.type)){
            mHandler.post(()-> updateData(depId, departments, members));
        }
    };

    private ReceiveMemberSelectedHandler receiveMemberSelectedHandler = (member, selected,type) -> {
        // TODO: 2019/4/22 如果选中的是子部门的，需要修改子部门数据的状态
        if(!type.equals(this.type)){
            return;
        }
        if(selected){
            if(!selectedMember.contains(member)){
                selectedMember.add(member);
            }
        }else {
            selectedMember.remove(member);
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
        TerminalFactory.getSDK().unregistReceiveHandler(receiveRemoveSelectedMemberHandler);
        TerminalFactory.getSDK().unregistReceiveHandler(receiveGetTerminalHandler);
        TerminalFactory.getSDK().unregistReceiveHandler(receiveMemberSelectedHandler);
        TerminalFactory.getSDK().unregistReceiveHandler(receiverMemberFragmentBackHandler);
    }

    private void updateData(int depId, List<Department> departments, List<Member> members){
        mData.clear();
        for(Member member : members){
            if(!uniqueNos.isEmpty()){
                for(String uniqueNo : uniqueNos){
                    if(!String.valueOf(member.getUniqueNo()).equals(uniqueNo)){
                        addData(member);
                    }
                }
            }else {
                addData(member);
            }
        }
        for(Department department : departments){
            ContactItemBean<Department> contactItemBean = new ContactItemBean<>();
            contactItemBean.setBean(department);
            contactItemBean.setType(Constants.TYPE_FOLDER);
            mData.add(contactItemBean);
        }

        //将之前选中的人重新选中
        for(Member member : selectedMember){
            for(ContactItemBean contactItemBean : mData){
                if(contactItemBean.getBean() instanceof Member){
                    if(member.getNo() == ((Member) contactItemBean.getBean()).getNo()){
                        ((Member) contactItemBean.getBean()).setChecked(true);
                    }
                }
            }
        }

        if(memberListAdapter !=null){
            memberListAdapter.notifyDataSetChanged();
        }
    }

    private void addData(Member member){
        ContactItemBean<Member> contactItemBean = new ContactItemBean<>();
        contactItemBean.setBean(member);
        contactItemBean.setType(Constants.TYPE_USER);
        mData.add(contactItemBean);
    }

    private ReceiverMemberFragmentBackHandler receiverMemberFragmentBackHandler = new ReceiverMemberFragmentBackHandler(){
        @Override
        public void handler(){
            if(isAdded() && isVisible() && isResumed()){
                mHandler.post(()->{
                    if(catalogNames.size() > 1){
                        //返回到上一级
                        catalogNames.remove(catalogNames.size()-1);
                        int position = catalogNames.size()-1;
                        mRecyclerview.scrollToPosition(0);
                        parentRecyclerAdapter.notifyDataSetChanged();
                        TerminalFactory.getSDK().getConfigManager().getTerminal(catalogNames.get(position).getId(),MemberListFragment.this.type);
                    }else{
                        if(null != getActivity()){
                            getActivity().finish();
                        }
                    }
                });
            }
        }
    };

    @Override
    public void onItemClick(View view, int position){
        if(position>=0 && position < catalogNames.size()){
            synchronized(MemberListFragment.this){
                List<CatalogBean> groupCatalogBeans = new ArrayList<>(catalogNames.subList(0, position + 1));
                catalogNames.clear();
                catalogNames.addAll(groupCatalogBeans);
                parentRecyclerAdapter.notifyDataSetChanged();
                TerminalFactory.getSDK().getConfigManager().getTerminal(catalogNames.get(position).getId(),MemberListFragment.this.type);
            }
        }
    }

    @Override
    public void onClick(View v){
        if(v.getId() == R.id.iv_search){
            List<Integer> selectedMemberNos = new ArrayList<>();
            for(Member member : selectedMember){
                selectedMemberNos.add(member.getNo());
            }
            if(TerminalMemberType.TERMINAL_PC.toString().equals(type)){
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiveShowSearchFragmentHandler.class, Constants.TYPE_CHECK_SEARCH_PC, selectedMemberNos);
            }else if(TerminalMemberType.TERMINAL_PHONE.toString().equals(type)){
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiveShowSearchFragmentHandler.class, Constants.TYPE_CHECK_SEARCH_POLICE, selectedMemberNos);
            }else if(TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.toString().equals(type)){
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiveShowSearchFragmentHandler.class, Constants.TYPE_CHECK_SEARCH_RECODER, selectedMemberNos);
            }else if(TerminalMemberType.TERMINAL_UAV.toString().equals(type)){
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiveShowSearchFragmentHandler.class, Constants.TYPE_CHECK_SEARCH_UAV, selectedMemberNos);
            }
        }
    }
}

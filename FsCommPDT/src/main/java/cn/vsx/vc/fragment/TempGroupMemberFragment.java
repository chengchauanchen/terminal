package cn.vsx.vc.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberSelectedHandler;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.SelectAdapter;
import cn.vsx.vc.view.TabView;

/**
 * A simple {@link Fragment} subclass.
 */
public class TempGroupMemberFragment extends Fragment implements View.OnClickListener{

    private LinearLayout mLlSelected;
    private RecyclerView mCatalogRecyclerview;
    private ImageView mIvSelect;
    private TabView mTabPc;
    private TabView mTabPolice;
    private TabView mTabRecoder;
    private TabView mTabUav;
    private LinearLayout mLlLayoutSearch;
    private ImageView mIvSearch;
    private TextView mRecoderTv;
    private FrameLayout mContactsViewPager;
    private MemberListFragment currentFragment;
    private MemberListFragment pcFragment;
    private MemberListFragment policeFragment;
    private MemberListFragment recoderFragment;
    private MemberListFragment uavFragment;

    private List<Member> selectedMembers;
    private List<Integer> selectedMemberNos;
    private SelectAdapter selectAdapter;

    public TempGroupMemberFragment(){
        // Required empty public constructor
    }

    public static TempGroupMemberFragment newInstance(int type){
        return new TempGroupMemberFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.layout_select_member, container, false);
        selectedMembers = new ArrayList<>();
        selectedMemberNos = new ArrayList<>();
        findView(view);
        initTab();
        initListener();
        initFragment();
        initCatalog();
        return view;
    }

    private void initFragment(){
        if(pcFragment == null){
            pcFragment = MemberListFragment.newInstance(TerminalMemberType.TERMINAL_PC.toString());
        }
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.contacts_viewPager, pcFragment).show(pcFragment).commit();
        currentFragment = pcFragment;
    }

    private void initCatalog(){
        mCatalogRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity(), OrientationHelper.HORIZONTAL, false));
        selectAdapter = new SelectAdapter(getContext(), selectedMembers);
        mCatalogRecyclerview.setAdapter(selectAdapter);
    }

    private void findView(View view){
        mLlSelected = view.findViewById(R.id.ll_selected);
        mCatalogRecyclerview = view.findViewById(R.id.catalog_recyclerview);
        mIvSelect = view.findViewById(R.id.iv_select);
        mTabPc = view.findViewById(R.id.tab_pc);
        mTabPolice = view.findViewById(R.id.tab_police);
        mTabRecoder = view.findViewById(R.id.tab_recoder);
        mTabUav = view.findViewById(R.id.tab_uav);
        mLlLayoutSearch = view.findViewById(R.id.ll_layout_search);
        mIvSearch = view.findViewById(R.id.iv_search);
        mRecoderTv = view.findViewById(R.id.recoder_tv);
        mContactsViewPager = view.findViewById(R.id.contacts_viewPager);
    }

    private void initTab(){
        mTabPc.setChecked(true);
        mTabPolice.setChecked(false);
        mTabRecoder.setChecked(false);
        mTabUav.setChecked(false);
    }

    private void initListener(){
        mTabPc.setOnClickListener(this);
        mTabPolice.setOnClickListener(this);
        mTabRecoder.setOnClickListener(this);
        mTabUav.setOnClickListener(this);
        mIvSearch.setOnClickListener(this);
        TerminalFactory.getSDK().registReceiveHandler(receiveMemberSelectedHandler);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        TerminalFactory.getSDK().unregistReceiveHandler(receiveMemberSelectedHandler);
    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.tab_pc:
                mTabPc.setChecked(true);
                mTabPolice.setChecked(false);
                mTabRecoder.setChecked(false);
                mTabUav.setChecked(false);
                if(pcFragment == null){
                    pcFragment = MemberListFragment.newInstance(TerminalMemberType.TERMINAL_PC.toString());
                }
                switchFragment(currentFragment, pcFragment);
                break;
            case R.id.tab_police:
                mTabPc.setChecked(false);
                mTabPolice.setChecked(true);
                mTabRecoder.setChecked(false);
                mTabUav.setChecked(false);
                if(policeFragment == null){
                    policeFragment = MemberListFragment.newInstance(TerminalMemberType.TERMINAL_PHONE.toString());
                }
                switchFragment(currentFragment, policeFragment);
                break;
            case R.id.tab_recoder:
                mTabPc.setChecked(false);
                mTabPolice.setChecked(false);
                mTabRecoder.setChecked(true);
                mTabUav.setChecked(false);
                if(recoderFragment == null){
                    recoderFragment = MemberListFragment.newInstance(TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.toString());
                }
                switchFragment(currentFragment, recoderFragment);
                break;
            case R.id.tab_uav:
                mTabPc.setChecked(false);
                mTabPolice.setChecked(false);
                mTabRecoder.setChecked(false);
                mTabUav.setChecked(true);
                if(uavFragment == null){
                    uavFragment = MemberListFragment.newInstance(TerminalMemberType.TERMINAL_UAV.toString());
                }
                switchFragment(currentFragment, uavFragment);
                break;
            case R.id.iv_select:
                break;
            default:
                break;
        }
    }

    private void switchFragment(MemberListFragment from, MemberListFragment to){
        if(currentFragment != to){
            currentFragment = to;
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            if(!to.isAdded()){    // 先判断是否被add过
                transaction.hide(from).add(R.id.contacts_viewPager, to).show(to).commit(); // 隐藏当前的fragment，add下一个Fragment
            }else{
                transaction.hide(from).show(to).commit(); // 隐藏当前的fragment，显示下一个
            }
        }
    }

    private ReceiveMemberSelectedHandler receiveMemberSelectedHandler = (member, selected) -> {
        if(selected){
            if(!selectedMembers.contains(member)){
                selectedMembers.add(member);
                selectedMemberNos.add(member.getNo());
            }
        }else{
            if(selectedMembers.contains(member)){
                selectedMembers.remove(member);
                selectedMemberNos.remove((Integer) member.getNo());
            }
        }
        if(selectedMemberNos.isEmpty()){
            mLlSelected.setVisibility(View.GONE);
        }else {
            mLlSelected.setVisibility(View.VISIBLE);
        }
        if(selectAdapter != null){
            selectAdapter.notifyDataSetChanged();
        }
    };

    public List<Member> getSelectedMember(){
        return selectedMembers;
    }

    public List<Integer> getSelectedMemberNo(){
        return selectedMemberNos;
    }
}

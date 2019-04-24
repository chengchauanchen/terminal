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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberSelectedHandler;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.SelectAdapter;
import cn.vsx.vc.model.ContactItemBean;
import cn.vsx.vc.receiveHandle.ReceiveRemoveSelectedMemberHandler;
import cn.vsx.vc.receiveHandle.ReceiveShowSearchFragmentHandler;
import cn.vsx.vc.receiveHandle.ReceiveShowSelectedFragmentHandler;
import cn.vsx.vc.utils.Constants;
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
    private LinearLayout mLl_search;
    private TextView mRecoderTv;
    private MemberListFragment pcFragment;
    private MemberListFragment policeFragment;
    private MemberListFragment recoderFragment;
    private MemberListFragment uavFragment;
    private BaseFragment currentFragment;

    private ArrayList<ContactItemBean> selectedMembers;
    private List<Integer> selectedMemberNos;
    private SelectAdapter selectAdapter;
    private int currentIndex;

    public TempGroupMemberFragment(){
        // Required empty public constructor
    }

    public static TempGroupMemberFragment newInstance(){
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
        pcFragment = MemberListFragment.newInstance(TerminalMemberType.TERMINAL_PC.toString());
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.contacts_viewPager, pcFragment).
                show(pcFragment).commit();
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
        mLl_search = view.findViewById(R.id.ll_search);
        mRecoderTv = view.findViewById(R.id.recoder_tv);
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
        mIvSelect.setOnClickListener(this);
        mLl_search.setOnClickListener(this);
        TerminalFactory.getSDK().registReceiveHandler(receiveMemberSelectedHandler);
        TerminalFactory.getSDK().registReceiveHandler(receiveRemoveSelectedMemberHandler);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        TerminalFactory.getSDK().unregistReceiveHandler(receiveMemberSelectedHandler);
        TerminalFactory.getSDK().unregistReceiveHandler(receiveRemoveSelectedMemberHandler);
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
                switchFragment(pcFragment);
                currentFragment = pcFragment;
                currentIndex = 0;
                break;
            case R.id.tab_police:
                mTabPc.setChecked(false);
                mTabPolice.setChecked(true);
                mTabRecoder.setChecked(false);
                mTabUav.setChecked(false);
                if(policeFragment == null){
                    policeFragment = MemberListFragment.newInstance(TerminalMemberType.TERMINAL_PHONE.toString());
                }
                switchFragment(policeFragment);
                currentFragment = policeFragment;
                currentIndex = 1;
                break;
            case R.id.tab_recoder:
                mTabPc.setChecked(false);
                mTabPolice.setChecked(false);
                mTabRecoder.setChecked(true);
                mTabUav.setChecked(false);
                if(recoderFragment == null){
                    recoderFragment = MemberListFragment.newInstance(TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.toString());
                }
                switchFragment(recoderFragment);
                currentFragment = recoderFragment;
                currentIndex = 2;
                break;
            case R.id.tab_uav:
                mTabPc.setChecked(false);
                mTabPolice.setChecked(false);
                mTabRecoder.setChecked(false);
                mTabUav.setChecked(true);
                if(uavFragment == null){
                    uavFragment = MemberListFragment.newInstance(TerminalMemberType.TERMINAL_UAV.toString());
                }
                switchFragment(uavFragment);
                currentFragment = uavFragment;
                currentIndex = 3;
                break;
            case R.id.ll_search:
                showSearchFragment();

                break;
            case R.id.iv_select:
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiveShowSelectedFragmentHandler.class,selectedMembers);

                break;
            default:
                break;
        }
    }

    private void showSearchFragment(){
        switch(currentIndex){
            case 0:
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiveShowSearchFragmentHandler.class,Constants.TYPE_CHECK_SEARCH_PC,selectedMemberNos);
                break;
            case 1:
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiveShowSearchFragmentHandler.class,Constants.TYPE_CHECK_SEARCH_POLICE,selectedMemberNos);
                break;
            case 2:
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiveShowSearchFragmentHandler.class,Constants.TYPE_CHECK_SEARCH_RECODER,selectedMemberNos);
                break;
            case 3:
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiveShowSearchFragmentHandler.class,Constants.TYPE_CHECK_SEARCH_UAV,selectedMemberNos);
                break;
            default:
        }

    }

    private void switchFragment(BaseFragment to){
        if(currentFragment != to){
            if(to.isAdded()){
                getChildFragmentManager().beginTransaction().hide(currentFragment).show(to).commit();
            }else{
                getChildFragmentManager().beginTransaction().hide(currentFragment).add(R.id.contacts_viewPager, to).show(to).commit();
            }
        }
    }

    private ReceiveRemoveSelectedMemberHandler receiveRemoveSelectedMemberHandler = new ReceiveRemoveSelectedMemberHandler(){
        @Override
        public void handle(ContactItemBean contactItemBean){
            if(contactItemBean.getBean() instanceof Member){
                Member member = (Member) contactItemBean.getBean();
                if(selectedMemberNos.contains(member.getNo())){
                    Iterator<ContactItemBean> iterator = selectedMembers.iterator();
                    while(iterator.hasNext()){
                        ContactItemBean next = iterator.next();
                        if(next.getType() == Constants.TYPE_USER){
                            Member member1 = (Member) next.getBean();
                            if(member1.getNo() == member.getNo()){
                                iterator.remove();
                            }
                        }
                    }
                    selectedMemberNos.remove((Integer) member.getNo());
                }
            }
            selectAdapter.notifyDataSetChanged();
        }
    };

    private ReceiveMemberSelectedHandler receiveMemberSelectedHandler = (member, selected) -> {
        //不是同一个member对象
        if(selected){
            ContactItemBean bean = new ContactItemBean();
            bean.setType(Constants.TYPE_USER);
            bean.setBean(member);
            if(!selectedMemberNos.contains(member.getNo())){
                selectedMembers.add(bean);
                selectedMemberNos.add(member.getNo());
            }
        }else{
            if(selectedMemberNos.contains(member.getNo())){
                Iterator<ContactItemBean> iterator = selectedMembers.iterator();
                while(iterator.hasNext()){
                    ContactItemBean next = iterator.next();
                    if(next.getType() == Constants.TYPE_USER){
                        Member member1 = (Member) next.getBean();
                        if(member1.getNo() == member.getNo()){
                            iterator.remove();
                        }
                    }
                }
                selectedMemberNos.remove((Integer) member.getNo());
            }
        }
        if(selectedMemberNos.isEmpty()){
            mLlSelected.setVisibility(View.GONE);
        }else{
            mLlSelected.setVisibility(View.VISIBLE);
        }

        if(selectAdapter != null){
            selectAdapter.notifyDataSetChanged();
        }
    };

    public ArrayList<Member> getSelectedMember(){
        ArrayList<Member> list = new ArrayList<>();
        for (ContactItemBean bean: selectedMembers) {
            if(bean.getType() == Constants.TYPE_USER){
                list.add((Member) bean.getBean());
            }
        }
        return list;
    }

    public List<Integer> getSelectedMemberNo(){
        return selectedMemberNos;
    }
}

package cn.vsx.vc.fragment;

import android.os.Bundle;
import android.os.Handler;
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
    private MemberListFragment pcFragment;
    private MemberListFragment policeFragment;
    private MemberListFragment recoderFragment;
    private MemberListFragment uavFragment;
    private BaseFragment currentFragment;

    private ArrayList<ContactItemBean> selectedMembers;

//    private List<Integer> selectedMemberNos;
    private List<Member> pcSelectMember = new ArrayList<>();
    private List<Member> policeSelectMember = new ArrayList<>();
    private List<Member> recoderSelectMember = new ArrayList<>();
    private List<Member> uavSelectMember = new ArrayList<>();

    private SelectAdapter selectAdapter;

    private int currentIndex;
    private Handler mHandler = new Handler();

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
        int i = v.getId();
        if(i == R.id.tab_pc){
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
        }else if(i == R.id.tab_police){
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
        }else if(i == R.id.tab_recoder){
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
        }else if(i == R.id.tab_uav){
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
        }else if(i == R.id.iv_select){
            TerminalFactory.getSDK().notifyReceiveHandler(ReceiveShowSelectedFragmentHandler.class, selectedMembers);
        }else{
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
            removeMember(contactItemBean);
            mHandler.post(()->{
                if(selectAdapter != null){
                    selectAdapter.notifyDataSetChanged();
                    setButtonCount();
                }
            });
        }
    };

    private ReceiveMemberSelectedHandler receiveMemberSelectedHandler = (member, selected,type) -> {
        if(selected){
            addMember(member,type);
        }else{
            removeMember(member,type);

        }
        mHandler.post(()->{
            if(selectAdapter != null){
                selectAdapter.notifyDataSetChanged();
                setButtonCount();
            }
        });
    };

    @SuppressWarnings("unchecked")
    private void addMember(Member member, String type){
        ContactItemBean bean = new ContactItemBean();
        bean.setType(Constants.TYPE_USER);
        bean.setBean(member);
        selectedMembers.add(bean);
        if(TerminalMemberType.TERMINAL_PC.toString().equals(type)){
            pcSelectMember.add(member);
        }else if(TerminalMemberType.TERMINAL_PHONE.toString().equals(type)){
            policeSelectMember.add(member);
        }else if(TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.toString().equals(type)){
            recoderSelectMember.add(member);
        }else if(TerminalMemberType.TERMINAL_UAV.toString().equals(type)){
            uavSelectMember.add(member);
        }
    }

    private void removeMember(Member member, String type){
        if(TerminalMemberType.TERMINAL_PC.toString().equals(type)){
            pcSelectMember.remove(member);
        }else if(TerminalMemberType.TERMINAL_PHONE.toString().equals(type)){
            policeSelectMember.remove(member);
        }else if(TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.toString().equals(type)){
            recoderSelectMember.remove(member);
        }else if(TerminalMemberType.TERMINAL_UAV.toString().equals(type)){
            uavSelectMember.remove(member);
        }
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
    }

    private void removeMember(ContactItemBean contactItemBean){
        if(contactItemBean.getBean() instanceof Member){
            Member member = (Member) contactItemBean.getBean();
            selectedMembers.remove(contactItemBean);
//            Iterator<ContactItemBean> iterator = selectedMembers.iterator();
//            while(iterator.hasNext()){
//                ContactItemBean next = iterator.next();
//                if(next.getType() == Constants.TYPE_USER){
//                    Member member1 = (Member) next.getBean();
//                    if(member1.getNo() == member.getNo() && member1.getType() == member.getType()){
//                        iterator.remove();
//                    }
//                }
//            }
            removeSelectedMember(member);
        }
    }

    private void removeSelectedMember(Member member){
        if(member.getType() == TerminalMemberType.TERMINAL_PC.getCode()){
            pcSelectMember.remove(member);
        }else if(member.getType() == TerminalMemberType.TERMINAL_PHONE.getCode()){
            policeSelectMember.remove(member);
        }else if(member.getType() == TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.getCode()){
            recoderSelectMember.remove(member);
        }else if(member.getType() == TerminalMemberType.TERMINAL_UAV.getCode()){
            uavSelectMember.remove(member);
        }
    }

    public ArrayList<Member> getSelectedMember(){
        ArrayList<Member> list = new ArrayList<>();
        for (ContactItemBean bean: selectedMembers) {
            if(bean.getType() == Constants.TYPE_USER){
                list.add((Member) bean.getBean());
            }
        }
        return list;
    }

    private void setButtonCount() {
        if(selectedMembers.isEmpty()){
            mLlSelected.setVisibility(View.GONE);
        }else{
            mLlSelected.setVisibility(View.VISIBLE);
        }
    }
}

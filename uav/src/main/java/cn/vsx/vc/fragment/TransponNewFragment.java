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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupSelectedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberSelectedHandler;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.SelectAdapter;
import cn.vsx.vc.model.ContactItemBean;
import cn.vsx.vc.receiveHandle.ReceiveRemoveSelectedMemberHandler;
import cn.vsx.vc.receiveHandle.ReceiveShowSearchFragmentHandler;
import cn.vsx.vc.receiveHandle.ReceiveShowSelectedFragmentHandler;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.ToastUtil;
import cn.vsx.vc.view.TabView;

/**
 * Created by gt358 on 2017/10/20.
 */

public class TransponNewFragment extends Fragment implements View.OnClickListener {

    private ImageView newsBarReturn;
    private Button okBtn;

    private LinearLayout mLlSelected;
    private RecyclerView mCatalogRecyclerview;
    private ImageView mIvSelect;

    private TabView mTabGroup;
    private TabView mTabPc;
    private TabView mTabPolice;

    private LinearLayout mLl_search;

    private GroupListFragment groupFragment;
    private MemberListFragment pcFragment;
    private MemberListFragment policeFragment;
    private BaseFragment currentFragment;

    private ArrayList<ContactItemBean> selectedMembers;
    private ArrayList<Integer> selectedMemberNos;
    private SelectAdapter selectAdapter;
    private int currentIndex;

    private Handler mHandler = new Handler();
    private BackListener backListener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.popup_transpon_new, container, false);
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
        groupFragment = new GroupListFragment();
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.contacts_viewPager, groupFragment).show(groupFragment).commit();
        currentFragment = groupFragment;
    }

    private void initCatalog(){
        mCatalogRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity(), OrientationHelper.HORIZONTAL, false));
        selectAdapter = new SelectAdapter(getContext(), selectedMembers);
        mCatalogRecyclerview.setAdapter(selectAdapter);
    }

    private void findView(View view){
        //返回按钮
        newsBarReturn = view.findViewById(R.id.news_bar_return);
        okBtn = view.findViewById(R.id.ok_btn);
        //已选择布局
        mLlSelected = view.findViewById(R.id.ll_selected);
        mCatalogRecyclerview = view.findViewById(R.id.catalog_recyclerview);
        mIvSelect = view.findViewById(R.id.iv_select);

        //tabview
        mTabGroup = view.findViewById(R.id.tab_group);
        mTabPc = view.findViewById(R.id.tab_pc);
        mTabPolice = view.findViewById(R.id.tab_police);

        mLl_search = view.findViewById(R.id.ll_search);
    }


    private void initTab(){
        mTabGroup.setChecked(true);
        mTabPc.setChecked(false);
        mTabPolice.setChecked(false);
    }


    private void initListener() {
        newsBarReturn.setOnClickListener(this);
        okBtn.setOnClickListener(this);
        mTabGroup.setOnClickListener(this);
        mTabPc.setOnClickListener(this);
        mTabPolice.setOnClickListener(this);

        mIvSelect.setOnClickListener(this);
        mLl_search.setOnClickListener(this);
        TerminalFactory.getSDK().registReceiveHandler(receiveMemberSelectedHandler);
        TerminalFactory.getSDK().registReceiveHandler(receiveGroupSelectedHandler);
        TerminalFactory.getSDK().registReceiveHandler(receiveRemoveSelectedMemberHandler);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        TerminalFactory.getSDK().unregistReceiveHandler(receiveMemberSelectedHandler);
        TerminalFactory.getSDK().unregistReceiveHandler(receiveGroupSelectedHandler);
        TerminalFactory.getSDK().unregistReceiveHandler(receiveRemoveSelectedMemberHandler);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.news_bar_return:
                if(backListener != null){
                    backListener.onBack();
                }
                break;
            case R.id.ok_btn:
                if(!selectedMemberNos.isEmpty()){
                    if(backListener != null){
                        backListener.onResult(selectedMembers);
                    }
                }else{
                    ToastUtil.showToast(this.getContext(),getString(R.string.please_choose_one));
                }
                break;
            case R.id.tab_group:
                mTabGroup.setChecked(true);
                mTabPc.setChecked(false);
                mTabPolice.setChecked(false);
                if(groupFragment == null){
                    groupFragment = new GroupListFragment();
                }
                switchFragment(groupFragment);
                currentFragment = groupFragment;
                currentIndex = 0;
                break;
            case R.id.tab_pc:
                mTabGroup.setChecked(false);
                mTabPc.setChecked(true);
                mTabPolice.setChecked(false);
                if(pcFragment == null){
                    pcFragment = MemberListFragment.newInstance(TerminalMemberType.TERMINAL_PC.toString());
                }
                switchFragment(pcFragment);
                currentFragment = pcFragment;
                currentIndex = 1;
                break;
            case R.id.tab_police:
                mTabGroup.setChecked(false);
                mTabPc.setChecked(false);
                mTabPolice.setChecked(true);
                if(policeFragment == null){
                    policeFragment = MemberListFragment.newInstance(TerminalMemberType.TERMINAL_PHONE.toString());
                }
                switchFragment(policeFragment);
                currentFragment = policeFragment;
                currentIndex = 2;
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

    private void switchFragment(BaseFragment to){
        if(currentFragment != to){
            if(to.isAdded()){
                getChildFragmentManager().beginTransaction().hide(currentFragment).show(to).commit();
            }else{
                getChildFragmentManager().beginTransaction().hide(currentFragment).add(R.id.contacts_viewPager, to).show(to).commit();
            }
        }
    }

    private void showSearchFragment(){
        switch(currentIndex){
            case 0:
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiveShowSearchFragmentHandler.class,Constants.TYPE_CHECK_SEARCH_GROUP,selectedMemberNos);
                break;
            case 1:
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiveShowSearchFragmentHandler.class,Constants.TYPE_CHECK_SEARCH_PC,selectedMemberNos);
                break;
            case 2:
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiveShowSearchFragmentHandler.class,Constants.TYPE_CHECK_SEARCH_POLICE,selectedMemberNos);
                break;
        }
    }

    private ReceiveGroupSelectedHandler receiveGroupSelectedHandler = (group, selected) -> {
        //不是同一个member对象
        if(selected){
            ContactItemBean bean = new ContactItemBean();
            bean.setType(Constants.TYPE_GROUP);
            bean.setBean(group);
            if(!selectedMemberNos.contains(group.getNo())){
                selectedMembers.add(bean);
                selectedMemberNos.add(group.getNo());
            }
        }else{
            if(selectedMemberNos.contains(group.getNo())){
                Iterator<ContactItemBean> iterator = selectedMembers.iterator();
                while(iterator.hasNext()){
                    ContactItemBean next = iterator.next();
                    if(next.getType() == Constants.TYPE_GROUP){
                        Group group1 = (Group) next.getBean();
                        if(group1.getNo() == group.getNo()){
                            iterator.remove();
                        }
                    }
                }
                selectedMemberNos.remove((Integer)group.getNo());
            }
        }
        mHandler.post(()->{
            setButtonCount();
            if(selectAdapter != null){
                selectAdapter.notifyDataSetChanged();
            }
        });

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
        mHandler.post(()->{
            setButtonCount();
            if(selectAdapter != null){
                selectAdapter.notifyDataSetChanged();
            }
        });
    };

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
            }else if(contactItemBean.getBean() instanceof Group){
                Group group = (Group) contactItemBean.getBean();
                if(selectedMemberNos.contains(group.getNo())){
                    Iterator<ContactItemBean> iterator = selectedMembers.iterator();
                    while(iterator.hasNext()){
                        ContactItemBean next = iterator.next();
                        if(next.getType() == Constants.TYPE_GROUP){
                            Group group1 = (Group) next.getBean();
                            if(group1.getNo() == group.getNo()){
                                iterator.remove();
                            }
                        }
                    }
                    selectedMemberNos.remove((Integer) group.getNo());
                }
            }
            mHandler.post(()->{
                setButtonCount();
                if (selectAdapter!=null) {
                    selectAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    private void setButtonCount() {
        if(selectedMemberNos.isEmpty()){
            okBtn.setText(getString(R.string.text_sure));
        }else{
            okBtn.setText(String.format(getString(R.string.button_sure_number),selectedMemberNos.size()));
        }
    }

    public void setBacklistener(BackListener backListener){
        this.backListener = backListener;
    }
    public interface BackListener{
        void onBack();
        void onResult(ArrayList<ContactItemBean> list);
    }
}

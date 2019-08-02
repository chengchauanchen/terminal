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

import java.util.ArrayList;
import java.util.Iterator;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveAccountSelectedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupSelectedHandler;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.SelectAdapter;
import cn.vsx.vc.model.ContactItemBean;
import cn.vsx.vc.receiveHandle.OnSearchListener;
import cn.vsx.vc.receiveHandle.ReceiveRemoveSelectedMemberHandler;
import cn.vsx.vc.receiveHandle.ReceiveShowSearchFragmentHandler;
import cn.vsx.vc.receiveHandle.ReceiveShowSelectedFragmentHandler;
import cn.vsx.vc.utils.ApkUtil;
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
    private TabView mTabPolice;

    private GroupListFragment groupFragment;
    private AccountListFragment accountListFragment;
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
        groupFragment.setOnSearchListener(mOnSearchListener);
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
        mTabPolice = view.findViewById(R.id.tab_police);
        if(ApkUtil.isAnjian()){
            mTabPolice.setName(getString(R.string.text_person));
        }else{
            mTabPolice.setName(getString(R.string.text_police_service));
        }
    }


    private void initTab(){
        mTabGroup.setChecked(true);
        mTabPolice.setChecked(false);
    }


    private void initListener() {
        newsBarReturn.setOnClickListener(this);
        okBtn.setOnClickListener(this);
        mTabGroup.setOnClickListener(this);
        mTabPolice.setOnClickListener(this);

        mIvSelect.setOnClickListener(this);
        TerminalFactory.getSDK().registReceiveHandler(receiveAccountSelectedHandler);
        TerminalFactory.getSDK().registReceiveHandler(receiveGroupSelectedHandler);
        TerminalFactory.getSDK().registReceiveHandler(receiveRemoveSelectedMemberHandler);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        TerminalFactory.getSDK().unregistReceiveHandler(receiveAccountSelectedHandler);
        TerminalFactory.getSDK().unregistReceiveHandler(receiveGroupSelectedHandler);
        TerminalFactory.getSDK().unregistReceiveHandler(receiveRemoveSelectedMemberHandler);
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if(i == R.id.news_bar_return){
            if(backListener != null){
                backListener.onBack();
            }
        }else if(i == R.id.ok_btn){
            if(!selectedMemberNos.isEmpty()){
                if(backListener != null){
                    backListener.onResult(selectedMembers);
                }
            }else{
                ToastUtil.showToast(this.getContext(), getString(R.string.please_choose_one));
            }
        }else if(i == R.id.tab_group){
            mTabGroup.setChecked(true);
            mTabPolice.setChecked(false);
            if(groupFragment == null){
                groupFragment = new GroupListFragment();
                groupFragment.setOnSearchListener(mOnSearchListener);
            }
            switchFragment(groupFragment);
            currentFragment = groupFragment;
            currentIndex = 0;
        }else if(i == R.id.tab_police){
            mTabGroup.setChecked(false);
            mTabPolice.setChecked(true);
            if(accountListFragment == null){
                accountListFragment = new AccountListFragment();
                accountListFragment.setOnSearchListener(mOnSearchListener);
            }
            switchFragment(accountListFragment);
            currentFragment = accountListFragment;
            currentIndex = 1;
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

    public void showSearchFragment(){
        switch(currentIndex){
            case 0:
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiveShowSearchFragmentHandler.class,Constants.TYPE_CHECK_SEARCH_GROUP,selectedMemberNos);
                break;
            case 1:
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiveShowSearchFragmentHandler.class,Constants.TYPE_CHECK_SEARCH_ACCOUNT,selectedMemberNos);
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


    private ReceiveAccountSelectedHandler receiveAccountSelectedHandler = (account, selected) -> {
        //不是同一个member对象
        if(selected){
            ContactItemBean bean = new ContactItemBean();
            bean.setType(Constants.TYPE_ACCOUNT);
            bean.setBean(account);
            if(!selectedMemberNos.contains(account.getNo())){
                selectedMembers.add(bean);
                selectedMemberNos.add(account.getNo());
            }
        }else{
            if(selectedMemberNos.contains(account.getNo())){
                Iterator<ContactItemBean> iterator = selectedMembers.iterator();
                while(iterator.hasNext()){
                    ContactItemBean next = iterator.next();
                    if(next.getType() == Constants.TYPE_ACCOUNT){
                        Account account1 = (Account) next.getBean();
                        if(account1.getNo() == account.getNo()){
                            iterator.remove();
                        }
                    }
                }
                selectedMemberNos.remove((Integer) account.getNo());
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
            if(contactItemBean.getBean() instanceof Account){
                Account account = (Account) contactItemBean.getBean();
                if(selectedMemberNos.contains(account.getNo())){
                    Iterator<ContactItemBean> iterator = selectedMembers.iterator();
                    while(iterator.hasNext()){
                        ContactItemBean next = iterator.next();
                        if(next.getType() == Constants.TYPE_ACCOUNT){
                            Account account1 = (Account) next.getBean();
                            if(account1.getNo() == account.getNo()){
                                iterator.remove();
                            }
                        }
                    }
                    selectedMemberNos.remove((Integer) account.getNo());
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
            mLlSelected.setVisibility(View.GONE);
        }else{
            okBtn.setText(String.format(getString(R.string.button_sure_number),selectedMemberNos.size()));
            mLlSelected.setVisibility(View.VISIBLE);
        }
    }
    /**
     * 搜索点击事件
     */
    private OnSearchListener mOnSearchListener = new OnSearchListener(){
        @Override
        public void onSearch(){
            showSearchFragment();
        }
    };

    public void setBacklistener(BackListener backListener){
        this.backListener = backListener;
    }



    public interface BackListener{
        void onBack();
        void onResult(ArrayList<ContactItemBean> list);
    }

}

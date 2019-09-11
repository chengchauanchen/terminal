package com.vsxin.terminalpad.mvp.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;

import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.entity.ContactItemBean;
import com.vsxin.terminalpad.mvp.ui.adapter.SelectedItemListAdapter;
import com.vsxin.terminalpad.receiveHandler.ReceiveRemoveSelectedMemberHandler;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.terminalsdk.TerminalFactory;

public class SelectedMemberFragment extends BaseFragment{
    private static final String FRAGMENT_TAG = "selectMember";
    private static final String DATA = "data";
    private List<ContactItemBean> mData;
    private RecyclerView mRvSelected;
    private ImageView mNewsBarBack;
    private TextView mBarTitle;
    private BackListener backListener;

    public SelectedMemberFragment(){
        // Required empty public constructor
    }

    /**
     */
    public static SelectedMemberFragment newInstance(ArrayList<ContactItemBean> mData){
        SelectedMemberFragment fragment = new SelectedMemberFragment();
        Bundle args = new Bundle();
        args.putSerializable(DATA, mData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            mData = (List<ContactItemBean>) getArguments().getSerializable(DATA);
        }
    }

    @Override
    public int getContentViewId(){
        return R.layout.fragment_selected_member;
    }

    @Override
    public void initView(){
        mNewsBarBack = mRootView.findViewById(R.id.news_bar_return);
        mBarTitle = mRootView.findViewById(R.id.bar_title);
        mRvSelected = mRootView.findViewById(R.id.rv_selected);
        mBarTitle.setText(R.string.text_choose);
    }

    @Override
    public void initListener(){
        mNewsBarBack.setOnClickListener(v -> {
            getFragmentManager().popBackStack();
        });
    }

    @Override
    public void initData(){
        SelectedItemListAdapter selectedListAdapter = new SelectedItemListAdapter(getContext(),mData);
        selectedListAdapter.setItemClickListener(position -> {
            if(position>=0&&position<mData.size()){
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiveRemoveSelectedMemberHandler.class,mData.get(position));
                mData.remove(position);
                selectedListAdapter.notifyDataSetChanged();
            }
        });
        mRvSelected.setAdapter(selectedListAdapter);
        mRvSelected.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    public void setBackListener(BackListener backListener){
        this.backListener = backListener;
    }
    public interface BackListener{
        void onBack();
    }

    /**
     * 开启 startSelectedMemberFragment
     *
     * @param fragmentActivity
     */
    public static void startSelectedMemberFragment(FragmentActivity fragmentActivity, ArrayList<ContactItemBean> mData) {
        SelectedMemberFragment selectMemberFragment = SelectedMemberFragment.newInstance(mData);
        FragmentManager supportFragmentManager = fragmentActivity.getSupportFragmentManager();
        //replace 会将上一个Fragment干掉
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fl_layer_member_info, selectMemberFragment, FRAGMENT_TAG);
        fragmentTransaction.show(selectMemberFragment);
        fragmentTransaction.addToBackStack(fragmentActivity.getClass().getName());
        fragmentTransaction.commit();
    }
}

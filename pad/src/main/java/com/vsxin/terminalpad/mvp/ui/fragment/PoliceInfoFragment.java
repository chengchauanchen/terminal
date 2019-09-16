package com.vsxin.terminalpad.mvp.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.ixiaoma.xiaomabus.architecture.mvp.refresh.adapter.BaseRecycleViewAdapter;
import com.ixiaoma.xiaomabus.architecture.mvp.refresh.fragment.RefreshRecycleViewFragment;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.constant.MemberTypeConstans;
import com.vsxin.terminalpad.mvp.contract.constant.MemberTypeEnum;
import com.vsxin.terminalpad.mvp.contract.constant.TerminalEnum;
import com.vsxin.terminalpad.mvp.contract.presenter.PoliceInfoPresenter;
import com.vsxin.terminalpad.mvp.contract.view.IPoliceInfoView;
import com.vsxin.terminalpad.mvp.entity.DeviceBean;
import com.vsxin.terminalpad.mvp.entity.MemberInfoBean;
import com.vsxin.terminalpad.mvp.entity.PersonnelBean;
import com.vsxin.terminalpad.mvp.entity.TerminalBean;
import com.vsxin.terminalpad.mvp.ui.adapter.PoliceBandDeviceAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * @author qzw
 * <p>
 * 地图气泡点击-民警详情页
 * 显示民警身上绑定的终端设备
 */
public class PoliceInfoFragment extends RefreshRecycleViewFragment<TerminalBean,IPoliceInfoView, PoliceInfoPresenter> implements IPoliceInfoView {

    private static final String FRAGMENT_TAG = "PoliceInfoFragment";
    public static final String PERSONNEL = "PersonnelBean";
    public static final String TERMINAL_ENUM = "terminalEnum";

    @BindView(R.id.iv_close)
    ImageView iv_close;
    @BindView(R.id.tv_name)
    TextView tv_name;
    @BindView(R.id.tv_department)
    TextView tv_department;
    private PersonnelBean personnelBean;

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_police_info;
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        refreshLayout.setEnableRefresh(false);
        refreshLayout.setEnableLoadMore(false);
        getPresenter().setAdapter(recyclerView, mSuperAdapter);
        //关闭 PoliceInfoFragment
        iv_close.setOnClickListener(v -> closePoliceInfoFragment(getActivity()));
    }

    @Override
    protected void initData() {
        super.initData();
        //personnelBean = (PersonnelBean) getArguments().getSerializable(PERSONNEL);
        TerminalEnum terminalEnum = (TerminalEnum) getArguments().getSerializable(TERMINAL_ENUM);

        List<TerminalBean> terminalDtoList = personnelBean.getTerminalDtoList();
        refreshOrLoadMore(terminalDtoList);

        tv_name.setText(personnelBean.getPersonnelName()+" "+ personnelBean.getPersonnelNo());
        tv_department.setText("");
    }

    @Override
    protected void refresh() {

    }

    @Override
    protected void loadMore() {

    }

    @Override
    protected BaseRecycleViewAdapter createAdapter() {
        personnelBean = (PersonnelBean) getArguments().getSerializable(PERSONNEL);

        return new PoliceBandDeviceAdapter(getContext(),personnelBean);
    }

    @Override
    public PoliceInfoPresenter createPresenter() {
        return new PoliceInfoPresenter(getContext());
    }

    /**
     * 开启 PoliceInfoFragment
     * @param fragmentActivity
     */
    public static void startPoliceInfoFragment(FragmentActivity fragmentActivity, PersonnelBean personnelBean,TerminalEnum terminalEnum) {
        PoliceInfoFragment policeInfoFragment = new PoliceInfoFragment();
        Bundle args = new Bundle();
        args.putSerializable(PERSONNEL, personnelBean);
        args.putSerializable(TERMINAL_ENUM,terminalEnum);
        policeInfoFragment.setArguments(args);
        FragmentManager supportFragmentManager = fragmentActivity.getSupportFragmentManager();
        //replace 会将上一个Fragment干掉
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fl_layer_member_info, policeInfoFragment,FRAGMENT_TAG);
        fragmentTransaction.commit();
    }

    /**
     * 关闭 PoliceInfoFragment
     * @param fragmentActivity
     */
    public static void closePoliceInfoFragment(FragmentActivity fragmentActivity){
        FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();
        Fragment memberInfo = fragmentManager.findFragmentByTag(FRAGMENT_TAG);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if(memberInfo!=null){
            fragmentTransaction.remove(memberInfo);
        }
        fragmentTransaction.commit();
    }

}
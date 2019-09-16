package com.vsxin.terminalpad.mvp.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageView;

import com.ixiaoma.xiaomabus.architecture.mvp.refresh.adapter.BaseRecycleViewAdapter;
import com.ixiaoma.xiaomabus.architecture.mvp.refresh.fragment.RefreshRecycleViewFragment;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.presenter.CarBoatInfoPresenter;
import com.vsxin.terminalpad.mvp.contract.presenter.PoliceInfoPresenter;
import com.vsxin.terminalpad.mvp.contract.view.ICarBoatInfoView;
import com.vsxin.terminalpad.mvp.entity.DeviceBean;
import com.vsxin.terminalpad.mvp.ui.adapter.CarBoatBandDeviceAdapter;
import com.vsxin.terminalpad.mvp.ui.adapter.PoliceBandDeviceAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * @author qzw
 * <p>
 * 地图气泡点击-车、船 详情页
 * 显示车、船上绑定的民警和终端设备
 */
public class CarBoatInfoFragment extends RefreshRecycleViewFragment<DeviceBean, ICarBoatInfoView, CarBoatInfoPresenter> implements ICarBoatInfoView {

    private static final String FRAGMENT_TAG = "CarBoatInfoFragment";

    @BindView(R.id.iv_close)
    ImageView iv_close;


    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_car_boat_info;
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        refreshLayout.setEnableRefresh(false);
        refreshLayout.setEnableLoadMore(false);
        getPresenter().setAdapter(recyclerView, mSuperAdapter);

        //关闭 PoliceInfoFragment
        iv_close.setOnClickListener(v -> closeCarBoatInfoFragment(getActivity()));
    }

    @Override
    protected void initData() {
        super.initData();
        List<DeviceBean> deviceBeans = new ArrayList<>();
        deviceBeans.add(new DeviceBean(false));
        deviceBeans.add(new DeviceBean(false));
        deviceBeans.add(new DeviceBean(false));
        deviceBeans.add(new DeviceBean(true));
        deviceBeans.add(new DeviceBean(true));
        deviceBeans.add(new DeviceBean(true));
        deviceBeans.add(new DeviceBean(true));
        deviceBeans.add(new DeviceBean(true));
        refreshOrLoadMore(deviceBeans);
    }

    @Override
    protected void refresh() {

    }

    @Override
    protected void loadMore() {

    }

    @Override
    protected BaseRecycleViewAdapter createAdapter() {
        return new CarBoatBandDeviceAdapter(getContext());
    }

    @Override
    public CarBoatInfoPresenter createPresenter() {
        return new CarBoatInfoPresenter(getContext());
    }

    /**
     * 开启 PoliceInfoFragment
     * @param fragmentActivity
     */
    public static void startCarBoatInfoFragment(FragmentActivity fragmentActivity) {
        CarBoatInfoFragment carBoatInfoFragment = new CarBoatInfoFragment();
        Bundle args = new Bundle();
        //args.putSerializable(PARAM_JSON, json);
        //args.putSerializable(PARAM_ENUM,typeEnum);
        carBoatInfoFragment.setArguments(args);
        FragmentManager supportFragmentManager = fragmentActivity.getSupportFragmentManager();
        //replace 会将上一个Fragment干掉
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fl_layer_member_info, carBoatInfoFragment,FRAGMENT_TAG);
        fragmentTransaction.commit();
    }

    /**
     * 关闭 PoliceInfoFragment
     * @param fragmentActivity
     */
    public static void closeCarBoatInfoFragment(FragmentActivity fragmentActivity){
        FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();
        Fragment memberInfo = fragmentManager.findFragmentByTag(FRAGMENT_TAG);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if(memberInfo!=null){
            fragmentTransaction.remove(memberInfo);
        }
        fragmentTransaction.commit();
    }

}

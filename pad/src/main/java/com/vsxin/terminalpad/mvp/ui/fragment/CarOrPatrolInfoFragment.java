package com.vsxin.terminalpad.mvp.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ixiaoma.xiaomabus.architecture.mvp.refresh.adapter.BaseRecycleViewAdapter;
import com.ixiaoma.xiaomabus.architecture.mvp.refresh.fragment.RefreshRecycleViewFragment;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.constant.TerminalEnum;
import com.vsxin.terminalpad.mvp.contract.constant.TerminalType;
import com.vsxin.terminalpad.mvp.contract.presenter.CarBoatInfoPresenter;
import com.vsxin.terminalpad.mvp.contract.view.ICarBoatInfoView;
import com.vsxin.terminalpad.mvp.entity.CarBean;
import com.vsxin.terminalpad.mvp.entity.DeviceBean;
import com.vsxin.terminalpad.mvp.entity.PatrolBean;
import com.vsxin.terminalpad.mvp.entity.PersonnelBean;
import com.vsxin.terminalpad.mvp.entity.TerminalBean;
import com.vsxin.terminalpad.mvp.ui.adapter.CarBandDeviceAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;

/**
 * @author qzw
 * <p>
 * 地图气泡点击-车、船 详情页
 * 显示车、船上绑定的民警和终端设备
 */
public class CarOrPatrolInfoFragment extends RefreshRecycleViewFragment<DeviceBean, ICarBoatInfoView, CarBoatInfoPresenter> implements ICarBoatInfoView {

    private static final String FRAGMENT_TAG = "CarOrPatrolInfoFragment";

    public static final String PATROL = "PatrolBean";
    public static final String CAR = "CarBean";
    public static final String TERMINAL_ENUM = "terminalEnum";

    @BindView(R.id.iv_close)
    ImageView iv_close;

    @BindView(R.id.tv_name)
    TextView tv_name;//名称

    @BindView(R.id.tv_department)
    TextView tv_department;//部门

    private TerminalEnum terminalEnum;
    private PatrolBean patrolBean;
    private CarBean carBean;

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
        terminalEnum = (TerminalEnum) getArguments().getSerializable(TERMINAL_ENUM);
        patrolBean = (PatrolBean) getArguments().getSerializable(PATROL);
        carBean = (CarBean) getArguments().getSerializable(CAR);
        if(terminalEnum==TerminalEnum.TERMINAL_CAR){//车
            initCar(carBean);
        }else if(terminalEnum==TerminalEnum.TERMINAL_PATROL){//船
            initPatrol(patrolBean);
        }
//        List<DeviceBean> deviceBeans = new ArrayList<>();
//        deviceBeans.add(new DeviceBean(false));
//        deviceBeans.add(new DeviceBean(false));
//        deviceBeans.add(new DeviceBean(false));
//        deviceBeans.add(new DeviceBean(true));
//        deviceBeans.add(new DeviceBean(true));
//        deviceBeans.add(new DeviceBean(true));
//        deviceBeans.add(new DeviceBean(true));
//        deviceBeans.add(new DeviceBean(true));
//        refreshOrLoadMore(deviceBeans);
    }


    /**
     * 初始化 船
     */
    private void initPatrol(PatrolBean patrol) {
        tv_name.setText(patrol.getPatrolName() + "    " + patrol.getPatrolNo());
        tv_department.setText(patrol.getPatrolOccupant());
        Map<String, PersonnelBean> personnelDtoMap = patrol.getPersonnelDtoMap();
        //Todo 其他单独终端设备列表

        List<PersonnelBean> personnels = getPresenter().getPersonnels(personnelDtoMap);
        List<DeviceBean> devices = getPresenter().getDevices(personnels);
        //refreshOrLoadMore(devices);
    }

    /**
     * 初始化 car
     */
    private void initCar(CarBean car) {
        List<DeviceBean> deviceBeanList = new ArrayList<>();
        tv_name.setText(car.getCarName() + "    " + car.getCarNo());
        tv_department.setText(car.getCarOccupant());
        Map<String, PersonnelBean> personnelDtoMap = car.getPersonnelDtoMap();
        //Todo 其他单独终端设备列表
        List<TerminalBean> terminals = car.getTerminals();

        TerminalBean terminalBean = new TerminalBean();
        terminalBean.setTerminalType(TerminalType.TERMINAL_BULL);
        terminalBean.setGb28181No("32010000001320000114");

        if(terminals==null){
            terminals = new ArrayList<>();
            terminals.add(terminalBean);
        }else{
            terminals.add(terminalBean);
        }
        List<DeviceBean> terminals1 = getPresenter().getTerminals(terminals);
        List<PersonnelBean> personnels = getPresenter().getPersonnels(personnelDtoMap);
        List<DeviceBean> devices = getPresenter().getDevices(personnels);

        deviceBeanList.addAll(terminals1);
        deviceBeanList.addAll(devices);

        refreshOrLoadMore(deviceBeanList);
    }


    @Override
    protected void refresh() {

    }

    @Override
    protected void loadMore() {

    }

    @Override
    protected BaseRecycleViewAdapter createAdapter() {
        return new CarBandDeviceAdapter(getContext());
    }

    @Override
    public CarBoatInfoPresenter createPresenter() {
        return new CarBoatInfoPresenter(getContext());
    }


    /**
     * 开启 PoliceInfoFragment
     *
     * @param fragmentActivity
     */
    public static void startCarBoatInfoFragment(FragmentActivity fragmentActivity, PatrolBean patrolBean, TerminalEnum terminalEnum) {
        CarOrPatrolInfoFragment carOrPatrolInfoFragment = new CarOrPatrolInfoFragment();
        Bundle args = new Bundle();
        args.putSerializable(PATROL, patrolBean);
        args.putSerializable(TERMINAL_ENUM, terminalEnum);
        carOrPatrolInfoFragment.setArguments(args);
        FragmentManager supportFragmentManager = fragmentActivity.getSupportFragmentManager();
        //replace 会将上一个Fragment干掉
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fl_layer_member_info, carOrPatrolInfoFragment, FRAGMENT_TAG);
        fragmentTransaction.commit();
    }


    /**
     * 开启 PoliceInfoFragment
     *
     * @param fragmentActivity
     */
    public static void startCarBoatInfoFragment(FragmentActivity fragmentActivity, CarBean carBean, TerminalEnum terminalEnum) {
        CarOrPatrolInfoFragment carOrPatrolInfoFragment = new CarOrPatrolInfoFragment();
        Bundle args = new Bundle();
        args.putSerializable(CAR, carBean);
        args.putSerializable(TERMINAL_ENUM, terminalEnum);
        carOrPatrolInfoFragment.setArguments(args);
        FragmentManager supportFragmentManager = fragmentActivity.getSupportFragmentManager();
        //replace 会将上一个Fragment干掉
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fl_layer_member_info, carOrPatrolInfoFragment, FRAGMENT_TAG);
        fragmentTransaction.commit();
    }

    /**
     * 关闭 PoliceInfoFragment
     *
     * @param fragmentActivity
     */
    public static void closeCarBoatInfoFragment(FragmentActivity fragmentActivity) {
        FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();
        Fragment memberInfo = fragmentManager.findFragmentByTag(FRAGMENT_TAG);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (memberInfo != null) {
            fragmentTransaction.remove(memberInfo);
        }
        fragmentTransaction.commit();
    }

}

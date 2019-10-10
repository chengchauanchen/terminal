package com.vsxin.terminalpad.mvp.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ixiaoma.xiaomabus.architecture.mvp.refresh.adapter.BaseRecycleViewAdapter;
import com.ixiaoma.xiaomabus.architecture.mvp.refresh.fragment.RefreshRecycleViewFragment;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.constant.TerminalEnum;
import com.vsxin.terminalpad.mvp.contract.presenter.CarBoatInfoPresenter;
import com.vsxin.terminalpad.mvp.contract.view.ICarBoatInfoView;
import com.vsxin.terminalpad.mvp.entity.CarBean;
import com.vsxin.terminalpad.mvp.entity.DeviceBean;
import com.vsxin.terminalpad.mvp.entity.PatrolBean;
import com.vsxin.terminalpad.mvp.entity.PersonnelBean;
import com.vsxin.terminalpad.mvp.entity.TerminalBean;
import com.vsxin.terminalpad.mvp.ui.adapter.CarBandDeviceAdapter;
import com.vsxin.terminalpad.utils.NumberUtil;
import com.vsxin.terminalpad.utils.TimeUtil;

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

    @BindView(R.id.tv_phone)
    TextView tv_phone;//电话

    @BindView(R.id.tv_group_no)
    TextView tv_group_no;//当前组号

    @BindView(R.id.tv_speed)
    TextView tv_speed;//速度

    @BindView(R.id.tv_time)
    TextView tv_time;//定位时间

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
        if (terminalEnum == TerminalEnum.TERMINAL_CAR) {//车
            initCar(carBean);
        } else if (terminalEnum == TerminalEnum.TERMINAL_PATROL) {//船
            initPatrol(patrolBean);
        }
    }


    /**
     * 初始化 船
     */
    private void initPatrol(PatrolBean patrol) {
        List<DeviceBean> deviceBeanList = new ArrayList<>();

        //名称
        tv_name.setText(TextUtils.isEmpty(patrol.getPatrolName()) ? "" : patrol.getPatrolName());
        //部门
        tv_department.setText(TextUtils.isEmpty(patrol.getPatrolOccupant()) ? getString(R.string.donghu) : patrol.getPatrolOccupant());
        //电话
        tv_phone.setText(TextUtils.isEmpty(patrol.getPhoneNumber()) ? "" : patrol.getPhoneNumber());
        //当前组号
        tv_group_no.setText(TextUtils.isEmpty(patrol.getGroup()) ? "" : patrol.getGroup());
        //速度
        tv_speed.setText(TextUtils.isEmpty(patrol.getSpeed()) ? "0km/h" : patrol.getSpeed()+"km/h");

        String patrolLastGpsGenerationTime = getPresenter().getPatrolLastGpsGenerationTime(patrol);
        tv_time.setText("定位时间："+TimeUtil.formatLongToStr(NumberUtil.strToLong(patrolLastGpsGenerationTime)));

        //获取 终端设备集合
        Map<String, TerminalBean> terminalDtoMap = patrol.getTerminalDtoMap();
        //获取 民警集合
        Map<String, PersonnelBean> personnelDtoMap = patrol.getPersonnelDtoMap();

        List<TerminalBean> terminalBeans = getPresenter().getTerminalBeans(terminalDtoMap);
        List<PersonnelBean> personnelBeans = getPresenter().getPersonnels(personnelDtoMap);

        List<DeviceBean> deviceBeans = getPresenter().changerTerminalToDevice(terminalBeans);
        List<DeviceBean> devices = getPresenter().changerPersonnelToDevice(personnelBeans);

        deviceBeanList.addAll(deviceBeans);
        deviceBeanList.addAll(devices);
        refreshOrLoadMore(deviceBeanList);

    }

    /**
     * 初始化 car
     */
    private void initCar(CarBean car) {
        List<DeviceBean> deviceBeanList = new ArrayList<>();

        String carName = TextUtils.isEmpty(car.getCarName()) ? "" : car.getCarName();
        String carNo = TextUtils.isEmpty(car.getCarNo()) ? "" : car.getCarNo();

        //名称
        tv_name.setText(carName + "    " + carNo);
        //部门
        tv_department.setText(TextUtils.isEmpty(car.getCarOccupant()) ? getString(R.string.donghu) : car.getCarOccupant());
        //电话
        tv_phone.setText(TextUtils.isEmpty(car.getPhoneNumber()) ? "" : car.getPhoneNumber());
        //当前组号
        tv_group_no.setText(TextUtils.isEmpty(car.getGroup()) ? "" : car.getGroup());
        //速度
        tv_speed.setText(TextUtils.isEmpty(car.getSpeed()) ? "0km/h" : car.getSpeed()+"km/h");

        String carLastGpsGenerationTime = getPresenter().getCarLastGpsGenerationTime(carBean);
        tv_time.setText("定位时间："+TimeUtil.formatLongToStr(NumberUtil.strToLong(carLastGpsGenerationTime)));

        //获取 终端设备集合
        Map<String, TerminalBean> terminalDtoMap = car.getTerminalDtoMap();
        //获取 民警集合
        Map<String, PersonnelBean> personnelDtoMap = car.getPersonnelDtoMap();

        List<TerminalBean> terminalBeans = getPresenter().getTerminalBeans(terminalDtoMap);
        List<PersonnelBean> personnelBeans = getPresenter().getPersonnels(personnelDtoMap);

        List<DeviceBean> deviceBeans = getPresenter().changerTerminalToDevice(terminalBeans);
        List<DeviceBean> devices = getPresenter().changerPersonnelToDevice(personnelBeans);

        deviceBeanList.addAll(deviceBeans);
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

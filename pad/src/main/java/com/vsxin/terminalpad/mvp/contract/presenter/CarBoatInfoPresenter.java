package com.vsxin.terminalpad.mvp.contract.presenter;

import android.content.Context;

import com.ixiaoma.xiaomabus.architecture.mvp.refresh.RefreshPresenter;
import com.vsxin.terminalpad.mvp.contract.view.ICarBoatInfoView;
import com.vsxin.terminalpad.mvp.entity.DeviceBean;
import com.vsxin.terminalpad.mvp.entity.PersonnelBean;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author qzw
 * <p>
 * 地图气泡点击-车、船 详情页
 * 显示车、船上绑定的民警和终端设备
 */
public class CarBoatInfoPresenter extends RefreshPresenter<DeviceBean, ICarBoatInfoView> {

    public CarBoatInfoPresenter(Context mContext) {
        super(mContext);
    }

    /**
     * 获取 PersonnelBean list
     * @param personnelDtoMap
     * @return
     */
    public List<PersonnelBean> getPersonnels(Map<String, PersonnelBean> personnelDtoMap){
        List<PersonnelBean> personnelBeans = new ArrayList<>();
        for(String key:personnelDtoMap.keySet()){//keySet获取map集合key的集合  然后在遍历key即可
            PersonnelBean personnelBean = personnelDtoMap.get(key);//
            personnelBeans.add(personnelBean);
        }
        return personnelBeans;
    }

    /**
     * 拼接数据
     * @return
     */
    public List<DeviceBean> getDevices(List<PersonnelBean> personnels){
        List<DeviceBean> deviceBeans = new ArrayList<>();
        for (PersonnelBean personnelBean : personnels){
            DeviceBean deviceBean = new DeviceBean();
            deviceBean.setPersonnel(personnelBean);
            deviceBean.setPolice(true);
            deviceBeans.add(deviceBean);
        }
        return deviceBeans;
    }

}

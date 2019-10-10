package com.vsxin.terminalpad.mvp.contract.presenter;

import android.content.Context;
import android.text.TextUtils;

import com.ixiaoma.xiaomabus.architecture.mvp.refresh.RefreshPresenter;
import com.vsxin.terminalpad.mvp.contract.constant.TerminalEnum;
import com.vsxin.terminalpad.mvp.contract.view.ICarBoatInfoView;
import com.vsxin.terminalpad.mvp.entity.CarBean;
import com.vsxin.terminalpad.mvp.entity.DeviceBean;
import com.vsxin.terminalpad.mvp.entity.PatrolBean;
import com.vsxin.terminalpad.mvp.entity.PersonnelBean;
import com.vsxin.terminalpad.mvp.entity.TerminalBean;
import com.vsxin.terminalpad.utils.SortUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
     * 巡逻船 获取最后定位时间
     * 在线
     * 离线
     * 1. 优先使用车载布控球和车载电台定位；
     * 2. 其次使用绑定的警务通定位；
     * 3. 再次使用执法记录仪定位；
     * 4. 再次使用350M电台定位。
     *
     * @return
     */
    public String getPatrolLastGpsGenerationTime(PatrolBean patrol) {
        return SortUtil.getPatrolLastGpsGenerationTime(patrol);
    }

    /**
     * 警车 获取最后定位时间
     * @param carBean
     * @return
     */
    public String getCarLastGpsGenerationTime(CarBean carBean) {
        return SortUtil.getCarLastGpsGenerationTime(carBean);
    }

    /**
     * 获取 PersonnelBean list
     *
     * @param personnelDtoMap
     * @return
     */
    public List<PersonnelBean> getPersonnels(Map<String, PersonnelBean> personnelDtoMap) {
        List<PersonnelBean> personnelBeans = new ArrayList<>();
        for (String key : personnelDtoMap.keySet()) {//keySet获取map集合key的集合  然后在遍历key即可
            PersonnelBean personnelBean = personnelDtoMap.get(key);//
            personnelBeans.add(personnelBean);
        }
        return personnelBeans;
    }

    /**
     * 获取 TerminalBean list
     *
     * @param terminalBeanMap
     * @return
     */
    public List<TerminalBean> getTerminalBeans(Map<String, TerminalBean> terminalBeanMap) {
        List<TerminalBean> terminalBeans = new ArrayList<>();
        for (String key : terminalBeanMap.keySet()) {//keySet获取map集合key的集合  然后在遍历key即可
            TerminalBean personnelBean = terminalBeanMap.get(key);//
            terminalBeans.add(personnelBean);
        }
        return terminalBeans;
    }


    /**
     * 将TerminalBean转化为DeviceBean
     *
     * @return
     */
    public List<DeviceBean> changerTerminalToDevice(List<TerminalBean> terminalBeans) {
        List<DeviceBean> deviceBeans = new ArrayList<>();
        for (TerminalBean terminalBean : terminalBeans) {
            DeviceBean deviceBean = new DeviceBean();
            deviceBean.setTerminal(terminalBean);
            deviceBean.setPolice(false);
            deviceBeans.add(deviceBean);
        }
        return deviceBeans;
    }

    /**
     * 将PersonnelBean转化为DeviceBean
     *
     * @return
     */
    public List<DeviceBean> changerPersonnelToDevice(List<PersonnelBean> personnels) {
        List<DeviceBean> deviceBeans = new ArrayList<>();
        for (PersonnelBean personnelBean : personnels) {
            DeviceBean deviceBean = new DeviceBean();
            deviceBean.setPersonnel(personnelBean);
            deviceBean.setPolice(true);
            deviceBeans.add(deviceBean);
        }
        return deviceBeans;
    }

}

package com.vsxin.terminalpad.utils;

import android.text.TextUtils;

import com.vsxin.terminalpad.mvp.contract.presenter.CarBoatInfoPresenter;
import com.vsxin.terminalpad.mvp.entity.CarBean;
import com.vsxin.terminalpad.mvp.entity.PatrolBean;
import com.vsxin.terminalpad.mvp.entity.PersonnelBean;
import com.vsxin.terminalpad.mvp.entity.TerminalBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 排序工具类
 */
public class SortUtil {

    /**
     * 获取警员最后上线时间
     * @param personnelBean
     * @return
     */
    public static String getPersonnelLastGpsGenerationTime(PersonnelBean personnelBean){
        boolean isRise = false;
        String lastTerminalTime = "";
        List<TerminalBean> terminalDtoList = personnelBean.getTerminalDtoList();
        Collections.sort(terminalDtoList, new Comparator<TerminalBean>() {
            @Override
            public int compare(TerminalBean u1, TerminalBean u2) {

                String t1GpsTime = TextUtils.isEmpty(u1.getGpsGenerationTime()) ? "" :u1.getGpsGenerationTime();
                String t2GpsTime = TextUtils.isEmpty(u2.getGpsGenerationTime()) ? "" : u2.getGpsGenerationTime();
                if (isRise) {// 升序排序
                    return t1GpsTime.compareTo(t2GpsTime);
                } else {// 降序排序
                    return t2GpsTime.compareTo(t1GpsTime);
                }
            }
        });
        if(terminalDtoList!=null  && terminalDtoList.size()>0){
            if(!TextUtils.isEmpty(terminalDtoList.get(0).getGpsGenerationTime())){
                lastTerminalTime = terminalDtoList.get(0).getGpsGenerationTime();
            }
        }
        return lastTerminalTime;
    }

    /**
     * 获取 警车最后上线时间
     * @param carBean
     * @return
     */
    public static String getCarLastGpsGenerationTime(CarBean carBean) {

        //单独装备最后上线时间
        String lastTerminalTime = "";
        String lastPersonnelTime = "";

        Map<String, TerminalBean> terminalDtoMap = carBean.getTerminalDtoMap();
        if (terminalDtoMap != null && !terminalDtoMap.isEmpty()) {
            Map<String, TerminalBean> sortTerminalBeanMap = SortUtil.sortMapTerminal(terminalDtoMap, false);//降序 第一个最大
            TerminalBean firstTerminal = SortUtil.getFirstTerminalBean(sortTerminalBeanMap);
            if(firstTerminal!=null){
                lastTerminalTime = firstTerminal.getGpsGenerationTime();
            }
        }
        //绑定民警 最后上线时间最大者
        Map<String, PersonnelBean> personnelDtoMap = carBean.getPersonnelDtoMap();
        if(personnelDtoMap != null && !personnelDtoMap.isEmpty()){
            Map<String, PersonnelBean> sortPersonnelBeanMap = SortUtil.sortMapPersonnel(personnelDtoMap,false);//降序 第一个最大
            PersonnelBean firstPersonnel = SortUtil.getFirstPersonnel(sortPersonnelBeanMap);
            if(firstPersonnel!=null && firstPersonnel.getTerminalDtoList()!=null && firstPersonnel.getTerminalDtoList().size()>0){
                if(!TextUtils.isEmpty(firstPersonnel.getTerminalDtoList().get(0).getGpsGenerationTime())){
                    lastPersonnelTime = firstPersonnel.getTerminalDtoList().get(0).getGpsGenerationTime();
                }
            }
        }
        if(!TextUtils.isEmpty(lastTerminalTime) && !TextUtils.isEmpty(lastPersonnelTime)){
            return lastTerminalTime.compareTo(lastPersonnelTime)>0?lastTerminalTime:lastPersonnelTime;
        }else if(TextUtils.isEmpty(lastTerminalTime)) {
            return lastPersonnelTime;
        }else {
            return lastTerminalTime;
        }
    }

    /**
     * 获取 巡逻船最后上线时间
     * @param patrol
     * @return
     */
    public static String getPatrolLastGpsGenerationTime(PatrolBean patrol) {

        //单独装备最后上线时间
        String lastTerminalTime = "";
        String lastPersonnelTime = "";

        Map<String, TerminalBean> terminalDtoMap = patrol.getTerminalDtoMap();
        if (terminalDtoMap != null && !terminalDtoMap.isEmpty()) {
            Map<String, TerminalBean> sortTerminalBeanMap = SortUtil.sortMapTerminal(terminalDtoMap, false);//降序 第一个最大
            TerminalBean firstTerminal = SortUtil.getFirstTerminalBean(sortTerminalBeanMap);
            if(firstTerminal!=null){
                lastTerminalTime = firstTerminal.getGpsGenerationTime();
            }
        }
        //绑定民警 最后上线时间最大者
        Map<String, PersonnelBean> personnelDtoMap = patrol.getPersonnelDtoMap();
        if(personnelDtoMap != null && !personnelDtoMap.isEmpty()){
            Map<String, PersonnelBean> sortPersonnelBeanMap = SortUtil.sortMapPersonnel(personnelDtoMap,false);//降序 第一个最大
            PersonnelBean firstPersonnel = SortUtil.getFirstPersonnel(sortPersonnelBeanMap);
            if(firstPersonnel!=null && firstPersonnel.getTerminalDtoList()!=null && firstPersonnel.getTerminalDtoList().size()>0){
                if(!TextUtils.isEmpty(firstPersonnel.getTerminalDtoList().get(0).getGpsGenerationTime())){
                    lastPersonnelTime = firstPersonnel.getTerminalDtoList().get(0).getGpsGenerationTime();
                }
            }
        }
        if(!TextUtils.isEmpty(lastTerminalTime) && !TextUtils.isEmpty(lastPersonnelTime)){
            return lastTerminalTime.compareTo(lastPersonnelTime)>0?lastTerminalTime:lastPersonnelTime;
        }else if(TextUtils.isEmpty(lastTerminalTime)) {
            return lastPersonnelTime;
        }else {
            return lastTerminalTime;
        }
    }

    /**
     * 获取map中第一个数据值
     *
     * @param map 数据源
     * @return
     */
    public static PersonnelBean getFirstPersonnel(Map<String, PersonnelBean> map) {
        PersonnelBean obj = null;
        for (Map.Entry<String, PersonnelBean> entry : map.entrySet()) {
            obj = entry.getValue();
            if (obj != null) {
                break;
            }
        }
        return obj;
    }

    /**
     * 获取map中第一个数据值
     *
     * @param map 数据源
     * @return
     */
    public static TerminalBean getFirstTerminalBean(Map<String, TerminalBean> map) {
        TerminalBean obj = null;
        if(map==null || map.isEmpty()){
            return obj;
        }
        for (Map.Entry<String, TerminalBean> entry : map.entrySet()) {
            obj = entry.getValue();
            if (obj != null) {
                break;
            }
        }
        return obj;
    }

    /**
     * map按value排序
     *
     * @param oriMap
     * @param isRise true 升序 false 降序
     * @return
     */
    public static Map<String, PersonnelBean> sortMapPersonnel(Map<String, PersonnelBean> oriMap, boolean isRise) {
        if (oriMap == null || oriMap.isEmpty()) {
            return null;
        }
        Map<String, PersonnelBean> sortedMap = new LinkedHashMap<String, PersonnelBean>();
        List<Map.Entry<String, PersonnelBean>> entryList = new ArrayList<Map.Entry<String, PersonnelBean>>(
                oriMap.entrySet());
        Collections.sort(entryList, new MapValueComparator2(isRise));
        Iterator<Map.Entry<String, PersonnelBean>> iter = entryList.iterator();
        Map.Entry<String, PersonnelBean> tmpEntry = null;
        while (iter.hasNext()) {
            tmpEntry = iter.next();
            sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
        }
        return sortedMap;
    }

    public static class MapValueComparator2 implements Comparator<Map.Entry<String, PersonnelBean>> {
        boolean isRise;//true 升序 false 降序

        MapValueComparator2(boolean isRise) {
            this.isRise = isRise;
        }

        @Override
        public int compare(Map.Entry<String, PersonnelBean> me1, Map.Entry<String, PersonnelBean> me2) {
            //按照从大到小的顺序排列，如果想正序 调换me1 me2的位置
            List<TerminalBean> terminalDtoList1 = me1.getValue().getTerminalDtoList();
            List<TerminalBean> terminalDtoList2 = me2.getValue().getTerminalDtoList();

            Collections.sort(terminalDtoList1, new Comparator<TerminalBean>() {
                @Override
                public int compare(TerminalBean u1, TerminalBean u2) {

                    String t1GpsTime = TextUtils.isEmpty(u1.getGpsGenerationTime()) ? "" :u1.getGpsGenerationTime();
                    String t2GpsTime = TextUtils.isEmpty(u2.getGpsGenerationTime()) ? "" : u2.getGpsGenerationTime();
                    if (isRise) {// 升序排序
                        return t1GpsTime.compareTo(t2GpsTime);
                    } else {// 降序排序
                        return t2GpsTime.compareTo(t1GpsTime);
                    }
                }
            });

            Collections.sort(terminalDtoList2, new Comparator<TerminalBean>() {
                @Override
                public int compare(TerminalBean u1, TerminalBean u2) {

                    String t1GpsTime = TextUtils.isEmpty(u1.getGpsGenerationTime()) ? "" :u1.getGpsGenerationTime();
                    String t2GpsTime = TextUtils.isEmpty(u2.getGpsGenerationTime()) ? "" : u2.getGpsGenerationTime();
                    if (isRise) {// 升序排序
                        return t1GpsTime.compareTo(t2GpsTime);
                    } else {// 降序排序
                        return t2GpsTime.compareTo(t1GpsTime);
                    }
                }
            });

            TerminalBean terminalBean1= terminalDtoList1.get(0);
            TerminalBean terminalBean2 = terminalDtoList2.get(0);

            String t1GpsTime = TextUtils.isEmpty(terminalBean1.getGpsGenerationTime()) ? "" : terminalBean1.getGpsGenerationTime();
            String t2GpsTime = TextUtils.isEmpty(terminalBean2.getGpsGenerationTime()) ? "" :terminalBean2.getGpsGenerationTime();
            if (isRise) {// 升序排序
                return t1GpsTime.compareTo(t2GpsTime);
            } else {// 降序排序
                return t2GpsTime.compareTo(t1GpsTime);
            }
        }
    }

    /**
     * map按value排序
     *
     * @param oriMap
     * @param isRise true 升序 false 降序
     * @return
     */
    public static Map<String, TerminalBean> sortMapTerminal(Map<String, TerminalBean> oriMap, boolean isRise) {
        if (oriMap == null || oriMap.isEmpty()) {
            return null;
        }
        Map<String, TerminalBean> sortedMap = new LinkedHashMap<String, TerminalBean>();
        List<Map.Entry<String, TerminalBean>> entryList = new ArrayList<Map.Entry<String, TerminalBean>>(
                oriMap.entrySet());
        Collections.sort(entryList, new MapValueComparator(isRise));
        Iterator<Map.Entry<String, TerminalBean>> iter = entryList.iterator();
        Map.Entry<String, TerminalBean> tmpEntry = null;
        while (iter.hasNext()) {
            tmpEntry = iter.next();
            sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
        }
        return sortedMap;
    }

    public static class MapValueComparator implements Comparator<Map.Entry<String, TerminalBean>> {
        boolean isRise;//true 升序 false 降序

        MapValueComparator(boolean isRise) {
            this.isRise = isRise;
        }

        @Override
        public int compare(Map.Entry<String, TerminalBean> me1, Map.Entry<String, TerminalBean> me2) {
            //按照从大到小的顺序排列，如果想正序 调换me1 me2的位置

            String t1GpsTime = TextUtils.isEmpty(me1.getValue().getGpsGenerationTime()) ? "" : me1.getValue().getGpsGenerationTime();
            String t2GpsTime = TextUtils.isEmpty(me2.getValue().getGpsGenerationTime()) ? "" : me2.getValue().getGpsGenerationTime();
            if (isRise) {// 升序排序
                return t1GpsTime.compareTo(t2GpsTime);
            } else {// 降序排序
                return t2GpsTime.compareTo(t1GpsTime);
            }
        }
    }
}

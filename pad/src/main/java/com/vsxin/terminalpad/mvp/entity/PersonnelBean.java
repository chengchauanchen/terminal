package com.vsxin.terminalpad.mvp.entity;

import com.ixiaoma.xiaomabus.architecture.bean.BaseBean;

import java.util.List;

/**
 * 民警实体Bean
 */
public class PersonnelBean extends BaseBean {
    private String personnelName;//民警名称
    private String personnelNo;//警号
    private String personnelLat;
    private String personnelLng;
    private String department;//部门
    private String name;//名称
    private String phoneNumber;//电话号码
    private String terminalType;//类型
    private String group;//组id

    private String bearing;//方向
    private String altitude;//海拔高度
    private String speed;//速度

    private List<TerminalBean> terminalDtoList;//终端list

    public String getPersonnelName() {
        return personnelName;
    }

    public void setPersonnelName(String personnelName) {
        this.personnelName = personnelName;
    }

    public String getPersonnelNo() {
        return personnelNo;
    }

    public void setPersonnelNo(String personnelNo) {
        this.personnelNo = personnelNo;
    }

    public String getPersonnelLat() {
        return personnelLat;
    }

    public void setPersonnelLat(String personnelLat) {
        this.personnelLat = personnelLat;
    }

    public String getPersonnelLng() {
        return personnelLng;
    }

    public void setPersonnelLng(String personnelLng) {
        this.personnelLng = personnelLng;
    }

    public List<TerminalBean> getTerminalDtoList() {
        return terminalDtoList;
    }

    public void setTerminalDtoList(List<TerminalBean> terminalDtoList) {
        this.terminalDtoList = terminalDtoList;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getTerminalType() {
        return terminalType;
    }

    public void setTerminalType(String terminalType) {
        this.terminalType = terminalType;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getBearing() {
        return bearing;
    }

    public void setBearing(String bearing) {
        this.bearing = bearing;
    }

    public String getAltitude() {
        return altitude;
    }

    public void setAltitude(String altitude) {
        this.altitude = altitude;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }
}


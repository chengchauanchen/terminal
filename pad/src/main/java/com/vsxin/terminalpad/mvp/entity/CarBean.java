package com.vsxin.terminalpad.mvp.entity;

import java.util.List;
import java.util.Map;

/**
 * 警车
 *
 *
 */
public class CarBean extends BaseBean {
    private String carName;//车名
    private String carOccupant;//部门
    private String carLat;//
    private String carLng;
    private String carNo;//车牌

    private String phoneNumber;//电话号码
    private String bearing;//方向
    private String altitude;//海拔高度
    private String group;//组id
    private String speed;//速度
    private String terminalType;//类型
    private String status;//类型
    private String crateTime;//
    private String updateTime;//
    private String gpsGenerationTime;//定位时间

    private Map<String,PersonnelBean> personnelDtoMap;//绑定在车上警员
    private Map<String,TerminalBean> terminalDtoMap;//直接绑定在车上的设备

    public String getCarName() {
        return carName;
    }

    public void setCarName(String carName) {
        this.carName = carName;
    }

    public String getCarOccupant() {
        return carOccupant;
    }

    public void setCarOccupant(String carOccupant) {
        this.carOccupant = carOccupant;
    }

    public String getCarLat() {
        return carLat;
    }

    public void setCarLat(String carLat) {
        this.carLat = carLat;
    }

    public String getCarLng() {
        return carLng;
    }

    public void setCarLng(String carLng) {
        this.carLng = carLng;
    }

    public String getCarNo() {
        return carNo;
    }

    public void setCarNo(String carNo) {
        this.carNo = carNo;
    }

    public Map<String, PersonnelBean> getPersonnelDtoMap() {
        return personnelDtoMap;
    }

    public void setPersonnelDtoMap(Map<String, PersonnelBean> personnelDtoMap) {
        this.personnelDtoMap = personnelDtoMap;
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

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getTerminalType() {
        return terminalType;
    }

    public void setTerminalType(String terminalType) {
        this.terminalType = terminalType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCrateTime() {
        return crateTime;
    }

    public void setCrateTime(String crateTime) {
        this.crateTime = crateTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getGpsGenerationTime() {
        return gpsGenerationTime;
    }

    public void setGpsGenerationTime(String gpsGenerationTime) {
        this.gpsGenerationTime = gpsGenerationTime;
    }

    public Map<String, TerminalBean> getTerminalDtoMap() {
        return terminalDtoMap;
    }

    public void setTerminalDtoMap(Map<String, TerminalBean> terminalDtoMap) {
        this.terminalDtoMap = terminalDtoMap;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}

/**
 *
 *"carName": "听涛派出所,鄂A0787警",
 * 		"carOccupant": "听涛派出所",
 * 		"carLat": "30.59675",
 * 		"carLng": "114.38873",
 * 		"carNo": "鄂A0787警",
 * 		"personnelDtoMap": {
 *
 **/
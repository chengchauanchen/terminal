package com.vsxin.terminalpad.mvp.entity;

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
    private Map<String,PersonnelBean> personnelDtoMap;//警员

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
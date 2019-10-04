package com.vsxin.terminalpad.mvp.entity;

import com.ixiaoma.xiaomabus.architecture.bean.BaseBean;

import java.util.Map;

/**
 * 巡逻船
 *
 */
public class PatrolBean extends BaseBean {
    private String patrolNo;//巡逻船编号
    private String patrolName;//巡逻船名称
    private String patrolOccupant;//所在部门
    private String patrolLat;
    private String patrolLng;

    private String phoneNumber;//电话号码
    private String bearing;//方向
    private String altitude;//海拔高度
    private String group;//组id
    private String speed;//速度
    private String terminalType;//类型
    private String status;//类型
    private Long crateTime;//
    private Long updateTime;//
    private Long gpsGenerationTime;//定位时间

    private Map<String,PersonnelBean> personnelDtoMap;//key:88021206,value:警员map
    private Map<String,TerminalBean> terminalDtoMap;//key:88021206,value:设备map

    public String getPatrolNo() {
        return patrolNo;
    }

    public void setPatrolNo(String patrolNo) {
        this.patrolNo = patrolNo;
    }

    public String getPatrolName() {
        return patrolName;
    }

    public void setPatrolName(String patrolName) {
        this.patrolName = patrolName;
    }

    public String getPatrolOccupant() {
        return patrolOccupant;
    }

    public void setPatrolOccupant(String patrolOccupant) {
        this.patrolOccupant = patrolOccupant;
    }

    public String getPatrolLat() {
        return patrolLat;
    }

    public void setPatrolLat(String patrolLat) {
        this.patrolLat = patrolLat;
    }

    public String getPatrolLng() {
        return patrolLng;
    }

    public void setPatrolLng(String patrolLng) {
        this.patrolLng = patrolLng;
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

    public Long getCrateTime() {
        return crateTime;
    }

    public void setCrateTime(Long crateTime) {
        this.crateTime = crateTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public Long getGpsGenerationTime() {
        return gpsGenerationTime;
    }

    public void setGpsGenerationTime(Long gpsGenerationTime) {
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
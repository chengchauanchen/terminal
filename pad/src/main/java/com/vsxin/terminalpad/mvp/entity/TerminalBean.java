package com.vsxin.terminalpad.mvp.entity;

/**
 * 终端/装备
 *
*                      "terminalType":"TERMINAL_BALL",
 *                     "terminalGroupNo":null,
 *                     "terminalUniqueNo":null,
 *                     "account":null,
 *                     "gb28181No":"32010000001320000114",
 *                     "group":null,
 *                     "lteNo":"32010000001320000114",
 *                     "pdtNo":null,
 *                     "ddtNo":null,
 *                     "ddtType":null,
 *                     "speed":"0.0",
 *                     "altitude":"0",
 *                     "bearing":"0.0",
 *                     "lat":null,
 *                     "lng":null,
 *                     "status":null,
 *                     "gpsGenerationTime":null
 */
public class TerminalBean extends BaseBean {
    private String phoneNumber;//电话号码
    private String terminalType;//终端类型
    private String terminalGroupNo;//阻断组号
    private String terminalUniqueNo;//终端UniqueNo

    private String department;//部门

    private String account;
    private String gb28181No;
    private String group;
    private String lteNo;
    private String pdtNo;
    private String ddtType;//交管电台类型
    private String speed;//速度
    private String altitude;//海拔高度
    private String bearing;//方向
    private String lat;
    private String lng;
    private String status;
    private String gpsGenerationTime;//定位时间

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getGb28181No() {
        return gb28181No;
    }

    public void setGb28181No(String gb28181No) {
        this.gb28181No = gb28181No;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getLteNo() {
        return lteNo;
    }

    public void setLteNo(String lteNo) {
        this.lteNo = lteNo;
    }

    public String getPdtNo() {
        return pdtNo;
    }

    public void setPdtNo(String pdtNo) {
        this.pdtNo = pdtNo;
    }

    public String getTerminalType() {
        return terminalType;
    }

    public void setTerminalType(String terminalType) {
        this.terminalType = terminalType;
    }

    public String getTerminalGroupNo() {
        return terminalGroupNo;
    }

    public void setTerminalGroupNo(String terminalGroupNo) {
        this.terminalGroupNo = terminalGroupNo;
    }

    public String getTerminalUniqueNo() {
        return terminalUniqueNo;
    }

    public void setTerminalUniqueNo(String terminalUniqueNo) {
        this.terminalUniqueNo = terminalUniqueNo;
    }

    public String getDdtType() {
        return ddtType;
    }

    public void setDdtType(String ddtType) {
        this.ddtType = ddtType;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getAltitude() {
        return altitude;
    }

    public void setAltitude(String altitude) {
        this.altitude = altitude;
    }

    public String getBearing() {
        return bearing;
    }

    public void setBearing(String bearing) {
        this.bearing = bearing;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGpsGenerationTime() {
        return gpsGenerationTime;
    }

    public void setGpsGenerationTime(String gpsGenerationTime) {
        this.gpsGenerationTime = gpsGenerationTime;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    //bearing 方向字段解释
//    public float getBearing()获取方向角(单位：度）默认值：0.0
//    取值范围：【0，360】，其中0度表示正北方向，90度表示正东，180度表示正南，270度表示正西
//    3.1.0之前的版本只有定位类型为 AMapLocation.LOCATION_TYPE_GPS时才有值
//    自3.1.0版本开始，不限定定位类型，当定位类型不是AMapLocation.LOCATION_TYPE_GPS时，可以通过 AMapLocationClientOption.setSensorEnable(boolean) 控制是否返回方向角，当设置为true时会通过手机传感器获取方向角,如果手机没有对应的传感器会返回0.0
//    注意：
//    定位类型为AMapLocation.LOCATION_TYPE_GPS时，方向角指的是运动方向
//    定位类型不是AMapLocation.LOCATION_TYPE_GPS时，方向角指的是手机朝向
//
//    返回:方向角从以下版本开始:2.0.0

}

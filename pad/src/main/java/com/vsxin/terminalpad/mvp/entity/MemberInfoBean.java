package com.vsxin.terminalpad.mvp.entity;

public class MemberInfoBean extends BaseBean{
    /**
 *         "no":"32010000001320009828",
     *     "speed":0,
     *     "lng":114.2583,
     *     "lat":30.61417,
     *     "uploadTime":"2019-07-11 17:22:22",
     *     "updateTime":1562837152000,
     *     "name":"布控球9828",
     *     "memberType":"TERMINAL_EQUIPMENT",
     *     "deptNo":"1",
     *     "deptName":"WHPolice",
     *     "gbNo":"32010000001320009828",
     *     "imisNo":"32010000001320009828",
     *     "gUniqueNo":"32010000001320009828"
     */

    private String no;
    private String speed;
    private String lng;
    private String lat;
    private String uploadTime;
    private String updateTime;
    private String name;
    private String memberType;
    private String deptNo;
    private String deptName;
    private String gbNo;
    private String imisNo;
    private String gUniqueNo;

    private int type2;

    public int getType2() {
        return type2;
    }

    public void setType2(int type2) {
        this.type2 = type2;
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(String uploadTime) {
        this.uploadTime = uploadTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMemberType() {
        return memberType;
    }

    public void setMemberType(String memberType) {
        this.memberType = memberType;
    }

    public String getDeptNo() {
        return deptNo;
    }

    public void setDeptNo(String deptNo) {
        this.deptNo = deptNo;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getGbNo() {
        return gbNo;
    }

    public void setGbNo(String gbNo) {
        this.gbNo = gbNo;
    }

    public String getImisNo() {
        return imisNo;
    }

    public void setImisNo(String imisNo) {
        this.imisNo = imisNo;
    }

    public String getgUniqueNo() {
        return gUniqueNo;
    }

    public void setgUniqueNo(String gUniqueNo) {
        this.gUniqueNo = gUniqueNo;
    }
}

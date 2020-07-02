package ptt.terminalsdk.bean;

import java.io.Serializable;
import java.util.List;

public class NfcDataBean implements Serializable {
    private static final long serialVersionUID = -2567478351625252532L;
    //绑定账号的名字
    private String name;
    //绑定账号的编号
    private int no;
    //绑定账号的设备编号
    private String uNo;
    //绑定账号的组编号
    private int gNo;
    //业务的状态
    private int state;
    //手机号
    private String phoneNo;
    //绑定账号的部门
    private String deptName;

    private boolean vType;
    //标记
    private String tag;
    //语音提示
    private List<String> vStr;

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public String getuNo() {
        return uNo;
    }

    public void setuNo(String uNo) {
        this.uNo = uNo;
    }

    public int getgNo() {
        return gNo;
    }

    public void setgNo(int gNo) {
        this.gNo = gNo;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public List<String> getvStr() {
        return vStr;
    }

    public void setvStr(List<String> vStr) {
        this.vStr = vStr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public boolean isvType() {
        return vType;
    }

    public void setvType(boolean vType) {
        this.vType = vType;
    }

    @Override public String toString() {
        return "NfcDataBean{" +
            "name='" + name + '\'' +
            ", no=" + no +
            ", uNo='" + uNo + '\'' +
            ", gNo=" + gNo +
            ", state=" + state +
            ", phoneNo='" + phoneNo + '\'' +
            ", deptName='" + deptName + '\'' +
            ", vType=" + vType +
            ", tag=" + tag +
            ", vStr=" + vStr +
            '}';
    }
}

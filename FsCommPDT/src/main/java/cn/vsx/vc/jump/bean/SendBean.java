package cn.vsx.vc.jump.bean;

import java.util.List;

/**
 * 第三方发送过来的数据bean
 */
public class SendBean extends BaseBean {
    private String memberNo;//警号
    private String groupNo;//组号
    private String groupName;//组名
    private int terminalType=-1;//终端类型 1:手机   6:PC
    List<String > numberList;//警号list
    List<String> groupList;//组号list
    private String phoneNo;//手机号


    private int whatVsxSDKProcess;//融合通信后台进程的状态怎么样?
    private String loginState;//登录状态
    private long time;//当前时间戳
    private boolean isAuth;//是否走广播认证(自启动)

    public int getWhatVsxSDKProcess() {
        return whatVsxSDKProcess;
    }

    public void setWhatVsxSDKProcess(int whatVsxSDKProcess) {
        this.whatVsxSDKProcess = whatVsxSDKProcess;
    }

    public String getLoginState() {
        return loginState;
    }

    public void setLoginState(String loginState) {
        this.loginState = loginState;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isAuth() {
        return isAuth;
    }

    public void setAuth(boolean auth) {
        isAuth = auth;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getMemberNo() {
        return memberNo;
    }

    public void setMemberNo(String memberNo) {
        this.memberNo = memberNo;
    }

    public String getGroupNo() {
        return groupNo;
    }

    public void setGroupNo(String groupNo) {
        this.groupNo = groupNo;
    }

    public int getTerminalType() {
        return terminalType;
    }

    public void setTerminalType(int terminalType) {
        this.terminalType = terminalType;
    }

    public List<String> getNumberList() {
        return numberList;
    }

    public void setNumberList(List<String> numberList) {
        this.numberList = numberList;
    }

    public List<String> getGroupList() {
        return groupList;
    }

    public void setGroupList(List<String> groupList) {
        this.groupList = groupList;
    }
}

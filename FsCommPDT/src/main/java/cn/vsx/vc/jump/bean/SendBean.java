package cn.vsx.vc.jump.bean;

import java.util.List;

/**
 * 第三方发送过来的数据bean
 */
public class SendBean extends BaseBean {
    private String memberNo;//警号
    private String groupNo;//组号
    private int terminalType=-1;//终端类型 1:手机   6:PC
    List<String > numberList;//警号list
    List<String> groupList;//组号list
    private String phoneNo;//手机号

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

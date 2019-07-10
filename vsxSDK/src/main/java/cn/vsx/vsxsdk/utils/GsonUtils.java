package cn.vsx.vsxsdk.utils;

import com.google.gson.Gson;

import java.util.List;

import cn.vsx.vsxsdk.bean.SendBean;

public class GsonUtils {

    public static String getEmptySendGson(){
        SendBean sendBean = new SendBean();
        return new Gson().toJson(sendBean);
    }

    /**
     * 将 memberNo转化为SendBean json对象
     * @param memberNo
     * @return
     */
    public static String getMemberNoToGson(String memberNo){
        SendBean sendBean = new SendBean();
        sendBean.setMemberNo(MemberUtil.checkMemberNo(memberNo));
        return new Gson().toJson(sendBean);
    }

    /**
     * 将 groupNo 转化为SendBean json对象
     * @param groupNo
     * @return
     */
    public static String getGroupNoToGson(String groupNo){
        SendBean sendBean = new SendBean();
        sendBean.setGroupNo(groupNo);
        return new Gson().toJson(sendBean);
    }

    /**
     * 将 groupName 转化为SendBean json对象
     * @param groupName
     * @return
     */
    public static String getGroupNameToGson(String groupName){
        SendBean sendBean = new SendBean();
        sendBean.setGroupName(groupName);
        return new Gson().toJson(sendBean);
    }

    /**
     * 将 phoneNo 转化为SendBean json对象
     * @param phoneNo
     * @return
     */
    public static String getPhoneNoToGson(String phoneNo){
        SendBean sendBean = new SendBean();
        sendBean.setPhoneNo(phoneNo);
        return new Gson().toJson(sendBean);
    }

    /**
     * 将 memberNo 和 terminalType 转化为SendBean json对象
     * @param memberNo
     * @param terminalType
     * @return
     */
    public static String getMemberTerminalTypeToGson(String memberNo,int terminalType){
        SendBean sendBean = new SendBean();
        sendBean.setMemberNo(MemberUtil.checkMemberNo(memberNo));
        sendBean.setTerminalType(terminalType);
        return new Gson().toJson(sendBean);
    }

    /**
     * 将 memberList 转化为SendBean json对象
     * @param memberList
     * @return
     */
    public static String getMemberListToGson(List<String> memberList){
        SendBean sendBean = new SendBean();
        sendBean.setNumberList(MemberUtil.checkMemberList(memberList));
        return new Gson().toJson(sendBean);
    }

    /**
     * 将 groupList 转化为SendBean json对象
     * @param groupList
     * @return
     */
    public static String getGroupListToGson(List<String> groupList){
        SendBean sendBean = new SendBean();
        sendBean.setGroupList(groupList);
        return new Gson().toJson(sendBean);
    }


    public static String getMembersGroupsToGson(List<String> memberList,List<String> groupList){
        SendBean sendBean = new SendBean();
        sendBean.setNumberList(MemberUtil.checkMemberList(memberList));
        sendBean.setGroupList(groupList);
        return new Gson().toJson(sendBean);
    }
}

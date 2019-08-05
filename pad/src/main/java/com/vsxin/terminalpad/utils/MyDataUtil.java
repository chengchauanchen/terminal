package com.vsxin.terminalpad.utils;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.common.ReceiveObjectMode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.Member;


/**
 *
 */
public class MyDataUtil{
    public static List<Member> getMemberInList(List<Member> memberList, int pageIndex, int number) {
        List<Member> pageMember = new ArrayList<>();
        for (int i = pageIndex * number; i < memberList.size(); i++) {
            pageMember.add(memberList.get(i));
        }
        return pageMember;
    }








    /**
     * 获取上报图像需要传的参数
     *
     * @param uniqueNo
     * @param type
     * @return
     */
    public static String getPushInviteMemberData(long uniqueNo, String type) {
        if (uniqueNo > 0 && !TextUtils.isEmpty(type)) {
            return uniqueNo + "_" + type;
        }
        return "";
    }

    /**
     * 通过推送的图像的String获取组信息
     * @param pushMemberList
     * @return
     */
    public static List<Group> checkIsGroupPush(List<String> pushMemberList) {
        List<Group> list = new ArrayList<>();
        List<Group> allGroups =  TerminalFactory.getSDK().getConfigManager().getAllGroups();
        List<Group> allTempGroups =  TerminalFactory.getSDK().getConfigManager().getAllTempGroup();
        List<Group> groups  = new ArrayList<>();
        groups.addAll(allGroups);
        groups.addAll(allTempGroups);
        if (pushMemberList != null && !pushMemberList.isEmpty()) {
            for (String string: pushMemberList) {
                if(!TextUtils.isEmpty(string)&&string.contains("_")){
                    String[] split = string.split("_");
                    if(split.length>1&& ReceiveObjectMode.valueOf(split[1]).getCode() == ReceiveObjectMode.GROUP.getCode()){
                        for (Group group: groups) {
                            if(StringUtil.toLong(split[0]) == group.getNo()){
                                list.add(group);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return list;
    }
}

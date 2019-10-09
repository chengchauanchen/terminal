package cn.vsx.vc.utils;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.common.ReceiveObjectMode;
import cn.vsx.hamster.common.util.NoCodec;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.model.VideoMember;
import cn.vsx.vc.model.ContactItemBean;
import cn.vsx.vc.model.InviteMemberExceptList;
import cn.vsx.vc.model.TransponToBean;
import ptt.terminalsdk.tools.StringUtil;

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
     * 获取合并转发的ToID
     *
     * @param list
     * @return
     */
    public static List<Integer> getToIdsTranspon(ArrayList<ContactItemBean> list) {
        List<Integer> toIds = new ArrayList<>();
        for (ContactItemBean bean : list) {
            if (bean.getType() == Constants.TYPE_ACCOUNT) {
                Account account = (Account) bean.getBean();
                if (account != null) {
                    toIds.add(NoCodec.encodeMemberNo(account.getNo()));
                }
            } else if (bean.getType() == Constants.TYPE_GROUP) {
                Group group = (Group) bean.getBean();
                if (group != null) {
                    toIds.add(NoCodec.encodeGroupNo(group.getNo()));
                }
            }
        }
        return toIds;
    }

    /**
     * 获取第一个名字
     *
     * @param list
     * @return
     */
    public static TransponToBean getToNamesTranspon(ArrayList<ContactItemBean> list) {
        TransponToBean result = null;
        for (ContactItemBean bean : list) {
            if (bean.getType() == Constants.TYPE_ACCOUNT) {
                Account account = (Account) bean.getBean();
                if (account != null) {
                    result = new TransponToBean(NoCodec.encodeMemberNo(account.getNo()), account.getName());
                    break;
                }
            } else if (bean.getType() == Constants.TYPE_GROUP) {
                Group group = (Group) bean.getBean();
                if (group != null) {
                    result = new TransponToBean(NoCodec.encodeGroupNo(group.getNo()), group.getName());
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 获取转发的对方的UniqueNo
     *
     * @param list
     * @return
     */
    public static List<Long> getToUniqueNoTranspon(ArrayList<ContactItemBean> list) {
        List<Long> toUniques = new ArrayList<>();
        for (ContactItemBean bean : list) {
            if (bean.getType() == Constants.TYPE_ACCOUNT) {
                //                Account account = (Account) bean.getBean();
                //                if (account != null) {
                //                    toUniques.add(account.getUniqueNo());
                //                }
            } else if (bean.getType() == Constants.TYPE_GROUP) {
                Group group = (Group) bean.getBean();
                if (group != null) {
                    toUniques.add(group.getUniqueNo());
                }
            }
        }

        return toUniques;
    }

    /**
     * 获取已经观看的成员的
     *
     * @return
     */
    public static InviteMemberExceptList getInviteMemberExceptList(ArrayList<VideoMember> watchMembers) {
        InviteMemberExceptList bean = new InviteMemberExceptList();
        List<Integer> list = new ArrayList<>();
        if (watchMembers != null && !watchMembers.isEmpty()) {
            for (VideoMember member : watchMembers) {
                if (member != null) {
                    list.add(member.getId());
                }
            }
        }
        bean.setList(list);
        return bean;
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

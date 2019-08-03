package com.vsxin.terminalpad.utils;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Member;

/**
 * @author qzw
 *
 * 成员工具类
 */
public class MemberUtil {

    /**
     * 根据终端类型获取Member
     *
     * @param account
     * @return
     */
    public static Member getMemberForTerminalMemberType(Account account,TerminalMemberType type) {
        for (Member member : account.getMembers()) {
            if(member.type  == type.getCode()){
                return member;
            }
        }
        return null;
    }

    /**
     * 获取可以打个呼的设备信息
     *
     * @param account
     * @return
     */
    private List<Member> getCallPrivateMemberList(Account account) {
        List<Member> result = new ArrayList<>();
        for (Member member : account.getMembers()) {
            if (member.type != TerminalMemberType.TERMINAL_HDMI.getCode() &&
                    member.type != TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.getCode()) {
                result.add(member);
            }
        }
        return result;
    }
}

package com.vsxin.terminalpad.utils;

import cn.vsx.hamster.terminalsdk.tools.Util;

/**
 * Created by hasee on 2017/12/21.
 */

public class HandleIdUtil {
    public static String handleId(int memberId){
        String account = "";
        String s = memberId +"";

        if (!Util.isEmpty(s) && s.length() > 2 && "88".equals(s.substring(0, 2))) {
            account = s.substring(2);
        }else {
            account = s;
        }
        return account;
    }

    public static String handleId(String memberId){
        String account = "";
        String s = memberId;

        if (!Util.isEmpty(s) && s.length() > 2 && "88".equals(s.substring(0, 2))) {
            account = s.substring(2);
        }else {
            account = s;
        }
        return account;
    }

    public static String handleName(String memberName){
        String account = memberName;
        if (!Util.isEmpty(memberName) && memberName.length() > 2 && "88".equals(memberName.substring(0, 2))) {
            account = memberName.substring(2);
        }else {
            account = memberName;
        }
        return account;
    }
}

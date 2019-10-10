package com.vsxin.terminalpad.utils;

import android.text.TextUtils;

public class NumberUtil {
    /**
     * 将字符串转化成int
     *
     * @param numStr
     * @return
     */
    public static int strToInt(String numStr) {
        int num = 0;
        try {
            num = Integer.parseInt(numStr);
        } catch (Exception e) {
            num = 0;
        }
        return num;
    }

    /**
     * 将字符串转化为 Long
     * @param numStr
     * @return
     */
    public static Long strToLong(String numStr){
        Long num = 0L;
        try {
            num = Long.parseLong(numStr);
        }catch (Exception e){
            num = 0L;
        }
        return num;
    }

    /**
     * 如果memberNo为6位，默认加"88"
     * @param memberNo
     * @return
     */
    public static String checkMemberNo(String memberNo) {
        if (length(memberNo) <= 6) {
            memberNo = "88" + memberNo;
        } else {
            return memberNo;
        }
        return memberNo;
    }

    public static String remove88(String no){
        if(TextUtils.isEmpty(no)){
            return "";
        }
        String b=no.substring(0,2);
        if(TextUtils.equals("88",b)){
            return no.substring(2);
        }else {
            return no;
        }
    }


    private static int length(String number) {
        int length = number.length();
        return length;
    }
}

package com.vsxin.terminalpad.utils;

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


    private static int length(String number) {
        int length = number.length();
        return length;
    }
}

package cn.vsx.vsxsdk.utils;

import java.util.ArrayList;
import java.util.List;

public class MemberUtil {
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
     * 遍历memberList，如果memberNo为6位，默认加"88"
     * @param memberList
     * @return
     */
    public static List<String> checkMemberList(List<String> memberList){
        List<String> newMemberList = new ArrayList<>();
        for (String member:memberList){
            newMemberList.add(checkMemberNo(member));
        }
        return newMemberList;
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

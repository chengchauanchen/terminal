package cn.vsx.vc.jump.utils;

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
     * 如果memberNo为6位，默认加"88"
     *
     * @param memberNo
     * @return
     */
    private static int checkMemberNo(int memberNo) {
        String memberNoStr = memberNo + "";
        if (length(memberNo) <= 6) {
            memberNoStr = "88" + memberNoStr;
        } else {
            return memberNo;
        }
        return Integer.parseInt(memberNoStr);
    }


    public static int length(int number) {
        int length = (number + " ").length();
        return length;
    }
}

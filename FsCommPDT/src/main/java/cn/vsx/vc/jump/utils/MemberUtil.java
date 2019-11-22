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
    public static int checkMemberNo(String memberNo) {
        try{
            String memberNoStr = memberNo + "";
            if (memberNo.length() <= 6) {
                memberNoStr = "88" + memberNoStr;
            } else {
                return Integer.parseInt(memberNo);
            }
            return Integer.parseInt(memberNoStr);
        }catch (Exception e){
            return 0;
        }
    }


    public static int length(int number) {
        int length = (number + " ").length();
        return length;
    }
}

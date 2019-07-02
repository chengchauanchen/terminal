package cn.vsx.vc.jump.utils;

import cn.vsx.vc.activity.NewMainActivity;

public class MemberUtil {

    /**
     * 将字符串转化成int
     * @param numStr
     * @return
     */
    public static int strToInt(String numStr){
        int num = 0;
        try {
            num = Integer.parseInt(numStr);
        }catch (Exception e){
            num=0;
        }
        return num;
    }

    /**
     * 将int转化为String
     * @param num
     * @return
     */
    public static String intToStr(int num){
        String numStr = "";
        try {
            numStr = String.valueOf(num);
        }catch (Exception e){
            numStr="";
        }
        return numStr;
    }

}

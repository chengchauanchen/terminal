package cn.vsx.vc.jump.utils;

import android.util.Log;

import java.util.Map;

import cn.vsx.vc.jump.constant.ParamKey;

public class MemberUtil {

    /**
     * 获取警员号
     * @param map
     * @return
     */
    public static int parseMemberNo(Map<Object, Object> map){
        String memberNo="";
        try{
            Object memberNoObj = map.get(ParamKey.MEMBER_NO);
            if(memberNoObj!=null){
                memberNo = (String) memberNoObj;
            }
        }catch (Exception e){
            Log.e("JumpService",e.toString());
        }
        return MemberUtil.strToInt(memberNo);
    }

    /**
     * 获取组号
     * @param map
     * @return
     */
    public static int parseGroupNo(Map<Object, Object> map){
        String groupNo="";
        try{
            Object groupNoObj = map.get(ParamKey.GROUP_NO);
            if(groupNoObj!=null){
                groupNo = (String) groupNoObj;
            }
        }catch (Exception e){
            Log.e("JumpService",e.toString());
        }
        return MemberUtil.strToInt(groupNo);
    }

    /**
     * 获取设备类型号
     * @param map
     * @return
     */
    public static int parseTerminalType(Map<Object, Object> map){
        int terminalType=-1;
        try{
            Object terminalTypeObj = map.get(ParamKey.TERMINAL_TYPE);
            if(terminalTypeObj!=null){
                terminalType = (int)terminalTypeObj;
            }
        }catch (Exception e){
            Log.e("JumpService",e.toString());
        }
        return terminalType;
    }


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

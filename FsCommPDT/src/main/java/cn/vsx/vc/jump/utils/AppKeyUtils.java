package cn.vsx.vc.jump.utils;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.tools.Params;

public class AppKeyUtils {
    /**
     * 设置appKey
     * @param appKey 为空,则用系统默认的“vsx”
     */
    public static void setAppKey(String appKey){
        TerminalFactory.getSDK().putParam(Params.APP_KEY,appKey);
    }

    /**
     * 获取 第三方appKey
     * @return
     */
    public static String getAppKey(){
        return TerminalFactory.getSDK().getParam(Params.APP_KEY,"vsx");
    }

    /**
     * 是否是vsx appKey
     * @return
     */
    public static boolean isVsxAppKey(){
        return "vsx".equals(getAppKey());
    }
}

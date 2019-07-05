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
}

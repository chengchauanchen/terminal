package cn.vsx.vc.utils;

import org.apache.log4j.Logger;

/**
 * Created by Hou on 2017/6/13.
 * 手机适配的相关处理
 */

public class PhoneAdapter {
    public static Logger logger = Logger.getLogger(PhoneAdapter.class);
    // 是否是F25机型
    public static boolean isF25() {
        String machineType = android.os.Build.MODEL;
        return machineType.equals("F25");
    }

    // 是否是F32机型
    public static boolean isF32() {
        String machineType = android.os.Build.MODEL;
        logger.info("android.os.Build.MODEL ---> "+machineType);
        return machineType.equals("F32") || machineType.equals("LT132");
    }
}

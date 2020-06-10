package ptt.terminalsdk.tools;

import android.text.TextUtils;

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
        if (TextUtils.equals("F25",machineType)) {
            return true;
        }
        return false;
    }

    // 是否是F32机型
    public static boolean isF32() {
        String machineType = android.os.Build.MODEL;
        logger.info("android.os.Build.MODEL ---> "+machineType);
        if (TextUtils.equals("F32",machineType) || TextUtils.equals("LT132",machineType)) {
            return true;
        }
        return false;
    }

    // 是否是PDC760机型
    public static boolean isPDC760() {
        String machineType = android.os.Build.MODEL;
        if (TextUtils.equals("PDC760",machineType)) {
            return true;
        }
        return false;
    }


}

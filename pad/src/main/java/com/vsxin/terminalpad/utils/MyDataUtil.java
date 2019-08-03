package com.vsxin.terminalpad.utils;

import android.text.TextUtils;

/**
 *
 */
public class MyDataUtil {
    /**
     * 获取上报图像需要传的参数
     *
     * @param uniqueNo
     * @param type
     * @return
     */
    public static String getPushInviteMemberData(long uniqueNo, String type) {
        if (uniqueNo > 0 && !TextUtils.isEmpty(type)) {
            return uniqueNo + "_" + type;
        }
        return "";
    }
}

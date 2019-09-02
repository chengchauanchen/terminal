package com.vsxin.terminalpad.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

/**
 * @author qzw
 *
 * 获取资源工具类
 */
public class ResUtil {

    /**
     * 获取 Color
     * @param context
     * @param rId
     * @return
     */
    public static int getColor(Context context, int rId) {
        return ContextCompat.getColor(context, rId);
    }

    /**
     * 获取 Drawable
     * @param context
     * @param rId
     * @return
     */
    public static Drawable getDrawable(Context context, int rId) {
        return ContextCompat.getDrawable(context, rId);
    }
}

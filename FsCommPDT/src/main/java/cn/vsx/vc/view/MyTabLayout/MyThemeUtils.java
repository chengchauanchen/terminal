package cn.vsx.vc.view.MyTabLayout;

import android.content.Context;
import android.content.res.TypedArray;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/11/5
 * 描述：
 * 修订历史：
 */
public class MyThemeUtils{
    private static final int[] APPCOMPAT_CHECK_ATTRS = {
            android.support.v7.appcompat.R.attr.colorPrimary
    };

    static void checkAppCompatTheme(Context context) {
        TypedArray a = context.obtainStyledAttributes(APPCOMPAT_CHECK_ATTRS);
        final boolean failed = !a.hasValue(0);
        a.recycle();
        if (failed) {
            throw new IllegalArgumentException("You need to use a Theme.AppCompat theme "
                    + "(or descendant) with the design library.");
        }
    }
}

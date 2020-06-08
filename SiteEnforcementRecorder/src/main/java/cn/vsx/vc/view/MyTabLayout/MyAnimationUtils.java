package cn.vsx.vc.view.MyTabLayout;

import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/11/5
 * 描述：
 * 修订历史：
 */
public class MyAnimationUtils{
    static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    static final Interpolator FAST_OUT_SLOW_IN_INTERPOLATOR = new FastOutSlowInInterpolator();
    static final Interpolator FAST_OUT_LINEAR_IN_INTERPOLATOR = new FastOutLinearInInterpolator();
    static final Interpolator LINEAR_OUT_SLOW_IN_INTERPOLATOR = new LinearOutSlowInInterpolator();
    static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

    /**
     * Linear interpolation between {@code startValue} and {@code endValue} by {@code fraction}.
     */
    static float lerp(float startValue, float endValue, float fraction) {
        return startValue + (fraction * (endValue - startValue));
    }

    static int lerp(int startValue, int endValue, float fraction) {
        return startValue + Math.round(fraction * (endValue - startValue));
    }
}

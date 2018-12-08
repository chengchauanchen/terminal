package cn.vsx.vc.utils;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;

import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.receiveHandle.ReceiverKeyboardHeightHandler;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * Created by gt358 on 2017/9/29.
 */

public class KeyboarUtils {

    /***  获取软键盘高度 **/
    public static void getKeyBoardHeight (final Activity activity) {
        final View decorView = activity.getWindow().getDecorView();

        decorView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            int previousKeyboardHeight = -1;
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                decorView.getWindowVisibleDisplayFrame(rect);
                int displayHeight = rect.bottom - rect.top;
                int height = decorView.getHeight();
                int keyboardHeight = height - rect.bottom;

                if (Build.VERSION.SDK_INT >= 20) {
                    // When SDK Level >= 20 (Android L), the softInputHeight will contain the height of softButtonsBar (if has)
                    keyboardHeight = keyboardHeight - getSoftButtonsBarHeight(activity);
                }

                if (previousKeyboardHeight != keyboardHeight) {
                    boolean hide = (double) displayHeight / height > 0.8;
                    if(!hide && keyboardHeight > 0) {
                        decorView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        MyTerminalFactory.getSDK().putParam(Params.KEYBOARD_HEIGHT, keyboardHeight);
                        MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiverKeyboardHeightHandler.class, keyboardHeight);
                    }
                }

                previousKeyboardHeight = height;
            }
        });

    }

    private static int getSoftButtonsBarHeight(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int usableHeight = metrics.heightPixels;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        }
        int realHeight = metrics.heightPixels;
        if (realHeight > usableHeight) {
            return realHeight - usableHeight;
        } else {
            return 0;
        }
    }
}

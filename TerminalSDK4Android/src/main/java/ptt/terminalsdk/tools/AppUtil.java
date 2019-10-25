package ptt.terminalsdk.tools;

import android.app.Activity;
public class AppUtil {

    /**
     * 判断Activity是否关闭
     * @param activity
     * @return
     */
    public static Boolean checkActivityIsRun(Activity activity) {
        if (activity == null) {
            return false;
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return !activity.isFinishing() || !activity.isDestroyed();
        }
        return !activity.isFinishing();
    }
}

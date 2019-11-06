package ptt.terminalsdk.tools;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;

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

    public static boolean getScreenOriention(Context context){
        Configuration mConfiguration = context.getResources().getConfiguration(); //获取设置的配置信息
        int ori = mConfiguration.orientation; //获取屏幕方向
        return (ori == Configuration.ORIENTATION_LANDSCAPE);
    }
}

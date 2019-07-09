package cn.vsx.vsxsdk.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import java.util.List;

public class SystemUtil {
    /**
     * 判断指定页面是否已经打开
     * @param context
     * @param clazz
     * @return
     */
    public static boolean isLaunchedActivity(Context context, Class<?> clazz) {
        Intent intent = new Intent(context.getApplicationContext(), clazz);
        ComponentName cmpName = intent.resolveActivity(context.getPackageManager());
        boolean flag = false;
        if (cmpName != null) {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<RunningTaskInfo> taskInfoList = am.getRunningTasks(10);
            for (ActivityManager.RunningTaskInfo taskInfo : taskInfoList) {
                if (taskInfo.baseActivity.equals(cmpName)) {
                    flag = true;
                    break;
                }
            }
        }
        return flag;
    }
}

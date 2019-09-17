package cn.vsx.vsxsdk.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class SystemUtil {


    public static boolean isServiceStarted(Context context, String PackageName) {
        boolean isStarted = false;
//        try {
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//            int intGetTastCounter = 1000;
//            List<ActivityManager.RunningServiceInfo> mRunningService =
//                    mActivityManager.getRunningServices(intGetTastCounter);
//            for (ActivityManager.RunningServiceInfo amService : mRunningService) {
//                if (0 == amService.service.getPackageName().compareTo(PackageName)) {
//                    isStarted = true;
//                    break;
//                }
//            }
//        } catch (SecurityException e) {
//            e.printStackTrace();
//        }
//        return isStarted;
        try {
            List<ActivityManager.RunningAppProcessInfo> list = mActivityManager.getRunningAppProcesses();
            if (list != null) {
                for (int i = 0; i < list.size(); ++i) {
                    if (PackageName.matches(list.get(i).processName)) {
                        isStarted = true;
                        break;
                    }
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        return isStarted;
    }

    /**
     * 判断服务是否启动,context上下文对象 ，className服务的name
     */
    public static boolean isServiceRunning(Context mContext, String className) {
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (className.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断包是否安装
     *
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isAvilible(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();//获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);//获取所有已安装程序的包信息
        List<String> pName = new ArrayList<String>();//用于存储所有已安装程序的包名
        //从pinfo中将包名字逐一取出，压入pName list中
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                pName.add(pn);
            }
        }
        return pName.contains(packageName);//判断pName中是否有目标程序的包名，有TRUE，没有FALSE
    }


    public static String getPackageName(Context context) {
        return context.getApplicationInfo().packageName;
    }

    /**
     * 判断指定页面是否已经打开
     *
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

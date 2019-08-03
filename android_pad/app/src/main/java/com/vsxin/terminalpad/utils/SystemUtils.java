package com.vsxin.terminalpad.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.os.Looper;

import java.util.List;

public class SystemUtils {

    /**
     * 判断当前进程，是否是主进程运行
     * @return
     */
    public static boolean isMainProcess(Context context,String packageName){
        return packageName.equals(getProcessName(context,android.os.Process.myPid()));

    }

    /**
     * 通过pid找进程名(包名)
     * @param cxt
     * @param pid
     * @return
     */
    private static String getProcessName(Context cxt, int pid) {
        ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) {
            return null;
        }

        List<RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps != null && !runningApps.isEmpty()) {
            for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
                if (procInfo.pid == pid) {
                    return procInfo.processName;
                }
            }
        }

        return null;
    }

    /**
     * 判断当前线程是否是主线程
     * @return
     */
    public static boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }
}

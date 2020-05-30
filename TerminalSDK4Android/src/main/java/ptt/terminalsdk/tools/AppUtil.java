package ptt.terminalsdk.tools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Configuration;
import android.os.PowerManager;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * 判断某个界面是否在前台
     *
     * @param context   Context
     * @param className 界面的类名
     * @return 是否在前台显示
     */
    public static boolean isForeground(Context context, String className) {
        try{
            if (context == null || TextUtils.isEmpty(className))
                return false;
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
            if (list != null && list.size() > 0) {
                ComponentName cpn = list.get(0).topActivity;
                if (className.equals(cpn.getClassName()))
                    return true;
            }
            return false;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断服务是否开启
     *
     * @return
     */
    public static boolean isServiceRunning(Context context, String ServiceName) {
        try{
            if (TextUtils.isEmpty(ServiceName)) {
                return false;
            }
            ActivityManager myManager = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager
                    .getRunningServices(Integer.MAX_VALUE);
            for (int i = 0; i < runningService.size(); i++) {
                if (runningService.get(i).service.getClassName().toString()
                        .equals(ServiceName)) {
                    return true;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 复制内容到剪切板
     *
     * @param copyStr
     * @return
     */
    private boolean copy(Context context,String copyStr) {
        try {
            //获取剪贴板管理器
            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            // 创建普通字符型ClipData
            ClipData mClipData = ClipData.newPlainText("Label", copyStr);
            // 将ClipData内容放到系统剪贴板里。
            cm.setPrimaryClip(mClipData);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressLint("Wakelock")
    @SuppressWarnings("deprecation")
    public static void wakeUpAndUnlock(Context context) {
        try{
            // 获取电源管理器对象
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            // 获取PowerManager.WakeLock对象，后面的参数|表示同时传入两个值，最后的是调试用的Tag
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
                    | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
            // 点亮屏幕
            wl.acquire();
            // 释放
            wl.release();
            // 得到键盘锁管理器对象
            KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
            // 解锁
            kl.disableKeyguard();
//
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

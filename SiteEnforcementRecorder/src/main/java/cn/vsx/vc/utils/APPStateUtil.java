package cn.vsx.vc.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import java.util.List;
import org.apache.log4j.Logger;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * @author martian on 2018/12/9.
 */
public class APPStateUtil {
  private static Logger logger = Logger.getLogger(APPStateUtil.class);

  public static boolean isBackground(Context context) {
    ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
    for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
      if (appProcess.processName.equals(context.getPackageName())) {
        logger.info("此app =" + appProcess.importance + ",context.getClass().getName()=" + context.getClass().getName());
        if (appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
          logger.info("处于后台" + appProcess.processName);
          return true;
        } else {
          logger.info("处于前台" + appProcess.processName);
          return false;
        }
      }
    }
    return false;
  }

  /**
   * 判断指定activity是否已经在任务栈中
   * @param context
   * @param cls
   * @return
   */
  public static  boolean activityIsOpened(Context context,Class<?> cls){
    Intent intent = new Intent(context, cls);
    ComponentName cmpName = intent.resolveActivity(context.getPackageManager());
    boolean flag = false;
    if (cmpName != null) { // 说明系统中存在这个activity
      ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
      List<ActivityManager.RunningTaskInfo> taskInfoList = am.getRunningTasks(10);
      for (ActivityManager.RunningTaskInfo taskInfo : taskInfoList) {
        if (taskInfo.baseActivity.equals(cmpName)) { // 说明它已经启动了
          flag = true;
          break;  //跳出循环，优化效率
        }
      }
    }
    return flag;
  }

  /**
   * 判断本应用是否已经位于最前端
   *
   * @param context
   * @return 本应用已经位于最前端时，返回 true；否则返回 false
   */
  public static boolean isRunningForeground(Context context) {
    ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningAppProcessInfo> appProcessInfoList = activityManager.getRunningAppProcesses();
    /**枚举进程*/
    for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessInfoList) {
      if (appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
        if (appProcessInfo.processName.equals(context.getApplicationInfo().processName)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * 将本应用置顶到最前端
   * 当本应用位于后台时，则将它切换到最前端
   *
   * @param context
   */
  public static void setTopApp(Context context) {
      /**获取ActivityManager*/
      ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);

      /**获得当前运行的task(任务)*/
      List<ActivityManager.RunningTaskInfo> taskInfoList = activityManager.getRunningTasks(100);
      for (ActivityManager.RunningTaskInfo taskInfo : taskInfoList) {
        /**找到本应用的 task，并将它切换到前台*/
        if (taskInfo.topActivity.getPackageName().equals(context.getPackageName())) {
          activityManager.moveTaskToFront(taskInfo.id, 0);
          break;
        }
      }
  }

  /**
   * 判断某个服务是否正在运行的方法
   *
   * @param mContext
   * @param cls      是包名+服务的类名（例如：net.loonggg.testbackstage.TestService）
   * @return true代表正在运行，false代表服务没有正在运行
   */
  public static boolean isServiceWork(Context mContext, Class cls) {
    boolean isWork = false;
    ActivityManager myAM = (ActivityManager) mContext
        .getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(Integer.MAX_VALUE);
    String serviceName = cls.getCanonicalName();
    if (myList.size() <= 0) {
      return false;
    }
    for (int i = 0; i < myList.size(); i++) {
      String mName = myList.get(i).service.getClassName().toString();
      if (mName.equals(serviceName)) {
        isWork = true;
        break;
      }
    }
    return isWork;
  }
}


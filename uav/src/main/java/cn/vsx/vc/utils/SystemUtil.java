package cn.vsx.vc.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import java.io.File;
import java.util.List;


public class SystemUtil {

    public static float density = 1;
    public static Point displaySize = new Point();
    public static DisplayMetrics displayMetrics = new DisplayMetrics();
    public static final Handler uiHandler = new Handler(Looper.getMainLooper());
    public static int statusBarHeight = 0;

    private SystemUtil() {
    }


    public static void init(Context context) {
        density = context.getResources().getDisplayMetrics().density;
        checkDisplaySize(context);
        statusBarHeight = getStatusBarHeight();
    }

    /**
     * get root directory
     *
     * @param applicationContext
     * @return
     */
    public static File getStoreDir(Context applicationContext) {
        File dataDir = null;
        if (Environment.MEDIA_MOUNTED.equalsIgnoreCase(Environment
                .getExternalStorageState())) {
            dataDir = Environment.getExternalStorageDirectory();
        } else {
            dataDir = applicationContext.getApplicationContext().getFilesDir();
        }
        return dataDir;
    }


    public static int dp(float value) {
        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(density * value);
    }

    public static void checkDisplaySize(Context context) {
        try {
            WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (manager != null) {
                Display display = manager.getDefaultDisplay();
                if (display != null) {
                    display.getMetrics(displayMetrics);
                    display.getSize(displaySize);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (displayMetrics != null && displayMetrics.heightPixels < displayMetrics.widthPixels) {
            final int tmp = displayMetrics.heightPixels;
            displayMetrics.heightPixels = displayMetrics.widthPixels;
            displayMetrics.widthPixels = tmp;
        }
        if (displaySize != null && displaySize.y < displaySize.x) {
            final int tmp = displaySize.y;
            displaySize.y = displaySize.x;
            displaySize.x = tmp;
        }
    }

    public static String getSdkVersion() {
        try {
            return Build.VERSION.SDK;
        } catch (Exception e) {
            e.printStackTrace();
            return String.valueOf(getSdkVersionInt());
        }
    }

    public static int getSdkVersionInt() {
        try {
            return Build.VERSION.SDK_INT;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static void runOnUIThread(Runnable runnable) {
        runOnUIThread(runnable, 0);
    }

    public static void runOnUIThread(Runnable runnable, long delay) {
        if (delay == 0) {
            uiHandler.post(runnable);
        } else {
            uiHandler.postDelayed(runnable, delay);
        }
    }

    public static void cancelTask(Runnable runnable) {
        if (runnable != null) {
            uiHandler.removeCallbacks(runnable);
        }
    }

    public static int getStatusBarHeight() {
        return Resources.getSystem().getDimensionPixelSize(
                Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android"));
    }

    public static int getNavigationBarHeight(Context context) {

        boolean hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey();
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
        if (!hasMenuKey && !hasBackKey) {
            Resources resources = context.getResources();
            int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            int height = resources.getDimensionPixelSize(resourceId);
            return height;
        } else {
            return 0;
        }
    }

    /**
     * 照相机是否可用
     */
    public static boolean cameraIsCanUse() {
        boolean isCanUse = true;
        Camera mCamera = null;
        try { mCamera = Camera.open();
            Camera.Parameters mParameters = mCamera.getParameters();
            mCamera.setParameters(mParameters);
        }catch (Exception e) {
            isCanUse = false;
        } if (mCamera != null) {
            try {
                mCamera.release();
            }catch (Exception e) {
                e.printStackTrace();
                return isCanUse;
            }
        }
        return isCanUse;
    }

    /**
     * 判断activity是否在前台
     * @param context
     * @return
     */
    public static boolean  isForeground(Context context) {

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            if (context.getClass().getName().equals(cpn.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static String getVersion(Context context) {
        String version = "";
        try {
            PackageManager pManager = context.getPackageManager();
            PackageInfo info = pManager.getPackageInfo(context.getPackageName(), 0);
            version = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    /**
     * 判断指定页面是否已经打开
     * @param context
     * @param clazz
     * @return
     */
    public static boolean isLaunchedActivity(@NonNull Context context, Class<?> clazz) {
        Intent intent = new Intent(context.getApplicationContext(), clazz);
        ComponentName cmpName = intent.resolveActivity(context.getPackageManager());
        boolean flag = false;
        if (cmpName != null) {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> taskInfoList = am.getRunningTasks(10);
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

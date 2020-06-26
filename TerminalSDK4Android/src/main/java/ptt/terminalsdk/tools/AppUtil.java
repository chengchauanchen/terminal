package ptt.terminalsdk.tools;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.PowerManager;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import org.apache.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ptt.terminalsdk.context.BaseApplication;

public class AppUtil {

    protected static Logger logger = Logger.getLogger(AppUtil.class);
    public static final String TAG = "AppUtil---";
    public static StringBuffer statusBuffer = null;
    /**
     * 获取当前手机系统版本号
     *
     * @return 系统版本号
     */
    public static String getSystemVersion() {
        return Build.DISPLAY;
        //return android.os.Build.VERSION.RELEASE;

    }


    /**
     * 获取手机型号
     *
     * @return 手机型号
     */
    public static String getSystemModel() {
        return Build.MODEL;
    }

    /**
     * 获取手机厂商
     *
     * @return 手机厂商
     */
    public static String getDeviceBrand() {
        return Build.BRAND;
    }

    /**
     * 获取SN
     *
     * @return
     */
    public static String getSn(Context ctx) {
        String serial = null;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");

        } catch (Exception ignored) {

        }

        return serial;
    }


    /**
     * 系统4.0的时候
     * 获取手机IMEI 或者MEID
     *
     * @return 手机IMEI
     */
    @SuppressLint("MissingPermission")
    public static String getImeiOrMeid(Context ctx) {
        try {
            TelephonyManager manager = (TelephonyManager) ctx.getSystemService(Activity.TELEPHONY_SERVICE);
            if (manager != null) {
                return manager.getDeviceId();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 拿到imei或者meid后判断是有多少位数
     *
     * @param ctx
     * @return
     */
    public static int getNumber(Context ctx) {
        return getImeiOrMeid(ctx).trim().length();
    }


    /**
     *  5.0统一使用这个获取IMEI IMEI2 MEID
     *
     * @param ctx
     * @return
     */
    @SuppressLint("MissingPermission")
    @TargetApi(Build.VERSION_CODES.M)
    public static Map getImeiforM(Context ctx) {
        Map<String, String> map = new HashMap<String, String>();
        TelephonyManager mTelephonyManager = (TelephonyManager) ctx.getSystemService(Activity.TELEPHONY_SERVICE);
        Class<?> clazz = null;
        Method method = null;//(int slotId)
        try {
            clazz = Class.forName("android.os.SystemProperties");
            method = clazz.getMethod("get", String.class, String.class);
            String gsm = (String) method.invoke(null, "ril.gsm.imei", "");
//            String meid = (String) method.invoke(null, "ril.cdma.meid", "");
//            map.put("meid", meid);
            if (!TextUtils.isEmpty(gsm)) {
                //the value of gsm like:xxxxxx,xxxxxx
                String imeiArray[] = gsm.split(",");
                if (imeiArray != null && imeiArray.length > 0) {
                    map.put("imei1", imeiArray[0]);
                    if (imeiArray.length > 1) {
                        map.put("imei2", imeiArray[1]);
                    } else {
                        map.put("imei2", mTelephonyManager.getDeviceId(1));
                    }
                } else {
                    map.put("imei1", mTelephonyManager.getDeviceId(0));
                    map.put("imei2", mTelephonyManager.getDeviceId(1));
                }
            } else {
                map.put("imei1", mTelephonyManager.getDeviceId(0));
                map.put("imei2", mTelephonyManager.getDeviceId(1));
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    @SuppressLint("MissingPermission")
    @TargetApi(Build.VERSION_CODES.O)
    public static Map getIMEIforO(Context context) {
        Map<String, String> map = new HashMap<String, String>();
        String imei1 = "";
        String imei2 = "";
        try {
            TelephonyManager tm = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
            int count = tm.getPhoneCount();
            logger.info(TAG + "getPhoneCount:" + count);
            if (tm.getPhoneCount() > 1) {
                imei1 = tm.getImei(0);
                imei2 = tm.getImei(1);
            } else {
                imei1 = tm.getImei();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        map.put("imei1", imei1);
        map.put("imei2", imei2);
        return map;
    }


    /**
     * 获取版本号
     *
     * @param context
     * @return
     */
    public static int getVerCode(Context context) {
        int vercoe = 0;
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            vercoe = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();

        }
        return vercoe;
    }

    public static String getIMEI(Context ctx) {
        String imei = "";
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {  //5.0以下 直接获取
                logger.info(TAG + "getIMEI-getImeiOrMeid");
                imei = getImeiOrMeid(ctx);
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) { //6.0，7.0系统
                Map imeiMaps = getImeiforM(ctx);
                logger.info(TAG + "getIMEI-getImeiforM-map:" + imeiMaps);
                imei = getTransform(imeiMaps);
            } else {
                Map imeiMaps = getIMEIforO(ctx);
                logger.info(TAG + "getIMEI-getIMEIforO-map:" + imeiMaps);
                imei = getTransform(imeiMaps);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imei;
    }

    private static String getTransform(Map imeiMaps) {
        String imei = "";
        if (imeiMaps != null && !imeiMaps.isEmpty()) {
            String imei1 = (String) imeiMaps.get("imei1");
            String imei2 = (String) imeiMaps.get("imei2");
            if (TextUtils.isEmpty(imei1) && TextUtils.isEmpty(imei2)) {
                return imei;
            }
            if (!TextUtils.isEmpty(imei1) && TextUtils.isEmpty(imei2)) {
                imei = imei1;
            } else if (TextUtils.isEmpty(imei1) && !TextUtils.isEmpty(imei2)) {
                imei = imei2;
            } else {
                if (imei1.trim().length() == 15 && imei2.trim().length() == 15) {
                    imei = imei1;
                } else {
                    if (imei1.trim().length() == 15) {
                        //如果只有imei1是有效的
                        imei = imei1;
                    } else if (imei2.trim().length() == 15) {
                        //如果只有imei2是有效的
                        imei = imei2;
                    } else {
                        //如果都无效那么都为meid。只取一个就可以
                        imei = imei1;
                    }
                }
            }
        }
        return imei;
    }

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

    public static boolean getScreenOriention(Context context) {
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
        try {
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
        } catch (Exception e) {
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
        try {
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
        } catch (Exception e) {
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
    private boolean copy(Context context, String copyStr) {
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
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取电量状态
     * @return
     */
    public static int getBatteryLevel(Context context) {
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            BatteryManager batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
            return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        } else {
            Intent intent = new ContextWrapper(context.getApplicationContext()).
                    registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            return (intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100) /
                    intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        }
    }

    /**
     * 获取手机信号强度，需添加权限 android.permission.ACCESS_COARSE_LOCATION <br>
     * API要求不低于17 <br>
     *
     * @return 当前手机主卡信号强度,单位 dBm（-1是默认值，表示获取失败）
     */
    public static int getMobileDbm(Context context) {
        int dbm = -1;
        TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        List<CellInfo> cellInfoList;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
        {
            cellInfoList = tm.getAllCellInfo();
            if (null != cellInfoList) {
                for (CellInfo cellInfo : cellInfoList) {
                    if (cellInfo instanceof CellInfoGsm) {
                        CellSignalStrengthGsm cellSignalStrengthGsm = ((CellInfoGsm)cellInfo).getCellSignalStrength();
                        dbm = cellSignalStrengthGsm.getDbm();
                    } else if (cellInfo instanceof CellInfoCdma) {
                        CellSignalStrengthCdma cellSignalStrengthCdma =
                                ((CellInfoCdma)cellInfo).getCellSignalStrength();
                        dbm = cellSignalStrengthCdma.getDbm();
                    } else if (cellInfo instanceof CellInfoWcdma) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                            CellSignalStrengthWcdma cellSignalStrengthWcdma =
                                    ((CellInfoWcdma)cellInfo).getCellSignalStrength();
                            dbm = cellSignalStrengthWcdma.getDbm();
                        }
                    } else if (cellInfo instanceof CellInfoLte) {
                        CellSignalStrengthLte cellSignalStrengthLte = ((CellInfoLte)cellInfo).getCellSignalStrength();
                        dbm = cellSignalStrengthLte.getDbm();
                    }
                }
            }
        }
        return dbm;
    }

    public static int getWifiDbm(Context mContext) {
        WifiManager mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
        //获取wifi信号强度
        int wifi = mWifiInfo.getRssi();
        return wifi;
    }

    public static String getPhoneDbmLevelStr(int dbm){
        String leverlStr = "强";
        if (dbm == 0 || dbm == -1){
            leverlStr = "无";
        }else if(dbm > -44){
            leverlStr = "无";
        }else if (dbm < -44 && dbm > -97){
            leverlStr = "极强";
        }else if (dbm < -97 && dbm >= -105){
            leverlStr = "强";
        }else if (dbm < -105 && dbm >= -110){
            leverlStr = "中";
        }else if (dbm < -110 && dbm >= -120){
            leverlStr = "弱";
        }else if (dbm < -120 && dbm >= -140){
            leverlStr = "无";
        }
        return leverlStr;
    }

    /**
     * 检查wifi的信号的强度
     * 0   —— (-55)dbm  满格(4格)信号
     * (-55) —— (-70)dbm  3格信号
     * (-70) —— (-85)dbm　2格信号
     * (-85) —— (-100)dbm 1格信号
     * @param dbm
     */
    public static String getWifiLevelStr(int dbm) {
        String leverlStr = "强";
        if (dbm == -1) {
            leverlStr = "无";
        }else if(dbm<=0&&dbm>-55){
            leverlStr = "极强";
        }else if(dbm<-55&&dbm>-70){
            leverlStr = "强";
        }else if(dbm<-70&&dbm>-85){
            leverlStr = "中";
        }else if(dbm<-85&&dbm>-100) {
            leverlStr = "弱";
        }else{
            leverlStr = "无";
        }
        return leverlStr;
    }


    //获取手机或者wifi得信号强度电量强度状态
    public static String getDbmStatusStr(Context context){
        if (statusBuffer == null){
            statusBuffer = new StringBuffer();
        }
        statusBuffer.delete(0,statusBuffer.length());
        statusBuffer.append("电量:");
        statusBuffer.append(BaseApplication.getApplication().getBatteryLevel());
        statusBuffer.append("%");
        statusBuffer.append(" 信号:");
        if (NetworkUtil.isWifi(context)){
            statusBuffer.append(AppUtil.getWifiLevelStr(getWifiDbm(context)));
        }else{
            statusBuffer.append(AppUtil.getPhoneDbmLevelStr(BaseApplication.getApplication().getDbmLevel()));
        }
        return statusBuffer.toString();
    }

    //获取手机或者wifi得信号强度
    public static int getWifiOrPhoneDbmStatus(Context context){
        int dbm = -1;
        if(NetworkUtil.isWifi(context)){
            dbm = getWifiDbm(context);
        }else{
            dbm =  BaseApplication.getApplication().getDbmLevel();
        }
        return dbm;
    }

    //获取字符串得宽度
    public static int getStrWidth(String str, int textSize){
        Paint pFont = new Paint();
        pFont.setTextSize(textSize);
        return (int)pFont.measureText(str);
    }

}

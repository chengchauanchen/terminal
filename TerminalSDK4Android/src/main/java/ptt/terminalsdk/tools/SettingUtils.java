package ptt.terminalsdk.tools;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

/**
 * 作者：xuxiaolong
 * 版本：1.0
 * 创建日期：2018/8/16
 * 描述：国内手机厂商白名单跳转工具类
 */

public class SettingUtils{

    public static void enterWhiteListSetting(Context context){
        try {
            context.startActivity(getSettingIntent());
        }catch (Exception e){
            context.startActivity(new Intent(Settings.ACTION_SETTINGS));
        }
    }

    private static Intent getSettingIntent(){

        ComponentName componentName = null;

        String brand = android.os.Build.BRAND;

        switch (brand.toLowerCase()){
            case "samsung":
                componentName = new ComponentName("com.samsung.android.sm",
                        "com.samsung.android.sm.app.dashboard.SmartManagerDashBoardActivity");
                break;
            case "huawei":
                componentName = new ComponentName("com.huawei.systemmanager",
                        "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity");
                break;
            case "xiaomi":
                componentName = new ComponentName("com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity");
                break;
            case "vivo":
                componentName = new ComponentName("com.iqoo.secure",
                        "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity");
                break;
            case "oppo":
                componentName = new ComponentName("com.coloros.oppoguardelf",
                        "com.coloros.powermanager.fuelgaue.PowerUsageModelActivity");
                break;
            case "360":
                componentName = new ComponentName("com.yulong.android.coolsafe",
                        "com.yulong.android.coolsafe.ui.activity.autorun.AutoRunListActivity");
                break;
            case "meizu":
                componentName = new ComponentName("com.meizu.safe",
                        "com.meizu.safe.permission.SmartBGActivity");
                break;
            case "oneplus":
                componentName = new ComponentName("com.oneplus.security",
                        "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity");
                break;
            default:
                break;
        }

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if(componentName!=null){
            intent.setComponent(componentName);
        }else{
            intent.setAction(Settings.ACTION_SETTINGS);
        }
        return intent;
    }

    /**
     * 跳转到权限设置页面
     */
    public static Intent gotoPermissionActivity(String packageName){
//        String model = android.os.Build.MODEL; // 手机型号
//        String release = android.os.Build.VERSION.RELEASE; // android系统版本号
        String brand = Build.BRAND;//手机厂商
        if (TextUtils.equals("redmi",brand.toLowerCase()) || TextUtils.equals("xiaomi",brand.toLowerCase())) {
            return gotoMiuiPermission(packageName);//小米
        } else if (TextUtils.equals("meizu",brand.toLowerCase())) {
            return gotoMeizuPermission(packageName);
        } else if (TextUtils.equals("huawei",brand.toLowerCase()) || TextUtils.equals("honor",brand.toLowerCase())) {
            return gotoHuaweiPermission(packageName);
        } else {
            return getAppDetailSettingIntent(packageName);
        }
    }

    /**
     * 跳转到miui的权限管理页面
     */
    private static Intent gotoMiuiPermission(String packageName) {
        try { // MIUI 8
            Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
            localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
            localIntent.putExtra("extra_pkgname", packageName);
            return localIntent;
        } catch (Exception e) {
            try { // MIUI 5/6/7
                Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                localIntent.putExtra("extra_pkgname", packageName);
                return localIntent;
            } catch (Exception e1) { // 否则跳转到应用详情
                return getAppDetailSettingIntent(packageName);
            }
        }
    }

    /**
     * 跳转到魅族的权限管理系统
     */
    private static Intent gotoMeizuPermission(String packageName) {
        try {
            Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra("packageName", packageName);
            return intent;
        } catch (Exception e) {
            e.printStackTrace();
            return getAppDetailSettingIntent(packageName);
        }
    }

    /**
     * 华为的权限管理页面
     */
    private static Intent gotoHuaweiPermission(String packageName) {
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity");//华为权限管理
            intent.setComponent(comp);
            return intent;
        } catch (Exception e) {
            e.printStackTrace();
            return getAppDetailSettingIntent(packageName);
        }
    }
    /**
     * 获取应用详情页面intent（如果找不到要跳转的界面，也可以先把用户引导到系统设置页面）
     *
     * @return
     */
    private static Intent getAppDetailSettingIntent(String packageName) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", packageName, null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", packageName);
        }
        return localIntent;
    }
}

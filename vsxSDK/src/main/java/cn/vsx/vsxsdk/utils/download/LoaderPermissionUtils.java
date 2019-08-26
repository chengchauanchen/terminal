package cn.vsx.vsxsdk.utils.download;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

public class LoaderPermissionUtils {

    /**
     * 开启设置安装未知来源应用权限界面
     * @param context
     */
    public static void startInstallPermissionSettingActivity(Context context) {
        if (context == null){
            return;
        }
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
        ((Activity)context).startActivityForResult(intent,100);
    }

    //如果为8.0以上系统，则判断是否有 未知应用安装权限
    public static boolean isHasInstallPermissionWithO(Context context) {
        if (context == null) {
            return true;
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            return context.getPackageManager().canRequestPackageInstalls();
        }else{
            return true;
        }
    }
}

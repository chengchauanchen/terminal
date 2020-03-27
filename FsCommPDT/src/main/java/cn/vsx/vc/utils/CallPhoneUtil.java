package cn.vsx.vc.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;

/**
 * Created by dragon on 2017/11/6.
 */

public class CallPhoneUtil {
    public static final int PHONE_PERMISSIONS_REQUEST_CODE = 0x6666;
    public static void callPhone(Activity activity, String phoneNo) {
        try{
            if (Build.VERSION.SDK_INT >= 23) {
                //判断有没有拨打电话权限
                if (PermissionChecker.checkSelfPermission(activity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    //请求拨打电话权限
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CALL_PHONE}, 0);
                } else {
                    //直接拨号
                    Uri uri = Uri.parse("tel:" + phoneNo);
                    Intent intent = new Intent(Intent.ACTION_CALL, uri);
                    activity.startActivity(intent);
                }
            } else {
                //直接拨号
                Uri uri = Uri.parse("tel:" + phoneNo);
                Intent intent = new Intent(Intent.ACTION_CALL, uri);
                //此处不判断就会报错
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    activity.startActivity(intent);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

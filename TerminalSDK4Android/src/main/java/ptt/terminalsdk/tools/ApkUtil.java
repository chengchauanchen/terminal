package ptt.terminalsdk.tools;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/6/18
 * 描述：
 * 修订历史：
 */
public class ApkUtil{

    //是否为市局的包
    public static boolean showLteApk(){
        return MyTerminalFactory.getSDK().getParam(Params.SHOW_LTE, false);
    }

    //是否为安监
    public static boolean isAnjian(){
        return MyTerminalFactory.getSDK().getParam(Params.IS_ANJIAN, false);
    }

    /**
     * 获取应用名称
     * @param context
     * @return
     */
    public static synchronized String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

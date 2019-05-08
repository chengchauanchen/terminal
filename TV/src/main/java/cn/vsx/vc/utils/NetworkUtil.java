package cn.vsx.vc.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * 作者：徐小龙
 * 版本：1.0
 * 创建日期：2018/1/5
 * 描述：
 * 修订历史：
 */

public class NetworkUtil {

    /**
     * 判断网络是否连接
     *
     */
    public static boolean isConnected(Context context)  {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null != connectivity) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
//            if (null != info && info.isConnected()){
//                if (info.getState() == NetworkInfo.State.CONNECTED) {
//                    return true;
//                }
//            }
            if(info!=null){
                return info.isAvailable();
            }
        }
        return false;
    }

    /**
     * 判断是否是wifi连接
     */
    public static boolean isWifi(Context context){
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) return false;
        return connectivity.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI;

    }
}

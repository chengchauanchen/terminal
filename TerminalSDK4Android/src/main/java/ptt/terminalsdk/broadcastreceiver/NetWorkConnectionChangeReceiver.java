package ptt.terminalsdk.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNetworkChangeHandler;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * 监听网络状态变化
 *
 * RuanShuo
 */
public class NetWorkConnectionChangeReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {



        boolean success = false;
        //获得网络连接服务
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        //获取wifi连接状态
        NetworkInfo.State state = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        //判断是否正在使用wifi网络
        if (state == NetworkInfo.State.CONNECTED) {
            success = true;
        }
        //获取GPRS状态
        state = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        //判断是否在使用GPRS网络
        if (state == NetworkInfo.State.CONNECTED) {
            success = true;
        }
        Log.i("网络状态", "网络是否连接："+success);
        MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveNetworkChangeHandler.class,success);

    }



}

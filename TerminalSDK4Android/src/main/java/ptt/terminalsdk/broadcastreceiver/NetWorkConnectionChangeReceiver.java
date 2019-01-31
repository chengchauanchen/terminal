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



        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {

            // 得到连接管理器对象
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            final boolean success = activeNetworkInfo != null && activeNetworkInfo.isConnected();
            //获取联网状态的NetworkInfo对象
            Log.e("收到网络变化的广播", "是否连接："+success);
            MyTerminalFactory.getSDK().getThreadPool().execute(new Runnable(){
                @Override
                public void run(){
                    MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveNetworkChangeHandler.class,success);
                }
            });
        }
    }



}

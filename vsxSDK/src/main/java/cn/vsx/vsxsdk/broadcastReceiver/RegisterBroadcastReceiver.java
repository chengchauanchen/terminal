package cn.vsx.vsxsdk.broadcastReceiver;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import cn.vsx.vsxsdk.constant.ParamKey;

public class RegisterBroadcastReceiver {

    private static final String MESSAGE_ACTION = "cn.vsx.vc.conn.MESSAGE_ACTION" ;
    private static final String JUMP_ACTION = "cn.vsx.vc.conn.JUMP_ACTION" ;
    private ConnectJumpReceiver receiver;

    public RegisterBroadcastReceiver() {
        receiver = new ConnectJumpReceiver();
    }

    /**
     * 注册 message监听广播
     * @param context
     */
    public void register(Context context){
        //注册“网络变化”的广播接收器
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(JUMP_ACTION);
        context.registerReceiver(receiver, intentFilter);
    }

    /**
     * 销毁
     * @param context
     */
    public void unregisterReceiver(Context context){
        context.unregisterReceiver(receiver);
    }

    /**
     * 向融合通信app发送一个 Message启动广播，收到广播者可连接Message
     * 其intent携带PACKAGE_NAME
     * @param context
     */
    public void sendMessageActionBroadcast(Context context){
        Intent intent = new Intent();
        intent.setAction(MESSAGE_ACTION);
        intent.putExtra(ParamKey.PACKAGE_NAME,getPackageName(context));
        context.sendBroadcast(intent);//发送标准广播
        Log.e("广播","发送标准广播__MESSAGE_ACTION");
    }

    private String getPackageName(Context context){
        return context.getApplicationInfo().packageName;
    }
}

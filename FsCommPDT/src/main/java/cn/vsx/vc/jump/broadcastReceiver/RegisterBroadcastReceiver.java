package cn.vsx.vc.jump.broadcastReceiver;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import org.apache.log4j.Logger;

public class RegisterBroadcastReceiver {

    protected static Logger logger = Logger.getLogger(RegisterBroadcastReceiver.class);
    private static final String MESSAGE_ACTION = "cn.vsx.vc.conn.MESSAGE_ACTION" ;
    private static final String JUMP_ACTION = "cn.vsx.vc.conn.JUMP_ACTION" ;
    private ConnectMessageReceiver receiver;

    public RegisterBroadcastReceiver() {
        receiver = new ConnectMessageReceiver();
    }

    /**
     * 注册 message监听广播
     * @param context
     */
    public void register(Context context){
        //注册“网络变化”的广播接收器
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MESSAGE_ACTION);
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
     * 向第三方应用发送一个 jump启动广播，收到广播者可连接jump
     * //发送标准广播
     * @param context
     */
    public void sendBroadcast(Context context){
        Intent intent = new Intent();
        intent.setAction(JUMP_ACTION);
        context.sendBroadcast(intent);
        logger.info("--vsxSDK--"+"发送标准广播__JUMP_ACTION");
    }
}

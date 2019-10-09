package cn.vsx.vc.jump.broadcastReceiver;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import org.apache.log4j.Logger;

import cn.vsx.vc.jump.sendMessage.Connect3rdParty;
import cn.vsx.vc.jump.sendMessage.ThirdSendMessage;

public class RegisterBroadcastReceiver {

    protected static Logger logger = Logger.getLogger(RegisterBroadcastReceiver.class);
    private static final String MESSAGE_ACTION = "cn.vsx.vc.conn.MESSAGE_ACTION" ;
    private static final String JUMP_ACTION = "cn.vsx.vc.conn.JUMP_ACTION" ;
    private static final String START_APP_RECEIVER = "cn.vsx.vc.START_APP_RECEIVER" ;
    private static final String THIRD_PACKAGE_NAME = "com.gcstorage.superpolice" ;
    private static final String CONNECT_JUMP_RECEIVER = "cn.vsx.vsxsdk.broadcastReceiver.ConnectJumpReceiver" ;
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

    public void kill(Context context){
        unregisterReceiver(context);
        Connect3rdParty.getConns().clear();
    }
    /**
     * 销毁
     * @param context
     */
    public void unregisterReceiver(Context context){
        if(receiver!=null){
            logger.error("--vsx--ConnectMessageReceiver 被销毁了");
            context.unregisterReceiver(receiver);
        }
    }

    /**
     * 向第三方应用发送一个 jump启动广播，收到广播者可连接jump
     * //发送标准广播
     * @param context
     */
    public void sendBroadcast(Context context){
        Intent intent = new Intent();
        intent.setAction(JUMP_ACTION);
        //在intent里面加   component（“广播接收者的包名”，“广播接收者路径”）
        intent.setComponent(new ComponentName(THIRD_PACKAGE_NAME,CONNECT_JUMP_RECEIVER));
        context.sendBroadcast(intent);
        logger.info("--vsxSDK--"+"发送标准广播__JUMP_ACTION");
    }
}

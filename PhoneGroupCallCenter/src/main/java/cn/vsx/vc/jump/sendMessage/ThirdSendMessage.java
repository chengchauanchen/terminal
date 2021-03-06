package cn.vsx.vc.jump.sendMessage;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.IBinder;
import android.util.Log;

import org.apache.log4j.Logger;

import cn.vsx.vc.jump.broadcastReceiver.RegisterBroadcastReceiver;
import cn.vsx.vc.jump.constant.ParamKey;
import cn.vsx.vc.jump.service.JumpService;
import cn.vsx.vsxsdk.IReceivedVSXMessage;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * 给第三方app传递消息
 */
public class ThirdSendMessage {

    protected static Logger logger = Logger.getLogger(ThirdSendMessage.class);

    private static ThirdSendMessage sendMessage;
    private Context mContext;
    private final RegisterBroadcastReceiver registerBroadcastReceiver;

    public static void initVsxSendMessage(Context context) {
        if (sendMessage == null) {
            sendMessage = new ThirdSendMessage(context);
        }
    }

    public static ThirdSendMessage getInstance() {
        if(sendMessage==null){
            logger.error("--vsx--第三方接收消息服务异常---未初始化 ThirdSendMessage");
            //throw new RuntimeException("第三方接收消息服务未初始化");
            //sendMessage = new ThirdSendMessage(MyApplication.instance);
        }
        return sendMessage;
    }

    private ThirdSendMessage(Context context) {
        mContext = context;
        startJumpService(context);
        registReceiveHandler();
        registerBroadcastReceiver = new RegisterBroadcastReceiver();
    }

    /**
     * 获取广播类
     * @return
     */
    public RegisterBroadcastReceiver getRegisterBroadcastReceiver() {
        if(registerBroadcastReceiver==null){
            throw new RuntimeException("第三方接收消息服务未初始化");
        }
        return registerBroadcastReceiver;
    }


    private void registReceiveHandler(){
        RegistReceiveHandler.getInstance().registReceiveHandler();
    }

    private void unregistReceiveHandler(){
        RegistReceiveHandler.getInstance().unregistReceiveHandler();
    }

    /**
     * 界面跳转JumpService
     * @param context
     */
    private void startJumpService(Context context){
        logger.info("--vsxSDK--"+"startJumpService");
//        if(VERSION.SDK_INT>= VERSION_CODES.O){//SDK>8.0
//            context.startForegroundService(new Intent(context, JumpService.class));
//        }else{
//            context.startService(new Intent(context, JumpService.class));
//        }
        if(VERSION.SDK_INT>= VERSION_CODES.O){//SDK>8.0
            context.startForegroundService(new Intent(context, JumpService.class));
        }else{
            context.startService(new Intent(context, JumpService.class));
        }


    }

    /**
     * 发送消息到第三方应用
     * @param messageJson
     */
    public void sendMessageToThird(String messageJson,ThirdMessageType messageType){
        try{
            Connect3rdParty.getInstance().receivedMessage(messageJson,messageType.getCode());
        }catch (Exception e){
            Log.e("ThirdSendMessage",e.toString());
        }
    }

    /**
     * 连接第三方应用接收消息的服务
     * @param context
     */
    public void connectReceivedService(Context context,String packageName,boolean isNotice) {

        //判断我们的应用是否启动
        ServiceConnection conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                //连接成功后，将之前的注册全部干掉，重新注册
                IReceivedVSXMessage iReceivedVSXMessage = IReceivedVSXMessage.Stub.asInterface(service);
                //连接成功 将连接对象放入连接池中
                logger.info("--vsxSDK--"+"connectReceivedService-连接成功");
                Connect3rdParty.getInstance().addConnect(packageName,iReceivedVSXMessage);
                if(isNotice){//是否通知第三方连接 JumpService
                    try {
                        iReceivedVSXMessage.noticeConnectJumpService();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else{//做他之前没有做完的指令
                    try {
//                        Connect3rdParty.getInstance().getConnectForPackageName(packageName).continueBeforeActions();
                        iReceivedVSXMessage.continueBeforeActions();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                unregistReceiveHandler();
            }
        };

        Intent intent = new Intent();
        intent.setAction(ParamKey.THIRD_APP_SEND_MESSAGE_SERVICE_PACKAGE_NAME);//
        intent.setPackage(packageName);//注册服务包名(第三方应用包名)
        context.getApplicationContext().bindService(intent, conn, BIND_AUTO_CREATE);
    }
}

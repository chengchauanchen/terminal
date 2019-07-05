package cn.vsx.vc.jump.sendMessage;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import cn.vsx.vc.jump.constant.ParamKey;
import cn.vsx.vc.jump.service.JumpService;
import cn.vsx.vsxsdk.IReceivedVSXMessage;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * 给第三方app传递消息
 */
public class ThirdSendMessage {

    private IReceivedVSXMessage iReceivedVSXMessage;
    private static ThirdSendMessage sendMessage;
    private Context mContext;

    public static void initVsxSendMessage(Context context) {
        if (sendMessage == null) {
            sendMessage = new ThirdSendMessage(context);
        }
    }

    public static ThirdSendMessage getInstance() {
        if(sendMessage==null){
            throw new RuntimeException("第三方接收消息服务未初始化");
        }
        return sendMessage;
    }

    private ThirdSendMessage(Context context) {
        mContext = context;
        startJumpService(context);
        connectReceivedService(context);
    }

    /**
     * 界面跳转JumpService
     * @param context
     */
    private void startJumpService(Context context){
        context.startService(new Intent(context, JumpService.class));
    }

    protected IReceivedVSXMessage getiReceivedVSXMessage(){
        if(iReceivedVSXMessage!=null){
            return  iReceivedVSXMessage;
        }else{
            Toast.makeText(mContext, "请打开融合通信", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    /**
     * 发送消息到第三方应用
     * @param messageJson
     */
    protected void sendMessageToThird(String messageJson,ThirdMessageType messageType){
        try{
            getiReceivedVSXMessage().receivedMessage(messageJson,messageType.getCode());
        }catch (Exception e){
            Log.e("ThirdSendMessage",e.toString());
        }
    }

    /**
     * 连接第三方应用接收消息的服务
     * @param context
     */
    public void connectReceivedService(Context context) {
        //连接成功 就不在连接了(手动将另一线程干掉，再次启动会出问题)
        //非静态不用判断重复连接了，可以多次连接
        ////先判空，避免循环连接
        if(iReceivedVSXMessage!=null){
            return;
        }

        //判断我们的应用是否启动
        ServiceConnection conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                iReceivedVSXMessage = IReceivedVSXMessage.Stub.asInterface(service);
                noticeConnectJumpService();
                RegistReceiveHandler.getInstance().registReceiveHandler();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                RegistReceiveHandler.getInstance().unregistReceiveHandler();
            }
        };

        Intent intent = new Intent();
        intent.setAction(ParamKey.THIRD_APP_SEND_MESSAGE_SERVICE_PACKAGE_NAME);//
        intent.setPackage(ParamKey.THIRD_APP_PACKAGE_NAME);//注册服务包名(第三方应用包名)
        context.bindService(intent, conn, BIND_AUTO_CREATE);
    }

    /**
     * 我 -> 通知第三方app -> 连接我的JumpService
     */
    private void noticeConnectJumpService(){
        try{
            getiReceivedVSXMessage().noticeConnectJumpService();
        }catch (Exception e){
            System.out.println(e);
        }
    }

}

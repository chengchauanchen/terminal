package cn.vsx.vsxsdk;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import cn.vsx.vc.IJump;
import cn.vsx.vc.IJump.Stub;
import cn.vsx.vsxsdk.Interf.IReceivedMessage;
import cn.vsx.vsxsdk.Interf.JumpInterface;
import cn.vsx.vsxsdk.constant.ParamKey;
import cn.vsx.vsxsdk.message.RegistMessageListener;
import cn.vsx.vsxsdk.service.VsxReceivedService;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/7/1
 * 描述：
 * 修订历史：
 */
public class VsxSDK {

    private static String APP_KEY = "";
    private  IJump iJump;
    private  JumpInterface jumpSDK;
    private  static VsxSDK vsxSDK;
    private static RegistMessageListener registMessageListener;
    private Context mContext;

    public static void initVsxSDK(Context context) {
        if (vsxSDK == null) {
            String appKey = getAppKey(context);
            String packageName = context.getApplicationInfo().packageName;
            Log.e("VsxSDK 包名",packageName);
            vsxSDK = new VsxSDK(context,appKey );
        }
    }


    public static VsxSDK getInstance() {
        if(vsxSDK==null){
            throw new RuntimeException("VsxSDK未初始化");
        }
        return vsxSDK;
    }

    public static String getAppKey(Context context){
        try{
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            return appInfo.metaData.getString(ParamKey.APP_KEY_META_DATA);
        }catch(PackageManager.NameNotFoundException e){
            e.printStackTrace();
            return "";
        }
    }


    private VsxSDK(Context context, String appKey) {
        mContext = context;
        setAppKey(appKey);
        //启动接收消息服务ReceivedService
        startReceivedService(context);
        //连接JumpService
        connectJumpService(context);
    }

    private void setAppKey(String appKey) {
        APP_KEY = appKey;
    }

    public String getAppKey() {
        return APP_KEY;
    }

    public JumpInterface getJumpSDK() {
        if (jumpSDK == null) {
            jumpSDK = new JumpSDK();
        }
        return jumpSDK;
    }

    public RegistMessageListener getRegistMessageListener() {
        if (registMessageListener == null) {
            registMessageListener = new RegistMessageListener();
        }
        return registMessageListener;
    }

    /**
     * 1.先启动自己的服务
     * 2.再试图连接对方的服务
     * 3.连接成功后通知对方，连接我。
     * @param context
     */
    public void connectJumpService(Context context) {
        //避免重复连接  非静态不用判断重复连接了，可以多次连接
        //先判空，避免循环连接
        if(iJump!=null){
            return;
        }

        //判断我们的应用是否启动
        ServiceConnection conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                iJump = Stub.asInterface(service);
                //跳转服务连接成功后，通知 融合通信 连接第三方应用的消息接收服务
                noticeConnectReceivedService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };

        Intent intent = new Intent();
        intent.setAction("cn.vsx.vc.jump.service.JumpService");
        intent.setPackage("cn.vsx.vc");
        context.bindService(intent, conn, BIND_AUTO_CREATE);
    }

    /**
     * 跳转服务连接成功后，通知 融合通信 连接第三方应用的消息接收服务
     */
    private void noticeConnectReceivedService(){
        try{
            getIJump().noticeConnectReceivedService();
        }catch (Exception e){
            System.out.println(e);
        }
    }

    protected IJump getIJump(){
        if(iJump!=null){
            return  iJump;
        }else{
            Toast.makeText(mContext, "请打开融合通信", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    /**
     * 启动接收消息服务ReceivedService
     */
    private void startReceivedService(Context context){
        context.startService(new Intent(context, VsxReceivedService.class));
    }
}

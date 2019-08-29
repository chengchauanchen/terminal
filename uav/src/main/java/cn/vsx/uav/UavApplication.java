package cn.vsx.uav;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import cn.vsx.SpecificSDK.SpecificSDK;
import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.uav.service.PushService;
import cn.vsx.uav.service.UavReceiveHandlerService;
import cn.vsx.vc.application.MyApplication;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/6/17
 * 描述：
 * 修订历史：
 */
public class UavApplication extends MyApplication{

    private PushService pushService;
    private static UavApplication app;


    @Override
    public void onCreate(){
        super.onCreate();
        app = this;
    }

    @Override
    protected void initSdk(){
        SpecificSDK.init(this,TerminalMemberType.TERMINAL_UAV.toString());
    }

    @Override
    protected void attachBaseContext(Context base){
        super.attachBaseContext(base);
        com.secneo.sdk.Helper.install(this);
    }

    @Override
    public void setTerminalMemberType(){
        SpecificSDK.setTerminalMemberType(TerminalMemberType.TERMINAL_UAV.toString());
    }

    @Override
    public void startHandlerService(){
        Intent intent = new Intent(this, UavReceiveHandlerService.class);
        isBinded=bindService(intent,serviceConnection,BIND_AUTO_CREATE);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e("UavApplication", "UavReceiveHandlerService服务断开了");
        }
    };

    @Override
    public void stopHandlerService(){
        if (serviceConnection != null) {
            Log.i("服务状态1：",""+serviceConnection);
            Log.i("服务状态2：",""+isBinded);
            if (isBinded) {
                unbindService(serviceConnection);
                isBinded=false;
            }
            stopService(new Intent(this, UavReceiveHandlerService.class));
        }

        if(pushServiceConnection != null){
            if(pushService !=null){
                unbindService(pushServiceConnection);
            }
            stopService(new Intent(this, PushService.class));
        }
    }

    public static UavApplication getApplication(){
        return app;
    }

    public PushService getPushService(){
        return pushService;
    }

    private ServiceConnection pushServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e("UavApplication", "PushService----onServiceConnected");
            pushService = ((PushService.PushServiceBinder) service).getService();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            pushService = null;
        }
    };

    public void startPushService(){
        Intent intent = new Intent(this, PushService.class);
        isBinded=bindService(intent,pushServiceConnection,BIND_AUTO_CREATE);
    }
}

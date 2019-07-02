package cn.vsx.vsxsdk;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import cn.vsx.vc.IJump;
import cn.vsx.vc.IJump.Stub;

import static android.content.Context.BIND_AUTO_CREATE;

public class JumpSDK {
    private static IJump iJump;

    public static void initServiceA(Context context) {

        //判断我们的应用是否启动

        ServiceConnection conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                iJump = Stub.asInterface(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };

        Intent intent = new Intent();
        intent.setAction("cn.vsx.vc.jump.service.JumpService");
        intent.setPackage("cn.vsx.vc");
        context.bindService(intent, conn,BIND_AUTO_CREATE);
    }

    private static IJump getIJump(){
        if(iJump!=null){
            return  iJump;
        }else{
            return null;
        }
    }

    public static void sendMessage()  {
        try{
            getIJump().activeIndividualCall();
        }catch (Exception e){

        }
    }

}

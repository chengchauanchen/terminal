package cn.vsx.vsxsdk;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.widget.Toast;

import cn.vsx.vc.IJump;
import cn.vsx.vc.IJump.Stub;
import cn.vsx.vsxsdk.Interf.JumpInterface;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/7/1
 * 描述：
 * 修订历史：
 */
public class VsxSDK{

    private static IJump iJump;
    private static JumpInterface jumpSDK;
    private static Context mContext;

    public static JumpInterface getJumpSDK(){
        if(jumpSDK==null){
            jumpSDK = new JumpSDK();
        }
        return jumpSDK;
    }

    public static void initServiceA(Context context) {

        //判断我们的应用是否启动
        mContext = context;
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

    protected static IJump getIJump(){
        if(iJump!=null){
            return  iJump;
        }else{
            Toast.makeText(mContext, "请打开融合通信", Toast.LENGTH_SHORT).show();
            return null;
        }
    }
}

package cn.vsx.vsxsdk;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
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
public class VsxSDK {

    private static String APP_KEY = "";
    private static IJump iJump;
    private static JumpInterface jumpSDK;
    private static VsxSDK vsxSDK;
    private Context mContext;

    public static void initVsxSDK(Context context) {
        if (vsxSDK == null) {
            String appKey = getAppKey(context);
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
            return appInfo.metaData.getString("cn.vsx.sdk.API_KEY");
        }catch(PackageManager.NameNotFoundException e){
            e.printStackTrace();
            return "";
        }
    }

    private VsxSDK(Context context, String appKey) {
        mContext = context;
        setAppKey(appKey);
        initServiceA(context);
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

    public void initServiceA(Context context) {

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
        context.bindService(intent, conn, BIND_AUTO_CREATE);
    }

    protected IJump getIJump(){
        if(iJump!=null){
            return  iJump;
        }else{
            Toast.makeText(mContext, "请打开融合通信", Toast.LENGTH_SHORT).show();
            return null;
        }
    }
}

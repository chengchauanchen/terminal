package cn.vsx.vc.app;

import android.app.Application;

import cn.vsx.vsxsdk.JumpSDK;
import cn.vsx.vsxsdk.VsxSDK;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        VsxSDK.initVsxSDK(this,"123456789");
    }
}

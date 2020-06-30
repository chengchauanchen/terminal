package cn.vsx.vc.app;

import android.app.Application;


public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //需要在Manifest的meta-data中加上cn.vsx.sdk.API_KEY，值为lzy
        //VsxSDK.initVsxSDK(this);
    }
}

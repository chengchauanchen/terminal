package cn.vsx.vc.application;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import ptt.terminalsdk.receiveHandler.ReceiverAppFrontAndBackStatusHandler;

/**
 *  author : RuanShuo
 *
 *  所有Activity生命周期回调
 */

public class SimpleActivityLifecycle implements Application.ActivityLifecycleCallbacks {

    private boolean isForeground=false;
    //打开的Activity数量统计
    private int activityStartCount = 0;

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        MyApplication.instance.currentActivity = activity;
        isForeground=true;
        activityStartCount++;
        //数值从0变到1说明是从后台切到前台
        if (activityStartCount == 1) {
            //从后台切到前台
            TerminalFactory.getSDK().notifyReceiveHandler(ReceiverAppFrontAndBackStatusHandler.class,true);
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        isForeground=false;
        activityStartCount--;
        //数值从1到0说明是从前台切到后台
        if (activityStartCount == 0) {
            //从前台切到后台
            TerminalFactory.getSDK().notifyReceiveHandler(ReceiverAppFrontAndBackStatusHandler.class,false);
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    public boolean isForeground(){
        return isForeground;
    }
}

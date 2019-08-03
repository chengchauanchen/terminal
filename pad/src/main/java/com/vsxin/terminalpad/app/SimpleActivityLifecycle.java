package com.vsxin.terminalpad.app;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 *  author : RuanShuo
 *
 *  所有Activity生命周期回调
 */

public class SimpleActivityLifecycle implements Application.ActivityLifecycleCallbacks {

    private boolean isForeground=false;

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        isForeground=true;
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

package com.xuchongyang.easyphone.linphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


/**
 * Created by Mark Xu on 17/3/13.
 * KeepAliveHandler
 */

public class KeepAliveHandler extends BroadcastReceiver {
    private static final String TAG = "KeepAliveHandler";
    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println(TAG+"电话来了");
        if (LinphoneManager.getLcIfManagerNotDestroyOrNull() != null) {
            LinphoneManager.getLc().refreshRegisters();
//            SPUtils.save(context, "keepAlive", true);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                System.out.println(TAG+"Cannot sleep for 2s");
            }
        }
    }
}

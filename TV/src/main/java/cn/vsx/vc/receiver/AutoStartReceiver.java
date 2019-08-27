package cn.vsx.vc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.apache.log4j.Logger;

/**
 * 开启广播监听
 * Created by gt358 on 2017/8/10.
 */
public class AutoStartReceiver extends BroadcastReceiver {
    private Logger logger = Logger.getLogger(getClass());
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Intent newIntent = context.getPackageManager()
                    .getLaunchIntentForPackage("cn.zectec.ptt");
            context.startActivity(newIntent);
        }
    }
}

package ptt.terminalsdk.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptt.terminalsdk.service.LivingStopTimeService;

public class LivingStopTimeReceiver extends BroadcastReceiver {
    private Logger logger = LoggerFactory.getLogger(LivingStopTimeReceiver.class);
    public static final String TAG = "LivingStopTime---";
    @Override
    public void onReceive(Context context, Intent intent) {
        logger.info(TAG+"LivingStopTimeReceiver：收到上报图像到最大时间的广播");
        context.startService(new Intent(context, LivingStopTimeService.class));
    }
}

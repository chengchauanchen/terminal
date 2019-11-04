package ptt.terminalsdk.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyWarnLivingTimeoutMessageHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;

public class LivingStopTimeService extends Service {
    private Logger logger = LoggerFactory.getLogger(LivingStopTimeService.class);
    public static final String TAG = "LivingStopTime---";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logger.info(TAG + "LivingStopTimeService：收到上报图像到最大时间的广播");
        long intervalTime = TerminalFactory.getSDK().getParam(Params.MAX_LIVING_TIME_TEMPT, 0L);
        TerminalFactory.getSDK().notifyReceiveHandler(ReceiveNotifyWarnLivingTimeoutMessageHandler.class, intervalTime, Params.LIVING_STOP_TIME_DELAY_TIME_SECONDS);
        return super.onStartCommand(intent, flags, startId);
    }
}

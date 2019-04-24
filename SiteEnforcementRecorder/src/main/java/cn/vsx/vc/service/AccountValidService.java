package cn.vsx.vc.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.vc.receiveHandle.ReceiverClearAccountHandler;
import cn.vsx.vc.receiveHandle.ReceiverStartAuthHandler;
import ptt.terminalsdk.context.MyTerminalFactory;

public class AccountValidService extends Service {
    private Logger logger = LoggerFactory.getLogger(AccountValidService.class);
    public static final String TAG = "AccountValidService---";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logger.info(TAG + "AccountValidService：处理账号过期");
        TerminalFactory.getSDK().notifyReceiveHandler(ReceiverClearAccountHandler.class,false);
        return START_STICKY;
    }
}

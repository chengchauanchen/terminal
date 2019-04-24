package cn.vsx.vc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.vsx.vc.service.AccountValidService;
import ptt.terminalsdk.service.FileExpireService;

public class AccountValidReceiver extends BroadcastReceiver {
    private Logger logger = LoggerFactory.getLogger(AccountValidReceiver.class);
    public static final String TAG = "AccountValidReceiver---";
    @Override
    public void onReceive(Context context, Intent intent) {
        logger.info(TAG+"AccountValidReceiver：收到账号过期的广播");
        context.startService(new Intent(context, AccountValidService.class));
    }
}

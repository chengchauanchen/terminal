package ptt.terminalsdk.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.apache.log4j.Logger;


/**
 *  通知栏点击事件
 */
public class KeepLiveNotificationClickReceiver extends BroadcastReceiver {
    protected Logger logger = Logger.getLogger(this.getClass());
    @Override
    public void onReceive(Context context, Intent intent) {
        logger.info("--KeepLiveNotificationClickReceiver--");
//        TerminalFactory.getSDK().notifyReceiveHandler(ReceiveCloseVideoMeetingMinimizeHandler.class);
    }
}

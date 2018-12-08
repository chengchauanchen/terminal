package ptt.terminalsdk.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveServerConnectionEstablishedHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;

/**
 * Created by zckj on 2017/4/5.
 */

public class ShutdownBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "ShutdownBroadcastReceiver";

    private static final String ACTION_SHUTDOWN = "android.intent.action.ACTION_SHUTDOWN";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_SHUTDOWN)) {
            TerminalFactory.getSDK().putParam(Params.NET_OFFLINE, false);
            TerminalFactory.getSDK().notifyReceiveHandler(ReceiveServerConnectionEstablishedHandler.class, false);
        }
    }
}

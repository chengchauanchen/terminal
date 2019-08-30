package cn.vsx.vc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.apache.log4j.Logger;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.vc.receiveHandle.ReceiverBootDownHandler;

/**
 * 开机的广播接收
 */
public class BootDownReceiver extends BroadcastReceiver {
	private Logger logger = Logger.getLogger(getClass());
	private static final String ACTION_SHUTDOWN = "android.intent.action.ACTION_SHUTDOWN";
	@Override
	public void onReceive(Context context, Intent intent) {
		String intentAction = intent.getAction();
		if (ACTION_SHUTDOWN.equals(intentAction)) {
			//关机
			logger.info("BootDownReceiver = " + intentAction);
			TerminalFactory.getSDK().notifyReceiveHandler(ReceiverBootDownHandler.class);
		}
	}

}

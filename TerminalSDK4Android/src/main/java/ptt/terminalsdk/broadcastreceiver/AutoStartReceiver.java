package ptt.terminalsdk.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.apache.log4j.Logger;

import ptt.terminalsdk.context.OnlineService;

public class AutoStartReceiver extends BroadcastReceiver {

	private Logger logger = Logger.getLogger(getClass());
	@Override
	public void onReceive(Context context, Intent intent) {
		logger.debug("AutoStartReceiver启动了OnlineService");
		Intent startSrv = new Intent(context, OnlineService.class);
		startSrv.putExtra("CMD", "TICK");
		context.startService(startSrv);
	}

}

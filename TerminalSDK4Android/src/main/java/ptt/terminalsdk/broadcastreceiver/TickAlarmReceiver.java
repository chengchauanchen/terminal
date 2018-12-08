package ptt.terminalsdk.broadcastreceiver;

import org.apache.log4j.Logger;

import ptt.terminalsdk.context.OnlineService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class TickAlarmReceiver extends BroadcastReceiver {

	private Logger logger = Logger.getLogger(getClass());

	@Override
	public void onReceive(Context context, Intent intent) {
		logger.info("TickAlarmReceiver启动了OnlineService");
		Intent startSrv = new Intent(context, OnlineService.class);
		startSrv.putExtra("CMD", "TICK");
		context.startService(startSrv);
	}

}

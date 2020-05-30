package ptt.terminalsdk.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import org.apache.log4j.Logger;

import ptt.terminalsdk.context.OnlineService;


public class TickAlarmReceiver extends BroadcastReceiver {

	private Logger logger = Logger.getLogger(getClass());

	@Override
	public void onReceive(Context context, Intent intent) {
		logger.info("TickAlarmReceiver启动了OnlineService");
		try{
			Intent startSrv = new Intent(context, OnlineService.class);
			startSrv.putExtra("CMD", "TICK");
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				context.startForegroundService(startSrv);
			} else {
				context.startService(startSrv);
			}
		}catch (Exception e){
			e.printStackTrace();
		}

	}

}

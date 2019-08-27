package ptt.terminalsdk.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.support.annotation.RequiresApi;

import org.apache.log4j.Logger;

import ptt.terminalsdk.context.OnlineService;

public class AutoStartReceiver extends BroadcastReceiver {

	private Logger logger = Logger.getLogger(getClass());
	@RequiresApi(api = VERSION_CODES.O)
	@Override
	public void onReceive(Context context, Intent intent) {
		logger.debug("--vsxSDK--AutoStartReceiver启动了OnlineService");
		Intent startSrv = new Intent(context, OnlineService.class);
		startSrv.putExtra("CMD", "TICK");

		if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) {//大于7.0使用此方法
			context.startForegroundService(startSrv);
		} else {//小于7.0就简单了
			context.startService(startSrv);
		}
	}

}

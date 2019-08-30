package cn.vsx.vc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.RequiresApi;

import org.apache.log4j.Logger;

import cn.vsx.vc.service.AuthService;
import ptt.terminalsdk.context.OnlineService;

public class AuthReceiver extends BroadcastReceiver {

	private Logger logger = Logger.getLogger(getClass());
	@RequiresApi(api = VERSION_CODES.O)
	@Override
	public void onReceive(Context context, Intent intent) {
		logger.debug("--vsxSDK--AuthReceiver启动了AuthService");
		Intent startSrv = new Intent(context, AuthService.class);
		if(VERSION.SDK_INT>= VERSION_CODES.O){//SDK>8.0
			context.startForegroundService(startSrv);
		}else{
			context.startService(startSrv);
		}
	}
}

package ptt.terminalsdk.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.RequiresApi;
import android.util.Log;

import org.apache.log4j.Logger;

import ptt.terminalsdk.context.OnlineService;

public class AutoStartReceiver extends BroadcastReceiver {

	private Logger logger = Logger.getLogger(getClass());
	@RequiresApi(api = VERSION_CODES.O)
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e("AutoStartReceiver","--vsxSDK--AutoStartReceiver启动了OnlineService");
//		try{
//			Class clazz = Class.forName("cn.vsx.vc.service.ReceiveHandlerService");
//			Intent startSrv = new Intent(context,clazz);
//			startSrv.putExtra("CMD", "AUTH");
//			//		startSrv.putExtra("CMD", "TICK");
//			if(VERSION.SDK_INT>= VERSION_CODES.O){//SDK>8.0
//				context.startForegroundService(startSrv);
//			}else{
//				context.startService(startSrv);
//			}
//		}catch(ClassNotFoundException e){
//			logger.info(e);
//			e.printStackTrace();
//		}

		Intent startSrv = new Intent(context, OnlineService.class);
		startSrv.putExtra("CMD", "AUTH");
//		startSrv.putExtra("CMD", "TICK");
		if(VERSION.SDK_INT>= VERSION_CODES.O){//SDK>8.0
			context.startForegroundService(startSrv);
		}else{
			context.startService(startSrv);
		}
	}

}

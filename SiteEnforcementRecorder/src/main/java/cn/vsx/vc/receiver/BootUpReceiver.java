package cn.vsx.vc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.apache.log4j.Logger;

import cn.vsx.vc.activity.RegistNFCActivity;

/**
 * 开机的广播接收
 */
public class BootUpReceiver extends BroadcastReceiver {
	private Logger logger = Logger.getLogger(getClass());
	private static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
	@Override
	public void onReceive(Context context, Intent intent) {
		String intentAction = intent.getAction();
		if (ACTION_BOOT_COMPLETED.equals(intentAction)) {
			//开机自启
			logger.info("BootUpReceiver = " + intentAction);
			Intent jumpIntent = new Intent(context, RegistNFCActivity.class);
			jumpIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(jumpIntent);
		}
	}

}

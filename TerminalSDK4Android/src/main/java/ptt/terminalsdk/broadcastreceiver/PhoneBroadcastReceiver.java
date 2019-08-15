package ptt.terminalsdk.broadcastreceiver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import org.apache.log4j.Logger;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import ptt.terminalsdk.context.MyTerminalFactory;

public class PhoneBroadcastReceiver extends BroadcastReceiver {

	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(getClass());

	public PhoneBroadcastReceiver(){}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("android.intent.action.PHONE_STATE")){
			TelephonyManager tManager = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
			logger.error("电话状态："+tManager.getCallState());
			switch (tManager.getCallState()) {
				case TelephonyManager.CALL_STATE_RINGING://响铃状态1
					MyTerminalFactory.getSDK().setCalling(true);
					TerminalFactory.getSDK().disConnectToServer();
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK://接通状态2
					MyTerminalFactory.getSDK().setCalling(true);
					TerminalFactory.getSDK().disConnectToServer();
					break;
				case TelephonyManager.CALL_STATE_IDLE://空闲状态0
					MyTerminalFactory.getSDK().setCalling(false);
					TerminalFactory.getSDK().connectToServer();
					break;
			}
		}
	}

}

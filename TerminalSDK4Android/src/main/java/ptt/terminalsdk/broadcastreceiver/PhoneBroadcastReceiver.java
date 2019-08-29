package ptt.terminalsdk.broadcastreceiver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.android.internal.telephony.ITelephony;

import org.apache.log4j.Logger;

import java.lang.reflect.Method;

import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.common.UrlParams;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePushingState;
import ptt.terminalsdk.context.MyTerminalFactory;

public class PhoneBroadcastReceiver extends BroadcastReceiver {

	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(getClass());
	TelephonyManager telMgr;
	public PhoneBroadcastReceiver(){}

	@Override
	public void onReceive(Context context, Intent intent) {
		telMgr = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
		if (intent.getAction().equals("android.intent.action.PHONE_STATE")){
			TelephonyManager tManager = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
			logger.error("电话状态："+tManager.getCallState());
			switch (tManager.getCallState()) {
				case TelephonyManager.CALL_STATE_RINGING://响铃状态1

					if(checkIfEndCall()){
						endCall();
					}else {
						MyTerminalFactory.getSDK().setCalling(true);
						TerminalFactory.getSDK().disConnectToServer();
					}
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

	private boolean checkIfEndCall(){
		String deviceType = TerminalFactory.getSDK().getParam(UrlParams.TERMINALMEMBERTYPE);
		return TextUtils.equals(deviceType,TerminalMemberType.TERMINAL_UAV.name()) && TerminalFactory.getSDK().getLiveManager().getVideoLivePushingStateMachine().getCurrentState() != VideoLivePushingState.IDLE;
	}

	/**
	 * 挂断电话
	 */
	private void endCall(){
		logger.info("挂断电话");
		try {
			Method method = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
			// 获取远程TELEPHONY_SERVICE的IBinder对象的代理
			IBinder binder = (IBinder) method.invoke(null, new Object[] { "phone" });
			// 将IBinder对象的代理转换为ITelephony对象
			ITelephony telephony = ITelephony.Stub.asInterface(binder);
			// 挂断电话
			telephony.endCall();
			//telephony.cancelMissedCallsNotification();

		} catch (Exception e) {
			logger.info("挂断电话失败----"+e);
		}
	}
}

package cn.vsx.vc.service;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import org.apache.log4j.Logger;

import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.activity.LockScreenActivity;
import cn.vsx.vc.activity.PixelActivity;
import cn.vsx.vc.application.MyApplication;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.service.KeepLiveManager;

public class LockScreenService extends Service {

	private Logger logger = Logger.getLogger(getClass());
	private BroadcastReceiver receiverLock = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.e("LockScreenService", intent.getAction());
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				MyApplication.instance.isScreenOff = true;
				logger.error("锁屏服务     "
						+"---是否显示锁屏："+(MyTerminalFactory.getSDK().getParam(Params.LOCK_SCREEN_HIDE_OR_SHOW, 0) == 1 ? "显示" : "隐藏")
						+"---是否被遥毙："+MyTerminalFactory.getSDK().isForbid()
						+"---是否退出："+MyTerminalFactory.getSDK().isExit()
						+"---当前是否在打电话中："+MyTerminalFactory.getSDK().isCalling()
						+"---锁屏界面是否创建：" +MyApplication.instance.isLockScreenCreat);
				//屏幕锁屏了，如果开启了锁屏设置，跳转到锁屏界面，如果没有设置，跳转到1像素界面
				if ((MyTerminalFactory.getSDK().getParam(Params.LOCK_SCREEN_HIDE_OR_SHOW, 0) == 1)
						&& !MyTerminalFactory.getSDK().isForbid()
						&& !MyTerminalFactory.getSDK().isExit()
						&& !MyTerminalFactory.getSDK().isCalling()
//						&& !MyApplication.instance.isLockScreenCreat
						) {
					Intent lockScreenIntent = new Intent(context,LockScreenActivity.class);
					lockScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
					context.startActivity(lockScreenIntent);
				} else {
					//需要判断是否有锁屏页面，如果有锁屏页面，可以跳转到1像素页面，如果没有就不能（导致系统直接杀死应用）。
					if(checkIsLockScreen()){
						Intent lockScreenIntent = new Intent(context,PixelActivity.class);
					    lockScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
					    context.startActivity(lockScreenIntent);
					}
				}
			}else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
				logger.error("锁屏服务     屏幕亮了，isScreenOff标记置为false");
				MyApplication.instance.isScreenOff = false;
				//发送一个广播关闭1像素界面
				Intent finishIntent = new Intent("FINISH_PIXELACTIVITY");
				sendBroadcast(finishIntent);
//				LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(finishIntent);
			}
		}
	};
	private BroadcastReceiver receiverCall = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("MainActivityfinish")) {
//				if (!MyTerminalFactory.getSDK().isForbid() && !MyTerminalFactory.getSDK().isExit()) {
//					Intent incomingCallIntent = new Intent(LockScreenService.this,NewMainActivity.class);
//					incomingCallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//					startActivity(incomingCallIntent);
//				}
			}
		}
	};


	private BroadcastReceiver mHomeKeyEventReceiver = new BroadcastReceiver() {
        String SYSTEM_REASON = "reason";  
        String SYSTEM_HOME_KEY = "homekey";  
        String SYSTEM_HOME_KEY_LONG = "recentapps";  
           
        @Override  
        public void onReceive(Context context, Intent intent) {  
            String action = intent.getAction();  
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {  
                String reason = intent.getStringExtra(SYSTEM_REASON);  
                if (TextUtils.equals(reason, SYSTEM_HOME_KEY)) {  
                     //表示按了home键,程序到了后台  
                }else if(TextUtils.equals(reason, SYSTEM_HOME_KEY_LONG)){  
                    //表示长按home键,显示最近使用的程序列表  
                }  
            }


        }  
    };

	/**
	 * 是否在锁屏的状态
	 * @return
	 */
	private boolean checkIsLockScreen(){
		boolean result = false;
		KeyguardManager mKeyguardManager = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
		if(mKeyguardManager!=null){
			result =  mKeyguardManager.inKeyguardRestrictedInputMode();
		}
		logger.info("LockScreenService-checkIsLockScreen-result:"+result);
		return result;
	}



	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		logger.debug("开启锁屏服务");
		KeepLiveManager.getInstance().setServiceForeground(this);
		return START_STICKY;
	}
	
	@Override
	public void onCreate() {
		IntentFilter filterLock = new IntentFilter();
		filterLock.addAction(Intent.ACTION_SCREEN_OFF);
		filterLock.addAction(Intent.ACTION_SCREEN_ON);
		registerReceiver(receiverLock, filterLock);
		
		IntentFilter filterCall = new IntentFilter();
		filterCall.addAction("MainActivityfinish");
//		KeepLiveManager.getInstance().setServiceForeground(this);
		registerReceiver(receiverCall, filterCall);


	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(receiverLock);
		unregisterReceiver(receiverCall);
		super.onDestroy();
		
//		Intent localIntent = new Intent();
//		localIntent.setClass(this, LockScreenService.class); //销毁时重新启动Service
//		this.startService(localIntent);
	}

}

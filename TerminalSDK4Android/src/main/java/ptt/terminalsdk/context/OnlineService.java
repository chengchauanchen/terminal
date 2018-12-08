package ptt.terminalsdk.context;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.widget.Toast;

import org.apache.log4j.Logger;

import ptt.terminalsdk.R;
import ptt.terminalsdk.broadcastreceiver.NetWorkConnectionChangeReceiver;
import ptt.terminalsdk.broadcastreceiver.PTTDownAndUpReceiver;
import ptt.terminalsdk.broadcastreceiver.PhoneBroadcastReceiver;
import ptt.terminalsdk.broadcastreceiver.TickAlarmReceiver;
import ptt.terminalsdk.service.KeepLiveManager;

@SuppressLint("Wakelock")
public class OnlineService extends Service {
//	private WindowManager mWindowManager;
//	private View view;
	private WakeLock wakeLock;
	
	private Logger logger = Logger.getLogger(getClass());
	
	protected PendingIntent tickPendIntent;

	private PhoneBroadcastReceiver receiver;
	private PTTDownAndUpReceiver pttDownAndUpReceiver;
	private NetWorkConnectionChangeReceiver netWorkConnectionChangeReceiver;

	@Override
	public IBinder onBind(Intent intent) {
		return new OnlineServiceBinder();
	}
	@Override
	public void onCreate() {
		super.onCreate();
		
		logger.info("OnlineService开始onCreate");
		
//		this.setTickAlarm();
		
		wakeLock = MyTerminalFactory.getSDK().getWakeLock();
//		MyTerminalFactory.getSDK().start();

		IntentFilter filterLock = new IntentFilter();
		filterLock.addAction(Intent.ACTION_SCREEN_OFF);
//		filterLock.addAction(Intent.ACTION_SCREEN_ON);
		registerReceiver(receiverLock, filterLock);

		receiver = new PhoneBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_CALL);
		registerReceiver(receiver, filter);

		pttDownAndUpReceiver = new PTTDownAndUpReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("BLUE_TOOTH_DOWN");
		intentFilter.addAction("com.zello.ptt.down");
		intentFilter.addAction("com.sonim.intent.action.PTT_KEY_DOWN");
		intentFilter.addAction("android.intent.action.PTT.down");
		intentFilter.addAction("com.runbo.ptt.key.down");
		intentFilter.addAction("com.yl.ptt.keydown");
		intentFilter.addAction("android.intent.action.OPEN_PTT_BUTTON");
		intentFilter.addAction("BLUE_TOOTH_UP");
		intentFilter.addAction("com.zello.ptt.up");
		intentFilter.addAction("com.sonim.intent.action.PTT_KEY_UP");
		intentFilter.addAction("android.intent.action.PTT.up");
		intentFilter.addAction("com.runbo.ptt.key.up");
		intentFilter.addAction("com.yl.ptt.keyup");
		intentFilter.addAction("CameraKeyDispatch");
		intentFilter.addAction("SpeakerKeyDispatch");
		intentFilter.addAction("com.ntdj.ptt_down");
		intentFilter.addAction("com.ntdj.ptt_up");
		registerReceiver(pttDownAndUpReceiver,intentFilter);
		netWorkConnectionChangeReceiver = new NetWorkConnectionChangeReceiver();
		IntentFilter netFilter = new IntentFilter();
		netFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(netWorkConnectionChangeReceiver,netFilter);
//		mWindowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
//		view = new View(this);
//		WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(1, 1, WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
//				WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, PixelFormat.TRANSLUCENT);
//		layoutParams.gravity = Gravity.RIGHT | Gravity.TOP;
//		mWindowManager.addView(view,layoutParams);



	}
	
	@Override
	public void onDestroy() {
		logger.info("OnlineService执行onDestroy");
//		MyTerminalFactory.getSDK().stop();
		unregisterReceiver(receiver);
		unregisterReceiver(pttDownAndUpReceiver);
		unregisterReceiver(receiverLock);
		unregisterReceiver(netWorkConnectionChangeReceiver);
		Log.e("OnlineService", "OnlineService被杀了，要重新启动");
		Intent intent = new Intent();
		intent.setAction("RESTART_ONLINESERVICE");
		sendBroadcast(intent);
//		startService(new Intent(this, OnlineService.class));
//		startService(new Intent(this,MessageService.class));
//		bindService(new Intent(this,MessageService.class), MyTerminalFactory.getSDK().getMessageServiceConn(), Context.BIND_IMPORTANT);
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent param, int flags, int startId) {
		KeepLiveManager.getInstance().setServiceForeground(this);
//		logger.info("OnlineService执行onStartCommand");
		try {
			//如果全部更新完成，没有退出，就发送OnlineService开启的广播
			if(MyTerminalFactory.getSDK().isAllConfigUpdateComplete() && !MyTerminalFactory.getSDK().isExit()){
				logger.debug("发布online_services_started_broadcast");
				Intent broadcast = new Intent(getResources().getString(R.string.online_services_started_broadcast));
				sendBroadcast(broadcast);
			}
			if (param == null) {
				return START_STICKY;
			}
			String cmd = param.getStringExtra("CMD");
			if (cmd == null) {
				cmd = "";
			}
			logger.info("OnlineService开始onStartCommand，CMD = " + cmd);
			if (cmd.equals("TICK")) {
				if (wakeLock != null && wakeLock.isHeld() == false) {
					wakeLock.acquire();
				}
			}
			if (cmd.equals("RESET")) {
				if (wakeLock != null && wakeLock.isHeld() == false) {
					wakeLock.acquire();
				}
				MyTerminalFactory.getSDK().reConnectToSignalServer();
			}
			if (cmd.equals("TOAST")) {
				String text = param.getStringExtra("TEXT");
				if (text != null && text.trim().length() != 0) {
					Toast.makeText(this, text, Toast.LENGTH_LONG).show();
				}
			}
			return START_STICKY;
		}
		catch(Exception e){
			logger.error("在线服务启动过程中出现异常", e);
			return START_STICKY;
		}
	}
	
	/**设置滴答警报声*/
	protected void setTickAlarm(){
		AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);  
		Intent intent = new Intent(this,TickAlarmReceiver.class);
		int requestCode = 0;  
		tickPendIntent = PendingIntent.getBroadcast(this,  
		requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);  
		//小米2s的MIUI操作系统，目前最短广播间隔为5分钟，少于5分钟的alarm会等到5分钟再触发！2014-04-28
		long triggerAtTime = System.currentTimeMillis();
		int interval = 300 * 1000;  
		alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtTime, interval, tickPendIntent);
	}

	private BroadcastReceiver receiverLock = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				Log.d("OnlineService", "屏幕锁屏了，将OnlineService放到前台进程");
				KeepLiveManager.getInstance().setServiceForeground(OnlineService.this);
			}
		}
	};

	public class OnlineServiceBinder extends Binder{
		public OnlineService getService() {
			return OnlineService.this;
		}
	}
}

package ptt.terminalsdk.context;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.widget.Toast;

import com.blankj.utilcode.util.PermissionUtils;

import org.apache.log4j.Logger;

import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.common.UrlParams;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.auth.LoginState;
import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.broadcastreceiver.PTTDownAndUpReceiver;
import ptt.terminalsdk.broadcastreceiver.PhoneBroadcastReceiver;
import ptt.terminalsdk.broadcastreceiver.TickAlarmReceiver;
import ptt.terminalsdk.permission.FloatWindowManager;
import ptt.terminalsdk.service.BluetoothLeService;
import ptt.terminalsdk.service.KeepLiveManager;
import ptt.terminalsdk.tools.AuthUtil;

@SuppressLint("Wakelock")
public class OnlineService extends Service {
	private WakeLock wakeLock;

	private Logger logger = Logger.getLogger(getClass());

	protected PendingIntent tickPendIntent;

	private PhoneBroadcastReceiver receiver;
	private PTTDownAndUpReceiver pttDownAndUpReceiver;

	@Override
	public IBinder onBind(Intent intent) {
		return new OnlineServiceBinder();
	}
	@Override
	public void onCreate() {
		super.onCreate();
		Log.e("--vsxSDK--","启动惟实信Service开始onCreate");
		logger.info("OnlineService开始onCreate");

//		this.setTickAlarm();

		wakeLock = MyTerminalFactory.getSDK().getWakeLock();
//		MyTerminalFactory.getSDK().start();

		//锁屏广播
		IntentFilter filterLock = new IntentFilter();
		filterLock.addAction(Intent.ACTION_SCREEN_OFF);
//		filterLock.addAction(Intent.ACTION_SCREEN_ON);
		registerReceiver(receiverLock, filterLock);

		//手机电话状态 (响铃、接通、空闲) 空闲时主动向服务器建立连接
		receiver = new PhoneBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_CALL);
		filter.addAction("android.intent.action.PHONE_STATE");
		registerReceiver(receiver, filter);

		//组呼按钮
		pttDownAndUpReceiver = new PTTDownAndUpReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("BLUE_TOOTH_DOWN");
		//com.zello.ptt.down
		intentFilter.addAction(BluetoothLeService.PTT_DOWN);
		//com.zello.ptt.up
		intentFilter.addAction(BluetoothLeService.PTT_UP);
		intentFilter.addAction("com.sonim.intent.action.PTT_KEY_DOWN");
		intentFilter.addAction("android.intent.action.PTT.down");
		intentFilter.addAction("com.runbo.ptt.key.down");
		intentFilter.addAction("com.yl.ptt.keydown");
		intentFilter.addAction("android.intent.action.OPEN_PTT_BUTTON");
		intentFilter.addAction("BLUE_TOOTH_UP");
		intentFilter.addAction("com.sonim.intent.action.PTT_KEY_UP");
		intentFilter.addAction("android.intent.action.PTT.up");
		intentFilter.addAction("com.runbo.ptt.key.up");
		intentFilter.addAction("com.yl.ptt.keyup");
		intentFilter.addAction("CameraKeyDispatch");
		intentFilter.addAction("SpeakerKeyDispatch");
		intentFilter.addAction("com.ntdj.ptt_down");
		intentFilter.addAction("com.ntdj.ptt_up");
		registerReceiver(pttDownAndUpReceiver,intentFilter);
	}

	@Override
	public void onDestroy() {
		logger.info("OnlineService执行onDestroy");
		unregisterReceiver(receiver);
		unregisterReceiver(pttDownAndUpReceiver);
		unregisterReceiver(receiverLock);
		Log.e("OnlineService", "OnlineService被杀了，要重新启动");
		Intent intent = new Intent();
		intent.setAction("RESTART_ONLINESERVICE");
		sendBroadcast(intent);
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent param, int flags, int startId) {
		Log.e("--vsxSDK--","启动惟实信Service开始onStartCommand"+param);
		KeepLiveManager.getInstance().setServiceForeground(this);
		try {
			//如果全部更新完成，没有退出，就发送OnlineService开启的广播
//			if(!MyTerminalFactory.getSDK().isExit()){
//				logger.debug("--vsxSDK--发布online_services_started_broadcast");
//				Intent broadcast = new Intent(getResources().getString(R.string.online_services_started_broadcast));
//				sendBroadcast(broadcast);
//
////				logger.debug("--vsxSDK--发布 自动认证 cn.vsx.vc.AUTH_RECEIVER");
////				//自动认证
////				Intent intent = new Intent("cn.vsx.vc.AUTH_RECEIVER");
////				sendBroadcast(intent);
//			}else{
//				logger.debug("--vsxSDK--发布 如果全部更新完成，没有退出");
//			}

			if (param == null) {
				Log.e("--vsxSDK--","Intent 为null");
				return START_STICKY;
			}
			String cmd = param.getStringExtra("CMD");
			if (cmd == null) {
				cmd = "";
			}
			Log.e("--vsxSDK-","-OnlineService开始onStartCommand，CMD = " + cmd);
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

			//关联启动 自动认证
			if("AUTH".equals(cmd)){
				//如果有读写权限就配置log
				if(PermissionUtils.isGranted(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
					MyTerminalFactory.getSDK().configLogger();
				}
				//如果有悬浮窗权限，把事件service启动起来
				if(!FloatWindowManager.getInstance().checkPermission(this)){
					logger.info("--vsxSDK--没有获取到悬浮窗权限");
				}else {
					logger.info("--vsxSDK--startHandlerService");
					BaseApplication.getApplication().startHandlerService();
				}
				LoginState loginState = TerminalFactory.getSDK().getAuthManagerTwo().getLoginStateMachine().getCurrentState();
				Log.e("--vsxSDK--","loginState:"+loginState);
				logger.info("--vsxSDK-loginState:"+loginState);
				if(TerminalFactory.getSDK().getAuthManagerTwo().getLoginStateMachine().getCurrentState() == null ||
						TerminalFactory.getSDK().getAuthManagerTwo().getLoginStateMachine().getCurrentState() == LoginState.IDLE){
					if(checkCanStartAuth()){
						logger.info("--vsxSDK-startAuth");
						startAuth();
					}
				}
			}
			return START_STICKY;
		}
		catch(Exception e){
			Log.e("--vsxSDK-","-在线服务启动过程中出现异常", e);
			return START_STICKY;
		}
	}

	/**
	 * 判断是否可以在这里开始认证
	 * @return
	 */
	private boolean checkCanStartAuth(){
		String deviceType = TerminalFactory.getSDK().getParam(UrlParams.TERMINALMEMBERTYPE);
		return (TerminalMemberType.valueOf(deviceType).getCode() == TerminalMemberType.TERMINAL_PHONE.getCode());
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

	private void startAuth(){
		Log.e("-vsxSDK--", "startAuth");
		logger.info("-vsxSDK--startAuth");
		AuthUtil.setOauthInfo(getApplicationContext());
		String authUrl = TerminalFactory.getSDK().getParam(Params.AUTH_URL, "");
		if(android.text.TextUtils.isEmpty(authUrl)){
			String[] defaultAddress = TerminalFactory.getSDK().getAuthManagerTwo().getDefaultAddress();
			if (defaultAddress.length >= 2) {
				int resultCode = TerminalFactory.getSDK().getAuthManagerTwo().startAuth(defaultAddress[0], defaultAddress[1]);
				if (resultCode == BaseCommonCode.SUCCESS_CODE) {
				} else {
					//状态机没有转到正在认证，说明已经在状态机中了，不用处理
					Log.e("--vsx--AuthService--", "状态机没有转到正在认证，说明已经在状态机中了，不用处理");
					logger.info("--vsx--AuthService--状态机没有转到正在认证，说明已经在状态机中了，不用处理");
				}
			} else {
				Log.e("--vsx--AuthService--", "没有注册服务地址，去探测地址");
				logger.info("--vsx--AuthService--没有注册服务地址，去探测地址");
				//没有注册服务地址，去探测地址
				TerminalFactory.getSDK().getAuthManagerTwo().checkRegistIp();
			}
		}else {
			//有注册服务地址，去认证
			Log.e("OnlineService", "startAuth");
			logger.info("--vsx--OnlineService--startAuth");
			TerminalFactory.getSDK().getAuthManagerTwo().startAuth(TerminalFactory.getSDK().getParam(Params.REGIST_IP, ""), TerminalFactory.getSDK().getParam(Params.REGIST_PORT, ""));
		}
	}
}

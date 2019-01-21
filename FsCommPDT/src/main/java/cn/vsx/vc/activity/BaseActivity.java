package cn.vsx.vc.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import org.apache.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import butterknife.ButterKnife;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveExitHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberDeleteHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyMemberKilledHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveVolumeOffCallHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.receive.Actions;
import cn.vsx.vc.receive.IBroadcastRecvHandler;
import cn.vsx.vc.receive.RecvCallBack;
import cn.vsx.vc.receive.SendRecvHelper;
import cn.vsx.vc.receiver.HeadsetPlugReceiver;
import cn.vsx.vc.utils.ActivityCollector;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.PhoneAdapter;
import cn.vsx.vc.utils.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

public abstract class BaseActivity extends AppCompatActivity implements RecvCallBack,Actions {

	private AudioManager audioManager;
	private HeadsetPlugReceiver headsetPlugReceiver;
	private PowerManager.WakeLock wakeLock;
	private PowerManager.WakeLock wakeLockScreen;


	private SensorManager sensorManager;// 传感器管理对象,调用距离传感器，控制屏幕
	private PowerManager powerManager;
	protected final String TAG = getClass().getSimpleName();

    //成员被删除了
	private ReceiveMemberDeleteHandler receiveMemberDeleteHandler = new ReceiveMemberDeleteHandler() {
		@Override
		public void handler() {
			myHandler.post(() -> MyApplication.instance.stopIndividualCallService());
		}
	};


	private boolean isBackground(Context context) {
		ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
		for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
			if (appProcess.processName.equals(context.getPackageName())) {
				logger.info("此app =" + appProcess.importance + ",context.getClass().getName()=" + context.getClass().getName());
				if (appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
					logger.info("处于后台" + appProcess.processName);
					return true;
				} else {
					logger.info("处于前台" + appProcess.processName);
					return false;
				}
			}
		}
		return false;
	}

	@SuppressWarnings("unused")
	private void setMySpeakerphoneOn(boolean on) {
		if (on) {
			if (!audioManager.isSpeakerphoneOn() && audioManager.getMode() == AudioManager.MODE_NORMAL) {
				logger.info("---------打开扬声器--------" + audioManager.getMode());
				audioManager.setSpeakerphoneOn(true);
			}
		} else {
			if (audioManager.isSpeakerphoneOn()) {
				audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
						audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
						AudioManager.STREAM_VOICE_CALL);//设置听筒的最大音量
				audioManager.setSpeakerphoneOn(false);//关闭扬声器
				logger.info("--------关闭扬声器---------" + audioManager.getMode());
			}
		}
	}


	private Handler myHandler = new Handler(Looper.getMainLooper());
	public Logger logger = Logger.getLogger(BaseActivity.class);

	/**组成员遥毙消息*/
	protected ReceiveNotifyMemberKilledHandler receiveNotifyMemberKilledHandler = new ReceiveNotifyMemberKilledHandler() {
		@Override
		public void handler(boolean forbid) {
			logger.error("收到遥毙，此时forbid状态为：" + forbid);
			if(forbid){
				myHandler.post(new Runnable() {
					@Override
					public void run() {
						TerminalFactory.getSDK().putParam(Params.IS_FIRST_LOGIN, true);
						TerminalFactory.getSDK().putParam(Params.IS_UPDATE_DATA, true);
						startActivity(new Intent(BaseActivity.this, KilledActivity.class));
						BaseActivity.this.finish();
						MyApplication.instance.stopIndividualCallService();
					}
				});
			}
		}
	};

	protected ReceiveExitHandler receiveExitHandler = new ReceiveExitHandler() {
		@Override
		public void handle(String msg){
			ToastUtil.showToast(BaseActivity.this,msg);
			myHandler.postDelayed(new Runnable(){
				@Override
				public void run(){
					Intent stoppedCallIntent = new Intent("stop_indivdualcall_service");
					stoppedCallIntent.putExtra("stoppedResult","0");
					SendRecvHelper.send(getApplicationContext(),stoppedCallIntent);

					MyTerminalFactory.getSDK().exit();//停止服务
					PromptManager.getInstance().stop();
					for (Activity activity : ActivityCollector.getAllActivity().values()) {
						activity.finish();
					}
					TerminalFactory.getSDK().putParam(Params.IS_FIRST_LOGIN, true);
					TerminalFactory.getSDK().putParam(Params.IS_UPDATE_DATA, true);
					MyApplication.instance.isClickVolumeToCall = false;
					MyApplication.instance.isPttPress = false;
					MyApplication.instance.stopIndividualCallService();
					Process.killProcess(Process.myPid());
				}
			},2000);
		}
	};

	static Method findViewBinderForClassMethod;
	static {
		Method[] ms = ButterKnife.class.getDeclaredMethods();
		for(Method m : ms){
			if(m.getName().equals("findViewBinderForClass")){
				findViewBinderForClassMethod = m;
				findViewBinderForClassMethod.setAccessible(true);
				break;
			}
		}
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 没有标题栏
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//不可横屏
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		//透明状态栏
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		}

		setContentView(getLayoutResId());
		Log.d(TAG, "onCreate:"+MyApplication.instance.mAppStatus);
		// 判断如果被强杀，就回到 MainActivity 中去，否则可以初始化
		if(MyApplication.instance.mAppStatus == Constants.FORCE_KILL){
			Log.d(TAG, "应用被强杀了，重新去注册界面");
			//重走应用流程
			protectApp();
		}else {
			ButterKnife.bind(this);
			try {
				ButterKnife.ViewBinder e = (ButterKnife.ViewBinder) findViewBinderForClassMethod.invoke(null, BaseActivity.class);
				if(e != null) {
					e.bind(ButterKnife.Finder.ACTIVITY, this, this);
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}

			regBroadcastRecv(ACT_SHOW_FULL_SCREEN,ACT_DISMISS_FULL_SCREEN,STOP_INDIVDUALCALL_SERVEIC);
			ActivityCollector.addActivity(this,getClass());
			initView();

			initData();

			initListener();

			powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
			audioManager= (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
			//距离感应器的电源锁
			wakeLock = powerManager.newWakeLock(32, "wakeLock");
			//插入耳机时的电源锁
			wakeLockScreen = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
					| PowerManager.SCREEN_DIM_WAKE_LOCK, "wakeLock");

		}

	}

	protected void protectApp(){
		// 重新走应用的流程是一个正确的做法，因为应用被强杀了还保存 Activity 的栈信息是不合理的
		Intent intent = new Intent(this, RegistActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
		finish();
	}

	/**
	 * 获取当前界面的布局
	 */
	public abstract int getLayoutResId();

	/**
	 * 初始化界面
	 */
	public abstract void initView();

	/**
	 * 给控件添加监听
	 */
	public abstract void initListener();

	/**
	 * 初始化数据 给控件填充内容
	 */
	public abstract void initData();

	/**
	 * 子类activity处理自己的destroy()
	 */
	public abstract void doOtherDestroy();


    @Override
    protected void onResume(){
        super.onResume();
        MyTerminalFactory.getSDK().registReceiveHandler(receiveExitHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyMemberKilledHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveMemberDeleteHandler);
        registerHeadsetPlugReceiver();
        setPttVolumeChangedListener();
    }

    @Override
    protected void onPause(){
        super.onPause();
        unregisterHeadsetPlugReceiver();
    }

	@Override
	protected void onDestroy() {
		try{
			doOtherDestroy();
			ButterKnife.unbind(this);//解除绑定，官方文档只对fragment做了解绑
			ActivityCollector.removeActivity(this);

			MyTerminalFactory.getSDK().unregistReceiveHandler(receiveExitHandler);
			MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMemberDeleteHandler);
			MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyMemberKilledHandler);

			if (mBroadcastReceiv != null) {
				LocalBroadcastManager.getInstance(BaseActivity.this).unregisterReceiver(
						mBroadcastReceiv);
			}
			logger=null;
		}catch(Exception e){
			e.printStackTrace();
		}
		super.onDestroy();
	}


	/** 广播 */
	protected IBroadcastRecvHandler mBroadcastReceiv;
	/** 广播过滤 */
	protected IntentFilter mReceivFilter;
	/**
	 * 注册广播
	 */
	public void regBroadcastRecv(String... actions) {
		if (mBroadcastReceiv == null || mReceivFilter == null) {
			mBroadcastReceiv = new IBroadcastRecvHandler(this);
			mReceivFilter = new IntentFilter();
		}
		if (actions != null) {
			for (String act : actions) {
				mReceivFilter.addAction(act);
			}
		}
		LocalBroadcastManager.getInstance(BaseActivity.this).registerReceiver(
				mBroadcastReceiv, mReceivFilter);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent != null) {
			String action = intent.getAction();
//			if (action.equals(HIDE_KEY)) {
//				logger.info("sjl_收到服务里的广播，收起键盘");
////				hideSoftKeyBoard();
//			}
		}
	}

	/**注册耳机插入拔出的监听*/
	private void registerHeadsetPlugReceiver() {
		headsetPlugReceiver = new HeadsetPlugReceiver(sensorManager,
				sensorEventListener, wakeLockScreen);
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.HEADSET_PLUG");
		registerReceiver(headsetPlugReceiver, filter);
	}

	/**注销耳机插入拔出的监听*/
	private void unregisterHeadsetPlugReceiver() {
		if (headsetPlugReceiver != null) {
			unregisterReceiver(headsetPlugReceiver);
		}
	}
	/**距离感应器*/
	@SuppressLint("Wakelock")
	private SensorEventListener sensorEventListener = new SensorEventListener() {

		@Override
		public void onSensorChanged(SensorEvent event) {
			float[] its = event.values;
			if (its != null && event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
				// 经过测试，当手贴近距离感应器的时候its[0]返回值为0.0，当手离开时返回1.0
				if (its[0] == 0.0) {// 贴近手机
					logger.info("hands up in calling activity贴近手机");
					if (!wakeLock.isHeld()) {
						wakeLock.acquire();// 申请设备电源锁
					}
				} else {// 远离手机
					logger.info("hands moved in calling activity远离手机");
					if (wakeLock.isHeld()) {
						wakeLock.release(); // 释放设备电源锁
					}
				}
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		logger.info("keyCode:"+keyCode+"--event:"+event);
		// 如果程序在后台运行，则忽略
		if ( isBackground( getApplicationContext()) ) {
			return super.onKeyDown(keyCode, event);
		}
		switch (keyCode) {
			case KeyEvent.KEYCODE_MEDIA_PLAY:
			case KeyEvent.KEYCODE_MEDIA_PAUSE:
				Intent intent = new Intent();
				//判断状态机此时的状态
				if(MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.IDLE){
					//蓝牙耳机点击事件
					intent.setAction("BLUE_TOOTH_DOWN");
				}else {
					intent.setAction("BLUE_TOOTH_UP");
				}
				sendBroadcast(intent);
				break;
			case KeyEvent.KEYCODE_VOLUME_UP:// 增大音量
				audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
						AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND
								| AudioManager.FLAG_SHOW_UI);
				return true;
			case KeyEvent.KEYCODE_VOLUME_DOWN:// 减小音量
				audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
						AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND
								| AudioManager.FLAG_SHOW_UI);
				return true;
			case KeyEvent.KEYCODE_HEADSETHOOK:
				if(PhoneAdapter.isF32()){
					return true;
				}
				if (!MyApplication.instance.isPttPress) {
					logger.info("onKeyUp执行第一次，开始说话");
					if (onPTTVolumeBtnStatusChangedListener != null) {
						logger.warn("用耳机中键做ptt按钮，按下时：ptt的当前状态是："+ MyApplication.instance.getGroupSpeakState());
						onPTTVolumeBtnStatusChangedListener.onPTTVolumeBtnStatusChange(MyApplication.instance.getGroupSpeakState());
					}
					MyApplication.instance.isPttPress = true;
				} else if (MyApplication.instance.isPttPress) {
					logger.info("onClickUp执行第二次，暂停说话");
					if (onPTTVolumeBtnStatusChangedListener != null) {
						onPTTVolumeBtnStatusChangedListener.onPTTVolumeBtnStatusChange(GroupCallSpeakState.END);
					}
					MyApplication.instance.isPttPress = false;
				}
				return true;
		}
		// 为true,则其它后台按键处理再也无法处理到该按键，为false,则其它后台按键处理可以继续处理该按键事件
		return super.onKeyDown(keyCode, event);
	}
	//音量上下键为PTT按钮状态改变的监听接口
	private  OnPTTVolumeBtnStatusChangedListener onPTTVolumeBtnStatusChangedListener;
	public interface OnPTTVolumeBtnStatusChangedListener{
		void onPTTVolumeBtnStatusChange(GroupCallSpeakState groupCallSpeakState);
	}
	public void setOnPTTVolumeBtnStatusChangedListener(OnPTTVolumeBtnStatusChangedListener onPTTVolumeBtnStatusChangedListener){
		this.onPTTVolumeBtnStatusChangedListener = onPTTVolumeBtnStatusChangedListener;
	}

	boolean isDown = true;
	long lastVolumeUpTime = 0;
	long lastVolumeDownTime = 0;
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		logger.info("event.getKeyCode() = " + event.getKeyCode());
		switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_VOLUME_UP:// 增大音量
				if (MyTerminalFactory.getSDK().getParam(Params.VOLUME_UP, false)) {
					groupCallByVolumeKey(event, true);
					return true;
				}else {
					if(System.currentTimeMillis() - lastVolumeUpTime > 500) {
						MyTerminalFactory.getSDK().getAudioProxy().volumeUp();
						if(MyTerminalFactory.getSDK().getAudioProxy().getVolume()>0){
							OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveVolumeOffCallHandler.class, false,1);
						}else {
							OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveVolumeOffCallHandler.class, true,1);
						}
						lastVolumeUpTime = System.currentTimeMillis();
					}
				}
				return true;
			case KeyEvent.KEYCODE_VOLUME_DOWN:// 减小音量
				if (MyTerminalFactory.getSDK().getParam(Params.VOLUME_DOWN, false)) {
					groupCallByVolumeKey(event, false);
					return true;
				}else{
					if(System.currentTimeMillis() - lastVolumeDownTime > 500) {
						MyTerminalFactory.getSDK().getAudioProxy().volumeDown();
						if(MyTerminalFactory.getSDK().getAudioProxy().getVolume()>0){
							OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveVolumeOffCallHandler.class, false,1);
						}else {
							OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveVolumeOffCallHandler.class, true,1);
						}
						lastVolumeDownTime = System.currentTimeMillis();
					}
				}
				return true;
			case KeyEvent.KEYCODE_DPAD_DOWN:
			case KeyEvent.KEYCODE_DPAD_LEFT:
			case KeyEvent.KEYCODE_DPAD_RIGHT:
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_DPAD_UP:
			{
//				// 后台运行或者机型未非F25，则忽略
//				if ( isBackground( getApplicationContext()) || !PhoneAdapter.isF25() ) {
//					break;
//				}
//
//				// 键盘按下，触发0到多次的down，一个Up操作，但只触发一个事件
//				if (event.getAction() == KeyEvent.ACTION_DOWN) {
//					if (null != currentFragment && isDown ) {
//						((BaseFragment) currentFragment).onMyKeyDown(event);
//						isDown = false;
//					}
//				}
//				else {
//					if (isDown && null != currentFragment) {
//						((BaseFragment) currentFragment).onMyKeyDown(event);
//					}
//					isDown = true;
//				}
				return true;
			}
			case KeyEvent.KEYCODE_F6:
				return true;
		}
		return super.dispatchKeyEvent(event);
	}

	/**设置音量上下键为PTT按钮*/
	private int downTimes = 0;
	private void groupCallByVolumeKey(KeyEvent event, boolean isVolumeUp) {
		switch (event.getAction()) {
			case KeyEvent.ACTION_DOWN:
				downTimes++;
				logger.warn("用音量键做ptt按钮，音量键按下时：downTimes："+downTimes+"    pttPress状态："+ MyApplication.instance.isPttPress);
				if (downTimes == 1 && !MyApplication.instance.folatWindowPress && !MyApplication.instance.isPttPress) {

					MyApplication.instance.isClickVolumeToCall = true;
					if (onPTTVolumeBtnStatusChangedListener != null) {
						logger.warn("用音量键做ptt按钮，音量键按下时：ptt的当前状态是："+ MyApplication.instance.getGroupSpeakState());
						onPTTVolumeBtnStatusChangedListener.onPTTVolumeBtnStatusChange(MyApplication.instance.getGroupSpeakState());
					}
					MyApplication.instance.volumePress = true;

					if (isVolumeUp){
						volumeUpCalling = true;
					}else {
						volumeDownCalling = true;
					}
				}
				break;
			case KeyEvent.ACTION_UP:
				logger.info("音量的抬起事件 " + MyApplication.instance.volumePress + (MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.WAITING) +
						(MyApplication.instance.getGroupListenenState() == GroupCallListenState.LISTENING));
				if(MyApplication.instance.volumePress){
					MyApplication.instance.isClickVolumeToCall = false;
					if (onPTTVolumeBtnStatusChangedListener != null) {
						onPTTVolumeBtnStatusChangedListener.onPTTVolumeBtnStatusChange(GroupCallSpeakState.END);
					}
					MyApplication.instance.volumePress = false;
				}
				downTimes = 0;
				volumeUpCalling = false;
				volumeDownCalling = false;
				break;
		}
	}
	private boolean volumeUpCalling, volumeDownCalling;
	private void setPttVolumeChangedListener() {
		SharedPreferences account = MyTerminalFactory.getSDK().getAccount();
		if (account != null){
			account.registerOnSharedPreferenceChangeListener(listener);
		}
	}
	private SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			if ((key.equals(Params.VOLUME_UP) && !MyTerminalFactory.getSDK().getParam(Params.VOLUME_UP, false) && volumeUpCalling)
					||(key.equals(Params.VOLUME_DOWN) && !MyTerminalFactory.getSDK().getParam(Params.VOLUME_DOWN, false) && volumeDownCalling)){
				if (onPTTVolumeBtnStatusChangedListener != null) {
					onPTTVolumeBtnStatusChangedListener.onPTTVolumeBtnStatusChange(GroupCallSpeakState.END);
				}
				MyApplication.instance.isClickVolumeToCall = false;
				MyApplication.instance.volumePress = false;
			}
		}
	};

}

package cn.vsx.vc.application;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.multidex.MultiDex;
import android.util.Log;

import org.apache.log4j.Logger;
import org.easydarwin.push.UVCCameraService;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.SpecificSDK.SpecificSDK;
import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.common.UrlParams;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenStateMachine;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakStateMachine;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallState;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallStateMachine;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePlayingState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePlayingStateMachine;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePushingState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePushingStateMachine;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.receiver.AccountValidReceiver;
import cn.vsx.vc.service.PTTButtonEventService;
import cn.vsx.vc.utils.CommonGroupUtil;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.NfcUtil;
import ptt.terminalsdk.broadcastreceiver.FileExpireReceiver;
import ptt.terminalsdk.context.MyTerminalFactory;

public class MyApplication extends Application {

	private Logger logger = Logger.getLogger(getClass());
	public static final String TAG = "MyApplication---";
	public int mAppStatus = Constants.FORCE_KILL;//App运行状态，是否被强杀
	public boolean isBinded=false;
	public boolean isGroupCalling = false;
	public boolean isContactsIndividual = false;
	public boolean isUpdatingAPP  = false;
	public boolean isChanging  = false;
	public boolean isScreenOff  = false;
	public boolean isLockScreenCreat = false;
	public boolean isClickVolumeToCall = false;
	public boolean isPttPress = false;
	public boolean isPopupWindowShow = false;
	public static List<Integer> catchGroupIdList = new ArrayList<>();
	public static MyApplication instance;
	public SpecificSDK specificSDK;

	public boolean folatWindowPress = false; //记录悬浮按钮是否按下
	public boolean volumePress = false; //记录音量是否按下
	public long clicktime = 0;//点击时间

	/**标记个呼来或者请求图形来，是否做了接受或拒绝的操作，默认是false*/
	public boolean isPrivateCallOrVideoLiveHand = false;
	//
	private AlarmManager accountValidAlarmManager;
	private PendingIntent accountValidPendingIntent;
	//通知账号有效的间隔时间
	public static final long ACCOUNT_VALID_TIME = 8 * 60 * 60 * 1000;

	@Override
	public void onCreate() {
		instance = this;
		super.onCreate();
		specificSDK = new SpecificSDK(this);
		MyTerminalFactory.setTerminalSDK(specificSDK);
		MyTerminalFactory.getSDK().setLoginFlag();
		registerActivityLifecycleCallbacks(new SimpleActivityLifecycle());
		try{
			ApplicationInfo appInfo = this.getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
			String apkType=appInfo.metaData.getString("APKTYPE");
			Log.d("MyApplication", " APKTYPE == " + apkType);
			MyTerminalFactory.getSDK().putParam(Params.APK_TYPE,apkType);
		}catch(PackageManager.NameNotFoundException e){
			e.printStackTrace();
		}
		MyTerminalFactory.getSDK().getAuthManagerTwo().initIp();

		//开启voip电话服务
//		MyTerminalFactory.getSDK().getVoipCallManager().startService(getApplicationContext());
		MyTerminalFactory.getSDK().putParam(UrlParams.TERMINALMEMBERTYPE, TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.toString());
		catchGroupIdList = CommonGroupUtil.getCatchGroupIds();
		//保存录像，录音，照片的存储路径
		MyTerminalFactory.getSDK().getFileTransferOperation().initExternalUsableStorage();
		//开启账号解绑的倒计时
		startAccountValidClock();
	}


	public void setIsContactsPersonal(boolean isContactsIndividual){
		this.isContactsIndividual = isContactsIndividual;
	}
	public SpecificSDK getSpecificSDK(){
		return specificSDK;
	}

	public VideoLivePlayingState getVideoLivePlayingState(){
		VideoLivePlayingStateMachine liveStateMachine = TerminalFactory.getSDK().getLiveManager().getVideoLivePlayingStateMachine();
		if (liveStateMachine != null){
			return liveStateMachine.getCurrentState();
		}
		return null;
	}
	public VideoLivePushingState getVideoLivePushingState(){
		VideoLivePushingStateMachine liveStateMachine = TerminalFactory.getSDK().getLiveManager().getVideoLivePushingStateMachine();
		if (liveStateMachine != null){
			return liveStateMachine.getCurrentState();
		}
		return null;
	}
	public IndividualCallState getIndividualState(){
		IndividualCallStateMachine individualCallStateMachine = TerminalFactory.getSDK().getIndividualCallManager().getIndividualCallStateMachine();
		if (individualCallStateMachine != null){
			return individualCallStateMachine.getCurrentState();
		}
		return null;
	}

	public GroupCallListenState getGroupListenenState(){
		GroupCallListenStateMachine groupCallListenStateMachine = TerminalFactory.getSDK().getGroupCallManager().getGroupCallListenStateMachine();
		if (groupCallListenStateMachine != null){
			return groupCallListenStateMachine.getCurrentState();
		}
		return null;
	}

	public GroupCallSpeakState getGroupSpeakState(){
		GroupCallSpeakStateMachine groupCallSpeakStateMachine = TerminalFactory.getSDK().getGroupCallManager().getGroupCallSpeakStateMachine();
		if (groupCallSpeakStateMachine != null){
			return groupCallSpeakStateMachine.getCurrentState();
		}
		return null;
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}

	public void startPTTButtonEventService(String  action) {
		Intent intent1 = new Intent(this,PTTButtonEventService.class);
		intent1.putExtra(Constants.PTTEVEVT_ACTION,action);
		isBinded=bindService(intent1,conn,BIND_AUTO_CREATE);
	}
	public void stopPTTButtonEventService(){
		if (conn != null) {
			if (pttButtonEventBinder != null) {
				pttButtonEventBinder.pttButtonEventService();
			}
			Log.i("服务状态1：",""+conn);
			Log.i("服务状态2：",""+isBinded);
			if (isBinded) {
				unbindService(conn);
				isBinded=false;
			}
			stopService(new Intent(this, PTTButtonEventService.class));
		}
	}
	private PTTButtonEventService.PTTButtonEventBinder pttButtonEventBinder;
	private ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			pttButtonEventBinder = (PTTButtonEventService.PTTButtonEventBinder) service;
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.e("MyApplication", "IndividualCallService服务断开了");
		}
	};

	public void startUVCCameraService(){
		Intent intent = new Intent(this,UVCCameraService.class);
		bindService(intent,cameraconn,BIND_AUTO_CREATE);
	}

	public void stopUVCCameraService(){
		unbindService(cameraconn);
	}

	private UVCCameraService.MyBinder uvcBinder;
	private ServiceConnection cameraconn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			uvcBinder = (UVCCameraService.MyBinder) service;
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.e("MyApplication", "UVCCameraService服务断开了");
		}
	};

	/**
	 * 获取AlarmManager
	 *
	 * @return
	 */
	public AlarmManager getAccountValidAlarmManager() {
		if (accountValidAlarmManager == null) {
			accountValidAlarmManager = (AlarmManager) this.getSystemService(ALARM_SERVICE);
		}
		return accountValidAlarmManager;
	}

	/**
	 * 获取PendingIntent
	 */
	private PendingIntent getAccountValidPendingIntent() {
		if (accountValidPendingIntent == null) {
			Intent intent = new Intent(this, AccountValidReceiver.class);
			intent.setAction("vsxin.action.accountvalid");
			accountValidPendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		}
		return accountValidPendingIntent;
	}

	/**
	 * 开启8小时解绑的倒计时
	 *
	 * @param endTime 到期时间
	 */
	public void startAccountValidAlarmManager(long endTime) {
		logger.info("AccountValidAlarmManager---" + "startAccountValidAlarmManager:endTime-" + endTime);
		getAccountValidAlarmManager().set(AlarmManager.RTC_WAKEUP, endTime, getAccountValidPendingIntent());
	}

	/**
	 * 关闭8小时解绑的倒计时
	 */
	public void cancelAccountValidAlarmManager() {
		logger.info("AccountValidAlarmManager---" + "cancelAccountValidAlarmManager");
		getAccountValidAlarmManager().cancel(getAccountValidPendingIntent());
	}

	/**
	 * 开启倒计时
	 */
	public void startAccountValidClock(){
		//停止之前的账号解绑的倒计时
		MyApplication.instance.cancelAccountValidAlarmManager();
		long time = TerminalFactory.getSDK().getParam(Params.NFC_BEAN_TIME, 0L);
		if(time != 0){
			startAccountValidAlarmManager(time + ACCOUNT_VALID_TIME);
		}
	}
}

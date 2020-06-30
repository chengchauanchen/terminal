package cn.vsx.vc.application;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;

import org.apache.log4j.Logger;

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
import cn.vsx.vc.R;
import cn.vsx.vc.service.PTTButtonEventService;
import cn.vsx.vc.utils.Constants;
import ptt.terminalsdk.context.BaseApplication;
import ptt.terminalsdk.context.MyTerminalFactory;

public class MyApplication extends BaseApplication {

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
	public static MyApplication instance;

	public boolean folatWindowPress = false; //记录悬浮按钮是否按下
	public boolean volumePress = false; //记录音量是否按下
	public long clicktime = 0;//点击时间

	/**标记个呼来或者请求图形来，是否做了接受或拒绝的操作，默认是false*/
	public boolean isPrivateCallOrVideoLiveHand = false;
	//
//	private AlarmManager accountValidAlarmManager;
//	private PendingIntent accountValidPendingIntent;
//	//通知账号有效的间隔时间
//	public static final long ACCOUNT_VALID_TIME = 8 * 60 * 60 * 1000;
//	public static final long ACCOUNT_VALID_TIME =  30 * 1000;

	@Override
	public void onCreate() {
		instance = this;
		initMscV5();
		super.onCreate();
		init();
		registerActivityLifecycleCallbacks(new SimpleActivityLifecycle());
		ZXingLibrary.initDisplayOpinion(this);
	}

	private void initMscV5(){
		// 应用程序入口处调用,避免手机内存过小,杀死后台进程后通过历史intent进入Activity造成SpeechUtility对象为null
		// 注意：此接口在非主进程调用会返回null对象，如需在非主进程使用语音功能，请增加参数：SpeechConstant.FORCE_LOGIN+"=true"
		// 参数间使用“,”分隔。
		// 设置你申请的应用appid

		// 注意： appid 必须和下载的SDK保持一致，否则会出现10407错误

		StringBuffer param = new StringBuffer();
		param.append("appid="+getString(R.string.app_id));
		param.append(",");
		// 设置使用v5+
		param.append(SpeechConstant.ENGINE_MODE+"="+SpeechConstant.MODE_MSC);
		SpeechUtility.createUtility(this, param.toString());
	}

	public void init(){
		SpecificSDK.init(this,TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.toString());
		//是否是4.0之前
		TerminalFactory.getSDK().putParam(Params.IS_PREFOUR_VERSION, false);
	}

	public void setIsContactsPersonal(boolean isContactsIndividual){
		this.isContactsIndividual = isContactsIndividual;
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

	public void killAllProcess() {
		ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		if (null != mActivityManager) {
			List<ActivityManager.RunningAppProcessInfo> mList = mActivityManager.getRunningAppProcesses();
			for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : mList) {
				if (runningAppProcessInfo.pid != android.os.Process.myPid()) {
					android.os.Process.killProcess(runningAppProcessInfo.pid);
				}
			}
			android.os.Process.killProcess(android.os.Process.myPid());
		}
	}

	@Override
	public void setApkType() {
		super.setApkType();
		SpecificSDK.setApkType(this);
	}

	@Override
	public void setAppKey() {
		super.setAppKey();
		SpecificSDK.setAppKey(this);
	}

	@Override
	public void setTerminalMemberType() {
		super.setTerminalMemberType();
		MyTerminalFactory.getSDK().putParam(UrlParams.TERMINALMEMBERTYPE, TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.toString());
	}

	//	/**
//	 * 获取AlarmManager
//	 *
//	 * @return
//	 */
//	public AlarmManager getAccountValidAlarmManager() {
//		if (accountValidAlarmManager == null) {
//			accountValidAlarmManager = (AlarmManager) this.getSystemService(ALARM_SERVICE);
//		}
//		return accountValidAlarmManager;
//	}

//	/**
//	 * 获取PendingIntent
//	 */
//	private PendingIntent getAccountValidPendingIntent() {
//		if (accountValidPendingIntent == null) {
//			Intent intent = new Intent(this, AccountValidReceiver.class);
//			intent.setAction("vsxin.action.accountvalid");
//			accountValidPendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//		}
//		return accountValidPendingIntent;
//	}

//	/**
//	 * 开启8小时解绑的倒计时
//	 *
//	 * @param endTime 到期时间
//	 */
//	public void startAccountValidAlarmManager(long endTime) {
//		logger.info("AccountValidAlarmManager---" + "startAccountValidAlarmManager:endTime-" + endTime);
//		getAccountValidAlarmManager().set(AlarmManager.RTC_WAKEUP, endTime, getAccountValidPendingIntent());
//	}

//	/**
//	 * 关闭8小时解绑的倒计时
//	 */
//	public void cancelAccountValidAlarmManager() {
//		logger.info("AccountValidAlarmManager---" + "cancelAccountValidAlarmManager");
//		getAccountValidAlarmManager().cancel(getAccountValidPendingIntent());
//	}

//	/**
//	 * 开启倒计时
//	 */
//	public void startAccountValidClock(){
//		//停止之前的账号解绑的倒计时
//		MyApplication.instance.cancelAccountValidAlarmManager();
//		long time = TerminalFactory.getSDK().getParam(Params.NFC_BEAN_TIME, 0L);
//		if(time != 0){
//			if(time+ACCOUNT_VALID_TIME<=System.currentTimeMillis()){
//				//保存账号解绑时间信息
//				TerminalFactory.getSDK().putParam(Params.NFC_BEAN_TIME, 0L);
//				//清空账号信息
//				DataUtil.clearRecorderBindBean();
//			}else{
//				startAccountValidAlarmManager(time + ACCOUNT_VALID_TIME);
//			}
//		}
//	}
}

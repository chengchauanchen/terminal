package cn.vsx.vc.application;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.multidex.MultiDex;
import android.util.Log;

import org.easydarwin.push.UVCCameraService;
import org.linphone.core.LinphoneCall;

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
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.model.NFCBean;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.service.ReceiveHandlerService;
import cn.vsx.vc.utils.CommonGroupUtil;
import cn.vsx.vc.utils.Constants;
import ptt.terminalsdk.context.MyTerminalFactory;
import skin.support.SkinCompatManager;
import skin.support.design.app.SkinMaterialViewInflater;

public class MyApplication extends Application {

	public int mAppStatus = Constants.FORCE_KILL;//App运行状态，是否被强杀
	public boolean isBinded=false;
	private boolean isBindedUVCCameraService = false;
	public boolean isPttFlowPress = false;
	public boolean isContactsIndividual = false;
	public boolean isUpdatingAPP  = false;
	public boolean isChanging  = false;
	public boolean isScreenOff  = false;
	public boolean isLockScreenCreat = false;
	public boolean isClickVolumeToCall = false;
	public boolean isPttViewPager = true;
	public boolean isTalkbackFragment = true;
	public boolean isPttPress = false;
	public boolean isMoved = false;
	public boolean viewAdded;
	public boolean usbAttached;//外置摄像头是否连接
	public static List<Integer> catchGroupIdList = new ArrayList<>();
	public boolean isPlayVoice = false;
	//	public boolean isScanGroupSearch=false;//区分扫描组搜索和正常搜索
	public static MyApplication instance;
	public SpecificSDK specificSDK;

	public boolean folatWindowPress = false; //记录悬浮按钮是否按下
	public boolean volumePress = false; //记录音量是否按下
	public boolean isCallState = false; //记录主动个呼嘟嘟声

	public boolean isMiniLive;//直播视频缩到小窗口
	public boolean headset;//耳机是否插入
	public LinphoneCall linphoneCall;//当前来电
	public Member groupCallMember;//正在组呼的人
	public int currentCallGroupId = -1;//正在组呼的组id
	/**标记个呼来或者请求图形来，是否做了接受或拒绝的操作，默认是false*/
	public boolean isPrivateCallOrVideoLiveHand = false;
	public boolean isLocked;//当前组是否被锁定，能否切走
	//保存在警情组会话页面的nfcBean
	private NFCBean nfcBean;

	@Override
	public void onCreate() {
		if (!getPackageName().equals(getProcessName(getApplicationContext(), android.os.Process.myPid()))) {
			return;
		}
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
		setTerminalMemberType();
		catchGroupIdList = CommonGroupUtil.getCatchGroupIds();
		//保存录像，录音，照片的存储路径
		MyTerminalFactory.getSDK().getFileTransferOperation().initExternalUsableStorage();
		SkinCompatManager.withoutActivity(this)                         // 基础控件换肤初始化
				.addInflater(new SkinMaterialViewInflater())            // material design 控件换肤初始化[可选]
				//				.setSkinStatusBarColorEnable(false)                     // 关闭状态栏换肤，默认打开[可选]
				//				.setSkinWindowBackgroundEnable(false)                   // 关闭windowBackground换肤，默认打开[可选]
				.loadSkin();
        //清空刷NFC需要传的数据
        MyApplication.instance.setNfcBean(null);

	}

	public void setTerminalMemberType(){
		MyTerminalFactory.getSDK().putParam(UrlParams.TERMINALMEMBERTYPE, TerminalMemberType.TERMINAL_UAV.toString());
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
		com.secneo.sdk.Helper.install(this);
	}
	public enum TYPE{
		RECODERPUSH,
		UVCPUSH,
		PUSH ,
		PULL,
		IDLE
	}
	public void startIndividualCallService() {
		Intent intent1 = new Intent(this,ReceiveHandlerService.class);
		isBinded=bindService(intent1,conn,BIND_AUTO_CREATE);
	}
	public void stopIndividualCallService(){
		if (conn != null) {

			Log.i("服务状态1：",""+conn);
			Log.i("服务状态2：",""+isBinded);
			if (isBinded) {
				unbindService(conn);
				isBinded=false;
			}
			stopService(new Intent(this, ReceiveHandlerService.class));
		}
	}
	private ReceiveHandlerService.ReceiveHandlerBinder individualCallBinder;
	private ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			individualCallBinder = (ReceiveHandlerService.ReceiveHandlerBinder) service;
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.e("MyApplication", "IndividualCallService服务断开了");
		}
	};

	public void startUVCCameraService(){
		Intent intent = new Intent(this,UVCCameraService.class);
		isBindedUVCCameraService = bindService(intent,cameraconn,BIND_AUTO_CREATE);
	}

	public void stopUVCCameraService(){
		if(isBindedUVCCameraService){
			unbindService(cameraconn);
		}
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
			isBindedUVCCameraService = false;
		}
	};

	public NFCBean getNfcBean() {
		return nfcBean;
	}

	public void setNfcBean(NFCBean nfcBean) {
		this.nfcBean = nfcBean;
	}

	public String getProcessName(Context cxt, int pid) {
		ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
		if (am == null) {
			return null;
		}

		List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
		if (runningApps != null && !runningApps.isEmpty()) {
			for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
				if (procInfo.pid == pid) {
					return procInfo.processName;
				}
			}
		}

		return null;
	}
}

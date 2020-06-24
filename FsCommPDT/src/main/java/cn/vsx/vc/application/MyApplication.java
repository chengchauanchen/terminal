package cn.vsx.vc.application;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.github.moduth.blockcanary.BlockCanary;
import com.tencent.bugly.crashreport.CrashReport;

import org.apache.log4j.Logger;
import org.linphone.core.LinphoneCall;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.vsx.SpecificSDK.SpecificSDK;
import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenStateMachine;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakStateMachine;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallState;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallStateMachine;
import cn.vsx.hamster.terminalsdk.manager.terminal.TerminalState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePlayingState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePlayingStateMachine;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePushingState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePushingStateMachine;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.model.RecorderBindTranslateBean;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyEmergencyMessageHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.util.StateMachine.IState;
import cn.vsx.vc.BuildConfig;
import cn.vsx.vc.jump.sendMessage.ThirdSendMessage;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.service.ReceiveHandlerService;
import cn.vsx.vc.service.VideoMeetingInvitationService;
import cn.vsx.vc.service.VideoMeetingService;
import cn.vsx.vc.utils.CommonGroupUtil;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.SystemUtil;
import ptt.terminalsdk.context.BaseApplication;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ApkUtil;
import skin.support.SkinCompatManager;
import skin.support.design.app.SkinMaterialViewInflater;

public class MyApplication extends BaseApplication{


	private Logger logger = Logger.getLogger(getClass());
	public static final String TAG = "MyApplication---";
	public int mAppStatus = Constants.FORCE_KILL;//App运行状态，是否被强杀
	public boolean isBinded=false;
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
	//保存绑定账号需要传的数据
	private RecorderBindTranslateBean bindTranslateBean;
	public Activity currentActivity = null;

	@Override
	public void onCreate() {
		if (!getPackageName().equals(getProcessName(getApplicationContext(), android.os.Process.myPid()))) {
			return;
		}
		instance = this;
		super.onCreate();
		initSdk();
		registerActivityLifecycleCallbacks(new SimpleActivityLifecycle());
		catchGroupIdList = CommonGroupUtil.getCatchGroupIds();

		SkinCompatManager.withoutActivity(this)                         // 基础控件换肤初始化
				.addInflater(new SkinMaterialViewInflater())            // material design 控件换肤初始化[可选]
				//				.setSkinStatusBarColorEnable(false)                     // 关闭状态栏换肤，默认打开[可选]
				//				.setSkinWindowBackgroundEnable(false)                   // 关闭windowBackground换肤，默认打开[可选]
				.loadSkin();
        //清空刷NFC需要传的数据
        MyApplication.instance.setBindTranslateBean(null);
        // TODO 初始化 向地三方应用同步消息的service
		//判断是否是绿之云的，如果是就初始化同步消息的service
		if(ApkUtil.isWuHanPoliceStore()){
			//初始化 ThirdSendMessage
			initVsxSendMessage();
		}
		//上报图像可以不走信令
		TerminalFactory.getSDK().putParam(Params.PUSH_LIVE_NO_SINGLE,true);

		//建议在测试阶段建议设置成true，发布时设置为false
		CrashReport.initCrashReport(getApplicationContext(), "3ebac89656", true);
		int memberId = MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
		CrashReport.setUserId(memberId+"");
        if (BuildConfig.DEBUG) {
            BlockCanary.install(this, new AppBlockCanaryContext()).start();
        }
	//		if (BuildConfig.DEBUG) {
	//            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
	//                    .detectAll()
	//                    .penaltyLog()
	//                    .build());
	//            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
	//                    .detectAll()
	//                    .penaltyLog()
	//                    .build());
	//        }
	}

	protected void initSdk(){
		Log.e("MyApplication", "initSdk");
		SpecificSDK.init(this,TerminalMemberType.TERMINAL_PHONE.toString());
	}

	@Override
	public void setTerminalMemberType(){
		SpecificSDK.setTerminalMemberType(TerminalMemberType.TERMINAL_PHONE.toString());
	}

	@Override
	public void setApkType(){
		SpecificSDK.setApkType(this);
	}

	@Override
	public void setAppKey(){
		SpecificSDK.setAppKey(this);
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

	/**
	 * 获取到悬浮窗权限之后需要调用
	 */
	@Override
	public void startHandlerService() {
		Intent intent = new Intent(this,ReceiveHandlerService.class);
		isBinded=bindService(intent,conn,BIND_AUTO_CREATE);
	}

	private ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e("MyApplication", "ReceiveHandlerService bind成功");
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.e("MyApplication", "ReceiveHandlerService服务断开了");
		}
	};

	@Override
	public void stopHandlerService(){
		Log.e("MyApplication", "stopHandlerService");
		if (TerminalFactory.getSDK().isServerConnected()) {
			TerminalFactory.getSDK().disConnectToServer();
		}

		if (conn != null) {
			Log.i("conn：",""+conn);
			Log.i("isBinded：",""+isBinded);
			if (isBinded) {
				unbindService(conn);
				isBinded=false;
			}
			stopService(new Intent(this, ReceiveHandlerService.class));
		}
		TerminalFactory.getSDK().stop();
		killAllProcess();
	}

	public RecorderBindTranslateBean getBindTranslateBean() {
		return bindTranslateBean;
	}

	public void setBindTranslateBean(RecorderBindTranslateBean bindTranslateBean) {
		this.bindTranslateBean = bindTranslateBean;
	}

	@Override
	public void setAppLogined(){
		MyApplication.instance.mAppStatus = Constants.LOGINED;
	}

	@Override
	public void startPromptManager(){
		PromptManager.getInstance().start();
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

	public String getVersionName(){
		String localVersion = "";
		try{
			PackageInfo packageInfo = getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0);
			localVersion = packageInfo.versionName;
		}catch(PackageManager.NameNotFoundException e){
			e.printStackTrace();
		}
		return localVersion;
	}

	public void killAllProcess() {
		//注意：不能先杀掉主进程，否则逻辑代码无法继续执行，需先杀掉相关进程最后杀掉主进程
		ActivityManager mActivityManager = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> mList = mActivityManager.getRunningAppProcesses();
		for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : mList){
			if (runningAppProcessInfo.pid != android.os.Process.myPid()){
				android.os.Process.killProcess(runningAppProcessInfo.pid);
			}
		}
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(0);
	}

	@Override
	public void initVsxSendMessage(){
		//初始化 向地三方应用同步消息的service
		ThirdSendMessage.initVsxSendMessage(this);
		//注册 连接jumpService的广播
		ThirdSendMessage.getInstance().getRegisterBroadcastReceiver().register(this);
		ThirdSendMessage.getInstance().getRegisterBroadcastReceiver().sendBroadcast(this);
	}

	/**
	 * 停止一切业务(除了视频会议)
	 */
	public void stopAllBusiness(){
		Map<TerminalState, IState<?>>
				currentStateMap = TerminalFactory.getSDK().getTerminalStateManager().getCurrentStateMap();
		if(currentStateMap.containsKey(TerminalState.VIDEO_LIVING_PUSHING)
				||currentStateMap.containsKey(TerminalState.VIDEO_LIVING_PLAYING)
				|| currentStateMap.containsKey(TerminalState.INDIVIDUAL_CALLING)){
			TerminalFactory.getSDK().notifyReceiveHandler(ReceiveNotifyEmergencyMessageHandler.class);
		}
		if(currentStateMap.containsKey(TerminalState.GROUP_CALL_LISTENING) || currentStateMap.containsKey(TerminalState.GROUP_CALL_SPEAKING)){
			TerminalFactory.getSDK().getGroupCallManager().ceaseGroupCall();
		}
	}

	/**
	 * 检查是否在视频会议中
	 * @return
	 */
	public boolean checkVideoMeeting(){
		boolean videoMeetingServiceRunning = SystemUtil.isServiceRunning(this, VideoMeetingService.class.getName());
		boolean videoMeetingInvitationServiceRunning = SystemUtil.isServiceRunning(this, VideoMeetingInvitationService.class.getName());
		boolean isMeetingStatus = TerminalFactory.getSDK().getVideoMeetingManager().isMeetingStatus();
		logger.info(TAG+"videoMeetingServiceRunning:"+videoMeetingServiceRunning+"-isMeetingStatus:"+isMeetingStatus+"-videoMeetingInvitationServiceRunning:"+videoMeetingInvitationServiceRunning);
		if((videoMeetingServiceRunning &&isMeetingStatus)|| (videoMeetingInvitationServiceRunning)){
			return true;
		}
		return false;
	}

	/**
	 * 检查是否有业务在service中
	 * @return
	 */
	public boolean checkBusinessInServiceIsWorking(){
		if(getIndividualState() !=  IndividualCallState.IDLE
				|| getVideoLivePlayingState()!= VideoLivePlayingState.IDLE
				|| getVideoLivePushingState()!= VideoLivePushingState.IDLE
		        || checkVideoMeeting()){
			return true;
		}
		return false;
	}

}

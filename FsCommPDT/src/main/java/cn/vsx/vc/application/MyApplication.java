package cn.vsx.vc.application;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.multidex.MultiDex;
import android.util.Log;

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
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.vc.utils.CommonGroupUtil;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.IndividualCallService;
import ptt.terminalsdk.context.MyTerminalFactory;
import skin.support.SkinCompatManager;
import skin.support.design.app.SkinMaterialViewInflater;

public class MyApplication extends Application {

	public int mAppStatus = Constants.FORCE_KILL;//App运行状态，是否被强杀
	public boolean isBinded=false;
	public boolean isPttFlowPress = false;
	public boolean isIndibvidualCalling = false;
	public boolean isGroupCalling = false;
	public static boolean isNight = true;
	public boolean isContactsIndividual = false;
	public boolean helpIsFirstClick = false;
	public boolean isUpdatingAPP  = false;
	public boolean isChanging  = false;
	public boolean isScreenOff  = false;
	public boolean isLockScreenCreat = false;
	public boolean isClickVolumeToCall = false;
	public boolean isPttViewPager = true;
	public boolean isTalkbackFragment = true;
	public boolean isPttPress = false;
	public boolean isPopupWindowShow = false;
	public boolean isMoved = false;
	public boolean usbAttached;//外置摄像头是否连接
	public static List<Integer> catchGroupIdList = new ArrayList<>();
	public boolean isPlayVoice = false;
	//	public boolean isScanGroupSearch=false;//区分扫描组搜索和正常搜索
	public static MyApplication instance;
	public SpecificSDK specificSDK;

	public boolean folatWindowPress = false; //记录悬浮按钮是否按下
	public boolean volumePress = false; //记录音量是否按下
	public boolean isCallState = false; //记录主动个呼嘟嘟声

	public Member callerMember;//被叫用的主叫对象
	public Member calleeMember;//主叫用的被叫成员对象
	public boolean isInitiativeCall = false; //记录个呼是主动方还是被叫方
	public boolean isMiniLive;//直播视频缩到小窗口

	/**标记个呼来或者请求图形来，是否做了接受或拒绝的操作，默认是false*/
	public boolean isPrivateCallOrVideoLiveHand = false;

	@Override
	public void onCreate() {
		instance = this;
		super.onCreate();
		specificSDK = new SpecificSDK(this);
		MyTerminalFactory.setTerminalSDK(specificSDK);
		MyTerminalFactory.getSDK().setLoginFlag();
		registerActivityLifecycleCallbacks(new SimpleActivityLifecycle());

		//开启voip电话服务
		MyTerminalFactory.getSDK().getVoipCallManager().startService(getApplicationContext());
		MyTerminalFactory.getSDK().putParam(UrlParams.TERMINALMEMBERTYPE, TerminalMemberType.TERMINAL_PHONE.toString());
		catchGroupIdList = CommonGroupUtil.getCatchGroupIds();
		//		SkinCompatManager.
		//				init(this)
		//				.addInflater(new SkinMaterialViewInflater())    // material design
		//				.addInflater(new SkinCardViewInflater())        // CardView v7
		//				.addInflater(new SkinCircleImageViewInflater()) // hdodenhof/CircleImageView
		//				.loadSkin();

		SkinCompatManager.withoutActivity(this)                         // 基础控件换肤初始化
				.addInflater(new SkinMaterialViewInflater())            // material design 控件换肤初始化[可选]
				//				.setSkinStatusBarColorEnable(false)                     // 关闭状态栏换肤，默认打开[可选]
				//				.setSkinWindowBackgroundEnable(false)                   // 关闭windowBackground换肤，默认打开[可选]
				.loadSkin();

		//		if (LeakCanary.isInAnalyzerProcess(this)) {
		//			// This process is dedicated to LeakCanary for heap analysis.
		//			// You should not init your app in this process.
		//			return;
		//		}
		//		LeakCanary.install(this);
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
	public enum TYPE{
		UVCPUSH,
		PUSH ,
		PULL,
		IDLE
	}
	public void startIndividualCallService() {
		Intent intent1 = new Intent(this,IndividualCallService.class);
		isBinded=bindService(intent1,conn,BIND_AUTO_CREATE);
	}
	public void stopIndividualCallService(){
		if (conn != null) {
			if (individualCallBinder != null) {
				individualCallBinder.removeServiceView();
			}
			Log.i("服务状态1：",""+conn);
			Log.i("服务状态2：",""+isBinded);
			if (isBinded) {
				unbindService(conn);
				isBinded=false;
			}
			stopService(new Intent(this, IndividualCallService.class));
		}
	}
	private IndividualCallService.IndividualCallBinder individualCallBinder;
	private ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			individualCallBinder = (IndividualCallService.IndividualCallBinder) service;
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

}

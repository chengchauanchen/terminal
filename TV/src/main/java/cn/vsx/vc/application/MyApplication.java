package cn.vsx.vc.application;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.multidex.MultiDex;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

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
import cn.vsx.vc.utils.CommonGroupUtil;
import cn.vsx.vc.utils.Constants;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.context.TerminalSDK4Android;

public class MyApplication extends Application {

	public int mAppStatus = Constants.FORCE_KILL;//App运行状态，是否被强杀
	public boolean isUpdatingAPP  = false;
	public static List<Integer> catchGroupIdList = new ArrayList<>();
	public static MyApplication instance;
	public TerminalSDK4Android terminalSDK4Android;
	public SpecificSDK specificSDK;
	Logger logger = Logger.getLogger(getClass().getName());
	public boolean folatWindowPress = false; //记录悬浮按钮是否按下
	public boolean volumePress = false; //记录音量是否按下
	public boolean isClickVolumeToCall = false;
	public boolean isPttPress = false;
	public boolean isMiniLive;//直播视频缩到小窗口
	public boolean headset;//耳机是否插入
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		specificSDK = new SpecificSDK(this);
		MyTerminalFactory.setTerminalSDK(specificSDK);
		MyTerminalFactory.getSDK().setLoginFlag();
		registerActivityLifecycleCallbacks(new SimpleActivityLifecycle());
		try{
			ApplicationInfo appInfo = this.getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
			String apkType=appInfo.metaData.getString("APKTYPE");
			Log.e("MyApplication", " APKTYPE == " + apkType);
			MyTerminalFactory.getSDK().putParam(Params.APK_TYPE,apkType);
		}catch(PackageManager.NameNotFoundException e){
			e.printStackTrace();
		}
		MyTerminalFactory.getSDK().getAuthManagerTwo().initIp();
		MyTerminalFactory.getSDK().putParam(UrlParams.TERMINALMEMBERTYPE, TerminalMemberType.TERMINAL_HDMI.toString());
		catchGroupIdList = CommonGroupUtil.getCatchGroupIds();
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}

	public SpecificSDK getSpecificSDK(){
		return specificSDK;
	}

	public TerminalSDK4Android getTerminalSDK4Android(){
		return terminalSDK4Android;
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

	public enum TYPE{
		RECODERPUSH,
		UVCPUSH,
		PUSH ,
		PULL,
		IDLE
	}
}

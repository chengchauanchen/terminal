package cn.vsx.vc.prompt;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.SystemClock;
import android.os.Vibrator;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import cn.vsx.hamster.common.CallMode;
import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.SignalServerErrorCode;
import cn.vsx.hamster.protolbuf.PTTProtolbuf;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePlayingState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePushingState;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveChangeGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetRtspStreamUrlHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberDeleteHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyInviteToWatchHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyMemberKilledHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRequestGroupCallConformationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseStartIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveServerConnectionEstablishedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveStartCeaseGroupCallHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveTalkWillTimeoutHandler;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.utils.DataUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * 提示管理器
 */
public class PromptManager {
	private Logger logger = Logger.getLogger(getClass());
	/** 组呼成功 */
	private long[] requestGroupCallOk = {0,200};   // 组呼成功
	/**通话即将结束 */
	private long[] talkWillTimeout = {0,400};   // 通话即将结束
	private SoundPool soundPool;
	private Map<Integer, Integer> soundMap = new HashMap<>();
	private static PromptManager instance;
	private int streamId;

	/**组成员遥毙消息*/
	private ReceiveNotifyMemberKilledHandler receiveNotifyMemberKilledHandler = forbid -> {
		logger.info("PromptManager收到遥毙，此时forbid状态为：" + forbid);
		if(forbid){
			stopRing();
		}
	};

	//成员被删除了
	private ReceiveMemberDeleteHandler receiveMemberDeleteHandler = () -> {
		logger.info("PromptManager收到删除消息");
		stopRing();
	};

	private ReceiveServerConnectionEstablishedHandler receiveServerConnectionEstablishedHandler = connected -> {
		logger.info("PromptManager收到网络状态通知：connected = "+connected);
		if (!connected) {
			stopRing();
		}
	};

	private ReceiveRequestGroupCallConformationHandler receiveRequestGroupCallConformationHandler = new ReceiveRequestGroupCallConformationHandler() {

		@Override
		public void handler(int methodResult, String resultDesc) {
			logger.info("界面声音管理类，开始组呼，提示音播放");
			if(MyApplication.instance.isPttPress){
				if(methodResult == BaseCommonCode.SUCCESS_CODE){
					//组呼成功，发出提示音
					vibrator = (Vibrator) MyTerminalFactory.getSDK().getApplication().getSystemService(Context.VIBRATOR_SERVICE);
					vibrator.vibrate(requestGroupCallOk,-1);
					cancelVibrator();
					if(soundPool != null){
						streamId =soundPool.play(soundMap.get(R.raw.request_call_ok), 0.5f, 0.5f, 0, 0, 1);
					}
				}else if(methodResult == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode() ||
						methodResult == SignalServerErrorCode.GROUP_CALL_IS_CANCELED.getErrorCode()){
					//不需要播声音
				}else{
					//组呼失败，发提示音
					if(soundPool != null){
						streamId =soundPool.play(soundMap.get(R.raw.request_call_fail), 0.5f, 0.5f, 0, 0, 1);
					}
				}
			}
		}
	};

	private ReceiveStartCeaseGroupCallHandler receiveStartCeaseGroupCallHandler = new ReceiveStartCeaseGroupCallHandler() {
		@Override
		public void handler(boolean isCalled) {
			if(soundPool != null){

				if (isCalled){
					streamId =soundPool.play(soundMap.get(R.raw.cease_call), 0.5f, 0.5f, 0, 0, 1);
				}else {
					streamId =soundPool.play(soundMap.get(ptt.terminalsdk.R.raw.request_call_ok), 0.5f, 0.5f, 0, 0, 1);

				}

			}
		}
	};



	private ReceiveGroupCallIncommingHandler receiveGroupCallIncommingHandler = new ReceiveGroupCallIncommingHandler() {
		@Override
		public void handler(final int memberId, final String memberName, final int groupId,
							String version, final CallMode currentCallMode) {
			TerminalMemberType terminalMemberTypeEnum = DataUtil.getMemberByMemberNo(memberId).getTerminalMemberTypeEnum();
			if (terminalMemberTypeEnum == TerminalMemberType.TERMINAL_PDT){
				if(soundPool != null){
					streamId =soundPool.play(soundMap.get(ptt.terminalsdk.R.raw.request_call_ok), 0.5f, 0.5f, 0, 0, 1);
				}
			}
		}
	};

	private ReceiveChangeGroupHandler receiveChangeGroupHandler = new ReceiveChangeGroupHandler() {
		@Override
		public void handler(int errorCode, String errorDesc) {
			if(errorCode == BaseCommonCode.SUCCESS_CODE || errorCode == SignalServerErrorCode.INVALID_SWITCH_GROUP.getErrorCode()){
				if(soundPool != null && MyApplication.instance.getIndividualState() == IndividualCallState.IDLE
						&& MyApplication.instance.getVideoLivePushingState() == VideoLivePushingState.IDLE
						&& MyApplication.instance.getVideoLivePlayingState() == VideoLivePlayingState.IDLE){
					streamId =soundPool.play(soundMap.get(R.raw.change_group_ok), 0.5f, 0.5f, 0, 0, 1);
				}
			}
		}
	};
	/**还有10秒超时，振动提醒*/
	private ReceiveTalkWillTimeoutHandler receiveTalkWillTimeoutHandler = new ReceiveTalkWillTimeoutHandler() {
		@Override
		public void handler() {
			vibrator = (Vibrator) MyTerminalFactory.getSDK().getApplication().getSystemService(Context.VIBRATOR_SERVICE);
			logger.info("组呼还有10秒超时，振动提醒");
			vibrator.vibrate(talkWillTimeout,-1);
			cancelVibrator();
		}
	};

	/**获取到rtsp地址，开始播放视频*/
	private ReceiveGetRtspStreamUrlHandler receiveGetRtspStreamUrlHandler = new ReceiveGetRtspStreamUrlHandler() {
		@Override
		public void handler(final String rtspUrl, Member liveMember,long callId) {
			if (!TextUtils.isEmpty(rtspUrl)) {
				vibrator = (Vibrator) MyTerminalFactory.getSDK().getApplication().getSystemService(Context.VIBRATOR_SERVICE);
				logger.info("请求别人视频直播成功，振动");
				vibrator.vibrate(new long[]{0, 200},-1);
				cancelVibrator();
			}
		}
	};

	/**主动方请求个呼开始*/
	private ReceiveResponseStartIndividualCallHandler receiveResponseStartIndividualCallHandler = new ReceiveResponseStartIndividualCallHandler() {
		@Override
		public void handler(final int resultCode, String resultDesc, int individualCallType) {
			if (resultCode == BaseCommonCode.SUCCESS_CODE) {//对方接听
				vibrator = (Vibrator) MyTerminalFactory.getSDK().getApplication().getSystemService(Context.VIBRATOR_SERVICE);
				logger.info("对方接受了你的个呼, 振动");
				vibrator.vibrate(new long[]{0, 200}, -1);
				cancelVibrator();
			}
		}
	};

	private void cancelVibrator() {
		MyTerminalFactory.getSDK().getTimer().schedule(new TimerTask() {
			@Override
			public void run() {
				vibrator.cancel();
			}
		}, 1000);
	}

	private Vibrator vibrator;
	/**收到通知，邀请自己去观看直播*/
	private ReceiveNotifyInviteToWatchHandler receiveNotifyInviteToWatchHandler = new ReceiveNotifyInviteToWatchHandler() {
		@Override
		public void handler(final PTTProtolbuf.NotifyDataMessage message) {
			if(soundPool != null && MyApplication.instance.getIndividualState() == IndividualCallState.IDLE
					&& MyApplication.instance.getVideoLivePlayingState() == VideoLivePlayingState.IDLE
					&& MyApplication.instance.getVideoLivePushingState() == VideoLivePushingState.IDLE){
				if (JSONObject.parseObject(message.getMessageBody()).getIntValue("remark") == 2) {//Remark为通知观看时，才响铃
					streamId =soundPool.play(soundMap.get(R.raw.request_call_ok), 0.5f, 0.5f, 0, 0, 1);
					logger.info("被叫收到邀请自己去观看直播的通知，开始响铃----------");
				}
			}
		}
	};
	public void IndividualCallRequestRing(){
		if(soundPool != null){
			streamId = soundPool.play(soundMap.get(R.raw.request_call_wait), 1, 1, 0, -1, 1);
			logger.info("主动请求个呼，开始响铃----------dududududu"+streamId);
		}
	}
	public void IndividualCallNotifyRing(){
		if(soundPool != null){
			streamId = soundPool.play(soundMap.get(R.raw.notific_call_coming), 1, 1, 0, -1, 1);
			logger.info("被叫收到个呼来，开始响铃-----------dengdengdengdengdeng"+streamId);
		}
	}
	//个呼异常提示音响铃
	public void IndividualHangUpRing(){
		if(soundPool != null){
			streamId = soundPool.play(soundMap.get(R.raw.passive_dropped_warning),1,1,0,-1,1);
			logger.info("个呼异常提示响铃——————dandandan" + streamId);
		}
	}

	/**别人邀请视频的响铃**/
	public void VideoLiveInCommimgRing(){
		if(soundPool != null){
			streamId = soundPool.play(soundMap.get(R.raw.notific_videolive_coming),1,1,0,-1,1);
			logger.info("被动方视频来了，开始响铃" + streamId);
		}
	}

	/**
	 * 组呼到来声音
	 */
	public void groupCallCommingRing(){
		if(soundMap !=null){
			streamId =soundPool.play(soundMap.get(R.raw.receive_group_comming), 0.5f, 0.5f, 0, 0, 1);
		}
	}

	public void stopRing(){
		if(soundPool != null){
			logger.info("停止响铃---------->" + streamId);
			if(streamId !=0){
				soundPool.stop(streamId);
				streamId = 0;
			}
		}
	}

	public void delayedStopRing(){
		if(soundPool != null){
			logger.info("3秒后停止响铃---------->" + streamId);
			SystemClock.sleep(3000);
			stopRing();
		}
	}

	private PromptManager(){}

	public static PromptManager getInstance(){
		if(instance == null){
			instance = new PromptManager();
		}
		return instance;
	}


	public void start(){
		logger.info("----------提示音管理类 start()------------");
		soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		soundMap.clear();
		soundMap.put(ptt.terminalsdk.R.raw.ppt_up, soundPool.load(MyApplication.instance,ptt.terminalsdk.R.raw.ppt_up, 1));

		soundMap.put(R.raw.notific_call_coming, soundPool.load(MyApplication.instance, R.raw.notific_call_coming, 1));
		soundMap.put(R.raw.notific_videolive_coming,soundPool.load(MyApplication.instance,R.raw.notific_videolive_coming,1));
		soundMap.put(R.raw.request_call_ok, soundPool.load(MyApplication.instance, R.raw.request_call_ok, 1));
		soundMap.put(R.raw.request_call_fail, soundPool.load(MyApplication.instance, R.raw.request_call_fail, 1));
		soundMap.put(R.raw.cease_call, soundPool.load(MyApplication.instance, R.raw.cease_call, 1));
		soundMap.put(R.raw.receive_group_comming,soundPool.load(MyApplication.instance,R.raw.receive_group_comming,1));
		soundMap.put(R.raw.change_group_ok, soundPool.load(MyApplication.instance, R.raw.change_group_ok, 1));
		soundMap.put(R.raw.request_call_wait, soundPool.load(MyApplication.instance, R.raw.request_call_wait, 1));
		soundMap.put(R.raw.passive_dropped_warning,soundPool.load(MyApplication.instance,R.raw.passive_dropped_warning,1));
		MyTerminalFactory.getSDK().registReceiveHandler(receiveChangeGroupHandler);
		MyTerminalFactory.getSDK().registReceiveHandler(receiveServerConnectionEstablishedHandler);
		MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseStartIndividualCallHandler);
		MyTerminalFactory.getSDK().registReceiveHandler(receiveGetRtspStreamUrlHandler);
		MyTerminalFactory.getSDK().registReceiveHandler(receiveMemberDeleteHandler);
		MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyMemberKilledHandler);
		MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallIncommingHandler);
	}

	public void stop(){
		logger.info("----------提示音管理类 stop()------------");
		MyTerminalFactory.getSDK().unregistReceiveHandler(receiveChangeGroupHandler);
		MyTerminalFactory.getSDK().unregistReceiveHandler(receiveServerConnectionEstablishedHandler);
		MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseStartIndividualCallHandler);
		MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetRtspStreamUrlHandler);
		MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMemberDeleteHandler);
		MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyMemberKilledHandler);
		MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallIncommingHandler);

		soundMap.clear();
		if(soundPool != null){
			soundPool.release();
			soundPool = null;
		}
	}
}

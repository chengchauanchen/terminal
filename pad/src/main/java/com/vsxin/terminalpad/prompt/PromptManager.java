package com.vsxin.terminalpad.prompt;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.SystemClock;
import android.os.Vibrator;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.app.PadApplication;
import com.vsxin.terminalpad.receiveHandler.ReceiverRequestLteBullHandler;
import com.vsxin.terminalpad.utils.OperateReceiveHandlerUtilSync;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.CallMode;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.SignalServerErrorCode;
import cn.vsx.hamster.protolbuf.PTTProtolbuf;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
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
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveOnLineStatusChangedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRequestGroupCallConformationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseStartIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveStartCeaseGroupCallHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveTalkWillTimeoutHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

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

	private ReceiveOnLineStatusChangedHandler receiveOnLineStatusChangedHandler = connected -> {
		logger.info("PromptManager收到网络状态通知：connected = "+connected);
		if (!connected) {
			stopRing();
		}
	};

	private ReceiveRequestGroupCallConformationHandler receiveRequestGroupCallConformationHandler = new ReceiveRequestGroupCallConformationHandler() {

		@Override
		public void handler(int methodResult, String resultDesc, int groupId) {
			logger.info("界面声音管理类，开始组呼，提示音播放");
			if(PadApplication.getPadApplication().isPttPress){
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
                            String version, final CallMode currentCallMode, long uniqueNo) {
			TerminalFactory.getSDK().getThreadPool().execute(new Runnable(){
				@Override
				public void run(){
					if(DataUtil.isPDTMember(memberId)){
						if(soundPool != null){
							streamId =soundPool.play(soundMap.get(ptt.terminalsdk.R.raw.request_call_ok), 0.5f, 0.5f, 0, 0, 1);
						}
					}
				}
			});

		}
	};

	private ReceiveChangeGroupHandler receiveChangeGroupHandler = new ReceiveChangeGroupHandler() {
		@Override
		public void handler(int errorCode, String errorDesc) {
			if(errorCode == BaseCommonCode.SUCCESS_CODE || errorCode == SignalServerErrorCode.INVALID_SWITCH_GROUP.getErrorCode()){
				if(soundPool != null && PadApplication.getPadApplication().getIndividualState() == IndividualCallState.IDLE
						&& PadApplication.getPadApplication().getVideoLivePushingState() == VideoLivePushingState.IDLE
						&& PadApplication.getPadApplication().getVideoLivePlayingState() == VideoLivePlayingState.IDLE){
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
		public void handler(final String rtspUrl, Member liveMember, long callId) {
			if (!TextUtils.isEmpty(rtspUrl)) {
				vibrator = (Vibrator) MyTerminalFactory.getSDK().getApplication().getSystemService(Context.VIBRATOR_SERVICE);
				logger.info("请求别人视频直播成功，振动");
				vibrator.vibrate(new long[]{0, 200},-1);
				cancelVibrator();
			}
		}
	};

	/**
	 * 拉取不控球视频
	 */
	private ReceiverRequestLteBullHandler receiverRequestLteBullHandler = new ReceiverRequestLteBullHandler() {
		@Override
		public void handler(String rtspUrl, String type, String title) {
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
			if(soundPool != null && PadApplication.getPadApplication().getIndividualState() == IndividualCallState.IDLE
					&& PadApplication.getPadApplication().getVideoLivePlayingState() == VideoLivePlayingState.IDLE
					&& PadApplication.getPadApplication().getVideoLivePushingState() == VideoLivePushingState.IDLE){
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
			streamId = soundPool.play(soundMap.get(R.raw.passive_dropped_warning),1,1,0,0,1);
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

	/**
	 * 存储空间严重不足
	 */
	public void startExternNoStorage(){
		if(soundPool != null){
			logger.info("存储空间严重不足！");
			streamId = soundPool.play(soundMap.get(R.raw.exten_no_storage), 0.5f, 0.5f, 0, 0, 1);
		}
	}

	/**
	 * 存储空间不足
	 */
	public void startExternStorageNotEnough(){
		if(soundPool != null){
			logger.info("存储空间不足！");
			streamId = soundPool.play(soundMap.get(R.raw.exten_storage_not_engou), 0.5f, 0.5f, 0, 0, 1);
		}
	}

	/**
	 * 收到请求上报的通知开始上报
	 */
	public void startReportByNotity(){
		if(soundPool != null){
			logger.info("开始被动上报！");
			streamId = soundPool.play(soundMap.get(R.raw.start_report_by_notify), 0.5f, 0.5f, 0, 0, 1);
		}
	}

	/**
	 * 收到请求观看的通知
	 */
	public void startPlayByNotity(){
		if(soundPool != null){
			logger.info("开始被动上报！");
			streamId = soundPool.play(soundMap.get(R.raw.start_play_by_notify), 0.5f, 0.5f, 0, 0, 1);
		}
	}

	/**
	 * 临时组到期
	 */
	public void playTempGroupExpire(){
		if(soundPool != null){
			streamId = soundPool.play(soundMap.get(R.raw.temp_group_expire), 0.5f, 0.5f, 0, 0, 1);
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
		soundMap.put(ptt.terminalsdk.R.raw.ppt_up, soundPool.load(PadApplication.getPadApplication(),ptt.terminalsdk.R.raw.ppt_up, 1));

		soundMap.put(R.raw.notific_call_coming, soundPool.load(PadApplication.getPadApplication(), R.raw.notific_call_coming, 1));
		soundMap.put(R.raw.notific_videolive_coming,soundPool.load(PadApplication.getPadApplication(),R.raw.notific_videolive_coming,1));
		soundMap.put(R.raw.request_call_ok, soundPool.load(PadApplication.getPadApplication(), R.raw.request_call_ok, 1));
		soundMap.put(R.raw.request_call_fail, soundPool.load(PadApplication.getPadApplication(), R.raw.request_call_fail, 1));
		soundMap.put(R.raw.cease_call, soundPool.load(PadApplication.getPadApplication(), R.raw.cease_call, 1));
		soundMap.put(R.raw.receive_group_comming,soundPool.load(PadApplication.getPadApplication(),R.raw.receive_group_comming,1));
		soundMap.put(R.raw.change_group_ok, soundPool.load(PadApplication.getPadApplication(), R.raw.change_group_ok, 1));
		soundMap.put(R.raw.request_call_wait, soundPool.load(PadApplication.getPadApplication(), R.raw.request_call_wait, 1));
		soundMap.put(R.raw.passive_dropped_warning,soundPool.load(PadApplication.getPadApplication(),R.raw.passive_dropped_warning,1));
		soundMap.put(R.raw.exten_no_storage, soundPool.load(PadApplication.getPadApplication(), R.raw.exten_no_storage,1));
		soundMap.put(R.raw.exten_storage_not_engou, soundPool.load(PadApplication.getPadApplication(), R.raw.exten_storage_not_engou,1));
		soundMap.put(R.raw.start_report_by_notify, soundPool.load(PadApplication.getPadApplication(), R.raw.start_report_by_notify,1));
		soundMap.put(R.raw.start_play_by_notify, soundPool.load(PadApplication.getPadApplication(), R.raw.start_play_by_notify,1));
		soundMap.put(R.raw.temp_group_expire, soundPool.load(PadApplication.getPadApplication(), R.raw.temp_group_expire,10));
		MyTerminalFactory.getSDK().registReceiveHandler(receiveChangeGroupHandler);
		MyTerminalFactory.getSDK().registReceiveHandler(receiveOnLineStatusChangedHandler);
		MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseStartIndividualCallHandler);
		MyTerminalFactory.getSDK().registReceiveHandler(receiveGetRtspStreamUrlHandler);
		MyTerminalFactory.getSDK().registReceiveHandler(receiveMemberDeleteHandler);
		MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyMemberKilledHandler);
		MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallIncommingHandler);

		//拉取不控球视频
		OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiverRequestLteBullHandler);
	}

	public void stop(){
		logger.info("----------提示音管理类 stop()------------");
		MyTerminalFactory.getSDK().unregistReceiveHandler(receiveChangeGroupHandler);
		MyTerminalFactory.getSDK().unregistReceiveHandler(receiveOnLineStatusChangedHandler);
		MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseStartIndividualCallHandler);
		MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetRtspStreamUrlHandler);
		MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMemberDeleteHandler);
		MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyMemberKilledHandler);
		MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallIncommingHandler);

		//拉取不控球视频
		OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiverRequestLteBullHandler);

		soundMap.clear();
		if(soundPool != null){
			soundPool.release();
			soundPool = null;
		}
	}
}

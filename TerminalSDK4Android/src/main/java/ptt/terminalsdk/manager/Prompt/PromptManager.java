package ptt.terminalsdk.manager.Prompt;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Vibrator;

import com.alibaba.fastjson.JSONObject;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.SignalServerErrorCode;
import cn.vsx.hamster.protolbuf.PTTProtolbuf;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyIndividualCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyInviteToWatchHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyLivingIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRequestGroupCallConformationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveServerConnectionEstablishedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveStartCeaseGroupCallHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveTalkWillTimeoutHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.R;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * 提示管理器
 */
public class PromptManager {
	private Logger logger = Logger.getLogger(getClass());
	/** 组呼成功 */
	private long[] requestGroupCallOk = {0,200};   // 组呼成功
	private long[] requestCall = {0,200,50*1000,200};   // 组呼成功
	/**通话即将结束 */
	private long[] talkWillTimeout = {0,400};   // 通话即将结束
	private SoundPool soundPool;
	private Map<Integer, Integer> soundMap = new HashMap<>();
	private static PromptManager instance;
	private int streamId;
    private Context context;


	private ReceiveServerConnectionEstablishedHandler receiveServerConnectionEstablishedHandler = new ReceiveServerConnectionEstablishedHandler() {
		@Override
		public void handler(boolean connected) {
			logger.info("PromptManager收到网络状态通知：connected = " + connected);
			if (!connected && soundPool != null) {
				soundPool.stop(streamId);
			}
		}
	};

	private ReceiveNotifyIndividualCallIncommingHandler receiveNotifyIndividualCallIncommingHandler = new ReceiveNotifyIndividualCallIncommingHandler() {
		@Override
		public void handler(String mainMemberName, int mainMemberId, int individualCallType) {
			logger.info("个呼来了，通知界面，打开应用");
			if(!TerminalFactory.getSDK().getParam(Params.IS_EXIT,false)){
				Intent intent = new Intent("android.intent.action.OPEN_APP");
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
				context.sendBroadcast(intent);
			}

		}
	};

	private ReceiveNotifyLivingIncommingHandler receiveNotifyLivingIncommingHandler = new ReceiveNotifyLivingIncommingHandler() {
		@Override
		public void handler(String mainMemberName, int mainMemberId,boolean emergencyType) {
			logger.info("直播来了，通知界面，打开应用");
			if(!TerminalFactory.getSDK().getParam(Params.IS_EXIT,false)){
				Intent intent = new Intent("android.intent.action.OPEN_APP");
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
				context.sendBroadcast(intent);
			}
		}
	};

	private ReceiveRequestGroupCallConformationHandler receiveRequestGroupCallConformationHandler = new ReceiveRequestGroupCallConformationHandler() {
		@Override
		public void handler(int methodResult, String resultDesc) {
			logger.info("主动发起组呼，开始响起提示音");
			if(methodResult == BaseCommonCode.SUCCESS_CODE){
				//组呼成功，发出提示音
				if (vibrator != null){
					vibrator.vibrate(requestCall,-1);
				}
				if(soundPool != null){
					soundPool.play(soundMap.get(R.raw.request_call_ok_new), 0.5f, 0.5f, 0, 0, 1);
				}
			}else if(methodResult == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode() ||
					methodResult == SignalServerErrorCode.GROUP_CALL_IS_CANCELED.getErrorCode()){
				//不需要播声音
			}else{
				//组呼失败，发提示音
				if(soundPool != null){
					soundPool.play(soundMap.get(R.raw.request_call_fail), 0.5f, 0.5f, 0, 0, 1);
				}
			}
		}

	};


	private ReceiveStartCeaseGroupCallHandler receiveStartCeaseGroupCallHandler = new ReceiveStartCeaseGroupCallHandler() {
		@Override
		public void handler(boolean isCalled) {
			if(!TerminalFactory.getSDK().getParam(Params.IS_EXIT,false)){
				if(soundPool != null){
					if (isCalled) {
						soundPool.play(soundMap.get(R.raw.cease_call), 0.5f, 0.5f, 0, 0, 1);
					}else {
						soundPool.play(soundMap.get(R.raw.request_call_ok), 0.5f, 0.5f, 0, 0, 1);

					}
				}
			}
		}
	};

	/**还有10秒超时，振动提醒*/
	private ReceiveTalkWillTimeoutHandler receiveTalkWillTimeoutHandler = new ReceiveTalkWillTimeoutHandler() {
		@Override
		public void handler() {
			logger.info("组呼还有10秒超时，振动提醒");
			if (vibrator != null)
	        	vibrator.vibrate(talkWillTimeout,-1);
		}
	};

	private Vibrator vibrator;
	/**收到通知，邀请自己去观看直播*/
	private ReceiveNotifyInviteToWatchHandler receiveNotifyInviteToWatchHandler = new ReceiveNotifyInviteToWatchHandler() {
		@Override
		public void handler(final PTTProtolbuf.NotifyDataMessage message) {

			if (soundPool != null && JSONObject.parseObject(message.getMessageBody()).getIntValue("remark") == 2) {//Remark为通知观看时，才响铃
				soundPool.play(soundMap.get(R.raw.request_call_ok), 0.5f, 0.5f, 0, 0, 1);
				logger.info("被叫收到邀请自己去观看直播的通知，开始响铃----------");
			}

		}
	};

	private PromptManager(){}

	public static PromptManager getInstance(){
		if(instance == null){
			instance = new PromptManager();
		}
		return instance;
	}


	public void start(Context context){
        this.context = context;
		vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		logger.info("----------sdk提示音管理类 start()------------");
		soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		soundMap.clear();
		soundMap.put(R.raw.request_call_ok_new, soundPool.load(context, R.raw.request_call_ok_new, 1));
		soundMap.put(R.raw.ppt_up, soundPool.load(context,R.raw.ppt_up, 1));

		soundMap.put(R.raw.receive_group_comming, soundPool.load(context, R.raw.receive_group_comming, 1));
		soundMap.put(R.raw.request_call_ok, soundPool.load(context, R.raw.request_call_ok, 1));
		soundMap.put(R.raw.request_call_fail, soundPool.load(context, R.raw.request_call_fail, 1));
		soundMap.put(R.raw.cease_call, soundPool.load(context, R.raw.cease_call, 1));
		MyTerminalFactory.getSDK().registReceiveHandler(receiveRequestGroupCallConformationHandler);
		MyTerminalFactory.getSDK().registReceiveHandler(receiveStartCeaseGroupCallHandler);
		MyTerminalFactory.getSDK().registReceiveHandler(receiveTalkWillTimeoutHandler);
		MyTerminalFactory.getSDK().registReceiveHandler(receiveServerConnectionEstablishedHandler);
		MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyInviteToWatchHandler);
		MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyIndividualCallIncommingHandler);
		MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyLivingIncommingHandler);
	}

	//半双工个呼主动按下
	public void playPrompt(){
		//组呼成功，发出提示音
		if (vibrator != null)
			vibrator.vibrate(requestGroupCallOk,-1);
		if(soundPool != null){
			soundPool.play(soundMap.get(R.raw.request_call_ok), 0.5f, 0.5f, 0, 0, 1);
		}
	}

	//半双工被叫
	public void playPromptCalled(){
		//组呼成功，发出提示音
		if (vibrator != null)
			vibrator.vibrate(requestGroupCallOk,-1);
		if(soundPool != null){
			soundPool.play(soundMap.get(R.raw.receive_group_comming), 0.5f, 0.5f, 0, 0, 1);
		}
	}


	public void stop(){
		logger.info("----------sdk提示音管理类 stop()------------");
		MyTerminalFactory.getSDK().unregistReceiveHandler(receiveRequestGroupCallConformationHandler);
		MyTerminalFactory.getSDK().unregistReceiveHandler(receiveStartCeaseGroupCallHandler);
		MyTerminalFactory.getSDK().unregistReceiveHandler(receiveTalkWillTimeoutHandler);
		MyTerminalFactory.getSDK().unregistReceiveHandler(receiveServerConnectionEstablishedHandler);
		MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyInviteToWatchHandler);
		MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyLivingIncommingHandler);
		MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyIndividualCallIncommingHandler);

		soundMap.clear();
		if(soundPool != null){
			soundPool.release();
			soundPool = null;
		}
		if (vibrator != null)
			vibrator.cancel();
	}
}

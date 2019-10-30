package cn.vsx.vc.prompt;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Vibrator;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.SignalServerErrorCode;
import cn.vsx.hamster.protolbuf.PTTProtolbuf;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePlayingState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePushingState;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveChangeGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetRtspStreamUrlHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberDeleteHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyInviteToWatchHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyMemberKilledHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveOnLineStatusChangedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRequestGroupCallConformationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseStartIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveStartCeaseGroupCallHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveTalkWillTimeoutHandler;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * 提示管理器
 */
public class PromptManager {
    private Logger logger = Logger.getLogger(getClass());
    /**
     * 组呼成功
     */
    private long[] requestGroupCallOk = {0, 200};   // 组呼成功
    /**
     * 通话即将结束
     */
    private long[] talkWillTimeout = {0, 400};   // 通话即将结束
    private SoundPool soundPool;
    private Map<Integer, Integer> soundMap = new HashMap<>();
    private static PromptManager instance;
    private int streamId;

    /**
     * 组成员遥毙消息
     */
    private ReceiveNotifyMemberKilledHandler receiveNotifyMemberKilledHandler = new ReceiveNotifyMemberKilledHandler() {
        @Override
        public void handler(boolean forbid) {
            logger.info("PromptManager收到遥毙，此时forbid状态为：" + forbid);
            if (forbid) {
                if (soundPool != null) {
                    soundPool.stop(streamId);
                }
            }
        }
    };

    //成员被删除了
    private ReceiveMemberDeleteHandler receiveMemberDeleteHandler = new ReceiveMemberDeleteHandler() {
        @Override
        public void handler() {
            logger.info("PromptManager收到删除消息");
            if (soundPool != null) {
                soundPool.stop(streamId);
            }
        }
    };

    private ReceiveOnLineStatusChangedHandler receiveOnLineStatusChangedHandler = new ReceiveOnLineStatusChangedHandler() {
        @Override
        public void handler(boolean connected) {
            logger.info("PromptManager收到网络状态通知：connected = " + connected);
            if (!connected && soundPool != null) {
                soundPool.stop(streamId);
            }
        }
    };

    private ReceiveRequestGroupCallConformationHandler receiveRequestGroupCallConformationHandler = new ReceiveRequestGroupCallConformationHandler() {

        @Override
        public void handler(int methodResult, String resultDesc, int groupId) {
            logger.info("界面声音管理类，开始组呼，提示音播放");
            if (MyApplication.instance.isPttPress) {
                if (methodResult == BaseCommonCode.SUCCESS_CODE) {
                    //组呼成功，发出提示音
                    vibrator = (Vibrator) MyTerminalFactory.getSDK().getApplication().getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(requestGroupCallOk, -1);
                    cancelVibrator();
                    if (soundPool != null) {
                        soundPool.play(soundMap.get(R.raw.request_call_ok), 0.5f, 0.5f, 0, 0, 1);
                    }
                } else if (methodResult == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode() ||
                        methodResult == SignalServerErrorCode.GROUP_CALL_IS_CANCELED.getErrorCode()) {
                    //不需要播声音
                } else {
                    //组呼失败，发提示音
                    if (soundPool != null) {
                        soundPool.play(soundMap.get(R.raw.request_call_fail), 0.5f, 0.5f, 0, 0, 1);
                    }
                }
            }
        }
    };

    private ReceiveStartCeaseGroupCallHandler receiveStartCeaseGroupCallHandler = new ReceiveStartCeaseGroupCallHandler() {
        @Override
        public void handler(boolean isCalled) {
            if (soundPool != null) {

                if (isCalled) {
                    soundPool.play(soundMap.get(R.raw.cease_call), 0.5f, 0.5f, 0, 0, 1);
                } else {
                    soundPool.play(soundMap.get(ptt.terminalsdk.R.raw.request_call_ok), 0.5f, 0.5f, 0, 0, 1);

                }

            }
        }
    };


//	private ReceiveGroupCallIncommingHandler receiveGroupCallIncommingHandler = new ReceiveGroupCallIncommingHandler() {
//		@Override
//		public void handler(final int memberId, final String memberName, final int groupId,
//							String version, final CallMode currentCallMode) {
//			TerminalMemberType terminalMemberTypeEnum = DataUtil.getMemberByMemberNo(memberId).getTerminalMemberTypeEnum();
//			if (terminalMemberTypeEnum == TerminalMemberType.TERMINAL_PDT){
//				if(soundPool != null){
//					soundPool.play(soundMap.get(ptt.terminalsdk.R.raw.request_call_ok), 0.5f, 0.5f, 0, 0, 1);
//				}
//			}
//		}
//	};

    private ReceiveChangeGroupHandler receiveChangeGroupHandler = new ReceiveChangeGroupHandler() {
        @Override
        public void handler(int errorCode, String errorDesc) {
            if (errorCode == BaseCommonCode.SUCCESS_CODE || errorCode == SignalServerErrorCode.INVALID_SWITCH_GROUP.getErrorCode()) {
                if (soundPool != null && MyApplication.instance.getIndividualState() == IndividualCallState.IDLE
                        && MyApplication.instance.getVideoLivePushingState() == VideoLivePushingState.IDLE
                        && MyApplication.instance.getVideoLivePlayingState() == VideoLivePlayingState.IDLE) {
                    soundPool.play(soundMap.get(R.raw.change_group_ok), 0.5f, 0.5f, 0, 0, 1);
                }
            }
        }
    };
    /**
     * 还有10秒超时，振动提醒
     */
    private ReceiveTalkWillTimeoutHandler receiveTalkWillTimeoutHandler = new ReceiveTalkWillTimeoutHandler() {
        @Override
        public void handler() {
            vibrator = (Vibrator) MyTerminalFactory.getSDK().getApplication().getSystemService(Context.VIBRATOR_SERVICE);
            logger.info("组呼还有10秒超时，振动提醒");
            vibrator.vibrate(talkWillTimeout, -1);
            cancelVibrator();
        }
    };

    /**
     * 获取到rtsp地址，开始播放视频
     */
    private ReceiveGetRtspStreamUrlHandler receiveGetRtspStreamUrlHandler = new ReceiveGetRtspStreamUrlHandler() {
        @Override
        public void handler(final String rtspUrl, Member liveMember, long callId) {
            if (!TextUtils.isEmpty(rtspUrl)) {
                vibrator = (Vibrator) MyTerminalFactory.getSDK().getApplication().getSystemService(Context.VIBRATOR_SERVICE);
                logger.info("请求别人视频直播成功，振动");
                vibrator.vibrate(new long[]{0, 200}, -1);
                cancelVibrator();
            }
        }
    };

    /**
     * 主动方请求个呼开始
     */
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
    /**
     * 收到通知，邀请自己去观看直播
     */
    private ReceiveNotifyInviteToWatchHandler receiveNotifyInviteToWatchHandler = new ReceiveNotifyInviteToWatchHandler() {
        @Override
        public void handler(final PTTProtolbuf.NotifyDataMessage message) {
            if (soundPool != null && MyApplication.instance.getIndividualState() == IndividualCallState.IDLE
                    && MyApplication.instance.getVideoLivePlayingState() == VideoLivePlayingState.IDLE
                    && MyApplication.instance.getVideoLivePushingState() == VideoLivePushingState.IDLE) {
                if (JSONObject.parseObject(message.getMessageBody()).getIntValue("remark") == 2) {//Remark为通知观看时，才响铃
                    soundPool.play(soundMap.get(R.raw.request_call_ok), 0.5f, 0.5f, 0, 0, 1);
                    logger.info("被叫收到邀请自己去观看直播的通知，开始响铃----------");
                }
            }
        }
    };

    public void IndividualCallRequestRing() {
        if (soundPool != null) {
            streamId = soundPool.play(soundMap.get(R.raw.request_call_wait), 1, 1, 0, -1, 1);
            logger.info("主动请求个呼，开始响铃----------dududududu" + streamId);
        }
    }

    public void IndividualCallNotifyRing() {
        if (soundPool != null) {
            streamId = soundPool.play(soundMap.get(R.raw.notific_call_coming), 1, 1, 0, -1, 1);
            logger.info("被叫收到个呼来，开始响铃-----------dengdengdengdengdeng" + streamId);
        }
    }

    //个呼异常提示音响铃
    public void IndividualHangUpRing() {
        if (soundPool != null) {
            streamId = soundPool.play(soundMap.get(R.raw.passive_dropped_warning), 1, 1, 0, -1, 1);
            logger.info("个呼异常提示响铃——————dandandan" + streamId);
        }
    }

    /**
     * 别人邀请视频的响铃
     **/
    public void VideoLiveInCommimgRing() {
        if (soundPool != null) {
            streamId = soundPool.play(soundMap.get(R.raw.notific_videolive_coming), 1, 1, 0, -1, 1);
            logger.info("被动方视频来了，开始响铃" + streamId);
        }
    }

    /**
     * 组呼到来声音
     */
    public void groupCallCommingRing() {
        if (soundMap != null) {
            soundPool.play(soundMap.get(R.raw.receive_group_comming), 0.5f, 0.5f, 0, 0, 1);
        }
    }

    /**
     * 存储空间严重不足
     */
    public void startExternNoStorage() {
        if (soundPool != null) {
            logger.info("存储空间严重不足！");
            soundPool.play(soundMap.get(R.raw.exten_no_storage), 0.5f, 0.5f, 0, 0, 1);
        }
    }

    /**
     * 存储空间不足
     */
    public void startExternStorageNotEnough() {
        if (soundPool != null) {
            logger.info("存储空间不足！");
            soundPool.play(soundMap.get(R.raw.exten_storage_not_engou), 0.5f, 0.5f, 0, 0, 1);
        }
    }

    /**
     * 拍照
     */
    public void startPhotograph() {
        if (soundPool != null) {
            logger.info("拍照");
            soundPool.play(soundMap.get(R.raw.photograph), 0.5f, 0.5f, 0, 0, 1);
        }
    }

    /**
     * 开始上报
     */
    public void startReport() {
        if (soundPool != null) {
            logger.info("开始上报！");
            soundPool.play(soundMap.get(R.raw.start_report), 0.5f, 0.5f, 0, 0, 1);
        }
    }

    /**
     * 收到请求上报的通知开始上报
     */
    public void startReportByNotity() {
        if (soundPool != null) {
            logger.info("开始被动上报！");
            soundPool.play(soundMap.get(R.raw.start_report_by_notify), 0.5f, 0.5f, 0, 0, 1);
        }
    }

    /**
     * 加入警情组开始上报
     */
    public void startReportByEnterGroup() {
        if (soundPool != null) {
            logger.info("加入警情组开始上报！");
            soundPool.play(soundMap.get(R.raw.start_report_by_entergroup), 0.5f, 0.5f, 0, 0, 1);
        }
    }

    /**
     * 停止上报
     */
    public void stopReport() {
        if (soundPool != null) {
            logger.info("停止上报！");
            soundPool.play(soundMap.get(R.raw.stop_report), 0.5f, 0.5f, 0, 0, 1);
        }
    }

    /**
     * 开始录像
     */
    public void startVideoTap() {
        if (soundPool != null) {
            logger.info("开始录像！");
            soundPool.play(soundMap.get(R.raw.start_videotape), 0.5f, 0.5f, 0, 0, 1);
        }
    }

    /**
     * 停止录像
     */
    public void stopVideoTap() {
        if (soundPool != null) {
            logger.info("停止录像！");
            soundPool.play(soundMap.get(R.raw.stop_videotape), 0.5f, 0.5f, 0, 0, 1);
        }
    }

    /**
     * 开始录音
     */
    public void startRecordAudio() {
        if (soundPool != null) {
            logger.info("开始录音！");
            soundPool.play(soundMap.get(R.raw.start_record_audio), 0.5f, 0.5f, 0, 0, 1);
        }
    }

    /**
     * 停止录音
     */
    public void stopRecordAudio() {
        if (soundPool != null) {
            logger.info("停止录音！");
            soundPool.play(soundMap.get(R.raw.stop_record_audio), 0.5f, 0.5f, 0, 0, 1);
        }
    }


    public void stopRing() {
        if (soundPool != null) {
            logger.info("停止响铃---------->" + streamId);
            soundPool.stop(streamId);
        }
    }

    public void delayedStopRing() {
        if (soundPool != null) {
            logger.info("3秒后停止响铃---------->" + streamId);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            soundPool.stop(streamId);
        }
    }

    /**
     * 电量不足时提示
     *
     * @param values
     */
    public void lowPower(int values) {
        switch (values) {
            case 50:
                if (soundPool != null) {
                    soundPool.play(soundMap.get(R.raw.lowpower_50), 0.5f, 0.5f, 0, 0, 1);
                }
                break;
            case 30:
                if (soundPool != null) {
                    soundPool.play(soundMap.get(R.raw.lowpower_30), 0.5f, 0.5f, 0, 0, 1);
                }
                break;
            case 10:
                if (soundPool != null) {
                    soundPool.play(soundMap.get(R.raw.lowpower_10), 0.5f, 0.5f, 0, 0, 1);
                }
                break;
        }
    }

    /**
     * 电量不足时提示
     */
    public void weakSignal() {
        if (soundPool != null) {
            soundPool.play(soundMap.get(R.raw.weaksignal), 0.5f, 0.5f, 0, 0, 1);
        }
    }

    /**
     * 电量不足时提示
     */
    public void livingStopTime(long livingTime) {
        if (soundPool != null) {
            if (livingTime == 3600 * 12) {
                soundPool.play(soundMap.get(R.raw.reported_12hours), 0.5f, 0.5f, 0, 0, 1);
            } else if (livingTime == 3600 * 10) {
                soundPool.play(soundMap.get(R.raw.reported_10hours), 0.5f, 0.5f, 0, 0, 1);
            } else if (livingTime == 3600 * 8) {
                soundPool.play(soundMap.get(R.raw.reported_8hours), 0.5f, 0.5f, 0, 0, 1);
            } else if (livingTime == 3600 * 6) {
                soundPool.play(soundMap.get(R.raw.reported_6hours), 0.5f, 0.5f, 0, 0, 1);
            } else if (livingTime == 3600 * 4) {
                soundPool.play(soundMap.get(R.raw.reported_4hours), 0.5f, 0.5f, 0, 0, 1);
            } else {
                soundPool.play(soundMap.get(R.raw.reported_2hours), 0.5f, 0.5f, 0, 0, 1);
            }
        }
    }

    private PromptManager() {
    }

    public static PromptManager getInstance() {
        if (instance == null) {
            instance = new PromptManager();
        }
        return instance;
    }


    public void start() {
        logger.info("----------提示音管理类 start()------------");
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        soundMap.clear();
        soundMap.put(ptt.terminalsdk.R.raw.ppt_up, soundPool.load(MyApplication.instance, ptt.terminalsdk.R.raw.ppt_up, 1));

        soundMap.put(R.raw.notific_call_coming, soundPool.load(MyApplication.instance, R.raw.notific_call_coming, 1));
        soundMap.put(R.raw.notific_videolive_coming, soundPool.load(MyApplication.instance, R.raw.notific_videolive_coming, 1));
        soundMap.put(R.raw.request_call_ok, soundPool.load(MyApplication.instance, R.raw.request_call_ok, 1));
        soundMap.put(R.raw.request_call_fail, soundPool.load(MyApplication.instance, R.raw.request_call_fail, 1));
        soundMap.put(R.raw.cease_call, soundPool.load(MyApplication.instance, R.raw.cease_call, 1));
        soundMap.put(R.raw.receive_group_comming, soundPool.load(MyApplication.instance, R.raw.receive_group_comming, 1));
        soundMap.put(R.raw.change_group_ok, soundPool.load(MyApplication.instance, R.raw.change_group_ok, 1));
        soundMap.put(R.raw.request_call_wait, soundPool.load(MyApplication.instance, R.raw.request_call_wait, 1));
        soundMap.put(R.raw.passive_dropped_warning, soundPool.load(MyApplication.instance, R.raw.passive_dropped_warning, 1));
        soundMap.put(R.raw.exten_no_storage, soundPool.load(MyApplication.instance, R.raw.exten_no_storage, 1));
        soundMap.put(R.raw.exten_storage_not_engou, soundPool.load(MyApplication.instance, R.raw.exten_storage_not_engou, 1));
        soundMap.put(R.raw.photograph, soundPool.load(MyApplication.instance, R.raw.photograph, 1));
        soundMap.put(R.raw.start_report, soundPool.load(MyApplication.instance, R.raw.start_report, 1));
        soundMap.put(R.raw.start_report_by_notify, soundPool.load(MyApplication.instance, R.raw.start_report_by_notify, 1));
        soundMap.put(R.raw.start_report_by_entergroup, soundPool.load(MyApplication.instance, R.raw.start_report_by_entergroup, 1));
        soundMap.put(R.raw.stop_report, soundPool.load(MyApplication.instance, R.raw.stop_report, 1));
        soundMap.put(R.raw.start_videotape, soundPool.load(MyApplication.instance, R.raw.start_videotape, 1));
        soundMap.put(R.raw.stop_videotape, soundPool.load(MyApplication.instance, R.raw.stop_videotape, 1));
        soundMap.put(R.raw.start_record_audio, soundPool.load(MyApplication.instance, R.raw.start_record_audio, 1));
        soundMap.put(R.raw.stop_record_audio, soundPool.load(MyApplication.instance, R.raw.stop_record_audio, 1));

        soundMap.put(R.raw.lowpower_10, soundPool.load(MyApplication.instance, R.raw.lowpower_10, 1));
        soundMap.put(R.raw.lowpower_30, soundPool.load(MyApplication.instance, R.raw.lowpower_30, 1));
        soundMap.put(R.raw.lowpower_50, soundPool.load(MyApplication.instance, R.raw.lowpower_50, 1));
        soundMap.put(R.raw.reported_2hours, soundPool.load(MyApplication.instance, R.raw.reported_2hours, 1));
        soundMap.put(R.raw.reported_4hours, soundPool.load(MyApplication.instance, R.raw.reported_4hours, 1));
        soundMap.put(R.raw.reported_6hours, soundPool.load(MyApplication.instance, R.raw.reported_6hours, 1));
        soundMap.put(R.raw.reported_8hours, soundPool.load(MyApplication.instance, R.raw.reported_8hours, 1));
        soundMap.put(R.raw.reported_10hours, soundPool.load(MyApplication.instance, R.raw.reported_10hours, 1));
        soundMap.put(R.raw.reported_12hours, soundPool.load(MyApplication.instance, R.raw.reported_12hours, 1));
        soundMap.put(R.raw.weaksignal, soundPool.load(MyApplication.instance, R.raw.weaksignal, 1));

        MyTerminalFactory.getSDK().registReceiveHandler(receiveChangeGroupHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveOnLineStatusChangedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseStartIndividualCallHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGetRtspStreamUrlHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveMemberDeleteHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyMemberKilledHandler);
//		MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallIncommingHandler);
    }

    public void stop() {
        logger.info("----------提示音管理类 stop()------------");
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveChangeGroupHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveOnLineStatusChangedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseStartIndividualCallHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetRtspStreamUrlHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMemberDeleteHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyMemberKilledHandler);
//		MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallIncommingHandler);

        soundMap.clear();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}

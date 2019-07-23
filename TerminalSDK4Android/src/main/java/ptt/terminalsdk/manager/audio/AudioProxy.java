package ptt.terminalsdk.manager.audio;

import android.content.Context;
import android.media.AudioManager;

import org.apache.log4j.Logger;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.audio.AudioResourceManager;
import cn.vsx.hamster.terminalsdk.manager.audio.IAudioPlayComplateHandler;
import cn.vsx.hamster.terminalsdk.manager.audio.IAudioProxy;
import cn.vsx.hamster.terminalsdk.manager.audio.RealtimeAudio;
import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.audio.realtimeaudio.MyAudioResourceManager;
import ptt.terminalsdk.manager.audio.record.Record;
//import cn.vsx.hamster.terminalsdk.manager.audio.RealtimeAudio;
//import ptt.terminalsdk.manager.audio.realtimeaudio.MyAudioResourceManager;


public class AudioProxy implements IAudioProxy{

	private RealtimeAudio realtimeAudio;
	private Record record;

	private final Context context;
	private final AudioManager audiomanage;

	private final int maxVolumeMusic;
	private final int maxVolumeCall;
	private int mode;
	private Logger logger = Logger.getLogger(getClass().getSimpleName());

	public AudioProxy(Context context){
		this.context = context;
		audiomanage = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		maxVolumeMusic = audiomanage.getStreamMaxVolume(AudioManager.STREAM_MUSIC);//获取媒体声音的最大音量
		maxVolumeCall = audiomanage.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);//获取通话声音的最大音量
		AudioResourceManager audioResourceManager = getAudioResourceManager();
		realtimeAudio = new RealtimeAudio(audioResourceManager);
		realtimeAudio.setVolume(TerminalFactory.getSDK().getParam(Params.VOLUME, IAudioProxy.VOLUME_DEFAULT));
	}

	@Override
	public void pauseSender(int cookie){
		realtimeAudio.pauseSender(cookie);
	}
	@Override
	public void resumeSender(String ip, int port, long callId, long uniqueNo, int cookie,int memberId){
		realtimeAudio.resumeSender(ip, port, callId, uniqueNo, cookie);
	}
	@Override
	public int getSenderCookie(){
		return realtimeAudio.getSenderCookie();
	}
	@Override
	public void pauseReceiver(int cookie){
		realtimeAudio.pauseReceiver(cookie);
	}
	@Override
	public void fausePauseReceiver(int cookie){
		realtimeAudio.fausePauseReceiver(cookie);
	}
	@Override
	public void resumeReceiver(String srcIp, int srcPort, long callId, long uniqueNo, int cookie,int memberId){
		realtimeAudio.resumeReceiver(srcIp, srcPort, callId, uniqueNo, cookie);
	}
	@Override
	public void playRecord(String fileName, final IAudioPlayComplateHandler handler){
		if(record != null) {
			record.playRecord(fileName, handler);
		}
	}
	@Override
	public void stopPlayRecord(){
		if(record != null) {
			record.stopPlayRecord();
		}
	}

	@Override
	public void startDuplexCommunication(String sendIp, int sendPort, long sendCallId, String receivedIp, int receivedPort, long receivedCallId, long uniqueNo,int memberId) {
		mode = audiomanage.getMode();
		if (audiomanage.isSpeakerphoneOn()) {
			audiomanage.setSpeakerphoneOn(false);
		}
		audiomanage.setMode(AudioManager.MODE_IN_COMMUNICATION);
		realtimeAudio.startDuplexCommunication(sendIp, sendPort, sendCallId, receivedIp, receivedPort, receivedCallId, uniqueNo);
	}

	@Override
	public void stopDuplexCommunication() {
		audiomanage.setMode(mode);
		realtimeAudio.stopDuplexCommunication();
	}

	@Override
	public void volumeUp() {
		setVolume(realtimeAudio.getVolume() + IAudioProxy.VOLUME_STEP);
	}

	@Override
	public void volumeDown() {
		setVolume(realtimeAudio.getVolume() - IAudioProxy.VOLUME_STEP);
	}

	@Override
	public void volumeQuiet() {
		logger.info( "设为静音");
		MyTerminalFactory.getSDK().putParam(Params.VOLUME_OLD, MyTerminalFactory.getSDK().getParam(Params.VOLUME, VOLUME_DEFAULT));
		setVolume(0);
	}

	@Override
	public void volumeCancelQuiet() {
		logger.info("取消静音");
		if(TerminalFactory.getSDK().getParam(Params.VOLUME, IAudioProxy.VOLUME_DEFAULT) == 0) {
			setVolume(MyTerminalFactory.getSDK().getParam(Params.VOLUME_OLD, IAudioProxy.VOLUME_DEFAULT));
		}
	}

	@Override
	public void start(String type){
		realtimeAudio.start();

		record = new Record();
	}

	@Override
	public void setVolume(int volume){
		if(volume % VOLUME_STEP != 0){
			throw new IllegalArgumentException("设置的音量值"+volume+"不是"+VOLUME_STEP+"的倍数");
		}
		realtimeAudio.setVolume(volume);
		int currentVolumn = realtimeAudio.getVolume();
		if (currentVolumn < IAudioProxy.VOLUME_DEFAULT){
			audiomanage.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolumn* maxVolumeMusic /100, 0);
			audiomanage.setStreamVolume(AudioManager.STREAM_VOICE_CALL, currentVolumn* maxVolumeCall /100, 0);
		}
		else{
			audiomanage.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolumeMusic, 0);
			audiomanage.setStreamVolume(AudioManager.STREAM_VOICE_CALL, maxVolumeCall, 0);
		}
		TerminalFactory.getSDK().putParam(Params.VOLUME, realtimeAudio.getVolume());
	}

	@Override
	public int getReceiverCookie(){
		return realtimeAudio.getReceiverCookie();
	}

	@Override
	public int getVolume() {
		if(realtimeAudio!=null){
			return realtimeAudio.getVolume();
		}else {
			return 0;
		}
	}

	@Override
	public void stop(){
		if(realtimeAudio != null) {
			realtimeAudio.stop();
		}
		if(record != null) {
			record.stopPlayRecord();
		}
	}

	@Override
	public void setSpeakerphoneOn(boolean b) {
		audiomanage.setSpeakerphoneOn(b);
	}

	@Override
	public boolean isSpeakerphoneOn() {
		return audiomanage.isSpeakerphoneOn();
	}

	@Override
	public AudioResourceManager getAudioResourceManager(){
		return new MyAudioResourceManager();
	}
}

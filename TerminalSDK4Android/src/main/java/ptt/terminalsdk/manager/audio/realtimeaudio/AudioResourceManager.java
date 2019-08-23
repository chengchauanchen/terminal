package ptt.terminalsdk.manager.audio.realtimeaudio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;

import org.apache.log4j.Logger;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.zectec.speex.Speex;


/**
 * Created by zc on 2017/3/24.
 * 音频资源管理类，仅提供给当前包内使用，@warn不维护数据一致性
 */

public class AudioResourceManager {
    final static AudioResourceManager INSTANCE;
    static {
        INSTANCE = new AudioResourceManager();
    }
    private AudioResourceManager(){}
    /** 采样率 */
    private int sampleRate = 8000;
    /** 发送端，speex编解码器 */
    private Speex speex4Sender;
    /** 接收端，speex编解码器 */
    private Speex speex4Receiver;
    /** 录音 */
    private AudioRecord audioRecord;
    /** 放音 */    /** 录音缓冲区长度 */
    private int audioRecordBufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)*4;
    private AudioTrack audioTrack;
    /** 放音缓冲区长度 */
    private int audioTrackBufferSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)*4;
    /** 接收socket */
    private Logger logger = Logger.getLogger(getClass());

    private ISendClient sendClient;

    private IReceiveClient receiveClient;

    int getSampleRate(){
        return sampleRate;
    }

    Speex getSpeex4Sender(){
        if(speex4Sender == null){
            speex4Sender = new Speex();
            speex4Sender.init();
        }
        return speex4Sender;
    }

    void releaseSpeex4Sender(){
        if(speex4Sender != null){
            speex4Sender.close();
            speex4Sender = null;
        }
    }

    Speex getSpeex4Receiver(){
        if(speex4Receiver == null){
            speex4Receiver = new Speex();
            speex4Receiver.init();
        }
        return speex4Receiver;
    }

    void releaseSpeex4Receiver(){
        if(speex4Receiver != null){
            speex4Receiver.close();
            speex4Receiver = null;
        }
    }

    AudioRecord getAudioRecord(){
        if(audioRecord == null){
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, audioRecordBufferSize);
            if(audioRecord.getState() != AudioRecord.STATE_INITIALIZED){//如果初始化失败，则再试一次
                logger.warn("audioRecord 初始化失败，state = " + audioRecord.getState() + "，再试一次");
                audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, audioRecordBufferSize);
            }
        }
        return audioRecord;
    }

    void releaseAudioRecord(){
        logger.info("---releaseAudioRecord---销毁资源1");
        if(audioRecord != null){
            logger.info("---releaseAudioRecord--,正在销毁资源1");
            audioRecord.release();
            audioRecord = null;
        }
    }

    int getAudioRecordBufferSize(){
        return audioRecordBufferSize;
    }

    /**
     * 发送数据
     * @return
     */
    synchronized ISendClient getSendClient(){
        if(sendClient == null){
            String protocolType = TerminalFactory.getSDK().getParam(Params.PROTOCOL_TYPE, Params.UDP);
            if(Params.TCP.equals(protocolType)){
                sendClient = TCPSendClient.getInstance();
            }else {
                sendClient = UDPSendClient.getInstance();
            }
        }
        return sendClient;
    }

    synchronized IReceiveClient getReceiveClient(){
        if(receiveClient == null){
            String protocolType = TerminalFactory.getSDK().getParam(Params.PROTOCOL_TYPE, Params.UDP);
            if(Params.TCP.equals(protocolType)){
                receiveClient = TCPReceiveClient.getInstance();
            }else {
                receiveClient = UDPReceiveClient.getInstance();
            }
        }
        return receiveClient;
    }

    private int currentStreamType;

    private AudioTrack getAudioTrack(int streamType){
        if(audioTrack == null){
            audioTrack = new AudioTrack(streamType, sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, audioTrackBufferSize, AudioTrack.MODE_STREAM);
            if(audioTrack.getState() != AudioTrack.STATE_INITIALIZED){//如果初始化失败，则再试一次
                logger.warn("audioTrack 初始化失败，state = " + audioTrack.getState() + "，再试一次");
                audioTrack = new AudioTrack(streamType, sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, audioTrackBufferSize, AudioTrack.MODE_STREAM);
            }
            currentStreamType = streamType;
        }
        else if(streamType != currentStreamType){
            releaseAudioTrack();
            return getAudioTrack(streamType);
        }
        logger.debug("audioTrackBufferSize = " + audioTrackBufferSize + ", audioRecordBufferSize = " + audioRecordBufferSize);
        return audioTrack;
    }

    void releaseAudioTrack(){
        if(audioTrack != null){
            audioTrack.release();
            audioTrack = null;
        }
    }

    void releaseSendClient(){
        if(sendClient != null){
            sendClient.release();
        }
    }

    void releaseReceiveClient(){
        if(receiveClient != null){
            receiveClient.release();
        }
    }

    int getAudioTrackBufferSize(){
        return audioTrackBufferSize;
    }

    public AudioTrack getAudioTrack(Command.CmdType cmdType){
        if(cmdType == Command.CmdType.RESUME_RECEIVER) {//单工通信使用媒体声音模式
            return AudioResourceManager.INSTANCE.getAudioTrack(AudioManager.STREAM_MUSIC);
        }
        else if(cmdType == Command.CmdType.START_DUPLEX_COMMUNICATION){//双工通信使用电话声音模式
            return AudioResourceManager.INSTANCE.getAudioTrack(AudioManager.STREAM_VOICE_CALL);
        }
        else{//其它模式暂不支持
            throw new IllegalArgumentException("不支持命令类型：" + cmdType);
        }
    }
}

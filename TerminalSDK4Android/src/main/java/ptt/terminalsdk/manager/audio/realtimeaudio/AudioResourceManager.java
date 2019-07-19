package ptt.terminalsdk.manager.audio.realtimeaudio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.channels.SocketChannel;

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
    /** 发送socket */
    private DatagramSocket sendSocket;
    private AudioTrack audioTrack;
    /** 放音缓冲区长度 */
    private int audioTrackBufferSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)*4;
    /** 接收socket */
    private DatagramSocket receiveSocket;
    private SocketChannel sendSocketChannel;
    private SocketChannel receiveSocketChannel;
    private Logger logger = Logger.getLogger(getClass());
    private IClient client;

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

    DatagramSocket getSendSocket(){
        if(sendSocket == null){
            try {
                sendSocket = new DatagramSocket();
            } catch (SocketException e) {
                logger.error("sendSocket创建失败", e);
            }
        }
        return sendSocket;
    }

    void releaseSendSocket(){
        if(sendSocket != null){
            sendSocket.close();
            sendSocket = null;
        }
    }

    SocketChannel getSendSocketChannel(){
        try{
            if(sendSocketChannel == null){
                sendSocketChannel = SocketChannel.open();
                sendSocketChannel.configureBlocking(true);
                sendSocketChannel.socket().setSoTimeout(1000 * 1);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        return sendSocketChannel;
    }

    void releaseSendSocketChannel(){
        if (sendSocketChannel != null) {
            try {
                sendSocketChannel.socket().close();
            } catch (Exception e) {
                logger.error(e);
            }
            try {
                sendSocketChannel.close();
            } catch (Exception e) {
                logger.error(e);
            }
        }
        sendSocketChannel = null;
    }

    SocketChannel getReceiveSocketChannel(){
        try{
            if(receiveSocketChannel == null){
                receiveSocketChannel = SocketChannel.open();
                receiveSocketChannel.configureBlocking(true);
                receiveSocketChannel.socket().setSoTimeout(1000 * 1);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        return receiveSocketChannel;
    }

    void releaseReceiveSocketChannel(){
        if (receiveSocketChannel != null) {
            try {
                receiveSocketChannel.socket().close();
            } catch (Exception e) {
                logger.error(e);
            }
            try {
                receiveSocketChannel.close();
            } catch (Exception e) {
                logger.error(e);
            }
        }
        receiveSocketChannel = null;
    }

    synchronized IClient getClient(){
        if(client == null){
            String protocolType = TerminalFactory.getSDK().getParam(Params.PROTOCOL_TYPE, Params.UDP);
            if(Params.TCP.equals(protocolType)){
                client = TCPClient.getInstance();
            }else {
                client = UDPClient.getInstance();
            }
        }
        return client;
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
        if(audioRecord != null){
            audioRecord.release();
            audioRecord = null;
        }
    }

    int getAudioRecordBufferSize(){
        return audioRecordBufferSize;
    }

    private int currentStreamType;
    AudioTrack getAudioTrack(int streamType){
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

    int getAudioTrackBufferSize(){
        return audioTrackBufferSize;
    }

    DatagramSocket getReceiveSocket(){
        if(receiveSocket == null){
            try {
                receiveSocket = new DatagramSocket();
            } catch (IOException e) {
                logger.error("receiveChannel创建失败", e);
            }
        }
        return receiveSocket;
    }

    void releaseReceiveSocket(){
        if(receiveSocket != null){
            receiveSocket.close();
            receiveSocket = null;
        }
    }
}

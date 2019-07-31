package ptt.terminalsdk.manager.audio.realtimeaudio;

import android.media.AudioRecord;
import android.util.Log;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;

import cn.zectec.speex.Speex;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/7/19
 * 描述：
 * 修订历史：
 */
public class UDPSendClient implements ISendClient{

    private Logger logger = Logger.getLogger(getClass());
    private DatagramSocket sendSocket;
    private DatagramPacket sendDp;

    private Speex speex;//编解码类的对象
    private AudioRecord audioRecord;//录音类的对象
    short[] collectedBuffer = new short[Math.min(AudioResourceManager.INSTANCE.getAudioRecordBufferSize()/2, 960)];//采集数据的缓冲区
    private byte[] sendBuf = new byte[512];//发送音频数据的缓冲区

    private UDPSendClient(){
    }

    private static class LazyHolder {
        private static final UDPSendClient INSTANCE = new UDPSendClient();
    }

    public static UDPSendClient getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public synchronized void initClient(Command command){
        try {
            sendSocket = new DatagramSocket();
        } catch (SocketException e) {
            logger.error("sendSocket创建失败", e);
        }
        sendDp = new DatagramPacket(sendBuf, sendBuf.length);//发送数据的数据载体
        speex = AudioResourceManager.INSTANCE.getSpeex4Sender();
        startRecording();
        ByteBuffer.wrap(sendBuf).putLong(command.getCallId()).putLong(command.getUniqueNo());
        sendDp.setAddress(command.getIp());
        sendDp.setPort(command.getPort());
    }

    @Override
    public void sendAudioData(){
        //采集到数据的长度，编码后数据的长度
        Log.d("UDPSendClient", "sendAudioData:" + audioRecord);
        int len = audioRecord.read(collectedBuffer, 0, collectedBuffer.length);
        if(len > 0){
            //发送的音频数据的数据头长度
            int sendBufHead = 16;
            len = speex.encode(collectedBuffer, 0, sendBuf, sendBufHead, len);
            if(len > 0){
                sendDp.setLength(len + sendBufHead);
                sendData();
            }
        }
    }

    @Override
    public void release(){
        if(sendSocket != null){
            sendSocket.close();
            sendSocket = null;
        }
    }

    private void sendData(){
        try{
            sendSocket.send(sendDp);
        }catch(IOException e){
            logger.warn("音频数据发送失败", e);
        }
    }

    private void startRecording(){
        audioRecord = AudioResourceManager.INSTANCE.getAudioRecord();
        try {
            if (audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord.startRecording();
            }
        } catch (IllegalStateException e){
            logger.error("audioRecord 状态发生异常，重建audioRecord");
            AudioResourceManager.INSTANCE.releaseAudioRecord();
            audioRecord = AudioResourceManager.INSTANCE.getAudioRecord();
            try {
                if (audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
                    audioRecord.startRecording();
                }
                logger.info("重建完毕");
            } catch (IllegalStateException e1){
                logger.warn("audioRecord重建后依然不能正常工作，放弃此次录音行为", e1);
            }
        }
    }
}

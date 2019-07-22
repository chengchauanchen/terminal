package ptt.terminalsdk.manager.audio.realtimeaudio;

import android.media.AudioRecord;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

import cn.zectec.speex.Speex;

/**
 * 作者：xuxiaolong
 * 版本：1.0
 * 创建日期：2019/7/19
 * 描述：
 * 修订历史：
 */
public class TCPSendClient implements ISendClient{
    private Logger logger = Logger.getLogger(getClass());
    private SocketChannel sendChannel;
    private Speex speex;//编解码类的对象
    private AudioRecord audioRecord;//录音类的对象
    private short[] collectedBuffer = new short[Math.min(AudioResourceManager.INSTANCE.getAudioRecordBufferSize()/2, 960)];//采集数据的缓冲区
    private byte[] sendBuf = new byte[512];//发送音频数据的缓冲区
    private TCPSendClient(){}

    private static class LazyHolder {
        private static final TCPSendClient INSTANCE = new TCPSendClient();
    }

    public static TCPSendClient getInstance() {
        return TCPSendClient.LazyHolder.INSTANCE;
    }

    @Override
    public synchronized void initClient(Command command){
        if(sendChannel == null){
            try{
                InetSocketAddress address = new InetSocketAddress(command.getIp(), command.getPort());
                sendChannel = SocketChannel.open(address);
                sendChannel.configureBlocking(true);
                sendChannel.socket().setSoTimeout(500);

            }catch(IOException e){
                e.printStackTrace();
            }
        }
        speex = AudioResourceManager.INSTANCE.getSpeex4Sender();
        startRecording();
        ByteBuffer.wrap(sendBuf).putLong(command.getCallId()).putLong(command.getUniqueNo());
    }

    @Override
    public void sendAudioData(){
        //采集到数据的长度，编码后数据的长度
        int len = audioRecord.read(collectedBuffer, 0, collectedBuffer.length);
        if(len > 0){
            //发送的音频数据的数据头长度
            int sendBufHead = 16;
            len = speex.encode(collectedBuffer, 0, sendBuf, sendBufHead, len);
            if(len > 0){
                //去掉后面的0
                byte[] realData = new byte[len+sendBufHead];
                System.arraycopy(sendBuf, 0, realData, 0, realData.length);
                sendData(realData);
            }
        }
    }

    @Override
    public void release(){
        if (sendChannel != null) {
            try{
                sendChannel.socket().close();
                sendChannel.close();
            }catch(IOException e){
                e.printStackTrace();
            }
            sendChannel = null;
        }
    }

    private void sendData(byte[] data){
        if (data == null) {
            return;
        }
        if (sendChannel == null || !sendChannel.isOpen() || !sendChannel.isConnected()) {
            return;
        }
        try{
            logger.info("TCP发送音频数据："+ Arrays.toString(data));
            ByteBuffer bb = ByteBuffer.wrap(data);
            while (bb.hasRemaining()) {
                sendChannel.write(bb);
            }
            sendChannel.socket().getOutputStream().flush();
        }catch(IOException e){
            e.printStackTrace();
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

package ptt.terminalsdk.manager.audio.realtimeaudio;

import android.media.AudioTrack;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.TimerTask;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.zectec.speex.Speex;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/7/20
 * 描述：
 * 修订历史：
 */
public class TCPReceiveClient implements IReceiveClient{

    private Logger logger = Logger.getLogger(getClass());
    private SocketChannel receiveSocketChannel;
    private byte[] receivedData = new byte[234];
    private ByteBuffer buffer;
    private byte[] receiveRequest = new byte[16];
    private Speex speex;
    private AudioTrack audioTrack;
    private TimerTask audioTrackNoDataTimeoutTask = null;
    private short[] playedBuffer = new short[Math.min(AudioResourceManager.INSTANCE.getAudioTrackBufferSize()/2, 960)];//播放数据的缓冲区
    private int len;//解码后数据的长度
    private long receiveCallId;
    private Command command;
    private boolean stop;

    public TCPReceiveClient(){
    }

    private static class LazyHolder {
        private static final TCPReceiveClient INSTANCE = new TCPReceiveClient();
    }

    public static TCPReceiveClient getInstance() {
        return TCPReceiveClient.LazyHolder.INSTANCE;
    }

    @Override
    public synchronized void initClient(Command command){
        this.command = command;
        if(receiveSocketChannel == null){
            try{
                receiveSocketChannel = SocketChannel.open();
                receiveSocketChannel.socket().connect(new InetSocketAddress(command.getIp(), command.getPort()), 400);
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        audioTrack = AudioResourceManager.INSTANCE.getAudioTrack(command.getCmdType());
        speex = AudioResourceManager.INSTANCE.getSpeex4Receiver();
        receiveCallId = command.getCallId();
        stop = false;
        //心跳数据
        ByteBuffer.wrap(receiveRequest).putLong(command.getCallId()).putLong(command.getUniqueNo());
        //接受数据的buffer
        buffer = ByteBuffer.wrap(receivedData);
        ReceiveWorker receiveWorker = new ReceiveWorker();
        Thread receiveThread = new Thread(receiveWorker, "receiveThread");
        receiveThread.start();
    }

    @Override
    public void sendHeatBeat(){
        sendData(receiveRequest);
    }


    @Override
    public void receiveAudioData(){
        logger.info("connect:"+receiveSocketChannel.isConnected()+"---open:"+receiveSocketChannel.isOpen());
        if (receiveSocketChannel == null || !receiveSocketChannel.isOpen() || !receiveSocketChannel.isConnected()) {
            return;
        }
        try{
            //先判断是否为同一个callId
            int read = receiveSocketChannel.read(buffer);
            logger.info("read:"+read);
            if (read > 0) {
                long callID = ByteBuffer.wrap(receivedData, 0, read).getLong();
                logger.info("callID:"+callID);
                if(receiveCallId != callID){
                    //收到的音频数据与预期接收的音频数据不符，不做处理
                    return;
                }
                //解码
                logger.info("TCP接受到音频数据:"+Arrays.toString(receivedData));
                decodeData(read);
                //播放录音
                playAudio();
            }
        } catch (SocketTimeoutException e){
            logger.info("接收数据超时");
        } catch (IOException e) {
            logger.warn("音频数据接收异常", e);
        } catch (IllegalStateException e){
            logger.error("audioTrack 状态发生异常，重建audioTrack", e);
            AudioResourceManager.INSTANCE.releaseAudioTrack();
            audioTrack = null;
            audioTrack = AudioResourceManager.INSTANCE.getAudioTrack(command.getCmdType());
            logger.info("audioTrack 重建完毕");
        }catch(Exception e){
            logger.error("音频解析出错",e);
        }
    }

    @Override
    public void release(){
        stop = true;
        if (receiveSocketChannel != null) {
            try{
                receiveSocketChannel.socket().close();
                receiveSocketChannel.close();
            }catch(IOException e){
                e.printStackTrace();
            }
            receiveSocketChannel = null;
        }
    }

    private void sendData(byte[] data){
        if (data == null) {
            return;
        }
        if (receiveSocketChannel == null || !receiveSocketChannel.isOpen() || !receiveSocketChannel.isConnected()) {
            return;
        }
        try{
            logger.info("TCP发送音频心跳数据："+ Arrays.toString(data));
            ByteBuffer bb = ByteBuffer.wrap(data);
            while (bb.hasRemaining()) {
                receiveSocketChannel.write(bb);
            }
            receiveSocketChannel.socket().getOutputStream().flush();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void decodeData(int read){
        len = speex.decode4Stream(receivedData, 8, playedBuffer, read - 8);
        if(TerminalFactory.getSDK().getAudioProxy().getVolume() > 100){
            int tempVoice;
            int _volumn = (TerminalFactory.getSDK().getAudioProxy().getVolume() - 100)/25;
            for(int i = 0 ; i<len ; i++){
                if(playedBuffer[i] >= 0){
                    tempVoice = playedBuffer[i];
                }
                else{
                    tempVoice = ~playedBuffer[i] + 1;
                }
                tempVoice += ((tempVoice >> 2) * _volumn + ((tempVoice >> 1) % 2) * (_volumn / 2) + (tempVoice % 2) * (_volumn / 4));
                if(playedBuffer[i] >= 0){
                    if(tempVoice >= 32767){
                        playedBuffer[i] = 32767;
                    }
                    else{
                        playedBuffer[i] = (short)tempVoice;
                    }
                }
                else{
                    if(tempVoice >= 32768){
                        playedBuffer[i] = -32768;
                    }
                    else{
                        playedBuffer[i] = (short)(~(tempVoice - 1));
                    }
                }
            }
        }
    }

    /**
     * 播放录音
     */
    private void playAudio(){
        logger.info("解码后数据长度是：" + len);
        audioTrack.write(playedBuffer, 0, len);
        if(audioTrackNoDataTimeoutTask != null){
            audioTrackNoDataTimeoutTask.cancel();
            audioTrackNoDataTimeoutTask = null;
        }
        audioTrackNoDataTimeoutTask = new TimerTask() {
            @Override
            public void run() {
                if(audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                    audioTrack.pause();
                }
            }
        };
        MyTerminalFactory.getSDK().getTimer().schedule(audioTrackNoDataTimeoutTask, 400);
        if(audioTrack.getPlayState()!=AudioTrack.PLAYSTATE_PLAYING){
            audioTrack.play();
        }
    }

    class ReceiveWorker implements Runnable{

        @Override
        public void run(){
            while(!stop){
//                receiveAudioData();
            }
        }
    }
}

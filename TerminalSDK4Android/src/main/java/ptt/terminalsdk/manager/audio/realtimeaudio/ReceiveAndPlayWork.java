package ptt.terminalsdk.manager.audio.realtimeaudio;

import android.media.AudioManager;
import android.media.AudioTrack;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;

import cn.vsx.hamster.terminalsdk.manager.audio.IAudioProxy;
import cn.zectec.speex.Speex;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * Created by zc on 2017/3/24.
 * 接收与播放工作
 */

public class ReceiveAndPlayWork implements Runnable {
    /** 标记工作线程是否已启动 */
    private boolean started;
    /** 音量 */
    private int volumn = 100;
    /** 接收与播放命令队列 */
    private BlockingQueue<Command> receiveAndPlayCommandBlockingQueue;
    private AudioTrack audioTrack;//播放类的对象
    private Logger logger = Logger.getLogger(getClass());

    public ReceiveAndPlayWork(BlockingQueue<Command> receiveAndPlayCommandBlockingQueue){
        if(receiveAndPlayCommandBlockingQueue == null){
            throw new IllegalArgumentException("receiveAndPlayCommandBlockingQueue不能为null");
        }
        this.receiveAndPlayCommandBlockingQueue = receiveAndPlayCommandBlockingQueue;
        started = true;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public boolean isStarted(){
        return started;
    }

    public int getVolumn() {
        return volumn;
    }

    public void setVolumn(int volumn) {
        //暂不提供音量的设置操作
        if(volumn < IAudioProxy.VOLUME_MIN){
            this.volumn = IAudioProxy.VOLUME_MIN;
        }
        else if(volumn > IAudioProxy.VOLUME_MAX){
            this.volumn = IAudioProxy.VOLUME_MAX;
        }
        else{
            this.volumn = volumn;
        }
    }

    @Override
    public void run() {
        Command command;//取出并处理的命令
        Speex speex;//编解码类的对象
        DatagramSocket receiveSocket;//接收音频数据的通道
        short[] playedBuffer = new short[Math.min(AudioResourceManager.INSTANCE.getAudioTrackBufferSize()/2, 960)];//播放数据的缓冲区
        int len;//解码后数据的长度
        byte[] receivedData = new byte[512];//接收到的数据
        DatagramPacket receivedDataDp = new DatagramPacket(receivedData, receivedData.length);
        byte[] receiveRequest = new byte[12];
        DatagramPacket receiveRequestDp = new DatagramPacket(receiveRequest, receiveRequest.length);
        SocketAddress receiveAddress;//接收数据的地址
        long receiveCallId;//接收数据的标识（CallId）
        long lastSendReceiveRequestTime;//最后一次发送接受请求的时间
        TimerTask audioTrackNoDataTimeoutTask = null;
        while (started){
            try{
                //从队列中取命令，直到队列中的命令全部取出，才开始执行最新的那一条命令
                command = receiveAndPlayCommandBlockingQueue.take();
                if(!receiveAndPlayCommandBlockingQueue.isEmpty()){
                    logger.debug("队列中还有命令没有取出，继续取命令");
                    continue;
                }
            }
            catch (InterruptedException e){
                logger.info("接收与播放任务被中断", e);
                continue;
            }
            //开启接收播放，单工通信和双工通信使用相同的处理方式
            if(command.getCmdType() == Command.CmdType.RESUME_RECEIVER || command.getCmdType() == Command.CmdType.START_DUPLEX_COMMUNICATION){
                receiveSocket = AudioResourceManager.INSTANCE.getReceiveSocket();
                if(receiveSocket != null){
                    audioTrack = getAudioTrack(command.getCmdType());
                    speex = AudioResourceManager.INSTANCE.getSpeex4Receiver();
                    lastSendReceiveRequestTime = 0;
                    receiveAddress = new InetSocketAddress(command.getIp(), command.getPort());
                    ByteBuffer.wrap(receiveRequest).putLong(command.getCallId()).putLong(command.getUniqueNo());
                    receiveCallId = command.getCallId();
                    receiveRequestDp.setSocketAddress(receiveAddress);
                    try {
                        receiveSocket.setSoTimeout(400);
                    } catch (SocketException e) {
                        logger.warn("接收通道设置超时时间失败", e);
                    }
                    while (receiveAndPlayCommandBlockingQueue.isEmpty() && started){
                        if(System.currentTimeMillis() - lastSendReceiveRequestTime > 1000){
                            try {
                                receiveSocket.send(receiveRequestDp);
                                lastSendReceiveRequestTime = System.currentTimeMillis();
                            } catch (IOException e) {
                                logger.warn("获取音频数据命令发送失败", e);
                            }
                        }
                        try {
                            receiveSocket.receive(receivedDataDp);
                            if(receivedDataDp.getLength() > 0){
                                if(receiveCallId != ByteBuffer.wrap(receivedData, 0, receivedDataDp.getLength()).getLong()){
                                    continue;//收到的音频数据与预期接收的音频数据不符，不做处理
                                }
                                len = speex.decode4Stream(receivedData, 8, playedBuffer, receivedDataDp.getLength() - 8);
                                if(volumn > 100){
                                    int tempVoice;
                                    int _volumn = (volumn - 100)/25;
                                    for(int i = 0 ; i<len ; i++){
                                        tempVoice = playedBuffer[i];
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
//                                logger.info("解码后数据长度是：" + len + "，volumn = " + volumn);
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
                        } catch (SocketTimeoutException e){
//                            logger.info("接收数据超时");
                        } catch (IOException e) {
                            logger.warn("音频数据接收异常", e);
                        } catch (IllegalStateException e){
                            logger.error("audioTrack 状态发生异常，重建audioTrack", e);
                            AudioResourceManager.INSTANCE.releaseAudioTrack();
                            audioTrack = null;
                            audioTrack = getAudioTrack(command.getCmdType());
                            logger.info("audioTrack 重建完毕");
                        }catch(Exception e){
                            logger.error("音频解析出错",e);
                        }
                    }
                }
            }
            //停止接收播放，单工通信和双工通信使用相同的处理方式
            else if(command.getCmdType() == Command.CmdType.PAUSE_RECEIVER || command.getCmdType() == Command.CmdType.FAUSE_PAUSE_RECEIVER || command.getCmdType() == Command.CmdType.STOP_DUPLEX_COMMUNICATION){
                if(audioTrackNoDataTimeoutTask != null){
                    audioTrackNoDataTimeoutTask.cancel();
                    audioTrackNoDataTimeoutTask = null;
                }
                AudioResourceManager.INSTANCE.releaseReceiveSocket();
                AudioResourceManager.INSTANCE.releaseSpeex4Receiver();
                AudioResourceManager.INSTANCE.releaseAudioTrack();
                audioTrack = null;
            }
        }
        //任务执行完毕，销毁可能未销毁的资源
        if(audioTrackNoDataTimeoutTask != null){
            audioTrackNoDataTimeoutTask.cancel();
            audioTrackNoDataTimeoutTask = null;
        }
        logger.info("接受与播放任务执行完毕，销毁可能未销毁的资源");
        AudioResourceManager.INSTANCE.releaseReceiveSocket();
        AudioResourceManager.INSTANCE.releaseSpeex4Receiver();
        AudioResourceManager.INSTANCE.releaseAudioTrack();
        audioTrack = null;
    }

    private AudioTrack getAudioTrack(Command.CmdType cmdType){
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

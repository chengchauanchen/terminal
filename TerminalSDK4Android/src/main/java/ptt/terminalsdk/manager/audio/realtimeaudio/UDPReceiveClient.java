package ptt.terminalsdk.manager.audio.realtimeaudio;

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
public class UDPReceiveClient implements IReceiveClient{
    private Logger logger = Logger.getLogger(getClass());

    private byte[] receivedData = new byte[512];//接收到的数据
    private DatagramPacket receivedDataDp = new DatagramPacket(receivedData, receivedData.length);
    private byte[] receiveRequest = new byte[16];
    //心跳包
    private DatagramPacket receiveRequestDp = new DatagramPacket(receiveRequest, receiveRequest.length);
    private DatagramSocket receiveSocket;
    private Speex speex;
    private AudioTrack audioTrack;
    private TimerTask audioTrackNoDataTimeoutTask = null;
    private short[] playedBuffer = new short[Math.min(AudioResourceManager.INSTANCE.getAudioTrackBufferSize()/2, 960)];//播放数据的缓冲区
    private int len;//解码后数据的长度
    private long receiveCallId;
    private Command command;

    private static class LazyHolder {
        private static final UDPReceiveClient INSTANCE = new UDPReceiveClient();
    }

    public static UDPReceiveClient getInstance() {
        return UDPReceiveClient.LazyHolder.INSTANCE;
    }

    @Override
    public synchronized void initClient(Command command){
        logger.info("playedBuffer:"+AudioResourceManager.INSTANCE.getAudioTrackBufferSize());
        this.command = command;
        try {
            receiveSocket = new DatagramSocket();
            receiveSocket.setSoTimeout(400);
        } catch (SocketException e) {
            logger.error("sendSocket创建失败", e);
        }
        audioTrack = AudioResourceManager.INSTANCE.getAudioTrack(command.getCmdType());
        speex = AudioResourceManager.INSTANCE.getSpeex4Receiver();
        //接收数据的地址
        SocketAddress receiveAddress = new InetSocketAddress(command.getIp(), command.getPort());
        ByteBuffer.wrap(receiveRequest).putLong(command.getCallId()).putLong(command.getUniqueNo());

        receiveRequestDp.setSocketAddress(receiveAddress);
        receivedDataDp.setSocketAddress(receiveAddress);
        receiveCallId = command.getCallId();
    }

    @Override
    public void sendHeatBeat() throws IOException{
        receiveSocket.send(receiveRequestDp);
    }

    @Override
    public void receiveAudioData(){
        try {
            receiveSocket.receive(receivedDataDp);
            logger.info("Packet.getLength():"+receivedDataDp.getLength());
            if(receivedDataDp.getLength() > 0){
                if(receiveCallId != ByteBuffer.wrap(receivedData, 0, receivedDataDp.getLength()).getLong()){
                    //收到的音频数据与预期接收的音频数据不符，不做处理
                    return;
                }
                //解码
                decodeData();
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

    private void decodeData(){
        logger.info("UDP接受音频数据"+ Arrays.toString(receivedData));
        logger.info("packet.length----"+receivedDataDp.getLength()+"-----receivedData.length:"+receivedData.length);
        len = speex.decode4Stream(receivedData, 8, playedBuffer, receivedDataDp.getLength() - 8);
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
        //logger.info("解码后数据长度是：" + len + "，volumn = " + volumn);
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

    @Override
    public void release(){
        if(audioTrackNoDataTimeoutTask != null){
            audioTrackNoDataTimeoutTask.cancel();
            audioTrackNoDataTimeoutTask = null;
        }
        if(receiveSocket != null){
            receiveSocket.close();
            receiveSocket = null;
        }
        AudioResourceManager.INSTANCE.releaseSpeex4Receiver();
        AudioResourceManager.INSTANCE.releaseAudioTrack();
        audioTrack = null;
    }
}

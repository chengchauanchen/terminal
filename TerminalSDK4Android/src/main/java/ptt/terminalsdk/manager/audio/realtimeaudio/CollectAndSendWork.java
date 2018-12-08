package ptt.terminalsdk.manager.audio.realtimeaudio;

import android.media.AudioRecord;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;

import cn.zectec.speex.Speex;

/**
 * Created by zc on 2017/3/24.
 * 采集与播放工作
 */

public class CollectAndSendWork implements Runnable {
    /** 标记工作线程是否已启动 */
    private boolean started;
    /** 采集与发送命令队列 */
    private BlockingQueue<Command> collectAndSendCommandBlockingQueue;
    private Logger logger = Logger.getLogger(getClass());

    public CollectAndSendWork(BlockingQueue<Command> collectAndSendCommandBlockingQueue){
        if(collectAndSendCommandBlockingQueue == null){
            throw new IllegalArgumentException("collectAndSendCommandBlockingQueue不能为null");
        }
        this.collectAndSendCommandBlockingQueue = collectAndSendCommandBlockingQueue;
        started = true;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public boolean isStarted(){
        return started;
    }

    @Override
    public void run() {
        Command command;//取出并处理的命令
        AudioRecord audioRecord;//录音类的对象
        Speex speex;//编解码类的对象
        DatagramSocket sendSocket;//发送音频数据的socket
        short[] collectedBuffer = new short[Math.min(AudioResourceManager.INSTANCE.getAudioRecordBufferSize()/2, 960)];//采集数据的缓冲区
        int len;//采集到数据的长度，编码后数据的长度
        byte[] sendBuf = new byte[512];//发送音频数据的缓冲区
        int sendBufHead = 12;//发送的音频数据的数据头长度
        DatagramPacket sendDp = new DatagramPacket(sendBuf, sendBuf.length);//发送数据的数据载体
        while (started){
            try{
                //从队列中取命令，直到队列中的命令全部取出，才开始执行最新的那一条命令
                command = collectAndSendCommandBlockingQueue.take();
                if(!collectAndSendCommandBlockingQueue.isEmpty()){
                    logger.debug("队列中还有命令没有取出，继续取命令");
                    continue;
                }
            }
            catch (InterruptedException e){
                logger.info("采集与发送任务被中断", e);
                continue;
            }
            //开启采集发送，单工通信和双工通信使用相同的处理方式
            if(command.getCmdType() == Command.CmdType.RESUME_SENDER || command.getCmdType() == Command.CmdType.START_DUPLEX_COMMUNICATION){
                sendSocket = AudioResourceManager.INSTANCE.getSendSocket();
                if(sendSocket != null) {
                    audioRecord = AudioResourceManager.INSTANCE.getAudioRecord();
                    speex = AudioResourceManager.INSTANCE.getSpeex4Sender();
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
                    ByteBuffer.wrap(sendBuf).putLong(command.getCallId()).putInt(command.getMemberId());
                    sendDp.setAddress(command.getIp());
                    sendDp.setPort(command.getPort());
                    while (collectAndSendCommandBlockingQueue.isEmpty() && started) {
                        len = audioRecord.read(collectedBuffer, 0, collectedBuffer.length);
                        if(len > 0){
                            len = speex.encode(collectedBuffer, 0, sendBuf, sendBufHead, len);
                            if(len > 0){
                                sendDp.setLength(len + sendBufHead);
                                try {
                                    sendSocket.send(sendDp);
                                } catch (IOException e) {
                                    logger.warn("音频数据发送失败", e);
                                }
                            }
                        }
                    }
                }
            }
            //停止采集发送，单工通信和双工通信使用相同的处理方式
            else if(command.getCmdType() == Command.CmdType.PAUSE_SENDER || command.getCmdType() == Command.CmdType.STOP_DUPLEX_COMMUNICATION){
                AudioResourceManager.INSTANCE.releaseAudioRecord();
                AudioResourceManager.INSTANCE.releaseSpeex4Sender();
                AudioResourceManager.INSTANCE.releaseSendSocket();
            }
        }
        logger.info("采集与发送任务执行完毕，销毁可能未销毁的资源");
        //任务执行完毕，销毁可能未销毁的资源
        AudioResourceManager.INSTANCE.releaseAudioRecord();
        AudioResourceManager.INSTANCE.releaseSpeex4Sender();
        AudioResourceManager.INSTANCE.releaseSendSocket();
    }
}

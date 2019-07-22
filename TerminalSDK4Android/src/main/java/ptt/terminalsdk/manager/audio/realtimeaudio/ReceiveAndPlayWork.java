package ptt.terminalsdk.manager.audio.realtimeaudio;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.audio.IAudioProxy;
import cn.vsx.hamster.terminalsdk.tools.Params;

/**
 * Created by zc on 2017/3/24.
 * 接收与播放工作
 */

public class ReceiveAndPlayWork implements Runnable {
    /** 标记工作线程是否已启动 */
    private boolean started;
    /** 接收与播放命令队列 */
    private BlockingQueue<Command> receiveAndPlayCommandBlockingQueue;
    private Logger logger = Logger.getLogger(getClass());
    private int volumn = 100;

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
        String protocolType = TerminalFactory.getSDK().getParam(Params.PROTOCOL_TYPE, Params.UDP);
        long lastSendReceiveRequestTime;//最后一次发送接受请求的时间
        IReceiveClient client;
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
                client = AudioResourceManager.INSTANCE.getReceiveClient();
                client.initClient(command);
                lastSendReceiveRequestTime = 0;


                while (receiveAndPlayCommandBlockingQueue.isEmpty() && started){
                    //发送心跳包
                    if(System.currentTimeMillis() - lastSendReceiveRequestTime > 1000){
                        try{
                            client.sendHeatBeat();
                            lastSendReceiveRequestTime = System.currentTimeMillis();
                        }catch(IOException e){
                            logger.warn("获取音频数据命令发送失败", e);
                        }
                    }
                    //接受数据
//                    if(Params.UDP.equals(protocolType)){
                        client.receiveAudioData();
//                    }
                }

            }
            //停止接收播放，单工通信和双工通信使用相同的处理方式
            else if(command.getCmdType() == Command.CmdType.PAUSE_RECEIVER || command.getCmdType() == Command.CmdType.FAUSE_PAUSE_RECEIVER || command.getCmdType() == Command.CmdType.STOP_DUPLEX_COMMUNICATION){
                AudioResourceManager.INSTANCE.releaseReceiveClient();
                AudioResourceManager.INSTANCE.releaseSpeex4Receiver();
                AudioResourceManager.INSTANCE.releaseAudioTrack();
            }
        }
        AudioResourceManager.INSTANCE.releaseReceiveClient();
        AudioResourceManager.INSTANCE.releaseSpeex4Receiver();
        AudioResourceManager.INSTANCE.releaseAudioTrack();
    }


}

package ptt.terminalsdk.manager.audio.realtimeaudio;

import org.apache.log4j.Logger;

import java.util.concurrent.BlockingQueue;

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
        ISendClient client;
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
                client = AudioResourceManager.INSTANCE.getSendClient();
                client.initClient(command);
                while (collectAndSendCommandBlockingQueue.isEmpty() && started){
                    client.sendAudioData();
                }
            }
            //停止采集发送，单工通信和双工通信使用相同的处理方式
            else if(command.getCmdType() == Command.CmdType.PAUSE_SENDER || command.getCmdType() == Command.CmdType.STOP_DUPLEX_COMMUNICATION){
                AudioResourceManager.INSTANCE.releaseSendClient();
                AudioResourceManager.INSTANCE.releaseSpeex4Receiver();
                AudioResourceManager.INSTANCE.releaseAudioTrack();
            }
        }
        logger.info("采集与发送任务执行完毕，销毁可能未销毁的资源");
        //任务执行完毕，销毁可能未销毁的资源
        AudioResourceManager.INSTANCE.releaseSendClient();
        AudioResourceManager.INSTANCE.releaseSpeex4Receiver();
        AudioResourceManager.INSTANCE.releaseAudioTrack();
    }
}

package ptt.terminalsdk.manager.audio.realtimeaudio;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by zc on 2017/3/23.
 * 实时音频处理类
 */

public class RealtimeAudio {
    /** 采集与发送命令队列 */
    private BlockingQueue<Command> collectAndSendCommandBlockingQueue;
    /** 接收与播放命令队列 */
    private BlockingQueue<Command> receiveAndPlayCommandBlockingQueue;
    /** 采集与发送任务 */
    private CollectAndSendWork collectAndSendWork;
    /** 采集与发送线程 */
    private Thread collectAndSendThread;
    /** 接收与播放工作 */
    private ReceiveAndPlayWork receiveAndPlayWork;
    /** 接收与播放线程 */
    private Thread receiveAndPlayThread;
    private int nextSendCookie = 0;
    private List<Integer> currentSendCookies = new ArrayList<>();
    private int nextReceiverCookie = 0;
    private List<Integer> currentReceiverCookies = new ArrayList<>();

    private Logger logger = Logger.getLogger(getClass());

    public synchronized void pauseReceiver(int cookie){
        if(currentReceiverCookies.contains(cookie)){
            currentReceiverCookies.remove(Integer.valueOf(cookie));
        }
        if(currentReceiverCookies.isEmpty()){
            Command cmd = new Command(Command.CmdType.PAUSE_RECEIVER);
            try {
                receiveAndPlayCommandBlockingQueue.put(cmd);
            } catch (InterruptedException e) {
                logger.warn("receiveAndPlayCommandBlockingQueue.put操作被中断了", e);
            }
        }
    }
    public synchronized void fausePauseReceiver(int cookie){
        if(currentReceiverCookies.contains(cookie)){
            currentReceiverCookies.remove(Integer.valueOf(cookie));
        }
        if(currentReceiverCookies.isEmpty()){
            Command cmd = new Command(Command.CmdType.FAUSE_PAUSE_RECEIVER);
            try {
                receiveAndPlayCommandBlockingQueue.put(cmd);
            } catch (InterruptedException e) {
                logger.warn("receiveAndPlayCommandBlockingQueue.put操作被中断了", e);
            }
        }
    }
    public synchronized void resumeReceiver(String srcIp, int srcPort, long callId, long uniqueNo, int cookie){
        if(currentReceiverCookies.isEmpty()){
            Command cmd = new Command(Command.CmdType.RESUME_RECEIVER, srcIp, srcPort, callId, uniqueNo);
            try {
                receiveAndPlayCommandBlockingQueue.put(cmd);
            } catch (InterruptedException e) {
                logger.warn("receiveAndPlayCommandBlockingQueue.put操作被中断了", e);
            }
        }
        if(!currentReceiverCookies.contains(cookie)){
            currentReceiverCookies.add(cookie);
        }
    }
    public synchronized int getReceiverCookie(){
        return nextReceiverCookie++;
    }
    public synchronized void pauseSender(int cookie){
        if(currentSendCookies.contains(cookie)){
            currentSendCookies.remove(Integer.valueOf(cookie));
        }
        if(currentSendCookies.isEmpty()){
            Command cmd = new Command(Command.CmdType.PAUSE_SENDER);
            try {
                collectAndSendCommandBlockingQueue.put(cmd);
            } catch (InterruptedException e) {
                logger.warn("collectAndSendCommandBlockingQueue.put操作被中断了", e);
            }
        }
    }
    public synchronized void resumeSender(String ip, int port, long callId, long uniqueNo, int cookie){
        if(currentSendCookies.isEmpty()){
            Command cmd = new Command(Command.CmdType.RESUME_SENDER, ip, port, callId, uniqueNo);
            try {
                collectAndSendCommandBlockingQueue.put(cmd);
            } catch (InterruptedException e) {
                logger.warn("collectAndSendCommandBlockingQueue.put操作被中断了", e);
            }
        }
        if(!currentSendCookies.contains(cookie)){
            currentSendCookies.add(cookie);
        }
    }
    public synchronized int getSenderCookie(){
        return nextSendCookie++;
    }

    public void startDuplexCommunication(String sendIp, int sendPort, long sendCallId,String receivedIp, int receivedPort, long receiveCallId, long uniqueNo) {
        Command sendCmd = new Command(Command.CmdType.START_DUPLEX_COMMUNICATION, sendIp, sendPort, sendCallId, uniqueNo);
        Command receivedCmd = new Command(Command.CmdType.START_DUPLEX_COMMUNICATION, receivedIp, receivedPort, receiveCallId, uniqueNo);
        try {
            collectAndSendCommandBlockingQueue.put(sendCmd);
        } catch (InterruptedException e) {
            logger.warn("collectAndSendCommandBlockingQueue.put操作被中断了", e);
        }
        try {
            receiveAndPlayCommandBlockingQueue.put(receivedCmd);
        } catch (InterruptedException e) {
            logger.warn("receiveAndPlayCommandBlockingQueue.put操作被中断了", e);
        }
    }

    public void stopDuplexCommunication() {
        Command sendCmd = new Command(Command.CmdType.STOP_DUPLEX_COMMUNICATION);
        Command receivedCmd = new Command(Command.CmdType.STOP_DUPLEX_COMMUNICATION);
        try {
            collectAndSendCommandBlockingQueue.put(sendCmd);
        } catch (InterruptedException e) {
            logger.warn("collectAndSendCommandBlockingQueue.put操作被中断了", e);
        }
        try {
            receiveAndPlayCommandBlockingQueue.put(receivedCmd);
        } catch (InterruptedException e) {
            logger.warn("receiveAndPlayCommandBlockingQueue.put操作被中断了", e);
        }
    }

    public void setVolume(int volume) {
        receiveAndPlayWork.setVolumn(volume);
    }

    public int getVolume() {
        return receiveAndPlayWork.getVolumn();
    }

    public synchronized void start(){
        //初始化并启动采集与发送线程
        collectAndSendCommandBlockingQueue = new LinkedBlockingQueue<>();
        collectAndSendWork = new CollectAndSendWork(collectAndSendCommandBlockingQueue);
        collectAndSendWork.setStarted(true);
        collectAndSendThread = new Thread(collectAndSendWork);
        collectAndSendThread.setDaemon(true);
        collectAndSendThread.start();

        //初始化并启动接收与播放线程
        receiveAndPlayCommandBlockingQueue = new LinkedBlockingQueue<>();
        receiveAndPlayWork = new ReceiveAndPlayWork(receiveAndPlayCommandBlockingQueue);
        receiveAndPlayWork.setStarted(true);
        receiveAndPlayThread = new Thread(receiveAndPlayWork);
        receiveAndPlayThread.setDaemon(true);
        receiveAndPlayThread.start();

        logger.debug("实时音频处理类启动完毕");
    }

    public synchronized void stop(){
        try {
            //中断并结束采集与发送线程
            collectAndSendWork.setStarted(false);
            collectAndSendThread.interrupt();
            collectAndSendThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            //中断并结束接收与播放线程
            receiveAndPlayWork.setStarted(false);
            receiveAndPlayThread.interrupt();
            receiveAndPlayThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logger.debug("实时音频处理类停止完毕");
    }
}

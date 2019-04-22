package ptt.terminalsdk.manager.audio.realtimeaudio;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * AudioProxy类使用的命令类
 */

public class Command {
    private CmdType cmdType;
    private InetAddress ip;
    private int port;
    private long callId;
    private long uniqueNo;
    public Command(CmdType cmdType){
        if(cmdType == null){
            throw new IllegalArgumentException("cmdType 不能为 null");
        }
        this.cmdType = cmdType;
    }
    public Command(CmdType cmdType, String ip, int port, long callId, long uniqueNo){
        this(cmdType);
        try {
            this.ip = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Ip地址: " + ip + "不合法");
        }
        this.port = port;
        this.callId = callId;
        this.uniqueNo = uniqueNo;
    }

    public CmdType getCmdType() {
        return cmdType;
    }

    public InetAddress getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public long getCallId() {
        return callId;
    }

    public long getUniqueNo() {
        return uniqueNo;
    }

    enum CmdType{
        RESUME_SENDER,
        PAUSE_SENDER,
        RESUME_RECEIVER,
        PAUSE_RECEIVER,
        FAUSE_PAUSE_RECEIVER,
        START_DUPLEX_COMMUNICATION,
        STOP_DUPLEX_COMMUNICATION;
    }
}

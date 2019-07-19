package ptt.terminalsdk.manager.audio.realtimeaudio;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/7/19
 * 描述：
 * 修订历史：
 */
public class UDPClient implements IClient{

    private Logger logger = Logger.getLogger(getClass());
    private DatagramSocket sendSocket;
    private DatagramPacket sendDp;

    private UDPClient(){
    }

    private static class LazyHolder {
        private static final UDPClient INSTANCE = new UDPClient();
    }

    public static UDPClient getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public void setAddress(InetAddress ip, int port){
        sendDp.setAddress(ip);
        sendDp.setPort(port);
    }

    @Override
    public void setLength(int length){
        sendDp.setLength(length);
    }

    @Override
    public void init(byte[] data, int length){
        getSendSocket();
        sendDp = new DatagramPacket(data, length);//发送数据的数据载体
    }

    private void getSendSocket(){
        if(sendSocket == null){
            synchronized(UDPClient.class){
                if(sendSocket == null){
                    try {
                        sendSocket = new DatagramSocket();
                    } catch (SocketException e) {
                        logger.error("sendSocket创建失败", e);
                    }
                }
            }
        }
    }

    @Override
    public void send(byte[] data){
        sendDp.setData(data);
        try{
            sendSocket.send(sendDp);
        }catch(IOException e){
            logger.warn("音频数据发送失败", e);
        }
    }

    @Override
    public void release(){
        if(sendSocket != null){
            sendSocket.close();
            sendSocket = null;
        }
    }
}

package ptt.terminalsdk.manager.audio.realtimeaudio;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * 作者：xuxiaolong
 * 版本：1.0
 * 创建日期：2019/7/19
 * 描述：
 * 修订历史：
 */
public class TCPClient implements IClient{
    private Logger logger = Logger.getLogger(getClass());
    private SocketChannel sendChannel;
    private TCPClient(){}

    private static class LazyHolder {
        private static final TCPClient INSTANCE = new TCPClient();
    }

    public static TCPClient getInstance() {
        return TCPClient.LazyHolder.INSTANCE;
    }

    @Override
    public void init(byte[] data, int length){
        getSendChannel();
    }

    private void getSendChannel(){
        if(sendChannel == null){
            synchronized(TCPClient.class){
                if(sendChannel == null){
                    try{
                        sendChannel = SocketChannel.open();
                        sendChannel.configureBlocking(true);
                        sendChannel.socket().setSoTimeout(1000);

                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void setAddress(InetAddress ip, int port){
        try{
            sendChannel.socket().connect(new InetSocketAddress(ip, port), 1000);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void setLength(int length){

    }

    @Override
    public void send(byte[] data){
        if (data == null) {
            return;
        }
        if (sendChannel == null || sendChannel.isOpen() == false || sendChannel.isConnected() == false) {
            return;
        }
        try{
            ByteBuffer bb = ByteBuffer.wrap(data);
            while (bb.hasRemaining()) {
                sendChannel.write(bb);
            }
            sendChannel.socket().getOutputStream().flush();
        }catch(IOException e){
            e.printStackTrace();
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
}

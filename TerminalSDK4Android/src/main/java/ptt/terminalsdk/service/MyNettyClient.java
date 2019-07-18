package ptt.terminalsdk.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.RemoteException;

import org.apache.log4j.Logger;
import org.ddpush.im.client.v1.ServerConnectionEstablishedHandler;
import org.ddpush.im.client.v1.netty.NettyClient;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.PushMessageSendResultHandlerAidl;
import ptt.terminalsdk.ServerConnectionEstablishedHandlerAidl;
import ptt.terminalsdk.ServerMessageReceivedHandlerAidl;

/**
 * 作者：xuxiaolong
 * 版本：1.0
 * 创建日期：2019/7/18
 * 描述：
 * 修订历史：
 */
public class MyNettyClient extends NettyClient implements IConnectionClient{

    private Logger logger = Logger.getLogger(getClass());
    private Context mContext;
    private SharedPreferences sp;
    private List<ServerConnectionEstablishedHandler> connectionEstablishedHandlers = new ArrayList<>();
    private List<ServerMessageReceivedHandlerAidl> serverMessageReceivedHandlerAidls = new ArrayList<>();

    private ServerConnectionEstablishedHandlerAidl handler;
    private  ServerConnectionEstablishedHandler serverConnectionEstablishedHandler = new ServerConnectionEstablishedHandler() {
        @Override
        public void handler(boolean connected) {
            try {
                handler.handler(connected);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    public MyNettyClient(Context context){
        this.mContext = context;
        sp = context.getSharedPreferences(Params.MESSAGE_SERVICE_PRE_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void registMessageReceivedHandler(ServerMessageReceivedHandlerAidl handler){
        for (ServerMessageReceivedHandlerAidl handler0 : serverMessageReceivedHandlerAidls){
            if (handler0.asBinder() != handler.asBinder()){
            }
        }
        serverMessageReceivedHandlerAidls.add(handler);
    }

    @Override
    public void unregistMessageReceivedHandler(ServerMessageReceivedHandlerAidl handler){

        serverMessageReceivedHandlerAidls.remove(handler);
    }

    @Override
    public void sendMessage(byte[] data, PushMessageSendResultHandlerAidl handler){
        sendRealTimeData(data, (sendOK, uuid) -> {
            try {
                handler.handler(sendOK, uuid);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    public void registServerConnectionEstablishedHandler(final ServerConnectionEstablishedHandlerAidl handler){
        logger.info("MyNettyClient----registServerConnectionEstablishedHandler----"+handler);
        this.handler = handler;
        registServerConnectionEstablishedHandler(serverConnectionEstablishedHandler);
    }

    @Override
    public void unregistServerConnectionEstablishedHandler(ServerConnectionEstablishedHandlerAidl handler){
        this.handler = handler;
        unregistServerConnectionEstablishedHandler(serverConnectionEstablishedHandler);
    }

    public void unregistServerConnectionEstablishedHandler(ServerConnectionEstablishedHandler handler) {
        logger.info("unregistServerConnectionEstablishedHandler--"+connectionEstablishedHandlers);
        if (connectionEstablishedHandlers.contains(handler)) {
            connectionEstablishedHandlers.remove(handler);
        }
    }

    @Override
    public void setUuid(byte[] uuid){
        super.setUuid(uuid);
    }

    @Override
    public void setServerIp(String serverIp){
        super.setServerIp(serverIp);
    }

    @Override
    public void setServerPort(int serverPort){
        super.setServerPort(serverPort);
    }

    @Override
    public boolean isStarted(){
        return isOpen();
    }

    private void registServerConnectionEstablishedHandler(ServerConnectionEstablishedHandler handler) {
        if (!connectionEstablishedHandlers.contains(handler)) {
            connectionEstablishedHandlers.add(handler);
        }
        logger.info("MyNetty----registServerConnectionEstablishedHandler:"+connectionEstablishedHandlers.toString());
        //部分手机注册在UDPClient启动之后，导致不会触发连接的通知，手动触发一下
        if(isOpen()){
            handler.handler(true);
        }
    }

    @Override
    protected boolean hasNetworkConnection() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }
        return false;
    }

    @Override
    public void start(){
        try{
            super.start();
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    @Override
    public void stop(){
        try{
            super.stop();
            super.close();
            for (ServerConnectionEstablishedHandler handler : connectionEstablishedHandlers) {
                handler.handler(false);
                logger.info("MyNettyClient---stop()--->"+false);
            }
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onConnected(){
        logger.info("onConnected----"+connectionEstablishedHandlers);
        for(ServerConnectionEstablishedHandler connectionEstablishedHandler : connectionEstablishedHandlers){
            connectionEstablishedHandler.handler(true);
        }
    }

    @Override
    public void onReceiveMsg(Object msg){
        logger.info("收到服务端消息:"+msg);
//        for(ServerMessageReceivedHandlerAidl handler : serverMessageReceivedHandlerAidls){
//            try {
//                handler.handle(data, offset, length);
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
//        }
    }

    @Override
    public void onDisConnect(){
        for(ServerConnectionEstablishedHandler connectionEstablishedHandler : connectionEstablishedHandlers){
            connectionEstablishedHandler.handler(false);
        }
    }

    @Override
    protected byte getSeq() {
        int byteSeq = sp.getInt(Params.SEQ, 0);
        byteSeq++;
        sp.edit().putInt(Params.SEQ, byteSeq).commit();
        return (byte) byteSeq;
    }
}

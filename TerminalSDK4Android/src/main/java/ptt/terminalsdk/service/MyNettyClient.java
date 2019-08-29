package ptt.terminalsdk.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.RemoteException;

import org.apache.log4j.Logger;
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
//    private List<ServerConnectionEstablishedHandler> connectionEstablishedHandlers = new ArrayList<>();
    private List<ServerMessageReceivedHandlerAidl> serverMessageReceivedHandlerAidls = new ArrayList<>();

    private ServerConnectionEstablishedHandlerAidl handler;
    private static MyNettyClient instance;
//    private  ServerConnectionEstablishedHandler serverConnectionEstablishedHandler = new ServerConnectionEstablishedHandler() {
//        @Override
//        public void handler(boolean connected) {
//            try {
//                handler.handler(connected);
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
//        }
//    };

    private MyNettyClient(Context context){
        this.mContext = context;
        sp = context.getSharedPreferences(Params.MESSAGE_SERVICE_PRE_NAME, Context.MODE_PRIVATE);
        init();
    }

    public static MyNettyClient newInstance(Context context){
        if(instance == null){
            synchronized(MyNettyClient.class){
                if(instance == null){
                    instance = new MyNettyClient(context);
                }
            }
        }
        return instance;
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
        if(connected){
            onConnected();
        }
    }

    @Override
    public void unregistServerConnectionEstablishedHandler(ServerConnectionEstablishedHandlerAidl handler){
//        this.handler = handler;
        this.handler = null;
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
        logger.info("netty is Started:"+ started);
        return started;
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
            if(isStarted()){
                super.connect();
            }else {
                super.start();
            }
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    @Override
    public void stop(){
        super.stop();
//        for (ServerConnectionEstablishedHandler handler : connectionEstablishedHandlers) {
//            handler.handler(false);
//            logger.info("MyNettyClient---stop()--->"+false);
//        }
    }

    @Override
    public boolean isConnected(){
        return super.isConnected();
    }

    @Override
    public void connect(){
        try{
            super.connect();
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onConnected(){
        logger.info("onConnected----"+connected);
        try{
            if(handler != null){
                handler.handler(true);
            }else {
                logger.error("还没有注册Netty连接监听，不能通知");
            }
        }catch(RemoteException e){
            e.printStackTrace();
        }
        //        for(ServerConnectionEstablishedHandler connectionEstablishedHandler : connectionEstablishedHandlers){
//            connectionEstablishedHandler.handler(true);
//        }
    }

    @Override
    protected void onPushMessage(byte[] data, int offset, int length){
        for(ServerMessageReceivedHandlerAidl handler : serverMessageReceivedHandlerAidls){
            try {
                handler.handle(data, offset, length);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDisConnect(){
        logger.info("onDisConnect---");
        try{
            if(handler != null){
                handler.handler(false);
            }else {
                logger.error("还没有注册Netty连接监听，不能通知");
            }
        }catch(RemoteException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void trySystemWake(){
//        if(wakeLock != null && wakeLock.isHeld() == false){
//            wakeLock.acquire();
//        }
    }

    @Override
    protected byte getSeq() {
        int byteSeq = sp.getInt(Params.SEQ, 0);
        byteSeq++;
        sp.edit().putInt(Params.SEQ, byteSeq).commit();
        return (byte) byteSeq;
    }
}

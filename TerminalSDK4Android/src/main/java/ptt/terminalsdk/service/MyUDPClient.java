package ptt.terminalsdk.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;

import org.ddpush.im.client.v1.ServerConnectionEstablishedHandler;
import org.ddpush.im.client.v1.UDPClientBase;
import org.ddpush.im.common.v1.handler.PushMessageSendResultHandler;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.PushMessageSendResultHandlerAidl;
import ptt.terminalsdk.ServerConnectionEstablishedHandlerAidl;
import ptt.terminalsdk.ServerMessageReceivedHandlerAidl;

/**
 * Created by ysl on 2017/8/23.
 */

public class MyUDPClient extends UDPClientBase implements IConnectionClient{

    private static MyUDPClient instance;
    private final Context context;
    private WakeLock wakeLock;
    private List<ServerMessageReceivedHandlerAidl> serverMessageReceivedHandlerAidls = new ArrayList<>();
    private SharedPreferences sp;

    private MyUDPClient(Context context){
        this.context = context;
        if(wakeLock == null){
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyUDPClient");
        }
        sp = context.getSharedPreferences(Params.MESSAGE_SERVICE_PRE_NAME, Context.MODE_PRIVATE);
    }

    public static MyUDPClient newInstance(Context context){
        if(instance == null){
            synchronized(MyUDPClient.class){
                if(instance == null){
                    instance = new MyUDPClient(context);
                }
            }
        }
        return instance;
    }

    @Override
    protected boolean hasNetworkConnection() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }
        return false;
    }


    @Override
    protected void trySystemSleep() {
        if(wakeLock != null && wakeLock.isHeld() == true){
            wakeLock.release();
        }
    }

    @Override
    protected void trySystemWake() {
        if(wakeLock != null && wakeLock.isHeld() == false){
            wakeLock.acquire();
        }
    }

    @Override
    protected void onPushMessage(byte[] data, int offset, int length) {
//        logger.info("收到消息 "+ Arrays.toString(data)+"of = "+offset+"  le = "+length+"--------------->"+serverMessageReceivedHandlerAidls.size());
        for(ServerMessageReceivedHandlerAidl handler : serverMessageReceivedHandlerAidls){
            try {
                handler.handle(data, offset, length);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void registMessageReceivedHandler(final ServerMessageReceivedHandlerAidl handler){
        for (ServerMessageReceivedHandlerAidl handler0 : serverMessageReceivedHandlerAidls){
            if (handler0.asBinder() != handler.asBinder()){
            }
        }
        serverMessageReceivedHandlerAidls.add(handler);
    }

    @Override
    public void unregistMessageReceivedHandler(final ServerMessageReceivedHandlerAidl handler) {
        serverMessageReceivedHandlerAidls.remove(handler);
    }

    @Override
    public void sendMessage(byte[] data, final PushMessageSendResultHandlerAidl handler) {
        if(hasNetworkConnection()){
            logger.debug("发送消息："+data+"----->"+handler.getClass());
            sendRealTimeData(data, new PushMessageSendResultHandler() {
                @Override
                public void handler(boolean sendOK, String uuid) {
                    try {
                        handler.handler(sendOK, uuid);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            });
        }else{
            try {
                handler.handler(false, null);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void registServerConnectionEstablishedHandler(final ServerConnectionEstablishedHandlerAidl handler){
        logger.info("MyUDPClient----registServerConnectionEstablishedHandler");
        this.handler = handler;
        registServerConnectionEstablishedHandler(serverConnectionEstablishedHandler);
    }

    @Override
    public void unregistServerConnectionEstablishedHandler(final ServerConnectionEstablishedHandlerAidl handler){
        this.handler = handler;
        unregistServerConnectionEstablishedHandler(serverConnectionEstablishedHandler);
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
        return super.isStarted();
    }

    @Override
    public void start(){
        try{
            super.start();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void stop(boolean isClearHandler){
        try{
            super.stop();
            if(isClearHandler){
                serverMessageReceivedHandlerAidls.clear();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

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
    @Override
    protected byte getSeq() {
        int byteSeq = sp.getInt(Params.SEQ, 0);
        byteSeq++;
        sp.edit().putInt(Params.SEQ, byteSeq).commit();
        return (byte) byteSeq;
    }

    @Override
    public boolean isConnected(){
        return super.isConnected();
    }
}

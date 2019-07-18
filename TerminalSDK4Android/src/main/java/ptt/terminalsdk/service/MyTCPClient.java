package ptt.terminalsdk.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.os.RemoteException;

import org.ddpush.im.client.v1.TCPClientBase;
import org.ddpush.im.common.v1.message.Message;

import java.util.ArrayList;
import java.util.List;

import ptt.terminalsdk.ServerMessageReceivedHandlerAidl;

/**
 * 作者：xuxiaolong
 * 版本：1.0
 * 创建日期：2019/7/18
 * 描述：
 * 修订历史：
 */
public class MyTCPClient extends TCPClientBase{

    private Context context;
    private PowerManager.WakeLock wakeLock;
    private List<ServerMessageReceivedHandlerAidl> serverMessageReceivedHandlerAidls = new ArrayList<>();
    protected int appid = 1;
    private int connectTimeout = 10;

    public MyTCPClient(Context context) throws Exception{
        this.context = context;
    }

    @Override
    public boolean hasNetworkConnection(){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }
        return false;
    }

    @Override
    public void trySystemSleep(){
        if(wakeLock != null && wakeLock.isHeld() == true){
            wakeLock.release();
        }
    }

    @Override
    public void onPushMessage(Message message){
        for(ServerMessageReceivedHandlerAidl handler : serverMessageReceivedHandlerAidls){
            try {
                handler.handle(message.getData(), Message.SERVER_MESSAGE_MIN_LENGTH, message.getContentLength());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void registMessageReceivedHandler(final ServerMessageReceivedHandlerAidl handler){
        for (ServerMessageReceivedHandlerAidl handler0 : serverMessageReceivedHandlerAidls){
            if (handler0.asBinder() != handler.asBinder()){
            }
        }
        serverMessageReceivedHandlerAidls.add(handler);
    }
    public void unregistMessageReceivedHandler(final ServerMessageReceivedHandlerAidl handler) {
        serverMessageReceivedHandlerAidls.remove(handler);
    }
}

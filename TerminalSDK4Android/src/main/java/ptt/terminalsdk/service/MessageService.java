package ptt.terminalsdk.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import cn.vsx.hamster.terminalsdk.tools.Params;
import de.mindpipe.android.logging.log4j.LogConfigurator;
import ptt.terminalsdk.IMessageService;
import ptt.terminalsdk.IMessageService.Stub;
import ptt.terminalsdk.PushMessageSendResultHandlerAidl;
import ptt.terminalsdk.ServerConnectionChangedHandlerAidl;
import ptt.terminalsdk.ServerConnectionEstablishedHandlerAidl;
import ptt.terminalsdk.ServerMessageReceivedHandlerAidl;

/**
 * Created by ysl on 2017/8/15.
 */

public class MessageService extends Service {

    private Logger logger = LoggerFactory.getLogger(MessageService.class);
    private static final String TAG = "MessageService--";
    private IConnectionClient connectionClient;
    private ServerConnectionChangedHandlerAidl serverConnectionChangedHandlerAidl;

    @Override
    public void onCreate() {
        configLogger();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Params.BR_START_CONNECT_CLIENT);
        registerReceiver(receiveStartConnectClientHandler, intentFilter);
        logger.info("MessageService执行了onCreate()");
    }

    @Override
    public void onLowMemory(){
        super.onLowMemory();
        logger.error("MessageService---onLowMemory");
    }

    @Override
    public int onStartCommand(Intent intent,  int flags, int startId) {
        logger.info("MessageService执行了onStartCommand()---flags:"+flags+"------startId:"+startId);
        KeepLiveManager.getInstance().setServiceForeground(this);

        return super.onStartCommand(intent,flags,startId);
    }

    /**
     * 开始连接Client
     * @param intent
     */
    private void startConnect(Intent intent){
        byte[] uuid = new byte[0];
        String accessServerIp = "";
        int accessServerPort = 0;
        String protocolType = Params.UDP;
        if (intent != null) {

            if (intent.hasExtra("uuid")){
                uuid = intent.getByteArrayExtra("uuid");
            }
            if (intent.hasExtra("accessServerIp")){
                accessServerIp = intent.getStringExtra("accessServerIp");
            }
            if (intent.hasExtra("accessServerPort")){
                accessServerPort = intent.getIntExtra("accessServerPort", 0);
            }
            if(intent.hasExtra("protocolType")){
                protocolType = intent.getStringExtra("protocolType");
            }
        }
        logger.info("MessageService ----> onStartCommand---- protocolType:"+protocolType+"--uuid = "+ uuid.length+"  accessServerIp = "+ accessServerIp +"  accessServerPort = "+ accessServerPort);
        initClient(protocolType);
        startClient(uuid,accessServerIp,accessServerPort);
    }

    private synchronized void initClient(String protocolType){
        logger.info("initClient---protocolType:"+protocolType);
        if(Params.TCP.equals(protocolType)){
            connectionClient = MyNettyClient.newInstance(this);
        }else if(Params.UDP.equals(protocolType)){
            connectionClient = MyUDPClient.newInstance(this);
        }else {
            logger.error("没有获取到通信方式！！");
        }
    }
    private void startClient(byte[] uuid, String accessServerIp, int accessServerPort){

        try {
            if(uuid.length != 0 && accessServerIp.length() != 0 && accessServerPort != 0){
                if(connectionClient.isConnected()){
                    logger.info("connectionClient isConnected");
                    connectionClient.stop(false);
                }
                logger.info("MessageService连接到信令服务器，调用了connectionClient的start()");
                connectionClient.setUuid(uuid);
                connectionClient.setServerIp(accessServerIp);
                connectionClient.setServerPort(accessServerPort);
                connectionClient.start();
            }else {
                logger.error("接入服务地址不对！！不能出现这种情况！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("连接到信令服务器时，出现异常", e);

            //连接失败继续重连
//            TerminalFactory.getSDK().connectToServer();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        logger.info("MessageService执行了onBind()");
        return messageServiceStub;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        logger.info("MessageService执行了onUnbind()");
        try {
            if(connectionClient!=null){
                connectionClient.stop(true);
                connectionClient = null;
            }
            logger.info("MessageService调用了connectionClient的stop()");
        } catch (Exception e) {
            logger.error("MessageService调用了onUnbind--e"+e);
            e.printStackTrace();
        }
        // Service 有时被系统杀死，会调用此方法，此时应该重新去连接
        try{
            if(serverConnectionChangedHandlerAidl != null){
                serverConnectionChangedHandlerAidl.handler(false);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        logger.info("MessageService执行了onDestroy()--connectionClient:"+(connectionClient!=null));
        if(receiveStartConnectClientHandler != null){
            unregisterReceiver(receiveStartConnectClientHandler);
        }
        try {
            if(connectionClient!=null){
                connectionClient.stop(true);
            }
            connectionClient = null;
            logger.info("MessageService调用了connectionClient的stop()");
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private IMessageService.Stub  messageServiceStub= new Stub() {

        @Override
        public void registMessageReceivedHandler(final ServerMessageReceivedHandlerAidl handler) throws RemoteException {
            logger.info(TAG+"registMessageReceivedHandler");
            if(connectionClient!=null){
                connectionClient.registMessageReceivedHandler(handler);
            }
        }

        @Override
        public void unregistMessageReceivedHandler(final ServerMessageReceivedHandlerAidl handler) throws RemoteException {
            if(connectionClient!=null){
                connectionClient.unregistMessageReceivedHandler(handler);
            }
        }

        @Override
        public void sendMessage(byte[] data, final PushMessageSendResultHandlerAidl handler) {
            if(connectionClient!=null){
                connectionClient.sendMessage(data, handler);
            }
        }

        @Override
        public void registServerConnectionEstablishedHandler(final ServerConnectionEstablishedHandlerAidl handler) throws RemoteException {
            logger.info(TAG+"registServerConnectionEstablishedHandler");
            if(connectionClient!=null){
                connectionClient.registServerConnectionEstablishedHandler(handler);
            }
        }

        @Override
        public void unregistServerConnectionEstablishedHandler(ServerConnectionEstablishedHandlerAidl handler) throws RemoteException {
            logger.info(TAG+"unregistServerConnectionEstablishedHandler");
            if(connectionClient!=null){
                connectionClient.unregistServerConnectionEstablishedHandler(handler);
            }
        }

        @Override
        public void initConnectionClient(String protocolType) throws RemoteException{
            logger.info("initConnectionClient--"+protocolType);
            initClient(protocolType);
        }

        @Override
        public void registServerConnectionChangedHandler(ServerConnectionChangedHandlerAidl handler) throws RemoteException{
            MessageService.this.serverConnectionChangedHandlerAidl = handler;
        }

        @Override
        public void unregistServerConnectionChangedHandler() throws RemoteException{
            MessageService.this.serverConnectionChangedHandlerAidl = null;
        }
    };

    @SuppressLint("NewApi")/**日志生成文件保存*/
    public void configLogger() {

        File dir = new File(getLogDirectory());
        if (!dir.exists()) {
            try {
                //按照指定的路径创建文件夹
                dir.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        File file = new File(getLogDirectory() + "log.txt");
        if (!file.exists()) {
            try {
                //在指定的文件夹中创建文件
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        LogConfigurator logConfigurator = new LogConfigurator();
        logConfigurator.setFileName(getLogDirectory() + "log.txt");
        logConfigurator.setRootLevel(Level.INFO);
        logConfigurator.setFilePattern("%d %-5p [%t][%c{2}]-[%l] %m%n");
        logConfigurator.setUseLogCatAppender(true);
        logConfigurator.setMaxFileSize(1024 * 1024 * 10);
        logConfigurator.setMaxBackupSize(0);
        logConfigurator.setImmediateFlush(true);
        logConfigurator.configure();
    }

    /**得到日志的存储位置*/
    public String getLogDirectory() {
        return Environment.getExternalStorageDirectory()
                + File.separator + this.getApplicationInfo().loadLabel(this.getPackageManager())+ File.separator + "logs"
                + File.separator;
    }

    /**
     * 通知开始连接Client
     */
    private BroadcastReceiver receiveStartConnectClientHandler = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent){
            String action = intent.getAction();
            if(action != null && TextUtils.equals(Params.BR_START_CONNECT_CLIENT,action)){
                logger.info("receiveStartConnectClientHandler--开始连接服务器");
                startConnect(intent);
            }
        }
    };
}

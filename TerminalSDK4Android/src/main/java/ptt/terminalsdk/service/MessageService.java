package ptt.terminalsdk.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import cn.vsx.hamster.terminalsdk.tools.Params;
import de.mindpipe.android.logging.log4j.LogConfigurator;
import ptt.terminalsdk.IMessageService;
import ptt.terminalsdk.IMessageService.Stub;
import ptt.terminalsdk.PushMessageSendResultHandlerAidl;
import ptt.terminalsdk.ServerConnectionEstablishedHandlerAidl;
import ptt.terminalsdk.ServerMessageReceivedHandlerAidl;

/**
 * Created by ysl on 2017/8/15.
 */

public class MessageService extends Service {

    private Logger logger = LoggerFactory.getLogger(MessageService.class);
    private Handler mHandler = new Handler();
    private static final String TAG = "MessageService--";
    private IConnectionClient connectionClient;

    @Override
    public void onCreate() {
        configLogger();
        logger.info("MessageService执行了onCreate()");
    }

    @Override
    public void onLowMemory(){
        super.onLowMemory();
        logger.error("MessageService---onLowMemory");
    }

    @Override
    public int onStartCommand(Intent intent,  int flags, int startId) {
        logger.info("MessageService执行了onStartCommand()");
        KeepLiveManager.getInstance().setServiceForeground(this);
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
        logger.info("MessageService ----> onStartCommand： protocolType:"+protocolType+"--uuid = "+uuid+"  accessServerIp = "+ accessServerIp +"  accessServerPort = "+ accessServerPort);
        initClient(protocolType);
        startClient(uuid,accessServerIp,accessServerPort);
        return START_STICKY;
    }

    private synchronized void initClient(String protocolType){
        logger.info("initClient---protocolType:"+protocolType);
        if(connectionClient == null){
            if(Params.TCP.equals(protocolType)){
                connectionClient = new MyNettyClient(this);
            }else if(Params.UDP.equals(protocolType)){
                connectionClient = new MyUDPClient(this);
            }else {
                logger.error("没有获取到通信方式！！");
            }
        }
    }
    private void startClient(byte[] uuid, String accessServerIp, int accessServerPort){

        try {
            if(uuid.length != 0 && accessServerIp.length() != 0 && accessServerPort != 0){
                if(connectionClient.isStarted()){
                    connectionClient.stop();
                }
                connectionClient.setUuid(uuid);
                connectionClient.setServerIp(accessServerIp);
                connectionClient.setServerPort(accessServerPort);
                connectionClient.start();
                logger.info("MessageService连接到信令服务器，调用了connectionClient的start()");
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
            connectionClient.stop();
            connectionClient = null;
            logger.info("MessageService调用了UDPClientBase的stop()");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        logger.info("MessageService执行了onDestroy()");
        try {
            connectionClient.stop();
            connectionClient = null;
            logger.info("MessageService调用了UDPClientBase的stop()");
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private IMessageService.Stub  messageServiceStub= new Stub() {

        @Override
        public void registMessageReceivedHandler(final ServerMessageReceivedHandlerAidl handler) throws RemoteException {
            logger.info(TAG+"registMessageReceivedHandler");
            connectionClient.registMessageReceivedHandler(handler);
        }

        @Override
        public void unregistMessageReceivedHandler(final ServerMessageReceivedHandlerAidl handler) throws RemoteException {
            connectionClient.unregistMessageReceivedHandler(handler);
        }

        @Override
        public void sendMessage(byte[] data, final PushMessageSendResultHandlerAidl handler) {
            connectionClient.sendMessage(data, handler);
        }

        @Override
        public void registServerConnectionEstablishedHandler(final ServerConnectionEstablishedHandlerAidl handler) throws RemoteException {
            logger.info(TAG+"registServerConnectionEstablishedHandler");
            connectionClient.registServerConnectionEstablishedHandler(handler);
        }

        @Override
        public void unregistServerConnectionEstablishedHandler(ServerConnectionEstablishedHandlerAidl handler) throws RemoteException {
            logger.info(TAG+"unregistServerConnectionEstablishedHandler");
            connectionClient.unregistServerConnectionEstablishedHandler(handler);
        }

        @Override
        public void initConnectionClient(String protocolType) throws RemoteException{
            logger.info("initConnectionClient--"+protocolType);
            initClient(protocolType);
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
}

package ptt.terminalsdk.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

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
    private MyUDPClient myUDPClient;
    private String accessServerIpTemp;
    private int accessServerPortTemp;

    @Override
    public void onCreate() {
        configLogger();
        myUDPClient = new MyUDPClient(this);
        logger.info("MessageService执行了onCreate()");
    }

    @Override
    public void onLowMemory(){
        super.onLowMemory();
        Log.e("MessageService", "onLowMemory");
    }

    @Override
    public int onStartCommand(Intent intent,  int flags, int startId) {
        logger.info("MessageService执行了onStartCommand()");
        KeepLiveManager.getInstance().setServiceForeground(this);
        byte[] uuid = new byte[0];
        String accessServerIp = "";
        int accessServerPort = 0;

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
        }

        logger.info("MessageService ----> onStartCommand： uuid = "+uuid+"  accessServerIp = "+ accessServerIp +"  accessServerPort = "+ accessServerPort);
        try {
            if(uuid.length != 0 && accessServerIp.length() != 0 && accessServerPort != 0){
                logger.info("accessServerIpTemp = "+accessServerIpTemp+"  accessServerPortTemp = "+accessServerPortTemp);
                if (!accessServerIp.equals(accessServerIpTemp) || accessServerPort != accessServerPortTemp){
                    myUDPClient.stop();
                    accessServerIpTemp = accessServerIp;
                    accessServerPortTemp = accessServerPort;
                }
                myUDPClient.setUuid(uuid);
                myUDPClient.setServerIp(accessServerIp);
                myUDPClient.setServerPort(accessServerPort);
                myUDPClient.start();
                logger.info("MessageService连接到信令服务器，调用了UDPClientBase的start()");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("连接到信令服务器时，出现异常", e);
        }
        return START_STICKY;
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
            myUDPClient.stop();
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
            myUDPClient.stop();
            logger.info("MessageService调用了UDPClientBase的stop()");
        } catch (Exception e) {
            e.printStackTrace();
        }
//        startService(new Intent(this, MessageService.class));
//        startService(new Intent(this,OnlineService.class));
//        bindService(new Intent(this,OnlineService.class), onlineServiceConn, Context.BIND_IMPORTANT);
        super.onDestroy();
    }

    private IMessageService.Stub  messageServiceStub= new Stub() {

        @Override
        public void registMessageReceivedHandler(final ServerMessageReceivedHandlerAidl handler) throws RemoteException {
            myUDPClient.registMessageReceivedHandler(handler);
        }

        @Override
        public void unregistMessageReceivedHandler(final ServerMessageReceivedHandlerAidl handler) throws RemoteException {
            myUDPClient.unregistMessageReceivedHandler(handler);
        }

        @Override
        public void sendMessage(byte[] data, final PushMessageSendResultHandlerAidl handler) {
            myUDPClient.sendMessage(data, handler);
        }

        @Override
        public void registServerConnectionEstablishedHandler(final ServerConnectionEstablishedHandlerAidl handler) throws RemoteException {
            myUDPClient.registServerConnectionEstablishedHandler(handler);
        }

        @Override
        public void unregistServerConnectionEstablishedHandler(ServerConnectionEstablishedHandlerAidl handler) throws RemoteException {
            myUDPClient.unregistServerConnectionEstablishedHandler(handler);
        }

    };


//    private ServiceConnection onlineServiceConn = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//        }
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            logger.error("onlineServiceConn服务断开连接，重新启动并绑定");
//            MessageService.this.startService(new Intent(MessageService.this,OnlineService.class));
//            MessageService.this.bindService(new Intent(MessageService.this,OnlineService.class), onlineServiceConn, Context.BIND_IMPORTANT);
//        }
//    };

    @SuppressLint("NewApi")/**日志生成文件保存*/
    public void configLogger() {

        File dir = new File(getLogDirectory());
        if (!dir.exists()) {
            try {
                //按照指定的路径创建文件夹
                dir.mkdirs();
            } catch (Exception e) {
            }
        }
        File file = new File(getLogDirectory() + "log.txt");
        if (!file.exists()) {
            try {
                //在指定的文件夹中创建文件
                file.createNewFile();
            } catch (Exception e) {
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

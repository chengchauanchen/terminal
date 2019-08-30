package cn.vsx.vc.jump.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import org.apache.log4j.Logger;

import cn.vsx.hamster.terminalsdk.TerminalSDKBaseImpl;
import cn.vsx.vc.IJump;
import cn.vsx.vc.jump.command.FactoryCommand;
import cn.vsx.vc.jump.sendMessage.ThirdSendMessage;

/**
 * 第三方应用与本应用通信Service
 */
public class JumpService extends Service {

    protected static Logger logger = Logger.getLogger(JumpService.class);

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new JumpBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public class JumpBinder extends IJump.Stub {

        @Override
        public void jumpPage(String sendJson, int commandType) throws RemoteException {
            FactoryCommand.getInstance(getApplicationContext()).getJumpCommand(commandType).jumpPage(sendJson);
        }

        /**
         * 第三方app->通知我->去试图连接 第三方app的消息接收服务
         * @throws RemoteException
         */
        @Override
        public void noticeConnectReceivedService(String packageName) throws RemoteException {
            logger.info("--vsxSDK--"+"接收到"+packageName+"发送过来的连接ReceivedService通知");
            if(ThirdSendMessage.getInstance()!=null){
                ThirdSendMessage.getInstance().connectReceivedService(getApplicationContext(),packageName,false);
            }
        }
    }



}

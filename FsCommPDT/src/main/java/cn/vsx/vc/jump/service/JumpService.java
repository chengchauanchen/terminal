package cn.vsx.vc.jump.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import cn.vsx.vc.IJump;
import cn.vsx.vc.jump.command.FactoryCommand;

/**
 * 第三方应用与本应用通信Service
 */
public class JumpService extends Service {

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
    }
}

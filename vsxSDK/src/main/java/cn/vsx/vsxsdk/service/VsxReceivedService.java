package cn.vsx.vsxsdk.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import cn.vsx.vsxsdk.IReceivedVSXMessage;
import cn.vsx.vsxsdk.VsxSDK;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/7/1
 * 描述：
 * 修订历史：
 */
public class VsxReceivedService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return new ReceivedMessageBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public class ReceivedMessageBinder extends IReceivedVSXMessage.Stub {

        @Override
        public void receivedMessage(String messageJson, int messageType) throws RemoteException {
            Log.e("VsxReceivedService接收:", messageJson);
            VsxSDK.getInstance().getRegistMessageListener().receivedMessage(messageJson, messageType);
        }

        //我-->连接第三方message服务-->成功后-我-->通知第三方 可以连接我的jump服务
        @Override
        public void noticeConnectJumpService() throws RemoteException {
            VsxSDK.getInstance().connectJumpService(VsxReceivedService.this,false);
        }
    }
}

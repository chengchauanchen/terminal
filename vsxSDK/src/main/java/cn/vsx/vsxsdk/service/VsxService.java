package cn.vsx.vsxsdk.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/7/1
 * 描述：
 * 修订历史：
 */
public class VsxService extends Service{

    @Override
    public void onCreate(){
        super.onCreate();

    }

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

//    private ReceiveEventAidl.Stub  messageServiceStub= new ReceiveEventAidl.Stub(){
//        @Override
//        public void receiveMessage() throws RemoteException{
//
//        }
//    };
}

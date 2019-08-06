package com.vsxin.terminalpad.utils;

/**
 * 接收消息服务 管理类
 */
public class ReceiveHandlerServiceManager {

//    /**
//     * 获取到悬浮窗权限之后需要调用
//     */
//    public void startHandlerService() {
//        Intent intent = new Intent(this,ReceiveHandlerService.class);
//        isBinded=bindService(intent,conn,BIND_AUTO_CREATE);
//    }
//
//    private ServiceConnection conn = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//        }
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            Log.e("MyApplication", "ReceiveHandlerService服务断开了");
//        }
//    };
//
//    public void stopHandlerService(){
//        if (conn != null) {
//
//            Log.i("服务状态1：",""+conn);
//            Log.i("服务状态2：",""+isBinded);
//            if (isBinded) {
//                unbindService(conn);
//                isBinded=false;
//            }
//            stopService(new Intent(this, ReceiveHandlerService.class));
//            killAllProcess();
//        }
//    }

}

package cn.vsx.vsxsdk.utils;

import android.os.Handler;
import android.os.Message;

/**
 * 定时执行 启动融合通信后台服务
 */
public class BackgroundServicesTimer {
    private static final int time = 10 * 1000;//10秒
    private static final int UPDATE = 0;
    private TimerListener listener;
    private static BackgroundServicesTimer backgroundServicesTimer;

    private BackgroundServicesTimer(TimerListener listener) {
        this.listener = listener;
    }

    public static BackgroundServicesTimer newInstance(TimerListener listener){
         if(backgroundServicesTimer == null){
             backgroundServicesTimer = new BackgroundServicesTimer(listener);
         }
        return backgroundServicesTimer;
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            if(msg.what == UPDATE){
                mHandler.removeMessages(UPDATE);
                if (listener != null) {
                    listener.time();
                }
                mHandler.sendEmptyMessageDelayed(UPDATE,time);
            }
        }
    };


    public void start() {
        if(mHandler.hasMessages(UPDATE)){
            mHandler.removeMessages(UPDATE);
        }
        mHandler.sendEmptyMessage(UPDATE);
    }

    public void stop() {
        mHandler.removeCallbacksAndMessages(null);
    }

    public interface TimerListener {
        void time();
    }
}

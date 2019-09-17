package cn.vsx.vsxsdk.utils;

import android.os.Handler;

/**
 * 定时执行 启动融合通信后台服务
 */
public class BackgroundServicesTimer {
    private static final int time = 20 * 1000;//10秒

    private TimerListener listener;

    public BackgroundServicesTimer(TimerListener listener) {
        this.listener = listener;
    }

    private Handler mHandler = new Handler();

    private Runnable updateThread = new Runnable() {

        @Override
        public void run() {
            if (listener != null) {
                listener.time();
            }
            mHandler.postDelayed(updateThread, time);
        }
    };


    public void start() {
        mHandler.post(updateThread);
    }

    public void stop() {
        mHandler.removeCallbacks(updateThread);
    }

    public interface TimerListener {
        void time();
    }
}

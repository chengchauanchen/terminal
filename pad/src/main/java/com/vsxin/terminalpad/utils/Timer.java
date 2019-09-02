package com.vsxin.terminalpad.utils;

import android.os.Handler;

public class Timer {
    private TimerListener listener;

    public Timer(TimerListener listener) {
        this.listener = listener;
    }

    private Handler mHandler = new Handler();

    private Runnable updateThread = new Runnable() {

        @Override
        public void run() {
            if (listener != null) {
                listener.time();
            }
            mHandler.postDelayed(updateThread, 1000);
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

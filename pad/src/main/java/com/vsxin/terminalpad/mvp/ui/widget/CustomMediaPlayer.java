package com.vsxin.terminalpad.mvp.ui.widget;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.IOException;

public class CustomMediaPlayer implements OnPreparedListener, OnErrorListener, OnCompletionListener, OnSeekCompleteListener {
    private MediaPlayer mPlayer;
    private boolean hasPrepared;

    private void initIfNecessary() {
        if (null == mPlayer) {
            mPlayer = new MediaPlayer();
            mPlayer.setOnErrorListener(this);
            mPlayer.setOnSeekCompleteListener(this);
            mPlayer.setOnCompletionListener(this);
            mPlayer.setOnPreparedListener(this);
        }
    }

    public void play(String audioPath) {
        hasPrepared = false; // 开始播放前讲Flag置为不可操作
        initIfNecessary(); // 如果是第一次播放/player已经释放了，就会重新创建、初始化
        try {
            mPlayer.reset();
            mPlayer.setDataSource(audioPath); // 设置曲目资源
            mPlayer.prepareAsync(); // 异步的准备方法
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        // release()会释放player、将player置空，所以这里需要判断一下
        if (null != mPlayer && hasPrepared) {
            mPlayer.start();
        }
    }

    public void stop() {
        // release()会释放player、将player置空，所以这里需要判断一下
        if (null != mPlayer && hasPrepared) {
            mPlayer.stop();
        }
    }

    public void pause() {
        if (null != mPlayer && hasPrepared) {
            mPlayer.pause();
        }
    }

    public void seekTo(int position) {
        if (null != mPlayer && hasPrepared) {
            mPlayer.seekTo(position);
        }
    }

    // 对于播放视频来说，通过设置SurfaceHolder来设置显示Surface。这个方法不需要判断状态、也不会改变player状态
    public void setDisplay(SurfaceHolder holder) {
        if (null != mPlayer) {
            mPlayer.setDisplay(holder);
        }
    }

    /**
     * 设置 Surface
     *
     * @param surface
     */
    public void setSurface(Surface surface) {
        if (null != mPlayer) {
            mPlayer.setSurface(surface);
        }
    }

    public void release() {
        hasPrepared = false;
        if(mPlayer!=null){
            mPlayer.stop();
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
        }
        if (playerListener != null) {
            playerListener.onStop();
        }
    }

    /**
     * 获取当前播放进度
     *
     * @return
     */
    public int getCurrentPosition() {
        return mPlayer != null ? mPlayer.getCurrentPosition() : 0;
    }

    /**
     * 获取视频时长
     *
     * @return
     */
    public int getDuration() {
        return mPlayer != null ? mPlayer.getDuration() : 0;
    }

    public boolean isPlaying() {
        return mPlayer != null && mPlayer.isPlaying();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.e("CustomMediaPlayer", "onPrepared");
        hasPrepared = true; // 准备完成后回调到这里
        start();
        if (playerListener != null) {
            playerListener.onPrepared(mp);
        }
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        Log.e("CustomMediaPlayer", "onSeekComplete");
        if (playerListener != null) {
            playerListener.onSeekComplete(mp);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.e("CustomMediaPlayer", "onCompletion");
        hasPrepared = false;
        // 通知调用处，调用play()方法进行下一个曲目的播放
        if (playerListener != null) {
            playerListener.onCompletion(mp);
        }
        //重新播放
        //release();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e("CustomMediaPlayer", "onError");
        hasPrepared = false;
        if (playerListener != null) {
            playerListener.onError(mp);
        }
        return false;
    }

    PlayerListener playerListener;

    public void setListener(PlayerListener listener) {
        this.playerListener = listener;
    }

    public interface PlayerListener {
        void onPrepared(MediaPlayer mp);

        void onSeekComplete(MediaPlayer mp);

        void onCompletion(MediaPlayer mp);

        void onError(MediaPlayer mp);

        void onStop();
    }
}

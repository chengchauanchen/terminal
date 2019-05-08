package cn.vsx.vc.utils;//package com.zectec.a4gptt_tv.utils;
//
//import android.util.Log;
//import android.view.View;
//import android.widget.LinearLayout;
//import android.widget.MediaController;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//
//import org.videolan.vlc.VlcVideoView;
//import org.videolan.vlc.listener.MediaListenerEvent;
//import org.videolan.vlc.listener.MediaPlayerControl;
//
///**
// * author: zjx.
// * data:on 2017/11/16
// */
//
//public class MediaControl implements MediaController.MediaPlayerControl, MediaListenerEvent {
//    MediaPlayerControl mediaPlayer;
//    final MediaController controller;
//    TextView logInfo;
//    String tag = "MediaControl";
//    LinearLayout linearLayout;
//    ProgressBar progressBar;
//
//    public MediaControl(VlcVideoView mediaPlayer, LinearLayout linearLayout , ProgressBar progressBar, TextView logInfo) {
//        this.mediaPlayer = mediaPlayer;
//        this.logInfo = logInfo;
//        this.linearLayout = linearLayout;
//        this.progressBar = progressBar;
//
//        controller = new MediaController(mediaPlayer.getContext());
//        controller.setMediaPlayer(this);
//        controller.setAnchorView(mediaPlayer);
////        mediaPlayer.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                if (!controller.isShowing())
////                    controller.show();
////                else
////                    controller.hide();
////            }
////        });
//    }
//
//
//    @Override
//    public void start() {
//        mediaPlayer.start();
//    }
//
//    @Override
//    public void pause() {
//        mediaPlayer.pause();
//    }
//
//    @Override
//    public int getDuration() {
//        return (int) mediaPlayer.getDuration();
//    }
//
//    @Override
//    public int getCurrentPosition() {
//        return (int) mediaPlayer.getCurrentPosition();
//    }
//
//    @Override
//    public void seekTo(int pos) {
//        mediaPlayer.seekTo(pos);
//    }
//
//    @Override
//    public boolean isPlaying() {
//        return mediaPlayer.isPlaying();
//    }
//
//    @Override
//    public int getBufferPercentage() {
//        return mediaPlayer.getBufferPercentage();
//    }
//
//    @Override
//    public boolean canPause() {
//        return mediaPlayer.isPrepare();
//    }
//
//    @Override
//    public boolean canSeekBackward() {//快退
//        return true;
//    }
//
//    @Override
//    public boolean canSeekForward() {//快进
//        return true;
//    }
//
//    @Override
//    public int getAudioSessionId() {
//        return 0;
//    }
//
//
//    long time;
//
//    @Override
//    public void eventBuffing(int event, float buffing) {
//
//    }
//
//    @Override
//    public void eventPlayInit(boolean openClose) {
//        if (openClose) {
//            Log.i(tag, "打开时间  00000");
//            time = System.currentTimeMillis();
//        }
//        logInfo.setText("加载中...");
//        linearLayout.setVisibility(View.VISIBLE);
//        progressBar.setVisibility(View.VISIBLE);
//        logInfo.setVisibility(View.VISIBLE);
//    }
//
//    @Override
//    public void eventStop(boolean isPlayError) {
//        logInfo.setText("Stop" + (isPlayError ? "  播放已停止   有错误" : ""));
//        linearLayout.setVisibility(View.VISIBLE);
//        progressBar.setVisibility(View.GONE);
//        logInfo.setVisibility(View.VISIBLE);
//    }
//
//    @Override
//    public void eventError(int error, boolean show) {
//        logInfo.setText("地址 出错了 error=" + error);
//        linearLayout.setVisibility(View.VISIBLE);
//        progressBar.setVisibility(View.GONE);
//        logInfo.setVisibility(View.VISIBLE);
//    }
//
//    @Override
//    public void eventPlay(boolean isPlaying) {
//        if (isPlaying) {
////            controller.show();
//            Log.i(tag, "打开时间是 time=" + (System.currentTimeMillis() - time));
//            logInfo.setVisibility(View.GONE);
//            linearLayout.setVisibility(View.GONE);
//            progressBar.setVisibility(View.GONE);
//        }
//
//    }
//}

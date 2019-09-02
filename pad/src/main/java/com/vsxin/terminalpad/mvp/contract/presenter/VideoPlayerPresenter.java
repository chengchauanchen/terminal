package com.vsxin.terminalpad.mvp.contract.presenter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.View;

import com.ixiaoma.xiaomabus.architecture.mvp.BasePresenter;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.view.ILivePlayerView;
import com.vsxin.terminalpad.mvp.contract.view.IVideoPlayerView;
import com.vsxin.terminalpad.mvp.entity.MediaBean;
import com.vsxin.terminalpad.prompt.PromptManager;
import com.vsxin.terminalpad.utils.SensorUtil;
import com.vsxin.terminalpad.utils.StateMachineUtils;

import org.easydarwin.video.EasyRTSPClient;
import org.easydarwin.video.RTSPClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNetworkChangeHandler;
import cn.vsx.hamster.terminalsdk.tools.DateUtils;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * @author qzw
 * <p>
 * 视频播放器控件--用于播放历史上报视频
 */
public class VideoPlayerPresenter extends BasePresenter<IVideoPlayerView> {

    private static final int UPDATE_PROGRESS = 0;
    private static final int COMPLETE_PROGRESS = 1;
    private static final int HIDE_SEEK_BAR = 2;
    private static final int RECEIVEVOICECHANGED = 3;
    private static final int GETDATA = 4;
    private List<MediaBean> mediaBeans = new ArrayList<>();
    private int currentMediaBeanPosition;
    private boolean playFinish = false;

    public VideoPlayerPresenter(Context mContext) {
        super(mContext);
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch(msg.what){
                case UPDATE_PROGRESS://更新进度
                    mHandler.removeMessages(UPDATE_PROGRESS);
                    if(null != getView().getMediaPlayer()){
                        float position = getView().getMediaPlayer().getCurrentPosition();
                        Log.e("PlayLiveHistoryActivity", "position:" + position);
                        if(position < 0){
                            return;
                        }
                        //播放比例
                        float percent = position / getView().getMaxTime();
                        Log.e("PlayLiveHistoryActivity", "position:" + position + "--percent:" + percent);
                        if(percent < 1){
                            getView().updateProgress(percent);
                            //每秒刷一次
                            mHandler.sendEmptyMessageDelayed(UPDATE_PROGRESS, 1000);
                        }
                    }
                    break;
                case COMPLETE_PROGRESS://完成\进度
                    getView().getLogger().info("currentMediaBeanPosition:"+currentMediaBeanPosition);
                    if(currentMediaBeanPosition < mediaBeans.size()-1){
                        playNext(currentMediaBeanPosition+1);//一个播完了，接着播下一个
                    }else {//没有下一个直接停止
                        if(null != getView().getMediaPlayer() && getView().getMediaPlayer().isPlaying()){
                            getView().getMediaPlayer().pause();
                        }
                        getView().setPauseContinue(R.drawable.on_pause);
                    }
                    break;
                case HIDE_SEEK_BAR://隐藏搜索栏
                    getView().isShowSeekBar(false);
                    break;
                case GETDATA://获取数据
                    break;
                default:
                    break;
            }
        }
    };


    protected void onPause(){
        try{
            if(getView().getMediaPlayer() != null && getView().getMediaPlayer().isPlaying()){
                float currentPosition  = getView().getMediaPlayer().getCurrentPosition();
                getView().getMediaPlayer().pause();
                getView().setPauseContinue(R.drawable.on_pause);
                getView().isShowPause(true);
                mHandler.removeMessages(UPDATE_PROGRESS);
            }
        }catch(IllegalStateException e){
            e.printStackTrace();
        }
    }

    protected void onResume(){
        if(getView().getMediaPlayer() != null){
            getView().getMediaPlayer().start();

            getView().setPauseContinue(R.drawable.continue_play);
            getView().isShowPause(false);
            mHandler.sendEmptyMessage(UPDATE_PROGRESS);
            mHandler.postDelayed(this::pauseOrContinue,100);
        }
    }

    public void pauseOrContinue(){
        try{
            if(isNetConnected){
                if(null != getView().getMediaPlayer() && getView().getMediaPlayer().isPlaying()){
                    getView().getMediaPlayer().pause();
                    getView().setPauseContinue(R.drawable.on_pause);
                    getView().isShowPause(true);
                    mHandler.removeMessages(UPDATE_PROGRESS);
                }else{
                    if(playFinish){
                        if(currentMediaBeanPosition < mediaBeans.size()-1){
                            playNext(currentMediaBeanPosition+1);
                        }else {
                            playNext(0);
                        }

                    }else{
                        if(null != getView().getMediaPlayer()){
                            getView().getMediaPlayer().start();
                        }
                    }
                    getView().setPauseContinue(R.drawable.continue_play);
                    getView().isShowPause(false);
                    mHandler.sendEmptyMessage(UPDATE_PROGRESS);
                    mHandler.sendEmptyMessageDelayed(HIDE_SEEK_BAR, 2000);
                }
            }else{
                ToastUtil.showToast(getContext(), "网络连接已断开");
            }
        }catch(IllegalStateException e){
            e.printStackTrace();
        }
    }

    private boolean isNetConnected =true;

    private ReceiveNetworkChangeHandler receiveNetworkChangeHandler = new ReceiveNetworkChangeHandler(){
        @Override
        public void handler(boolean connected){
            isNetConnected = connected;
            mHandler.post(new Runnable(){
                @Override
                public void run(){
                    if(!isNetConnected){
                        if(getView().getMediaPlayer() != null && getView().getMediaPlayer().isPlaying()){
                            getView().getMediaPlayer().pause();
                            getView().setPauseContinue(R.drawable.on_pause);
                            getView().isShowPause(true);
                            mHandler.removeMessages(UPDATE_PROGRESS);
                        }
                    }else{
                        if(getView().getMediaPlayer() != null){
                            getView().setPauseContinue(R.drawable.continue_play);
                            getView().isShowPause(false);
                            getView().getMediaPlayer().start();
                            mHandler.sendEmptyMessage(UPDATE_PROGRESS);
                            mHandler.sendEmptyMessageDelayed(HIDE_SEEK_BAR, 2000);
                        }
                    }
                }
            });
        }
    };


    private void playNext(int position){
        //将上一个播放的不选中
        mediaBeans.get(currentMediaBeanPosition).setSelected(false);
        currentMediaBeanPosition = position;
        mediaBeans.get(currentMediaBeanPosition).setSelected(true);
//        playLiveAdapter.notifyDataSetChanged();
        String url = mediaBeans.get(currentMediaBeanPosition).getUrl();
        play(url);
    }


    private void close(){
        mHandler.removeCallbacksAndMessages(null);
        destroyMediaPlayer();
    }

    private void destroyMediaPlayer(){
        try{
            if(getView().getMediaPlayer() != null){
                getView().getMediaPlayer().stop();
                getView().getMediaPlayer().release();
                getView().setMediaPlayer(null);
            }
        }catch(IllegalStateException e){
            e.printStackTrace();
        }
    }

    /**
     * 还原SeekBar
     */
    private void revertSeekBar(){
        mHandler.removeMessages(UPDATE_PROGRESS);
        mHandler.removeMessages(COMPLETE_PROGRESS);
        getView().setSeekBarProgress(0);
    }

    /**
     * 还原媒体播放器
     */
    private void revertMediaPlayer(){
        destroyMediaPlayer();
        revertSeekBar();
    }


    public void play(String url) {
        if (TextUtils.isEmpty(url)) {
            ToastUtil.showToast(getContext(), "url为空，不能播放");
        } else {
            try {
                initMediaPlayer(url);
            } catch (Exception e) {
                getView().getLogger().error(e);
            }
        }
    }


    private void initMediaPlayer(String url) throws IOException {
        if (getView().getMediaPlayer() == null) {
            getView().setMediaPlayer(new MediaPlayer());
            getView().getMediaPlayer().setAudioStreamType(AudioManager.STREAM_MUSIC);
            getView().getMediaPlayer().setDataSource(url);
            getView().getMediaPlayer().prepareAsync();
        }
        /**
         * 准备完成 加载视频
         * 可以知道时长
         */
        getView().getMediaPlayer().setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                if (null != getView().getTextureView().getSurfaceTexture()) {
                    Surface face = new Surface(getView().getTextureView().getSurfaceTexture());
                    getView().getMediaPlayer().setSurface(face);
                }
                playFinish = false;

                //设置视频时长
                getView().setMaxTime(mp.getDuration());
                mp.start();
            }
        });

        /**
         * 让播放器从 指定的位置开始播放
         *
         * 快进/回退
         */
        getView().getMediaPlayer().setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {
                mp.start();
            }
        });

        /**
         * 处理播放 结束后
         */
        getView().getMediaPlayer().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                getView().getLogger().info("onCompletion");
                playFinish = false;
                //重新播放
                getView().getMediaPlayer().reset();
                getView().getMediaPlayer().release();
                getView().setMediaPlayer(null);
            }
        });

        /**
         * 处理播放过程中遇到 错误的监听
         */
        getView().getMediaPlayer().setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                getView().getLogger().error("mediaPlayer  onError");
                if (getView().getMediaPlayer().isPlaying()) {
                    getView().getMediaPlayer().stop();
                }
                getView().getMediaPlayer().reset();
                getView().getMediaPlayer().release();
                getView().setMediaPlayer(null);
                return false;
            }
        });
    }

}

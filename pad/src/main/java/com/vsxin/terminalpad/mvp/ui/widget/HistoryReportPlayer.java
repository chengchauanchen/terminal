package com.vsxin.terminalpad.mvp.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.ixiaoma.xiaomabus.architecture.mvp.view.layout.MvpFrameLayout;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.presenter.HistoryReportPlayerPresenter;
import com.vsxin.terminalpad.mvp.contract.view.IHistoryReportPlayerView;
import com.vsxin.terminalpad.mvp.entity.MediaBean;
import com.vsxin.terminalpad.utils.NiceUtil;
import com.vsxin.terminalpad.utils.Timer;
import com.vsxin.terminalpad.utils.Timer.TimerListener;

import java.io.IOException;
import java.util.List;

import cn.vsx.hamster.terminalsdk.tools.DateUtils;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * @author qzw
 * <p>
 * 视频播放器控件--用于播放历史上报视频
 */
public class HistoryReportPlayer extends MvpFrameLayout<IHistoryReportPlayerView, HistoryReportPlayerPresenter> implements IHistoryReportPlayerView, SurfaceTextureListener {

    /**
     * 普通(小屏)模式
     **/
    public static final int MODE_NORMAL = 10;
    /**
     * 全屏模式
     **/
    public static final int MODE_FULL_SCREEN = 11;
    /**
     * 大屏到小屏 切换
     */
    public static final int MODE_SMALL_SCREEN = 12;

    /**
     * 当前屏幕模式，小屏/全屏
     */
    private int mCurrentMode = MODE_NORMAL;

    private FrameLayout rootLayout;
    private TextureView mTextureView;

    private MediaPlayer mediaPlayer;
    private List<MediaBean> testData;
    private SurfaceTexture mSurfaceTexture;

    private View historyReportPlayerCoverView;

    private HistoryReportPlayerListener listener;

    public HistoryReportPlayer(Context context) {
        super(context);
    }

    public HistoryReportPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView(){
        //testData = getPresenter().getTestData();
        firstFrameLayout();
        initTextureView();
        addTextureView();
    }

    public void setListener(HistoryReportPlayerListener listener) {
        this.listener = listener;
    }

    /**
     * 获取当前播放进度
     * @return
     */
    public int getCurrentPosition(){
        return mediaPlayer!=null?mediaPlayer.getCurrentPosition():0;
    }

    /**
     * 获取视频时长
     * @return
     */
    public int getDuration(){
        return mediaPlayer!=null?mediaPlayer.getDuration():0;
    }

    /**
     * 获取当前 屏幕大小状态
     * @return
     */
    public int getCurrentMode() {
        return mCurrentMode;
    }

    public void setTestData(List<MediaBean> testData){
        this.testData = testData;
    }

    public void play(int positoon){
        String url = testData.get(positoon).getUrl();
        getLogger().error("play---url:"+url);
        if(TextUtils.isEmpty(url)){
            ToastUtil.showToast(getContext(), "url为空，不能播放");
        }else{
            try{
                show(true);
                initTextureView();
                addTextureView();
                initMediaPlayer(url);
            }catch(Exception e){
                getLogger().error(e);
            }
        }
    }

    /**
     * 暂停播放
     */
    public void pause(){
        if(mediaPlayer!=null){
            mediaPlayer.pause();
        }
    }

    /**
     * 继续播放
     */
    public void continuePlay(){
        if(mediaPlayer!=null){
            mediaPlayer.start();
        }
    }

    /**
     * 停止播放
     */
    public void stopPlay(){
        try{
            if(mediaPlayer != null){
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.stop();
                }
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            }

            exitFullScreen();
            mCurrentMode = MODE_NORMAL;
            destroySurface();
            show(false);
        }catch(IllegalStateException e){
            e.printStackTrace();
        }
    }

    public boolean isPlaying(){
       return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    /**
     * 若默认是隐藏的，一旦显示，就会走 SurfaceTextureListener回调
     * @param isShow
     */
    public void show(boolean isShow) {
        this.setVisibility(isShow ? VISIBLE : GONE);
    }

    /***************浮层*******************/

    /**
     * 小屏浮层
     *
     * @param view
     */
    public void setHistoryReportPlayerCoverView(View view) {
        historyReportPlayerCoverView = view;
    }

    /**
     * 添加下屏浮层
     */
    public void addCoverView() {
        this.removeView(historyReportPlayerCoverView);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
        rootLayout.addView(historyReportPlayerCoverView, params);
    }


    /*****************全屏/缩小****************/

    /**
     * 全屏，将mContainer(内部包含mTextureView和mController)从当前容器中移除，并添加到android.R.content中.
     * 切换横屏时需要在manifest的activity标签下添加android:configChanges="orientation|keyboardHidden|screenSize"配置，
     * 以避免Activity重新走生命周期
     */
    public void enterFullScreen() {
        if (mCurrentMode == MODE_FULL_SCREEN) {
            return;
        }

        ViewGroup contentView = NiceUtil.scanForActivity(getSuperContext())
                .findViewById(android.R.id.content);
        if (mCurrentMode == MODE_NORMAL || mCurrentMode == MODE_SMALL_SCREEN) {//小屏
//            rootLayout.removeView(smallCoverView);
            this.removeView(rootLayout);
        }
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
//        rootLayout.addView(fullScreenCoverView, params);//添加大屏浮层
        contentView.addView(rootLayout, params);//将布局添加到activity最外层 android.R.id.content
        mCurrentMode = MODE_FULL_SCREEN;
    }

    /**
     * 退出全屏，移除mTextureView和mController，并添加到非全屏的容器中。
     * 切换竖屏时需要在manifest的activity标签下添加android:configChanges="orientation|keyboardHidden|screenSize"配置，
     * 以避免Activity重新走生命周期.
     *
     * @return true退出全屏.
     */
    public boolean exitFullScreen() {
        if (mCurrentMode == MODE_FULL_SCREEN) {
            ViewGroup contentView = NiceUtil.scanForActivity(getSuperContext())
                    .findViewById(android.R.id.content);
//            mContainer.removeView(fullScreenCoverView);//删除大屏浮层
            contentView.removeView(rootLayout);
            LayoutParams params = new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
//            rootLayout.addView(smallCoverView, params);//添加小屏浮层
            this.addView(rootLayout, params);
            mCurrentMode = MODE_SMALL_SCREEN;
            return true;
        }
        return false;
    }

    /************初始化 布局**********/
    /**
     * 第一层 FrameLayout
     */
    private void firstFrameLayout() {
        rootLayout = new FrameLayout(getSuperContext());
        rootLayout.setBackgroundColor(Color.BLACK);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(rootLayout, params);
    }

    private void initTextureView() {
        getLogger().info("-----initTextureView-----");
        if (mTextureView == null) {
            getLogger().info("-----mTextureView == null-----");
            mTextureView = new TextureView(getSuperContext());
            mTextureView.setSurfaceTextureListener(this);
        }
    }

    private void addTextureView() {
        getLogger().info("-----addTextureView-----");
        rootLayout.removeView(mTextureView);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
        rootLayout.addView(mTextureView, 0, params);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mSurfaceTexture = surface;
        Surface face = new Surface(mSurfaceTexture);
        if (mediaPlayer != null) {
            mediaPlayer.setSurface(face);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return mSurfaceTexture == null;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private void initMediaPlayer(String url) throws IOException {
        getLogger().info("url:"+url);

        if (mediaPlayer == null) {
            getLogger().info("mediaPlayer不为空");
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();
        }
        /**
         * 准备完成 加载视频
         * 可以知道时长
         */
        mediaPlayer.setOnPreparedListener(mp -> {
            if (null != mTextureView.getSurfaceTexture()) {
                Surface face = new Surface(mTextureView.getSurfaceTexture());
                mp.setSurface(face);
            }
//                playFinish = false;
            //设置视频时长
//                getView().setMaxTime(mp.getDuration());
            getLogger().info("视频时长:"+mp.getDuration());
            mp.start();

            if(listener!=null){
                listener.onPrepared(mp);
            }
        });

        /**
         * 让播放器从 指定的位置开始播放
         *
         * 快进/回退
         */
        mediaPlayer.setOnSeekCompleteListener(mp -> {
            mp.start();
            if(listener!=null){
                listener.onSeekComplete(mp);
            }
        });

        /**
         * 处理播放 结束后
         */
        mediaPlayer.setOnCompletionListener(mp -> {
            getLogger().info("onCompletion");
            destroyMediaPlayer(mp);
            mediaPlayer = null;
            if(listener!=null){
                listener.onCompletion(mp);
            }
        });

        /**
         * 处理播放过程中遇到 错误的监听
         */
        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            getLogger().error("mediaPlayer  onError");
            if (mp.isPlaying()) {
                mp.stop();
            }
            destroyMediaPlayer(mp);
            mediaPlayer = null;
            if(listener!=null){
                listener.onError(mp);
            }
            return false;

        });
    }

    /**
     * 释放MediaPlayer资源
     */
    private void destroyMediaPlayer(MediaPlayer mp){
        mp.reset();//重置状态，使其恢复到idle空闲状态。
        mp.release();//释放资源
    }

    private void destroySurface(){
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
    }

    @Override
    public HistoryReportPlayerPresenter createPresenter() {
        return new HistoryReportPlayerPresenter(getContext());
    }

    public interface HistoryReportPlayerListener{
        void onPrepared(MediaPlayer mp);
        void onSeekComplete(MediaPlayer mp);
        void onCompletion(MediaPlayer mp);
        void onError(MediaPlayer mp);
    }
}

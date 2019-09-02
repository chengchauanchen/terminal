package com.vsxin.terminalpad.mvp.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ixiaoma.xiaomabus.architecture.mvp.view.layout.MvpFrameLayout;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.presenter.VideoPlayerPresenter;
import com.vsxin.terminalpad.mvp.contract.view.IVideoPlayerView;
import com.vsxin.terminalpad.utils.NiceUtil;

import java.io.IOException;

import cn.vsx.hamster.terminalsdk.tools.DateUtils;

/**
 * @author qzw
 * <p>
 * 视频播放器控件--用于播放历史上报视频
 */
public class VideoPlayer extends MvpFrameLayout<IVideoPlayerView, VideoPlayerPresenter> implements IVideoPlayerView, SurfaceTextureListener {

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

    private View smallCoverView;//小屏浮层
    private View fullScreenCoverView;//全屏浮层

    private MediaPlayer mediaPlayer;
    private ImageView iv_close;
    private TextView tv_theme;
    private ImageView iv_pause;
    private LinearLayout ll_seek_bar;
    private ImageView iv_pause_continue;
    private TextView tv_current_time;
    private SeekBar seek_bar;
    private TextView tv_max_time;
    private TextView tv_choice;

    /**
     * 视频最大时长
     */
    private int maxTime;

    public VideoPlayer(Context context) {
        super(context);
    }

    public VideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);

        initView();
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_video_player_cover, this, true);

        iv_close = findViewById(R.id.iv_close);
        tv_theme = findViewById(R.id.tv_theme);
        iv_pause = findViewById(R.id.iv_pause);
        ll_seek_bar = findViewById(R.id.ll_seek_bar);
        iv_pause_continue = findViewById(R.id.iv_pause_continue);
        tv_current_time = findViewById(R.id.tv_current_time);
        seek_bar = findViewById(R.id.seek_bar);
        tv_max_time = findViewById(R.id.tv_max_time);
        tv_choice = findViewById(R.id.tv_choice);

    }
    /********************基本接口*********************/
    /**
     * 更新播放进度 进度条移动，时间减少等
     */
    @Override
    public void updateProgress(float position) {
        float sMax = seek_bar.getMax();
        float percent = position / getMaxTime();
        seek_bar.setProgress((int) (sMax * percent));
        tv_current_time.setText(DateUtils.getTime((int) position));
    }


    @Override
    public void setSeekBarProgress(int progress) {
        seek_bar.setProgress(progress);
    }

    /**
     * 完成进度 一个视频播放完了
     * 1.继续下一个
     * 2.没有下一个停止
     */
    @Override
    public void completeProgress() {
        seek_bar.setProgress(seek_bar.getMax());
        tv_current_time.setText(DateUtils.getTime(maxTime));
    }

    @Override
    public void setPauseContinue(int resId) {
        iv_pause_continue.setImageResource(resId);
    }

    /**
     *
     * @param isShow
     */
    @Override
    public void isShowSeekBar(boolean isShow) {
        ll_seek_bar.setVisibility(isShow ? VISIBLE : View.GONE);
    }


    @Override
    public void isShowPause(boolean isShow) {
        iv_pause.setVisibility(isShow ? VISIBLE : View.GONE);
    }

    /**
     * 隐藏进度条
     */
    private void hideSeekBar() {

    }

    /**
     * 更新数据源
     */
    private void getdataSouce() {

    }

    /******************MediaPlayer*******************/
    @Override
    public TextureView getTextureView() {
        return mTextureView;
    }

    @Override
    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    @Override
    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    @Override
    public int getMaxTime() {
        return maxTime;
    }

    @Override
    public void setMaxTime(int maxTime) {
        this.maxTime = maxTime;
        seek_bar.setMax(maxTime);
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
    public VideoPlayerPresenter createPresenter() {
        return new VideoPlayerPresenter(getContext());
    }

    /***************浮层*******************/

    /**
     * 小屏浮层
     *
     * @param view
     */
    public void setSmallCoverView(View view) {
        smallCoverView = view;
    }

    /**
     * 全屏浮层
     *
     * @param view
     */
    public void setFullScreenCoverView(View view) {
        fullScreenCoverView = view;
    }

    /**
     * 添加下屏浮层
     */
    public void addSmallCoverView() {
        this.removeView(smallCoverView);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
        rootLayout.addView(smallCoverView, params);
    }

    /**
     * 添加全屏浮层
     */
    public void addFullScreenCoverView() {
        rootLayout.removeView(fullScreenCoverView);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
        rootLayout.addView(fullScreenCoverView, params);
    }


    /*****************全屏/缩小****************/

    /**
     * 全屏
     */
    public void enterFullScreen() {
        if (mCurrentMode == MODE_FULL_SCREEN) {
            return;
        }

        ViewGroup contentView = NiceUtil.scanForActivity(getSuperContext())
                .findViewById(android.R.id.content);
        if (mCurrentMode == MODE_NORMAL || mCurrentMode == MODE_SMALL_SCREEN) {//小屏
            rootLayout.removeView(smallCoverView);
            this.removeView(rootLayout);
        }
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        rootLayout.addView(fullScreenCoverView, params);//添加大屏浮层
        contentView.addView(rootLayout, params);//将布局添加到activity最外层 android.R.id.content
        mCurrentMode = MODE_FULL_SCREEN;
    }

    /**
     * 退出全屏
     */
    public boolean exitFullScreen() {
        if (mCurrentMode == MODE_FULL_SCREEN) {
            ViewGroup contentView = NiceUtil.scanForActivity(getSuperContext())
                    .findViewById(android.R.id.content);
            rootLayout.removeView(fullScreenCoverView);//删除大屏浮层
            contentView.removeView(rootLayout);
            LayoutParams params = new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            rootLayout.addView(smallCoverView, params);//添加小屏浮层
            this.addView(rootLayout, params);
            mCurrentMode = MODE_SMALL_SCREEN;
            return true;
        }
        return false;
    }


    /**********************SurfaceTextureListener**************************/
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Surface face = new Surface(surface);
        if (mediaPlayer != null) {
            mediaPlayer.setSurface(face);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}

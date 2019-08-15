package com.vsxin.terminalpad.mvp.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.ixiaoma.xiaomabus.architecture.mvp.view.layout.MvpFrameLayout;
import com.vsxin.terminalpad.mvp.contract.presenter.LivePlayerPresenter;
import com.vsxin.terminalpad.mvp.contract.view.ILivePlayerView;
import com.vsxin.terminalpad.utils.NiceUtil;

/**
 * @author qzw
 *
 * 拉流播发器
 */
public class LivePlayer extends MvpFrameLayout<ILivePlayerView, LivePlayerPresenter> implements ILivePlayerView, SurfaceTextureListener {

    /**
     * 普通(小屏)模式
     **/
    public static final int MODE_NORMAL = 10;
    /**
     * 全屏模式
     **/
    public static final int MODE_FULL_SCREEN = 11;
    /**
     * 当前屏幕模式，小屏/全屏
     */
    private int mCurrentMode = MODE_NORMAL;


    public static final int MODE_PULL_LIVE = 100;//拉流
    public static final int MODE_PUSH_LIVE = 101;//推流
    public static final int MODE_DEFAULT_LIVE = 102;//推流

    private int mLiveMode = MODE_DEFAULT_LIVE;//当前播放流模式

    private FrameLayout mContainer;
    private TextureView mTextureView;
    private SurfaceTexture mSurfaceTexture;

    public void setmLiveMode(int mLiveMode) {
        this.mLiveMode = mLiveMode;
    }

    public LivePlayer(Context context) {
        super(context);
    }

    public LivePlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        firstFrameLayout();
        initTextureView();
        addTextureView();

        show(false);
    }

    /************************提供接口*************************/

    /**
     * 开始拉取视频
     */
    public void startPullLive(String rtspURL){
        setmLiveMode(MODE_PULL_LIVE);
        getPresenter().startPullLive(mSurfaceTexture,rtspURL);
    }

    /**
     * 停止拉取视频
     */
    public void stopPullLive(){
        setmLiveMode(MODE_DEFAULT_LIVE);
        getPresenter().stopPullLive();
    }

    /**
     * 开始推流
     */
    public void startPushLive(){

    }

    /**
     * 停止推流
     */
    public void stopPushLive(){

    }



    /***********************大小屏*****************************/
    /**
     * 第一层 FrameLayout
     */
    private void firstFrameLayout() {
        mContainer = new FrameLayout(getSuperContext());
        mContainer.setBackgroundColor(Color.BLACK);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(mContainer, params);
    }

    private void initTextureView() {
        if (mTextureView == null) {
            mTextureView = new TextureView(getSuperContext());
            mTextureView.setSurfaceTextureListener(this);
        }
    }

    private void addTextureView() {
        mContainer.removeView(mTextureView);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
        mContainer.addView(mTextureView, 0, params);
    }

    @Override
    public LivePlayerPresenter createPresenter() {
        return new LivePlayerPresenter(getContext());
    }

    /**
     * 若默认是隐藏的，一旦显示，就会走 SurfaceTextureListener回调
     * @param isShow
     */
    @Override
    public void show(boolean isShow) {
        this.setVisibility(isShow?VISIBLE:GONE);
    }

    @Override
    public boolean visibility(){
        int visibility = this.getVisibility();
        if(visibility==VISIBLE){//显示
            return true;
        }else {
            return false;
        }
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
        if (mCurrentMode == MODE_NORMAL) {
            this.removeView(mContainer);
        }
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        contentView.addView(mContainer, params);//将布局添加到activity最外层 android.R.id.content
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
            ViewGroup contentView = (ViewGroup) NiceUtil.scanForActivity(getSuperContext())
                    .findViewById(android.R.id.content);
            contentView.removeView(mContainer);
            LayoutParams params = new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            this.addView(mContainer, params);
            mCurrentMode = MODE_NORMAL;
            return true;
        }
        return false;
    }


    /*****************SurfaceTextureListener****************/

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (mSurfaceTexture == null) {
            mSurfaceTexture = surface;
        } else {
            mTextureView.setSurfaceTexture(mSurfaceTexture);
        }

        //缩放时会重新走这个回调，但不能又拨一次
        getLogger().info("SurfaceTexture 走了onSurfaceTextureAvailable");

        if(mLiveMode==MODE_PULL_LIVE){//拉流
            getLogger().info("重新拉流");
            if(mCurrentMode==MODE_FULL_SCREEN){
               return;
            }
            getPresenter().initEasyPlay(mSurfaceTexture);
        }else if(mLiveMode==MODE_PUSH_LIVE){//推流

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

}

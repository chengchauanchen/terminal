package com.vsxin.terminalpad.mvp.ui.fragment;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.MvpFragment;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.presenter.LivePresenter;
import com.vsxin.terminalpad.mvp.contract.view.ILiveView;
import com.vsxin.terminalpad.receiveHandler.ReceiveStopPullLiveHandler;
import com.vsxin.terminalpad.utils.OperateReceiveHandlerUtilSync;

import org.easydarwin.push.MediaStream;

import butterknife.BindView;


/**
 * @author qzw
 * <p>
 * 直播模块
 */
public class LiveFragment extends MvpFragment<ILiveView, LivePresenter> implements ILiveView {

    @BindView(R.id.sv_live)
    TextureView sv_live;

    @BindView(R.id.rl_live_view)
    RelativeLayout rl_live_view;

    @BindView(R.id.iv_break_live)
    ImageView iv_break_live;//退出直播

    private WindowManager windowManager;
    private MediaStream mMediaStream;

    private static int PULL_LIVE = 1;//拉取视频
    private static int PUSH_LIVE = 2;//上报视频

    private static int LIVE_TYPE=0;


    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_live;
    }

    @Override
    protected void initViews(View view) {
        windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        getPresenter().registReceiveHandler();
        //默认隐藏
        rl_live_view.setVisibility(View.INVISIBLE);

        sv_live.setSurfaceTextureListener(surfaceTextureListener);
        sv_live.setOnTouchListener(svOnTouchListener);

        iv_break_live.setOnClickListener(v -> {
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveStopPullLiveHandler.class);
            getPresenter().finishVideoLive();
        });
    }

    @Override
    protected void initData() {

    }

    @Override
    public void startPush() {
//        getActivity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                getPresenter().startPush(sv_live);
//            }
//        });
        LIVE_TYPE=PUSH_LIVE;
        if(sv_live!=null && sv_live.getSurfaceTexture()!=null){
            getPresenter().startPush(sv_live.getSurfaceTexture());
        }else{
            isShowLiveView(true);
        }
    }

    @Override
    public void startPull() {
        LIVE_TYPE=PULL_LIVE;

        if(sv_live!=null && sv_live.getSurfaceTexture()!=null){
            getPresenter().startPull(sv_live.getSurfaceTexture());
        }else{
            isShowLiveView(true);
        }
    }

    @Override
    public void startGB28121Pull() {
        LIVE_TYPE=PULL_LIVE;
        if(sv_live!=null && sv_live.getSurfaceTexture()!=null){
            getPresenter().startPullGB28121(sv_live.getSurfaceTexture());
        }else{
            isShowLiveView(true);
        }
    }


    @Override
    public void isShowLiveView(boolean isShow) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                rl_live_view.setVisibility(isShow ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public LivePresenter createPresenter() {
        return new LivePresenter(getContext());
    }

    @Override
    public WindowManager getWindowManager() {
        return windowManager;
    }

    @Override
    public MediaStream getmMediaStream() {
        return mMediaStream;
    }

    @Override
    public void setMediaStream(MediaStream mediaStream) {
        mMediaStream = mediaStream;
    }

    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            getLogger().info("onSurfaceTextureAvailable----->" + surface);
            //TODO 要注意拉流 和 推流 这逻辑还不一样哦 这里只写拉流得逻辑

            if(LIVE_TYPE==PULL_LIVE){
                if (getPresenter().isGB28181Live()) {
                    getPresenter().startPullGB28121(surface);
                } else {
                    getPresenter().startPull(surface);
                }
            }else if (LIVE_TYPE==PUSH_LIVE){
                getPresenter().startPush(surface);
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            getLogger().info("onSurfaceTextureDestroyed----->" + surface);
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    private float oldDist;
    private View.OnTouchListener svOnTouchListener = (v, event) -> {

        if (event.getPointerCount() == 1) {
            if (mMediaStream != null) {
                handleFocus(mMediaStream.getCamera());
            }
        } else {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = getFingerSpacing(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    float newDist = getFingerSpacing(event);
                    if (Math.abs(newDist - oldDist) > 5f) {
                        if (newDist > oldDist) {
                            handleZoom(false);
                        } else if (newDist < oldDist) {
                            handleZoom(true);
                        }
                        oldDist = newDist;
                    }
                    break;
            }
        }
        return true;
    };


    private void handleFocus(Camera camera) {
        if (null != mMediaStream && mMediaStream.isStreaming() && camera != null && mMediaStream.isPreView()) {
            camera.autoFocus(null);//屏幕聚焦
        }
    }

    private float getFingerSpacing(MotionEvent event) {
        return (float) Math.sqrt(event.getX(0) * event.getX(1) + event.getY(0) * event.getY(1));
    }

    private void handleZoom(boolean isScale) {
        if (null != mMediaStream && mMediaStream.isStreaming()) {
            mMediaStream.ZoomOrReduceVideo(isScale);
        }
    }

    /****************************************被人上报，邀请我看************************************************/


    @Override
    public void onDestroy() {
        super.onDestroy();
        getPresenter().unregistReceiveHandler();
    }
}

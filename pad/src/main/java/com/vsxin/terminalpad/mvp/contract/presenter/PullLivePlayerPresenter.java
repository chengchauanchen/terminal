package com.vsxin.terminalpad.mvp.contract.presenter;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.view.TextureView;
import com.ixiaoma.xiaomabus.architecture.mvp.BasePresenter;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.view.IPullLivePlayerView;
import com.vsxin.terminalpad.prompt.PromptManager;
import com.vsxin.terminalpad.utils.SensorUtil;
import com.vsxin.terminalpad.utils.StateMachineUtils;

import org.easydarwin.video.EasyRTSPClient;
import org.easydarwin.video.RTSPClient;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * @author qzw
 * <p>
 * 播放直播控件
 */
public class PullLivePlayerPresenter extends BasePresenter<IPullLivePlayerView> {

    private PullRtspReceiver mPullRtspReceiver;
    private String rtspUrl;//直播流URL
    private EasyRTSPClient mStreamRender;

    public PullLivePlayerPresenter(Context mContext) {
        super(mContext);
    }

    /*************拉警务通****************/
    /*************拉执法记录仪****************/
    /*************拉无人机****************/
    /*************拉LTE****************/
    /*************拉布控球****************/
    /*************拉PAD****************/

    /**
     * 播放视频
     */
    public void playVideo(String rtspUrl) {
        this.rtspUrl = rtspUrl;
        initEasyPlay(rtspUrl);
    }

    public void stopPlay(){
        stopBusiness();
        stopPull();
        getView().show(false);
    }

    /**
     * 退出业务状态
     */
    protected void stopBusiness() {
        PromptManager.getInstance().stopRing();
        SensorUtil.getInstance().unregistSensor();
        StateMachineUtils.revertStateMachine();
    }

    private void stopPull() {
        if (mStreamRender != null) {
            mStreamRender.stop();
            mStreamRender = null;
        }
    }

    private void initEasyPlay(String rtspUrl) {
        SurfaceTexture surface = getView().getSurfaceTexture();
        if (null == mPullRtspReceiver) {
            mPullRtspReceiver = new PullRtspReceiver(new Handler());
        }
        if (null != surface) {//说明没有显示出来
            getView().getLogger().info("开始播放 null != surface");
            mStreamRender = new EasyRTSPClient(
                    getContext(),
                    MyTerminalFactory.getSDK().getLiveConfigManager().getPlayKey(),
                    surface,
                    mPullRtspReceiver);
            try {
                if (!TextUtils.isEmpty(rtspUrl)) {
                    mStreamRender.start(
                            rtspUrl,
                            RTSPClient.TRANSTYPE_TCP,
                            RTSPClient.EASY_SDK_VIDEO_FRAME_FLAG | RTSPClient.EASY_SDK_AUDIO_FRAME_FLAG,
                            "", "", null);
                    getView().getLogger().info("开始播放 mStreamRender.start(rtspUrl)");
                }
            } catch (Exception e) {
                e.printStackTrace();
                getView().getLogger().error(e.toString());
            }
        }else{
            getView().show(true);
        }
    }

    /**
     * 切换线程
     */
    private class PullRtspReceiver extends ResultReceiver {

        //private int pullcount;

        private PullRtspReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == EasyRTSPClient.RESULT_VIDEO_DISPLAYED) {
                //pullcount = 0;
            } else if (resultCode == EasyRTSPClient.RESULT_VIDEO_SIZE) {
//                mLiveWidth = resultData.getInt(EasyRTSPClient.EXTRA_VIDEO_WIDTH);
//                mLiveHeight = resultData.getInt(EasyRTSPClient.EXTRA_VIDEO_HEIGHT);
//                onVideoSizeChange();
                //TODO 在这可处理视频长宽与屏幕长宽比不匹配的问题

            } else if (resultCode == EasyRTSPClient.RESULT_TIMEOUT) {
                ToastUtil.showToast(getContext(), getContext().getString(R.string.time_up));
            } else if (resultCode == EasyRTSPClient.RESULT_UNSUPPORTED_AUDIO) {
                ToastUtil.showToast(getContext(), getContext().getString(R.string.voice_not_support));
            } else if (resultCode == EasyRTSPClient.RESULT_UNSUPPORTED_VIDEO) {
                ToastUtil.showToast(getContext(), getContext().getString(R.string.video_not_support));
            } else if (resultCode == EasyRTSPClient.RESULT_EVENT) {
                int errorCode = resultData.getInt("errorcode");
                String resultDataString = resultData.getString("event-msg");
                //getView().getLogger().error("视频流播放状态：" + errorCode + "=========" + resultDataString + "-----count:" + pullcount);
                getView().getLogger().error("视频流播放状态：" + errorCode + "=========" + resultDataString);
                if (errorCode == 500 || errorCode == 404 || errorCode == -32 || errorCode == -101) {
                    //在这些异常中，需循环重新播放
                } else if (errorCode != 0) {
                    stopPlay();
                }
            }
        }
    }

    /**
     * 初始化 TextureView
     */
    public void setSurfaceTextureListener(TextureView ttv_live){
        ttv_live.setSurfaceTextureListener(surfaceTextureListener);
    }

    /**
     * SurfaceTexture 需要注册这个监听
     *
     * 监听回调成功后，SurfaceTexture对象才会被赋值
     *
     * 显示SurfaceTexture才能正常回调，父布局也必须显示
     */
    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            getView().getLogger().info("onSurfaceTextureAvailable----->" + surface);
            //SurfaceTexture 初始化成功后，重新播放视频
            if(TextUtils.isEmpty(rtspUrl)){
                initEasyPlay(rtspUrl);
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            getView().getLogger().info("onSurfaceTextureDestroyed----->" + surface);
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };
}

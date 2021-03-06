package com.vsxin.terminalpad.mvp.contract.presenter;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.text.TextUtils;

import com.ixiaoma.xiaomabus.architecture.mvp.BasePresenter;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.manager.PullLiveManager;
import com.vsxin.terminalpad.mvp.contract.view.ILivePlayerView;
import com.vsxin.terminalpad.prompt.PromptManager;
import com.vsxin.terminalpad.utils.SensorUtil;
import com.vsxin.terminalpad.utils.StateMachineUtils;

import org.easydarwin.video.EasyRTSPClient;
import org.easydarwin.video.RTSPClient;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePlayingState;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * @author qzw
 * <p>
 * 播放直播控件
 */
public class LivePlayerPresenter extends BasePresenter<ILivePlayerView> {

    private PullRtspReceiver mPullRtspReceiver;
    private EasyRTSPClient mStreamRender;
    private String rtspUrl;


    public LivePlayerPresenter(Context mContext) {
        super(mContext);
    }

    /*********************拉流**********************/

    protected Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * 播放视频
     */
    public void startPullLive(SurfaceTexture surface, String rtspUrl) {
        this.rtspUrl = rtspUrl;
        getView().show(true);
        mHandler.postDelayed(() -> initEasyPlay(surface), 2000);
    }

    public void stopPullLive() {
        stopBusiness();
        stopPull();
    }

    /**
     * 退出业务状态
     */
    private void stopBusiness() {
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

    public void initEasyPlay(SurfaceTexture surface) {
        getView().getLogger().info("-----initEasyPlay-----");

        //兼容两种播放视频的情况 1.走正常的服务信令 2.直接播流地址 lte、布控球、摄像头 手动控制状态机
        //这样就防止,在播lte等视频时，若又播其它视频导致状态机异常错误的情况
        //不用关结束，关闭是会将状态机回执
        PullLiveManager.moveToStateIdleToPlaying();

        if (null == mPullRtspReceiver) {
            mPullRtspReceiver = new LivePlayerPresenter.PullRtspReceiver(new Handler());
        }
        if (null != surface) {//说明没有显示出来
            getView().getLogger().info("-----正在播放----- rtspUrl=:" + rtspUrl);
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
                    getView().getLogger().info("-----正在播放-----");
                }
            } catch (Exception e) {
                e.printStackTrace();
                getView().getLogger().error(e.toString());
            }
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
                ToastUtil.showToast(getContext(), resultDataString);
                if (errorCode == 500 || errorCode == 404 || errorCode == -32 || errorCode == -101) {
                    //在这些异常中，需循环重新播放
                } else if (errorCode != 0) {
                    stopPullLive();
                }
            }else {
                ToastUtil.showToast("播放异常");
            }
        }
    }

    /*********************1主动拉流**********************/

    /*********************2被动拉流**********************/


    /*********************推流**********************/

    /*********************1主动推流**********************/

    /*********************2被动推流**********************/

}

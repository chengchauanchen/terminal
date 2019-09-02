package com.vsxin.terminalpad.mvp.contract.view;


import android.media.MediaPlayer;
import android.view.TextureView;

import com.ixiaoma.xiaomabus.architecture.mvp.IBaseView;

/**
 * @author qzw
 * <p>
 * 视频播放器控件--用于播放历史上报视频
 */
public interface IVideoPlayerView extends IBaseView {

    MediaPlayer getMediaPlayer();

    void setMediaPlayer(MediaPlayer mediaPlayer);

    TextureView getTextureView();

    int getMaxTime();
    void setMaxTime(int maxTime);

    void updateProgress(float position);

    void completeProgress();

    void setPauseContinue(int resId);

    void isShowSeekBar(boolean isShow);

    void isShowPause(boolean isShow);

    void setSeekBarProgress(int progress);
}

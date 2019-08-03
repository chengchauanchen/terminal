package com.vsxin.terminalpad.mvp.contract.view;


import android.view.TextureView;
import android.view.WindowManager;

import com.ixiaoma.xiaomabus.architecture.mvp.IBaseView;

import org.easydarwin.push.MediaStream;

/**
 * Created by PC on 2018/11/1.
 */

public interface ILiveView extends IBaseView {
    void isShowLiveView(boolean isShow);
    void startPush();
    WindowManager getWindowManager();

    MediaStream getmMediaStream();

    void setMediaStream(MediaStream mediaStream);

    void startPull(String rtspUrl);
}

package com.vsxin.terminalpad.mvp.contract.view;


import android.graphics.SurfaceTexture;

import com.ixiaoma.xiaomabus.architecture.mvp.IBaseView;

/**
 * @author qzw
 * <p>
 * 播放直播控件
 */
public interface IPullLivePlayerView extends IBaseView {

    /**
     * 获取 SurfaceTexture对象
     * @return
     */
    SurfaceTexture getSurfaceTexture();

    /**
     * 显示 SurfaceTexture
     * @param isShow
     */
    void show(boolean isShow);

}

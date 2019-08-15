package com.vsxin.terminalpad.mvp.contract.view;


import com.ixiaoma.xiaomabus.architecture.mvp.IBaseView;

/**
 * @author qzw
 * <p>
 * 播放直播控件
 */
public interface ILivePlayerView extends IBaseView {

    /**
     * 显示 SurfaceTexture
     * @param isShow
     */
    void show(boolean isShow);

    boolean visibility();
}

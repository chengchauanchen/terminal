package com.vsxin.terminalpad.mvp.contract.view;


import com.ixiaoma.xiaomabus.architecture.mvp.IBaseView;

/**
 * @author qzw
 * 播放器模块
 * 包含：直播拉流rtsp 和 历史上报记录播放mp4
 */
public interface IPlayerView extends IBaseView {

    void showFragmentForTag(String tag);

}

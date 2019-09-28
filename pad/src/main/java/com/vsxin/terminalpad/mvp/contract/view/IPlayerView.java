package com.vsxin.terminalpad.mvp.contract.view;


import com.ixiaoma.xiaomabus.architecture.mvp.IBaseView;
import com.vsxin.terminalpad.mvp.entity.MediaBean;

import java.util.List;

/**
 * @author qzw
 * 播放器模块
 * 包含：直播拉流rtsp 和 历史上报记录播放mp4
 */
public interface IPlayerView extends IBaseView {

    void showFragmentForTag(String tag);

    void setHistoryMediaDataSource(List<MediaBean> dataSource, String name, int memberId);
}

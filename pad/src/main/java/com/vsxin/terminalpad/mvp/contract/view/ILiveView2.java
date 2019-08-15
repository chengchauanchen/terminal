package com.vsxin.terminalpad.mvp.contract.view;


import com.ixiaoma.xiaomabus.architecture.mvp.IBaseView;

/**
 * Created by PC on 2018/11/1.
 */

public interface ILiveView2 extends IBaseView {
    void stopPullLive();

    void startPullLive(String rtspURL);
}

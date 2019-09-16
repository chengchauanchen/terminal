package com.vsxin.terminalpad.mvp.contract.view;


import com.ixiaoma.xiaomabus.architecture.mvp.IBaseView;

/**
 * @author qzw
 * <p>
 * 地图气泡点击-发起个呼
 */
public interface IIndividualCallView extends IBaseView {

    /**
     * 半双工
     */
    void startHalfDuplexIndividualCall();

    /**
     * 全双工
     */
    void startFullDuplexIndividualCall();

    void stopAndDestroy();
}

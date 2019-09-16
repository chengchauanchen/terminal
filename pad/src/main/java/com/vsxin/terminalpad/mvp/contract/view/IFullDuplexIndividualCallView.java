package com.vsxin.terminalpad.mvp.contract.view;


import com.ixiaoma.xiaomabus.architecture.mvp.IBaseView;

/**
 * @author qzw
 *
 * 全双工个呼
 */

public interface IFullDuplexIndividualCallView extends IBaseView {

    void setSpeakingToast(String des);

    void closeFragment();
}

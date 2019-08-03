package com.vsxin.terminalpad.mvp.contract.view;


import com.ixiaoma.xiaomabus.architecture.mvp.IBaseView;

/**
 * @author qzw
 *
 * 地图聚合气泡点击-成员列表页
 */

public interface IHalfDuplexIndividualCallView extends IBaseView {

    void groupCallOtherSpeaking();

    void individualCallPttStatus(boolean pttIsDown, int outerMemberId);

    void ceaseGroupCallConformation(int resultCode, String resultDesc);

    void groupCallCeasedIndication(int reasonCode);

    void requestGroupCallConformation(int methodResult, String resultDesc,int groupId);

    void notifyIndividualCallStopped(int methodResult, String resultDesc);

}

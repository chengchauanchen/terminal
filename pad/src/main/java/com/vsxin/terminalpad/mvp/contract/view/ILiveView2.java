package com.vsxin.terminalpad.mvp.contract.view;


import android.view.View.OnClickListener;

import com.ixiaoma.xiaomabus.architecture.mvp.IBaseView;

import cn.vsx.hamster.terminalsdk.model.Member;

/**
 * Created by PC on 2018/11/1.
 */

public interface ILiveView2 extends IBaseView {
    void stopPullLive();

    void startPullLive(String rtspURL);

    void setMemberInfo(Member member);

    void setShareLiveClickListener(OnClickListener shareLiveClickListener);
}

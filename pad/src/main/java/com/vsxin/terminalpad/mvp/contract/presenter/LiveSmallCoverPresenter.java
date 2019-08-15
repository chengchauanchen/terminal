package com.vsxin.terminalpad.mvp.contract.presenter;

import android.content.Context;

import com.ixiaoma.xiaomabus.architecture.mvp.BasePresenter;
import com.vsxin.terminalpad.mvp.contract.view.ILiveSmallCoverView;

/**
 * @author qzw
 * <p>
 * 直播小框 浮层view
 */
public class LiveSmallCoverPresenter extends BasePresenter<ILiveSmallCoverView> {

    public LiveSmallCoverPresenter(Context mContext) {
        super(mContext);
    }
}

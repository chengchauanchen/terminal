package com.vsxin.terminalpad.mvp.contract.presenter;

import android.content.Context;

import com.ixiaoma.xiaomabus.architecture.mvp.BasePresenter;
import com.vsxin.terminalpad.mvp.contract.view.IHistoryReportPlayerCoverView;
import com.vsxin.terminalpad.mvp.contract.view.ILiveSmallCoverView;

/**
 * @author qzw
 * <p>
 * 播放历史上报记录  浮层view
 */
public class HistoryReportPlayerCoverPresenter extends BasePresenter<IHistoryReportPlayerCoverView> {

    public HistoryReportPlayerCoverPresenter(Context mContext) {
        super(mContext);
    }
}

package com.vsxin.terminalpad.mvp.contract.presenter;

import android.content.Context;

import com.ixiaoma.xiaomabus.architecture.mvp.BasePresenter;
import com.vsxin.terminalpad.mvp.contract.view.IHistoryReportPlayerView;
import com.vsxin.terminalpad.mvp.entity.HistoryMediaBean;
import com.vsxin.terminalpad.mvp.entity.MediaBean;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * @author qzw
 * <p>
 * 视频播放器控件--用于播放历史上报视频
 */
public class HistoryReportPlayerPresenter extends BasePresenter<IHistoryReportPlayerView> {

    public HistoryReportPlayerPresenter(Context mContext) {
        super(mContext);
    }





}

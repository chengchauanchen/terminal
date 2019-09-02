package com.vsxin.terminalpad.mvp.contract.presenter;

import android.content.Context;

import com.ixiaoma.xiaomabus.architecture.mvp.BasePresenter;
import com.vsxin.terminalpad.mvp.contract.constant.FragmentTagConstants;
import com.vsxin.terminalpad.mvp.contract.constant.NoticeInOrOutEnum;
import com.vsxin.terminalpad.mvp.contract.constant.NoticeOutLiveEnum;
import com.vsxin.terminalpad.mvp.contract.constant.NoticeTypeEnum;
import com.vsxin.terminalpad.mvp.contract.view.IMainView;
import com.vsxin.terminalpad.mvp.contract.view.IPlayerView;
import com.vsxin.terminalpad.mvp.entity.NoticeBean;
import com.vsxin.terminalpad.receiveHandler.HistoryReportPlayerHandler;
import com.vsxin.terminalpad.receiveHandler.ReceiveStartPullLiveHandler;
import com.vsxin.terminalpad.utils.OperateReceiveHandlerUtilSync;
import com.vsxin.terminalpad.utils.TimeUtil;

import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * @author qzw
 * 播放器模块
 * 包含：直播拉流rtsp 和 历史上报记录播放mp4
 */
public class PlayerPresenter extends BasePresenter<IPlayerView> {

    public PlayerPresenter(Context mContext) {
        super(mContext);
    }

    /**
     * 播放历史视频
     */
    private HistoryReportPlayerHandler historyReportPlayerHandler = () -> {
        //getView().playerHistory();
        getView().showFragmentForTag(FragmentTagConstants.MP4);
    };

    /**
     * 主动拉流
     */
    private ReceiveStartPullLiveHandler receiveStartPullLiveHandler = (member) -> {
        getView().showFragmentForTag(FragmentTagConstants.LIVE);
    };

    /**
     * 注册监听
     */
    public void registReceiveHandler() {
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(historyReportPlayerHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiveStartPullLiveHandler);
    }

    /**
     * 取消监听
     */
    public void unregistReceiveHandler() {
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(historyReportPlayerHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiveStartPullLiveHandler);
    }

    @Override
    public void attachView(IPlayerView view) {
        super.attachView(view);
        registReceiveHandler();
    }

    @Override
    public void detachView() {
        super.detachView();
        unregistReceiveHandler();
    }
}

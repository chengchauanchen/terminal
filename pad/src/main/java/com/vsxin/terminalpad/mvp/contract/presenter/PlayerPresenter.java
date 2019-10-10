package com.vsxin.terminalpad.mvp.contract.presenter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.ixiaoma.xiaomabus.architecture.mvp.BasePresenter;
import com.vsxin.terminalpad.mvp.contract.constant.FragmentTagConstants;
import com.vsxin.terminalpad.mvp.contract.view.IPlayerView;
import com.vsxin.terminalpad.mvp.entity.HistoryMediaBean;
import com.vsxin.terminalpad.receiveHandler.HistoryReportPlayerHandler;
import com.vsxin.terminalpad.receiveHandler.ReceiveGoWatchLiveHandler2;
import com.vsxin.terminalpad.receiveHandler.ReceiveStartPullLiveHandler;
import com.vsxin.terminalpad.utils.OperateReceiveHandlerUtilSync;

import java.util.ArrayList;
import java.util.List;

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

    protected Handler mHandler = new Handler(Looper.getMainLooper());
    /**
     * 播放历史视频
     */
    private ReceiveGoWatchLiveHandler2 receiveGoWatchLiveHandler = (liveUrl, name, memberId) -> mHandler.post(() -> {
        getView().getLogger().info("ReceiveGoWatchLiveHandler--播放历史视频");
        getView().setHistoryMediaDataSource(liveUrl,name, memberId);
        getView().showFragmentForTag(FragmentTagConstants.MP4);
    });

    /**
     * 播放历史视频
     */
    private HistoryReportPlayerHandler historyReportPlayerHandler = () -> {
        getView().getLogger().info("HistoryReportPlayerHandler--播放历史视频");
        getView().showFragmentForTag(FragmentTagConstants.MP4);
    };

    /**
     * 主动拉流
     */
    private ReceiveStartPullLiveHandler receiveStartPullLiveHandler = (member) -> {
        getView().getLogger().info("直播播放器显示了");
        getView().showFragmentForTag(FragmentTagConstants.LIVE);
    };

    /**
     * 注册监听
     */
    public void registReceiveHandler() {
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGoWatchLiveHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(historyReportPlayerHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiveStartPullLiveHandler);
    }

    /**
     * 取消监听
     */
    public void unregistReceiveHandler() {
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(historyReportPlayerHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiveStartPullLiveHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGoWatchLiveHandler);
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



    public List<HistoryMediaBean> getTestData(List<String> liveUrls) {
        List<HistoryMediaBean> mediaBeans = new ArrayList<>();
        for (String urlStr : liveUrls) {
            HistoryMediaBean mediaBean = new HistoryMediaBean();
//            String fileServerIp = MyTerminalFactory.getSDK().getParam(Params.MEDIA_HISTORY_SERVER_IP);
//            String port = MyTerminalFactory.getSDK().getParam(Params.MEDIA_HISTORY_SERVER_PORT, 0) + "";
//            String liveUrl = "http://" + fileServerIp + ":" + port + urlStr;
            getView().getLogger().info("liveUrl：" + urlStr);
            mediaBean.setUrl(urlStr);
            mediaBean.setSelected(true);
            mediaBean.setStartTime("");
            mediaBeans.add(mediaBean);
        }
        return mediaBeans;
    }
}

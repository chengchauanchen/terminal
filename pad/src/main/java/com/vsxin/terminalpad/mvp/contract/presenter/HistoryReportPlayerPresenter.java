package com.vsxin.terminalpad.mvp.contract.presenter;

import android.content.Context;

import com.ixiaoma.xiaomabus.architecture.mvp.BasePresenter;
import com.vsxin.terminalpad.mvp.contract.view.IHistoryReportPlayerView;
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


    public  List<MediaBean> getTestData() {
        List<MediaBean> mediaBeans = new ArrayList<>();
        String url1 = "/hls/156015518433156208_3132127691661450946/20190819/20190819164250/2019-08-19-16-42-50.mp4";
        String url2 = "/hls/156015518433156208_3132127691661450946/20190819/20190819164250/2019-08-19-16-47-50.mp4";
        String url3 = "/hls/156015518433156208_3132127691661450946/20190819/20190819164250/2019-08-19-16-52-50.mp4";
        String url4 = "/hls/156015518433156208_3132127691661450946/20190819/20190819164250/2019-08-19-16-57-50.mp4";
        String[] url = {url1, url2, url3, url4};
        for (String urlStr : url) {
            MediaBean mediaBean = new MediaBean();
            String fileServerIp = MyTerminalFactory.getSDK().getParam(Params.MEDIA_HISTORY_SERVER_IP);
            String port = MyTerminalFactory.getSDK().getParam(Params.MEDIA_HISTORY_SERVER_PORT, 0) + "";
            String liveUrl = "http://" + fileServerIp + ":" + port + urlStr;
            getView().getLogger().info("liveUrl：" + liveUrl);
            mediaBean.setUrl(liveUrl);
            mediaBean.setSelected(true);
            mediaBean.setStartTime("");
            mediaBeans.add(mediaBean);
        }
        return mediaBeans;
    }



}

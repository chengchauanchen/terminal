package com.vsxin.terminalpad.mvp.contract.presenter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.ixiaoma.xiaomabus.architecture.mvp.BasePresenter;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.app.PadApplication;
import com.vsxin.terminalpad.mvp.contract.view.IHistoryReportView;
import com.vsxin.terminalpad.mvp.contract.view.ILiveView3;
import com.vsxin.terminalpad.mvp.entity.HistoryMediaBean;
import com.vsxin.terminalpad.prompt.PromptManager;
import com.vsxin.terminalpad.receiveHandler.HistoryReportPlayerHandler;
import com.vsxin.terminalpad.receiveHandler.ReceiveGoWatchRTSPHandler;
import com.vsxin.terminalpad.receiveHandler.ReceiveStartPullLiveHandler;
import com.vsxin.terminalpad.receiveHandler.ReceiverRequestLteBullHandler;
import com.vsxin.terminalpad.receiveHandler.ReceiverRequestVideoHandler;
import com.vsxin.terminalpad.utils.OperateReceiveHandlerUtilSync;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.TerminalErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePlayingState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePushingState;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveAnswerLiveTimeoutHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCeaseGroupCallConformationHander;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetRtspStreamUrlHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberNotLivingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyLivingStoppedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyMemberStopWatchMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRequestGroupCallConformationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseStartLiveHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.Util;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * Created by PC on 2018/11/1.
 */

public class HistoryReportPresenter extends BasePresenter<IHistoryReportView> {


    public HistoryReportPresenter(Context mContext) {
        super(mContext);
    }

    public List<HistoryMediaBean> getTestData() {
        List<HistoryMediaBean> mediaBeans = new ArrayList<>();
//        String url1 = "/hls/156015518433156208_3132127691661450946/20190819/20190819164250/2019-08-19-16-42-50.mp4";
//        String url2 = "/hls/156015518433156208_3132127691661450946/20190819/20190819164250/2019-08-19-16-47-50.mp4";
//        String url3 = "/hls/156015518433156208_3132127691661450946/20190819/20190819164250/2019-08-19-16-52-50.mp4";
//        String url4 = "/hls/156015518433156208_3132127691661450946/20190819/20190819164250/2019-08-19-16-57-50.mp4";
//        String[] url = {url1, url2, url3, url4};

        String url5 = "/hls/316755467864899584_4073276524725482789/20190805/20190805095859/2019-08-05-09-58-59.mp4";
        String url6 = "/hls/316755467864899584_7123783655049759443/20190805/20190805163929/2019-08-05-16-39-29.mp4";
        String url7 = "/hls/316755467864899584_3980146939522371053/20190805/20190805164127/2019-08-05-16-41-27.mp4";
        String[] url = {url5, url6, url7};

        for (String urlStr : url) {
            HistoryMediaBean mediaBean = new HistoryMediaBean();
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

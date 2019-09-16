package com.vsxin.terminalpad.mvp.contract.presenter;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.ixiaoma.xiaomabus.architecture.mvp.BasePresenter;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.app.PadApplication;
import com.vsxin.terminalpad.mvp.contract.view.ILiveView2;
import com.vsxin.terminalpad.mvp.entity.InviteMemberExceptList;
import com.vsxin.terminalpad.mvp.entity.InviteMemberLiverMember;
import com.vsxin.terminalpad.mvp.ui.fragment.SelectMemberFragment;
import com.vsxin.terminalpad.prompt.PromptManager;
import com.vsxin.terminalpad.receiveHandler.ReceiveGoWatchRTSPHandler;
import com.vsxin.terminalpad.receiveHandler.ReceiveStartPullLiveHandler;
import com.vsxin.terminalpad.receiveHandler.ReceiverRequestLteBullHandler;
import com.vsxin.terminalpad.receiveHandler.ReceiverRequestVideoHandler;
import com.vsxin.terminalpad.utils.Constants;
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
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetRtspStreamUrlHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberNotLivingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyLivingStoppedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyMemberStopWatchMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseStartLiveHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.Util;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * Created by PC on 2018/11/1.
 */

public class LivePresenter2 extends BasePresenter<ILiveView2> {

    protected Handler mHandler = new Handler(Looper.getMainLooper());

    public LivePresenter2(Context mContext) {
        super(mContext);
    }

    /**
     * 注册监听
     */
    public void registReceiveHandler() {
        //自己主动请求他人上报
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiverRequestVideoHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveReaponseStartLiveHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveAnswerLiveTimeoutHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGetRtspStreamUrlHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyLivingStoppedHandler);

        MyTerminalFactory.getSDK().registReceiveHandler(receiveMemberNotLivingHandler);
        //MyTerminalFactory.getSDK().registReceiveHandler(receiveRequestGroupCallConformationHandler);
        //MyTerminalFactory.getSDK().registReceiveHandler(receiveCeaseGroupCallConformationHander);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyMemberStopWatchMessageHandler);

        //拉取不控球视频
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiverRequestLteBullHandler);
    }

    /**
     * 取消监听
     */
    public void unregistReceiveHandler() {
        //自己主动请求他人上报
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiverRequestVideoHandler);

        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveReaponseStartLiveHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveAnswerLiveTimeoutHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetRtspStreamUrlHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMemberNotLivingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyLivingStoppedHandler);

        //MyTerminalFactory.getSDK().unregistReceiveHandler(receiveRequestGroupCallConformationHandler);
        //MyTerminalFactory.getSDK().unregistReceiveHandler(receiveCeaseGroupCallConformationHander);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyMemberStopWatchMessageHandler);

        //拉取不控球视频
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiverRequestLteBullHandler);
    }

    /******************************************2自己主动请求别人上报**********************************************/
    /**
     * 拉取布控球视频
     */
    private ReceiverRequestLteBullHandler receiverRequestLteBullHandler = (rtsp) -> {
        if (PadApplication.getPadApplication().getVideoLivePlayingState() != VideoLivePlayingState.IDLE) {
            ToastUtil.showToast(getContext(), getContext().getString(R.string.text_watching_can_not_request_report));
            return;
        }
        if (PadApplication.getPadApplication().getVideoLivePushingState() != VideoLivePushingState.IDLE) {
            ToastUtil.showToast(getContext(), getContext().getString(R.string.text_pushing_can_not_pull));
            return;
        }
        if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_ASK.name())) {
            ToastUtil.showToast(getContext(), getContext().getString(R.string.no_pull_authority));
            return;
        }
        goWatchLteBall(rtsp);
    };

    /**
     * LTE 布控球 视频上报
     *
     * @param rtspUrl
     */
    private void goWatchLteBall(String rtspUrl) {
        if (!TextUtils.isEmpty(rtspUrl)) {
            setPushAuthority();
            getView().getLogger().info("播放地址：" + rtspUrl);
            //拉取LTE 上报视频
            //getView().startGB28121Pull();
        }
    }

    /**
     * 请求直播
     */
    private ReceiverRequestVideoHandler receiverRequestVideoHandler = (member) -> {
        if (PadApplication.getPadApplication().getVideoLivePlayingState() != VideoLivePlayingState.IDLE) {
            ToastUtil.showToast(getContext(), getContext().getString(R.string.text_watching_can_not_request_report));
            return;
        }
        if (PadApplication.getPadApplication().getVideoLivePushingState() != VideoLivePushingState.IDLE) {
            ToastUtil.showToast(getContext(), getContext().getString(R.string.text_pushing_can_not_pull));
            return;
        }
        if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_ASK.name())) {
            ToastUtil.showToast(getContext(), getContext().getString(R.string.no_pull_authority));
            return;
        }
        getView().getLogger().error("请求的直播人：" + member);

        if (member.getType() == TerminalMemberType.TERMINAL_LTE.getCode()) {//LTE
            String gb28181No = member.getGb28181No();
            TerminalMessage terminalMessage = new TerminalMessage();
            terminalMessage.messageBody = new JSONObject();
            terminalMessage.messageBody.put(JsonParam.GB28181_RTSP_URL, gb28181No);
            terminalMessage.messageBody.put(JsonParam.DEVICE_NAME, member.getName());
            terminalMessage.messageBody.put(JsonParam.ACCOUNT_ID, member.getNo());
            terminalMessage.messageBody.put(JsonParam.DEVICE_DEPT_NAME, member.getDepartmentName());
            terminalMessage.messageBody.put(JsonParam.DEVICE_DEPT_ID, member.getDeptId());
            goWatchGB28121(terminalMessage);
        } else {
            requestOtherLive(member);
        }
        //通知拉取成功，正在观看 notice
        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveStartPullLiveHandler.class, member);

    };

    private ReceiveGoWatchRTSPHandler receiveGoWatchRTSPHandler = new ReceiveGoWatchRTSPHandler() {
        @Override
        public void handler(TerminalMessage terminalMessage) {
            if (terminalMessage != null) {
                if (terminalMessage.messageType == MessageType.GB28181_RECORD.getCode()) {
                    goWatchGB28121(terminalMessage);
                } else if (terminalMessage.messageType == MessageType.OUTER_GB28181_RECORD.getCode()) {
//                    goWatchOutGB28121(terminalMessage);
                }
            }
        }
    };

    /**
     * 我请求 别人 直播
     */
    private void requestOtherLive(Member member) {
        PromptManager.getInstance().IndividualCallRequestRing();//响铃
        int requestCode = MyTerminalFactory.getSDK().getLiveManager().requestMemberLive(member.getNo(), member.getUniqueNo(), "", false);
        if (requestCode == BaseCommonCode.SUCCESS_CODE) {
            //getView().isShowLiveView(true);
        } else {
            ToastUtil.livingFailToast(getContext(), requestCode, TerminalErrorCode.LIVING_REQUEST.getErrorCode());
            getView().stopPullLive();
        }
    }

    /**
     * LTE 视频上报
     * <p>
     *
     * @param terminalMessage
     */
    private void goWatchGB28121(TerminalMessage terminalMessage) {
        if (terminalMessage.messageBody.containsKey(JsonParam.GB28181_RTSP_URL)) {
            setPushAuthority();
            String deviceId = terminalMessage.messageBody.getString(JsonParam.GB28181_RTSP_URL);
            String gateWayUrl = TerminalFactory.getSDK().getParam(Params.GATE_WAY_URL);
            String gb28181Url = gateWayUrl + "DevAor=" + deviceId;
            getView().getLogger().info("播放地址：" + gb28181Url);
            //拉取LTE 上报视频
            //getView().startGB28121Pull();
        }
        if (terminalMessage.messageBody.containsKey(JsonParam.DEVICE_NAME)) {
            String deviceName = terminalMessage.messageBody.getString(JsonParam.DEVICE_NAME);
            getView().getLogger().info(deviceName + "正在上报图像");
        }
    }

    /**
     * 根据权限设置组呼PTT图标
     */
    private void setPushAuthority() {
        //图像推送
        if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_PUSH.name())) {
            getView().getLogger().info("根据权限设置组呼PTT图标 没权限");
        } else {
            getView().getLogger().info("根据权限设置组呼PTT图标 有权限");
        }
    }


    /**
     * 对方拒绝直播，通知界面关闭响铃页
     **/
    private ReceiveResponseStartLiveHandler receiveReaponseStartLiveHandler = (resultCode, resultDesc)-> mHandler.post(() -> {
        ToastUtil.showToast(getContext(), resultDesc);
        getView().stopPullLive();
    });

    /**
     * 响应超时
     **/
    private ReceiveAnswerLiveTimeoutHandler receiveAnswerLiveTimeoutHandler = ()-> mHandler.post(() -> {
        ToastUtil.showToast(getContext(), getContext().getString(R.string.other_no_answer));
        getView().stopPullLive();
    });

    /**
     * 通知直播停止 通知界面关闭视频页
     **/
    private ReceiveNotifyLivingStoppedHandler receiveNotifyLivingStoppedHandler = (liveMemberId, callId, methodResult, resultDesc)-> mHandler.post(() -> {
        ToastUtil.showToast(getContext(), getContext().getString(R.string.other_no_answer));
        getView().stopPullLive();
    });

    /**
     * 去观看时，发现没有在直播，关闭界面吧
     **/
    private ReceiveMemberNotLivingHandler receiveMemberNotLivingHandler = callId-> mHandler.post(() -> {
        ToastUtil.showToast(getContext(), getContext().getString(R.string.other_no_answer));
        getView().stopPullLive();
    });



    /**
     * 通知终端停止观看直播
     **/
    private ReceiveNotifyMemberStopWatchMessageHandler receiveNotifyMemberStopWatchMessageHandler = message -> {
        ToastUtil.showToast(getContext(), getContext().getString(R.string.force_stop_watch));
        getView().stopPullLive();
    };

    /**
     * 获取到rtsp地址，开始播放视频
     */
    private ReceiveGetRtspStreamUrlHandler receiveGetRtspStreamUrlHandler = (final String rtspUrl, final Member liveMember, long callId) -> {
        if (Util.isEmpty(rtspUrl)) {
            ToastUtil.showToast(getContext(), getContext().getString(R.string.no_rtsp_data));
            getView().stopPullLive();
        } else {
            getView().getLogger().info("rtspUrl ----> " + rtspUrl);
            PromptManager.getInstance().stopRing();
            mHandler.postDelayed(() -> {
                getView().setMemberInfo(liveMember);//设置成员信息
                getView().startPullLive(rtspUrl);//播放
            }, 1200);

            //获取到流地址后，设置分享点击事件
            getView().setShareLiveClickListener(v -> shareLive(liveMember));
        }
    };

    /**
     * 分享上报视频
     * @param member
     */
    private void shareLive(Member member){
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TYPE, Constants.PULL);
        bundle.putBoolean(Constants.PULLING, true);
        bundle.putSerializable(Constants.LIVE_MEMBER,new InviteMemberLiverMember(member.getNo(),member.getUniqueNo()));
        List<Integer> list = new ArrayList<>();
        list.add(member.getNo());
        bundle.putSerializable(Constants.INVITE_MEMBER_EXCEPT_UNIQUE_NO,new InviteMemberExceptList(list));
        SelectMemberFragment.startSelectMemberFragment((FragmentActivity)getContext(),bundle);
    }
}

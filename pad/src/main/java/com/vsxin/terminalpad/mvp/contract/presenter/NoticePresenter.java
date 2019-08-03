package com.vsxin.terminalpad.mvp.contract.presenter;

import android.content.Context;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.ixiaoma.xiaomabus.architecture.mvp.refresh.RefreshPresenter;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.app.PadApplication;
import com.vsxin.terminalpad.mvp.contract.constant.NoticeInCallEnum;
import com.vsxin.terminalpad.mvp.contract.constant.NoticeInOrOutEnum;
import com.vsxin.terminalpad.mvp.contract.constant.NoticeOutCallEnum;
import com.vsxin.terminalpad.mvp.contract.constant.NoticeTypeEnum;
import com.vsxin.terminalpad.mvp.contract.view.INoticeView;
import com.vsxin.terminalpad.mvp.entity.NoticeBean;
import com.vsxin.terminalpad.prompt.PromptManager;
import com.vsxin.terminalpad.utils.SensorUtil;
import com.vsxin.terminalpad.utils.TimeUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.vsx.SpecificSDK.OperateReceiveHandlerUtilSync;
import cn.vsx.SpecificSDK.StateMachineUtils;
import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.Remark;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.TerminalErrorCode;
import cn.vsx.hamster.protolbuf.PTTProtolbuf;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallState;
import cn.vsx.hamster.terminalsdk.manager.terminal.TerminalState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePlayingState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePushingState;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveAnswerIndividualCallTimeoutHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveAnswerLiveTimeoutHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCurrentGroupIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNobodyRequestVideoLiveHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyDataMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyEmergencyMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyEmergencyVideoLiveIncommingMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyIndividualCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyIndividualCallStoppedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyLivingIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseStartIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.SignatureUtil;
import cn.vsx.hamster.terminalsdk.tools.TerminalMessageUtil;
import cn.vsx.hamster.terminalsdk.tools.Util;
import cn.vsx.util.StateMachine.IState;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * @author qzw
 * <p>
 * 左中 代办事项 个呼，视频上传
 */
public class NoticePresenter extends RefreshPresenter<NoticeBean, INoticeView> {

    List<NoticeBean> noticeBeans = new ArrayList<>();

    public NoticePresenter(Context mContext) {
        super(mContext);
    }


    /*****************************被动接收 个呼**********************************/
    /**
     * 被动接收 被动方个呼来了，选择接听或挂断
     */
    private ReceiveNotifyIndividualCallIncommingHandler receiveNotifyIndividualCallIncommingHandler = (mainMemberName, mainMemberId, individualCallType) -> {
        getView().getLogger().info("NoticePresenter 被动方个呼来了，选择接听或挂断");

        startPromptSound();//开启提示音 个呼进来提示音
        NoticeBean notice = new NoticeBean();
        notice.setNoticeType(NoticeTypeEnum.CALL);
        notice.setInOrOut(NoticeInOrOutEnum.IN);
        notice.setInCall(NoticeInCallEnum.CALL_IN_WAIT);
        notice.setMemberName(mainMemberName);
        notice.setMemberId(mainMemberId);
        notice.setStartTime(TimeUtil.getCurrentTime());
        noticeBeans.add(notice);
        getView().notifyDataSetChanged(noticeBeans);
    };

    /**
     * 停止个呼  主动/被动 停止 都会走这个吗？？？？
     *
     * 被动 对方停止个呼 回调
     *
     * 主动 我停止个呼，不会有回调
     *
     */
    private ReceiveNotifyIndividualCallStoppedHandler receiveNotifyIndividualCallStoppedHandler = (methodResult, resultDesc) -> {
        getView().getLogger().info("NoticePresenter 个呼停止");
        stopPromptSound();

        NoticeBean noticeBean = noticeBeans.get(noticeBeans.size() - 1);//取最后一个
        if(noticeBean.getNoticeType()==NoticeTypeEnum.CALL){//是否是个呼
            if(noticeBean.getInOrOut()==NoticeInOrOutEnum.IN){//打进来的
                noticeBean.setInCall(NoticeInCallEnum.CALL_IN_END);//通话结束
            }else{//拨出去的
                noticeBean.setOutCall(NoticeOutCallEnum.CALL_OUT_END);//通话结束
            }
        }
        noticeBean.setStopTime(TimeUtil.getCurrentTime());
        //刷新
        getView().notifyDataSetChanged(noticeBeans);
    };

    /*****************************主动发起 个呼**********************************/

    /**
     * 主动发起个呼
     */
    private ReceiveCurrentGroupIndividualCallHandler receiveCurrentGroupIndividualCallHandler = (member) -> {
        getView().getLogger().info("当前呼叫对象:" + member);

        if(PadApplication.getPadApplication().getVideoLivePlayingState() != VideoLivePlayingState.IDLE){
            ToastUtil.showToast(getContext(),getContext().getString(R.string.text_watching_can_not_private_call));
            return;
        }
        if(PadApplication.getPadApplication().getVideoLivePushingState() != VideoLivePushingState.IDLE){
            ToastUtil.showToast(getContext(),getContext().getString(R.string.text_pushing_can_not_private_call));
            return;
        }
        if(PadApplication.getPadApplication().getIndividualState() != IndividualCallState.IDLE){
            ToastUtil.showToast(getContext(),getContext().getString(R.string.text_personal_calling_can_not_do_others));
            return;
        }

        SensorUtil.getInstance().registSensor();
        PromptManager.getInstance().IndividualCallRequestRing();
        int resultCode = MyTerminalFactory.getSDK().getIndividualCallManager().requestIndividualCall(member.getId(),member.getUniqueNo(),"");
        if (resultCode == BaseCommonCode.SUCCESS_CODE){
            NoticeBean notice = new NoticeBean();
            notice.setNoticeType(NoticeTypeEnum.CALL);
            notice.setInOrOut(NoticeInOrOutEnum.OUT);
            notice.setOutCall(NoticeOutCallEnum.CALL_OUT_WAIT);

            notice.setMemberName(member.getName());
            notice.setMemberId(member.getId());

            notice.setStartTime(TimeUtil.getCurrentTime());

            noticeBeans.add(notice);
            getView().notifyDataSetChanged(noticeBeans);

        }else {
            ToastUtil.individualCallFailToast(getContext(), resultCode);
            stopIndividualCall();
        }
    };

    /**
     * 主动方请求个呼回应
     * <p>
     * 我主动给别人发个呼的   别人给我的回应
     */
    private ReceiveResponseStartIndividualCallHandler receiveResponseStartIndividualCallHandler = (resultCode, resultDesc, individualCallType) -> {
        getView().getLogger().info(" NoticePresenter ReceiveResponseStartIndividualCallHandler====" + "resultCode:" + resultCode + "=====resultDesc:" + resultDesc);
        if (resultCode == BaseCommonCode.SUCCESS_CODE) {//对方接听
            getView().getLogger().info("NoticePresenter 对方接受了你的个呼:" + resultCode + resultDesc + "callType;" + individualCallType);

            NoticeBean noticeBean = noticeBeans.get(noticeBeans.size() - 1);//取最后一个
            if(noticeBean.getNoticeType()==NoticeTypeEnum.CALL){//是否是个呼
                if(noticeBean.getInOrOut()==NoticeInOrOutEnum.IN){//打进来的
                    noticeBean.setInCall(NoticeInCallEnum.CALL_IN_CONNECT);//正在通话中
                }else{//拨出去的
                    noticeBean.setOutCall(NoticeOutCallEnum.CALL_OUT_CONNECT);//正在通话中
                }
            }
            noticeBean.setStopTime(TimeUtil.getCurrentTime());
            //刷新
            getView().notifyDataSetChanged(noticeBeans);

        } else {//对方拒绝
            getView().getLogger().info("NoticePresenter 对方拒绝了你的个呼:" + resultCode + resultDesc + "callType;" + individualCallType);

            NoticeBean noticeBean = noticeBeans.get(noticeBeans.size() - 1);//取最后一个
            if(noticeBean.getNoticeType()==NoticeTypeEnum.CALL){//是否是个呼
                if(noticeBean.getInOrOut()==NoticeInOrOutEnum.IN){//打进来的
                    noticeBean.setInCall(NoticeInCallEnum.CALL_IN_REFUSE);//拒接接听
                }else{//拨出去的
                    noticeBean.setOutCall(NoticeOutCallEnum.CALL_OUT_REFUSE);//拒接接听
                }
            }
            noticeBean.setStopTime(TimeUtil.getCurrentTime());
            //刷新
            getView().notifyDataSetChanged(noticeBeans);
        }
    };

    /**
     * 主动or被动 超时都会收到此回调
     */
    private ReceiveAnswerIndividualCallTimeoutHandler receiveAnswerIndividualCallTimeoutHandler = () -> {
        getView().getLogger().info("NoticePresenter 呼叫超时");
        stopPromptSound();
        NoticeBean noticeBean = noticeBeans.get(noticeBeans.size() - 1);//取最后一个

        if(noticeBean.getNoticeType()==NoticeTypeEnum.CALL){//是否是个呼
            if(noticeBean.getInOrOut()==NoticeInOrOutEnum.IN){//打进来的
                noticeBean.setInCall(NoticeInCallEnum.CALL_IN_TIME_OUT);//超时未接听
            }else{//拨出去的
                noticeBean.setOutCall(NoticeOutCallEnum.CALL_OUT_TIME_OUT);//超时未接听
            }
        }
        noticeBean.setStopTime(TimeUtil.getCurrentTime());
        //刷新
        getView().notifyDataSetChanged(noticeBeans);
    };

    /**
     * 开启提示音
     */
    private void startPromptSound() {
        PromptManager.getInstance().IndividualCallNotifyRing();
    }

    /**
     * 关闭提示音
     */
    private void stopPromptSound() {
        PromptManager.getInstance().IndividualHangUpRing();
        PromptManager.getInstance().delayedStopRing();
    }

    public void registReceiveHandler() {
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyIndividualCallIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveAnswerIndividualCallTimeoutHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyIndividualCallStoppedHandler);

        //主动发起 个呼
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiveCurrentGroupIndividualCallHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseStartIndividualCallHandler);
        //别人邀请我上报视频
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyLivingIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNobodyRequestVideoLiveHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveAnswerLiveTimeoutHandler);
        //邀请我观看
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveNotifyDataMessageHandler);
    }

    public void unRegistReceiveHandler() {
        //被动接收 个呼
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyIndividualCallIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveAnswerIndividualCallTimeoutHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyIndividualCallStoppedHandler);

        //主动发起 个呼
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiveCurrentGroupIndividualCallHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseStartIndividualCallHandler);

        //别人邀请我上报视频
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyLivingIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNobodyRequestVideoLiveHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveAnswerLiveTimeoutHandler);
        //邀请我观看
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveNotifyDataMessageHandler);
    }

    /**
     * 挂断个呼后
     */
    public void stopIndividualCall() {
        MyTerminalFactory.getSDK().getIndividualCallManager().ceaseIndividualCall();
        stopPromptSound();
        revertStateMachine();
    }

    /**
     * 接听 个呼
     */
    public void startIndividualCall() {
        stopPromptSound();
        MyTerminalFactory.getSDK().getIndividualCallManager().responseIndividualCall(true);
    }

    /**
     * 重置状态机
     */
    protected void revertStateMachine() {
        if (PadApplication.getPadApplication().getIndividualState() != IndividualCallState.IDLE) {
            MyTerminalFactory.getSDK().getIndividualCallManager().ceaseIndividualCall();
        }
    }

    /*********************************************别人请求我视频上报*************************************************/

    /**
     * 收到别人请求我开启直播的通知
     * <p>
     * 接收/拒绝
     **/
    private ReceiveNotifyLivingIncommingHandler receiveNotifyLivingIncommingHandler = (mainMemberName, mainMemberId, emergencyType) -> {
//        NoticeBean notice = new NoticeBean();
//        notice.setNoticeType(NoticeBean.LIVE);
//        notice.setMemberName(mainMemberName);
//        notice.setMemberId(mainMemberId);
//        notice.setEmergencyType(emergencyType);
//        noticeBeans.add(notice);
//        getView().notifyDataSetChanged(noticeBeans);

        if (emergencyType) {//强制上报图像
            //如果在组呼或者听组呼时  就停止
            Map<TerminalState, IState<?>> currentStateMap = TerminalFactory.getSDK().getTerminalStateManager().getCurrentStateMap();
            if (currentStateMap.containsKey(TerminalState.GROUP_CALL_LISTENING) || currentStateMap.containsKey(TerminalState.GROUP_CALL_SPEAKING)) {
                TerminalFactory.getSDK().getGroupCallManager().ceaseGroupCall();
            }
            PromptManager.getInstance().startReportByNotity();
            PadApplication.getPadApplication().isPrivateCallOrVideoLiveHand = true;
        } else {
            PromptManager.getInstance().VideoLiveInCommimgRing();
        }
    };

    /**
     * 收到强制上报图像的通知
     */
    private ReceiveNotifyEmergencyVideoLiveIncommingMessageHandler receiveNotifyEmergencyVideoLiveIncommingMessageHandler = message -> {
        Map<TerminalState, IState<?>> currentStateMap = TerminalFactory.getSDK().getTerminalStateManager().getCurrentStateMap();
        //观看上报图像,个呼
        if (currentStateMap.containsKey(TerminalState.VIDEO_LIVING_PLAYING) || currentStateMap.containsKey(TerminalState.INDIVIDUAL_CALLING)) {
            TerminalFactory.getSDK().notifyReceiveHandler(ReceiveNotifyEmergencyMessageHandler.class);
        }
        //开启上报功能
        //myHandler.postDelayed(() -> TerminalFactory.getSDK().getLiveManager().openFunctionToLivingIncomming(message),1000);
    };

    /**
     * 对方取消了
     * 收到没人请求我开视频的消息，关闭界面和响铃
     */
    private ReceiveNobodyRequestVideoLiveHandler receiveNobodyRequestVideoLiveHandler = () -> {
        ToastUtil.showToast(getContext(), getContext().getString(R.string.other_cancel));
        MyTerminalFactory.getSDK().getLiveManager().ceaseLiving();
        stopBusiness();
    };

    /**
     * 超时未回复answer 通知界面关闭
     **/
    private ReceiveAnswerLiveTimeoutHandler receiveAnswerLiveTimeoutHandler = () -> {
        PromptManager.getInstance().stopRing();//停止响铃
        ToastUtil.showToast(getContext(), getContext().getString(R.string.other_cancel));
        stopBusiness();
    };

    /**
     * 退出业务状态
     */
    protected void stopBusiness() {
        PromptManager.getInstance().stopRing();
        SensorUtil.getInstance().unregistSensor();
        StateMachineUtils.revertStateMachine();
    }

    /**
     * 同意上报直播
     */
    public void acceptLive(NoticeBean noticeBean) {
        MyTerminalFactory.getSDK().getLiveManager().responseLiving(true);
        PromptManager.getInstance().stopRing();
        PadApplication.getPadApplication().isPrivateCallOrVideoLiveHand = true;
    }

    /***************************************别人上报视频 邀请我观看***********************************************/

    /**
     * 记录紧急观看的CallId，防止PC端重复发送强制观看的消息
     */
    private String emergencyCallId;

    /**
     * 强制观看直播，关闭所有正在进行的业务
     */
    private void emergencyWatchCloseAnything() {
        Map<TerminalState, IState<?>> currentStateMap = StateMachineUtils.getCurrentStateMap();
        if (currentStateMap.containsKey(TerminalState.VIDEO_LIVING_PUSHING)
                || currentStateMap.containsKey(TerminalState.VIDEO_LIVING_PLAYING)
                || currentStateMap.containsKey(TerminalState.INDIVIDUAL_CALLING)) {
            //停止正在进行的直播相关操作
            TerminalFactory.getSDK().notifyReceiveHandler(ReceiveNotifyEmergencyMessageHandler.class);
        }
        if (currentStateMap.containsKey(TerminalState.GROUP_CALL_LISTENING) || currentStateMap.containsKey(TerminalState.GROUP_CALL_SPEAKING)) {
            TerminalFactory.getSDK().getGroupCallManager().ceaseGroupCall();
        }
    }

    /**
     * 接收到消息
     */
    protected ReceiveNotifyDataMessageHandler mReceiveNotifyDataMessageHandler = terminalMessage -> {
        getView().getLogger().info("接收到消息" + terminalMessage.toString());

        //是否为别人发的消息
        if (!TerminalMessageUtil.isReceiver(terminalMessage)) {
            return;
        }
        //视频消息
        if (terminalMessage.messageType == MessageType.VIDEO_LIVE.getCode()) {
            //紧急观看
            if (terminalMessage.messageBody.getInteger(JsonParam.REMARK) == Remark.EMERGENCY_INFORM_TO_WATCH_LIVE) {
                //停止一切业务，开始观看
                String callId = terminalMessage.messageBody.getString(JsonParam.CALLID);
                //过滤重复收到强制观看的消息
                if (TextUtils.isEmpty(emergencyCallId) || (!TextUtils.isEmpty(emergencyCallId) && !TextUtils.equals(callId, emergencyCallId))) {
                    emergencyCallId = callId;
                    emergencyWatchCloseAnything();
                    PromptManager.getInstance().startPlayByNotity();
                    //要延时观看
                    goToWatch(terminalMessage, -1);
                    //myHandler.post(() -> PromptManager.getInstance().startPlayByNotity());
                    //myHandler.postDelayed(() -> goToWatch(terminalMessage,-1),1000);
                }
                return;
            } else if (terminalMessage.messageBody.getInteger(JsonParam.REMARK) == Remark.INFORM_TO_WATCH_LIVE) {
                if (StateMachineUtils.getIndividualState() == IndividualCallState.IDLE && !PadApplication.getPadApplication().isPttPress) {
                    // 判断是否是组内上报，组内上报不弹窗
                    if (!TerminalMessageUtil.isGroupMessage(terminalMessage)) {
                        //延迟弹窗，否则判断是否在上报接口返回的是没有在上报
                        //Todo 弹框 提示有人上报，取观看
//                        NoticeBean notice = new NoticeBean();
//                        notice.setNoticeType(NoticeBean.WATCH);
//                        notice.setMemberName(terminalMessage.messageFromName);
//                        notice.setMemberId(terminalMessage.messageFromId);
//                        notice.setEmergencyType(false);
//                        notice.setTerminalMessage(terminalMessage);
//                        noticeBeans.add(notice);
//                        getView().notifyDataSetChanged(noticeBeans);
                    }
                }
            }
        } else if (terminalMessage.messageType == MessageType.GB28181_RECORD.getCode() ||
                terminalMessage.messageType == MessageType.OUTER_GB28181_RECORD.getCode()) {
            //国标平台消息
            if (StateMachineUtils.getIndividualState() == IndividualCallState.IDLE && !PadApplication.getPadApplication().isPttPress) {
                if (!TerminalMessageUtil.isGroupMessage(terminalMessage)) {
                    //延迟弹窗，否则判断是否在上报接口返回的是没有在上报
                    //Todo 弹框 提示有人上报，取观看

                }
            }
        }

        String callId = terminalMessage.messageBody.getString(JsonParam.CALLID);
    };


    /**
     * 去观看
     */
    public void goToWatch(TerminalMessage terminalMessage, int position) {
        //判断是否有接受图像功能权限
        if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_ACCEPT.name())) {
            ToastUtil.showToast(getContext(), getContext().getString(R.string.text_has_no_video_receiver_authority));
            return;
        }
        //判断消息类型
        if (terminalMessage.messageType == MessageType.VIDEO_LIVE.getCode()) {
            MyTerminalFactory.getSDK().getThreadPool().execute(() -> {
                String serverIp = MyTerminalFactory.getSDK().getParam(Params.FILE_SERVER_IP, "");
                int serverPort = MyTerminalFactory.getSDK().getParam(Params.FILE_SERVER_PORT, 0);
                String url = "http://" + serverIp + ":" + serverPort + "/file/download/isLiving";
                Map<String, String> paramsMap = new HashMap<>();
                paramsMap.put("callId", terminalMessage.messageBody.getString(JsonParam.CALLID));
                paramsMap.put("sign", SignatureUtil.sign(paramsMap));
                getView().getLogger().info("查看视频播放是否结束url：" + url);
                String result = MyTerminalFactory.getSDK().getHttpClient().sendGet(url, paramsMap);
                getView().getLogger().info("查看视频播放是否结束结果：" + result);
                if (!Util.isEmpty(result)) {
                    JSONObject jsonObject = JSONObject.parseObject(result);
                    boolean living = jsonObject.getBoolean("living");
                    if (living) {//正在直播
                        requestToWatchLiving(terminalMessage);
                    } else {//直播结束，观看历史视频
//                        removeView();
//                        Intent intent = new Intent(MyTerminalFactory.getSDK().application, PlayLiveHistoryActivity.class);
//                        intent.putExtra("terminalMessage", terminalMessage);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        //                                intent.putExtra("endChatTime",endChatTime);
//                        startActivity(intent);
                    }
                }
            });
        } else if (terminalMessage.messageType == MessageType.GB28181_RECORD.getCode()) {
            //goWatchGB28121(terminalMessage);
        } else if (terminalMessage.messageType == MessageType.OUTER_GB28181_RECORD.getCode()) {
            //goWatchOutGB28121(terminalMessage);
        }
    }

    private void requestToWatchLiving(TerminalMessage terminalMessage) {
        PTTProtolbuf.NotifyDataMessage.Builder builder = PTTProtolbuf.NotifyDataMessage.newBuilder();
        builder.setMessageUrl(org.apache.http.util.TextUtils.isEmpty(terminalMessage.messageUrl) ? "" : terminalMessage.messageUrl);
        builder.setMessageFromName(terminalMessage.messageFromName);
        builder.setMessageFromNo(terminalMessage.messageFromId);
        builder.setMessageFromUniqueNo(terminalMessage.messageFromUniqueNo);
        builder.setMessageToName(terminalMessage.messageToName);
        builder.setMessageToNo(terminalMessage.messageToId);
        builder.setMessageToUniqueNo(terminalMessage.messageToUniqueNo);
        builder.setMessageType(terminalMessage.messageType);
        builder.setMessageVersion(terminalMessage.messageVersion);
        builder.setResultCode(terminalMessage.resultCode);
        builder.setSendingTime(terminalMessage.sendTime);
        builder.setMessageBody(terminalMessage.messageBody.toString());
        PTTProtolbuf.NotifyDataMessage message = builder.build();
        int resultCode = MyTerminalFactory.getSDK().getLiveManager().requestToWatchLiving(message);
        if (resultCode == 0) {
            //live_theme = terminalMessage1.messageFromName + "上报图像";
            if (TextUtils.isEmpty(terminalMessage.messageBody.getString(JsonParam.TITLE))) {
                String liver = (String) terminalMessage.messageBody.get("liver");
                if (!TextUtils.isEmpty(liver)) {
                    if (liver.contains("_")) {
                        String[] split = liver.split("_");
                        if (split.length > 0) {
                            String theme = String.format(getContext().getString(R.string.text_pushing_video_name), split[1]);
                        }
                    }
                }
            } else {
                String theme = terminalMessage.messageBody.getString(JsonParam.TITLE);
            }
            // mLiveVedioTheme.setText(theme);
        } else {
            ToastUtil.livingFailToast(getContext(), resultCode, TerminalErrorCode.LIVING_PLAYING.getErrorCode());
        }
    }
}

package com.vsxin.terminalpad.mvp.contract.presenter;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import com.alibaba.fastjson.JSONObject;
import com.ixiaoma.xiaomabus.architecture.mvp.BasePresenter;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.app.PadApplication;
import com.vsxin.terminalpad.mvp.contract.view.ILiveView;
import com.vsxin.terminalpad.prompt.PromptManager;
import com.vsxin.terminalpad.receiveHandler.ReceiveGoWatchRTSPHandler;
import com.vsxin.terminalpad.receiveHandler.ReceivePullLivingHandler;
import com.vsxin.terminalpad.receiveHandler.ReceiveStartPullLiveHandler;
import com.vsxin.terminalpad.receiveHandler.ReceiverActivePushVideoHandler;
import com.vsxin.terminalpad.receiveHandler.ReceiverRequestLteBullHandler;
import com.vsxin.terminalpad.receiveHandler.ReceiverRequestVideoHandler;
import com.vsxin.terminalpad.utils.OperateReceiveHandlerUtilSync;
import com.vsxin.terminalpad.utils.SensorUtil;
import com.vsxin.terminalpad.utils.StateMachineUtils;

import org.easydarwin.push.EasyPusher;
import org.easydarwin.push.InitCallback;
import org.easydarwin.push.MediaStream;
import org.easydarwin.video.EasyRTSPClient;
import org.easydarwin.video.RTSPClient;

import java.util.ArrayList;
import java.util.Arrays;
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
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveExternStorageSizeHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetRtspStreamUrlHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetVideoPushUrlHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallCeasedIndicationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberJoinOrExitHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberNotLivingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyLivingStoppedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyMemberStopWatchMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyOtherStopVideoMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRequestGroupCallConformationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseMyselfLiveHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseStartLiveHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSupportResolutionHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.Util;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * Created by PC on 2018/11/1.
 */

public class LivePresenter extends BasePresenter<ILiveView> {

    private ArrayList<String> uniqueNos;
    private String ip;
    private String port;
    private String id;


    private int width = 640;
    private int height = 480;
    private List<String> listResolution = new ArrayList<>(Arrays.asList("1920x1080", "1280x720", "640x480", "320x240"));

    private int pushcount;
    private PushCallback pushCallback;
    private EasyRTSPClient mStreamRender;
    private RtspReceiver mResultReceiver;
    private int mLiveWidth;
    private int mLiveHeight;
    private String gb28181Url;

    public LivePresenter(Context mContext) {
        super(mContext);
    }


    /**
     * 注册监听
     */
    public void registReceiveHandler() {
        //自己主动上报
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiverActivePushVideoHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseMyselfLiveHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyLivingStoppedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGetVideoPushUrlHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveMemberJoinOrExitHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveExternStorageSizeHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyOtherStopVideoMessageHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveSupportResolutionHandler);
        //自己主动请求他人上报
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiverRequestVideoHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveReaponseStartLiveHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveAnswerLiveTimeoutHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGetRtspStreamUrlHandler);

        MyTerminalFactory.getSDK().registReceiveHandler(receiveMemberNotLivingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveRequestGroupCallConformationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveCeaseGroupCallConformationHander);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyMemberStopWatchMessageHandler);

        //拉取不控球视频
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiverRequestLteBullHandler);
    }

    /**
     * 取消监听
     */
    public void unregistReceiveHandler() {
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiverActivePushVideoHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseMyselfLiveHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyLivingStoppedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetVideoPushUrlHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMemberJoinOrExitHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveExternStorageSizeHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyOtherStopVideoMessageHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveSupportResolutionHandler);

        //自己主动请求他人上报
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiverRequestVideoHandler);

        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveReaponseStartLiveHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveAnswerLiveTimeoutHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetRtspStreamUrlHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMemberNotLivingHandler);

        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveRequestGroupCallConformationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveCeaseGroupCallConformationHander);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyMemberStopWatchMessageHandler);

        //拉取不控球视频
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiverRequestLteBullHandler);
    }

    /***************************************1自己主动上报视频*******************************************/

    /**
     * 自己主动上报视频
     */
    private ReceiverActivePushVideoHandler receiverActivePushVideoHandler = (uniqueNoAndType, isGroupPushLive) -> {
        if (PadApplication.getPadApplication().getVideoLivePlayingState() != VideoLivePlayingState.IDLE) {
            ToastUtil.showToast(MyTerminalFactory.getSDK().application, getContext().getString(R.string.text_watching_can_not_report));
            return;
        }
        if (PadApplication.getPadApplication().getVideoLivePushingState() != VideoLivePushingState.IDLE) {
            ToastUtil.showToast(MyTerminalFactory.getSDK().application, getContext().getString(R.string.text_pushing_can_not_report));
            return;
        }
        getView().getLogger().error("上报给：" + uniqueNoAndType);
        if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_PUSH.name())) {
            ToastUtil.showToast(MyTerminalFactory.getSDK().application, getContext().getResources().getString(R.string.no_push_authority));
            return;
        }
//        if (TextUtils.isEmpty(uniqueNoAndType)) {//要弹出选择成员页
//            Intent intent = new Intent(ReceiveHandlerService.this, InviteMemberService.class);
//            intent.putExtra(Constants.TYPE, Constants.PUSH);
//            intent.putExtra(Constants.PUSHING, false);
//            intent.putExtra(Constants.IS_GROUP_PUSH_LIVING, isGroupPushLive);
//            intent.putExtra(Constants.INVITE_MEMBER_EXCEPT_UNIQUE_NO, new InviteMemberExceptList());
//            startService(intent);
//        } else {//直接上报了
        uniqueNos = new ArrayList<>();
        uniqueNos.add(uniqueNoAndType);

        //请求成功,直接开始推送视频
        int requestCode = MyTerminalFactory.getSDK().getLiveManager().requestMyselfLive("", "");
        if (requestCode != BaseCommonCode.SUCCESS_CODE) {
            ToastUtil.livingFailToast(getContext(), requestCode, TerminalErrorCode.LIVING_PUSHING.getErrorCode());
            MyTerminalFactory.getSDK().getLiveManager().ceaseLiving();
        } else {
            getView().isShowLiveView(true);
        }
//        }
    };

    /**
     * 自己主动上报视频，并推送给其他人
     */
    private ReceiveResponseMyselfLiveHandler receiveResponseMyselfLiveHandler = (resultCode, resultDesc) -> {
        if (resultCode == BaseCommonCode.SUCCESS_CODE) {
            if (uniqueNos != null && !uniqueNos.isEmpty()) {
                getView().getLogger().info("自己发起直播成功,要推送的列表：" + uniqueNos);
                MyTerminalFactory.getSDK().getLiveManager().requestNotifyWatch(uniqueNos,
                        MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0),
                        TerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO, 0L));
            }
        } else {
            ToastUtil.showToast(getContext(), resultDesc);
            finishVideoLive();
        }
    };

    /**
     * 通知直播停止 通知界面关闭视频页
     **/
    private ReceiveNotifyLivingStoppedHandler receiveNotifyLivingStoppedHandler = (liveMemberId, callId, methodResult, resultDesc) -> {
        ToastUtil.showToast(getContext(), getContext().getString(R.string.push_stoped));
        finishVideoLive();
    };

    /**
     * 自己发起直播的响应 or 别人请求我直播  上传视频回调
     **/
    private ReceiveGetVideoPushUrlHandler receiveGetVideoPushUrlHandler = (streamMediaServerIp, streamMediaServerPort, callId) -> {
        getView().getLogger().info("自己发起直播，服务端返回的ip：" + streamMediaServerIp + "端口：" + streamMediaServerPort + "---callId:" + callId);
        ip = streamMediaServerIp;
        port = String.valueOf(streamMediaServerPort);
        id = TerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO, 0L) + "_" + callId;
        getView().startPush();
    };

    /**
     * 观看成员的进入和退出
     **/
    private ReceiveMemberJoinOrExitHandler receiveMemberJoinOrExitHandler = (memberName, memberId, joinOrExit) -> {
        Log.e("IndividualCallService", memberName + ",memberId:" + memberId);
        if (joinOrExit) {//进入直播间
            getView().getLogger().info("memberName:" + memberName + "----进入直播间");
        } else {//退出直播间
            getView().getLogger().info("memberName:" + memberName + "----退出直播间");
        }
    };

    /**
     * 组呼进来了
     */
    private ReceiveGroupCallIncommingHandler receiveGroupCallIncommingHandler = (memberId, memberName, groupId, groupName, currentCallMode, uniqueNo) -> {
        if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_LISTEN.name())) {
            ToastUtil.showToast(getContext(), getContext().getString(R.string.text_has_no_group_call_listener_authority));
        } else {
            getView().getLogger().info("memberName:" + memberName + "----组呼进来了");
        }
    };

    /**
     * 收到组呼停止
     */
    private ReceiveGroupCallCeasedIndicationHandler receiveGroupCallCeasedIndicationHandler = (reasonCode) -> {
        getView().getLogger().info("收到组呼停止");
    };

    /**
     * 更新配置 看上报时 有没有组呼的权限
     */
    private ReceiveUpdateConfigHandler mReceiveUpdateConfigHandler = () -> {
        setPushAuthority();
    };

    /**
     * 通知存储空间不足
     */
    private ReceiveExternStorageSizeHandler mReceiveExternStorageSizeHandler = memorySize -> {
        if (memorySize < 100) {
            ToastUtil.showToast(getContext(), getContext().getString(R.string.toast_tempt_insufficient_storage_space));
            PromptManager.getInstance().startExternNoStorage();
            if (getView().getmMediaStream() != null && getView().getmMediaStream().isRecording()) {
                //停止录像
                getView().getmMediaStream().stopRecord();
            }
            //上传没有上传的文件，删除已经上传的文件
            MyTerminalFactory.getSDK().getFileTransferOperation().externNoStorageOperation();
        } else if (memorySize < 200) {
            PromptManager.getInstance().startExternStorageNotEnough();
            ToastUtil.showToast(getContext(), getContext().getString(R.string.toast_tempt_storage_space_is_in_urgent_need));
        }
    };

    /**
     * 收到上报停止的通知
     */
    private ReceiveNotifyOtherStopVideoMessageHandler receiveNotifyOtherStopVideoMessageHandler = (message) -> {
        getView().getLogger().info("收到停止上报通知");
        finishVideoLive();
    };

    /**
     * 更新分辨率
     */
    private ReceiveSupportResolutionHandler receiveSupportResolutionHandler = new ReceiveSupportResolutionHandler() {
        @Override
        public void Handle() {
            setResolution();
            if (getView().getmMediaStream() != null) {
                getView().getmMediaStream().updateResolution(width, height);
            }
        }
    };

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

    public void startPush(SurfaceTexture surface) {
        if (TextUtils.isEmpty(ip) || TextUtils.isEmpty(port) || TextUtils.isEmpty(id)) {
            return;
        }
        getView().getLogger().info("mMediaStream:" + getView().getmMediaStream() + "----SurfaceTexture:" + surface);
        if (getView().getmMediaStream() == null) {
            if (surface != null) {
                getView().isShowLiveView(true);
                pushStream(surface);
            } else {
                ToastUtil.showToast(getContext(), getContext().getString(R.string.push_failed));
                finishVideoLive();
                return;
            }
        }
        if (null == pushCallback) {
            pushCallback = new PushCallback();
        }
        getView().getmMediaStream().startStream(ip, port, id, pushCallback);

        String sdp = TerminalFactory.getSDK().getLiveManager().getLivePathSdp();
        String url = String.format("rtsp://%s:%s/%s"+sdp, ip, port, id);
        getView().getLogger().info("推送地址：" + url);

    }

    public void pushStream(SurfaceTexture surface) {
        if (getView().getmMediaStream() != null) {    // switch from background to front
            getView().getmMediaStream().stopPreview();
            getView().getmMediaStream().setSurfaceTexture(surface);
            getView().getmMediaStream().startPreview();
            if (getView().getmMediaStream().isStreaming()) {
                ToastUtil.showToast(getContext(), getContext().getString(R.string.pushing_stream));
            }
        } else {
            getView().setMediaStream(new MediaStream(getContext(), surface, true));
            startCamera();
        }
        pushcount = 0;
    }

    private void startCamera() {
        setResolution();
        getView().getLogger().error("分辨率--width:" + width + "----height:" + height);
        getView().getmMediaStream().updateResolution(width, height);
        getView().getmMediaStream().setDgree(getDgree());
        getView().getmMediaStream().createCamera();
        getView().getmMediaStream().startPreview();
        startRecord();
        if (getView().getmMediaStream().isStreaming()) {
            ToastUtil.showToast(getContext(), getContext().getString(R.string.pushing_stream));
        }
    }

    /**
     * 开始录像
     */
    private void startRecord() {
        //开始录像
        if (TerminalFactory.getSDK().checkeExternalStorageIsAvailable(MyTerminalFactory.getSDK().getFileTransferOperation().getExternalUsableStorageDirectory())) {
            getView().getmMediaStream().startRecord();
        }
    }

    /**
     * 根据相机分辨率 设置分辨率
     */
    private void setResolution() {
        List<String> supportListResolution = org.easydarwin.util.Util.getSupportResolution(getContext());
        if (null == supportListResolution || supportListResolution.isEmpty()) {
            return;
        }
        int position = MyTerminalFactory.getSDK().getParam(Params.VIDEO_RESOLUTION, 2);
        String setResolution = listResolution.get(position);
        if (supportListResolution.contains(setResolution)) {
            String[] splitR = setResolution.split("x");
            width = Integer.parseInt(splitR[0]);
            height = Integer.parseInt(splitR[1]);
        } else {
            getView().getLogger().info("支持的分辨率：" + supportListResolution);
            String[] splitR = supportListResolution.get(0).split("x");
            width = Integer.parseInt(splitR[0]);
            height = Integer.parseInt(splitR[1]);
        }
    }

    /**
     * 获取屏幕旋转角度
     *
     * @return
     */
    private int getDgree() {
        int rotation = getView().getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                break; // Natural orientation
            case Surface.ROTATION_90:
                degrees = 90;
                break; // Landscape left
            case Surface.ROTATION_180:
                degrees = 180;
                break;// Upside down
            case Surface.ROTATION_270:
                degrees = 270;
                break;// Landscape right
            default:
                break;
        }
        return degrees;
    }


    public void finishVideoLive() {
        stopBusiness();
        stopPush();
        stopPull();
        getView().isShowLiveView(false);
    }

    private void stopPush() {
        if (getView().getmMediaStream() != null) {
            getView().getmMediaStream().stopPreview();
            getView().getmMediaStream().stopStream();
            getView().getmMediaStream().stopRecord();
            getView().getmMediaStream().release();
            getView().setMediaStream(null);
            getView().getLogger().info("---->>>>页面关闭，停止推送视频");
        }
    }

    private class PushCallback implements InitCallback {

        @Override
        public void onCallback(int code) {
            Bundle resultData = new Bundle();
            switch (code) {
                case EasyPusher.OnInitPusherCallback.CODE.EASY_ACTIVATE_INVALID_KEY:
                    resultData.putString("event-msg", "EasyRTSP 无效Key");
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_ACTIVATE_SUCCESS:
                    resultData.putString("event-msg", "EasyRTSP 激活成功");
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_CONNECTING:
                    resultData.putString("event-msg", "EasyRTSP 连接中");
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_CONNECTED:
                    resultData.putString("event-msg", "EasyRTSP 连接成功");
                    pushcount = 0;
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_CONNECT_FAILED:
                    resultData.putString("event-msg", "EasyRTSP 连接失败");
                    if (pushcount <= 10) {
                        pushcount++;
                    } else {
                        finishVideoLive();
                    }
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_CONNECT_ABORT:
                    resultData.putString("event-msg", "EasyRTSP 连接异常中断");
                    if (pushcount <= 10) {
                        pushcount++;
//                        startPush();
                    } else {
                        finishVideoLive();
                    }
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_PUSHING:
                    resultData.putString("event-msg", "EasyRTSP 推流中");
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_DISCONNECTED:
                    resultData.putString("event-msg", "EasyRTSP 断开连接");
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_ACTIVATE_PLATFORM_ERR:
                    resultData.putString("event-msg", "EasyRTSP 平台不匹配");
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_ACTIVATE_COMPANY_ID_LEN_ERR:
                    resultData.putString("event-msg", "EasyRTSP 断授权使用商不匹配");
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_ACTIVATE_PROCESS_NAME_LEN_ERR:
                    resultData.putString("event-msg", "EasyRTSP 进程名称长度不匹配");
                    break;
                default:
                    resultData.putString("event-msg", "EasyRTSP 其它异常");
                    break;
            }

            getView().getLogger().info("event-msg" + resultData.get("event-msg"));
        }
    }

    /******************************************2自己主动请求别人上报**********************************************/

    private boolean isGB28181Live = false;

    public boolean isGB28181Live() {
        return isGB28181Live;
    }

    /**
     * 拉取不控球视频
     */
    private ReceiverRequestLteBullHandler receiverRequestLteBullHandler = (rtsp,type,title) -> {
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
        if (member == null || member.getNo() == 0) {//选择要上报的人
//            Intent intent = new Intent(ReceiveHandlerService.this, InviteMemberService.class);
//            intent.putExtra(Constants.TYPE, Constants.PULL);
//            intent.putExtra(Constants.PULLING, false);
//            intent.putExtra(Constants.INVITE_MEMBER_EXCEPT_UNIQUE_NO, new InviteMemberExceptList());
//            startService(intent);

        } else {//直接请求
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
//                Intent intent = new Intent(ReceiveHandlerService.this, LiveRequestService.class);
//                intent.putExtra(Constants.MEMBER_NAME, member.getName());
//                intent.putExtra(Constants.MEMBER_ID, member.getNo());
//                intent.putExtra(Constants.UNIQUE_NO, member.getUniqueNo());
//                startService(intent);
                requestOtherLive(member);

            }
            //通知拉取成功，正在观看 notice
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveStartPullLiveHandler.class, member);
        }
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
        isGB28181Live = false;
        PromptManager.getInstance().IndividualCallRequestRing();//响铃
        int requestCode = MyTerminalFactory.getSDK().getLiveManager().requestMemberLive(member.getNo(), member.getUniqueNo(), "", false);
        if (requestCode == BaseCommonCode.SUCCESS_CODE) {
            //getView().isShowLiveView(true);
        } else {
            ToastUtil.livingFailToast(getContext(), requestCode, TerminalErrorCode.LIVING_REQUEST.getErrorCode());
            stopRequestOtherLive();
        }
    }

    /**
     * LTE 视频上报
     * <p>
     * Todo LTE是个啥呀？？？？？？？？
     *
     * @param terminalMessage
     */
    private void goWatchGB28121(TerminalMessage terminalMessage) {
        isGB28181Live = true;
        if (terminalMessage.messageBody.containsKey(JsonParam.GB28181_RTSP_URL)) {
            setPushAuthority();
            String deviceId = terminalMessage.messageBody.getString(JsonParam.GB28181_RTSP_URL);
            String gateWayUrl = TerminalFactory.getSDK().getParam(Params.GATE_WAY_URL);
            gb28181Url = gateWayUrl + "DevAor=" + deviceId;
            getView().getLogger().info("播放地址：" + gb28181Url);
            //拉取LTE 上报视频
            getView().startGB28121Pull();
        }
        if (terminalMessage.messageBody.containsKey(JsonParam.DEVICE_NAME)) {
            String deviceName = terminalMessage.messageBody.getString(JsonParam.DEVICE_NAME);
            getView().getLogger().info(deviceName + "正在上报图像");
        }
    }


    /**
     * LTE 布控球 视频上报
     *
     * @param rtspUrl
     */
    private void goWatchLteBall(String rtspUrl) {
        isGB28181Live = true;
        if(!TextUtils.isEmpty(rtspUrl)){
            setPushAuthority();
            //String gateWayUrl = TerminalFactory.getSDK().getParam(Params.GATE_WAY_URL);
            //gb28181Url = gateWayUrl + "DevAor=" + deviceId;
            //getView().getLogger().info("播放地址：" + gb28181Url);
            gb28181Url = rtspUrl;
            getView().getLogger().info("播放地址：" + rtspUrl);
            //拉取LTE 上报视频
            getView().startGB28121Pull();
            getView().getLogger().info(rtspUrl + "正在上报图像");
        }
    }

    /**
     * 对方拒绝直播，通知界面关闭响铃页
     **/
    private ReceiveResponseStartLiveHandler receiveReaponseStartLiveHandler = (resultCode, resultDesc) -> {
        ToastUtil.showToast(getContext(), resultDesc);
        stopRequestOtherLive();
    };

    /**
     * 超时未回复answer 通知界面关闭
     **/
    private ReceiveAnswerLiveTimeoutHandler receiveAnswerLiveTimeoutHandler = () -> {
        ToastUtil.showToast(getContext(), getContext().getString(R.string.other_no_answer));
        stopRequestOtherLive();
    };

//    /**
//     * 通知直播停止 通知界面关闭视频页
//     **/
//    private ReceiveNotifyLivingStoppedHandler receiveNotifyLivingStoppedHandler = (liveMemberId, callId, methodResult, resultDesc) -> mHandler.post(() -> {
//        ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.push_stoped));
//        stopBusiness();
//    });

    /**
     * 去观看时，发现没有在直播，关闭界面吧
     */
    private ReceiveMemberNotLivingHandler receiveMemberNotLivingHandler = callId -> {
        ToastUtil.showToast(getContext(), getContext().getString(R.string.push_stoped));
        stopRequestOtherLive();
    };

    /**
     * 主动方请求组呼的消息
     */
    private ReceiveRequestGroupCallConformationHandler receiveRequestGroupCallConformationHandler = (methodResult, resultDesc, groupId) -> {
//        if(methodResult == BaseCommonCode.SUCCESS_CODE){
//            mBtnLiveLookPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_speaking);
//            if (!MyTerminalFactory.getSDK().getAudioProxy().isSpeakerphoneOn()) {
//                MyTerminalFactory.getSDK().getAudioProxy().setSpeakerphoneOn(true);
//            }
//        }else if (methodResult == SignalServerErrorCode.CANT_SPEAK_IN_GROUP.getErrorCode()) {//只听组
//            ToastUtil.showToast(MyTerminalFactory.getSDK().application, "当前组是只听组，不能发起组呼");
//        } else if (methodResult == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode()) {//请求等待中
//            mBtnLiveLookPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_yellow);
//        } else {//请求失败
//            if (MyApplication.instance.getGroupListenenState() != LISTENING) {
//                mBtnLiveLookPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_dodgerblue2);
//            } else {
//                mBtnLiveLookPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_gray);
//            }
//        }
    };

    /**
     * 主动方停止组呼
     */

    private ReceiveCeaseGroupCallConformationHander receiveCeaseGroupCallConformationHander = new ReceiveCeaseGroupCallConformationHander() {
        @Override
        public void handler(int resultCode, String resultDesc) {
            MyTerminalFactory.getSDK().getAudioProxy().volumeCancelQuiet();
//            mHandler.post(() -> {
//                mBtnLiveLookPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_dodgerblue2);
//                mLlLiveGroupCall.setVisibility(View.GONE);
//            });
        }
    };

    /**
     * 通知终端停止观看直播
     **/
    private ReceiveNotifyMemberStopWatchMessageHandler receiveNotifyMemberStopWatchMessageHandler = message -> {
        ToastUtil.showToast(getContext(), getContext().getString(R.string.force_stop_watch));
        stopPull();
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

    private String rtspUrl;

    /**
     * 获取到rtsp地址，开始播放视频
     */
    private ReceiveGetRtspStreamUrlHandler receiveGetRtspStreamUrlHandler = (final String rtspUrl, final Member liveMember, long callId) -> {
        if (Util.isEmpty(rtspUrl)) {
            ToastUtil.showToast(getContext(), getContext().getString(R.string.no_rtsp_data));
            stopRequestOtherLive();
        } else {
            getView().getLogger().info("rtspUrl ----> " + rtspUrl);
            PromptManager.getInstance().stopRing();
            this.rtspUrl = rtspUrl;
            getView().startPull();
        }
    };

    private boolean rtspPlay = true;

    protected Handler mHandler = new Handler(Looper.getMainLooper());

    public void startPull(SurfaceTexture surface) {
        getView().isShowLiveView(true);
        getView().getLogger().info("开始播放");
        //通知拉取成功，正在观看 notice
        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceivePullLivingHandler.class);
        mHandler.postDelayed(()-> initEasyPlay(surface),1200);
    }


    private void initEasyPlay(SurfaceTexture surface){
        if (null == mResultReceiver) {
            mResultReceiver = new RtspReceiver(new Handler(), surface);
        }
        if (null != surface) {
            getView().getLogger().info("开始播放 null != surface");
            mStreamRender = new EasyRTSPClient(getContext(), MyTerminalFactory.getSDK().getLiveConfigManager().getPlayKey(), surface, mResultReceiver);
            try {
                if (!TextUtils.isEmpty(rtspUrl)) {
                    mStreamRender.start(rtspUrl, RTSPClient.TRANSTYPE_TCP, RTSPClient.EASY_SDK_VIDEO_FRAME_FLAG | RTSPClient.EASY_SDK_AUDIO_FRAME_FLAG, "", "", null);
                    getView().getLogger().info("开始播放 mStreamRender.start(rtspUrl)");
                }
            } catch (Exception e) {
                e.printStackTrace();
                getView().getLogger().error(e.toString());
            }
        }
    }

    /**
     * 拉取LTE 上报视频
     *
     * @param surface
     */
    public void startPullGB28121(SurfaceTexture surface) {
        if (!TextUtils.isEmpty(gb28181Url)) {
            getView().isShowLiveView(true);
//            initLteEasyPlay(surface);
            mHandler.postDelayed(()-> initLteEasyPlay(surface),1200);
        } else {
            ToastUtil.showToast(getContext(), getContext().getString(R.string.no_rtsp_data));
            stopBusiness();
        }
    }

    private void initLteEasyPlay(SurfaceTexture surface){
        if (null == mResultReceiver) {
            mResultReceiver = new RtspReceiver(new Handler(), surface);
        }
        mStreamRender = new EasyRTSPClient(getContext(), MyTerminalFactory.getSDK().getLiveConfigManager().getPlayKey(),
                surface, mResultReceiver);
        try {
            if (gb28181Url != null) {
                mStreamRender.start(gb28181Url, RTSPClient.TRANSTYPE_TCP, RTSPClient.EASY_SDK_VIDEO_FRAME_FLAG | RTSPClient.EASY_SDK_AUDIO_FRAME_FLAG, "", "", null);
            }
            //通知拉取成功，正在观看 notice
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceivePullLivingHandler.class);
        } catch (Exception e) {
            e.printStackTrace();
            getView().getLogger().error("LTE上报 :" + e.toString());
        }
    }

    private class RtspReceiver extends ResultReceiver {

        private int pullcount;
        private SurfaceTexture surface;

        private RtspReceiver(Handler handler, SurfaceTexture surface) {
            super(handler);
            this.surface = surface;
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == EasyRTSPClient.RESULT_VIDEO_DISPLAYED) {
                pullcount = 0;
            } else if (resultCode == EasyRTSPClient.RESULT_VIDEO_SIZE) {
                mLiveWidth = resultData.getInt(EasyRTSPClient.EXTRA_VIDEO_WIDTH);
                mLiveHeight = resultData.getInt(EasyRTSPClient.EXTRA_VIDEO_HEIGHT);
                onVideoSizeChange();
            } else if (resultCode == EasyRTSPClient.RESULT_TIMEOUT) {
                ToastUtil.showToast(getContext(), getContext().getString(R.string.time_up));
            } else if (resultCode == EasyRTSPClient.RESULT_UNSUPPORTED_AUDIO) {
                ToastUtil.showToast(getContext(), getContext().getString(R.string.voice_not_support));
            } else if (resultCode == EasyRTSPClient.RESULT_UNSUPPORTED_VIDEO) {
                ToastUtil.showToast(getContext(), getContext().getString(R.string.video_not_support));
            } else if (resultCode == EasyRTSPClient.RESULT_EVENT) {
                int errorcode = resultData.getInt("errorcode");
                String resultDataString = resultData.getString("event-msg");
                getView().getLogger().error("视频流播放状态：" + errorcode + "=========" + resultDataString + "-----count:" + pullcount);
                if (errorcode != 0) {
                    stopPull();
                }
                if (errorcode == 500 || errorcode == 404 || errorcode == -32 || errorcode == -101) {
                    if (pullcount < 10) {
                        try {
                            Thread.sleep(300);
                            getView().getLogger().error("请求第" + pullcount + "次");
                            if (surface != null) {
                                if (isGB28181Live) {
                                    getView().startGB28121Pull();
                                } else {
                                    getView().startPull();
                                }
                                pullcount++;
                            } else {
                                ToastUtil.showToast(getContext(), getContext().getString(R.string.push_stoped));
                                stopRequestOtherLive();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        ToastUtil.showToast(getContext(), getContext().getString(R.string.push_stoped));
                        stopRequestOtherLive();
                    }
                } else if (errorcode != 0) {
                    ToastUtil.showToast(getContext(), resultDataString);
                    stopRequestOtherLive();
                }
            }
        }
    }


    private void onVideoSizeChange() {
//        if(mLiveWidth == 0 || mLiveHeight == 0){
//            return;
//        }
//        getView().getTextureView().setTransform(new Matrix());
//        float ratioView = mRlLiveGeneralView.getWidth() * 1.0f / mRlLiveGeneralView.getHeight();
//        float ratio = mLiveWidth * 1.0f / mLiveHeight;
//        // 屏幕比视频的宽高比更小.表示视频是过于宽屏了.
//        if(ratioView - ratio < 0){
//            // 宽为基准.
//            getView().getTextureView().getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
//            getView().getTextureView().getLayoutParams().height = (int) (mRlLiveGeneralView.getWidth() / ratio + 0.5f);
//        }
//        // 视频是竖屏了.
//        else{
//            getView().getTextureView().getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
//            getView().getTextureView().getLayoutParams().width = (int) (mRlLiveGeneralView.getHeight() * ratio + 0.5f);
//        }
//        getView().getTextureView().requestLayout();
    }

    private void stopPull() {
        if (rtspPlay) {
            if (mStreamRender != null) {
                mStreamRender.stop();
                mStreamRender = null;
            }
        } else {
        }
    }

    /**
     * 停止请求别人直播
     */
    protected void stopRequestOtherLive() {
        PromptManager.getInstance().stopRing();
        SensorUtil.getInstance().unregistSensor();
        StateMachineUtils.revertStateMachine();
        getView().isShowLiveView(false);
    }

    /******************************************3别人邀请我上报视频****************************************************/
    //ReceiveGetVideoPushUrlHandler

    /******************************************4别人邀请我观看视频****************************************************/
    //ReceiveGetRtspStreamUrlHandler


}

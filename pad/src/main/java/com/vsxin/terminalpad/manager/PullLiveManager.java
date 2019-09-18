package com.vsxin.terminalpad.manager;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.app.PadApplication;
import com.vsxin.terminalpad.mvp.contract.constant.TerminalType;
import com.vsxin.terminalpad.receiveHandler.ReceiverRequestLteBullHandler;
import com.vsxin.terminalpad.receiveHandler.ReceiverRequestVideoHandler;
import com.vsxin.terminalpad.utils.MemberUtil;
import com.vsxin.terminalpad.utils.NumberUtil;
import com.vsxin.terminalpad.utils.OperateReceiveHandlerUtilSync;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.terminal.TerminalState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePlayingParameter;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePlayingState;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.audio.CheckMyPermission;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * 拉视频管理类
 */
public class PullLiveManager {
    private Context context;

    public PullLiveManager(Context context) {
        this.context = context;
    }

    /**
     * 拉视频
     *
     * @param memberNo         警号
     * @param type             终端类型
     * @param terminalUniqueNo
     */
    public void pullVideo(String memberNo, String type, String terminalUniqueNo) {
        if (!PadApplication.getPadApplication().isPttPress) {
            if (!CheckMyPermission.selfPermissionGranted(context, Manifest.permission.RECORD_AUDIO)) {//没有录音权限
                CheckMyPermission.permissionPrompt((Activity) context, Manifest.permission.RECORD_AUDIO);
                return;
            }

            if (!CheckMyPermission.selfPermissionGranted(context, Manifest.permission.CAMERA)) {//没有录音权限
                CheckMyPermission.permissionPrompt((Activity) context, Manifest.permission.CAMERA);
                return;
            }
            //判断终端权限
            if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_ASK.name())) {
                ToastUtil.showToast(context, context.getString(R.string.text_has_no_image_request_authority));
                return;
            }

            //加88
            String number = NumberUtil.checkMemberNo(memberNo + "");
            int no = NumberUtil.strToInt(number);
            if (no == 0) {
                ToastUtil.showToast(context, "警员编号异常");
                return;
            }

            if (TextUtils.equals(type, TerminalType.TERMINAL_PHONE)) {//ok  警务通拉视频掩饰20秒左右,才有视频过来 播放时做延时1~2秒
                //警务通
                pullVideoForMemberNo(no, TerminalMemberType.TERMINAL_PHONE);
            } else if (TextUtils.equals(type, TerminalType.TERMINAL_BODY_WORN_CAMERA)) {
                //执法记录仪
                pullVideoForMemberNo(no, TerminalMemberType.TERMINAL_BODY_WORN_CAMERA);
            } else if (TextUtils.equals(type, TerminalType.TERMINAL_UAV)) {
                //无人机
                pullVideoForMemberNo(no, TerminalMemberType.TERMINAL_UAV);
            } else if (TextUtils.equals(type, TerminalType.TERMINAL_LTE)) {
                //LTE
                playRTSPUrl(terminalUniqueNo, type, "LTE");
            } else if (TextUtils.equals(type, TerminalType.TERMINAL_BULL)) {
                //布控球
                playRTSPUrl(terminalUniqueNo, type, "布控球");
            } else if (TextUtils.equals(type, TerminalType.TERMINAL_CAMERA)) {
                //城市摄像头
                playRTSPUrl(terminalUniqueNo, type, "城市摄像头");
            } else {
                ToastUtil.showToast(context, "暂不支持拉取该设备视频");
            }
        }
    }

    /**
     * 播放RTSP url 直播流 包含：LTE、布控球、城市摄像头
     *
     * @param terminalNo 设备编号
     * @param type       类型
     * @param title      显示title
     */
    public void playRTSPUrl(String terminalNo, String type, String title) {
        String rtspUrl = getRtspUrl(terminalNo);
        pullVideoForRtspUrl(rtspUrl, type, title);
    }


    private String getRtspUrl(String terminalUniqueNo) {
        String gateWayUrl = TerminalFactory.getSDK().getParam(Params.GATE_WAY_URL);
        String rtspUrl = gateWayUrl + "DevAor=" + terminalUniqueNo;
        return rtspUrl;
    }


    /**
     * 拉视频
     * @param memberNo 警号
     * @param type 终端类型
     */
//    public void pullVideo(String memberNo,MemberTypeEnum type) {
//        if (!PadApplication.getPadApplication().isPttPress) {
//            if (!CheckMyPermission.selfPermissionGranted(context, Manifest.permission.RECORD_AUDIO)) {//没有录音权限
//                CheckMyPermission.permissionPrompt((Activity) context, Manifest.permission.RECORD_AUDIO);
//                return;
//            }
//
//            if (!CheckMyPermission.selfPermissionGranted(context, Manifest.permission.CAMERA)) {//没有录音权限
//                CheckMyPermission.permissionPrompt((Activity)context, Manifest.permission.CAMERA);
//                return;
//            }
//            //判断终端权限
//            if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_ASK.name())) {
//                ToastUtil.showToast(context, context.getString(R.string.text_has_no_image_request_authority));
//                return;
//            }
//
//            //加88
//            String number = NumberUtil.checkMemberNo(memberNo+"");
//            int no = NumberUtil.strToInt(number);
//            if(no==0){
//                ToastUtil.showToast(context, "警员编号异常");
//                return;
//            }
//
//            if (MemberTypeEnum.PHONE == type) {//ok  警务通拉视频掩饰20秒左右,才有视频过来
//                pullVideoForMemberNo(no, TerminalMemberType.TERMINAL_PHONE);
//            } else if (MemberTypeEnum.VIDEO == type) {//ok
//                //执法记录仪
//                pullVideoForMemberNo(no, TerminalMemberType.TERMINAL_BODY_WORN_CAMERA);
//            }else if (MemberTypeEnum.UAV == type) {//ok
//                //无人机
//                pullVideoForMemberNo(no, TerminalMemberType.TERMINAL_UAV);
//            }  else if (MemberTypeEnum.LTE == type) {
//                //LTE rtsp://59.32.1.174:554/DevAor=34020100001320000021
//                String rtsp = "rtsp://192.168.20.188:554/DevAor=34020100001320000021";
//                pullVideoForRtspUrl(rtsp);
////                pullVideoForMemberNo(10000369, TerminalMemberType.TERMINAL_LTE);
//            } else if (MemberTypeEnum.BALL == type) {
//                //不控球  rtsp://59.32.1.174:554/DevAor=32010000001320000114
//                String rtsp = "rtsp://192.168.20.188:554/DevAor=32010000001320000114";
//                pullVideoForRtspUrl(rtsp);
////                pullVideoForMemberNo(10000368, TerminalMemberType.TERMINAL_LTE);
//            } else {
//                ToastUtil.showToast(context, "暂不支持拉取该设备视频");
//            }


//            if (MemberTypeEnum.PHONE == type) {//ok  警务通拉视频掩饰20秒左右,才有视频过来
//                //警务通
////                pullVideoForMemberNo(88011075, TerminalMemberType.TERMINAL_PHONE);
//                pullVideoForMemberNo(88020446, TerminalMemberType.TERMINAL_PHONE);
//            } else if (MemberTypeEnum.VIDEO == type) {//ok
//                //执法记录仪
//                pullVideoForMemberNo(77000000, TerminalMemberType.TERMINAL_BODY_WORN_CAMERA);
//            }else if (MemberTypeEnum.UAV == type) {//ok
//                //无人机
//                pullVideoForMemberNo(88000369, TerminalMemberType.TERMINAL_UAV);
//            }  else if (MemberTypeEnum.LTE == type) {
//                //LTE rtsp://59.32.1.174:554/DevAor=34020100001320000021
//                String rtsp = "rtsp://192.168.20.188:554/DevAor=34020100001320000021";
//                pullVideoForRtspUrl(rtsp);
////                pullVideoForMemberNo(10000369, TerminalMemberType.TERMINAL_LTE);
//            } else if (MemberTypeEnum.BALL == type) {
//                //不控球  rtsp://59.32.1.174:554/DevAor=32010000001320000114
//                String rtsp = "rtsp://192.168.20.188:554/DevAor=32010000001320000114";
//                pullVideoForRtspUrl(rtsp);
////                pullVideoForMemberNo(10000368, TerminalMemberType.TERMINAL_LTE);
//            } else {
//                ToastUtil.showToast(context, "暂不支持拉取该设备视频");
//            }
//        }
//    }

    /**
     * 通过memberNo 拉取视频 主要用于 警务通，执法记录仪
     *
     * @param memberNo
     * @param type
     */
    private void pullVideoForMemberNo(int memberNo, TerminalMemberType type) {
        TerminalFactory.getSDK().getThreadPool().execute(() -> {
            Account account = DataUtil.getAccountByMemberNo(memberNo, true);
            Member member = MemberUtil.getMemberForTerminalMemberType(account, type);
            //拉取账号的警务通终端
            if (member == null) {
                ToastUtil.showToast(context, context.getString(R.string.text_has_no_image_request_authority));
                return;
            }
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverRequestVideoHandler.class, member);
        });
    }

    /**
     * 通过memberNo 拉取视频 主要用于 不控球
     *
     * @param rtspUrl
     * @param type
     * @param title
     */
    private void pullVideoForRtspUrl(String rtspUrl, String type, String title) {
        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverRequestLteBullHandler.class, rtspUrl, type, title);
    }

    /**
     * 开启播放 手动将视频状态机 改为 播放中
     * <p>
     * 观看lte,布控球,城市摄像头 手动控制状态机
     */
    public static void moveToStateIdleToPlaying() {
        //手动
        VideoLivePlayingState currentState = TerminalFactory.getSDK().getLiveManager().getVideoLivePlayingStateMachine().getCurrentState();
        if(currentState ==VideoLivePlayingState.IDLE){
            TerminalFactory.getSDK().getLiveManager().handMoveIdleToPlaying();
        }
    }

    /**
     * 关闭播放 手动视频状态机 改为 空闲
     * <p>
     * 观看lte,布控球,城市摄像头 手动控制状态机
     */
    private static void handMovePlayingToIdle() {
        TerminalFactory.getSDK().getLiveManager().handMovePlayingToIdle();
    }

}

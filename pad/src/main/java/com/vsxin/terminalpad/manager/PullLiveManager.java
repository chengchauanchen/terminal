package com.vsxin.terminalpad.manager;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.app.PadApplication;
import com.vsxin.terminalpad.mvp.contract.constant.MemberTypeEnum;
import com.vsxin.terminalpad.mvp.contract.constant.TerminalType;
import com.vsxin.terminalpad.receiveHandler.ReceiverRequestLteBullHandler;
import com.vsxin.terminalpad.receiveHandler.ReceiverRequestVideoHandler;
import com.vsxin.terminalpad.utils.MemberUtil;
import com.vsxin.terminalpad.utils.NumberUtil;
import com.vsxin.terminalpad.utils.OperateReceiveHandlerUtilSync;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
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
     * @param memberNo 警号
     * @param type 终端类型
     */
    public void pullVideo(String memberNo, String type) {
        if (!PadApplication.getPadApplication().isPttPress) {
            if (!CheckMyPermission.selfPermissionGranted(context, Manifest.permission.RECORD_AUDIO)) {//没有录音权限
                CheckMyPermission.permissionPrompt((Activity) context, Manifest.permission.RECORD_AUDIO);
                return;
            }

            if (!CheckMyPermission.selfPermissionGranted(context, Manifest.permission.CAMERA)) {//没有录音权限
                CheckMyPermission.permissionPrompt((Activity)context, Manifest.permission.CAMERA);
                return;
            }
            //判断终端权限
            if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_ASK.name())) {
                ToastUtil.showToast(context, context.getString(R.string.text_has_no_image_request_authority));
                return;
            }

            //加88
            String number = NumberUtil.checkMemberNo(memberNo+"");
            int no = NumberUtil.strToInt(number);
            if(no==0){
                ToastUtil.showToast(context, "警员编号异常");
                return;
            }

            if (TextUtils.equals(type,TerminalType.TERMINAL_PHONE)) {//ok  警务通拉视频掩饰20秒左右,才有视频过来
                pullVideoForMemberNo(no, TerminalMemberType.TERMINAL_PHONE);
            } else if (TextUtils.equals(type,TerminalType.TERMINAL_BODY_WORN_CAMERA)) {//ok
                //执法记录仪
                pullVideoForMemberNo(no, TerminalMemberType.TERMINAL_BODY_WORN_CAMERA);
            }else if (TextUtils.equals(type,TerminalType.TERMINAL_UAV)) {//ok
                //无人机
                pullVideoForMemberNo(no, TerminalMemberType.TERMINAL_UAV);
            }  else if (TextUtils.equals(type,TerminalType.TERMINAL_LTE)) {
                //LTE
//                String gb28181No = member.getGb28181No();
//                //String gateWayUrl = TerminalFactory.getSDK().getParam(Params.GATE_WAY_URL);
//                //String gb28181RtspUrl = gateWayUrl+"DevAor="+gb28181No;
//                TerminalMessage terminalMessage = new TerminalMessage();
//                terminalMessage.messageBody = new JSONObject();
//                terminalMessage.messageBody.put(JsonParam.GB28181_RTSP_URL,gb28181No);
//                terminalMessage.messageBody.put(JsonParam.DEVICE_NAME,member.getName());
//                terminalMessage.messageBody.put(JsonParam.ACCOUNT_ID,member.getNo());
//                terminalMessage.messageBody.put(JsonParam.DEVICE_DEPT_NAME,member.getDepartmentName());
//                terminalMessage.messageBody.put(JsonParam.DEVICE_DEPT_ID,member.getDeptId());
//                goWatchGB28121(terminalMessage);
                //LTE rtsp://59.32.1.174:554/DevAor=34020100001320000021
                String rtsp = "rtsp://192.168.20.188:554/DevAor=34020100001320000021";
                pullVideoForRtspUrl(rtsp);
//                pullVideoForMemberNo(10000369, TerminalMemberType.TERMINAL_LTE);
            } else if (TextUtils.equals(type,TerminalType.TERMINAL_BULL)) {
                //不控球  rtsp://59.32.1.174:554/DevAor=32010000001320000114
                String rtsp = "rtsp://192.168.20.188:554/DevAor=32010000001320000114";
                pullVideoForRtspUrl(rtsp);
//                pullVideoForMemberNo(10000368, TerminalMemberType.TERMINAL_LTE);
            } else {
                ToastUtil.showToast(context, "暂不支持拉取该设备视频");
            }
        }
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
     * @param rtspUrl
     */
    private void pullVideoForRtspUrl(String rtspUrl) {
        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverRequestLteBullHandler.class, rtspUrl);
    }
}

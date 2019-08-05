package com.vsxin.terminalpad.mvp.contract.presenter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;

import com.ixiaoma.xiaomabus.architecture.mvp.BasePresenter;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.app.PadApplication;
import com.vsxin.terminalpad.mvp.contract.view.IMemberInfoView;
import com.vsxin.terminalpad.mvp.ui.widget.ChooseDevicesDialog;
import com.vsxin.terminalpad.prompt.PromptManager;
import com.vsxin.terminalpad.receiveHandler.ReceiverActivePushVideoHandler;
import com.vsxin.terminalpad.receiveHandler.ReceiverRequestVideoHandler;
import com.vsxin.terminalpad.utils.MemberUtil;
import com.vsxin.terminalpad.utils.MyDataUtil;
import com.vsxin.terminalpad.utils.NumberUtil;
import com.vsxin.terminalpad.utils.SensorUtil;

import cn.vsx.SpecificSDK.OperateReceiveHandlerUtilSync;
import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.ReceiveObjectMode;
import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.SignalServerErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePlayingState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePushingState;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCurrentGroupIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyIndividualCallStoppedHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.audio.CheckMyPermission;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * Created by PC on 2018/11/1.
 * <p>
 * 地图气泡点击-成员详情页
 */

public class MemberInfoPresenter extends BasePresenter<IMemberInfoView> {

    public MemberInfoPresenter(Context mContext) {
        super(mContext);
    }


    /**
     * 打个呼
     *
     * @param memberNo
     */
    public void startIndividualCall(String memberNo, TerminalMemberType type) {
        TerminalFactory.getSDK().getThreadPool().execute(() -> {
            Account account = DataUtil.getAccountByMemberNo(NumberUtil.strToInt(memberNo), true);
            Member member = MemberUtil.getMemberForTerminalMemberType(account, type);
            activeIndividualCall(member);
        });
    }


    /**
     * 选择设备进行相应的操作
     *
     * @param memberNo
     * @param type
     */
    public void goToChooseDevices(String memberNo, int type) {
        TerminalFactory.getSDK().getThreadPool().execute(() -> {
            Account account = DataUtil.getAccountByMemberNo(NumberUtil.strToInt(memberNo), true);
            getView().showChooseDevicesDialog(account, type);
        });
    }

    /**
     * 显示选择框
     * 按之前逻辑，打电话是有选择框出现
     *
     * @param account
     * @param type
     */
    public void showChooseDevicesDialog(Account account, int type) {
        if (account == null) {
            ToastUtil.showToast(getContext(), getContext().getString(R.string.text_has_no_found_this_user));
            return;
        }
        new ChooseDevicesDialog(getContext(), type, account, (dialog, member) -> {
            switch (type) {
                case ChooseDevicesDialog.TYPE_CALL_PRIVATE:
                    activeIndividualCall(member);
                    break;
                case ChooseDevicesDialog.TYPE_CALL_PHONE:
                    //goToCall(member);
                    break;
                case ChooseDevicesDialog.TYPE_PULL_LIVE:
                    //goToPullLive(member);
                    break;
                case ChooseDevicesDialog.TYPE_PUSH_LIVE:
                    //goToPushLive(member);
                    break;
                default:
                    break;
            }
            dialog.dismiss();
        }).showDialog();
    }

    private void activeIndividualCall(Member member) {
        //MyApplication.instance.isCallState = true;
        boolean network = MyTerminalFactory.getSDK().hasNetwork();
        if (network) {
            if (member != null) {
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCurrentGroupIndividualCallHandler.class, member);
            } else {
                ToastUtil.showToast(getContext(), getContext().getString(R.string.text_get_member_info_fail));
            }
        } else {
            ToastUtil.showToast(getContext(), getContext().getString(R.string.text_network_connection_abnormal_please_check_the_network));
        }
    }

    /**
     * 注册监听
     */
    public void registReceiveHandler() {
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiveCurrentGroupIndividualCallHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyIndividualCallStoppedHandler);
    }

    /**
     * 取消监听
     */
    public void unregistReceiveHandler() {
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiveCurrentGroupIndividualCallHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyIndividualCallStoppedHandler);
    }

    /**
     * 主动发起个呼
     */
    private ReceiveCurrentGroupIndividualCallHandler receiveCurrentGroupIndividualCallHandler = (member) -> {
        getView().getLogger().info("当前呼叫对象:" + member);

        if (PadApplication.getPadApplication().getVideoLivePlayingState() != VideoLivePlayingState.IDLE) {
            ToastUtil.showToast(getContext(), getContext().getString(R.string.text_watching_can_not_private_call));
            return;
        }
        if (PadApplication.getPadApplication().getVideoLivePushingState() != VideoLivePushingState.IDLE) {
            ToastUtil.showToast(getContext(), getContext().getString(R.string.text_pushing_can_not_private_call));
            return;
        }
        if (PadApplication.getPadApplication().getIndividualState() != IndividualCallState.IDLE) {
            ToastUtil.showToast(getContext(), getContext().getString(R.string.text_personal_calling_can_not_do_others));
            return;
        }

        SensorUtil.getInstance().registSensor();
        PromptManager.getInstance().IndividualCallRequestRing();
        int resultCode = MyTerminalFactory.getSDK().getIndividualCallManager().requestIndividualCall(member.getId(), member.getUniqueNo(), "");
        if (resultCode == BaseCommonCode.SUCCESS_CODE) {
            ToastUtil.individualCallFailToast(getContext(), resultCode);
        } else {
            ToastUtil.individualCallFailToast(getContext(), resultCode);
        }
    };

    /**
     * 被动方通知个呼停止，界面---------静默状态
     */
    private ReceiveNotifyIndividualCallStoppedHandler receiveNotifyIndividualCallStoppedHandler = (methodResult, resultDesc) -> {
        if (SignalServerErrorCode.getInstanceByCode(methodResult) != null) {
            ToastUtil.showToast(MyTerminalFactory.getSDK().application, resultDesc);
        } else {
            ToastUtil.showToast(MyTerminalFactory.getSDK().application, getContext().getString(R.string.other_stop_call));
        }
        individualCallStopped();
    };


    private void individualCallStopped() {
        //发送通知关闭StartIndividualCallService和ReceiveCallComingService
        PromptManager.getInstance().IndividualHangUpRing();
        PromptManager.getInstance().delayedStopRing();
    }

    /*******************************************自己主动上报视频，并邀请其他人观看***********************************************/

    /**
     * 自己主动上报视频，并邀请 其他人来观看
     */
    public void pushVideo() {
        if (!PadApplication.getPadApplication().isPttPress) {//是否按下组呼键(是否在组呼)
            if (!CheckMyPermission.selfPermissionGranted(getContext(), Manifest.permission.RECORD_AUDIO)) {//没有录音权限
                CheckMyPermission.permissionPrompt((Activity) getContext(), Manifest.permission.RECORD_AUDIO);
                return;
            }
            if (!CheckMyPermission.selfPermissionGranted(getContext(), Manifest.permission.CAMERA)) {//没有相机权限
                CheckMyPermission.permissionPrompt((Activity) getContext(), Manifest.permission.CAMERA);
                return;
            }

            if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_UP.name())) {
                ToastUtil.showToast(getContext(), getContext().getString(R.string.text_has_no_image_report_authority));
                return;
            }
            //Todo 模拟发个 mate7的uniqueNo
            long uniqueNo = 156015606669449299L;
            String pushInviteMember = MyDataUtil.getPushInviteMemberData(uniqueNo, ReceiveObjectMode.MEMBER.toString());
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverActivePushVideoHandler.class, pushInviteMember, false);
        }
    }

    /****************************************自己主动请求他人上报视频，自己观看**************************************************/

    /**
     * 自己主动请求他人上报视频，给我看
     *
     */
    public void pullVideo(TerminalMemberType type) {
        if (!PadApplication.getPadApplication().isPttPress) {
            if (!CheckMyPermission.selfPermissionGranted(getContext(), Manifest.permission.RECORD_AUDIO)) {//没有录音权限
                CheckMyPermission.permissionPrompt((Activity) getContext(), Manifest.permission.RECORD_AUDIO);
                return;
            }
            //判断终端权限
            if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_ASK.name())) {
                ToastUtil.showToast(getContext(), getContext().getString(R.string.text_has_no_image_request_authority));
                return;
            }

            if (TerminalMemberType.TERMINAL_PHONE == type) {
                //警务通
                pullVideoForMemberNo(10000367,TerminalMemberType.TERMINAL_PHONE);
            } else if (TerminalMemberType.TERMINAL_BODY_WORN_CAMERA == type) {
                //执法记录仪
                pullVideoForMemberNo(77000002,TerminalMemberType.TERMINAL_BODY_WORN_CAMERA);
            }else if(TerminalMemberType.TERMINAL_LTE == type){
                //LTE
                //pullVideoForMemberNo(77000002,TerminalMemberType.TERMINAL_LTE);
            }else if(TerminalMemberType.TERMINAL_UAV == type){
                //无人机
            }else{
                ToastUtil.showToast(getContext(), "暂不支持拉取该设备视频");
                return;
            }
        }
    }

    /**
     * 通过memberNo 拉取视频 主要用于 警务通，执法记录仪
     * @param memberNo
     * @param type
     */
    private void pullVideoForMemberNo(int memberNo,TerminalMemberType type){
        TerminalFactory.getSDK().getThreadPool().execute(() -> {
            Account account = DataUtil.getAccountByMemberNo(memberNo, true);
            Member member = MemberUtil.getMemberForTerminalMemberType(account, type);
            //拉取账号的警务通终端
            if (member == null) {
                ToastUtil.showToast(getContext(), getContext().getString(R.string.text_has_no_image_request_authority));
                return;
            }
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverRequestVideoHandler.class, member);
        });
    }

    /*******************************************别人请求我上报视频************************************************/

}

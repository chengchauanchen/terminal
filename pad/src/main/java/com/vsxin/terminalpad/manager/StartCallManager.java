package com.vsxin.terminalpad.manager;

import android.content.Context;
import android.support.v4.app.FragmentActivity;

import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.app.PadApplication;
import com.vsxin.terminalpad.mvp.ui.fragment.IndividualCallFragment;
import com.vsxin.terminalpad.prompt.PromptManager;
import com.vsxin.terminalpad.utils.MemberUtil;
import com.vsxin.terminalpad.utils.NumberUtil;
import com.vsxin.terminalpad.utils.SensorUtil;

import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePlayingState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePushingState;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * @author qzw
 *
 * 发起个呼 方法封装
 */
public class StartCallManager {
    private Context context;

    public StartCallManager(Context context) {
        this.context = context;
    }

    /**
     * 打个呼
     *
     * @param memberNo
     */
    public void startIndividualCall(String memberNo, TerminalMemberType type) {
        if (PadApplication.getPadApplication().getVideoLivePlayingState() != VideoLivePlayingState.IDLE) {
            ToastUtil.showToast(context,context.getString(R.string.text_watching_can_not_private_call));
            return;
        }
        if (PadApplication.getPadApplication().getVideoLivePushingState() != VideoLivePushingState.IDLE) {
            ToastUtil.showToast(context, context.getString(R.string.text_pushing_can_not_private_call));
            return;
        }
        if (PadApplication.getPadApplication().getIndividualState() != IndividualCallState.IDLE) {
            ToastUtil.showToast(context, context.getString(R.string.text_personal_calling_can_not_do_others));
            return;
        }

        //根据警号找 Account
        TerminalFactory.getSDK().getThreadPool().execute(() -> {
            Account account = DataUtil.getAccountByMemberNo(NumberUtil.strToInt(memberNo), true);
            //根据终端类型 找Member
            Member member = MemberUtil.getMemberForTerminalMemberType(account, type);
            activeIndividualCall(member);
        });
    }

    public void activeIndividualCall(Member member) {
        //MyApplication.instance.isCallState = true;
        boolean network = MyTerminalFactory.getSDK().hasNetwork();
        if (network) {
            if (member != null) {
                //OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCurrentGroupIndividualCallHandler.class, member);
                startIndividualCall(member);
            } else {
                ToastUtil.showToast(context, context.getString(R.string.text_get_member_info_fail));
            }
        } else {
            ToastUtil.showToast(context, context.getString(R.string.text_network_connection_abnormal_please_check_the_network));
        }
    }

    /**
     * 发起个呼请求
     * @param member
     */
    private void startIndividualCall(Member member){
        SensorUtil.getInstance().registSensor();
        PromptManager.getInstance().IndividualCallRequestRing();
        int resultCode = MyTerminalFactory.getSDK().getIndividualCallManager().requestIndividualCall(member.getId(), member.getUniqueNo(), "");
        if (resultCode == BaseCommonCode.SUCCESS_CODE) {
            //getView().setMemberinfo();//设置头像名称
            IndividualCallFragment.startIndividualCallFragment((FragmentActivity)context,member.getName(),member.getId()+"");
        } else {
            ToastUtil.individualCallFailToast(context, resultCode);
            stopIndividualCall();
        }
    }

    /**
     * 发起个呼
     * @param memberName 警员名称
     * @param memberId 警员编号
     * @param uniqueNo
     */
    public void startIndividualCall(String memberName,String memberId,long uniqueNo){
        if (PadApplication.getPadApplication().getVideoLivePlayingState() != VideoLivePlayingState.IDLE) {
            ToastUtil.showToast(context,context.getString(R.string.text_watching_can_not_private_call));
            return;
        }
        if (PadApplication.getPadApplication().getVideoLivePushingState() != VideoLivePushingState.IDLE) {
            ToastUtil.showToast(context, context.getString(R.string.text_pushing_can_not_private_call));
            return;
        }
        if (PadApplication.getPadApplication().getIndividualState() != IndividualCallState.IDLE) {
            ToastUtil.showToast(context, context.getString(R.string.text_personal_calling_can_not_do_others));
            return;
        }

        //加88
        String number = NumberUtil.checkMemberNo(memberId);
        int no = NumberUtil.strToInt(number);
        if(no==0){
            ToastUtil.showToast(context, "警员编号异常");
            return;
        }

        SensorUtil.getInstance().registSensor();
        PromptManager.getInstance().IndividualCallRequestRing();
        int resultCode = MyTerminalFactory.getSDK().getIndividualCallManager().requestIndividualCall(no, uniqueNo, "");
        if (resultCode == BaseCommonCode.SUCCESS_CODE) {
            //getView().setMemberinfo();//设置头像名称
            IndividualCallFragment.startIndividualCallFragment((FragmentActivity)context,memberName,memberId+"");
        } else {
            ToastUtil.individualCallFailToast(context, resultCode);
            stopIndividualCall();
        }
    }


    /**
     * 挂断个呼
     */
    public void stopIndividualCall() {
        stopPromptSound();
        revertStateMachine();
    }

    /**
     * 关闭提示音
     */
    public void stopPromptSound() {
        PromptManager.getInstance().IndividualHangUpRing();
        PromptManager.getInstance().delayedStopRing();
    }


    /**
     * 重置状态机
     */
    private void revertStateMachine() {
        if (PadApplication.getPadApplication().getIndividualState() != IndividualCallState.IDLE) {
            MyTerminalFactory.getSDK().getIndividualCallManager().ceaseIndividualCall();
        }
    }

}

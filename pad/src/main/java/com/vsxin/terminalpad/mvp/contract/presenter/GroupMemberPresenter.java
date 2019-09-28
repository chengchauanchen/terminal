package com.vsxin.terminalpad.mvp.contract.presenter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.blankj.utilcode.util.ToastUtils;
import com.ixiaoma.xiaomabus.architecture.mvp.refresh.RefreshPresenter;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.app.PadApplication;
import com.vsxin.terminalpad.mvp.contract.view.IGroupMemberView;
import com.vsxin.terminalpad.mvp.contract.view.IGroupMessageView;
import com.vsxin.terminalpad.utils.OperateReceiveHandlerUtilSync;

import java.util.Collections;
import java.util.List;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.CallMode;
import cn.vsx.hamster.common.GroupType;
import cn.vsx.hamster.common.MemberChangeType;
import cn.vsx.hamster.common.TerminalMemberStatusEnum;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.SignalServerErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCeaseGroupCallConformationHander;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveChangeGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetGroupCurrentOnlineMemberListHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetGroupLivingListHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallCeasedIndicationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyMemberChangeHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRequestGroupCallConformationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseAddMemberToTempGroupMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseChangeTempGroupProcessingStateHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseRemoveMemberToTempGroupMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveTempGroupMembersHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * @author qzw
 * <p>
 * 组内上线成员
 */
public class GroupMemberPresenter extends RefreshPresenter<TerminalMessage, IGroupMemberView> {

    public GroupMemberPresenter(Context mContext) {
        super(mContext);
    }

    protected Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * 获取组内在线成员列表
     *
     * @param groupId
     */
    public void getGroupMembers(int groupId) {
        try {
            Group group = DataUtil.getTempGroupByGroupNo(groupId);
            if (null == group) {
                group = DataUtil.getGroupByGroupNo(groupId);
            }
            if (group != null) {
                boolean isTemporaryGroup = GroupType.TEMPORARY.toString().equals(group.getGroupType());
                if (isTemporaryGroup) {
                    TerminalFactory.getSDK().getThreadPool().execute(() -> TerminalFactory.getSDK().getDataManager().getMemberByTempNo(groupId));
                } else {
                    MyTerminalFactory.getSDK().getGroupManager().getGroupCurrentOnlineMemberListNewMethod(groupId, TerminalMemberStatusEnum.ONLINE.toString());
                }
            } else {
                ToastUtil.showToast(getContext(), "未找到当前组");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取会话组当前成员列表
     **/
    private ReceiveGetGroupCurrentOnlineMemberListHandler mReceiveGetGroupCurrentOnlineMemberListHandler = (members, status, groupId) -> mHandler.post(() -> {
        if (members == null) {
            return;
        }
        getView().setMemberNum(String.format(getContext().getString(R.string.text_intra_group_line_members_number), members.size()));
        getView().refreshOrLoadMore(members);
    });


    /**
     * 获取临时组的成员列表
     */
    private ReceiveTempGroupMembersHandler receiveTempGroupMembersHandler = (groupId, members, total, onlineNumber, offlineNumber) -> mHandler.post(() -> {
        if (members == null) {
            return;
        }
        getView().setMemberNum(String.format(getContext().getString(R.string.text_intra_group_line_members_number), members.size()));
        getView().refreshOrLoadMore(members);
    });

    /**
     * 移除成员
     */
    private ReceiveResponseRemoveMemberToTempGroupMessageHandler mReceiveResponseRemoveMemberToTempGroupMessageHandler = (methodResult, resultDesc, tempGroupNo) -> {
        if (methodResult == BaseCommonCode.SUCCESS_CODE) {
            getView().getGroupMembers();
        }
    };

    /**
     * 添加成员
     */
    private ReceiveResponseAddMemberToTempGroupMessageHandler mReceiveResponseAddMemberToTempGroupMessageHandler = (methodResult, resultDesc, tempGroupNo) -> {
        if (methodResult == BaseCommonCode.SUCCESS_CODE) {
            getView().getGroupMembers();
        }
    };

    /**
     * 绑定
     *
     * @param view
     */
    @Override
    public void attachView(IGroupMemberView view) {
        super.attachView(view);
        registerReceiveHandler();
    }

    /**
     * 解绑
     */
    @Override
    public void detachView() {
        super.detachView();
        unRgisterReceiveHandler();
    }

    /**
     * 注册监听
     */
    public void registerReceiveHandler() {
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveGetGroupCurrentOnlineMemberListHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveTempGroupMembersHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveResponseRemoveMemberToTempGroupMessageHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveResponseAddMemberToTempGroupMessageHandler);
    }

    /**
     * 取消监听
     */
    public void unRgisterReceiveHandler() {
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveGetGroupCurrentOnlineMemberListHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveTempGroupMembersHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveResponseRemoveMemberToTempGroupMessageHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveResponseAddMemberToTempGroupMessageHandler);
    }
}

package com.vsxin.terminalpad.mvp.contract.view;

import com.ixiaoma.xiaomabus.architecture.mvp.refresh.IRefreshView;

import java.util.List;

import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;

/**
 * 组内成员
 */
public interface IGroupMemberView extends IRefreshView<Member> {

    /**
     * 设置在线人数
     * @param num
     */
    void setMemberNum(String num);

    /**
     * 获取组内在线成员列表
     */
    void getGroupMembers();
}

package com.vsxin.terminalpad.receiveHandler;

import com.vsxin.terminalpad.mvp.entity.ContactItemBean;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/1/2
 * 描述：
 * 修订历史：
 */
public interface ReceiveRemoveSelectedMemberHandler extends ReceiveHandler {
    void handle(ContactItemBean contactItemBean);
}

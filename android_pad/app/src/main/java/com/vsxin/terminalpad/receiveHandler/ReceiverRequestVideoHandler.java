package com.vsxin.terminalpad.receiveHandler;

import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * 请求他人上报
 */
public interface ReceiverRequestVideoHandler extends ReceiveHandler {
    void handler(Member member);
}

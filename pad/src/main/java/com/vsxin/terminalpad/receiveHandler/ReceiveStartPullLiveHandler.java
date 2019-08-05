package com.vsxin.terminalpad.receiveHandler;

import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * 开始拉取视频 正在呼叫
 */
public interface ReceiveStartPullLiveHandler extends ReceiveHandler {
    void handler(Member member);
}

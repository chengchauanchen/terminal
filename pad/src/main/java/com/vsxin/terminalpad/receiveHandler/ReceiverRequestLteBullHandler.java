package com.vsxin.terminalpad.receiveHandler;

import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * 拉取lte 不控球视频
 */
public interface ReceiverRequestLteBullHandler extends ReceiveHandler {
    void handler(String rtspUrl);
}

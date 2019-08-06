package com.vsxin.terminalpad.receiveHandler;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * 自己停止拉取视频
 */
public interface ReceiveStopPullLiveHandler extends ReceiveHandler {
    void handler();
}

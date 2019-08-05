package com.vsxin.terminalpad.receiveHandler;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

public interface ReceiveUpdateMainFrgamentPTTButtonHandler extends ReceiveHandler {
    void handler(boolean show);
}

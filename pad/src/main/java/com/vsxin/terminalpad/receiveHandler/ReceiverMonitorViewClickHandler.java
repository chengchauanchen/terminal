package com.vsxin.terminalpad.receiveHandler;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * Created by gt358 on 2017/10/28.
 */

public interface ReceiverMonitorViewClickHandler extends ReceiveHandler {
    void handler(int groupId);
}

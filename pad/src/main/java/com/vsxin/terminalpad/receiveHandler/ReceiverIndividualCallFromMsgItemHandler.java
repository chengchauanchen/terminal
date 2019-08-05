package com.vsxin.terminalpad.receiveHandler;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 *  点击消息条目进行个呼
 * Created by gt358 on 2017/9/13.
 */

public interface ReceiverIndividualCallFromMsgItemHandler extends ReceiveHandler{
    void handler();
}

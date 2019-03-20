package cn.vsx.vc.receiveHandle;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * ptt按键发送给pttservice，处理事件
 */

public interface ReceiverPTTButtonEventHandler extends ReceiveHandler {
    public void handler(String action);
}

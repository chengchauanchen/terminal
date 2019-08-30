package cn.vsx.vc.receiveHandle;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * 停止一切业务
 */

public interface ReceiverStopBusniessHandler extends ReceiveHandler {
    public void handler();
}

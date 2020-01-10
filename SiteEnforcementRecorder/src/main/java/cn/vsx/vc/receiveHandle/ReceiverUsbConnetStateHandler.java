package cn.vsx.vc.receiveHandle;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * Usb连接
 */

public interface ReceiverUsbConnetStateHandler extends ReceiveHandler {
    public void handler(boolean isConnected);
}

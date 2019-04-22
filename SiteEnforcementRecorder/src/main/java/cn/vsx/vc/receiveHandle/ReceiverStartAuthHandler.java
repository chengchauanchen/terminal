package cn.vsx.vc.receiveHandle;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * 停止所有业务
 */

public interface ReceiverStartAuthHandler extends ReceiveHandler {
    public void handler(boolean showMessage);
}

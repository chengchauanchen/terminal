package cn.vsx.vc.receiveHandle;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * 清除账号信息
 */

public interface ReceiverClearAccountHandler extends ReceiveHandler {
    public void handler(boolean showMessage);
}

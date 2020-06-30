package cn.vsx.vc.receiveHandle;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * nfc
 */

public interface ReceiverNfcStatusHandler extends ReceiveHandler {
    public void handler(boolean open);
}

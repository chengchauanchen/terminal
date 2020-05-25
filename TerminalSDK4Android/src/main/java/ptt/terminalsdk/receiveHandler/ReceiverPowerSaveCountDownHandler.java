package ptt.terminalsdk.receiveHandler;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * auth
 */

public interface ReceiverPowerSaveCountDownHandler extends ReceiveHandler {
    void handler(String action);
}

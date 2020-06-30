package ptt.terminalsdk.receiveHandler;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * auth
 */

public interface ReceiverAppFrontAndBackStatusHandler extends ReceiveHandler {
    void handler(boolean isFront);
}

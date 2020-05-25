package ptt.terminalsdk.receiveHandler;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * auth
 */

public interface ReceiverBusinessInServiceStatusHandler extends ReceiveHandler {
    void handler(String name,boolean create);
}

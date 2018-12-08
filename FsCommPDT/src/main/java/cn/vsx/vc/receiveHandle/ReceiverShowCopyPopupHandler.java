package cn.vsx.vc.receiveHandle;

import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * Created by jamie on 2017/11/24.
 */

public interface ReceiverShowCopyPopupHandler extends ReceiveHandler {
    public void handler(TerminalMessage terminalMessage);
}

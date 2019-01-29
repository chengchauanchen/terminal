package cn.vsx.vc.receiveHandle;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * Created by gt358 on 2017/10/23.
 */

public interface ReceiverDialogDimissHandler extends ReceiveHandler {

    void handler(String dialogName);
}

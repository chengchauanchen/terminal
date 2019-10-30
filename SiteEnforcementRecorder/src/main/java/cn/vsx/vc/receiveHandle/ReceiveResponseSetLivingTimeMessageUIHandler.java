package cn.vsx.vc.receiveHandle;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * fragment popBackStack
 */

public interface ReceiveResponseSetLivingTimeMessageUIHandler extends ReceiveHandler {
    public void handler(long livingTime ,boolean isResetLiving);
}

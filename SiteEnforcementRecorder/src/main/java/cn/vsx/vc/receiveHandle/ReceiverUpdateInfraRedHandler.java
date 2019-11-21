package cn.vsx.vc.receiveHandle;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * 更新红外的状态
 */

public interface ReceiverUpdateInfraRedHandler extends ReceiveHandler {
    public void handler(boolean open);
}

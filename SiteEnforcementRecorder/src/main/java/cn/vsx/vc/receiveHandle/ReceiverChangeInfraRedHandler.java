package cn.vsx.vc.receiveHandle;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * 设置红外的状态
 */

public interface ReceiverChangeInfraRedHandler extends ReceiveHandler {
    public void handler(int state);
}

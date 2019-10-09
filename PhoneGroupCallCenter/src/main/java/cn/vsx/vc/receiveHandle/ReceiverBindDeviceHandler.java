package cn.vsx.vc.receiveHandle;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 *绑定装备列表
 *
 */
public interface ReceiverBindDeviceHandler extends ReceiveHandler {
    void handler(String deviceJson);
}

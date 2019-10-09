package cn.vsx.vc.receiveHandle;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 *解绑装备--成功回调--绑定装备列表
 */
public interface ReceiverUnBindDeviceHandler extends ReceiveHandler {
    void handler(int id,int position);
}

package cn.vsx.vc.receiveHandle;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * 蓝牙是否打开
 */
public interface ReceiveBluetoothListenerHandler extends ReceiveHandler {
    void handler(int state);
}

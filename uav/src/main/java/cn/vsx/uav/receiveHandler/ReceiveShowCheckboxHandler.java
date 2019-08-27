package cn.vsx.uav.receiveHandler;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 *是否显示checkbox
 */
public interface ReceiveShowCheckboxHandler extends ReceiveHandler{
    void handler(boolean show);
}

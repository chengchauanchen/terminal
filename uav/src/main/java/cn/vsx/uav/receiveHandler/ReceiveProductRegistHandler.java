package cn.vsx.uav.receiveHandler;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 *大疆无人机SDK注册结果
 */
public interface ReceiveProductRegistHandler extends ReceiveHandler{
    void handler(boolean success,String description);
}

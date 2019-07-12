package cn.vsx.vc.receiveHandle;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * 切换环境
 */

public interface ReceiverChangeServerHandler extends ReceiveHandler {
    public void handler(String ip,String port,boolean showMessage);
}

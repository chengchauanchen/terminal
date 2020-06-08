package cn.vsx.vc.receiveHandle;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * 更新播放录像时是否是静音模式
 */

public interface ReceiverUpdatePlayVideoStateHandler extends ReceiveHandler {
    public void handler();
}

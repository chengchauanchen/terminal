package cn.vsx.vc.receiveHandle;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * 视频按键发送给MainActivity，上报视频
 */

public interface ReceiverVideoButtonEventHandler extends ReceiveHandler {
    public void handler(boolean isLongPress);
}

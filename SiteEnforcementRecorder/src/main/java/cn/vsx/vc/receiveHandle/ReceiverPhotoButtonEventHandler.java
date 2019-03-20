package cn.vsx.vc.receiveHandle;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * 拍照按键发送给MainActivity，上报视频
 */

public interface ReceiverPhotoButtonEventHandler extends ReceiveHandler {
    public void handler();
}

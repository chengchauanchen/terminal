package cn.vsx.vc.receiveHandle;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * 录音按键发送给MainActivity，录音
 */

public interface ReceiverAudioButtonEventHandler extends ReceiveHandler {
    public void handler(boolean isLongPress);
}

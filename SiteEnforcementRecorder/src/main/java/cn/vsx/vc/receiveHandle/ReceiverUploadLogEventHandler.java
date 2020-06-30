package cn.vsx.vc.receiveHandle;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * 上传日志
 */

public interface ReceiverUploadLogEventHandler extends ReceiveHandler {
    public void handler();
}

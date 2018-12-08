package com.zectec.imageandfileselector.receivehandler;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * Created by gt358 on 2017/10/11.
 */

public interface ReceiverToFaceRecognitionHandler extends ReceiveHandler {
    public void handler(String url, String name);
}

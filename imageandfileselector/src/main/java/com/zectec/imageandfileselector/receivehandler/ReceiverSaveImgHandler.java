package com.zectec.imageandfileselector.receivehandler;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * 图片保存成功或者失败
 * Created by jamie on 2017/12/22.
 */

public interface ReceiverSaveImgHandler extends ReceiveHandler {
    public void handler(boolean isSave,String filePath);
}

package com.zectec.imageandfileselector.receivehandler;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * Created by gt358 on 2017/8/16.
 */

public interface ReceiverSendFileCheckMessageHandler extends ReceiveHandler {
    public static final int PHOTO_ALBUM = 1;//相册
    public static final int CAMERA = 2;//相机
    public static final int FILE = 3;//文件
    public static final int POST_BACK_VIDEO = 4;//上报图像
    public static final int REQUEST_VIDEO = 5;//请求图像
    public static final int RECORD=6;//语音输入

    /**
     * @param msgType
     * 1----相册
     * 2----拍摄
     * 3----文件
     * 4----上报图像
     * 5----请求图像
     * 6----语音输入
     */
    public void handler(int msgType, boolean showOrHidden, int userId);
}

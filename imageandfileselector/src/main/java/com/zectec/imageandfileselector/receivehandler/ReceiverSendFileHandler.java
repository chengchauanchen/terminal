package com.zectec.imageandfileselector.receivehandler;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * 会话界面选择文件进行发送
 * Created by gt358 on 2017/9/6.
 */

public interface ReceiverSendFileHandler extends ReceiveHandler {

    public static final int TEXT = 201709061;//短文本
    public static final int PHOTO_ALBUM = 201709063;//图片
    public static final int VOICE = 201709064;//录音
    public static final int VEDIO = 201709065;//小视频
    public static final int FILE = 201709066;//文件
    public static final int LOCATION = 201709067;//位置
    public static final int AFFICHE = 201709068;//公告
    public static final int WARNING_INSTANCE = 201709069;//警情
    public static final int PRIVATE_CALL = 2017090610;//个呼
    public static final int VIDEO_LIVE = 2017090611;//图像记录
    public static final int GROUP_CALL = 2017090612;//组呼
    public void handler(int type);
}

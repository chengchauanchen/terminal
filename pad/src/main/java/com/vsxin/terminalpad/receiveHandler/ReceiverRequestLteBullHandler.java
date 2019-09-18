package com.vsxin.terminalpad.receiveHandler;

import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * 拉取lte 不控球视频
 */
public interface ReceiverRequestLteBullHandler extends ReceiveHandler {
    /**
     * 播放RTSP url 直播流 包含：LTE、布控球、城市摄像头
     * @param rtspUrl 流地址
     * @param type 类型
     * @param title 显示title
     */
    void handler(String rtspUrl,String type,String title);
}

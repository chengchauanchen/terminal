package cn.vsx.uav.receiveHandler;

import java.util.List;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;
import dji.sdk.media.MediaFile;

/**
 *获取到无人机文件
 */
public interface ReceiveAircraftFilesHandler extends ReceiveHandler{
    void handler(List<MediaFile> medias);
}

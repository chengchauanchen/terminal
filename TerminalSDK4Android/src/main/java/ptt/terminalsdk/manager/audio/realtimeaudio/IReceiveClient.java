package ptt.terminalsdk.manager.audio.realtimeaudio;

import java.io.IOException;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/7/19
 * 描述：
 * 修订历史：
 */
public interface IReceiveClient{

    void initClient(Command command);

    void sendHeatBeat() throws IOException;

    void receiveAudioData();

    void release();
}

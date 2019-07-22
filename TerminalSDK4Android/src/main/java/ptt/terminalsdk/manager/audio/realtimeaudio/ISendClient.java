package ptt.terminalsdk.manager.audio.realtimeaudio;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/7/19
 * 描述：
 * 修订历史：
 */
public interface ISendClient{

    void initClient(Command command);

    void sendAudioData();

    void release();
}

package ptt.terminalsdk.manager.audio.realtimeaudio;

import java.net.InetAddress;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/7/19
 * 描述：
 * 修订历史：
 */
public interface IClient{

    void init(byte[] data, int length);

    void setAddress(InetAddress ip, int port);

    void setLength(int length);

    void send(byte[] data);

    void release();
}

package cn.vsx.vc.receiveHandle;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 *  点击消息的组呼条目，播放组呼录音
 * Created by gt358 on 2017/9/13.
 */

public interface ReceiverReplayGroupChatVoiceHandler extends ReceiveHandler {

    public void handler (int postion);
}

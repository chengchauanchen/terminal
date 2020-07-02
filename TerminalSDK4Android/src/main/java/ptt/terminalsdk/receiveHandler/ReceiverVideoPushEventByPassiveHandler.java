package ptt.terminalsdk.receiveHandler;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * 被动接受命令，执行上报视频
 */

public interface ReceiverVideoPushEventByPassiveHandler extends ReceiveHandler {
    public void handler(int state);
}

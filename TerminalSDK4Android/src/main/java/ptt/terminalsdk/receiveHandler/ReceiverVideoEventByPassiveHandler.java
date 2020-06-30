package ptt.terminalsdk.receiveHandler;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * 被动接受命令，执行录像
 */

public interface ReceiverVideoEventByPassiveHandler extends ReceiveHandler {
    public void handler(int state,String type);
}

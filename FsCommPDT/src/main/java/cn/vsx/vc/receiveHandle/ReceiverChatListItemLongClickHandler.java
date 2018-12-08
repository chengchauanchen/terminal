package cn.vsx.vc.receiveHandle;

import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 *  长按消息列表的条目
 * Created by gt358 on 2017/9/14.
 */

public interface ReceiverChatListItemLongClickHandler extends ReceiveHandler {
    public void handler(TerminalMessage terminalMessage);
}

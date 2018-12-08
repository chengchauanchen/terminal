package cn.vsx.vc.receiveHandle;

import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * 会话界面列表条目点击事件
 * Created by gt358 on 2017/9/14.
 */

public interface ReceiverChatListItemClickHandler extends ReceiveHandler {
    /**
     * @param terminalMessage 消息
     * @param isReceiver  是否是接受
     */
    public void handler(TerminalMessage terminalMessage, boolean isReceiver);
}

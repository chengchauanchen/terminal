package cn.vsx.vc.receiveHandle;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;
import cn.vsx.vc.model.ChatMember;

/**
 *  转发handler
 * Created by gt358 on 2017/9/15.
 */

public interface ReceiverTransponHandler extends ReceiveHandler {

    public void handler (ChatMember chatMember);
}

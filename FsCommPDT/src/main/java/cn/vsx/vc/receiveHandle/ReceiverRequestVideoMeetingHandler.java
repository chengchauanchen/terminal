package cn.vsx.vc.receiveHandle;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * Created by gt358 on 2017/9/27.
 */

public interface ReceiverRequestVideoMeetingHandler extends ReceiveHandler {
    void handler(int videoMeetingType);
}

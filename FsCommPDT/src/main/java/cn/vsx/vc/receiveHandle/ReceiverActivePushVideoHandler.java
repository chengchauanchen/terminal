package cn.vsx.vc.receiveHandle;

import java.util.List;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * Created by gt358 on 2017/9/27.
 */

public interface ReceiverActivePushVideoHandler extends ReceiveHandler {
    public void handler(List<Integer> memberIds);
}

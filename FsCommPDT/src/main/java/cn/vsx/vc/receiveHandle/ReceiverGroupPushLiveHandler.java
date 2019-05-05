package cn.vsx.vc.receiveHandle;

import java.util.List;

import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * Created by gt358 on 2017/9/27.
 */

public interface ReceiverGroupPushLiveHandler extends ReceiveHandler {
    void handler(String streamMediaServerIp, int streamMediaServerPort, long callId, List<Group> list);
}

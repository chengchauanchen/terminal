package cn.vsx.vc.receiveHandle;

import java.util.List;

import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * Created by gt358 on 2017/10/21.
 */

public interface ReceiverShowGroupFragmentHandler extends ReceiveHandler {
    public void handler(List<Group> groupList,boolean isScanGroupSearch);
}

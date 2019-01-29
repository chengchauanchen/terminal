package cn.vsx.vc.receiveHandle;

import java.util.List;

import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 *
 * Created by gt358 on 2017/10/21.
 */

public interface ReceiverShowPersonFragmentHandler extends ReceiveHandler {
    void handler(List<Member> memberList);
}

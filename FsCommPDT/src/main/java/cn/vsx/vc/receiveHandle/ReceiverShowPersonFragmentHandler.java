package cn.vsx.vc.receiveHandle;

import java.util.ArrayList;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 *
 * Created by gt358 on 2017/10/21.
 */

public interface ReceiverShowPersonFragmentHandler extends ReceiveHandler {
    void handler(int type, ArrayList<String> terminalMemberTypes);
}

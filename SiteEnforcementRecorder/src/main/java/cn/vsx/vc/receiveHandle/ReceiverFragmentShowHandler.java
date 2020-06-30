package cn.vsx.vc.receiveHandle;

import android.os.Bundle;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * fragment popBackStack
 */

public interface ReceiverFragmentShowHandler extends ReceiveHandler {
    public void handler(String tag, Bundle bundle);
}

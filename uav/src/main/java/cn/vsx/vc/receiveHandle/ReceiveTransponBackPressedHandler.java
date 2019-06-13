package cn.vsx.vc.receiveHandle;

import android.app.Activity;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

public interface ReceiveTransponBackPressedHandler extends ReceiveHandler {
    void handler(Activity activity);
}

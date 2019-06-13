package cn.vsx.vc.receiveHandle;

import android.view.KeyEvent;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * 在service中监听实体按键的点击事件回调
 */

public interface ReceiverEntityKeyEventInServiceHandler extends ReceiveHandler {

    public void handler(KeyEvent event);
}

package cn.vsx.vc.receiveHandle;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 *  软键盘高度
 * Created by gt358 on 2017/9/29.
 */

public interface ReceiverKeyboardHeightHandler extends ReceiveHandler {

    public void handler(int keyboardHeight);
}

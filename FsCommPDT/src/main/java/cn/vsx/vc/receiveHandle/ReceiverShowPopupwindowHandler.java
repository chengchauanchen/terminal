package cn.vsx.vc.receiveHandle;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * 显示成员搜索popupwindow的handler
 * Created by gt358 on 2017/8/15.
 */

public interface ReceiverShowPopupwindowHandler extends ReceiveHandler {
    public void handler(String className);
}

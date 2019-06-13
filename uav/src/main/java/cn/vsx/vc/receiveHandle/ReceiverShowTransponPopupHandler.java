package cn.vsx.vc.receiveHandle;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 *  显示转发popupwindow
 * Created by gt358 on 2017/9/15.
 * type:1 转发
 * type:2 合并转发
 *
 */

public interface ReceiverShowTransponPopupHandler  extends ReceiveHandler {
    void handler(int transponType);
}

package cn.vsx.vc.receiveHandle;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2018/10/31
 * 描述：
 * 修订历史：
 */

public interface ReceiverRemoveWindowViewHandler extends ReceiveHandler{
    void handle(String className);
}

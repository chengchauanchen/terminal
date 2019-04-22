package cn.vsx.vc.receiveHandle;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;
import cn.vsx.vc.model.ContactItemBean;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/1/2
 * 描述：
 * 修订历史：
 */
public interface ReceiveRemoveSelectedMemberHandler extends ReceiveHandler{
    void handle(ContactItemBean contactItemBean);
}

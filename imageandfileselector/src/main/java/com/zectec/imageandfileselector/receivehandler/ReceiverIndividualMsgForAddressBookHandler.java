package com.zectec.imageandfileselector.receivehandler;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * 通讯录界面开启会话界面
 * Created by gt358 on 2017/8/22.
 */

public interface ReceiverIndividualMsgForAddressBookHandler extends ReceiveHandler {
    /**
     *
     * @param where  那个地方发出“1---通讯录个人界面，2------添加联系人界面，3-------当前组在线成员，4----------通讯录个人界面搜索界面”
     * @param position
     */
    public void handler(int where, int position);
}

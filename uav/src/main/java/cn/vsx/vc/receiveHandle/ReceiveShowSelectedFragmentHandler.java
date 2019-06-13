package cn.vsx.vc.receiveHandle;

import java.util.ArrayList;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;
import cn.vsx.vc.model.ContactItemBean;

public interface ReceiveShowSelectedFragmentHandler extends ReceiveHandler{

    void handler(ArrayList<ContactItemBean> selectedContacts);
}

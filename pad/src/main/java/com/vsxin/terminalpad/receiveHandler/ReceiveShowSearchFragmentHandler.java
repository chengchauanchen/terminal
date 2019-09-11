package com.vsxin.terminalpad.receiveHandler;

import java.util.ArrayList;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

public interface ReceiveShowSearchFragmentHandler extends ReceiveHandler {
    //第二个参数是之前被选中的人或组的编号
    void handler(int type, ArrayList<Integer> selectedNos);
}

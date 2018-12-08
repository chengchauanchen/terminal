package com.zectec.imageandfileselector.receivehandler;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 *  fragment显示或者隐藏的handler
 * Created by gt358 on 2017/8/23.
 */

public interface ReceiverShowOrHideFragmentHandler extends ReceiveHandler {
    public void handler(String FragmentName, boolean show);
}

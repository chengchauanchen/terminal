package cn.vsx.vc.receiveHandle;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

public interface ReceiveGoToHelpCombatHandler extends ReceiveHandler {
    void handler(boolean gotoHelpCombat);
}

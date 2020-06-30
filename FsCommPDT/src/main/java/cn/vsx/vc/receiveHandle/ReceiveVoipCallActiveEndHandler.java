package cn.vsx.vc.receiveHandle;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * @author martian on 2020/3/26.
 */
public interface ReceiveVoipCallActiveEndHandler extends ReceiveHandler {
  void handle();
}

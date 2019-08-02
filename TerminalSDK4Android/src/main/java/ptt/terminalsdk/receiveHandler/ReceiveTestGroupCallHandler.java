package ptt.terminalsdk.receiveHandler;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

public interface ReceiveTestGroupCallHandler extends ReceiveHandler{
	public void handler(boolean isStart);
}

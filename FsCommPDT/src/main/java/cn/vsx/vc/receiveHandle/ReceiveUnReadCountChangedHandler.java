package cn.vsx.vc.receiveHandle;


import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

public interface ReceiveUnReadCountChangedHandler extends ReceiveHandler {
	void handler(int unReadCount);
}

package cn.vsx.vc.receiveHandle;


import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * 点击警情通知弹窗中进入警情详情
 */
public interface ReceiveWarningReadCountChangedHandler extends ReceiveHandler{
	public void handler();
}

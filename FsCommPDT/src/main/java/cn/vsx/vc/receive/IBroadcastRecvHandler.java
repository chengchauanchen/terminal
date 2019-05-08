package cn.vsx.vc.receive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 
 * ======================================================<br/>
 * 功能概述:广播处理器<br/>
 * ======================================================<br/>
 */
public class IBroadcastRecvHandler extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (iRecvCallBack != null) {
			iRecvCallBack.onReceive(context, intent);
		}
	}

	/** 构造函数,初始化回调函数 */
	public IBroadcastRecvHandler(RecvCallBack iRecvCallBack) {
		this.iRecvCallBack = iRecvCallBack;
	}

	RecvCallBack iRecvCallBack;
}

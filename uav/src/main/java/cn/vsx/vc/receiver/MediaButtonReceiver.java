package cn.vsx.vc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import cn.vsx.vc.utils.HeadSetUtil;
import cn.vsx.vc.utils.HeadSetUtil.OnHeadSetListener;

public class MediaButtonReceiver extends BroadcastReceiver {

	private OnHeadSetListener headSetListener;

	public MediaButtonReceiver() {
		this.headSetListener = HeadSetUtil.getInstance().getOnHeadSetListener();
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String intentAction = intent.getAction();
		if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
			KeyEvent keyEvent =  intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
			if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
				if (headSetListener != null) {
				headSetListener.onClickDown();
				}
			}
		}
		abortBroadcast();
	}

}

package cn.vsx.vc.utils;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;

public abstract class DialogUtil {

	public void showDialog() {
		Builder builder = new Builder(getContext());
		builder.setTitle(getMessage());
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				doConfirmThings();
			}

		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				doCancelThings();
			}
		});
		builder.show();
	}

	public abstract CharSequence getMessage();

	public abstract Context getContext();

	public abstract void doConfirmThings();
	
	public abstract void doCancelThings();
	
}

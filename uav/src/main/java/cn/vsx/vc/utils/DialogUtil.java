package cn.vsx.vc.utils;

import android.app.AlertDialog.Builder;
import android.content.Context;

public abstract class DialogUtil {

	public void showDialog() {
		Builder builder = new Builder(getContext());
		builder.setTitle(getMessage());
		builder.setPositiveButton("确定", (dialog, which) -> {
			dialog.dismiss();
			doConfirmThings();
		});
		builder.setNegativeButton("取消", (dialog, which) -> {
			dialog.dismiss();
			doCancelThings();
		});
		builder.show();
	}

	public abstract CharSequence getMessage();

	public abstract Context getContext();

	public abstract void doConfirmThings();
	
	public abstract void doCancelThings();
	
}

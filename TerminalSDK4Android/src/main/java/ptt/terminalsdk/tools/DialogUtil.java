package ptt.terminalsdk.tools;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;

public abstract class DialogUtil {

	public AlertDialog showDialog() {
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
		builder.setCancelable(false);
		return builder.show();
	}

	public AlertDialog showDialog(boolean cancelable) {
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
		builder.setCancelable(cancelable);
		return builder.show();
	}

	public AlertDialog showDialog(String positive,String negative,boolean cancelable) {
		Builder builder = new Builder(getContext());
		builder.setTitle(getMessage());
		builder.setPositiveButton(positive, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				doConfirmThings();
			}

		});
		builder.setNegativeButton(negative, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				doCancelThings();
			}
		});
		builder.setCancelable(cancelable);
		return builder.show();
	}

	public abstract CharSequence getMessage();

	public abstract Context getContext();

	public abstract void doConfirmThings();
	
	public abstract void doCancelThings();
	
}

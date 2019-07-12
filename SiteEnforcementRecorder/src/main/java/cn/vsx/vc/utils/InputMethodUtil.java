package cn.vsx.vc.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.provider.Settings;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.List;

public class InputMethodUtil {

	//打开软键盘
	public static void showInputMethod(Context context) {
		InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
	}
	
	//关闭软键盘
	public static void hideInputMethod(Context context, EditText  editText) {
		InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}

	/**
	 * 软键盘状态判断
	 * @param context
	 */
	@SuppressWarnings("unchecked")
	public static boolean inputMethodSate(Context context) {
		//得到默认输入法包名
		String defaultInputName = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
		defaultInputName = defaultInputName.substring(0, defaultInputName.indexOf("/"));

		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
		boolean isInputing = false;
		for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
			if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
				if(appProcess.processName.equals(defaultInputName)) {
					isInputing = true;
					break;
				}
			}
		}
		return isInputing;
	}
}

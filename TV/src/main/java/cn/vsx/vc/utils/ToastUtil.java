package cn.vsx.vc.utils;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import cn.vsx.hamster.errcode.module.TerminalErrorCode;

public class ToastUtil {
	
	private static Toast toast;
	
	/*
	 * 为了让主线程和子线程通用
	 */
	public static void showToast(final String content ,final Activity activity ) {

		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(activity, content, Toast.LENGTH_SHORT).show();
			}
		});

	}
	
	/*
	 * 静态吐司显示的速度更快
	 * 静态toast  toast 消失 变为null 不消失就不用重新创建
	 */
    public static void showToast(Context context, String text) {
        if (toast == null)
            toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        toast.setText(text);
        toast.show();
    }
    
    /*
     * 让主线程和子线程通用
	 * 静态吐司显示的速度更快
	 */
    public static void toast(final Activity activity, final String string) {
    	
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				 if (toast == null){
			            toast = Toast.makeText(activity, string, Toast.LENGTH_SHORT);
				 }
		        toast.setText(string);
		        toast.show();
			}
		});
	}

	public static void closeToast(){
		if(toast != null ){
			try {
				//  从Toast对象中获得mTN变量
				Field field = toast.getClass().getDeclaredField("mTN");
				field.setAccessible(true);
				Object obj = field.get(toast);
				//  TN对象中获得了show方法
				Method method =  obj.getClass().getDeclaredMethod("hide",  new Class[ 0 ]);
				//  调用show方法来显示Toast信息提示框
				method.invoke(obj, new Object[]{});
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 不能进行直播的各种提示
	 * @param context toast所需的上下文对象
	 * @param resultCode 请求直播时返回的结果码
	 */
	public static void livingFailToast(Context context, int resultCode, int pushOrPull) {
		if (pushOrPull == TerminalErrorCode.LIVING_PUSHING.getErrorCode()) {
			if (resultCode == TerminalErrorCode.GROUP_CALLING.getErrorCode()){
				ToastUtil.showToast(context, TerminalErrorCode.GROUP_CALLING.getErrorDiscribe()+"，不能上报图像");
			}else if (resultCode == TerminalErrorCode.INDIVIDUAL_CALLING.getErrorCode()){
				ToastUtil.showToast(context, TerminalErrorCode.INDIVIDUAL_CALLING.getErrorDiscribe()+"，不能上报图像");
			}else if (resultCode == TerminalErrorCode.LIVING_PLAYING.getErrorCode()){
				ToastUtil.showToast(context, TerminalErrorCode.LIVING_PLAYING.getErrorDiscribe()+"，不能再次上报图像");
			}else if (resultCode == TerminalErrorCode.LIVING_PUSHING.getErrorCode()){
				ToastUtil.showToast(context, TerminalErrorCode.LIVING_PUSHING.getErrorDiscribe()+"，不能再次上报图像");
			}else{
				ToastUtil.showToast(context, "上报图像失败！");
			}
		} else if (pushOrPull == TerminalErrorCode.LIVING_PLAYING.getErrorCode()){
			if (resultCode == TerminalErrorCode.GROUP_CALLING.getErrorCode()){
				ToastUtil.showToast(context, TerminalErrorCode.GROUP_CALLING.getErrorDiscribe()+"，不能观看图像");
			}else if (resultCode == TerminalErrorCode.INDIVIDUAL_CALLING.getErrorCode()){
				ToastUtil.showToast(context, TerminalErrorCode.INDIVIDUAL_CALLING.getErrorDiscribe()+"，不能观看图像");
			}else if (resultCode == TerminalErrorCode.LIVING_PLAYING.getErrorCode()){
				ToastUtil.showToast(context, TerminalErrorCode.LIVING_PLAYING.getErrorDiscribe()+"，不能再次观看图像");
			}else if (resultCode == TerminalErrorCode.LIVING_PUSHING.getErrorCode()){
				ToastUtil.showToast(context, TerminalErrorCode.LIVING_PUSHING.getErrorDiscribe()+"，不能再次观看图像");
			}else{
				ToastUtil.showToast(context, "观看图像失败！");
			}
		} else if (pushOrPull == TerminalErrorCode.LIVING_REQUEST.getErrorCode()){
			if (resultCode == TerminalErrorCode.GROUP_CALLING.getErrorCode()){
				ToastUtil.showToast(context, TerminalErrorCode.GROUP_CALLING.getErrorDiscribe()+"，不能请求图像");
			}else if (resultCode == TerminalErrorCode.INDIVIDUAL_CALLING.getErrorCode()){
				ToastUtil.showToast(context, TerminalErrorCode.INDIVIDUAL_CALLING.getErrorDiscribe()+"，不能请求图像");
			}else if (resultCode == TerminalErrorCode.LIVING_PLAYING.getErrorCode()){
				ToastUtil.showToast(context, TerminalErrorCode.LIVING_PLAYING.getErrorDiscribe()+"，不能再次请求图像");
			}else if (resultCode == TerminalErrorCode.LIVING_PUSHING.getErrorCode()){
				ToastUtil.showToast(context, TerminalErrorCode.LIVING_PUSHING.getErrorDiscribe()+"，不能再次请求图像");
			}else{
				ToastUtil.showToast(context, "请求图像失败！");
			}
		}
	}
}

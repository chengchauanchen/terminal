package ptt.terminalsdk.tools;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.common.UrlParams;
import cn.vsx.hamster.errcode.module.TerminalErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;

public class ToastUtil {
	
	private static Toast toast;
	private static Handler handler = new Handler(Looper.getMainLooper());
	
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
	public static void showToast(final String message) {
		handler.post(new Runnable(){
			@Override
			public void run(){
				if (toast == null)
					toast = Toast.makeText(Utils.getApp(), message, Toast.LENGTH_LONG);
				String deviceType = TerminalFactory.getSDK().getParam(UrlParams.TERMINALMEMBERTYPE);
				if(!TextUtils.isEmpty(deviceType)&&TerminalMemberType.valueOf(deviceType).getCode() == TerminalMemberType.TERMINAL_PAD.getCode()){
					LinearLayout layout = (LinearLayout) toast.getView();
					TextView tv = (TextView) layout.getChildAt(0);
					tv.setTextSize(25);
					toast.setGravity(Gravity.BOTTOM, 0, 0);
				}
				toast.setText(message);
				toast.show();
			}
		});
	}

	/*
	 * 静态吐司显示的速度更快
	 * 静态toast  toast 消失 变为null 不消失就不用重新创建
	 */
	public static void showToast(final int resId) {
		CharSequence text = Utils.getApp().getResources().getText(resId);

		handler.post(new Runnable(){
			@Override
			public void run(){
				if (toast == null)
					toast = Toast.makeText(Utils.getApp(), text, Toast.LENGTH_LONG);

				String deviceType = TerminalFactory.getSDK().getParam(UrlParams.TERMINALMEMBERTYPE);
				if(!TextUtils.isEmpty(deviceType)&&TerminalMemberType.valueOf(deviceType).getCode() == TerminalMemberType.TERMINAL_PAD.getCode()){
					LinearLayout layout = (LinearLayout) toast.getView();
					TextView tv = (TextView) layout.getChildAt(0);
					tv.setTextSize(25);
					toast.setGravity(Gravity.BOTTOM, 0, -50);
				}
				toast.setText(text);
				toast.show();
			}
		});
	}

	/*
	 * 静态吐司显示的速度更快
	 * 静态toast  toast 消失 变为null 不消失就不用重新创建
	 */
    public static void showToast(final Context context, final String text) {
		handler.post(new Runnable(){
			@Override
			public void run(){
				if (toast == null)
					toast = Toast.makeText(context, text, Toast.LENGTH_LONG);

				String deviceType = TerminalFactory.getSDK().getParam(UrlParams.TERMINALMEMBERTYPE);
				if(!TextUtils.isEmpty(deviceType)&&TerminalMemberType.valueOf(deviceType).getCode() == TerminalMemberType.TERMINAL_PAD.getCode()){
					LinearLayout layout = (LinearLayout) toast.getView();
					TextView tv = (TextView) layout.getChildAt(0);
					tv.setTextSize(25);
					toast.setGravity(Gravity.BOTTOM, 0, -50);
				}
				toast.setText(text);
				toast.show();
			}
		});
    }

	/*
	 * 静态吐司显示的速度更快
	 * 静态toast  toast 消失 变为null 不消失就不用重新创建
	 */
	public static void showToastText(final Context context, final String text) {
		handler.post(new Runnable(){
			@Override
			public void run(){
				if (toast == null)
					toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);

				LinearLayout layout = (LinearLayout) toast.getView();
				TextView tv = (TextView) layout.getChildAt(0);
				tv.setTextSize(25);
				toast.setGravity(Gravity.CENTER, 0, -50);
				toast.setText(text);
				showSystemToast(toast);
			}
		});
	}

	/*
	 * 静态吐司显示的速度更快
	 * 静态toast  toast 消失 变为null 不消失就不用重新创建
	 */
	public static void showSystemToast(final Context context, final String text) {
		handler.post(new Runnable(){
			@Override
			public void run(){
				if (toast == null)
					toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
				toast.setText(text);
				showSystemToast(toast);
			}
		});
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
				e.printStackTrace();
			}
		}
	}

	/**
	 * 不能进行组呼的各种提示
	 * @param context toast所需的上下文对象
	 * @param resultCode 请求组呼时返回的结果码
	 */
	public static void groupCallFailToast(Context context, int resultCode) {
		if (resultCode == TerminalErrorCode.INDIVIDUAL_CALLING.getErrorCode()){
			ToastUtil.showToast(context, TerminalErrorCode.INDIVIDUAL_CALLING.getErrorDiscribe()+"，不能组呼");
		}else if (resultCode == TerminalErrorCode.LIVING_PLAYING.getErrorCode()){
			ToastUtil.showToast(context, TerminalErrorCode.LIVING_PLAYING.getErrorDiscribe()+"，不能组呼");
		}else if (resultCode == TerminalErrorCode.LIVING_PUSHING.getErrorCode()){
			ToastUtil.showToast(context, TerminalErrorCode.LIVING_PUSHING.getErrorDiscribe()+"，不能组呼");
		}else if (resultCode == TerminalErrorCode.GROUP_CHANGING.getErrorCode()){
			ToastUtil.showToast(context, TerminalErrorCode.GROUP_CHANGING.getErrorDiscribe()+"，不能组呼");
		}else if (resultCode == TerminalErrorCode.GROUP_CALLING.getErrorCode()){
			ToastUtil.showToast(context, TerminalErrorCode.GROUP_CALLING.getErrorDiscribe()+"，不能再次组呼");
		}else{
			ToastUtil.showToast(context, "组呼失败！");
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

	/**
	 * 不能进行个呼的各种提示
	 * @param context toast所需的上下文对象
	 * @param resultCode 请求个呼时返回的结果码
	 */
	public static void individualCallFailToast(Context context, int resultCode) {
		if (resultCode == TerminalErrorCode.GROUP_CALLING.getErrorCode()){
			ToastUtil.showToast(context, TerminalErrorCode.GROUP_CALLING.getErrorDiscribe()+"，不能个呼");
		}else if (resultCode == TerminalErrorCode.LIVING_PLAYING.getErrorCode()){
			ToastUtil.showToast(context, TerminalErrorCode.LIVING_PLAYING.getErrorDiscribe()+"，不能个呼");
		}else if (resultCode == TerminalErrorCode.LIVING_PUSHING.getErrorCode()){
			ToastUtil.showToast(context, TerminalErrorCode.LIVING_PUSHING.getErrorDiscribe()+"，不能个呼");
		}else if (resultCode == TerminalErrorCode.INDIVIDUAL_CALLING.getErrorCode()){
			ToastUtil.showToast(context, TerminalErrorCode.INDIVIDUAL_CALLING.getErrorDiscribe()+"，不能再次个呼");
		}else{
			ToastUtil.showToast(context, "个呼失败！");
		}
	}

	public static void groupChangedFailToast(Context context, int resultCode) {
		if (resultCode == TerminalErrorCode.GROUP_CALLING.getErrorCode()){
			ToastUtil.showToast(context, TerminalErrorCode.GROUP_CALLING.getErrorDiscribe()+"，不能转组");
		}else if (resultCode == TerminalErrorCode.GROUP_CHANGING.getErrorCode()){
			ToastUtil.showToast(context, TerminalErrorCode.GROUP_CHANGING.getErrorDiscribe()+"，不能再次转组");
		}else{
			ToastUtil.showToast(context, "转组失败！");
		}
	}

	/**
	 * 显示系统Toast
	 */
	private static void showSystemToast(Toast toast){
		try{
			Method getServiceMethod = Toast.class.getDeclaredMethod("getService");
			getServiceMethod.setAccessible(true);

			final Object iNotificationManager = getServiceMethod.invoke(null);
			Class iNotificationManagerCls = Class.forName("android.app.INotificationManager");
			Object iNotificationManagerProxy = Proxy.newProxyInstance(toast.getClass().getClassLoader(), new Class[]{iNotificationManagerCls}, new InvocationHandler() {
				@Override
				public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
					// 强制使用系统Toast
					// 华为p20 pro上为enqueueToastEx
					if("enqueueToast".equals(method.getName())
							|| "enqueueToastEx".equals(method.getName())){
						args[0] = "android";
					}
					return method.invoke(iNotificationManager, args);
				}
			});
			Field sServiceFiled = Toast.class.getDeclaredField("sService");
			sServiceFiled.setAccessible(true);
			sServiceFiled.set(null, iNotificationManagerProxy);
			toast.show();

		}catch (Exception e){
			e.printStackTrace();
		}
	}
}

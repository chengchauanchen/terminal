package cn.vsx.vc.receiver;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import org.apache.log4j.Logger;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.utils.HeadSetUtil;
import cn.vsx.vc.utils.HeadSetUtil.OnHeadSetListener;
import cn.vsx.vc.utils.SensorUtil;
import ptt.terminalsdk.receiveHandler.ReceiveHeadSetPlugHandler;

public class HeadsetPlugReceiver extends BroadcastReceiver {
	private Logger logger = Logger.getLogger(getClass());
	private WakeLock wakeLockScreen;
	private Dialog dialog;

	@SuppressLint("InvalidWakeLockTag")
	public HeadsetPlugReceiver(Context context) {
		PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		wakeLockScreen = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
				| PowerManager.SCREEN_DIM_WAKE_LOCK, "wakeLock");
	}

	@Override
	public void onReceive(final Context context, Intent intent) {
		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

		// intent.getIntExtra("state" , 0); //0代表拔出，1代表插入
		// intent.getStringExtra("name"); //字符串，代表headset的类型 h2w
		// intent.getIntExtra("microphone", 1); //1代表这个headset有麦克风，0则没有
		// 测试结果是不管插入耳机还是米键都返回1

		if (intent.hasExtra("state")) {
			
			if (dialog == null) {
				dialog = new AlertDialog.Builder(context).create();
			}
			//个呼中插上耳机不要距离监听，拔出耳机需要
			//缩小也不需要

			if (intent.getIntExtra("state", 0) == 0) {//耳机拔出
				MyApplication.instance.headset = false;
				SensorUtil.getInstance().registSensor();
				// 注销耳机线控监听
				HeadSetUtil.getInstance().close(context);
				if (null != wakeLockScreen && wakeLockScreen.isHeld()) {
					wakeLockScreen.release();
					wakeLockScreen = null;
				}
				if (dialog!=null && dialog.isShowing()) {
					dialog.dismiss();
				}
				
				logger.info("耳机拔出，声音类型："+ audioManager.getMode()+"	扬声器状态："+ audioManager.isSpeakerphoneOn());
				TerminalFactory.getSDK().notifyReceiveHandler(ReceiveHeadSetPlugHandler.class,MyApplication.instance.headset);
			} else if (intent.getIntExtra("state", 0) == 1) {//耳机插入
				MyApplication.instance.headset = true;
				logger.info("耳机插入，声音类型："+ audioManager.getMode()+"	扬声器状态："+ audioManager.isSpeakerphoneOn());

				SensorUtil.getInstance().unregistSensor();
				// 注册耳机线控按钮监听
				HeadSetUtil.getInstance().setOnHeadSetListener(headSetListener);
				HeadSetUtil.getInstance().open(context);
				TerminalFactory.getSDK().notifyReceiveHandler(ReceiveHeadSetPlugHandler.class,MyApplication.instance.headset);
			}
		}
	}

	private OnHeadSetListener headSetListener = new OnHeadSetListener() {

		@Override
		public void onClick() {
		}

		@Override
		public void onDoubleClick() {
		}

		@Override
		public void onThreeClick() {
		}

		@Override
		public void onClickUp() {
		}

		@Override
		public void onClickDown() {
			// logger.info("先把屏幕点亮，进入程序的activity界面");
			// 获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU，保持运行
//			ToastUtil.showToast(context, "屏幕点亮");
			wakeLockScreen.acquire();
		}
	};
}

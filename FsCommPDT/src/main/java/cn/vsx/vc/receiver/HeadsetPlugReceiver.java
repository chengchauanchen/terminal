package cn.vsx.vc.receiver;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.PowerManager.WakeLock;

import org.apache.log4j.Logger;

import cn.vsx.vc.utils.HeadSetUtil;
import cn.vsx.vc.utils.HeadSetUtil.OnHeadSetListener;

public class HeadsetPlugReceiver extends BroadcastReceiver {
	private Logger logger = Logger.getLogger(getClass());
	private Context context;
	private SensorManager sensorManager;
	private SensorEventListener sensorEventListener;
	private WakeLock wakeLockScreen;
	private Dialog dialog;
	private AudioManager audioManager;

	public HeadsetPlugReceiver(){}
	public HeadsetPlugReceiver(SensorManager sensorManager,
                               SensorEventListener sensorEventListener, WakeLock wakeLockScreen) {
		this.sensorManager = sensorManager;
		this.sensorEventListener = sensorEventListener;
		this.wakeLockScreen = wakeLockScreen;
	}

	@Override
	public void onReceive(final Context context, Intent intent) {
		this.context = context;
		audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		// intent.getIntExtra("state" , 0); //0代表拔出，1代表插入
		// intent.getStringExtra("name"); //字符串，代表headset的类型 h2w
		// intent.getIntExtra("microphone", 1); //1代表这个headset有麦克风，0则没有
		// 测试结果是不管插入耳机还是米键都返回1

		if (intent.hasExtra("state")) {
			
			if (dialog == null) {
				dialog = new AlertDialog.Builder(context).create();
			}
			if (intent.getIntExtra("state", 0) == 0) {//耳机拔出
				//注销距离监听
				sensorManager.unregisterListener(sensorEventListener);
				// 注销耳机线控监听
				HeadSetUtil.getInstance().close(context);
				if (null != wakeLockScreen && wakeLockScreen.isHeld()) {
					wakeLockScreen.release();
					wakeLockScreen = null;
				}
				if (dialog!=null && dialog.isShowing()) {
					dialog.dismiss();
				}
				
//				if (MyTerminalFactory.getSDK().getGroupCallManager().getGroupCallListenStateMachine().getCurrentState() == GroupCallListenState.LISTENING
//						&& audioManager.getMode() == AudioManager.MODE_NORMAL) {
//					audioManager.setSpeakerphoneOn(true);
//				}
				logger.info("耳机拔出，声音类型："+audioManager.getMode()+"	扬声器状态："+audioManager.isSpeakerphoneOn());
				
			} else if (intent.getIntExtra("state", 0) == 1) {//耳机插入
				
//				if (MyTerminalFactory.getSDK().getGroupCallManager().getGroupCallListenStateMachine().getCurrentState() == GroupCallListenState.LISTENING
//						&& audioManager.getMode() == AudioManager.MODE_NORMAL) {
//					audioManager.setSpeakerphoneOn(false);
//				}
				logger.info("耳机插入，声音类型："+audioManager.getMode()+"	扬声器状态："+audioManager.isSpeakerphoneOn());
				
				//注册距离监听
				sensorManager.registerListener(sensorEventListener,
						sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY),// 距离感应器
						SensorManager.SENSOR_DELAY_NORMAL);// 注册传感器，第一个参数为距离监听器，第二个是传感器类型，第三个是延迟类型
				
				// 注册耳机线控按钮监听
				HeadSetUtil.getInstance().setOnHeadSetListener(headSetListener);
				HeadSetUtil.getInstance().open(context);
				
//				dialog.show();
//				View view = View.inflate(context,cn.zectec.ptt.R.layout.activity_headset, null);
//				dialog.getWindow().setContentView(view);
//				
//				view.findViewById(cn.zectec.ptt.R.id.ll_headset)
//						.setOnClickListener(new OnClickListener() {
//							@Override
//							public void onClick(View v) {
//								ToastUtil.showToast(context, "您选择的是耳机");
//								dialog.dismiss();
//							}
//						});
//				view.findViewById(cn.zectec.ptt.R.id.ll_miKey)
//						.setOnClickListener(new OnClickListener() {
//							@Override
//							public void onClick(View v) {
//								ToastUtil.showToast(context, "您选择的是米键，用手机的麦克和扬声器");
//								dialog.dismiss();
//							}
//						});

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

package cn.vsx.uav.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/8/13
 * 描述：
 * 修订历史：
 */
public class ScreenSwitchUtils{
    private static final String TAG = ScreenSwitchUtils.class.getSimpleName();

    private volatile static ScreenSwitchUtils mInstance;

    private Activity mActivity;

    private SensorManager sm;
    private OrientationSensorListener listener;
    private Sensor sensor;
    private boolean landscape = true;//横屏

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 888:
                    int orientation = msg.arg1;
//                    Log.d(TAG, "orientation:" + orientation);
                    if (orientation > 45 && orientation < 135) {
                        if(landscape){
                            Log.d(TAG, "反向横屏");
                            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                            landscape = false;
                        }
                    } else if (orientation > 135 && orientation < 225) {

                    } else if (orientation > 225 && orientation < 315) {
                        if (!landscape) {
                            Log.d(TAG, "切换成横屏");
                            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                            landscape = true;
                        }
                    } else if ((orientation > 315 && orientation < 360) || (orientation > 0 && orientation < 45)) {
//                        if (!isPortrait) {
//                            Log.d(TAG,"切换成竖屏");
//                            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//                            isPortrait = true;
//                        }
                    }
                    break;
                default:
                    break;
            }

        };
    };

    /** 返回ScreenSwitchUtils单例 **/
    public static ScreenSwitchUtils init(Context context) {
        if (mInstance == null) {
            synchronized (ScreenSwitchUtils.class) {
                if (mInstance == null) {
                    mInstance = new ScreenSwitchUtils(context);
                }
            }
        }
        return mInstance;
    }

    private ScreenSwitchUtils(Context context) {
        // 注册重力感应器,监听屏幕旋转
        sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        listener = new OrientationSensorListener(mHandler);
    }

    /** 开始监听 */
    public void start(Activity activity) {
        mActivity = activity;
        sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI);
    }

    /** 停止监听 */
    public void stop() {
        sm.unregisterListener(listener);
    }


//    public boolean isPortrait(){
//        return this.isPortrait;
//    }

    /**
     * 重力感应监听者
     */
    public class OrientationSensorListener implements SensorEventListener{
        private static final int _DATA_X = 0;
        private static final int _DATA_Y = 1;
        private static final int _DATA_Z = 2;

        public static final int ORIENTATION_UNKNOWN = -1;

        private Handler rotateHandler;

        public OrientationSensorListener(Handler handler) {
            rotateHandler = handler;
        }

        public void onAccuracyChanged(Sensor arg0, int arg1) {
        }

        public void onSensorChanged(SensorEvent event) {
            float[] values = event.values;
            int orientation = ORIENTATION_UNKNOWN;
            float X = -values[_DATA_X];
            float Y = -values[_DATA_Y];
            float Z = -values[_DATA_Z];
            float magnitude = X * X + Y * Y;
            // Don't trust the angle if the magnitude is small compared to the y
            // value
            if (magnitude * 4 >= Z * Z) {
                // 屏幕旋转时
                float OneEightyOverPi = 57.29577957855f;
                float angle = (float) Math.atan2(-Y, X) * OneEightyOverPi;
                orientation = 90 - (int) Math.round(angle);
                // normalize to 0 - 359 range
                while (orientation >= 360) {
                    orientation -= 360;
                }
                while (orientation < 0) {
                    orientation += 360;
                }
            }
            if (rotateHandler != null) {
                rotateHandler.obtainMessage(888, orientation, 0).sendToTarget();
            }
        }
    }
}

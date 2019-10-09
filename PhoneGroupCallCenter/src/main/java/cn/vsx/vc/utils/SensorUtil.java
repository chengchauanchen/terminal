package cn.vsx.vc.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PowerManager;

import org.apache.log4j.Logger;

import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallState;
import cn.vsx.vc.application.MyApplication;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/1/21
 * 描述：
 * 修订历史：
 */
public class SensorUtil{

    private final SensorManager sensorManager;
    private Logger logger = Logger.getLogger(SensorUtil.class);
    private PowerManager.WakeLock wakeLock;
    private boolean registed;
    private static SensorUtil instance;

    @SuppressLint("InvalidWakeLockTag")
    private SensorUtil(Context context){
        sensorManager = (SensorManager) context.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        PowerManager powerManager = (PowerManager) context.getApplicationContext().getSystemService(Context.POWER_SERVICE);
        if(null != powerManager){
            wakeLock = powerManager.newWakeLock(32, "wakeLock");
        }
    }

    public static synchronized SensorUtil getInstance() {
        if (instance == null) {
            instance = new SensorUtil(MyApplication.instance.getApplicationContext());
        }
        return instance;
    }

    /**
     * 注册距离监听器
     */

    public void registSensor(){
        logger.info("个呼状态:"+MyApplication.instance.getIndividualState());
        if(!registed && null != MyApplication.instance.getIndividualState() && MyApplication.instance.getIndividualState() != IndividualCallState.IDLE
                && !MyApplication.instance.isMiniLive && !MyApplication.instance.headset){
            if(null != sensorManager){
                sensorManager.registerListener(sensorEventListener,
                        sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY),// 距离感应器
                        SensorManager.SENSOR_DELAY_NORMAL);// 注册传感器，第一个参数为距离监听器，第二个是传感器类型，第三个是延迟类型
                registed = true;
            }
        }
    }

    public void unregistSensor(){
        if(registed){
            sensorManager.unregisterListener(sensorEventListener);
            registed = false;
        }
    }

    /**距离感应器*/
    @SuppressLint("Wakelock")
    private SensorEventListener sensorEventListener = new SensorEventListener() {

        @SuppressLint("WakelockTimeout")
        @Override
        public void onSensorChanged(SensorEvent event) {
            float[] its = event.values;
            if (its != null && event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                // 经过测试，当手贴近距离感应器的时候its[0]返回值为0.0，当手离开时返回1.0
                if (its[0] == 0.0) {// 贴近手机
                    logger.info("hands up in calling activity贴近手机");
                    if (null != wakeLock && !wakeLock.isHeld()) {
                        wakeLock.acquire();// 申请设备电源锁
                    }
                } else {// 远离手机
                    logger.info("hands moved in calling activity远离手机");
                    if (null != wakeLock && wakeLock.isHeld()) {
                        wakeLock.release(); // 释放设备电源锁
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
}

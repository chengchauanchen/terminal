package ptt.terminalsdk.listener;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import ptt.terminalsdk.broadcastreceiver.BatteryBroadcastReceiver;
import ptt.terminalsdk.broadcastreceiver.MyPhoneStateListener;

/**
 * @author jizheng
 * @version v1.0.0
 * @date 2020/6/26
 * @desc
 * @update
 * @upate_desc
 */
public class BaseListener {
    private Context context;

    public TelephonyManager mTelephonyManager;
    public MyPhoneStateListener myPhoneStateListener;
    private static BaseListener baseListener = null;
    public BaseListener(Context context){
        this.context = context;
    }
    public static BaseListener getInstance(Context context){
        if (baseListener == null){
            baseListener = new BaseListener(context);
        }
        return baseListener;
    }
    /**
     * 注册电量广播
     */
    public void registBatterBroadcastReceiver(BatteryBroadcastReceiver batteryBroadcastReceiver) {
        if (batteryBroadcastReceiver != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_BATTERY_CHANGED);
            context.registerReceiver(batteryBroadcastReceiver, filter);
        }
    }

    /**
     * 注销电量的广播
     */
    public void unRegistBatterBroadcastReceiver(BatteryBroadcastReceiver batteryBroadcastReceiver) {
        if (batteryBroadcastReceiver != null) {
            context.unregisterReceiver(batteryBroadcastReceiver);
        }
    }

    /**
     * 初始化手机信号的监听
     */
    public void initPhoneStateListener() {
        //获取telephonyManager
        mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        //开始监听
        myPhoneStateListener = new MyPhoneStateListener(context);
        //监听信号强度
        mTelephonyManager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    /**
     * 注册手机信号的监听
     */
    public void registPhoneStateListener() {
        if (mTelephonyManager != null && myPhoneStateListener != null) {
            mTelephonyManager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        }
    }

    /**
     * 注销手机信号的监听
     */
    public void unRegistPhoneStateListener() {
        if (mTelephonyManager != null && myPhoneStateListener != null) {
            myPhoneStateListener.onStop();
            mTelephonyManager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }
}

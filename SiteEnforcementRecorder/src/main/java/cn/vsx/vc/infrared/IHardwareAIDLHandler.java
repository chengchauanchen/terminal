package cn.vsx.vc.infrared;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IRService;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by abero on 2018/7/11.
 */

public class IHardwareAIDLHandler {

    private static final String TAG = "IHardwareAIDLHandler";

    private HardwareHandler mHandler;
    private HandlerThread mHandlerThread;
    private IRService mHardwareService;
    private boolean isBlinking = false;
    private int mLed;
    private boolean isOpen = false;
    private boolean sensing = false;
    private OnHardwareCallback mCallback;
    private boolean isInfredOpen = false;
    private float[] mSampliingValues = new float[5];
    private int mIndex = 0;

    private IHardwareAIDLHandler() {

        mHandlerThread = new HandlerThread(TAG);
        mHandlerThread.start();
        mHandler = new HardwareHandler(mHandlerThread.getLooper());

        //normalLed();
    }

    public static IHardwareAIDLHandler getInstance() {
        return InstanceHolder.sInstance;
    }

    private static class InstanceHolder {
        private static IHardwareAIDLHandler sInstance = new IHardwareAIDLHandler();
    }

    public interface OnHardwareCallback {

        void onInfredOpen();

        void onInfredClose();

        void onSensor(float value);
    }

    public void setOnHardwareCallback(OnHardwareCallback callback) {
        mCallback = callback;
    }

    public void getHardwareService() {
        //反射获取ServiceManager
        try {
            //指定反射类
            Class forName = Class.forName("android.os.ServiceManager");
            //获取方法，参数是String类型
            Method method = forName.getMethod("getService", String.class);
            //传入参数
            IBinder iBinder = (IBinder) method.invoke(null, new Object[]{"IRCtrlService"});
            //初始化AIDL
            mHardwareService = IRService.Stub.asInterface(iBinder);
            Log.i(TAG, "got: .....");

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    public void openRedLed() {
        mHandler.sendEmptyMessage(RED_LED_OPEN);
    }

    public void closeRedLed() {
        mHandler.sendEmptyMessage(RED_LED_CLOSE);
    }


    public void openGreenLed() {
        mHandler.sendEmptyMessage(GREEN_LED_OPEN);
    }

    public void closeGreenLed() {
        mHandler.sendEmptyMessage(GREEN_LED_CLOSE);
    }

    public void openBlueLed() {
        mHandler.sendEmptyMessage(BLUE_LED_OPEN);
    }

    public void closeBlueLed() {
        mHandler.sendEmptyMessage(BLUE_LED_CLOSE);

    }

    public void startBlinkLed(int led) {
        Log.i(TAG, "startBlinkLed: ");
        mHandler.removeMessages(LED_BLINKING);

        isBlinking = true;
        Message msg = mHandler.obtainMessage();
        msg.what = LED_BLINKING;
        msg.arg1 = led;
        mHandler.sendMessage(msg);
    }

    public void stopBlinkLed(int led) {
        isBlinking = false;
        Message msg = mHandler.obtainMessage();
        msg.what = LED_BLINK_CLOSE;
        msg.arg1 = led;
        mHandler.sendMessage(msg);
    }

    private void normalLed() {
        openGreenLed();
    }

    private void binking() throws RemoteException {
        isOpen = !isOpen;
        if (RED_LED == mLed) {


        } else if (GREEN_LED == mLed) {

        } else if (BLUE_LED == mLed) {

        }

    }

    public void openInfred() {
        mHandler.sendEmptyMessage(INFRED_OPEN);
    }

    public void closeInfred() {
        mHandler.sendEmptyMessage(INFRED_CLOSE);
    }

    public void startInfreSensing() {
        sensing = true;
        mHandler.sendEmptyMessage(INFRED_SENSING);
    }

    public void stopInfredSensing() {
        sensing = false;
        mHandler.removeMessages(INFRED_SENSING);
        mHandler.sendEmptyMessage(INFRED_CLOSE);
    }

    public void screenOn() {
        mHandler.sendEmptyMessage(SCREEN_ON);
    }

    public void screenOff() {
        mHandler.sendEmptyMessage(SCREEN_OFF);
    }

    private void sensingForInfred() throws RemoteException {
        if(mHardwareService!=null){
            float value = mHardwareService.IRLightSensor();

            if (mCallback != null)
                mCallback.onSensor(value);
            Log.i(TAG, "sensingForInfred: value=" + value);

            mIndex = (mIndex + 1) % 5;
            Log.i(TAG, "sensingForInfred:  index=" + mIndex);
            mSampliingValues[mIndex] = value;
            float total = 0;
            for (int i = 0; i < 5; i++)
                total = total + mSampliingValues[i];

            float av = total / 5;
            Log.i(TAG, "sensingForInfred:  av=" + av);

            if (av > 120000) {
                if (!isInfredOpen) {
                    isInfredOpen = true;
                    mHardwareService.IRCUTForward();
                    if (mCallback != null)
                        mCallback.onInfredOpen();
                }
            } else {
                if (isInfredOpen) {
                    isInfredOpen = false;
                    mHardwareService.IRCUTReverse();
                    if (mCallback != null)
                        mCallback.onInfredClose();
                }
            }
        }
    }


    private final int RED_LED_OPEN = 1;
    private final int RED_LED_CLOSE = 2;
    private final int GREEN_LED_OPEN = 3;
    private final int GREEN_LED_CLOSE = 4;
    private final int BLUE_LED_OPEN = 5;
    private final int BLUE_LED_CLOSE = 6;
    private final int LED_BLINKING = 8;
    private final int INFRED_OPEN = 9;
    private final int INFRED_CLOSE = 10;
    private final int INFRED_SENSING = 11;

    private final int SCREEN_ON = 12;
    private final int SCREEN_OFF = 13;

    private final int LED_BLINK_CLOSE = 14;

    public static final int RED_LED = 1;
    public static final int GREEN_LED = 2;
    public static final int BLUE_LED = 3;

    public static int LED_OPEN = 255;
    public static int LED_CLOSE = 0;


    private class HardwareHandler extends Handler {

        public HardwareHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case RED_LED_OPEN:
                        mHardwareService.BLNCTRLBrightness(RED_LED, LED_OPEN);
                        break;
                    case RED_LED_CLOSE:
                        mHardwareService.BLNCTRLBrightness(RED_LED, LED_CLOSE);
                        break;
                    case GREEN_LED_OPEN:
                        mHardwareService.BLNCTRLBrightness(GREEN_LED, LED_OPEN);
                        break;
                    case GREEN_LED_CLOSE:
                        mHardwareService.BLNCTRLBrightness(GREEN_LED, LED_CLOSE);
                        break;
                    case BLUE_LED_OPEN:
                        mHardwareService.BLNCTRLBrightness(BLUE_LED, LED_OPEN);
                        break;
                    case BLUE_LED_CLOSE:
                        mHardwareService.BLNCTRLBrightness(BLUE_LED, LED_CLOSE);
                        break;
                    case LED_BLINKING:
                        Log.i(TAG, "handleMessage: LED_BLINKING");
                        int led = msg.arg1;
                        mHardwareService.BLNCTRLBlink(led, LED_OPEN);
                        break;
                    case LED_BLINK_CLOSE:
                        int ld = msg.arg1;
                        mHardwareService.BLNCTRLBlink(ld, LED_CLOSE);
                        break;
                    case INFRED_OPEN:
                        Log.i(TAG, "handleMessage: INFRED_OPEN");
                        mHardwareService.IRLEDBrightness(255);
                        mHardwareService.IRCUTForward();
                        if (mCallback != null)
                            mCallback.onInfredOpen();
                        break;
                    case INFRED_CLOSE:
                        Log.i(TAG, "handleMessage:  INFRED_CLOSE");
                        mHardwareService.IRLEDBrightness(0);
                        mHardwareService.IRCUTReverse();
                        if (mCallback != null)
                            mCallback.onInfredClose();
                        break;
                    case INFRED_SENSING:
                        if (sensing) {
                            sensingForInfred();
                            removeMessages(INFRED_SENSING);
                            sendEmptyMessageDelayed(INFRED_SENSING, 2000);
                        }
                        break;
                    case SCREEN_ON:
                        Log.i("mHardwareService","ScreenOn");
                        mHardwareService.ScreenOn();
                        break;
                    case SCREEN_OFF:
                        mHardwareService.ScreenOff();
                        break;
                    default:
                        break;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
                Log.i(TAG, "handleMessage: RemoteException");
            }
            super.handleMessage(msg);
        }


    }

    /**
     * 退出
     */
    public void quit() {
        stopInfredSensing();

        if (mHandlerThread != null) {
            mHandlerThread.quit();
        }

        if(mHandler!=null){
            mHandler.removeCallbacksAndMessages(null);
        }
        mHardwareService = null;
    }


}

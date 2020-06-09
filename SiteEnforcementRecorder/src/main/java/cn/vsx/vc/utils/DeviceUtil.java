package cn.vsx.vc.utils;


import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import org.apache.log4j.Logger;

import java.lang.reflect.Method;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.infrared.IHardwareAIDLHandler;
import cn.vsx.vc.key.CL310AKey;
import cn.vsx.vc.key.KeyUtil;
import cn.vsx.vc.model.InfraRedState;
import cn.vsx.vc.receiveHandle.ReceiverNfcStatusHandler;
import cn.vsx.vc.receiveHandle.ReceiverUpdateInfraRedHandler;
import ptt.terminalsdk.context.MyTerminalFactory;

public class DeviceUtil {

    public static Logger logger = Logger.getLogger(KeyUtil.class);
    public static final String TAG = "DeviceUtil---";

    public static final String TYPE_BITSTART_I7 = "A7 for arm64";
    public static final String TYPE_CL310A = "CL310A";

    private static IHardwareAIDLHandler hardwareAIDLHandler;
    private static int infraRedState;

    /**
     * 初始化红外
     */
    public static void initInfraRedState(){
        if(TextUtils.equals(TYPE_BITSTART_I7,Build.MODEL)){
            initInfraRedStateFromBitStartI7();
        }
    }

    /**
     * 比特星I7红外
     */
    private static void initInfraRedStateFromBitStartI7() {
        hardwareAIDLHandler = IHardwareAIDLHandler.getInstance();
        hardwareAIDLHandler.setOnHardwareCallback(new IHardwareAIDLHandler.OnHardwareCallback() {
            @Override
            public void onInfredOpen() {
                MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiverUpdateInfraRedHandler.class, true);
            }

            @Override
            public void onInfredClose() {
                MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiverUpdateInfraRedHandler.class, false);
            }

            @Override
            public void onSensor(final float value) {
                if (infraRedState == InfraRedState.AUTO.getCode()) {
                    logger.info("--光敏值--:" + value);
//                    ToastUtil.showToast(BaseActivity.this,"光敏值："+value);
                    if (value > 1) {
                        openInfraRed();
                    } else {
                        closeInfraRed();
                    }
                }
            }
        });
        hardwareAIDLHandler.getHardwareService();
        changeInfraRed(TerminalFactory.getSDK().getParam(Params.INFRA_RED_STATE, InfraRedState.CLOSE.getCode()));
    }

    /**
     * USB连接状态
     * @param isConnected
     */
    public static void usbConnetStatus(boolean isConnected){
        if(TextUtils.equals(TYPE_BITSTART_I7,Build.MODEL)){
            usbConnetStatusFromBitStartI7(isConnected);
        }
    }

    /**
     * 比特星I7USB连接状态
     * @param isConnected
     */
    private static void usbConnetStatusFromBitStartI7(boolean isConnected) {
        setFlyMode(isConnected);
        if(isConnected){
            setUsbFunction(MyTerminalFactory.getSDK().getFileTransferOperation().checkOnlyUseSdCardStorage()?"mass_storage":"mtp", true);
        }else{
            setUsbFunction("none", false);
        }
        MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiverNfcStatusHandler.class, !isConnected);
    }

    /**
     * 设置是否打开飞行模式
     * @param enable
     */
    private static void setFlyMode(boolean enable) {
        try{
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
                Settings.System.putInt(MyApplication.instance.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, enable ? 1 : 0);
            } else {
                Settings.Global.putInt(MyApplication.instance.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, enable ? 1 : 0);
            }
            Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            intent.putExtra("state", enable);
            MyApplication.instance.sendBroadcast(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private static void setUsbFunction(String mode, boolean unlock) {
        logger.info(TAG+"setUsbFunction--mode:"+mode+"---unlock:" + unlock);
        try {
            //mUsbManager.setCurrentFunction(UsbManager.USB_FUNCTION_MASS_STORAGE,true);
            UsbManager um = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                um = MyApplication.instance.getSystemService(UsbManager.class);
            }
            Method m = um.getClass().getDeclaredMethod("setCurrentFunction", new Class[]{String.class, boolean.class});
            m.setAccessible(true);
            if (null == um)
                Log.e("setUsbFunction", "UsbManager : null");
            m.invoke(um, mode, unlock);
            Log.i(TAG, "setUsbFunction:  done");
        } catch (Exception e) {
            logger.info(TAG+"setUsbFunction--e:"+e);
        }

    }


    /**
     * 设置红外的开关
     *
     * @param state
     */
    public static void changeInfraRed(int state) {
        if(TextUtils.equals(TYPE_BITSTART_I7,Build.MODEL)){
            infraRedState = state;
            TerminalFactory.getSDK().putParam(Params.INFRA_RED_STATE, state);
            if (state == InfraRedState.CLOSE.getCode()) {
                if (hardwareAIDLHandler != null) {
                    hardwareAIDLHandler.stopInfredSensing();
                }
                closeInfraRed();
            } else if (state == InfraRedState.OPEN.getCode()) {
                if (hardwareAIDLHandler != null) {
                    hardwareAIDLHandler.stopInfredSensing();
                }
                openInfraRed();
            } else if (state == InfraRedState.AUTO.getCode()) {
                if (hardwareAIDLHandler != null) {
                    hardwareAIDLHandler.startInfreSensing();
                }
            }
        }
    }

    /**
     * 开启红外
     */
    private static void openInfraRed() {
        if (hardwareAIDLHandler != null) {
            hardwareAIDLHandler.openInfred();
        }
    }

    /**
     * 关闭红外
     */
    public static void closeInfraRed() {
        if (hardwareAIDLHandler != null) {
            hardwareAIDLHandler.closeInfred();
        }
    }

    /**
     * 停止
     */
    public static void stopInfraRed() {
        if(TextUtils.equals(TYPE_BITSTART_I7,Build.MODEL)){
            if (hardwareAIDLHandler != null) {
                hardwareAIDLHandler.quit();
                hardwareAIDLHandler = null;
            }
        }
    }

    /**
     * 注册按键事件
     */
    public static void registListener(){
        //注册CL310A实体按键事件的监听
        if(TextUtils.equals(TYPE_CL310A,Build.MODEL)){
            CL310AKey key = (CL310AKey)KeyUtil.getKeyByType(Build.MODEL);
            key.registKeyEventFromCL310AReceiver();
        }
    }

    /**
     * 注销按键事件
     */
    public static void unRegistListener(){
        //注册CL310A实体按键事件的监听
        if(TextUtils.equals(TYPE_CL310A,Build.MODEL)){
            CL310AKey key = (CL310AKey)KeyUtil.getKeyByType(Build.MODEL);
            key.unRegistKeyEventFromCL310AReceiver();
        }
    }
    /**
     * 是否显示红外设置页面
     * @return
     */
    public static boolean showSettingInfraRed(){
        return (TextUtils.equals(TYPE_BITSTART_I7,Build.MODEL));
    }

    /**
     * 是否可以点击返回按键返回到home页面
     * @return
     */
    public static boolean canBackToHome(){
       return (!TextUtils.equals(TYPE_BITSTART_I7,Build.MODEL));
    }
}

package cn.vsx.vc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import org.apache.log4j.Logger;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.vc.receiveHandle.ReceiverUsbConnetStateHandler;

public class UsbConnetStateReceiver extends BroadcastReceiver {
    public Logger logger = Logger.getLogger(getClass());
    private static final String TAG = "UsbConnetStateReceiver---";
    public static final String ACTION_USB = "android.hardware.usb.action.USB_STATE" ;
    @Override
    public void onReceive(Context context, Intent intent) {
        try{
            if(intent!=null&&intent.getAction()!=null){
                if (TextUtils.equals(ACTION_USB,intent.getAction())) {
                    boolean connected = intent.getExtras().getBoolean("connected");
                    logger.info(TAG+"onReceive--connected:"+connected);
                    //电池电量
                    TerminalFactory.getSDK().notifyReceiveHandler(ReceiverUsbConnetStateHandler.class,connected);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

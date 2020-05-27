package ptt.terminalsdk.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import ptt.terminalsdk.receiveHandler.ReceiverPowerSaveCountDownHandler;

public class PowerSaveReceiver extends BroadcastReceiver {
    private Logger logger = LoggerFactory.getLogger(PowerSaveReceiver.class);
    public static final String TAG = "PowerSaveManager---";
    @Override
    public void onReceive(Context context, Intent intent) {
//        logger.info(TAG+"PowerSaveReceiver：收到倒计时的广播");
        try{
            if(intent!=null){
                logger.info(TAG+"PowerSaveReceiver--getAction："+intent.getAction());
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiverPowerSaveCountDownHandler.class,intent.getAction());
            }
        }catch (Exception e){
         e.printStackTrace();
        }
    }
}

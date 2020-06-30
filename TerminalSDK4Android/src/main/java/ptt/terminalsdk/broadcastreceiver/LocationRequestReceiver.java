package ptt.terminalsdk.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import ptt.terminalsdk.receiveHandler.ReceiverLocationCountDownHandler;

public class LocationRequestReceiver extends BroadcastReceiver {
    private Logger logger = LoggerFactory.getLogger(LocationRequestReceiver.class);
    public static final String TAG = "LocationManager---";
    @Override
    public void onReceive(Context context, Intent intent) {
        try{
            if(intent!=null){
                logger.info(TAG+"LocationRequestReceiver--getAction："+intent.getAction());
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiverLocationCountDownHandler.class,intent.getAction());
            }
        }catch (Exception e){
         e.printStackTrace();
        }
    }
}

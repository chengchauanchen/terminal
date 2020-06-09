package cn.vsx.vc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import org.apache.log4j.Logger;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.vc.key.CL310AKey;
import cn.vsx.vc.key.KeyUtil;

import static cn.vsx.vc.utils.DeviceUtil.TYPE_CL310A;

public class KeyEventFromCL310AReceiver extends BroadcastReceiver {
    public Logger logger = Logger.getLogger(getClass());
    private static final String TAG = "KeyEventFromCL310AReceiver---";
    public static final String ACTION_KEY_EVENT = "android.intent.action.SIDE_KEY_INTENT" ;
    @Override
    public void onReceive(Context context, Intent intent) {
        try{
            if(intent!=null&&intent.getAction()!=null){
                if (TextUtils.equals(ACTION_KEY_EVENT,intent.getAction())&& intent.getExtras()!=null) {
                    TerminalFactory.getSDK().getThreadPool().execute(() -> {
                        int keyCode = intent.getExtras().getInt("key_code");
                        int keyEvent = intent.getExtras().getInt("key_event");
                        int keyStatus = intent.getExtras().getInt("key_status");
                        logger.info(TAG+"onReceive--keyCode:"+keyCode+"-keyEvent:"+keyEvent+"-keyStatus:"+keyStatus);
                        if(TextUtils.equals(TYPE_CL310A,Build.MODEL)){
                            CL310AKey key = (CL310AKey)KeyUtil.getKeyByType(Build.MODEL);
                            key.onKeyEvent(keyCode,keyEvent,keyStatus);
                        }
                    });
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

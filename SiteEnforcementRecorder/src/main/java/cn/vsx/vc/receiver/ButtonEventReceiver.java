package cn.vsx.vc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivePTTDownHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivePTTUpHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.utils.Constants;

public class ButtonEventReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        Log.e("ButtonEventReceiver",intentAction);
        if (TextUtils.equals(Constants.BUTTON_EVENT_ACTION,intentAction)){
            int key_code = intent.getIntExtra("key_code",-1);
            int key_event = intent.getIntExtra("key_event",-1);
            int key_status = intent.getIntExtra("key_status",-1);
            if (key_code == 285){
                //0代表按下，1代表抬起
                if (key_status == 0){
                    //0代表短按,1代表长按
                    if (key_event ==1){
                        //发起组呼
                        int requestGroupCall = TerminalFactory.getSDK().getGroupCallManager().requestGroupCall("",TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID,0));
                        TerminalFactory.getSDK().stopMultimediaMessage();//发起组呼时，把录音关掉
                        TerminalFactory.getSDK().notifyReceiveHandler(ReceivePTTDownHandler.class,requestGroupCall);
                    }
                }else if (key_status == 1){
                    TerminalFactory.getSDK().getGroupCallManager().ceaseGroupCall();
                    TerminalFactory.getSDK().notifyReceiveHandler(ReceivePTTUpHandler.class);
                }
            }
        }
    }
}

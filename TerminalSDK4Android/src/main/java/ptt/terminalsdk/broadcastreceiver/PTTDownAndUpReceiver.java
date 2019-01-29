package ptt.terminalsdk.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.apache.log4j.Logger;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivePTTDownHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivePTTUpHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;

/**
 * Created by zckj on 2017/5/27.
 */

public class PTTDownAndUpReceiver extends BroadcastReceiver {

    private static final String[] down = { "BLUE_TOOTH_DOWN","com.zello.ptt.down", "com.sonim.intent.action.PTT_KEY_DOWN", "android.intent.action.PTT.down", "com.runbo.ptt.key.down","com.yl.ptt.keydown" ,"com.ntdj.ptt_down"};
    private static final String[] up = { "BLUE_TOOTH_UP","com.zello.ptt.up", "com.sonim.intent.action.PTT_KEY_UP", "android.intent.action.PTT.up", "com.runbo.ptt.key.up","com.yl.ptt.keyup" ,"com.ntdj.ptt_up"};
    private Logger logger = Logger.getLogger(getClass());

    @Override
    public void onReceive(Context context, Intent intent) {
        logger.info("什么类型的手机按下抬起PTT按键" + intent.getAction());
        if(intent.getAction().equals(down[0])
                || intent.getAction().equals(down[1])
                || intent.getAction().equals(down[2])
                || intent.getAction().equals(down[3])
                || intent.getAction().equals(down[4])
                || intent.getAction().equals(down[5])
                || intent.getAction().equals(down[6])
                ){
            if(!TerminalFactory.getSDK().getParam(Params.IS_EXIT,false)){
                TerminalFactory.getSDK().getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        int requestGroupCall = TerminalFactory.getSDK().getGroupCallManager().requestGroupCall("");
                        TerminalFactory.getSDK().stopMultimediaMessage();//发起组呼时，把录音关掉
                        TerminalFactory.getSDK().notifyReceiveHandler(ReceivePTTDownHandler.class, requestGroupCall);
                    }
                });
            }
        }
        if(intent.getAction().equals(up[0])
                || intent.getAction().equals(up[1])
                || intent.getAction().equals(up[2])
                || intent.getAction().equals(up[3])
                || intent.getAction().equals(up[4])
                || intent.getAction().equals(up[5])
                || intent.getAction().equals(up[6])
                ){
            if(!TerminalFactory.getSDK().getParam(Params.IS_EXIT,false)) {
                TerminalFactory.getSDK().getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        TerminalFactory.getSDK().getGroupCallManager().ceaseGroupCall();
                        TerminalFactory.getSDK().notifyReceiveHandler(ReceivePTTUpHandler.class);
                    }
                });
            }
        }
    }
}

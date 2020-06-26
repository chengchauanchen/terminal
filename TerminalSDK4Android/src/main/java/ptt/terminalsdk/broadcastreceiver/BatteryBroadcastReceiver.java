package ptt.terminalsdk.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

import org.apache.log4j.Logger;

import ptt.terminalsdk.context.BaseApplication;
import ptt.terminalsdk.manager.Prompt.PromptManager;


public class BatteryBroadcastReceiver extends BroadcastReceiver {
    public Logger logger = Logger.getLogger(getClass());
    private int temptValue = 0;
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent!=null&&intent.getAction()!=null){
            if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                //电池电量
                int level = intent.getIntExtra("level", 0);
                //电池状态
                int status=intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
                if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                    //正在充电
                    temptValue = 0;
                }else{
                    if (level >50) {
                    }else if (level > 30) {
                        //低于50
                        checkBatteryTempt(50,level);
                    }else if (level > 10) {
                        //低于30
                        checkBatteryTempt(30,level);
                    }else if (10 <= level ) {
                        //低于10
                        checkBatteryTempt(10,level);
                    }
                }
                //保存电池电量
                BaseApplication.getApplication().setBatteryLevel(level);
            }
        }
    }

    /**
     *检查是否提示
     * @param compareValue
     */
    private void checkBatteryTempt(int compareValue,int level){
        if(temptValue != compareValue){
            logger.info("BatteryBroadcastReceiver---checkBatteryTempt--level："+level);
            PromptManager.getInstance().lowPower(compareValue);
            temptValue = compareValue;
        }
    }
}

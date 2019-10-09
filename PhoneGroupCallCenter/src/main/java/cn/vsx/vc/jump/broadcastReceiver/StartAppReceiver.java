package cn.vsx.vc.jump.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.apache.log4j.Logger;

import cn.vsx.vc.activity.RegistGroupCallCenterActivity;

/**
 * 连接MessageService 的广播消息
 * <p>
 * 当收到 第三方应用发送的 message广播后，连接其message
 * <p>
 * Intent 中会带包名
 *
 * @author qzw
 */
public class StartAppReceiver extends BroadcastReceiver {

    protected static Logger logger = Logger.getLogger(StartAppReceiver.class);

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1 = new Intent(context, RegistGroupCallCenterActivity.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent1);
    }
}

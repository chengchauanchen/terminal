package cn.vsx.vc.jump.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import cn.vsx.vc.jump.sendMessage.ThirdSendMessage;

/**
 * 连接MessageService 的广播消息
 * <p>
 * 当收到 第三方应用发送的 message广播后，连接其message
 * <p>
 * Intent 中会带包名
 *
 * @author qzw
 */
public class ConnectMessageReceiver extends BroadcastReceiver {

    public static final String PACKAGE_NAME = "PackageName";

    @Override
    public void onReceive(Context context, Intent intent) {
        String packageName = intent.getStringExtra(PACKAGE_NAME);
        Log.e("广播", "收到绿之云的MessageService启动了的广播，可以尝试连接" + ",包名：" + packageName);

        ThirdSendMessage.getInstance().connectReceivedService(context, packageName, true);
    }
}

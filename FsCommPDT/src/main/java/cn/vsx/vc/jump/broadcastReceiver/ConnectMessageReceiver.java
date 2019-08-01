package cn.vsx.vc.jump.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.apache.log4j.Logger;

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

    protected static Logger logger = Logger.getLogger(ConnectMessageReceiver.class);
    public static final String PACKAGE_NAME = "PackageName";

    @Override
    public void onReceive(Context context, Intent intent) {
        String packageName = intent.getStringExtra(PACKAGE_NAME);
        logger.info("--vsxSDK--"+"收到广播--正在连接--"+packageName);
        ThirdSendMessage.getInstance().connectReceivedService(context, packageName, true);
    }
}

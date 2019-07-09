package cn.vsx.vsxsdk.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import cn.vsx.vsxsdk.VsxSDK;
import cn.vsx.vsxsdk.service.VsxReceivedService;

/**
 * 连接JumpService 的广播消息
 *
 * 当收到 融合通信app发送的 Jump广播后，可连接其JumpService
 *
 * @author qzw
 */
public class ConnectJumpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("广播","收到JumpService启动了的广播，可以尝试连接");
        VsxSDK.getInstance().connectJumpService(context,true);
    }
}

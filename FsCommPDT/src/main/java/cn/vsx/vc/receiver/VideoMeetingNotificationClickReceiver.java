package cn.vsx.vc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCloseVideoMeetingMinimizeHandler;

/**
 *  author : RuanShuo
 *
 *  通知栏点击事件
 */
public class VideoMeetingNotificationClickReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        TerminalFactory.getSDK().notifyReceiveHandler(ReceiveCloseVideoMeetingMinimizeHandler.class);
    }
}

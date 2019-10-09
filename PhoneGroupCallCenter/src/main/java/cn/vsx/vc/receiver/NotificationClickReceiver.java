package cn.vsx.vc.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import cn.vsx.hamster.common.MessageCategory;
import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUnreadMessageChangedHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.TerminalMessageUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 *  author : RuanShuo
 *
 *  通知栏点击事件
 */
public class NotificationClickReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getBundleExtra("bundle");
        TerminalMessage terminalMessage = (TerminalMessage) bundle.getSerializable("TerminalMessage");
        if(terminalMessage ==null){
            return;
        }

        if(terminalMessage.messageFromId == terminalMessage.messageToId &&
                terminalMessage.messageFromId == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) &&
                terminalMessage.messageType == MessageType.VIDEO_LIVE.getCode()) {/**  进入图像助手  ***/

//            Intent mIntent = new Intent(context, PushLiveMessageManageActivity.class);
//            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(mIntent);
        }
        else if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_PERSONAGE.getCode()) {
//            IndividualNewsActivity.startCurrentActivity(context,TerminalMessageUtil.getNo(terminalMessage),TerminalMessageUtil.getTitleName(terminalMessage),true,0);
        }
        else if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){//进入组会话页
//            Intent mIntent = new Intent(context, GroupCallNewsActivity.class);
//            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            mIntent.putExtra("isGroup", TerminalMessageUtil.isGroupMessage(terminalMessage));
//            mIntent.putExtra("uniqueNo",terminalMessage.messageToUniqueNo);
//            mIntent.putExtra("userId", TerminalMessageUtil.getNo(terminalMessage));//组id
//            mIntent.putExtra("userName", TerminalMessageUtil.getTitleName(terminalMessage));
//            mIntent.putExtra("speakingId",terminalMessage.messageFromId);
//            mIntent.putExtra("speakingName",terminalMessage.messageFromName);
//            context.startActivity(mIntent);
        }

        TerminalFactory.getSDK().notifyReceiveHandler(ReceiveUnreadMessageChangedHandler.class,terminalMessage);
        //清除所有通知
        NotificationManager notificationManager=(NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

    }
}

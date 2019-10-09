package cn.vsx.vc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import org.apache.log4j.Logger;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.vc.receiveHandle.ReceiveTianjinAuthLoginAndLogoutHandler;

public class TianjinAuthLoginAndLogoutReceiver extends BroadcastReceiver {
    public Logger logger = Logger.getLogger(getClass());
    private static final String ACTION_LOGIN = "cn.vsx.vc";
    private static final String ACTION_LOGOUT = "com.xdja.unifyauthorize.ACTION_LOGOUT";
    @Override
    public void onReceive(Context context, Intent intent) {
        logger.info("TianjinAuthLoginAndLogoutReceiver--onReceive:"+intent.getAction());
        if(TextUtils.equals(ACTION_LOGIN,intent.getAction())){
            //登录
            TerminalFactory.getSDK().notifyReceiveHandler(ReceiveTianjinAuthLoginAndLogoutHandler.class,true);
        }else if(TextUtils.equals(ACTION_LOGOUT,intent.getAction())){
            //登出
            TerminalFactory.getSDK().notifyReceiveHandler(ReceiveTianjinAuthLoginAndLogoutHandler.class,false);
        }
    }
}

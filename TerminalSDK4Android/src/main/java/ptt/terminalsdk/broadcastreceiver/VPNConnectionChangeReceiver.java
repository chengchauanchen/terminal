package ptt.terminalsdk.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNetworkChangeHandler;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/7/30
 * 描述：
 * 修订历史：
 */
public class VPNConnectionChangeReceiver extends BroadcastReceiver{

    // SSL广播消息数据
    public static final String ACTION_INTENT_DATA="data";
    // SSL广播消息：启动服务中
    public static final String ACTION_INTENT_STARTSERVER_INPROC="koal.ssl.broadcast.startserver.inproc";
    // SSL广播消息：启动服务成功
    public static final String ACTION_INTENT_STARTSERVER_SUCCESS="koal.ssl.broadcast.startserver.success";
    // SSL广播消息：启动服务失败
    public static final String ACTION_INTENT_STARTSERVER_FAILURE="koal.ssl.broadcast.startserver.failure";
    // SSL广播消息：下载策略成功
    public static final String ACTION_INTENT_DOWNLOADCFG_SUCCESS="koal.ssl.broadcast.downloadcfg.success";
    // SSL广播消息：停止服务成功
    public static final String ACTION_INTENT_STOPSERVER_SUCCESS="koal.ssl.broadcast.stopserver.success";
    // SSL广播消息：检测到新版本，可升级
    public static final String ACTION_INTENT_UPGRADE="koal.ssl.broadcast.upgrade";
    // SSL广播消息：网络（wifi/apn）已链接
    public static final String ACTION_INTENT_NETWORK_CONNECTED="koal.ssl.broadcast.network.connected";
    // SSL广播消息：网络（wifi/apn）已断开
    public static final String ACTION_INTENT_NETWORK_DISCONNECTED="koal.ssl.broadcast.network.disconnected";
    // SSL广播消息：隧道已建立
    public static final String ACTION_INTENT_TUNNEL_CONNECTED = "koal.ssl.broadcast.tunnel.connected";
    // SSL广播消息：隧道已断开
    public static final String ACTION_INTENT_TUNNEL_FAILURE = "koal.ssl.broadcast.tunnel.failure";
    // SSL广播消息：隧道已断开（认证错误）
    public static final String ACTION_INTENT_TUNNEL_FAILURE_AUTH = "koal.ssl.broadcast.tunnel.failure.auth";
    // 终端通讯组件广播消息：运营商网络可达
    public static final String ACTION_INTENT_CHECKCARRIERS_SUCCESS = "com.cr.communication.broadcast.checkcarriers.success";
    // 终端通讯组件广播消息：运营商网络不可达
    public static final String ACTION_INTENT_CHECKCARRIERS_FAILURE = "com.cr.communication.broadcast.checkcarriers.failure";


    @Override
    public void onReceive(Context context, Intent intent){
        String data = intent.getStringExtra(ACTION_INTENT_DATA);
        if(intent.getAction().equals(ACTION_INTENT_STARTSERVER_INPROC)){
//            handleMessage(handler, MSG_SHOWLOG, data);
        }else if(intent.getAction().equals(ACTION_INTENT_STARTSERVER_SUCCESS)){
//            handleMessage(handler, MSG_SHOWLOG, "启动服务成功！");
        }else if(intent.getAction().equals(ACTION_INTENT_STARTSERVER_FAILURE)){
//            handleMessage(handler, MSG_SHOWLOG, "启动服务失败！");
        }else if(intent.getAction().equals(ACTION_INTENT_DOWNLOADCFG_SUCCESS)){
//            handleMessage(handler, MSG_SHOWLOG, "下载策略成功！");
        }else if(intent.getAction().equals(ACTION_INTENT_STOPSERVER_SUCCESS)){
//            handleMessage(handler, MSG_SHOWLOG, "停止服务成功！");
        }else if(intent.getAction().equals(ACTION_INTENT_UPGRADE)){
//            handleMessage(handler, MSG_UPGRADE, data);
        }else if(intent.getAction().equals(ACTION_INTENT_NETWORK_CONNECTED)){
            handleMessage(true,"网络已连接");
        }else if(intent.getAction().equals(ACTION_INTENT_NETWORK_DISCONNECTED)){
            handleMessage(false, "网络已断开");
        }else if(intent.getAction().equals(ACTION_INTENT_TUNNEL_CONNECTED)){
//            handleMessage(handler, MSG_SHOWLOG, "隧道已建立");
        }else if(intent.getAction().equals(ACTION_INTENT_TUNNEL_FAILURE) || intent.getAction().equals(ACTION_INTENT_TUNNEL_FAILURE_AUTH)){
//            handleMessage(handler, MSG_SHOWLOG, "隧道已断开");
        }else if(intent.getAction().equals(ACTION_INTENT_CHECKCARRIERS_SUCCESS)){
//            handleMessage(handler, MSG_SHOWLOG, "运营商网络可达");
        }else if(intent.getAction().equals(ACTION_INTENT_CHECKCARRIERS_FAILURE)){
//            handleMessage(handler, MSG_SHOWLOG, "运营商网络不可达");
        }
    }

    private void handleMessage(boolean connect, String result){
        MyTerminalFactory.getSDK().getThreadPool().execute(() -> MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveNetworkChangeHandler.class,connect));
    }
}

package cn.vsx.vc.receiver;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import org.apache.log4j.Logger;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNetworkChangeHandler;
import cn.vsx.vc.prompt.PromptManager;

/**
 * 信号强度监听
 */
public class MyPhoneStateListener extends PhoneStateListener {

    public Logger logger = Logger.getLogger(getClass());
    private Context context;
    //没有网络连接
    private static final int NETWORN_NONE = 0;
    //wifi连接
    private static final int NETWORN_WIFI = 1;
    private static final int UnCon_WIFI = 7;
    //手机网络数据连接类型
    private static final int NETWORN_2G = 2;
    private static final int NETWORN_3G = 3;
    private static final int NETWORN_4G = 4;
    private static final int NETWORN_MOBILE = 5;
    private static final int NETWORN_ETHERNET=6;


    private static final int HANDLE_SHOW_TEMPT=10;
    private static final int HANDLE_CLEAR_TEMPT=11;

    private Handler myHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case HANDLE_SHOW_TEMPT:
                    myHandler.removeMessages(HANDLE_SHOW_TEMPT);
                    PromptManager.getInstance().weakSignal();
                    myHandler.sendEmptyMessageDelayed(HANDLE_CLEAR_TEMPT,60*1000);
                    break;
                case HANDLE_CLEAR_TEMPT:
                    showTempt = true;
                    break;
            }
        }
    };
    private boolean connect = false;
    private boolean showTempt = true;

    public MyPhoneStateListener(Context context){
        this.context = context;
        TerminalFactory.getSDK().registReceiveHandler(receiveNetworkChangeHandler);
    }
    public void onStop(){
        myHandler.removeCallbacksAndMessages(null);
        TerminalFactory.getSDK().unregistReceiveHandler(receiveNetworkChangeHandler);
    }

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        super.onSignalStrengthsChanged(signalStrength);
        int gsmSignalStrength = signalStrength.getGsmSignalStrength();
        int dbm = -113 + 2*gsmSignalStrength;
        logger.info("MyPhoneStateListener--onSignalStrengthsChanged--gsmSignalStrength:"+gsmSignalStrength+"--dbm:"+dbm);
        checkTempt(dbm);
         //获取网络类型
//        int netWorkType = getNetWorkType(context);
//        switch (netWorkType) {
//            case NETWORN_NONE:
//                break;
//            case NETWORN_WIFI:
//                WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
//                checkTempt(wifiManager.getConnectionInfo().getRssi());
//                break;
//            case NETWORN_2G:
//            case NETWORN_3G:
//            case NETWORN_4G:
//            case NETWORN_MOBILE:
//            case NETWORN_ETHERNET:
//                checkTempt(dbm);
//                break;
//                default:
//                    break;
//        }
    }

    public static int getNetWorkType(Context context) {
        //获取系统的网络服务
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //如果当前没有网络
        if (null == connManager) {
            return NETWORN_NONE;
        }
        //获取当前网络类型，如果为空，返回无网络
        NetworkInfo activeNetInfo = connManager.getActiveNetworkInfo();
        if (activeNetInfo == null || !activeNetInfo.isAvailable()) {
            return NETWORN_NONE;
        }
        // 判断是不是连接的是不是wifi
        NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (null != wifiInfo) {
            NetworkInfo.State state = wifiInfo.getState();
            if (null != state) {
                if (state == NetworkInfo.State.CONNECTED
                        || state == NetworkInfo.State.CONNECTING) {
                    return NETWORN_WIFI;
                }else if(state == NetworkInfo.State.DISCONNECTED){
                    return UnCon_WIFI;
                }
            }
        }
        // 如果不是wifi，则判断当前连接的是运营商的哪种网络2g、3g、4g等
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (null != networkInfo) {
            NetworkInfo.State state = networkInfo.getState();
            String strSubTypeName = networkInfo.getSubtypeName();
            if (null != state) {
                if (state == NetworkInfo.State.CONNECTED
                        || state == NetworkInfo.State.CONNECTING) {
                    switch (activeNetInfo.getSubtype()) {
                        //如果是2g类型
                        case TelephonyManager.NETWORK_TYPE_GPRS: // 联通2g
                        case TelephonyManager.NETWORK_TYPE_CDMA: // 电信2g
                        case TelephonyManager.NETWORK_TYPE_EDGE: // 移动2g
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                        case TelephonyManager.NETWORK_TYPE_IDEN:
                            return NETWORN_2G;
                        //如果是3g类型
                        case TelephonyManager.NETWORK_TYPE_EVDO_A: // 电信3g
                        case TelephonyManager.NETWORK_TYPE_UMTS:
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        case TelephonyManager.NETWORK_TYPE_HSDPA:
                        case TelephonyManager.NETWORK_TYPE_HSUPA:
                        case TelephonyManager.NETWORK_TYPE_HSPA:
                        case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        case TelephonyManager.NETWORK_TYPE_EHRPD:
                        case TelephonyManager.NETWORK_TYPE_HSPAP:
                            return NETWORN_3G;
                        //如果是4g类型
                        case TelephonyManager.NETWORK_TYPE_LTE:
                            return NETWORN_4G;
                        default:
                            //中国移动 联通 电信 三种3G制式
                            if (strSubTypeName.equalsIgnoreCase("TD-SCDMA")
                                    || strSubTypeName.equalsIgnoreCase("WCDMA")
                                    || strSubTypeName.equalsIgnoreCase("CDMA2000")) {
                                return NETWORN_3G;
                            } else {
                                return NETWORN_MOBILE;
                            }
                    }
                }
            }
        }
        NetworkInfo EthernetInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
        if (null != EthernetInfo){
            NetworkInfo.State state = EthernetInfo.getState();
            if (null != state) {
                if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
                    return NETWORN_ETHERNET;
                }
            }
        }
        return NETWORN_NONE;
    }

    /**
     * 检查wifi的信号的强度
     * 0   —— (-55)dbm  满格(4格)信号
     * (-55) —— (-70)dbm  3格信号
     * (-70) —— (-85)dbm　2格信号
     * (-85) —— (-100)dbm 1格信号
     * @param dbm
     */
//    private void checkWifiTempt(int dbm) {
//        logger.info("MyPhoneStateListener--checkWifiTempt--dbm:"+dbm);
////        if(dbm<=0&&dbm>-55){
////
////        }else if(dbm<-55&&dbm>-70){
////
////        }else if(dbm<-70&&dbm>-85){
////
////        }else if(dbm<-85&&dbm>-100){
//        if(dbm<-85){
//            logger.info("MyPhoneStateListener--checkWifiTempt:信号比较弱:");
//        }

//    }

    /**
     * 检查数据的信号的强度
     * 1.大于-97时候，等级为SIGNAL_STRENGTH_GREAT，即为4
     * 2.大于-105时候，等级为SIGNAL_STRENGTH_GOOD，即为3
     * 3.大于-110时候，等级为SIGNAL_STRENGTH_MODERATE，即为2
     * 4.大于-120时候，等级为SIGNAL_STRENGTH_POOR，即为1
     * 5.大于-140时候，等级为SIGNAL_STRENGTH_NONE_OR_UNKNOWN，即为0
     * 6.大于-44时候，等级为-1
     * @param dbm
     */
    private void checkTempt(int dbm) {
        logger.info("MyPhoneStateListener--checkSingleTempt--dbm:"+dbm);
        if(connect){
            if (dbm < -100) {
                if(showTempt){
                    myHandler.sendEmptyMessage(HANDLE_SHOW_TEMPT);
                    showTempt = false;
                }
            }
        }
    }

    /**
     * 真实网络
     */
    private ReceiveNetworkChangeHandler receiveNetworkChangeHandler = connected -> {
        connect = connected;
    };
}

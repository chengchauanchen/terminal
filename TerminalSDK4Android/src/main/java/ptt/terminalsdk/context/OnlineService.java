package ptt.terminalsdk.context;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager.WakeLock;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.blankj.utilcode.util.PermissionUtils;

import org.apache.log4j.Logger;

import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.common.UrlParams;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.auth.LoginState;
import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.broadcastreceiver.PTTDownAndUpReceiver;
import ptt.terminalsdk.broadcastreceiver.TickAlarmReceiver;
import ptt.terminalsdk.permission.FloatWindowManager;
import ptt.terminalsdk.service.BluetoothLeService;
import ptt.terminalsdk.service.KeepLiveManager;
import ptt.terminalsdk.tools.AuthUtil;

@SuppressLint("Wakelock")
public class OnlineService extends Service {
    private WakeLock wakeLock;

    private Logger logger = Logger.getLogger(getClass());
    private static final String TAG = "OnlineService---";
    protected PendingIntent tickPendIntent;

    //	private PhoneBroadcastReceiver receiver;
    private PTTDownAndUpReceiver pttDownAndUpReceiver;

    @Override
    public IBinder onBind(Intent intent) {
        return new OnlineServiceBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        logger.info(TAG+"onCreate");
//		this.setTickAlarm();

        wakeLock = MyTerminalFactory.getSDK().getWakeLock();
//		MyTerminalFactory.getSDK().start();

        //锁屏广播
        IntentFilter filterLock = new IntentFilter();
        filterLock.addAction(Intent.ACTION_SCREEN_OFF);
//		filterLock.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(receiverLock, filterLock);

//		//手机电话状态 (响铃、接通、空闲) 空闲时主动向服务器建立连接
//		receiver = new PhoneBroadcastReceiver();
//		IntentFilter filter = new IntentFilter();
//		filter.addAction(Intent.ACTION_CALL);
//		filter.addAction("android.intent.action.PHONE_STATE");
//		registerReceiver(receiver, filter);

        //组呼按钮
        pttDownAndUpReceiver = new PTTDownAndUpReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("BLUE_TOOTH_DOWN");
        //com.zello.ptt.down
        intentFilter.addAction(BluetoothLeService.PTT_DOWN);
        //com.zello.ptt.up
        intentFilter.addAction(BluetoothLeService.PTT_UP);
        intentFilter.addAction("com.sonim.intent.action.PTT_KEY_DOWN");
        intentFilter.addAction("android.intent.action.PTT.down");
        intentFilter.addAction("com.runbo.ptt.key.down");
        intentFilter.addAction("com.yl.ptt.keydown");
        intentFilter.addAction("android.intent.action.OPEN_PTT_BUTTON");
        intentFilter.addAction("BLUE_TOOTH_UP");
        intentFilter.addAction("com.sonim.intent.action.PTT_KEY_UP");
        intentFilter.addAction("android.intent.action.PTT.up");
        intentFilter.addAction("com.runbo.ptt.key.up");
        intentFilter.addAction("com.yl.ptt.keyup");
        intentFilter.addAction("CameraKeyDispatch");
        intentFilter.addAction("SpeakerKeyDispatch");
        intentFilter.addAction("com.ntdj.ptt_down");
        intentFilter.addAction("com.ntdj.ptt_up");
        intentFilter.addAction("com.chivin.action.MEDIA_PTT_DOWN");
        intentFilter.addAction("com.chivin.action.MEDIA_PTT_UP");
        registerReceiver(pttDownAndUpReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        logger.info(TAG+"onDestroy");
//		unregisterReceiver(receiver);
        unregisterReceiver(pttDownAndUpReceiver);
        unregisterReceiver(receiverLock);
        logger.info(TAG+"被杀了，要重新启动");
        Intent intent = new Intent();
        intent.setAction("RESTART_ONLINESERVICE");
        sendBroadcast(intent);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent param, int flags, int startId) {
        logger.info(TAG+"onStartCommand" + param);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            KeepLiveManager.getInstance().setServiceForeground(this);
        }
        try {
            //如果全部更新完成，没有退出，就发送OnlineService开启的广播
//			if(!MyTerminalFactory.getSDK().isExit()){
//				logger.debug("--vsxSDK--发布online_services_started_broadcast");
//				Intent broadcast = new Intent(getResources().getString(R.string.online_services_started_broadcast));
//				sendBroadcast(broadcast);
//
////				logger.debug("--vsxSDK--发布 自动认证 cn.vsx.vc.AUTH_RECEIVER");
////				//自动认证
////				Intent intent = new Intent("cn.vsx.vc.AUTH_RECEIVER");
////				sendBroadcast(intent);
//			}else{
//				logger.debug("--vsxSDK--发布 如果全部更新完成，没有退出");
//			}

            if (param == null) {
                logger.info(TAG+"Intent 为null");
                return START_STICKY;
            }
            String cmd = param.getStringExtra("CMD");
            if (cmd == null) {
                cmd = "";
            }
            logger.info(TAG+"onStartCommand，CMD = " + cmd);
            if (cmd.equals("TICK")) {
                if (wakeLock != null && wakeLock.isHeld() == false) {
                    wakeLock.acquire();
                }
            }
            if (cmd.equals("RESET")) {
                if (wakeLock != null && wakeLock.isHeld() == false) {
                    wakeLock.acquire();
                }
                MyTerminalFactory.getSDK().reConnectToSignalServer();
            }
            if (cmd.equals("TOAST")) {
                String text = param.getStringExtra("TEXT");
                if (text != null && text.trim().length() != 0) {
                    Toast.makeText(this, text, Toast.LENGTH_LONG).show();
                }
            }

            //关联启动 自动认证
            if (TextUtils.equals("AUTH",cmd)) {
                logger.info(TAG+"关联启动 自动认证");
                //开了自启权限,才可进到这个方法
                //TerminalFactory.getSDK().putParam(Params.SDK_PROCESS_STATE,SDKProcessStateEnum.SELF_STARTUP_PERMISSION.name());

                //如果有读写权限就配置log
                if (PermissionUtils.isGranted(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    MyTerminalFactory.getSDK().configLogger();
                } else {
                    //没有存储权限
                    TerminalFactory.getSDK().putParam(Params.SDK_PROCESS_STATE, SDKProcessStateEnum.NO_EXTERNAL_PERMISSION.name());
                    return START_STICKY;
                }
                //如果有悬浮窗权限，把事件service启动起来
                if (!FloatWindowManager.getInstance().checkPermission(this)) {
                    logger.info(TAG+"没有获取到悬浮窗权限");
                    TerminalFactory.getSDK().putParam(Params.SDK_PROCESS_STATE, SDKProcessStateEnum.NO_WINDOW_PERMISSION.name());
                    return START_STICKY;
                } else {
                    TerminalFactory.getSDK().putParam(Params.SDK_PROCESS_STATE, SDKProcessStateEnum.HAVE_WINDOW_PERMISSION.name());
                    logger.info(TAG+"startHandlerService");
                    BaseApplication.getApplication().startHandlerService();
                }
                BaseApplication.getApplication().setAppLogined();
                BaseApplication.getApplication().startPromptManager();
                LoginState loginState = TerminalFactory.getSDK().getAuthManagerTwo().getLoginStateMachine().getCurrentState();
                Log.e("--vsxSDK--", "loginState:" + loginState);
                logger.info(TAG+"loginState:" + loginState);
                if (TerminalFactory.getSDK().getAuthManagerTwo().getLoginStateMachine().getCurrentState() == null ||
                        TerminalFactory.getSDK().getAuthManagerTwo().getLoginStateMachine().getCurrentState() == LoginState.IDLE) {
                    if (checkCanStartAuth()) {
                        logger.info(TAG+"startAuth");
                        startAuth();
                    } else {
                        TerminalFactory.getSDK().putParam(Params.SDK_PROCESS_STATE, SDKProcessStateEnum.NO_PHONE_TYPE.name());
                    }
                } else {
                    TerminalFactory.getSDK().putParam(Params.SDK_PROCESS_STATE, SDKProcessStateEnum.SUCCESS_STATE.name());
                }
            }
            return START_STICKY;
        } catch (Exception e) {
            logger.info(TAG+"在线服务启动过程中出现异常", e);
            return START_STICKY;
        }
    }

    /**
     * 判断是否可以在这里开始认证
     *
     * @return
     */
    private boolean checkCanStartAuth() {
        logger.info(TAG+"判断是否可以在这里开始认证");
        String deviceType = TerminalFactory.getSDK().getParam(UrlParams.TERMINALMEMBERTYPE);
        return (TerminalMemberType.valueOf(deviceType).getCode() == TerminalMemberType.TERMINAL_PHONE.getCode());
    }

    /**
     * 设置滴答警报声
     */
    protected void setTickAlarm() {
        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, TickAlarmReceiver.class);
        int requestCode = 0;
        tickPendIntent = PendingIntent.getBroadcast(this,
                requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //小米2s的MIUI操作系统，目前最短广播间隔为5分钟，少于5分钟的alarm会等到5分钟再触发！2014-04-28
        long triggerAtTime = System.currentTimeMillis();
        int interval = 300 * 1000;
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtTime, interval, tickPendIntent);
    }

    private BroadcastReceiver receiverLock = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                Log.d("OnlineService", "屏幕锁屏了，将OnlineService放到前台进程");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    KeepLiveManager.getInstance().setServiceForeground(OnlineService.this);
                }
            }
        }
    };

    public class OnlineServiceBinder extends Binder {
        public OnlineService getService() {
            return OnlineService.this;
        }
    }

    private void startAuth() {
        logger.info(TAG+"TAG+startAuth");
        AuthUtil.setOauthInfo(getApplicationContext());

        String authUrl = TerminalFactory.getSDK().getParam(Params.AUTH_URL, "");
        if (android.text.TextUtils.isEmpty(authUrl)) {
            String type = TerminalFactory.getSDK().getParam(UrlParams.TERMINALMEMBERTYPE);
            if(TerminalMemberType.valueOf(type).getCode() == TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.getCode()){
                TerminalFactory.getSDK().getAuthManagerTwo().setChangeServer();
            }
            String[] defaultAddress = TerminalFactory.getSDK().getAuthManagerTwo().getDefaultAddress();
            if (defaultAddress.length >= 2) {
                int resultCode = TerminalFactory.getSDK().getAuthManagerTwo().startAuth(defaultAddress[0], defaultAddress[1]);
                if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                } else {
                    //状态机没有转到正在认证，说明已经在状态机中了，不用处理
//                    Log.e("--vsx--AuthService--", "状态机没有转到正在认证，说明已经在状态机中了，不用处理");
                    logger.info(TAG+"AuthService--状态机没有转到正在认证，说明已经在状态机中了，不用处理");
                }
                TerminalFactory.getSDK().putParam(Params.SDK_PROCESS_STATE, SDKProcessStateEnum.SUCCESS_STATE.name());
            } else {
                logger.info(TAG+"AuthService--没有注册服务地址，去探测地址");
                //没有注册服务地址，去探测地址
                TerminalFactory.getSDK().getAuthManagerTwo().checkRegistIp();
                TerminalFactory.getSDK().putParam(Params.SDK_PROCESS_STATE, SDKProcessStateEnum.NO_AUTH_URL.name());
            }
        } else {
            //有注册服务地址，去认证
//            Log.e("OnlineService", "startAuth");
            logger.info(TAG+"OnlineService--startAuth");
            TerminalFactory.getSDK().getAuthManagerTwo().startAuth(TerminalFactory.getSDK().getParam(Params.REGIST_IP, ""), TerminalFactory.getSDK().getParam(Params.REGIST_PORT, ""));
            TerminalFactory.getSDK().putParam(Params.SDK_PROCESS_STATE, SDKProcessStateEnum.SUCCESS_STATE.name());
        }
    }
}

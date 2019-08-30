package cn.vsx.vc.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.Process;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.Window;
import android.view.WindowManager;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import org.apache.log4j.Logger;
import org.easydarwin.push.BITMediaStream;
import org.easydarwin.push.EasyPusher;
import org.easydarwin.push.InitCallback;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import cn.vsx.hamster.common.TempGroupType;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState;
import cn.vsx.hamster.terminalsdk.model.RecorderBindBean;
import cn.vsx.hamster.terminalsdk.model.RecorderBindTranslateBean;
import cn.vsx.hamster.terminalsdk.model.RecorderServerBean;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveExitHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveForceReloginHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveLogFileUploadCompleteHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberAboutTempGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberDeleteHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyMemberKilledHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyZfyBoundPhoneMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRecorderLoginHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRequestRecorderBindHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseZfyBoundPhoneByRequestMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseZfyBoundPhoneMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveVolumeOffCallHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.dialog.ExitAccountDialog;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.receive.Actions;
import cn.vsx.vc.receive.IBroadcastRecvHandler;
import cn.vsx.vc.receive.RecvCallBack;
import cn.vsx.vc.receiveHandle.ReceiverAudioButtonEventHandler;
import cn.vsx.vc.receiveHandle.ReceiverChangeServerHandler;
import cn.vsx.vc.receiveHandle.ReceiverFragmentClearHandler;
import cn.vsx.vc.receiveHandle.ReceiverFragmentShowHandler;
import cn.vsx.vc.receiveHandle.ReceiverPhotoButtonEventHandler;
import cn.vsx.vc.receiveHandle.ReceiverStopAllBusniessHandler;
import cn.vsx.vc.receiveHandle.ReceiverStopBusniessHandler;
import cn.vsx.vc.receiveHandle.ReceiverVideoButtonEventHandler;
import cn.vsx.vc.receiver.HeadsetPlugReceiver;
import cn.vsx.vc.utils.ActivityCollector;
import cn.vsx.vc.utils.BITDialogUtil;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.NfcUtil;
import cn.vsx.vc.utils.SystemUtil;
import cn.vsx.vc.utils.ToastUtil;
import cn.vsx.vc.utils.VolumeToastUitl;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.audio.CheckMyPermission;
import ptt.terminalsdk.manager.recordingAudio.AudioRecordStatus;
import ptt.terminalsdk.tools.DeleteData;
import ptt.terminalsdk.tools.DialogUtil;

public abstract class BaseActivity extends AppCompatActivity implements RecvCallBack, Actions, NfcUtil.OnReadListener {

    private AudioManager audioManager;
    private HeadsetPlugReceiver headsetPlugReceiver;
    private PowerManager.WakeLock wakeLock;
    private PowerManager.WakeLock wakeLockScreen;
    protected PowerManager.WakeLock wakeLockComing;

    private SensorManager sensorManager;// 传感器管理对象,调用距离传感器，控制屏幕
    private PowerManager powerManager;
    public static final String TAG = "BaseActivity---";

    private long videoKeyLongPressStartTime = 0;
    private long audioKeyLongPressStartTime = 0;
    private long menuKeyLongPressStartTime = 0;
    //
    private boolean videoKeyIsLongPress = false;
    private boolean audioKeyIsLongPress = false;
    private boolean menuKeyIsLongPress = false;
    //长按时间
    private static final long LONG_PRESS_TIME = 1500;
    //登录延时时间
    public static final int LOGIN_DELAY_TIME = 2 * 1000;

    private Timer timer = new Timer();
    private long currentTime;

    private NfcUtil mNFCUtil;

    protected boolean onRecordAudioDenied;
    protected boolean onLocationDenied;
    protected boolean onCameraDenied;
    public static final int REQUEST_PERMISSION_SETTING = 1235;
    private int pushcount;

    private AlertDialog pushDialog;
    private AlertDialog logDialog;
    private ExitAccountDialog exitAccountDialog;
    protected boolean isFristLogin = true;

    @Override
    protected void onStart() {
        super.onStart();
        initBaseListener();
    }

    /**
     * 注册监听
     */
    private void initBaseListener() {
        MyTerminalFactory.getSDK().registReceiveHandler(receiveExitHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyMemberKilledHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveMemberDeleteHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveForceReloginHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveLogFileUploadCompleteHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveRecorderLoginHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveRequestRecorderBindHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseZfyBoundPhoneByRequestMessageHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseZfyBoundPhoneMessageHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyZfyBoundPhoneMessageHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiverChangeServerHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveMemberAboutTempGroupHandler);//临时组相关的通知
        setPttVolumeChangedListener();
    }

    //成员被删除了,销毁锁屏
    private ReceiveMemberDeleteHandler receiveMemberDeleteHandler = new ReceiveMemberDeleteHandler() {
        @Override
        public void handler() {
            myHandler.post(() -> {
                MyApplication.instance.stopPTTButtonEventService();
                MyTerminalFactory.getSDK().stop();
            });
        }
    };


    private Handler myHandler = new Handler(Looper.getMainLooper());
    public Logger logger = Logger.getLogger(getClass());

    /**
     * 组成员遥毙消息
     */
    protected ReceiveNotifyMemberKilledHandler receiveNotifyMemberKilledHandler = forbid -> {
        logger.error(TAG + "收到遥毙，此时forbid状态为：" + forbid);
        if (forbid) {
            myHandler.post(() -> {
                TerminalFactory.getSDK().putParam(Params.IS_FIRST_LOGIN, true);
                TerminalFactory.getSDK().putParam(Params.IS_UPDATE_DATA, true);
                MyApplication.instance.stopPTTButtonEventService();
                forbid();
            });
        }
    };

    protected ReceiveExitHandler receiveExitHandler = (msg, isExit) -> {
        if (isExit) {
            ToastUtil.showToast(BaseActivity.this, msg);
            myHandler.postDelayed(() -> {
                PromptManager.getInstance().stop();
                for (Activity activity : ActivityCollector.getAllActivity().values()) {
                    activity.finish();
                }
                TerminalFactory.getSDK().putParam(Params.IS_FIRST_LOGIN, true);
                TerminalFactory.getSDK().putParam(Params.IS_UPDATE_DATA, true);
                MyApplication.instance.isClickVolumeToCall = false;
                MyApplication.instance.isPttPress = false;
                MyApplication.instance.stopPTTButtonEventService();
                //停止上报或者观看的页面
                MyTerminalFactory.getSDK().stop();
                Process.killProcess(Process.myPid());
            }, 2000);
        }
    };

    /**
     * 日志上传是否成功的消息
     */
    private ReceiveLogFileUploadCompleteHandler receiveLogFileUploadCompleteHandler = (resultCode, type) -> myHandler.post(() -> {
        if ("log".equals(type)) {
            if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                ToastUtil.toast(this, getString(R.string.text_log_upload_success_thanks));
            } else {
                ToastUtil.showToast(getString(R.string.text_log_upload_fail_please_try_later), this);
            }
        }
    });

    /**
     * 通知登录
     */
    private ReceiveRecorderLoginHandler receiveRecorderLoginHandler = () -> myHandler.post(() -> checkLogin(false, 0));

    /**
     * 发送绑定操作
     */
    private ReceiveRequestRecorderBindHandler receiveRequestRecorderBindHandler = (errorCode, errorDesc) -> myHandler.post(() -> {
        ptt.terminalsdk.manager.Prompt.PromptManager.getInstance().bindFail();
        ToastUtil.showToast(BaseActivity.this, errorDesc);
    });

    /**
     * 发送绑定请求的响应
     */
    private ReceiveResponseZfyBoundPhoneByRequestMessageHandler receiveResponseZfyBoundPhoneByRequestMessageHandler = (resultCode, resultDesc) -> {
        if (resultCode == BaseCommonCode.SUCCESS_CODE) {
            ToastUtil.showToast(BaseActivity.this, getString(R.string.text_request_bind_success));
        } else {
            ptt.terminalsdk.manager.Prompt.PromptManager.getInstance().bindFail();
            ToastUtil.showToast(BaseActivity.this, (TextUtils.isEmpty(resultDesc)) ? getString(R.string.text_request_bind_fail) : resultDesc);
        }
    };

    /**
     * 接收到绑定需要请求的参数
     */
    private ReceiveResponseZfyBoundPhoneMessageHandler receiveResponseZfyBoundPhoneMessageHandler = (memberUuid, groupNo, isTempGroup, alarmNo) -> {
        //清空fragment
        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverFragmentClearHandler.class);
        //判断是否是绑定的是同一个账号，如果是同一个账号，停止一切业务，切组
        RecorderBindBean bean = DataUtil.getRecorderBindBean();
        boolean isChange = !(bean != null && (TextUtils.equals(bean.getBindUuid(), memberUuid)));
        int currentGroupId = TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);
        //绑定成功之后，执法记录仪收到通知，退出默认账号，登录绑定账号
        DataUtil.saveRecorderBindBean(new RecorderBindBean(memberUuid, groupNo, isTempGroup, alarmNo));
        //切换到绑定账号
        if (isChange) {
            TerminalFactory.getSDK().getTimer().schedule(new TimerTask() {
                @Override
                public void run() {
                    changeAccount();
                }
            }, 1000);
        } else if (currentGroupId != groupNo) {
            //停止一切业务
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverStopBusniessHandler.class);
            //切组
            //判断是否是临时组
            if (isTempGroup) {
                //临时组,发送加入临时组的通知
                TerminalFactory.getSDK().getGroupManager().joinInWarningGroup(groupNo);
            } else {
                //普通组，只需要转组
                TerminalFactory.getSDK().getGroupManager().changeGroup(groupNo);
            }
        }
    };
    /**
     * 接收到绑定/解绑的结果
     */
    private ReceiveNotifyZfyBoundPhoneMessageHandler receiveNotifyZfyBoundPhoneMessageHandler = (isBound) -> {
        if (!isBound) {
            if (DataUtil.getRecorderBindBean() != null) {
                ptt.terminalsdk.manager.Prompt.PromptManager.getInstance().unbinding();
                ToastUtil.showToast(BaseActivity.this, getString(R.string.text_unbind_success));
                //清空绑定信息
                DataUtil.clearRecorderBindBean();
                //切换到绑定账号
                TerminalFactory.getSDK().getTimer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        changeAccount();
                    }
                }, 1000);

            }
        } else {
            if (DataUtil.getRecorderBindBean() != null) {
                ToastUtil.showToast(BaseActivity.this, getString(R.string.text_bind_success));
                RecorderBindBean bindBean = DataUtil.getRecorderBindBean();
                if (TextUtils.isEmpty(bindBean.getWarningId())) {
                    ptt.terminalsdk.manager.Prompt.PromptManager.getInstance().bindPoliceSuccess();
                } else {
                    ptt.terminalsdk.manager.Prompt.PromptManager.getInstance().bindPoliceAlertSuccess();
                }
            }
        }
    };
    /**
     * 切换环境
     */
    private ReceiverChangeServerHandler receiverChangeServerHandler = (ip, port, showMessage) -> myHandler.post(() -> {
        ToastUtil.showToast(BaseActivity.this, getString(R.string.text_change_server));
        //保存ip和port
        DataUtil.saveRecorderServerBean(new RecorderServerBean(ip, port, getString(R.string.text_change_server)));
        //清空fragment
        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverFragmentClearHandler.class);
        //退出账号，清空绑定账号
        DataUtil.clearRecorderBindBean();

        //清空认证url
        TerminalFactory.getSDK().putParam(Params.AUTH_URL, "");
        //切换到绑定账号
        TerminalFactory.getSDK().getTimer().schedule(new TimerTask() {
            @Override
            public void run() {
                changeAccount();
            }
        }, 1000);
    });

    static Method findViewBinderForClassMethod;

    static {
        Method[] ms = ButterKnife.class.getDeclaredMethods();
        for (Method m : ms) {
            if (m.getName().equals("findViewBinderForClass")) {
                findViewBinderForClassMethod = m;
                findViewBinderForClassMethod.setAccessible(true);
                break;
            }
        }
    }

    @SuppressLint("InvalidWakeLockTag")
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 没有标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //判断横竖屏
        setRequestedOrientation(SystemUtil.isScreenLandscape(this) ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        //透明状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(getLayoutResId());
        logger.info(TAG + "onCreate:" + MyApplication.instance.mAppStatus);
        // 判断如果被强杀，就回到 MainActivity 中去，否则可以初始化
        if (MyApplication.instance.mAppStatus == Constants.FORCE_KILL) {
            logger.info(TAG + "应用被强杀了，重新去注册界面");
            //重走应用流程
            protectApp();
        } else {
            ButterKnife.bind(this);
            try {
                ButterKnife.ViewBinder e = (ButterKnife.ViewBinder) findViewBinderForClassMethod.invoke(null, BaseActivity.class);
                if (e != null) {
                    e.bind(ButterKnife.Finder.ACTIVITY, this, this);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

            regBroadcastRecv(ACT_SHOW_FULL_SCREEN, ACT_DISMISS_FULL_SCREEN, STOP_INDIVDUALCALL_SERVEIC);
            ActivityCollector.addActivity(this, getClass());

            //nfc初始化
            mNFCUtil = new NfcUtil(this);
            mNFCUtil.setOnReadListener(this);

            initData();
            initView();
            initListener();

            powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            //距离感应器的电源锁
            wakeLock = powerManager.newWakeLock(32, "wakeLock");
            //插入耳机时的电源锁
            wakeLockScreen = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
                    | PowerManager.SCREEN_DIM_WAKE_LOCK, "wakeLock");
            //距离感应器的电源锁
            wakeLockComing = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "wakeLock");

            if (wakeLockComing != null) {
                wakeLockComing.acquire();
            }
            //注册页面提示nfc是否打开
//            if(BaseActivity.this instanceof RegistNFCActivity && mNFCUtil!=null){
//                mNFCUtil.nfcCheck(this);
//            }
        }
    }

    /**
     * 注册耳机插入拔出的监听
     */
    private void registerHeadsetPlugReceiver() {
        headsetPlugReceiver = new HeadsetPlugReceiver(sensorManager,
                sensorEventListener, wakeLockScreen);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.HEADSET_PLUG");
        registerReceiver(headsetPlugReceiver, filter);
    }

    /**
     * 注销耳机插入拔出的监听
     */
    private void unregisterHeadsetPlugReceiver() {
        if (headsetPlugReceiver != null) {
            unregisterReceiver(headsetPlugReceiver);
        }
    }

    protected void protectApp() {
        // 重新走应用的流程是一个正确的做法，因为应用被强杀了还保存 Activity 的栈信息是不合理的
//        Intent intent = new Intent(this, RegistNFCActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(intent);
//        finish();
    }

    /**
     * 获取当前界面的布局
     */
    public abstract int getLayoutResId();

    /**
     * 初始化界面
     */
    public abstract void initView();

    /**
     * 给控件添加监听
     */
    public abstract void initListener();

    /**
     * 初始化数据 给控件填充内容
     */
    public abstract void initData();

    /**
     * 子类activity处理自己的destroy()
     */
    public abstract void doOtherDestroy();

    protected abstract void startLiveService();

    protected abstract void requestStartLive();

    protected abstract void finishVideoLive();


    @Override
    protected void onResume() {
        super.onResume();
        registerHeadsetPlugReceiver();
        //nfc
        if (mNFCUtil != null && mNFCUtil.getmNfcAdapter() != null) {
            mNFCUtil.getmNfcAdapter().enableForegroundDispatch(this, mNFCUtil.getmPendingIntent(), mNFCUtil.getmIntentFilter(), mNFCUtil.getmTechList());
            mNFCUtil.proccessIntent(getIntent());
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterHeadsetPlugReceiver();
        if (mNFCUtil != null && mNFCUtil.getmNfcAdapter() != null) {
            mNFCUtil.getmNfcAdapter().disableForegroundDispatch(this);
        }
    }


    @Override
    protected void onDestroy() {
        try {
//            dismissProgressDialog();
            doOtherDestroy();
            ButterKnife.unbind(this);//解除绑定，官方文档只对fragment做了解绑
            ActivityCollector.removeActivity(this);

            MyTerminalFactory.getSDK().unregistReceiveHandler(receiveExitHandler);
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMemberDeleteHandler);
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyMemberKilledHandler);
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiveForceReloginHandler);
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiveLogFileUploadCompleteHandler);
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiveRecorderLoginHandler);
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiveRequestRecorderBindHandler);
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseZfyBoundPhoneByRequestMessageHandler);
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseZfyBoundPhoneMessageHandler);
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyZfyBoundPhoneMessageHandler);
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiverChangeServerHandler);
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMemberAboutTempGroupHandler);//临时组相关的通知

            if (mBroadcastReceiv != null) {
                LocalBroadcastManager.getInstance(BaseActivity.this).unregisterReceiver(
                        mBroadcastReceiv);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }


    /**
     * 广播
     */
    protected IBroadcastRecvHandler mBroadcastReceiv;
    /**
     * 广播过滤
     */
    protected IntentFilter mReceivFilter;

    /**
     * 注册广播
     */
    public void regBroadcastRecv(String... actions) {
        if (mBroadcastReceiv == null || mReceivFilter == null) {
            mBroadcastReceiv = new IBroadcastRecvHandler(this);
            mReceivFilter = new IntentFilter();
        }
        if (actions != null) {
            for (String act : actions) {
                mReceivFilter.addAction(act);
            }
        }
        LocalBroadcastManager.getInstance(BaseActivity.this).registerReceiver(
                mBroadcastReceiv, mReceivFilter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
//			if (action.equals(HIDE_KEY)) {
//				logger.info("sjl_收到服务里的广播，收起键盘");
////				hideSoftKeyBoard();
//			}
        }
    }

    /**
     * 距离感应器
     */
    @SuppressLint("Wakelock")
    private SensorEventListener sensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            float[] its = event.values;
            if (its != null && event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                // 经过测试，当手贴近距离感应器的时候its[0]返回值为0.0，当手离开时返回1.0
                if (its[0] == 0.0) {// 贴近手机
                    logger.info(TAG + "hands up in calling activity贴近手机");
                    if (!wakeLock.isHeld()) {
                        wakeLock.acquire();// 申请设备电源锁
                    }
                } else {// 远离手机
                    logger.info(TAG + "hands moved in calling activity远离手机");
                    if (wakeLock.isHeld()) {
                        wakeLock.release(); // 释放设备电源锁
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    // TODO: 2019/7/12  按键说明  285，286，287 是比特星DSJ-BITI7A1型号的按键适配

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        logger.info(TAG + "onKeyDown:event.getKeyCode():" + keyCode + "--event:" + event);
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case 286:
                //摄像按键
//                dismissDialog();
                if (event.getRepeatCount() == 0) {
                    videoKeyLongPressStartTime = System.currentTimeMillis();
                } else {
                    if (videoKeyLongPressStartTime != 0 && ((System.currentTimeMillis() - videoKeyLongPressStartTime) >= LONG_PRESS_TIME) && !videoKeyIsLongPress) {
                        videoKeyIsLongPress = true;
                        MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiverVideoButtonEventHandler.class, true);
                    }
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
//                dismissDialog();
                //音量下键和录音键
                if (event.getRepeatCount() == 0) {
                    audioKeyLongPressStartTime = System.currentTimeMillis();
                } else {
                    if (audioKeyLongPressStartTime != 0 && System.currentTimeMillis() - audioKeyLongPressStartTime >= LONG_PRESS_TIME && !audioKeyIsLongPress) {
                        audioKeyIsLongPress = true;
                        MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiverAudioButtonEventHandler.class, true);
                    }
                }
                return true;

            case 285:
                //菜单键和上传日志
                if (event.getRepeatCount() == 0) {
                    menuKeyLongPressStartTime = System.currentTimeMillis();
                } else {
                    if (menuKeyLongPressStartTime != 0 && System.currentTimeMillis() - menuKeyLongPressStartTime >= LONG_PRESS_TIME && !menuKeyIsLongPress) {
                        menuKeyIsLongPress = true;
                        //长按是上传日志
                        if (TerminalFactory.getSDK().getAuthManagerTwo().isOnLine()) {
                            uploadLog();
                        } else {
                            ToastUtil.showToast(BaseActivity.this, getString(R.string.text_no_login_can_not_upload_log));
                        }
                    }
                }
                return true;


        }
        // 为true,则其它后台按键处理再也无法处理到该按键，为false,则其它后台按键处理可以继续处理该按键事件
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        logger.info(TAG + "onKeyUp:event.getKeyCode():" + keyCode + "--event:" + event);
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                // 增大音量
                if (System.currentTimeMillis() - lastVolumeUpTime > 500) {
                    MyTerminalFactory.getSDK().getAudioProxy().volumeUp();
                    if (MyTerminalFactory.getSDK().getAudioProxy().getVolume() > 0) {
                        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveVolumeOffCallHandler.class, false, 1);
                    } else {
                        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveVolumeOffCallHandler.class, true, 1);
                    }
                    lastVolumeUpTime = System.currentTimeMillis();
                }
                //显示音量的Toast
                VolumeToastUitl.showToastWithImg(this, MyTerminalFactory.getSDK().getAudioProxy().getVolume() + "%");
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (!audioKeyIsLongPress) {
                    if (MyTerminalFactory.getSDK().getRecordingAudioManager().getStatus() == AudioRecordStatus.STATUS_STOPED) {
                        // 减小音量
                        if (System.currentTimeMillis() - lastVolumeDownTime > 500) {
                            MyTerminalFactory.getSDK().getAudioProxy().volumeDown();
                            if (MyTerminalFactory.getSDK().getAudioProxy().getVolume() > 0) {
                                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveVolumeOffCallHandler.class, false, 1);
                            } else {
                                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveVolumeOffCallHandler.class, true, 1);
                            }
                            lastVolumeDownTime = System.currentTimeMillis();
                        }
                        //显示音量的Toast
                        VolumeToastUitl.showToastWithImg(this, MyTerminalFactory.getSDK().getAudioProxy().getVolume() + "%");
                    } else {
                        MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiverAudioButtonEventHandler.class, false);
                    }
                }
                audioKeyIsLongPress = false;
                return true;
            case 287:
                //录音按键
                if (MyTerminalFactory.getSDK().getRecordingAudioManager().getStatus() == AudioRecordStatus.STATUS_STOPED) {
                    MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiverAudioButtonEventHandler.class, true);
                } else {
                    MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiverAudioButtonEventHandler.class, false);
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case 286:
                //摄像按键
//                dismissDialog();
                if (!videoKeyIsLongPress) {
                    MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiverVideoButtonEventHandler.class, false);
                }
                videoKeyIsLongPress = false;
                return true;
            case KeyEvent.KEYCODE_CAMERA:
                //拍照按键
//                dismissDialog();
                MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiverPhotoButtonEventHandler.class);
                return true;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                //数据上传按键
//                dismissDialog();
                if (TerminalFactory.getSDK().getAuthManagerTwo().isOnLine()) {
                    uploadLog();
                } else {
                    ToastUtil.showToast(BaseActivity.this, getString(R.string.text_no_login_can_not_upload_log));
                }
                return true;

            case KeyEvent.KEYCODE_DPAD_UP:
                //ok按键
//                dismissDialog();
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverFragmentShowHandler.class, Constants.FRAGMENT_TAG_MENU);
                return true;
            case 285:
                //短按是菜单键
                if (!menuKeyIsLongPress) {
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverFragmentShowHandler.class, Constants.FRAGMENT_TAG_MENU);
                }
                menuKeyIsLongPress = false;
                return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    //音量上下键为PTT按钮状态改变的监听接口
    private OnPTTVolumeBtnStatusChangedListener onPTTVolumeBtnStatusChangedListener;

    public interface OnPTTVolumeBtnStatusChangedListener {
        void onPTTVolumeBtnStatusChange(GroupCallSpeakState groupCallSpeakState);
    }

    public void setOnPTTVolumeBtnStatusChangedListener(OnPTTVolumeBtnStatusChangedListener onPTTVolumeBtnStatusChangedListener) {
        this.onPTTVolumeBtnStatusChangedListener = onPTTVolumeBtnStatusChangedListener;
    }

    boolean isDown = true;
    long lastVolumeUpTime = 0;
    long lastVolumeDownTime = 0;


    /**
     * PTT实体按钮发起组呼
     */
    private int downTimes = 0;

    private void groupCallByPTTKey(KeyEvent event) {
        switch (event.getAction()) {
            case KeyEvent.ACTION_DOWN:
                downTimes++;
                logger.info(TAG + "用音量键做ptt按钮，音量键按下时：downTimes：" + downTimes + "    pttPress状态：" + MyApplication.instance.isPttPress);
                if (downTimes == 1 && !MyApplication.instance.folatWindowPress && !MyApplication.instance.isPttPress) {

                    MyApplication.instance.isClickVolumeToCall = true;
                    if (onPTTVolumeBtnStatusChangedListener != null) {
                        logger.info(TAG + "用音量键做ptt按钮，音量键按下时：ptt的当前状态是：" + MyApplication.instance.getGroupSpeakState());
                        onPTTVolumeBtnStatusChangedListener.onPTTVolumeBtnStatusChange(MyApplication.instance.getGroupSpeakState());
                    }
                    MyApplication.instance.volumePress = true;
                    pttCalling = true;
                }
                break;
            case KeyEvent.ACTION_UP:
                logger.info(TAG + "音量的抬起事件 " + MyApplication.instance.volumePress + (MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.WAITING) +
                        (MyApplication.instance.getGroupListenenState() == GroupCallListenState.LISTENING));
                if (MyApplication.instance.volumePress) {
                    MyApplication.instance.isClickVolumeToCall = false;
                    if (onPTTVolumeBtnStatusChangedListener != null) {
                        onPTTVolumeBtnStatusChangedListener.onPTTVolumeBtnStatusChange(GroupCallSpeakState.END);
                    }
                    MyApplication.instance.volumePress = false;
                }
                downTimes = 0;
                pttCalling = false;
                break;
        }
    }

    private boolean pttCalling;

    private void setPttVolumeChangedListener() {
        SharedPreferences account = MyTerminalFactory.getSDK().getAccount();
        if (account != null) {
            account.registerOnSharedPreferenceChangeListener(listener);
        }
    }

    private SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if ((key.equals(Params.VOLUME_UP) && !MyTerminalFactory.getSDK().getParam(Params.VOLUME_UP, false) && pttCalling)
                    || (key.equals(Params.VOLUME_DOWN) && !MyTerminalFactory.getSDK().getParam(Params.VOLUME_DOWN, false) && pttCalling)) {
                if (onPTTVolumeBtnStatusChangedListener != null) {
                    onPTTVolumeBtnStatusChangedListener.onPTTVolumeBtnStatusChange(GroupCallSpeakState.END);
                }
                MyApplication.instance.isClickVolumeToCall = false;
                MyApplication.instance.volumePress = false;
            }
        }
    };

    /**
     * 必须要有录音和相机的权限，APP才能去视频页面
     */
    protected void judgePermission() {
        //6.0以下判断相机权限
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (!SystemUtil.cameraIsCanUse()) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CheckMyPermission.REQUEST_CAMERA);
            } else {
                startLiveService();
                //登录
                checkLogin(true, LOGIN_DELAY_TIME);
            }
        } else {
            if (CheckMyPermission.selfPermissionGranted(this, Manifest.permission.RECORD_AUDIO)) {
                if (CheckMyPermission.selfPermissionGranted(this, Manifest.permission.CAMERA)) {
                    if (!CheckMyPermission.selfPermissionGranted(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        CheckMyPermission.permissionPrompt(this, Manifest.permission.ACCESS_FINE_LOCATION);
                    } else {
                        startLiveService();
                        //登录
                        checkLogin(true, LOGIN_DELAY_TIME);
                        //权限打开之后判断是否需要上传位置信息，这种情况是之前没有打开权限使得登录或者成员信息改变的时候不能上传位置信息，到了主页面才申请权限的情况
                        MyTerminalFactory.getSDK().getLocationManager().requestLocationByJudgePermission();
                    }
                } else {
                    CheckMyPermission.permissionPrompt(this, Manifest.permission.CAMERA);
                }
            } else {
                //如果权限被拒绝，申请下一个权限
                if (onRecordAudioDenied) {
                    if (CheckMyPermission.selfPermissionGranted(this, Manifest.permission.CAMERA)) {
                        startLiveService();
                        //登录
                        checkLogin(true, LOGIN_DELAY_TIME);
//                        myHandler.postDelayed(this::autoStartLive,500);
                        if (!CheckMyPermission.selfPermissionGranted(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                            CheckMyPermission.permissionPrompt(this, Manifest.permission.ACCESS_FINE_LOCATION);
                        } else {
                            //权限打开之后判断是否需要上传位置信息，这种情况是之前没有打开权限使得登录或者成员信息改变的时候不能上传位置信息，到了主页面才申请权限的情况
                            MyTerminalFactory.getSDK().getLocationManager().requestLocationByJudgePermission();
                        }
                    } else {
                        if (onCameraDenied) {
                            if (!CheckMyPermission.selfPermissionGranted(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                                if (!onLocationDenied) {
                                    CheckMyPermission.permissionPrompt(this, Manifest.permission.ACCESS_FINE_LOCATION);
                                }
                            } else {
                                //权限打开之后判断是否需要上传位置信息，这种情况是之前没有打开权限使得登录或者成员信息改变的时候不能上传位置信息，到了主页面才申请权限的情况
                                MyTerminalFactory.getSDK().getLocationManager().requestLocationByJudgePermission();
                            }
                        } else {
                            CheckMyPermission.permissionPrompt(this, Manifest.permission.CAMERA);
                        }
                    }
                } else {
                    CheckMyPermission.permissionPrompt(this, Manifest.permission.RECORD_AUDIO);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CheckMyPermission.REQUEST_RECORD_AUDIO:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onRecordAudioDenied = true;
                    judgePermission();
                } else {
                    onRecordAudioDenied = false;
                    permissionDenied(Manifest.permission.RECORD_AUDIO);
                }
                break;
            case CheckMyPermission.REQUEST_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onCameraDenied = true;
                    judgePermission();
                } else {
                    onCameraDenied = false;
                    permissionDenied(Manifest.permission.CAMERA);
                }
                break;
            case CheckMyPermission.REQUEST_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onLocationDenied = true;
                    judgePermission();
                } else {
                    onLocationDenied = false;
                    permissionDenied(Manifest.permission.ACCESS_FINE_LOCATION);
                }
                break;
            default:
                break;
        }
    }

    private void permissionDenied(int requestCode) {
        if (requestCode == CheckMyPermission.REQUEST_RECORD_AUDIO) {
            ToastUtil.showToast(this, "录制音频权限未打开，语音功能将不能使用。");
        } else if (requestCode == CheckMyPermission.REQUEST_CAMERA) {
            ToastUtil.showToast(this, "相机未打开，图像上报功能将不能使用。");
        } else if (requestCode == CheckMyPermission.REQUEST_LOCATION) {
            ToastUtil.showToast(this, "位置信息权限未打开，定位功能将不能使用。");
        }
    }

    private void permissionDenied(final String permissionName) {
        new DialogUtil() {
            @Override
            public CharSequence getMessage() {
                return CheckMyPermission.getDesForPermission(permissionName);
            }

            @Override
            public Context getContext() {
                return BaseActivity.this;
            }

            @Override
            public void doConfirmThings() {
                //点击确定时跳转到设置界面
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
            }

            @Override
            public void doCancelThings() {
                BaseActivity.this.finish();
            }
        }.showDialog();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PERMISSION_SETTING) {
            // 从设置界面返回时再判断权限是否开启
            judgePermission();
        }
    }

    /**
     * 登录
     */
    protected void checkLogin(boolean show, int time) {
        String imei = MyTerminalFactory.getSDK().getIMEI();
        logger.debug("获取imei:" + imei);
        myHandler.postDelayed(() -> {
            if (TextUtils.isEmpty(imei)) {
                if (show) {
                    ToastUtil.showToast(this, getString(R.string.text_imei_null));
                }
            } else {
                login();
            }
        }, time);
    }

    private void login() {
        //进入注册界面了，先判断有没有认证地址
        String authUrl = TerminalFactory.getSDK().getParam(Params.AUTH_URL, "");
        if (TextUtils.isEmpty(authUrl)) {
            //平台包或者没获取到类型，直接用AuthManager中的地址,
            //设置切换的环境
            setChangeServer();
            String[] defaultAddress = TerminalFactory.getSDK().getAuthManagerTwo().getDefaultAddress();
            if (defaultAddress.length >= 2) {
                int resultCode = TerminalFactory.getSDK().getAuthManagerTwo().startAuth(defaultAddress[0], defaultAddress[1]);
                if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                    ToastUtil.showToast(BaseActivity.this, getString(R.string.text_authing));
                } else {
                    //状态机没有转到正在认证，说明已经在状态机中了，不用处理
                }
            } else {
                //没有注册服务地址，去探测地址
                TerminalFactory.getSDK().getAuthManagerTwo().checkRegistIp();
            }
        } else {
            //有注册服务地址，去认证
            int resultCode = TerminalFactory.getSDK().getAuthManagerTwo().startAuth(TerminalFactory.getSDK().getParam(Params.REGIST_IP, ""), TerminalFactory.getSDK().getParam(Params.REGIST_PORT, ""));
            if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                ToastUtil.showToast(BaseActivity.this, getString(R.string.text_authing));
            } else {
                //状态机没有转到正在认证，说明已经在状态机中了，不用处理
            }
        }
    }

    /**
     * 强制重新注册的消息
     */
    private ReceiveForceReloginHandler receiveForceReloginHandler = version -> {
        if (SystemUtil.isForeground(BaseActivity.this)) {
            myHandler.post(() -> ToastUtil.showToast(BaseActivity.this, "正在强制重新登录"));
        }
    };

    /**
     * 临时组相关的通知
     */
    private ReceiveMemberAboutTempGroupHandler receiveMemberAboutTempGroupHandler = new ReceiveMemberAboutTempGroupHandler() {
        @Override
        public void handler(boolean isAdd, boolean isLocked, boolean isScan, boolean isSwitch, int tempGroupNo, String tempGroupName, String tempGroupType) {
            if (TempGroupType.TO_HELP_COMBAT.toString().equals(tempGroupType)) {
                if (!isAdd) {
                    //如果消息列表里有合成作战组，去掉再刷新
                    RecorderBindBean bean = cn.vsx.hamster.terminalsdk.tools.DataUtil.getRecorderBindBean();
                    if (bean != null && bean.getGroupId() == tempGroupNo) {
                        //警情组到期，退出账号，登录默认账号。
                        TerminalFactory.getSDK().getRecorderBindManager().requestUnBind();
                    }
                }
            }
        }
    };

    /**
     * 读取NFC数据的回调
     *
     * @param resultCode
     * @param readType
     * @param resultDescribe
     * @param bean
     */
    @Override
    public void onReadResult(int resultCode, String readType, String resultDescribe, final RecorderBindTranslateBean bean) {
        ToastUtil.showToast(this, resultDescribe);
        switch (resultCode) {
            case NfcUtil.RESULT_CODE_SUCCESS:
                //切组，上报，录像
                if (bean != null) {
                    logger.debug("onReadResult---bean:" + bean);
                    TerminalFactory.getSDK().getThreadPool().execute(() -> {
                        long uniqueNo = MyTerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO, 0L);
                        TerminalFactory.getSDK().getRecorderBindManager().requestBind(bean.getAccountNo(), uniqueNo, bean.getGroupId(), bean.getWarningId());
                    });
                }
                break;
            default:
                break;
        }
    }

    /**
     * 退出登录
     */
    private void loginOut(boolean isChangeAccount) {
        TerminalFactory.getSDK().notifyReceiveHandler(ReceiverStopAllBusniessHandler.class, false);
        //停止一切业务
        if (TerminalFactory.getSDK().isServerConnected()) {
            TerminalFactory.getSDK().getAuthManagerTwo().logout();
        }

        TerminalFactory.getSDK().putParam(Params.IS_FIRST_LOGIN, true);
        TerminalFactory.getSDK().putParam(Params.IS_UPDATE_DATA, true);
        MyApplication.instance.isClickVolumeToCall = false;
        MyApplication.instance.isPttPress = false;
        isFristLogin = true;
        TerminalFactory.getSDK().getAuthManagerTwo().getLoginStateMachine().stop();
//        MyApplication.instance.stopPTTButtonEventService();
        if (this instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) this;
            if (!isChangeAccount) {
                //解除预览图像的service
                mainActivity.stopLiveService();
            }
            //清空自动上报标记
            mainActivity.updateNormalPushingState(false);
        }
        //切换账号的时候，清除账号相关的信息
        TerminalFactory.getSDK().getDataManager().clearDataByAccountChanged();
//        MyTerminalFactory.getSDK().stop();
        TerminalFactory.getSDK().getClientChannel().stop();
        //停止上报或者观看的页面
        if (isChangeAccount && TerminalFactory.getSDK().isServerConnected()) {
            TerminalFactory.getSDK().disConnectToServer();
        }
    }

    /**
     * 切换到绑定账号
     */
    public void changeAccount() {
        //退出账号
        loginOut(true);
        //登录绑定账号
        checkLogin(true, LOGIN_DELAY_TIME);
    }

    /**
     * 退出应用
     */
    protected void exitApp() {
        //退出账号
        loginOut(false);
        //清除绑定账号
        DataUtil.clearRecorderBindBean();
        //清除sp
        DeleteData.deleteSharedPreferences();
        //退出页面
        for (Activity activity : ActivityCollector.getAllActivity().values()) {
            activity.finish();
        }
        //停止SDK
        MyTerminalFactory.getSDK().stop();
        //杀掉进程
        MyApplication.instance.killAllProcess();
    }

    /**
     * 遥毙
     */
    protected void forbid() {
        //退出账号
        loginOut(false);
        //清除绑定账号
        DataUtil.clearRecorderBindBean();
        //停止SDK
        MyTerminalFactory.getSDK().stop();
        //退出页面
        startActivity(new Intent(BaseActivity.this, KilledActivity.class));
        BaseActivity.this.finish();
    }


    /**
     * 重置服务ip和port
     */
    private void setChangeServer() {
        RecorderServerBean bean = DataUtil.getRecorderServerBean();
        if (bean != null) {
            TerminalFactory.getSDK().getAuthManagerTwo().resetServer(bean.getIp(), DataUtil.stringToInt(bean.getPort()), bean.getName());
        }
    }

    /**
     * 获取退出时的提示
     *
     * @return
     */
    protected String getExitState(BITMediaStream mMediaStream) {
        if (mMediaStream != null && mMediaStream.isStreaming()) {
            return "停止上报";
        } else if (mMediaStream != null && mMediaStream.isRecording()) {
            return "停止录像";
        } else if (MyTerminalFactory.getSDK().getRecordingAudioManager().getStatus() != AudioRecordStatus.STATUS_STOPED) {
            return "停止录音";
        } else {
            return "退出";
        }
    }

    protected int getDgree(WindowManager windowManager) {
        int rotation = windowManager.getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break; // Natural orientation
            case Surface.ROTATION_90:
                degrees = 90;
                break; // Landscape left
            case Surface.ROTATION_180:
                degrees = 180;
                break;// Upside down
            case Surface.ROTATION_270:
                degrees = 270;
                break;// Landscape right
            default:
                break;
        }
        return degrees;
    }

    protected class PushCallback implements InitCallback {

        @Override
        public void onCallback(int code) {
            Bundle resultData = new Bundle();
            switch (code) {
                case EasyPusher.OnInitPusherCallback.CODE.EASY_ACTIVATE_INVALID_KEY:
                    resultData.putString("event-msg", "EasyRTSP 无效Key");
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_ACTIVATE_SUCCESS:
                    resultData.putString("event-msg", "EasyRTSP 激活成功");
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_CONNECTING:
                    resultData.putString("event-msg", "EasyRTSP 连接中");
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_CONNECTED:
                    resultData.putString("event-msg", "EasyRTSP 连接成功");
                    pushcount = 0;
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_CONNECT_FAILED:
                    resultData.putString("event-msg", "EasyRTSP 连接失败");
                    if (pushcount <= 10) {
                        pushcount++;
                    } else {
                        finishVideoLive();
                    }
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_CONNECT_ABORT:
                    resultData.putString("event-msg", "EasyRTSP 连接异常中断");
                    if (pushcount <= 10) {
                        pushcount++;
                    } else {
                        finishVideoLive();
                    }
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_PUSHING:
                    resultData.putString("event-msg", "EasyRTSP 推流中");
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_DISCONNECTED:
                    resultData.putString("event-msg", "EasyRTSP 断开连接");
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_ACTIVATE_PLATFORM_ERR:
                    resultData.putString("event-msg", "EasyRTSP 平台不匹配");
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_ACTIVATE_COMPANY_ID_LEN_ERR:
                    resultData.putString("event-msg", "EasyRTSP 断授权使用商不匹配");
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_ACTIVATE_PROCESS_NAME_LEN_ERR:
                    resultData.putString("event-msg", "EasyRTSP 进程名称长度不匹配");
                    break;
            }
//            mResultReceiver.send(EasyRTSPClient.RESULT_EVENT, resultData);
        }
    }

    public void uploadLog() {
        logDialog = new BITDialogUtil() {
            @Override
            public CharSequence getMessage() {
                return "确定上传日志?";
            }

            @Override
            public Context getContext() {
                return BaseActivity.this;
            }

            @Override
            public void doConfirmThings() {
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (System.currentTimeMillis() - currentTime > 5000) {
                            MyTerminalFactory.getSDK().getLogFileManager().uploadAllLogFile();
                            currentTime = System.currentTimeMillis();
                        } else {
                            ToastUtil.showToast("您已经上传过日志了，请稍后再上传", BaseActivity.this);
                        }
                    }
                }, 0);
            }

            @Override
            public void doCancelThings() {
            }
        }.showDialog();
    }

    /**
     * 弹窗提示是否自动上报
     */
    protected void showPushLiveDialog() {
        pushDialog = new BITDialogUtil() {
            @Override
            public CharSequence getMessage() {
                return "是否继续上报?";
            }

            @Override
            public Context getContext() {
                return BaseActivity.this;
            }

            @Override
            public void doConfirmThings() {
                requestStartLive();
            }

            @Override
            public void doCancelThings() {
            }
        }.showDialog();
    }

    /**
     * 弹窗提示是否是否退出账号
     */
    protected void showExitDialog() {
        boolean showExitAccount = TerminalFactory.getSDK().getAuthManagerTwo().isOnLine();
        exitAccountDialog = new ExitAccountDialog(this, showExitAccount, new ExitAccountDialog.OnClickListener() {
            @Override
            public void onMoveTaskToBack() {
                moveTaskToBack(true);
            }

            @Override
            public void onExitAccount() {
                exitApp();
            }
        });
        exitAccountDialog.show();
    }

    /**
     * 隐藏退出账号弹窗
     */
    protected void dismissDialog() {
        if (exitAccountDialog != null && exitAccountDialog.isShowing()) {
            exitAccountDialog.dismiss();
        }
        if (logDialog != null && logDialog.isShowing()) {
            logDialog.dismiss();
        }
        if (pushDialog != null && pushDialog.isShowing()) {
            pushDialog.dismiss();
        }
    }
}

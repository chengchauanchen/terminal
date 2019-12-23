package cn.vsx.vc.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.bean.ZxingConfig;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePlayingState;
import cn.vsx.hamster.terminalsdk.model.RecorderBindTranslateBean;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveForceOfflineHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveForceReloginHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberDeleteHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyMemberKilledHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseZfyBoundPhoneByRequestMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveVolumeOffCallHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.GroupUtils;
import cn.vsx.hamster.terminalsdk.tools.OperateReceiveHandlerUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.application.UpdateManager;
import cn.vsx.vc.dialog.NFCBindingDialog;
import cn.vsx.vc.dialog.ProgressDialog;
import cn.vsx.vc.receive.Actions;
import cn.vsx.vc.receive.IBroadcastRecvHandler;
import cn.vsx.vc.receive.RecvCallBack;
import cn.vsx.vc.receive.SendRecvHelper;
import cn.vsx.vc.receiveHandle.OnBackListener;
import cn.vsx.vc.receiveHandle.ReceiveTianjinAuthLoginAndLogoutHandler;
import cn.vsx.vc.receiver.HeadsetPlugReceiver;
import cn.vsx.vc.utils.ActivityCollector;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.DialogUtil;
import cn.vsx.vc.utils.NfcUtil;
import cn.vsx.vc.utils.PhoneAdapter;
import cn.vsx.vc.utils.SystemUtil;
import cn.vsx.vc.utils.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

import static cn.vsx.vc.activity.NewMainActivity.REQUEST_CODE_SCAN;

public abstract class BaseActivity extends AppCompatActivity implements RecvCallBack, Actions {

    private AudioManager audioManager;
    private HeadsetPlugReceiver headsetPlugReceiver;

    protected Logger logger = Logger.getLogger(getClass().getSimpleName());
    protected final String TAG = getClass().getSimpleName();
    private ProgressDialog myProgressDialog;//加载数据的弹窗

    public OnBackListener backListener;
    protected boolean oritationPort;

    protected static final int CODE_FNC_REQUEST = 0x15;
    public static final int REQUEST_INSTALL_PACKAGES_CODE = 1235;
    public static final int GET_UNKNOWN_APP_SOURCES = 1236;
    public UpdateManager updateManager;

    //成员被删除了
    private ReceiveMemberDeleteHandler receiveMemberDeleteHandler = new ReceiveMemberDeleteHandler() {
        @Override
        public void handler() {
            myHandler.post(() -> MyApplication.instance.stopHandlerService());
        }
    };


    private boolean isBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                logger.info("此app =" + appProcess.importance + ",context.getClass().getTitleName()=" + context.getClass().getName());
                if (appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    logger.info("处于后台" + appProcess.processName);
                    return true;
                } else {
                    logger.info("处于前台" + appProcess.processName);
                    return false;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unused")
    private void setMySpeakerphoneOn(boolean on) {
        if (on) {
            if (!audioManager.isSpeakerphoneOn() && audioManager.getMode() == AudioManager.MODE_NORMAL) {
                logger.info("---------打开扬声器--------" + audioManager.getMode());
                audioManager.setSpeakerphoneOn(true);
            }
        } else {
            if (audioManager.isSpeakerphoneOn()) {
                audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                        audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
                        AudioManager.STREAM_VOICE_CALL);//设置听筒的最大音量
                audioManager.setSpeakerphoneOn(false);//关闭扬声器
                logger.info("--------关闭扬声器---------" + audioManager.getMode());
            }
        }
    }


    protected Handler myHandler = new Handler(Looper.getMainLooper());


    /**
     * 组成员遥毙消息
     */
    protected ReceiveNotifyMemberKilledHandler receiveNotifyMemberKilledHandler = forbid -> {
        logger.error("收到遥毙，此时forbid状态为：" + forbid);
        if (forbid) {
            TerminalFactory.getSDK().putParam(Params.IS_FIRST_LOGIN, true);
            TerminalFactory.getSDK().putParam(Params.IS_UPDATE_DATA, true);
            startActivity(new Intent(BaseActivity.this, KilledActivity.class));
            myHandler.postDelayed(() -> {
                BaseActivity.this.finish();
                MyApplication.instance.stopHandlerService();
            }, 5000);
        }
    };

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

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 没有标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        setOritation();
//        if (oritationPort) {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        } else {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        }
        setOrientation();
        //透明状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(getLayoutResId());
        Log.d(TAG, "onCreate:" + MyApplication.instance.mAppStatus);
        // 判断如果被强杀，就回到 MainActivity 中去，否则可以初始化
        if (MyApplication.instance.mAppStatus == Constants.FORCE_KILL) {
            Log.d(TAG, "应用被强杀了，重新去注册界面");
            //重走应用流程
            protectApp();
        } else {


            regBroadcastRecv(ACT_SHOW_FULL_SCREEN, ACT_DISMISS_FULL_SCREEN, STOP_INDIVDUALCALL_SERVEIC);
            ActivityCollector.addActivity(this, getClass());
            initView();

            initData();

            initListener();

            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        }
        //适配Android9.0调用hide时，关闭警告弹窗
        closeAndroidPDialog();
        createProgressDialog();
        MyTerminalFactory.getSDK().registReceiveHandler(receiveForceOfflineHandler);
    }

    protected void setOrientation(){
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    protected void setOritation() {
        this.oritationPort = true;
    }

    protected void protectApp() {
        // 重新走应用的流程是一个正确的做法，因为应用被强杀了还保存 Activity 的栈信息是不合理的
        if(TerminalFactory.getSDK().isServerConnected()){
            TerminalFactory.getSDK().getAuthManagerTwo().logout();
        }
        MyApplication.instance.isClickVolumeToCall = false;
        MyApplication.instance.isPttPress = false;
        OperateReceiveHandlerUtil.getInstance().clear();
        Intent intent = new Intent(this, RegistActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        ActivityCollector.removeAllActivityExcept(RegistActivity.class);
        android.os.Process.killProcess(android.os.Process.myPid());

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


    @Override
    protected void onResume() {
        super.onResume();

        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyMemberKilledHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveMemberDeleteHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveForceReloginHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseZfyBoundPhoneByRequestMessageHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveTianjinAuthLoginAndLogoutHandler);
        registerHeadsetPlugReceiver();
        setPttVolumeChangedListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterHeadsetPlugReceiver();
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMemberDeleteHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyMemberKilledHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveForceReloginHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseZfyBoundPhoneByRequestMessageHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveTianjinAuthLoginAndLogoutHandler);
    }

    @Override
    protected void onDestroy() {
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveForceOfflineHandler);
        try {
            doOtherDestroy();

            ActivityCollector.removeActivity(this);

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
     * 注册耳机插入拔出的监听
     */
    private void registerHeadsetPlugReceiver() {
        headsetPlugReceiver = new HeadsetPlugReceiver(getApplicationContext());
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GET_UNKNOWN_APP_SOURCES){
            if(null !=updateManager){
                updateManager.checkIsAndroidO((this instanceof AboutActivity));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_INSTALL_PACKAGES_CODE:
                if(grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                    startActivityForResult(intent, GET_UNKNOWN_APP_SOURCES);
                }
            default:
                break;

        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        logger.info("keyCode:" + keyCode + "--event:" + event);
        // 如果程序在后台运行，则忽略
        if (isBackground(getApplicationContext())) {
            return super.onKeyDown(keyCode, event);
        }
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY:
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                Intent intent = new Intent();
                //判断状态机此时的状态
                if (MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.IDLE) {
                    //蓝牙耳机点击事件
                    intent.setAction("BLUE_TOOTH_DOWN");
                } else {
                    intent.setAction("BLUE_TOOTH_UP");
                }
                sendBroadcast(intent);
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:// 增大音量
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND
                                | AudioManager.FLAG_SHOW_UI);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:// 减小音量
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND
                                | AudioManager.FLAG_SHOW_UI);
                return true;
            case KeyEvent.KEYCODE_HEADSETHOOK:
                if (PhoneAdapter.isF32()) {
                    return true;
                }
                if (!MyApplication.instance.isPttPress) {
                    logger.info("onKeyUp执行第一次，开始说话");
                    if (onPTTVolumeBtnStatusChangedListener != null) {
                        logger.warn("用耳机中键做ptt按钮，按下时：ptt的当前状态是：" + MyApplication.instance.getGroupSpeakState());
                        onPTTVolumeBtnStatusChangedListener.onPTTVolumeBtnStatusChange(MyApplication.instance.getGroupSpeakState(),true);
                    }
                    MyApplication.instance.isPttPress = true;
                } else if (MyApplication.instance.isPttPress) {
                    logger.info("onClickUp执行第二次，暂停说话");
                    if (onPTTVolumeBtnStatusChangedListener != null) {
                        onPTTVolumeBtnStatusChangedListener.onPTTVolumeBtnStatusChange(GroupCallSpeakState.END,false);
                    }
                    MyApplication.instance.isPttPress = false;
                }
                return true;
        }
        // 为true,则其它后台按键处理再也无法处理到该按键，为false,则其它后台按键处理可以继续处理该按键事件
        return super.onKeyDown(keyCode, event);
    }

    //音量上下键为PTT按钮状态改变的监听接口
    private OnPTTVolumeBtnStatusChangedListener onPTTVolumeBtnStatusChangedListener;

    public interface OnPTTVolumeBtnStatusChangedListener {
        void onPTTVolumeBtnStatusChange(GroupCallSpeakState groupCallSpeakState,boolean isVolumeUp);
    }

    public void setOnPTTVolumeBtnStatusChangedListener(OnPTTVolumeBtnStatusChangedListener onPTTVolumeBtnStatusChangedListener) {
        this.onPTTVolumeBtnStatusChangedListener = onPTTVolumeBtnStatusChangedListener;
    }

    boolean isDown = true;
    long lastVolumeUpTime = 0;
    long lastVolumeDownTime = 0;

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        logger.info("event.getKeyCode() = " + event.getKeyCode());
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_VOLUME_UP:// 增大音量
                if (MyTerminalFactory.getSDK().getParam(Params.VOLUME_UP, false)) {
                    //如果当前是禁呼的组，点击音量键控制是无效的
                    if (!GroupUtils.currentIsForbid()) {
                        groupCallByVolumeKey(event, true);
                    }
                    return true;
                } else {
                    if (System.currentTimeMillis() - lastVolumeUpTime > 500) {
                        MyTerminalFactory.getSDK().getAudioProxy().volumeUp();
                        if (MyTerminalFactory.getSDK().getAudioProxy().getVolume() > 0) {
                            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveVolumeOffCallHandler.class, false, 1);
                        } else {
                            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveVolumeOffCallHandler.class, true, 1);
                        }
                        lastVolumeUpTime = System.currentTimeMillis();
                    }
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:// 减小音量
                if (MyTerminalFactory.getSDK().getParam(Params.VOLUME_DOWN, false)) {
                    //如果当前是禁呼的组，点击音量键控制是无效的
                    if (!GroupUtils.currentIsForbid()) {
                        groupCallByVolumeKey(event, false);
                    }
                    return true;
                } else {
                    if (System.currentTimeMillis() - lastVolumeDownTime > 500) {
                        MyTerminalFactory.getSDK().getAudioProxy().volumeDown();
                        if (MyTerminalFactory.getSDK().getAudioProxy().getVolume() > 0) {
                            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveVolumeOffCallHandler.class, false, 1);
                        } else {
                            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveVolumeOffCallHandler.class, true, 1);
                        }
                        lastVolumeDownTime = System.currentTimeMillis();
                    }
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_DPAD_UP: {
//				// 后台运行或者机型未非F25，则忽略
//				if ( isBackground( getApplicationContext()) || !PhoneAdapter.isF25() ) {
//					break;
//				}
//
//				// 键盘按下，触发0到多次的down，一个Up操作，但只触发一个事件
//				if (event.getAction() == KeyEvent.ACTION_DOWN) {
//					if (null != currentFragment && isDown ) {
//						((BaseFragment) currentFragment).onMyKeyDown(event);
//						isDown = false;
//					}
//				}
//				else {
//					if (isDown && null != currentFragment) {
//						((BaseFragment) currentFragment).onMyKeyDown(event);
//					}
//					isDown = true;
//				}
                return true;
            }
            case KeyEvent.KEYCODE_F6:
                return true;
        }
        return super.dispatchKeyEvent(event);
    }
    /**
     * 设置音量上下键为PTT按钮
     */
    private int downTimes = 0;

    private void groupCallByVolumeKey(KeyEvent event, boolean isVolumeUp) {
        switch (event.getAction()) {
            case KeyEvent.ACTION_DOWN:
                downTimes++;
                logger.warn("用音量键做ptt按钮，音量键按下时：downTimes：" + downTimes + "    pttPress状态：" + MyApplication.instance.isPttPress);
                if (downTimes == 1 && !MyApplication.instance.folatWindowPress && !MyApplication.instance.isPttPress) {
                    if (MyApplication.instance.getIndividualState() != IndividualCallState.IDLE) {
                        ToastUtil.showToast(BaseActivity.this, getResources().getString(R.string.text_personal_calling_can_not_group_call));
                        return;
                    } else if (MyApplication.instance.getVideoLivePlayingState() != VideoLivePlayingState.IDLE) {
                        ToastUtil.showToast(BaseActivity.this, getResources().getString(R.string.text_personal_pulling_can_not_group_call));
                        return;
                    }
                    MyApplication.instance.isClickVolumeToCall = true;
                    if (onPTTVolumeBtnStatusChangedListener != null) {
                        logger.warn("用音量键做ptt按钮，音量键按下时：ptt的当前状态是：" + MyApplication.instance.getGroupSpeakState());
                        onPTTVolumeBtnStatusChangedListener.onPTTVolumeBtnStatusChange(MyApplication.instance.getGroupSpeakState(),isVolumeUp);
                    }
                    MyApplication.instance.volumePress = true;

                    if (isVolumeUp) {
                        volumeUpCalling = true;
                    } else {
                        volumeDownCalling = true;
                    }
                }
                break;
            case KeyEvent.ACTION_UP:
                logger.info("音量的抬起事件 " + MyApplication.instance.volumePress + "--组呼说状态：" + MyApplication.instance.getGroupSpeakState() +
                        "---组呼听状态：" + MyApplication.instance.getGroupListenenState());

                if (MyApplication.instance.volumePress) {
                    MyApplication.instance.isClickVolumeToCall = false;
                    if (onPTTVolumeBtnStatusChangedListener != null) {
                        onPTTVolumeBtnStatusChangedListener.onPTTVolumeBtnStatusChange(GroupCallSpeakState.END,isVolumeUp);
                    }
                    MyApplication.instance.volumePress = false;
                }
                downTimes = 0;
                volumeUpCalling = false;
                volumeDownCalling = false;
                break;
        }
    }

    private boolean volumeUpCalling, volumeDownCalling;

    private void setPttVolumeChangedListener() {
        SharedPreferences account = MyTerminalFactory.getSDK().getAccount();
        if (account != null) {
            account.registerOnSharedPreferenceChangeListener(listener);
        }
    }

    private SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if ((key.equals(Params.VOLUME_UP) && !MyTerminalFactory.getSDK().getParam(Params.VOLUME_UP, false) && volumeUpCalling)
                    || (key.equals(Params.VOLUME_DOWN) && !MyTerminalFactory.getSDK().getParam(Params.VOLUME_DOWN, false) && volumeDownCalling)) {
                if (onPTTVolumeBtnStatusChangedListener != null) {
                    onPTTVolumeBtnStatusChangedListener.onPTTVolumeBtnStatusChange(GroupCallSpeakState.END,false);
                }
                MyApplication.instance.isClickVolumeToCall = false;
                MyApplication.instance.volumePress = false;
            }
        }
    };

    private void closeAndroidPDialog() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            try {
                Class aClass = Class.forName("android.content.pm.PackageParser$Package");
                Constructor declaredConstructor = aClass.getDeclaredConstructor(String.class);
                declaredConstructor.setAccessible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Class cls = Class.forName("android.app.ActivityThread");
                Method declaredMethod = cls.getDeclaredMethod("currentActivityThread");
                declaredMethod.setAccessible(true);
                Object activityThread = declaredMethod.invoke(null);
                Field mHiddenApiWarningShown = cls.getDeclaredField("mHiddenApiWarningShown");
                mHiddenApiWarningShown.setAccessible(true);
                mHiddenApiWarningShown.setBoolean(activityThread, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 强制重新注册的消息
     */
    private ReceiveForceReloginHandler receiveForceReloginHandler = new ReceiveForceReloginHandler() {
        @Override
        public void handler(String version) {
            if (SystemUtil.isForeground(BaseActivity.this)) {
                myHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.showToast(BaseActivity.this, "正在强制重新登录");
                    }
                });
            }
        }
    };

    /**
     * 响应执法记录仪绑定警务通（相应给请求人，说明绑定请求是否成功）
     */
    private ReceiveResponseZfyBoundPhoneByRequestMessageHandler receiveResponseZfyBoundPhoneByRequestMessageHandler = new ReceiveResponseZfyBoundPhoneByRequestMessageHandler(){
        @Override
        public void handler(int resultCode,String resultDesc) {
            if(resultCode == BaseCommonCode.SUCCESS_CODE){
                cn.vsx.vc.utils.ToastUtil.showToast(BaseActivity.this,getString(R.string.text_request_bind_success));
            }else{
                cn.vsx.vc.utils.ToastUtil.showToast(BaseActivity.this,(android.text.TextUtils.isEmpty(resultDesc))?getString(R.string.text_request_bind_fail):resultDesc);
            }
        }
    };

    /**
     *天津统一认证客户端登录、退出的通知
     */
    private ReceiveTianjinAuthLoginAndLogoutHandler receiveTianjinAuthLoginAndLogoutHandler = new ReceiveTianjinAuthLoginAndLogoutHandler(){
        @Override
        public void handler(boolean isLogin) {
            if(isLogin){
                //登录成功-去认证登录
                //判断是否是
                if(ActivityCollector.isActivityExist(RegistActivity.class)){
                    RegistActivity activity = ActivityCollector.getActivity(RegistActivity.class);
                    //分两种情况，1.什么都没有操作，2.正在登录
                    if(!TerminalFactory.getSDK().getAuthManagerTwo().needLogin()){
                        TerminalFactory.getSDK().getAuthManagerTwo().getLoginStateMachine().stop();
                    }
                    activity.judgePermission();
                }else {
                    //退出登录
                    logoutAccount();
                }
            }else{
                //退出登录-退出登录
                logoutAccount();
            }
        }
    };

    /**
     * 创建加载数据的ProgressDialog
     */
    private void createProgressDialog() {
        if (myProgressDialog == null) {
            myProgressDialog = new ProgressDialog(this);
            myProgressDialog.setCancelable(true);
        }
    }

    /**
     * 显示加载数据的ProgressDialog
     */
    public void showProgressDialog() {
        if (myProgressDialog != null) {
            myProgressDialog.setMsg(this.getString(R.string.get_data_now));
            myProgressDialog.show();
        }
    }

    /**
     * 隐藏加载数据的ProgressDialog
     */
    public void dismissProgressDialog() {
        if (myProgressDialog != null) {
            myProgressDialog.dismiss();
        }
    }

    public void setBackListener(OnBackListener backListener) {
        this.backListener = backListener;
    }

    private ReceiveForceOfflineHandler receiveForceOfflineHandler = () -> myHandler.post(() -> {
        new DialogUtil() {

            @Override
            public CharSequence getMessage() {
                return getString(R.string.force_off_line);
            }

            @Override
            public Context getContext() {
                return BaseActivity.this;
            }

            @Override
            public void doConfirmThings() {
                exitApp();
            }

            @Override
            public void doCancelThings() {
                exitApp();
            }
        }.showDialog();
    });

    /**
     * 检查NFC功能，并提示
     */
    public void checkNFC(int userId,boolean openSetting) {
        int result = NfcUtil.nfcCheck(this);
        switch (result) {
            case NfcUtil.NFC_ENABLE_FALSE_NONE:
                ToastUtil.showToast(this, getString(R.string.is_not_support_nfc));
                break;
            case NfcUtil.NFC_ENABLE_FALSE_JUMP:
                ToastUtil.showToast(this, this.getString(R.string.is_not_open_nfc));
                if (openSetting) {
                    myHandler.postDelayed(() -> startActivityForResult(new Intent(Settings.ACTION_NFC_SETTINGS), CODE_FNC_REQUEST), 500);
                }
                break;
            case NfcUtil.NFC_ENABLE_FALSE_SHOW:
                showNFCDialog(userId);
                break;
            case NfcUtil.NFC_ENABLE_NONE:
                break;
        }
    }

    /**
     * 绑定设备
     */
    public void bandDeviceDialog(){
        startActivity(new Intent(this,AddEquipmentActivity.class));

    }

    /**
     * 显示刷NFC的弹窗
     */
    private void showNFCDialog(int userId) {
        if(userId!=0){
            NFCBindingDialog nfcBindingDialog = new NFCBindingDialog(BaseActivity.this, NFCBindingDialog.TYPE_WAIT);
            HashMap<String, String> hashMap = TerminalFactory.getSDK().getHashMap(Params.GROUP_WARNING_MAP, new HashMap<String, String>());
            if (hashMap.containsKey(userId + "") && !android.text.TextUtils.isEmpty(hashMap.get(userId + ""))) {
                nfcBindingDialog.showDialog(userId, hashMap.get(userId + ""),"");
            }else{
                nfcBindingDialog.showDialog(userId, "","");
            }
        }else{
            ToastUtil.showToast(BaseActivity.this,getString(R.string.text_group_id_abnormal));
        }
    }

    /**
     * 跳转到扫码页面
     */
    public void goToScanActivity(){
        Intent intent = new Intent(this, CaptureActivity.class);
        ZxingConfig config = new ZxingConfig();
        config.setShowbottomLayout(false);//底部布局（包括闪光灯和相册）
        config.setPlayBeep(true);//是否播放提示音
        config.setShake(true);//是否震动
        config.setReactColor(R.color.ok_blue);
        config.setScanLineColor(R.color.ok_blue);
        //config.setShowAlbum(true);//是否显示相册
        //config.setShowFlashLight(true);//是否显示闪光灯
        intent.putExtra(com.yzq.zxinglibrary.common.Constant.INTENT_ZXING_CONFIG, config);
        startActivityForResult(intent, REQUEST_CODE_SCAN);
    }

    /**
     * 解析扫码的数据
     * @param result
     * @param groupId
     */
    protected void analysisScanData(String result, int groupId) {
        RecorderBindTranslateBean bean = DataUtil.getRecorderBindTranslateBean(result);
        // TODO: 2019/4/10 给注册服务发送扫码结果
        if(DataUtil.isLegalPcCode(result)) {
            //PC登录
            Intent intent = new Intent(this, PcLoginActivity.class);
            intent.putExtra(Constants.SCAN_DATA, result);
            startActivity(intent);
        }else if(bean!=null){
            //执法记录仪绑定
            TerminalFactory.getSDK().getThreadPool().execute(() -> {
                if(groupId!=0){
                    HashMap<String, String> hashMap = TerminalFactory.getSDK().getHashMap(Params.GROUP_WARNING_MAP, new HashMap<String, String>());
                    int memberId = MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
                    if(hashMap.containsKey(groupId + "") && !android.text.TextUtils.isEmpty(hashMap.get(groupId + ""))){
                        TerminalFactory.getSDK().getRecorderBindManager().requestBind(memberId,bean.getUniqueNo(),groupId,hashMap.get(groupId + ""));
                    }else{
                        TerminalFactory.getSDK().getRecorderBindManager().requestBind(memberId,bean.getUniqueNo(),groupId,"");
                    }
                }else{
                    ToastUtil.showToast(this,getString(R.string.text_group_id_abnormal));
                }
            });
        }else{
            ToastUtil.showToast(this,getString(R.string.text_please_scan_correct_qr_recorder));
        }
    }

    /**
     * 设置是否免提
     * @param result
     */
    protected void setSpeakPhoneOn(ImageView imageView,boolean result){
        if(result){
            if (!MyTerminalFactory.getSDK().getAudioProxy().isSpeakerphoneOn()) {
                MyTerminalFactory.getSDK().getAudioProxy().setSpeakerphoneOn(result);
            }
            if(imageView!=null){
                imageView.setImageResource(R.drawable.ic_hand_free_2);
            }
        }else{
            if (MyTerminalFactory.getSDK().getAudioProxy().isSpeakerphoneOn()) {
                MyTerminalFactory.getSDK().getAudioProxy().setSpeakerphoneOn(result);
            }
            if(imageView!=null){
                imageView.setImageResource(R.drawable.ic_hand_free_1);
            }
        }
    }
    /**
     * 设置是否静音
     * @param result
     */
    protected void setMicrophoneMute(ImageView imageView,boolean result){
        MyTerminalFactory.getSDK().getAudioProxy().setMicrophoneMute(result);
        if(imageView!=null){
            imageView.setImageResource(result?R.drawable.ic_micro_mute_2:R.drawable.ic_micro_mute_1);
        }
    }

    /**
     * 退出账号并退出应用
     */
    private void logoutAccount(){
        //清除数据
        TerminalFactory.getSDK().clearData();
        //退出应用
        exitApp();
    }

    public void exitApp() {

        if(TerminalFactory.getSDK().isServerConnected()){
            TerminalFactory.getSDK().getAuthManagerTwo().logout();
        }

        Intent stoppedCallIntent = new Intent("stop_indivdualcall_service");
        stoppedCallIntent.putExtra("stoppedResult", "0");
        SendRecvHelper.send(BaseActivity.this, stoppedCallIntent);


        for (Activity activity : ActivityCollector.getAllActivity().values()) {
            activity.finish();
        }
        MyApplication.instance.isClickVolumeToCall = false;
        MyApplication.instance.isPttPress = false;
        MyApplication.instance.stopHandlerService();
    }



}

package cn.vsx.vc.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import org.apache.log4j.Logger;

import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePlayingState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePushingState;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveForceReloginHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveLoginResponseHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyLivingIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyMemberKilledHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveOnLineStatusChangedHandler;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.receiveHandle.ReceiverRemoveWindowViewHandler;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.SensorUtil;
import cn.vsx.vc.utils.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.service.KeepLiveManager;

/**
 * 作者：xuxiaolong
 * 版本：1.0
 * 创建日期：2018/12/29
 * 描述：
 * 修订历史：
 */
public abstract class BaseService extends Service{

    protected Logger logger = Logger.getLogger(this.getClass());
    protected final String TAG = this.getClass().getSimpleName();
    protected WindowManager windowManager;
    protected WindowManager.LayoutParams layoutParams;
    protected WindowManager.LayoutParams layoutParams1;
    protected int screenWidth;
    protected PowerManager.WakeLock wakeLock;
    protected View rootView;
    protected boolean dialogAdd;//是否添加了弹窗
    public static final int OFF_LINE_TIME = 60*1000;
    protected static final int OFF_LINE = 55;


    @SuppressLint("HandlerLeak")
    protected Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            handleMesage(msg);
            super.handleMessage(msg);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public void onCreate(){
        logger.info(TAG+":onCreate");
        super.onCreate();
        initWakeLock();
        setRootView();
        findView();
        initData();
        initWindow();
        initListener();
        initHomeBroadCastReceiver();
        initBroadCastReceiver();
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyMemberKilledHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveForceReloginHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveOnLineStatusChangedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveLoginResponseHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyLivingIncommingHandler);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        KeepLiveManager.getInstance().setServiceForeground(this);
        logger.info(TAG+"---onStartCommand--"+dialogAdd);
        if(!dialogAdd){
            windowManager.addView(rootView, layoutParams1);
            MyApplication.instance.viewAdded = true;
            dialogAdd = true;
            if(null != intent){
                initView(intent);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @SuppressLint("InvalidWakeLockTag")

    protected void initWakeLock(){
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if(null != powerManager){
            //noinspection deprecation
            wakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "wakeLock");
        }
    }


    protected void initWindow(){
        windowManager = (WindowManager) this.getSystemService(WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_PHONE ,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.RGBA_8888);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.gravity = Gravity.END|Gravity.TOP;
        //小窗口type，要让下层view可以获取焦点
        layoutParams.flags = WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        layoutParams1 = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_PHONE ,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED, PixelFormat.RGBA_8888);

        //大窗口
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams1.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams1.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams1.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING;
        layoutParams1.gravity = Gravity.CENTER;
        //大窗口type，下层view不获取焦点
        layoutParams1.flags = WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED|
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
    }

    protected void initHomeBroadCastReceiver(){
        IntentFilter mReceivFilter = new IntentFilter();
        mReceivFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mBroadcastReceiv, mReceivFilter);
    }

    protected abstract void setRootView();

    protected abstract void findView();

    protected abstract void initData();

    protected abstract void initBroadCastReceiver();

    protected abstract void initListener();

    protected abstract void initView(Intent intent);

    protected abstract void showPopMiniView();

    protected abstract void handleMesage(Message msg);

    protected abstract void onNetworkChanged(boolean connected);


    @Override
    public void onDestroy(){
        super.onDestroy();
        logger.info(TAG+":onDestroy");
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyMemberKilledHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveForceReloginHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveOnLineStatusChangedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveLoginResponseHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyLivingIncommingHandler);
        unregisterReceiver(mBroadcastReceiv);
    }

    /**
     * 重置状态机
     */
    protected void revertStateMachine(){

        if(MyApplication.instance.getVideoLivePushingState() != VideoLivePushingState.IDLE){
            MyTerminalFactory.getSDK().getLiveManager().ceaseLiving();
        }
        if(MyApplication.instance.getVideoLivePlayingState() != VideoLivePlayingState.IDLE){
            MyTerminalFactory.getSDK().getLiveManager().ceaseWatching();
        }
        Log.d("BaseService", "MyApplication.instance.getIndividualState():" + MyApplication.instance.getIndividualState());
        if(MyApplication.instance.getIndividualState() != IndividualCallState.IDLE){
            MyTerminalFactory.getSDK().getIndividualCallManager().ceaseIndividualCall();
        }
    }

    /**
     * 移除view并停止service
     */
    protected void removeView(){
        logger.info(TAG+"--ReceiverRemoveWindowViewHandler:"+dialogAdd);
        mHandler.removeCallbacksAndMessages(null);
        if(dialogAdd){
            windowManager.removeView(rootView);
            dialogAdd = false;
            MyApplication.instance.viewAdded = false;
        }
        MyApplication.instance.isMiniLive = false;
        PromptManager.getInstance().stopRing();
        MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiverRemoveWindowViewHandler.class,this.getClass().getSimpleName());
        stopSelf();
    }

    /**
     * 退出业务状态
     */
    protected void stopBusiness(){
        SensorUtil.getInstance().unregistSensor();
        revertStateMachine();
        removeView();
    }

    /**
     * 组成员遥毙消息
     */
    private ReceiveNotifyMemberKilledHandler receiveNotifyMemberKilledHandler = forbid -> {
        logger.info("收到遥毙，此时forbid" + forbid);
        if (forbid) {
            mHandler.post(() -> {
                PromptManager.getInstance().stopRing();
                MyTerminalFactory.getSDK().getLiveManager().ceaseLiving();
                MyTerminalFactory.getSDK().getLiveManager().ceaseWatching();
                MyTerminalFactory.getSDK().getIndividualCallManager().ceaseIndividualCall();
                removeView();
            });
        }
    };

    /**
     * 服务端通知强制重新认证登陆
     */
    private ReceiveForceReloginHandler receiveForceReloginHandler = version -> {
        ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.net_work_disconnect));
        removeView();
    };

    /**
     * 在线状态
     */
    private ReceiveOnLineStatusChangedHandler receiveOnLineStatusChangedHandler = connected -> {
        if(!connected){
            ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.net_work_disconnect));
        }
        mHandler.post(()-> onNetworkChanged(connected));
    };

    /**
     * 登陆响应
     */
    private ReceiveLoginResponseHandler receiveLoginResponseHandler = (resultCode, resultDesc) -> mHandler.post(() -> {
        if(resultCode != BaseCommonCode.SUCCESS_CODE){
            removeView();
        }
    });

    private BroadcastReceiver mBroadcastReceiv = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent){
            String action = intent.getAction();
            if(null == action){
                return;
            }
            if(action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)){
                String reason = intent.getStringExtra(Constants.SYSTEM_DIALOG_REASON_KEY);
                if(Constants.SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason) || Constants.SYSTEM_DIALOG_REASON_RECENT_APPS.equals(reason)){
                    showPopMiniView();
                }
            }
        }
    };
    /**
     * 收到上报图像的通知
     */
    private ReceiveNotifyLivingIncommingHandler receiveNotifyLivingIncommingHandler = (mainMemberName, mainMemberId, emergencyType) -> {
        //强制上报
        if(emergencyType){
            SensorUtil.getInstance().unregistSensor();

            if(MyApplication.instance.getVideoLivePlayingState() != VideoLivePlayingState.IDLE){
                MyTerminalFactory.getSDK().getLiveManager().ceaseWatching();
            }
            Log.d("BaseService", "MyApplication.instance.getIndividualState():" + MyApplication.instance.getIndividualState());
            if(MyApplication.instance.getIndividualState() != IndividualCallState.IDLE){
                MyTerminalFactory.getSDK().getIndividualCallManager().ceaseIndividualCall();
            }
            removeView();
        }
    };
}

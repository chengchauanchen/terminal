package com.vsxin.terminalpad.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.app.PadApplication;
import com.vsxin.terminalpad.prompt.PromptManager;
import com.vsxin.terminalpad.utils.SensorUtil;

import org.apache.log4j.Logger;

import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallState;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveForceOfflineHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveForceReloginHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveLoginResponseHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyEmergencyMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyMemberKilledHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveOnLineStatusChangedHandler;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.service.KeepLiveManager;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * 作者：xuxiaolong
 * 版本：1.0
 * 创建日期：2018/12/29
 * 描述：
 * 修订历史：
 */
public abstract class BaseService extends Service {

    protected Logger logger = Logger.getLogger(this.getClass());
    protected final String TAG = this.getClass().getSimpleName();
    protected WindowManager windowManager;
    protected WindowManager.LayoutParams layoutParams;
    protected WindowManager.LayoutParams layoutParams1;
    protected int screenWidth;
    protected int screenHeight;
    protected PowerManager.WakeLock wakeLock;
    protected View rootView;
    protected LinearLayout mLlNoNetwork;
    protected boolean dialogAdd;//是否添加了弹窗
    public static final int OFF_LINE_TIME = 60*1000;
    protected static final int OFF_LINE = 55;


    @SuppressLint("HandlerLeak")
    protected Handler mHandler = new Handler(Looper.getMainLooper()){
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
        initBroadCastReceiver();
        MyTerminalFactory.getSDK().registReceiveHandler(receiveForceOfflineHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyMemberKilledHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveForceReloginHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveOnLineStatusChangedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveLoginResponseHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyEmergencyMessageHandler);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        KeepLiveManager.getInstance().setServiceForeground(this);

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
        layoutParams.gravity = Gravity.END| Gravity.TOP;
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

        setScreenWidth();
    }

    protected void setScreenWidth(){
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
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

//    protected  void setCommonView(){
//        if(rootView!=null){
//            mLlNoNetwork = rootView.findViewById(R.id.ll_no_network);
//        }
//    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        logger.info(TAG+":onDestroy");
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveForceOfflineHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyMemberKilledHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveForceReloginHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveOnLineStatusChangedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveLoginResponseHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyEmergencyMessageHandler);
    }

    /**
     * 重置状态机
     */
    protected void revertStateMachine(){

        if(PadApplication.getPadApplication().getIndividualState() != IndividualCallState.IDLE){
            MyTerminalFactory.getSDK().getIndividualCallManager().ceaseIndividualCall();
        }
    }

    /**
     * 移除view并停止service
     */
    protected void removeView(){
        logger.info(TAG+"--ReceiverRemoveWindowViewHandler:"+dialogAdd);
        mHandler.removeCallbacksAndMessages(null);
        PromptManager.getInstance().stopRing();
        stopSelf();
    }

    /**
     * 退出业务状态
     */
    protected void stopBusiness(){
        PromptManager.getInstance().stopRing();
        SensorUtil.getInstance().unregistSensor();
        revertStateMachine();
        mHandler.post(this::removeView);
    }

    private ReceiveForceOfflineHandler receiveForceOfflineHandler = () -> {
        mHandler.post(this::stopBusiness);
    };

    /**
     * 组成员遥毙消息
     */
    private ReceiveNotifyMemberKilledHandler receiveNotifyMemberKilledHandler = forbid -> {
        logger.info("收到遥毙，此时forbid" + forbid);
        if (forbid) {
            mHandler.post(() -> {
                PromptManager.getInstance().stopRing();
                revertStateMachine();
                removeView();
            });
        }
    };

    /**
     * 服务端通知强制重新认证登陆
     */
    private ReceiveForceReloginHandler receiveForceReloginHandler = version -> {
        ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.net_work_disconnect));
        mHandler.post(() -> removeView());
    };

    /**
     * 在线状态
     */
    private ReceiveOnLineStatusChangedHandler receiveOnLineStatusChangedHandler = connected -> {
        mHandler.post(()-> {
            if(mLlNoNetwork!=null){
                mLlNoNetwork.setVisibility((!connected)? View.VISIBLE: View.GONE);
            }else{
                if(!connected){
                    ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.net_work_disconnect));
                }
            }
            onNetworkChanged(connected);
        });
    };

    /**
     * 登陆响应
     */
    private ReceiveLoginResponseHandler receiveLoginResponseHandler = (resultCode, resultDesc) -> mHandler.post(() -> {
        if(resultCode != BaseCommonCode.SUCCESS_CODE){
            removeView();
        }
    });

    /**
     * 收到强制停止的通知
     */
    private ReceiveNotifyEmergencyMessageHandler receiveNotifyEmergencyMessageHandler = this::stopBusiness;


}

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
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import org.apache.log4j.Logger;

import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyMemberKilledHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSendUuidResponseHandler;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.receiveHandle.ReceiverRemoveWindowViewHandler;
import cn.vsx.vc.utils.Constants;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * 作者：xuxiaolong
 * 版本：1.0
 * 创建日期：2018/12/29
 * 描述：
 * 修订历史：
 */
public abstract class BaseService extends Service{

    protected Logger logger = Logger.getLogger(this.getClass());
    protected WindowManager windowManager;
    protected WindowManager.LayoutParams layoutParams;
    protected WindowManager.LayoutParams layoutParams1;
    protected int screenWidth;
    protected PowerManager.WakeLock wakeLock;
    protected View rootView;
    private boolean dialogAdd;//是否添加了弹窗

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
        MyTerminalFactory.getSDK().registReceiveHandler(receiveSendUuidResponseHandler);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        if(!dialogAdd){
            windowManager.addView(rootView, layoutParams1);
            MyApplication.instance.viewAdded = true;
            dialogAdd = true;
            initView(intent);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @SuppressLint("InvalidWakeLockTag")
    protected void initWakeLock(){
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if(null != powerManager){
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "wakeLock");
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
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiv, mReceivFilter);
    }

    protected abstract void setRootView();

    protected abstract void findView();

    protected abstract void initData();

    protected abstract void initBroadCastReceiver();

    protected abstract void initListener();

    protected abstract void initView(Intent intent);

    protected abstract void showPopMiniView();

    protected abstract void handleMesage(Message msg);


    @Override
    public void onDestroy(){
        super.onDestroy();
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyMemberKilledHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveSendUuidResponseHandler);
    }

    protected void removeView(){
        mHandler.removeCallbacksAndMessages(null);
        if(dialogAdd){
            windowManager.removeView(rootView);
            dialogAdd = false;
            MyApplication.instance.viewAdded = false;
        }
        MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiverRemoveWindowViewHandler.class);
        stopSelf();
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
     * 信令服务发送NotifyForceRegisterMessage消息时，先去reAuth(false)，然后login()
     */
    private ReceiveSendUuidResponseHandler receiveSendUuidResponseHandler = (resultCode, resultDesc, isRegisted) -> {
        if (resultCode == BaseCommonCode.SUCCESS_CODE) {
            MyTerminalFactory.getSDK().getLiveManager().ceaseLiving();
            MyTerminalFactory.getSDK().getLiveManager().ceaseWatching();
            MyTerminalFactory.getSDK().getIndividualCallManager().ceaseIndividualCall();
            removeView();
        }
    };

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
}

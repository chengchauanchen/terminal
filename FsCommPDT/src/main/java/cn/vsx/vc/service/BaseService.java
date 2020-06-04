package cn.vsx.vc.service;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
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
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.alibaba.fastjson.JSONObject;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.common.MessageSendStateEnum;
import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.common.util.NoCodec;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePlayingState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePushingState;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveDeparmentChangeHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveForceOfflineHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveForceReloginHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveLoginResponseHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberDeleteHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyEmergencyMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyLivingIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyMemberKilledHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveOnLineStatusChangedHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.TransparentActivity;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.jump.utils.AppKeyUtils;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.receiveHandle.ReceiverRemoveWindowViewHandler;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.MyDataUtil;
import cn.vsx.vc.utils.SensorUtil;
import cn.vsx.vc.utils.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.receiveHandler.ReceiverBusinessInServiceStatusHandler;
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
        initHomeBroadCastReceiver();
        initBroadCastReceiver();
        MyTerminalFactory.getSDK().registReceiveHandler(receiveForceOfflineHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveMemberDeleteHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyMemberKilledHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveForceReloginHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveOnLineStatusChangedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveLoginResponseHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyLivingIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyEmergencyMessageHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveDeparmentChangeHandler);

        TerminalFactory.getSDK().notifyReceiveHandler(ReceiverBusinessInServiceStatusHandler.class,this.getClass().getSimpleName(),true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        KeepLiveManager.getInstance().setServiceForeground(this);
        logger.info(TAG+"---onStartCommand--"+dialogAdd+"-intent-"+(null != intent));
        if(!dialogAdd){
            windowManager.addView(rootView, layoutParams1);
            MyApplication.instance.viewAdded = true;
            dialogAdd = true;
            if(null != intent){
                initView(intent);
            }else{
                stopBusiness();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }


    protected void initWakeLock(){
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if(null != powerManager){
            //noinspection deprecation
            wakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "vsx:BaseServiceTag");
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

        setScreenWidth();
    }

    protected void setScreenWidth(){
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
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

//    protected  void setCommonView(){
//        if(rootView!=null){
//            mLlNoNetwork = rootView.findViewById(R.id.ll_no_network);
//        }
//    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        logger.info(TAG+":onDestroy");
        TerminalFactory.getSDK().notifyReceiveHandler(ReceiverBusinessInServiceStatusHandler.class,this.getClass().getSimpleName(),false);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveForceOfflineHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMemberDeleteHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyMemberKilledHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveForceReloginHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveOnLineStatusChangedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveLoginResponseHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyLivingIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyEmergencyMessageHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveDeparmentChangeHandler);
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
            ceaseWatching();
        }
        if(MyApplication.instance.getIndividualState() != IndividualCallState.IDLE){
            MyTerminalFactory.getSDK().getIndividualCallManager().ceaseIndividualCall();
        }
        //如果切组是在听组呼，先停止
        if(MyApplication.instance.getGroupListenenState() != GroupCallListenState.IDLE){
            TerminalFactory.getSDK().getGroupCallManager().getGroupCallListenStateMachine().cleanListen(false);
        }
    }

    /**
     * 移除view并停止service
     */
    protected void removeView(){
        try{
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
            finishTransparentActivity();

            AppKeyUtils.setAppKey(null);//退出业务将appKey重置
        }catch (Exception e){
            e.printStackTrace();
        }
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

    //成员被删除了
    private ReceiveMemberDeleteHandler receiveMemberDeleteHandler = new ReceiveMemberDeleteHandler() {
        @Override
        public void handler() {
            mHandler.post(() -> {
                PromptManager.getInstance().stopRing();
                revertStateMachine();
                removeView();
            });
        }
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
        if(MyApplication.instance.getVideoLivePushingState() != VideoLivePushingState.IDLE
                &&MyApplication.instance.getVideoLivePlayingState() != VideoLivePlayingState.IDLE){
            ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.net_work_disconnect));
            mHandler.post(() -> stopBusiness());
        }
    };

    /**
     * 部门修改之后，提示用户重新登录
     */
    private ReceiveDeparmentChangeHandler receiveDeparmentChangeHandler = new ReceiveDeparmentChangeHandler() {
        @Override
        public void handler() {
            mHandler.post(() -> stopBusiness());
        }
    };

    /**
     * 在线状态
     */
    private ReceiveOnLineStatusChangedHandler receiveOnLineStatusChangedHandler = connected -> {
        mHandler.post(()-> {
            if(mLlNoNetwork!=null){
                mLlNoNetwork.setVisibility((!connected)?View.VISIBLE:View.GONE);
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
            if(MyApplication.instance.getVideoLivePushingState() == VideoLivePushingState.IDLE
                    && MyApplication.instance.getVideoLivePlayingState() == VideoLivePlayingState.IDLE){
                stopBusiness();
            }
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
                    if(!MyApplication.instance.isMiniLive){
                        showPopMiniView();
                    }
                }
            }
        }
    };

    /**
     * 收到强制停止的通知
     */
    private ReceiveNotifyEmergencyMessageHandler receiveNotifyEmergencyMessageHandler = this::stopBusiness;

    /**
     * 收到上报图像的通知
     */
    private ReceiveNotifyLivingIncommingHandler receiveNotifyLivingIncommingHandler = (mainMemberName, mainMemberId, emergencyType) -> {
        //强制上报
        if(emergencyType){
            SensorUtil.getInstance().unregistSensor();
            ceaseWatching();
            Log.d("BaseService", "MyApplication.instance.getIndividualState():" + MyApplication.instance.getIndividualState());
            if(MyApplication.instance.getIndividualState() != IndividualCallState.IDLE){
                MyTerminalFactory.getSDK().getIndividualCallManager().ceaseIndividualCall();
            }
            mHandler.post(this::removeView);
        }
    };

    /**
     * 清空观看的状态
     */
    private void ceaseWatching(){
        if(MyApplication.instance.getVideoLivePlayingState() != VideoLivePlayingState.IDLE){
            if(BaseService.this instanceof PullLivingService){
                PullLivingService service = (PullLivingService) BaseService.this;
                if(service.liveMember!=null&&service.callId!=0){
                    MyTerminalFactory.getSDK().getLiveManager().ceaseWatching(service.liveMember.id,service.callId,service.liveMember.getUniqueNo());
                }else{
                    MyTerminalFactory.getSDK().getLiveManager().ceaseWatching();
                }
            }else {
                MyTerminalFactory.getSDK().getLiveManager().ceaseWatching();
            }
        }
    }
    /**
     * 在组内发一条消息
     */
    public void sendGroupMessage(String streamMediaServerIp, int streamMediaServerPort, long callId,List<String> pushMemberList,boolean isGroupPushLive) {
        TerminalFactory.getSDK().getThreadPool().execute(() -> {
            List<Group> list = MyDataUtil.checkIsGroupPush(pushMemberList);
            if(!list.isEmpty()){

                int memberId = MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
                long memberUniqueNo = MyTerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO, 0L);
                String memberName = MyTerminalFactory.getSDK().getParam(Params.MEMBER_NAME, "");

                String sdp = TerminalFactory.getSDK().getLiveManager().getLivePathSdp();
                String url = "rtsp://"+streamMediaServerIp+":"+streamMediaServerPort+"/"+memberUniqueNo+"_"+callId+sdp;
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(JsonParam.SEND_STATE, MessageSendStateEnum.SEND_PRE);
                jsonObject.put(JsonParam.TOKEN_ID, MyTerminalFactory.getSDK().getMessageSeq());
//        jsonObject.put(JsonParam.DOWN_VERSION_FOR_FAIL, lastVersion);
                jsonObject.put(JsonParam.CALLID, String.valueOf(callId));
                jsonObject.put(JsonParam.REMARK, 2);
                jsonObject.put(JsonParam.LIVER, memberUniqueNo+"_"+memberName);
                jsonObject.put(JsonParam.LIVERNO, memberId);
                jsonObject.put(JsonParam.BACKUP, memberId+"_"+memberName);
                jsonObject.put(JsonParam.EASYDARWIN_RTSP_URL, url);
                List<TerminalMessage> messages = new ArrayList<>();
                for (Group group: list) {
                    TerminalMessage mTerminalMessage = new TerminalMessage();
                    mTerminalMessage.messageFromId = MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
                    mTerminalMessage.messageFromName = memberName;
                    mTerminalMessage.messageToId =NoCodec.encodeGroupNo(group.getNo());
                    mTerminalMessage.messageToName = group.getName();
                    mTerminalMessage.messageBody = jsonObject;
                    mTerminalMessage.sendTime = System.currentTimeMillis();
                    mTerminalMessage.messageType = MessageType.VIDEO_LIVE.getCode();
                    mTerminalMessage.messageUrl = url;
                    TerminalMessage terminalMessage1 = (TerminalMessage) mTerminalMessage.clone();
                    MyTerminalFactory.getSDK().getTerminalMessageManager().uploadDataByDDPUSH("", terminalMessage1);
//                    messages.add(mTerminalMessage);
                }
//                MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiverGroupPushLiveHandler.class, messages);
            }
        });
    }

    private void finishTransparentActivity(){
        Intent intent = new Intent(Constants.FINISH_TRANSPARENT);
        sendBroadcast(intent);
    }

    /**
     * 设置是否免提
     * @param result
     */
    protected void setSpeakPhoneOn(ImageView imageView, boolean result){
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

    protected void startTranspantActivity(){
        //判断是否锁屏
        KeyguardManager mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if(null != mKeyguardManager){
            boolean flag = mKeyguardManager.inKeyguardRestrictedInputMode();
            if(flag){
                //                //无屏保界面
                if(MyTerminalFactory.getSDK().getParam(Params.LOCK_SCREEN_HIDE_OR_SHOW, 0) != 1){
                    Intent intent = new Intent(BaseService.this, TransparentActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    startActivity(intent);
                }
            }
        }
    }

    /**
     * 显示加载UI
     */
    protected void showLoadingView(LinearLayout mLlRefreshing,ImageView mRefreshingIcon){
        if(mLlRefreshing!=null && mRefreshingIcon!=null && mLlRefreshing.getVisibility() == View.GONE){
            mRefreshingIcon.setVisibility(View.VISIBLE);
            RotateAnimation refreshingAnimation = (RotateAnimation) AnimationUtils.loadAnimation(
                    this, R.anim.rotating_refreshing);
            refreshingAnimation.setInterpolator(new LinearInterpolator());
            mRefreshingIcon.startAnimation(refreshingAnimation);
            mLlRefreshing.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 隐藏加载UI
     */
    protected void dismissLoadingView(LinearLayout mLlRefreshing,ImageView mRefreshingIcon){
        if(mLlRefreshing!=null && mRefreshingIcon!=null){
            mRefreshingIcon.clearAnimation();
            mRefreshingIcon.setVisibility(View.GONE);
            mLlRefreshing.setVisibility(View.GONE);
        }
    }
}

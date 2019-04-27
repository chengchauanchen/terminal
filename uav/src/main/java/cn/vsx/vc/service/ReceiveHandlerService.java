package cn.vsx.vc.service;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.xuchongyang.easyphone.callback.PhoneCallback;
import com.xuchongyang.easyphone.callback.RegistrationCallback;
import com.zectec.imageandfileselector.utils.FileUtil;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import org.apache.http.util.TextUtils;
import org.apache.log4j.Logger;
import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneCall;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.MessageCategory;
import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.Remark;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.TerminalErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallState;
import cn.vsx.hamster.terminalsdk.manager.terminal.TerminalState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePlayingState;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveAirCraftStatusChangedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCurrentGroupIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyDataMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyEmergencyIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyEmergencyMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyEmergencyVideoLiveIncommingMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyIndividualCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyLivingIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveVolumeOffCallHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.SignatureUtil;
import cn.vsx.hamster.terminalsdk.tools.Util;
import cn.vsx.util.StateMachine.IState;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.GroupCallNewsActivity;
import cn.vsx.vc.activity.IndividualNewsActivity;
import cn.vsx.vc.activity.LiveHistoryActivity;
import cn.vsx.vc.activity.NewMainActivity;
import cn.vsx.vc.adapter.StackViewAdapter;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.dialog.ProgressDialog;
import cn.vsx.vc.model.PushLiveMemberList;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.receiveHandle.ReceiveGoWatchRTSPHandler;
import cn.vsx.vc.receiveHandle.ReceiveVoipCallEndHandler;
import cn.vsx.vc.receiveHandle.ReceiveVoipConnectedHandler;
import cn.vsx.vc.receiveHandle.ReceiveVoipErrorHandler;
import cn.vsx.vc.receiveHandle.ReceiverActivePushVideoHandler;
import cn.vsx.vc.receiveHandle.ReceiverRequestVideoHandler;
import cn.vsx.vc.receiver.NotificationClickReceiver;
import cn.vsx.vc.utils.ActivityCollector;
import cn.vsx.vc.utils.AirCraftUtil;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.view.flingswipe.SwipeFlingAdapterView;
import dji.common.battery.BatteryState;
import dji.common.error.DJIError;
import dji.common.flightcontroller.ConnectionFailSafeBehavior;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.common.util.CommonCallbacks;
import dji.sdk.battery.Battery;
import dji.sdk.products.Aircraft;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.service.KeepLiveManager;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/1/4
 * 描述：
 * 修订历史：
 */
public class ReceiveHandlerService extends Service{
    private FrameLayout view;
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams2;//弹窗

    private boolean dialogAdded;
    private ReceiveHandlerBinder receiveHandlerBinder = new ReceiveHandlerBinder();

    private RelativeLayout video_dialog;
    private StackViewAdapter stackViewAdapter;

    private static final int DISSMISS_CURRENT_DIALOG = 6;
    private static final int WATCH_LIVE = 7;
    private Logger logger = Logger.getLogger(this.getClass());
    private List<TerminalMessage> data = new ArrayList<>();
    //记录紧急观看的CallId，防止PC端重复发送强制观看的消息
    private String emergencyCallId ;

    private ProgressDialog myProgressDialog;
    private long uploadTime;
    //弹窗
    @Bind(R.id.swipeFlingAdapterView)
    SwipeFlingAdapterView swipeFlingAdapterView;

    /**
     * 搜索到的结果集合
     */
    @SuppressWarnings("HandlerLeak,SimpleDateFormat")
    private Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch(msg.what){

                case DISSMISS_CURRENT_DIALOG:
                    TerminalMessage terminalMessage = (TerminalMessage) msg.obj;
                    if(dialogAdded && null != stackViewAdapter && data.contains(terminalMessage)){
                        data.remove(terminalMessage);
                        stackViewAdapter.setData(data);
                    }
                    break;
                case WATCH_LIVE:
                    TerminalMessage terminalMessage1 = (TerminalMessage) msg.obj;
                    int position = msg.arg1;
                    if(position>=0){
                        data.remove(stackViewAdapter.getItem(position));
                        stackViewAdapter.remove(position);
                        video_dialog.setVisibility(View.GONE);
                        if(data.size() ==0){
                            removeView();
                        }
                    }
                    Intent intent = new Intent(ReceiveHandlerService.this,PullLivingService.class);
                    intent.putExtra(Constants.WATCH_TYPE,Constants.ACTIVE_WATCH);
                    intent.putExtra(Constants.TERMINALMESSAGE,terminalMessage1);
                    startService(intent);
                    break;
                default:
                    break;
            }
        }
    };


    public class ReceiveHandlerBinder extends Binder{}

    @Nullable
    @Override
    public IBinder onBind(Intent intent){
        KeepLiveManager.getInstance().setServiceForeground(this);
//        MyTerminalFactory.getSDK().getThreadPool().execute(() -> MyTerminalFactory.getSDK().start());
        return receiveHandlerBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        return START_STICKY;
    }

    public ReceiveHandlerService(){
        super();
    }

    @SuppressLint({"WrongConstant", "InvalidWakeLockTag"})
    @Override
    public void onCreate(){
        super.onCreate();
        createFloatView();
        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveVolumeOffCallHandler.class, true, 1);
        //个呼监听
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyIndividualCallIncommingHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiveCurrentGroupIndividualCallHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyEmergencyIndividualCallHandler);
        //视频上报监听
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGoWatchRTSPHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyLivingIncommingHandler);//请求开视频
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiverActivePushVideoHandler);//上报视频
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiverRequestVideoHandler);//请求视频
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveNotifyDataMessageHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyEmergencyVideoLiveIncommingMessageHandler);
        //开启voip电话服务
        MyTerminalFactory.getSDK().getVoipCallManager().startService(MyTerminalFactory.getSDK().application);
        //监听voip来电
        MyTerminalFactory.getSDK().getVoipCallManager().addCallback(voipRegistrationCallback,voipPhoneCallback);

        if (myProgressDialog == null) {
            myProgressDialog = new ProgressDialog(MyApplication.instance.getApplicationContext());
            myProgressDialog.setCancelable(true);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        myHandler.removeCallbacksAndMessages(null);
        MyTerminalFactory.getSDK().stop();
    }

    @SuppressWarnings("ClickableViewAccessibility,InflateParams")
    public void createFloatView(){
        view = (FrameLayout) LayoutInflater.from(MyTerminalFactory.getSDK().application).inflate(R.layout.layout_receive_handler, null);
        video_dialog = view.findViewById(R.id.video_dialog);
        ButterKnife.bind(this, view);
        windowManager = (WindowManager) this.getSystemService(WINDOW_SERVICE);
        //弹窗
        layoutParams2 = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.RGBA_8888);
        //type为"TYPE_TOAST"在sdk19之前不接收事件,之后可以
        //type为"TYPE_PHONE"需要"SYSTEM_ALERT_WINDOW"权限.在sdk19之前不可以直接申明使用
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            layoutParams2.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else{
            layoutParams2.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams2.gravity = Gravity.CENTER;
        layoutParams2.flags = WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
    }


    private void hideAllView(){
        video_dialog.setVisibility(View.GONE);
    }

    private void removeView(){
        if(dialogAdded){
            data.clear();
            windowManager.removeView(view);
            dialogAdded = false;
            PromptManager.getInstance().stopRing();
        }
    }

    private Runnable updateAircraftLocation = () -> {
        if(MyApplication.instance.aircraftConnected){
            Aircraft aircraft = AirCraftUtil.getAircraftInstance();
            if(null != aircraft){
                LocationCoordinate3D location = aircraft.getFlightController().getState().getAircraftLocation();
                logger.info("location.getLatitude():"+location.getLatitude());
                logger.info("location.getLongitude():"+location.getLongitude());
                logger.info("location.getAltitude():"+location.getAltitude());

                updateAircraftLocation(location.getLatitude(),location.getLongitude(),location.getAltitude());
            }
        }
    };

    private RegistrationCallback voipRegistrationCallback = new RegistrationCallback(){
        @Override
        public void registrationOk(){
            super.registrationOk();
            MyTerminalFactory.getSDK().putParam(Params.VOIP_SUCCESS,true);
            logger.info("voip注册成功");
        }

        @Override
        public void registrationFailed(){
            super.registrationFailed();
            MyTerminalFactory.getSDK().putParam(Params.VOIP_SUCCESS,false);
            logger.error("voip注册失败");
        }
    };

    private PhoneCallback voipPhoneCallback = new PhoneCallback(){
        @Override
        public void incomingCall(LinphoneCall linphoneCall){
            super.incomingCall(linphoneCall);
            //将状态机至于正在个呼状态
            int code = TerminalFactory.getSDK().getTerminalStateManager().openFunction(TerminalState.INDIVIDUAL_CALLING, IndividualCallState.IDLE);
            if(code == BaseCommonCode.SUCCESS_CODE){
                //将个呼状态机移动到响铃中
                if (TerminalFactory.getSDK().getIndividualCallManager().getIndividualCallStateMachine().moveToState(IndividualCallState.RINGING)) {
                    TerminalFactory.getSDK().getTerminalStateManager().moveToState(TerminalState.INDIVIDUAL_CALLING, IndividualCallState.RINGING);
                }
                MyApplication.instance.linphoneCall = linphoneCall;
                Intent intent = new Intent(ReceiveHandlerService.this,ReceiveVoipService.class);
                LinphoneAddress from = linphoneCall.getCallLog().getFrom();
                String userName = from.getUserName();
                intent.putExtra(Constants.USER_NAME,userName);
                startService(intent);
            }
        }
        @Override
        public void callConnected(LinphoneCall linphoneCall){
            super.callConnected(linphoneCall);
            MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveVoipConnectedHandler.class,linphoneCall);
        }

        @Override
        public void callEnd(LinphoneCall linphoneCall){
            super.callEnd(linphoneCall);
            //电话接通之后挂断，还有主叫拨号时挂断
            MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveVoipCallEndHandler.class,linphoneCall);
        }

        @Override
        public void error(LinphoneCall linphoneCall){
            super.error(linphoneCall);
            //被叫收到来电，挂断电话，主叫会回调此方法
            MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveVoipErrorHandler.class,linphoneCall);
        }
    };

    /**
     * 关闭当前弹窗
     */
    private final class OnClickListenerCloseDialog implements StackViewAdapter.CloseDialogListener{

        @Override
        public void onCloseDialogClick(int position){
            data.remove(stackViewAdapter.getItem(position));
            stackViewAdapter.remove(position);
            if(data.isEmpty()){
                removeView();
            }
        }
    }

    /**
     * 上报图像或者去看警情
     **/
    private final class OnClickListenerGoWatch implements StackViewAdapter.GoWatchListener{
        @Override
        public void onGoWatchClick(final int position){
            if(position > data.size() - 1){
                return;
            }
            final TerminalMessage terminalMessage = stackViewAdapter.getItem(position);
            goToWatch(terminalMessage,position);
        }
    }

    /**
     * 去观看
     */
    private void goToWatch(TerminalMessage terminalMessage,int position){
        //判断是否有接受图像功能权限
        if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_ACCEPT.name())){
            ToastUtil.showToast(MyTerminalFactory.getSDK().application, getString(R.string.text_has_no_video_receiver_authority));
            removeView();
            return;
        }
        //判断消息类型
        if(terminalMessage.messageType == MessageType.VIDEO_LIVE.getCode()){
            MyTerminalFactory.getSDK().getThreadPool().execute(() -> {
                String serverIp = MyTerminalFactory.getSDK().getParam(Params.FILE_SERVER_IP, "");
                int serverPort = MyTerminalFactory.getSDK().getParam(Params.FILE_SERVER_PORT, 0);
                String url = "http://" + serverIp + ":" + serverPort + "/file/download/isLiving";
                Map<String, String> paramsMap = new HashMap<>();
                paramsMap.put("callId", terminalMessage.messageBody.getString(JsonParam.CALLID));
                paramsMap.put("sign", SignatureUtil.sign(paramsMap));
                logger.info("查看视频播放是否结束url：" + url);
                String result = MyTerminalFactory.getSDK().getHttpClient().sendGet(url, paramsMap);
                logger.info("查看视频播放是否结束结果：" + result);
                if(!Util.isEmpty(result)){
                    JSONObject jsonObject = JSONObject.parseObject(result);
                    boolean living = jsonObject.getBoolean("living");
                    if(living){
                        Message msg = Message.obtain();
                        msg.what = WATCH_LIVE;
                        msg.obj = terminalMessage;
                        msg.arg1 = position;
                        myHandler.sendMessage(msg);
                    }else{
                        removeView();
                        Intent intent = new Intent(MyTerminalFactory.getSDK().application, LiveHistoryActivity.class);
                        intent.putExtra("terminalMessage", terminalMessage);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        //                                intent.putExtra("endChatTime",endChatTime);
                        startActivity(intent);
                    }
                }
            });
        }else if(terminalMessage.messageType == MessageType.WARNING_INSTANCE.getCode()){
            // TODO: 2018/5/4 去看警情
        }else if(terminalMessage.messageType == MessageType.GB28181_RECORD.getCode()){
            goWatchGB28121(terminalMessage);
        }
    }

    /**
     * 被动方个呼来了，选择接听或挂断
     */
    private ReceiveNotifyIndividualCallIncommingHandler receiveNotifyIndividualCallIncommingHandler = (mainMemberName, mainMemberId, individualCallType) -> {

        Intent individualCallIntent = new Intent(ReceiveHandlerService.this, ReceiveCallComingService.class);
        individualCallIntent.putExtra(Constants.MEMBER_NAME, mainMemberName);
        individualCallIntent.putExtra(Constants.MEMBER_ID, mainMemberId);
        individualCallIntent.putExtra(Constants.CALL_TYPE, individualCallType);
        individualCallIntent.putExtra(Constants.TYPE, Constants.RECEIVE_CALL);
        startService(individualCallIntent);
    };

    /**
     * 主动发起个呼
     */
    private ReceiveCurrentGroupIndividualCallHandler receiveCurrentGroupIndividualCallHandler = (member) -> {
        logger.info("当前呼叫对象:" + member);
        Intent intent = new Intent(ReceiveHandlerService.this, StartIndividualCallService.class);
        intent.putExtra(Constants.MEMBER_NAME, member.getName());
        intent.putExtra(Constants.MEMBER_ID, member.getNo());
        intent.putExtra(Constants.UNIQUE_NO, member.getUniqueNo());
        startService(intent);

    };

    /**
     * 紧急个呼时，被动方强制接听
     */
    @SuppressWarnings("unused")
    private ReceiveNotifyEmergencyIndividualCallHandler receiveNotifyEmergencyIndividualCallHandler = mainMemberId -> {
    };


    @Override
    public boolean onUnbind(Intent intent){
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyIndividualCallIncommingHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiveCurrentGroupIndividualCallHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyEmergencyIndividualCallHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGoWatchRTSPHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyLivingIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveNotifyDataMessageHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyEmergencyVideoLiveIncommingMessageHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiverActivePushVideoHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiverRequestVideoHandler);

        removeView();
        return super.onUnbind(intent);
    }



    /**
     * 收到别人请求我开启直播的通知
     **/
    @SuppressWarnings("unchecked")
    private ReceiveNotifyLivingIncommingHandler receiveNotifyLivingIncommingHandler = (mainMemberName, mainMemberId, emergencyType) -> myHandler.post(() -> {
        Intent intent = new Intent();
        intent.putExtra(Constants.MEMBER_NAME, mainMemberName);
        intent.putExtra(Constants.MEMBER_ID, mainMemberId);
        intent.putExtra(Constants.THEME,"");
        if(Constants.HYTERA.equals(Build.MODEL)){
            logger.info("usbAttached ：" + MyApplication.instance.usbAttached);
            if(MyApplication.instance.usbAttached){
                logger.info("海能达手台，外置摄像头开启,使用外置摄像头上报");
                MyApplication.instance.isPrivateCallOrVideoLiveHand = true;
                intent.setClass(ReceiveHandlerService.this, UVCPushService.class);
                startService(intent);

            }else{
                //外置摄像头没连接，弹窗选择是否用执法记录仪
                intent.setClass(ReceiveHandlerService.this,SwitchCameraService.class);
                intent.putExtra(Constants.CAMERA_TYPE,Constants.RECODER_CAMERA);
                intent.putExtra(Constants.TYPE,Constants.RECEIVE_PUSH);
                startService(intent);
            }
        }else{
            if(emergencyType){
                //强制上报图像
                //如果在组呼或者听组呼时  就停止
                Map<TerminalState, IState<?>> currentStateMap = TerminalFactory.getSDK().getTerminalStateManager().getCurrentStateMap();
                if(currentStateMap.containsKey(TerminalState.GROUP_CALL_LISTENING)||currentStateMap.containsKey(TerminalState.GROUP_CALL_SPEAKING)){
                    TerminalFactory.getSDK().getGroupCallManager().ceaseGroupCall();
                }
                myHandler.post(() -> PromptManager.getInstance().startReportByNotity());
                MyApplication.instance.isPrivateCallOrVideoLiveHand = true;
                intent.setClass(ReceiveHandlerService.this, PhonePushService.class);
                intent.putExtra(Constants.TYPE,Constants.RECEIVE_PUSH);
                myHandler.postDelayed(() -> startService(intent),1000);
            }else{
                intent.setClass(ReceiveHandlerService.this, ReceiveLiveCommingService.class);
                startService(intent);
            }
        }
    });

    private ReceiveGoWatchRTSPHandler receiveGoWatchRTSPHandler = terminalMessage -> myHandler.post(() -> goWatchGB28121(terminalMessage));

    private void goWatchGB28121(TerminalMessage terminalMessage){
        Intent intent = new Intent(ReceiveHandlerService.this,PullGB28181Service.class);
        intent.putExtra(Constants.TERMINALMESSAGE,terminalMessage);
        startService(intent);
    }


    private long lastNotifyTime = 0;
    /**
     * 接收到消息
     */
    @SuppressWarnings("unchecked")
    private ReceiveNotifyDataMessageHandler mReceiveNotifyDataMessageHandler = terminalMessage -> {
        logger.info("接收到消息" + terminalMessage.toString());
        if(lastNotifyTime != 0 && System.currentTimeMillis() - lastNotifyTime < 500){
            return;
        }
        //是否为别人发的消息
        boolean isReceiver = terminalMessage.messageFromId != MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
        if(!isReceiver){
            return;
        }
        //收到紧急观看的消息(国标、视频)
        if((terminalMessage.messageType == MessageType.VIDEO_LIVE.getCode() && terminalMessage.messageBody.getInteger(JsonParam.REMARK) == Remark.EMERGENCY_INFORM_TO_WATCH_LIVE)||
                (terminalMessage.messageType == MessageType.GB28181_RECORD.getCode() && terminalMessage.messageBody.getInteger(JsonParam.REMARK) == Remark.EMERGENCY_INFORM_TO_WATCH_LIVE)){
            //停止一切业务，开始观看
            String callId = terminalMessage.messageBody.getString(JsonParam.CALLID);
            //过滤重复收到强制观看的消息
            if(android.text.TextUtils.isEmpty(emergencyCallId)||(!android.text.TextUtils.isEmpty(emergencyCallId)&&!android.text.TextUtils.equals(callId,emergencyCallId))){
                emergencyCallId = callId;
                Map<TerminalState, IState<?>> currentStateMap = TerminalFactory.getSDK().getTerminalStateManager().getCurrentStateMap();
                if(currentStateMap.containsKey(TerminalState.VIDEO_LIVING_PUSHING)
                        ||currentStateMap.containsKey(TerminalState.VIDEO_LIVING_PLAYING)
                        || currentStateMap.containsKey(TerminalState.INDIVIDUAL_CALLING)){
                    TerminalFactory.getSDK().notifyReceiveHandler(ReceiveNotifyEmergencyMessageHandler.class);
                }
                if(currentStateMap.containsKey(TerminalState.GROUP_CALL_LISTENING) || currentStateMap.containsKey(TerminalState.GROUP_CALL_SPEAKING)){
                    TerminalFactory.getSDK().getGroupCallManager().ceaseGroupCall();
                }
                myHandler.post(() -> PromptManager.getInstance().startPlayByNotity());
                myHandler.postDelayed(() -> goToWatch(terminalMessage,-1),1000);
            }
         return;
        }

        //判断消息类型，是否弹窗
        if(terminalMessage.messageType == MessageType.WARNING_INSTANCE.getCode() || terminalMessage.messageType == MessageType.VIDEO_LIVE.getCode() && terminalMessage.messageBody.getInteger(JsonParam.REMARK) == Remark.INFORM_TO_WATCH_LIVE){
            if(!MyApplication.instance.viewAdded && !MyApplication.instance.isPttPress){
                //延迟弹窗，否则判断是否在上报接口返回的是没有在上报
                myHandler.postDelayed(() -> {
                    data.add(terminalMessage);
                    showDialogView();
                }, 3000);
                //30s没观看就取消当前弹窗
                Message message = Message.obtain();
                message.what = DISSMISS_CURRENT_DIALOG;
                message.obj = terminalMessage;
                myHandler.sendMessageDelayed(message, 30 * 1000);
            }
        }
        if(terminalMessage.messageType == MessageType.VIDEO_LIVE.getCode() && terminalMessage.messageBody.getInteger(JsonParam.REMARK) == Remark.LIVE_WATCHING_END || terminalMessage.messageType == MessageType.VIDEO_LIVE.getCode() && terminalMessage.messageBody.getInteger(JsonParam.REMARK) == Remark.STOP_ASK_VIDEO_LIVE){
            return;
        }
        String callId = terminalMessage.messageBody.getString(JsonParam.CALLID);
        //如果是个呼但是接通了
        if(terminalMessage.messageType == MessageType.PRIVATE_CALL.getCode() && !TextUtils.isEmpty(callId)){
            return;
        }
        //判断是否在主页面的消息Fragment
        if(ActivityCollector.isActivityExist(NewMainActivity.class) && NewMainActivity.isForeground && NewMainActivity.mCurrentFragmentCode == 2){
            return;
        }
        //判断是否在个人当前聊天的Activity
        if(ActivityCollector.isActivityExist(IndividualNewsActivity.class) && IndividualNewsActivity.isForeground && terminalMessage.messageFromId == IndividualNewsActivity.mFromId){
            return;
        }
        //判断是否在组当前聊天的Activity
        if(ActivityCollector.isActivityExist(GroupCallNewsActivity.class) && GroupCallNewsActivity.isForeground && terminalMessage.messageToId == GroupCallNewsActivity.mGroupId){
            return;
        }
        createNotification(terminalMessage);
    };

    private void createNotification(TerminalMessage terminalMessage){

        //通知栏标题
        String noticeTitle;
        //通知栏内容
        String noticeContent = null;
        //通知Id
        int noticeId;
        int unReadCount;
        String unReadCountText;
        if(terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_PERSONAGE.getCode()){//个呼
            noticeTitle = terminalMessage.messageFromName;
            noticeId = terminalMessage.messageFromId;
            unReadCount = TerminalFactory.getSDK().getTerminalMessageManager().getUnReadMessageCount(terminalMessage.messageFromId, MessageCategory.MESSAGE_TO_PERSONAGE.getCode()) + 1;
        }else{//组呼
            noticeTitle = terminalMessage.messageToName;
            noticeId = terminalMessage.messageToId;
            unReadCount = TerminalFactory.getSDK().getTerminalMessageManager().getUnReadMessageCount(terminalMessage.messageToId, MessageCategory.MESSAGE_TO_GROUP.getCode()) + 1;
            //当前组消息也不通知
            if(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0) == terminalMessage.messageToId){
                return;
            }
        }
        if(unReadCount > 99){
            unReadCountText = "[99+条] ";
        }else if(unReadCount <= 0){
            unReadCountText = " ";
        }else{
            unReadCountText = "[" + unReadCount + "条] ";
        }
        if(terminalMessage.messageType == MessageType.SHORT_TEXT.getCode()){
            String content = terminalMessage.messageBody.getString(JsonParam.CONTENT);
            if(terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){
                noticeContent = terminalMessage.messageFromName + ":" + content;
            }else{
                noticeContent = content;
            }
        }
        if(terminalMessage.messageType == MessageType.LONG_TEXT.getCode()){
            String path = terminalMessage.messagePath;
            File file = new File(path);
            if(!file.exists()){
                MyTerminalFactory.getSDK().getTerminalMessageManager().setMessagePath(terminalMessage, false);
                MyTerminalFactory.getSDK().download(terminalMessage, true);
            }
            String content = FileUtil.getStringFromFile(file);
            logger.info("长文本： path:" + path + "    content:" + content);
            if(terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){
                noticeContent = String.format(getString(R.string.text_message_list_text_),terminalMessage.messageFromName,content);
            }else{
                noticeContent = content;
            }
        }
        if(terminalMessage.messageType == MessageType.PICTURE.getCode()){
            if(terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){

                noticeContent = String.format(getString(R.string.text_message_list_picture_),terminalMessage.messageFromName);
            }else{
                noticeContent = getString(R.string.text_message_list_picture);
            }
        }
        if(terminalMessage.messageType == MessageType.AUDIO.getCode()){
            if(terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){
                noticeContent = String.format(getString(R.string.text_message_list_voice_),terminalMessage.messageFromName);
            }else{
                noticeContent = getString(R.string.text_message_list_voice);
            }
        }
        if(terminalMessage.messageType == MessageType.VIDEO_CLIPS.getCode()){
            if(terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){
                noticeContent = String.format(getString(R.string.text_message_list_video_),terminalMessage.messageFromName);
            }else{
                noticeContent = getString(R.string.text_message_list_video);
            }
        }
        if(terminalMessage.messageType == MessageType.FILE.getCode()){
            if(terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){
                noticeContent = String.format(getString(R.string.text_message_list_file_),terminalMessage.messageFromName);
            }else{
                noticeContent = getString(R.string.text_message_list_file);
            }
        }
        if(terminalMessage.messageType == MessageType.POSITION.getCode()){
            if(terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){
                noticeContent = String.format(getString(R.string.text_message_list_location_),terminalMessage.messageFromName);
            }else{
                noticeContent = getString(R.string.text_message_list_location);
            }
        }
        if(terminalMessage.messageType == MessageType.AFFICHE.getCode()){
            if(terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){
                noticeContent = String.format(getString(R.string.text_message_list_notice_),terminalMessage.messageFromName);
            }else{
                noticeContent = getString(R.string.text_message_list_notice);
            }
        }
        if(terminalMessage.messageType == MessageType.WARNING_INSTANCE.getCode()){
            if(terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){
                noticeContent = String.format(getString(R.string.text_message_list_warning_),terminalMessage.messageFromName);
            }else{
                noticeContent = getString(R.string.text_message_list_warning);
            }
        }
        if(terminalMessage.messageType == MessageType.PRIVATE_CALL.getCode()){
            if(terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){
                noticeContent = String.format(getString(R.string.text_message_list_personal_call_),terminalMessage.messageFromName);
            }else{
                noticeContent = getString(R.string.text_message_list_personal_call);
            }
        }
        if(terminalMessage.messageType == MessageType.VIDEO_LIVE.getCode()){
            if(terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){
                noticeContent = String.format(getString(R.string.text_message_list_image_),terminalMessage.messageFromName);
            }else{
                noticeContent = getString(R.string.text_message_list_image);
            }
        }
        if(terminalMessage.messageType == MessageType.GROUP_CALL.getCode()){
            if(terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){
                noticeContent = String.format(getString(R.string.text_message_list_group_call_),terminalMessage.messageFromName);
            }else{
                noticeContent = getString(R.string.text_message_list_group_call);
            }
        }
        if(terminalMessage.messageType == MessageType.AUDIO.getCode()){
            if(terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){
                noticeContent = String.format(getString(R.string.text_message_list_sound_recording_),terminalMessage.messageFromName);
            }else{
                noticeContent = getString(R.string.text_message_list_sound_recording);
            }
        }
        if(terminalMessage.messageType == MessageType.HYPERLINK.getCode()){//人脸识别
            if(terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){
                noticeContent = String.format(getString(R.string.text_message_list_face_recognition_),terminalMessage.messageFromName);
            }else{
                noticeContent = getString(R.string.text_message_list_face_recognition);
            }
        }
        //合并转发
        if(terminalMessage.messageType ==  MessageType.MERGE_TRANSMIT.getCode()) {
            if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                noticeContent=String.format(getString(R.string.text_message_list_merge_transmit_),terminalMessage.messageFromName);
            }else{
                noticeContent=getString(R.string.text_message_list_merge_transmit);
            }

        }
        Intent intent = new Intent(MyTerminalFactory.getSDK().application, NotificationClickReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("TerminalMessage", terminalMessage);
        intent.putExtra("bundle", bundle);
        Log.e("IndividualCallService", "通知栏消息:" + terminalMessage);
        PendingIntent pIntent = PendingIntent.getBroadcast(MyTerminalFactory.getSDK().application, noticeId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            String channelId = createNotificationChannel();
            NotificationCompat.Builder myBuilder = new NotificationCompat.Builder(getApplicationContext(), channelId);
            myBuilder.setContentTitle(noticeTitle)//设置通知标题
                    .setContentText(unReadCountText + noticeContent)//设置通知内容
                    .setTicker(getString(R.string.text_you_has_a_new_message))//设置状态栏提示消息
                    .setSmallIcon(R.drawable.pttpdt)//设置通知图标
                    .setAutoCancel(true)//点击后取消
                    .setWhen(System.currentTimeMillis())//设置通知时间
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setContentIntent(pIntent).setDefaults(Notification.DEFAULT_SOUND); //设置通知点击事件
            Notification notification = myBuilder.build();
            if(null != notificationManager){
                notificationManager.notify(noticeId,notification);
            }
        }else {
            Notification.Builder myBuilder = new Notification.Builder(MyTerminalFactory.getSDK().application);
            myBuilder.setContentTitle(noticeTitle)//设置通知标题
                    .setContentText(unReadCountText + noticeContent)//设置通知内容
                    .setTicker(getString(R.string.text_you_has_a_new_message))//设置状态栏提示消息
                    .setSmallIcon(R.drawable.pttpdt)//设置通知图标
                    .setAutoCancel(true)//点击后取消
                    .setWhen(System.currentTimeMillis())//设置通知时间
                    .setPriority(Notification.PRIORITY_HIGH)//高优先级
                    .setContentIntent(pIntent).setDefaults(Notification.DEFAULT_SOUND); //设置通知点击事件
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                //设置任何情况都会显示通知
                myBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
            }
            Notification notification = myBuilder.build();
            //通过通知管理器来发起通知，ID区分通知
            if(null != notificationManager){
                notificationManager.notify(noticeId, notification);
            }
        }
        lastNotifyTime = System.currentTimeMillis();
    }

    @TargetApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(){
        String channelId = "cn.vsx.vc";
        NotificationChannel chan = new NotificationChannel(channelId,
                "notifycationMessage", NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setSound(null,null);
        chan.enableVibration(false);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if(null !=manager){
            manager.createNotificationChannel(chan);
        }
        return channelId;
    }

    /**
     * 收到强制上报图像的通知
     */
    @SuppressWarnings("unchecked")
    private ReceiveNotifyEmergencyVideoLiveIncommingMessageHandler receiveNotifyEmergencyVideoLiveIncommingMessageHandler = message -> myHandler.post(() -> {
        Map<TerminalState, IState<?>> currentStateMap = TerminalFactory.getSDK().getTerminalStateManager().getCurrentStateMap();
        //观看上报图像,个呼
        if(currentStateMap.containsKey(TerminalState.VIDEO_LIVING_PLAYING)|| currentStateMap.containsKey(TerminalState.INDIVIDUAL_CALLING)){
            TerminalFactory.getSDK().notifyReceiveHandler(ReceiveNotifyEmergencyMessageHandler.class);
        }
        //开启上报功能
        myHandler.postDelayed(() -> TerminalFactory.getSDK().getLiveManager().openFunctionToLivingIncomming(message),1000);
    });


    private void showDialogView(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(!Settings.canDrawOverlays(ReceiveHandlerService.this)){
                ToastUtil.showToast(ReceiveHandlerService.this, getString(R.string.open_overlay_permisson));
                return;
            }
        }
        logger.info("显示弹窗");
        hideAllView();
        // 如果已经添加了就只更新view
        if(dialogAdded){
            windowManager.updateViewLayout(view, layoutParams2);
            video_dialog.setVisibility(View.VISIBLE);
            stackViewAdapter.setData(data);
        }else{
            windowManager.addView(view, layoutParams2);
            video_dialog.setVisibility(View.VISIBLE);
            dialogAdded = true;
            stackViewAdapter = new StackViewAdapter(MyTerminalFactory.getSDK().application);
            stackViewAdapter.setData(data);
            swipeFlingAdapterView.setFlingListener(new FlingListener());
            stackViewAdapter.setCloseDialogListener(new OnClickListenerCloseDialog());
            stackViewAdapter.setGoWatchListener(new OnClickListenerGoWatch());
            swipeFlingAdapterView.setAdapter(stackViewAdapter);
        }
    }


    //接收到上报视频的回调
    private ReceiverActivePushVideoHandler receiverActivePushVideoHandler = (uniqueNo,isGroupPushLive) -> {
        if(MyApplication.instance.getVideoLivePlayingState() != VideoLivePlayingState.IDLE){
            ToastUtil.showToast(MyTerminalFactory.getSDK().application,getString(R.string.text_watching_can_not_report));
            return;
        }
        logger.error("上报给：" + uniqueNo);
        if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_PUSH.name())){
            ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.no_push_authority));
            return;
        }
        if(uniqueNo == 0){//要弹出选择成员页
            Intent intent = new Intent(ReceiveHandlerService.this, InviteMemberService.class);
            intent.putExtra(Constants.TYPE, Constants.PUSH);
            intent.putExtra(Constants.PUSHING, false);
            intent.putExtra(Constants.IS_GROUP_PUSH_LIVING, isGroupPushLive);
            startService(intent);
        }else{//直接上报了
            ArrayList<Long> uniqueNos = new ArrayList<>();
            //如果是组内上报
            if(!isGroupPushLive){
                uniqueNos.add(uniqueNo);
            }
            if(MyApplication.instance.usbAttached){
                Intent intent = new Intent(ReceiveHandlerService.this,SwitchCameraService.class);
                intent.putExtra(Constants.TYPE,Constants.ACTIVE_PUSH);
                intent.putExtra(Constants.THEME,"");
                intent.putExtra(Constants.CAMERA_TYPE,Constants.UVC_CAMERA);
                intent.putExtra(Constants.PUSH_MEMBERS,new PushLiveMemberList(uniqueNos));
                intent.putExtra(Constants.IS_GROUP_PUSH_LIVING, isGroupPushLive);
                startService(intent);
            }else{
                if(Constants.HYTERA.equals(Build.MODEL)){
                    Intent intent = new Intent(ReceiveHandlerService.this,SwitchCameraService.class);
                    intent.putExtra(Constants.TYPE,Constants.ACTIVE_PUSH);
                    intent.putExtra(Constants.THEME,"");
                    intent.putExtra(Constants.CAMERA_TYPE,Constants.RECODER_CAMERA);
                    intent.putExtra(Constants.PUSH_MEMBERS,new PushLiveMemberList(uniqueNos));
                    intent.putExtra(Constants.IS_GROUP_PUSH_LIVING, isGroupPushLive);
                    startService(intent);
                }else{
                        //请求成功,直接开始推送视频
                        Intent intent = new Intent();
                        intent.putExtra(Constants.TYPE, Constants.ACTIVE_PUSH);
                        intent.putExtra(Constants.THEME,"");
                        intent.setClass(ReceiveHandlerService.this, PhonePushService.class);
                        intent.putExtra(Constants.PUSH_MEMBERS,new PushLiveMemberList(uniqueNos));
                        intent.putExtra(Constants.IS_GROUP_PUSH_LIVING, isGroupPushLive);
                        startService(intent);
                }
            }
        }
    };

    private ReceiveAirCraftStatusChangedHandler receiveAirCraftStatusChangedHandler = connected -> {
        MyApplication.instance.aircraftConnected = connected;
        MyTerminalFactory.getSDK().getLiveManager().sendAircraftConnectStatus(connected);
        if(connected){
            Aircraft aircraft = AirCraftUtil.getAircraftInstance();
            if(aircraft == null){
                ToastUtil.showToast(getApplicationContext(),"aircraft为null");
                logger.error("aircraft为null");
            }else {
                setConnectionFailBehavior();
                myHandler.postDelayed(() -> checkBattery(aircraft),2000);
                checkUploadState();
                //无人机连上时自动位置
                myHandler.postDelayed(updateAircraftLocation,uploadTime);
                //无人机连上时自动上报
                if(MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_UP.name())){
                    Intent intent = new Intent(getApplicationContext(), AircraftPushService.class);
                    startService(intent);
                }else {
                    ToastUtil.showToast(getApplicationContext(),"没有图像上报的权限，不能自动上报");
                }
            }

        }else {
            myHandler.removeCallbacks(updateAircraftLocation);
        }
    };

    /**
     * 请求直播
     */
    private ReceiverRequestVideoHandler receiverRequestVideoHandler = (member) -> {
        if(MyApplication.instance.getVideoLivePlayingState() != VideoLivePlayingState.IDLE){
            ToastUtil.showToast(MyTerminalFactory.getSDK().application,getString(R.string.text_watching_can_not_request_report));
            return;
        }
        if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_ASK.name())){
            ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.no_pull_authority));
            return;
        }
        logger.error("请求的直播人：" + member);
        if(member.getNo() == 0){
            Intent intent = new Intent(ReceiveHandlerService.this, InviteMemberService.class);
            intent.putExtra(Constants.TYPE, Constants.PULL);
            intent.putExtra(Constants.PULLING, false);
            startService(intent);

        }else{//直接请求
            int requestCode = MyTerminalFactory.getSDK().getLiveManager().requestMemberLive(member.getNo(),member.getUniqueNo(), "");

            if(requestCode == BaseCommonCode.SUCCESS_CODE){

                Intent intent = new Intent(ReceiveHandlerService.this, LiveRequestService.class);
                intent.putExtra(Constants.MEMBER_NAME, member.getName());
                intent.putExtra(Constants.MEMBER_ID, member.getNo());
                intent.putExtra(Constants.UNIQUE_NO, member.getUniqueNo());
                startService(intent);
            }else{
                ToastUtil.livingFailToast(ReceiveHandlerService.this, requestCode, TerminalErrorCode.LIVING_REQUEST.getErrorCode());
            }
        }
    };

    private class FlingListener implements SwipeFlingAdapterView.onFlingListener{

        @Override
        public void removeFirstObjectInAdapter(){
            if(stackViewAdapter.getCount() == 1){
                stackViewAdapter.remove(0);
            }else{
                stackViewAdapter.setLast(0);
            }
        }

        @Override
        public void onLeftCardExit(Object dataObject){
        }

        @Override
        public void onRightCardExit(Object dataObject){
        }

        @Override
        public void onAdapterAboutToEmpty(int itemsInAdapter){
            removeView();
        }

        @Override
        public void onScroll(float progress, float scrollXProgress){
        }
    }

    /**
     * 设置遥控器和飞机失联动作
     */
    private void setConnectionFailBehavior(){
        Aircraft aircraft = AirCraftUtil.getAircraftInstance();
        if(aircraft !=null){
            aircraft.getFlightController().setConnectionFailSafeBehavior(ConnectionFailSafeBehavior.GO_HOME, new CommonCallbacks.CompletionCallback(){
                @Override
                public void onResult(DJIError djiError){
                    if(djiError == null){
                        logger.error("设置失联自动返航成功");
                    }else {
                        logger.error("设置失联自动返航失败:"+djiError.getDescription());
                    }
                }
            });
        }
    }

    /**
     * 监听电量
     */
    private void checkBattery(Aircraft aircraft){
        // TODO: 2019/4/25 有的飞机有多个电池
        //测试一下如果只有单个电池会不会走下面代码
        List<Battery> batteries = aircraft.getBatteries();
        if(batteries !=null && !batteries.isEmpty()){
            logger.info("电池个数："+batteries.size());
            Battery battery = batteries.get(0);
            battery.setStateCallback(new BatteryState.Callback(){
                @Override
                public void onUpdate(BatteryState batteryState){
                    //电池电量小于等于10%时自动返航
                    //                logger.info("无人机电池电量:"+batteryState.getChargeRemainingInPercent());
                    if(batteryState.getChargeRemainingInPercent() == 10){
                        if(!aircraft.getFlightController().getState().isGoingHome()){
                            //自动返航代码
                            autoGoHome();
                        }
                    }
                }
            });
        }

        //        if(mProduct.getBattery() !=null){
        //
        //            mProduct.getBattery().setStateCallback(new BatteryState.Callback(){
        //                @Override
        //                public void onUpdate(BatteryState batteryState){
        //                    //电池电量小于等于10%时自动返航
        //                    //                logger.info("无人机电池电量:"+batteryState.getChargeRemainingInPercent());
        //                    if(batteryState.getChargeRemainingInPercent() == 10){
        //                        if(!((Aircraft) mProduct).getFlightController().getState().isGoingHome()){
        //                            //自动返航代码
        //                            autoGoHome(mProduct);
        //                        }
        //                    }
        //                }
        //            });
        //        }
    }

    /**
     * 自动返航
     */
    private void autoGoHome(){
        Aircraft aircraft = AirCraftUtil.getAircraftInstance();
        if(null != aircraft){
            aircraft.getFlightController().startGoHome(new CommonCallbacks.CompletionCallback(){
                @Override
                public void onResult(DJIError djiError){
                    if(djiError == null ){
                        logger.info("自动返航成功");
                    }else {
                        logger.error("autoGoHome--"+djiError.getDescription());
                    }
                }
            });
        }

    }

    /**
     * 检查上传位置信息的状态
     * 优先级：强制上传 > Gps开关打开 > 普通上传
     */
    private void checkUploadState() {
        //检查强制上传GPS信息的状态是否依然存在，若存在，则进行强制上传
        if (MyTerminalFactory.getSDK().getLocationManager().checkForceUploadState()) {
            logger.info("强制上传!");
            uploadTime = MyTerminalFactory.getSDK().getLocationManager().getUploadTime(false, false);
        } else if (TerminalFactory.getSDK().getParam(Params.GPS_STATE, false)) {
            //GPS 开关打开
            uploadTime = MyTerminalFactory.getSDK().getLocationManager().getUploadTime(true, false);
            logger.info("普通上传!");
        }
    }

    private void updateAircraftLocation (double latitude,double longitude,double altitude){


        //上传位置信息
        sendLocationMessage(latitude,longitude,altitude);
        myHandler.removeCallbacks(updateAircraftLocation);
        myHandler.postDelayed(updateAircraftLocation,uploadTime);
    }

    private void sendLocationMessage(double latitude,double longitude,double altitude){
        if(TerminalFactory.getSDK().getParam(Params.GPS_STATE, false) && !TerminalFactory.getSDK().isExit()){//配置要求GPS打开，时间间隔大于5秒
            logger.info("开始上传位置信息---Longitude:" + longitude + "---Latitude：" + latitude);
            String ip = MyTerminalFactory.getSDK().getParam(Params.GPS_IP);
            int port = MyTerminalFactory.getSDK().getParam(Params.GPS_PORT, 0);
            final String url = "http://" + ip + ":" + port + "/save";
            Map<String, Object> params = new HashMap<>();
            params.put("terminalno", MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0));
            params.put("memberuniqueno", MyTerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO, 0l));
            params.put("longitude", longitude);
            params.put("latitude", latitude);
            //            params.put("speed",location.getSpeed());
            //            params.put("bearing",location.getBearing());
            params.put("altitude", altitude);
            //            params.put("mountType",MOUNT_UAV.name());
            Gson gson = new Gson();
            final String json = gson.toJson(params);
            MyTerminalFactory.getSDK().getThreadPool().execute(new Runnable(){
                @Override
                public void run(){
                    MyTerminalFactory.getSDK().getHttpClient().postJson(url, "gps=" + json);
                }
            });
        }
    }
}
package cn.vsx.vc.service;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
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
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.ToastUtils;
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
import java.util.List;
import java.util.Map;

import cn.vsx.SpecificSDK.SpecificSDK;
import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.MessageCategory;
import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.ReceiveObjectMode;
import cn.vsx.hamster.common.Remark;
import cn.vsx.hamster.common.ResponseGroupType;
import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.common.UrlParams;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.SignalServerErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallState;
import cn.vsx.hamster.terminalsdk.manager.terminal.TerminalState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePlayingState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePushingState;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.model.VideoMeetingMessage;
import cn.vsx.hamster.terminalsdk.model.WarningRecord;
import cn.vsx.hamster.terminalsdk.receiveHandler.GetWarningMessageDetailHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCurrentGroupIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNetworkChangeByNoRegistHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNetworkChangeHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyAddVideoMeetingMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyDataMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyEmergencyIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyEmergencyMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyEmergencyVideoLiveIncommingMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyIndividualCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyLivingIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyVideoMeetingMessageAddOrOutCompleteHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveOnLineStatusChangedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveReStartVoipHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRemoveMonitorGroupListHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponNotifyWatchHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSetMonitorGroupListHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSetMonitorGroupViewHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveVolumeOffCallHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.TerminalMessageUtil;
import cn.vsx.util.StateMachine.IState;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.GroupCallNewsActivity;
import cn.vsx.vc.activity.IndividualNewsActivity;
import cn.vsx.vc.activity.NewMainActivity;
import cn.vsx.vc.activity.PlayLiveHistoryActivity;
import cn.vsx.vc.activity.TransparentActivity;
import cn.vsx.vc.activity.WarningMessageDetailActivity;
import cn.vsx.vc.adapter.StackViewAdapter;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.dialog.ProgressDialog;
import cn.vsx.vc.jump.utils.AppKeyUtils;
import cn.vsx.vc.model.InviteMemberExceptList;
import cn.vsx.vc.model.PushLiveMemberList;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.receiveHandle.ReceiveGoWatchRTSPHandler;
import cn.vsx.vc.receiveHandle.ReceiveVoipCallActiveEndHandler;
import cn.vsx.vc.receiveHandle.ReceiveVoipCallEndHandler;
import cn.vsx.vc.receiveHandle.ReceiveVoipConnectedHandler;
import cn.vsx.vc.receiveHandle.ReceiveVoipErrorHandler;
import cn.vsx.vc.receiveHandle.ReceiveWarningReadCountChangedHandler;
import cn.vsx.vc.receiveHandle.ReceiverActivePushVideoByNoRegistHandler;
import cn.vsx.vc.receiveHandle.ReceiverActivePushVideoHandler;
import cn.vsx.vc.receiveHandle.ReceiverMonitorViewClickHandler;
import cn.vsx.vc.receiveHandle.ReceiverRequestVideoHandler;
import cn.vsx.vc.receiveHandle.ReceiverRequestVideoMeetingHandler;
import cn.vsx.vc.receiver.NotificationClickReceiver;
import cn.vsx.vc.utils.ActivityCollector;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.DensityUtil;
import cn.vsx.vc.utils.MyDataUtil;
import cn.vsx.vc.utils.SensorUtil;
import cn.vsx.vc.view.flingswipe.SwipeFlingAdapterView;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.filetransfer.FileTransferOperation;
import ptt.terminalsdk.permission.FloatWindowManager;
import ptt.terminalsdk.tools.ApkUtil;
import ptt.terminalsdk.tools.AppUtil;
import ptt.terminalsdk.tools.StringUtil;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * 作者：xuxiaolong
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
    private StackViewAdapter videoStackViewAdapter;
    private StackViewAdapter warningStackViewAdapter;
    private boolean videoDialogMoved;
    private boolean warningDialogMoved;

    private static final int DISSMISS_CURRENT_DIALOG = 6;
    private static final int WATCH_LIVE = 7;

    protected Logger logger = Logger.getLogger(this.getClass());
    private static final String TAG = "ReceiveHandlerService---";
    private List<TerminalMessage> data = new ArrayList<>();
    private List<TerminalMessage> warningData = new ArrayList<>();
    //记录紧急观看的CallId，防止PC端重复发送强制观看的消息
    private String emergencyCallId ;

    private ProgressDialog myProgressDialog;

    //弹窗

    SwipeFlingAdapterView videoSwipeFlingAdapterView;
    SwipeFlingAdapterView warningSwipeFlingAdapterView;

    /**
     * 搜索到的结果集合
     */
    @SuppressWarnings("HandlerLeak,SimpleDateFormat")
    protected Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            handleMyMessage(msg);
        }
    };

    protected void handleMyMessage(Message msg){
        if(msg.what == DISSMISS_CURRENT_DIALOG){
            TerminalMessage terminalMessage = (TerminalMessage) msg.obj;
            if(dialogAdded && null != videoStackViewAdapter && data.contains(terminalMessage)){
                data.remove(terminalMessage);
                videoStackViewAdapter.setData(data);
            }
        }else if(msg.what == WATCH_LIVE){
            if(!checkFloatPermission()){
                startSetting();
                return;
            }
            TerminalMessage terminalMessage1 = (TerminalMessage) msg.obj;
            int position = msg.arg1;
            if(position >= 0){
                data.remove(videoStackViewAdapter.getItem(position));
                videoStackViewAdapter.remove(position);
                video_dialog.setVisibility(View.GONE);
                if(data.size() == 0){
                    removeView();
                }
            }
            Intent intent = new Intent(ReceiveHandlerService.this, PullLivingService.class);
            intent.putExtra(Constants.WATCH_TYPE, Constants.ACTIVE_WATCH);
            intent.putExtra(Constants.TERMINALMESSAGE, terminalMessage1);
            startService(intent);
        }
    }

    private class ReceiveHandlerBinder extends Binder{}

    @Nullable
    @Override
    public IBinder onBind(Intent intent){
//        KeepLiveManager.getInstance().setServiceForeground(this);
        return receiveHandlerBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        return super.onStartCommand(intent,flags,startId);
    }

    public ReceiveHandlerService(){
        super();
    }

    @SuppressLint({"WrongConstant", "InvalidWakeLockTag"})
    @Override
    public void onCreate(){
        super.onCreate();
        logger.info(TAG+"onCreate");
        createFloatView();
        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveVolumeOffCallHandler.class, true, 1);
        //个呼监听
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyIndividualCallIncommingHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiveCurrentGroupIndividualCallHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyEmergencyIndividualCallHandler);
        //视频上报监听
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGoWatchRTSPHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyLivingIncommingHandler);//请求开视频
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiverActivePushVideoByNoRegistHandler);//上报视频（没有注册）
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiverActivePushVideoHandler);//上报视频
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiverRequestVideoHandler);//请求视频
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiverRequestVideoMeetingHandler);//视频会商
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveNotifyDataMessageHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyEmergencyVideoLiveIncommingMessageHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(getWarningMessageDetailHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveResponNotifyWatchHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyAddVideoMeetingMessageHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveReStartVoipHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveOnLineStatusChangedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNetworkChangeHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNetworkChangeByNoRegistHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveRemoveMonitorGroupListHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiverMonitorViewClickHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveSetMonitorGroupListHandler);

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
        SensorUtil.getInstance().unregistSensor();
        PromptManager.getInstance().stop();
        MyTerminalFactory.getSDK().stop();
    }

    @SuppressWarnings("ClickableViewAccessibility,InflateParams")
    public void createFloatView(){
        view = (FrameLayout) LayoutInflater.from(MyTerminalFactory.getSDK().application).inflate(R.layout.layout_receive_handler, null);
        video_dialog = view.findViewById(R.id.video_dialog);
        videoSwipeFlingAdapterView = view.findViewById(R.id.swipeFlingAdapterView);
        warningSwipeFlingAdapterView = view.findViewById(R.id.warning_swipeFlingAdapterView);
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

//    private int registCount;
    private RegistrationCallback voipRegistrationCallback = new RegistrationCallback(){
        @Override
        public void registrationOk(){
            super.registrationOk();
            MyTerminalFactory.getSDK().putParam(Params.VOIP_SUCCESS,true);
            logger.info(TAG+"voip注册成功");
//            registCount = 0;
        }

        @Override
        public void registrationFailed(){
            super.registrationFailed();
            MyTerminalFactory.getSDK().putParam(Params.VOIP_SUCCESS,false);
            logger.error(TAG+"voip注册失败");
//            if(registCount == 10){
//                //10次都注册失败就退出VOIP客户端
//                MyTerminalFactory.getSDK().getVoipCallManager().destroy(MyApplication.instance);//VOIP服务注销
//            }
//            registCount++;
        }
    };

    private PhoneCallback voipPhoneCallback = new PhoneCallback(){
        @Override
        public void incomingCall(LinphoneCall linphoneCall){
            super.incomingCall(linphoneCall);
            if(!checkFloatPermission()){
                startSetting();
                return;
            }
            //判断是否在视频会议中
            if(MyApplication.instance.checkVideoMeeting()){
                return;
            }
            //将状态机至于正在个呼状态
            int code = TerminalFactory.getSDK().getTerminalStateManager().openFunction(TerminalState.INDIVIDUAL_CALLING, IndividualCallState.IDLE);
            if(code == BaseCommonCode.SUCCESS_CODE){
                //将个呼状态机移动到响铃中
                if (TerminalFactory.getSDK().getIndividualCallManager().getIndividualCallStateMachine().moveToState(IndividualCallState.RINGING)) {
                    TerminalFactory.getSDK().getTerminalStateManager().moveToState(TerminalState.INDIVIDUAL_CALLING, IndividualCallState.RINGING);
                    MyApplication.instance.linphoneCall = linphoneCall;
                    Intent intent = new Intent(ReceiveHandlerService.this,ReceiveVoipService.class);
                    LinphoneAddress from = linphoneCall.getCallLog().getFrom();
                    String userName = from.getUserName();
                    logger.info(TAG+"incomingCall-userName:"+userName);
                    intent.putExtra(Constants.USER_NAME,userName);
                    startService(intent);
                }
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
    private final class OnVideoClickListenerCloseDialog implements StackViewAdapter.CloseDialogListener{

        @Override
        public void onCloseDialogClick(int position){
            data.remove(videoStackViewAdapter.getItem(position));
            videoStackViewAdapter.remove(position);
            if(data.isEmpty()){
                removeView();
            }
        }
    }

    private final class WarningCloseDialogClickListener implements StackViewAdapter.CloseDialogListener{
        @Override
        public void onCloseDialogClick(int position){
            warningData.remove(warningStackViewAdapter.getItem(position));
            warningStackViewAdapter.remove(position);
            if(warningData.isEmpty()){
                if(videoSwipeFlingAdapterView.getVisibility() == View.VISIBLE){
                    revertDialog();
                }else {
                    removeView();
                }
            }

        }
    }

    /**
     * 上报图像
     **/
    private final class OnVideoClickListenerGoWatch implements StackViewAdapter.GoWatchListener{
        @Override
        public void onGoWatchClick(final int position){
            if(position > data.size() - 1){
                return;
            }
            final TerminalMessage terminalMessage = videoStackViewAdapter.getItem(position);
            goToWatch(terminalMessage,position);
        }
    }

    private final class goWarningDetailListener implements StackViewAdapter.GoWatchListener{

        @Override
        public void onGoWatchClick(int position){
            if(position > warningData.size()-1){
                return;
            }
            final TerminalMessage terminalMessage = warningStackViewAdapter.getItem(position);
            if(terminalMessage.messageType == MessageType.WARNING_INSTANCE.getCode()){
                if(terminalMessage.messageBody.containsKey(JsonParam.DETAIL) && terminalMessage.messageBody.getBoolean(JsonParam.DETAIL)){
                    warningData.clear();
                    warningStackViewAdapter.clear();
                    //                    warningData.remove(warningStackViewAdapter.getItem(position));
                    //                    warningStackViewAdapter.remove(position);
                    revertDialog();
                    removeView();
                    WarningRecord warningRecord = new WarningRecord();
                    warningRecord.setAperson(terminalMessage.messageBody.getString(JsonParam.APERSON));
                    warningRecord.setApersonPhone(terminalMessage.messageBody.getString(JsonParam.APERSON_PHONE));
                    warningRecord.setRecvperson(terminalMessage.messageBody.getString(JsonParam.RECVPERSON));
                    warningRecord.setRecvphone(terminalMessage.messageBody.getString(JsonParam.RECVPHONE));
                    warningRecord.setAlarmTime(terminalMessage.messageBody.getString(JsonParam.ALARM_TIME));
                    warningRecord.setAddress(terminalMessage.messageBody.getString(JsonParam.ADDRESS));
                    warningRecord.setSummary(terminalMessage.messageBody.getString(JsonParam.SUMMARY));

                    warningRecord.setAlarmNo(terminalMessage.messageBody.getString(JsonParam.ALARM_NO));
                    warningRecord.setLevels(terminalMessage.messageBody.getIntValue(JsonParam.LEVELS));
                    warningRecord.setStatus(terminalMessage.messageBody.getIntValue(JsonParam.STATUS));
                    warningRecord.setUnRead(1);
                    MyTerminalFactory.getSDK().getThreadPool().execute(() -> MyTerminalFactory.getSDK().getSQLiteDBManager().updateWarningRecord(warningRecord));
                    Intent intent = new Intent(getApplicationContext(), WarningMessageDetailActivity.class);
                    intent.putExtra("warningRecord",warningRecord);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveWarningReadCountChangedHandler.class);
                }
            }
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
                String liveUrl = "";
                try{
                    liveUrl = terminalMessage.messageBody.getString(JsonParam.EASYDARWIN_RTSP_URL);
                }catch (Exception e){
                    e.printStackTrace();
                }
                if(!android.text.TextUtils.isEmpty(liveUrl)){
                    boolean isLiving = TerminalFactory.getSDK().getLiveManager().checkPushLiveIsLivingByUrl(liveUrl);
                    if (isLiving) {
                        Message msg = Message.obtain();
                        msg.what = WATCH_LIVE;
                        msg.obj = terminalMessage;
                        msg.arg1 = position;
                        myHandler.sendMessage(msg);
                    } else {
                        removeView();
                        if(TerminalFactory.getSDK().getTerminalMessageManager().checkVideoLiveMessageFromNoRegist(terminalMessage.messageBody)){
                            cn.vsx.vc.utils.ToastUtil.showToast(getString(R.string.text_video_live_from_no_regist_can_not_watch_history));
                        }else{
                            Intent intent = new Intent(MyTerminalFactory.getSDK().application, PlayLiveHistoryActivity.class);
                            intent.putExtra("terminalMessage", terminalMessage);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            //intent.putExtra("endChatTime",endChatTime);
                            startActivity(intent);
                        }
                    }
                }else{
//                    ToastUtil.showToast(getString(R.string.text_liveing_url_is_empty));
                }
            });
        }else if(terminalMessage.messageType == MessageType.GB28181_RECORD.getCode()){
            if(position >= 0){
                data.remove(videoStackViewAdapter.getItem(position));
                videoStackViewAdapter.remove(position);
                video_dialog.setVisibility(View.GONE);
                if(data.size() == 0){
                    removeView();
                }
            }
            goWatchGB28121(terminalMessage);
        }else if(terminalMessage.messageType == MessageType.OUTER_GB28181_RECORD.getCode()){
            if(position >= 0){
                data.remove(videoStackViewAdapter.getItem(position));
                videoStackViewAdapter.remove(position);
                video_dialog.setVisibility(View.GONE);
                if(data.size() == 0){
                    removeView();
                }
            }
            goWatchOutGB28121(terminalMessage);
        }
    }

    /**
     * 被动方个呼来了，选择接听或挂断
     */
    private ReceiveNotifyIndividualCallIncommingHandler receiveNotifyIndividualCallIncommingHandler = (mainMemberName, mainMemberId, individualCallType) -> {
        if(!checkFloatPermission()){
            startSetting();
            return;
        }
        //判断是否在视频会议中
        if(MyApplication.instance.checkVideoMeeting()){
            return;
        }
        myHandler.postDelayed(() -> {
            startTranspantActivity();
            Intent individualCallIntent = new Intent(ReceiveHandlerService.this, ReceiveCallComingService.class);
            individualCallIntent.putExtra(Constants.MEMBER_NAME, mainMemberName);
            individualCallIntent.putExtra(Constants.MEMBER_ID, mainMemberId);
            individualCallIntent.putExtra(Constants.CALL_TYPE, individualCallType);
            individualCallIntent.putExtra(Constants.TYPE, Constants.RECEIVE_CALL);
            startService(individualCallIntent);
        },500);

    };

    /**
     * 主动发起个呼
     */
    private ReceiveCurrentGroupIndividualCallHandler receiveCurrentGroupIndividualCallHandler = (member) -> {
        logger.info(TAG+"当前呼叫对象:" + member);
        if(!checkFloatPermission()){
            startSetting();
            return;
        }
        if(MyApplication.instance.getVideoLivePlayingState() != VideoLivePlayingState.IDLE){
            ToastUtil.showToast(MyTerminalFactory.getSDK().application,getString(R.string.text_watching_can_not_private_call));
            AppKeyUtils.setAppKey(null);
            return;
        }
        if(MyApplication.instance.getVideoLivePushingState() != VideoLivePushingState.IDLE){
            ToastUtil.showToast(MyTerminalFactory.getSDK().application,getString(R.string.text_pushing_can_not_private_call));
            AppKeyUtils.setAppKey(null);
            return;
        }

        if(MyApplication.instance.getGroupListenenState() != GroupCallListenState.IDLE){
            ToastUtil.showToast(MyTerminalFactory.getSDK().application,getString(R.string.text_group_call_listener_can_not_private_call));
            AppKeyUtils.setAppKey(null);
            return;
        }
        if(MyApplication.instance.getIndividualState() != IndividualCallState.IDLE){
            ToastUtil.showToast(ReceiveHandlerService.this,getString(R.string.text_personal_calling_can_not_do_others));
            AppKeyUtils.setAppKey(null);
            return;
        }
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
        //判断是否在视频会议中
        if(MyApplication.instance.checkVideoMeeting()){
            return;
        }
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
        MyTerminalFactory.getSDK().unregistReceiveHandler(getWarningMessageDetailHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponNotifyWatchHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyAddVideoMeetingMessageHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveReStartVoipHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveOnLineStatusChangedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNetworkChangeHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNetworkChangeByNoRegistHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveRemoveMonitorGroupListHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiverMonitorViewClickHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveSetMonitorGroupListHandler);

        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiverActivePushVideoByNoRegistHandler);//上报图像（没有注册）
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiverActivePushVideoHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiverRequestVideoHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiverRequestVideoMeetingHandler);//视频会商

        removeView();
        return super.onUnbind(intent);
    }

    /**
     * 收到别人请求我开启直播的通知
     **/
    @SuppressWarnings("unchecked")
    private ReceiveNotifyLivingIncommingHandler receiveNotifyLivingIncommingHandler = (mainMemberName, mainMemberId, emergencyType) -> myHandler.post(() -> {
        onVideoLiveComming(mainMemberName, mainMemberId, emergencyType);
    });

    protected void onVideoLiveComming(String mainMemberName, int mainMemberId, boolean emergencyType){
        if(!checkFloatPermission()){
            startSetting();
            return;
        }
        //判断是否在视频会议中
        if(MyApplication.instance.checkVideoMeeting()){
            return;
        }
        //判断是否在上报中

        startTranspantActivity();
        Intent intent = new Intent();
        intent.putExtra(Constants.MEMBER_NAME, mainMemberName);
        intent.putExtra(Constants.MEMBER_ID, mainMemberId);
        intent.putExtra(Constants.THEME,"");
        if(Constants.HYTERA.equals(Build.MODEL)){
            logger.info(TAG+"usbAttached ：" + MyApplication.instance.usbAttached);
            if(MyApplication.instance.usbAttached){
                logger.info(TAG+"海能达手台，外置摄像头开启,使用外置摄像头上报");
                MyApplication.instance.isPrivateCallOrVideoLiveHand = true;
                intent.setClass(ReceiveHandlerService.this, UVCPushService.class);
                startService(intent);

            }else{
                //外置摄像头没连接，弹窗选择是否用执法记录仪
                intent.setClass(ReceiveHandlerService.this, SwitchCameraService.class);
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
                //解锁
                AppUtil.wakeUpAndUnlock(MyApplication.instance);
                MyApplication.instance.isPrivateCallOrVideoLiveHand = true;
                intent.setClass(ReceiveHandlerService.this, PhonePushService.class);
                intent.putExtra(Constants.TYPE,Constants.RECEIVE_PUSH);
                intent.putExtra(Constants.EMERGENCY_TYPE,true);
                myHandler.postDelayed(() -> startService(intent),1000);
            }else{
//                if(MyApplication.instance.getVideoLivePushingState() != VideoLivePushingState.IDLE){
//                    return;
//                }
//                if(MyApplication.instance.getVideoLivePlayingState() != VideoLivePlayingState.IDLE){
//                    return;
//                }
//                if(MyApplication.instance.getIndividualState() != IndividualCallState.IDLE){
//                    return;
//                }
                intent.setClass(ReceiveHandlerService.this, ReceiveLiveCommingService.class);
                startService(intent);
            }
        }
    }

    /**
     * 获取到观看GB28181和OutGB28181的通知
     */
    private ReceiveGoWatchRTSPHandler receiveGoWatchRTSPHandler = terminalMessage -> myHandler.post(() -> {
        if(terminalMessage!=null){
            if(terminalMessage.messageType == MessageType.GB28181_RECORD.getCode()){
                goWatchGB28121(terminalMessage);
            }else if(terminalMessage.messageType == MessageType.OUTER_GB28181_RECORD.getCode()){
                goWatchOutGB28121(terminalMessage);
            }
        }
    });

    /**
     * 观看GB28181
     * @param terminalMessage
     */
    private void goWatchGB28121(TerminalMessage terminalMessage){
        if(!checkFloatPermission()){
            startSetting();
            return;
        }
        Intent intent = new Intent(ReceiveHandlerService.this,PullGB28181Service.class);
        intent.putExtra(Constants.TERMINALMESSAGE,terminalMessage);
        startService(intent);
    }

    /**
     * 观看OutGB28181
     * @param terminalMessage
     */
    private void goWatchOutGB28121(TerminalMessage terminalMessage){
        if(!checkFloatPermission()){
            startSetting();
            return;
        }
        Intent intent = new Intent(ReceiveHandlerService.this,PullOutGB28181Service.class);
        intent.putExtra(Constants.TERMINALMESSAGE,terminalMessage);
        startService(intent);
    }



    //获取到警情详情
    private GetWarningMessageDetailHandler getWarningMessageDetailHandler = (terminalMessage,newMessage) -> {
        if(!newMessage){
            return;
        }
        //判断是否在视频会议中
        if(MyApplication.instance.checkVideoMeeting()){
            return;
        }
        if(!TerminalMessageUtil.isGroupMessage(terminalMessage)){
            //个人的警情消息需要弹窗显示
            myHandler.post(()->{
                if(terminalMessage.messageFromId == TerminalFactory.getSDK().getParam(Params.MEMBER_ID,0)){
                    //自己发的警情消息，不弹窗
                    return;
                }
                //判断是否已经存在，没有就添加
                if(!warningData.contains(terminalMessage)){
                    warningData.add(0,terminalMessage);
                }
                if(!MyApplication.instance.checkBusinessInServiceIsWorking() && !MyApplication.instance.isPttPress) {
                    showWarningDialog();
                }
            });

            myHandler.postDelayed(() -> {
                if(dialogAdded && null != warningStackViewAdapter && warningData.contains(terminalMessage)){
                    warningData.remove(terminalMessage);
                    warningStackViewAdapter.setData(warningData);
                    if(warningData.isEmpty()){
                        revertDialog();
                    }
                }
            },30*1000);
        }
    };



    private long lastNotifyTime = 0;
    /**
     * 接收到消息
     */
    @SuppressWarnings("unchecked")
    protected ReceiveNotifyDataMessageHandler mReceiveNotifyDataMessageHandler = terminalMessage -> {
//        logger.info(TAG+"接收到消息" + terminalMessage.toString());
        if(lastNotifyTime != 0 && System.currentTimeMillis() - lastNotifyTime < 500){
            return;
        }
        //是否为别人发的消息
        if(!TerminalMessageUtil.isReceiver(terminalMessage)){
            return;
        }
        //警情消息
        if(terminalMessage.messageType == MessageType.WARNING_INSTANCE.getCode()){
            if(!TerminalMessageUtil.isGroupMessage(terminalMessage)){
                return;
            }
        }

        if(MyApplication.instance.getVideoLivePushingState() != VideoLivePushingState.IDLE){
            return;
        }
        //视频消息
        if(terminalMessage.messageType == MessageType.VIDEO_LIVE.getCode()){
            //紧急观看
            if(terminalMessage.messageBody.containsKey(JsonParam.REMARK)&&terminalMessage.messageBody.getInteger(JsonParam.REMARK) == Remark.EMERGENCY_INFORM_TO_WATCH_LIVE){
                //判断是否在视频会议中
                if(MyApplication.instance.checkVideoMeeting()){
                    return;
                }
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
            }else if((terminalMessage.messageBody.containsKey(JsonParam.REMARK)&&terminalMessage.messageBody.getInteger(JsonParam.REMARK) == Remark.INFORM_TO_WATCH_LIVE)
                    ||TerminalFactory.getSDK().getTerminalMessageManager().checkVideoLiveMessageFromNoRegist(terminalMessage.messageBody)){
                //voip走的个呼状态机
                if(!MyApplication.instance.checkBusinessInServiceIsWorking() && !MyApplication.instance.isPttPress){
                    String liver = (String) terminalMessage.messageBody.get(JsonParam.LIVER);
                    TerminalFactory.getSDK().getThreadPool().execute(() -> {
                        if(!android.text.TextUtils.isEmpty(liver)){
                            if(liver.contains("_")) {
                                String[] split = liver.split("_");
                                if(split.length>0){
                                    long uniqueNo = DataUtil.stringToLong(split[0]);
                                    DataUtil.getMemberByUniqueNo(uniqueNo,true);
                                }
                            }
                        }
                        cn.vsx.hamster.terminalsdk.tools.DataUtil.getAccountByMemberNo(terminalMessage.messageFromId,true);
                    });
                    //判断是否在视频会议中
                    if(MyApplication.instance.checkVideoMeeting()){
                        return;
                    }
                    //判断是否是组内上报，组内上报不弹窗
                    if(!TerminalMessageUtil.isGroupMessage(terminalMessage)){
                        //延迟弹窗，否则判断是否在上报接口返回的是没有在上报
                        myHandler.postDelayed(() -> {
                            data.add(terminalMessage);
                            //判断显示的时候是否已经进入其他业务，或者
                            if(!MyApplication.instance.checkBusinessInServiceIsWorking()){
                                showVideoDialogView();
                            }else{
                                myHandler.removeMessages(DISSMISS_CURRENT_DIALOG);
                            }
                        }, 5000);
                        //30s没观看就取消当前弹窗
                        Message message = Message.obtain();
                        message.what = DISSMISS_CURRENT_DIALOG;
                        message.obj = terminalMessage;
                        myHandler.sendMessageDelayed(message, 30 * 1000);
                    }
                }
            }else if(terminalMessage.messageBody.containsKey(JsonParam.REMARK)&&(terminalMessage.messageBody.getInteger(JsonParam.REMARK) == Remark.LIVE_WATCHING_END ||
                    terminalMessage.messageBody.getInteger(JsonParam.REMARK) == Remark.STOP_ASK_VIDEO_LIVE)){
                return;
            }
        }else if(terminalMessage.messageType == MessageType.GB28181_RECORD.getCode() ||
                terminalMessage.messageType == MessageType.OUTER_GB28181_RECORD.getCode()){
            //国标平台消息
            // TODO: 2019/6/27  
            if(!MyApplication.instance.checkBusinessInServiceIsWorking() && !MyApplication.instance.isPttPress){
                TerminalFactory.getSDK().getThreadPool().execute(() -> {
                    cn.vsx.hamster.terminalsdk.tools.DataUtil.getAccountByMemberNo(terminalMessage.messageFromId,true);
                    if(terminalMessage.messageBody.containsKey(JsonParam.ACCOUNT_ID) && !TextUtils.isEmpty(terminalMessage.messageBody.getString(JsonParam.ACCOUNT_ID))){
                        cn.vsx.hamster.terminalsdk.tools.DataUtil.getAccountByMemberNo(StringUtil.stringToInt(terminalMessage.messageBody.getString(JsonParam.ACCOUNT_ID)),true);
                    }
                });
                //判断是否在视频会议中
                if(MyApplication.instance.checkVideoMeeting()){
                    return;
                }
                if(!TerminalMessageUtil.isGroupMessage(terminalMessage)){
                    //延迟弹窗，否则判断是否在上报接口返回的是没有在上报
                    myHandler.postDelayed(() -> {
                        data.add(terminalMessage);
                        showVideoDialogView();
                    }, 5000);
                    //30s没观看就取消当前弹窗
                    Message message = Message.obtain();
                    message.what = DISSMISS_CURRENT_DIALOG;
                    message.obj = terminalMessage;
                    myHandler.sendMessageDelayed(message, 30 * 1000);
                }
            }
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
//         createNotification(terminalMessage);
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
//            logger.info(TAG+"长文本： path:" + path + "    content:" + content);
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
                    .setSmallIcon(R.drawable.new_logo_icon)//设置通知图标
                    .setAutoCancel(true)//点击后取消
                    .setWhen(System.currentTimeMillis())//设置通知时间
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setOngoing(false)
                    .setContentIntent(pIntent)//设置通知点击事件
                    .setDefaults(Notification.DEFAULT_LIGHTS|Notification.DEFAULT_VIBRATE);
            Notification notification = myBuilder.build();
            if(null != notificationManager){
                notificationManager.notify(noticeId,notification);
            }
        }else {
            Notification.Builder myBuilder = new Notification.Builder(MyTerminalFactory.getSDK().application);
            myBuilder.setContentTitle(noticeTitle)//设置通知标题
                    .setContentText(unReadCountText + noticeContent)//设置通知内容
                    .setTicker(getString(R.string.text_you_has_a_new_message))//设置状态栏提示消息
                    .setSmallIcon(R.drawable.new_logo_icon)//设置通知图标
                    .setAutoCancel(true)//点击后取消
                    .setOngoing(false)
                    .setWhen(System.currentTimeMillis())//设置通知时间
                    .setPriority(Notification.PRIORITY_HIGH)//高优先级
                    .setContentIntent(pIntent)//设置通知点击事件
                    .setDefaults(Notification.DEFAULT_LIGHTS|Notification.DEFAULT_VIBRATE);
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
        //判断是否在视频会议中
        if(MyApplication.instance.checkVideoMeeting()){
            return;
        }
        startTranspantActivity();
        Map<TerminalState, IState<?>> currentStateMap = TerminalFactory.getSDK().getTerminalStateManager().getCurrentStateMap();
        //观看上报图像,个呼
        if(currentStateMap.containsKey(TerminalState.VIDEO_LIVING_PUSHING)||currentStateMap.containsKey(TerminalState.VIDEO_LIVING_PLAYING)
                || currentStateMap.containsKey(TerminalState.INDIVIDUAL_CALLING)){
            TerminalFactory.getSDK().notifyReceiveHandler(ReceiveNotifyEmergencyMessageHandler.class);
        }
        //组呼
        if(currentStateMap.containsKey(TerminalState.GROUP_CALL_LISTENING) || currentStateMap.containsKey(TerminalState.GROUP_CALL_SPEAKING)){
            TerminalFactory.getSDK().getGroupCallManager().ceaseGroupCall();
        }
        //开启上报功能
        myHandler.postDelayed(() -> TerminalFactory.getSDK().getLiveManager().openFunctionToLivingIncomming(message),1000);
    });

    private ReceiveResponNotifyWatchHandler receiveResponNotifyWatchHandler = message -> {
        //推送失败
        if(message.getResultCode() == SignalServerErrorCode.WITHOUT_LIVING.getErrorCode()){
            //如果是自己上报的话需要向信令发起上报
            if(message.getLiveUniqueNo() == TerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO,0L) &&
                    TerminalFactory.getSDK().getLiveManager().getVideoLivePushingStateMachine().getCurrentState() == VideoLivePushingState.PUSHING){
                TerminalFactory.getSDK().getLiveManager().requestMyselfLive("","");
            }else {
                ToastUtils.showShort(message.getResultDesc());
            }
        }else if(message.getResultCode() != BaseCommonCode.SUCCESS_CODE){
            ToastUtils.showShort(message.getResultDesc());
        }
    };
    //接收到上报视频的回调(没有注册)
    private ReceiverActivePushVideoByNoRegistHandler receiverActivePushVideoByNoRegistHandler = this::onActivePushVideoByNoRegist;

    private void onActivePushVideoByNoRegist() {
        try{
            //判断权限
            if(!checkFloatPermission()){
                startSetting();
                return;
            }
            //清空状态
            MyApplication.instance.stopAllBusiness();
            myHandler.postDelayed(() -> {
                Intent intent = new Intent(this, PhonePushByNoRegistService.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startService(intent);
            },500);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //接收到上报视频的回调
    private ReceiverActivePushVideoHandler receiverActivePushVideoHandler = this::onActivePushVideo;

    protected void onActivePushVideo(String uniqueNoAndType, boolean isGroupPushLive){
        if(!checkFloatPermission()){
            startSetting();
            return;
        }
        if(MyApplication.instance.getVideoLivePlayingState() != VideoLivePlayingState.IDLE){
            ToastUtil.showToast(MyTerminalFactory.getSDK().application,getString(R.string.text_watching_can_not_report));
            AppKeyUtils.setAppKey(null);
            return;
        }
        if(MyApplication.instance.getVideoLivePushingState() != VideoLivePushingState.IDLE){
            ToastUtil.showToast(MyTerminalFactory.getSDK().application,getString(R.string.text_pushing_can_not_report));
            AppKeyUtils.setAppKey(null);
            return;
        }
        if(MyApplication.instance.getIndividualState() != IndividualCallState.IDLE){
            ToastUtil.showToast(ReceiveHandlerService.this,getString(R.string.text_personal_calling_can_not_report));
            AppKeyUtils.setAppKey(null);
            return;
        }
        logger.error(TAG+"上报给：" + uniqueNoAndType);
        if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_UP.name())){
            ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.no_push_authority));
            AppKeyUtils.setAppKey(null);
            return;
        }
        String deviceType = MyTerminalFactory.getSDK().getParam(UrlParams.TERMINALMEMBERTYPE, "");
        // 判断有没有直接上报到组的权限，如果有直接上报到组，不弹出选择界面
        if(android.text.TextUtils.equals(deviceType, TerminalMemberType.TERMINAL_PHONE.toString()) && TerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_UP_CURRENT_GROUP.name())){
            int currentGroupId = TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID,0);
            uniqueNoAndType = MyDataUtil.getPushInviteMemberData(currentGroupId, ReceiveObjectMode.GROUP.toString());
            ArrayList<String> uniqueNos = new ArrayList<>();
            uniqueNos.add(uniqueNoAndType);
            Intent intent = new Intent(ReceiveHandlerService.this, PhonePushService.class);
            intent.putExtra(Constants.PUSH_MEMBERS,new PushLiveMemberList(uniqueNos));
            intent.putExtra(Constants.THEME,"");
            intent.putExtra(Constants.TYPE,Constants.ACTIVE_PUSH);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startService(intent);
        }else {
            if(android.text.TextUtils.isEmpty(uniqueNoAndType)){//要弹出选择成员页
                Intent intent = new Intent(ReceiveHandlerService.this, InviteMemberService.class);
                intent.putExtra(Constants.TYPE, Constants.PUSH);
                intent.putExtra(Constants.PUSHING, false);
                intent.putExtra(Constants.IS_GROUP_PUSH_LIVING, isGroupPushLive);
                intent.putExtra(Constants.INVITE_MEMBER_EXCEPT_UNIQUE_NO,new InviteMemberExceptList());
                startService(intent);
            }else{//直接上报了
                ArrayList<String> uniqueNos = new ArrayList<>();
                uniqueNos.add(uniqueNoAndType);
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
        }
    }

    /**
     * 请求直播
     */
    private ReceiverRequestVideoHandler receiverRequestVideoHandler = (member) -> {
        if(!checkFloatPermission()){
            startSetting();
            return;
        }
        if(MyApplication.instance.getVideoLivePlayingState() != VideoLivePlayingState.IDLE){
            ToastUtil.showToast(MyTerminalFactory.getSDK().application,getString(R.string.text_watching_can_not_request_report));
            AppKeyUtils.setAppKey(null);
            return;
        }
        if(MyApplication.instance.getVideoLivePushingState() != VideoLivePushingState.IDLE){
            ToastUtil.showToast(MyTerminalFactory.getSDK().application,getString(R.string.text_pushing_can_not_pull));
            AppKeyUtils.setAppKey(null);
            return;
        }
        if(MyApplication.instance.getIndividualState() != IndividualCallState.IDLE){
            ToastUtil.showToast(ReceiveHandlerService.this,getString(R.string.text_personal_calling_can_not_pull));
            AppKeyUtils.setAppKey(null);
            return;
        }
        if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_ASK.name())){
            ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.no_pull_authority));
            AppKeyUtils.setAppKey(null);
            return;
        }
        logger.error(TAG+"请求的直播人：" + member);
        if(member.getNo() == 0){
            Intent intent = new Intent(ReceiveHandlerService.this, InviteMemberService.class);
            intent.putExtra(Constants.TYPE, Constants.PULL);
            intent.putExtra(Constants.PULLING, false);
            intent.putExtra(Constants.INVITE_MEMBER_EXCEPT_UNIQUE_NO,new InviteMemberExceptList());
            startService(intent);

        }else{//直接请求
            if(member.getType() == TerminalMemberType.TERMINAL_LTE.getCode()){
                //市局和安监的LTE不同，市局的直接拼接流地址就行，安监的去请求信令
                if(ApkUtil.isAnjian()){
                    Intent intent = new Intent(ReceiveHandlerService.this, LiveRequestService.class);
                    intent.putExtra(Constants.MEMBER_NAME, member.getName());
                    intent.putExtra(Constants.MEMBER_ID, member.getNo());
                    intent.putExtra(Constants.UNIQUE_NO, member.getUniqueNo());
                    startService(intent);
                }else {
                    String gb28181No = member.getGb28181No();
                    //String gateWayUrl = TerminalFactory.getSDK().getParam(Params.GATE_WAY_URL);
                    //String gb28181RtspUrl = gateWayUrl+"DevAor="+gb28181No;
                    TerminalMessage terminalMessage = new TerminalMessage();
                    terminalMessage.messageType = MessageType.GB28181_RECORD.getCode();
                    terminalMessage.messageBody = new JSONObject();
                    terminalMessage.messageBody.put(JsonParam.GB28181_RTSP_URL,gb28181No);
                    terminalMessage.messageBody.put(JsonParam.DEVICE_NAME,member.getName());
                    terminalMessage.messageBody.put(JsonParam.ACCOUNT_ID,member.getNo());
                    terminalMessage.messageBody.put(JsonParam.DEVICE_DEPT_NAME,member.getDepartmentName());
                    terminalMessage.messageBody.put(JsonParam.DEVICE_DEPT_ID,member.getDeptId());
                    goWatchGB28121(terminalMessage);
                }
            }else{
                Intent intent = new Intent(ReceiveHandlerService.this, LiveRequestService.class);
                intent.putExtra(Constants.MEMBER_NAME, member.getName());
                intent.putExtra(Constants.MEMBER_ID, member.getNo());
                intent.putExtra(Constants.UNIQUE_NO, member.getUniqueNo());
                startService(intent);
            }
        }
    };
    /**
     * 发起视频会商
     */
    private ReceiverRequestVideoMeetingHandler receiverRequestVideoMeetingHandler = (videoMeetingType) -> {
        if(!checkFloatPermission()){
            startSetting();
            return;
        }
        if(MyApplication.instance.getVideoLivePlayingState() != VideoLivePlayingState.IDLE){
            ToastUtil.showToast(MyTerminalFactory.getSDK().application,getString(R.string.text_watching_can_not_request_video_meeting));
            AppKeyUtils.setAppKey(null);
            return;
        }
        if(MyApplication.instance.getVideoLivePushingState() != VideoLivePushingState.IDLE){
            ToastUtil.showToast(MyTerminalFactory.getSDK().application,getString(R.string.text_pushing_can_not_request_video_meeting));
            AppKeyUtils.setAppKey(null);
            return;
        }
        if(MyApplication.instance.getIndividualState() != IndividualCallState.IDLE){
            ToastUtil.showToast(ReceiveHandlerService.this,getString(R.string.text_personal_calling_can_not_request_video_meeting));
            AppKeyUtils.setAppKey(null);
            return;
        }
        if(MyApplication.instance.checkVideoMeeting()){
            //正在视频会议的业务中
            ToastUtil.showToast(ReceiveHandlerService.this,getString(R.string.text_video_meeting_can_not_request_video_meeting));
            return;
        }

        Intent intent = new Intent(this, VideoMeetingService.class);
        //intent.putExtra(Constants.VIDEO_MEETING_GROUP_NO,groupNo);
        intent.putExtra(Constants.VIDEO_MEETING_TYPE,videoMeetingType);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(intent);
    };


    /**
     * 通知终端加入视频会商会议室
     */
    private ReceiveNotifyAddVideoMeetingMessageHandler receiveNotifyAddVideoMeetingMessageHandler = (notifyMessage) -> {
        //1.保存到数据库中，2.从数据库中查到所有正在会议的和时间最近的一条消息，3.刷新UI
        TerminalFactory.getSDK().getThreadPool().execute(() -> {
            if(notifyMessage!=null){
                VideoMeetingMessage meetingMessage =  DataUtil.getVideoMeetingMessageByNotify(notifyMessage);
                if(meetingMessage!=null){
                    TerminalFactory.getSDK().getSQLiteDBManager().addVideoMeetingMessage(meetingMessage);
                    ////获取更新完的数据
                    VideoMeetingMessage message = TerminalFactory.getSDK().getSQLiteDBManager().getMeetingVideoMeetingMessageByRoomId(meetingMessage.getRoomId());
                    //如果是加入邀请通知，跳转到邀请页面
                    //但要判断当前的状态，是否在是否在其他业务和在视频会议业务中
                    if(message.isAddOrOutMeeting()){
                        //更新UI
                        TerminalFactory.getSDK().notifyReceiveHandler(
                            ReceiveNotifyVideoMeetingMessageAddOrOutCompleteHandler.class,message,true,true);
                        if(!MyApplication.instance.checkVideoMeeting()){
                            //不在视频会议的业务中
                            MyApplication.instance.stopAllBusiness();
                            myHandler.postDelayed(() -> {
                                Intent intent = new Intent(this, VideoMeetingInvitationService.class);
                                intent.putExtra(Constants.VIDEO_MEETING_MESSAGE,message);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startService(intent);
                            },500);
                        }else{
                            //正在视频会议的业务中
                            TerminalFactory.getSDK().getVideoMeetingManager().responseInvitationMessage(message.getRoomId(),1);
                        }
                    }else{
                        //更新UI
                        TerminalFactory.getSDK().notifyReceiveHandler(ReceiveNotifyVideoMeetingMessageAddOrOutCompleteHandler.class,message,true,false);
                    }
                }
            }
        });
    };

    /**
     * 通知重启voip
     */
    private ReceiveReStartVoipHandler receiveReStartVoipHandler = () -> {
        //TerminalFactory.getSDK().getThreadPool().execute(() -> {
        MyTerminalFactory.getSDK().putParam(Params.VOIP_SUCCESS,false);
        //关闭页面
        MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveVoipCallActiveEndHandler.class);
        //VOIP服务注销
        myHandler.postDelayed(() ->{
            MyTerminalFactory.getSDK().getVoipCallManager().removeAuthInfo();
        } ,1000);
        //重启voip
        myHandler.postDelayed(() ->{
            //监听voip来电
            SpecificSDK.initVoipSpecificSDK();
        } ,2000);
        //});
    };

    /**
     * 在线状态
     */
    private ReceiveOnLineStatusChangedHandler receiveOnLineStatusChangedHandler = connected -> {
        if(connected){
            MyTerminalFactory.getSDK().getVoipCallManager().refreshLogin();
        }
    };

    /**
     * 网络状态
     */
    private ReceiveNetworkChangeHandler receiveNetworkChangeHandler = new ReceiveNetworkChangeHandler(){
        @Override
        public void handler(boolean connected){
            if(connected){
                //清理数据库
                FileTransferOperation manager =  MyTerminalFactory.getSDK().getFileTransferOperation();
                //48小时未上传的文件上传,警务通暂时不要自动上传48小时未上传的功能
                //上传没有上传的文件信息
                manager.uploadFileTreeBean(null,false);
            }
        }
    };
    /**
     * 网络状态(没有注册时的网络变化)
     */
    private ReceiveNetworkChangeByNoRegistHandler receiveNetworkChangeByNoRegistHandler = new ReceiveNetworkChangeByNoRegistHandler(){
        @Override
        public void handler(boolean connected){
            if(connected){
                //清理数据库
                FileTransferOperation manager =  MyTerminalFactory.getSDK().getFileTransferOperation();
                //48小时未上传的文件上传,警务通暂时不要自动上传48小时未上传的功能
                //上传没有上传的文件信息
                manager.uploadFileTreeBean(null,false);
            }
        }
    };

    /**
     * 移除监听组
     */
    private ReceiveRemoveMonitorGroupListHandler receiveRemoveMonitorGroupListHandler=new ReceiveRemoveMonitorGroupListHandler() {
        @Override
        public void handler(List<Integer> removeScanGroupList) {
            if(removeScanGroupList!=null&&!removeScanGroupList.isEmpty()){
                int size = removeScanGroupList.size();
                StringBuilder builder=new StringBuilder();
                builder.append("\n");
                for (Integer groupnNo:removeScanGroupList) {
                    Group group = TerminalFactory.getSDK().getGroupByGroupNo(groupnNo);
                    builder.append(group.name);
                    if(size>1){
                        builder.append(",");
                    }
                    builder.append("\n");
                }
                cn.vsx.vc.utils.ToastUtil.showToast(String.format(getString(R.string.notify_invalid_listener_group),builder.toString()),true);
            }
        }
    };

    private SparseBooleanArray currentMonitorGroup = new SparseBooleanArray();
    private int monitorGroupNo;

    private ReceiverMonitorViewClickHandler receiverMonitorViewClickHandler = new ReceiverMonitorViewClickHandler(){
        @Override
        public void handler(int groupNo){
            //响应组不能取消监听
            Group group = TerminalFactory.getSDK().getGroupByGroupNo(groupNo);
            if(ResponseGroupType.RESPONSE_TRUE.toString().equals(group.getResponseGroupType())){
                ToastUtils.showShort(R.string.response_group_cannot_cancel_monitor);
                return;
            }
            //如果是当前组，取消当前组
            if(TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID,0) == groupNo){
                ToastUtils.showShort(R.string.current_group_cannot_cancel_monitor);
                return;
            }
            List<Integer> monitorGroups = new ArrayList<>();
            monitorGroups.add(groupNo);
            ReceiveHandlerService.this.monitorGroupNo = groupNo;
            if(null != DataUtil.getTempGroupByGroupNo(groupNo)){
                //是临时组
                if(TerminalFactory.getSDK().getConfigManager().getTempMonitorGroupNos().contains(groupNo)){
                    logger.info(TAG+"将临时组取消监听");
                    currentMonitorGroup.put(groupNo,false);
                    MyTerminalFactory.getSDK().getGroupManager().setMonitorGroup(monitorGroups,false);
                }else {
                    logger.info(TAG+"将临时组设置监听");
                    currentMonitorGroup.put(groupNo,true);
                    MyTerminalFactory.getSDK().getGroupManager().setMonitorGroup(monitorGroups,true);
                }
            }else {
                //不是临时组
                if(TerminalFactory.getSDK().getConfigManager().getMonitorGroupNo().contains(groupNo)){
                    currentMonitorGroup.put(groupNo,false);
                    MyTerminalFactory.getSDK().getGroupManager().setMonitorGroup(monitorGroups,false);
                }else {
                    //判断有没有超过5个监听组
                    if(TerminalFactory.getSDK().getConfigManager().getMonitorGroupNo().size()>=5){
                        logger.info(TAG+getResources().getString(R.string.monitor_more_than_five));
                        ToastUtil.showToast(getResources().getString(R.string.monitor_more_than_five));
                    }else {
                        currentMonitorGroup.put(groupNo,true);
                        MyTerminalFactory.getSDK().getGroupManager().setMonitorGroup(monitorGroups,true);
                    }
                }
            }
        }
    };

    private ReceiveSetMonitorGroupListHandler receiveSetMonitorGroupListHandler = new ReceiveSetMonitorGroupListHandler(){
        @Override
        public void handler(int errorCode, String errorDesc){
            if(errorCode == BaseCommonCode.SUCCESS_CODE){
                //加入监听
                if(currentMonitorGroup.get(monitorGroupNo)){
//                    TerminalFactory.getSDK().getGroupManager().changeGroup(monitorGroupNo);
                    if(null != DataUtil.getTempGroupByGroupNo(monitorGroupNo)&&!TerminalFactory.getSDK().getConfigManager().getTempMonitorGroupNos().contains(monitorGroupNo)){
                        TerminalFactory.getSDK().getConfigManager().getTempMonitorGroupNos().add(monitorGroupNo);
                        TerminalFactory.getSDK().getConfigManager().updateTempMonitorGroup();
                    }
                }else {
                    //移除监听
                    if(monitorGroupNo == TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID,0)){
                        //当前组被移除监听了切换到主组去
                        if(monitorGroupNo != TerminalFactory.getSDK().getParam(Params.MAIN_GROUP_ID,0)){
//                            TerminalFactory.getSDK().getGroupManager().changeGroup(TerminalFactory.getSDK().getParam(Params.MAIN_GROUP_ID,0));
                        }
                    }
                    if(TerminalFactory.getSDK().getConfigManager().getTempMonitorGroupNos().contains(monitorGroupNo)){
                        TerminalFactory.getSDK().getConfigManager().getTempMonitorGroupNos().remove((Integer)monitorGroupNo);
                        TerminalFactory.getSDK().getConfigManager().updateTempMonitorGroup();
                    }else if(TerminalFactory.getSDK().getConfigManager().getMonitorGroupNo().contains(monitorGroupNo)){
                        TerminalFactory.getSDK().getConfigManager().getMonitorGroupNo().remove((Integer)monitorGroupNo);
                        TerminalFactory.getSDK().getConfigManager().saveMonitorGroup();
                    }
                }
                //监听的组，从本地的失效列表中移除
                List<Group> removelists = TerminalFactory.getSDK().getList(Params.TOTAL_REMOVE_GROUP_LIST, new ArrayList<Group>(), Group.class);
                Group group = new Group(monitorGroupNo);
                if(removelists.contains(group)){
                    removelists.remove(group);
                }
                TerminalFactory.getSDK().putList(Params.TOTAL_REMOVE_GROUP_LIST,removelists);
                //更新UI
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiveSetMonitorGroupViewHandler.class);
                monitorGroupNo = 0;
            }else {
                monitorGroupNo = 0;
                ToastUtil.showToast(errorDesc);
            }
        }
    };

    private void showWarningDialog(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(ReceiveHandlerService.this)) {
                cn.vsx.vc.utils.ToastUtil.showToast(ReceiveHandlerService.this, getResources().getString(R.string.open_overlay_permisson));
            }
        }
        hideAllView();
        // 如果已经添加了就只更新view
        if (dialogAdded) {
            windowManager.updateViewLayout(view, layoutParams2);
            video_dialog.setVisibility(View.VISIBLE);
            if(videoSwipeFlingAdapterView.getVisibility() == View.VISIBLE){
                moveDialog();
            }
            if(warningStackViewAdapter == null){
                initWarningStackViewAdapter();
            }
            warningStackViewAdapter.setData(warningData);
            warningSwipeFlingAdapterView.setVisibility(View.VISIBLE);

        } else {
            windowManager.addView(view, layoutParams2);
            video_dialog.setVisibility(View.VISIBLE);
            warningSwipeFlingAdapterView.setVisibility(View.VISIBLE);
            videoSwipeFlingAdapterView.setVisibility(View.GONE);
            dialogAdded = true;
            if(warningStackViewAdapter == null){
                initWarningStackViewAdapter();
            }else {
                warningStackViewAdapter.setData(warningData);
            }
        }
    }

    private void showVideoDialogView(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(ReceiveHandlerService.this)) {
                cn.vsx.vc.utils.ToastUtil.showToast(ReceiveHandlerService.this, "请打开悬浮窗权限，否则私密呼叫和图像功能无法使用！");
                return;
            }
        }
        // 如果已经添加了就只更新view
        if (dialogAdded) {
            windowManager.updateViewLayout(view, layoutParams2);
            video_dialog.setVisibility(View.VISIBLE);
            if(warningSwipeFlingAdapterView.getVisibility() == View.VISIBLE){
                moveDialog();
            }

            if(null == videoStackViewAdapter){
                initStackViewAdapter();
            }
            videoSwipeFlingAdapterView.setVisibility(View.VISIBLE);
            videoStackViewAdapter.setData(data);

        } else {
            windowManager.addView(view, layoutParams2);
            video_dialog.setVisibility(View.VISIBLE);
            warningSwipeFlingAdapterView.setVisibility(View.GONE);
            videoSwipeFlingAdapterView.setVisibility(View.VISIBLE);
            dialogAdded = true;
            if(null == videoStackViewAdapter){
                initStackViewAdapter();
            }else {
                videoStackViewAdapter.setData(data);
            }
        }
    }

    private void initStackViewAdapter(){
        videoStackViewAdapter = new StackViewAdapter(getApplicationContext());
        videoStackViewAdapter.setData(data);
        videoSwipeFlingAdapterView.setFlingListener(new VideoFlingListener());
        videoStackViewAdapter.setCloseDialogListener(new OnVideoClickListenerCloseDialog());
        videoStackViewAdapter.setGoWatchListener(new OnVideoClickListenerGoWatch() );
        videoSwipeFlingAdapterView.setAdapter(videoStackViewAdapter);
    }

    private void initWarningStackViewAdapter(){
        warningStackViewAdapter = new StackViewAdapter(getApplicationContext());
        warningStackViewAdapter.setData(warningData);
        warningSwipeFlingAdapterView.setFlingListener(new WarningFlingListener());
        warningStackViewAdapter.setCloseDialogListener(new WarningCloseDialogClickListener());
        warningStackViewAdapter.setGoWatchListener(new goWarningDetailListener());
        warningSwipeFlingAdapterView.setAdapter(warningStackViewAdapter);
    }

    private void setLayoutY(View view, int y){
        view.scrollBy(0,y);
    }

    private void moveDialog(){
        if(!warningDialogMoved){
            setLayoutY(warningSwipeFlingAdapterView, DensityUtil.dip2px(getApplicationContext(),120));
            warningDialogMoved = true;
        }
        if(!videoDialogMoved){
            setLayoutY(videoSwipeFlingAdapterView,DensityUtil.dip2px(getApplicationContext(),-120));
            videoDialogMoved = true;
        }
    }

    private void revertDialog(){
        if(warningDialogMoved){
            setLayoutY(warningSwipeFlingAdapterView,DensityUtil.dip2px(getApplicationContext(),-120));
            warningDialogMoved = false;
        }
        if(videoDialogMoved){
            setLayoutY(videoSwipeFlingAdapterView,DensityUtil.dip2px(getApplicationContext(),120));
            videoDialogMoved = false;
        }
    }

    private class VideoFlingListener implements SwipeFlingAdapterView.onFlingListener{

        @Override
        public void removeFirstObjectInAdapter(){
            if(videoStackViewAdapter.getCount() == 1){
                videoStackViewAdapter.remove(0);
            }else{
                videoStackViewAdapter.setLast(0);
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
            if(warningSwipeFlingAdapterView.getVisibility() == View.VISIBLE){
                revertDialog();
                videoSwipeFlingAdapterView.setVisibility(View.GONE);
            }else {
                removeView();
            }
        }

        @Override
        public void onScroll(float progress, float scrollXProgress){
        }
    }

    private class WarningFlingListener implements SwipeFlingAdapterView.onFlingListener{

        @Override
        public void removeFirstObjectInAdapter(){
            if(warningStackViewAdapter.getCount()==1){
                warningData.remove(warningStackViewAdapter.getItem(0));
                warningStackViewAdapter.remove(0);
            }else {
                warningStackViewAdapter.setLast(0);
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
            if(videoSwipeFlingAdapterView.getVisibility() == View.VISIBLE){
                revertDialog();
                warningSwipeFlingAdapterView.setVisibility(View.GONE);
            }else {
                removeView();
            }
        }

        @Override
        public void onScroll(float progress, float scrollXProgress){
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
                    Intent intent = new Intent(ReceiveHandlerService.this, TransparentActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    startActivity(intent);
                }
            }
        }
    }

    protected boolean checkFloatPermission(){
        return FloatWindowManager.getInstance().checkPermission(this);
    }

    public void startSetting(){
        logger.error(TAG+"没有获取到悬浮窗权限!!!");
        ToastUtils.showShort(getString(R.string.open_overlay_permisson));
        myHandler.postDelayed(()->{
            FloatWindowManager.getInstance().requestPermission(getApplicationContext());
        },500);
    }
}

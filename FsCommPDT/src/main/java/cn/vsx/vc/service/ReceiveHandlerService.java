package cn.vsx.vc.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSONObject;
import com.xuchongyang.easyphone.callback.PhoneCallback;
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
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCurrentGroupIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyDataMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyEmergencyIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyIndividualCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyLivingIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveVolumeOffCallHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.SignatureUtil;
import cn.vsx.hamster.terminalsdk.tools.Util;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.GroupCallNewsActivity;
import cn.vsx.vc.activity.IndividualNewsActivity;
import cn.vsx.vc.activity.LiveHistoryActivity;
import cn.vsx.vc.activity.NewMainActivity;
import cn.vsx.vc.adapter.StackViewAdapter;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.receiveHandle.ReceiveGoWatchRTSPHandler;
import cn.vsx.vc.receiveHandle.ReceiverActivePushVideoHandler;
import cn.vsx.vc.receiveHandle.ReceiverRequestVideoHandler;
import cn.vsx.vc.receiver.NotificationClickReceiver;
import cn.vsx.vc.utils.ActivityCollector;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.view.flingswipe.SwipeFlingAdapterView;
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
                    data.remove(stackViewAdapter.getItem(position));
                    stackViewAdapter.remove(position);
                    video_dialog.setVisibility(View.GONE);
                    if(data.size() ==0){
                        removeView();
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
        MyTerminalFactory.getSDK().getThreadPool().execute(() -> MyTerminalFactory.getSDK().start());
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
        //监听voip来电
        MyTerminalFactory.getSDK().getVoipCallManager().addCallback(null,phoneCallback);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        myHandler.removeCallbacksAndMessages(null);
        MyTerminalFactory.getSDK().stop();
    }

    @SuppressWarnings("ClickableViewAccessibility,InflateParams")
    public void createFloatView(){
        view = (FrameLayout) LayoutInflater.from(MyApplication.instance.getApplicationContext()).inflate(R.layout.layout_receive_handler, null);
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
            MyApplication.instance.isMiniLive = false;
            PromptManager.getInstance().stopRing();
        }
    }



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
            //判断是否有接受图像功能权限
            if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_ACCEPT.name())){
                ToastUtil.showToast(getApplicationContext(), "您还没有图像接受权限");
                removeView();
                return;
            }
            if(position > data.size() - 1){
                return;
            }
            final TerminalMessage terminalMessage = stackViewAdapter.getItem(position);
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
                            Intent intent = new Intent(getApplicationContext(), LiveHistoryActivity.class);
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
    private ReceiveCurrentGroupIndividualCallHandler receiveCurrentGroupIndividualCallHandler = member -> {
        logger.info("当前呼叫对象:" + member);
        Intent intent = new Intent(ReceiveHandlerService.this, StartIndividualCallService.class);
        intent.putExtra(Constants.MEMBER_NAME, member.getName());
        intent.putExtra(Constants.MEMBER_ID, member.getNo());
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
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiverActivePushVideoHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiverRequestVideoHandler);

        removeView();
        return super.onUnbind(intent);
    }


    private PhoneCallback phoneCallback = new PhoneCallback(){
        @Override
        public void incomingCall(LinphoneCall linphoneCall){
            super.incomingCall(linphoneCall);
            //将状态机至于正在个呼状态
            int code = TerminalFactory.getSDK().getTerminalStateManager().openFunction(TerminalState.INDIVIDUAL_CALLING, IndividualCallState.IDLE);
            if(code == BaseCommonCode.SUCCESS_CODE){
                Intent intent = new Intent(ReceiveHandlerService.this,ReceiveVoipService.class);
                Log.e("ReceiveHandlerService", "Domain:"+linphoneCall.getCallLog().getFrom().getDomain());
                LinphoneAddress from = linphoneCall.getCallLog().getFrom();
                String userName = from.getUserName();
                Log.e("ReceiveHandlerService", "userName:"+userName);
                String displayName = from.getDisplayName();
                Log.e("ReceiveHandlerService", "displayName:"+displayName);
                intent.putExtra(Constants.USER_NAME,userName);
                startService(intent);
            }
        }
    };
    /**
     * 收到别人请求我开启直播的通知
     **/
    private ReceiveNotifyLivingIncommingHandler receiveNotifyLivingIncommingHandler = (mainMemberName, mainMemberId) -> myHandler.post(() -> {
        Intent intent = new Intent();
        intent.putExtra(Constants.MEMBER_NAME, mainMemberName);
        intent.putExtra(Constants.MEMBER_ID, mainMemberId);
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
            intent.setClass(ReceiveHandlerService.this, ReceiveLiveCommingService.class);
            startService(intent);
        }
    });

    private ReceiveGoWatchRTSPHandler receiveGoWatchRTSPHandler = terminalMessage -> myHandler.post(() -> goWatchGB28121(terminalMessage));

    private void goWatchGB28121(TerminalMessage terminalMessage){
        Intent intent = new Intent(ReceiveHandlerService.this,PullGB28181Service.class);
        intent.putExtra(Constants.TERMINALMESSAGE,terminalMessage);
        startService(intent);
    }

    private void showToast(final String resultDesc){
        ToastUtil.showToast(ReceiveHandlerService.this, resultDesc);
    }


    private long lastNotifyTime = 0;
    /**
     * 接收到消息
     */
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
                noticeContent = terminalMessage.messageFromName + ":" + content;
            }else{
                noticeContent = content;
            }
        }
        if(terminalMessage.messageType == MessageType.PICTURE.getCode()){
            if(terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){
                noticeContent = terminalMessage.messageFromName + ":" + "[图片]";
            }else{
                noticeContent = "[图片]";
            }
        }
        if(terminalMessage.messageType == MessageType.AUDIO.getCode()){
            if(terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){
                noticeContent = terminalMessage.messageFromName + ":" + "[语音]";
            }else{
                noticeContent = "[语音]";
            }
        }
        if(terminalMessage.messageType == MessageType.VIDEO_CLIPS.getCode()){
            if(terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){
                noticeContent = terminalMessage.messageFromName + ":" + "[视频]";
            }else{
                noticeContent = "[视频]";
            }
        }
        if(terminalMessage.messageType == MessageType.FILE.getCode()){
            if(terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){
                noticeContent = terminalMessage.messageFromName + ":" + "[文件]";
            }else{
                noticeContent = "[文件]";
            }
        }
        if(terminalMessage.messageType == MessageType.POSITION.getCode()){
            if(terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){
                noticeContent = terminalMessage.messageFromName + ":" + "[位置]";
            }else{
                noticeContent = "[位置]";
            }
        }
        if(terminalMessage.messageType == MessageType.AFFICHE.getCode()){
            if(terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){
                noticeContent = terminalMessage.messageFromName + ":" + "[公告]";
            }else{
                noticeContent = "[公告]";
            }
        }
        if(terminalMessage.messageType == MessageType.WARNING_INSTANCE.getCode()){
            if(terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){
                noticeContent = terminalMessage.messageFromName + ":" + "[警情]";
            }else{
                noticeContent = "[警情]";
            }
        }
        if(terminalMessage.messageType == MessageType.PRIVATE_CALL.getCode()){
            if(terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){
                noticeContent = terminalMessage.messageFromName + ":" + "[个呼]";
            }else{
                noticeContent = "[个呼]";
            }
        }
        if(terminalMessage.messageType == MessageType.VIDEO_LIVE.getCode()){
            if(terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){
                noticeContent = terminalMessage.messageFromName + ":" + "[图像]";
            }else{
                noticeContent = "[图像]";
            }
        }
        if(terminalMessage.messageType == MessageType.GROUP_CALL.getCode()){
            if(terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){
                noticeContent = terminalMessage.messageFromName + ":" + "[组呼]";
            }else{
                noticeContent = "[组呼]";
            }
        }
        if(terminalMessage.messageType == MessageType.AUDIO.getCode()){
            if(terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){
                noticeContent = terminalMessage.messageFromName + ":" + "[录音]";
            }else{
                noticeContent = "[录音]";
            }
        }
        if(terminalMessage.messageType == MessageType.HYPERLINK.getCode()){//人脸识别
            if(terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){
                noticeContent = terminalMessage.messageFromName + ":" + "[人脸识别]";
            }else{
                noticeContent = "[人脸识别]";
            }
        }
        Intent intent = new Intent(getApplicationContext(), NotificationClickReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("TerminalMessage", terminalMessage);
        intent.putExtra("bundle", bundle);
        Log.e("IndividualCallService", "通知栏消息:" + terminalMessage);
        //            intent.putExtra("TerminalMessage",terminalMessage);
        PendingIntent pIntent = PendingIntent.getBroadcast(getApplicationContext(), noticeId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder myBuilder = new Notification.Builder(getApplicationContext());
        myBuilder.setContentTitle(noticeTitle)//设置通知标题
                .setContentText(unReadCountText + noticeContent)//设置通知内容
                .setTicker("您有一条新消息！")//设置状态栏提示消息
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
        notificationManager.notify(noticeId, notification);
        lastNotifyTime = System.currentTimeMillis();
    };

    private void showDialogView(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(!Settings.canDrawOverlays(ReceiveHandlerService.this)){
                ToastUtil.showToast(ReceiveHandlerService.this, "请打开悬浮窗权限，否则私密呼叫和图像功能无法使用！");
                return;
            }
        }
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
            stackViewAdapter = new StackViewAdapter(getApplicationContext());
            stackViewAdapter.setData(data);
            swipeFlingAdapterView.setFlingListener(new FlingListener());
            stackViewAdapter.setCloseDialogListener(new OnClickListenerCloseDialog());
            stackViewAdapter.setGoWatchListener(new OnClickListenerGoWatch());
            swipeFlingAdapterView.setAdapter(stackViewAdapter);
        }
    }


    //接收到上报视频的回调
    private ReceiverActivePushVideoHandler receiverActivePushVideoHandler = memberId -> {
        logger.error("上报给：" + memberId);
        if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_PUSH.name())){
            ToastUtil.showToast(getApplicationContext(),getResources().getString(R.string.no_push_authority));
            return;
        }
        if(memberId == 0){//要弹出选择成员页
            Intent intent = new Intent(ReceiveHandlerService.this, InviteMemberService.class);
            intent.putExtra(Constants.TYPE, Constants.PUSH);
            intent.putExtra(Constants.PUSHING, false);
            startService(intent);
        }else{//直接上报了
            if(MyApplication.instance.usbAttached){
                Intent intent = new Intent(ReceiveHandlerService.this,SwitchCameraService.class);
                intent.putExtra(Constants.TYPE,Constants.ACTIVE_PUSH);
                intent.putExtra(Constants.CAMERA_TYPE,Constants.UVC_CAMERA);
                startService(intent);
            }else{
                if(Constants.HYTERA.equals(Build.MODEL)){
                    Intent intent = new Intent(ReceiveHandlerService.this,SwitchCameraService.class);
                    intent.putExtra(Constants.TYPE,Constants.ACTIVE_PUSH);
                    intent.putExtra(Constants.CAMERA_TYPE,Constants.RECODER_CAMERA);
                    startService(intent);
                }else{
                    int requestCode = MyTerminalFactory.getSDK().getLiveManager().requestMyselfLive("", "");
                    if(requestCode == BaseCommonCode.SUCCESS_CODE){
                        //请求成功,直接开始推送视频
                        Intent intent = new Intent();
                        intent.putExtra(Constants.TYPE, Constants.ACTIVE_PUSH);
                        intent.setClass(ReceiveHandlerService.this, PhonePushService.class);
                        ArrayList<Integer> memberIds = new ArrayList<>();
                        memberIds.add(memberId);
                        intent.putIntegerArrayListExtra(Constants.PUSH_MEMBERS, memberIds);
                        startService(intent);
                    }else{
                        ToastUtil.livingFailToast(ReceiveHandlerService.this, requestCode, TerminalErrorCode.LIVING_PUSHING.getErrorCode());
                    }
                }
            }
        }
    };

    /**
     * 请求直播
     */
    private ReceiverRequestVideoHandler receiverRequestVideoHandler = member -> {
        if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_ASK.name())){
            ToastUtil.showToast(getApplicationContext(),getResources().getString(R.string.no_pull_authority));
            return;
        }
        logger.error("请求的直播人：" + member);
        if(member.getNo() == 0){
            Intent intent = new Intent(ReceiveHandlerService.this, InviteMemberService.class);
            intent.putExtra(Constants.TYPE, Constants.PULL);
            intent.putExtra(Constants.PULLING, false);
            startService(intent);

        }else{//直接请求
            int requestCode = MyTerminalFactory.getSDK().getLiveManager().requestMemberLive(member.id, "");

            if(requestCode == BaseCommonCode.SUCCESS_CODE){

                Intent intent = new Intent(ReceiveHandlerService.this, LiveRequestService.class);
                intent.putExtra(Constants.MEMBER_NAME, member.getName());
                intent.putExtra(Constants.MEMBER_ID, member.getNo());
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
}
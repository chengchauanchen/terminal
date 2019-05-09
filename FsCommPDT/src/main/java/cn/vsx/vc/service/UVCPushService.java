package cn.vsx.vc.service;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import org.easydarwin.push.EasyPusher;
import org.easydarwin.push.InitCallback;
import org.easydarwin.push.UVCMediaStream;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.TerminalErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState;
import cn.vsx.hamster.terminalsdk.model.VideoMember;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveExternStorageSizeHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetVideoPushUrlHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallCeasedIndicationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberJoinOrExitHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyLivingStoppedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyOtherStopVideoMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseMyselfLiveHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveServerConnectionEstablishedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUVCCameraConnectChangeHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.MemberEnterAdapter;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.model.PushLiveMemberList;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.receiveHandle.ReceiverCloseKeyBoardHandler;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.HandleIdUtil;
import cn.vsx.vc.utils.MyDataUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * 外置摄像头上报视频
 */
public class UVCPushService extends BaseService{

    private TextureView mSvLivePop;
    private TextureView mSvUvcLive;
    private TextView mTvUvcLiveTheme;
    private LinearLayout mLlUvcSpeakState;
    private TextView mTvUvcLiveSpeakingName;
    private TextView mTvUvcLiveGroupName;
    private TextView mTvUvcLiveSpeakingId;
    private ImageView mIvUvcLiveRetract;
    private TextView mTvUvcLiveTime;
    private ListView mLvUvcLiveMemberInfo;
    private ImageView mIvUvcHangup;
    private ImageView mIvUvcInviteMember;
    private RelativeLayout mPopMiniLive;
    private RelativeLayout mUsbLive;
    private LinearLayout mLlFunction;
    private float downX = 0;
    private float downY = 0;
    private int oddOffsetX = 0;
    private int oddOffsetY = 0;
    private MemberEnterAdapter enterOrExitMemberAdapter;
    private List<VideoMember> watchOrExitMembers;
    private ArrayList<VideoMember> watchMembers;
    private List<String> pushMemberList = new ArrayList<>();
    private PushCallback pushCallback;
    private static final int CURRENTTIME = 0;
    private static final int HIDELIVINGVIEW = 1;
    private UVCMediaStream mUvcMediaStream;
    private int pushcount;
    private ArrayList<String> listResolution;
    private View mLlUvcInviteMember;
    private boolean isGroupPushLive;

    public UVCPushService(){}


    @SuppressLint("InflateParams")
    @Override
    protected void setRootView(){
        rootView = LayoutInflater.from(MyTerminalFactory.getSDK().application).inflate(R.layout.layout_uvc_push, null);
    }

    @Override
    protected void initWindow() {
        super.initWindow();
        //如果屏幕宽度小于高度就开启横屏
        if(screenWidth < screenHeight){
            layoutParams1.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        }
    }

    @Override
    protected void findView(){
        mPopMiniLive = rootView.findViewById(R.id.popup_mini_live);
        mSvLivePop = rootView.findViewById(R.id.sv_live_pop);
        mSvUvcLive = rootView.findViewById(R.id.sv_uvc_live);
        mUsbLive = rootView.findViewById(R.id.usb_live);
        mTvUvcLiveTheme = rootView.findViewById(R.id.tv_uvc_liveTheme);
        mLlUvcSpeakState = rootView.findViewById(R.id.ll_uvc_speak_state);
        mTvUvcLiveSpeakingName = rootView.findViewById(R.id.tv_uvc_live_speakingName);
        mTvUvcLiveGroupName = rootView.findViewById(R.id.tv_uvc_live_groupName);
        mTvUvcLiveSpeakingId = rootView.findViewById(R.id.tv_uvc_live_speakingId);
        mIvUvcLiveRetract = rootView.findViewById(R.id.iv_uvc_live_retract);
        mTvUvcLiveTime = rootView.findViewById(R.id.tv_uvc_live_time);
        mLvUvcLiveMemberInfo = rootView.findViewById(R.id.lv_uvc_live_member_info);
        mLlFunction = rootView.findViewById(R.id.ll_function);
        mIvUvcHangup = rootView.findViewById(R.id.iv_uvc_hangup);
        mIvUvcInviteMember = rootView.findViewById(R.id.iv_uvc_invite_member);
        mLlUvcInviteMember = rootView.findViewById(R.id.ll_uvc_invite_member);
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void initListener(){
        MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseMyselfLiveHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyLivingStoppedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGetVideoPushUrlHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveMemberJoinOrExitHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveExternStorageSizeHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveServerConnectionEstablishedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUVCCameraConnectChangeHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyOtherStopVideoMessageHandler);

        mSvUvcLive.setSurfaceTextureListener(surfaceTextureListener);
        mSvUvcLive.setOnClickListener(svOnClickListener);
        mSvLivePop.setSurfaceTextureListener(surfaceTextureListener);
        mIvUvcInviteMember.setOnClickListener(inviteMemberOnClickListener);
        mIvUvcHangup.setOnClickListener(hangUpOnClickListener);
        mIvUvcLiveRetract.setOnClickListener(retractOnClickListener);
        mPopMiniLive.setOnTouchListener(miniPopOnTouchListener);
        mLvUvcLiveMemberInfo.setOnTouchListener(listViewOnTouchListener);
    }

    @Override
    protected void initView(Intent intent){
        mPopMiniLive.setVisibility(View.GONE);
        mUsbLive.setVisibility(View.VISIBLE);
        setAuthorityView();
        String type = intent.getStringExtra(Constants.TYPE);
        isGroupPushLive =  intent.getBooleanExtra(Constants.IS_GROUP_PUSH_LIVING,false);
        if(Constants.ACTIVE_PUSH.equals(type)){
            PushLiveMemberList list = (PushLiveMemberList) intent.getSerializableExtra(Constants.PUSH_MEMBERS);
            if(list!=null&&list.getList()!=null){
                pushMemberList.clear();
                pushMemberList.addAll(list.getList());
            }
            String theme = intent.getStringExtra(Constants.THEME);
            if(TextUtils.isEmpty(theme)){
                mTvUvcLiveTheme.setText(getResources().getString(R.string.i_pushing_video));
            }else {
                mTvUvcLiveTheme.setText(theme);
            }
            int requestCode = MyTerminalFactory.getSDK().getLiveManager().requestMyselfLive(theme, "");
            if(requestCode != BaseCommonCode.SUCCESS_CODE){
                ToastUtil.livingFailToast(this, requestCode, TerminalErrorCode.LIVING_PUSHING.getErrorCode());
                finishVideoLive();
            }
        }else if(Constants.RECEIVE_PUSH.equals(type)){
            MyTerminalFactory.getSDK().getLiveManager().responseLiving(true);
            PromptManager.getInstance().stopRing();
            MyApplication.instance.isPrivateCallOrVideoLiveHand = true;

        }

        showLivingView();
        mHandler.sendEmptyMessage(CURRENTTIME);
        mHandler.sendEmptyMessageDelayed(HIDELIVINGVIEW, 5000);
    }

    @Override
    protected void showPopMiniView(){
        windowManager.removeView(rootView);
        windowManager.addView(rootView, layoutParams);
        hideAllView();
        MyApplication.instance.isMiniLive = true;
        mPopMiniLive.setVisibility(View.VISIBLE);
    }

    @Override
    protected void handleMesage(Message msg){
        switch(msg.what){
            case CURRENTTIME:
                setCurrentTime();
                break;
            case HIDELIVINGVIEW:
                mHandler.removeMessages(HIDELIVINGVIEW);
                hideLivingView();
                break;
            case OFF_LINE:
                ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.exit_push));
                stopBusiness();
                break;
        }
    }

    @Override
    protected void onNetworkChanged(boolean connected){
        if(!connected){
            if(!mHandler.hasMessages(OFF_LINE)){
                mHandler.sendEmptyMessageDelayed(OFF_LINE,OFF_LINE_TIME);
            }
        }else {
            mHandler.removeMessages(OFF_LINE);
            if(MyApplication.instance.isMiniLive){
                pushStream(mSvLivePop.getSurfaceTexture());
            }else{
                pushStream(mSvUvcLive.getSurfaceTexture());
            }
        }
    }

    @Override
    public void onDestroy(){
        //处理当正在录像的时候，异常退出处理
        if(mUvcMediaStream!=null){
            mUvcMediaStream.stopRecord();
        }
        super.onDestroy();
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseMyselfLiveHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyLivingStoppedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetVideoPushUrlHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMemberJoinOrExitHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveExternStorageSizeHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveServerConnectionEstablishedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUVCCameraConnectChangeHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyOtherStopVideoMessageHandler);
    }

    @Override
    protected void initData(){
        listResolution = new ArrayList<>(Arrays.asList("1920x1080", "1280x720", "640x480", "320x240"));
        watchOrExitMembers = new ArrayList<>();
        watchMembers = new ArrayList<>();
        enterOrExitMemberAdapter = new MemberEnterAdapter(MyTerminalFactory.getSDK().application, watchOrExitMembers);
        mLvUvcLiveMemberInfo.setAdapter(enterOrExitMemberAdapter);
    }

    @Override
    protected void initBroadCastReceiver(){}

    private ReceiveUpdateConfigHandler mReceiveUpdateConfigHandler= () -> mHandler.post(this::setAuthorityView);

    /**
     * 自己发起直播的响应
     **/
    private ReceiveResponseMyselfLiveHandler receiveResponseMyselfLiveHandler = (resultCode, resultDesc) -> mHandler.post(() -> {
        if(resultCode == BaseCommonCode.SUCCESS_CODE){
            if(pushMemberList != null && !pushMemberList.isEmpty()){
                logger.info("自己发起直播成功,要推送的列表：" + pushMemberList);
                MyTerminalFactory.getSDK().getLiveManager().requestNotifyWatch(pushMemberList,
                        MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0),
                        TerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO, 0l));
            }
        }else{
            ToastUtil.showToast(MyTerminalFactory.getSDK().application, resultDesc);
            finishVideoLive();
        }
    });

    private ReceiveGroupCallIncommingHandler receiveGroupCallIncommingHandler = (memberId, memberName, groupId, groupName, currentCallMode) -> {
        if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_LISTEN.name())){
            ToastUtil.showToast(MyTerminalFactory.getSDK().application,getString(R.string.text_has_no_group_call_listener_authority));
        }else{
            mHandler.post(() -> {
                mLlUvcSpeakState.setVisibility(View.VISIBLE);
                mTvUvcLiveGroupName.setText(groupName);
                mTvUvcLiveSpeakingName.setText(memberName);
                mTvUvcLiveSpeakingId.setText(HandleIdUtil.handleId(memberId));
            });
        }
    };

    private ReceiveGroupCallCeasedIndicationHandler receiveGroupCallCeasedIndicationHandler = (reasonCode) -> {
        logger.info("收到组呼停止");
        mHandler.post(() -> mLlUvcSpeakState.setVisibility(View.GONE));
    };

    /**
     * 通知直播停止 通知界面关闭视频页
     **/
    private ReceiveNotifyLivingStoppedHandler receiveNotifyLivingStoppedHandler = (liveMemberId, callId, methodResult, resultDesc) -> {
        ToastUtil.showToast(MyTerminalFactory.getSDK().application, getResources().getString(R.string.push_stoped));
        mHandler.post(this::finishVideoLive);
    };

    private String ip;
    private String port;
    private String id;
    /**
     * 自己发起直播的响应
     **/
    private ReceiveGetVideoPushUrlHandler receiveGetVideoPushUrlHandler = (streamMediaServerIp, streamMediaServerPort, callId) -> mHandler.postDelayed(() -> {
        logger.info("自己发起直播，服务端返回的ip：" + streamMediaServerIp + "端口：" + streamMediaServerPort + "---callId:" + callId);
        ip = streamMediaServerIp;
        port = String.valueOf(streamMediaServerPort);
        id = TerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO, 0L) + "_" + callId;
        //如果是组内上报，在组内发送一条上报消息
        sendGroupMessage(streamMediaServerIp,streamMediaServerPort,callId,pushMemberList,isGroupPushLive);
        startPush();
    }, 1000);

    /**
     * 观看成员的进入和退出
     **/
    @SuppressLint("SimpleDateFormat")
    private ReceiveMemberJoinOrExitHandler receiveMemberJoinOrExitHandler = (memberName, memberId, joinOrExit) -> mHandler.post(() -> {
        Log.e("IndividualCallService", memberName + ",memberId:" + memberId);
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        Date currentTime = new Date();
        String enterTime = formatter.format(currentTime);
        VideoMember videoMember = new VideoMember(memberId, memberName, enterTime, joinOrExit);
        watchOrExitMembers.add(videoMember);
        enterOrExitMemberAdapter.notifyDataSetChanged();
        if(joinOrExit){//进入直播间
            watchMembers.add(videoMember);
        }else{//退出直播间
            Iterator<VideoMember> iterator = watchMembers.iterator();
            while(iterator.hasNext()){
                if(iterator.next().getId() == memberId){
                    iterator.remove();
                    break;
                }
            }
        }
        if(!watchOrExitMembers.isEmpty()){
            mLvUvcLiveMemberInfo.smoothScrollToPosition(watchOrExitMembers.size() - 1);
        }
    });

    /**
     *通知存储空间不足
     */
    private ReceiveExternStorageSizeHandler mReceiveExternStorageSizeHandler = memorySize -> mHandler.post(() -> {
        if (memorySize < 100) {
            ToastUtil.showToast(UVCPushService.this, getString(R.string.toast_tempt_insufficient_storage_space));
            PromptManager.getInstance().startExternNoStorage();
//            if(mUvcMediaStream!=null&&mUvcMediaStream.isRecording()){
//                //停止录像
//                mUvcMediaStream.stopRecord();
//            }
            //上传没有上传的文件，删除已经上传的文件
            MyTerminalFactory.getSDK().getFileTransferOperation().externNoStorageOperation();
        } else if (memorySize < 200){
            PromptManager.getInstance().startExternStorageNotEnough();
            ToastUtil.showToast(UVCPushService.this, getString(R.string.toast_tempt_storage_space_is_in_urgent_need));
        }
    });

    private ReceiveUVCCameraConnectChangeHandler receiveUVCCameraConnectChangeHandler = connected -> {
        if(!connected){
            finishVideoLive();
        }
    };

    private ReceiveServerConnectionEstablishedHandler receiveServerConnectionEstablishedHandler = connected -> {
        if(!connected){
            ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.net_work_disconnect));
            mHandler.sendEmptyMessageDelayed(OFF_LINE,OFF_LINE_TIME);
        }
    };

    /**
     * 收到上报停止的通知
     */
    private ReceiveNotifyOtherStopVideoMessageHandler receiveNotifyOtherStopVideoMessageHandler = (message) -> {
        logger.info("收到停止上报通知");
        mHandler.post(() -> finishVideoLive());
    };


    private View.OnClickListener hangUpOnClickListener = v-> finishVideoLive();

    private View.OnClickListener retractOnClickListener = v -> showPopMiniView();

    private View.OnClickListener inviteMemberOnClickListener = v -> {
        Intent intent = new Intent(UVCPushService.this, InviteMemberService.class);
        intent.putExtra(Constants.TYPE, Constants.PUSH);
        intent.putExtra(Constants.PUSHING, true);
        intent.putExtra(Constants.INVITE_MEMBER_EXCEPT_UNIQUE_NO, MyDataUtil.getInviteMemberExceptList(watchMembers));
        startService(intent);
    };
    
    private View.OnClickListener svOnClickListener = v->{
        showLivingView();
        mHandler.removeMessages(HIDELIVINGVIEW);
        mHandler.sendEmptyMessageDelayed(HIDELIVINGVIEW, 5000);
    };
    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener(){
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height){
            logger.info("onSurfaceTextureAvailable----->" + surface);
            pushStream(surface);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height){
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface){
            logger.info("onSurfaceTextureDestroyed----->" + surface);
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface){
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private View.OnTouchListener miniPopOnTouchListener = (v,event)->{
        //触摸点到边界屏幕的距离
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                //触摸点到自身边界的距离
                downX = event.getX();
                downY = event.getY();
                oddOffsetX = layoutParams.x;
                oddOffsetY = layoutParams.y;
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();
                //不除以3，拖动的view抖动的有点厉害
                if(Math.abs(downX - moveX) > 5 || Math.abs(downY - moveY) > 5){
                    // 更新浮动窗口位置参数
                    layoutParams.x = (int) (screenWidth - (x + downX));
                    layoutParams.y = (int) (y - downY);
                    windowManager.updateViewLayout(rootView, layoutParams);
                }
                break;
            case MotionEvent.ACTION_UP:
                int newOffsetX = layoutParams.x;
                int newOffsetY = layoutParams.y;
                if(Math.abs(newOffsetX - oddOffsetX) <= 30 && Math.abs(newOffsetY - oddOffsetY) <= 30){
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverCloseKeyBoardHandler.class);
                    windowManager.removeView(rootView);
                    windowManager.addView(rootView, layoutParams1);
                    hideAllView();
                    MyApplication.instance.isMiniLive = false;
                    mUsbLive.setVisibility(View.VISIBLE);
                }
                break;
        }
        return true;
    };

    private class PushCallback implements InitCallback{

        @Override
        public void onCallback(int code){
            Bundle resultData = new Bundle();
            switch(code){
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
                    if(pushcount <= 10){
                        pushcount++;
                    }else{
                        finishVideoLive();
                    }
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_CONNECT_ABORT:
                    resultData.putString("event-msg", "EasyRTSP 连接异常中断");
                    if(pushcount <= 10){
                        pushcount++;
//                        startPush();
                    }else{
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
        }
    }

    private void hideAllView(){
        mPopMiniLive.setVisibility(View.GONE);
        mUsbLive.setVisibility(View.GONE);
    }

    private void startPush(){
        if(TextUtils.isEmpty(ip) || TextUtils.isEmpty(port) || TextUtils.isEmpty(id)){
            return;
        }
        logger.info("mMediaStream:" + mUvcMediaStream + "----SurfaceTexture:" + mSvUvcLive.getSurfaceTexture());
        if(mUvcMediaStream == null){
            if(mSvUvcLive.getSurfaceTexture() != null){
                pushStream(mSvUvcLive.getSurfaceTexture());
            }else{
                ToastUtil.showToast(MyTerminalFactory.getSDK().application, getResources().getString(R.string.push_failed));
                finishVideoLive();
                return;
            }
        }
        if(null == pushCallback){
            pushCallback = new PushCallback();
        }
        mUvcMediaStream.startStream(ip, port, id, pushCallback);
        String url = String.format("rtsp://%s:%s/%s.sdp", ip, port, id);
        logger.info("推送地址：" + url);
    }

    /**
     * 开始录像
     */
    private void startRecord(){
        //开始录像
        if (TerminalFactory.getSDK().checkeExternalStorageIsAvailable(MyTerminalFactory.getSDK().getFileTransferOperation().getExternalUsableStorageDirectory())) {
            mUvcMediaStream.startRecord();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private View.OnTouchListener listViewOnTouchListener = (v,event)->{
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                v.performClick();
                mHandler.removeMessages(HIDELIVINGVIEW);
                break;
            case MotionEvent.ACTION_UP:
                Message message = Message.obtain();
                message.what = HIDELIVINGVIEW;
                mHandler.sendMessageDelayed(message, 5000);
                break;
        }
        return false;
    };

    private void finishVideoLive(){
        mHandler.removeCallbacksAndMessages(null);
        stopPush();
        stopBusiness();
    }


    private void pushStream(SurfaceTexture surface){
        if(mUvcMediaStream != null){    // switch from background to front
//            mUvcMediaStream.stopPreview();
            mUvcMediaStream.setSurfaceTexture(surface);
            mUvcMediaStream.startPreview();
            startRecord();
            if(mUvcMediaStream.isStreaming()){
                ToastUtil.showToast(this, getResources().getString(R.string.pushing_stream));
            }
        }else{
            mUvcMediaStream = new UVCMediaStream(MyTerminalFactory.getSDK().application, surface);
            startCamera();
        }
        pushcount = 0;
    }

    private void stopPush(){
        mHandler.removeMessages(CURRENTTIME);
        if(mUvcMediaStream != null){
            mUvcMediaStream.stopPreview();
            mUvcMediaStream.stopStream();
            mUvcMediaStream.release();
            mUvcMediaStream = null;
            logger.info("---->>>>页面关闭，停止推送视频");
        }
        TerminalFactory.getSDK().getLiveManager().ceaseLiving();
    }

    private void startCamera(){
        int position = MyTerminalFactory.getSDK().getParam(Params.VIDEO_RESOLUTION, 2);
        String r = listResolution.get(position);
        String[] splitR = r.split("x");
        int width = Integer.parseInt(splitR[0]);
        int height = Integer.parseInt(splitR[1]);
        logger.error("分辨率--width:" + width + "----height:" + height);
        mUvcMediaStream.updateResolution(width, height);
        mUvcMediaStream.setDgree(getDgree());
        mUvcMediaStream.createCamera();
        mUvcMediaStream.startPreview();
        startRecord();
        logger.info("------>>>>startCamera");
        if(mUvcMediaStream.isStreaming()){
            ToastUtil.showToast(this, getResources().getString(R.string.pushing_stream));
        }
    }

    private int getDgree(){
        int rotation = windowManager.getDefaultDisplay().getRotation();
        int degrees = 0;
        switch(rotation){
            case Surface.ROTATION_0:
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

    @SuppressLint("SimpleDateFormat")
    private void setCurrentTime(){
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        Date currentTime = new Date();
        String dateString = formatter.format(currentTime);
        mTvUvcLiveTime.setText(dateString);
        mHandler.sendEmptyMessageDelayed(CURRENTTIME, 10000);
    }

    private void hideLivingView(){
        mTvUvcLiveTime.setVisibility(View.GONE);
        mIvUvcLiveRetract.setVisibility(View.GONE);
        mLlFunction.setVisibility(View.GONE);
    }

    private void showLivingView(){
        mTvUvcLiveTime.setVisibility(View.VISIBLE);
        mIvUvcLiveRetract.setVisibility(View.VISIBLE);
        mLlFunction.setVisibility(View.VISIBLE);
        setAuthorityView();
        if(MyApplication.instance.getGroupListenenState() == GroupCallListenState.LISTENING){
            if(null != MyApplication.instance.groupCallMember){
                mLlUvcSpeakState.setVisibility(View.VISIBLE);
                mTvUvcLiveSpeakingName.setText(MyApplication.instance.groupCallMember.getName());
                mTvUvcLiveSpeakingId.setText(HandleIdUtil.handleId(MyApplication.instance.groupCallMember.getId()));
            }
            if(MyApplication.instance.currentCallGroupId !=-1){
                mTvUvcLiveGroupName.setText(DataUtil.getGroupName(MyApplication.instance.currentCallGroupId));
            }
        }
    }

    private void setAuthorityView(){
        //推送图像权限
        if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_PUSH.name())){
            mLlUvcInviteMember.setVisibility(View.GONE);
        }else {
            mLlUvcInviteMember.setVisibility(View.VISIBLE);
        }
    }

}

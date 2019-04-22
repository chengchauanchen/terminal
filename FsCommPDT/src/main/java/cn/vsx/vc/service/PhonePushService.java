package cn.vsx.vc.service;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
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
import org.easydarwin.push.MediaStream;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.MemberEnterAdapter;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.model.PushLiveMemberList;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.receiveHandle.ReceiverCloseKeyBoardHandler;
import cn.vsx.vc.receiveHandle.ReceiverGroupPushLiveHandler;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.DataUtil;
import cn.vsx.vc.utils.HandleIdUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * 普通手机上报视频
 */
public class PhonePushService extends BaseService{

    private RelativeLayout mPopupMiniLive;
    private TextureView mSvLivePop;
    private RelativeLayout mRlPhonePushLive;
    private TextureView mSvLive;
    private TextView mLiveVedioTheme;
    private TextView mTvLiveRealtime;
    private ImageView mIvLiveRetract;
    private LinearLayout mLlLiveGroupCall;
    private TextView mTvLiveSpeakingName;
    private TextView mTvLiveGroupName;
    private TextView mTvLiveSpeakingId;
    private ListView mLvLiveMemberInfo;
    private LinearLayout mLlLiveChageCamera;
    private ImageView mIvLiveChageCamera;
    private LinearLayout mLlLiveHangupTotal;
    private LinearLayout mLlLiveInviteMember;
    private ImageView mIvLiveAddmember;
    private MediaStream mMediaStream;
    private int pushcount;
    private List<String> listResolution;
    private List<VideoMember> watchOrExitMembers;
    private ArrayList<VideoMember> watchMembers;
    private List<Long> pushMemberList = new ArrayList<>();
    private PushCallback pushCallback;
    private String ip;
    private String port;
    private String id;
    private float downX = 0;
    private float downY = 0;
    private int oddOffsetX = 0;
    private int oddOffsetY = 0;
    private MemberEnterAdapter enterOrExitMemberAdapter;
    private static final int CURRENTTIME = 0;
    private static final int HIDELIVINGVIEW = 1;
    private boolean isGroupPushLive;

    public PhonePushService(){}

    @SuppressLint("InflateParams")
    @Override
    protected void setRootView(){
        rootView = LayoutInflater.from(MyTerminalFactory.getSDK().application).inflate(R.layout.layout_phone_push, null);
    }

    @Override
    protected void findView(){
        //小窗口
        mPopupMiniLive = rootView.findViewById(R.id.popup_mini_live);
        mSvLivePop = rootView.findViewById(R.id.sv_live_pop);
        //大窗口
        mRlPhonePushLive = rootView.findViewById(R.id.rl_phone_push_live);
        mSvLive = rootView.findViewById(R.id.sv_live);
        mLiveVedioTheme = rootView.findViewById(R.id.live_vedioTheme);
        mTvLiveRealtime = rootView.findViewById(R.id.tv_live_realtime);
        mIvLiveRetract = rootView.findViewById(R.id.iv_live_retract);
        mLlLiveGroupCall = rootView.findViewById(R.id.ll_live_group_call);
        mTvLiveSpeakingName = rootView.findViewById(R.id.tv_live_speakingName);
        mTvLiveGroupName = rootView.findViewById(R.id.tv_live_groupName);
        mTvLiveSpeakingId = rootView.findViewById(R.id.tv_live_speakingId);
        mLvLiveMemberInfo = rootView.findViewById(R.id.lv_live_member_info);
        mLlLiveChageCamera = rootView.findViewById(R.id.ll_live_chage_camera);
        mIvLiveChageCamera = rootView.findViewById(R.id.iv_live_chage_camera);
        mLlLiveHangupTotal = rootView.findViewById(R.id.ll_live_hangup_total);
        mLlLiveInviteMember = rootView.findViewById(R.id.ll_live_invite_member);
        mIvLiveAddmember = rootView.findViewById(R.id.iv_live_addmember);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initListener(){
        MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseMyselfLiveHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyLivingStoppedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGetVideoPushUrlHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveMemberJoinOrExitHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveExternStorageSizeHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyOtherStopVideoMessageHandler);


        mSvLive.setSurfaceTextureListener(surfaceTextureListener);
        mSvLive.setOnClickListener(svOnClickListener);
//        mSvLive.setOnTouchListener(svOnTouchListener);
        mSvLivePop.setSurfaceTextureListener(surfaceTextureListener);
        mIvLiveAddmember.setOnClickListener(inviteMemberOnClickListener);
        mLlLiveHangupTotal.setOnClickListener(hangUpOnClickListener);
        mIvLiveRetract.setOnClickListener(retractOnClickListener);
        mPopupMiniLive.setOnTouchListener(miniPopOnTouchListener);
        mIvLiveChageCamera.setOnClickListener(changeCameraOnClickListener);
        mLvLiveMemberInfo.setOnTouchListener(listViewOnTouchListener);
    }

    @Override
    protected void initView(Intent intent){
        String type = intent.getStringExtra(Constants.TYPE);
        isGroupPushLive =  intent.getBooleanExtra(Constants.IS_GROUP_PUSH_LIVING,false);
        hideAllView();
        mRlPhonePushLive.setVisibility(View.VISIBLE);
        showLivingView();
        mHandler.sendEmptyMessage(CURRENTTIME);
        mHandler.sendEmptyMessageDelayed(HIDELIVINGVIEW, 5000);
        if(Constants.ACTIVE_PUSH.equals(type)){
            PushLiveMemberList list = (PushLiveMemberList) intent.getSerializableExtra(Constants.PUSH_MEMBERS);
            if(list!=null&&list.getList()!=null){
                pushMemberList.clear();
                pushMemberList.addAll(list.getList());
            }
            String theme = intent.getStringExtra(Constants.THEME);
            if(TextUtils.isEmpty(theme)){
                mLiveVedioTheme.setText(getResources().getString(R.string.i_pushing_video));
            }else {
                mLiveVedioTheme.setText(theme);
            }
            int requestCode = MyTerminalFactory.getSDK().getLiveManager().requestMyselfLive(theme, "");
            if(requestCode != BaseCommonCode.SUCCESS_CODE){
                ToastUtil.livingFailToast(this, requestCode, TerminalErrorCode.LIVING_PUSHING.getErrorCode());
                finishVideoLive();
            }
        }else if(Constants.RECEIVE_PUSH.equals(type)){
            MyTerminalFactory.getSDK().getLiveManager().responseLiving(true);
            PromptManager.getInstance().stopRing();
            mLiveVedioTheme.setText(getResources().getString(R.string.i_pushing_video));
            MyApplication.instance.isPrivateCallOrVideoLiveHand = true;
        }
    }

    @Override
    protected void showPopMiniView(){
        windowManager.removeView(rootView);
        windowManager.addView(rootView, layoutParams);
        hideAllView();
        MyApplication.instance.isMiniLive = true;
        mPopupMiniLive.setVisibility(View.VISIBLE);
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
                pushStream(mSvLive.getSurfaceTexture());
            }
        }
    }

    protected void initBroadCastReceiver(){}

    protected void initData(){
        listResolution = new ArrayList<>(Arrays.asList("1920x1080", "1280x720", "640x480", "320x240"));
        watchOrExitMembers = new ArrayList<>();
        watchMembers = new ArrayList<>();
        enterOrExitMemberAdapter = new MemberEnterAdapter(MyTerminalFactory.getSDK().application, watchOrExitMembers);
        mLvLiveMemberInfo.setAdapter(enterOrExitMemberAdapter);
    }

    @Override
    public void onDestroy(){
        //处理当正在录像的时候，异常退出处理
        if(mMediaStream!=null){
            mMediaStream.stopRecord();
        }
        super.onDestroy();
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseMyselfLiveHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyLivingStoppedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetVideoPushUrlHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMemberJoinOrExitHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveExternStorageSizeHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyOtherStopVideoMessageHandler);
    }

    private void hideAllView(){
        mPopupMiniLive.setVisibility(View.GONE);
        mRlPhonePushLive.setVisibility(View.GONE);
    }

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
                mLlLiveGroupCall.setVisibility(View.VISIBLE);
                mTvLiveGroupName.setText(DataUtil.getGroupByGroupNo(groupId).name);
                mTvLiveSpeakingName.setText(memberName);
                mTvLiveSpeakingId.setText(HandleIdUtil.handleId(memberId));
            });
        }
    };

    private ReceiveUpdateConfigHandler mReceiveUpdateConfigHandler= () -> mHandler.post(this::setPushAuthority);

    private ReceiveGroupCallCeasedIndicationHandler receiveGroupCallCeasedIndicationHandler = (reasonCode) -> {
        logger.info("收到组呼停止");
        mHandler.post(() -> mLlLiveGroupCall.setVisibility(View.GONE));
    };

    /**
     * 通知直播停止 通知界面关闭视频页
     **/
    private ReceiveNotifyLivingStoppedHandler receiveNotifyLivingStoppedHandler = (liveMemberId, callId, methodResult, resultDesc) -> mHandler.post(() -> {
        ToastUtil.showToast(MyTerminalFactory.getSDK().application, getResources().getString(R.string.push_stoped));
        finishVideoLive();
    });

    /**
     * 自己发起直播的响应
     **/
    private ReceiveGetVideoPushUrlHandler receiveGetVideoPushUrlHandler = (streamMediaServerIp, streamMediaServerPort, callId) -> mHandler.postDelayed(() -> {
        logger.info("自己发起直播，服务端返回的ip：" + streamMediaServerIp + "端口：" + streamMediaServerPort + "---callId:" + callId);
        ip = streamMediaServerIp;
        port = String.valueOf(streamMediaServerPort);
        id = TerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO, 0) + "_" + callId;
        //如果是组内上报，在组内发送一条上报消息
        if(isGroupPushLive){
            MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiverGroupPushLiveHandler.class, streamMediaServerIp,streamMediaServerPort,callId);
        }
        startPush();
//        startRecord();
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
            mLvLiveMemberInfo.smoothScrollToPosition(watchOrExitMembers.size() - 1);
        }
    });

    /**
     *通知存储空间不足
     */
    private ReceiveExternStorageSizeHandler mReceiveExternStorageSizeHandler = memorySize -> mHandler.post(() -> {
        if (memorySize < 100) {
            ToastUtil.showToast(PhonePushService.this, getString(R.string.toast_tempt_insufficient_storage_space));
            PromptManager.getInstance().startExternNoStorage();
            if(mMediaStream!=null&&mMediaStream.isRecording()){
                //停止录像
                mMediaStream.stopRecord();
            }
            //上传没有上传的文件，删除已经上传的文件
            MyTerminalFactory.getSDK().getFileTransferOperation().externNoStorageOperation();
        } else if (memorySize < 200){
            PromptManager.getInstance().startExternStorageNotEnough();
            ToastUtil.showToast(PhonePushService.this, getString(R.string.toast_tempt_storage_space_is_in_urgent_need));
        }
    });
    /**
     * 收到上报停止的通知
     */
    private ReceiveNotifyOtherStopVideoMessageHandler receiveNotifyOtherStopVideoMessageHandler = (message) -> {
        logger.info("收到停止上报通知");
        mHandler.post(() -> finishVideoLive());
    };


    /**
     * 根据权限设置组呼PTT图标
     */
    private void setPushAuthority() {
        //图像推送
        if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_PUSH.name())){
            mIvLiveAddmember.setEnabled(false);
            mLlLiveInviteMember.setVisibility(View.GONE);
        }else {
            mLlLiveInviteMember.setVisibility(View.VISIBLE);
        }
    }

    private void startPush(){
        if(TextUtils.isEmpty(ip) || TextUtils.isEmpty(port) || TextUtils.isEmpty(id)){
            return;
        }
        logger.info("mMediaStream:" + mMediaStream + "----SurfaceTexture:" + mSvLive.getSurfaceTexture());
        if(mMediaStream == null){
            if(mSvLive.getSurfaceTexture() != null){
                pushStream(mSvLive.getSurfaceTexture());
            }else{
                ToastUtil.showToast(MyTerminalFactory.getSDK().application, getResources().getString(R.string.push_failed));
                finishVideoLive();
                return;
            }
        }
        if(null == pushCallback){
            pushCallback = new PushCallback();
        }
        mMediaStream.startStream(ip, port, id, pushCallback);
        String url = String.format("rtsp://%s:%s/%s.sdp", ip, port, id);
        logger.info("推送地址：" + url);

    }

    /**
     * 开始录像
     */
    private void startRecord(){
        //开始录像
        if (TerminalFactory.getSDK().checkeExternalStorageIsAvailable(MyTerminalFactory.getSDK().getFileTransferOperation().getExternalUsableStorageDirectory())) {
//            if(!mMediaStream.isRecording()){
                mMediaStream.startRecord();
//            }
        }
    }

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



    private View.OnClickListener hangUpOnClickListener = v-> finishVideoLive();

    private View.OnClickListener retractOnClickListener = v -> showPopMiniView();

    private View.OnClickListener changeCameraOnClickListener = v -> changeCamera();

    private View.OnClickListener svOnClickListener = v->{
        if(null != mMediaStream && mMediaStream.isStreaming()){
            mMediaStream.getCamera().autoFocus(null);//屏幕聚焦
        }
        showLivingView();
        mHandler.removeMessages(HIDELIVINGVIEW);
        mHandler.sendEmptyMessageDelayed(HIDELIVINGVIEW, 5000);
    };

    private float oldDist;
    @SuppressLint("ClickableViewAccessibility")
    private View.OnTouchListener svOnTouchListener = (v,event)->{

        if (event.getPointerCount() == 1) {
            handleFocus(mMediaStream.getCamera());
        } else {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = getFingerSpacing(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    float newDist = getFingerSpacing(event);
                    if(Math.abs(newDist-oldDist) > 5f){
                        if (newDist > oldDist) {
                            handleZoom(true);
                        } else if (newDist < oldDist) {
                            handleZoom(false);
                        }
                        oldDist = newDist;
                    }
                    break;
            }
        }
        return true;
    };

    private View.OnClickListener inviteMemberOnClickListener = v -> {
        if (MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_PUSH.name())) {
            Intent intent = new Intent(PhonePushService.this, InviteMemberService.class);
            intent.putExtra(Constants.TYPE, Constants.PUSH);
            intent.putExtra(Constants.PUSHING, true);
            intent.putExtra(Constants.WATCHING_MEMBERS, watchMembers);
            startService(intent);
        }else{
            ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.text_no_video_push_authority));
        }
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
                    windowManager.addView(rootView,layoutParams1);
                    hideAllView();
                    MyApplication.instance.isMiniLive = false;
                    mRlPhonePushLive.setVisibility(View.VISIBLE);
                }
                break;
        }
        return true;
    };

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

    private void handleFocus(Camera camera){
        if(null != mMediaStream && mMediaStream.isStreaming()){
            camera.autoFocus(null);//屏幕聚焦
        }
    }

    private void handleZoom(boolean isScale){
        if(null !=mMediaStream && mMediaStream.isStreaming()){
            mMediaStream.ZoomOrReduceVideo(isScale);
        }
    }

    private float getFingerSpacing(MotionEvent event){
        return (float)Math.sqrt(event.getX(0)*event.getX(1)+event.getY(0)*event.getY(1));
    }

    private void changeCamera(){
        logger.info("开始转换摄像头");
        mMediaStream.setDgree(getDgree());
        mMediaStream.switchCamera();
    }


    private void finishVideoLive(){
        mHandler.removeCallbacksAndMessages(null);
        MyTerminalFactory.getSDK().getLiveManager().ceaseLiving();
        PromptManager.getInstance().stopRing();//停止响铃
        stopPush();
        hideAllView();
        stopBusiness();
    }

    private void pushStream(SurfaceTexture surface){
        if(mMediaStream != null){    // switch from background to front
            mMediaStream.stopPreview();
            if(mMediaStream.isRecording()){
                mMediaStream.stopRecord();
            }
            mMediaStream.setSurfaceTexture(surface);
            mMediaStream.startPreview();
            startRecord();
            if(mMediaStream.isStreaming()){
                ToastUtil.showToast(PhonePushService.this, getResources().getString(R.string.pushing_stream));
            }
        }else{
            mMediaStream = new MediaStream(MyTerminalFactory.getSDK().application, surface, true);
            startCamera();
        }
        pushcount = 0;
    }

    private void stopPush(){
        mHandler.removeMessages(CURRENTTIME);
        if(mMediaStream != null){
            mMediaStream.stopPreview();
            mMediaStream.stopStream();
            mMediaStream.stopRecord();
            mMediaStream.release();
            mMediaStream = null;
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
        mMediaStream.updateResolution(width, height);
        mMediaStream.setDgree(getDgree());
        mMediaStream.createCamera();
        mMediaStream.startPreview();
        logger.info("------>>>>startCamera");
        startRecord();
        if(mMediaStream.isStreaming()){
            ToastUtil.showToast(PhonePushService.this, getResources().getString(R.string.pushing_stream));
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
        mTvLiveRealtime.setText(dateString);
        mHandler.sendEmptyMessageDelayed(CURRENTTIME, 10000);
    }

    private void hideLivingView(){
        mIvLiveRetract.setVisibility(View.GONE);
        mLlLiveChageCamera.setVisibility(View.GONE);
        mLlLiveHangupTotal.setVisibility(View.GONE);
        mLlLiveInviteMember.setVisibility(View.GONE);
    }

    private void showLivingView(){
        setPushAuthority();
        mTvLiveRealtime.setVisibility(View.VISIBLE);
        mIvLiveRetract.setVisibility(View.VISIBLE);
        mLlLiveChageCamera.setVisibility(View.VISIBLE);
        mLlLiveHangupTotal.setVisibility(View.VISIBLE);
        mLlLiveInviteMember.setVisibility(View.VISIBLE);
        if(MyApplication.instance.getGroupListenenState() == GroupCallListenState.LISTENING){
            if(null != MyApplication.instance.groupCallMember){
                mLlLiveGroupCall.setVisibility(View.VISIBLE);
                mTvLiveSpeakingName.setText(MyApplication.instance.groupCallMember.getName());
                mTvLiveSpeakingId.setText(HandleIdUtil.handleId(MyApplication.instance.groupCallMember.getId()));
            }
            if(MyApplication.instance.currentCallGroupId !=-1){
                mTvLiveGroupName.setText(DataUtil.getGroupByGroupNo(MyApplication.instance.currentCallGroupId).name);
            }
        }
    }
}

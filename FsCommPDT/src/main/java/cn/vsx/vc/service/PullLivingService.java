package cn.vsx.vc.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.IGotaKeyHandler;
import android.app.IGotaKeyMonitor;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import org.apache.http.util.TextUtils;
import org.easydarwin.video.EasyRTSPClient;
import org.easydarwin.video.RTMPEasyPlayerClient;
import org.easydarwin.video.RTSPClient;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.SignalServerErrorCode;
import cn.vsx.hamster.errcode.module.TerminalErrorCode;
import cn.vsx.hamster.protolbuf.PTTProtolbuf;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCallingCannotClickHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCeaseGroupCallConformationHander;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetRtspStreamUrlHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallCeasedIndicationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberNotLivingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNetworkChangeHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyLivingStoppedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyMemberStopWatchMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivePTTDownHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivePTTUpHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRequestGroupCallConformationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Util;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.model.InviteMemberExceptList;
import cn.vsx.vc.model.InviteMemberLiverMember;
import cn.vsx.vc.receiveHandle.ReceiverCloseKeyBoardHandler;
import cn.vsx.vc.utils.BitmapUtil;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.HandleIdUtil;
import cn.vsx.vc.utils.NetworkUtil;
import cn.vsx.vc.utils.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.audio.CheckMyPermission;

import static cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState.LISTENING;
import static cn.vsx.vc.receive.Actions.KILL_LIVE_WATCH;

/**
 * 观看上报
 */
public class PullLivingService extends BaseService{

    private TextureView mSvLivePop;
    private RelativeLayout mRlLiveGeneralView;
    private TextureView mSvLive;
    private TextView mLiveVedioTheme;
    private ImageView mIvLiveRetract;
//    private ImageView mLiveVedioIcon;
    private TextView mLiveVedioName;
    private TextView mLiveVedioId;
    private LinearLayout mLlLiveLookHangup;
    private LinearLayout mLlLiveLookInviteMember;
    private Button mBtnLiveLookPtt;
    private LinearLayout mLlLiveGroupCall;
    private TextView mTvLiveSpeakingName;
    private TextView mTvLiveGroupName;
    private TextView mTvLiveSpeakingId;
    protected LinearLayout mLlRefreshing;
    protected ImageView mRefreshingIcon;
    private boolean rtspPlay = true;
    private String url;
    private RTMPEasyPlayerClient rtmpClient;
    private EasyRTSPClient mStreamRender;
    private int mLiveWidth;
    private int mLiveHeight;
    private String theme;
    private float downX = 0;
    private float downY = 0;
    private int oddOffsetX = 0;
    private int oddOffsetY = 0;
    private RelativeLayout mPopupMiniLive;
    private RelativeLayout mRlPullLive;
    public Member liveMember;
    protected LinearLayout llNoNetwork;
    //callId
    public long callId;

    public PullLivingService(){}

    @Override
    @SuppressLint({"WrongConstant", "ClickableViewAccessibility"})
    protected void initListener(){
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNetworkChangeHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receivePTTUpHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receivePTTDownHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGetRtspStreamUrlHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveMemberNotLivingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyLivingStoppedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveRequestGroupCallConformationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveCeaseGroupCallConformationHander);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyMemberStopWatchMessageHandler);
//        MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseWatchLiveAndTempGroupMessageHandler);
        mPopupMiniLive.setOnTouchListener(miniPopOnTouchListener);
        mSvLive.setSurfaceTextureListener(surfaceTextureListener);
        mSvLivePop.setSurfaceTextureListener(surfaceTextureListener);
        mBtnLiveLookPtt.setOnTouchListener(pttOnTouchListener);
        mLlLiveLookHangup.setOnClickListener(hangUpOnClickListener);
        mIvLiveRetract.setOnClickListener(retractOnClickListener);
        mLlLiveLookInviteMember.setOnClickListener(inviteMemberOnClickListener);
        //GH880手机按键服务
        IGotaKeyMonitor keyMointor = (IGotaKeyMonitor) getSystemService("gotakeymonitor");
        if(null != keyMointor){
            try{
                IGotaKeyHandler gotaKeyHandler = keyMointor.setHandler(gotaKeHandler);
                keyMointor.setHandler(gotaKeyHandler);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void initView(Intent intent){
        mRlPullLive.setVisibility(View.VISIBLE);
        mPopupMiniLive.setVisibility(View.GONE);
        setAuthorityView();
        String watchType = intent.getStringExtra(Constants.WATCH_TYPE);
        if(Constants.ACTIVE_WATCH.equals(watchType)){
            TerminalMessage terminalMessage = (TerminalMessage) intent.getSerializableExtra(Constants.TERMINALMESSAGE);
            requestToWatchLiving(terminalMessage);
        }else if(Constants.RECEIVE_WATCH.equals(watchType)){
            url = intent.getStringExtra(Constants.RTSP_URL);
            liveMember = (Member) intent.getSerializableExtra(Constants.LIVE_MEMBER);
            mLiveVedioName.setText(liveMember.getName());
            mLiveVedioId.setText(HandleIdUtil.handleId(liveMember.getNo()));
            theme = String.format(getString(R.string.text_pushing_video_name),liveMember.getName());
            showPullView();
        }
    }

    private void setAuthorityView(){
        //组呼权限
        if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_TALK.name())){
            mBtnLiveLookPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_gray);
            mBtnLiveLookPtt.setEnabled(false);
        }else{
            mBtnLiveLookPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_silence);
        }
        //推送图像权限
        if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_PUSH.name())){
            mLlLiveLookInviteMember.setVisibility(View.GONE);
        }else {
//            mLlLiveLookInviteMember.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void showPopMiniView(){
        MyApplication.instance.isMiniLive = true;
        windowManager.removeView(rootView);
        windowManager.addView(rootView, layoutParams);
        mRlPullLive.setVisibility(View.GONE);
        mPopupMiniLive.setVisibility(View.VISIBLE);
    }

    @Override
    protected void handleMesage(Message msg){
        switch(msg.what){
            case OFF_LINE:
                ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.exit_pull));
                stopBusiness();
                break;
        }
    }

    @Override
    protected void onNetworkChanged(boolean connected){
    }

    @SuppressLint("InflateParams")
    @Override
    protected void setRootView(){
        rootView = LayoutInflater.from(MyTerminalFactory.getSDK().application).inflate(R.layout.layout_pull_stream, null);
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
        mPopupMiniLive = rootView.findViewById(R.id.popup_mini_live);
        mSvLivePop = rootView.findViewById(R.id.sv_live_pop);
        mRlPullLive = rootView.findViewById(R.id.rl_pull_live);
        mRlLiveGeneralView = rootView.findViewById(R.id.rl_live_general_view);
        mSvLive = rootView.findViewById(R.id.sv_live);
        mLiveVedioTheme = rootView.findViewById(R.id.live_vedioTheme);
        mIvLiveRetract = rootView.findViewById(R.id.iv_live_retract);
        mLiveVedioName = rootView.findViewById(R.id.live_vedioName);
        mLiveVedioId = rootView.findViewById(R.id.live_vedioId);
        mLlLiveLookHangup = rootView.findViewById(R.id.ll_live_look_hangup);
        mLlLiveLookInviteMember = rootView.findViewById(R.id.ll_live_look_invite_member);
        mBtnLiveLookPtt = rootView.findViewById(R.id.btn_live_look_ptt);
        mLlLiveGroupCall = rootView.findViewById(R.id.ll_live_group_call);
        mTvLiveSpeakingName = rootView.findViewById(R.id.tv_live_speakingName);
        mTvLiveGroupName = rootView.findViewById(R.id.tv_live_groupName);
        mTvLiveSpeakingId = rootView.findViewById(R.id.tv_live_speakingId);
        llNoNetwork = rootView.findViewById(R.id.ll_no_network);
        mLlRefreshing = rootView.findViewById(R.id.ll_refreshing);
        mRefreshingIcon = rootView.findViewById(R.id.refreshing_icon);
        ImageView mLiveVedioIcon = rootView.findViewById(R.id.live_vedioIcon);
        mLiveVedioIcon.setImageResource(BitmapUtil.getUserPhotoRound());
        ImageView ivLiveSpeakingHead = rootView.findViewById(R.id.iv_live_speaking_head);
        ivLiveSpeakingHead.setImageResource(BitmapUtil.getUserPhotoRound());
        dismissLoadingView(mLlRefreshing,mRefreshingIcon);
        llNoNetwork.setVisibility(NetworkUtil.isConnected(MyApplication.getApplication())?View.GONE:View.VISIBLE);
    }

    @Override
    protected void initData(){
    }

    @Override
    protected void initBroadCastReceiver(){
        IntentFilter mReceivFilter = new IntentFilter();
        mReceivFilter.addAction(KILL_LIVE_WATCH);
        registerReceiver(mBroadcastReceiv, mReceivFilter);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNetworkChangeHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receivePTTUpHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receivePTTDownHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyLivingStoppedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMemberNotLivingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetRtspStreamUrlHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveRequestGroupCallConformationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveCeaseGroupCallConformationHander);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyMemberStopWatchMessageHandler);
//        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseWatchLiveAndTempGroupMessageHandler);
        unregisterReceiver(mBroadcastReceiv);
    }

    private ReceiveNetworkChangeHandler receiveNetworkChangeHandler = new ReceiveNetworkChangeHandler(){
        @Override
        public void handler(boolean connected){
            mHandler.post(() -> {
                if(llNoNetwork!=null) {
                    llNoNetwork.setVisibility((!connected) ? View.VISIBLE : View.GONE);
                }
            });
            if(connected){
                if(MyApplication.instance.isMiniLive){
                if(null != mSvLivePop.getSurfaceTexture()){
                    startPull(mSvLivePop);
                }
            }else {
                if(null != mSvLive.getSurfaceTexture()){
                    startPull(mSvLive);
                }
            }
            }
        }
    };
    /**
     * 主动方请求组呼的消息
     */
    private ReceiveRequestGroupCallConformationHandler receiveRequestGroupCallConformationHandler = (methodResult, resultDesc,groupId) -> mHandler.post(() -> {
        if(methodResult == BaseCommonCode.SUCCESS_CODE){
            mLlLiveGroupCall.setVisibility(View.GONE);
            mBtnLiveLookPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_speaking);
            if (!MyTerminalFactory.getSDK().getAudioProxy().isSpeakerphoneOn()) {
                MyTerminalFactory.getSDK().getAudioProxy().setSpeakerphoneOn(true);
            }
        }else if (methodResult == SignalServerErrorCode.CANT_SPEAK_IN_GROUP.getErrorCode()) {//只听组
            ToastUtil.showToast(MyTerminalFactory.getSDK().application, "当前组是只听组，不能发起组呼");
        } else if (methodResult == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode()) {//请求等待中
            mBtnLiveLookPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_yellow);
        } else {//请求失败
            if (MyApplication.instance.getGroupListenenState() != LISTENING) {
                mBtnLiveLookPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_dodgerblue2);
            } else {
                mBtnLiveLookPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_gray);
            }
        }
    });
    /**
     * 主动方停止组呼
     */

    private ReceiveCeaseGroupCallConformationHander receiveCeaseGroupCallConformationHander = new ReceiveCeaseGroupCallConformationHander() {
        @Override
        public void handler(int resultCode, String resultDesc) {
//            MyTerminalFactory.getSDK().getAudioProxy().volumeCancelQuiet();
            mHandler.post(() -> {
                mBtnLiveLookPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_dodgerblue2);
            });
        }
    };

    private ReceiveGroupCallIncommingHandler receiveGroupCallIncommingHandler = (memberId, memberName, groupId, groupName, currentCallMode,uniqueNo) -> {
        logger.info("PullLivingService---ReceiveGroupCallIncommingHandler");
        if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_LISTEN.name())){
            ptt.terminalsdk.tools.ToastUtil.showToast(MyTerminalFactory.getSDK().application,getString(R.string.text_has_no_group_call_listener_authority));
        }else{
            mHandler.post(() -> {
                mLlLiveGroupCall.setVisibility(View.VISIBLE);
                mTvLiveGroupName.setText(DataUtil.getGroupName(groupId));
                mTvLiveSpeakingName.setText(memberName);
                mTvLiveSpeakingId.setText(HandleIdUtil.handleId(memberId));
            });
        }
    };

    private ReceiveGroupCallCeasedIndicationHandler receiveGroupCallCeasedIndicationHandler = (reasonCode) -> {
        logger.info("收到组呼停止");
        mHandler.post(() -> mLlLiveGroupCall.setVisibility(View.GONE));
    };

    private ReceiveUpdateConfigHandler mReceiveUpdateConfigHandler= () -> mHandler.post(this::setAuthorityView);

    private ReceivePTTUpHandler receivePTTUpHandler = ()-> {
        if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_TALK.name())){
            return;
        }
        mBtnLiveLookPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_dodgerblue2);
    };


    private ReceivePTTDownHandler receivePTTDownHandler = (requestGroupCall)->{
        if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_TALK.name())){
            return;
        }
        if (requestGroupCall == BaseCommonCode.SUCCESS_CODE){
            if (!CheckMyPermission.selfPermissionGranted(PullLivingService.this, Manifest.permission.RECORD_AUDIO)) {//没有录音权限
                // CheckMyPermission.permissionPrompt(IndividualCallService.this, Manifest.permission.RECORD_AUDIO);
                return;
            }
            if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_TALK.name())){
                return;
            }
            mBtnLiveLookPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_yellow);
        }else if (requestGroupCall == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode()) {
            mBtnLiveLookPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_yellow);
        } else {//组呼失败的提示
            ToastUtil.groupCallFailToast(PullLivingService.this, requestGroupCall);
        }
    };

    /**
     * 获取到流地址
     */
    private ReceiveGetRtspStreamUrlHandler receiveGetRtspStreamUrlHandler = (final String rtspUrl, final Member liveMember, long callId)-> mHandler.post(() -> {
        PullLivingService.this.callId = callId;
        if (Util.isEmpty(rtspUrl)) {
            ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.no_rtsp_data));
            stopBusiness();
        }else {
            logger.info("rtspUrl ----> " + rtspUrl);
            PullLivingService.this.liveMember = liveMember;
            showPullView();
            PullLivingService.this.url = rtspUrl;
            if (mSvLive.getSurfaceTexture() != null){
                startPull(mSvLive);
            }
        }
    });

    /**
     * 去观看时，发现没有在直播，关闭界面吧
     */
    private ReceiveMemberNotLivingHandler receiveMemberNotLivingHandler = callId -> {
        ToastUtil.showToast(MyTerminalFactory.getSDK().application, getResources().getString(R.string.push_stoped));
        stopBusiness();
    };

    /**
     * 通知直播停止 通知界面关闭视频页
     **/
    private ReceiveNotifyLivingStoppedHandler receiveNotifyLivingStoppedHandler = (liveMemberId, callId, methodResult, resultDesc) -> {
        ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.push_stoped));
        mHandler.post(this::finishVideoLive);
    };

    /**
     * 通知终端停止观看直播
     **/
    private ReceiveNotifyMemberStopWatchMessageHandler receiveNotifyMemberStopWatchMessageHandler = message -> {
        if(callId!=0&&message.getCallId() == callId){
            ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.force_stop_watch));
            mHandler.post(this::finishVideoLive);
        }
    };

//    /**
//     * 通知观看直播获取临时组id
//     **/
//    private ReceiveResponseWatchLiveAndTempGroupMessageHandler receiveResponseWatchLiveAndTempGroupMessageHandler = (callId,liveMemberId,liveMemberUniqueNo,tempGroupId,uniqueNo) -> {
//        mHandler.post(() -> {
//            if(PullLivingService.this.callId == callId){
//                PullLivingService.this.tempGroupId = tempGroupId;
//            }
//        });
//    };


    /**
     * 停止观看
     */
    private BroadcastReceiver mBroadcastReceiv = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent){
            String action = intent.getAction();
            if(null == action){
                return;
            }
            if(KILL_LIVE_WATCH.equals(intent.getAction())){
                stopBusiness();
            }
        }
    };




    private IGotaKeyHandler.Stub gotaKeHandler = new IGotaKeyHandler.Stub(){
        @Override
        public void onPTTKeyDown(){
            try{
                pttDownDoThing();
            }catch(Exception e){
                logger.error(e);
            }
        }

        @Override
        public void onPTTKeyUp(){
            try{
                pttUpDoThing();
            }catch(Exception e){
                logger.error(e);
            }
        }

        @Override
        public void onSOSKeyDown(){
        }

        @Override
        public void onSOSKeyUp(){
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
                    mRlPullLive.setVisibility(View.VISIBLE);
                    mPopupMiniLive.setVisibility(View.GONE);
                    MyApplication.instance.isMiniLive = false;
                }
                break;
        }
        return true;
    };

    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener(){
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height){
            logger.info("onSurfaceTextureAvailable--"+surface+"-----"+url);
            if(TextUtils.isEmpty(url)){
                return;
            }
            if(MyApplication.instance.isMiniLive){
                startPull(mSvLivePop);
            }else{
                startPull(mSvLive);
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height){
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface){
            stopPull();
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface){
        }
    };

    private View.OnClickListener hangUpOnClickListener = v-> finishVideoLive();

    private void finishVideoLive(){
        stopPull();
        stopBusiness();
    }

    private View.OnClickListener retractOnClickListener = v -> showPopMiniView();

    private View.OnClickListener inviteMemberOnClickListener = v -> {
        if (MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_PUSH.name())) {
            if(liveMember!=null){
               Intent intent = new Intent(PullLivingService.this, InviteMemberService.class);
               intent.putExtra(Constants.TYPE, Constants.PULL);
               intent.putExtra(Constants.PULLING, true);
               intent.putExtra(Constants.LIVE_MEMBER,new InviteMemberLiverMember(liveMember.getNo(),liveMember.getUniqueNo()));
               List<Integer> list = new ArrayList<>();
               list.add((Integer)liveMember.getNo());
               intent.putExtra(Constants.INVITE_MEMBER_EXCEPT_UNIQUE_NO,new InviteMemberExceptList(list));
               startService(intent);
            }
        }else{
            ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.text_no_video_push_authority));
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private View.OnTouchListener pttOnTouchListener = (v, event) -> {
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(!MyApplication.instance.folatWindowPress && !MyApplication.instance.volumePress){
                    pttDownDoThing();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(event.getX() + v.getWidth() / 4 < 0 || event.getX() - v.getWidth() * 1.25 > 0 || event.getY() + v.getHeight() / 8 < 0 || event.getY() - v.getHeight() * 1.125 > 0){
                    if(MyApplication.instance.isPttPress){
                        pttUpDoThing();
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if(MyApplication.instance.isPttPress){
                    pttUpDoThing();
                }
                break;
            default:
                break;
        }
        return true;
    };


    private void startPull(TextureView surface){
        logger.info("开始播放");
        if(!rtspPlay){
            //rtmp播放
            //测试url
            url = "rtmp://202.69.69.180:443/webcast/bshdlive-pc";
            if(null != surface.getSurfaceTexture()){
                rtmpClient = new RTMPEasyPlayerClient(this, MyTerminalFactory.getSDK().getLiveConfigManager().getRTMPPlayKey(), surface, null, null);
                rtmpClient.play(url);
            }
        }else{
            //rtsp 播放
            if(null == mResultReceiver){
                mResultReceiver = new RtspReceiver(new Handler());
            }
//            url = "rtsp://192.168.1.230:554/main";
            if(null != surface.getSurfaceTexture()){
                mStreamRender = new EasyRTSPClient(this, MyTerminalFactory.getSDK().getLiveConfigManager().getPlayKey(), new Surface(surface.getSurfaceTexture()), mResultReceiver);
                try{
                    if(!android.text.TextUtils.isEmpty(url)){
                        mStreamRender.start(url, RTSPClient.TRANSTYPE_TCP, RTSPClient.EASY_SDK_VIDEO_FRAME_FLAG | RTSPClient.EASY_SDK_AUDIO_FRAME_FLAG, "", "", null);
                    }
                }catch(Exception e){
                    e.printStackTrace();
                    logger.error("PullLivingService :" + e.toString());
                }
            }
        }
    }

    private RtspReceiver mResultReceiver;

    private class RtspReceiver extends ResultReceiver{

        private int pullcount;

        private RtspReceiver(Handler handler){
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData){
            super.onReceiveResult(resultCode, resultData);
            if(resultCode == EasyRTSPClient.RESULT_VIDEO_DISPLAYED){
                pullcount = 0;
                mHandler.post(() -> dismissLoadingView(mLlRefreshing,mRefreshingIcon));
            }else if(resultCode == EasyRTSPClient.RESULT_VIDEO_SIZE){
                mLiveWidth = resultData.getInt(EasyRTSPClient.EXTRA_VIDEO_WIDTH);
                mLiveHeight = resultData.getInt(EasyRTSPClient.EXTRA_VIDEO_HEIGHT);
                onVideoSizeChange();
            }else if(resultCode == EasyRTSPClient.RESULT_TIMEOUT){
                ToastUtil.showToast(MyTerminalFactory.getSDK().application, getResources().getString(R.string.time_up));
            }else if(resultCode == EasyRTSPClient.RESULT_UNSUPPORTED_AUDIO){
                ToastUtil.showToast(MyTerminalFactory.getSDK().application, getResources().getString(R.string.voice_not_support));
            }else if(resultCode == EasyRTSPClient.RESULT_UNSUPPORTED_VIDEO){
                ToastUtil.showToast(MyTerminalFactory.getSDK().application, getResources().getString(R.string.video_not_support));
            }else if(resultCode == EasyRTSPClient.RESULT_EVENT){
                mHandler.post(() -> showLoadingView(mLlRefreshing,mRefreshingIcon));
                int errorcode = resultData.getInt("errorcode");
                String resultDataString = resultData.getString("event-msg");
                logger.error("视频流播放状态：" + errorcode + "=========" + resultDataString + "-----count:" + pullcount);
                if(errorcode != 0){
                    stopPull();
                }
                if(errorcode == 500 || errorcode == 404 || errorcode == -32 || errorcode == -101){
                    if(pullcount < 10){
                        try{
                            Thread.sleep(300);
                            logger.error("请求第" + pullcount + "次");
                            if(mSvLive != null && mSvLive.getVisibility() == View.VISIBLE && mSvLive.getSurfaceTexture() != null){
                                startPull(mSvLive);
                                pullcount++;
                            }else if(mSvLivePop != null && mSvLivePop.getVisibility() == View.VISIBLE && mSvLivePop.getSurfaceTexture() != null){
                                startPull(mSvLivePop);
                                pullcount++;
                            }else{
                                ToastUtil.showToast(MyTerminalFactory.getSDK().application, getResources().getString(R.string.push_stoped));
                                mHandler.post(() -> dismissLoadingView(mLlRefreshing,mRefreshingIcon));
                                stopBusiness();
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }else{
                        ToastUtil.showToast(MyTerminalFactory.getSDK().application, getResources().getString(R.string.push_stoped));
                        mHandler.post(() -> dismissLoadingView(mLlRefreshing,mRefreshingIcon));
                        stopBusiness();
                    }
                }else if(errorcode != 0){
                    ToastUtil.showToast(MyTerminalFactory.getSDK().application, resultDataString);
                    mHandler.post(() -> dismissLoadingView(mLlRefreshing,mRefreshingIcon));
                    stopBusiness();
                }
            }
        }
    }

    private void stopPull(){
        if(rtspPlay){
            if(mStreamRender != null){
                mStreamRender.stop();
                mStreamRender = null;
            }
        }else{
            if(null != rtmpClient){
                rtmpClient.stop();
                rtmpClient = null;
            }
        }
    }

    private void pttDownDoThing(){
        logger.info("pttDownDoThing执行了 isPttPress：" + MyApplication.instance.isPttPress);
        if(!CheckMyPermission.selfPermissionGranted(this, Manifest.permission.RECORD_AUDIO)){//没有录音权限
            //            CheckMyPermission.permissionPrompt(IndividualCallService.this, Manifest.permission.RECORD_AUDIO);
            return;
        }
        //没有组呼权限
        if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_TALK.name())){
            ToastUtil.showToast(this,getString(R.string.no_group_call_permission));
            return;
        }
        // FIXME: 2019/4/8 观看视频上报时发起组呼，是在临时组里
        int tempGroupId = MyTerminalFactory.getSDK().getLiveManager().getTempGroupId();
        if(tempGroupId!=0){
            int resultCode = MyTerminalFactory.getSDK().getGroupCallManager().requestGroupCall("",tempGroupId);
            if(resultCode == BaseCommonCode.SUCCESS_CODE){//允许组呼了
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCallingCannotClickHandler.class, true);
                MyApplication.instance.isPttPress = true;
                mBtnLiveLookPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_yellow);
            }else if(resultCode == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode()){
                mBtnLiveLookPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_yellow);
            }else{//组呼失败的提示
                ToastUtil.groupCallFailToast(this, resultCode);
            }
            setViewEnable(false);
        }else{
            logger.error(getString(R.string.no_get_temporary_group_id));
        }

    }

    private void pttUpDoThing(){
        if(MyApplication.instance.isPttPress){
            MyApplication.instance.isPttPress = false;

            //没有组呼权限
            if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_TALK.name())){
                return;
            }
            if(MyApplication.instance.getGroupListenenState() == LISTENING){
                mBtnLiveLookPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_gray);
            }else{
                mBtnLiveLookPtt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_dodgerblue2);
            }
            MyTerminalFactory.getSDK().getGroupCallManager().ceaseGroupCall();
            //            MyTerminalFactory.getSDK().getAudioProxy().volumeCancelQuiet();
        }
        setViewEnable(true);
        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCallingCannotClickHandler.class, false);
    }

    private void setViewEnable(boolean isEnable){
        mLlLiveLookInviteMember.setEnabled(isEnable);
        mLlLiveLookHangup.setEnabled(isEnable);
        mIvLiveRetract.setEnabled(isEnable);
    }

    private void requestToWatchLiving(TerminalMessage terminalMessage){
        PTTProtolbuf.NotifyDataMessage.Builder builder = PTTProtolbuf.NotifyDataMessage.newBuilder();
        builder.setMessageUrl(TextUtils.isEmpty(terminalMessage.messageUrl)?"":terminalMessage.messageUrl);
        builder.setMessageFromName(terminalMessage.messageFromName);
        builder.setMessageFromNo(terminalMessage.messageFromId);
        builder.setMessageFromUniqueNo(terminalMessage.messageFromUniqueNo);
        builder.setMessageToName(terminalMessage.messageToName);
        builder.setMessageToNo(terminalMessage.messageToId);
        builder.setMessageToUniqueNo(terminalMessage.messageToUniqueNo);
        builder.setMessageType(terminalMessage.messageType);
        builder.setMessageVersion(terminalMessage.messageVersion);
        builder.setResultCode(terminalMessage.resultCode);
        builder.setSendingTime(terminalMessage.sendTime);
        builder.setMessageBody(terminalMessage.messageBody.toString());
        PTTProtolbuf.NotifyDataMessage message = builder.build();
        int resultCode = MyTerminalFactory.getSDK().getLiveManager().requestToWatchLiving(message);
        if(resultCode == 0){
            //                        live_theme = terminalMessage1.messageFromName + "上报图像";
            if(TextUtils.isEmpty(terminalMessage.messageBody.getString(JsonParam.TITLE))){
                String liver = (String) terminalMessage.messageBody.get("liver");
                if(!TextUtils.isEmpty(liver)){
                    if(liver.contains("_")){
                        String[] split = liver.split("_");
                        if(split.length > 0){
                            theme = String.format(getString(R.string.text_pushing_video_name), split[1]);
                        }
                    }
                }
            }else{
                theme = terminalMessage.messageBody.getString(JsonParam.TITLE);
            }
            mLiveVedioTheme.setText(theme);
        }else{
            ToastUtil.livingFailToast(this, resultCode, TerminalErrorCode.LIVING_PLAYING.getErrorCode());
        }
    }

    private void showPullView(){
        mLiveVedioTheme.setText(theme);
        mLiveVedioName.setText(liveMember.getName());
        mLiveVedioId.setText((liveMember.getNo()==0)?"":HandleIdUtil.handleId(liveMember.getNo()));
        if(MyApplication.instance.getGroupListenenState() == GroupCallListenState.LISTENING){
            if(null != MyApplication.instance.groupCallMember){
                mLlLiveGroupCall.setVisibility(View.VISIBLE);
                mTvLiveSpeakingName.setText(MyApplication.instance.groupCallMember.getName());
                mTvLiveSpeakingId.setText(HandleIdUtil.handleId(MyApplication.instance.groupCallMember.getId()));
            }
            if(MyApplication.instance.currentCallGroupId !=-1){
                mTvLiveGroupName.setText(DataUtil.getGroupName(MyApplication.instance.currentCallGroupId));
            }
        }
    }
    private void onVideoSizeChange(){
        if(mLiveWidth == 0 || mLiveHeight == 0){
            return;
        }
        mSvLive.setTransform(new Matrix());
        float ratioView = mRlLiveGeneralView.getWidth() * 1.0f / mRlLiveGeneralView.getHeight();
        float ratio = mLiveWidth * 1.0f / mLiveHeight;
        // 屏幕比视频的宽高比更小.表示视频是过于宽屏了.
        if(ratioView - ratio < 0){
            // 宽为基准.
            mSvLive.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            mSvLive.getLayoutParams().height = (int) (mRlLiveGeneralView.getWidth() / ratio + 0.5f);
        }
        // 视频是竖屏了.
        else{
            mSvLive.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            mSvLive.getLayoutParams().width = (int) (mRlLiveGeneralView.getHeight() * ratio + 0.5f);
        }
        mSvLive.requestLayout();
    }
}

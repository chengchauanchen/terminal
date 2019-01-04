package cn.vsx.vc.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.IGotaKeyHandler;
import android.app.IGotaKeyMonitor;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import org.apache.http.util.TextUtils;
import org.easydarwin.video.EasyRTSPClient;
import org.easydarwin.video.RTMPEasyPlayerClient;
import org.easydarwin.video.RTSPClient;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.SignalServerErrorCode;
import cn.vsx.hamster.errcode.module.TerminalErrorCode;
import cn.vsx.hamster.protolbuf.PTTProtolbuf;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCallingCannotClickHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetRtspStreamUrlHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberNotLivingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyLivingStoppedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivePTTDownHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivePTTUpHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveServerConnectionEstablishedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.hamster.terminalsdk.tools.Util;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.receiveHandle.ReceiverCloseKeyBoardHandler;
import cn.vsx.vc.receiveHandle.ReceiverRemoveWindowViewHandler;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.HandleIdUtil;
import cn.vsx.vc.utils.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.audio.CheckMyPermission;

import static cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState.LISTENING;
import static cn.vsx.vc.pager.PTTViewPager.myHandler;
import static org.easydarwin.config.Config.PLAYKEY;
import static org.easydarwin.config.Config.RTMPPLAYKEY;

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
    private Member liveMember;

    public PullLivingService(){}

    @SuppressLint({"WrongConstant", "ClickableViewAccessibility"})
    protected void initListener(){
        MyTerminalFactory.getSDK().registReceiveHandler(receivePTTUpHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receivePTTDownHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGetRtspStreamUrlHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveMemberNotLivingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyLivingStoppedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveServerConnectionEstablishedHandler);
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
            theme = String.format("%s正在上报视频",liveMember.getName());
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
            mLlLiveLookInviteMember.setVisibility(View.VISIBLE);
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
    }

    @SuppressLint("InflateParams")
    @Override
    protected void setRootView(){
        rootView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_pull_stream, null);
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
        //        mLiveVedioIcon = rootView.findViewById(R.id.live_vedioIcon);
        mLiveVedioName = rootView.findViewById(R.id.live_vedioName);
        mLiveVedioId = rootView.findViewById(R.id.live_vedioId);
        mLlLiveLookHangup = rootView.findViewById(R.id.ll_live_look_hangup);
        mLlLiveLookInviteMember = rootView.findViewById(R.id.ll_live_look_invite_member);
        mBtnLiveLookPtt = rootView.findViewById(R.id.btn_live_look_ptt);
    }

    @Override
    protected void initData(){
    }

    @Override
    protected void initBroadCastReceiver(){
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        MyTerminalFactory.getSDK().unregistReceiveHandler(receivePTTUpHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receivePTTDownHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyLivingStoppedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMemberNotLivingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetRtspStreamUrlHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveServerConnectionEstablishedHandler);
    }

    private ReceiveUpdateConfigHandler mReceiveUpdateConfigHandler= () -> myHandler.post(this::setAuthorityView);

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

    private ReceiveGetRtspStreamUrlHandler receiveGetRtspStreamUrlHandler = (final String rtspUrl, final Member liveMember, long callId)-> mHandler.post(() -> {
        if (Util.isEmpty(rtspUrl)) {
            ToastUtil.showToast(getApplicationContext(),getResources().getString(R.string.no_rtsp_data));
            finishVideoLive();
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
        ToastUtil.showToast(getApplicationContext(), getResources().getString(R.string.push_stoped));
        TerminalFactory.getSDK().getLiveManager().ceaseWatching();
        finishVideoLive();
    };

    /**
     * 通知直播停止 通知界面关闭视频页
     **/
    private ReceiveNotifyLivingStoppedHandler receiveNotifyLivingStoppedHandler = (methodResult, resultDesc) -> {
        ToastUtil.showToast(getApplicationContext(),getResources().getString(R.string.push_stoped));
        mHandler.post(this::finishVideoLive);
    };

    private ReceiveServerConnectionEstablishedHandler receiveServerConnectionEstablishedHandler = connected -> {
        if(!connected){
            return;
        }
        mHandler.post(() -> {
            if(MyApplication.instance.isMiniLive){
                if(null != mSvLivePop.getSurfaceTexture()){
                    startPull(mSvLivePop);
                }
            }else {
                if(null != mSvLive.getSurfaceTexture()){
                    startPull(mSvLive);
                }
            }
        });
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
                    mRlPullLive.setVisibility(View.GONE);
                    mPopupMiniLive.setVisibility(View.VISIBLE);
                    MyApplication.instance.isMiniLive = false;
                }
                break;
        }
        return true;
    };

    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener(){
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height){
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
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface){
        }
    };

    private View.OnClickListener hangUpOnClickListener = v-> finishVideoLive();

    private View.OnClickListener retractOnClickListener = v -> showPopMiniView();

    private View.OnClickListener inviteMemberOnClickListener = v -> {
        Intent intent = new Intent(PullLivingService.this, InviteMemberService.class);
        intent.putExtra(Constants.TYPE, Constants.PULL);
        intent.putExtra(Constants.PULLING, true);
        intent.putExtra(Constants.LIVING_MEMBER_ID,liveMember.getNo());
        startService(intent);
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
            rtmpClient = new RTMPEasyPlayerClient(this, RTMPPLAYKEY, surface, null, null);
            rtmpClient.play(url);
        }else{
            //rtsp 播放
            if(null == mResultReceiver){
                mResultReceiver = new RtspReceiver(new Handler());
            }
            mStreamRender = new EasyRTSPClient(this, PLAYKEY, surface.getSurfaceTexture(), mResultReceiver);
            try{
                if(url != null){
                    mStreamRender.start(url, RTSPClient.TRANSTYPE_TCP, RTSPClient.EASY_SDK_VIDEO_FRAME_FLAG | RTSPClient.EASY_SDK_AUDIO_FRAME_FLAG, "", "", null);
                }
            }catch(Exception e){
                e.printStackTrace();
                logger.error("IndividualCallService :" + e.toString());
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
            }else if(resultCode == EasyRTSPClient.RESULT_VIDEO_SIZE){
                mLiveWidth = resultData.getInt(EasyRTSPClient.EXTRA_VIDEO_WIDTH);
                mLiveHeight = resultData.getInt(EasyRTSPClient.EXTRA_VIDEO_HEIGHT);
                onVideoSizeChange();
            }else if(resultCode == EasyRTSPClient.RESULT_TIMEOUT){
                ToastUtil.showToast(getApplicationContext(), getResources().getString(R.string.time_up));
            }else if(resultCode == EasyRTSPClient.RESULT_UNSUPPORTED_AUDIO){
                ToastUtil.showToast(getApplicationContext(), getResources().getString(R.string.voice_not_support));
            }else if(resultCode == EasyRTSPClient.RESULT_UNSUPPORTED_VIDEO){
                ToastUtil.showToast(getApplicationContext(), getResources().getString(R.string.video_not_support));
            }else if(resultCode == EasyRTSPClient.RESULT_EVENT){
                int errorcode = resultData.getInt("errorcode");
                String resultDataString = resultData.getString("event-msg");
                logger.error("视频流播放状态：" + errorcode + "=========" + resultDataString + "-----count:" + pullcount);
                if(errorcode != 0){
                    stopPull();
                }
                if(errorcode == -101){
                    ToastUtil.showToast(getApplicationContext(), getResources().getString(R.string.please_check_network));
                    TerminalFactory.getSDK().getLiveManager().ceaseWatching();
                    finishVideoLive();
                }
                if(errorcode == 500 || errorcode == 404 || errorcode == -32){
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
                                TerminalFactory.getSDK().getLiveManager().ceaseWatching();
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }else{
                        ToastUtil.showToast(getApplicationContext(), getResources().getString(R.string.push_stoped));
                        TerminalFactory.getSDK().getLiveManager().ceaseWatching();
                        finishVideoLive();
                    }
                }else if(errorcode != 0){
                    ToastUtil.showToast(getApplicationContext(), resultDataString);
                    finishVideoLive();
                }
            }
        }
    }

    private void finishVideoLive(){
        MyTerminalFactory.getSDK().getLiveManager().ceaseWatching();
        windowManager.removeView(rootView);
        MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiverRemoveWindowViewHandler.class);
        stopSelf();
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
        TerminalFactory.getSDK().getLiveManager().ceaseLiving();
    }

    private void pttDownDoThing(){
        logger.info("pttDownDoThing执行了 isPttPress：" + MyApplication.instance.isPttPress);
        if(!CheckMyPermission.selfPermissionGranted(this, Manifest.permission.RECORD_AUDIO)){//没有录音权限
            //            CheckMyPermission.permissionPrompt(IndividualCallService.this, Manifest.permission.RECORD_AUDIO);
            return;
        }
        //没有组呼权限
        if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_TALK.name())){
            Toast.makeText(this, "没有组呼权限", Toast.LENGTH_SHORT).show();
            return;
        }
        int resultCode = MyTerminalFactory.getSDK().getGroupCallManager().requestGroupCall("");
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
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCallingCannotClickHandler.class, false);
        }
        setViewEnable(true);
    }

    private void setViewEnable(boolean isEnable){
        mLlLiveLookInviteMember.setEnabled(isEnable);
        mLlLiveLookHangup.setEnabled(isEnable);
        mIvLiveRetract.setEnabled(isEnable);
    }

    private void requestToWatchLiving(TerminalMessage terminalMessage){
        PTTProtolbuf.NotifyDataMessage.Builder builder = PTTProtolbuf.NotifyDataMessage.newBuilder();
        builder.setMessageUrl(terminalMessage.messageUrl);
        builder.setMessageFromName(terminalMessage.messageFromName);
        builder.setMessageFromNo(terminalMessage.messageFromId);
        builder.setMessageToName(terminalMessage.messageToName);
        builder.setMessageToNo(terminalMessage.messageToId);
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
                            theme = String.format("%s正在上报图像", split[1]);
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
        mLiveVedioId.setText(HandleIdUtil.handleId(liveMember.getNo()));
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

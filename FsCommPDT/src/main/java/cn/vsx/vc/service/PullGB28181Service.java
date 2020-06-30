package cn.vsx.vc.service;

import android.annotation.SuppressLint;
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
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apache.http.util.TextUtils;
import org.apache.log4j.Logger;
import org.easydarwin.video.EasyRTSPClient;
import org.easydarwin.video.RTSPClient;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNetworkChangeHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyMemberStopWatchMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.NetworkUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

import static cn.vsx.vc.receive.Actions.KILL_LIVE_WATCH;

public class PullGB28181Service extends BaseService{

    private RelativeLayout mReVideoRoot;
    private TextureView mSvGb28181;
    private TextView mTvGb28181Time;
    private TextView mTvDeviceName;
    private ImageView mIvClose;
    private LinearLayout mLlInviteMember;
    private ImageView mIvLiveLookAddmember;
    private RelativeLayout mVideoPlatform;
    protected LinearLayout mLlRefreshing;
    protected ImageView mRefreshingIcon;
    private String gb28181Url;
    private Logger logger = Logger.getLogger(this.getClass());
    private static final int CURRENTTIME = 1;
    private static final int HIDELIVINGVIEW = 2;
    private static final int SHOW_LOADING_VIEW = 3;
    private static final int HIDE_LOADING_VIEW = 4;
    private static final int HIDE_LOADING_VIEW_TIME = 2*1000;


    private EasyRTSPClient mStreamRender;
    private TerminalMessage terminalMessage;
    protected LinearLayout llNoNetwork;

    public PullGB28181Service(){}


    @SuppressLint("InflateParams")
    @Override
    protected void setRootView(){
        rootView = LayoutInflater.from(MyTerminalFactory.getSDK().application).inflate(R.layout.layout_gb28181_view, null);
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
        mVideoPlatform = rootView.findViewById(R.id.video_platform);
        mReVideoRoot =  rootView.findViewById(R.id.re_video_root);
        mSvGb28181 =  rootView.findViewById(R.id.sv_gb28181);
        mTvGb28181Time =  rootView.findViewById(R.id.tv_gb28181_time);
        mTvDeviceName =  rootView.findViewById(R.id.tv_device_name);
        mIvClose =  rootView.findViewById(R.id.iv_close);
        mLlInviteMember =  rootView.findViewById(R.id.ll_invite_member);
        mIvLiveLookAddmember =  rootView.findViewById(R.id.iv_live_look_addmember);
        llNoNetwork = rootView.findViewById(R.id.ll_no_network);
        mLlRefreshing = rootView.findViewById(R.id.ll_refreshing);
        mRefreshingIcon = rootView.findViewById(R.id.refreshing_icon);
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
    protected void initListener(){
        mIvLiveLookAddmember.setOnClickListener(inviteMemberOnClickListener);
        mSvGb28181.setSurfaceTextureListener(GB28181SurfaceTextureListener);
        mSvGb28181.setOnClickListener(svOnClickListener);
        mIvClose.setOnClickListener(closeOnClickListener);
        mLlInviteMember.setOnClickListener(inviteOnClickListener);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNetworkChangeHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyMemberStopWatchMessageHandler);
    }

    @Override
    protected void initView(Intent intent){
        terminalMessage = (TerminalMessage) intent.getSerializableExtra(Constants.TERMINALMESSAGE);
        logger.info("terminalMessage:"+terminalMessage);
        mHandler.removeMessages(HIDELIVINGVIEW);
        mHandler.sendEmptyMessageDelayed(HIDELIVINGVIEW, 5000);
        if(terminalMessage.messageBody.containsKey(JsonParam.GB28181_RTSP_URL)){
            setPushAuthority();
            //gb28181Url = terminalMessage.messageBody.getString(JsonParam.GB28181_RTSP_URL);
            String deviceId = terminalMessage.messageBody.getString(JsonParam.GB28181_RTSP_URL);
            String gateWayUrl = TerminalFactory.getSDK().getParam(Params.GATE_WAY_URL);
            gb28181Url = gateWayUrl + "DevAor=" + deviceId;
            logger.info("播放地址："+ gb28181Url);
            mVideoPlatform.setVisibility(View.VISIBLE);
        }
        if(terminalMessage.messageBody.containsKey(JsonParam.DEVICE_NAME)){
            String deviceName = terminalMessage.messageBody.getString(JsonParam.DEVICE_NAME);
            mTvDeviceName.setText(deviceName+"正在上报图像");
        }
    }

    @Override
    protected void showPopMiniView(){

    }

    @Override
    protected void handleMesage(Message msg){
        switch(msg.what){
            case CURRENTTIME:
                setCurrentTime();
                break;
//            case OFF_LINE:
//                ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.exit_pull));
//                stopBusiness();
//                break;
            case HIDELIVINGVIEW:
                mHandler.removeMessages(HIDELIVINGVIEW);
                hideLivingView();
                break;
            case SHOW_LOADING_VIEW:
                mHandler.removeMessages(SHOW_LOADING_VIEW);
                showLoadingView(mLlRefreshing,mRefreshingIcon);
                break;
            case HIDE_LOADING_VIEW:
                mHandler.removeMessages(HIDE_LOADING_VIEW);
                dismissLoadingView(mLlRefreshing,mRefreshingIcon);
                break;
                default:break;
        }
    }

    @Override
    protected void onNetworkChanged(boolean connected){
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNetworkChangeHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyMemberStopWatchMessageHandler);
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
                if(null != mSvGb28181.getSurfaceTexture()){
                startPullGB28121(mSvGb28181.getSurfaceTexture());
                }
            }
        }
    };

    private ReceiveUpdateConfigHandler mReceiveUpdateConfigHandler= () -> mHandler.post(this::setPushAuthority);

    /**
     * 通知终端停止观看直播
     **/
    private ReceiveNotifyMemberStopWatchMessageHandler receiveNotifyMemberStopWatchMessageHandler = message -> {
        ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.force_stop_watch));
        mHandler.post(this::stopBusiness);
    };
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

    private View.OnClickListener inviteOnClickListener = v->{
        if (MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_PUSH.name())) {
            Intent intent = new Intent(PullGB28181Service.this,InviteMemberService.class);
            intent.putExtra(Constants.TYPE,Constants.PULL);
            intent.putExtra(Constants.PULLING,true);
            intent.putExtra(Constants.GB28181_PULL,true);
            intent.putExtra(Constants.TERMINALMESSAGE,terminalMessage);
            startService(intent);
        }else{
            ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.text_no_video_push_authority));
        }
    };

    private View.OnClickListener inviteMemberOnClickListener = v -> {
        if (MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_PUSH.name())) {
            Intent intent = new Intent(PullGB28181Service.this,InviteMemberService.class);
            intent.putExtra(Constants.TYPE,Constants.PULL);
            intent.putExtra(Constants.PULLING,true);
            intent.putExtra(Constants.GB28181_PULL,true);
            intent.putExtra(Constants.TERMINALMESSAGE,terminalMessage);
            startService(intent);
        }else{
            ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.text_no_video_push_authority));
        }
    };

    private View.OnClickListener svOnClickListener = v->{
        showLivingView();
        mHandler.removeMessages(HIDELIVINGVIEW);
        mHandler.sendEmptyMessageDelayed(HIDELIVINGVIEW, 5000);
    };

    private View.OnClickListener closeOnClickListener = v -> {
        ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.watch_finished));
        stopBusiness();
    };

    private TextureView.SurfaceTextureListener GB28181SurfaceTextureListener  = new TextureView.SurfaceTextureListener(){
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height){
            startPullGB28121(surface);
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

    private void stopPull(){
        mHandler.removeMessages(CURRENTTIME);
        if (mStreamRender != null) {
            mStreamRender.stop();
            mStreamRender = null;
        }
    }

    private void startPullGB28121(SurfaceTexture surface){
        mHandler.sendEmptyMessage(CURRENTTIME);
        if(!TextUtils.isEmpty(gb28181Url)){

            mStreamRender = new EasyRTSPClient(PullGB28181Service.this, MyTerminalFactory.getSDK().getLiveConfigManager().getPlayKey(),
                    new Surface(surface), mResultReceiver);
            try {
                if (gb28181Url != null) {
                    mStreamRender.start(gb28181Url, RTSPClient.TRANSTYPE_TCP, RTSPClient.EASY_SDK_VIDEO_FRAME_FLAG | RTSPClient.EASY_SDK_AUDIO_FRAME_FLAG, "", "", null);

                }
            } catch (Exception e) {
                e.printStackTrace();
                //Toast.makeText(IndividualCallService.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                logger.error("IndividualCallService :"+e.toString());
            }
        }else {
            ToastUtil.showToast(PullGB28181Service.this,getResources().getString(R.string.no_rtsp_data));
            stopBusiness();
        }
    }



    private int pullcount;
    private int mLiveWidth;
    private int mLiveHeight;
    private ResultReceiver mResultReceiver = new ResultReceiver(new Handler()){
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData){
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == EasyRTSPClient.RESULT_VIDEO_DISPLAYED) {
                mHandler.sendEmptyMessage(HIDE_LOADING_VIEW);
            } else if (resultCode == EasyRTSPClient.RESULT_VIDEO_SIZE) {
//                if(isPulling){
//                    return;
//                }
                mLiveWidth = resultData.getInt(EasyRTSPClient.EXTRA_VIDEO_WIDTH);
                mLiveHeight = resultData.getInt(EasyRTSPClient.EXTRA_VIDEO_HEIGHT);
//                onVideoSizeChange();
            } else if (resultCode == EasyRTSPClient.RESULT_TIMEOUT) {
                ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.time_up));
                finishVideoLive();
            } else if (resultCode == EasyRTSPClient.RESULT_UNSUPPORTED_AUDIO) {
                ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.voice_not_support));
                finishVideoLive();
            } else if (resultCode == EasyRTSPClient.RESULT_UNSUPPORTED_VIDEO) {
                ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.video_not_support));
                finishVideoLive();
            } else if (resultCode == EasyRTSPClient.RESULT_EVENT) {
//                int errorcode = resultData.getInt("errorcode");
//                String resultDataString = resultData.getString("event-msg");
                int state = resultData.getInt("state");
                if(state == 1){
                    //延时发送取消显示加载布局
                    mHandler.sendEmptyMessage(SHOW_LOADING_VIEW);
                    mHandler.sendEmptyMessageDelayed(HIDE_LOADING_VIEW,HIDE_LOADING_VIEW_TIME);
                }else{
                    //如果在state == 1状态下延时发送取消显示加载布局之前，收到state!=1的状态时，就把取消显示加载布局的message去掉
                    mHandler.removeMessages(HIDE_LOADING_VIEW);
                }
            }
        }
    };

    private void finishVideoLive(){
        stopPull();
        stopBusiness();
    }

    private void onVideoSizeChange() {
        if (mLiveWidth == 0 || mLiveHeight == 0) {
            return;
        }
        mSvGb28181.setTransform(new Matrix());
        float gb18181RatioView = mReVideoRoot.getWidth() * 1.0f/mReVideoRoot.getHeight();
        float ratio = mLiveWidth * 1.0f/mLiveHeight;


        if (gb18181RatioView - ratio < 0){
            // 宽为基准.

            mSvGb28181.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            mSvGb28181.getLayoutParams().height = (int) (mReVideoRoot.getWidth() / ratio + 0.5f);
        }
        // 视频是竖屏了.
        else{

            mSvGb28181.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            mSvGb28181.getLayoutParams().width = (int) (mReVideoRoot.getHeight() * ratio + 0.5f);
        }

        mSvGb28181.requestLayout();
    }


    @SuppressLint("SimpleDateFormat")
    private void setCurrentTime(){
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        Date currentTime = new Date();
        String dateString = formatter.format(currentTime);
        mTvGb28181Time.setText(dateString);
        mHandler.sendEmptyMessageDelayed(CURRENTTIME, 10000);
    }

    /**
     * 根据权限设置组呼PTT图标
     */
    private void setPushAuthority() {
        //图像推送
        if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_PUSH.name())){
            mLlInviteMember.setVisibility(View.GONE);
        }else {
//            mLlInviteMember.setVisibility(View.VISIBLE);
        }
    }

    private void showLivingView() {
        try{
            if(MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_PUSH.name())){
                mLlInviteMember.setVisibility(View.VISIBLE);
            }else{
                mLlInviteMember.setVisibility(View.GONE);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void hideLivingView() {
        try{
            mLlInviteMember.setVisibility(View.GONE);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

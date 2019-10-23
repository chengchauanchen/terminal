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
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHiKvisionUrlHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyMemberStopWatchMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.vc.R;
import cn.vsx.vc.utils.Constants;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

import static cn.vsx.vc.receive.Actions.KILL_LIVE_WATCH;

public class PullOutGB28181Service extends BaseService{

    private RelativeLayout mReVideoRoot;
    private TextureView mSvGb28181;
    private TextView mTvGb28181Time;
    private TextView mTvDeviceName;
    private ImageView mIvClose;
    private LinearLayout mLlInviteMember;
    private ImageView mIvLiveLookAddmember;
    private RelativeLayout mVideoPlatform;
    private String gb28181Url;
    private Logger logger = Logger.getLogger(this.getClass());
    private static final int CURRENTTIME = 1;


    private EasyRTSPClient mStreamRender;
    private TerminalMessage terminalMessage;
    //是否开始拉流
    private boolean started;


    public PullOutGB28181Service(){}


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
        mLlNoNetwork = rootView.findViewById(R.id.ll_no_network);
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
        mIvClose.setOnClickListener(closeOnClickListener);
        mLlInviteMember.setOnClickListener(inviteOnClickListener);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyMemberStopWatchMessageHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveHiKvisionUrlHandler);
    }

    @Override
    protected void initView(Intent intent){
        terminalMessage = (TerminalMessage) intent.getSerializableExtra(Constants.TERMINALMESSAGE);

        if(terminalMessage!=null){
            setPushAuthority();
            final String deviceId = terminalMessage.messageBody.getString(JsonParam.DEVICE_ID);
            TerminalFactory.getSDK().getDataManager().getHikvisionUrl(terminalMessage);
            mTvDeviceName.setText(deviceId+"正在上报图像");
        }else{
            ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.text_pull_data_error));
            stopBusiness();
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
            case OFF_LINE:
                ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.exit_pull));
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
            if(checkIfStartPull()){
                startPullGB28121(mSvGb28181.getSurfaceTexture());
            }
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyMemberStopWatchMessageHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveHiKvisionUrlHandler);
        unregisterReceiver(mBroadcastReceiv);
    }

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

    /**
     * 收到OutGB28181的播放地址
     **/
    private ReceiveHiKvisionUrlHandler receiveHiKvisionUrlHandler = (success,result,deviceId) -> {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(success){
                    gb28181Url = result;
                    logger.info("播放地址："+ gb28181Url);
                    mVideoPlatform.setVisibility(View.VISIBLE);
                    if(!started && null != mSvGb28181.getSurfaceTexture()){
                        startPullGB28121(mSvGb28181.getSurfaceTexture());
                    }
                }else{
                    ToastUtil.showToast(MyTerminalFactory.getSDK().application,result);
                    stopBusiness();
                }
            }
        });
    };

    private View.OnClickListener inviteOnClickListener = v->{
        pushToUser();
    };

    private View.OnClickListener inviteMemberOnClickListener = v -> {
        pushToUser();
    };

    private View.OnClickListener closeOnClickListener = v -> {
        ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.watch_finished));
        stopBusiness();
    };

    private TextureView.SurfaceTextureListener GB28181SurfaceTextureListener  = new TextureView.SurfaceTextureListener(){
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height){
            logger.info("onSurfaceTextureAvailable");
            if(checkIfStartPull()){
                startPullGB28121(surface);
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height){
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface){
            logger.info("onSurfaceTextureDestroyed");
            stopPull();
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface){
        }
    };

    private boolean checkIfStartPull(){
        return mSvGb28181.getSurfaceTexture() != null && !android.text.TextUtils.isEmpty(gb28181Url);
    }

    private void stopPull(){
        mHandler.removeMessages(CURRENTTIME);
        if (mStreamRender != null) {
            mStreamRender.stop();
            started = false;
            mStreamRender = null;
        }
    }

    private void startPullGB28121(SurfaceTexture surface){
        mHandler.sendEmptyMessage(CURRENTTIME);
        if(!TextUtils.isEmpty(gb28181Url)){

            mStreamRender = new EasyRTSPClient(PullOutGB28181Service.this, MyTerminalFactory.getSDK().getLiveConfigManager().getPlayKey(),
                    surface, mResultReceiver);
            try {
                if (gb28181Url != null) {
                    mStreamRender.start(gb28181Url, RTSPClient.TRANSTYPE_TCP, RTSPClient.EASY_SDK_VIDEO_FRAME_FLAG | RTSPClient.EASY_SDK_AUDIO_FRAME_FLAG, "", "", null);
                    started = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                //Toast.makeText(IndividualCallService.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                logger.error("拉流失败 :"+e);
            }
        }
        //去海康服务请求视频地址可能还没返回
        //        else {
        //            ToastUtil.showToast(PullOutGB28181Service.this,getResources().getString(R.string.no_rtsp_data));
        //            stopBusiness();
        //        }
    }



    private int pullcount;
    private int mLiveWidth;
    private int mLiveHeight;
    private ResultReceiver mResultReceiver = new ResultReceiver(new Handler()){
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData){
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == EasyRTSPClient.RESULT_VIDEO_DISPLAYED) {
                pullcount = 0;
            } else if (resultCode == EasyRTSPClient.RESULT_VIDEO_SIZE) {
                //                if(isPulling){
                //                    return;
                //                }
                mLiveWidth = resultData.getInt(EasyRTSPClient.EXTRA_VIDEO_WIDTH);
                mLiveHeight = resultData.getInt(EasyRTSPClient.EXTRA_VIDEO_HEIGHT);
                onVideoSizeChange();
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

                int errorcode = resultData.getInt("errorcode");
                String resultDataString = resultData.getString("event-msg");
                logger.error("视频流播放状态：" + errorcode + "=========" + resultDataString+"-----count:"+ pullcount);

                if (errorcode != 0) {
                    stopPull();
                }
                if (errorcode == 500 || errorcode == 404 ||errorcode ==-32 || errorcode == -101) {
                    if (pullcount < 10) {
                        try {
                            Thread.sleep(300);
                            logger.error("请求第" + pullcount + "次");
                            if (mSvGb28181 != null && mSvGb28181.getVisibility() == View.VISIBLE && mSvGb28181.getSurfaceTexture() != null) {
                                startPullGB28121(mSvGb28181.getSurfaceTexture());
                                pullcount++;

                            }else{
                                ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.push_stoped));
                                finishVideoLive();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.push_stoped));
                        finishVideoLive();
                    }
                } else if(errorcode !=0){
                    ToastUtil.showToast(MyTerminalFactory.getSDK().application,resultDataString);
                    finishVideoLive();
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
            mLlInviteMember.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 推送图像给用户
     */
    private void pushToUser(){
        if (MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_PUSH.name())) {
            Intent intent = new Intent(PullOutGB28181Service.this,InviteMemberService.class);
            intent.putExtra(Constants.TYPE,Constants.PULL);
            intent.putExtra(Constants.PULLING,true);
            intent.putExtra(Constants.GB28181_PULL,true);
            intent.putExtra(Constants.TERMINALMESSAGE,terminalMessage);
            startService(intent);
        }else{
            ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.text_no_video_push_authority));
        }
    }
}

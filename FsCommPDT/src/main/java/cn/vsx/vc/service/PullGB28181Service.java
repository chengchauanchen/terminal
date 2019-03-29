package cn.vsx.vc.service;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.vc.R;
import cn.vsx.vc.utils.Constants;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

import static org.easydarwin.config.Config.PLAYKEY;

public class PullGB28181Service extends BaseService{

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

    public PullGB28181Service(){}


    @SuppressLint("InflateParams")
    @Override
    protected void setRootView(){
        rootView = LayoutInflater.from(MyTerminalFactory.getSDK().application).inflate(R.layout.layout_gb28181_view, null);
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
    }

    @Override
    protected void initData(){
    }

    @Override
    protected void initBroadCastReceiver(){
    }

    @Override
    protected void initListener(){
        mIvLiveLookAddmember.setOnClickListener(inviteMemberOnClickListener);
        mSvGb28181.setSurfaceTextureListener(GB28181SurfaceTextureListener);
        mIvClose.setOnClickListener(closeOnClickListener);
        mLlInviteMember.setOnClickListener(inviteOnClickListener);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveUpdateConfigHandler);
    }

    @Override
    protected void initView(Intent intent){
        terminalMessage = (TerminalMessage) intent.getSerializableExtra(Constants.TERMINALMESSAGE);
        if(terminalMessage.messageBody.containsKey(JsonParam.GB28181_RTSP_URL)){
            setPushAuthority();
            gb28181Url = terminalMessage.messageBody.getString(JsonParam.GB28181_RTSP_URL);
            logger.info("播放地址："+ gb28181Url);
            String deviceName = terminalMessage.messageBody.getString(JsonParam.DEVICE_NAME);
            mVideoPlatform.setVisibility(View.VISIBLE);
            mTvDeviceName.setText(deviceName);
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
            if(null != mSvGb28181.getSurfaceTexture()){
                startPullGB28121(mSvGb28181.getSurfaceTexture());
            }
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveUpdateConfigHandler);
    }

    private ReceiveUpdateConfigHandler mReceiveUpdateConfigHandler= () -> mHandler.post(this::setPushAuthority);

    private View.OnClickListener inviteOnClickListener = v->{
        if (MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_PUSH.name())) {
            Intent intent = new Intent(PullGB28181Service.this,InviteMemberService.class);
            intent.putExtra(Constants.TYPE,Constants.PULL);
            intent.putExtra(Constants.PULLING,true);
        }else{
            ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.text_no_video_push_authority));
        }
    };

    private View.OnClickListener inviteMemberOnClickListener = v -> {
        Intent intent = new Intent(PullGB28181Service.this,InviteMemberService.class);
        intent.putExtra(Constants.TYPE,Constants.PULL);
        intent.putExtra(Constants.PULLING,true);
        intent.putExtra(Constants.GB28181_PULL,true);
        intent.putExtra(Constants.TERMINALMESSAGE,terminalMessage);
        startService(intent);
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

            mStreamRender = new EasyRTSPClient(PullGB28181Service.this, PLAYKEY,
                    surface, mResultReceiver);
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
                stopBusiness();
            } else if (resultCode == EasyRTSPClient.RESULT_UNSUPPORTED_AUDIO) {
                ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.voice_not_support));
                stopBusiness();
            } else if (resultCode == EasyRTSPClient.RESULT_UNSUPPORTED_VIDEO) {
                ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.video_not_support));
                stopBusiness();
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
                                stopBusiness();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.push_stoped));
                        stopBusiness();
                    }
                } else if(errorcode !=0){
                    ToastUtil.showToast(MyTerminalFactory.getSDK().application,resultDataString);
                    stopBusiness();
                }
            }
        }
    };

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
}

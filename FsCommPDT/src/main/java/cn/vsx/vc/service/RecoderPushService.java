package cn.vsx.vc.service;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.easydarwin.push.EasyPusher;
import org.easydarwin.push.InitCallback;
import org.easydarwin.util.PushRTSPClient;
import org.easydarwin.video.EasyRTSPClient;
import org.easydarwin.video.RTSPClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.TerminalErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState;
import cn.vsx.hamster.terminalsdk.model.VideoMember;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetVideoPushUrlHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallCeasedIndicationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberJoinOrExitHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyLivingStoppedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseMyselfLiveHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.MemberEnterAdapter;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.DataUtil;
import cn.vsx.vc.utils.HandleIdUtil;
import cn.vsx.vc.utils.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

import static org.easydarwin.config.Config.PLAYKEY;

public class RecoderPushService extends BaseService{

    private RelativeLayout mPopupMiniLive;
    private TextureView mSvLivePop;
    private TextureView mSvLive;
    private TextView mLiveVedioTheme;
    private TextView mTvLiveRealtime;
    private ImageView mIvLiveRetract;
    private LinearLayout mLlLiveGroupCall;
    private TextView mTvLiveSpeakingName;
    private TextView mTvLiveGroupName;
    private TextView mTvLiveSpeakingId;
    private ListView mLvLiveMemberInfo;
    private ImageView mIvLiveAddmember;
    private ImageView mIvHangup;
    private RelativeLayout mRlRecoderView;
    private PushRTSPClient pushRTSPClient;
    private int pushcount;
    private int mLiveWidth;
    private int mLiveHeight;
    private int pullcount;
    private String id;//自己发起直播返回的callId和member拼接
    private RelativeLayout mRlLiveGeneralView;
    private String ip;
    private String port;
    private ArrayList<VideoMember> watchMembers;
    private ArrayList<VideoMember> watchOrExitMembers;
    private List<Integer> pushMemberList;
    private MemberEnterAdapter enterOrExitMemberAdapter;
    private static final int CURRENTTIME = 0;
    private static final int HIDELIVINGVIEW = 1;
    private LinearLayout mLlLiveInviteMember;
    private LinearLayout mLlLiveHangupTotal;

    public RecoderPushService(){}

    @Override
    protected void initListener(){
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyLivingStoppedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseMyselfLiveHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGetVideoPushUrlHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveMemberJoinOrExitHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveUpdateConfigHandler);

        mSvLive.setSurfaceTextureListener(surfaceTextureListener);
        mSvLivePop.setSurfaceTextureListener(surfaceTextureListener);
        mIvHangup.setOnClickListener(hangUpOnClickListener);
        mIvLiveRetract.setOnClickListener(retractOnClickListener);
        mSvLive.setOnClickListener(svOnClickListener);
        mIvLiveAddmember.setOnClickListener(inviteMemberOnClickListener);

    }

    @Override
    protected void initView(Intent intent){
        String type = intent.getStringExtra(Constants.TYPE);

        mPopupMiniLive.setVisibility(View.GONE);
        mRlRecoderView.setVisibility(View.VISIBLE);
        showLivingView();
        mHandler.sendEmptyMessage(CURRENTTIME);
        mHandler.sendEmptyMessageDelayed(HIDELIVINGVIEW, 5000);
        if(Constants.ACTIVE_PUSH.equals(type)){
            pushMemberList = intent.getIntegerArrayListExtra(Constants.PUSH_MEMBERS);
            String theme = intent.getStringExtra(Constants.THEME);
            if(TextUtils.isEmpty(theme)){
                mLiveVedioTheme.setText(getResources().getString(R.string.i_pushing_video));
            }else {
                mLiveVedioTheme.setText(theme);
            }
            int requestCode = MyTerminalFactory.getSDK().getLiveManager().requestMyselfLive(theme, "");
            if(requestCode != BaseCommonCode.SUCCESS_CODE){
                ptt.terminalsdk.tools.ToastUtil.livingFailToast(this, requestCode, TerminalErrorCode.LIVING_PUSHING.getErrorCode());
                finishVideoLive();
            }
        }else if(Constants.RECEIVE_PUSH.equals(type)){
            mLiveVedioTheme.setText(getResources().getString(R.string.i_pushing_video));
        }
    }

    @Override
    protected void showPopMiniView(){
        MyApplication.instance.isMiniLive = true;
        windowManager.removeView(rootView);
        windowManager.addView(rootView, layoutParams);
        mRlRecoderView.setVisibility(View.GONE);
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
                mHandler.removeMessages(OFF_LINE);
                ToastUtil.showToast(getApplicationContext(),getResources().getString(R.string.exit_push));
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
                pushLawRecorder(mSvLivePop.getSurfaceTexture());
            }else{
                pushLawRecorder(mSvLive.getSurfaceTexture());
            }
        }
    }

    protected void initData(){
        watchOrExitMembers = new ArrayList<>();
        watchMembers = new ArrayList<>();
        enterOrExitMemberAdapter = new MemberEnterAdapter(getApplicationContext(), watchOrExitMembers);
        mLvLiveMemberInfo.setAdapter(enterOrExitMemberAdapter);
    }

    @Override
    protected void initBroadCastReceiver(){
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyLivingStoppedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseMyselfLiveHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetVideoPushUrlHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMemberJoinOrExitHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveUpdateConfigHandler);
    }

    @SuppressLint("InflateParams")
    @Override
    protected void setRootView(){
        rootView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_recoder_push, null);
    }

    @Override
    protected void findView(){
        mPopupMiniLive = rootView.findViewById(R.id.popup_mini_live);
        mSvLivePop = rootView.findViewById(R.id.sv_live_pop);
        mSvLive =  rootView.findViewById(R.id.sv_live);
        mRlRecoderView = rootView.findViewById(R.id.rl_recoder_view);
        mLlLiveInviteMember = rootView.findViewById(R.id.ll_live_invite_member);
        mLlLiveHangupTotal = rootView.findViewById(R.id.ll_live_hangup_total);
        mRlLiveGeneralView = rootView.findViewById(R.id.rl_live_general_view);
        mLiveVedioTheme =  rootView.findViewById(R.id.live_vedioTheme);
        mTvLiveRealtime =  rootView.findViewById(R.id.tv_live_realtime);
        mIvLiveRetract =  rootView.findViewById(R.id.iv_live_retract);
        mLlLiveGroupCall =  rootView.findViewById(R.id.ll_live_group_call);
        mTvLiveSpeakingName =  rootView.findViewById(R.id.tv_live_speakingName);
        mTvLiveGroupName =  rootView.findViewById(R.id.tv_live_groupName);
        mTvLiveSpeakingId =  rootView.findViewById(R.id.tv_live_speakingId);
        mLvLiveMemberInfo =  rootView.findViewById(R.id.lv_live_member_info);
        mIvLiveAddmember =  rootView.findViewById(R.id.iv_live_addmember);
        mIvHangup = rootView.findViewById(R.id.iv_hangup);
    }

    private View.OnClickListener hangUpOnClickListener = v-> finishVideoLive();

    private View.OnClickListener retractOnClickListener = v -> showPopMiniView();


    private View.OnClickListener svOnClickListener = v->{
        showLivingView();
        mHandler.removeMessages(HIDELIVINGVIEW);
        mHandler.sendEmptyMessageDelayed(HIDELIVINGVIEW, 5000);
    };

    private View.OnClickListener inviteMemberOnClickListener = v -> {
        Intent intent = new Intent(this, InviteMemberService.class);
        intent.putExtra(Constants.TYPE, Constants.PUSH);
        intent.putExtra(Constants.PUSHING, true);
        intent.putExtra(Constants.WATCHING_MEMBERS, watchMembers);
        startService(intent);
    };

    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener(){
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height){
            if(null != id){
                pushLawRecorder(surface);
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

    private ReceiveGroupCallIncommingHandler receiveGroupCallIncommingHandler = (memberId, memberName, groupId, groupName, currentCallMode) -> {
        if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_LISTEN.name())){
            ptt.terminalsdk.tools.ToastUtil.showToast(getApplicationContext(),getString(R.string.text_has_no_group_call_listener_authority));
        }else{
            mHandler.post(() -> {
                mLlLiveGroupCall.setVisibility(View.VISIBLE);
                mTvLiveGroupName.setText(DataUtil.getGroupByGroupNo(groupId).name);
                mTvLiveSpeakingName.setText(memberName);
                mTvLiveSpeakingId.setText(HandleIdUtil.handleId(memberId));
            });
        }
    };

    private ReceiveGroupCallCeasedIndicationHandler receiveGroupCallCeasedIndicationHandler = (reasonCode) -> {
        logger.info("收到组呼停止");
        mHandler.post(() -> mLlLiveGroupCall.setVisibility(View.GONE));
    };

    /**
     * 通知直播停止 通知界面关闭视频页
     **/
    private ReceiveNotifyLivingStoppedHandler receiveNotifyLivingStoppedHandler = (methodResult, resultDesc) -> {
        ToastUtil.showToast(getApplicationContext(), getResources().getString(R.string.push_stoped));
        mHandler.post(this::finishVideoLive);
    };

    /**
     * 自己发起直播的响应
     **/
    private ReceiveResponseMyselfLiveHandler receiveResponseMyselfLiveHandler = (resultCode, resultDesc) -> mHandler.post(() -> {
        if(resultCode == BaseCommonCode.SUCCESS_CODE){
            if(pushMemberList != null && !pushMemberList.isEmpty()){
                logger.info("自己发起直播成功,要推送的列表：" + pushMemberList);
                MyTerminalFactory.getSDK().getLiveManager().requestNotifyWatch(pushMemberList, MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0));
            }
        }else{
            ptt.terminalsdk.tools.ToastUtil.showToast(getApplicationContext(), resultDesc);
            finishVideoLive();
        }
    });

    /**
     * 自己发起直播的响应
     **/
    private ReceiveGetVideoPushUrlHandler receiveGetVideoPushUrlHandler = (streamMediaServerIp, streamMediaServerPort, callId) -> mHandler.postDelayed(() -> {
        logger.info("自己发起直播，服务端返回的ip：" + streamMediaServerIp + "端口：" + streamMediaServerPort + "---callId:" + callId);
        ip = streamMediaServerIp;
        port = String.valueOf(streamMediaServerPort);
        id = TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) + "_" + callId;
        if(null != mSvLive.getSurfaceTexture()){
            pushLawRecorder(mSvLive.getSurfaceTexture());
        }
    }, 1000);

    private ReceiveUpdateConfigHandler mReceiveUpdateConfigHandler= () -> mHandler.post(this::setAuthorityView);

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

    private RtspReceiver mResultReceiver;

    private class PushCallback implements InitCallback{

        @Override
        public void onCallback(int code){
            Bundle resultData = new Bundle();
            switch (code) {
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
                    if(pushcount <=10){
                        pushcount++;
                    }else {
                        finishVideoLive();
                    }
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_CONNECT_ABORT:
                    resultData.putString("event-msg", "EasyRTSP 连接异常中断");
                    if(pushcount <=10){
                        pushcount++;
                    }else {
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

    private PushCallback pushCallback;

    private class RtspReceiver extends ResultReceiver{


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
                ToastUtil.showToast(getApplicationContext(),getResources().getString(R.string.time_up));

            }else if(resultCode == EasyRTSPClient.RESULT_UNSUPPORTED_AUDIO){
                ToastUtil.showToast(getApplicationContext(),getResources().getString(R.string.voice_not_support));
            }else if(resultCode == EasyRTSPClient.RESULT_UNSUPPORTED_VIDEO){
                ToastUtil.showToast(getApplicationContext(),getResources().getString(R.string.video_not_support));
            }else if(resultCode == EasyRTSPClient.RESULT_EVENT){
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
                                pushLawRecorder(mSvLive.getSurfaceTexture());
                                pullcount++;

                            }else if(mSvLivePop != null && mSvLivePop.getVisibility() == View.VISIBLE && mSvLivePop.getSurfaceTexture() != null){
                                pushLawRecorder(mSvLivePop.getSurfaceTexture());
                                pullcount++;
                            }else{
                                TerminalFactory.getSDK().getLiveManager().ceaseWatching();
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }else{
                        ToastUtil.showToast(getApplicationContext(),getResources().getString(R.string.push_stoped));
                        TerminalFactory.getSDK().getLiveManager().ceaseWatching();
                        finishVideoLive();
                    }
                }else if(errorcode != 0){
                    ToastUtil.showToast(getApplicationContext(),resultDataString);
                    finishVideoLive();
                }
            }
        }
    }


    private void finishVideoLive(){
        stopPull();
        stopBusiness();
    }

    private void stopPull(){
        mHandler.removeMessages(CURRENTTIME);
        if(null !=pushRTSPClient){
            pushRTSPClient.stop();
            pushRTSPClient = null;
        }
        TerminalFactory.getSDK().getLiveManager().ceaseLiving();
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

    private void pushLawRecorder(SurfaceTexture surface){
        if(null == mResultReceiver){
            mResultReceiver = new RtspReceiver(new Handler());
        }
        if(null == pushCallback){
            pushCallback = new PushCallback();
        }
        pushRTSPClient = new PushRTSPClient(this, PLAYKEY,surface, mResultReceiver);
        pushRTSPClient.setRTSPInfo(ip,port,id, pushCallback);
        pushRTSPClient.start(Constants.LAWRECODER, RTSPClient.TRANSTYPE_TCP, RTSPClient.EASY_SDK_VIDEO_FRAME_FLAG | RTSPClient.EASY_SDK_AUDIO_FRAME_FLAG, "", "", null);
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
        mTvLiveRealtime.setVisibility(View.GONE);
        mIvLiveRetract.setVisibility(View.GONE);
        mLlLiveHangupTotal.setVisibility(View.GONE);
        mLlLiveInviteMember.setVisibility(View.GONE);
    }

    private void showLivingView(){
        mTvLiveRealtime.setVisibility(View.VISIBLE);
        mIvLiveRetract.setVisibility(View.VISIBLE);
        mLlLiveHangupTotal.setVisibility(View.VISIBLE);
        mLlLiveInviteMember.setVisibility(View.VISIBLE);
        setAuthorityView();
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

    private void setAuthorityView(){
        //推送图像权限
        if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_PUSH.name())){
            mLlLiveInviteMember.setVisibility(View.GONE);
        }else {
            mLlLiveInviteMember.setVisibility(View.VISIBLE);
        }
    }
}

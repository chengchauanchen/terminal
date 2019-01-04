package cn.vsx.vc.service;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveAnswerLiveTimeoutHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetRtspStreamUrlHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyLivingStoppedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseStartLiveHandler;
import cn.vsx.hamster.terminalsdk.tools.Util;
import cn.vsx.vc.R;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.HandleIdUtil;
import cn.vsx.vc.utils.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

public class LiveRequestService extends BaseService{


//    private ImageView mIvAvatarRequest;
    private TextView mTvLiveRequestName;
    private TextView mTvLiveRequestId;
    private LinearLayout mLlLiveRequestStopTotal;
    private int memberId;

    public LiveRequestService(){}

    @SuppressLint("InflateParams")
    @Override
    protected void setRootView(){
        rootView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.live_request, null);
    }

    @Override
    protected void findView(){
        //        mIvAvatarRequest =  rootView.findViewById(R.id.iv_avatar_request);
        mTvLiveRequestName =  rootView.findViewById(R.id.tv_live_request_name);
        mTvLiveRequestId =  rootView.findViewById(R.id.tv_live_request_id);
        mLlLiveRequestStopTotal = rootView.findViewById(R.id.ll_live_request_stop_total);
    }

    @Override
    protected void initData(){
    }

    @Override
    protected void initBroadCastReceiver(){
    }

    @Override
    protected void initListener(){
        mLlLiveRequestStopTotal.setOnClickListener(cancelOnClickListener);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveReaponseStartLiveHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveAnswerLiveTimeoutHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGetRtspStreamUrlHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyLivingStoppedHandler);
    }

    @Override
    protected void initView(Intent intent){
        String memberName = intent.getStringExtra(Constants.MEMBER_NAME);
        memberId = intent.getIntExtra(Constants.MEMBER_ID, 0);
        //开始响铃
        PromptManager.getInstance().IndividualCallRequestRing();
        mTvLiveRequestName.setText(HandleIdUtil.handleName(memberName));
        mTvLiveRequestId.setText(HandleIdUtil.handleId(memberId));
    }

    @Override
    protected void showPopMiniView(){
    }

    @Override
    protected void handleMesage(Message msg){
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveReaponseStartLiveHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveAnswerLiveTimeoutHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetRtspStreamUrlHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyLivingStoppedHandler);
    }

    private View.OnClickListener cancelOnClickListener = v ->{
        MyTerminalFactory.getSDK().getLiveManager().stopRequestMemberLive(memberId);
        PromptManager.getInstance().stopRing();
        ToastUtil.showToast(getApplicationContext(),getResources().getString(R.string.canceled));
        removeView();
    };

    /**
     * 对方拒绝直播，通知界面关闭响铃页
     **/
    private ReceiveResponseStartLiveHandler receiveReaponseStartLiveHandler = (resultCode, resultDesc)-> mHandler.post(() -> {
        ToastUtil.showToast(getApplicationContext(),resultDesc);
        MyTerminalFactory.getSDK().getLiveManager().ceaseWatching();
        windowManager.removeView(rootView);
    });

    /**
     * 超时未回复answer 通知界面关闭
     **/
    private ReceiveAnswerLiveTimeoutHandler receiveAnswerLiveTimeoutHandler = () -> {
        PromptManager.getInstance().stopRing();//停止响铃
        ToastUtil.showToast(getApplicationContext(),getResources().getString(R.string.other_no_answer));
        MyTerminalFactory.getSDK().getLiveManager().ceaseWatching();
        windowManager.removeView(rootView);
    };

    /**
     * 通知直播停止 通知界面关闭视频页
     **/
    private ReceiveNotifyLivingStoppedHandler receiveNotifyLivingStoppedHandler = (methodResult, resultDesc) -> {
        ToastUtil.showToast(getApplicationContext(),getResources().getString(R.string.push_stoped));
        MyTerminalFactory.getSDK().getLiveManager().ceaseWatching();
        windowManager.removeView(rootView);
    };

    /**
     * 获取到rtsp地址，开始播放视频
     */
    private ReceiveGetRtspStreamUrlHandler receiveGetRtspStreamUrlHandler = (final String rtspUrl, final Member liveMember, long callId)-> mHandler.post(() -> {
        if (Util.isEmpty(rtspUrl)) {
            ToastUtil.showToast(getApplicationContext(),getResources().getString(R.string.no_rtsp_data));
            MyTerminalFactory.getSDK().getLiveManager().ceaseWatching();
            windowManager.removeView(rootView);
        }else {
            logger.info("rtspUrl ----> " + rtspUrl);
            PromptManager.getInstance().stopRing();
            Intent intent = new Intent(LiveRequestService.this,PullLivingService.class);
            intent.putExtra(Constants.WATCH_TYPE,Constants.RECEIVE_WATCH);
            intent.putExtra(Constants.RTSP_URL,rtspUrl);
            intent.putExtra(Constants.LIVE_MEMBER,liveMember);
            startService(intent);
            mHandler.postDelayed(() -> windowManager.removeView(rootView),1000);
        }
    });
}

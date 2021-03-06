package cn.vsx.vc.service;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.TerminalErrorCode;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveAnswerLiveTimeoutHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetRtspStreamUrlHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNetworkChangeHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyLivingStoppedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseStartLiveHandler;
import cn.vsx.hamster.terminalsdk.tools.Util;
import cn.vsx.vc.R;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.utils.BitmapUtil;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.HandleIdUtil;
import cn.vsx.vc.utils.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.search.SearchUtil;

public class LiveRequestService extends BaseService{


//    private ImageView mIvAvatarRequest;
    private TextView mTvLiveRequestName;
    private TextView mTvLiveRequestId;
    private LinearLayout mLlLiveRequestStopTotal;
    private int memberId;
    private long uniqueNo;

    public LiveRequestService(){}

    @SuppressLint("InflateParams")
    @Override
    protected void setRootView(){
        rootView = LayoutInflater.from(MyTerminalFactory.getSDK().application).inflate(R.layout.live_request, null);
    }

    @Override
    protected void initWindow() {
        super.initWindow();
        //如果屏幕宽度小于高度就开启横屏
        layoutParams1.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    }

    @Override
    protected void findView(){
        ImageView mIvAvatarRequest =  rootView.findViewById(R.id.iv_avatar_request);
        mIvAvatarRequest.setImageResource(BitmapUtil.getUserPhoto());
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
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNetworkChangeHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveReaponseStartLiveHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveAnswerLiveTimeoutHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGetRtspStreamUrlHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyLivingStoppedHandler);
    }

    @Override
    protected void initView(Intent intent){

        String memberName = intent.getStringExtra(Constants.MEMBER_NAME);
        memberId = intent.getIntExtra(Constants.MEMBER_ID, 0);
        uniqueNo = intent.getLongExtra(Constants.UNIQUE_NO, 0L);
        //开始响铃
        PromptManager.getInstance().IndividualCallRequestRing();
        mTvLiveRequestName.setText(HandleIdUtil.handleName(memberName));
        mTvLiveRequestId.setText(HandleIdUtil.handleId(memberId));

        int requestCode = MyTerminalFactory.getSDK().getLiveManager().requestMemberLive(memberId,uniqueNo, "",false);
        if(requestCode == BaseCommonCode.SUCCESS_CODE){
        }else{
            ptt.terminalsdk.tools.ToastUtil.livingFailToast(LiveRequestService.this, requestCode, TerminalErrorCode.LIVING_REQUEST.getErrorCode());
            stopBusiness();
        }

        //设置常用联系人 的Tag
        logger.info("设置常用联系人 memberId:"+memberId);
        SearchUtil.setUpdateUseTimeTag(memberId);
    }

    @Override
    protected void showPopMiniView(){
    }

    @Override
    protected void handleMesage(Message msg){
    }

    @Override
    protected void onNetworkChanged(boolean connected){
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNetworkChangeHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveReaponseStartLiveHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveAnswerLiveTimeoutHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetRtspStreamUrlHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyLivingStoppedHandler);
    }

    private ReceiveNetworkChangeHandler receiveNetworkChangeHandler = new ReceiveNetworkChangeHandler(){
        @Override
        public void handler(boolean connected){
            if(!connected){
                MyTerminalFactory.getSDK().getLiveManager().stopRequestMemberLive(memberId,uniqueNo,TerminalErrorCode.STOP_REQUEST.getErrorCode());
                stopBusiness();
            }
        }
    };

    private View.OnClickListener cancelOnClickListener = v ->{
        ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.canceled));
        MyTerminalFactory.getSDK().getLiveManager().stopRequestMemberLive(memberId,uniqueNo,TerminalErrorCode.STOP_REQUEST.getErrorCode());
        stopBusiness();
    };

    /**
     * 对方拒绝直播，通知界面关闭响铃页
     **/
    private ReceiveResponseStartLiveHandler receiveReaponseStartLiveHandler = (resultCode, resultDesc, liveMemberId, liveUniqueNo)-> mHandler.post(() -> {
        if(!TextUtils.isEmpty(resultDesc)){
            ToastUtil.showToast(MyTerminalFactory.getSDK().application,resultDesc);
        }
        stopBusiness();
    });

    /**
     * 超时未回复answer 通知界面关闭
     **/
    private ReceiveAnswerLiveTimeoutHandler receiveAnswerLiveTimeoutHandler = () ->  mHandler.post(() -> {
        ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.other_no_answer));
        stopBusiness();
    });

    /**
     * 通知直播停止 通知界面关闭视频页
     **/
    private ReceiveNotifyLivingStoppedHandler receiveNotifyLivingStoppedHandler = (liveMemberId, callId, methodResult, resultDesc) -> mHandler.post(() -> {
        ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.push_stoped));
        stopBusiness();
    });

    /**
     * 获取到rtsp地址，开始播放视频
     */
    private ReceiveGetRtspStreamUrlHandler receiveGetRtspStreamUrlHandler = (final String rtspUrl, final Member liveMember, long callId)-> mHandler.post(() -> {
        if (Util.isEmpty(rtspUrl)) {
            ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.no_rtsp_data));
            stopBusiness();
        }else {
            logger.info("rtspUrl ----> " + rtspUrl);
            PromptManager.getInstance().stopRing();
            Intent intent = new Intent(LiveRequestService.this,PullLivingService.class);
            intent.putExtra(Constants.WATCH_TYPE,Constants.RECEIVE_WATCH);
            intent.putExtra(Constants.RTSP_URL,rtspUrl);
            intent.putExtra(Constants.LIVE_MEMBER,liveMember);
            startService(intent);
            mHandler.postDelayed(this::removeView,500);
        }
    });
}

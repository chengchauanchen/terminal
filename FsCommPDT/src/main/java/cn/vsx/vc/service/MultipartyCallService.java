package cn.vsx.vc.service;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.receiveHandle.ReceiveVoipCallEndHandler;
import cn.vsx.vc.receiveHandle.ReceiveVoipConnectedHandler;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.view.IndividualCallView;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.receiveHandler.ReceiveHeadSetPlugHandler;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/1/11
 * 描述：
 * 修订历史：
 */
public class MultipartyCallService extends BaseService{

//    private ImageView mIvMemberPortraitSpeaking;
    private TextView mTvMemberNameSpeaking;
//    private TextView mTvMemberIdSpeaking;
    private TextView mTvSpeakingToast;
    private IndividualCallView mIctvSpeakingTimeSpeaking;
    private ImageView mIvIndividualCallHangupSpeaking;
    private ImageView mIvIndividualCallMicroMute;
    private ImageView mIvIndividualCallHandFree;


    @SuppressLint("InflateParams")
    @Override
    protected void setRootView(){
        rootView = LayoutInflater.from(MyTerminalFactory.getSDK().application).inflate(R.layout.layout_multiparty_call, null);
    }

    @Override
    protected void findView(){
//        mIvMemberPortraitSpeaking =  rootView.findViewById(R.id.iv_member_portrait_speaking);
        mTvMemberNameSpeaking =  rootView.findViewById(R.id.tv_member_name_speaking);
//        mTvMemberIdSpeaking =  rootView.findViewById(R.id.tv_member_id_speaking);
        mTvSpeakingToast =  rootView.findViewById(R.id.tv_speaking_toast);
        mIctvSpeakingTimeSpeaking =  rootView.findViewById(R.id.ictv_speaking_time_speaking);
        mIvIndividualCallHangupSpeaking =  rootView.findViewById(R.id.iv_individual_call_hangup_speaking);
        mIvIndividualCallMicroMute = rootView.findViewById(R.id.iv_individual_call_micro_mute);
        mIvIndividualCallHandFree = rootView.findViewById(R.id.iv_individual_call_hand_free);
    }

    @Override
    protected void initData(){
    }

    @Override
    protected void initBroadCastReceiver(){
    }

    @Override
    protected void initListener(){
        mIvIndividualCallHangupSpeaking.setOnClickListener(hungUpOnClickListener);
        mIvIndividualCallMicroMute.setOnClickListener(microMuteListener);
        mIvIndividualCallHandFree.setOnClickListener(handFreeListener);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveVoipConnectedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveVoipCallEndHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveHeadSetPlugHandler);
    }

    @Override
    protected void initView(Intent intent){
        PromptManager.getInstance().stopRing();
        String userName = intent.getStringExtra(Constants.USER_NAME);
//        mTvMemberNameSpeaking.setText(userName);
        MyTerminalFactory.getSDK().getVoipCallManager().acceptCall();

        //打开默认听筒说话，
        setSpeakPhoneOn(mIvIndividualCallHandFree,false);
        setMicrophoneMute(mIvIndividualCallMicroMute,false);
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
        setMicrophoneMute(mIvIndividualCallMicroMute,false);
        //打开默认听筒说话，
        setSpeakPhoneOn(mIvIndividualCallHandFree,false);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveVoipConnectedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveVoipCallEndHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveHeadSetPlugHandler);
    }

    private View.OnClickListener hungUpOnClickListener = v-> huangUp();

    private View.OnClickListener microMuteListener = v -> {
        boolean isMicrophoneMute = MyTerminalFactory.getSDK().getAudioProxy().isMicrophoneMute();
        setMicrophoneMute(mIvIndividualCallMicroMute,!isMicrophoneMute);
    };

    private View.OnClickListener handFreeListener = v -> {
        if(MyApplication.instance.headset){
            ToastUtil.showToast(this,getString(R.string.text_head_set_can_not_hand_free));
            //设置为耳机模式
            setSpeakPhoneOn(mIvIndividualCallHandFree,false);
        }else{
            boolean isSpeakerphoneOn = MyTerminalFactory.getSDK().getAudioProxy().isSpeakerphoneOn();
            setSpeakPhoneOn(mIvIndividualCallHandFree,!isSpeakerphoneOn);
        }
    };

    private void huangUp(){
        mTvSpeakingToast.setText(getResources().getString(R.string.huang_up));
        MyTerminalFactory.getSDK().getVoipCallManager().hangUp();
        mIctvSpeakingTimeSpeaking.onStop();
        stopBusiness();
    }

    private ReceiveVoipConnectedHandler receiveVoipConnectedHandler = (linphoneCall)-> mHandler.post(new Runnable(){
        @Override
        public void run(){
            MyTerminalFactory.getSDK().getIndividualCallManager().responseIndividualCall(true);
            mIctvSpeakingTimeSpeaking.onStart();
        }
    });

    private ReceiveVoipCallEndHandler receiveVoipCallEndHandler = linphoneCall -> mHandler.post(this::huangUp);

    /**
     * 设置是否可以打开扬声器
     */
    private ReceiveHeadSetPlugHandler receiveHeadSetPlugHandler = new ReceiveHeadSetPlugHandler(){
        @Override
        public void handler(boolean headset){
            mHandler.post(()-> {
                if(headset){
                    //设置为耳机模式
                    setSpeakPhoneOn(mIvIndividualCallHandFree,false);
                }
            });
        }
    };
}

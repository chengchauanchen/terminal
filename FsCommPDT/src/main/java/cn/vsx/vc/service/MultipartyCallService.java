package cn.vsx.vc.service;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xuchongyang.easyphone.callback.PhoneCallback;

import org.linphone.core.LinphoneCall;

import cn.vsx.vc.R;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.view.IndividualCallView;
import ptt.terminalsdk.context.MyTerminalFactory;

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
    private TextView mTvMemberIdSpeaking;
    private TextView mTvSpeakingToast;
    private IndividualCallView mIctvSpeakingTimeSpeaking;
    private ImageView mIvIndividualCallHangupSpeaking;


    @SuppressLint("InflateParams")
    @Override
    protected void setRootView(){
        rootView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_multiparty_call, null);
    }

    @Override
    protected void findView(){
//        mIvMemberPortraitSpeaking =  rootView.findViewById(R.id.iv_member_portrait_speaking);
        mTvMemberNameSpeaking =  rootView.findViewById(R.id.tv_member_name_speaking);
        mTvMemberIdSpeaking =  rootView.findViewById(R.id.tv_member_id_speaking);
        mTvSpeakingToast =  rootView.findViewById(R.id.tv_speaking_toast);
        mIctvSpeakingTimeSpeaking =  rootView.findViewById(R.id.ictv_speaking_time_speaking);
        mIvIndividualCallHangupSpeaking =  rootView.findViewById(R.id.iv_individual_call_hangup_speaking);
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
    }

    @Override
    protected void initView(Intent intent){
        PromptManager.getInstance().stopRing();
        String userName = intent.getStringExtra(Constants.USER_NAME);
        mTvMemberIdSpeaking.setText(userName);
        MyTerminalFactory.getSDK().getVoipCallManager().acceptCall();
        MyTerminalFactory.getSDK().getVoipCallManager().addCallback(null,phoneCallback);
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

    private View.OnClickListener hungUpOnClickListener = v-> huangUp();

    private void huangUp(){
        mTvSpeakingToast.setText(getResources().getString(R.string.huang_up));
        MyTerminalFactory.getSDK().getIndividualCallManager().ceaseIndividualCall();
        MyTerminalFactory.getSDK().getVoipCallManager().hangUp();
        mIctvSpeakingTimeSpeaking.onStop();
        stopBusiness();
    }

    private PhoneCallback phoneCallback = new PhoneCallback(){

        @Override
        public void callConnected(LinphoneCall linphoneCall){
            super.callConnected(linphoneCall);
            MyTerminalFactory.getSDK().getIndividualCallManager().responseIndividualCall(true);
            mIctvSpeakingTimeSpeaking.onStart();
        }

        @Override
        public void callEnd(LinphoneCall linphoneCall){
            super.callEnd(linphoneCall);
            //电话接通之后挂断，还有主叫拨号时挂断
            huangUp();
        }
    };
}

package cn.vsx.vc.activity;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.ToastUtils;

import java.util.concurrent.CopyOnWriteArrayList;

import cn.vsx.hamster.common.MessageCategory;
import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallState;
import cn.vsx.hamster.terminalsdk.manager.terminal.TerminalState;
import cn.vsx.hamster.terminalsdk.model.CallRecord;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateFoldersAndGroupsHandler;
import cn.vsx.hamster.terminalsdk.tools.DateUtils;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.receiveHandle.ReceiveVoipCallEndHandler;
import cn.vsx.vc.receiveHandle.ReceiveVoipConnectedHandler;
import cn.vsx.vc.receiveHandle.ReceiveVoipErrorHandler;
import cn.vsx.vc.utils.ToastUtil;
import cn.vsx.vc.view.IndividualCallView;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.receiveHandler.ReceiveHeadSetPlugHandler;

public class VoipPhoneActivity extends BaseActivity{

    private int userId;
    private Member member;
    private String userName;
    private TextView memberNameRequest;
    private TextView memberPhoneRequest;
    private TextView requestCall;
    private LinearLayout llHangupRequest;
    private TextView memberNameSpeaking;
    private TextView memberPhoneSpeaking;
    private TextView SpeakingPrompt;
    private ImageView ivHangupSpeaking;
    private ImageView ivMicroMute;
    private ImageView ivHandFree;
    private RelativeLayout voipCallRequest;
    private LinearLayout voipCallSpeaking;
    private String phone;
    private String status="0";
    private int CALL_END=0;
    private int HANG_UP_SELF=1;
    private int CALL_ERROR=2;
    private Handler mHandler=new Handler();
    private IndividualCallView ictVspeakingTimeSpeaking;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_voip_phone;
    }

    @Override
    public void initView() {

        //将状态机至于正在个呼状态
        int code = TerminalFactory.getSDK().getTerminalStateManager().openFunction(TerminalState.INDIVIDUAL_CALLING, IndividualCallState.IDLE);
        if(code == BaseCommonCode.SUCCESS_CODE){
            if(TerminalFactory.getSDK().getIndividualCallManager().getIndividualCallStateMachine().moveToState(IndividualCallState.RINGING)){
                //将个呼状态机移动到响铃状态
                TerminalFactory.getSDK().getTerminalStateManager().moveToState(TerminalState.INDIVIDUAL_CALLING, IndividualCallState.RINGING);
            }
        }else {
            finish();
            return;
        }
        //请求界面
        voipCallRequest = findViewById(R.id.voip_call_request);
        memberNameRequest = findViewById(R.id.tv_member_name_request);//名字
        memberPhoneRequest = findViewById(R.id.tv_member_phone_request);//电话
        requestCall = findViewById(R.id.tv_requestCall);//通话状态
        llHangupRequest = findViewById(R.id.ll_hangup_request);//挂断


        //通话界面
        voipCallSpeaking = findViewById(R.id.voip_call_speaking);
        memberNameSpeaking = findViewById(R.id.tv_member_name_speaking);//名字
        memberPhoneSpeaking = findViewById(R.id.tv_member_phone_speaking);//电话
        SpeakingPrompt = findViewById(R.id.tv_speaking_prompt);//通话状态
        ivHangupSpeaking = findViewById(R.id.iv_hangup_speaking);//挂断
        ivMicroMute = findViewById(R.id.iv_micro_mute);//静音
        ivHandFree = findViewById(R.id.iv_hand_free);//免提
        ictVspeakingTimeSpeaking = findViewById(R.id.ICTV_speaking_time_speaking);//计时器

        voipCallRequest.setVisibility(View.VISIBLE);
        voipCallSpeaking.setVisibility(View.GONE);

        //打开默认听筒说话，
        setSpeakPhoneOn(ivHandFree,false);
        setMicrophoneMute(ivMicroMute,false);
    }

    @Override
    public void initListener(){
        llHangupRequest.setOnClickListener(v -> {
            ToastUtil.showToast(VoipPhoneActivity.this, getString(R.string.text_call_is_over));
            status = HANG_UP_SELF + "";
            MyTerminalFactory.getSDK().getVoipCallManager().hangUp();
            mHandler.postDelayed(() -> finish(),500);
        });
        ivHangupSpeaking.setOnClickListener(v -> {
            ToastUtil.showToast(VoipPhoneActivity.this, getString(R.string.text_call_is_over));
            status = CALL_END + "";
            MyTerminalFactory.getSDK().getVoipCallManager().hangUp();
            mHandler.postDelayed(() -> finish(),500);
        });
        ivMicroMute.setOnClickListener(v -> {
            boolean isMicrophoneMute = MyTerminalFactory.getSDK().getAudioProxy().isMicrophoneMute();
            setMicrophoneMute(ivMicroMute,!isMicrophoneMute);
        });
        ivHandFree.setOnClickListener(v -> {
            if(MyApplication.instance.headset){
                ptt.terminalsdk.tools.ToastUtil.showToast(this,getString(R.string.text_head_set_can_not_hand_free));
                //设置为耳机模式
                setSpeakPhoneOn(ivHandFree,false);
            }else{
                boolean isSpeakerphoneOn = MyTerminalFactory.getSDK().getAudioProxy().isSpeakerphoneOn();
                setSpeakPhoneOn(ivHandFree,!isSpeakerphoneOn);
            }
        });

        MyTerminalFactory.getSDK().registReceiveHandler(receiveVoipConnectedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveVoipCallEndHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveVoipErrorHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveHeadSetPlugHandler);
    }

    private ReceiveVoipConnectedHandler receiveVoipConnectedHandler = (linphoneCall)->{
        Log.d("VoipPhoneActivity", "电话接通");
        mHandler.post(new Runnable(){
            @Override
            public void run(){
                if(TerminalFactory.getSDK().getIndividualCallManager().getIndividualCallStateMachine().getCurrentState() == IndividualCallState.RINGING){
                    if (TerminalFactory.getSDK().getIndividualCallManager().getIndividualCallStateMachine().moveToState(IndividualCallState.SPEAKING)){
                        //将状态机移动到说话状态
                        TerminalFactory.getSDK().getTerminalStateManager().moveToState(TerminalState.INDIVIDUAL_CALLING, IndividualCallState.SPEAKING);
                        voipCallRequest.setVisibility(View.GONE);
                        voipCallSpeaking.setVisibility(View.VISIBLE);
                        ictVspeakingTimeSpeaking.onStart();
                    }
                }
            }
        });

    };

    private ReceiveVoipCallEndHandler receiveVoipCallEndHandler = (linphoneCall)->{
        //电话接通之后挂断，还有主叫拨号时挂断
        Log.e("VoipPhoneActivity", "电话挂断");
        TerminalFactory.getSDK().getIndividualCallManager().ceaseIndividualCall();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(status.equals("0")){
                    status="呼出 " + DateUtils.secToTime(linphoneCall.getCallLog().getCallDuration());
                }

                CallRecord callRecord = new CallRecord();
                callRecord.setCallId(linphoneCall.getCallLog().getCallId());
                callRecord.setMemberName(userName);
                callRecord.setPhone(phone);
                callRecord.setCallRecords(status);
                callRecord.setTime(DateUtils.getNowTime());
                String url = MyTerminalFactory.getSDK().getParam(Params.DOWNLOAD_VOIP_FILE_URL,"") +"?callId="+callRecord.getCallId();
                callRecord.setPath(url);

                final TerminalMessage terminalMessage = new TerminalMessage();
                terminalMessage.messageFromId=MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID,0);
                terminalMessage.messageFromName=MyTerminalFactory.getSDK().getParam(Params.MEMBER_NAME,"");
                terminalMessage.messageToId=userId;
                terminalMessage.messageToName=userName;
                terminalMessage.messagePath=url;
                terminalMessage.messageCategory= MessageCategory.MESSAGE_TO_GROUP.getCode();
                terminalMessage.messageType= MessageType.CALL_RECORD.getCode();
                terminalMessage.sendTime=System.currentTimeMillis();
                terminalMessage.messageBody=new JSONObject();
                terminalMessage.messageVersion=0;
                terminalMessage.resultCode=0;
//                new Thread(() -> {
//                    MyTerminalFactory.getSDK().getTerminalMessageManager().sendMessageToApplyServer(terminalMessage,"bbbb");  //转发通话记录给应用服务
//                }).start();



                CopyOnWriteArrayList<CallRecord> callRecords = MyTerminalFactory.getSDK().getSQLiteDBManager().getCallRecords();
                callRecords.add(callRecord);
                MyTerminalFactory.getSDK().getSQLiteDBManager().addCallRecord(callRecords);

                ictVspeakingTimeSpeaking.onStop();
                try{
                    finish();
                }catch (Exception e){
                    logger.error(e.toString());
                }


            }
        });
    };

    private ReceiveVoipErrorHandler receiveVoipErrorHandler = (linphoneCall)->{
        Log.e("VoipPhoneActivity", "error");
        TerminalFactory.getSDK().getIndividualCallManager().ceaseIndividualCall();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                status=CALL_ERROR+"";
                CallRecord callRecord = new CallRecord();
                callRecord.setCallId(linphoneCall.getCallLog().getCallId());
                callRecord.setMemberName(userName);
                callRecord.setPhone(phone);
                callRecord.setTime(DateUtils.getNowTime());
                callRecord.setCallRecords(status);

                CopyOnWriteArrayList<CallRecord> callRecords = MyTerminalFactory.getSDK().getSQLiteDBManager().getCallRecords();
                callRecords.add(callRecord);
                MyTerminalFactory.getSDK().getSQLiteDBManager().addCallRecord(callRecords);
                ictVspeakingTimeSpeaking.onStop();
                ToastUtil.showToast(VoipPhoneActivity.this,getString(R.string.other_stop_call));
                SystemClock.sleep(2000);
                try{
                    finish();
                }catch (Exception e){
                    logger.error(e.toString());
                }
            }
        });
    };

    /**
     * 设置是否可以打开扬声器
     */
    private ReceiveHeadSetPlugHandler receiveHeadSetPlugHandler = new ReceiveHeadSetPlugHandler(){
        @Override
        public void handler(boolean headset){
            mHandler.post(()-> {
                if(headset){
                    //设置为耳机模式
                    setSpeakPhoneOn(ivHandFree,false);
                }
            });
        }
    };

    @Override
    public void initData() {
        member =(Member) getIntent().getSerializableExtra("member");
        userId =member.id;
        userName = member.getName();
        phone = member.getPhone();
        if(TextUtils.isEmpty(phone)){
            ToastUtils.showShort(R.string.text_has_no_member_phone_number);
            finish();
            return;
        }

        memberNameRequest.setText(userName + "");
        memberPhoneRequest.setText(phone);
        requestCall.setText(R.string.text_in_call);

        memberNameSpeaking.setText(userName+"");
        memberPhoneSpeaking.setText(phone);
        SpeakingPrompt.setText(R.string.text_in_the_call);



        //打电话
        if(MyTerminalFactory.getSDK().getParam(Params.POLICE_STORE_APK,false)){
            logger.info("VOIPcallto: "+"0"+phone);
            MyTerminalFactory.getSDK().getVoipCallManager().audioCall("0"+phone);
        }else {
            logger.info("VOIPcallto: "+phone);
            MyTerminalFactory.getSDK().getVoipCallManager().audioCall(phone);
        }

//        PromptManager.getInstance().IndividualCallRequestRing();//提示音
        MyTerminalFactory.getSDK().getVoipCallManager().toggleSpeaker(false);//是否开启免提

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            finish();
        }
        return super.onKeyDown(keyCode, event);

    }

    @Override
    public void doOtherDestroy() {
        setMicrophoneMute(ivMicroMute,false);
        //打开默认听筒说话，
        setSpeakPhoneOn(ivHandFree,false);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveVoipConnectedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveVoipCallEndHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveVoipErrorHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveHeadSetPlugHandler);

        if (getSharedPreferences("CallRecord", MODE_PRIVATE).getBoolean("isFirstCall", true)) {//第一次打VOIP电话结束通知消息列表添加电话助手
            SharedPreferences.Editor editor = getSharedPreferences("CallRecord",
                    MODE_PRIVATE).edit();
            editor.putBoolean("isFirstCall", true);
            editor.commit();
            MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveUpdateFoldersAndGroupsHandler.class);
        }
        MyTerminalFactory.getSDK().getVoipCallManager().hangUp();
        mHandler.removeCallbacksAndMessages(null);
    }

}

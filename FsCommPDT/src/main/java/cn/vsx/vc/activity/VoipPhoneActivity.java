package cn.vsx.vc.activity;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.xuchongyang.easyphone.callback.PhoneCallback;

import org.linphone.core.LinphoneCall;

import java.util.concurrent.CopyOnWriteArrayList;

import cn.vsx.hamster.common.MessageCategory;
import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallState;
import cn.vsx.hamster.terminalsdk.manager.terminal.TerminalState;
import cn.vsx.hamster.terminalsdk.model.CallRecord;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateFoldersAndGroupsHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.utils.DataUtil;
import cn.vsx.vc.utils.ToastUtil;
import cn.vsx.vc.view.IndividualCallView;
import ptt.terminalsdk.context.MyTerminalFactory;

public class VoipPhoneActivity extends BaseActivity{

    private int userId;
    private Member member;
    private String userName;
    private TextView memberNameRequest;
    private TextView memberPhoneRequest;
    private TextView requestCall;
    private TextView tvRequestPrompt;
    private LinearLayout llHangupRequest;
    private TextView memberNameSpeaking;
    private TextView memberPhoneSpeaking;
    private TextView SpeakingPrompt;
    private View llHangupSpreaking;
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
        llHangupSpreaking = findViewById(R.id.ll_hangup_speaking);//挂断
        ictVspeakingTimeSpeaking = findViewById(R.id.ICTV_speaking_time_speaking);//计时器

        voipCallRequest.setVisibility(View.VISIBLE);
        voipCallSpeaking.setVisibility(View.GONE);


    }

    @Override
    public void initListener() {

        llHangupRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.showToast(VoipPhoneActivity.this,"通话已结束");
                status=HANG_UP_SELF+"";
                MyTerminalFactory.getSDK().getVoipCallManager().hangUp();

            }
        });

        llHangupSpreaking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.showToast(VoipPhoneActivity.this,"通话已结束");
                status=CALL_END+"";
                MyTerminalFactory.getSDK().getVoipCallManager().hangUp();
            }
        });

        MyTerminalFactory.getSDK().getVoipCallManager().addCallback(null, new PhoneCallback(){

            @Override
            public void outgoingInit(LinphoneCall linphoneCall){
                super.outgoingInit(linphoneCall);
            }

            @Override
            public void callConnected(LinphoneCall linphoneCall){
                super.callConnected(linphoneCall);
                Log.d("VoipPhoneActivity", "电话接通");
                TerminalFactory.getSDK().getTerminalStateManager().openFunction(TerminalState.INDIVIDUAL_CALLING, IndividualCallState.IDLE);//将状态机至于正在个呼状态
                voipCallRequest.setVisibility(View.GONE);
                voipCallSpeaking.setVisibility(View.VISIBLE);
                ictVspeakingTimeSpeaking.start();
            }

            @Override
            public void callEnd(final LinphoneCall linphoneCall){
                super.callEnd(linphoneCall);
                //电话接通之后挂断，还有主叫拨号时挂断
//                linphoneCall.getCallLog().getCallDuration()
                Log.e("VoipPhoneActivity", "电话挂断");
                TerminalFactory.getSDK().getTerminalStateManager().closeFunction(TerminalState.INDIVIDUAL_CALLING);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(status.equals("0")){
                            status="呼出 " + DataUtil.secToTime(linphoneCall.getCallLog().getCallDuration());
                        }

                        CallRecord callRecord = new CallRecord();
                        callRecord.setCallId(linphoneCall.getCallLog().getCallId());
                        callRecord.setMemberName(userName);
                        callRecord.setPhone(phone);
                        callRecord.setCallRecords(status);
                        callRecord.setTime(DataUtil.getNowTime());
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
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                MyTerminalFactory.getSDK().getTerminalMessageManager().sendMessageToApplyServer(terminalMessage,"bbbb");  //转发通话记录给应用服务
                            }
                        }).start();



                        CopyOnWriteArrayList<CallRecord> callRecords = MyTerminalFactory.getSDK().getSQLiteDBManager().getCallRecords();
                        callRecords.add(callRecord);
                        MyTerminalFactory.getSDK().getSQLiteDBManager().addCallRecord(callRecords);

                        ictVspeakingTimeSpeaking.stop();
                        try{
                            finish();
                        }catch (Exception e){
                            logger.error(e.toString());
                        }


                    }
                });
            }

            @Override
            public void error(final LinphoneCall linphoneCall){
                super.error(linphoneCall);
                //被叫收到来电，挂断电话，主叫会回调此方法
                Log.e("VoipPhoneActivity", "error");
                TerminalFactory.getSDK().getTerminalStateManager().closeFunction(TerminalState.INDIVIDUAL_CALLING);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() { ;
                        status=CALL_ERROR+"";
                        CallRecord callRecord = new CallRecord();
                        callRecord.setCallId(linphoneCall.getCallLog().getCallId());
                        callRecord.setMemberName(userName);
                        callRecord.setPhone(phone);
                        callRecord.setTime(DataUtil.getNowTime());
                        callRecord.setCallRecords(status);

                        CopyOnWriteArrayList<CallRecord> callRecords = MyTerminalFactory.getSDK().getSQLiteDBManager().getCallRecords();
                        callRecords.add(callRecord);
                        MyTerminalFactory.getSDK().getSQLiteDBManager().addCallRecord(callRecords);
                        ictVspeakingTimeSpeaking.stop();
                        ToastUtil.showToast(VoipPhoneActivity.this,"对方已挂断");
                        SystemClock.sleep(2000);
                        try{
                            finish();
                        }catch (Exception e){
                            logger.error(e.toString());
                        }
                    }
                });
            }

        });
    }

    @Override
    public void initData() {
        member =(Member) getIntent().getSerializableExtra("member");
        userId =member.id;
        userName = member.getName();
        phone = member.getPhone();


        memberNameRequest.setText(userName + "");
        memberPhoneRequest.setText(phone);
        requestCall.setText("呼叫中...");

        memberNameSpeaking.setText(userName+"");
        memberPhoneSpeaking.setText(phone);
        SpeakingPrompt.setText("通话中...");



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


        if (getSharedPreferences("CallRecord", MODE_PRIVATE).getBoolean("isFirstCall", true)) {//第一次打VOIP电话结束通知消息列表添加电话助手
            SharedPreferences.Editor editor = getSharedPreferences("CallRecord",
                    MODE_PRIVATE).edit();
            editor.putBoolean("isFirstCall", true);
            editor.commit();
            MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveUpdateFoldersAndGroupsHandler.class);
        }
        MyTerminalFactory.getSDK().getVoipCallManager().hangUp();
    }

}

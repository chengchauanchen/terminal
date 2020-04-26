package cn.vsx.vc.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.List;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.MemberChangeType;
import cn.vsx.hamster.common.ReceiveObjectMode;
import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveAnswerIndividualCallTimeoutHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCurrentGroupIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMultimediaMessageCompleteHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyIndividualCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyLivingIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyMemberChangeHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseStartLiveHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.dialog.ChooseDevicesDialog;
import cn.vsx.vc.jump.utils.AppKeyUtils;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.receiveHandle.ReceiverActivePushVideoHandler;
import cn.vsx.vc.receiveHandle.ReceiverCloseKeyBoardHandler;
import cn.vsx.vc.receiveHandle.ReceiverIndividualCallFromMsgItemHandler;
import cn.vsx.vc.receiveHandle.ReceiverRequestVideoHandler;
import cn.vsx.vc.record.AudioRecordButton;
import cn.vsx.vc.record.MediaManager;
import cn.vsx.vc.utils.CallPhoneUtil;
import cn.vsx.vc.utils.InputMethodUtil;
import cn.vsx.vc.utils.MyDataUtil;
import cn.vsx.vc.utils.ToastUtil;
import cn.vsx.vc.view.FixedRecyclerView;
import cn.vsx.vc.view.FunctionHidePlus;
import cn.vsx.vc.view.VolumeViewLayout;
import ptt.terminalsdk.context.MyTerminalFactory;


/**
 * Created by zckj on 2017/3/15.
 */
//人员聊天的界面(部分处理和变量存储在父类ChatBaseActivity中)
public class IndividualNewsActivity extends ChatBaseActivity implements View.OnClickListener {

    ImageView newsBarReturn;

    ImageView individualNewsPhone;

    ImageView individualNewsInfo;

    ImageView individual_news_help;

    VolumeViewLayout volumeViewLayout;

    TextView newsBarGroupName;

    Button ptt;

    //底部聊天发送和拓展面板的自定义控件
    FunctionHidePlus funcation;

    AudioRecordButton record;
    //    @Bind(R.id.rl_include_listview)
    //    RelativeLayout rl_include_listview;


    SwipeRefreshLayout sflCallList;

    //TODO 这个好像没有用？
    FixedRecyclerView groupCallList;


    FrameLayout fl_fragment_container;

    EditText groupCallNewsEt;

    ImageView group_call_news_keyboard;

    ImageView ivCall;
    private static int VOIP=0;
    private static int TELEPHONE=1;

    public static boolean isForeground = false;
    public static int mFromId;
    private int type;//跳转过来时Member类型
//    private Member member;

    public static void startCurrentActivity(Context context, int userId, String userName,int type) {
        Intent intent = new Intent(context, IndividualNewsActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("userName", userName);
        intent.putExtra("isGroup", false);
        intent.putExtra("type",type);
        context.startActivity(intent);
    }

    public static void startCurrentActivity(Context context, int userId, String userName,boolean newTask,int type) {
        Intent intent = new Intent(context, IndividualNewsActivity.class);
        if(newTask){
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtra("userId", userId);
        intent.putExtra("userName", userName);
        intent.putExtra("isGroup", false);
        intent.putExtra("type",type);
        context.startActivity(intent);
    }

    @Override
    public int getLayoutResId() {
        setSatusBarTransparent();
        return R.layout.activity_individual_news;
    }

    @Override
    public void initView() {
        ivCall = (ImageView) findViewById(R.id.iv_call);
        group_call_news_keyboard = (ImageView) findViewById(R.id.group_call_news_keyboard);
        groupCallNewsEt = (EditText) findViewById(R.id.group_call_news_et);
        fl_fragment_container = (FrameLayout) findViewById(R.id.fl_fragment_container);
        groupCallList = (FixedRecyclerView) findViewById(R.id.group_call_list);
        sflCallList = (SwipeRefreshLayout) findViewById(R.id.sfl_call_list);
        record = (AudioRecordButton) findViewById(R.id.btn_record);
        funcation = (FunctionHidePlus) findViewById(R.id.funcation);
        ptt = (Button) findViewById(R.id.btn_ptt);
        newsBarGroupName = (TextView) findViewById(R.id.tv_chat_name);
        volumeViewLayout = (VolumeViewLayout) findViewById(R.id.volume_layout);
        noNetWork = (LinearLayout) findViewById(R.id.noNetWork);
        tv_status = (TextView) findViewById(R.id.tv_status);
        individual_news_help = (ImageView) findViewById(R.id.individual_news_help);
        individualNewsInfo = (ImageView) findViewById(R.id.individual_news_info);
        individualNewsPhone = (ImageView) findViewById(R.id.individual_news_phone);
        newsBarReturn = (ImageView) findViewById(R.id.news_bar_return);
        sflCallList.setColorSchemeResources(R.color.colorPrimary);
        sflCallList.setProgressViewOffset(false, 0, (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources()
                        .getDisplayMetrics()));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        groupCallList.setLayoutManager(linearLayoutManager);
        super.newsBarGroupName = newsBarGroupName;
        super.sflCallList = sflCallList;
        super.groupCallList = groupCallList;
        super.fl_fragment_container = fl_fragment_container;
        super.groupCallNewsEt = groupCallNewsEt;
        super.funcation = funcation;
        super.ptt = ptt;
        setStatusBarColor();
        groupCallList.setVerticalScrollBarEnabled(false);
        record.setAudioPauseListener(() -> sendRecord());
    }

    @Override
    public void initListener() {
        newsBarReturn.setOnClickListener(this);
        individualNewsPhone.setOnClickListener(this);
        individualNewsInfo.setOnClickListener(this);
        individual_news_help.setOnClickListener(this);
        ivCall.setOnClickListener(this);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseStartLiveHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyIndividualCallIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyLivingIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyMemberChangeHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverIndividualCallFromMsgItemHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiverCloseKeyBoardHandler);

        super.initListener();
    }

    @Override
    public void initData() {
        super.initData();
        mFromId = userId;
        logger.info("userId：" + userId);
        type = getIntent().getIntExtra("type",0);
        funcation.setFunction(false, userId);
        funcation.setMemberFunction(type);
        //ivCall
        ivCall.setVisibility(((userId == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)))?View.GONE:View.VISIBLE);
        if(type == TerminalMemberType.TERMINAL_PDT.getCode()){
            ivCall.setVisibility(View.GONE);
        }else if(type == TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.getCode()){
            ivCall.setVisibility(View.GONE);
            individualNewsPhone.setVisibility(View.GONE);
        }
    }


    @Override
    public void postVideo() {
        if(type == 0){
            goToChooseDevices(ChooseDevicesDialog.TYPE_PUSH_LIVE);
        }else {
            showProgressDialog();
            TerminalFactory.getSDK().getThreadPool().execute(()-> {
                Account account = DataUtil.getAccountByMemberNo(userId,true);
                myHandler.post(this::dismissProgressDialog);
                Member member = null;
                if(account != null){
                    List<Member> members = account.getMembers();
                    for(Member next : members){
                        if(type != TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.getCode() &&
                                type != TerminalMemberType.TERMINAL_PDT.getCode() &&
                                next.getType() == type){
                            member = next;
                            break;
                        }
                    }
                    if(member !=null){
                        goToPushLive(member);
                    }
                }
            });
        }
    }

    @Override
    public void requestVideo() {
        if(type == 0){
            goToChooseDevices(ChooseDevicesDialog.TYPE_PULL_LIVE);
        }else {
            showProgressDialog();
            TerminalFactory.getSDK().getThreadPool().execute(()->{
                Account account = DataUtil.getAccountByMemberNo(userId,true);
                myHandler.post(this::dismissProgressDialog);
                Member member = null;
                if(account != null){
                    List<Member> members = account.getMembers();
                    for(Member next : members){
                        if(next.getType() == type){
                            member = next;
                            break;
                        }
                    }
                    if(member !=null){
                        goToPullLive(member);
                    }
                }
            });

        }
    }

    @Override
    public void doOtherDestroy() {
        handler.removeCallbacksAndMessages(null);
        record.cancel();
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseStartLiveHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverIndividualCallFromMsgItemHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyIndividualCallIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyLivingIncommingHandler);

        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyMemberChangeHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiverCloseKeyBoardHandler);
        if (volumeViewLayout != null) {
            volumeViewLayout.unRegistLintener();
        }
        MediaManager.release();
        MyTerminalFactory.getSDK().getTerminalMessageManager().stopMultimediaMessage();
        MyApplication.instance.isPlayVoice = false;
        super.doOtherDestroy();
    }


    protected void receiveIndividualCall() {
        funcation.hideKeyboard(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isForeground = true;

    }

    @Override
    protected void onPause() {
        super.onPause();
        isForeground = false;
        record.onPause();
    }

    /**
     * 请求个呼
     */
    private void activeIndividualCall(Member member) {
        MyApplication.instance.isCallState = true;
        boolean network = MyTerminalFactory.getSDK().hasNetwork();
        if (network) {
            if(member!=null){
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCurrentGroupIndividualCallHandler.class, member);
            }else {
                ToastUtil.showToast(IndividualNewsActivity.this,getString(R.string.text_get_member_info_fail));
            }
        } else {
            ToastUtil.showToast(this, getString(R.string.text_network_connection_abnormal_please_check_the_network));
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if(i == R.id.news_bar_return){
            onBackPressed();
        }else if(i == R.id.individual_news_phone){
            hideKey();
            if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_CALL_PRIVATE.name())){
                ToastUtil.showToast(this, getString(R.string.text_no_call_permission));
            }else{
                goToChooseDevices(ChooseDevicesDialog.TYPE_CALL_PRIVATE);
            }
        }else if(i == R.id.individual_news_info){
            Intent intent = new Intent(MyApplication.instance, UserInfoActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("userName", userName);
            startActivity(intent);
        }else if(i == R.id.individual_news_help){//                Intent intent = new Intent(this, HelpActivity.class);
            //                intent.setAction("5");
            //                startActivity(intent);
        }else if(i == R.id.iv_call){
            hideKey();
            goToChooseDevices(ChooseDevicesDialog.TYPE_CALL_PHONE);
        }
    }

    /**
     * 选择设备进行相应的操作
     * @param type
     */
    private void goToChooseDevices(int type){
        showProgressDialog();
        TerminalFactory.getSDK().getThreadPool().execute(() -> {
            Account account = DataUtil.getAccountByMemberNo(userId,true);
            myHandler.post(() -> {
                dismissProgressDialog();
                if(account == null){
                    ToastUtil.showToast(IndividualNewsActivity.this,getString(R.string.text_has_no_found_this_user));
                    return;
                }
                new ChooseDevicesDialog(this,type, account, (dialog,member) -> {
                    switch (type){
                        case ChooseDevicesDialog.TYPE_CALL_PRIVATE:
                            activeIndividualCall(member);
                            break;
                        case ChooseDevicesDialog.TYPE_CALL_PHONE:
                            goToCall(member);
                            break;
                        case ChooseDevicesDialog.TYPE_PULL_LIVE:
                            goToPullLive(member);
                            break;
                        case ChooseDevicesDialog.TYPE_PUSH_LIVE:
                            goToPushLive(member);
                            break;
                    }
                    dialog.dismiss();
                }).showDialog();
            });
        });
    }

    /**
     * 拨打电话
     */
    private void goToCall(Member member) {
        try{
            if(TextUtils.isEmpty(member.getPhone())){
                ToastUtils.showShort(R.string.text_has_no_member_phone_number);
                return;
            }
            if(member.getUniqueNo() == 0){
                //普通电话
                CallPhoneUtil.callPhone( IndividualNewsActivity.this, member.getPhone());
            }else{
                if(MyTerminalFactory.getSDK().getParam(Params.VOIP_SUCCESS,false)){
                    Intent intent = new Intent(IndividualNewsActivity.this, VoipPhoneActivity.class);
                    intent.putExtra("member",member);
                    startActivity(intent);
                }else {
                    ToastUtil.showToast(IndividualNewsActivity.this,getString(R.string.text_voip_regist_fail_please_check_server_configure));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 请求图像
     * @param member
     */
    private void goToPullLive(Member member) {
        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverRequestVideoHandler.class, member);
    }

    /**
     * 上报图像
     * @param member
     */
    private void goToPushLive(Member member) {
        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverActivePushVideoHandler.class,
                MyDataUtil.getPushInviteMemberData(member.getUniqueNo(), ReceiveObjectMode.MEMBER.toString()),false);
    }

    /**
     * 点击个呼条目
     */
    private ReceiverIndividualCallFromMsgItemHandler mReceiverIndividualCallFromMsgItemHandler = () -> {
        handler.post(() -> goToChooseDevices(ChooseDevicesDialog.TYPE_CALL_PRIVATE));
    };

    private Handler myHandler = new Handler(Looper.getMainLooper());

    /**更新所有成员列表*/
    private ReceiveNotifyMemberChangeHandler receiveNotifyMemberChangeHandler = new ReceiveNotifyMemberChangeHandler() {
        @Override
        public void handler(final MemberChangeType memberChangeType) {
            myHandler.post(() -> funcation.setFunction(false,userId));
        }
    };

    /**
     * 被动方组呼来了
     */
    private ReceiveGroupCallIncommingHandler receiveGroupCallIncommingHandler = (memberId, memberName, groupId, version, currentCallMode,uniqueNo) -> {
        logger.info("触发了被动方组呼来了receiveGroupCallIncommingHandler:" + currentCallMode);
        speakingId = groupId;
        speakingName = memberName;
        if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_LISTEN.name())) {
            ToastUtil.showToast(IndividualNewsActivity.this, getString(R.string.text_has_no_group_call_listener_authority));
        }
    };

    /**
     * 被动方个呼来了，选择接听或挂断
     */
    private ReceiveNotifyIndividualCallIncommingHandler receiveNotifyIndividualCallIncommingHandler = (mainMemberName, mainMemberId, individualCallType) -> handler.post(() -> {
        isReject=true;
        MediaManager.release();
        MyApplication.instance.isPlayVoice=true;
        TerminalFactory.getSDK().notifyReceiveHandler(ReceiveMultimediaMessageCompleteHandler.class, 0, "",new TerminalMessage());
    });

    /**
     * 被动方个呼答复超时
     */
    private ReceiveAnswerIndividualCallTimeoutHandler receiveAnswerIndividualCallTimeoutHandler = () -> {

    };

    /**收到别人请求我开启直播的通知**/
    private ReceiveNotifyLivingIncommingHandler receiveNotifyLivingIncommingHandler = (mainMemberName, mainMemberId, emergencyType) -> {
        logger.info("ReceiveNotifyLivingIncommingHandler:"+mainMemberName+"/"+mainMemberId);
        handler.post(() -> {
            isReject=true;
            MediaManager.release();
            MyApplication.instance.isPlayVoice=true;
            TerminalFactory.getSDK().notifyReceiveHandler(ReceiveMultimediaMessageCompleteHandler.class, 0, "",new TerminalMessage());
        });
    };

    private ReceiveUpdateConfigHandler receiveUpdateConfigHandler = new ReceiveUpdateConfigHandler() {
        @Override
        public void handler() {
            IndividualNewsActivity.this.runOnUiThread(() -> funcation.setFunction(false, userId));
        }
    };
    private ReceiveResponseStartLiveHandler receiveResponseStartLiveHandler = (resultCode, resultDesc, liveMemberId, liveUniqueNo) -> PromptManager.getInstance().stopRing();

    private ReceiverCloseKeyBoardHandler receiverCloseKeyBoardHandler = new ReceiverCloseKeyBoardHandler() {
        @Override
        public void handler() {
            logger.info("sjl_收到来自服务的关闭键盘handler");
            InputMethodUtil.hideInputMethod(IndividualNewsActivity.this, groupCallNewsEt);
        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_RECORD_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            }else {
                ToastUtils.showShort(R.string.no_record_perssion);
            }
        }else if(requestCode == CallPhoneUtil.PHONE_PERMISSIONS_REQUEST_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //同意，拨打电话
                CallPhoneUtil.callPhone( IndividualNewsActivity.this, TerminalFactory.getSDK().getParam(Params.TEMP_CALL_PHONE_NUMBER,""));
            }else {
                //不同意，提示
                ToastUtil.showToast(MyApplication.instance, getString(R.string.text_call_phone_not_open_call_is_unenabled));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        int flags = getIntent().getFlags();
        if(flags==Intent.FLAG_ACTIVITY_NEW_TASK){//另一个app近来的
            AppKeyUtils.setAppKey(null);//销毁时，将appKey置空
        }
    }
}

package cn.vsx.vc.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.IGotaKeyHandler;
import android.app.IGotaKeyMonitor;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import org.apache.http.util.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.Bind;
import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.CallMode;
import cn.vsx.hamster.common.MemberChangeType;
import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.UserType;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.SignalServerErrorCode;
import cn.vsx.hamster.errcode.module.TerminalErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.audio.IAudioPlayComplateHandler;
import cn.vsx.hamster.terminalsdk.manager.auth.AuthManagerTwo;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallState;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCallingCannotClickHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCeaseGroupCallConformationHander;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveChangeGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveForceChangeGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetGroupCurrentOnlineMemberListHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetGroupLivingListHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallCeasedIndicationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHistoryMultimediaFailHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMultimediaMessageCompleteHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyMemberChangeHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveOnLineStatusChangedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivePTTUpHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRequestGroupCallConformationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseGroupActiveHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSetMonitorGroupListHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateFoldersAndGroupsHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.receiveHandle.ReceiverActivePushVideoHandler;
import cn.vsx.vc.receiveHandle.ReceiverCloseKeyBoardHandler;
import cn.vsx.vc.receiveHandle.ReceiverReplayGroupChatVoiceHandler;
import cn.vsx.vc.receiveHandle.ReceiverShowTransponPopupHandler;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.DataUtil;
import cn.vsx.vc.utils.InputMethodUtil;
import cn.vsx.vc.utils.ToastUtil;
import cn.vsx.vc.view.FixedRecyclerView;
import cn.vsx.vc.view.FunctionHidePlus;
import cn.vsx.vc.view.RoundProgressBarWidthNumber;
import cn.vsx.vc.view.VolumeViewLayout;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.audio.CheckMyPermission;

import static cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState.LISTENING;

/**
 * Created by zckj on 2017/3/14.
 */

public class GroupCallNewsActivity extends ChatBaseActivity implements View.OnClickListener {
    @Bind(R.id.noNetWork)
    LinearLayout noNetWork;
    @Bind(R.id.news_bar_return)
    ImageView newsBarReturn;
    @Bind(R.id.group_live_history)
    ImageView groupLiveHistory;
    @Bind(R.id.group_call_activity_member_info)
    ImageView groupCallActivityMemberInfo;
    @Bind(R.id.tv_speaker)
    TextView tv_speaker;
    @Bind(R.id.group_call_time_progress)
    RoundProgressBarWidthNumber groupCallTimeProgress;
    @Bind(R.id.progress_group_call)
    RelativeLayout progressGroupCall;
    @Bind(R.id.ll_individual_call_come)
    LinearLayout ll_individual_call_come;
    @Bind(R.id.ll_living)
    LinearLayout ll_living;
    @Bind(R.id.tv_living_number)
    TextView tv_living_number;
    @Bind(R.id.ll_speaker)
    LinearLayout ll_speaker;
    @Bind(R.id.group_call_activity_help)
    ImageView group_call_activity_help;
    @Bind(R.id.tv_pre_speak)
    TextView tv_pre_speak;

    @Bind(R.id.tv_chat_name)
    TextView newsBarGroupName;
    @Bind(R.id.btn_ptt)
    Button ptt;

    @Bind(R.id.sfl_call_list)
    SwipeRefreshLayout sflCallList;
    @Bind(R.id.group_call_list)
    FixedRecyclerView groupCallList;
    @Bind(R.id.fl_fragment_container)
    FrameLayout fl_fragment_container;
    @Bind(R.id.group_call_news_et)
    EditText groupCallNewsEt;
    @Bind(R.id.funcation)
    FunctionHidePlus funcation;
    @Bind(R.id.tv_scan)
    TextView tv_scan;
    @Bind(R.id.img_scan)
    ImageView img_scan;
    @Bind(R.id.volume_layout)
    VolumeViewLayout volumeViewLayout;
    @Bind(R.id.iv_monitor)
    ImageView iv_monitor;
    //组Id
    public static int mGroupId;
    //获取组内正在上报人数的间隔时间
    private static final long GET_GROUP_LIVING_INTERVAL_TIME = 20*1000;
    @SuppressWarnings("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int progress = groupCallTimeProgress.getProgress();
            groupCallTimeProgress.setProgress(--progress);
            if (progress < 0) {
                this.removeMessages(1);
            }
            this.sendEmptyMessageDelayed(1, 100);
        }
    };

    public static boolean isForeground = false;
    private IGotaKeyMonitor keyMointor;
    private IGotaKeyHandler gotaKeyHandler;

    public static void startCurrentActivity(Context context, int userId, String userName, int speakingId, String speakingName) {
        Intent intent = new Intent(context, GroupCallNewsActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("userName", userName);
        intent.putExtra("isGroup", true);
        intent.putExtra("speakingId", speakingId);
        intent.putExtra("speakingName", speakingName);
        context.startActivity(intent);
    }

    @Override
    public int getLayoutResId() {
        setSatusBarTransparent();
        return R.layout.activity_group_call_news;
    }

    @SuppressLint("WrongConstant")
    @Override
    public void initView() {
        sflCallList.setColorSchemeResources(R.color.colorPrimary);
        sflCallList.setProgressViewOffset(false, 0, (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources()
                        .getDisplayMetrics()));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        groupCallList.setLayoutManager(linearLayoutManager);
//        super.rl_include_listview = rlIncludeListview;
        super.newsBarGroupName = newsBarGroupName;
        super.sflCallList = sflCallList;
        super.groupCallList = groupCallList;
        super.fl_fragment_container = fl_fragment_container;
        super.groupCallNewsEt = groupCallNewsEt;
        super.funcation = funcation;
        super.ptt = ptt;
        setStatusBarColor();
        allViewDefault();
        funcation.setPttOnTouchLitener(new FunctionHidePlus.PttOnTouchLitener() {
            @Override
            public void up() {
                mHandler.removeMessages(1);
                groupCallTimeProgress.setProgress(605);
                setViewVisibility(progressGroupCall, View.GONE);
            }

            @Override
            public void down() {
                setViewVisibility(progressGroupCall, View.VISIBLE);
                mHandler.sendEmptyMessage(1);
            }
        });

        //GH880手机按键服务
        keyMointor =(IGotaKeyMonitor)getSystemService("gotakeymonitor");
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
        if(null != keyMointor){
            try{
                keyMointor.setHandler(gotaKeyHandler);
            }catch(RemoteException e){
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        stopRecord();
    }

    public void initListener() {
        newsBarReturn.setOnClickListener(this);
        groupLiveHistory.setOnClickListener(this);
        groupCallActivityMemberInfo.setOnClickListener(this);

        group_call_activity_help.setOnClickListener(this);
        iv_monitor.setOnClickListener(this);
        ptt.setOnTouchListener(mOnTouchListener);
        ll_living.setOnClickListener(this);
        if(keyMointor !=null){
            try{
                gotaKeyHandler = keyMointor.setHandler(new GotaKeHandler());
            }catch (Exception e){
              e.printStackTrace();
            }
        }

        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiverCloseKeyBoardHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveSetMonitorGroupListHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyMemberChangeHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveRequestGroupCallConformationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveCeaseGroupCallConformationHander);
        MyTerminalFactory.getSDK().registReceiveHandler(receivePTTUpHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveHistoryMultimediaFailHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveMultimediaMessageCompleteHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mmReceiveGetGroupCurrentOnlineMemberListHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateFoldersAndGroupsHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveOnLineStatusChangedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveChangeGroupHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveForceChangeGroupHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseGroupActiveHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGetGroupLivingListHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverReplayGroupChatVoiceHandler);
        super.initListener();
    }


    public void initData() {
        super.initData();
        mGroupId = userId;
        setIvMonitorDrawable();
        funcation.setFunction(true, userId);
        //获取组内在线人数
        MyTerminalFactory.getSDK().getGroupManager().getGroupCurrentOnlineMemberList(userId, false);
        //获取组内正在上报的人数
        getGroupLivingList();
    }

    @Override
    public void doOtherDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiverCloseKeyBoardHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveSetMonitorGroupListHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyMemberChangeHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveRequestGroupCallConformationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveOnLineStatusChangedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveCeaseGroupCallConformationHander);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receivePTTUpHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveHistoryMultimediaFailHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMultimediaMessageCompleteHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mmReceiveGetGroupCurrentOnlineMemberListHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateFoldersAndGroupsHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveChangeGroupHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveForceChangeGroupHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseGroupActiveHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetGroupLivingListHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverReplayGroupChatVoiceHandler);
        if (volumeViewLayout != null) {
            volumeViewLayout.unRegistLintener();
        }
        super.doOtherDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.news_bar_return:
                onBackPressed();
                break;
            case R.id.group_live_history:
                goToVideoLiveList(false);
                break;
            case R.id.group_call_activity_member_info:
                if (DataUtil.isExistGroup(userId)) {
                    Intent intent = new Intent(MyApplication.instance, GroupMemberActivity.class);
                    intent.putExtra("groupId", userId);
                    intent.putExtra("groupName", userName);
                    startActivity(intent);
                } else {
                    ToastUtil.showToast(this, getString(R.string.text_member_has_no_authority_in_this_group));
                }
                break;
            case R.id.group_call_activity_help:
//                Intent intent = new Intent(GroupCallNewsActivity.this, HelpActivity.class);
//                intent.setAction("5");
//                startActivity(intent);
                break;
            case R.id.ll_living:
                //点击跳转到正在上报的列表
//                ToastUtil.showToast(GroupCallNewsActivity.this,"点击跳转到正在上报的列表");
                goToVideoLiveList(true);
                break;


            case R.id.iv_monitor:
                List<Integer> monitorGroups = new ArrayList<>();
                monitorGroups.add(userId);
                if(TerminalFactory.getSDK().getConfigManager().getMonitorGroupNo().contains(userId)){
                    TerminalFactory.getSDK().getGroupManager().setMonitorGroup(monitorGroups,false);
                }else {
                    TerminalFactory.getSDK().getGroupManager().setMonitorGroup(monitorGroups,true);
                }
                break;
        }
    }

    private void setIvMonitorDrawable(){
        if(TerminalFactory.getSDK().getConfigManager().getMonitorGroupNo().contains(userId)){
            iv_monitor.setImageResource(R.drawable.monitor_open);
        }else {
            iv_monitor.setImageResource(R.drawable.monitor_close);
        }
    }

    private void stateView() {
        switch (TerminalFactory.getSDK().getGroupCallManager().getGroupCallSpeakStateMachine().getCurrentState()) {
            case IDLE:
                change2Silence();
                break;
            case GRANTING:
                change2PreSpeaking();
                break;
            case GRANTED:
                change2Speaking();
                break;
            case WAITING:
                change2Waiting();
                break;
            default:
                break;
        }
        switch (TerminalFactory.getSDK().getGroupCallManager().getGroupCallListenStateMachine().getCurrentState()) {
            case IDLE:
                change2Silence();
                break;
            case LISTENING:
                change2Listening();
                break;
            default:
                break;
        }
    }

    private void pttDownDoThing() {
        logger.info("pttDownDoThing执行了 isPttPress：" + MyApplication.instance.isPttPress);
        if (!CheckMyPermission.selfPermissionGranted(GroupCallNewsActivity.this, Manifest.permission.RECORD_AUDIO)) {//没有录音权限
            CheckMyPermission.permissionPrompt(GroupCallNewsActivity.this, Manifest.permission.RECORD_AUDIO);
            return;
        }
        //没有组呼权限
        if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_TALK.name())) {
            ToastUtil.showToast(this, getString(R.string.text_has_no_group_call_authority));
            return;
        }

        int resultCode = MyTerminalFactory.getSDK().getGroupCallManager().requestGroupCall("",mGroupId);

        if (resultCode == BaseCommonCode.SUCCESS_CODE) {//允许组呼了
            if (!MyTerminalFactory.getSDK().getAudioProxy().isSpeakerphoneOn()) {//打开扬声器
                MyTerminalFactory.getSDK().getAudioProxy().setSpeakerphoneOn(true);
            }
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCallingCannotClickHandler.class, true);
            MyApplication.instance.isPttPress = true;
            change2PreSpeaking();
        } else if (resultCode == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode()) {
            MyApplication.instance.isPttPress = true;
            change2Waiting();
        } else {//组呼失败的提示
            ToastUtil.groupCallFailToast(GroupCallNewsActivity.this, resultCode);
        }

    }

    private void change2Waiting() {
        ptt.setBackgroundResource(R.drawable.shape_news_ptt_pre);
        ptt.setEnabled(true);
    }

    private void change2PreSpeaking() {
        ptt.setBackgroundResource(R.drawable.shape_news_ptt_pre);
        ptt.setText("PTT");
        TextViewCompat.setTextAppearance(ptt, R.style.white);
        ptt.setEnabled(true);
        MyApplication.instance.isPttPress = true;
        if (MyApplication.instance.getGroupListenenState() == GroupCallListenState.LISTENING) {
            return;
        }
        allViewDefault();
        if (MyApplication.instance.getGroupListenenState() != GroupCallListenState.LISTENING) {
            tv_pre_speak.setVisibility(View.VISIBLE);
        } else {
            tv_pre_speak.setVisibility(View.GONE);
        }
    }

    private void change2Speaking() {

        ptt.setText("PTT");
        TextViewCompat.setTextAppearance(ptt, R.style.white);
        ptt.setBackgroundResource(R.drawable.shape_news_ptt_speak);
        logger.info("主界面，ptt被禁 ？  isClickVolumeToCall：" + MyApplication.instance.isClickVolumeToCall);
        ptt.setEnabled(!MyApplication.instance.isClickVolumeToCall);

        allViewDefault();
        progressGroupCall.setVisibility(View.VISIBLE);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
//                groupCallTimeProgress = new RoundProgressBarWidthNumber(context);
                int progress = groupCallTimeProgress.getProgress();
                groupCallTimeProgress.setProgress(--progress);
                if (progress < 0) {
                    this.removeMessages(1);
                }
                sendEmptyMessageDelayed(1, 100);
            }
        };
        mHandler.sendEmptyMessage(1);
    }

    private void pttUpDoThing() {
        logger.info("pttUpDoThing执行了 isPttPress：" + MyApplication.instance.isPttPress);
        if (MyApplication.instance.isPttPress) {
            MyApplication.instance.isPttPress = false;

            //没有组呼权限
            if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_TALK.name())) {
                return;
            }

            if (MyApplication.instance.getGroupListenenState() == GroupCallListenState.LISTENING) {
                change2Listening();
            } else {
                change2Silence();
            }
            MyTerminalFactory.getSDK().getGroupCallManager().ceaseGroupCall();
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCallingCannotClickHandler.class, false);
        }
    }

    private void change2Silence() {
        if (ptt != null) {

            ptt.setBackgroundResource(R.drawable.shape_news_ptt_listen);
            ptt.setText(R.string.text_ptt);
            TextViewCompat.setTextAppearance(ptt, R.style.funcation_top_btn_text);
            ptt.setEnabled(true);
        }

        if (MyApplication.instance.getGroupListenenState() == GroupCallListenState.LISTENING) {
            return;
        }
        allViewDefault();

        if (!TextUtils.isEmpty(MyTerminalFactory.getSDK().getParam(Params.CURRENT_SPEAKER))) {
            ll_speaker.setVisibility(View.GONE);
            tv_speaker.setText(MyTerminalFactory.getSDK().getParam(Params.CURRENT_SPEAKER, ""));
        }
    }

    private void change2Listening() {

        ptt.setBackgroundResource(R.drawable.shape_news_ptt_wait);
        ptt.setText(R.string.button_press_to_line_up);
        TextViewCompat.setTextAppearance(ptt, R.style.ptt_gray);
        logger.info("主界面，ptt被禁了  isPttPress：" + MyApplication.instance.isPttPress);
        if (MyApplication.instance.isPttPress) {
            pttUpDoThing();
        }

        allViewDefault();
        if (MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_LISTEN.name())) {//没有组呼听的功能不显示通知
//            if (speakingId != MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)) {
//                //设置说话人名字,在组呼来的handler中设置
//                ll_speaker.setVisibility(View.VISIBLE);
//                tv_scan.setVisibility(View.VISIBLE);
//                tv_scan.setText(DataUtil.getGroupByGroupNo(speakingId).name);
//                img_scan.setVisibility(View.VISIBLE);
//                tv_speaker.setText(speakingName + "");
//                ptt.setBackgroundResource(R.drawable.shape_news_ptt_wait);
//                ptt.setText(R.string.button_change_to_this_group_and_speak);
//                TextViewCompat.setTextAppearance(ptt, R.style.ptt_gray);
//
//            } else {
                logger.info("sjl_当前组");
                ll_speaker.setVisibility(View.VISIBLE);
                tv_scan.setVisibility(View.GONE);
                img_scan.setVisibility(View.GONE);
                tv_speaker.setText(speakingName + "");
                ptt.setBackgroundResource(R.drawable.shape_news_ptt_wait);
                ptt.setText(R.string.button_press_to_line_up);
                TextViewCompat.setTextAppearance(ptt, R.style.ptt_gray);
//            }
        }
    }

    private void change2Forbid() {//禁止组呼，不是遥毙

        ptt.setBackgroundResource(R.drawable.shape_news_ptt_wait);
        ptt.setText(R.string.button_press_to_speak);
        TextViewCompat.setTextAppearance(ptt, R.style.function_wait_text);
        logger.info("主界面，ptt被禁了  isPttPress：" + MyApplication.instance.isPttPress);
        ptt.setEnabled(false);
        if (MyApplication.instance.isPttPress) {
            pttUpDoThing();
        }
    }

    private void allViewDefault() {
        mHandler.removeMessages(1);
        if (tv_pre_speak == null)
            return;
        tv_pre_speak.setVisibility(View.GONE);
        progressGroupCall.setVisibility(View.GONE);
        groupCallTimeProgress.setProgress(605);
        ll_speaker.setVisibility(View.GONE);
    }

    /**
     * 停止播放组呼录音
     */
    public void stopRecord() {
        if (MyApplication.instance.isPlayVoice) {
            MyTerminalFactory.getSDK().getTerminalMessageManager().stopMultimediaMessage();
        }
    }

    /**
     * 主动组呼时屏蔽其他按键
     ***/
    private void setViewEnable(boolean isEnable) {
        newsBarReturn.setEnabled(isEnable);
        groupCallActivityMemberInfo.setEnabled(isEnable);
        funcation.setViewEnable(isEnable);
        groupCallList.setEnabled(isEnable);
        temporaryAdapter.setEnable(isEnable);
    }


    /**
     * ptt按钮触摸监听
     */
    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {


            if (MyTerminalFactory.getSDK().getAuthManagerTwo().getLoginStatus() != AuthManagerTwo.ONLINE) {
                ToastUtil.showToast(GroupCallNewsActivity.this, GroupCallNewsActivity.this.getResources().getString(R.string.net_work_disconnect));
                return true;
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    logger.info("ACTION_DOWN，ptt按钮按下，开始组呼：" + MyApplication.instance.folatWindowPress + MyApplication.instance.volumePress);
                    if (!MyApplication.instance.folatWindowPress && !MyApplication.instance.volumePress) {
                        pttDownDoThing();
                    }

                    break;
                case MotionEvent.ACTION_MOVE:
                    if (event.getX() + v.getWidth() / 4 < 0 || event.getX() - v.getWidth() * 1.25 > 0 ||
                            event.getY() + v.getHeight() / 8 < 0 || event.getY() - v.getHeight() * 1.125 > 0) {
                        logger.info("ACTION_MOVE，ptt按钮移动，停止组呼：" + MyApplication.instance.isPttPress);

                        if (MyApplication.instance.isPttPress) {
                            pttUpDoThing();
                        }
                    }
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    logger.info("ACTION_UP，ACTION_CANCEL，ptt按钮抬起，停止组呼：" + MyApplication.instance.isPttPress);
                    if (MyApplication.instance.isPttPress) {
                        pttUpDoThing();
                    }
                    break;

                default:
                    break;
            }
            return true;
        }
    };


/**===============================================================================================handler====================================================================================**/

    private ReceiveSetMonitorGroupListHandler receiveSetMonitorGroupListHandler = new ReceiveSetMonitorGroupListHandler(){
        @Override
        public void handler(int errorCode, String errorDesc){
            if(errorCode == BaseCommonCode.SUCCESS_CODE){
                setIvMonitorDrawable();
            }else {
                ToastUtil.showToast(GroupCallNewsActivity.this,errorDesc);
            }
        }
    };

    /**
     * 获取组内正在上报人数
     */
    private void getGroupLivingList(){
        long groupUniqueNo = MyTerminalFactory.getSDK().getTerminalMessageManager().getGroupUniqueNo(userId);
        MyTerminalFactory.getSDK().getGroupManager().getGroupLivingList(String.valueOf(groupUniqueNo),true);
    }

    @Override
    public void postVideo() {
//        ToastUtil.showToast(GroupCallNewsActivity.this,"组内上报");
        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverActivePushVideoHandler.class, userId,true);
        //发送组内观看视频消息

    }

    /**
     * 跳转到组内上报列表
     * @param isGroupVideoLiving 是否是正在上报列表
     */
    private void goToVideoLiveList(boolean isGroupVideoLiving){
        Intent intent = new Intent(GroupCallNewsActivity.this, GroupVideoLiveListActivity.class);
        intent.putExtra(Constants.IS_GROUP_VIDEO_LIVING, isGroupVideoLiving);
        intent.putExtra(Constants.GROUP_ID, userId);
        startActivity(intent);
    }

    /**===============================================================================================handler====================================================================================**/
    /**
     * 是否禁止组呼
     */
    private ReceiveNotifyMemberChangeHandler receiveNotifyMemberChangeHandler = memberChangeType -> {

        if (memberChangeType == MemberChangeType.MEMBER_ACTIVE_GROUP_CALL) {
            mHandler.post(() -> change2Silence());

        } else if (memberChangeType == MemberChangeType.MEMBER_PROHIBIT_GROUP_CALL) {
            mHandler.post(() -> change2Forbid());
        }
    };

    /**
     * 主动方请求组呼的消息
     */
    private ReceiveRequestGroupCallConformationHandler receiveRequestGroupCallConformationHandler = (methodResult, resultDesc) -> {
        logger.info("PTTViewPager触发了请求组呼的响应methodResult:" + methodResult);
        if (MyTerminalFactory.getSDK().getGroupCallManager().getCurrentCallMode() == CallMode.GENERAL_CALL_MODE) {
            logger.error("isPttPress值为" + MyApplication.instance.isPttPress);
            if (MyApplication.instance.isPttPress) {
                if (methodResult == 0) {//请求成功，开始组呼
                    mHandler.post(() -> {
                        change2Speaking();
                        MyTerminalFactory.getSDK().putParam(Params.CURRENT_SPEAKER, "");
                        setViewEnable(false);
                    });
                } else if (methodResult == SignalServerErrorCode.CANT_SPEAK_IN_GROUP.getErrorCode()) {//只听组
                    mHandler.post(() -> ToastUtil.showToast(GroupCallNewsActivity.this, getString(R.string.text_current_group_only_listener_can_not_speak)));
                } else if (methodResult == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode()) {//请求等待中
                    mHandler.post(() -> change2Waiting());
                } else {//请求失败
                    mHandler.post(() -> {
                        if (MyApplication.instance.getGroupListenenState() != GroupCallListenState.LISTENING) {
                            change2Silence();
                        } else {
                            change2Listening();
                        }
                    });
                }
            }
        }
    };

    /**
     * 网络连接状态
     */
    private ReceiveOnLineStatusChangedHandler receiveOnLineStatusChangedHandler = new ReceiveOnLineStatusChangedHandler() {

        @Override
        public void handler(final boolean connected) {
            logger.info("组会话页面收到服务是否连接的通知" + connected);
            GroupCallNewsActivity.this.runOnUiThread(() -> {
                if (!connected) {
                    noNetWork.setVisibility(View.VISIBLE);
                } else {
                    noNetWork.setVisibility(View.GONE);
                }
            });
        }
    };

    /**
     * 被动方组呼来了
     */
    private ReceiveGroupCallIncommingHandler receiveGroupCallIncommingHandler = (memberId, memberName, groupId, version, currentCallMode) -> {
        logger.info("触发了被动方组呼来了receiveGroupCallIncommingHandler:" + currentCallMode);
        speakingId = groupId;
        speakingName = memberName;
        if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_LISTEN.name())) {
            ToastUtil.showToast(GroupCallNewsActivity.this, getString(R.string.text_has_no_group_call_listener_authority));
        }

        if (currentCallMode == CallMode.GENERAL_CALL_MODE) {
            mHandler.post(() -> {
                if (MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.GRANTING
                        || MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.WAITING) {
                    change2Waiting();
                } else if (MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.GRANTED) {
                    //什么都不用做
                } else {
                    change2Listening();
                }
            });
        }
    };

    /**
     * 被动方组呼停止
     */
    private ReceiveGroupCallCeasedIndicationHandler receiveGroupCallCeasedIndicationHandler = reasonCode -> {
        logger.info("触发了被动方组呼停止receiveGroupCallCeasedIndicationHandler");
//            groupScanId = 0;
        mHandler.post(() -> {
            if (MyTerminalFactory.getSDK().getGroupCallManager().getCurrentCallMode() == CallMode.GENERAL_CALL_MODE) {
                if (MyApplication.instance.isPttPress && MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.IDLE) {
                    change2Speaking();
                }
                if (MyApplication.instance.getGroupSpeakState() != GroupCallSpeakState.GRANTING && MyApplication.instance.getGroupSpeakState() != GroupCallSpeakState.WAITING && MyApplication.instance.getGroupSpeakState() != GroupCallSpeakState.GRANTED) {
                    change2Silence();
                }
            }
        });
    };

    /**
     * 主动方停止组呼的消息
     */
    private ReceiveCeaseGroupCallConformationHander receiveCeaseGroupCallConformationHander = (resultCode, resultDesc) -> {
        logger.info("主动方停止组呼的消息ReceiveCeaseGroupCallConformationHander");
        mHandler.post(() -> {
            if (MyTerminalFactory.getSDK().getGroupCallManager().getCurrentCallMode() == CallMode.GENERAL_CALL_MODE) {
                if (MyApplication.instance.getGroupListenenState() == GroupCallListenState.LISTENING) {
                    change2Listening();
                } else {
                    change2Silence();
                }
            }
            setViewEnable(true);
        });
    };

    private ReceivePTTUpHandler receivePTTUpHandler = () -> mHandler.post(() -> {
        MyApplication.instance.isClickVolumeToCall = false;
        if (MyApplication.instance.getGroupListenenState() == LISTENING) {
            change2Listening();
        } else {
            change2Silence();
        }

    });

    private int mposition = -1;
    private int lastPosition = -1;
    private boolean isSameItem = true;
    private Handler myHandler = new Handler();
    private ExecutorService executorService = Executors.newFixedThreadPool(1);
    /**
     * 点击消息的组呼条目，播放组呼录音
     **/
    private ReceiverReplayGroupChatVoiceHandler mReceiverReplayGroupChatVoiceHandler = new ReceiverReplayGroupChatVoiceHandler() {
        @Override
        public void handler(int postion) {
            mposition = postion;

            myHandler.post(() -> {
                if (MyApplication.instance.getIndividualState() == IndividualCallState.IDLE &&
                        MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.IDLE &&
                        MyApplication.instance.getGroupListenenState() == GroupCallListenState.IDLE) {//不是在组呼也不是在个呼中，可以播放录音

                    if (lastPosition == mposition) {//点击同一个条目
                        if (MyApplication.instance.isPlayVoice) {
                            MyTerminalFactory.getSDK().getTerminalMessageManager().stopMultimediaMessage();
                        } else {
                            executorService.execute(() -> {
                                if (mposition < chatMessageList.size() && mposition >= 0) {
                                    try {
                                        MyTerminalFactory.getSDK().getTerminalMessageManager().playMultimediaMessage(chatMessageList.get(mposition), audioPlayComplateHandler);
                                    } catch (IndexOutOfBoundsException e) {
                                        logger.warn("mPosition出现异常，其中mposition=" + mposition + "，mTerminalMessageList.size()=" + chatMessageList.size(), e);
                                    }
                                }
                            });
                        }
                    } else {//点击不同条目

                        if (MyApplication.instance.isPlayVoice) {
                            MyTerminalFactory.getSDK().getTerminalMessageManager().stopMultimediaMessage();

                        }

                        //播放当前的
                        executorService.execute(() -> {
                            if (mposition < chatMessageList.size() && mposition >= 0) {
                                try {
//                                            logger.error("当前播放的条目是：" + mposition);
                                    MyTerminalFactory.getSDK().getTerminalMessageManager().playMultimediaMessage(chatMessageList.get(mposition), audioPlayComplateHandler);
                                } catch (IndexOutOfBoundsException e) {
                                    logger.warn("mPosition出现异常，其中mposition=" + mposition + "，mTerminalMessageList.size()=" + chatMessageList.size(), e);
                                }
                            }
                        });
                    }

                } else {
                    ToastUtil.showToast(GroupCallNewsActivity.this, getString(R.string.text_can_not_play_recording_now));
                }
            });
        }
    };

    /**
     * 录音播放完成的消息
     */
    private IAudioPlayComplateHandler audioPlayComplateHandler = () -> myHandler.post(() -> {
        MyApplication.instance.isPlayVoice = false;
        isSameItem = true;
//                    logger.error("录音播放完成的消息：" + chatMessageList.get(mposition).toString());
        temporaryAdapter.refreshPersonContactsAdapter(mposition, chatMessageList, MyApplication.instance.isPlayVoice, isSameItem);
        setSmoothScrollToPosition(mposition);
//        temporaryAdapter.notifyDataSetChanged();

        autoPlay(mposition + 1);

    });

    //自动播放下一条语音
    private void autoPlay(int index) {
//        logger.debug("自动播放:第"+index+"条");
        //不是最后一条消息，自动播放
        if (index < chatMessageList.size()) {
            //不是语音消息跳过执行下一条
            if (chatMessageList.get(index).messageType != MessageType.AUDIO.getCode()&&chatMessageList.get(index).messageType!=MessageType.GROUP_CALL.getCode()) {
                index = index + 1;
                autoPlay(index);
            } else {
                if (chatMessageList.get(index).messageBody.containsKey(JsonParam.UNREAD) &&
                        chatMessageList.get(index).messageBody.getBooleanValue(JsonParam.UNREAD)
                        && MyTerminalFactory.getSDK().getParam(Params.IS_PLAY_END, false)) {
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverReplayGroupChatVoiceHandler.class, index);
                }
            }
        }else {
            logger.debug("最后一条消息已播放完成");
        }


    }

    /**
     * 音频播放失败
     **/
    private ReceiveHistoryMultimediaFailHandler receiveHistoryMultimediaFailHandler = resultCode -> {
        if (resultCode == TerminalErrorCode.STOP_PLAY_RECORD.getErrorCode()) {
            MyApplication.instance.isPlayVoice = false;
            isSameItem = true;
            temporaryAdapter.refreshPersonContactsAdapter(mposition, chatMessageList, false, true);
//            temporaryAdapter.notifyDataSetChanged();
        } else {
            logger.info("音频播放失败了！！errorCode=" + resultCode);
            ToastUtil.showToast(GroupCallNewsActivity.this, getString(R.string.text_play_recorder_fail_has_no_get_recorder_data_please_try_later));
        }
    };

    /**
     * 开始播放或停止播放的回调
     */
    private ReceiveMultimediaMessageCompleteHandler receiveMultimediaMessageCompleteHandler = (resultCode, resultDes) -> {
        logger.error("开始播放或者停止播放的回调");
        myHandler.post(() -> {
            if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                if (lastPosition == mposition) {//点击同一个条目
                    isSameItem = true;
                    MyApplication.instance.isPlayVoice = !MyApplication.instance.isPlayVoice;
                } else {//点击不同条目
                    isSameItem = false;
                    MyApplication.instance.isPlayVoice = true;
                }
                Collections.sort(chatMessageList);
                if (temporaryAdapter != null) {
                    temporaryAdapter.refreshPersonContactsAdapter(mposition, chatMessageList, MyApplication.instance.isPlayVoice, isSameItem);
//                    temporaryAdapter.notifyDataSetChanged();
                }
                lastPosition = mposition;
            } else {
                logger.info("开始播放或停止播放的回调" + resultDes);
                ToastUtil.showToast(GroupCallNewsActivity.this, resultDes);
            }
        });
    };


    /**
     * 获取当前在线人员列表
     **/
    private ReceiveGetGroupCurrentOnlineMemberListHandler mmReceiveGetGroupCurrentOnlineMemberListHandler = new ReceiveGetGroupCurrentOnlineMemberListHandler() {
        @Override
        public void handler(final List<Member> memberList, final boolean isAllMember,int groupId) {
            Log.e("GroupCallNewsActivity", "memberList:" + memberList);
            mHandler.post(() -> {
                if (isFinishing() ||isAllMember) {
                    return;
                }
                if (DataUtil.isExistGroup(userId)) {
                    newsBarGroupName.setText(userName + "(" + memberList.size() + ")");
                } else {
                    newsBarGroupName.setText(userName);
                }
            });

        }
    };
    /**
     * 更新文件夹和组列表数据
     */
    private ReceiveUpdateFoldersAndGroupsHandler receiveUpdateFoldersAndGroupsHandler = () -> {
        if (MyTerminalFactory.getSDK().getGroupManager().getErrorCode() == -1) {
            finish();
        }
        MyTerminalFactory.getSDK().getGroupManager().getGroupCurrentOnlineMemberList(userId, false);
    };

    /***  主动切组成功 ***/
    private ReceiveChangeGroupHandler mReceiveChangeGroupHandler = new ReceiveChangeGroupHandler() {
        @Override
        public void handler(final int errorCode, final String errorDesc) {
            logger.info("转组成功回调消息");
            handler.post(() -> {
                if (errorCode == 0 || errorCode == SignalServerErrorCode.INVALID_SWITCH_GROUP.getErrorCode()) {
                    if (MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_TALK.name())) {
                        ptt.setBackgroundResource(R.drawable.shape_news_ptt_listen);
                        ptt.setText(R.string.text_ptt);
                    } else {
                        ptt.setBackgroundResource(R.drawable.shape_news_ptt_wait);
                        ptt.setText(R.string.text_no_group_calls);
                    }

                    TextViewCompat.setTextAppearance(ptt, R.style.funcation_top_btn_text);
                } else {
                    ToastUtil.showToast(GroupCallNewsActivity.this, errorDesc);
                }
            });
        }
    };


    /*** 强制切组回调消息 **/
    private ReceiveForceChangeGroupHandler mReceiveForceChangeGroupHandler = new ReceiveForceChangeGroupHandler() {
        @Override
        public void handler(final int memberId, final int toGroupId,boolean forceSwitchGroup,String tempGroupType) {
            logger.info("GroupCallNewsActivity接受了ReceiveForceChangeGroupHandler======"+"toGroupId:"+toGroupId+"===memberId:"+memberId);
            if(!forceSwitchGroup){
                return;
            }
            if (memberId == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)) {
                if (toGroupId == userId) {//强制切组是当前会话组
                    myHandler.post(() -> {
                        ptt.setBackgroundResource(R.drawable.shape_news_ptt_listen);
                        ptt.setText("PTT");
                        TextViewCompat.setTextAppearance(ptt, R.style.funcation_top_btn_text);
                    });

                } else {//强制切组不是是当前会话组
                    myHandler.postDelayed(() -> {
                        if(!DataUtil.isExistGroup(userId)){
                            finish();
                        }else {
                            ptt.setText(R.string.button_change_to_this_group_and_speak);
                            ptt.setBackgroundResource(R.drawable.shape_news_ptt_wait);
                            TextViewCompat.setTextAppearance(ptt, R.style.ptt_gray);
                        }
                    },1000);
                }
            }
        }
    };
    private ReceiveUpdateConfigHandler receiveUpdateConfigHandler = new ReceiveUpdateConfigHandler() {
        @Override
        public void handler() {
            myHandler.post(() -> funcation.setFunction(true, userId));
        }
    };
    private ReceiverCloseKeyBoardHandler receiverCloseKeyBoardHandler = new ReceiverCloseKeyBoardHandler() {
        @Override
        public void handler() {
            logger.info("sjl_收到来自服务的关闭键盘handler");
            InputMethodUtil.hideInputMethod(GroupCallNewsActivity.this, groupCallNewsEt);
        }
    };

    private ReceiveResponseGroupActiveHandler receiveResponseGroupActiveHandler = (isActive, responseGroupId) -> {
        if(TerminalFactory.getSDK().getParam(Params.USER_TYPE,"").equals(UserType.USER_HIGH.toString())){
            return;
        }
        if(userId == responseGroupId && !isActive){
            finish();
        }
    };
    /**
     * 获取组内正在直播的成员列表
     */
    private ReceiveGetGroupLivingListHandler receiveGetGroupLivingListHandler = (memberList,  resultCode, resultDesc,forNumber) -> {
        handler.post(() -> {
            if(resultCode == BaseCommonCode.SUCCESS_CODE && !memberList.isEmpty()){
                //正在上报的人
                tv_living_number.setText(String.format(GroupCallNewsActivity.this.getString(R.string.group_living_number),memberList.size()));
                ll_living.setVisibility(View.VISIBLE);
            }else{
                //没有正在上报的人
                tv_living_number.setText("");
                ll_living.setVisibility(View.GONE);
            }
        });
        if(forNumber){
            handler.postDelayed(() -> getGroupLivingList(),GET_GROUP_LIVING_INTERVAL_TIME);
        }
    };


    //GH880手机PTT按钮事件
    public class GotaKeHandler extends IGotaKeyHandler.Stub{

        @Override
        public void onPTTKeyDown() throws RemoteException {
            myHandler.post(() -> {
                try {
                    pttDownDoThing();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        }

        @Override
        public void onPTTKeyUp() throws RemoteException {
            myHandler.post(() -> {
                try {
                    pttUpDoThing();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        @Override
        public void onSOSKeyDown() throws RemoteException {

        }

        @Override
        public void onSOSKeyUp() throws RemoteException {

        }
    }
}

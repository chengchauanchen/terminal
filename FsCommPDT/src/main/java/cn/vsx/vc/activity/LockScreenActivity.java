package cn.vsx.vc.activity;

import android.Manifest.permission;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.CallMode;
import cn.vsx.hamster.common.MemberChangeType;
import cn.vsx.hamster.common.ResponseGroupType;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.SignalServerErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCallingCannotClickHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCeaseGroupCallConformationHander;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallCeasedIndicationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberDeleteHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyMemberChangeHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveOnLineStatusChangedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRequestGroupCallConformationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSendUuidResponseHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateFoldersAndGroupsHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.service.LockScreenService;
import cn.vsx.vc.view.IndividualCallTimerView;
import cn.vsx.vc.view.MyRelativeLayout;
import cn.vsx.vc.view.TimerView;
import cn.vsx.vc.view.VolumeViewLayout;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.audio.CheckMyPermission;
import ptt.terminalsdk.tools.PhoneAdapter;
import ptt.terminalsdk.tools.ToastUtil;

import static cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState.GRANTED;
import static cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState.IDLE;

//import cn.vsx.hamster.terminalsdk.model.Group;

public class LockScreenActivity extends BaseActivity {

    //成员被删除了,销毁锁屏
    private ReceiveMemberDeleteHandler receiveMemberDeleteHandler = new ReceiveMemberDeleteHandler() {
        @Override
        public void handler() {
            logger.info("receiveMemberDeleteHandler" + "被调用了");
            mHandler.post(() -> {
                LockScreenActivity.this.finish();
                stopService(new Intent(LockScreenActivity.this, LockScreenService.class));
            });
        }
    };

    /**
     * 更新配置信息
     */
    private ReceiveUpdateConfigHandler receiveUpdateConfigHandler = new ReceiveUpdateConfigHandler() {
        @Override
        public void handler() {
            mHandler.post(() -> {
                setCurrentGroupView();//当前的组和文件夹名字重置
            });
        }
    };
    /**
     * 网络连接状态
     */
    private ReceiveOnLineStatusChangedHandler receiveOnLineStatusChangedHandler = new ReceiveOnLineStatusChangedHandler() {
        @Override
        public void handler(final boolean connected) {
            logger.info("锁屏界面服务是否连接：" + connected);
            mHandler.post(() -> {
            });
        }
    };
    /**
     * 信令服务发送NotifyForceRegisterMessage消息时，先去reAuth(false)，然后login()
     */
    private ReceiveSendUuidResponseHandler receiveSendUuidResponseHandler = (resultCode, resultDesc, isRegisted) -> {
        if (resultCode == BaseCommonCode.SUCCESS_CODE) {
            if (isRegisted) {
//                TerminalFactory.getSDK().getAuthManagerTwo().login();
                logger.info("信令服务器通知NotifyForceRegisterMessage消息，在LockScreenActivity中登录了");
            } else {
                runOnUiThread(() -> LockScreenActivity.this.finish());
            }
        }
    };


    /**
     * 更新文件夹和组列表数据
     */
    private ReceiveUpdateFoldersAndGroupsHandler receiveUpdateFoldersAndGroupsHandler = new ReceiveUpdateFoldersAndGroupsHandler() {
        @Override
        public void handler() {
            mHandler.post(() -> {
                //当前文件夹、组数据的显示设置
                setCurrentGroupView();
            });
        }
    };

    /**
     * 被动方组呼停止
     */
    private ReceiveGroupCallCeasedIndicationHandler receiveGroupCallCeasedIndicationHandler = new ReceiveGroupCallCeasedIndicationHandler() {

        @Override
        public void handler(int reasonCode) {
            mHandler.post(() -> {
                setCurrentGroupView();
                MyTerminalFactory.getSDK().putParam(Params.CURRENT_SPEAKER, "");
                if (MyApplication.instance.getGroupSpeakState() != GroupCallSpeakState.GRANTING && MyApplication.instance.getGroupSpeakState() != GroupCallSpeakState.WAITING && MyApplication.instance.getGroupSpeakState() != GRANTED) {
                    change2Silence();
                }

            });
        }
    };

    /**
     * 被动方组呼来了
     */
    private ReceiveGroupCallIncommingHandler receiveGroupCallIncommingHandler = new ReceiveGroupCallIncommingHandler() {
        @Override
        public void handler(int memberId, String memberName, int groupId, String groupName,CallMode currentCallMode, long uniqueNo) {
            if (MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_LISTEN.name())) {
                mHandler.post(() -> {
                    change2Listening();
                    setCurrentGroupScanView(groupId);
                    MyTerminalFactory.getSDK().putParam(Params.CURRENT_SPEAKER, memberName);
                });
            }

        }
    };

    /**
     * 主动方停止组呼的消息
     */
    private ReceiveCeaseGroupCallConformationHander receiveCeaseGroupCallConformationHander = new ReceiveCeaseGroupCallConformationHander() {
        @Override
        public void handler(final int resultCode, String resultDesc) {
            if (currentCallMode == CallMode.GENERAL_CALL_MODE) {
                mHandler.post(() -> {
                    if (MyApplication.instance.getGroupListenenState() == GroupCallListenState.LISTENING) {
                        change2Listening();
                    } else {
                        //如果是停止组呼
                        MyApplication.instance.isPttPress = false;
//                            myHandler.removeMessages(1);
//                            timeProgress = 60;
                        change2Silence();
                    }
                });
            }
        }
    };

    /**
     * 主动方请求组呼的消息
     */
    private ReceiveRequestGroupCallConformationHandler receiveRequestGroupCallConformationHandler = new ReceiveRequestGroupCallConformationHandler() {
        @Override
        public void handler(int methodResult, String resultDesc, int groupId) {
            currentCallMode = MyTerminalFactory.getSDK().getGroupCallManager().getCurrentCallMode();
            if (currentCallMode == CallMode.GENERAL_CALL_MODE) {

                if (methodResult == 0) {//请求成功，开始组呼
                    mHandler.post(() -> {
                        change2Speaking();
                        setCurrentGroupView();
                    });
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
     * ptt按钮的监听
     */
    private final class OnTouchListenerImplementation implements
            OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lockPttDownDoThing(true);
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    lockPttUpDoThing();
                    break;

                default:
                    break;
            }
            return true;
        }

    }

    private void lockPttDownDoThing(boolean isCurrentGroup) {
        if (!CheckMyPermission.selfPermissionGranted(this, permission.RECORD_AUDIO)) {
            ToastUtil.showToast(this, getString(R.string.text_audio_frequency_is_not_open_audio_is_not_used));
            logger.error("录制音频权限未打开，语音功能将不能使用。");
            return;
        }
        if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_TALK.name())) {
            ToastUtil.showToast(this, getString(R.string.text_has_no_group_call_speak_authority));
            return;
        }
        logger.info("锁屏界面PTT按下");
        int resultCode;
        if(isCurrentGroup){
            resultCode = MyTerminalFactory.getSDK().getGroupCallManager().requestCurrentGroupCall("");
        }else {
            int lastGroupId = TerminalFactory.getSDK().getParam(Params.SECOND_GROUP_ID, 0);
            if(lastGroupId != 0){
                resultCode = MyTerminalFactory.getSDK().getGroupCallManager().requestGroupCall("",lastGroupId);
            }else {
                resultCode = MyTerminalFactory.getSDK().getGroupCallManager().requestCurrentGroupCall("");
            }
        }
        if (resultCode == BaseCommonCode.SUCCESS_CODE) {
            MyApplication.instance.isPttPress = true;
            change2PreSpeaking();
        } else {
            ToastUtil.groupCallFailToast(this, resultCode);
        }
    }

    private void lockPttUpDoThing() {
        MyApplication.instance.isPttPress = false;
        MyTerminalFactory.getSDK().getAudioProxy().volumeCancelQuiet();
        if (MyApplication.instance.getGroupListenenState() == GroupCallListenState.LISTENING) {
            change2Listening();
        } else {
            change2Silence();
        }
        logger.info("锁屏界面的PTT抬起");
        MyTerminalFactory.getSDK().getGroupCallManager().ceaseGroupCall();
    }


    private RelativeLayout mContentView;
    private View mMyView;
    private FrameLayout mFlLockscreen;
    private RelativeLayout mRlLockscreen;
    private TextView mTvLockscreenTime;
    private TextView mTvLockscreenDate;
    private TextView mTvLockscreenWeek;
    private LinearLayout mLlGroup;
    private TextView mTvCurrentOnline;
    private TextView mTvCurrentFolder;
    private TextView mTvCurrentGroup;
    private LinearLayout mLlSpeakingPrompt;
    private IndividualCallTimerView mICTVSpeakingTime;
    private MyRelativeLayout mRlLockScreen;
    private Button mPtt;
    private LinearLayout mLlSpeakingTime;
    private TimerView mTalkTime;
    private LinearLayout mLlListening;
    private TextView mIncommingCallCurrentSpeaker;
    private LinearLayout mLlPreSpeaking;
    private LinearLayout mLlSilence;
    private LinearLayout mLlForbid;
    private LinearLayout mLlWaiting;
    private VolumeViewLayout mVolumeLayout;


    private CallMode currentCallMode;
    private static final int UPDATETIME = 0;
    @SuppressLint("HandlerLeak")
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATETIME:
                    mHandler.removeMessages(UPDATETIME);
                    setDateTime();
                    Message message = Message.obtain();
                    message.what = UPDATETIME;
                    mHandler.sendMessageDelayed(message, 30 * 1000);
                    break;
                default:
                    break;
            }
        }
    };

    private BroadcastReceiver openLockReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                if (VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    if (((KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE)).isKeyguardSecure()) {
                        mHandler.removeCallbacksAndMessages(null);
                        LockScreenActivity.this.finish();
                        MyApplication.instance.isLockScreenCreat = false;
                        logger.error("------屏幕解锁了-------");
                    }
                }
            }
        }
    };


    @Override
    public int getLayoutResId() {
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        return R.layout.activity_lockscreen;
    }

    @Override
    public void initView() {
        findView();
        online_number = MyTerminalFactory.getSDK().getConfigManager().getCurrentGroupMembers().size();
        setDateTime();
        setCurrentGroupView();
        if (!MyTerminalFactory.getSDK().getParam(Params.CURRENT_SPEAKER).equals("")) {
            mLlListening.setVisibility(View.VISIBLE);
            mIncommingCallCurrentSpeaker.setText(MyTerminalFactory.getSDK().getParam(Params.CURRENT_SPEAKER));
        }

        Message msg = Message.obtain();
        msg.what = UPDATETIME;
        mHandler.sendMessage(msg);

        //这里对当前的状态判断不正确，直接使用TalkbackFragment中的方法
//        if (MyApplication.instance.getGroupListenenState() == GroupCallListenState.LISTENING){
//            change2Listening();
//        } else {
//            change2Silence();
//        }
        setPttText();

        // F25手机不显示PTT按钮
        if (PhoneAdapter.isF25()) {
            mPtt.setVisibility(View.GONE);
        }
        logger.error("创建了一个锁屏界面");
    }


    private void setPttText() {
        int currentGroupId = TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);
        Group groupByGroupNo = TerminalFactory.getSDK().getGroupByGroupNo(currentGroupId);
        //响应组  普通用户  不在响应状态
        if (ResponseGroupType.RESPONSE_TRUE.toString().equals(groupByGroupNo.getResponseGroupType()) &&
                !groupByGroupNo.isHighUser() &&
                !TerminalFactory.getSDK().getGroupCallManager().getActiveResponseGroup().contains(currentGroupId)) {
            change2Forbid();
        } else if (MyApplication.instance.getGroupListenenState() != GroupCallListenState.IDLE) {
            change2Listening();
        } else if (MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.GRANTING) {
            change2PreSpeaking();
        } else if (MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.WAITING) {
            change2Waiting();
        } else if (MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.GRANTED) {
            change2Speaking();
        } else {
            change2Silence();
        }
    }


    private void findView() {
        mContentView = (RelativeLayout) findViewById(R.id.content_view);
        mMyView = (View) findViewById(R.id.my_view);
        mFlLockscreen = (FrameLayout) findViewById(R.id.fl_lockscreen);
        mRlLockscreen = (RelativeLayout) findViewById(R.id.rl_lockscreen);
        mTvLockscreenTime = (TextView) findViewById(R.id.tv_lockscreen_time);
        mTvLockscreenDate = (TextView) findViewById(R.id.tv_lockscreen_date);
        mTvLockscreenWeek = (TextView) findViewById(R.id.tv_lockscreen_week);
        mLlGroup = (LinearLayout) findViewById(R.id.ll_group);
        mTvCurrentOnline = (TextView) findViewById(R.id.tv_current_online);
        mTvCurrentFolder = (TextView) findViewById(R.id.tv_current_folder);
        mTvCurrentGroup = (TextView) findViewById(R.id.tv_current_group);
        mLlSpeakingPrompt = (LinearLayout) findViewById(R.id.ll_speaking_prompt);
        mICTVSpeakingTime = (IndividualCallTimerView) findViewById(R.id.ICTV_speaking_time);
        mRlLockScreen = (MyRelativeLayout) findViewById(R.id.rl_lock_screen);
        mPtt = (Button) findViewById(R.id.ptt);
        mLlSpeakingTime = (LinearLayout) findViewById(R.id.ll_speaking_time);
        mTalkTime = (TimerView) findViewById(R.id.talk_time);
        mLlListening = (LinearLayout) findViewById(R.id.ll_listening);
        mIncommingCallCurrentSpeaker = (TextView) findViewById(R.id.incomming_call_current_speaker);
        mLlPreSpeaking = (LinearLayout) findViewById(R.id.ll_pre_speaking);
        mLlSilence = (LinearLayout) findViewById(R.id.ll_silence);
        mLlForbid = (LinearLayout) findViewById(R.id.ll_forbid);
        mLlWaiting = (LinearLayout) findViewById(R.id.ll_waiting);
        mVolumeLayout = (VolumeViewLayout) findViewById(R.id.volume_layout);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.e("LockScreenActivity", "onNewIntent");
        super.onNewIntent(intent);
        setIntent(intent);
        initData();
    }

    @Override
    public void initListener() {
        mPtt.setOnTouchListener(new OnTouchListenerImplementation());

        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyMemberChangeHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateFoldersAndGroupsHandler);

        MyTerminalFactory.getSDK().registReceiveHandler(receiveRequestGroupCallConformationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveCeaseGroupCallConformationHander);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallIncommingHandler);

        MyTerminalFactory.getSDK().registReceiveHandler(receiveSendUuidResponseHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveOnLineStatusChangedHandler);

        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveMemberDeleteHandler);


        registerReceiver(openLockReceiver, new IntentFilter(Intent.ACTION_USER_PRESENT));
        setOnPTTVolumeBtnStatusChangedListener(new OnPTTVolumeBtnStatusChangedListenerImp());
        mRlLockScreen.setScreenLockListener(() -> {
            Log.d("LockScreenActivity", "执行动画");
            playAnimation();
        });

    }

    @Override
    public void initData() {
        MyApplication.instance.isLockScreenCreat = true;
    }


    @Override
    public void doOtherDestroy() {
        Log.e("LockScreenActivity", "doOtherDestroy");
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyMemberChangeHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveRequestGroupCallConformationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveCeaseGroupCallConformationHander);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallIncommingHandler);

        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateFoldersAndGroupsHandler);

        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveSendUuidResponseHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveOnLineStatusChangedHandler);

        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMemberDeleteHandler);


        if (openLockReceiver != null) {
            unregisterReceiver(openLockReceiver);
        }
        if (mVolumeLayout != null) {
            mVolumeLayout.unRegistLintener();
        }

        MyApplication.instance.isLockScreenCreat = false;

        mHandler.removeCallbacksAndMessages(null);

        logger.error("锁屏页面销毁了");
    }

    private void change2Speaking() {

        mTvCurrentOnline.setText(String.format(getString(R.string.text_online_member_number), online_number));
        allViewDefault();
        mLlSpeakingTime.setVisibility(View.VISIBLE);
        mTalkTime.start(Color.GREEN);
        mPtt.setText(R.string.button_release_end);
        mPtt.setBackgroundResource(R.drawable.ptt_speaking);
    }

    private void change2PreSpeaking() {
        mTvCurrentOnline.setText(String.format(getString(R.string.text_online_member_number), online_number));
        allViewDefault();
        mPtt.setText(R.string.text_ready_to_speak);
        mPtt.setBackgroundResource(R.drawable.ptt_pre_speaking);
        mLlPreSpeaking.setVisibility(View.VISIBLE);
    }

    private void change2Silence() {
        mTvCurrentOnline.setText(String.format(getString(R.string.text_online_member_number), online_number));
        allViewDefault();
        mPtt.setText(R.string.press_blank_space_talk_text);
        mPtt.setBackgroundResource(R.drawable.ptt_silence);
        mLlSilence.setVisibility(View.VISIBLE);
    }

    private void change2Waiting() {
        mTvCurrentOnline.setText(String.format(getString(R.string.text_online_member_number), online_number));
        allViewDefault();
        mLlPreSpeaking.setVisibility(View.VISIBLE);
        mPtt.setText(R.string.text_ready_to_speak);
        mPtt.setBackgroundResource(R.drawable.ptt_pre_speaking);
    }

    private void change2Listening() {
        String speakMemberName = MyTerminalFactory.getSDK().getGroupCallManager().getSpeakingMemberName();
        mTvCurrentOnline.setText(String.format(getString(R.string.text_online_member_number), online_number));
        allViewDefault();
        if (!TextUtils.isEmpty(speakMemberName)) {
            mLlListening.setVisibility(View.VISIBLE);
            mIncommingCallCurrentSpeaker.setText(speakMemberName);
        }
//        ptt.setBackgroundResource(R.drawable.ptt_listening3);
        mPtt.setText(R.string.button_press_to_line_up);
        mPtt.setTextColor(getResources().getColor(R.color.darkgray));
        mPtt.setBackgroundResource(R.drawable.ptt_listening);
    }

    private void allViewDefault() {
        if (mTalkTime != null) {
            mTalkTime.stop();
        }
        mLlListening.setVisibility(View.GONE);
        mLlSilence.setVisibility(View.GONE);
        mLlSpeakingTime.setVisibility(View.GONE);
        mLlPreSpeaking.setVisibility(View.GONE);
        mLlForbid.setVisibility(View.GONE);
        mLlWaiting.setVisibility(View.GONE);
    }


    private void setDateTime() {
        Date date = new Date(System.currentTimeMillis());
        String mWeek = DataUtil.getWeek();
        String mAP = DataUtil.getAPM();

        mTvLockscreenTime.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date));
        mTvLockscreenDate.setText(new SimpleDateFormat("MM月dd日", Locale.getDefault()).format(date));
        mTvLockscreenWeek.setText(String.format(getString(R.string.text_week_content), mWeek, mAP));

    }

    private void setCurrentGroupView() {
        mTvCurrentGroup.setText(DataUtil.getGroupName(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)));
        mTvCurrentFolder.setText(DataUtil.getGroupDepartmentName(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)));
    }

    private void setCurrentGroupScanView(final int groupId) {
        mTvCurrentGroup.setText(DataUtil.getGroupName(groupId));
        mTvCurrentFolder.setText(DataUtil.getGroupDepartmentName(groupId));
    }

    @Override
    public void onBackPressed() {
        // 不做任何事，为了屏蔽back键
    }

    /**
     * 设置音量键为ptt键时的监听
     */
    private final class OnPTTVolumeBtnStatusChangedListenerImp
            implements OnPTTVolumeBtnStatusChangedListener {
        @Override
        public void onPTTVolumeBtnStatusChange(GroupCallSpeakState groupCallSpeakState,boolean isVolumeUp) {
            if (groupCallSpeakState == IDLE) {
                lockPttDownDoThing(isVolumeUp);
            } else {
                lockPttUpDoThing();
            }
        }
    }


    private void playAnimation() {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mContentView, "scaleX", 1f, 0.5f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mContentView, "scaleY", 1f, 0.5f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(mContentView, "alpha", 1f, 0.5f);
        animatorSet.play(alpha).with(scaleX).with(scaleY);
        animatorSet.setDuration(800);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.start();
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                unLockScreen();
                finish();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
    }

    private void unLockScreen(){
        // 屏幕解锁
        KeyguardManager keyguardManager = (KeyguardManager) this.getSystemService(KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("unLock");
        // 屏幕锁定
        keyguardLock.reenableKeyguard();
        keyguardLock.disableKeyguard(); // 解锁
    }

    private int online_number;
    /**
     * 是否禁止组呼
     */
    private ReceiveNotifyMemberChangeHandler receiveNotifyMemberChangeHandler = new ReceiveNotifyMemberChangeHandler() {

        @Override
        public void handler(MemberChangeType memberChangeType) {
            logger.info("触发了receiveNotifyMemberChangeHandler");
            online_number = MyTerminalFactory.getSDK().getConfigManager().getCurrentGroupMembers().size();
            if (memberChangeType == MemberChangeType.MEMBER_ACTIVE_GROUP_CALL) {
                mHandler.post(() -> change2Silence());

            } else if (memberChangeType == MemberChangeType.MEMBER_PROHIBIT_GROUP_CALL) {
                mHandler.post(() -> change2Forbid());
            }
        }
    };

    private void change2Forbid() {
        allViewDefault();
        mLlForbid.setVisibility(View.VISIBLE);
        //12.25
        logger.info("ptt.change2Forbid()按住排队");
        mTvCurrentOnline.setText(String.format(getString(R.string.text_online_member_number), online_number));
        mPtt.setText(R.string.text_no_group_calls);
        mPtt.setTextColor(getResources().getColor(R.color.darkgray));
        mPtt.setBackgroundResource(R.drawable.ptt_listening);
        logger.info("主界面，ptt被禁了  isPttPress：" + MyApplication.instance.isPttPress);
        mPtt.setEnabled(false);
        if (MyApplication.instance.isPttPress) {
            pttUpDoThing();
        }
    }

    private void pttUpDoThing() {
        logger.info("ptt.pttUpDoThing执行了 isPttPress：" + MyApplication.instance.isPttPress);
        //        talkback_change_session.setEnabled(true);
        if (MyApplication.instance.isPttPress) {
            MyApplication.instance.isPttPress = false;
            MyTerminalFactory.getSDK().getAudioProxy().volumeCancelQuiet();
            //            canScroll = true;
            if (MyApplication.instance.getGroupListenenState() == GroupCallListenState.LISTENING) {

                change2Listening();
            } else {
                change2Silence();

            }
            MyTerminalFactory.getSDK().getGroupCallManager().ceaseGroupCall();
            Log.e("LockScreenActivity", "触发了ReceiveCallingCannotClickHandler");
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCallingCannotClickHandler.class, false);

        }
    }
}

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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.Bind;
import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.CallMode;
import cn.vsx.hamster.common.MemberChangeType;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.SignalServerErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCallingCannotClickHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCeaseGroupCallConformationHander;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallCeasedIndicationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberDeleteHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyMemberChangeHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRequestGroupCallConformationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSendUuidResponseHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveServerConnectionEstablishedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateFoldersAndGroupsHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.service.LockScreenService;
import cn.vsx.vc.utils.ActivityCollector;
import cn.vsx.vc.utils.DataUtil;
import ptt.terminalsdk.tools.ToastUtil;
import cn.vsx.vc.view.MyRelativeLayout;
import cn.vsx.vc.view.TimerView;
import cn.vsx.vc.view.VolumeViewLayout;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.audio.CheckMyPermission;
import ptt.terminalsdk.tools.PhoneAdapter;

import static cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState.GRANTED;
import static cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState.IDLE;

public class LockScreenActivity extends BaseActivity {

    //成员被删除了,销毁锁屏
    private ReceiveMemberDeleteHandler receiveMemberDeleteHandler = new ReceiveMemberDeleteHandler() {
        @Override
        public void handler() {
            logger.info("receiveMemberDeleteHandler"+"被调用了");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    LockScreenActivity.this.finish();
                    stopService(new Intent(LockScreenActivity.this, LockScreenService.class));
                }
            });
        }
    };

    /**更新配置信息*/
    private ReceiveUpdateConfigHandler receiveUpdateConfigHandler = new ReceiveUpdateConfigHandler() {
        @Override
        public void handler() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    setCurrentGroupView();//当前的组和文件夹名字重置
                }
            });
        }
    };
    /**网络连接状态*/
    private ReceiveServerConnectionEstablishedHandler receiveServerConnectionEstablishedHandler = new ReceiveServerConnectionEstablishedHandler() {
        @Override
        public void handler(final boolean connected) {
            logger.info("锁屏界面服务是否连接：" + connected);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                }
            });
        }
    };
    /**
     * 信令服务发送NotifyForceRegisterMessage消息时，先去reAuth(false)，然后login()
     */
    private ReceiveSendUuidResponseHandler receiveSendUuidResponseHandler = new ReceiveSendUuidResponseHandler() {
        @Override
        public void handler(int resultCode, final String resultDesc, boolean isRegisted) {
            if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                if(isRegisted){
                    TerminalFactory.getSDK().getAuthManagerTwo().login();
                    logger.info("信令服务器通知NotifyForceRegisterMessage消息，在LockScreenActivity中登录了");
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LockScreenActivity.this.finish();
                        }
                    });
                }
            }
        }
    };


    /**更新文件夹和组列表数据*/
    private ReceiveUpdateFoldersAndGroupsHandler receiveUpdateFoldersAndGroupsHandler = new ReceiveUpdateFoldersAndGroupsHandler(){
        @Override
        public void handler() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    //当前文件夹、组数据的显示设置
                    setCurrentGroupView();
                }
            });
        }
    };

    /**
     * 被动方组呼停止
     */
    private ReceiveGroupCallCeasedIndicationHandler receiveGroupCallCeasedIndicationHandler = new ReceiveGroupCallCeasedIndicationHandler() {

        @Override
        public void handler(int reasonCode) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    setCurrentGroupView();
                    MyTerminalFactory.getSDK().putParam(Params.CURRENT_SPEAKER, "");
                    if (MyApplication.instance.getGroupSpeakState() != GroupCallSpeakState.GRANTING && MyApplication.instance.getGroupSpeakState() != GroupCallSpeakState.WAITING && MyApplication.instance.getGroupSpeakState() != GRANTED) {
                        change2Silence();
                    }

                }
            });
        }
    };

    /**
     * 被动方组呼来了
     */
    private ReceiveGroupCallIncommingHandler receiveGroupCallIncommingHandler = new ReceiveGroupCallIncommingHandler() {
        @Override
        public void handler(int memberId, final String memberName, final int groupId,
                            String version, CallMode currentCallMode) {
            if(MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_LISTEN.name())){
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        change2Listening();
                        setCurrentGroupScanView(groupId);
                        MyTerminalFactory.getSDK().putParam(Params.CURRENT_SPEAKER, memberName);
                    }
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
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (MyApplication.instance.getGroupListenenState() == GroupCallListenState.LISTENING) {
                            change2Listening();
                        } else {
                            //如果是停止组呼
                            MyApplication.instance.isPttPress = false;
//                            myHandler.removeMessages(1);
//                            timeProgress = 60;
                            change2Silence();
                        }
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
        public void handler(int methodResult, String resultDesc) {
            currentCallMode = MyTerminalFactory.getSDK().getGroupCallManager().getCurrentCallMode();
            if (currentCallMode == CallMode.GENERAL_CALL_MODE) {

                if (methodResult == 0) {//请求成功，开始组呼
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            change2Speaking();
                            setCurrentGroupView();
                        }
                    });
                } else if (methodResult == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode()) {//请求等待中
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            change2Waiting();
                        }
                    });
                } else {//请求失败
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (MyApplication.instance.getGroupListenenState() != GroupCallListenState.LISTENING) {
                                change2Silence();
                            } else {
                                change2Listening();
                            }
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
                    lockPttDownDoThing();
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

    private void lockPttDownDoThing() {
        if (!CheckMyPermission.selfPermissionGranted(this, permission.RECORD_AUDIO)){
            ToastUtil.showToast(this, "录制音频权限未打开，语音功能将不能使用。");
            logger.error("录制音频权限未打开，语音功能将不能使用。");
            return;
        }
        if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_TALK.name())){
            ToastUtil.showToast(this,"没有组呼说的权限");
            return;
        }
        logger.info("锁屏界面PTT按下");
        int resultCode = MyTerminalFactory.getSDK().getGroupCallManager().requestGroupCall("");
        if (resultCode == BaseCommonCode.SUCCESS_CODE){
            MyApplication.instance.isPttPress = true;
            change2PreSpeaking();
        }else{
            ToastUtil.groupCallFailToast(this, resultCode);
        }
    }

    private void lockPttUpDoThing() {
        if(MyApplication.instance.isPttPress){
            MyApplication.instance.isPttPress = false;
            if (MyApplication.instance.getGroupListenenState() == GroupCallListenState.LISTENING) {
                change2Listening();
            } else {
                change2Silence();
            }
            logger.info("锁屏界面的PTT抬起");
            MyTerminalFactory.getSDK().getGroupCallManager().ceaseGroupCall();
        }
    }



    @Bind(R.id.content_view)
    RelativeLayout content_view;
    @Bind(R.id.rl_lockscreen)
    RelativeLayout rl_lockscreen;
    @Bind(R.id.ptt)
    Button ptt;
    @Bind(R.id.ll_speaking_time)
    LinearLayout ll_speaking_time;
    @Bind(R.id.ll_listening)
    LinearLayout ll_listening;
    @Bind(R.id.talk_time)
    TimerView talk_time;
    @Bind(R.id.tv_lockscreen_time)
    TextView tv_lockscreen_time;
    @Bind(R.id.tv_lockscreen_date)
    TextView tv_lockscreen_date;
    @Bind(R.id.tv_lockscreen_week)
    TextView tv_lockscreen_week;
    @Bind(R.id.tv_current_folder)
    TextView tv_current_folder;
    @Bind(R.id.tv_current_group)
    TextView tv_current_group;
    @Bind(R.id.incomming_call_current_speaker)
    TextView incomming_call_current_speaker;
    @Bind(R.id.volume_layout)
    VolumeViewLayout volumeViewLayout;
    @Bind(R.id.ll_pre_speaking)
    LinearLayout ll_pre_speaking;
    @Bind(R.id.ll_silence)
    LinearLayout ll_silence;
    @Bind(R.id.ll_forbid)
    LinearLayout ll_forbid;
    @Bind(R.id.ll_waiting)
    LinearLayout ll_waiting;
    @Bind(R.id.tv_current_online)
    TextView tv_current_online;
    @Bind(R.id.rl_lock_screen)
    MyRelativeLayout rl_lock_screen;
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
                    mHandler.sendMessageDelayed(message,30*1000);
                    break;
                default:
                    break;
            }
        }

        ;
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

        online_number = MyTerminalFactory.getSDK().getConfigManager().getCurrentGroupMembers().size();
        setDateTime();
        setCurrentGroupView();
        if (!MyTerminalFactory.getSDK().getParam(Params.CURRENT_SPEAKER).equals("")) {
            ll_listening.setVisibility(View.VISIBLE);
            incomming_call_current_speaker.setText(MyTerminalFactory.getSDK().getParam(Params.CURRENT_SPEAKER));
        }

        Message msg = Message.obtain();
        msg.what = UPDATETIME;
        mHandler.sendMessage(msg);

        if (MyApplication.instance.getGroupListenenState() == GroupCallListenState.LISTENING){
            change2Listening();
        } else {
            change2Silence();
        }

        // F25手机不显示PTT按钮
        if (PhoneAdapter.isF25()) {
            ptt.setVisibility(View.GONE);
        }
        logger.error("创建了一个锁屏界面");
    }

    @Override
    protected void onNewIntent(Intent intent){
        Log.e("LockScreenActivity", "onNewIntent");
        super.onNewIntent(intent);
        setIntent(intent);
        initData();
    }

    @Override
    public void initListener() {
        ptt.setOnTouchListener(new OnTouchListenerImplementation());

        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyMemberChangeHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateFoldersAndGroupsHandler);

        MyTerminalFactory.getSDK().registReceiveHandler(receiveRequestGroupCallConformationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveCeaseGroupCallConformationHander);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallIncommingHandler);

        MyTerminalFactory.getSDK().registReceiveHandler(receiveSendUuidResponseHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveServerConnectionEstablishedHandler);

        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveMemberDeleteHandler);


        registerReceiver(openLockReceiver, new IntentFilter(Intent.ACTION_USER_PRESENT));
        setOnPTTVolumeBtnStatusChangedListener(new OnPTTVolumeBtnStatusChangedListenerImp());
        rl_lock_screen.setScreenLockListener(new MyRelativeLayout.ScreenLockListener(){
            @Override
            public void onScreenLock(){
                Log.d("LockScreenActivity", "执行动画");
                playAnimation();
            }
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
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveServerConnectionEstablishedHandler);

        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMemberDeleteHandler);


        if (openLockReceiver != null) {
            unregisterReceiver(openLockReceiver);
        }
        if (volumeViewLayout!= null){
            volumeViewLayout.unRegistLintener();
        }

        MyApplication.instance.isLockScreenCreat = false;

        mHandler.removeCallbacksAndMessages(null);

        logger.error("锁屏页面销毁了");
    }

    private void change2Speaking() {
        tv_current_online.setText(online_number+"人在线");
        allViewDefault();
        ll_speaking_time.setVisibility(View.VISIBLE);
        talk_time.start(Color.GREEN);
        ptt.setText("松开结束");
        ptt.setBackgroundResource(R.drawable.ptt_speaking);
    }

    private void change2PreSpeaking() {
        tv_current_online.setText(online_number+"人在线");
        allViewDefault();
        ptt.setText("准备说话");
        ptt.setBackgroundResource(R.drawable.ptt_pre_speaking);
        ll_pre_speaking.setVisibility(View.VISIBLE);
    }

    private void change2Silence() {
        tv_current_online.setText(online_number+"人在线");
        allViewDefault();
        ptt.setText("按住 说话");
        ptt.setBackgroundResource(R.drawable.ptt_silence);
        ll_silence.setVisibility(View.VISIBLE);
    }

    private void change2Waiting() {
        tv_current_online.setText(online_number+"人在线");
        allViewDefault();
        ll_pre_speaking.setVisibility(View.VISIBLE);
        ptt.setText("准备说话");
        ptt.setBackgroundResource(R.drawable.ptt_pre_speaking);
    }

    private void change2Listening() {
        String speakMemberName = MyTerminalFactory.getSDK().getGroupCallManager().getSpeakingMemberName();
        tv_current_online.setText(online_number+"人在线");
        allViewDefault();
        if (!TextUtils.isEmpty(speakMemberName)) {
            ll_listening.setVisibility(View.VISIBLE);
            incomming_call_current_speaker.setText(speakMemberName);
        }
//        ptt.setBackgroundResource(R.drawable.ptt_listening3);
        ptt.setText("按住 排队");
        ptt.setTextColor(Color.parseColor("#a9a9a9"));
        ptt.setBackgroundResource(R.drawable.ptt_listening);
    }

    private void allViewDefault() {
        if (talk_time != null) {
            talk_time.stop();
        }
        ll_listening.setVisibility(View.GONE);
        ll_silence.setVisibility(View.GONE);
        ll_speaking_time.setVisibility(View.GONE);
        ll_pre_speaking.setVisibility(View.GONE);
        ll_forbid.setVisibility(View.GONE);
        ll_waiting.setVisibility(View.GONE);
    }


    private void setDateTime() {
        Date date = new Date(System.currentTimeMillis());
        String mWeek = DataUtil.getWeek();
        String mAP = DataUtil.getAPM();

        tv_lockscreen_time.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date));
        tv_lockscreen_date.setText(new SimpleDateFormat("MM月dd日", Locale.getDefault()).format(date));
        tv_lockscreen_week.setText("星期" + mWeek + "    " + mAP);

    }

    private void setCurrentGroupView() {
//        tv_current_group = (TextView) findViewById(R.id.tv_current_group);
//        tv_current_folder = (TextView) findViewById(R.id.tv_current_folder);
        tv_current_group.setText(DataUtil.getGroupByGroupNo(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)).name);
        tv_current_folder.setText(DataUtil.getGroupByGroupNo(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)).getDepartmentName());
    }

    private void setCurrentGroupScanView(final int groupId) {
//        tv_current_group = (TextView) findViewById(R.id.tv_current_group);
//        tv_current_folder = (TextView) findViewById(R.id.tv_current_folder);
        tv_current_group.setText(DataUtil.getGroupByGroupNo(groupId).name);
        tv_current_folder.setText(DataUtil.getGroupByGroupNo(groupId).getDepartmentName());
    }

    @Override
    public void onBackPressed() {
        // 不做任何事，为了屏蔽back键
    }

    /**设置音量键为ptt键时的监听*/
    private final class OnPTTVolumeBtnStatusChangedListenerImp
            implements BaseActivity.OnPTTVolumeBtnStatusChangedListener {
        @Override
        public void onPTTVolumeBtnStatusChange(GroupCallSpeakState groupCallSpeakState) {
            if (groupCallSpeakState == IDLE) {
                lockPttDownDoThing();
            } else {
                lockPttUpDoThing();
            }
        }
    }


    private void playAnimation(){
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(content_view, "scaleX", 1f, 0.5f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(content_view, "scaleY", 1f, 0.5f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(content_view, "alpha", 1f, 0.5f);
        animatorSet.play(alpha).with(scaleX).with(scaleY);
        animatorSet.setDuration(800);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.start();
        animatorSet.addListener(new Animator.AnimatorListener(){
            @Override
            public void onAnimationStart(Animator animation){
            }

            @Override
            public void onAnimationEnd(Animator animation){
                finish();
            }

            @Override
            public void onAnimationCancel(Animator animation){
            }

            @Override
            public void onAnimationRepeat(Animator animation){
            }
        });
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
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        change2Silence();
                    }
                });

            } else if (memberChangeType == MemberChangeType.MEMBER_PROHIBIT_GROUP_CALL) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        change2Forbid();
                    }
                });
            }
        }
    };

    private void change2Forbid() {
        allViewDefault();
        ll_forbid.setVisibility(View.VISIBLE);
        //12.25
        logger.info("ptt.change2Forbid()按住排队");
        tv_current_online.setText(online_number+"人在线");
        ptt.setText("按住 排队");
        ptt.setTextColor(Color.parseColor("#a9a9a9"));
        ptt.setBackgroundResource(R.drawable.ptt_listening);
        logger.info("主界面，ptt被禁了  isPttPress："+MyApplication.instance.isPttPress);
        ptt.setEnabled(false);
        if(MyApplication.instance.isPttPress){
            pttUpDoThing();
        }
    }
    private void pttUpDoThing() {
        logger.info("ptt.pttUpDoThing执行了 isPttPress："+MyApplication.instance.isPttPress);
        //        talkback_change_session.setEnabled(true);
        if (MyApplication.instance.isPttPress){
            MyApplication.instance.isPttPress = false;
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

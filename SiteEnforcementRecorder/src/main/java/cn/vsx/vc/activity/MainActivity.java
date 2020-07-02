package cn.vsx.vc.activity;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import org.apache.log4j.Logger;
import org.easydarwin.push.BITMediaStream;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.OnClick;
import cn.vsx.SpecificSDK.SpecificSDK;
import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.ReceiveObjectMode;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.SignalServerErrorCode;
import cn.vsx.hamster.errcode.module.TerminalErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.auth.LoginModel;
import cn.vsx.hamster.terminalsdk.manager.terminal.TerminalState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePushingState;
import cn.vsx.hamster.terminalsdk.model.BitStarFileDirectory;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.RecorderBindBean;
import cn.vsx.hamster.terminalsdk.model.VideoMember;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveAnswerLiveTimeoutHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveChangeGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveExternStorageSizeHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveForceOfflineHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetVideoPushUrlHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveJoinInWarningGroupPushLiveHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveLoginResponseHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberJoinOrExitHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNetworkChangeHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNobodyRequestVideoLiveHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyEmergencyVideoLiveIncommingMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyLivingIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyLivingStoppedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyOtherStopVideoMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyWarnLivingTimeoutMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveOnLineStatusChangedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRecorderLoginHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRegistCompleteHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseMyselfLiveHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseSetLivingTimeMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseStartLiveHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveReturnAvailableIPHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSendUuidResponseHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateAllDataCompleteHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateUploadFileRateLimitHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.util.StateMachine.IState;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.dialog.LivingStopTimeDialog;
import cn.vsx.vc.listener.PlayVideoStateListener;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.receiveHandle.ReceiveResponseSetLivingTimeMessageUIHandler;
import cn.vsx.vc.receiveHandle.ReceiverAudioButtonEventHandler;
import cn.vsx.vc.receiveHandle.ReceiverFragmentBackPressedByGroupChangeHandler;
import cn.vsx.vc.receiveHandle.ReceiverFragmentClearHandler;
import cn.vsx.vc.receiveHandle.ReceiverFragmentPopBackStackHandler;
import cn.vsx.vc.receiveHandle.ReceiverFragmentShowHandler;
import cn.vsx.vc.receiveHandle.ReceiverNfcStatusHandler;
import cn.vsx.vc.receiveHandle.ReceiverPhotoButtonEventHandler;
import cn.vsx.vc.receiveHandle.ReceiverStopAllBusniessHandler;
import cn.vsx.vc.receiveHandle.ReceiverStopBusniessHandler;
import cn.vsx.vc.receiveHandle.ReceiverUpdateInfraRedHandler;
import cn.vsx.vc.receiveHandle.ReceiverUpdatePlayVideoStateHandler;
import cn.vsx.vc.receiveHandle.ReceiverUsbConnetStateHandler;
import cn.vsx.vc.receiveHandle.ReceiverVideoButtonEventHandler;
import cn.vsx.vc.receiver.NFCCardReader;
import cn.vsx.vc.receiver.UsbConnetStateReceiver;
import cn.vsx.vc.service.LockScreenService;
import cn.vsx.vc.utils.APPStateUtil;
import cn.vsx.vc.utils.ApkUtil;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.DataUtil;
import cn.vsx.vc.utils.DeviceUtil;
import cn.vsx.vc.utils.FragmentUtil;
import cn.vsx.vc.utils.NfcUtil;
import cn.vsx.vc.utils.PhotoUtils;
import cn.vsx.vc.utils.SetToListUtil;
import cn.vsx.vc.utils.ToastUtil;
import ptt.terminalsdk.broadcastreceiver.BatteryBroadcastReceiver;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.filetransfer.FileTransferOperation;
import ptt.terminalsdk.manager.recordingAudio.AudioRecordStatus;
import ptt.terminalsdk.receiveHandler.ReceiveLivingStopTimeHandler;
import ptt.terminalsdk.receiveHandler.ReceiverVideoEventByPassiveHandler;
import ptt.terminalsdk.receiveHandler.ReceiverVideoPushEventByPassiveHandler;
import ptt.terminalsdk.tools.FileTransgerUtil;
import ptt.terminalsdk.tools.NetworkUtil;
import ptt.terminalsdk.tools.SDCardUtil;

import static cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState.LISTENING;
import static cn.vsx.hamster.terminalsdk.model.BitStarFileDirectory.USB;

public class MainActivity extends BaseActivity implements NFCCardReader.OnReadListener , PlayVideoStateListener, FragmentManager.OnBackStackChangedListener {
    @Bind(R.id.rl_login_bind)
    RelativeLayout rlLoginBind;
    @Bind(R.id.tv_login_info)
    TextView tvLoginInfo;
    //    @Bind(R.id.tv_bind_info)
//    TextView tvBindInfo;
    @Bind(R.id.bt_bind_state)
    TextView btBindState;

    //操作的framelayout
    @Bind(R.id.fl_content)
    FrameLayout flContent;

    //视频
    @Bind(R.id.sv_live)
    TextureView svLive;
//    private ServiceConnection conn;
//    private IBinder myIBinder;

    private Logger logger = Logger.getLogger(getClass());
    // 退出标记
    private boolean mExitFlag;
    //双击退出应用的时间间隔
    public static final int CLICK_EXIT_TIME = 3000;
    //不点击屏幕隐藏头部信息布局的时间间隔
    public static final int HIDE_INFO_LAYOUT_TIME = 3 * 1000;

    private static final String STATE = "state";
    private static final int HANDLE_CODE_OPEN_CAMERA = -1;
    private static final int HANDLE_CODE_MSG_STATE = 0;
    private static final int HANDLE_CODE_HIDE_INFO_LAYOUT = 1;
    //定时写入上报时间点
    private static final int HANDLE_WRITE_AUTO_PUSH_INTERVAL_TIME = 2;
    //上报最长时间延长时间
    private static final int HANDLE_LIVING_STOP_TIME_DELAY_TIME = 3;
    private static final int WRITE_AUTO_PUSH_INTERVAL_TIME = 10 * 1000;
    //间隔时间
    private static final int AUTO_PUSH_INTERVAL_TIME = 10 * 60 * 1000;

    BITMediaStream mMediaStream;
    List<String> listResolution;
    List<String> listResolutionName;

//    private BITBackgroundCameraService mService;
//    public boolean isBinded = false;
    int width = 640, height = 480;

    private WindowManager windowManager;

    private boolean isPushing;//正在上报
    private boolean isPassiveReport;//是否是被动上报(用于区分提示音)
    private String ip;
    private String port;
    private String id;//自己发起直播返回的callId和member拼接
    private PushCallback pushCallback;

    private List<VideoMember> watchLiveList = new ArrayList<>();//加入观看人员的集合

    private boolean canTakePicture = true;//是否可以拍照
    private TimerTask takePictureTimer;

    ArrayList<String> availableIPlist = new ArrayList<>();

    private NFCCardReader nfcCardReader = new NFCCardReader(this);
    public static int READER_FLAGS =
            NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;

    private BatteryBroadcastReceiver batteryBroadcastReceiver;
    private UsbConnetStateReceiver usbConnetStateReceiver;
    private LivingStopTimeDialog livingStopTimeDialog;
    private Fragment currentFragment;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_main;
    }

    @SuppressLint("InvalidWakeLockTag")
    @Override
    public void initData() {
        svLive.setVisibility(View.GONE);
        svLive.setOpaque(false);
        svLive.setSurfaceTextureListener(new SurfaceTextureListener());

        windowManager = (WindowManager) this.getSystemService(WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        initResolution();

        startService(new Intent(MainActivity.this, LockScreenService.class));
        MyApplication.instance.startPTTButtonEventService("");
        MyTerminalFactory.getSDK().getVideoProxy().setActivity(this);

        MyTerminalFactory.getSDK().putParam(Params.ANDROID_BUILD_MODEL, Build.MODEL);

        judgePermission();

        //清理数据库
        FileTransferOperation manager = MyTerminalFactory.getSDK().getFileTransferOperation();
        //48小时未上传的文件上传
        manager.checkStartExpireFileAlarm();
        //上传没有上传的文件信息
        manager.uploadFileTreeBean(null);
//        //检测内存卡的size
//        manager.checkExternalUsableSize();
        showLoginAndBindUI(Constants.LOGIN_BIND_STATE_IDLE);
        enableReaderMode();
        //停止上报最长时长的倒计时
        cancelLivingStopAlarmAlarmManager();
        myHandler.removeMessages(HANDLE_LIVING_STOP_TIME_DELAY_TIME);

//        //初始化红外的开关的状态
//        initInfraRedState();
    }

    @Override
    public void initView() {
    }

    @Override
    public void initListener() {
        try{
            MyTerminalFactory.getSDK().registReceiveHandler(receiveReaponseMyselfLiveHandler);//直播成功
            MyTerminalFactory.getSDK().registReceiveHandler(receiveGetVideoPushUrlHandler);//自己发起直播的响应
            TerminalFactory.getSDK().registReceiveHandler(receiveNetworkChangeHandler);//真实网络连接状态
            MyTerminalFactory.getSDK().registReceiveHandler(receiveOnLineStatusChangedHandler);//网络连接状态
            MyTerminalFactory.getSDK().registReceiveHandler(receiveSendUuidResponseHandler);//认证
            MyTerminalFactory.getSDK().registReceiveHandler(receiveReturnAvailableIPHandler);//可用的ip
            MyTerminalFactory.getSDK().registReceiveHandler(receiveRegistCompleteHandler);//注册
            MyTerminalFactory.getSDK().registReceiveHandler(receiveLoginResponseHandler);//登录
            MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateAllDataCompleteHandler);//更新数据
            MyTerminalFactory.getSDK().registReceiveHandler(receiveChangeGroupHandler);//转组
            MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallIncommingHandler);//组呼来了
            MyTerminalFactory.getSDK().registReceiveHandler(receiveReaponseStartLiveHandler);//请求时，对方拒绝
            MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyLivingStoppedHandler);//通知停止直播
            MyTerminalFactory.getSDK().registReceiveHandler(receiveAnswerLiveTimeoutHandler);//直播应答超时
            MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyEmergencyVideoLiveIncommingMessageHandler);//请求开视频(紧急)
            MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyLivingIncommingHandler);//请求开视频
            MyTerminalFactory.getSDK().registReceiveHandler(receiveNobodyRequestVideoLiveHandler);//对方取消请求
            MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyOtherStopVideoMessageHandler);//收到停止上报的通知
            MyTerminalFactory.getSDK().registReceiveHandler(receiverStopAllBusniessHandler);//收到停止一切业务的通知
            MyTerminalFactory.getSDK().registReceiveHandler(receiverCameraButtonEventHandler);//视频实体按钮上报和录像视频
            MyTerminalFactory.getSDK().registReceiveHandler(receiverPhotoButtonEventHandler);//拍照实体按钮
            MyTerminalFactory.getSDK().registReceiveHandler(receiverAudioButtonEventHandler);//录音实体按钮
            MyTerminalFactory.getSDK().registReceiveHandler(receiveMemberJoinOrExitHandler);//观看人员的加入或者退出
            MyTerminalFactory.getSDK().registReceiveHandler(mReceiveExternStorageSizeHandler);//通知存储空间不足
            MyTerminalFactory.getSDK().registReceiveHandler(receiveForceOfflineHandler);//终端强制下线
            OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiverFragmentShowHandler);//收到fragment show
            OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiverFragmentPopBackStackHandler);//收到fragment PopBackStack
            OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiverFragmentClearHandler);//收到fragment clear
            OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiverStopBusniessHandler);//停止一切业务
//            OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiveVolumeOffCallHandler);//控制音量
            MyTerminalFactory.getSDK().registReceiveHandler(receiveJoinInWarningGroupPushLiveHandler);//绑定同一个警务通账号，切组到警情组时，上报图像
            MyTerminalFactory.getSDK().registReceiveHandler(receiveLivingStopTimeHandler);//收到上报停止时间到时的通知
            MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyWarnLivingTimeoutMessageHandler);//终端上报即将超时提醒
            MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseSetLivingTimeMessageHandler);//响应设置终端上报时长
            MyTerminalFactory.getSDK().registReceiveHandler(receiverUpdateInfraRedHandler);
            MyTerminalFactory.getSDK().registReceiveHandler(receiverUsbConnetStateHandler);//USB连接状态
            MyTerminalFactory.getSDK().registReceiveHandler(receiverNfcStatusHandler);//NFC状态
            MyTerminalFactory.getSDK().registReceiveHandler(receiverVideoEventByPassiveHandler);//被动接受命令，执行录像
            MyTerminalFactory.getSDK().registReceiveHandler(receiverVideoPushEventByPassiveHandler);//被动接受命令，执行上报视频
            batteryBroadcastReceiver = new BatteryBroadcastReceiver();
            usbConnetStateReceiver = new UsbConnetStateReceiver();
            registBatterBroadcastReceiver(batteryBroadcastReceiver);//注册电量的广播
            registUsbConnetStateReceiver(usbConnetStateReceiver);//注册电量的广播
            initPhoneStateListener();//初始化手机信号的监听
            registPhoneStateListener();//注册手机信号的监听
            DeviceUtil.registListener();
            getSupportFragmentManager().addOnBackStackChangedListener(this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerHeadsetPlugReceiver();
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            //文件
            File file = new File(Environment.getExternalStorageDirectory()
                    + File.separator + MyApplication.instance.getApplicationInfo()
                    .loadLabel(MyApplication.instance.getPackageManager()) + File.separator + "logs"
                    + File.separator + "log.txt");
            if (!file.exists()) {
                SpecificSDK.getInstance().configLogger();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        onRecordAudioDenied = false;
        onLocationDenied = false;
        onCameraDenied = false;

        //清楚所有通知
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        enableReaderMode();

        FileTransferOperation operation = MyTerminalFactory.getSDK().getFileTransferOperation();
        boolean onlyUserSdCard = operation.checkOnlyUseSdCardStorage();
        if (onlyUserSdCard &&!TerminalFactory.getSDK().checkeExternalStorageIsAvailable(operation.getExternalUsableStorageDirectory())) {
            ToastUtil.showToast(MyApplication.instance, "没有找到存储卡");
            PromptManager.getInstance().startExternNoFind();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        disableReaderMode();
    }

    @Override
    public void doOtherDestroy() {
        try{
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiveReaponseMyselfLiveHandler);//直播成功
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetVideoPushUrlHandler);//自己发起直播的响应
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiveSendUuidResponseHandler);//认证
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiveReturnAvailableIPHandler);//可用的ip
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiveRegistCompleteHandler);//注册
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiveLoginResponseHandler);//登录
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateAllDataCompleteHandler);//更新数据
            TerminalFactory.getSDK().unregistReceiveHandler(receiveNetworkChangeHandler);//真实网络连接状态
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiveOnLineStatusChangedHandler);//网络连接状态
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiveChangeGroupHandler);//转组
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallIncommingHandler);//组呼来了
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiveReaponseStartLiveHandler);//请求时，对方拒绝
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyLivingStoppedHandler);//通知停止直播
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiveAnswerLiveTimeoutHandler);//直播应答超时
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyEmergencyVideoLiveIncommingMessageHandler);//请求开视频(紧急)
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyLivingIncommingHandler);//请求开视频
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNobodyRequestVideoLiveHandler);//对方取消请求
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyOtherStopVideoMessageHandler);//收到停止上报的通知
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiverStopAllBusniessHandler);//收到停止一切业务的通知
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiverCameraButtonEventHandler);//视频实体按钮上报和录像视频
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiverPhotoButtonEventHandler);//拍照实体按钮
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiverAudioButtonEventHandler);//录音实体按钮
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMemberJoinOrExitHandler);//观看人员的加入或者退出
            MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveExternStorageSizeHandler);//通知存储空间不足
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiveForceOfflineHandler);//终端强制下线
            OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiverFragmentShowHandler);//收到fragment show
            OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiverFragmentPopBackStackHandler);//收到fragment PopBackStack
            OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiverFragmentClearHandler);//收到fragment clear
            OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiverStopBusniessHandler);//停止一切业务
//            OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiveVolumeOffCallHandler);//控制音量
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiveJoinInWarningGroupPushLiveHandler);//绑定同一个警务通账号，切组到警情组时，上报图像
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiveLivingStopTimeHandler);//收到上报停止时间到时的通知
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyWarnLivingTimeoutMessageHandler);//终端上报即将超时提醒
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseSetLivingTimeMessageHandler);//响应设置终端上报时长
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiverUpdateInfraRedHandler);
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiverNfcStatusHandler);//USB连接
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiverVideoEventByPassiveHandler);//被动接受命令，执行录像
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiverVideoPushEventByPassiveHandler);//被动接受命令，执行上报视频
            myHandler.removeCallbacksAndMessages(null);
            PromptManager.getInstance().stopRing();
            stopService(new Intent(MainActivity.this, LockScreenService.class));
            MyTerminalFactory.getSDK().getVideoProxy().start().unregister(this);
            MyTerminalFactory.getSDK().unregistNetworkChangeHandler();
            unregisterHeadsetPlugReceiver();
            unRegistBatterBroadcastReceiver(batteryBroadcastReceiver);//注销电量的广播
            unRegistUsbConnetStateReceiver(usbConnetStateReceiver);//注销USB的广播
            unRegistPhoneStateListener();//注册手机信号的监听
            DeviceUtil.unRegistListener();
            DeviceUtil.stopInfraRed();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @OnClick({R.id.sv_live, R.id.tv_login_info, R.id.bt_bind_state})
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.sv_live:
                try {
                    if(mMediaStream!=null){
                        Camera camera = mMediaStream.getCamera();
                        if (camera != null){
                            camera.autoFocus(null);//屏幕聚焦
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //显示头部的信息布局
                showTopInfoLayout();
                break;
            case R.id.tv_login_info:
                //认证
                if (btBindState.getVisibility() == View.GONE && TerminalFactory.getSDK().getAuthManagerTwo().needLogin()) {
                    TerminalFactory.getSDK().notifyReceiveHandler(ReceiveRecorderLoginHandler.class);
                }
                break;
            case R.id.bt_bind_state:
                //绑定或者解绑
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverFragmentShowHandler.class, Constants.FRAGMENT_TAG_BIND,new Bundle());
                break;
                default:break;
        }
    }

    public Handler myHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLE_CODE_OPEN_CAMERA:
                    startCamera();
                    break;
                case HANDLE_CODE_MSG_STATE:
                    String state = msg.getData().getString(STATE);
                    ToastUtil.showToast(getApplicationContext(), state);
                    break;
                case HANDLE_CODE_HIDE_INFO_LAYOUT:
                    //隐藏头部的布局
                    myHandler.removeMessages(HANDLE_CODE_HIDE_INFO_LAYOUT);
                    rlLoginBind.setVisibility(View.GONE);
                    break;
                case HANDLE_WRITE_AUTO_PUSH_INTERVAL_TIME:
                    removeMessages(HANDLE_WRITE_AUTO_PUSH_INTERVAL_TIME);
                    TerminalFactory.getSDK().putParam(Params.RECORDER_AUTO_PUSH_INTERVAL_TIME, System.currentTimeMillis());
                    sendEmptyMessageDelayed(HANDLE_WRITE_AUTO_PUSH_INTERVAL_TIME, WRITE_AUTO_PUSH_INTERVAL_TIME);
                    break;
                case HANDLE_LIVING_STOP_TIME_DELAY_TIME:
                    removeMessages(HANDLE_LIVING_STOP_TIME_DELAY_TIME);
                    TerminalFactory.getSDK().notifyReceiveHandler(ReceiveLivingStopTimeHandler.class);
                    break;
                    default:break;
            }
        }
    };

    /**
     * 信令服务发送NotifyForceRegisterMessage消息时，先去reAuth(false)，然后login()
     */
    private ReceiveSendUuidResponseHandler receiveSendUuidResponseHandler = (resultCode, resultDesc, isRegisted) -> {
        myHandler.post(() -> {
            if (resultCode == BaseCommonCode.SUCCESS_CODE) {
//                //版本自动更新检测
//                updataVersion();
                ToastUtil.showToast(MyApplication.instance, getString(R.string.text_connecting));
            } else if (resultCode == TerminalErrorCode.DEPT_NOT_ACTIVATED.getErrorCode()) {
                ToastUtil.showToast(MyApplication.instance, getString(R.string.text_dept_not_activated));
            } else if (resultCode == TerminalErrorCode.DEPT_EXPIRED.getErrorCode()) {
                ToastUtil.showToast(MyApplication.instance, getString(R.string.text_dept_expired));
            } else if (resultCode == TerminalErrorCode.TERMINAL_TYPE_ERROR.getErrorCode()) {
                ToastUtil.showToast(MyApplication.instance, getString(R.string.text_terminal_type_error));
            } else if (resultCode == TerminalErrorCode.TERMINAL_REPEAT.getErrorCode()) {
                ToastUtil.showToast(MyApplication.instance, getString(R.string.text_terminal_repeat));
            } else if (resultCode == TerminalErrorCode.EXCEPTION.getErrorCode()) {
                if (NetworkUtil.isConnected(MainActivity.this)) {
                    myHandler.postDelayed(() -> {
                        //发生异常的时候重试几次，因为网络原因经常导致一个io异常
                        TerminalFactory.getSDK().getAuthManagerTwo().startAuthByDefaultAddress();
                    }, 5 * 1000);
                }
                ToastUtil.showToast(MyApplication.instance, TextUtils.isEmpty(resultDesc) ? getString(R.string.text_auth_error) : resultDesc);
            } else if (resultCode == TerminalErrorCode.TERMINAL_FAIL.getErrorCode()) {
                ToastUtil.showToast(MyApplication.instance, TextUtils.isEmpty(resultDesc) ? getString(R.string.text_auth_error) : resultDesc);
            }else if(resultCode == TerminalErrorCode.REGISTER_ACCOUNT_DELETE.getErrorCode()
                    ||resultCode == TerminalErrorCode.REGISTER_NO_REGIST.getErrorCode()
                    ||resultCode == TerminalErrorCode.UNKNOWN_ERROR.getErrorCode()){
                ToastUtil.showToast(MyApplication.instance, TextUtils.isEmpty(resultDesc) ? getString(R.string.text_please_regist_first) : resultDesc);
            } else {
                //没有注册服务地址，去探测地址
                if (availableIPlist.isEmpty()) {
                    TerminalFactory.getSDK().getAuthManagerTwo().setChangeServer();
                    TerminalFactory.getSDK().getAuthManagerTwo().checkRegistIp();
                } else {
                    ToastUtil.showToast(MyApplication.instance, (!isRegisted) ? getString(R.string.text_please_regist_first) : resultDesc);
                }
            }
        });
    };

    /**
     * 获取可用的IP列表
     **/
    private ReceiveReturnAvailableIPHandler receiveReturnAvailableIPHandler = availableIP -> myHandler.post(() -> {
        logger.info("收到可用IP列表");
        if (availableIP.size() > 0) {
            availableIPlist.addAll(SetToListUtil.setToArrayList(availableIP));
        }
        //拿到可用IP列表，遍历取第一个
        if (availableIP != null && availableIP.size() > 0) {
            LoginModel authModel = null;
            for (Map.Entry<String, LoginModel> entry : availableIP.entrySet()) {
                authModel = entry.getValue();
            }
            if (authModel != null) {
                if (NetworkUtil.isConnected(this)) {
                    int resultCode = TerminalFactory.getSDK().getAuthManagerTwo().startAuth(authModel.getIp(), authModel.getPort());
                    if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                        ToastUtil.showToast(MyApplication.instance, getString(R.string.text_authing));
                    } else {
                        //状态机没有转到正在认证，说明已经在状态机中了，不用处理
                    }
                } else {
                    ToastUtil.showToast(MyApplication.instance, getString(R.string.text_network_disconnect));
                }
            }
        } else {
            ToastUtil.showToast(MyApplication.instance, getString(R.string.text_server_ip_no_used));
        }
    });

    /**
     * 注册完成的消息
     */
    private ReceiveRegistCompleteHandler receiveRegistCompleteHandler = (errorCode, errorDesc) -> myHandler.post(() -> {
        if (errorCode == BaseCommonCode.SUCCESS_CODE) {//注册成功，直接登录
            logger.info("注册完成的回调----注册成功，直接登录");
            ToastUtil.showToast(MyApplication.instance, getString(R.string.text_logging));
        } else {//注册失败，提示并关界面
            if (errorCode == TerminalErrorCode.REGISTER_PARAMETER_ERROR.getErrorCode()) {
                ToastUtil.showToast(MyApplication.instance, getString(R.string.text_invitation_code_error_regist_again));
            } else if (errorCode == TerminalErrorCode.REGISTER_UNKNOWN_ERROR.getErrorCode()) {
                ToastUtil.showToast(MyApplication.instance, getString(R.string.text_regist_error_please_check));
            } else {
                ToastUtil.showToast(MyApplication.instance, errorDesc);
            }
        }
    });


    /**
     * 登陆响应的消息
     */
    private ReceiveLoginResponseHandler receiveLoginResponseHandler = (resultCode, resultDesc) -> {
        logger.info("MainActivity---收到登录的消息---resultCode:" + resultCode + "     resultDesc:" + resultDesc);
        if (resultCode == BaseCommonCode.SUCCESS_CODE) {
            ToastUtil.showToast(MyApplication.instance, getString(R.string.text_updatting));
        } else if (resultCode == Params.JOININ_WARNING_GROUP_ERROR_CODE || resultCode == Params.JOININ_GROUP_ERROR_CODE) {
            ToastUtil.showToast(MyApplication.instance, resultDesc);
        } else {
            ToastUtil.showToast(MyApplication.instance, resultDesc);
        }
    };

    /**
     * 更新所有数据信息的消息
     */
    private ReceiveUpdateAllDataCompleteHandler receiveUpdateAllDataCompleteHandler = (errorCode, errorDesc) ->  {
        if (errorCode == BaseCommonCode.SUCCESS_CODE) {
            if (cn.vsx.hamster.terminalsdk.tools.DataUtil.getRecorderBindBean() == null) {
                ToastUtil.showToast(MyApplication.instance, getString(R.string.text_login_success));
            }else{
                //检查是否有执行的业务，如果有就执行
                MyTerminalFactory.getSDK().getNfcManager().performBusinessByTime();
            }
            //更新UI
            RecorderBindBean bean = cn.vsx.hamster.terminalsdk.tools.DataUtil.getRecorderBindBean();
            myHandler.post(() ->{
                showLoginAndBindUI((bean == null) ? Constants.LOGIN_BIND_STATE_LOGIN : Constants.LOGIN_BIND_STATE_BIND);
            });

            //自动上报
//            myHandler.postDelayed(() -> {
//                if (checkCanAutoStartLive()) {
//                    requestStartLive();
//                }
//            }, 2000);
        } else {
            ToastUtil.showToast(MyApplication.instance, errorDesc);
        }
    };

    /**
     * 转组消息
     */
    private ReceiveChangeGroupHandler receiveChangeGroupHandler = (errorCode, errorDesc) -> {
        if (errorCode == 0 || errorCode == SignalServerErrorCode.INVALID_SWITCH_GROUP.getErrorCode()) {
            myHandler.post(() -> {
                List<Group> list = TerminalFactory.getSDK().getConfigManager().getAllListenerGroupExceptCurrentGroup();
                List<Integer> cancelList = new ArrayList<>();
                for (Group group : list) {
                    if (group != null) {
                        cancelList.add(group.getNo());
                    }
                }
                if (!cancelList.isEmpty()) {
                    MyTerminalFactory.getSDK().getGroupManager().setMonitorGroup(cancelList, false);
                }
            });
        }
    };

    /**
     * 被动方组呼来了
     */
    private ReceiveGroupCallIncommingHandler receiveGroupCallIncommingHandler = (memberId, memberName, groupId, version, currentCallMode, uniqueNo) -> {
        if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_LISTEN.name())) {
            ptt.terminalsdk.tools.ToastUtil.showToast(getApplicationContext(), "没有组呼听的权限");
        }
        PromptManager.getInstance().groupCallCommingRing();
        logger.info("组呼来了");
        myHandler.post(() -> {
            //是组扫描的组呼,且当前组没人说话，变文件夹和组名字
            if (groupId != MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)) {
                logger.info("扫描组组呼来了");
            }
            //是当前组的组呼,且扫描组有人说话，变文件夹和组名字
            if (groupId == MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0) && MyApplication.instance.getGroupListenenState() == LISTENING) {
                logger.info("当前组组呼来了");
            }
            MyTerminalFactory.getSDK().putParam(Params.CURRENT_SPEAKER, memberName);
        });

    };

    /**
     * 真实网络
     */
    private ReceiveNetworkChangeHandler receiveNetworkChangeHandler = connected -> {
//        if(!connected){
//        }else {
//            if(!TerminalFactory.getSDK().isServerConnected()){
//                //网络连接上了，心跳断开了，需要重新登陆，等待心跳连接成功
//                if(TerminalFactory.getSDK().getAuthManagerTwo().needLogin()){
//                    TerminalFactory.getSDK().notifyReceiveHandler(ReceiveRecorderLoginHandler.class);
//                }
//            }
//        }
    };

    /**
     * 网络连接状态
     */
    private ReceiveOnLineStatusChangedHandler receiveOnLineStatusChangedHandler = connected -> {
        logger.info("主界面收到服务是否连接的通知ReceiveOnLineStatusChangedHandler--" + connected);
        MainActivity.this.runOnUiThread(() -> {
            if (!connected) {
//                TerminalFactory.getSDK().getAuthManagerTwo().getLoginStateMachine().stop();
                ToastUtil.showToast(MyApplication.instance, getString(R.string.text_network_is_disconnect));
//                stopPush(false);
//                showLoginAndBindUI(Constants.LOGIN_BIND_STATE_IDLE);
            } else {
                //上传未上传的文件信息
                MyTerminalFactory.getSDK().getFileTransferOperation().uploadFileTreeBean(null);
            }
        });
    };


    /**
     * 获取上报地址
     **/
    private ReceiveGetVideoPushUrlHandler receiveGetVideoPushUrlHandler = new ReceiveGetVideoPushUrlHandler() {
        @Override
        public void handler(final String streamMediaServerIp, final int streamMediaServerPort, final long callId) {
            TerminalFactory.getSDK().notifyReceiveHandler(ReceiveUpdateUploadFileRateLimitHandler.class, true);
            myHandler.postDelayed(() -> {
                logger.info("自己发起直播，服务端返回的ip：" + streamMediaServerIp + "端口：" + streamMediaServerPort + "---callId:" + callId);
                ip = streamMediaServerIp;
                port = streamMediaServerPort + "";
                boolean isPreFourVersion = TerminalFactory.getSDK().getParam(Params.IS_PREFOUR_VERSION,true);
                id = TerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO, 0L) + (isPreFourVersion?"_"+callId:"");
                startPush(streamMediaServerIp, port, id);
            }, 1000);
        }
    };

    /**
     * 自己发起直播的响应
     **/
    private ReceiveResponseMyselfLiveHandler receiveReaponseMyselfLiveHandler = new ReceiveResponseMyselfLiveHandler() {
        @Override
        public void handler(final int resultCode, final String resultDesc) {
            myHandler.post(() -> {
                if (resultCode == 0) {
                    isPushing = true;
                    updateNormalPushingState(isPushing);
                    pushRelationGroup();
                } else if (resultCode == 4308) {
                    //已经在上报
                    requestStartLive();
                } else {
                    isPushing = false;
                    updateNormalPushingState(isPushing);
                    ToastUtil.showToast(getApplicationContext(), resultDesc);
                    finishVideoLive();
                }
            });
        }
    };

    /**
     * 对方拒绝直播，通知界面关闭响铃页
     **/
    private ReceiveResponseStartLiveHandler receiveReaponseStartLiveHandler = (resultCode, resultDesc, liveMemberId, liveUniqueNo) -> {
        ToastUtil.showToast(getApplicationContext(), resultDesc);
        updateNormalPushingState(false);
        finishVideoLive();
    };

    /**
     * 通知直播停止 通知界面关闭视频页
     **/
    private ReceiveNotifyLivingStoppedHandler receiveNotifyLivingStoppedHandler = (liveMemberId, callId, methodResult, resultDesc) -> {
        ToastUtil.showToast(getApplicationContext(), "上报已结束");
        updateNormalPushingState(false);
        finishVideoLive();
    };

    /**
     * 超时未回复answer 通知界面关闭
     **/
    private ReceiveAnswerLiveTimeoutHandler receiveAnswerLiveTimeoutHandler = () -> {
        ToastUtil.showToast(getApplicationContext(), "对方已取消");
        updateNormalPushingState(false);
        finishVideoLive();
    };

    /**
     * 收到强制上报图像的通知
     */
    @SuppressWarnings("unchecked")
    private ReceiveNotifyEmergencyVideoLiveIncommingMessageHandler receiveNotifyEmergencyVideoLiveIncommingMessageHandler = message -> myHandler.post(() -> {
        //开启上报功能
        myHandler.postDelayed(() -> TerminalFactory.getSDK().getLiveManager().openFunctionToLivingIncomming(message), 1000);
    });

    /**
     * 收到别人请求我开启直播的通知
     **/
    private ReceiveNotifyLivingIncommingHandler receiveNotifyLivingIncommingHandler = new ReceiveNotifyLivingIncommingHandler() {
        @Override
        public void handler(String mainMemberName, int mainMemberId, boolean emergencyType) {
            myHandler.post(() -> {
                //如果正在拍摄视频时
                if (APPStateUtil.isBackground(getApplicationContext())) {//程序处于后台
                    //                        sendBroadcast(new Intent("MainActivityfinish"));
                    logger.info("main程序拿到前台");
                    //无屏保界面
                    APPStateUtil.setTopApp(MainActivity.this);
                }
                if (wakeLockComing != null) {
                    wakeLockComing.acquire();
                }
                //自动接收上报视频
                MyTerminalFactory.getSDK().getLiveManager().responseLiving(true);
                MyApplication.instance.isPrivateCallOrVideoLiveHand = true;
                isPassiveReport = true;
            });
        }
    };

    /**
     * 收到没人请求我开视频的消息，关闭界面和响铃
     */
    private ReceiveNobodyRequestVideoLiveHandler receiveNobodyRequestVideoLiveHandler = () -> {
        ToastUtil.showToast(getApplicationContext(), "对方已取消");
        finishVideoLive();
    };

    /**
     * 收到上报停止的通知
     */
    private ReceiveNotifyOtherStopVideoMessageHandler receiveNotifyOtherStopVideoMessageHandler = (message) -> {
        logger.info("收到停止上报通知");
        ToastUtil.showToast(getApplicationContext(), "收到停止上报的通知");
        stopPushAndRecord();
    };

    /**
     * 收到上报一切业务的通知
     */
    private ReceiverStopAllBusniessHandler receiverStopAllBusniessHandler = (showMessage) -> {
        logger.info("收到停止上报通知");
        if (showMessage) {
            ToastUtil.showToast(getApplicationContext(), "收到停止业务的通知");
        }
        updateNormalPushingState(false);
        stopBusniess();
        //修改账号信息的布局
        myHandler.post(() -> showLoginAndBindUI(Constants.LOGIN_BIND_STATE_IDLE));
    };

    /**
     * 视频实体按钮，上报视频
     */
    private ReceiverVideoButtonEventHandler receiverCameraButtonEventHandler = new ReceiverVideoButtonEventHandler() {
        @Override
        public void handler(boolean isLongPress) {
            if (ApkUtil.isXinZhou()) {
                isPressFromXinZhou(isLongPress);
            } else {
                isPressFromOther(isLongPress);
            }
            TerminalFactory.getSDK().notifyReceiveHandler(ReceiverUpdatePlayVideoStateHandler.class);
        }
    };

    /**
     * 图像按钮-新洲
     */
    private void isPressFromXinZhou(boolean isLongPress) {
        if (isLongPress) {
            //当前没有录像，开始录像
            startVideoRecoder();
        } else {
            //录像  （判断当前的状态，如果已经在录像，停止录像）
            if (mMediaStream != null) {
                if (mMediaStream.isStreaming()) {
                    stopVideoPush();
                } else if (mMediaStream.isRecording()) {
                    //已经在录像，停止录像
                    stopVideoRecoder();
                } else {
                    //上报视频
                    logger.info("视频实体按钮，上报视频");
                    startVideoPush();
                }
            }
        }
    }
    /**
     * 图像按钮-其他
     */
    private void isPressFromOther(boolean isLongPress) {
        if (isLongPress) {
            //上报视频
            logger.info("视频实体按钮，上报视频");
            startVideoPush();
        } else {
            //录像  （判断当前的状态，如果已经在录像，停止录像）
            if (mMediaStream != null) {
                if (mMediaStream.isStreaming()) {
                    stopVideoPush();
                } else if (mMediaStream.isRecording()) {
                    //已经在录像，停止录像
                    stopVideoRecoder();
                } else {
                    //当前没有录像，开始录像
                    startVideoRecoder();
                }
            }
        }
    }

    /**
     * 执行开始录像的操作
     */
    private void startVideoRecoder(){
        if (mMediaStream != null) {
            if (mMediaStream.isRecording()) {
                ToastUtil.showToast(MyApplication.instance, "录像中");
            } else {
                FileTransferOperation operation = MyTerminalFactory.getSDK().getFileTransferOperation();
                boolean onlyUserSdCard = operation.checkOnlyUseSdCardStorage();
                if(!onlyUserSdCard){
                    operation.checkExternalUsableSize();
                }
                if (TerminalFactory.getSDK().checkeExternalStorageIsAvailable(operation.getExternalUsableStorageDirectory())) {
                    ToastUtil.showToast(MyApplication.instance, "开始录像");
                    //判断是否是用其他提示音
                    promptStartVideoRecoder();
                    mMediaStream.startRecord();
                } else {
                    if(onlyUserSdCard){
                        ToastUtil.showToast(MyApplication.instance, "没有找到存储卡");
                        PromptManager.getInstance().startExternNoFind();
                    }else{
                        ToastUtil.showToast(MyApplication.instance, "存储空间不可用");
                    }
                }
            }
        }
    }

    /**
     * 停止录像
     */
    private void stopVideoRecoder() {
        if (mMediaStream != null) {
            if (mMediaStream.isRecording()) {
                ToastUtil.showToast(MyApplication.instance, "停止录像");
                promptStopVideoRecoder();
                mMediaStream.stopRecord();
            }
        }
    }

    /**
     * 上报图像
     */
    private void startVideoPush(){
        if (!TerminalFactory.getSDK().getAuthManagerTwo().isOnLine()) {
            ToastUtil.showToast(MyApplication.instance, getString(R.string.text_no_login_can_not_push));
            return;
        }
        if (mMediaStream != null) {
            if (mMediaStream.isStreaming()) {
                ToastUtil.showToast(MyApplication.instance, "上报中");
            } else {
                if (MyTerminalFactory.getSDK().getRecordingAudioManager().getStatus() != AudioRecordStatus.STATUS_STOPED) {
                    stopRecordAudio();
                }
                requestStartLive();
            }
        }
    }

    /**
     *停止上报图像
     */
    private void stopVideoPush(){
        if (mMediaStream != null) {
            if (mMediaStream.isStreaming()) {
                updateNormalPushingState(false);
                stopPush(true);
                if (mMediaStream.isRecording()) {
                    mMediaStream.stopRecord();
                }
                ToastUtil.showToast(MyApplication.instance, "停止上报");
            }
        }
    }
    /**
     * 拍照实体按钮
     */
    private ReceiverPhotoButtonEventHandler receiverPhotoButtonEventHandler = new ReceiverPhotoButtonEventHandler() {
        @Override
        public void handler() {
            if (mMediaStream != null) {
                FileTransferOperation operation = MyTerminalFactory.getSDK().getFileTransferOperation();
                boolean onlyUserSdCard = operation.checkOnlyUseSdCardStorage();
                //判断存储空间是否可用
                if (onlyUserSdCard && !TerminalFactory.getSDK().checkeExternalStorageIsAvailable(operation.getExternalUsableStorageDirectory())) {
                    PromptManager.getInstance().startExternNoFind();
                    ToastUtil.showToast(MyApplication.instance, "没有找到存储卡");
                    return;
                }
                int code = onlyUserSdCard?BitStarFileDirectory.SDCARD.getCode():operation.getExternalUsableStorageDirectory();
                long usb = MyTerminalFactory.getSDK().getExternalUsableSize(USB.getCode()) / 1024 / 1024;
                long sdCard = MyTerminalFactory.getSDK().getExternalUsableSize(BitStarFileDirectory.SDCARD.getCode()) / 1024 / 1024;
                long memorySize = (code == USB.getCode()) ? usb : sdCard;
                boolean isNeedNotify = onlyUserSdCard ?( sdCard < 200):(usb < 200 && sdCard < 200);
                boolean minSize = onlyUserSdCard?(sdCard < 3):(usb < 3 && sdCard < 3);
                if (minSize) {
                    PromptManager.getInstance().startExternNoStorage();
                    ToastUtil.showToast(MyApplication.instance, "存储空间不足");
                    //上传没有上传的文件，删除已经上传的文件
                    operation.externNoStorageOperation();
                    return;
                } else if (isNeedNotify) {
                    PromptManager.getInstance().startExternStorageNotEnough();
                    ToastUtil.showToast(MyApplication.instance, "存储空间告急");
                }
                //切换空间
                if (!onlyUserSdCard && memorySize < 3) {
                    boolean success = operation.changeExternalStorage(code, usb, sdCard, 3);
                    if (!success) {
                        ToastUtil.showToast(MyApplication.instance, "存储空间不足");
                        //上传没有上传的文件，删除已经上传的文件
                        operation.externNoStorageOperation();
                        return;
                    }
                }
                //拍照
                if (canTakePicture && System.currentTimeMillis() - MyApplication.instance.clicktime > 3000) {
                    canTakePicture = false;
                    if (takePictureTimer != null) {
                        takePictureTimer.cancel();
                        takePictureTimer = null;
                    }
                    takePicture();
                    takePictureTimer = new TimerTask() {
                        @Override
                        public void run() {
                            canTakePicture = true;
                        }
                    };
                    MyTerminalFactory.getSDK().getTimer().schedule(takePictureTimer, 3000L);
                }
                ToastUtil.showToast(MyApplication.instance, "拍照");
            }
        }
    };

    /**
     * 录音实体按钮，录音
     */
    private ReceiverAudioButtonEventHandler receiverAudioButtonEventHandler = isLongPress -> {
        if (isLongPress) {
            //长按
            if (MyTerminalFactory.getSDK().getRecordingAudioManager().getStatus() == AudioRecordStatus.STATUS_STOPED) {
                FileTransferOperation operation = MyTerminalFactory.getSDK().getFileTransferOperation();
                boolean onlyUserSdCard = operation.checkOnlyUseSdCardStorage();
                if(!onlyUserSdCard){
                    operation.checkExternalUsableSize();
                }
                //开始录音
                if (TerminalFactory.getSDK().checkeExternalStorageIsAvailable(operation.getExternalUsableStorageDirectory())) {
                    startRecordAudio();
                    PromptManager.getInstance().startRecordAudio();
                    ToastUtil.showToast(MyApplication.instance, "开始录音");
                }else{
                    if(onlyUserSdCard){
                        ToastUtil.showToast(MyApplication.instance, "没有找到存储卡");
                        PromptManager.getInstance().startExternNoFind();
                    }else{
                        ToastUtil.showToast(MyApplication.instance, "存储空间不可用");
                    }
                }
            } else {
                ToastUtil.showToast(MyApplication.instance, "录音中");
            }
        } else {
            //短按-停止录音
            stopRecordAudio();
            PromptManager.getInstance().stopRecordAudio();
            ToastUtil.showToast(MyApplication.instance, "停止录音");
        }
        TerminalFactory.getSDK().notifyReceiveHandler(ReceiverUpdatePlayVideoStateHandler.class);
    };

    /**
     * 观看成员的进入和退出
     **/
    @SuppressLint("SimpleDateFormat")
    private ReceiveMemberJoinOrExitHandler receiveMemberJoinOrExitHandler = (memberName, memberId, joinOrExit) -> myHandler.post(() -> {
        Log.e("IndividualCallService", memberName + ",memberId:" + memberId);
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        Date currentTime = new Date();
        String enterTime = formatter.format(currentTime);
        VideoMember videoMember = new VideoMember(memberId, memberName, enterTime, joinOrExit);
        if (joinOrExit) {//进入直播间
            watchLiveList.add(videoMember);
        } else {//退出直播间
            int position = -1;
            for (int i = 0; i < watchLiveList.size(); i++) {
                if (watchLiveList.get(i).getId() == memberId) {
                    position = i;
                }
            }
            if (position != -1) {
                watchLiveList.remove(position);
            }
//            if(watchLiveList.size()<1){
//                stopPush();
//            }
        }
    });

    /**
     * 通知存储空间不足
     */
    private ReceiveExternStorageSizeHandler mReceiveExternStorageSizeHandler = new ReceiveExternStorageSizeHandler() {
        @Override
        public void handler(final long memorySize) {
            myHandler.post(() -> {
                if (memorySize < 100) {
                    ToastUtil.showToast(MyApplication.instance, "存储空间不足");
                    PromptManager.getInstance().startExternNoStorage();
                    if(mMediaStream!=null){
                        mMediaStream.stopRecord();
                    }
                    //停止录音
                    stopRecordAudio();
                    //上传没有上传的文件，删除已经上传的文件
                    MyTerminalFactory.getSDK().getFileTransferOperation().externNoStorageOperation();
                    TerminalFactory.getSDK().notifyReceiveHandler(ReceiverUpdatePlayVideoStateHandler.class);
                } else if (memorySize < 200) {
                    PromptManager.getInstance().startExternStorageNotEnough();
                    ToastUtil.showToast(MyApplication.instance, "存储空间告急");
                }
            });
        }
    };

    /**
     * 终端强制下线
     */
    private ReceiveForceOfflineHandler receiveForceOfflineHandler = () -> {
        ToastUtil.showToast(MyApplication.instance, getResources().getString(R.string.force_off_line));
        if (cn.vsx.hamster.terminalsdk.tools.DataUtil.getRecorderBindBean() != null) {
            //绑定账号，退出绑定账号，登录默认账号
            //清空绑定信息
            cn.vsx.hamster.terminalsdk.tools.DataUtil.clearRecorderBindBean();
            //切换到绑定账号
            changeAccount();
        } else {
            //默认账号，退出应用
            myHandler.postDelayed(() -> exitApp(), 2000);
        }
    };

    /**
     * 收到fragment 显示的通知
     */
    private ReceiverFragmentShowHandler receiverFragmentShowHandler = (tag,bundle) -> {
        logger.info("ReceiverFragmentShowHandler---currentFragment:"+currentFragment);
        myHandler.post(() -> {
            Fragment mFragment = FragmentUtil.getFragmentByTag(tag,bundle);
            if (mFragment != null) {
                if (TextUtils.equals(Constants.FRAGMENT_TAG_MENU, tag)) {
                    currentFragment = null;
                    clearFragmentBackStack();
                }
                hideTopInfoLayout();
                if(currentFragment == null){
                    getSupportFragmentManager().beginTransaction().add(R.id.fl_content, mFragment, tag).addToBackStack(tag).show(mFragment).commit();
                }else{
                    getSupportFragmentManager().beginTransaction().hide(currentFragment).add(R.id.fl_content, mFragment, tag).addToBackStack(tag).show(mFragment).commit();
                }
                if(flContent!=null){
                    flContent.setVisibility(View.VISIBLE);
                }
                currentFragment = mFragment;
            }
        });
    };

    /**
     * 收到fragment popBackStack
     */
    private ReceiverFragmentPopBackStackHandler receiverFragmentPopBackStackHandler = () -> {
        logger.info("ReceiverFragmentPopBackStackHandler---getBackStackEntryCount:"+getSupportFragmentManager().getBackStackEntryCount());
        if (getSupportFragmentManager().getBackStackEntryCount() != 0) {
            popBackStack();
        }
    };

    /**
     * 更新红外状态的UI
     */
    private ReceiverUpdateInfraRedHandler receiverUpdateInfraRedHandler = new ReceiverUpdateInfraRedHandler() {
        @Override
        public void handler(boolean open) {
            if(mMediaStream!=null){
                Camera camera = mMediaStream.getCamera();
                if (camera != null) {
                    Camera.Parameters parameters = camera.getParameters();
                    if (open) {
                        parameters.setColorEffect(Camera.Parameters.EFFECT_MONO);
                    } else {
                        parameters.setColorEffect(Camera.Parameters.EFFECT_NONE);
                    }
                    camera.setParameters(parameters);
                }
            }
        }
    };

    /**
     * 更新USB连接状态的
     */
    private ReceiverUsbConnetStateHandler receiverUsbConnetStateHandler = new ReceiverUsbConnetStateHandler() {
        @Override
        public void handler(boolean isConnected) {
            ToastUtil.showToast(MyApplication.instance,isConnected?"USB连接":"USB断开");
            DeviceUtil.usbConnetStatus(isConnected);
        }
    };

    /**
     * 设置nfc
     */
    private ReceiverNfcStatusHandler receiverNfcStatusHandler = new ReceiverNfcStatusHandler() {
        @Override
        public void handler(boolean open) {
            if(open){
                NfcUtil.enable(MainActivity.this);
                enableReaderMode();
            }else{
                NfcUtil.disable(MainActivity.this);
                disableReaderMode();
            }
        }
    };

    /**
     * 被动接受命令，执行录像
     */
    private ReceiverVideoEventByPassiveHandler receiverVideoEventByPassiveHandler = new ReceiverVideoEventByPassiveHandler() {
        @Override
        public void handler(int state) {
          switch (state){
              case 1:
                  //开始录像
                  startVideoRecoder();
                  break;
              case 2:
                  //结束录像
                  stopVideoRecoder();
                  break;
                  default:break;
          }
            //检查是否有执行的业务，如果有就执行
            MyTerminalFactory.getSDK().getNfcManager().performBusinessByTime();
        }
    };

    /**
     * 被动接受命令，执行上报视频
     */
    private ReceiverVideoPushEventByPassiveHandler receiverVideoPushEventByPassiveHandler = new ReceiverVideoPushEventByPassiveHandler() {
        @Override
        public void handler(int state) {
            switch (state){
                case 1:
                    //开始上报图像
                    startVideoPush();
                    break;
                case 2:
                    //结束上报图像
                    stopVideoPush();
                     break;
                default:break;
            }
            //检查是否有执行的业务，如果有就执行
            MyTerminalFactory.getSDK().getNfcManager().performBusinessByTime();
        }
    };

    /**
     * 收到fragment clear
     */
    private ReceiverFragmentClearHandler receiverFragmentClearHandler = () -> {
        try{
            if (getSupportFragmentManager().getBackStackEntryCount() != 0) {
                clearFragmentBackStack();
            }
            goneFragmentView();
        }catch (Exception e){
            e.printStackTrace();
        }
    };
    /**
     * 停止一切业务
     */
    private ReceiverStopBusniessHandler receiverStopBusniessHandler = this::stopBusniess;

    /**
     * 绑定同一个警务通账号，切组到警情组时，上报图像
     */
    private ReceiveJoinInWarningGroupPushLiveHandler receiveJoinInWarningGroupPushLiveHandler = () -> {
        if (checkCanAutoStartLive()) {
            requestStartLive();
        }
    };


    /**
     * 收到上报停止时间到时的通知
     */
    private ReceiveLivingStopTimeHandler receiveLivingStopTimeHandler = new ReceiveLivingStopTimeHandler() {
        @Override
        public void handler() {
            if (MyApplication.instance.getVideoLivePushingState() != VideoLivePushingState.IDLE) {
                //提示单次上报时长到时，没有收到结束上报的通知时
                stopPushAndRecord();
            }
        }
    };

    /**
     * 收到提醒上报停止时间到时的通知
     */
    private ReceiveNotifyWarnLivingTimeoutMessageHandler receiveNotifyWarnLivingTimeoutMessageHandler = new ReceiveNotifyWarnLivingTimeoutMessageHandler() {
        @Override
        public void handler(long livingTime, long surplusTime) {
            if (MyApplication.instance.getVideoLivePushingState() != VideoLivePushingState.IDLE) {
                PromptManager.getInstance().livingStopTime(livingTime);
                myHandler.post(() -> {
                    //弹窗提示
                    if (livingStopTimeDialog != null) {
                        livingStopTimeDialog.dismiss();
                        livingStopTimeDialog = null;
                    }
                    livingStopTimeDialog = new LivingStopTimeDialog(MainActivity.this, livingTime, surplusTime, () ->
                            //拒绝
                    {
                        ToastUtil.showToast(MyApplication.instance, getResources().getString(R.string.text_set_success));
                        myHandler.removeMessages(HANDLE_LIVING_STOP_TIME_DELAY_TIME);
                        cancelLivingStopAlarmAlarmManager();
                        startLivingStopAlarmManager(true, true);
                    });

//                            TerminalFactory.getSDK().getThreadPool().execute(() -> {
//                                TerminalFactory.getSDK().getLiveManager().requestSetLivingTimeMessage(
//                                        TerminalFactory.getSDK().getParam(Params.MAX_LIVING_TIME, 0L),true);
//                            }));
                    if (!MainActivity.this.isFinishing()) {
                        livingStopTimeDialog.show();
                        myHandler.postDelayed(() -> {
                            if (livingStopTimeDialog != null) {
                                livingStopTimeDialog.dismiss();
                                livingStopTimeDialog = null;
                            }
                        }, 15 * 1000);
                    }
                    myHandler.sendEmptyMessageDelayed(HANDLE_LIVING_STOP_TIME_DELAY_TIME, Params.LIVING_STOP_TIME_DELAY_TIME);
                });
            }
        }
    };

    /**
     * 响应设置终端上报时长的通知
     */
    private ReceiveResponseSetLivingTimeMessageHandler receiveResponseSetLivingTimeMessageHandler = new ReceiveResponseSetLivingTimeMessageHandler() {
        @Override
        public void handler(int resultCode, String resultDesc, long livingTime, boolean isResetLiving) {
            if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                //重新保存上报最长时长
                TerminalFactory.getSDK().putParam(Params.MAX_LIVING_TIME, livingTime);
                if (MyApplication.instance.getVideoLivePushingState() != VideoLivePushingState.IDLE) {
                    //防止已经提醒上报到时，5分钟后自动停止上报时，又去设置上报时长，这个时候需要停止5分钟的倒计时。
                    myHandler.removeMessages(HANDLE_LIVING_STOP_TIME_DELAY_TIME);
                    //重新设置倒计时时间
                    startLivingStopAlarmManager(true, isResetLiving);
                }
                //提示不同
                ToastUtil.showToast(MyApplication.instance, getResources().getString(R.string.text_set_success));
                //通知页面刷新
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiveResponseSetLivingTimeMessageUIHandler.class, livingTime, isResetLiving);
            } else {
                ToastUtil.showToast(MyApplication.instance, TextUtils.isEmpty(resultDesc) ? getResources().getString(R.string.text_set_fail) : resultDesc);
            }
        }
    };

    /**
     * 是否是静默播放
     * @return
     */
    @Override
    public boolean canQuitPlay() {
        if(mMediaStream != null){
            //是否在录像、上报图像
            if (mMediaStream.isRecording()||mMediaStream.isStreaming()) {
                return true;
            }
            if (MyTerminalFactory.getSDK().getRecordingAudioManager().getStatus() != AudioRecordStatus.STATUS_STOPED) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBackStackChanged() {
        resetCurrentFragment();
    }


    private final class SurfaceTextureListener implements TextureView.SurfaceTextureListener {
        @Override
        public void onSurfaceTextureAvailable(final SurfaceTexture surface, int width, int height) {
            //view第一次显示的时候回调
            logger.info("SurfaceTexture：宽" + width + "高" + height);
            //宽高就是手机屏幕的宽高
            //界面可见时，就会执行
            initMediaStream(surface);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            logger.info("onSurfaceTextureDestroyed----->" + surface);
            //界面不可见，就会销毁
            stopPushAndPreview();
            stopRecordAudio();
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            //视频一帧一帧刷新，会执行此方法
        }

    }

    private void initResolution() {
        listResolution = new ArrayList<>(Arrays.asList("1920x1080", "1280x720", "640x480", "320x240"));
        listResolutionName = new ArrayList<>(Arrays.asList("超清", "高清", "标清", "流畅"));
        logger.info("listResolution----->" + listResolution);
        //手机支持的分辨率，暂时没有用注释掉
        int position = MyTerminalFactory.getSDK().getParam(Params.VIDEO_RESOLUTION, 2);
        String r = listResolution.get(position);
        String[] splitR = r.split("x");
        width = Integer.parseInt(splitR[0]);
        height = Integer.parseInt(splitR[1]);
        logger.error("分辨率--width:" + width + "----height:" + height);

    }

    /**
     * 请求自己开始上报
     */
    @Override
    protected void requestStartLive() {
        int requestCode = MyTerminalFactory.getSDK().getLiveManager().requestMyselfLive("", "");
        logger.error("上报图像：requestCode=" + requestCode);
        if (requestCode == BaseCommonCode.SUCCESS_CODE) {
            ToastUtil.showToast(getApplicationContext(), "开始主动上报图像");
            isPassiveReport = false;
        } else {
            ToastUtil.livingFailToast(getApplicationContext(), requestCode, TerminalErrorCode.LIVING_PUSHING.getErrorCode());
        }
    }

    /**
     * 上报图像与组关联
     */
    private void pushRelationGroup() {
        RecorderBindBean bean = cn.vsx.hamster.terminalsdk.tools.DataUtil.getRecorderBindBean();
        int groupNo = (bean != null) ? bean.getGroupId() : MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);
        List<String> list = new ArrayList<>();
        list.add(DataUtil.getPushInviteMemberData(groupNo, ReceiveObjectMode.GROUP.toString()));
        logger.debug("pushRelationGroup-list" + list);
        MyTerminalFactory.getSDK().getLiveManager().requestNotifyWatch(list,
                MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0),
                TerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO, 0l));
    }

    /**
     * 开始上报
     *
     * @param ip
     * @param port
     * @param id
     */
    private void startPush(String ip, String port, String id) {
        logger.info("sjl_:" + mMediaStream + "----" + svLive.getSurfaceTexture());
        if (mMediaStream == null) {
            if (svLive.getSurfaceTexture() != null) {
                initMediaStream(svLive.getSurfaceTexture());
            } else {
                ToastUtil.showToast(getApplicationContext(), "图像上传失败");
                finishVideoLive();
                return;
            }
        }
        if (null == pushCallback) {
            pushCallback = new PushCallback();
        }
        //开始推流
        mMediaStream.startStream(ip, port, id, pushCallback);

        String sdp = TerminalFactory.getSDK().getLiveManager().getLivePathSdp();

        String url = String.format("rtsp://%s:%s/%s"+sdp, ip, port, id);
        logger.info("推送地址：" + url);
        //开始录像
        if (TerminalFactory.getSDK().checkeExternalStorageIsAvailable(MyTerminalFactory.getSDK().getFileTransferOperation().getExternalUsableStorageDirectory())) {
            mMediaStream.startRecord();
        }
        //提示音
        if (isPassiveReport) {
            //被动上报
            PromptManager.getInstance().startReportByNotity();
        } else {
            //主动上报
            PromptManager.getInstance().startReport();
        }
        //开始上报结束的倒计时
        cancelLivingStopAlarmAlarmManager();
        startLivingStopAlarmManager(false, false);
    }

    private void startCamera() {
        mMediaStream.updateResolution(width, height);
        mMediaStream.setDgree(getDgree(windowManager));
        mMediaStream.createCamera();
        mMediaStream.startPreview();
        if (mMediaStream.isStreaming()) {
            ToastUtil.showToast(this, "推流中..");
        }
    }

    /**
     * 创建直播服务
     */
    @Override
    protected void startLiveService() {
        MyTerminalFactory.getSDK().getVideoProxy().start().register(this);
        if(svLive!=null){
            svLive.setVisibility(View.VISIBLE);
        }
    }


    /**
     * 初始化MediaStream并预览
     *
     * @param surface
     */
    private void initMediaStream(SurfaceTexture surface) {
        BITMediaStream ms = mMediaStream;
        if (ms != null) {    // switch from background to front
            ms.stopPreview();
//            mService.inActivePreview();
//                ms.destroyCamera();
            ms.setSurfaceTexture(surface);
//                ms.createCamera();
            ms.startPreview();
            mMediaStream = ms;
            if (ms.isStreaming()) {
                ToastUtil.showToast(this, "推流中..");
            }
        } else {
            ms = new BITMediaStream(getApplicationContext(), surface, true, width, height);
            mMediaStream = ms;
            startCamera();
            DeviceUtil.initInfraRedState();
        }
    }

    /**
     * 停止上报
     */
    @Override
    protected void finishVideoLive() {
        myHandler.post(() -> {
            PromptManager.getInstance().stopRing();//停止响铃
            //将TextureView设置成全屏
            svLive.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            svLive.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            svLive.requestLayout();
            stopPush(false);
            ip = null;
            port = null;
            id = null;
            myHandler.removeMessages(HANDLE_WRITE_AUTO_PUSH_INTERVAL_TIME);
        });

    }

    /**
     * 停止推流
     */
    private void stopPush(boolean isReportAudio) {
        isPushing = false;
        boolean isStreaming = mMediaStream != null && mMediaStream.isStreaming();
        logger.info("isFinishing() = " + "isFinishing()" + "    isStreaming = " + isStreaming);
        if (mMediaStream != null) {
            mMediaStream.stopStream();
//        } else {
//            if (isStreaming) {
//                mService.activePreview();
//            }
        }
        watchLiveList.clear();
        TerminalFactory.getSDK().getLiveManager().ceaseLiving();
        //提示音
        isPassiveReport = false;
        if (isReportAudio) {
            PromptManager.getInstance().stopReport();
        }
        TerminalFactory.getSDK().notifyReceiveHandler(ReceiveUpdateUploadFileRateLimitHandler.class, false);
        //关闭上报结束的倒计时
        cancelLivingStopAlarmAlarmManager();
        myHandler.removeMessages(HANDLE_LIVING_STOP_TIME_DELAY_TIME);
        TerminalFactory.getSDK().notifyReceiveHandler(ReceiverUpdatePlayVideoStateHandler.class);
    }


    /**
     * 停止推流并停止预览
     */
    public void stopPushAndPreview() {
        isPushing = false;
        boolean isStreaming = mMediaStream != null && mMediaStream.isStreaming();
        if (mMediaStream != null) {
            mMediaStream.stopRecord();
            mMediaStream.stopPreview();
        } else {
            return;
        }
        logger.info("isFinishing() = " + "isFinishing()" + "    isStreaming = " + isStreaming);
        if (mMediaStream != null) {
            mMediaStream.stopStream();
            mMediaStream.release();
            mMediaStream = null;
//            if (mService != null) {
//                mService.setMediaStream(null);
//            }
//        } else {
//            if (isStreaming) {
//                mService.activePreview();
//            }
        }
        watchLiveList.clear();
        TerminalFactory.getSDK().getLiveManager().ceaseLiving();
        TerminalFactory.getSDK().notifyReceiveHandler(ReceiverUpdatePlayVideoStateHandler.class);
    }

    /**
     * 开始录音
     */
    private void startRecordAudio() {
        myHandler.postDelayed(() -> MyTerminalFactory.getSDK().getRecordingAudioManager().start((mr, what, extra) -> myHandler.post(this::stopRecordAudio)), 500);
    }

    /**
     * 停止录音
     */
    private void stopRecordAudio() {
        if (MyTerminalFactory.getSDK().getRecordingAudioManager().getStatus() == AudioRecordStatus.STATUS_RECORDING) {
            MyTerminalFactory.getSDK().getRecordingAudioManager().stop();
        }
    }

    /**
     * 拍照
     */
    private void takePicture() {
        if (mMediaStream == null ) {
            return;
        }
        Camera camera = mMediaStream.getCamera();
        if (camera == null) {
            return;
        }
        camera.takePicture(null, null, (data, camera1) -> {
            if (takePictureTimer != null) {
                takePictureTimer.cancel();
                takePictureTimer = null;
            }
            PromptManager.getInstance().startPhotograph();
            camera1.startPreview();
            MyTerminalFactory.getSDK().getThreadPool().execute(() -> {
                //检测内存卡的size
                FileTransferOperation operation = MyTerminalFactory.getSDK().getFileTransferOperation();
                operation.checkExternalUsableSize();
                String fileName = FileTransgerUtil.getPhotoFileName();
                String directoty = MyTerminalFactory.getSDK().getBITPhotoRecordedDirectoty(operation.getExternalUsableStorageDirectory());
                File file = new File(directoty, fileName + FileTransgerUtil._TYPE_IMAGE_SUFFIX);
                PhotoUtils.savePictureForByte(data, file, MainActivity.this);
                SDCardUtil.scanMtpAsync(MainActivity.this, file.getAbsolutePath());
                long fileSize = DataUtil.getFileSize(file);
                logger.info("保存图片的大小=====" + fileSize);
                if (fileSize > 0) {
                    //拍完照 上传文件目录，并保存到数据库中
                    operation.generateFileComplete(directoty, file.getPath());
                    //发送到群组中
//                            PhotoUtils.sendPhotoFromCamera(file);
                }
            });
            canTakePicture = true;
        });
    }

    /**
     * 进入应用自动上报图像
     */
    private void autoStartLive() {
        boolean isContains = TerminalFactory.getSDK().contains(Params.PUSH_LIVE_STATE);
        boolean state = TerminalFactory.getSDK().getParam(Params.PUSH_LIVE_STATE, false);
        if (isContains && state) {
//            stopPush(false);
            //弹窗提示
            showPushLiveDialog();
        } else {
            myHandler.postDelayed(this::requestStartLive, 1000);
        }
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        logger.debug("mainactivity--onKeyUp-keyCode:" + keyCode );
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                FragmentManager fragmentManager = getSupportFragmentManager();
                int size = fragmentManager.getBackStackEntryCount();
                if (size != 0) {
                    FragmentManager.BackStackEntry backstatck = fragmentManager.getBackStackEntryAt(size-1);
                    logger.debug("onKeyUp-count:" + size + "-size-" + size + "-tag-" + ((backstatck!=null)?backstatck.getName():""));
                    if (backstatck!=null && TextUtils.equals(Constants.FRAGMENT_TAG_GROUP_CHANGE, backstatck.getName())) {
                        //说明当前显示的转组页面
                        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverFragmentBackPressedByGroupChangeHandler.class);
                    } else {
                        popBackStack();
                    }
                } else if ((mMediaStream != null && (mMediaStream.isStreaming() || mMediaStream.isRecording()))
                        || MyTerminalFactory.getSDK().getRecordingAudioManager().getStatus() != AudioRecordStatus.STATUS_STOPED) {
                    exit();
                } else {
//                    ToastUtil.showToast(this, getString(R.string.text_move_task_to_back));
                    //判断是否可以点击返回按键返回到home页面
                    if(DeviceUtil.canBackToHome()){
                        moveTaskToBack(true);
                    }
                }
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    /**
     * 返回键退出应用
     */
    public void exit() {
        if (!mExitFlag) {
            ToastUtil.showToast(this, "再点一次" + getExitState(mMediaStream));
            mExitFlag = true;
            new Handler().postDelayed(() -> mExitFlag = false, CLICK_EXIT_TIME);
        } else {
            updateNormalPushingState(false);
            stopBusniess();
        }
    }

    /**
     * 停止业务
     */
    private void stopBusniess() {
        boolean report = false;
        if (mMediaStream != null && mMediaStream.isStreaming()) {
            stopPush(true);
            report = true;
        }
        if (mMediaStream != null && mMediaStream.isRecording()) {
            mMediaStream.stopRecord();
            if (!report) {
                promptStopVideoRecoder();
                report = true;
            }
        }
        if (MyTerminalFactory.getSDK().getRecordingAudioManager().getStatus() != AudioRecordStatus.STATUS_STOPED) {
            stopRecordAudio();
            if (!report) {
                PromptManager.getInstance().stopRecordAudio();
                report = true;
            }
        }
        Map<TerminalState, IState<?>> currentStateMap = TerminalFactory.getSDK().getTerminalStateManager().getCurrentStateMap();
        if (currentStateMap.containsKey(TerminalState.GROUP_CALL_LISTENING)) {
            TerminalFactory.getSDK().getGroupCallManager().ceaseGroupCall();
        }
        if (currentStateMap.containsKey(TerminalState.GROUP_CALL_SPEAKING)) {
            TerminalFactory.getSDK().getGroupCallManager().ceaseGroupCall();
        }
        TerminalFactory.getSDK().notifyReceiveHandler(ReceiverUpdatePlayVideoStateHandler.class);
    }

    /**
     * 停止上报和停止录像业务
     */
    private void stopPushAndRecord() {
        if (mMediaStream != null) {
            if (mMediaStream.isStreaming()) {
                stopPush(true);
            }
            if (mMediaStream.isRecording()) {
                //已经在录像，停止录像
                mMediaStream.stopRecord();
            }
        }
        TerminalFactory.getSDK().notifyReceiveHandler(ReceiverUpdatePlayVideoStateHandler.class);
    }

//    public void stopLiveService() {
//        if (conn != null) {
//            if (isBinded) {
//                unbindService(conn);
//                isBinded = false;
//            }
//            stopService(new Intent(this, BITBackgroundCameraService.class));
//            conn = null;
//            mService = null;
//
//        }
//    }

    /**
     * 显示登陆和绑定的UI
     *
     * @param state
     */
    private void showLoginAndBindUI(int state) {
        switch (state) {
            case Constants.LOGIN_BIND_STATE_IDLE:
                //未登录
                tvLoginInfo.setCompoundDrawables(null, null, null, null);
                tvLoginInfo.setText(getString(R.string.text_unlogin));
                btBindState.setVisibility(View.GONE);
                break;
            case Constants.LOGIN_BIND_STATE_LOGIN:
                //已登录
                Drawable d1 = getResources().getDrawable(R.drawable.icon_logined);
                d1.setBounds(0, 0, d1.getMinimumWidth(), d1.getMinimumHeight());
                tvLoginInfo.setCompoundDrawables(d1, null, null, null);
                tvLoginInfo.setText(String.format(getString(R.string.text_member_id), MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)));
                btBindState.setText(getString(R.string.text_bind));
                btBindState.setVisibility(View.VISIBLE);
                break;
            case Constants.LOGIN_BIND_STATE_BIND:
                //已绑定
                Drawable d2 = getResources().getDrawable(R.drawable.icon_binded);
                d2.setBounds(0, 0, d2.getMinimumWidth(), d2.getMinimumHeight());
                tvLoginInfo.setCompoundDrawables(d2, null, null, null);
                tvLoginInfo.setText(String.format(getString(R.string.text_bind_member_id),
                        MyTerminalFactory.getSDK().getParam(Params.MEMBER_NAME, ""),
                        MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)));
                btBindState.setText(getString(R.string.text_unbind));
                btBindState.setVisibility(View.VISIBLE);
                break;
        }
        showTopInfoLayout();
    }

    /**
     * 显示头部的信息布局
     */
    private void showTopInfoLayout() {
        rlLoginBind.setVisibility(View.VISIBLE);
        myHandler.sendEmptyMessageDelayed(HANDLE_CODE_HIDE_INFO_LAYOUT, HIDE_INFO_LAYOUT_TIME);
    }

    /**
     * 隐藏头部的信息布局
     */
    private void hideTopInfoLayout() {
        rlLoginBind.setVisibility(View.GONE);
        myHandler.removeMessages(HANDLE_CODE_HIDE_INFO_LAYOUT);
    }

    /**
     * 清空BackStack中所有的fragment
     */
    private void clearFragmentBackStack() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();
//        List<Fragment> list = fragmentManager.getFragments();
        for (int i = 0; i < count; ++i) {
            fragmentManager.popBackStack();
//            if(list.get(i)!=null){
//                fragmentManager.beginTransaction().remove(list.get(i)).commit();
//            }
        }
    }

    /**
     * 退栈
     */
    private void popBackStack() {
        getSupportFragmentManager().popBackStack();
    }

    /**
     * 重新对当前的fragment赋值
     */
    private void resetCurrentFragment(){
        try{
            FragmentManager fragmentManager = getSupportFragmentManager();
            int count = fragmentManager.getBackStackEntryCount();
            logger.info("resetCurrentFragment---count:" + count);
            if(count <= 0){
                currentFragment = null;
                goneFragmentView();
            }else{
                FragmentManager.BackStackEntry backstatck = getSupportFragmentManager().getBackStackEntryAt(count-1);
                logger.info("resetCurrentFragment---backstatck:" + backstatck+"---name:"+backstatck.getName());
                if (backstatck !=null&&!TextUtils.isEmpty(backstatck.getName())) {
                    currentFragment = fragmentManager.findFragmentByTag(backstatck.getName());
                    logger.info("resetCurrentFragment---backstatck--currentFragment:" + currentFragment);
                }else{
                    currentFragment = null;
                    goneFragmentView();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        logger.info("resetCurrentFragment---currentFragment:" + currentFragment);
    }

    /**
     * 更新 是否是正常操作停止上报，（如果是再次进入应用不再自动上报，如果不是弹窗提示）
     *
     * @param state
     */
    public void updateNormalPushingState(boolean state) {
        if (state) {
            myHandler.sendEmptyMessage(HANDLE_WRITE_AUTO_PUSH_INTERVAL_TIME);
        } else {
            myHandler.removeMessages(HANDLE_WRITE_AUTO_PUSH_INTERVAL_TIME);
            TerminalFactory.getSDK().putParam(Params.RECORDER_AUTO_PUSH_INTERVAL_TIME, 0L);
        }
    }

    /**
     * 判断是否自动上报
     * 1.警情组，2上次上报时间到这次开启应用的时间间隔小于10分钟
     *
     * @return
     */
    private boolean checkCanAutoStartLive() {
        RecorderBindBean bean = cn.vsx.hamster.terminalsdk.tools.DataUtil.getRecorderBindBean();
        long time = TerminalFactory.getSDK().getParam(Params.RECORDER_AUTO_PUSH_INTERVAL_TIME, 0L);
        long result = System.currentTimeMillis() - time;
        return ((bean != null && !TextUtils.isEmpty(bean.getWarningId())) && ((time == 0) || (time != 0) && result < AUTO_PUSH_INTERVAL_TIME));
    }

    /**
     * 隐藏fragment显示的view
     */
    private void goneFragmentView(){
        if(flContent!=null){
            flContent.setVisibility(View.GONE);
        }
    }

    private void enableReaderMode() {
        Log.i(TAG, "Enabling reader mode");
        try{
            NfcAdapter nfc = NfcAdapter.getDefaultAdapter(this);
            if (nfc != null) {
                nfc.enableReaderMode(this, nfcCardReader, READER_FLAGS, null);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void disableReaderMode() {
        Log.i(TAG, "Disabling reader mode");
        try{
            NfcAdapter nfc = NfcAdapter.getDefaultAdapter(this);
            if (nfc != null) {
                nfc.disableReaderMode(this);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}

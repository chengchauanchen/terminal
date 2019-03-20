package cn.vsx.vc.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import org.apache.log4j.Logger;
import org.easydarwin.easypusher.BITBackgroundCameraService;
import org.easydarwin.push.BITMediaStream;
import org.easydarwin.push.EasyPusher;
import org.easydarwin.push.InitCallback;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.CallMode;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.TerminalErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.BitStarFileDirectory;
import cn.vsx.hamster.terminalsdk.model.VideoMember;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveAnswerLiveTimeoutHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveChangeGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveExternStorageSizeHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetVideoPushUrlHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberJoinOrExitHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNobodyRequestVideoLiveHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyLivingIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyLivingStoppedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveOnLineStatusChangedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseMyselfLiveHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseStartLiveHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSendUuidResponseHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.application.UpdateManager;
import cn.vsx.vc.model.NFCBean;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.receiveHandle.ReceiverAudioButtonEventHandler;
import cn.vsx.vc.receiveHandle.ReceiverPhotoButtonEventHandler;
import cn.vsx.vc.receiveHandle.ReceiverVideoButtonEventHandler;
import cn.vsx.vc.service.LockScreenService;
import cn.vsx.vc.utils.APPStateUtil;
import cn.vsx.vc.utils.DataUtil;
import cn.vsx.vc.utils.NfcUtil;
import cn.vsx.vc.utils.PhotoUtils;
import cn.vsx.vc.utils.SystemUtil;
import cn.vsx.vc.utils.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.audio.CheckMyPermission;
import ptt.terminalsdk.manager.filetransfer.FileTransferOperation;
import ptt.terminalsdk.manager.recordingAudio.AudioRecordStatus;
import ptt.terminalsdk.tools.DialogUtil;
import ptt.terminalsdk.tools.FileTransgerUtil;
import ptt.terminalsdk.tools.SDCardUtil;

import static cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState.LISTENING;
import static cn.vsx.hamster.terminalsdk.model.BitStarFileDirectory.USB;

public class MainActivity extends BaseActivity implements NfcUtil.OnReadListener {
    //没有网络的提示
    @Bind(R.id.ll_no_network)
    LinearLayout llNoNetwork;
    //视频
    @Bind(R.id.sv_live)
    TextureView svLive;

    @Bind(R.id.button1)
    Button button1;
    @Bind(R.id.button2)
    Button button2;


    private Timer timer = new Timer();

    private boolean onRecordAudioDenied;
    private boolean onLocationDenied;
    private boolean onCameraDenied;

    private ServiceConnection conn;
    private IBinder myIBinder;

    private Logger logger = Logger.getLogger(getClass());
    // 退出标记
    private boolean mExitFlag;
    //双击退出应用的时间间隔
    public static final int CLICK_EXIT_TIME = 3000;

    private static final String STATE = "state";
    private static final int HANDLE_CODE_OPEN_CAMERA = -1;
    private static final int HANDLE_CODE_MSG_STATE = 0;
    //上报时开始每隔5s摄像头自动对焦一次
    private static final int HANDLE_CODE_LIVING_STATE = 0;

    BITMediaStream mMediaStream;
    List<String> listResolution;
    List<String> listResolutionName;


//    private MyApplication.TYPE currentType = MyApplication.TYPE.PUSH;
    private BITBackgroundCameraService mService;
    int width = 640, height = 480;

    private WindowManager windowManager;
    private int pushcount;
    private boolean isPushing;//正在上报
    private boolean isFromNFCToChangeGroup;//是否是来自nfc的转组，（如果是 需要上报图像和录像）
    private boolean isPassiveReport;//是否是被动上报(用于区分提示音)
    private String id;//自己发起直播返回的callId和member拼接
    private PushCallback pushCallback;

    private PowerManager.WakeLock wakeLockComing;
    public static final int REQUEST_PERMISSION_SETTING = 1235;
    private List<VideoMember> watchLiveList = new ArrayList<>();//加入观看人员的集合

    private boolean canTakePicture = true;//是否可以拍照
    private TimerTask takePictureTimer;
    //nfcutil
    private NfcUtil mNFCUtil;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_main;
    }

    @SuppressLint("InvalidWakeLockTag")
    @Override
    public void initData() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if(powerManager!=null){
            wakeLockComing = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "wakeLock3");//
            logger.info("wakeLock3 = "+wakeLockComing);
        }

        svLive.setVisibility(View.GONE);
        svLive.setOpaque(false);
        svLive.setSurfaceTextureListener(new SurfaceTextureListener());
        svLive.setOnClickListener(new OnClickListenerAutoFocus());

        windowManager = (WindowManager) this.getSystemService(WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        initResolution();

        startService(new Intent(MainActivity.this, LockScreenService.class));
        MyApplication.instance.startPTTButtonEventService("");
        MyTerminalFactory.getSDK().getVideoProxy().setActivity(this);

        String machineType = Build.MODEL;
        MyTerminalFactory.getSDK().putParam(Params.ANDROID_BUILD_MODEL, machineType);

        //版本自动更新检测
        if (MyTerminalFactory.getSDK().getParam(Params.IS_AUTO_UPDATE, false) && !MyApplication.instance.isUpdatingAPP) {
            final UpdateManager manager = new UpdateManager(MainActivity.this);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    manager.checkUpdate(MyTerminalFactory.getSDK().getParam(Params.UPDATE_URL, ""), false);
                }
            }, 4000);
        }
        judgePermission();
        //nfc初始化
        mNFCUtil = new NfcUtil(this);
        mNFCUtil.setOnReadListener(this);

        button1.setText("录像");
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TerminalFactory.getSDK().getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
//                        FileTransferOperation operation = MyTerminalFactory.getSDK().getFileTransferOperation();
//                        operation.deleteUploadedFile();
                    }
                 });
            }
        });
        button2.setText("上传未上传的文件信息");
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                TerminalFactory.getSDK().getThreadPool().execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        FileTransferOperation operation = MyTerminalFactory.getSDK().getFileTransferOperation();
//                        operation.getRecordByAll();
//                        operation.uploadFileByPath("/storage/emulated/0/Android/data/cn.vsx.vc/VideoRecord/2019022616555402OON000110000001.mp4",0,false);
//                        operation.uploadFileByPath("/storage/sdcard1/Android/data/cn.vsx.vc/VideoRecord/2019022616555402OON000110000001.mp4",0,false);
//                        operation.uploadFileTreeBean(null);
//                    }
//                });

            }
        });
        button1.setVisibility(View.GONE);
        button2.setVisibility(View.GONE);
        //清理数据库
        FileTransferOperation manager =  MyTerminalFactory.getSDK().getFileTransferOperation();
        //48小时未上传的文件上传
        manager.checkStartExpireFileAlarm();
        //上传没有上传的文件信息
        manager.uploadFileTreeBean(null);
//        //检测内存卡的size
//        manager.checkExternalUsableSize();
    }

    @Override
    public void initView() {
    }

    @Override
    public void initListener() {
        MyTerminalFactory.getSDK().registReceiveHandler(receiveReaponseMyselfLiveHandler);//直播成功
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGetVideoPushUrlHandler);//自己发起直播的响应
        MyTerminalFactory.getSDK().registReceiveHandler(receiveOnLineStatusChangedHandler);//网络连接状态
        MyTerminalFactory.getSDK().registReceiveHandler(receiveSendUuidResponseHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveChangeGroupHandler);//转组
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallIncommingHandler);//组呼来了


        MyTerminalFactory.getSDK().registReceiveHandler(receiveReaponseStartLiveHandler);//请求时，对方拒绝
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyLivingStoppedHandler);//通知停止直播
        MyTerminalFactory.getSDK().registReceiveHandler(receiveAnswerLiveTimeoutHandler);//直播应答超时
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyLivingIncommingHandler);//请求开视频
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNobodyRequestVideoLiveHandler);//对方取消请求
        MyTerminalFactory.getSDK().registReceiveHandler(receiverCameraButtonEventHandler);//视频实体按钮上报和录像视频
        MyTerminalFactory.getSDK().registReceiveHandler(receiverPhotoButtonEventHandler);//拍照实体按钮
        MyTerminalFactory.getSDK().registReceiveHandler(receiverAudioButtonEventHandler);//录音实体按钮
        MyTerminalFactory.getSDK().registReceiveHandler(receiveMemberJoinOrExitHandler);//观看人员的加入或者退出
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveExternStorageSizeHandler);//通知存储空间不足
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
                MyApplication.instance.getSpecificSDK().configLogger();
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

        //nfc
        if(mNFCUtil!=null&&mNFCUtil.getmNfcAdapter()!=null){
            mNFCUtil.getmNfcAdapter().enableForegroundDispatch(this, mNFCUtil.getmPendingIntent(), mNFCUtil.getmIntentFilter(), mNFCUtil.getmTechList());
            mNFCUtil.proccessIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
            setIntent(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mNFCUtil!=null&&mNFCUtil.getmNfcAdapter()!=null){
            mNFCUtil.getmNfcAdapter().disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    public void doOtherDestroy() {
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveReaponseMyselfLiveHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetVideoPushUrlHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveSendUuidResponseHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveOnLineStatusChangedHandler);//网络连接状态
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveChangeGroupHandler);//转组
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallIncommingHandler);//组呼来了

        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveReaponseStartLiveHandler);//请求时，对方拒绝
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyLivingStoppedHandler);//通知停止直播
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveAnswerLiveTimeoutHandler);//直播应答超时
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyLivingIncommingHandler);//请求开视频
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNobodyRequestVideoLiveHandler);//对方取消请求
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiverCameraButtonEventHandler);//视频实体按钮上报和录像视频
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiverPhotoButtonEventHandler);//拍照实体按钮
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiverAudioButtonEventHandler);//录音实体按钮
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMemberJoinOrExitHandler);//观看人员的加入或者退出
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveExternStorageSizeHandler);//通知存储空间不足


        myHandler.removeCallbacksAndMessages(null);

        PromptManager.getInstance().stopRing();

        stopService(new Intent(MainActivity.this, LockScreenService.class));
        MyApplication.instance.stopUVCCameraService();
        MyTerminalFactory.getSDK().getVideoProxy().start().unregister(this);
        if (conn != null) {
            unbindService(conn);
        }
        stopService(new Intent(MainActivity.this, BITBackgroundCameraService.class));
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
            }
        }
    };

    /**
     * 信令服务发送NotifyForceRegisterMessage消息时，先去reAuth(false)，然后login()
     */
    private ReceiveSendUuidResponseHandler receiveSendUuidResponseHandler = new ReceiveSendUuidResponseHandler() {
        @Override
        public void handler(int resultCode, final String resultDesc, boolean isRegisted) {
            if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                logger.info("信令服务器通知NotifyForceRegisterMessage消息，在MainActivity: isRegisted" + isRegisted);
                if (isRegisted) {//注册过，在后台登录，session超时也走这
                    TerminalFactory.getSDK().getAuthManagerTwo().login();
                    logger.info("信令服务器通知NotifyForceRegisterMessage消息，在MainActivity中登录了");
//                    MyTerminalFactory.getSDK().getTerminalMessageManager().getAllMessageRecord();
                } else {//没注册过，关掉主界面，去注册界面
                    startActivity(new Intent(getApplicationContext(), RegistActivity.class));
                    MainActivity.this.finish();
                    stopService(new Intent(getApplicationContext(), LockScreenService.class));
                }
            }
        }
    };

    /**
     * 转组消息
     */
    private ReceiveChangeGroupHandler receiveChangeGroupHandler = new ReceiveChangeGroupHandler() {
        @Override
        public void handler(int errorCode, String errorDesc) {
            logger.info("转组成功回调消息, isChanging:" + MyApplication.instance.isChanging);
            synchronized (MyApplication.instance) {
                MyApplication.instance.isChanging = false;
//                if (MyApplication.instance.isPttPress) {
                    logger.info("转组成功回调消息：isPttPress" + MyApplication.instance.isPttPress);
                    //来自NFC消息转组，需要上报图像，录像
                    if(isFromNFCToChangeGroup){
                        isFromNFCToChangeGroup = false;
                        //上报,先停止上报，再开始主动上报,并录像。
                        finishVideoLive();
                        requestStartLive();
                    }
//                }
                MyApplication.instance.notifyAll();
            }
        }
    };

    /**
     * 被动方组呼来了
     */
    private ReceiveGroupCallIncommingHandler receiveGroupCallIncommingHandler = new ReceiveGroupCallIncommingHandler() {
        @Override
        public void handler(final int memberId, final String memberName, final int groupId,
                            String version, CallMode currentCallMode) {
            if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_LISTEN.name())){
                ptt.terminalsdk.tools.ToastUtil.showToast(getApplicationContext(),"没有组呼听的权限");
            }
            PromptManager.getInstance().groupCallCommingRing();
            logger.info("组呼来了");
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    //是组扫描的组呼,且当前组没人说话，变文件夹和组名字
                    if (groupId != MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)) {
                        logger.info("扫描组组呼来了");
                    }
                    //是当前组的组呼,且扫描组有人说话，变文件夹和组名字
                    if (groupId == MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0) && MyApplication.instance.getGroupListenenState() == LISTENING) {
                        logger.info("当前组组呼来了");
                    }
                    MyTerminalFactory.getSDK().putParam(Params.CURRENT_SPEAKER, memberName);
                }
            });

        }
    };

    /**
     * 网络连接状态
     */
    private ReceiveOnLineStatusChangedHandler receiveOnLineStatusChangedHandler = new ReceiveOnLineStatusChangedHandler() {
        @Override
        public void handler(final boolean connected) {
            logger.info("主界面收到服务是否连接的通知ServerConnectionEstablishedHandler" + connected);
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!connected) {
                        llNoNetwork.setVisibility(View.VISIBLE);
                        MyApplication.instance.isPopupWindowShow = false;
                    } else {
//                            initMediaStream(svLive.getSurfaceTexture());
//                            //判断是否之前在上报中，如果上报中请求继续上报
//                            if(isPushing){
//                                requestStartLive();
//                            }
                        llNoNetwork.setVisibility(View.GONE);
                        //上传未上传的文件信息
                        MyTerminalFactory.getSDK().getFileTransferOperation().uploadFileTreeBean(null);
                    }
                }
            });
        }
    };


    /**
     *获取上报地址
     **/
    private ReceiveGetVideoPushUrlHandler receiveGetVideoPushUrlHandler = new ReceiveGetVideoPushUrlHandler() {
        @Override
        public void handler(final String streamMediaServerIp, final int streamMediaServerPort, final long callId) {
            myHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    logger.info("自己发起直播，服务端返回的ip：" + streamMediaServerIp + "端口：" + streamMediaServerPort + "---callId:" + callId);
                    String port = streamMediaServerPort + "";
                    id = TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) + "_" + callId;
                        startPush(streamMediaServerIp, port, id);
                }
            }, 1000);
        }
    };

    /**
     * 自己发起直播的响应
     **/
    private ReceiveResponseMyselfLiveHandler receiveReaponseMyselfLiveHandler = new ReceiveResponseMyselfLiveHandler() {
        @Override
        public void handler(final int resultCode, final String resultDesc) {
            myHandler.post(new Runnable(){
                @Override
                public void run(){
                    if(resultCode == 0){
//                        List<Integer> pushMemberList = new ArrayList<>();
//                        pushMemberList.add(88011103);
//                        logger.info("自己发起直播成功,要推送的列表：" + pushMemberList);
//                        if (pushMemberList != null) {
//                            MyTerminalFactory.getSDK().getLiveManager().requestNotifyWatch(pushMemberList,MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID,0));
//                        }
                        isPushing = true;
                    }else {
                        isPushing = false;
                        ToastUtil.showToast(getApplicationContext(),resultDesc);
                        finishVideoLive();
                    }
                }
            });
        }
    };

    /**
     * 对方拒绝直播，通知界面关闭响铃页
     **/
    private ReceiveResponseStartLiveHandler receiveReaponseStartLiveHandler = new ReceiveResponseStartLiveHandler() {
        @Override
        public void handler(int resultCode, final String resultDesc) {
            ToastUtil.showToast(getApplicationContext(),resultDesc);
            finishVideoLive();
        }
    };

    /**
     * 通知直播停止 通知界面关闭视频页
     **/
    private ReceiveNotifyLivingStoppedHandler receiveNotifyLivingStoppedHandler = new ReceiveNotifyLivingStoppedHandler() {
        @Override
        public void handler(int liveMemberId,long callId,int methodResult, String resultDesc) {
            ToastUtil.showToast(getApplicationContext(),"上报已结束");
            finishVideoLive();
        }
    };

    /**
     * 超时未回复answer 通知界面关闭
     **/
    private ReceiveAnswerLiveTimeoutHandler receiveAnswerLiveTimeoutHandler = new ReceiveAnswerLiveTimeoutHandler() {
        @Override
        public void handler() {
            ToastUtil.showToast(getApplicationContext(),"对方已取消");
            finishVideoLive();
        }
    };

    /**
     * 收到别人请求我开启直播的通知
     **/
    private ReceiveNotifyLivingIncommingHandler receiveNotifyLivingIncommingHandler = new ReceiveNotifyLivingIncommingHandler() {
        @Override
        public void handler(final String mainMemberName, final int mainMemberId) {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    //如果正在拍摄视频时
                    if (APPStateUtil.isBackground(getApplicationContext())) {//程序处于后台
                        //                        sendBroadcast(new Intent("MainActivityfinish"));
                        logger.info("main程序拿到前台");
                        //无屏保界面
                        APPStateUtil.setTopApp(MainActivity.this);
                    }
                    wakeLockComing.acquire();
                    logger.info("main点亮屏幕");
                    //自动接收上报视频
                    MyTerminalFactory.getSDK().getLiveManager().responseLiving(true);
                    MyApplication.instance.isPrivateCallOrVideoLiveHand = true;
                    isPassiveReport = true;
                }
            });
        }
    };

    /**
     * 收到没人请求我开视频的消息，关闭界面和响铃
     */
    private ReceiveNobodyRequestVideoLiveHandler receiveNobodyRequestVideoLiveHandler = new ReceiveNobodyRequestVideoLiveHandler() {
        @Override
        public void handler() {
            ToastUtil.showToast(getApplicationContext(),"对方已取消");
            finishVideoLive();
        }
    };

    /**
     * 视频实体按钮，上报视频
     */
    private ReceiverVideoButtonEventHandler receiverCameraButtonEventHandler = new ReceiverVideoButtonEventHandler() {
        @Override
        public void handler(boolean isLongPress) {
            if(isLongPress){
                //上报视频
                logger.info("视频实体按钮，上报视频");
                if(mMediaStream!=null) {
                    if (mMediaStream.isStreaming()) {
                        ToastUtil.showToast(MainActivity.this, "上报中");
                    } else {
                        if(MyTerminalFactory.getSDK().getRecordingAudioManager().getStatus() != AudioRecordStatus.STATUS_STOPED){
                            stopRecordAudio();
                        }
                        requestStartLive();
                    }
                }
            }else{
                //录像  （判断当前的状态，如果已经在录像，停止录像）
                if(mMediaStream!=null) {
                    if(mMediaStream.isStreaming()){
                        stopPush();
                        ToastUtil.showToast(MainActivity.this, "停止上报");
                    }else if (mMediaStream.isRecording()) {
                        //已经在录像，停止录像
                        ToastUtil.showToast(MainActivity.this,"停止录像");
                        PromptManager.getInstance().stopVideoTap();
                        mMediaStream.stopRecord();

                    } else {
                        //当前没有录像，开始录像
                        if (TerminalFactory.getSDK().checkeExternalStorageIsAvailable(MyTerminalFactory.getSDK().getFileTransferOperation().getExternalUsableStorageDirectory())) {
                            ToastUtil.showToast(MainActivity.this, "开始录像");
                            PromptManager.getInstance().startVideoTap();
                            mMediaStream.startRecord();
                        }
                    }
                }
            }
        }
    };

    /**
     * 拍照实体按钮
     */
    private ReceiverPhotoButtonEventHandler receiverPhotoButtonEventHandler = new ReceiverPhotoButtonEventHandler() {
        @Override
        public void handler() {
           if(mMediaStream!=null){
            FileTransferOperation operation = MyTerminalFactory.getSDK().getFileTransferOperation();
            int code = operation.getExternalUsableStorageDirectory();
            if(TerminalFactory.getSDK().checkeExternalStorageIsAvailable(code)){
                long usb = MyTerminalFactory.getSDK().getExternalUsableSize(USB.getCode())/ 1024 / 1024;
                long sdCard = MyTerminalFactory.getSDK().getExternalUsableSize(BitStarFileDirectory.SDCARD.getCode())/ 1024 / 1024;
                long memorySize = (code == USB.getCode())?usb:sdCard;
                boolean isNeedNotify = (usb<200&&sdCard<200);
                //提示空间不足
                if (usb < 3 && sdCard < 3) {
                    PromptManager.getInstance().startExternNoStorage();
                    ToastUtil.showToast(MainActivity.this, "存储空间不足");
                    return ;
                } else if (isNeedNotify) {
                    PromptManager.getInstance().startExternStorageNotEnough();
                    ToastUtil.showToast(MainActivity.this, "存储空间告急");
                }
                //切换空间
                if(memorySize<3){
                    boolean success = operation.changeExternalStorage(code,usb,sdCard,3);
                    if(!success){
                        ToastUtil.showToast(MainActivity.this, "存储空间不足");
                        return;
                    }
                }
                if (canTakePicture && System.currentTimeMillis() -  MyApplication.instance.clicktime > 3000) {
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
                    MyTerminalFactory.getSDK().getTimer().schedule(takePictureTimer, 3000l);
                }
                ToastUtil.showToast(MainActivity.this,"拍照");
            }
         }
        }
    };

    /**
     * 录音实体按钮，录音
     */
    private ReceiverAudioButtonEventHandler receiverAudioButtonEventHandler = new ReceiverAudioButtonEventHandler() {
        @Override
        public void handler(boolean isLongPress) {
            if(isLongPress){
                //长按
                if(MyTerminalFactory.getSDK().getRecordingAudioManager().getStatus() == AudioRecordStatus.STATUS_STOPED){
                    //开始录音
                    if(TerminalFactory.getSDK().checkeExternalStorageIsAvailable(MyTerminalFactory.getSDK().getFileTransferOperation().getExternalUsableStorageDirectory())){
                        startRecordAudio();
                        PromptManager.getInstance().startRecordAudio();
                        ToastUtil.showToast(MainActivity.this,"开始录音");

                    }
                }else{
                    ToastUtil.showToast(MainActivity.this,"录音中");
                }
            }else{
                //短按-停止录音
                stopRecordAudio();
                PromptManager.getInstance().stopRecordAudio();
                ToastUtil.showToast(MainActivity.this,"停止录音");
            }
        }
    };

    /**
     * 观看成员的进入和退出
     **/
    @SuppressLint("SimpleDateFormat")
    private ReceiveMemberJoinOrExitHandler receiveMemberJoinOrExitHandler = new ReceiveMemberJoinOrExitHandler() {
        @Override
        public void handler(final String memberName, final int memberId, final boolean joinOrExit) {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.e("IndividualCallService", memberName+",memberId:"+memberId);
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
                        if(watchLiveList.size()<1){
                            stopPush();
                        }
                    }
                }
            });

        }
    };

    /**
     *通知存储空间不足
     */
    private ReceiveExternStorageSizeHandler mReceiveExternStorageSizeHandler = new ReceiveExternStorageSizeHandler() {
        @Override
        public void handler(final long memorySize) {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (memorySize < 100) {
                        ToastUtil.showToast(MainActivity.this, "存储空间不足");
                        PromptManager.getInstance().startExternNoStorage();
                        if(mService!=null&&mService.getMediaStream()!=null){
                            //停止上报
                            stopPush();
                            //停止录像
                            mService.getMediaStream().stopRecord();
                        }
                        //停止录音
                        stopRecordAudio();
                        //上传没有上传的文件，删除已经上传的文件
                       MyTerminalFactory.getSDK().getFileTransferOperation().externNoStorageOperation();

                    } else if (memorySize < 200){
                        PromptManager.getInstance().startExternStorageNotEnough();
                        ToastUtil.showToast(MainActivity.this, "存储空间告急");
                    }
                }
            });
        }
    };

    /**
     * 读取NFC数据的回调
     * @param resultCode
     * @param readType
     * @param resultDescribe
     * @param bean
     */
    @Override
    public void onReadResult(int resultCode, String readType, String resultDescribe, final NFCBean bean) {
        ToastUtil.showToast(MainActivity.this,resultDescribe);
        switch (resultCode){
            case NfcUtil.RESULT_CODE_SUCCESS:
                //切组，上报，录像
                if(bean!=null){
                    //切组
                    changeGroup(bean);
                }
                break;
            default:
                break;
        }

    }

    /**
     * 切组
     * @param bean
     */
    private void changeGroup(final NFCBean bean){
        int result = MyTerminalFactory.getSDK().getGroupManager().changeGroup(bean.getGroupId());
        if (result == BaseCommonCode.SUCCESS_CODE) {
            isFromNFCToChangeGroup = true;
            //转组成功重新请求在线人数
            myHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MyTerminalFactory.getSDK().getGroupManager().getGroupCurrentOnlineMemberList(bean.getGroupId(), false);
                }
            }, 500);
        } else {
            ToastUtil.groupChangedFailToast(MainActivity.this, result);
        }
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
    private void requestStartLive() {
        int requestCode = MyTerminalFactory.getSDK().getLiveManager().requestMyselfLive("", "");
        logger.error("上报图像：requestCode=" + requestCode);
        if (requestCode == BaseCommonCode.SUCCESS_CODE) {
            ToastUtil.showToast(getApplicationContext(),"开始主动上报图像");
            isPassiveReport = false;
        } else {
            ToastUtil.livingFailToast(getApplicationContext(), requestCode, TerminalErrorCode.LIVING_PUSHING.getErrorCode());
        }
    }

    /**
     * 开始上报
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
        String url = String.format("rtsp://%s:%s/%s.sdp", ip, port, id);
        logger.info("推送地址：" + url);
        //开始录像
        if (TerminalFactory.getSDK().checkeExternalStorageIsAvailable(MyTerminalFactory.getSDK().getFileTransferOperation().getExternalUsableStorageDirectory())) {
            mMediaStream.startRecord();
        }
        //提示音
        if(isPassiveReport){
            //被动上报
            PromptManager.getInstance().startReportByNotity();
        }else{
            //主动上报
            PromptManager.getInstance().startReport();
        }

    }

    private void startCamera() {
        mMediaStream.updateResolution(width, height);
        mMediaStream.setDgree(getDgree());
        mMediaStream.createCamera();
        mMediaStream.startPreview();
        logger.info("------>>>>startCamera");
        if (mMediaStream.isStreaming()) {
            sendMessage("推流中..");
            String ip = MyTerminalFactory.getSDK().getParam(Params.VIDEO_SERVER_IP, "");
            int port = MyTerminalFactory.getSDK().getParam(Params.VIDEO_SERVER_PORT, 0);
            int id = MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);

            String url = String.format("rtsp://%s:%s/%s.sdp", ip, port, id);
            logger.info("startCamera ----->  " + url);
        }
    }


    /**
     * 创建直播服务
     */
    private void startLiveService() {
        MyTerminalFactory.getSDK().getVideoProxy().start().register(this);
        startService(new Intent(this, BITBackgroundCameraService.class));

        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                mService = ((BITBackgroundCameraService.LocalBinder) iBinder).getService();
                myIBinder = iBinder;
                logger.error("绑定视频服务成功:" + myIBinder);
                svLive.setVisibility(View.VISIBLE);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                logger.error("绑定视频服务失败:" + componentName);
            }
        };
        //BIND_AUTO_CREATE  如果没有服务就自己创建一个，执行onCreate()；
        bindService(new Intent(this, BITBackgroundCameraService.class), conn, BIND_AUTO_CREATE);
    }


    /**
     * 初始化MediaStream并预览
     *
     * @param surface
     */
    private void initMediaStream(SurfaceTexture surface) {
        if (mService != null) {
            BITMediaStream ms = mService.getMediaStream();
            if (ms != null) {    // switch from background to front
                ms.stopPreview();
                mService.inActivePreview();
//                ms.destroyCamera();
                ms.setSurfaceTexture(surface);
//                ms.createCamera();
                ms.startPreview();
                mMediaStream = ms;
                if (ms.isStreaming()) {
                    String ip = MyTerminalFactory.getSDK().getParam(Params.VIDEO_SERVER_IP, "");
                    int port = MyTerminalFactory.getSDK().getParam(Params.VIDEO_SERVER_PORT, 0);
                    int id = MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
                    String url = String.format("rtsp://%s:%s/%s.sdp", ip, port, id);
                    sendMessage("推流中.");
                    logger.info("推流地址:" + url);

                }
            } else {
                ms = new BITMediaStream(getApplicationContext(), surface, true,width,height);
                mMediaStream = ms;
                startCamera();
                mService.setMediaStream(ms);
            }
        } else {
            ToastUtil.showToast(getApplicationContext(), "服务启动失败");
            finishVideoLive();
        }
    }

    /**
     * 停止上报
     */
    private void finishVideoLive() {
        myHandler.post(new Runnable() {
            @Override
            public void run() {
                PromptManager.getInstance().stopRing();//停止响铃
                //将TextureView设置成全屏
                svLive.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                svLive.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                svLive.requestLayout();
                stopPush();
                id = null;
            }
        });

    }

    /**
     * 停止推流
     */
    private void stopPush() {
        isPushing = false;
        boolean isStreaming = mMediaStream != null && mMediaStream.isStreaming();
        logger.info("isFinishing() = " + "isFinishing()" + "    isStreaming = " + isStreaming);
        if (mMediaStream != null) {
            mMediaStream.stopStream();
            //            stopService(new Intent(VideoLiveActivity.this, BackgroundCameraService.class));
            logger.info("---->>>>页面关闭，停止推送视频");
        } else {
            if (isStreaming) {
                mService.activePreview();
                logger.info("---->>>>退到后台，继续推送视频");
            }
        }
        watchLiveList.clear();
        TerminalFactory.getSDK().getLiveManager().ceaseLiving();
        //提示音
        isPassiveReport = false;
        PromptManager.getInstance().stopReport();

    }


    /**
     * 停止推流并停止预览
     */
    private void stopPushAndPreview() {
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
            mService.setMediaStream(null);
            //            stopService(new Intent(VideoLiveActivity.this, BackgroundCameraService.class));
            logger.info("---->>>>页面关闭，停止推送视频");
        } else {
            if (isStreaming) {
                mService.activePreview();
                logger.info("---->>>>退到后台，继续推送视频");
            }
        }
        watchLiveList.clear();
        TerminalFactory.getSDK().getLiveManager().ceaseLiving();
    }

    /**
     * 开始录音
     */
    private void startRecordAudio(){
        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MyTerminalFactory.getSDK().getRecordingAudioManager().start(new MediaRecorder.OnErrorListener() {
                    @Override
                    public void onError(MediaRecorder mr, int what, int extra) {
                        myHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                stopRecordAudio();
                            }
                        });
                    }
                });
            }
        },500);
    }

    /**
     * 停止录音
     */
    private void stopRecordAudio(){
        if (MyTerminalFactory.getSDK().getRecordingAudioManager().getStatus() == AudioRecordStatus.STATUS_RECORDING){
            MyTerminalFactory.getSDK().getRecordingAudioManager().stop();
        }
    }

    /**
     * 拍照
     */
    private void takePicture () {
        if (mService == null || mService.getMediaStream() == null)
            return;
        Camera camera = mService.getMediaStream().getCamera();
        if (camera == null)
            return;
        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(final byte[] data, final Camera camera) {
                if (takePictureTimer != null) {
                    takePictureTimer.cancel();
                    takePictureTimer = null;
                }
                PromptManager.getInstance().startPhotograph();
                camera.startPreview();
                MyTerminalFactory.getSDK().getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        //检测内存卡的size
                        FileTransferOperation operation = MyTerminalFactory.getSDK().getFileTransferOperation();
                        operation.checkExternalUsableSize();
                        String fileName = FileTransgerUtil.getPhotoFileName();
                        String directoty = MyTerminalFactory.getSDK().getBITPhotoRecordedDirectoty(operation.getExternalUsableStorageDirectory());
                        File file = new File( directoty, fileName+FileTransgerUtil._TYPE_IMAGE_SUFFIX);
                        PhotoUtils.savePictureForByte(data, file, MainActivity.this);
                        SDCardUtil.scanMtpAsync(MainActivity.this, file.getAbsolutePath());
                        long fileSize = DataUtil.getFileSize(file);
                        logger.info("保存图片的大小=====" + fileSize);
                        if (fileSize > 0) {
                            //拍完照 上传文件目录，并保存到数据库中
                            operation.generateFileComplete(directoty,file.getPath());
                            //发送到群组中
//                            PhotoUtils.sendPhotoFromCamera(file);
                        }
                    }
                });
                canTakePicture = true;
            }
        });
    }

    private void sendMessage(String message) {
        Message msg = Message.obtain();
        msg.what = HANDLE_CODE_MSG_STATE;
        Bundle bundle = new Bundle();
        bundle.putString(STATE, message);
        msg.setData(bundle);
        myHandler.sendMessage(msg);
    }

    private int getDgree() {
        int rotation = windowManager.getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break; // Natural orientation
            case Surface.ROTATION_90:
                degrees = 90;
                break; // Landscape left
            case Surface.ROTATION_180:
                degrees = 180;
                break;// Upside down
            case Surface.ROTATION_270:
                degrees = 270;
                break;// Landscape right
            default:
                break;
        }
        return degrees;
    }

    private final class OnClickListenerAutoFocus implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            try {
                mMediaStream.getCamera().autoFocus(null);//屏幕聚焦
            } catch (Exception e) {

            }
        }
    }

    private class PushCallback implements InitCallback {

        @Override
        public void onCallback(int code) {
            Bundle resultData = new Bundle();
            switch (code) {
                case EasyPusher.OnInitPusherCallback.CODE.EASY_ACTIVATE_INVALID_KEY:
                    resultData.putString("event-msg", "EasyRTSP 无效Key");
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_ACTIVATE_SUCCESS:
                    resultData.putString("event-msg", "EasyRTSP 激活成功");
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_CONNECTING:
                    resultData.putString("event-msg", "EasyRTSP 连接中");
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_CONNECTED:
                    resultData.putString("event-msg", "EasyRTSP 连接成功");
                    pushcount = 0;
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_CONNECT_FAILED:
                    resultData.putString("event-msg", "EasyRTSP 连接失败");
                    if (pushcount <= 10) {
                        pushcount++;
                    } else {
                        finishVideoLive();
                    }
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_CONNECT_ABORT:
                    resultData.putString("event-msg", "EasyRTSP 连接异常中断");
                    if (pushcount <= 10) {
                        pushcount++;
                    } else {
                        finishVideoLive();
                    }
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_PUSHING:
                    resultData.putString("event-msg", "EasyRTSP 推流中");
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_DISCONNECTED:
                    resultData.putString("event-msg", "EasyRTSP 断开连接");
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_ACTIVATE_PLATFORM_ERR:
                    resultData.putString("event-msg", "EasyRTSP 平台不匹配");
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_ACTIVATE_COMPANY_ID_LEN_ERR:
                    resultData.putString("event-msg", "EasyRTSP 断授权使用商不匹配");
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_ACTIVATE_PROCESS_NAME_LEN_ERR:
                    resultData.putString("event-msg", "EasyRTSP 进程名称长度不匹配");
                    break;
            }
//            mResultReceiver.send(EasyRTSPClient.RESULT_EVENT, resultData);
        }
    }


    /**
     * 必须要有录音和相机的权限，APP才能去视频页面
     */
    private void judgePermission() {

        //6.0以下判断相机权限
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (!SystemUtil.cameraIsCanUse()) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CheckMyPermission.REQUEST_CAMERA);
            }else {
                startLiveService();
            }
        } else {
            if (CheckMyPermission.selfPermissionGranted(this, Manifest.permission.RECORD_AUDIO)) {
                if (CheckMyPermission.selfPermissionGranted(this, Manifest.permission.CAMERA)) {
                    startLiveService();
                    if (!CheckMyPermission.selfPermissionGranted(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        CheckMyPermission.permissionPrompt(this, Manifest.permission.ACCESS_FINE_LOCATION);
                    }else{
                        //权限打开之后判断是否需要上传位置信息，这种情况是之前没有打开权限使得登录或者成员信息改变的时候不能上传位置信息，到了主页面才申请权限的情况
                        MyTerminalFactory.getSDK().getLocationManager().requestLocationByJudgePermission();
                    }
//                    else {
//                        CheckMyPermission.permissionPrompt(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
//                    }
                } else {
                    CheckMyPermission.permissionPrompt(this, Manifest.permission.CAMERA);
                }
            } else {
                //如果权限被拒绝，申请下一个权限
                if (onRecordAudioDenied) {
                    if (CheckMyPermission.selfPermissionGranted(this, Manifest.permission.CAMERA)) {
                        startLiveService();
                        if (!CheckMyPermission.selfPermissionGranted(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                            CheckMyPermission.permissionPrompt(this, Manifest.permission.ACCESS_FINE_LOCATION);
                        }else{
                            //权限打开之后判断是否需要上传位置信息，这种情况是之前没有打开权限使得登录或者成员信息改变的时候不能上传位置信息，到了主页面才申请权限的情况
                            MyTerminalFactory.getSDK().getLocationManager().requestLocationByJudgePermission();
                        }
                    } else {
                        if (onCameraDenied) {
                            if (!CheckMyPermission.selfPermissionGranted(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                                if (!onLocationDenied) {
                                    CheckMyPermission.permissionPrompt(this, Manifest.permission.ACCESS_FINE_LOCATION);
                                }
                            }else{
                                //权限打开之后判断是否需要上传位置信息，这种情况是之前没有打开权限使得登录或者成员信息改变的时候不能上传位置信息，到了主页面才申请权限的情况
                                MyTerminalFactory.getSDK().getLocationManager().requestLocationByJudgePermission();
                            }
                        } else {
                            CheckMyPermission.permissionPrompt(this, Manifest.permission.CAMERA);
                        }
                    }
                } else {
                    CheckMyPermission.permissionPrompt(this, Manifest.permission.RECORD_AUDIO);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CheckMyPermission.REQUEST_RECORD_AUDIO:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onRecordAudioDenied = true;
                    judgePermission();
                } else {
                    onRecordAudioDenied = false;
                    permissionDenied(Manifest.permission.RECORD_AUDIO);
                }
                break;
            case CheckMyPermission.REQUEST_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onCameraDenied = true;
                    judgePermission();
                } else {
                    onCameraDenied = false;
                    permissionDenied(Manifest.permission.CAMERA);
                }
                break;
            case CheckMyPermission.REQUEST_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onLocationDenied = true;
                    judgePermission();
                } else {
                    onLocationDenied = false;
                    permissionDenied(requestCode);
                }
                break;
            default:
                break;

        }
    }

    private void permissionDenied(int requestCode) {
        if (requestCode == CheckMyPermission.REQUEST_RECORD_AUDIO) {
            ToastUtil.showToast(this, "录制音频权限未打开，语音功能将不能使用。");
        } else if (requestCode == CheckMyPermission.REQUEST_CAMERA) {
            ToastUtil.showToast(this, "相机未打开，图像上报功能将不能使用。");
        } else if (requestCode == CheckMyPermission.REQUEST_LOCATION) {
            ToastUtil.showToast(this, "位置信息权限未打开，定位功能将不能使用。");
        }
    }

    private void permissionDenied(final String permissionName) {
        new DialogUtil() {
            @Override
            public CharSequence getMessage() {
                return CheckMyPermission.getDesForPermission(permissionName);
            }
            @Override
            public Context getContext() {
                return MainActivity.this;
            }

            @Override
            public void doConfirmThings() {
                //点击确定时跳转到设置界面
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
            }

            @Override
            public void doCancelThings() {
                MainActivity.this.finish();
            }
        }.showDialog();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       if (requestCode == REQUEST_PERMISSION_SETTING) {
            // 从设置界面返回时再判断权限是否开启
            judgePermission();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if((mMediaStream != null && (mMediaStream.isStreaming()||mMediaStream.isRecording()))
                        ||MyTerminalFactory.getSDK().getRecordingAudioManager().getStatus()!=AudioRecordStatus.STATUS_STOPED){
                    exit();
                }else{
                    moveTaskToBack(true);//把程序变成后台的
                }
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    /**
     * 返回键退出应用
     */
    public void exit() {
        if (!mExitFlag) {
            ToastUtil.showToast(this, "再点一次"+getExitState());
            mExitFlag = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mExitFlag = false;
                }
            }, CLICK_EXIT_TIME);
        } else {
            moveTaskToBack(true);//把程序变成后台的
        }
    }

    /**
     * 获取退出时的提示
     * @return
     */
    private String getExitState(){
        if(mMediaStream != null&&mMediaStream.isStreaming()){
            return "停止上报";
        }else if(mMediaStream != null&&mMediaStream.isRecording()){
            return "停止录像";
        }else if(MyTerminalFactory.getSDK().getRecordingAudioManager().getStatus()!=AudioRecordStatus.STATUS_STOPED){
            return "停止录音";
        }else {
            return "退出";
        }
    }
}

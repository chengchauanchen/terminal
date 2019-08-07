package cn.vsx.vc.activity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ResultReceiver;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.apache.log4j.Logger;
import org.easydarwin.video.EasyRTSPClient;
import org.easydarwin.video.RTSPClient;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import butterknife.Bind;
import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.MemberChangeType;
import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.Remark;
import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.common.UrlParams;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.TerminalErrorCode;
import cn.vsx.hamster.protolbuf.PTTProtolbuf;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePlayingState;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.model.RotationImageType;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveAnswerLiveTimeoutHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetRtspStreamUrlHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveLogFileUploadCompleteHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveLoginResponseHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberNotLivingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyDataMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyInviteToWatchHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyLivingStoppedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyMemberChangeHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyMemberStopWatchMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyPushPartyLiveMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSendUuidResponseHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveServerConnectionEstablishedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.SignatureUtil;
import cn.vsx.hamster.terminalsdk.tools.Util;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.application.UpdateManager;
import cn.vsx.vc.model.RotationLiveBean;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.service.MAcessibilityService;
import cn.vsx.vc.utils.ActivityCollector;
import cn.vsx.vc.utils.DataUtil;
import cn.vsx.vc.utils.EnableServiceUtils;
import cn.vsx.vc.utils.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.DialogUtil;

import static com.alibaba.fastjson.JSON.parseObject;
import static org.easydarwin.config.Config.PLAYKEY;

/**
 * 直播观看界面
 */
public class MainActivity extends BaseActivity {
    /******************************************************************   属性和view  ****************************************************************************/
    @Bind(R.id.sv_live)
    TextureView svLive;
    @Bind(R.id.bg)
    ImageView foreground;
    @Bind(R.id.icon)
    ImageView icon;
    @Bind(R.id.ll_tempt)
    LinearLayout llTempt;

    @Bind(R.id.tv_nolive)
    TextView noLive;
    @Bind(R.id.tv_connecting_to_other)
    TextView tv_connecting_to_other;
    @Bind(R.id.tv_connect_to_other_success)
    TextView tv_connect_to_other_success;
    @Bind(R.id.tv_connect_to_other_failed)
    TextView tv_connect_to_other_failed;

    @Bind(R.id.tv_user_name)
    TextView tv_user_name;
    @Bind(R.id.tv_user_id)
    TextView tv_user_id;
    @Bind(R.id.tv_title)
    TextView tv_title;
    @Bind(R.id.tv_version_name)
    TextView tv_version_name;
    @Bind(R.id.tv_log_upload)
    TextView tv_log_upload;

    @Bind(R.id.tv_live_content)
    TextView tv_live_content;

    private String streamMediaServerUrl;//收到的直播流rtsp地址
    //    private int mType = Client.TRANSTYPE_TCP;
    private boolean isQuiteApp;//是否退出App
    private static final String STATE = "state";
    private static final int MSG_STATE = 1;
    private static final int MSG_BACKPRESS = 2;
    private static final int MSG_ROTATION_LIVE = 3;

    private Logger logger = Logger.getLogger(getClass());
    private EasyRTSPClient mStreamRender;
    private PTTProtolbuf.NotifyDataMessage message;
    private List<PTTProtolbuf.NotifyDataMessage> liveDescList = new ArrayList<>();
    //记录轮播是的索引
    private int rotationIndex = 0;
    //轮播图像的时间间隔
    private static final int ROTATION_TIME = 15 * 1000;
    //    private boolean isCamera;
//    private String cameraName;
//    private String cameraNo;
    private Handler myHandler = new Handler(Looper.getMainLooper());
    private final int OPEN_ACCESSIBILITY_SETTING_REQUESTCODE = 130;
    private int pullcount;
    private long uploadLogTime;
    private android.app.AlertDialog dialog;

    private Member liveMember;
    private long callId;
    //显示上报人员的信息
    private String showContent = "";
    /******************************************************************   属性和view  ****************************************************************************/
    /*******************************************************************   Activity生命周期   ***************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideSmartBar();
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
                MyApplication.instance.getTerminalSDK4Android().configLogger();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //清楚所有通知
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    public void initView() {
        try {
            PackageManager pm = getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(getPackageName(), 0);
            String versionName = packageInfo.versionName;
            if (!TextUtils.isEmpty(versionName)) {
                tv_version_name.setText(versionName + "版本");
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        tv_user_id.setText(MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) + "");
        tv_user_name.setText(MyTerminalFactory.getSDK().getParam(Params.MEMBER_NAME, ""));

        tv_version_name.setOnFocusChangeListener(onFocusChangeListener);
        tv_log_upload.setOnFocusChangeListener(onFocusChangeListener);
        tv_version_name.requestFocus();
        tv_version_name.setFocusable(true);
    }

    @Override
    public void initData() {
        //版本自动更新检测
        if (MyTerminalFactory.getSDK().getParam(Params.IS_AUTO_UPDATE, false) && !MyApplication.instance.isUpdatingAPP) {
            handler.postDelayed(() -> MyTerminalFactory.getSDK().getThreadPool().execute(() -> {
                final UpdateManager manager = new UpdateManager(MainActivity.this);
                manager.checkUpdate(MyTerminalFactory.getSDK().getParam(Params.UPDATE_URL, ""), false);
            }), 4000);
        }
    }

    @Override
    public void initListener() {
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyInviteToWatchHandler);//收到观看上报图像的通知
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveNotifyDataMessageHandler);//收到GB28181的上报图像的通知
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGetRtspStreamUrlHandler);//收到观看上报图像的地址通知
        MyTerminalFactory.getSDK().registReceiveHandler(receiveSendUuidResponseHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyLivingStoppedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyMemberChangeHandler);
//        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveNotifyInviteToWatchCameraHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveServerConnectionEstablishedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveAnswerLiveTimeoutHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveMemberNotLivingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveLogFileUploadCompleteHandler);
//        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveNotifyMemberStartCameraHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveLoginResponseHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyMemberStopWatchMessageHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyPushPartyLiveMessageHandler);//收到轮播推送的通知
        svLive.setSurfaceTextureListener(surfaceTextureListener);
        tv_log_upload.setOnClickListener(new OnClickListenerImplementationLogUpload());
    }

    @Override
    public void doOtherDestroy() {
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyInviteToWatchHandler);//收到观看上报图像的通知
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveNotifyDataMessageHandler);//收到GB28181的上报图像的通知
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetRtspStreamUrlHandler);//收到观看上报图像的地址通知
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveSendUuidResponseHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyLivingStoppedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyMemberChangeHandler);
//        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveNotifyInviteToWatchCameraHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveServerConnectionEstablishedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveAnswerLiveTimeoutHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveMemberNotLivingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveLogFileUploadCompleteHandler);
//        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveNotifyMemberStartCameraHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveLoginResponseHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyMemberStopWatchMessageHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyPushPartyLiveMessageHandler);//收到轮播推送的通知

        myHandler.removeCallbacksAndMessages(null);
        handler.removeCallbacksAndMessages(null);
        PromptManager.getInstance().stopRing();
        stopPull(true);
    }

    /*******************************************************************   Activity生命周期   ***************************************************************************/

    /******************************************************************   handler  ****************************************************************************/
    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_STATE://Toast
                    String state = msg.getData().getString("state");
                    ToastUtil.showToast(MainActivity.this, state);
                    break;
                case MSG_BACKPRESS://返回键
                    isQuiteApp = false;
                    break;
            }
        }
    };
    /******************************************************************   handler   ****************************************************************************/
    /******************************************************************   监听   ****************************************************************************/
    /**
     * 信令服务发送NotifyForceRegisterMessage消息时，先去reAuth(false)，然后login()
     */
    private ReceiveSendUuidResponseHandler receiveSendUuidResponseHandler = new ReceiveSendUuidResponseHandler() {
        @Override
        public void handler(int resultCode, final String resultDesc, boolean isRegisted) {
            if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                logger.info("信令服务器通知NotifyForceRegisterMessage消息，在MainActivity: isRegisted" + isRegisted);
                handler.post(() -> showConnectToOtherView(tv_connect_to_other_success));

                handler.postDelayed(() -> showConnectToOtherView(noLive), 1000);
                if (isRegisted) {//注册过，在后台登录，session超时也走这
//                    TerminalFactory.getSDK().getAuthManagerTwo().login();
                    logger.info("信令服务器通知NotifyForceRegisterMessage消息，在MainActivity中登录了");
                } else {//没注册过，关掉主界面，去注册界面
                    startActivity(new Intent(MainActivity.this, RegistActivity.class));
                    MainActivity.this.finish();
                }
            }
        }
    };

    /**
     * 网络连接状态
     */
    private ReceiveServerConnectionEstablishedHandler receiveServerConnectionEstablishedHandler = new ReceiveServerConnectionEstablishedHandler() {
        @Override
        public void handler(boolean connected) {
            logger.info("收到ReceiveServerConnectionEstablishedHandler -- " + connected);
            if (connected) {
//                MyTerminalFactory.getSDK().putParam(Params.NET_OFFLINE, false);
                handler.post(() -> {
                    if (checkIsWatchingLive()) {
                        stopPull(false);
                    }
                    if (MainActivity.this.message != null) {
                        requestToWatchLiving(MainActivity.this.message);
                    }
                    showToast("网络已连接");
                    if (noLive != null) {
                        noLive.setText("暂无图像");
                    }
                    showConnectToOtherView(noLive);
                });
            } else {
                handler.post(() -> {
                    stopPull(false);
                    logger.info("设置网络断开的UI显示");
                    showForeground(true);
                    showToast("网络连接已断开");
                    if (noLive != null) {
                        noLive.setText("网络连接已断开");
                    }
                    showConnectToOtherView(tv_connect_to_other_failed);
                });
            }
        }
    };

    private ReceiveUpdateConfigHandler mReceiveUpdateConfigHandler = () -> {
//            handler.post(() -> tv_user_name.setText(TerminalFactory.getSDK().getParam(Params.MEMBER_NAME, "")));
    };

    private ReceiveLoginResponseHandler mReceiveLoginResponseHandler = (resultCode, resultDesc) -> handler.post(() -> {
        if (resultCode == BaseCommonCode.SUCCESS_CODE) {
            //每次启动都要更新数据，不然本地存的数据和服务不一样就会有问题
            if (noLive != null) {
                noLive.setText("暂无图像");
            }
            showConnectToOtherView(noLive);
            //登录响应成功，把第一次登录标记置为false；
            MyTerminalFactory.getSDK().putParam(Params.IS_FIRST_LOGIN, false);
        } else {
            showToast("请重新登录");
            handler.postDelayed(() -> {
                startActivity(new Intent(MainActivity.this, RegistActivity.class));
                MainActivity.this.finish();
            }, 3000);
        }
    });

    private TimerTask messageTimerTask;
    /**
     * 收到观看上报图像的通知
     */
    private ReceiveNotifyInviteToWatchHandler receiveNotifyInviteToWatchHandler = message -> {
        if (message == null) {
            return;
        }
        if (JSONObject.parseObject(message.getMessageBody()).getIntValue("remark") == Remark.INFORM_TO_WATCH_LIVE ||
                JSONObject.parseObject(message.getMessageBody()).getIntValue("remark") == Remark.EMERGENCY_INFORM_TO_WATCH_LIVE) {
            clearRotationLive();
            checkWatchStateAndStartWatch(message);
        }
    };

    /**
     * 接收到消息
     */
    private ReceiveNotifyDataMessageHandler mReceiveNotifyDataMessageHandler = terminalMessage -> {
        if (terminalMessage == null) {
            return;
        }
        logger.info("接收到消息" + terminalMessage.toString());
        //是否为别人发的消息
        boolean isReceiver = terminalMessage.messageFromId != MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
        if (!isReceiver) {
            return;
        }
        //判断消息类型
        if ((terminalMessage.messageType == MessageType.GB28181_RECORD.getCode()) || terminalMessage.messageType == MessageType.OUTER_GB28181_RECORD.getCode()) {
            PTTProtolbuf.NotifyDataMessage message = terminalMessageToTerminalMessage(terminalMessage);
            if (message != null) {
                clearRotationLive();
                checkWatchStateAndStartWatch(message);
            }
        }
    };

    /**
     * 获取到rtsp地址，开始播放视频
     */
    private ReceiveGetRtspStreamUrlHandler receiveGetRtspStreamUrlHandler = (rtspUrl, liveMember, callId) -> handler.post(() -> {
        MainActivity.this.liveMember = liveMember;
        MainActivity.this.callId = callId;
        checkStartPull(rtspUrl, getShowContent(liveMember));
        //获取上报者的信息
        if (liveMember != null) {
            TerminalFactory.getSDK().getThreadPool().execute(() -> {
                Member member = TerminalFactory.getSDK().getConfigManager().getMemberByNo(liveMember.getUniqueNo());
                if (member != null) {
                    handler.post(() -> {
                        MainActivity.this.showContent = getShowContent(member);
                        tv_live_content.setText(MainActivity.this.showContent);
                    });
                }
            });
        }
    });

    /**
     * 通知直播停止 通知界面关闭视频页
     **/
    private ReceiveNotifyLivingStoppedHandler receiveNotifyLivingStoppedHandler = (liveMemberId, callId, methodResult, resultDesc) -> handler.post(() -> {
        //需要判断停止直播的是不是当前观看的
        cancelTask();
        allFinishWatchLive();
        showToast("图像上报已结束!");
        toWatchNext(false);
    });

    /**
     * 超时未回复answer 通知界面关闭
     **/
    private ReceiveAnswerLiveTimeoutHandler mReceiveAnswerLiveTimeoutHandler = () -> handler.post(() -> {
        showToast("回答超时！");
        allFinishWatchLive();
        toWatchNext(false);
    });

    /**
     * 去观看时，发现没有在直播，关闭界面吧
     */
    private ReceiveMemberNotLivingHandler mReceiveMemberNotLivingHandler = callId -> handler.post(() -> {
        showToast("图像上报已结束!");
        allFinishWatchLive();
        cancelTask();
        toWatchNext(false);
    });

    /**
     * 观看图像的监听
     */
    ResultReceiver mResultReceiver = new ResultReceiver(new Handler()) {
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            Log.e("mResultReceiver", resultCode + "---" + resultData);
            if (resultCode == EasyRTSPClient.RESULT_VIDEO_DISPLAYED) {
                pullcount = 0;
            } else if (resultCode == EasyRTSPClient.RESULT_VIDEO_SIZE) {
                int mWidth = resultData.getInt(EasyRTSPClient.EXTRA_VIDEO_WIDTH);
                int mHeight = resultData.getInt(EasyRTSPClient.EXTRA_VIDEO_HEIGHT);

                if (mWidth < mHeight) {
                    logger.info("图像旋转角度====" + svLive.getRotation());
                    DisplayMetrics dm = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(dm);
                    int mSurfaceViewWidth = dm.widthPixels;
                    int mSurfaceViewHeight = dm.heightPixels;
                    logger.info("mWidth = " + mWidth + ", mHeight = " + mHeight + ", mSurfaceViewWidth = " + mSurfaceViewWidth + ", mSurfaceViewHeight " + mSurfaceViewHeight);
                    int marginWidth = (mSurfaceViewWidth - mWidth) / 2;
                    int marginHeight = 0;
                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.MATCH_PARENT);
                    logger.info("marginWidth = " + marginWidth + ", marginHeight = " + marginHeight);
                    lp.setMargins(marginWidth, marginHeight, marginWidth, marginHeight);
                    svLive.setLayoutParams(lp);
                } else {
                    logger.info("图像旋转角度====" + svLive.getRotation());
                    DisplayMetrics dm = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(dm);
                    int mSurfaceViewWidth = dm.widthPixels;
                    int mSurfaceViewHeight = dm.heightPixels;
                    logger.info("看看控件：" + mSurfaceViewWidth + "高度：" + mSurfaceViewHeight
                            + "视频的宽：" + mWidth + "高：" + mHeight);

                    double mRatio = (float) mWidth / mHeight;
                    double mSurfaceViewRatio = (float) mSurfaceViewWidth / mSurfaceViewHeight;

                    int marginWidth = 0;
                    int marginHeight = 0;
                    if (mRatio > mSurfaceViewRatio) {
                        marginWidth = 0;
                        marginHeight = (mSurfaceViewHeight - mHeight * mSurfaceViewWidth / mWidth) / 2;
                    } else {
                        marginWidth = (mSurfaceViewWidth - mWidth * mSurfaceViewHeight / mHeight) / 2;
                        marginHeight = 0;

                    }
                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.MATCH_PARENT);
                    logger.info("marginWidth" + marginWidth + "marginHeight" + marginHeight + "marginWidth" + marginWidth + "marginHeight" + marginHeight);
                    lp.setMargins(marginWidth, marginHeight, marginWidth, marginHeight);
                    svLive.setLayoutParams(lp);
                }
                showForeground(false);
                showConnectToOtherView(null);
            } else if (resultCode == EasyRTSPClient.RESULT_TIMEOUT) {
                new AlertDialog.Builder(MainActivity.this).setMessage("试播时间到").setTitle("SORRY").setPositiveButton(android.R.string.ok, null).show();
            } else if (resultCode == EasyRTSPClient.RESULT_UNSUPPORTED_AUDIO) {
                new AlertDialog.Builder(MainActivity.this).setMessage("音频格式不支持").setTitle("SORRY").setPositiveButton(android.R.string.ok, null).show();
            } else if (resultCode == EasyRTSPClient.RESULT_UNSUPPORTED_VIDEO) {
                new AlertDialog.Builder(MainActivity.this).setMessage("视频格式不支持").setTitle("SORRY").setPositiveButton(android.R.string.ok, null).show();
            } else if (resultCode == EasyRTSPClient.RESULT_EVENT) {

                int errorcode = resultData.getInt("errorcode");
                String resultDataString = resultData.getString("event-msg");
                if (errorcode != 0) {
                    stopPull(true);
                }
                if (errorcode == -101) {
                    sendMessage("请检查网络连接");
                    ceaseWatching();
                    finishWatchLive();
                }
                if (errorcode == 500 || errorcode == 404 || errorcode == -32) {
                    if (pullcount < 3) {
                        try {
                            Thread.sleep(300);
                            logger.error("请求第" + pullcount + "次");
                            if (MainActivity.this.message != null) {
                                requestToWatchLiving(MainActivity.this.message);
                            }
                            pullcount++;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        ceaseWatching();
                        allFinishWatchLive();
                        sendMessage("图像上报已结束");
                        toWatchNext(false);
                    }
                } else if (errorcode == 0) {
                    return;
                } else {
                    logger.info("sjl_错误日志");
                    sendMessage(resultDataString);
                    allFinishWatchLive();
                    toWatchNext(false);
                }
            } else if (resultCode == EasyRTSPClient.RESULT_RECORD_BEGIN) {
            } else if (resultCode == EasyRTSPClient.RESULT_RECORD_END) {
            }
        }
    };

    /**
     * TextureView的surface状态改变的监听
     */
    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            logger.info("onSurfaceTextureAvailable   width(" + i + ") or height(" + i1 + ")");
            if (MainActivity.this.message != null) {
                requestToWatchLiving(MainActivity.this.message);
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            finishWatchLive();
            stopPull(true);
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }
    };

    /**
     * 更新所有成员列表
     */
    private ReceiveNotifyMemberChangeHandler receiveNotifyMemberChangeHandler = memberChangeType -> {
        logger.info("setting的memberChangeType" + memberChangeType);
//        handler.post(() -> tv_user_name.setText(TerminalFactory.getSDK().getParam(Params.MEMBER_NAME, "")));
    };

    private final class OnClickListenerImplementationLogUpload implements
            View.OnClickListener {
        @Override
        public void onClick(View v) {
            uploadLog();
        }
    }


    /**
     * 日志上传是否成功的消息
     */
    private ReceiveLogFileUploadCompleteHandler receiveLogFileUploadCompleteHandler = (resultCode, type) -> handler.post(() -> {
        if (resultCode == BaseCommonCode.SUCCESS_CODE) {
            ToastUtil.showToast(MainActivity.this, "日志上传成功，感谢您的支持！");
        } else {
            ToastUtil.showToast("日志上传失败，请稍后重试！", MainActivity.this);
        }
    });

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OPEN_ACCESSIBILITY_SETTING_REQUESTCODE) {
            if (EnableServiceUtils.isAccessibilitySettingsOn(MainActivity.this, MAcessibilityService.class.getName())) {
                UpdateManager manager = new UpdateManager(MainActivity.this);
                manager.checkUpdate(MyTerminalFactory.getSDK().getParam(Params.UPDATE_URL, ""), false);
            }
        }
    }

    /**
     * 邀请码用户名输入框焦点
     */
    private View.OnFocusChangeListener onFocusChangeListener = (v, hasFocus) -> {
        if (hasFocus) {
            ViewCompat.animate(v).scaleX(1.2f).scaleY(1.2f).start();
            v.setBackgroundColor(getResources().getColor(R.color.transparent_20));
        } else {
            ViewCompat.animate(v).scaleX(1.0f).scaleY(1.0f).start();
			v.setBackgroundColor(getResources().getColor(R.color.TRANSPARENT));
        }
    };

    /**
     * 通知终端停止观看直播
     **/
    private ReceiveNotifyMemberStopWatchMessageHandler receiveNotifyMemberStopWatchMessageHandler = message -> {
        handler.post(() -> {
            if (MainActivity.this.message != null) {
                if (MainActivity.this.message.getMessageType() == MessageType.VIDEO_LIVE.getCode()) {
                    if (TextUtils.equals(JSONObject.parseObject(MainActivity.this.message.getMessageBody()).getString(JsonParam.CALLID), String.valueOf(message.getCallId()))) {
                        ToastUtil.showToast(MyTerminalFactory.getSDK().application, getResources().getString(R.string.force_stop_watch));
                        finishWatchLive();
                    }
                } else if (MainActivity.this.message.getMessageType() == MessageType.GB28181_RECORD.getCode()) {

                }
            }
        });
    };

    /**
     * 收到轮播推送的通知
     **/
    private ReceiveNotifyPushPartyLiveMessageHandler receiveNotifyPushPartyLiveMessageHandler = (message) -> {
        handler.post(() -> {
            myHandler.removeCallbacksAndMessages(null);
            prepareStartRotationLive(addRotationLiveData(message));
        });
    };

    /*******************************************************************   监听   ***************************************************************************/

    /**
     * 检查当前观看的状态并开始观看
     *
     * @param message
     */
    private void checkWatchStateAndStartWatch(final PTTProtolbuf.NotifyDataMessage message) {
        //观看手机推送图像
        if (MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_ACCEPT.name())) {
            //如果正在观看，先停止之前的图像，再请求观看现在的图像
            if (checkIsWatchingLive()) {
                handler.post(() -> stopPull(false));
            }
            if (messageTimerTask != null) {
                messageTimerTask.cancel();
                messageTimerTask = null;
            }
            messageTimerTask = new TimerTask() {
                @Override
                public void run() {
                    handler.post(() -> {
                        if (noLive != null) {
                            noLive.setText("暂无图像");
                            showConnectToOtherView(null);
                        }
                    });
                    requestToWatchLiving(message);
                }
            };
            handler.post(() -> {
                allFinishWatchLive();
                if (noLive != null) {
                    noLive.setText("图像连接中...");
                    showConnectToOtherView(noLive);
                }
            });
            if (messageTimerTask != null) {
                MyTerminalFactory.getSDK().getTimer().schedule(messageTimerTask, 5000);
            }
        } else {
            handler.post(() -> {
                ToastUtil.showToast(MainActivity.this, "没有图像观看的权限");
                cancelTask();
                allFinishWatchLive();
            });
        }
    }

    /**
     * 开始拉流
     *
     * @param surface
     */
    private void startPull(SurfaceTexture surface) {
        mStreamRender = new EasyRTSPClient(MainActivity.this, PLAYKEY, surface, mResultReceiver);
        try {
            if (streamMediaServerUrl != null) {
                //显示上报图像的信息
                tv_title.setText((MainActivity.this.message != null) ? MainActivity.this.message.getMessageFromName() + " 推送的图像" : "");
                mStreamRender.stop();
                mStreamRender.start(streamMediaServerUrl, RTSPClient.TRANSTYPE_TCP, RTSPClient.EASY_SDK_VIDEO_FRAME_FLAG | RTSPClient.EASY_SDK_AUDIO_FRAME_FLAG, "", "", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            //Toast.makeText(IndividualCallService.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            logger.error("IndividualCallService :" + e.toString());
        }
    }

    /**
     * 停止拉流
     */
    private void stopPull(boolean isShowNoLive) {
        if (mStreamRender != null) {
            mStreamRender.stop();
            recoverStateMachine();
            if (noLive != null) {
                noLive.setText("暂无图像");
                showConnectToOtherView(noLive);
                if (!isShowNoLive) {
                    noLive.setVisibility(View.GONE);
                }
            }
        }
    }

    /**
     * 检查是否还在上报，如果还在上报就申请观看
     *
     * @param message
     */
    private void requestToWatchLiving(final PTTProtolbuf.NotifyDataMessage message) {
        if (message.getMessageType() == MessageType.VIDEO_LIVE.getCode()) {
            //rtsp
            goWatchRTSP(message);
        } else if (message.getMessageType() == MessageType.OUTER_GB28181_RECORD.getCode()) {
            //海康摄像头
            goWatchOuterGB28121(message);
        } else if (message.getMessageType() == MessageType.GB28181_RECORD.getCode()) {
            //国标
            goWatchGB28121(message);
        }
    }

    /**
     * 观看rtsp
     *
     * @param message
     */
    private void goWatchRTSP(final PTTProtolbuf.NotifyDataMessage message) {
        MyTerminalFactory.getSDK().getThreadPool().execute(() -> {
            String serverIp = MyTerminalFactory.getSDK().getParam(Params.FILE_SERVER_IP, "");
            int serverPort = MyTerminalFactory.getSDK().getParam(Params.FILE_SERVER_PORT, 0);
            String url = "http://" + serverIp + ":" + serverPort + "/file/download/isLiving";
            Map<String, String> paramsMap = new HashMap<>();
            paramsMap.put("callId", JSONObject.parseObject(message.getMessageBody()).getString(JsonParam.CALLID));
            paramsMap.put("sign", SignatureUtil.sign(paramsMap));
            logger.info("查看视频播放是否结束url：" + url);
            String result = MyTerminalFactory.getSDK().getHttpClient().sendGet(url, paramsMap);
            logger.info("查看视频播放是否结束结果：" + result);
            if (!Util.isEmpty(result)) {
                JSONObject jsonObject = JSONObject.parseObject(result);
                boolean living = jsonObject.getBoolean("living");
                Long endChatTime = jsonObject.getLong("endChatTime");
                if (living) {
                    int resultCode = MyTerminalFactory.getSDK().getLiveManager().requestToWatchLiving(message);
                    if (resultCode == 0) {
                        //观看上报图像
                        MainActivity.this.message = message;
                    } else {
                        ToastUtil.livingFailToast(MainActivity.this, resultCode, TerminalErrorCode.LIVING_PLAYING.getErrorCode());
                        handler.post(() -> {
                            cancelTask();
                            allFinishWatchLive();
                            toWatchNext(false);
                        });
                    }
                } else {
                    handler.post(() -> {
                        showToast("图像上报已结束");
                        cancelTask();
                        allFinishWatchLive();
                        toWatchNext(false);
                    });
                }
            }
        });
    }

    /**
     * 海康摄像头
     *
     * @param message
     */
    private void goWatchOuterGB28121(final PTTProtolbuf.NotifyDataMessage message) {
        final JSONObject messageBody = JSONObject.parseObject(message.getMessageBody());
        if (messageBody == null) {
            return;
        }
//        if (messageBody.containsKey(JsonParam.GB28181_RTSP_URL)) {
        final String deviceId = messageBody.getString(JsonParam.DEVICE_ID);
        TerminalFactory.getSDK().getThreadPool().execute(() -> {
            String url = MyTerminalFactory.getSDK().getParam(Params.HIKVISION_CAMERADATA_URL) + "?cameraNo=" + deviceId;
            String response = MyTerminalFactory.getSDK().getHttpClient().sendGet(url);
            if (!TextUtils.isEmpty(response)) {
                JSONObject jsonObject = parseObject(response);
                String result = jsonObject.getString("result");
                if ("success".equals(result)) {
                    JSONObject resultdata = jsonObject.getJSONObject("resultdata");
                    String code = resultdata.getString("code");
                    if ("0".equals(code)) {
                        JSONObject data = resultdata.getJSONObject("data");
                        final String gb28181Url = data.getString("url");
                        logger.info("播放地址：" + gb28181Url);
                        MainActivity.this.message = message;
                        handler.post(() -> checkStartPull(gb28181Url, getShowContent(deviceId)));
                        return;
                    }
                }
            }
            handler.post(() -> {
                showToast("获取视频地址失败");
                allFinishWatchLive();
                toWatchNext(false);
            });
        });
//        }
    }

    /**
     * 国标平台
     *
     * @param message
     */
    private void goWatchGB28121(final PTTProtolbuf.NotifyDataMessage message) {
        final JSONObject messageBody = JSONObject.parseObject(message.getMessageBody());
        if (messageBody == null) {
            return;
        }
        if (messageBody.containsKey(JsonParam.GB28181_RTSP_URL)) {
            String gb28181Url = messageBody.getString(JsonParam.GB28181_RTSP_URL);
            logger.info("播放地址：" + gb28181Url);
            String deviceName = messageBody.getString(JsonParam.DEVICE_NAME);
//            String deviceDeptId = messageBody.getString(JsonParam.DEVICE_DEPT_ID);
            String deviceDeptName = messageBody.getString(JsonParam.DEVICE_DEPT_NAME);
            MainActivity.this.message = message;
            handler.post(() -> checkStartPull(gb28181Url, getShowContent(deviceName, deviceDeptName)));
        }

    }

    /**
     * 准备开始轮播
     */
    private void prepareStartRotationLive(List<PTTProtolbuf.NotifyDataMessage> liveList) {
        if (liveList != null && !liveList.isEmpty()) {
            //停止观看
            clearRotationLive();
            allFinishWatchLive();
            //添加数据
            liveDescList.addAll(liveList);
            startRotationLive();
        }
    }

    /**
     * 开始轮播图像
     */
    private void startRotationLive() {
        PTTProtolbuf.NotifyDataMessage message = liveDescList.get(rotationIndex);
        if (message != null) {
            checkWatchStateAndStartWatch(message);
        }
    }


    /**
     * 检查rtsp地址
     *
     * @param url
     */
    private void checkStartPull(String url, String showContent) {
        if (Util.isEmpty(url)) {
            streamMediaServerUrl = null;
            MainActivity.this.message = null;
            MainActivity.this.showContent = "";
            showToast("没有获取到数据流地址或图像上报已结束");
            finishWatchLive();
            toWatchNext(false);
        } else {
            logger.info("url ----> " + url);
            streamMediaServerUrl = url;
            MainActivity.this.showContent = showContent;
            if (svLive.getSurfaceTexture() != null) {
                handler.post(() -> {
                    tv_live_content.setText(MainActivity.this.showContent);
                    startPull(svLive.getSurfaceTexture());
                });
            }
            PromptManager.getInstance().stopRing();
            toWatchNext(true);
        }

    }

    /**
     * 切换到下个live
     *
     * @param delay
     */
    private void toWatchNext(boolean delay) {
        if (!liveDescList.isEmpty()) {
            myHandler.postDelayed(() -> {
                rotationIndex++;
                if(rotationIndex>=liveDescList.size()){
                    rotationIndex = 0;
                }
                if (rotationIndex >= 0 && rotationIndex < liveDescList.size()) {
                    allFinishWatchLive();
                    startRotationLive();
                }else {
                    allFinishWatchLive();
                    clearRotationLive();
                }
            }, delay ? ROTATION_TIME : 0);
        }
    }

    /**
     * 恢复到等待页面
     */
    private void allFinishWatchLive() {
        streamMediaServerUrl = null;
        MainActivity.this.message = null;
        stopPull(true);
        finishWatchLive();
    }

    /**
     * 结束Task
     */
    private void cancelTask() {
        if (messageTimerTask != null) {
            MyTerminalFactory.getSDK().getTimer().purge();
            messageTimerTask.cancel();
            messageTimerTask = null;
        }
    }

    /**
     * 发送Toast消息
     *
     * @param message
     */
    private void sendMessage(String message) {
        Message msg = Message.obtain();
        msg.what = MSG_STATE;
        Bundle bundle = new Bundle();
        bundle.putString(STATE, message);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    /**
     * show  Toast
     *
     * @param resultDesc
     */
    private void showToast(final String resultDesc) {
        handler.post(() -> ToastUtil.showToast(MainActivity.this, resultDesc));
    }

    /**
     * 前景图片的显示和隐藏
     *
     * @param isShow
     */
    private void showForeground(boolean isShow) {
        if (foreground == null || icon == null || llTempt == null
                ||tv_user_id == null || tv_user_name == null)
            return;
//                tv_user_id == null || tv_user_name == null)

        if (isShow) {
            foreground.setVisibility(View.VISIBLE);
            icon.setVisibility(View.VISIBLE);
            llTempt.setVisibility(View.VISIBLE);
            tv_user_id.setVisibility(View.VISIBLE);
            tv_user_name.setVisibility(View.VISIBLE);
            tv_log_upload.setVisibility(View.VISIBLE);
            tv_title.setText("");
            tv_live_content.setText("");
        } else {
            pullcount = 0;
            foreground.setVisibility(View.INVISIBLE);
            icon.setVisibility(View.INVISIBLE);
            llTempt.setVisibility(View.INVISIBLE);
            tv_user_id.setVisibility(View.INVISIBLE);
            tv_user_name.setVisibility(View.INVISIBLE);
            tv_log_upload.setVisibility(View.GONE);
        }
    }

    /**
     * 检查是否在观看中
     *
     * @return
     */
    private boolean checkIsWatchingLive() {
        return MyTerminalFactory.getSDK().getLiveManager().getVideoLivePlayingStateMachine().getCurrentState() != VideoLivePlayingState.IDLE
                || ((foreground != null) && (foreground.getVisibility() != View.VISIBLE));
    }

    /**
     * 结束观看
     */
    private void finishWatchLive() {
        showForeground(true);
        showConnectToOtherView(noLive);
    }

    /**
     * 清空轮播图像
     */
    private void clearRotationLive() {
        rotationIndex = 0;
        liveDescList.clear();
    }

    private void recoverStateMachine() {
        if (checkIsWatchingLive()) {
            ceaseWatching();
        }
    }

    private void ceaseWatching() {
        if (liveMember != null && callId != 0) {
            MyTerminalFactory.getSDK().getLiveManager().ceaseWatching(liveMember.id, callId, liveMember.getUniqueNo());
        } else {
            MyTerminalFactory.getSDK().getLiveManager().ceaseWatching();
        }
    }


    /**
     * 返回键监听
     */
    @Override
    public void onBackPressed() {
        if (checkIsWatchingLive()) {
            allFinishWatchLive();
            clearRotationLive();
        } else {
            if (isQuiteApp) {
                ActivityCollector.removeAllActivity();
            } else {
                showToast("再按一次退出应用");
                isQuiteApp = true;
                handler.sendEmptyMessageDelayed(MSG_BACKPRESS, 2000);
            }
        }
    }

    private void showConnectToOtherView(View view) {
        if (noLive == null || tv_connecting_to_other == null
                || tv_connect_to_other_success == null || tv_connect_to_other_failed == null) {
            return;
        }
        if (view == null) {
            noLive.setVisibility(View.GONE);
            tv_connecting_to_other.setVisibility(View.GONE);
            tv_connect_to_other_success.setVisibility(View.GONE);
            tv_connect_to_other_failed.setVisibility(View.GONE);
        } else if (view == noLive) {
            noLive.setVisibility(View.VISIBLE);
            tv_connecting_to_other.setVisibility(View.GONE);
            tv_connect_to_other_success.setVisibility(View.GONE);
            tv_connect_to_other_failed.setVisibility(View.GONE);
        } else if (view == tv_connecting_to_other) {
            noLive.setVisibility(View.GONE);
            tv_connecting_to_other.setVisibility(View.VISIBLE);
            tv_connect_to_other_success.setVisibility(View.GONE);
            tv_connect_to_other_failed.setVisibility(View.GONE);
        } else if (view == tv_connect_to_other_success) {
            noLive.setVisibility(View.GONE);
            tv_connecting_to_other.setVisibility(View.GONE);
            tv_connect_to_other_success.setVisibility(View.VISIBLE);
            tv_connect_to_other_failed.setVisibility(View.GONE);
        } else if (view == tv_connect_to_other_failed) {
            noLive.setVisibility(View.GONE);
            tv_connecting_to_other.setVisibility(View.GONE);
            tv_connect_to_other_success.setVisibility(View.GONE);
            tv_connect_to_other_failed.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 隐藏虚拟按键
     */
    private void hideSmartBar() {
        int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN; // hide status bar

        if (Build.VERSION.SDK_INT >= 19) {
            uiFlags |= View.SYSTEM_UI_FLAG_IMMERSIVE;//0x00001000; // SYSTEM_UI_FLAG_IMMERSIVE_STICKY: hide
        } else {
            uiFlags |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
        }

        try {
            getWindow().getDecorView().setSystemUiVisibility(uiFlags);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    /**
     * 上传日志
     */
    public void uploadLog() {
        dialog = new DialogUtil() {
            @Override
            public CharSequence getMessage() {
                return "确定上传日志?";
            }

            @Override
            public Context getContext() {
                return MainActivity.this;
            }

            @Override
            public void doConfirmThings() {
                if (System.currentTimeMillis() - uploadLogTime > 5000) {
                    MyTerminalFactory.getSDK().getThreadPool().execute(() -> MyTerminalFactory.getSDK().getLogFileManager().uploadAllLogFile());
                    uploadLogTime = System.currentTimeMillis();
                } else {
                    ToastUtil.showToast("请稍后再上传", MainActivity.this);
                }
            }

            @Override
            public void doCancelThings() {

            }
        }.showDialog();
    }

    /**
     * 获取显示的上报账号的信息
     *
     * @param member
     * @return
     */
    private String getShowContent(Member member) {
        StringBuilder content = new StringBuilder();
        if (member != null && !TextUtils.isEmpty(member.getTerminalMemberType())) {
            content.append(member.getName()).append(" ").append(member.getNo()).append(" ").append(member.getDepartmentName()).append("\n");
            content.append(TerminalMemberType.valueOf(member.getTerminalMemberType()).getValue());
            content.append("上报图像");
        }
        return content.toString();
    }

    /**
     * 获取显示的上报账号的信息
     *
     * @param devicesId
     * @return
     */
    private String getShowContent(String devicesId) {
        StringBuilder content = new StringBuilder();
        if (!TextUtils.isEmpty(devicesId)) {
            content.append(devicesId).append("上报图像");
        }
        return content.toString();
    }

    /**
     * 获取显示的上报账号的信息
     *
     * @param devicesName
     * @param deviceDeptName
     * @return
     */
    private String getShowContent(String devicesName, String deviceDeptName) {
        StringBuffer content = new StringBuffer();
        if (!TextUtils.isEmpty(devicesName) || !TextUtils.isEmpty(deviceDeptName)) {
            content.append(devicesName).append(" ").append(deviceDeptName).append("\n").append("上报图像");
        }
        return content.toString();
    }

    /**
     * TerminalMessage 转 NotifyDataMessage
     *
     * @param terminalMessage
     * @return PTTProtolbuf.NotifyDataMessage
     */
    private PTTProtolbuf.NotifyDataMessage terminalMessageToTerminalMessage(TerminalMessage terminalMessage) {
        PTTProtolbuf.NotifyDataMessage.Builder builder = PTTProtolbuf.NotifyDataMessage.newBuilder();
        builder.setMessageUrl(terminalMessage.messageUrl);
        builder.setMessageFromName(terminalMessage.messageFromName);
        builder.setMessageFromNo(terminalMessage.messageFromId);
        builder.setMessageToName(terminalMessage.messageToName);
        builder.setMessageToNo(terminalMessage.messageToId);
        builder.setMessageType(terminalMessage.messageType);
        builder.setMessageVersion(terminalMessage.messageVersion);
        builder.setResultCode(terminalMessage.resultCode);
        builder.setSendingTime(terminalMessage.sendTime);
        builder.setMessageBody(terminalMessage.messageBody.toString());
        return builder.build();
    }

    /**
     * 添加NotifyDataMessage
     * @param message
     * @return
     */
    private List<PTTProtolbuf.NotifyDataMessage> addRotationLiveData(PTTProtolbuf.NotifyPushPartyLiveMessage message) {
        List<PTTProtolbuf.NotifyDataMessage> list = new ArrayList<>();
        if (message != null && message.getLiveDescListList() != null && !message.getLiveDescListList().isEmpty()) {
            List<String> strings = DataUtil.getRotationLiveBeanList(message.getLiveDescListList().get(0));
            for (String string : strings) {
                PTTProtolbuf.NotifyDataMessage message1 = analysisRotationLiveData(message,string);
                if (message1 != null) {
                    list.add(message1);
                }
            }
        }
        return list;
    }

    /**
     * 重装NotifyDataMessage
     * @param message
     * @param string
     * @return
     */
    private PTTProtolbuf.NotifyDataMessage analysisRotationLiveData(PTTProtolbuf.NotifyPushPartyLiveMessage message,String string) {
        PTTProtolbuf.NotifyDataMessage result = null;
        RotationLiveBean bean = DataUtil.getRotationLiveBean(string);
        if(!TextUtils.isEmpty(string)&&bean!=null){
            PTTProtolbuf.NotifyDataMessage.Builder builder = PTTProtolbuf.NotifyDataMessage.newBuilder();
            Account account = cn.vsx.hamster.terminalsdk.tools.DataUtil.getAccountByMemberNo(message.getRequestMemberId(),false);
            builder.setMessageFromName((account!=null)?account.getName():"");
            builder.setMessageFromNo(message.getRequestMemberId());
            builder.setMessageFromUniqueNo(message.getRequestUniqueNo());
            builder.setMessageToName(MyTerminalFactory.getSDK().getParam(Params.MEMBER_NAME, ""));
            builder.setMessageToNo(TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0));
            builder.setMessageToUniqueNo(TerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO,0L));
            builder.setMessageVersion(message.getVersion());
            builder.setResultCode(0);
            builder.setSendingTime(System.currentTimeMillis());

            int type = bean.getType();
            if (type == RotationImageType.RTSP.getCode()) {
                builder.setMessageType(MessageType.VIDEO_LIVE.getCode());
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(JsonParam.CALLID, bean.getData().getCallId());
                jsonObject.put(JsonParam.LIVER, bean.getData().getLiveUniqueNo() + "_" + bean.getData().getLiveName());
                jsonObject.put(JsonParam.LIVERNO, bean.getData().getLiveNo());
                String rtspUrl = "rtsp://" + TerminalFactory.getSDK().getParam(Params.MEDIA_SERVER_IP, "") + ":" + TerminalFactory.getSDK().getParam(Params.MEDIA_SERVER_PORT, 0) + "/" +
                        bean.getData().getLiveUniqueNo() + "_" + bean.getData().getCallId() + ".sdp";
                jsonObject.put(JsonParam.EASYDARWIN_RTSP_URL, rtspUrl);
                builder.setMessageBody(jsonObject.toString());
            } else if (type == RotationImageType.OuterGB28181.getCode()) {
                builder.setMessageType(MessageType.OUTER_GB28181_RECORD.getCode());
                String deviceId = bean.getData().getDeviceId();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(JsonParam.DEVICE_ID, deviceId);
                builder.setMessageBody(jsonObject.toString());
            } else if (type == RotationImageType.GB28181.getCode()) {
                builder.setMessageType(MessageType.GB28181_RECORD.getCode());
                String deviceId = bean.getData().getDeviceId();
                String gateWayUrl = TerminalFactory.getSDK().getParam(Params.GATE_WAY_URL);
                String gb28181RtspUrl = gateWayUrl + "DevAor=" + deviceId;
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(JsonParam.DEVICE_ID, deviceId);
                jsonObject.put(JsonParam.GB28181_RTSP_URL, gb28181RtspUrl);
                jsonObject.put(JsonParam.DEVICE_NAME, "");
                jsonObject.put(JsonParam.DEVICE_DEPT_NAME, "");
                builder.setMessageBody(jsonObject.toString());
            }
            result = builder.build();
        }
        return result;
    }
}

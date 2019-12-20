package cn.vsx.vc.activity;

import android.Manifest;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.google.gson.internal.LinkedTreeMap;
import com.zectec.imageandfileselector.base.Constant;
import com.zectec.imageandfileselector.bean.FileInfo;
import com.zectec.imageandfileselector.bean.Image;
import com.zectec.imageandfileselector.bean.Record;
import com.zectec.imageandfileselector.fragment.FileMainFragment;
import com.zectec.imageandfileselector.fragment.ImagePreviewFragment;
import com.zectec.imageandfileselector.fragment.ImageSelectorFragment;
import com.zectec.imageandfileselector.receivehandler.ReceiverSendFileCheckMessageHandler;
import com.zectec.imageandfileselector.receivehandler.ReceiverSendFileHandler;
import com.zectec.imageandfileselector.receivehandler.ReceiverToFaceRecognitionHandler;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;
import com.zectec.imageandfileselector.view.LoadingCircleView;

import org.apache.http.util.TextUtils;
import org.apache.log4j.Logger;
import org.ddpush.im.common.v1.handler.PushMessageSendResultHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.vsx.hamster.common.Constants;
import cn.vsx.hamster.common.MessageCategory;
import cn.vsx.hamster.common.MessageSendStateEnum;
import cn.vsx.hamster.common.MessageStatus;
import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.Remark;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.common.util.NoCodec;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.SignalServerErrorCode;
import cn.vsx.hamster.errcode.module.TerminalErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.audio.IAudioPlayComplateHandler;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePlayingState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePushingState;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.GetHistoryMessageRecordHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.GetWarningMessageDetailHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveDownloadFinishHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveDownloadProgressHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetGPSLocationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupOrMemberNotExistHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHistoryMessageNotifyDateHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHistoryMultimediaFailHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMultimediaMessageCompleteHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyDataMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyRecallRecordHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseRecallRecordHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSendDataMessageFailedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSendDataMessageSuccessHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUploadProgressHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiverReplayIndividualChatVoiceHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.SignatureUtil;
import cn.vsx.hamster.terminalsdk.tools.Util;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.TemporaryAdapter;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.dialog.NFCBindingDialog;
import cn.vsx.vc.dialog.QRBindingDialog;
import cn.vsx.vc.fragment.LocationFragment;
import cn.vsx.vc.model.ContactItemBean;
import cn.vsx.vc.model.PlayType;
import cn.vsx.vc.model.TransponSelectedBean;
import cn.vsx.vc.receiveHandle.ReceiverChatListItemClickHandler;
import cn.vsx.vc.receiveHandle.ReceiverSelectChatListHandler;
import cn.vsx.vc.receiveHandle.ReceiverShowCopyPopupHandler;
import cn.vsx.vc.receiveHandle.ReceiverShowForwardMoreHandler;
import cn.vsx.vc.receiveHandle.ReceiverShowTransponPopupHandler;
import cn.vsx.vc.receiveHandle.ReceiverShowWithDrawPopupHandler;
import cn.vsx.vc.record.MediaManager;
import cn.vsx.vc.service.PullLivingService;
import cn.vsx.vc.utils.BitmapUtil;
import cn.vsx.vc.utils.DensityUtil;
import cn.vsx.vc.utils.FileUtil;
import cn.vsx.vc.utils.HandleIdUtil;
import cn.vsx.vc.utils.StatusBarUtil;
import cn.vsx.vc.utils.ToastUtil;
import cn.vsx.vc.view.FixedRecyclerView;
import cn.vsx.vc.view.FunctionHidePlus;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.audio.CheckMyPermission;
import ptt.terminalsdk.tools.HttpUtil;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

import static cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState.IDLE;
import static cn.vsx.vc.activity.NewMainActivity.REQUEST_CODE_SCAN;

/**
 * Created by gt358 on 2017/8/16.
 */

public abstract class ChatBaseActivity extends BaseActivity
//    implements
//    NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback
{
    private static final int CODE_CAMERA_REQUEST = 0x11;
    /**
     * 打开相机
     */
    private static final int CODE_IMAGE_RESULT = 0;
    private static final int CODE_VIDEO_RESULT = 1;

    private static final int CAMERA_PERMISSIONS_REQUEST_CODE = 0x13;
    /**
     * 请求相机权限
     */
    private static final int STORAGE_PERMISSIONS_REQUEST_CODE = 0x14;
    /**
     * 请求存储读取权限
     */

    private static final int CODE_TRANSPON_REQUEST = 0x16;//转发

    protected static final int REQUEST_RECORD_CODE = 999;
    protected Logger logger = Logger.getLogger(getClass());

    protected List<TerminalMessage> allFailMessageList = new ArrayList<>();//当前会话所有发送失败消息集合
    protected List<TerminalMessage> historyFailMessageList = new ArrayList<>();//当前会话历史发送失败消息集合
    protected Map<Integer, TerminalMessage> unFinishMsgList = new HashMap<>();//
    TextView newsBarGroupName;
    SwipeRefreshLayout sflCallList;
    FixedRecyclerView groupCallList;
    FrameLayout fl_fragment_container;
    EditText groupCallNewsEt;
    FunctionHidePlus funcation;
    Button ptt;

    protected TemporaryAdapter temporaryAdapter;
    protected List<TerminalMessage> chatMessageList = new ArrayList<>();

    protected int userId;
    protected int userIdEncode;
    protected String userName;
    protected int speakingId;
    protected String speakingName;
    protected boolean isGroup;//组会话还是个人会话

    private long lastVersion;//最后一条发送成功的消息的version
    private TerminalMessage tempGetMessage;//获取消息时传给服务器查询该消息版本号之前的10条
    private int tempPage = 1;
    private boolean refreshing;
    private static final int WATCH_LIVE = 0;
    private static final int PAGE_COUNT = 10;//每次加载的消息数量
    private boolean isEnoughPageCount = false;//每次从本地取的数据的条数是否够10条
    private NFCBindingDialog nfcBindingDialog;//nfc弹窗
    private boolean isActivity;//是否是显示

//    private NfcAdapter mNfcAdapter;
//    private PendingIntent mPendingIntent;

    protected Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case WATCH_LIVE:
                    TerminalMessage terminalMessage = (TerminalMessage) msg.obj;
                    watchLive(terminalMessage);
                    break;
            }
        }
    };
    private long uniqueNo;

    /**
     * 设置状态栏透明
     **/
    protected void setSatusBarTransparent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }
    }

    /**
     * 设置statusbar颜色
     **/
    protected void setStatusBarColor() {
        StatusBarUtil.setStatusBarColor(this, R.color.backgroudblue);
//        SystemBarTintManager tintManager = new SystemBarTintManager(this);
//        tintManager.setStatusBarTintEnabled(true);
//        tintManager.setTintColor(ContextCompat.getColor(this, R.color.sweep_text_black));
    }

    public void initListener() {
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveNotifyDataMessageHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveSendDataMessageFailedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveDownloadFinishHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveUploadProgressHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveDownloadProgressHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupOrMemberNotExistHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveSendDataMessageSuccessHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(getHistoryMessageRecordHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receivePersonMessageNotifyDateHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveResponseRecallRecordHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mNotifyRecallRecordMessageHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(getWarningMessageDetailHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverSendFileCheckMessageHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverChatListItemClickHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverShowTransponPopupHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverShowForwardMoreHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverShowCopyPopupHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverShowWithDrawPopupHandler);
//        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverTransponHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverToFaceRecognitionHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverSelectChatListHandler);
        sflCallList.setOnRefreshListener(new OnRefreshListenerImplementationImpl());
        groupCallList.setOnTouchListener(mMessageTouchListener);
        setOnPTTVolumeBtnStatusChangedListener(new OnPTTVolumeBtnStatusChangedListenerImp());
        groupCallList.addOnLayoutChangeListener(myOnLayoutChangeListener);
    }

    @Override
    public void initData() {
        userId = getIntent().getIntExtra("userId", 0);
        userName = getIntent().getStringExtra("userName");
        isGroup = getIntent().getBooleanExtra("isGroup", false);
        uniqueNo = getIntent().getLongExtra("uniqueNo", 0L);
        speakingId = getIntent().getIntExtra("speakingId", 0);
        speakingName = getIntent().getStringExtra("speakingName");
        newsBarGroupName.setText(HandleIdUtil.handleName(userName));
//        initNFC();

        setToIds();
        //消息从文件服务获取
//        if(isGroup){
        refreshing = true;
        getHistoryMessageRecord(PAGE_COUNT);
//        }else {
//            List<TerminalMessage> groupMessageRecord = MyTerminalFactory.getSDK().getTerminalMessageManager().getGroupMessageRecord(
//                    isGroup ? MessageCategory.MESSAGE_TO_GROUP.getCode() : MessageCategory.MESSAGE_TO_PERSONAGE.getCode(), userId,
//                    0, TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0));
//
//            Collections.sort(groupMessageRecord);
//            chatMessageList.addAll(groupMessageRecord);
//            if (chatMessageList.size() > 0) {
//                lastVersion = chatMessageList.get(chatMessageList.size() - 1).messageVersion;
//                tempGetMessage = chatMessageList.get(0);
//            }
//            //如果本地没有或者本地的数据不足10条从网络获取
//            if (chatMessageList.size() == 0 || chatMessageList.size() < PAGE_COUNT) {
//                isEnoughPageCount = false;
//                getHistoryMessageRecord(PAGE_COUNT - chatMessageList.size());
//            }else{
//                isEnoughPageCount = true;
//            }
//        }

//        //先从本地数据库获取
//        List<TerminalMessage> groupMessageRecord = MyTerminalFactory.getSDK().getTerminalMessageManager().getGroupMessageRecord(
//                isGroup ? MessageCategory.MESSAGE_TO_GROUP.getCode() : MessageCategory.MESSAGE_TO_PERSONAGE.getCode(), userId,
//                0, TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0));
//        Collections.sort(groupMessageRecord);
//        chatMessageList.addAll(groupMessageRecord);
//        if (chatMessageList.size() > 0) {
//            lastVersion = chatMessageList.get(chatMessageList.size() - 1).messageVersion;
//            tempGetMessage = chatMessageList.get(0);
//        }
//        //如果本地没有或者本地的数据不足10条从网络获取
//        if (chatMessageList.size() == 0 || chatMessageList.size() < PAGE_COUNT) {
//            isEnoughPageCount = false;
//            getHistoryMessageRecord(PAGE_COUNT - chatMessageList.size());
//        }else{
//            isEnoughPageCount = true;
//        }

        LinkedTreeMap<String, List<TerminalMessage>> sendFailMap = MyTerminalFactory.getSDK().getTerminalMessageListMap(Params.MESSAGE_SEND_FAIL, new LinkedTreeMap<String, List<TerminalMessage>>());

        if (sendFailMap != null) {
            List<TerminalMessage> list = sendFailMap.get(userId + "");
            if (list != null) {
                historyFailMessageList.addAll(list);
                allFailMessageList.addAll(list);
                intersetMessageToList(chatMessageList, historyFailMessageList);
            }
        }

        /**获取到未发送完成的消息列表**/
        unFinishMsgList = MyTerminalFactory.getSDK().getTerminalMessageManager().getSendDataMessageMap();
        if (unFinishMsgList != null && unFinishMsgList.size() > 0) {
            Iterator<Integer> keys = unFinishMsgList.keySet().iterator();
            while (keys.hasNext()) {
                int tokenId = keys.next();
                TerminalMessage terminalMessage = unFinishMsgList.get(tokenId);
                if (terminalMessage.messageToId == userIdEncode) {
                    if (terminalMessage.resultCode != 0) {
                        terminalMessage.messageBody.put(JsonParam.DOWN_VERSION_FOR_FAIL, lastVersion);
                    } else {
                        terminalMessage.messageBody.put(JsonParam.SEND_STATE, MessageSendStateEnum.SEND_PRE);
                    }
                    chatMessageList.add(terminalMessage);
                }
            }
        }
        if (unFinishMsgList == null) {
            unFinishMsgList = new HashMap<>();
        }
        temporaryAdapter = new TemporaryAdapter(chatMessageList, this);
        temporaryAdapter.setIsGroup(isGroup);
        temporaryAdapter.setFragment_contener(fl_fragment_container);
        groupCallList.setAdapter(temporaryAdapter);
        groupCallList.setItemAnimator(null);
        if (chatMessageList.size() > 0) {
            setListSelection(chatMessageList.size() - 1);
            temporaryAdapter.notifyDataSetChanged();
        }
    }

//    private void initNFC() {
//        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
//        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,getClass()), 0);
//        if(mNfcAdapter!=null){
//          // 指定要传输文本的回调
//          mNfcAdapter.setNdefPushMessageCallback(this, this);
//          // 传输完成调用
//          mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
//        }
//    }

    @Override
    protected void onStop() {
        super.onStop();
        stopRecord();
        if(funcation!=null){
            funcation.hideKeyboard(true);
            funcation.hideKeyboardAndBottom();
        }
    }

    /**
     * 停止播放组呼录音
     */
    public void stopRecord() {
        if (MyApplication.instance.isPlayVoice) {
            MyTerminalFactory.getSDK().getTerminalMessageManager().stopMultimediaMessage();
            MediaManager.release();
            MyApplication.instance.isPlayVoice = false;
            isSameItem = true;
            temporaryAdapter.refreshPersonContactsAdapter(mposition, chatMessageList, MyApplication.instance.isPlayVoice, isSameItem);
        }
    }


    /**
     * 将数据插入列表中
     **/
    protected void intersetMessageToList(List<TerminalMessage> messageList, List<TerminalMessage> intersetMessageList) {
        int count = intersetMessageList.size();
        if (!messageList.isEmpty()) {
            long firstVersion = messageList.get(0).messageVersion;
            int failStart = -1;
            List<TerminalMessage> interFailMessageList = new ArrayList<>();
            /**  获取错误列表从哪个位置开始轮询 **/
            for (int i = 0; i < count; i++) {
                TerminalMessage failTerminalMessage = intersetMessageList.get(i);
                long failVersion = failTerminalMessage.messageBody.getLong(JsonParam.DOWN_VERSION_FOR_FAIL);
                if (failStart == -1
                        && firstVersion <= failVersion) {
                    failStart = i;
                }
                if (failStart != -1) {
                    interFailMessageList.add(failTerminalMessage);
                }
            }

            if (interFailMessageList.size() > 0) {
                for (int i = 0; i < interFailMessageList.size(); i++) {
                    TerminalMessage failTerminalMessage = interFailMessageList.get(i);
                    long failVersion = failTerminalMessage.messageBody.getLong(JsonParam.DOWN_VERSION_FOR_FAIL);
                    int interposition = -1;//失败消息插入消息列表的定位
                    for (int j = 0; j < messageList.size(); j++) {
                        TerminalMessage terminalMessage = messageList.get(j);
                        if (failVersion == terminalMessage.messageVersion) {
                            interposition = j + 1;
                        }
                        if (interposition != -1
                                && terminalMessage.resultCode != 0) {//判断后面是否连着失败消息
                            interposition = j + 1;
                        }
                    }
                    if (interposition != -1) {
                        messageList.add(interposition, failTerminalMessage);
                        intersetMessageList.remove(failTerminalMessage);
                    }
                }
            }
        } else {
            messageList.addAll(intersetMessageList);
            Collections.sort(messageList);
        }
    }

    public void doOtherDestroy() {

        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveNotifyDataMessageHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveSendDataMessageFailedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveDownloadFinishHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveUploadProgressHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveDownloadProgressHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveGetGPSLocationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupOrMemberNotExistHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveSendDataMessageSuccessHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(getHistoryMessageRecordHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receivePersonMessageNotifyDateHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveResponseRecallRecordHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mNotifyRecallRecordMessageHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(getWarningMessageDetailHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverChatListItemClickHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverShowTransponPopupHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverShowForwardMoreHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverShowCopyPopupHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverShowWithDrawPopupHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverSendFileCheckMessageHandler);
//        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverTransponHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverToFaceRecognitionHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverSelectChatListHandler);

        LinkedTreeMap<String, List<TerminalMessage>> sendFailMap = MyTerminalFactory.getSDK().getTerminalMessageListMap(Params.MESSAGE_SEND_FAIL, new LinkedTreeMap<String, List<TerminalMessage>>());
        sendFailMap.put(userId + "", allFailMessageList);
        MyTerminalFactory.getSDK().putTerminalMessageListMap(Params.MESSAGE_SEND_FAIL, sendFailMap);

        groupCallList.removeOnLayoutChangeListener(myOnLayoutChangeListener);
        handler.removeCallbacksAndMessages(null);
        Glide.with(MyApplication.instance.getApplicationContext()).onDestroy();
    }


    public void postVideo() {
    }

    public void requestVideo() {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        logger.info("onActivityResult-----" + "requestCode:" + requestCode + ",resultCode:" + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE_CAMERA_REQUEST) {
            switch (resultCode) {
                case CODE_IMAGE_RESULT://拍照完成回调
                    //两种方式 获取拍好的图片
                    sendPhotoFromCamera(data);
                    break;
                case CODE_VIDEO_RESULT://小视频完成回调
                    sendVideoFileOrPhoto(data);

            }
        } else if (requestCode == CODE_FNC_REQUEST) {
            checkNFC(userId,false);
        } else if (requestCode == CODE_TRANSPON_REQUEST) {
            if (resultCode == RESULT_OK) {
                //转发返回结果
                TransponSelectedBean bean = (TransponSelectedBean) data.getSerializableExtra(cn.vsx.vc.utils.Constants.TRANSPON_SELECTED_BEAN);
                if (bean != null && bean.getList() != null && !bean.getList().isEmpty()) {
                    int type = data.getIntExtra(cn.vsx.vc.utils.Constants.TRANSPON_TYPE, cn.vsx.vc.utils.Constants.TRANSPON_TYPE_ONE);
                    if (type == cn.vsx.vc.utils.Constants.TRANSPON_TYPE_ONE) {
                        //单个转发
                        temporaryAdapter.transponMessage(bean.getList(), pushMessageSendResultHandler);
                    } else if (type == cn.vsx.vc.utils.Constants.TRANSPON_TYPE_MORE) {
                        //合并转发
                        transponMessageMore(bean.getList());
                    }
                }
            }
        }else if(requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK){
            if (data != null) {
                String result = data.getStringExtra(com.yzq.zxinglibrary.common.Constant.CODED_CONTENT);
                logger.info("扫描二维码结果："+result);
                // TODO: 2019/4/10 给注册服务发送扫码结果
                analysisScanData(result,userId);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSIONS_REQUEST_CODE: {//调用系统相机申请拍照权限回调
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(new Intent(ChatBaseActivity.this, CameraActivity.class), CODE_CAMERA_REQUEST);
                } else {
                    ToastUtil.showToast(this, getString(R.string.text_need_camera_privileges));
                }
            }
            break;
        }
    }

    /**
     * 自动获取相机权限
     */
    private void autoObtainCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
//                ToastUtils.showShort(this, "您已经拒绝过一次");
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSIONS_REQUEST_CODE);
        } else {//有权限直接调用系统相机拍照
//            PhotoUtils.openCamera(this, CODE_CAMERA_REQUEST);
            startActivityForResult(new Intent(ChatBaseActivity.this, CameraActivity.class), CODE_CAMERA_REQUEST);
        }
    }

    @Override
    public void onBackPressed() {

        //返回时删除文件
        File dir = new File(TerminalFactory.getSDK().getLogDirectory() + File.separator + "log");
        if (dir.exists() && dir.isDirectory()) {
            String[] files = dir.list();
            for (String file : files) {
                File deleteFile = new File(dir.getPath() + File.separator + file);
                if (deleteFile.exists()) {
                    deleteFile.delete();
                }
            }
            dir.delete();
        }
        if (null != backListener) {
            backListener.onBack();
        } else {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                //取消合并转发
                if (getMergeTransmitState()) {
                    clearMergeTransmitState();
                } else {
                    //如果消息不为空则保存信息
                    saveUnsendMessage();
                    super.onBackPressed();
                }
            } else if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
                setViewVisibility(fl_fragment_container, View.GONE);
                getSupportFragmentManager().popBackStack();
            } else {
                super.onBackPressed();
            }
        }
    }

    protected void saveUnsendMessage() {
        String message = groupCallNewsEt.getText().toString().trim();
        if (!Util.isEmpty(message)) {
            getSharedPreferences("unsendMessage", MODE_PRIVATE).edit().putString(String.valueOf(userId), message).apply();
        } else {
            if (!TextUtils.isEmpty(getSharedPreferences("unsendMessage", MODE_PRIVATE).getString(String.valueOf(userId), ""))) {
                getSharedPreferences("unsendMessage", MODE_PRIVATE).edit().remove(String.valueOf(userId)).apply();
            }
        }
    }

    /**
     * 发送消息文件的回调
     */
    private ReceiverSendFileHandler mReceiverSendFileHandler = new ReceiverSendFileHandler() {
        @Override
        public void handler(int type) {
            setViewVisibility(fl_fragment_container, View.GONE);
            scrollMyListViewToBottom();
            switch (type) {
                case ReceiverSendFileHandler.TEXT:
                    //点击发送触发这个handler的回调
                    sendText();
                    break;
                case ReceiverSendFileHandler.PHOTO_ALBUM:
                    getSupportFragmentManager().popBackStack();
                    sendPhoto();
                    break;
                case ReceiverSendFileHandler.FILE:
                    setViewVisibility(fl_fragment_container, View.GONE);
                    getSupportFragmentManager().popBackStack();
                    sendFile();
                    break;
                case ReceiverSendFileHandler.LOCATION:
                    MyTerminalFactory.getSDK().registReceiveHandler(mReceiveGetGPSLocationHandler);
                    MyTerminalFactory.getSDK().getLocationManager().requestLocationByChat();
                    sendLocation(0, 0, TEMP_TOKEN_ID, false, false);
                    break;
                case ReceiverSendFileHandler.VOICE:
                    sendRecord();

            }
        }
    };

    private void scrollMyListViewToBottom() {
        groupCallList.postDelayed(() -> groupCallList.scrollToPosition(temporaryAdapter.getItemCount() - 1), 10);
    }

    /**
     * 发送录音
     */
    protected void sendRecord() {
        Set<Map.Entry<String, Record>> entries = Constant.records.entrySet();
        if (entries.size() == 0) {
            return;
        } else {
            for (Map.Entry<String, Record> entry : entries) {
                Record record = entry.getValue();
                logger.debug(record.toString());
                addRecordToList(record.getPath(), record.getId(), record.getSize(), record.getStartTime(), record.getEndTime());
            }
            Constant.records.clear();
            setListSelection(chatMessageList.size() - 1);
            temporaryAdapter.notifyDataSetChanged();
            temporaryAdapter.uploadFileDelay();
        }
    }

    /**
     * 发送图片
     */
    private void sendPhoto() {
        Set<Map.Entry<String, Image>> entries = Constant.images.entrySet();
        if (entries.size() == 0) {
            return;
        } else {
            for (Map.Entry<String, Image> entry : entries) {
                Image image = entry.getValue();
                addPhotoToList(image.getPath(), image.getName(), image.getSize(), 0);
            }
            Constant.images.clear();
            setListSelection(chatMessageList.size() - 1);
            temporaryAdapter.notifyDataSetChanged();
            temporaryAdapter.uploadFileDelay();
        }
    }

    /**
     * 发送照相之后的图片
     */
    private void sendPhotoFromCamera(Intent data) {
        int photoNum = MyTerminalFactory.getSDK().getParam(Params.PHOTO_NUM, 0);
        File file = new File(MyTerminalFactory.getSDK().getPhotoRecordDirectory(), "image" + photoNum + ".jpg");
        logger.debug(file.getPath() + "," + file.getName() + FileUtil.getFileSize(file));
        if (file.exists()) {
            addPhotoToList(file.getPath(), file.getName(), FileUtil.getFileSize(file) + "", 0);
            temporaryAdapter.notifyDataSetChanged();
            setListSelection(chatMessageList.size() - 1);
            temporaryAdapter.uploadFileDelay();
        }
    }

    /**
     * 发送小视频文件
     */
    private void sendVideoFileOrPhoto(Intent data) {
        if (data == null || data.getExtras() == null) {
            return;
        }
        String url = data.getExtras().getString("url");
        if (TextUtils.isEmpty(url)) {
            return;
        }
        File file = new File(url);
        long videoTime = 0;
        try {
            android.media.MediaPlayer mediaPlayer = new android.media.MediaPlayer();
            mediaPlayer.setDataSource(file.getPath());
            mediaPlayer.prepare();
            //获得了视频的时长（以毫秒为单位）
            videoTime = mediaPlayer.getDuration();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (file.exists()) {
            addVideoToList(file.getPath(), file.getName(), FileUtil.getFileSize(file), videoTime);
            temporaryAdapter.notifyDataSetChanged();
            setListSelection(chatMessageList.size() - 1);
            temporaryAdapter.uploadFileDelay();
        }
    }

    private void addVideoToList(String filePath, String fileName, long fileSize, long videoTime) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JsonParam.FILE_NAME, fileName);
        jsonObject.put(JsonParam.FILE_SIZE, fileSize);
        jsonObject.put(JsonParam.SEND_STATE, MessageSendStateEnum.SEND_PRE);
        jsonObject.put(JsonParam.TOKEN_ID, MyTerminalFactory.getSDK().getMessageSeq());
        jsonObject.put(JsonParam.DOWN_VERSION_FOR_FAIL, lastVersion);
        jsonObject.put(JsonParam.VIDEO_TIME, videoTime);
        Bitmap bitmap = BitmapUtil.createVideoThumbnail(filePath);
        String picture = HttpUtil.saveFileByBitmap(MyTerminalFactory.getSDK().getPhotoRecordDirectory(), System.currentTimeMillis() + ".jpg", bitmap);
        jsonObject.put(JsonParam.PICTURE_THUMB_URL, picture);
        TerminalMessage mTerminalMessage = new TerminalMessage();
        mTerminalMessage.messageType = MessageType.VIDEO_CLIPS.getCode();
        mTerminalMessage.sendTime = System.currentTimeMillis();
        mTerminalMessage.messagePath = filePath;
        mTerminalMessage.messageFromId = MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
        mTerminalMessage.messageFromName = MyTerminalFactory.getSDK().getParam(Params.MEMBER_NAME, "");
        mTerminalMessage.messageToId = userId;
        mTerminalMessage.messageToName = userName;
        mTerminalMessage.messageBody = jsonObject;

        chatMessageList.add(mTerminalMessage);
        temporaryAdapter.uploadMessages.add(mTerminalMessage);
        unFinishMsgList.put(jsonObject.getIntValue(JsonParam.TOKEN_ID), mTerminalMessage);
        temporaryAdapter.progressPercentMap.put(jsonObject.getIntValue(JsonParam.TOKEN_ID), 0);
        if (temporaryAdapter != null) {
            temporaryAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 将photo的terminalmessage添加到列表中
     *
     * @param url  url
     * @param name 名字
     * @param size 大小
     */
    private void addPhotoToList(String url, String name, String size, int pos) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JsonParam.SEND_STATE, MessageSendStateEnum.SEND_PRE);
        jsonObject.put(JsonParam.PICTURE_NAME, name);
        jsonObject.put(JsonParam.PICTURE_SIZE, size);
        jsonObject.put(JsonParam.TOKEN_ID, MyTerminalFactory.getSDK().getMessageSeq());
        jsonObject.put(JsonParam.DOWN_VERSION_FOR_FAIL, lastVersion);
        TerminalMessage mTerminalMessage = new TerminalMessage();
        mTerminalMessage.messageFromId = MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
        mTerminalMessage.messageFromName = MyTerminalFactory.getSDK().getParam(Params.MEMBER_NAME, "");
        mTerminalMessage.messageToId = userId;
        mTerminalMessage.messageToName = userName;
        mTerminalMessage.messagePath = url;
        mTerminalMessage.sendTime = System.currentTimeMillis();
        mTerminalMessage.messageType = MessageType.PICTURE.getCode();

        mTerminalMessage.messageBody = jsonObject;

        chatMessageList.add(mTerminalMessage);

        temporaryAdapter.uploadMessages.add(mTerminalMessage);
        unFinishMsgList.put(jsonObject.getIntValue(JsonParam.TOKEN_ID), mTerminalMessage);
        temporaryAdapter.progressPercentMap.put(jsonObject.getIntValue(JsonParam.TOKEN_ID), 0);
        if (temporaryAdapter != null) {
            temporaryAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 发送文件
     */
    private void sendFile() {
        Set<Map.Entry<String, FileInfo>> entries = Constant.files.entrySet();
        if (entries.size() == 0) {
            return;
        } else {
            for (Map.Entry<String, FileInfo> entry : entries) {
                FileInfo fileInfo = entry.getValue();
                if (fileInfo.isPhoto) {
                    addPhotoToList(fileInfo.getFilePath(), fileInfo.getFileName(), fileInfo.getFileSize() + "", 0);
                } else {
                    //如果是日志，因为上传时还在写，所以上传长度和实际长度不一致，导致报错，上传失败
                    if (fileInfo.getFilePath().equals(TerminalFactory.getSDK().getLogDirectory() + "log.txt")) {
                        try {
                            File src = new File(fileInfo.getFilePath());
                            File dir = new File(src.getParent() + File.separator + "log");
                            if (!dir.exists()) {
                                dir.mkdirs();
                            }
                            File dst = new File(dir.getPath(), fileInfo.getFileName());
                            if (dst.exists()) {
                                dst.delete();
                                dst = new File(dir.getPath(), fileInfo.getFileName());
                            }
                            if (!dst.exists()) {
                                dst.createNewFile();
                            }
                            copy(src, dst);
                            addFileToList(dst.getPath(), dst.getName(), dst.length());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        addFileToList(fileInfo.getFilePath(), fileInfo.getFileName(), fileInfo.getFileSize());
                    }
                }
            }
            Constant.files.clear();
            temporaryAdapter.notifyDataSetChanged();
            setListSelection(chatMessageList.size() - 1);
            temporaryAdapter.uploadFileDelay();
        }
    }

    /**
     * copy a file
     */
    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    //将file的terminalmessage添加到列表中
    private void addFileToList(String filePath, String fileName, long fileSize) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JsonParam.FILE_NAME, fileName);
        jsonObject.put(JsonParam.FILE_SIZE, fileSize);
        jsonObject.put(JsonParam.SEND_STATE, MessageSendStateEnum.SEND_PRE);
        jsonObject.put(JsonParam.TOKEN_ID, MyTerminalFactory.getSDK().getMessageSeq());
        jsonObject.put(JsonParam.DOWN_VERSION_FOR_FAIL, lastVersion);
        TerminalMessage mTerminalMessage = new TerminalMessage();
        mTerminalMessage.messageType = MessageType.FILE.getCode();
        mTerminalMessage.sendTime = System.currentTimeMillis();
        mTerminalMessage.messagePath = filePath;
        mTerminalMessage.messageFromId = MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
        mTerminalMessage.messageFromName = MyTerminalFactory.getSDK().getParam(Params.MEMBER_NAME, "");
        mTerminalMessage.messageToId = userId;
        mTerminalMessage.messageToName = userName;
        mTerminalMessage.messageBody = jsonObject;

        chatMessageList.add(mTerminalMessage);
        temporaryAdapter.uploadMessages.add(mTerminalMessage);
        unFinishMsgList.put(jsonObject.getIntValue(JsonParam.TOKEN_ID), mTerminalMessage);
        temporaryAdapter.progressPercentMap.put(jsonObject.getIntValue(JsonParam.TOKEN_ID), 0);
        if (temporaryAdapter != null) {
            temporaryAdapter.notifyDataSetChanged();
        }
    }

    //将RecordFile的terminalmessage添加到列表中
    private void addRecordToList(String filePath, String fileName, long fileSize, long startTime, long endTime) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JsonParam.FILE_NAME, fileName);
        jsonObject.put(JsonParam.FILE_SIZE, fileSize);
        jsonObject.put(JsonParam.SEND_STATE, MessageSendStateEnum.SEND_PRE);
        jsonObject.put(JsonParam.TOKEN_ID, MyTerminalFactory.getSDK().getMessageSeq());
        jsonObject.put(JsonParam.DOWN_VERSION_FOR_FAIL, lastVersion);
        jsonObject.put(JsonParam.START_TIME, startTime);
        jsonObject.put(JsonParam.END_TIME, endTime);
        TerminalMessage mTerminalMessage = new TerminalMessage();
        mTerminalMessage.messageType = MessageType.AUDIO.getCode();
        mTerminalMessage.sendTime = System.currentTimeMillis();
        mTerminalMessage.messagePath = filePath;
        mTerminalMessage.messageFromId = MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
        mTerminalMessage.messageFromName = MyTerminalFactory.getSDK().getParam(Params.MEMBER_NAME, "");
        mTerminalMessage.messageToId = userId;
        mTerminalMessage.messageToName = userName;
        mTerminalMessage.messageBody = jsonObject;

        chatMessageList.add(mTerminalMessage);
        temporaryAdapter.uploadMessages.add(mTerminalMessage);
        unFinishMsgList.put(jsonObject.getIntValue(JsonParam.TOKEN_ID), mTerminalMessage);
        temporaryAdapter.progressPercentMap.put(jsonObject.getIntValue(JsonParam.TOKEN_ID), 0);
        if (temporaryAdapter != null) {
            temporaryAdapter.notifyDataSetChanged();
        }
    }


    /**
     * 发送文本
     */
    private void sendText() {
        String msg = groupCallNewsEt.getText().toString();
        if (TextUtils.isEmpty(msg)) {
            return;
        }
        if (msg.length() <= Constants.MAX_SHORT_TEXT) {
            sendShortText(msg);
        } else {
            sendLongText(msg);
        }
        setText(groupCallNewsEt, "");
    }

    /**
     * 发送短文本
     */
    private void sendShortText(String msg) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JsonParam.SEND_STATE, MessageSendStateEnum.SEND_PRE);
        jsonObject.put(JsonParam.CONTENT, msg);
        jsonObject.put(JsonParam.TOKEN_ID, MyTerminalFactory.getSDK().getMessageSeq());
        jsonObject.put(JsonParam.DOWN_VERSION_FOR_FAIL, lastVersion);
        TerminalMessage mTerminalMessage = new TerminalMessage();
        mTerminalMessage.messageFromId = MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
        mTerminalMessage.messageFromName = MyTerminalFactory.getSDK().getParam(Params.MEMBER_NAME, "");
        mTerminalMessage.messageToId = userId;
        mTerminalMessage.messageToName = userName;
        mTerminalMessage.sendTime = System.currentTimeMillis();
        mTerminalMessage.messageType = MessageType.SHORT_TEXT.getCode();
        mTerminalMessage.messageBody = jsonObject;

        chatMessageList.add(mTerminalMessage);
        setListSelection(chatMessageList.size() - 1);
        if (temporaryAdapter != null) {
            temporaryAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 发送长文本
     */
    private void sendLongText(String msg) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JsonParam.SEND_STATE, MessageSendStateEnum.SEND_PRE);
        jsonObject.put(JsonParam.TOKEN_ID, MyTerminalFactory.getSDK().getMessageSeq());
        File file = FileUtil.saveString2File(msg, jsonObject.getIntValue(JsonParam.TOKEN_ID));
        jsonObject.put(JsonParam.DOWN_VERSION_FOR_FAIL, lastVersion);
        TerminalMessage mTerminalMessage = new TerminalMessage();
        mTerminalMessage.messageFromId = MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
        mTerminalMessage.messageFromName = MyTerminalFactory.getSDK().getParam(Params.MEMBER_NAME, "");
        mTerminalMessage.messageToId = userId;
        mTerminalMessage.messageToName = userName;
        mTerminalMessage.messageBody = jsonObject;
        mTerminalMessage.sendTime = System.currentTimeMillis();
        mTerminalMessage.messageType = MessageType.LONG_TEXT.getCode();
        mTerminalMessage.messagePath = file.getPath();

        chatMessageList.add(mTerminalMessage);
        setListSelection(chatMessageList.size() - 1);
        if (temporaryAdapter != null) {
            temporaryAdapter.notifyDataSetChanged();
        }
    }

    private final int TEMP_TOKEN_ID = -1;

    /**
     * 发送位置
     */
    private void sendLocation(double longitude, double latitude, int tokenId, boolean realSend, boolean getLocationFail) {
        logger.error("sendLocation:" + "longitude：" + longitude + "/latitude:" + latitude);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JsonParam.SEND_STATE, MessageSendStateEnum.SEND_PRE);
        if (longitude != 0 && latitude != 0) {
            //            List<Double> locations = CoordTransformUtils.bd2wgs(latitude, longitude);
            //            if(locations.size()>1){
            jsonObject.put(JsonParam.LONGITUDE, longitude);
            jsonObject.put(JsonParam.LATITUDE, latitude);
            //            }
        }
        jsonObject.put(JsonParam.TOKEN_ID, tokenId);
        jsonObject.put(JsonParam.ACTUAL_SEND, realSend);
        jsonObject.put(JsonParam.GET_LOCATION_FAIL, getLocationFail);
        jsonObject.put(JsonParam.DOWN_VERSION_FOR_FAIL, lastVersion);
        TerminalMessage mTerminalMessage = new TerminalMessage();
        mTerminalMessage.messageFromId = MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
        mTerminalMessage.messageFromName = MyTerminalFactory.getSDK().getParam(Params.MEMBER_NAME, "");
        mTerminalMessage.messageToId = userId;
        mTerminalMessage.messageToName = userName;
        mTerminalMessage.sendTime = System.currentTimeMillis();
        mTerminalMessage.messageType = MessageType.POSITION.getCode();
        mTerminalMessage.messageBody = jsonObject;
        Iterator<TerminalMessage> it = chatMessageList.iterator();
        while (it.hasNext()) {
            TerminalMessage next = it.next();
            if (next.messageBody.containsKey(JsonParam.TOKEN_ID) && next.messageBody.getIntValue(JsonParam.TOKEN_ID) == TEMP_TOKEN_ID) {
                //这里会删除所有TOKEN_ID为-1的临时位置(无效位置数据，用来显示发送中的UI效果)
                it.remove();
            }
        }
        chatMessageList.add(mTerminalMessage);

        if (getLocationFail) {
            boolean isContainFail = false;
            for (TerminalMessage terminalMessage1 : allFailMessageList) {
                if (terminalMessage1.messageBody.containsKey(JsonParam.TOKEN_ID) && mTerminalMessage.messageBody.containsKey(JsonParam.TOKEN_ID) &&
                        terminalMessage1.messageBody.getIntValue(JsonParam.TOKEN_ID) == mTerminalMessage.messageBody.getIntValue(JsonParam.TOKEN_ID)) {
                    isContainFail = true;
                    terminalMessage1.messageBody.put(JsonParam.SEND_STATE, MessageSendStateEnum.SEND_FAIL);
                    break;
                }
            }
            //添加到发送失败列表
            if (!isContainFail) {
                allFailMessageList.add(mTerminalMessage);
            }
        }
        setListSelection(chatMessageList.size() - 1);
        if (temporaryAdapter != null) {
            temporaryAdapter.notifyDataSetChanged();
        }
    }

    protected void setListSelection(int position) {
        groupCallList.scrollToPosition(position);
    }


    public void hideKey() {
        if(funcation!=null){
            funcation.hideKey();
        }
    }

    /**
     * 设置组呼消息是否未读
     **/
    private void setGroupMessageUnread(TerminalMessage terminalMessage) {
        int currentGroupId = MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);//当前组id
        List<Integer> scanGroups = MyTerminalFactory.getSDK().getConfigManager().loadScanGroup();//组扫描列表
        boolean groupScanTog = MyTerminalFactory.getSDK().getParam(Params.GROUP_SCAN, false);//组扫描开关
        int mainGroupId = MyTerminalFactory.getSDK().getParam(Params.MAIN_GROUP_ID, 0);//主组id
        boolean guardMainGroupTog = MyTerminalFactory.getSDK().getParam(Params.GUARD_MAIN_GROUP, false);//主组开关
        if (terminalMessage.isOffLineMessage) {
            terminalMessage.messageBody.put(JsonParam.UNREAD, true);
        } else {
            if (terminalMessage.messageToId == currentGroupId) {//是当前值
                terminalMessage.messageBody.put(JsonParam.UNREAD, false);
            } else {//不是当前值
                if (groupScanTog) {//组扫描开着
                    boolean isScanGroup = false;
                    for (Integer integer : scanGroups) {
                        if (integer == terminalMessage.messageToId) {//是扫描的组
                            isScanGroup = true;
                            break;
                        }
                    }
                    if (isScanGroup) {//在组扫描列表中
                        terminalMessage.messageBody.put(JsonParam.UNREAD, false);
                    } else {
                        terminalMessage.messageBody.put(JsonParam.UNREAD, true);
                    }
                } else {//组扫描关着，判断主组状态
                    if (guardMainGroupTog) {//主组开着
                        if (mainGroupId == terminalMessage.messageToId) {//是主组消息
                            terminalMessage.messageBody.put(JsonParam.UNREAD, false);
                        } else {//不是主组消息
                            terminalMessage.messageBody.put(JsonParam.UNREAD, true);
                        }
                    } else {//主组关着
                        terminalMessage.messageBody.put(JsonParam.UNREAD, true);
                    }
                }
            }
        }
        MyTerminalFactory.getSDK().getTerminalMessageManager().updateTerminalMessage(terminalMessage);
    }

    private void setRecordMessageUnread(TerminalMessage terminalMessage) {
        if (terminalMessage.isOffLineMessage) {//如果是离线消息 则未读
            terminalMessage.messageBody.put(JsonParam.UNREAD, true);
            logger.debug("离线录音消息未读");
        }
        MyTerminalFactory.getSDK().getTerminalMessageManager().updateTerminalMessage(terminalMessage);
    }

    /**
     * 发送信令服务器失败
     **/
    private void sendMessageFail(final TerminalMessage terminalMessage, final int resultCode) {
        handler.post(() -> {
            terminalMessage.messageBody.put(JsonParam.SEND_STATE, MessageSendStateEnum.SEND_FAIL);
//            terminalMessage.messageBody.put(JsonParam.SEND_FAILED, true);
            logger.info("发送信令服务器失败" + terminalMessage);
            boolean isContainFail = false;
            for (TerminalMessage terminalMessage1 : allFailMessageList) {
                if (terminalMessage1.messageBody.containsKey(JsonParam.TOKEN_ID) && terminalMessage.messageBody.containsKey(JsonParam.TOKEN_ID) &&
                        terminalMessage1.messageBody.getIntValue(JsonParam.TOKEN_ID) == terminalMessage.messageBody.getIntValue(JsonParam.TOKEN_ID)) {
                    isContainFail = true;
                    terminalMessage1.messageBody.put(JsonParam.SEND_STATE, MessageSendStateEnum.SEND_FAIL);
                    break;
                }
            }
            if (!isContainFail) {
                allFailMessageList.add(terminalMessage);
            }

            Iterator<TerminalMessage> it = chatMessageList.iterator();
            while (it.hasNext()) {
                TerminalMessage next = it.next();
                if (next.messageBody.containsKey(JsonParam.TOKEN_ID) && terminalMessage.messageBody.containsKey(JsonParam.TOKEN_ID) &&
                        next.messageBody.getIntValue(JsonParam.TOKEN_ID) == terminalMessage.messageBody.getIntValue(JsonParam.TOKEN_ID)) {
                    it.remove();
                }
            }
            terminalMessage.messageBody.put(JsonParam.SEND_STATE, MessageSendStateEnum.SEND_FAIL);
            terminalMessage.resultCode = resultCode;
            chatMessageList.add(terminalMessage);
            if (temporaryAdapter != null) {
                temporaryAdapter.notifyDataSetChanged();
            }
        });
    }


    private boolean isContainMessage(TerminalMessage terminalMessage) {
        for (TerminalMessage mTerminalMessage : chatMessageList) {
            if (terminalMessage.messageVersion == mTerminalMessage.messageVersion)
                return true;
        }
        return false;
    }

    /**
     * 将列表中的terminalMessage替换成新的terminalMessage
     **/
    private void replaceMessage(TerminalMessage newTerminalMessage) {
        boolean has = false;
        Iterator<TerminalMessage> it = chatMessageList.iterator();
        while (it.hasNext()) {
            TerminalMessage next = it.next();
            if (next.messageBody.containsKey(JsonParam.TOKEN_ID) && newTerminalMessage.messageBody.containsKey(JsonParam.TOKEN_ID) &&
                    next.messageBody.getIntValue(JsonParam.TOKEN_ID) == newTerminalMessage.messageBody.getIntValue(JsonParam.TOKEN_ID)) {
                it.remove();
                has = true;
                break;
            }
        }
        if (has) {
            chatMessageList.add(newTerminalMessage);
        }
        Collections.sort(chatMessageList);
        if (temporaryAdapter != null) {
            temporaryAdapter.notifyDataSetChanged();
        }
        if (!has) {
            setListSelection(chatMessageList.size() - 1);
            lastVersion = newTerminalMessage.messageVersion;
        }
    }

    /**
     * 根据tokenId从消息集合中获取对应的消息
     **/
    private TerminalMessage getTerminalMessageByTokenId(int tokenId) {
        for (int i = chatMessageList.size() - 1; i >= 0; i--) {
            TerminalMessage terminalMessage1 = chatMessageList.get(i);
            if (terminalMessage1.messageBody.containsKey(JsonParam.TOKEN_ID)
                    && terminalMessage1.messageBody.getIntValue(JsonParam.TOKEN_ID) == tokenId) {
                return terminalMessage1;
            }
        }
        return null;
    }

    /***  根据TokenId从消息集合中获取position **/
    private int getPosByTokenId(int tokenId, List<TerminalMessage> messageList) {
        for (int i = messageList.size() - 1; i >= 0; i--) {
            TerminalMessage terminalMessage1 = messageList.get(i);
            if (terminalMessage1.messageBody.containsKey(JsonParam.TOKEN_ID)
                    && terminalMessage1.messageBody.getIntValue(JsonParam.TOKEN_ID) == tokenId) {
                return i;
            }
        }
        return -1;
    }

    /***  对toid进行编码 ***/
    private void setToIds() {
        if (isGroup) {
            userIdEncode = NoCodec.encodeGroupNo(userId);
        } else {
            userIdEncode = NoCodec.encodeMemberNo(userId);
        }
    }

    /***  获取ViewPos **/
    private int getViewPos(int positionForList) {
        int viewPos = -1;
        RecyclerView.LayoutManager layoutManager = groupCallList.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
            int firstVisiablePos = linearManager.findFirstVisibleItemPosition();
            int lastVisiablePos = linearManager.findLastVisibleItemPosition();
            if (positionForList >= firstVisiablePos && positionForList <= lastVisiablePos) {
                viewPos = positionForList - firstVisiablePos;
            }
        }

        return viewPos;
    }

    /**
     * 获取原生GPS定位信息
     **/
    private ReceiveGetGPSLocationHandler mReceiveGetGPSLocationHandler = new ReceiveGetGPSLocationHandler() {
        @Override
        public void handler(final double longitude, final double latitude) {
            logger.error("ReceiveGetGPSLocationHandler-------" + " " + longitude + "/" + latitude);
            handler.post(() -> {
//                sendLocation(30.495792, 114.433282, MyTerminalFactory.getSDK().getMessageSeq(), true, false);
                if (longitude != 0.0 && latitude != 0.0) {//获取位置成功
                    sendLocation(longitude, latitude, MyTerminalFactory.getSDK().getMessageSeq(), true, false);
                } else {//获取位置失败
                    sendLocation(longitude, latitude, TEMP_TOKEN_ID, false, true);
                    ToastUtil.showToast(ChatBaseActivity.this, getString(R.string.text_gps_positioning_failed));
                }
                MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveGetGPSLocationHandler);
            });
        }
    };

    /***  上传图片或者文件。。。成功或失败时更新列表UI和数据 ***/
    private void setFileAndPhotoMsg(int position) {
        TerminalMessage terminalMessage = chatMessageList.get(position);
        if (terminalMessage.messageType == MessageType.PICTURE.getCode()
                || terminalMessage.messageType == MessageType.FILE.getCode()) {
            temporaryAdapter.progressPercentMap.remove(terminalMessage.messageBody.getIntValue(JsonParam.TOKEN_ID));
            unFinishMsgList.remove(terminalMessage.messageBody.getIntValue(JsonParam.TOKEN_ID));
            int viewPos = getViewPos(position);
            if (viewPos != -1) {
                ProgressBar progressBar_pre_upload = (ProgressBar) groupCallList.getChildAt(viewPos).findViewById(R.id.progress_bar);
                TextView tv_progress_pre_upload = (TextView) groupCallList.getChildAt(viewPos).findViewById(R.id.tv_progress);
                setViewVisibility(progressBar_pre_upload, View.GONE);
                setViewVisibility(tv_progress_pre_upload, View.GONE);
            }

        }
    }

    protected void setText(TextView textView, String content) {
        if (textView != null) {
            textView.setText(content);
        }
    }

    protected void setViewVisibility(View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
        }
    }


    /**
     * 接收消息
     **/
    private ReceiveNotifyDataMessageHandler mReceiveNotifyDataMessageHandler = new ReceiveNotifyDataMessageHandler() {
        @Override
        public void handler(final TerminalMessage terminalMessage) {
            logger.info("接收到消息-----》" + terminalMessage.toString());
            handler.post(() -> {
                //判断列表中是否存在相同版本号的消息
                if (chatMessageList.contains(terminalMessage)) {
                    return;
                }
//                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_PERSONAGE.getCode()) {//个人消息
//                    newsBarGroupName.setText(HandleIdUtil.handleName(TerminalMessageUtil.getTitleName(terminalMessage)));
//                }
                //转发
                if (temporaryAdapter.transponMessage != null
                        && terminalMessage.messageFromId == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)
                        && terminalMessage.messageToId != userId
                        && terminalMessage.messageBody.containsKey(JsonParam.TOKEN_ID)
                        && terminalMessage.messageBody.getIntValue(JsonParam.TOKEN_ID) == temporaryAdapter.transponMessage.messageBody.getIntValue(JsonParam.TOKEN_ID)) {
                    temporaryAdapter.transponMessage = null;
                    if (terminalMessage.resultCode == 0)
                        ToastUtil.showToast(ChatBaseActivity.this, getString(R.string.text_forward_success));
                    else
                        ToastUtil.showToast(ChatBaseActivity.this, getString(R.string.text_forward_fail));
                    return;
                }

                if (isGroup) {//组会话界面
                    if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_PERSONAGE.getCode())//个人消息屏蔽
                        return;
                    if (terminalMessage.messageToId != userId)//其它组的屏蔽
                        return;
                } else {//个人会话界面
                    if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode())//组消息屏蔽
                        return;
                    if (terminalMessage.messageFromId == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)) {//自己发的
                        if (terminalMessage.messageToId != userId)
                            return;
                    } else {//接收的
                        if (terminalMessage.messageFromId != userId)//其它人的屏蔽
                            return;
                    }
                    /**  图像推送消息，去图像推送助手 **/
                    if (terminalMessage.messageType == MessageType.VIDEO_LIVE.getCode()) {
                        if (terminalMessage.messageFromId == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)
                                && terminalMessage.messageBody.containsKey(JsonParam.REMARK)
                                && (terminalMessage.messageBody.getIntValue(JsonParam.REMARK) == Remark.ACTIVE_VIDEO_LIVE)
                                && terminalMessage.resultCode == 0) {
                            //自己上报的消息，不显示在聊天界面
                            return;
                        } else {
                            Iterator<TerminalMessage> iterator = chatMessageList.iterator();
                            while (iterator.hasNext()) {
                                TerminalMessage next = iterator.next();
                                //删除之前的上报消息
                                if (null == next.messageBody ||
                                        TextUtils.isEmpty(next.messageBody.getString(JsonParam.CALLID)) ||
                                        null == terminalMessage.messageBody ||
                                        TextUtils.isEmpty(terminalMessage.messageBody.getString(JsonParam.CALLID))) {
                                    continue;
                                }
                                if (next.messageBody.getString(JsonParam.CALLID).equals(terminalMessage.messageBody.getString(JsonParam.CALLID))) {
                                    iterator.remove();
                                }
                            }
                        }
                    }
                }

                //发送失败的消息重新发送，成功后改变状态
                if (/*terminalMessage.messageFromId == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)
                        && terminalMessage.messageType != MessageType.GROUP_CALL.getCode()
                        && terminalMessage.messageType != MessageType.PRIVATE_CALL.getCode()
                        && terminalMessage.messageType != MessageType.VIDEO_LIVE.getCode()*/true) {
                    boolean isSendFail = false;
                    //如果是自己发送的消息，删除
                    if (!DataUtil.isReceiver(terminalMessage)) {
                        Iterator<TerminalMessage> it = chatMessageList.iterator();
                        while (it.hasNext()) {
                            TerminalMessage next = it.next();
                            //是自己发送的消息，并且是没有消息Id的
                            if (!DataUtil.isReceiver(next) && next.messageId == 0 && next.messageBody.containsKey(JsonParam.TOKEN_ID) && terminalMessage.messageBody.containsKey(JsonParam.TOKEN_ID) &&
                                    next.messageBody.getIntValue(JsonParam.TOKEN_ID) == terminalMessage.messageBody.getIntValue(JsonParam.TOKEN_ID)) {
                                it.remove();
                                isSendFail = true;
                            }
                        }
                    }

                    if (terminalMessage.messageType == MessageType.HYPERLINK.getCode()) {//人脸识别
                        int code = terminalMessage.messageBody.getIntValue(JsonParam.CODE);
                        if (code != 0) {
                            if (!isContainMessage(terminalMessage)) {
                                chatMessageList.add(terminalMessage);
                                if (temporaryAdapter != null) {
                                    temporaryAdapter.notifyDataSetChanged();
                                }
                                lastVersion = terminalMessage.messageVersion;
                            }
                        }
                    } else {
                        //NewsFragment已经保存过数据，此处不需要再保存
//                            if (terminalMessage.messageType==MessageType.GROUP_CALL.getCode()) {
//                                setGroupMessageUnread(terminalMessage);
//                            }else if(terminalMessage.messageType==MessageType.AUDIO.getCode()){
////                                setRecordMessageUnread(terminalMessage);
//                            }

                        chatMessageList.add(terminalMessage);
                        if (temporaryAdapter != null) {
                            temporaryAdapter.notifyDataSetChanged();
                        }
                        lastVersion = terminalMessage.messageVersion;
                    }

//                    Collections.sort(chatMessageList);
                    setListSelection(chatMessageList.size() - 1);


                    if (isSendFail) {
                        /**  发送失败的消息重新发送，发送成功将其从失败列表中删除  ***/
                        int failPos = getPosByTokenId(terminalMessage.messageBody.getIntValue(JsonParam.TOKEN_ID), allFailMessageList);
                        if (failPos != -1) {
                            allFailMessageList.remove(failPos);
                        }
                    }
                }

            });

        }
    };

    /**
     * 文件等下载完成的监听handler
     */
    private ReceiveDownloadFinishHandler mReceiveDownloadFinishHandler = new ReceiveDownloadFinishHandler() {
        @Override
        public void handler(final TerminalMessage terminalMessage, final boolean success) {
            handler.post(() -> {
                if (!isActivity) {
                    return;
                }

                if (!success) {
                    return;
                }

                if (isGroup) {//组消息
                    if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_PERSONAGE.getCode())//个人消息屏蔽
                        return;
                    if (terminalMessage.messageToId != userId)//其它组的屏蔽
                        return;
                } else {//个人消息
                    if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode())//个人消息屏蔽
                        return;
                    if (terminalMessage.messageFromId == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)) {//自己发的
                        if (terminalMessage.messageToId != userId)
                            return;
                    } else {//接收的
                        if (terminalMessage.messageFromId != userId)//其它人的屏蔽
                            return;
                    }
                }

                if (terminalMessage.messageType == MessageType.LONG_TEXT.getCode()
                        || terminalMessage.messageType == MessageType.HYPERLINK.getCode()) {
                    replaceMessage(terminalMessage);
                }
                if (terminalMessage.messageType == MessageType.PICTURE.getCode()) {
                    temporaryAdapter.isDownloadingPicture = false;
                    replaceMessage(terminalMessage);
                    //如果是原图下载完了就打开
                    if (terminalMessage.messageBody.containsKey(JsonParam.ISMICROPICTURE) &&
                            !terminalMessage.messageBody.getBooleanValue(JsonParam.ISMICROPICTURE)) {
                        temporaryAdapter.openPhotoAfterDownload(terminalMessage);
                    }
                }

                if (terminalMessage.messageType == MessageType.FILE.getCode()) {
                    temporaryAdapter.openFileAfterDownload(terminalMessage);
                    temporaryAdapter.isDownloading = false;
                    terminalMessage.messageBody.put(JsonParam.IS_DOWNLOADINF, false);
                    replaceMessage(terminalMessage);
                }
                if (terminalMessage.messageType == MessageType.AUDIO.getCode()) {
                    temporaryAdapter.isDownloading = false;
                    terminalMessage.messageBody.put(JsonParam.IS_DOWNLOADINF, false);
                    replaceMessage(terminalMessage);
                }
                if (terminalMessage.messageType == MessageType.VIDEO_CLIPS.getCode()) {
                    File file = new File(terminalMessage.messagePath);
                    temporaryAdapter.openVideo(terminalMessage, file);
                    temporaryAdapter.isDownloading = false;
                    terminalMessage.messageBody.put(JsonParam.IS_DOWNLOADINF, false);
                    replaceMessage(terminalMessage);
                }

            });
        }
    };

    private ReceiverToFaceRecognitionHandler mReceiverToFaceRecognitionHandler = new ReceiverToFaceRecognitionHandler() {
        @Override
        public void handler(String url, String name) {
            setViewVisibility(fl_fragment_container, View.VISIBLE);
            LocationFragment locationFragment = LocationFragment.getInstance(url, name, false);
            locationFragment.setFrameLayout(fl_fragment_container);
            getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fl_fragment_container, locationFragment).commit();
        }
    };

    /**
     * 会话界面列表条目点击事件
     **/

    private ReceiverChatListItemClickHandler mReceiverChatListItemClickHandler = new ReceiverChatListItemClickHandler() {
        @Override
        public void handler(final TerminalMessage terminalMessage, boolean isReceiver) {

            /**  进入定位界面 **/
            if (terminalMessage.messageType == MessageType.POSITION.getCode()) {
                if (terminalMessage.messageBody.containsKey(JsonParam.LONGITUDE) &&
                        terminalMessage.messageBody.containsKey(JsonParam.LATITUDE)) {
                    setViewVisibility(fl_fragment_container, View.VISIBLE);
                    double longitude = terminalMessage.messageBody.getDouble(JsonParam.LONGITUDE);
                    double altitude = terminalMessage.messageBody.getDouble(JsonParam.LATITUDE);
                    //http://192.168.1.96:7007/mapLocationl.html?lng=117.68&lat=39.456
                    String url = TerminalFactory.getSDK().getParam(Params.LOCATION_URL, "") + "?lng=" + longitude + "&lat=" + altitude;
                    if (TextUtils.isEmpty(TerminalFactory.getSDK().getParam(Params.LOCATION_URL, ""))) {
                        ToastUtil.showToast(ChatBaseActivity.this, getString(R.string.text_please_go_to_the_management_background_configuration_location_url));
                    } else {
                        LocationFragment locationFragment = LocationFragment.getInstance(url, "", true);
                        locationFragment.setFrameLayout(fl_fragment_container);
                        getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fl_fragment_container, locationFragment).commit();
                    }
                } else {
                    setViewVisibility(fl_fragment_container, View.VISIBLE);
                    String url = TerminalFactory.getSDK().getParam(Params.LOCATION_URL, "");
                    if (TextUtils.isEmpty(TerminalFactory.getSDK().getParam(Params.LOCATION_URL, ""))) {
                        ToastUtil.showToast(ChatBaseActivity.this, getString(R.string.text_please_go_to_the_management_background_configuration_location_url));
                    } else {
                        LocationFragment locationFragment = LocationFragment.getInstance(url, "", true);
                        locationFragment.setFrameLayout(fl_fragment_container);
                        getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fl_fragment_container, locationFragment).commit();
                    }
                }
            }

            /**  进入图片预览界面  **/
            if (terminalMessage.messageType == MessageType.PICTURE.getCode()) {
                setViewVisibility(fl_fragment_container, View.VISIBLE);
                FileInfo fileInfo = new FileInfo();
                fileInfo.setFilePath(terminalMessage.messagePath);
                List<FileInfo> images = new ArrayList<>();
                images.add(fileInfo);
                getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fl_fragment_container, new ImagePreviewFragment(images)).commit();
            }

            /**  上报图像  **/
            if (terminalMessage.messageType == MessageType.VIDEO_LIVE.getCode()) {
                //如果在视频上报、观看、个呼中不允许观看
                if (MyApplication.instance.getVideoLivePushingState() != VideoLivePushingState.IDLE) {
                    ToastUtils.showShort(R.string.text_pushing_cannot_pull);
                    return;
                } else if (MyApplication.instance.getVideoLivePlayingState() != VideoLivePlayingState.IDLE) {
                    ToastUtils.showShort(R.string.text_pulling_cannot_pull);
                    return;
                } else if (MyApplication.instance.getIndividualState() != IndividualCallState.IDLE) {
                    ToastUtils.showShort(R.string.text_calling_cannot_pull);
                    return;
                }


                //先请求看视频上报是否已经结束
                MyTerminalFactory.getSDK().getThreadPool().execute(() -> {
                    String serverIp = MyTerminalFactory.getSDK().getParam(Params.FILE_SERVER_IP, "");
                    int serverPort = MyTerminalFactory.getSDK().getParam(Params.FILE_SERVER_PORT, 0);
                    String url = "http://" + serverIp + ":" + serverPort + "/file/download/isLiving";
                    Map<String, String> paramsMap = new HashMap<>();
                    paramsMap.put("callId", terminalMessage.messageBody.getString(JsonParam.CALLID));
                    paramsMap.put("sign", SignatureUtil.sign(paramsMap));
                    logger.info("查看视频播放是否结束url：" + url);
                    String result = MyTerminalFactory.getSDK().getHttpClient().sendGet(url, paramsMap);
                    logger.info("查看视频播放是否结束结果：" + result);
                    if (!Util.isEmpty(result)) {
                        JSONObject jsonObject = JSONObject.parseObject(result);
                        boolean living = jsonObject.getBoolean("living");
                        if (living) {
                            Message msg = Message.obtain();
                            msg.what = WATCH_LIVE;
                            msg.obj = terminalMessage;
                            handler.sendMessage(msg);
                        } else {
                            // TODO: 2018/8/7
                            Intent intent = new Intent(ChatBaseActivity.this, PlayLiveHistoryActivity.class);
                            intent.putExtra("terminalMessage", terminalMessage);
                            ChatBaseActivity.this.startActivity(intent);
                        }
                    }
                });
            }

            if (terminalMessage.messageType == MessageType.AUDIO.getCode()) {
                logger.debug("点击了录音消息！");
            }
            /**  跳转到合并转发  **/
            if (terminalMessage.messageType == MessageType.MERGE_TRANSMIT.getCode()) {
                Intent intent = new Intent(ChatBaseActivity.this, MergeTransmitListActivity.class);
                intent.putExtra(cn.vsx.vc.utils.Constants.IS_GROUP, isGroup);
                intent.putExtra(cn.vsx.vc.utils.Constants.USER_ID, userId);
                intent.putExtra(cn.vsx.vc.utils.Constants.TERMINALMESSAGE, terminalMessage);
                ChatBaseActivity.this.startActivity(intent);
            }
        }
    };

    private void watchLive(TerminalMessage terminalMessage) {
        Intent intent = new Intent(this, PullLivingService.class);
        intent.putExtra(cn.vsx.vc.utils.Constants.WATCH_TYPE, cn.vsx.vc.utils.Constants.ACTIVE_WATCH);
        intent.putExtra(cn.vsx.vc.utils.Constants.TERMINALMESSAGE, terminalMessage);
        startService(intent);
    }

    /**
     * 显示转发popupwindow
     **/
    private ReceiverShowTransponPopupHandler mReceiverShowTransponPopupHandler = new ReceiverShowTransponPopupHandler() {
        @Override
        public void handler(int transponType) {
            handler.post(() -> {
                /**  没有进行组呼的时候才弹出 **/
                //隐藏合并转发按钮
                if (getMergeTransmitState()) {
                    //检查是否选择了消息
                    if (checkChooseMessageToMergeTransmit()) {
                        if(funcation!=null){
                            funcation.setMergeTransmitVisibility(View.GONE);
                        }
                        if (temporaryAdapter != null) {
                            temporaryAdapter.setIsForWardMore(false);
                            temporaryAdapter.notifyDataSetChanged();
                        }
                    } else {
                        ToastUtil.showToast(ChatBaseActivity.this, getString(R.string.please_choose_use_merge_transmit_message));
                        return;
                    }
                }
                Intent intent = new Intent(ChatBaseActivity.this, TransponActivity.class);
                intent.putExtra(cn.vsx.vc.utils.Constants.TRANSPON_TYPE, transponType);
                startActivityForResult(intent, CODE_TRANSPON_REQUEST);
            });
        }
    };

    /**
     * 合并转发
     **/
    private ReceiverShowForwardMoreHandler mReceiverShowForwardMoreHandler = new ReceiverShowForwardMoreHandler() {
        @Override
        public void handler() {
            handler.post(() -> openMergeTransmitState());
        }
    };

    /**
     * 显示复制popupwindow
     **/
    private ReceiverShowCopyPopupHandler mReceiverShowCopyPopupHandler = new ReceiverShowCopyPopupHandler() {
        @Override
        public void handler(final TerminalMessage terminalMessage) {
            handler.post(() -> {
                ClipboardManager cmb = (ClipboardManager) ChatBaseActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                if (terminalMessage.messageType == 1) {
                    cmb.setText(temporaryAdapter.transponMessage.messageBody.getString(JsonParam.CONTENT));
                } else if (terminalMessage.messageType == 2) {
                    logger.info("sjl_:" + terminalMessage.messageType + "," + temporaryAdapter.transponMessage.messagePath);
                    String path = temporaryAdapter.transponMessage.messagePath;
                    File file = new File(path);
                    String content = com.zectec.imageandfileselector.utils.FileUtil.getStringFromFile(file);
                    cmb.setText(content);
                }
                ToastUtil.showToast(getString(R.string.text_replication_success), ChatBaseActivity.this);
            });
        }
    };

    /**
     * 撤回消息
     **/
    private ReceiverShowWithDrawPopupHandler mReceiverShowWithDrawPopupHandler = terminalMessage -> handler.post(() -> {
        TerminalFactory.getSDK().getTerminalMessageManager().requestRecallRecordMessage(terminalMessage.messageId, terminalMessage.messageBodyId);
    });
    /**
     * 撤回消息
     **/
    private ReceiveResponseRecallRecordHandler mReceiveResponseRecallRecordHandler = (resultCode, resultDesc, messageId, messageBodyId) -> {
        if (resultCode == 0) {
            updataMessageWithDrawState(messageId, messageBodyId);
        } else {
            ToastUtil.showToast(ChatBaseActivity.this, resultDesc);
        }
    };

    /**
     * 收到别人撤回消息的通知
     **/
    private ReceiveNotifyRecallRecordHandler mNotifyRecallRecordMessageHandler = (version, messageId, messageBodyId) -> {
        updataMessageWithDrawState(messageId, messageBodyId);
    };

    /**
     * 更新消息的撤回状态
     *
     * @param messageId
     */
    private void updataMessageWithDrawState(long messageId, String messageBodyId) {
        if (android.text.TextUtils.isEmpty(messageBodyId)) {
            return;
        }
        for (TerminalMessage message1 : chatMessageList) {
            if (android.text.TextUtils.equals(messageBodyId, message1.messageBodyId)) {
                message1.messageStatus = MessageStatus.MESSAGE_RECALL.toString();
            }
        }
        handler.post(() -> {
            if (temporaryAdapter != null) {
                temporaryAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * 选择相片、打开相机、选择文件、发送位置、上报图像、请求上报图像
     */
    public ReceiverSendFileCheckMessageHandler mReceiverSendFileCheckMessageHandler = new ReceiverSendFileCheckMessageHandler() {
        @Override
        public void handler(int msgType, final boolean showOrHidden, int userId) {
            if (userId != 0 && ChatBaseActivity.this.userId != userId)
                return;
            Observable.just(msgType)
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(msgType1 -> {
                        if (showOrHidden) {
                            switch (msgType1) {
                                case ReceiverSendFileCheckMessageHandler.PHOTO_ALBUM://从相册中选择相片
                                    if (ContextCompat.checkSelfPermission(ChatBaseActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(ChatBaseActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSIONS_REQUEST_CODE);
                                    } else {
                                        setViewVisibility(fl_fragment_container, View.VISIBLE);
                                        getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fl_fragment_container, new ImageSelectorFragment()).commit();
                                    }
                                    break;
                                case ReceiverSendFileCheckMessageHandler.CAMERA://调用相机拍照
                                    autoObtainCameraPermission();
                                    break;
                                case ReceiverSendFileCheckMessageHandler.FILE://选取文件
                                    if (ContextCompat.checkSelfPermission(ChatBaseActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(ChatBaseActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSIONS_REQUEST_CODE);
                                    } else {
                                        setViewVisibility(fl_fragment_container, View.VISIBLE);
                                        getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fl_fragment_container, new FileMainFragment()).commit();
                                    }
                                    break;
                                case ReceiverSendFileCheckMessageHandler.POST_BACK_VIDEO://上报图像
                                    postVideo();
                                    break;
                                case ReceiverSendFileCheckMessageHandler.REQUEST_VIDEO://请求图像
                                    requestVideo();
                                    break;
                                case ReceiverSendFileCheckMessageHandler.NFC://NFC
                                    checkNFC(userId,true);
                                    break;
                                case ReceiverSendFileCheckMessageHandler.QR_CODE://二维码
//                                    showQRDialog();
                                    goToScan();
                                    break;
                            }
                        } else {
                            setViewVisibility(fl_fragment_container, View.GONE);
                        }
                    });
        }
    };


    /**
     * 扫描二维码
     */
    private void goToScan(){
        if (MyApplication.instance.getVideoLivePlayingState() == VideoLivePlayingState.IDLE && MyApplication.instance.getVideoLivePushingState() == VideoLivePushingState.IDLE){
            goToScanActivity();
        }else{
            ToastUtil.showToast(this,getString(R.string.text_in_video_function));
        }

    }
    /**
     * 显示扫二维码的弹窗
     */
    private void showQRDialog() {
        QRBindingDialog qrBindingDialog = new QRBindingDialog(ChatBaseActivity.this);
        HashMap<String, String> hashMap = TerminalFactory.getSDK().getHashMap(Params.GROUP_WARNING_MAP, new HashMap<String, String>());
        //是否是临时组
        if (hashMap.containsKey(userId + "") && !android.text.TextUtils.isEmpty(hashMap.get(userId + ""))) {
            qrBindingDialog.showDialog(userId, hashMap.get(userId + ""));
        }else{
            qrBindingDialog.showDialog(userId, "");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActivity = true;
//        if (mNfcAdapter != null) {
//            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
//        }
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverSendFileHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverReplayIndividualChatVoiceHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveMultimediaMessageCompleteHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveHistoryMultimediaFailHandler);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivity = false;
//        if (mNfcAdapter != null) {
//            mNfcAdapter.disableForegroundDispatch(this);
//        }
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverSendFileHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverReplayIndividualChatVoiceHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMultimediaMessageCompleteHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveHistoryMultimediaFailHandler);
    }

//    @Override public NdefMessage createNdefMessage(NfcEvent event) {
//        RecorderBindTranslateBean bean = MyApplication.instance.getBindTranslateBean();
//        if(bean != null){
//            return new NdefMessage(new NdefRecord[] { NfcUtil.creatTextRecord(new Gson().toJson(bean))});
//        }
//        return null;
//    }
//
//    @Override
//    public void onNdefPushComplete(NfcEvent event) {
//        logger.debug("onNdefPushComplete:"+event);
//        MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveNFCWriteResultHandler.class,0,"");
//    }
    /**
     * 获取数据并刷新页面
     */
    private void refreshData() {
        refreshing = true;
        // 下拉刷新操作
//        if (chatMessageList.size() <= 0) {
//            refreshing = false;
//            stopRefreshAndToast(getString(R.string.text_no_more_data));
//            return;
//        }
        //先从本地数据库中拿数据
//        List<TerminalMessage> groupMessageRecord1 = MyTerminalFactory.getSDK().getTerminalMessageManager().getGroupMessageRecord(
//                isGroup ? MessageCategory.MESSAGE_TO_GROUP.getCode() : MessageCategory.MESSAGE_TO_PERSONAGE.getCode(), userId,
//                chatMessageList.size()>0?(chatMessageList.get(0).sendTime - 1):0, TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0));

//        if (groupMessageRecord1 != null && groupMessageRecord1.size() > 0) {
//            logger.info("会话列表刷新成功");
//            tempPage++;
//            initClient(groupMessageRecord1, false);
//            if (groupMessageRecord1.size() < PAGE_COUNT) {
//                //从网络获取
//                isEnoughPageCount = false;
//                getHistoryMessageRecord(PAGE_COUNT - groupMessageRecord1.size());
//            } else {
//                isEnoughPageCount = true;
//                handler.post(() -> sflCallList.setRefreshing(false));
//                refreshing = false;
//            }
//        } else {
//            //从网络获取
//            isEnoughPageCount = false;
        tempPage++;
        getHistoryMessageRecord(PAGE_COUNT);
//        }
    }

    /**
     * 设置数据
     *
     * @param groupMessageRecord
     */
    private void setData(List<TerminalMessage> groupMessageRecord, boolean isStopRefresh) {
        if (!isGroup) {
            //个人的警情消息不显示
            Iterator<TerminalMessage> iterator = groupMessageRecord.iterator();
            while (iterator.hasNext()) {
                TerminalMessage next = iterator.next();
                if (next.messageType == MessageType.WARNING_INSTANCE.getCode()) {
                    iterator.remove();
                }
            }

        }
        Collections.sort(groupMessageRecord);
        intersetMessageToList(groupMessageRecord, historyFailMessageList);
        List<TerminalMessage> groupMessageRecord2 = new ArrayList<>();
        groupMessageRecord2.addAll(chatMessageList);
        stopRefresh(groupMessageRecord, groupMessageRecord2, groupMessageRecord.size(), isStopRefresh);
        tempGetMessage = groupMessageRecord.get(0);
        if (isStopRefresh) {
            refreshing = false;
        }
    }

    /**
     * 获取历史消息记录
     *
     * @param messageCount
     */
    private void getHistoryMessageRecord(int messageCount) {
        long messageId = tempGetMessage != null ? tempGetMessage.messageId : 0l;
        long messageVersion = tempGetMessage != null ? tempGetMessage.messageVersion : 0l;
        MyTerminalFactory.getSDK().getThreadPool().execute(() -> {
            long groupUniqueNo = 0L;
            if (isGroup) {
                if (uniqueNo != 0L) {
                    groupUniqueNo = uniqueNo;
                } else {
                    groupUniqueNo = MyTerminalFactory.getSDK().getTerminalMessageManager().getGroupUniqueNo(userId);
                }
            }
            MyTerminalFactory.getSDK().getTerminalMessageManager().getHistoryMessageRecord(isGroup, userId, messageId, groupUniqueNo, messageVersion, messageCount);
        });
    }

    /**
     * 停止刷新
     */
    private void stopRefresh(final List<TerminalMessage> groupMessageRecord1, final List<TerminalMessage> groupMessageRecord2, final int position, boolean isStopRefresh) {
        handler.post(() -> {
            chatMessageList.clear();
            chatMessageList.addAll(groupMessageRecord1);
            chatMessageList.addAll(groupMessageRecord2);
            if (temporaryAdapter != null) {
                temporaryAdapter.notifyItemRangeInserted(0, position);
            }
            if (tempPage == 1) {
                groupCallList.smoothScrollBy(0, 0);
            } else {
                groupCallList.smoothScrollBy(0, -DensityUtil.dip2px(ChatBaseActivity.this, 30));
            }
//            groupCallList.scrollBy(0, -DensityUtil.dip2px(ChatBaseActivity.this, 30));
            if (isStopRefresh) {
                sflCallList.setRefreshing(false);
            }
        });
    }

    /**
     * 停止刷新
     */
    private void stopRefreshAndToast(final String messge) {
        handler.post(() -> {
            sflCallList.setRefreshing(false);
            ToastUtil.showToast(ChatBaseActivity.this, messge);
        });
    }

//    /**
//     * 转发
//     **/
//    private ReceiverTransponHandler mReceiverTransponHandler = new ReceiverTransponHandler() {
//        @Override
//        public void handler(ChatMember chatMember) {
//            if(temporaryAdapter!=null){
//                if(temporaryAdapter.isForWardMore()){
//                    //合并转发
//                    transponMessageMore(chatMember);
//                }else{
//                    //单个转发
//                    temporaryAdapter.transponMessage(chatMember);
//                }
//            }
//        }
//    };


    private GetHistoryMessageRecordHandler getHistoryMessageRecordHandler = messageRecord -> {
        //加上同步，防止更新消息时又来新的消息，导致错乱
        synchronized (ChatBaseActivity.this) {
            //更新未读消息和聊天界面
            if (messageRecord.isEmpty()) {
                handler.post(() -> {
                    if (chatMessageList.size() != 0) {
                        stopRefreshAndToast("没有更多消息了");
                    }
                });
            } else {
//                messageRecord.remove(0);
                setData(messageRecord, true);
            }
            handler.post(() -> {
                if (chatMessageList.size() > 0) {
                    if (tempPage == 1) {
                        setListSelection(chatMessageList.size() - 1);
                    } else {
                        if (chatMessageList.size() > messageRecord.size()) {
                            setListSelection(messageRecord.size());
                        }
                    }
                }
                refreshing = false;
                temporaryAdapter.notifyDataSetChanged();
            });
        }
    };
    /**
     * 从网络获取数据后刷新页面
     */
    private ReceiveHistoryMessageNotifyDateHandler receivePersonMessageNotifyDateHandler = (resultCode, resultDes) -> {
        if (resultCode != BaseCommonCode.SUCCESS_CODE) {
            if (tempPage > 1) {
                tempPage--;
            }
            if (resultCode == TerminalErrorCode.OPTION_EXECUTE_ERROR.getErrorCode() && !isEnoughPageCount) {
                handler.post(() -> {
                    sflCallList.setRefreshing(false);


                });
            } else {
                stopRefreshAndToast(resultDes);
            }
            refreshing = false;
        } else {

        }
        handler.post(() -> temporaryAdapter.notifyDataSetChanged());
    };

    /**
     * 获取警情详情之后替换消息
     */
    private GetWarningMessageDetailHandler getWarningMessageDetailHandler = new GetWarningMessageDetailHandler(){
        @Override
        public void handle(TerminalMessage terminalMessage,boolean newMessage) {
            if(!newMessage&&(chatMessageList.contains(terminalMessage))){
                int index = chatMessageList.indexOf(terminalMessage);
                if(index>=0&&index<chatMessageList.size()&&chatMessageList.get(index)!=null){
                    TerminalMessage oldMessage = chatMessageList.get(index);
                    //if(!oldMessage.messageBody.containsKey(JsonParam.DETAIL) || !oldMessage.messageBody.getBooleanValue(JsonParam.DETAIL)){
                        Collections.replaceAll(chatMessageList, oldMessage,terminalMessage);
                        handler.post(() -> temporaryAdapter.notifyItemChanged(index));
                    //}
                }
            }
        }
    };

//    /**  获取百度地图定位的信息  **/
//    private ReceiveGetBaiDuLocationHandler mReceiveGetBaiDuLocationHandler = new ReceiveGetBaiDuLocationHandler() {
//        @Override
//        public void handler(final double longitude, final double latitude) {
//            logger.info("ReceiveGetBaiDuLocationHandler:longitude="+longitude+"&&latitude="+latitude);
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    if(longitude != 0 && latitude != 0) {//获取成功
//                        sendLocation(longitude, latitude, MyTerminalFactory.getSDK().getMessageSeq(), true, false);
//                        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveGetGPSLocationHandler);
//                    }
////                    MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveGetBaiDuLocationHandler);
//                }
//            });
//        }
//    };

    /***  重新进入界面将未发送完的消息插入消息列表中 **/
    private void intersetUnUploadMessage(List<TerminalMessage> unUploadMessageList) {
        long firstVersion = unUploadMessageList.get(0).messageBody.getLongValue(JsonParam.DOWN_VERSION_FOR_FAIL);
        int count = chatMessageList.size();
        int intersetPos = -1;
        for (int i = count - 1; i >= 0; i--) {
            TerminalMessage terminalMessage1 = chatMessageList.get(i);
            if (firstVersion == terminalMessage1.messageVersion) {
                intersetPos = i;
                break;
            }
        }
        if (intersetPos == -1) {
            chatMessageList.addAll(0, unUploadMessageList);
        } else {
            chatMessageList.addAll(intersetPos + 1, unUploadMessageList);
        }
        if (temporaryAdapter != null) {
            temporaryAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 上传进度更新
     ***/
    private ReceiveUploadProgressHandler mReceiveUploadProgressHandler = new ReceiveUploadProgressHandler() {

        @Override
        public void handler(final float percent, final TerminalMessage terminalMessage) {
            if (terminalMessage.messageToId != userIdEncode)
                return;

            final int tokenId = terminalMessage.messageBody.getIntValue(JsonParam.TOKEN_ID);
            int percentInt1 = (int) (percent * 100);
            final int percentInt = percentInt1;
            temporaryAdapter.progressPercentMap.put(tokenId, percentInt);
            final int position = getPosByTokenId(tokenId, chatMessageList);
            if (position < 0)
                return;

            final int viewPos = getViewPos(position);
            logger.info("上传中viewPos:" + viewPos + "percentInt:" + percentInt);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (terminalMessage.messageType == MessageType.VIDEO_CLIPS.getCode()) {
                        //上传视频中，上传成功之后messageUrl才有值
                        if (TextUtils.isEmpty(terminalMessage.messageUrl)) {
                            LoadingCircleView loading_view = null;
                            if (viewPos != -1) {
                                View childView = groupCallList.getChildAt(viewPos);
                                if (childView != null) {
                                    loading_view = childView.findViewById(R.id.loading_view);
                                }
                            }
                            if (percentInt >= 100) {
                                temporaryAdapter.progressPercentMap.remove(tokenId);
                                /***  文件正在发送更新进度条显示 **/
                                if (loading_view != null) {
                                    loading_view.setVisibility(View.GONE);
                                }
                            } else {
                                if (loading_view != null) {
                                    loading_view.setVisibility(View.VISIBLE);
                                    loading_view.setProgerss(percentInt);
                                }
                            }

                        }
                    } else {

                        ProgressBar progressBar_pre_upload = null;
                        TextView tv_progress_pre_upload = null;
                        if (viewPos != -1) {
                            View childView = groupCallList.getChildAt(viewPos);
                            if (childView != null) {
                                progressBar_pre_upload = (ProgressBar) childView.findViewById(R.id.progress_bar);
                                tv_progress_pre_upload = (TextView) childView.findViewById(R.id.tv_progress);
                            }
                        }
                        if (percentInt >= 100) {
                            temporaryAdapter.progressPercentMap.remove(tokenId);
                            /***  文件正在发送更新进度条显示 **/
                            if (progressBar_pre_upload != null && tv_progress_pre_upload != null) {
                                progressBar_pre_upload.setVisibility(View.GONE);
                                tv_progress_pre_upload.setVisibility(View.GONE);
                            }
                        } else {
                            if (progressBar_pre_upload != null && tv_progress_pre_upload != null) {
                                progressBar_pre_upload.setVisibility(View.VISIBLE);
                                tv_progress_pre_upload.setVisibility(View.VISIBLE);
                                progressBar_pre_upload.setProgress(percentInt);
                                setText(tv_progress_pre_upload, percentInt + "%");
                            }
                        }
                    }

                }
            });
        }
    };

    /***  下载进度更新 **/
    private ReceiveDownloadProgressHandler mReceiveDownloadProgressHandler = new ReceiveDownloadProgressHandler() {

        @Override
        public void handler(final float percent, TerminalMessage terminalMessage) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (isActivity) {
                        if (terminalMessage.messageType == MessageType.VIDEO_CLIPS.getCode()) {
                            if (null != temporaryAdapter.loadingView) {
                                int percentInt = (int) (percent * 100);
                                if (percentInt >= 100) {
                                    setViewVisibility(temporaryAdapter.loadingView, View.GONE);
                                    temporaryAdapter.loadingView = null;
                                } else {
                                    setViewVisibility(temporaryAdapter.loadingView, View.VISIBLE);
                                    temporaryAdapter.loadingView.setProgerss(percentInt);
                                }
                            }
                        } else {
                            if (temporaryAdapter.downloadProgressBar != null
                                    && temporaryAdapter.download_tv_progressBars != null) {
                                int percentInt = (int) (percent * 100);
                                temporaryAdapter.downloadProgressBar.setProgress(percentInt);
                                setText(temporaryAdapter.download_tv_progressBars, percentInt + "%");

                                if (percentInt >= 100) {
                                    setViewVisibility(temporaryAdapter.downloadProgressBar, View.GONE);
                                    setViewVisibility(temporaryAdapter.download_tv_progressBars, View.GONE);
                                    temporaryAdapter.downloadProgressBar = null;
                                    temporaryAdapter.download_tv_progressBars = null;
                                }
                            }
                        }
                    }
                }
            });
        }
    };

    /**
     * 下拉刷新
     */
    private final class OnRefreshListenerImplementationImpl implements SwipeRefreshLayout.OnRefreshListener {
        @Override
        public void onRefresh() {
            if(!refreshing){
                MyTerminalFactory.getSDK().getThreadPool().execute(() -> refreshData());
            }
        }

    }

    private final class OnInclude_listviewTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                funcation.hideKeyboardAndBottom();
            }
            return true;
        }
    }

    /**
     * 列表touch事件监听
     **/
    private View.OnTouchListener mMessageTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(funcation!=null){
                funcation.hideKeyboardAndBottom();
                funcation.showBottom(false);
            }
            return false;
        }
    };

    public int getChatTargetId() {
        return userId;
    }

    /**
     * 弹出底部菜单栏
     **/
    private ReceiverSelectChatListHandler mReceiverSelectChatListHandler = () -> setListSelection(chatMessageList.size() - 1);

    /**
     * 设置音量键为ptt键时的监听
     */
    private final class OnPTTVolumeBtnStatusChangedListenerImp
            implements OnPTTVolumeBtnStatusChangedListener {
        @Override
        public void onPTTVolumeBtnStatusChange(GroupCallSpeakState groupCallSpeakState,boolean isVolumeUp) {
            if (groupCallSpeakState == IDLE) {
                if (!CheckMyPermission.selfPermissionGranted(ChatBaseActivity.this, Manifest.permission.RECORD_AUDIO)) {
                    ToastUtil.showToast(ChatBaseActivity.this, getString(R.string.text_audio_frequency_is_not_open_audio_is_not_used));
                    logger.error("录制音频权限未打开，语音功能将不能使用。");
                    return;
                }
                int resultCode;
                if(isVolumeUp){
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
                } else {
                    ToastUtil.groupCallFailToast(ChatBaseActivity.this, resultCode);
                }
            } else {
                if (MyApplication.instance.isPttPress) {
                    MyApplication.instance.isPttPress = false;
                    MyTerminalFactory.getSDK().getAudioProxy().volumeCancelQuiet();
                    MyTerminalFactory.getSDK().getGroupCallManager().ceaseGroupCall();
                }
            }
        }
    }

    //消息发送成功
    private ReceiveSendDataMessageSuccessHandler receiveSendDataMessageSuccessHandler = new ReceiveSendDataMessageSuccessHandler() {
        @Override
        public void handler(TerminalMessage terminalMessage) {
            final int tokenId = terminalMessage.messageBody.getIntValue(JsonParam.TOKEN_ID);
            final int position = getPosByTokenId(tokenId, chatMessageList);
            if (position < 0) {
                return;
            }
//            chatMessageList.get(position).messageBody.put(JsonParam.SEND_STATE, MessageSendStateEnum.SEND_SUCCESS);
            final int viewPos = getViewPos(position);
            logger.info("发送成功position:" + position + ",viewPos:" + viewPos);
            handler.post(() -> {
                if (viewPos != -1) {
//                    View childView = groupCallList.getChildAt(viewPos);
//                    temporaryAdapter.progressPercentMap.remove(tokenId);
//                    if (childView != null) {
//                        ProgressBar progressBar_pre_upload = childView.findViewById(R.id.progress_bar);
//                        TextView tv_progress_pre_upload = childView.findViewById(R.id.tv_progress);
//                        if (null != progressBar_pre_upload) {
//                            progressBar_pre_upload.setVisibility(View.GONE);
//                        }
//                        if (null != tv_progress_pre_upload) {
//                            tv_progress_pre_upload.setVisibility(View.GONE);
//                        }
//                    }
                }
//                temporaryAdapter.notifyItemChanged(position);
                temporaryAdapter.setUploadFinished();
            });
            if(funcation!=null){
                funcation.showBottom(false);
            }
            scrollMyListViewToBottom();
        }
    };

    private void deteleFailedMessage(TerminalMessage terminalMessage) {
        LinkedTreeMap<String, List<TerminalMessage>> sendFailMap = MyTerminalFactory.getSDK().getTerminalMessageListMap(Params.MESSAGE_SEND_FAIL, new LinkedTreeMap<String, List<TerminalMessage>>());
        List<TerminalMessage> list = sendFailMap.get(userId + "");
        if (null != list) {
            Iterator<TerminalMessage> iterator = list.iterator();
            while (iterator.hasNext()) {
                TerminalMessage next = iterator.next();
                if (terminalMessage.messageBody.get(JsonParam.TOKEN_ID) == next.messageBody.get(JsonParam.TOKEN_ID)) {
                    //同一条消息，删除
                    iterator.remove();
                }
            }
            Iterator<TerminalMessage> iterator1 = chatMessageList.iterator();
            while (iterator1.hasNext()) {
                TerminalMessage next = iterator1.next();
                if (terminalMessage.messageBody.get(JsonParam.TOKEN_ID) == next.messageBody.get(JsonParam.TOKEN_ID)) {
                    //同一条消息，删除
                    iterator.remove();
                }
            }
        }
    }

    /**
     * 发送信令失败
     **/
    private ReceiveSendDataMessageFailedHandler mReceiveSendDataMessageFailedHandler = new ReceiveSendDataMessageFailedHandler() {

        @Override
        public void handler(TerminalMessage terminalMessage) {
            if (temporaryAdapter.transponMessage != null
                    && terminalMessage.messageFromId == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)
                    && terminalMessage.messageToId != userId
                    && terminalMessage.messageBody.containsKey(JsonParam.TOKEN_ID)
                    && terminalMessage.messageBody.getIntValue(JsonParam.TOKEN_ID) == temporaryAdapter.transponMessage.messageBody.getIntValue(JsonParam.TOKEN_ID)) {
                temporaryAdapter.transponMessage = null;
                ToastUtil.showToast(ChatBaseActivity.this, getString(R.string.text_forward_fail));
                return;
            }
            int tokenId = terminalMessage.messageBody.getIntValue(JsonParam.TOKEN_ID);
            temporaryAdapter.progressPercentMap.remove(tokenId);
            sendMessageFail(terminalMessage, -1);
            if (terminalMessage.messageType == MessageType.VIDEO_CLIPS.getCode()) {
                handler.post(() -> {
                    final int position = getPosByTokenId(tokenId, chatMessageList);
                    final int viewPos = getViewPos(position);
                    if (viewPos != -1) {
                        View childView = groupCallList.getChildAt(viewPos);
                        if (childView != null) {
                            LoadingCircleView loading_view = childView.findViewById(R.id.loading_view);
                            loading_view.setVisibility(View.GONE);
                        }
                    }
                });

            }
        }
    };

    private ReceiveGroupOrMemberNotExistHandler receiveGroupOrMemberNotExistHandler = new ReceiveGroupOrMemberNotExistHandler() {
        @Override
        public void handler(final TerminalMessage terminalMessage) {
            handler.post(() -> {
                if (terminalMessage.messageToId == userId) {
                    ToastUtil.showToast(ChatBaseActivity.this, SignalServerErrorCode.getInstanceByCode(terminalMessage.resultCode).getErrorDiscribe());
                    sendMessageFail(terminalMessage, terminalMessage.resultCode);
                    if (temporaryAdapter != null)
                        temporaryAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    /**
     * 转发结果回调
     */
    private PushMessageSendResultHandler pushMessageSendResultHandler = new PushMessageSendResultHandler() {
        @Override
        public void handler(boolean sendOK, String uuid) {
            handler.post(() -> {
                ToastUtil.showToast(ChatBaseActivity.this, ChatBaseActivity.this.getString(sendOK ? R.string.transpond_success : R.string.transpond_fail));
            });
        }
    };

    public void setSmoothScrollToPosition(int position) {
        groupCallList.smoothScrollToPosition(position);
//        groupCallList.scrollToPosition(position);
    }

    /**
     * 对recyclerview的大小变化的监听
     */
    private View.OnLayoutChangeListener myOnLayoutChangeListener = new View.OnLayoutChangeListener() {

        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            if (oldBottom != -1 && oldBottom > bottom) {
                if (groupCallList != null) {
                    groupCallList.requestLayout();
                    groupCallList.post(() -> {
                        if (temporaryAdapter != null) {
                            groupCallList.scrollToPosition(temporaryAdapter.getItemCount() - 1);
                        }
                    });
                }
            }
        }
    };

    /**
     * 获取是否在合并转发的状态
     *
     * @return
     */
    private boolean getMergeTransmitState() {
        return (temporaryAdapter != null && temporaryAdapter.isForWardMore()) ||
                (funcation != null && funcation.getMergeTransmitVisibility() == View.VISIBLE);
    }

    /**
     * 清空合并转发的状态
     */
    private void clearMergeTransmitState() {
        if (temporaryAdapter != null) {
            temporaryAdapter.clearForWardState();
        }
        if (funcation != null) {
            funcation.setMergeTransmitVisibility(View.GONE);
        }
    }

    /**
     * 打开合并转发的状态
     */
    private void openMergeTransmitState() {
        if (temporaryAdapter != null) {
            temporaryAdapter.openForWardState();
        }
        //显示合并转发的确定按钮
        if (funcation != null) {
            funcation.setMergeTransmitVisibility(View.VISIBLE);
        }
    }

    /**
     * 检查在合并转发时是否选择了消息
     *
     * @return
     */
    private boolean checkChooseMessageToMergeTransmit() {
        boolean canDo = false;
        lee:
        for (TerminalMessage message : chatMessageList) {
            if (message.isForward) {
                canDo = true;
                break lee;
            }
        }
        return canDo;
    }


    /**
     * 合并转发
     *
     * @param list
     */
    private void transponMessageMore(ArrayList<ContactItemBean> list) {
        if (chatMessageList != null && chatMessageList.size() > 0) {
            List<TerminalMessage> forwardList = new ArrayList<>();
            for (TerminalMessage message : chatMessageList) {
                if (message.isForward) {
                    forwardList.add(message);
                }
            }
            int size = (forwardList.size() > 3 ? 3 : forwardList.size());
            JSONArray noteJsonArray = new JSONArray();
            JSONArray idJsonArray = new JSONArray();
            for (int i = 0; i < forwardList.size(); i++) {
                if (i < size) {
                    noteJsonArray.add(String.valueOf(temporaryAdapter.getMessageContent(forwardList.get(i))));
                }
                idJsonArray.add(forwardList.get(i).messageId);
            }
            JSONObject jsonObject = new JSONObject();
            String name = MyTerminalFactory.getSDK().getParam(Params.MEMBER_NAME, "");
            jsonObject.put(JsonParam.SEND_STATE, MessageSendStateEnum.SEND_PRE);
            jsonObject.put(JsonParam.TOKEN_ID, MyTerminalFactory.getSDK().getMessageSeq());
            jsonObject.put(JsonParam.DOWN_VERSION_FOR_FAIL, lastVersion);
            jsonObject.put(JsonParam.CONTENT, String.valueOf(isGroup ? userName + "的消息记录" : (name + "和" + userName + "的消息记录")));
            jsonObject.put(JsonParam.NOTE_LIST, noteJsonArray);
            jsonObject.put(JsonParam.MESSAGE_ID_LIST, idJsonArray);
            TerminalMessage mTerminalMessage = new TerminalMessage();
            mTerminalMessage.messageFromId = MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
            mTerminalMessage.messageFromName = name;
//            mTerminalMessage.messageToId = chatMember.getId();
//            mTerminalMessage.messageToName = chatMember.getTitleName();
            mTerminalMessage.messageBody = jsonObject;
            mTerminalMessage.sendTime = System.currentTimeMillis();
            mTerminalMessage.messageType = MessageType.MERGE_TRANSMIT.getCode();
            mTerminalMessage.messageUrl = "";

            if (temporaryAdapter != null) {
                //发送
                temporaryAdapter.transponForwardMoreMessage(mTerminalMessage, list, pushMessageSendResultHandler);
                //清空转发选择的状态
                temporaryAdapter.clearForWardState();
            }
        }
    }


    private int mposition = -1;
    private int lastPosition = -1;
    private boolean isSameItem = true;
    private ExecutorService executorService = Executors.newFixedThreadPool(1);
    /**
     * 点击个呼录音条目
     */
    private ReceiverReplayIndividualChatVoiceHandler mReceiverReplayIndividualChatVoiceHandler = new ReceiverReplayIndividualChatVoiceHandler() {

        @Override
        public void handler(final TerminalMessage terminalMessage, int postion,int type) {
            mposition = postion;
            isReject=false;
            handler.post(() -> {
                if (MyApplication.instance.getIndividualState() == IndividualCallState.IDLE &&
                        MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.IDLE &&
                        MyApplication.instance.getGroupListenenState() == GroupCallListenState.IDLE) {//不是在组呼也不是在个呼中，可以播放录音

                    if (lastPosition == mposition) {//点击同一个条目
                        if (MyApplication.instance.isPlayVoice) {
                            logger.error("点击同一条目，停止录音，停止动画");
                            if(type == PlayType.PLAY_PRIVATE_CALL.getCode()||type == PlayType.PLAY_GROUP_CALL.getCode()){
                                MyTerminalFactory.getSDK().getTerminalMessageManager().stopMultimediaMessage();
                            }else{
                                MediaManager.release();
                            }
                            MyApplication.instance.isPlayVoice = false;
                            isSameItem = true;
                            temporaryAdapter.refreshPersonContactsAdapter(mposition, chatMessageList, MyApplication.instance.isPlayVoice, isSameItem);
//                            temporaryAdapter.notifyDataSetChanged();
                        } else {
                            executorService.execute(new Runnable() {
                                @Override
                                public void run() {
                                    if (mposition < chatMessageList.size() && mposition >= 0) {
                                        try {
                                            if(type == PlayType.PLAY_PRIVATE_CALL.getCode()||type == PlayType.PLAY_GROUP_CALL.getCode()){
                                                handler.post(() -> {
                                                    terminalMessage.isDownLoadAudio = true;
                                                    temporaryAdapter.notifyItemChanged(postion);
                                                });
                                                MyTerminalFactory.getSDK().getTerminalMessageManager().playMultimediaMessage(chatMessageList.get(mposition), audioPlayComplateHandler);
                                            }else{
                                                downloadRecordFileOrPlay(terminalMessage, onCompletionListener);
                                            }
                                        } catch (IndexOutOfBoundsException e) {
                                            logger.warn("mPosition出现异常，其中mposition=" + mposition + "，mTerminalMessageList.size()=" + chatMessageList.size(), e);
                                        }
                                    }
                                }
                            });
                        }
                    } else {//点击不同条目
                        if (MyApplication.instance.isPlayVoice) {
                            MyTerminalFactory.getSDK().getTerminalMessageManager().stopMultimediaMessage();
                            MediaManager.release();
                        }
                        //播放当前的
                        executorService.execute(new Runnable() {
                            public void run() {
                                if (mposition < chatMessageList.size() && mposition >= 0) {
                                    try {
                                        if(type == PlayType.PLAY_PRIVATE_CALL.getCode()||type == PlayType.PLAY_GROUP_CALL.getCode()){
                                            handler.post(() -> {
                                                terminalMessage.isDownLoadAudio = true;
                                                temporaryAdapter.notifyItemChanged(postion);
                                            });
                                            MyTerminalFactory.getSDK().getTerminalMessageManager().playMultimediaMessage(chatMessageList.get(mposition), audioPlayComplateHandler);
                                        }else{
                                            downloadRecordFileOrPlay(terminalMessage, onCompletionListener);
                                        }
                                    } catch (IndexOutOfBoundsException e) {
                                        logger.warn("mPosition出现异常，其中mposition=" + mposition + "，mTerminalMessageList.size()=" + chatMessageList.size(), e);
                                    }
                                }
                            }
                        });
                    }

                } else {
                    ToastUtil.showToast(ChatBaseActivity.this, getString(R.string.text_can_not_play_audio_now));
                }
            });
        }
    };

    /**
     * 录音播放完成的消息
     */
    private MediaPlayer.OnCompletionListener onCompletionListener = mediaPlayer -> {
        handler.post(() -> {
            //                    logger.error("播放完成的回调触发了========> "+lastPosition+"/"+mposition+"/"+chatMessageList.size());
            MyApplication.instance.isPlayVoice = false;
            isSameItem = true;
            chatMessageList.get(mposition).messageBody.put(JsonParam.UNREAD, false);
            temporaryAdapter.refreshPersonContactsAdapter(mposition, chatMessageList, MyApplication.instance.isPlayVoice, isSameItem);
//            temporaryAdapter.notifyDataSetChanged();
        });
        autoPlay(mposition+1);
    };

    /**
     * 录音播放完成的消息
     */
    private IAudioPlayComplateHandler audioPlayComplateHandler = new IAudioPlayComplateHandler() {
        @Override
        public void handle() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    MyApplication.instance.isPlayVoice = false;
                    isSameItem = true;
//                    logger.error("录音播放完成的消息：" + chatMessageList.get(mposition).toString());
                    temporaryAdapter.refreshPersonContactsAdapter(mposition, chatMessageList, MyApplication.instance.isPlayVoice, isSameItem);
                    setSmoothScrollToPosition(mposition);
                    temporaryAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    protected boolean isReject=false;
    //自动播放下一条语音
    private void autoPlay(int index){
        if(index<chatMessageList.size()){//不是最后一条消息，自动播放
            //不是语音消息跳过执行下一条
            if (chatMessageList.get(index).messageType != MessageType.AUDIO.getCode()&&!isReject) {
                index = index + 1;
                autoPlay(index);
            }else {
                if (chatMessageList.get(index).messageBody.containsKey(JsonParam.UNREAD) &&
                        chatMessageList.get(index).messageBody.getBooleanValue(JsonParam.UNREAD) && !MediaManager.isPlaying() && !isReject) {
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverReplayIndividualChatVoiceHandler.class, chatMessageList.get(index), index, PlayType.PLAY_AUDIO.getCode());
                } else {
                    logger.error("点击消息以下的未读消息已播放完成");
                }
            }

        }else {
            logger.debug("最后一条消息已播放完成");
        }
    }

    /**
     * 开始播放或停止播放的回调
     */
    private ReceiveMultimediaMessageCompleteHandler receiveMultimediaMessageCompleteHandler = (resultCode, resultDes, message) -> {
        logger.info("ReceiveMultimediaMessageCompleteHandler   "+resultCode+"/"+resultDes);
        handler.post(() -> {
            if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                if (lastPosition == mposition) {//点击同一个条目
                    isSameItem = true;
                    MyApplication.instance.isPlayVoice = !MyApplication.instance.isPlayVoice;
                } else {//点击不同条目
                    isSameItem = false;
                    MyApplication.instance.isPlayVoice = true;
                }
                if(message!=null&&message.messageId!=0){
                    message.isDownLoadAudio = false;
                }
                Collections.sort(chatMessageList);
                if (temporaryAdapter != null) {
                    temporaryAdapter.refreshPersonContactsAdapter(mposition, chatMessageList, MyApplication.instance.isPlayVoice, isSameItem);
//                    temporaryAdapter.notifyDataSetChanged();
                }
                lastPosition = mposition;
            } else {
                logger.info("开始播放或停止播放的回调" + resultDes);
                ToastUtil.showToast(ChatBaseActivity.this, resultDes);
                if(message!=null){
                    message.isDownLoadAudio = false;
                    if (temporaryAdapter != null) {
                        temporaryAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    };

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
            ToastUtil.showToast(ChatBaseActivity.this, getString(R.string.text_play_recorder_fail_has_no_get_recorder_data_please_try_later));
        }
    };


    /**
     * 下载录音文件并播放
     */
    public void downloadRecordFileOrPlay(final TerminalMessage terminalMessage, final MediaPlayer.OnCompletionListener onCompletionListener) {
        //        logger.info("准备播放个呼音频数据");
        final int[] resultCode = {TerminalErrorCode.UNKNOWN_ERROR.getErrorCode()};
        final String[] resultDes = {""};
        File file = null;
        try {
            if (terminalMessage != null) {
                //如果已经播放过
                file = new File(terminalMessage.messagePath);
                //如果播放过没找到文件，或者未播放 则去服务器请求数据
                if (file == null || !file.exists() || file.length() <= 0) {
                    logger.info("请求录音时URL：" + terminalMessage.messagePath);
                    OkHttpClient mOkHttpClient = new OkHttpClient();
                    MyTerminalFactory.getSDK().getTerminalMessageManager().setMessagePath(terminalMessage, false);
                    String url = terminalMessage.messagePath;
//                    url = TerminalFactory.getSDK().getServiceBusManager().getUrl(url);
                    Request request = new Request.Builder()
                            .url(url)
                            .build();
                    mOkHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
//                            TerminalFactory.getSDK().getServiceBusManager().addErrorCount();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            BufferedSource source = response.body().source();

                            File outFile = MyTerminalFactory.getSDK().getTerminalMessageManager().createFile(terminalMessage);
                            BufferedSink sink = Okio.buffer(Okio.sink(outFile));
                            source.readAll(sink);
                            sink.flush();
                            source.close();

                            if (outFile.length() <= 0) {
                                logger.error("请求到的音频数据是null，直接return！");
                            } else {
                                terminalMessage.messagePath = TerminalFactory.getSDK().getAudioRecordDirectory() + terminalMessage.messageVersion;
                                MyTerminalFactory.getSDK().getTerminalMessageManager().updateTerminalMessage(terminalMessage);
                            }

                            //播放
                            logger.info("获得音频文件为：file=" + outFile.length() + "---文件的路径---" + outFile.getAbsolutePath());
                            if (outFile.exists() && outFile.length() > 0) {
                                resultCode[0] = BaseCommonCode.SUCCESS_CODE;
                                resultDes[0] = getString(R.string.text_get_audio_success_ready_to_play);
                                logger.info("URL获得音频文件，播放录音");
                                MediaManager.playSound(terminalMessage.messagePath, onCompletionListener);
                            } else {
                                resultCode[0] = TerminalErrorCode.SERVER_NOT_RESPONSE.getErrorCode();
                                resultDes[0] = getString(R.string.text_audio_history_not_find_or_server_no_response);
                                logger.error("历史音频未找到，或服务器无响应！");
                            }
                            TerminalFactory.getSDK().notifyReceiveHandler(ReceiveMultimediaMessageCompleteHandler.class, resultCode[0], resultDes[0],terminalMessage);
                        }
                    });
                } else {
                    resultCode[0] = BaseCommonCode.SUCCESS_CODE;
                    resultDes[0] = getString(R.string.text_get_audio_success_from_local_ready_to_play);
                    logger.info("从本地音频文件，播放录音");
                    MediaManager.playSound(terminalMessage.messagePath, onCompletionListener);
                    TerminalFactory.getSDK().notifyReceiveHandler(ReceiveMultimediaMessageCompleteHandler.class, resultCode[0], resultDes[0],terminalMessage);
                }
            } else {
                resultCode[0] = TerminalErrorCode.PARAMETER_ERROR.getErrorCode();
                resultDes[0] = getString(R.string.text_get_audio_history_paramter_wrongful);
                logger.error("获取历史音频参数不合法！");
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiveMultimediaMessageCompleteHandler.class, resultCode[0], resultDes[0],terminalMessage);
            }
        } catch (Exception e) {
            resultDes[0] = getString(R.string.text_get_audio_messge_fail);
            resultCode[0] = TerminalErrorCode.PARAMETER_ERROR.getErrorCode();
            logger.error("获得语音消息失败！", e);
            TerminalFactory.getSDK().notifyReceiveHandler(ReceiveMultimediaMessageCompleteHandler.class, resultCode[0], resultDes[0],terminalMessage);
        }


    }

}

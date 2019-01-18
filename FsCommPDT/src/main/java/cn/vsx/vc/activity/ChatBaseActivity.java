package cn.vsx.vc.activity;

import android.Manifest;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
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

import com.alibaba.fastjson.JSONObject;
import com.readystatesoftware.systembartint.SystemBarTintManager;
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

import org.apache.http.util.TextUtils;
import org.apache.log4j.Logger;

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

import cn.vsx.hamster.common.Constants;
import cn.vsx.hamster.common.MessageCategory;
import cn.vsx.hamster.common.MessageSendStateEnum;
import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.Remark;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.common.util.NoCodec;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.SignalServerErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveDownloadFinishHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveDownloadProgressHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetGPSLocationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupOrMemberNotExistHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyDataMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSendDataMessageFailedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSendDataMessageSuccessHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUploadProgressHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.SignatureUtil;
import cn.vsx.hamster.terminalsdk.tools.Util;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.TemporaryAdapter;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.fragment.LocationFragment;
import cn.vsx.vc.fragment.TransponFragment;
import cn.vsx.vc.model.ChatMember;
import cn.vsx.vc.receiveHandle.ReceiverChatListItemClickHandler;
import cn.vsx.vc.receiveHandle.ReceiverSelectChatListHandler;
import cn.vsx.vc.receiveHandle.ReceiverShowCopyPopupHandler;
import cn.vsx.vc.receiveHandle.ReceiverShowTransponPopupHandler;
import cn.vsx.vc.receiveHandle.ReceiverTransponHandler;
import cn.vsx.vc.service.PullLivingService;
import cn.vsx.vc.utils.DataUtil;
import cn.vsx.vc.utils.DensityUtil;
import cn.vsx.vc.utils.FileUtil;
import cn.vsx.vc.utils.HandleIdUtil;
import cn.vsx.vc.utils.ToastUtil;
import cn.vsx.vc.view.FixedRecyclerView;
import cn.vsx.vc.view.FunctionHidePlus;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.audio.CheckMyPermission;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

import static cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState.IDLE;

/**
 * Created by gt358 on 2017/8/16.
 */

public abstract class ChatBaseActivity extends BaseActivity{
    private static final int CODE_CAMERA_REQUEST = 0x11;/** 打开相机 */
    private static final int CODE_IMAGE_RESULT=0;
    private static final int CODE_VIDEO_RESULT=1;
    private static final int CAMERA_PERMISSIONS_REQUEST_CODE = 0x13;/** 请求相机权限 */
    private static final int STORAGE_PERMISSIONS_REQUEST_CODE = 0x14;/** 请求存储读取权限 */

    protected Logger logger = Logger.getLogger(getClass());
    protected HashMap<Integer, String> idNameMap = TerminalFactory.getSDK().getSerializable(Params.ID_NAME_MAP, new HashMap<Integer, String>());

    protected List<TerminalMessage> allFailMessageList = new ArrayList<>();//当前会话所有发送失败消息集合
    protected List<TerminalMessage> historyFailMessageList = new ArrayList<>();//当前会话历史发送失败消息集合
    protected Map<Integer, TerminalMessage> unFinishMsgList = new HashMap<>();//
//    RelativeLayout rl_include_listview;
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
    private OnBackListener backListener;
    private long lastVersion;//最后一条发送成功的消息的version
    private String live_theme;
    private boolean refreshing;
    private static final int WATCH_LIVE = 0;
    protected Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch(msg.what){
                case WATCH_LIVE:
                    TerminalMessage terminalMessage = (TerminalMessage) msg.obj;
                    watchLive(terminalMessage);
                break;
            }
        }
    };
    /** 设置状态栏透明 **/
    protected  void setSatusBarTransparent () {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    /**  设置statusbar颜色 **/
    protected  void setStatusBarColor () {
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setTintColor(ContextCompat.getColor(this, R.color.group_call_news_bar_bg));
    }

    public void initListener () {
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveNotifyDataMessageHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveSendDataMessageFailedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveDownloadFinishHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveUploadProgressHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveDownloadProgressHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupOrMemberNotExistHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveSendDataMessageSuccessHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverSendFileCheckMessageHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverChatListItemClickHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverShowTransponPopupHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverShowCopyPopupHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverTransponHandler);
//        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverSendFileHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverToFaceRecognitionHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverSelectChatListHandler);
        sflCallList.setOnRefreshListener(new OnRefreshListenerImplementationImpl());
        groupCallList.setOnTouchListener(mMessageTouchListener);
        setOnPTTVolumeBtnStatusChangedListener(new OnPTTVolumeBtnStatusChangedListenerImp());
        groupCallList.addOnLayoutChangeListener(myOnLayoutChangeListener);
//        rl_include_listview.setOnTouchListener(new OnInclude_listviewTouchListener());
    }
    @Override
    public void initData() {
        userId = getIntent().getIntExtra("userId", 0);
        userName = getIntent().getStringExtra("userName");
        isGroup = getIntent().getBooleanExtra("isGroup", false);
        speakingId = getIntent().getIntExtra("speakingId",0);
        speakingName = getIntent().getStringExtra("speakingName");
        newsBarGroupName.setText(HandleIdUtil.handleName(userName));
        setToIds();
        List<TerminalMessage> groupMessageRecord = MyTerminalFactory.getSDK().getTerminalMessageManager().getGroupMessageRecord(
                isGroup ? MessageCategory.MESSAGE_TO_GROUP.getCode() : MessageCategory.MESSAGE_TO_PERSONAGE.getCode(), userId,
                0, TerminalFactory.getSDK().getParam(Params.MEMBER_ID,0));
        Collections.sort(groupMessageRecord);
        chatMessageList.addAll(groupMessageRecord);
        if (chatMessageList.size() > 0) {
            lastVersion = chatMessageList.get(chatMessageList.size() - 1).messageVersion;
        }
        HashMap<String, List<TerminalMessage>> sendFailMap = MyTerminalFactory.getSDK().getSerializable(Params.MESSAGE_SEND_FAIL, new HashMap<String, List<TerminalMessage>>());
        if(sendFailMap != null ) {
            List<TerminalMessage> list = sendFailMap.get(userId + "");
            if(list != null) {
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
                    if(terminalMessage.resultCode != 0) {
                        terminalMessage.messageBody.put(JsonParam.DOWN_VERSION_FOR_FAIL, lastVersion);
                    }else {
                        terminalMessage.messageBody.put(JsonParam.SEND_STATE, MessageSendStateEnum.SEND_PRE);
                    }
                    chatMessageList.add(terminalMessage);
                }
            }
        }
        if (unFinishMsgList == null) {
            unFinishMsgList = new HashMap<>();
        }
        temporaryAdapter = new TemporaryAdapter(chatMessageList, this, idNameMap);
        temporaryAdapter.setIsGroup(isGroup);
        temporaryAdapter.setFragment_contener(fl_fragment_container);
        groupCallList.setAdapter(temporaryAdapter);
        if (chatMessageList.size() > 0){
            setListSelection(chatMessageList.size() - 1);
        }
//        keyHeight = MyTerminalFactory.getSDK().getParam(Params.KEYBOARD_HEIGHT, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        funcation.hideKeyboard(true);
        funcation.hideKeyboardAndBottom();
    }

    /**  将数据插入列表中  **/
    protected void intersetMessageToList(List<TerminalMessage> messageList, List<TerminalMessage> intersetMessageList) {
        int count = intersetMessageList.size();
        if(!messageList.isEmpty()){
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
                            interposition = j+1;
                        }
                    }
                    if (interposition != -1) {
                        messageList.add(interposition, failTerminalMessage);
                        intersetMessageList.remove(failTerminalMessage);
                    }
                }
            }
        }else {
            messageList.addAll(intersetMessageList);
            Collections.sort(messageList);
        }
    }

    public void doOtherDestroy () {

        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveNotifyDataMessageHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveSendDataMessageFailedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveDownloadFinishHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveUploadProgressHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveDownloadProgressHandler);
//        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveGetBaiDuLocationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveGetGPSLocationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupOrMemberNotExistHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveSendDataMessageSuccessHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverChatListItemClickHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverShowTransponPopupHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverShowCopyPopupHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverSendFileCheckMessageHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverTransponHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverToFaceRecognitionHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverSelectChatListHandler);
        HashMap<String, List<TerminalMessage>> sendFailMap = MyTerminalFactory.getSDK().getSerializable(Params.MESSAGE_SEND_FAIL, new HashMap<String, List<TerminalMessage>>());
        sendFailMap.put(userId+"", allFailMessageList);
        MyTerminalFactory.getSDK().putSerializable(Params.MESSAGE_SEND_FAIL, sendFailMap);
        groupCallList.removeOnLayoutChangeListener(myOnLayoutChangeListener);
    }


    public void postVideo () {}

    public void requestVideo () {}

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        logger.info("onActivityResult-----"+"requestCode:"+requestCode+",resultCode:"+resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CODE_CAMERA_REQUEST) {
            switch (resultCode) {
                case CODE_IMAGE_RESULT://拍照完成回调
                    //两种方式 获取拍好的图片
                    sendPhotoFromCamera(data);
                    break;
                case CODE_VIDEO_RESULT://小视频完成回调
                    sendVideoFileOrPhoto(data);

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSIONS_REQUEST_CODE: {//调用系统相机申请拍照权限回调
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    PhotoUtils.openCamera(this, CODE_CAMERA_REQUEST);
                    startActivityForResult(new Intent(ChatBaseActivity.this,CameraActivity.class),CODE_CAMERA_REQUEST);
                } else {
                    ToastUtil.showToast(this, "需要相机的权限");
                }
            }    break;
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
            startActivityForResult(new Intent(ChatBaseActivity.this,CameraActivity.class),CODE_CAMERA_REQUEST);
        }
    }

    @Override
    public void onBackPressed() {

        //返回时删除文件
       File dir = new File(TerminalFactory.getSDK().getLogDirectory()+File.separator+"log");
        if(dir.exists() && dir.isDirectory()){
            String[] files = dir.list();
            for(String file : files){
                File deleteFile = new File(dir.getPath()+File.separator+file);
                if(deleteFile.exists()){
                    deleteFile.delete();
                }
            }
            dir.delete();
        }
        if(null != backListener){
            backListener.onBack();
        }else{
            if(getSupportFragmentManager().getBackStackEntryCount() ==0){
                //如果消息不为空则保存信息
                saveUnsendMessage();
                super.onBackPressed();
            }else if(getSupportFragmentManager().getBackStackEntryCount() ==1){
                setViewVisibility(fl_fragment_container, View.GONE);
                getSupportFragmentManager().popBackStack();
            }else {
                super.onBackPressed();
            }
        }
    }

    protected void saveUnsendMessage(){
        String message = groupCallNewsEt.getText().toString().trim();
        if(!Util.isEmpty(message)){
            getSharedPreferences("unsendMessage",MODE_PRIVATE).edit().putString(String.valueOf(userId),message).apply();
        }else {
            if(!TextUtils.isEmpty(getSharedPreferences("unsendMessage", MODE_PRIVATE).getString(String.valueOf(userId),""))){
                getSharedPreferences("unsendMessage",MODE_PRIVATE).edit().remove(String.valueOf(userId)).apply();
            }
        }
    }

    /**  发送消息文件的回调*/
    private ReceiverSendFileHandler mReceiverSendFileHandler = new ReceiverSendFileHandler() {
        @Override
        public void handler(int type) {
            setViewVisibility(fl_fragment_container, View.GONE);
            scrollMyListViewToBottom();
            switch (type) {
                case ReceiverSendFileHandler.TEXT:
                    sendText();
                    break;
                case ReceiverSendFileHandler.PHOTO_ALBUM:
                    getSupportFragmentManager().popBackStack();
                    sendPhoto();
                    break;
                case ReceiverSendFileHandler.FILE:
                    getSupportFragmentManager().popBackStack();
                    sendFile();
                    break;
                case ReceiverSendFileHandler.LOCATION:
//                    MyTerminalFactory.getSDK().registReceiveHandler(mReceiveGetBaiDuLocationHandler);
                    MyTerminalFactory.getSDK().registReceiveHandler(mReceiveGetGPSLocationHandler);
                    MyTerminalFactory.getSDK().getBDGPSManager().myListener.baiduUpdate();
                    sendLocation(0, 0, TEMP_TOKEN_ID, false, false);
                    break;
                case ReceiverSendFileHandler.VOICE:
                    sendRecord();

            }
        }
    };

    private void scrollMyListViewToBottom() {
        groupCallList.postDelayed(new Runnable() {
            @Override
            public void run() {
                groupCallList.scrollToPosition(temporaryAdapter.getItemCount() - 1);
            }
        },10);
    }

    /**发送录音*/
    protected void sendRecord(){
        Set<Map.Entry<String, Record>> entries = Constant.records.entrySet();
        if (entries.size() == 0) {
            return;
        }
        else {
            for (Map.Entry<String, Record> entry : entries) {
                Record record = entry.getValue();
                logger.debug(record.toString());
                addRecordToList(record.getPath(), record.getId(), record.getSize(), record.getStartTime(),record.getEndTime());
            }
            Constant.records.clear();
            setListSelection(chatMessageList.size()-1);
            temporaryAdapter.notifyDataSetChanged();
            temporaryAdapter.uploadFileDelay();
        }
    }
    /** 发送图片 */
    private void sendPhoto() {
        Set<Map.Entry<String, Image>> entries = Constant.images.entrySet();
        if (entries.size() == 0) {
            return;
        }
        else {
            for (Map.Entry<String, Image> entry : entries) {
                Image image = entry.getValue();
                addPhotoToList(image.getPath(), image.getName(), image.getSize(), 0);
            }
            Constant.images.clear();
            setListSelection(chatMessageList.size()-1);
            temporaryAdapter.notifyDataSetChanged();
            temporaryAdapter.uploadFileDelay();
        }
    }
    /** 发送照相之后的图片 */
    private void sendPhotoFromCamera(Intent data) {
        int photoNum = MyTerminalFactory.getSDK().getParam(Params.PHOTO_NUM, 0);
        File file = new File(MyTerminalFactory.getSDK().getPhotoRecordDirectory(), "image"+photoNum+".jpg");
        logger.debug(file.getPath()+","+file.getName()+DataUtil.getFileSize(file));
        if(file.exists()) {
            addPhotoToList(file.getPath(), file.getName(), DataUtil.getFileSize(file)+"", 0);
            temporaryAdapter.notifyDataSetChanged();
            setListSelection(chatMessageList.size()-1);
            temporaryAdapter.uploadFileDelay();
        }
    }

    /** 发送小视频文件 */
    private void sendVideoFileOrPhoto(Intent data){
        if(data == null ||data.getExtras() == null){
            return;
        }
        String url = data.getExtras().getString("url");
        if(TextUtils.isEmpty(url)){
            return;
        }
        File file = new File(url);
        long videoTime = 0;
        try{
            android.media.MediaPlayer mediaPlayer = new android.media.MediaPlayer();
            mediaPlayer.setDataSource(file.getPath());
            mediaPlayer.prepare();
            //获得了视频的时长（以毫秒为单位）
            videoTime  = mediaPlayer.getDuration();
        }catch(IOException e){
            e.printStackTrace();
        }
        if(file.exists()){
            addVideoToList(file.getPath(),file.getName(),DataUtil.getFileSize(file),videoTime);
            temporaryAdapter.notifyDataSetChanged();
            setListSelection(chatMessageList.size()-1);
            temporaryAdapter.uploadFileDelay();
        }
    }
    private void addVideoToList(String filePath, String fileName, long fileSize,long videoTime){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JsonParam.FILE_NAME, fileName);
        jsonObject.put(JsonParam.FILE_SIZE, fileSize);
        jsonObject.put(JsonParam.SEND_STATE, MessageSendStateEnum.SEND_PRE);
        jsonObject.put(JsonParam.TOKEN_ID, MyTerminalFactory.getSDK().getMessageSeq());
        jsonObject.put(JsonParam.DOWN_VERSION_FOR_FAIL, lastVersion);
        jsonObject.put(JsonParam.VIDEO_TIME,videoTime);
        TerminalMessage mTerminalMessage = new TerminalMessage();
        mTerminalMessage.messageType = MessageType.VIDEO_CLIPS.getCode();
        mTerminalMessage.sendTime = System.currentTimeMillis();
        mTerminalMessage.messagePath = filePath;
        mTerminalMessage.messageFromId =  MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
        mTerminalMessage.messageFromName = MyTerminalFactory.getSDK().getParam(Params.MEMBER_NAME, "");
        mTerminalMessage.messageToId = userId;
        mTerminalMessage.messageToName = userName;
        mTerminalMessage.messageBody = jsonObject;

        chatMessageList.add(mTerminalMessage);
        temporaryAdapter.uploadMessages.add(mTerminalMessage);
        unFinishMsgList.put(jsonObject.getIntValue(JsonParam.TOKEN_ID), mTerminalMessage);
        temporaryAdapter.progressPercentMap.put(jsonObject.getIntValue(JsonParam.TOKEN_ID), 0);
        if(temporaryAdapter!=null){
            temporaryAdapter.notifyDataSetChanged();
        }
    }
    /**
     *  将photo的terminalmessage添加到列表中
     * @param url    url
     * @param name   名字
     * @param size   大小
     */
    private void addPhotoToList (String url, String name, String size, int pos) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JsonParam.SEND_STATE, MessageSendStateEnum.SEND_PRE);
        jsonObject.put(JsonParam.PICTURE_NAME, name);
        jsonObject.put(JsonParam.PICTURE_SIZE, size);
        jsonObject.put(JsonParam.TOKEN_ID, MyTerminalFactory.getSDK().getMessageSeq());
        jsonObject.put(JsonParam.DOWN_VERSION_FOR_FAIL, lastVersion);
        TerminalMessage mTerminalMessage = new TerminalMessage();
        mTerminalMessage.messageFromId =  MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
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
        if(temporaryAdapter!=null){
            temporaryAdapter.notifyDataSetChanged();
        }
    }

    /** 发送文件 */
    private void sendFile() {
        Set<Map.Entry<String, FileInfo>> entries = Constant.files.entrySet();
        if (entries.size() == 0) {
            return;
        } else {
            for (Map.Entry<String, FileInfo> entry : entries) {
                FileInfo fileInfo = entry.getValue();
                if(fileInfo.isPhoto){
                    addPhotoToList(fileInfo.getFilePath(), fileInfo.getFileName(), fileInfo.getFileSize()+"", 0);
                }
                else{
                    //如果是日志，因为上传时还在写，所以上传长度和实际长度不一致，导致报错，上传失败
                    if(fileInfo.getFilePath().equals(TerminalFactory.getSDK().getLogDirectory()+ "log.txt")){
                        try{
                            File src = new File(fileInfo.getFilePath());
                            File dir = new File(src.getParent()+File.separator+"log");
                            if(!dir.exists()){
                                dir.mkdirs();
                            }
                            File dst = new File(dir.getPath(),fileInfo.getFileName());
                            if(dst.exists()){
                                dst.delete();
                                dst = new File(dir.getPath(),fileInfo.getFileName());
                            }
                            if(!dst.exists()){
                                dst.createNewFile();
                            }
                            copy(src,dst);
                            addFileToList(dst.getPath(), dst.getName(), dst.length());
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                    }else {
                        addFileToList(fileInfo.getFilePath(), fileInfo.getFileName(), fileInfo.getFileSize());
                    }
                }
            }
            Constant.files.clear();
            temporaryAdapter.notifyDataSetChanged();
            setListSelection(chatMessageList.size()-1);
            temporaryAdapter.uploadFileDelay();
        }
    }

    /**
     * copy a file
     */
    public static void copy(File src, File dst) throws IOException{
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
        mTerminalMessage.messageFromId =  MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
        mTerminalMessage.messageFromName = MyTerminalFactory.getSDK().getParam(Params.MEMBER_NAME, "");
        mTerminalMessage.messageToId = userId;
        mTerminalMessage.messageToName = userName;
        mTerminalMessage.messageBody = jsonObject;

        chatMessageList.add(mTerminalMessage);
        temporaryAdapter.uploadMessages.add(mTerminalMessage);
        unFinishMsgList.put(jsonObject.getIntValue(JsonParam.TOKEN_ID), mTerminalMessage);
        temporaryAdapter.progressPercentMap.put(jsonObject.getIntValue(JsonParam.TOKEN_ID), 0);
        if(temporaryAdapter!=null){
            temporaryAdapter.notifyDataSetChanged();
        }
    }
    //将RecordFile的terminalmessage添加到列表中
    private void addRecordToList(String filePath, String fileName, long fileSize,long startTime,long endTime) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JsonParam.FILE_NAME, fileName);
        jsonObject.put(JsonParam.FILE_SIZE, fileSize);
        jsonObject.put(JsonParam.SEND_STATE, MessageSendStateEnum.SEND_PRE);
        jsonObject.put(JsonParam.TOKEN_ID, MyTerminalFactory.getSDK().getMessageSeq());
        jsonObject.put(JsonParam.DOWN_VERSION_FOR_FAIL, lastVersion);
        jsonObject.put(JsonParam.START_TIME,startTime);
        jsonObject.put(JsonParam.END_TIME,endTime);
        TerminalMessage mTerminalMessage = new TerminalMessage();
        mTerminalMessage.messageType = MessageType.AUDIO.getCode();
        mTerminalMessage.sendTime = System.currentTimeMillis();
        mTerminalMessage.messagePath = filePath;
        mTerminalMessage.messageFromId =  MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
        mTerminalMessage.messageFromName = MyTerminalFactory.getSDK().getParam(Params.MEMBER_NAME, "");
        mTerminalMessage.messageToId = userId;
        mTerminalMessage.messageToName = userName;
        mTerminalMessage.messageBody = jsonObject;

        chatMessageList.add(mTerminalMessage);
        temporaryAdapter.uploadMessages.add(mTerminalMessage);
        unFinishMsgList.put(jsonObject.getIntValue(JsonParam.TOKEN_ID), mTerminalMessage);
        temporaryAdapter.progressPercentMap.put(jsonObject.getIntValue(JsonParam.TOKEN_ID), 0);
        if(temporaryAdapter!=null){
            temporaryAdapter.notifyDataSetChanged();
        }
    }



    /** 发送文本 */
    private void sendText () {
//        if(MyTerminalFactory.getSDK().getParam(Params.NET_OFFLINE, false)) {
//            ToastUtil.showToast(this, "网络已断开！");
//            return;
//        }
        String msg = groupCallNewsEt.getText().toString();
        if(TextUtils.isEmpty(msg)) {
            return;
        }
        if(msg.length() <= Constants.MAX_SHORT_TEXT){
            sendShortText(msg);
        } else{
            sendLongText(msg);
        }
        setText(groupCallNewsEt, "");
    }

    /** 发送短文本 */
    private void sendShortText (String msg) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JsonParam.SEND_STATE, MessageSendStateEnum.SEND_PRE);
        jsonObject.put(JsonParam.CONTENT, msg);
        jsonObject.put(JsonParam.TOKEN_ID, MyTerminalFactory.getSDK().getMessageSeq());
        jsonObject.put(JsonParam.DOWN_VERSION_FOR_FAIL, lastVersion);
        TerminalMessage mTerminalMessage = new TerminalMessage();
        mTerminalMessage.messageFromId =  MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
        mTerminalMessage.messageFromName = MyTerminalFactory.getSDK().getParam(Params.MEMBER_NAME, "");
        mTerminalMessage.messageToId = userId;
        mTerminalMessage.messageToName = userName;
        mTerminalMessage.sendTime = System.currentTimeMillis();
        mTerminalMessage.messageType = MessageType.SHORT_TEXT.getCode();
        mTerminalMessage.messageBody = jsonObject;

        chatMessageList.add(mTerminalMessage);
        setListSelection(chatMessageList.size()-1);
        if(temporaryAdapter!=null){
            temporaryAdapter.notifyDataSetChanged();
        }
    }

    /** 发送长文本 */
    private void sendLongText (String msg) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JsonParam.SEND_STATE, MessageSendStateEnum.SEND_PRE);
        jsonObject.put(JsonParam.TOKEN_ID, MyTerminalFactory.getSDK().getMessageSeq());
        File file = FileUtil.saveString2File(msg, jsonObject.getIntValue(JsonParam.TOKEN_ID));
        jsonObject.put(JsonParam.DOWN_VERSION_FOR_FAIL, lastVersion);
        TerminalMessage mTerminalMessage = new TerminalMessage();
        mTerminalMessage.messageFromId =  MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
        mTerminalMessage.messageFromName = MyTerminalFactory.getSDK().getParam(Params.MEMBER_NAME, "");
        mTerminalMessage.messageToId = userId;
        mTerminalMessage.messageToName = userName;
        mTerminalMessage.messageBody = jsonObject;
        mTerminalMessage.sendTime = System.currentTimeMillis();
        mTerminalMessage.messageType = MessageType.LONG_TEXT.getCode();
        mTerminalMessage.messagePath = file.getPath();

        chatMessageList.add(mTerminalMessage);
        setListSelection(chatMessageList.size()-1);
        if(temporaryAdapter!=null){
            temporaryAdapter.notifyDataSetChanged();
        }
    }

    private final int TEMP_TOKEN_ID = -1;
    /** 发送位置*/
    private void sendLocation (double longitude, double latitude, int tokenId, boolean realSend, boolean getLocationFail) {
        logger.error("sendLocation:"+"longitude："+longitude+"/latitude:"+latitude);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JsonParam.SEND_STATE, MessageSendStateEnum.SEND_PRE);
        if(longitude!=0&&latitude!=0){
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
        mTerminalMessage.messageFromId =  MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
        mTerminalMessage.messageFromName = MyTerminalFactory.getSDK().getParam(Params.MEMBER_NAME, "");
        mTerminalMessage.messageToId = userId;
        mTerminalMessage.messageToName = userName;
        mTerminalMessage.sendTime = System.currentTimeMillis();
        mTerminalMessage.messageType = MessageType.POSITION.getCode();
        mTerminalMessage.messageBody = jsonObject;

        Iterator<TerminalMessage> it = chatMessageList.iterator();
        while(it.hasNext()){
            TerminalMessage next = it.next();
            if(next.messageBody.containsKey(JsonParam.TOKEN_ID) && next.messageBody.getIntValue(JsonParam.TOKEN_ID) == TEMP_TOKEN_ID){
                it.remove();
            }
        }
        chatMessageList.add(mTerminalMessage);

        if(getLocationFail){
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
        setListSelection(chatMessageList.size()-1);
        if(temporaryAdapter!=null){
            temporaryAdapter.notifyDataSetChanged();
        }
    }

    protected void setListSelection (int position) {
        groupCallList.scrollToPosition(position);
    }


    public void hideKey() {
        funcation.hideKey();
    }

    private void saveMemberMap(TerminalMessage terminalMessage) {
        idNameMap.putAll(TerminalFactory.getSDK().getSerializable(Params.ID_NAME_MAP, new HashMap<Integer, String>()));
        idNameMap.put(terminalMessage.messageFromId, terminalMessage.messageFromName);
        idNameMap.put(terminalMessage.messageToId, terminalMessage.messageToName);
        TerminalFactory.getSDK().putSerializable(Params.ID_NAME_MAP, idNameMap);
    }

    /**  设置组呼消息是否未读 **/
    private void setGroupMessageUnread (TerminalMessage terminalMessage) {
        int currentGroupId = MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);//当前组id
        List<Integer> scanGroups = MyTerminalFactory.getSDK().getConfigManager().loadScanGroup();//组扫描列表
        boolean groupScanTog = MyTerminalFactory.getSDK().getParam(Params.GROUP_SCAN, false);//组扫描开关
        int mainGroupId = MyTerminalFactory.getSDK().getParam(Params.MAIN_GROUP_ID, 0);//主组id
        boolean guardMainGroupTog = MyTerminalFactory.getSDK().getParam(Params.GUARD_MAIN_GROUP, false);//主组开关
        if (terminalMessage.isOffLineMessage) {
            terminalMessage.messageBody.put(JsonParam.UNREAD, true);
        } else {
            if(terminalMessage.messageToId == currentGroupId){//是当前值
                terminalMessage.messageBody.put(JsonParam.UNREAD, false);
            }else{//不是当前值
                if (groupScanTog){//组扫描开着
                    boolean isScanGroup = false;
                    for (Integer integer : scanGroups){
                        if (integer == terminalMessage.messageToId){//是扫描的组
                            isScanGroup = true;
                            break;
                        }
                    }
                    if (isScanGroup){//在组扫描列表中
                        terminalMessage.messageBody.put(JsonParam.UNREAD, false);
                    }else {
                        terminalMessage.messageBody.put(JsonParam.UNREAD, true);
                    }
                }else {//组扫描关着，判断主组状态
                    if (guardMainGroupTog){//主组开着
                        if (mainGroupId == terminalMessage.messageToId){//是主组消息
                            terminalMessage.messageBody.put(JsonParam.UNREAD, false);
                        }else {//不是主组消息
                            terminalMessage.messageBody.put(JsonParam.UNREAD, true);
                        }
                    }else {//主组关着
                        terminalMessage.messageBody.put(JsonParam.UNREAD, true);
                    }
                }
            }
        }
        MyTerminalFactory.getSDK().getTerminalMessageManager().updateTerminalMessage(terminalMessage);
    }

    private void setRecordMessageUnread(TerminalMessage terminalMessage){
        if(terminalMessage.isOffLineMessage){//如果是离线消息 则未读
            terminalMessage.messageBody.put(JsonParam.UNREAD, true);
            logger.debug("离线录音消息未读");
        }
        MyTerminalFactory.getSDK().getTerminalMessageManager().updateTerminalMessage(terminalMessage);
    }

    /**  发送信令服务器失败  **/
    private void sendMessageFail (final TerminalMessage terminalMessage, final int resultCode) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                terminalMessage.messageBody.put(JsonParam.SEND_STATE, MessageSendStateEnum.SEND_FAIL);
                logger.info("发送信令服务器失败"+terminalMessage);
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
                while(it.hasNext()){
                    TerminalMessage next = it.next();
                    if(next.messageBody.containsKey(JsonParam.TOKEN_ID) && terminalMessage.messageBody.containsKey(JsonParam.TOKEN_ID) &&
                            next.messageBody.getIntValue(JsonParam.TOKEN_ID) == terminalMessage.messageBody.getIntValue(JsonParam.TOKEN_ID)){
                        it.remove();
                    }
                }
                terminalMessage.messageBody.put(JsonParam.SEND_STATE, MessageSendStateEnum.SEND_FAIL);
                terminalMessage.resultCode = resultCode;
                chatMessageList.add(terminalMessage);
                if(temporaryAdapter!=null){
                    temporaryAdapter.notifyDataSetChanged();
                }
            }
        });
    }


    private boolean isContainMessage (TerminalMessage terminalMessage) {
        for(TerminalMessage mTerminalMessage : chatMessageList) {
            if (terminalMessage.messageVersion == mTerminalMessage.messageVersion)
                return true;
        }
        return false;
    }

    /**  将列表中的terminalMessage替换成新的terminalMessage  **/
    private void replaceMessage (TerminalMessage newTerminalMessage) {
        boolean has = false;
        Iterator<TerminalMessage> it = chatMessageList.iterator();
        while(it.hasNext()){
            TerminalMessage next = it.next();
            if(next.messageBody.containsKey(JsonParam.TOKEN_ID) && newTerminalMessage.messageBody.containsKey(JsonParam.TOKEN_ID) &&
                    next.messageBody.getIntValue(JsonParam.TOKEN_ID) == newTerminalMessage.messageBody.getIntValue(JsonParam.TOKEN_ID)){
                it.remove();
                has = true;
                break;
            }
        }
        if(has){
            chatMessageList.add(newTerminalMessage);
        }
        Collections.sort(chatMessageList);
        if(temporaryAdapter!=null){
            temporaryAdapter.notifyDataSetChanged();
        }
        if (!has) {
            setListSelection(chatMessageList.size()-1);
            lastVersion = newTerminalMessage.messageVersion;
        }
    }

    /**  根据tokenId从消息集合中获取对应的消息 **/
    private TerminalMessage getTerminalMessageByTokenId (int tokenId) {
        for (int i = chatMessageList.size()-1; i >= 0; i--) {
            TerminalMessage terminalMessage1 = chatMessageList.get(i);
            if(terminalMessage1.messageBody.containsKey(JsonParam.TOKEN_ID)
                    && terminalMessage1.messageBody.getIntValue(JsonParam.TOKEN_ID) == tokenId) {
                return terminalMessage1;
            }
        }
        return null;
    }

    /***  根据TokenId从消息集合中获取position **/
    private int getPosByTokenId (int tokenId, List<TerminalMessage> messageList) {
        for (int i = messageList.size()-1; i >= 0; i--) {
            TerminalMessage terminalMessage1 = messageList.get(i);
            if(terminalMessage1.messageBody.containsKey(JsonParam.TOKEN_ID)
                    && terminalMessage1.messageBody.getIntValue(JsonParam.TOKEN_ID) == tokenId) {
                return i;
            }
        }
        return -1;
    }

    /***  对toid进行编码 ***/
    private void setToIds() {
        if (isGroup){
            userIdEncode = NoCodec.encodeGroupNo(userId);
        }else {
            userIdEncode = NoCodec.encodeMemberNo(userId);
        }
    }

    /***  获取ViewPos **/
    private int getViewPos (int positionForList) {
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

    /**  获取原生GPS定位信息   **/
    private ReceiveGetGPSLocationHandler mReceiveGetGPSLocationHandler = new ReceiveGetGPSLocationHandler() {
        @Override
        public void handler(final double longitude, final double latitude) {
            logger.error("ReceiveGetGPSLocationHandler-------"+" "+longitude+"/"+latitude);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if(longitude != 0.0 && latitude != 0.0) {//获取位置成功
                        sendLocation(longitude, latitude, MyTerminalFactory.getSDK().getMessageSeq(), true, false);
                    }else {//获取位置失败
                        sendLocation(longitude, latitude, TEMP_TOKEN_ID, false, true);
                        ToastUtil.showToast(ChatBaseActivity.this, "GPS定位失败！");
                    }
                    MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveGetGPSLocationHandler);
                }
            });
        }
    };

    /***  上传图片或者文件。。。成功或失败时更新列表UI和数据 ***/
    private void setFileAndPhotoMsg (int position) {
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

    protected void setText (TextView textView, String content) {
        if (textView != null) {
            textView.setText(content);
        }
    }

    protected void setViewVisibility (View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
        }
    }


    /**  接收消息 **/
    private ReceiveNotifyDataMessageHandler mReceiveNotifyDataMessageHandler = new ReceiveNotifyDataMessageHandler() {
        @Override
        public void handler(final TerminalMessage terminalMessage) {
            logger.info("接收到消息-----》"+terminalMessage.toString());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    saveMemberMap(terminalMessage);
                    if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_PERSONAGE.getCode()) {//个人消息
                        if(TextUtils.isEmpty(HandleIdUtil.handleName(idNameMap.get(userId)))){
                            newsBarGroupName.setText(HandleIdUtil.handleName(terminalMessage.messageFromName));
                        }else {
                            newsBarGroupName.setText(HandleIdUtil.handleName(idNameMap.get(userId)));
                        }
                    }
                    //转发
                    if (temporaryAdapter.transponMessage != null
                            && terminalMessage.messageFromId == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)
                            && terminalMessage.messageToId != userId
                            && terminalMessage.messageBody.containsKey(JsonParam.TOKEN_ID)
                            && terminalMessage.messageBody.getIntValue(JsonParam.TOKEN_ID) == temporaryAdapter.transponMessage.messageBody.getIntValue(JsonParam.TOKEN_ID)) {
                        temporaryAdapter.transponMessage = null;
                        if (terminalMessage.resultCode == 0)
                            ToastUtil.showToast(ChatBaseActivity.this, "转发成功");
                        else
                            ToastUtil.showToast(ChatBaseActivity.this, "转发失败");
                        return;
                    }

                    if (isGroup){//组会话界面
                        if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_PERSONAGE.getCode())//个人消息屏蔽
                            return;
                        if(terminalMessage.messageToId != userId)//其它组的屏蔽
                            return;
                    }else {//个人会话界面
                        if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode())//组消息屏蔽
                            return;
                        if(terminalMessage.messageFromId == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)){//自己发的
                            if (terminalMessage.messageToId != userId)
                                return;
                        }else {//接收的
                            if(terminalMessage.messageFromId != userId)//其它人的屏蔽
                                return;
                        }
                        /**  图像推送消息，去图像推送助手 **/
                        if (terminalMessage.messageType == MessageType.VIDEO_LIVE.getCode()){
                            if(terminalMessage.messageFromId == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)
                                    && terminalMessage.messageBody.containsKey(JsonParam.REMARK)
                                    && (terminalMessage.messageBody.getIntValue(JsonParam.REMARK) == Remark.ACTIVE_VIDEO_LIVE)
                                    && terminalMessage.resultCode==0){
                                //自己上报的消息，不显示在聊天界面
                                return;
                            }else {
                                Iterator<TerminalMessage> iterator = chatMessageList.iterator();
                                while(iterator.hasNext()){
                                    TerminalMessage next = iterator.next();
                                    //删除之前的上报消息
                                    if(null == next.messageBody ||
                                            TextUtils.isEmpty(next.messageBody.getString(JsonParam.CALLID)) ||
                                            null == terminalMessage.messageBody ||
                                            TextUtils.isEmpty(terminalMessage.messageBody.getString(JsonParam.CALLID))){
                                        continue;
                                    }
                                    if(next.messageBody.getString(JsonParam.CALLID).equals(terminalMessage.messageBody.getString(JsonParam.CALLID))){
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
                        Iterator<TerminalMessage> it = chatMessageList.iterator();
                        while(it.hasNext()){
                            TerminalMessage next = it.next();
                            if(next.messageBody.containsKey(JsonParam.TOKEN_ID) && terminalMessage.messageBody.containsKey(JsonParam.TOKEN_ID) &&
                                    next.messageBody.getIntValue(JsonParam.TOKEN_ID) == terminalMessage.messageBody.getIntValue(JsonParam.TOKEN_ID)){
                                it.remove();
                                isSendFail = true;
                            }
                        }
                        if (terminalMessage.messageType == MessageType.HYPERLINK.getCode()) {//人脸识别
                            int code = terminalMessage.messageBody.getIntValue(JsonParam.CODE);
                            if (code != 0) {
                                if (!isContainMessage(terminalMessage)) {
                                    chatMessageList.add(terminalMessage);
                                    if(temporaryAdapter!=null){
                                        temporaryAdapter.notifyDataSetChanged();
                                    }
                                    lastVersion = terminalMessage.messageVersion;
                                }
                            }
                        }else {
                            //NewsFragment已经保存过数据，此处不需要再保存
//                            if (terminalMessage.messageType==MessageType.GROUP_CALL.getCode()) {
//                                setGroupMessageUnread(terminalMessage);
//                            }else if(terminalMessage.messageType==MessageType.AUDIO.getCode()){
////                                setRecordMessageUnread(terminalMessage);
//                            }

//                            if (terminalMessage.messageFromId == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)){
//                                Map<Integer, TerminalMessage> sendDataSuccessMap = MyTerminalFactory.getSDK().getTerminalMessageManager().getSendDataSuccessMap();
//                                if(!sendDataSuccessMap.containsKey(terminalMessage.messageBody.getIntValue(JsonParam.TOKEN_ID))){
//                                    chatMessageList.add(terminalMessage);
//                                    sendDataSuccessMap.clear();
//                                }
//                            }else {
//                                chatMessageList.add(terminalMessage);
//                            }
                            chatMessageList.add(terminalMessage);
                            if(temporaryAdapter!=null){
                                temporaryAdapter.notifyDataSetChanged();
                            }
                            lastVersion = terminalMessage.messageVersion;
                        }

                        Collections.sort(chatMessageList);
                        setListSelection(chatMessageList.size() - 1);


                        if (isSendFail) {
                            /**  发送失败的消息重新发送，发送成功将其从失败列表中删除  ***/
                            int failPos = getPosByTokenId(terminalMessage.messageBody.getIntValue(JsonParam.TOKEN_ID), allFailMessageList);
                            if (failPos != -1) {
                                allFailMessageList.remove(failPos);
                            }
                        }
                    }


//                    if (terminalMessage.messageType == MessageType.HYPERLINK.getCode()
//                            && terminalMessage.messageFromId != userId) {
//                        return;
//                    }
//                    /**  图片消息不在这添加 **/
//                    if (terminalMessage.messageType == MessageType.PICTURE.getCode() ||
//                            terminalMessage.messageType == MessageType.LONG_TEXT.getCode())
//                        return;
//                    /**   组呼界面不接受个呼和图像消息，，，，，个呼界面不接收组呼消息   **/
//                    if ((isGroup &&(terminalMessage.messageType == MessageType.VIDEO_LIVE.getCode() || terminalMessage.messageType == MessageType.PRIVATE_CALL.getCode()))
//                            || (!isGroup && terminalMessage.messageType == MessageType.GROUP_CALL.getCode()))
//                        return;
//                    //发送消息
//                    if (terminalMessage.messageFromId == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)) {
//                        if (terminalMessage.messageType == MessageType.PRIVATE_CALL.getCode() ||
//                                terminalMessage.messageType == MessageType.GROUP_CALL.getCode()) {
//                            if (!isContainMessage(terminalMessage)) {
//                                chatMessageList.add(terminalMessage);
//                                lastVersion = terminalMessage.messageVersion;
//                            }
//                        }
//                    } else {//接收的消息
//                        if (terminalMessage.messageType == MessageType.HYPERLINK.getCode()) {//人脸识别
//                            int code = terminalMessage.messageBody.getIntValue(JsonParam.CODE);
//                            if (code != 0) {
//                                if (!isContainMessage(terminalMessage)) {
//                                    chatMessageList.add(terminalMessage);
//                                    lastVersion = terminalMessage.messageVersion;
//                                }
//                            }
//                        } else {
//                            if (terminalMessage.messageType == MessageType.GROUP_CALL.getCode()) {
//                                setGroupMessageUnread(terminalMessage);
//                            }
//                            if (!isContainMessage(terminalMessage)) {
//                                lastVersion = terminalMessage.messageVersion;
//                                chatMessageList.add(terminalMessage);
//                            }
//
//                        }
//
//                    }
//                    temporaryAdapter.notifyDataSetChanged();
//                    setListSelection(chatMessageList.size() - 1);

                }
            });

        }
    };

    /** 文件等下载完成的监听handler */
    private ReceiveDownloadFinishHandler mReceiveDownloadFinishHandler = new ReceiveDownloadFinishHandler() {
        @Override
        public void handler(final TerminalMessage terminalMessage, final boolean success) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if(!success){
                        return;
                    }
                    if (isGroup){//组消息
                        if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_PERSONAGE.getCode())//个人消息屏蔽
                            return;
                        if(terminalMessage.messageToId != userId)//其它组的屏蔽
                            return;
                    }else {//个人消息
                        if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode())//个人消息屏蔽
                            return;
                        if(terminalMessage.messageFromId == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)){//自己发的
                            if (terminalMessage.messageToId != userId)
                                return;
                        }else {//接收的
                            if(terminalMessage.messageFromId != userId)//其它人的屏蔽
                                return;
                        }
                    }

                    if (terminalMessage.messageType ==  MessageType.LONG_TEXT.getCode()
                            || terminalMessage.messageType == MessageType.HYPERLINK.getCode()){
                        replaceMessage(terminalMessage);
                    }
                    if(terminalMessage.messageType ==  MessageType.PICTURE.getCode()) {
                        temporaryAdapter.isDownloadingPicture = false;
                        replaceMessage(terminalMessage);
                        //如果是原图下载完了就打开
                        if(terminalMessage.messageBody.containsKey(JsonParam.ISMICROPICTURE) &&
                                !terminalMessage.messageBody.getBooleanValue(JsonParam.ISMICROPICTURE)) {
                            temporaryAdapter.openPhotoAfterDownload(terminalMessage);
                        }
                    }

                    if(terminalMessage.messageType ==  MessageType.FILE.getCode()) {
                        temporaryAdapter.openFileAfterDownload(terminalMessage);
                        temporaryAdapter.isDownloading = false;
                        terminalMessage.messageBody.put(JsonParam.IS_DOWNLOADINF, false);
                        replaceMessage(terminalMessage);
                    }
                    if(terminalMessage.messageType == MessageType.AUDIO.getCode()){
                        temporaryAdapter.isDownloading = false;
                        terminalMessage.messageBody.put(JsonParam.IS_DOWNLOADINF, false);
                        replaceMessage(terminalMessage);
                    }
                    if(terminalMessage.messageType ==  MessageType.VIDEO_CLIPS.getCode()) {
//                        temporaryAdapter.openFileAfterDownload(terminalMessage);点击以后再打开
                        temporaryAdapter.isDownloading = false;
                        terminalMessage.messageBody.put(JsonParam.IS_DOWNLOADINF, false);
                        replaceMessage(terminalMessage);
                    }

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

    /**  会话界面列表条目点击事件 **/

    private ReceiverChatListItemClickHandler mReceiverChatListItemClickHandler = new ReceiverChatListItemClickHandler() {
        @Override
        public void handler(final TerminalMessage terminalMessage, boolean isReceiver) {

            /**  进入定位界面 **/
            if(terminalMessage.messageType == MessageType.POSITION.getCode()) {
                if(terminalMessage.messageBody.containsKey(JsonParam.LONGITUDE) &&
                        terminalMessage.messageBody.containsKey(JsonParam.LATITUDE)){
                    setViewVisibility(fl_fragment_container, View.VISIBLE);
                    double longitude = terminalMessage.messageBody.getDouble(JsonParam.LONGITUDE);
                    double altitude = terminalMessage.messageBody.getDouble(JsonParam.LATITUDE);
                    //http://192.168.1.96:7007/mapLocationl.html?lng=117.68&lat=39.456
                    String url = TerminalFactory.getSDK().getParam(Params.LOCATION_URL,"")+"?lng=" + longitude + "&lat=" + altitude;
                    if(TextUtils.isEmpty(TerminalFactory.getSDK().getParam(Params.LOCATION_URL,""))){
                        ToastUtil.showToast(ChatBaseActivity.this,"请去管理后台配置定位url");
                    }else {
                        LocationFragment locationFragment = LocationFragment.getInstance(url, "", true);
                        locationFragment.setFragment_contener(fl_fragment_container);
                        getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fl_fragment_container,locationFragment ).commit();
                    }
                }else {
                    setViewVisibility(fl_fragment_container, View.VISIBLE);
                    String url = TerminalFactory.getSDK().getParam(Params.LOCATION_URL,"");
                    if(TextUtils.isEmpty(TerminalFactory.getSDK().getParam(Params.LOCATION_URL,""))){
                        ToastUtil.showToast(ChatBaseActivity.this,"请去管理后台配置定位url");
                    }else {
                        LocationFragment locationFragment = LocationFragment.getInstance(url, "", true);
                        locationFragment.setFragment_contener(fl_fragment_container);
                        getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fl_fragment_container,locationFragment ).commit();
                    }
                }
            }

            /**  进入图片预览界面  **/
            if(terminalMessage.messageType == MessageType.PICTURE.getCode()) {
                setViewVisibility(fl_fragment_container, View.VISIBLE);
                FileInfo fileInfo = new FileInfo();
                fileInfo.setFilePath(terminalMessage.messagePath);
                List<FileInfo> images = new ArrayList<>();
                images.add(fileInfo);
                getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fl_fragment_container, new ImagePreviewFragment(images)).commit();
            }

            /**  上报图像  **/
            if(terminalMessage.messageType == MessageType.VIDEO_LIVE.getCode()) {
                //先请求看视频上报是否已经结束
                MyTerminalFactory.getSDK().getThreadPool().execute(new Runnable(){
                    @Override
                    public void run(){
                        String serverIp = MyTerminalFactory.getSDK().getParam(Params.FILE_SERVER_IP, "");
                        int serverPort = MyTerminalFactory.getSDK().getParam(Params.FILE_SERVER_PORT, 0);
                        String url = "http://"+serverIp+":"+serverPort+"/file/download/isLiving";
                        Map<String,String> paramsMap = new HashMap<>();
                        paramsMap.put("callId",terminalMessage.messageBody.getString(JsonParam.CALLID));
                        paramsMap.put("sign", SignatureUtil.sign(paramsMap));
                        logger.info("查看视频播放是否结束url："+url);
                        String result = MyTerminalFactory.getSDK().getHttpClient().sendGet(url, paramsMap);
                        logger.info("查看视频播放是否结束结果："+result);
                        if(!Util.isEmpty(result)){
                            JSONObject jsonObject = JSONObject.parseObject(result);
                            boolean living = jsonObject.getBoolean("living");
                            Long endChatTime = jsonObject.getLong("endChatTime");
                            if(living){
                                Message msg = Message.obtain();
                                msg.what = WATCH_LIVE;
                                msg.obj = terminalMessage;
                                handler.sendMessage(msg);
                            }else {
                                // TODO: 2018/8/7
                                Intent intent = new Intent(ChatBaseActivity.this,LiveHistoryActivity.class);
                                intent.putExtra("terminalMessage",terminalMessage);
//                                intent.putExtra("endChatTime",endChatTime);
                                ChatBaseActivity.this.startActivity(intent);
                            }
                        }
                    }
                });
            }

            if(terminalMessage.messageType==MessageType.AUDIO.getCode()){
                logger.debug("点击了录音消息！");
            }
        }
    };

    private void watchLive(TerminalMessage terminalMessage){
        Intent intent = new Intent(this, PullLivingService.class);
        intent.putExtra(cn.vsx.vc.utils.Constants.WATCH_TYPE, cn.vsx.vc.utils.Constants.ACTIVE_WATCH);
        intent.putExtra(cn.vsx.vc.utils.Constants.TERMINALMESSAGE, terminalMessage);
        startService(intent);
    }

    /**  显示转发popupwindow **/
    private ReceiverShowTransponPopupHandler mReceiverShowTransponPopupHandler = new ReceiverShowTransponPopupHandler() {
        @Override
        public void handler() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    /**  没有进行组呼的时候才弹出 **/
                    TransponFragment transponFragment = TransponFragment.getInstance(userId, temporaryAdapter.transponMessage.messageType);
                    transponFragment.setFragmentContainer(fl_fragment_container);
                    setViewVisibility(fl_fragment_container, View.VISIBLE);
                    getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fl_fragment_container, transponFragment).commit();
                }
            });
        }
    };
    /**  显示复制popupwindow **/
    private ReceiverShowCopyPopupHandler mReceiverShowCopyPopupHandler = new ReceiverShowCopyPopupHandler(){
        @Override
        public void handler(final TerminalMessage terminalMessage) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ClipboardManager cmb = (ClipboardManager) ChatBaseActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                    if (terminalMessage.messageType == 1){
                        cmb.setText(temporaryAdapter.transponMessage.messageBody.getString(JsonParam.CONTENT));
                    }else if (terminalMessage.messageType == 2){
                        logger.info("sjl_:"+terminalMessage.messageType+","+temporaryAdapter.transponMessage.messagePath);
                        String path = temporaryAdapter.transponMessage.messagePath;
                        File file = new File(path);
                        String content = com.zectec.imageandfileselector.utils.FileUtil.getStringFromFile(file);
                        cmb.setText(content);
                    }
                    ToastUtil.showToast("复制成功",ChatBaseActivity.this);
                }
            });
        }
    };
    /**选择相片、打开相机、选择文件、发送位置、上报图像、请求上报图像*/
    public ReceiverSendFileCheckMessageHandler mReceiverSendFileCheckMessageHandler = new ReceiverSendFileCheckMessageHandler() {
        @Override
        public void handler(int msgType, final boolean showOrHidden, int userId) {
            if (userId != 0 && ChatBaseActivity.this.userId != userId)
                return;
            Observable.just(msgType)
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Integer>() {
                        @Override
                        public void call(Integer msgType) {
                            if(showOrHidden) {
                                switch (msgType) {
                                    case ReceiverSendFileCheckMessageHandler.PHOTO_ALBUM://从相册中选择相片
                                        if (ContextCompat.checkSelfPermission(ChatBaseActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                            ActivityCompat.requestPermissions(ChatBaseActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSIONS_REQUEST_CODE);
                                        }
                                        else {
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
                                        }
                                        else {
                                            setViewVisibility(fl_fragment_container, View.VISIBLE);
                                            getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fl_fragment_container,  new FileMainFragment()).commit();
                                        }
                                        break;
                                    case ReceiverSendFileCheckMessageHandler.POST_BACK_VIDEO://上报图像
                                        postVideo();
                                        break;
                                    case ReceiverSendFileCheckMessageHandler.REQUEST_VIDEO://请求图像
                                        requestVideo();
                                        break;
                                }
                            }
                            else {
                                setViewVisibility(fl_fragment_container, View.GONE);
                            }
                        }
                    });
        }
    };

    @Override
    protected void onResume(){
        super.onResume();
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverSendFileHandler);
    }

    @Override
    protected void onPause(){
        super.onPause();
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverSendFileHandler);
    }

    /**
     * 获取数据并刷新页面
     */
    private void refreshData(){
        refreshing = true;
        // 下拉刷新操作
        if (chatMessageList.size() <= 0) {
            refreshing = false;
            stopRefreshAndToast("没有更多消息了！");
            return;
        }
        List<TerminalMessage> groupMessageRecord1 = MyTerminalFactory.getSDK().getTerminalMessageManager().getGroupMessageRecord(
                isGroup ? MessageCategory.MESSAGE_TO_GROUP.getCode() : MessageCategory.MESSAGE_TO_PERSONAGE.getCode(), userId,
                chatMessageList.get(0).sendTime - 1, TerminalFactory.getSDK().getParam(Params.MEMBER_ID,0));
        if (groupMessageRecord1 != null && groupMessageRecord1.size() > 0 ) {
            logger.info("会话列表刷新成功");
            Collections.sort(groupMessageRecord1);
            intersetMessageToList(groupMessageRecord1, historyFailMessageList);
            List<TerminalMessage> groupMessageRecord2 = new ArrayList<>();
            groupMessageRecord2.addAll(chatMessageList);
            stopRefresh(groupMessageRecord1,groupMessageRecord2,groupMessageRecord1.size());
        }else {
            stopRefreshAndToast("没有更多消息了！");
        }
        refreshing = false;
    }
    /**
     * 停止刷新
     */
    private void stopRefresh(final List<TerminalMessage> groupMessageRecord1,final List<TerminalMessage> groupMessageRecord2,final int position){
        handler.post(new Runnable() {
            @Override
            public void run() {
                chatMessageList.clear();
                chatMessageList.addAll(groupMessageRecord1);
                chatMessageList.addAll(groupMessageRecord2);
                if(temporaryAdapter!=null){
                    temporaryAdapter.notifyItemRangeInserted(0,position);
                }
                groupCallList.smoothScrollBy(0, -DensityUtil.dip2px(ChatBaseActivity.this,30));
                sflCallList.setRefreshing(false);
            }
        });
    }
    /**
     * 停止刷新
     */
    private void stopRefreshAndToast(final String messge){
        handler.post(new Runnable() {
            @Override
            public void run() {
                sflCallList.setRefreshing(false);
                ToastUtil.showToast(ChatBaseActivity.this, messge);
            }
        });
    }

    /**  转发 **/
    private ReceiverTransponHandler mReceiverTransponHandler = new ReceiverTransponHandler() {
        @Override
        public void handler(ChatMember chatMember) {
            temporaryAdapter.transponMessage(chatMember);
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
    private void intersetUnUploadMessage (List<TerminalMessage> unUploadMessageList) {
        long firstVersion = unUploadMessageList.get(0).messageBody.getLongValue(JsonParam.DOWN_VERSION_FOR_FAIL);
        int count = chatMessageList.size();
        int intersetPos = -1;
        for (int i = count-1; i >= 0; i--) {
            TerminalMessage terminalMessage1 = chatMessageList.get(i);
            if (firstVersion == terminalMessage1.messageVersion) {
                intersetPos = i;
                break;
            }
        }
        if (intersetPos == -1) {
            chatMessageList.addAll(0, unUploadMessageList);
        }
        else {
            chatMessageList.addAll(intersetPos+1, unUploadMessageList);
        }
        if(temporaryAdapter!=null){
            temporaryAdapter.notifyDataSetChanged();
        }
    }

    /**   上传进度更新 ***/
    private ReceiveUploadProgressHandler mReceiveUploadProgressHandler = new ReceiveUploadProgressHandler() {

        @Override
        public void handler(final float percent, final TerminalMessage terminalMessage) {
            if (terminalMessage.messageToId != userIdEncode)
                return;

            final int tokenId =  terminalMessage.messageBody.getIntValue(JsonParam.TOKEN_ID);
            int percentInt1 = (int) (percent*100);
            final int percentInt = percentInt1;
            temporaryAdapter.progressPercentMap.put(tokenId, percentInt);
            final int position = getPosByTokenId(tokenId, chatMessageList);
            if (position < 0)
                return;

            final int viewPos = getViewPos(position);
            logger.info("上传中viewPos:" + viewPos+"percentInt:"+percentInt);
            handler.post(new Runnable() {
                @Override
                public void run() {
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
                    }
                    else {
                        if (progressBar_pre_upload != null && tv_progress_pre_upload != null) {
                            progressBar_pre_upload.setVisibility(View.VISIBLE);
                            tv_progress_pre_upload.setVisibility(View.VISIBLE);
                            progressBar_pre_upload.setProgress(percentInt);
                            setText( tv_progress_pre_upload, percentInt + "%");
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
                    if(temporaryAdapter.downloadProgressBar != null
                            && temporaryAdapter.download_tv_progressBars != null) {
                        int percentInt = (int) (percent * 100);
                        temporaryAdapter.downloadProgressBar.setProgress(percentInt);
                        setText(temporaryAdapter.download_tv_progressBars, percentInt + "%");

                        if(percentInt >= 100) {
                            setViewVisibility(temporaryAdapter.downloadProgressBar, View.GONE);
                            setViewVisibility(temporaryAdapter.download_tv_progressBars, View.GONE);
                            temporaryAdapter.downloadProgressBar = null;
                            temporaryAdapter.download_tv_progressBars = null;
                        }
                    }
                }
            });
        }
    };

    /**  下拉刷新   */
    private final class OnRefreshListenerImplementationImpl implements SwipeRefreshLayout.OnRefreshListener {
        @Override
        public void onRefresh() {
            MyTerminalFactory.getSDK().getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    refreshData();
                }
            });
        }

    }

    private final class OnInclude_listviewTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event){
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                funcation.hideKeyboardAndBottom ();
            }
            return true;
        }
    }

    /**  列表touch事件监听 **/
    private View.OnTouchListener mMessageTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            funcation.hideKeyboardAndBottom();
            funcation.showBottom(false);
            return false;
        }
    };

    public int getChatTargetId(){
        return userId;
    }

    /**  弹出底部菜单栏 **/
    private ReceiverSelectChatListHandler mReceiverSelectChatListHandler = new ReceiverSelectChatListHandler() {
        @Override
        public void handler() {
            setListSelection(chatMessageList.size() - 1);
        }
    };

    /**设置音量键为ptt键时的监听*/
    private final class OnPTTVolumeBtnStatusChangedListenerImp
            implements BaseActivity.OnPTTVolumeBtnStatusChangedListener {
        @Override
        public void onPTTVolumeBtnStatusChange(GroupCallSpeakState groupCallSpeakState) {
            if (groupCallSpeakState == IDLE) {
                if (!CheckMyPermission.selfPermissionGranted(ChatBaseActivity.this, Manifest.permission.RECORD_AUDIO)){
                    ToastUtil.showToast(ChatBaseActivity.this, "录制音频权限未打开，语音功能将不能使用。");
                    logger.error("录制音频权限未打开，语音功能将不能使用。");
                    return;
                }
                int resultCode = MyTerminalFactory.getSDK().getGroupCallManager().requestGroupCall("");
                if (resultCode == BaseCommonCode.SUCCESS_CODE){
                    MyApplication.instance.isPttPress = true;
                }else{
                    ToastUtil.groupCallFailToast(ChatBaseActivity.this, resultCode);
                }
            } else {
                if(MyApplication.instance.isPttPress){
                    MyApplication.instance.isPttPress = false;
                    MyTerminalFactory.getSDK().getGroupCallManager().ceaseGroupCall();
                }
            }
        }
    }

    //消息发送成功
    private ReceiveSendDataMessageSuccessHandler receiveSendDataMessageSuccessHandler = new ReceiveSendDataMessageSuccessHandler(){
        @Override
        public void handler(TerminalMessage terminalMessage){
            final int tokenId =  terminalMessage.messageBody.getIntValue(JsonParam.TOKEN_ID);
            final int position = getPosByTokenId(tokenId, chatMessageList);
            if (position < 0){
                return;
            }
            final int viewPos = getViewPos(position);
            logger.info("发送成功position:"+position+",viewPos:" + viewPos);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (viewPos != -1) {
                        View childView = groupCallList.getChildAt(viewPos);
                        temporaryAdapter.progressPercentMap.remove(tokenId);
                        if (childView != null) {
                            ProgressBar progressBar_pre_upload = childView.findViewById(R.id.progress_bar);
                            TextView tv_progress_pre_upload = childView.findViewById(R.id.tv_progress);
                            if(null !=progressBar_pre_upload){
                                progressBar_pre_upload.setVisibility(View.GONE);
                            }
                            if(null !=tv_progress_pre_upload){
                                tv_progress_pre_upload.setVisibility(View.GONE);
                            }
                        }
                    }

                }
            });
            temporaryAdapter.setUploadFinished();
            funcation.showBottom(false);
            scrollMyListViewToBottom();
        }
    };

    /**  发送信令失败 **/
    private ReceiveSendDataMessageFailedHandler mReceiveSendDataMessageFailedHandler = new ReceiveSendDataMessageFailedHandler() {

        @Override
        public void handler(TerminalMessage terminalMessage) {
            if (temporaryAdapter.transponMessage != null
                    && terminalMessage.messageFromId == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)
                    && terminalMessage.messageToId != userId
                    && terminalMessage.messageBody.containsKey(JsonParam.TOKEN_ID)
                    && terminalMessage.messageBody.getIntValue(JsonParam.TOKEN_ID) == temporaryAdapter.transponMessage.messageBody.getIntValue(JsonParam.TOKEN_ID)) {
                temporaryAdapter.transponMessage = null;
                ToastUtil.showToast(ChatBaseActivity.this, "转发失败");
                return;
            }
            int tokenId =  terminalMessage.messageBody.getIntValue(JsonParam.TOKEN_ID);
            temporaryAdapter.progressPercentMap.remove(tokenId);
            sendMessageFail(terminalMessage, -1);
        }
    };

    private ReceiveGroupOrMemberNotExistHandler receiveGroupOrMemberNotExistHandler = new ReceiveGroupOrMemberNotExistHandler() {
        @Override
        public void handler(final TerminalMessage terminalMessage) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (terminalMessage.messageToId == userId) {
                        ToastUtil.showToast(ChatBaseActivity.this, SignalServerErrorCode.getInstanceByCode(terminalMessage.resultCode).getErrorDiscribe());
                        sendMessageFail(terminalMessage, terminalMessage.resultCode);
                        if (temporaryAdapter != null)
                            temporaryAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    };

//    @Override
//    public void setSelectedFragment(ImagePreviewItemFragment backHandledFragment) {
//        imagePreviewItemFragment = backHandledFragment;
//    }

    public void setSmoothScrollToPosition(int position){
        groupCallList.smoothScrollToPosition(position);
    }

    public void setBackListener(OnBackListener backListener){
        this.backListener = backListener;
    }
    public interface OnBackListener{
        void onBack();
    }

    /**
     * 对recyclerview的大小变化的监听
     */
    private View.OnLayoutChangeListener myOnLayoutChangeListener = new View.OnLayoutChangeListener() {

        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            if (oldBottom != -1 && oldBottom > bottom) {
                if(groupCallList!=null){
                    groupCallList.requestLayout();
                    groupCallList.post(new Runnable() {
                        @Override
                        public void run() {
                            if (temporaryAdapter != null) {
                                groupCallList.scrollToPosition(temporaryAdapter.getItemCount() - 1);
                            }
                        }
                    });
                }
            }
        }
    };
}

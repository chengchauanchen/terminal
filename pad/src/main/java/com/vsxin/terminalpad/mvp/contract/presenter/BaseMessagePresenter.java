package com.vsxin.terminalpad.mvp.contract.presenter;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;

import com.blankj.utilcode.util.ToastUtils;
import com.ixiaoma.xiaomabus.architecture.mvp.refresh.RefreshPresenter;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.app.PadApplication;
import com.vsxin.terminalpad.mvp.contract.view.IBaseMessageView;
import com.vsxin.terminalpad.mvp.entity.PlayType;
import com.vsxin.terminalpad.mvp.ui.widget.ChooseDevicesDialog;
import com.vsxin.terminalpad.receiveHandler.ReceiveGetHistoryLiveUrlsHandler;
import com.vsxin.terminalpad.receiveHandler.ReceiveGoWatchLiveHandler;
import com.vsxin.terminalpad.receiveHandler.ReceiverActivePushVideoHandler;
import com.vsxin.terminalpad.receiveHandler.ReceiverChatListItemClickHandler;
import com.vsxin.terminalpad.receiveHandler.ReceiverIndividualCallFromMsgItemHandler;
import com.vsxin.terminalpad.receiveHandler.ReceiverRequestVideoHandler;
import com.vsxin.terminalpad.record.MediaManager;
import com.vsxin.terminalpad.utils.DensityUtil;
import com.vsxin.terminalpad.utils.MyDataUtil;
import com.zectec.imageandfileselector.receivehandler.ReceiverSendFileCheckMessageHandler;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import org.apache.http.util.TextUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.vsx.hamster.common.MessageCategory;
import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.ReceiveObjectMode;
import cn.vsx.hamster.common.Remark;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.TerminalErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.audio.IAudioPlayComplateHandler;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallState;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.GetHistoryMessageRecordHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCurrentGroupIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveDownloadFinishHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveDownloadProgressHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHiKvisionUrlHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHistoryMessageNotifyDateHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHistoryMultimediaFailHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMultimediaMessageCompleteHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyDataMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiverReplayIndividualChatVoiceHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * 作者：xuxiaolong
 * 版本：1.0
 * 创建日期：2019/8/3
 * 描述：
 * 修订历史：
 */
public class BaseMessagePresenter<V extends IBaseMessageView> extends RefreshPresenter<TerminalMessage, V>{

    protected Logger logger = Logger.getLogger(getClass());

    private List<TerminalMessage> data = new ArrayList<>();
    private TerminalMessage tempGetMessage;
    protected Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean isEnoughPageCount = false;//每次从本地取的数据的条数是否够10条
    protected List<TerminalMessage> allFailMessageList = new ArrayList<>();//当前会话所有发送失败消息集合
    protected List<TerminalMessage> historyFailMessageList = new ArrayList<>();//当前会话历史发送失败消息集合
    protected Map<Integer, TerminalMessage> unFinishMsgList = new HashMap<>();//

    private int mposition = -1;
    private int lastPosition = -1;
    private boolean isSameItem = true;
    protected boolean isReject=false;
    private ExecutorService executorService = Executors.newFixedThreadPool(1);


    public BaseMessagePresenter(Context mContext){
        super(mContext);
    }



    public void getMessageFromServer(boolean isGroup,long uniqueNo,int userId,int messageCount,boolean isGetMore){

        long messageId = this.tempGetMessage != null ? this.tempGetMessage.messageId : 0L;
        long messageVersion = this.tempGetMessage != null ? this.tempGetMessage.messageVersion : 0L;
        MyTerminalFactory.getSDK().getThreadPool().execute(() -> {
            long groupUniqueNo = 0L;
            if (isGroup) {
                if (uniqueNo != 0L) {
                    groupUniqueNo = uniqueNo;
                } else {
                    groupUniqueNo = MyTerminalFactory.getSDK().getTerminalMessageManager().getGroupUniqueNo(userId);
                }
            }
         if(isGetMore&&data.size()>0){
             tempPage++;
         }
            MyTerminalFactory.getSDK().getTerminalMessageManager().getHistoryMessageRecord(isGroup, (long)userId, messageId, groupUniqueNo, messageVersion, messageCount);
        });
    }

    public List<TerminalMessage> getData(){
        return data;
    }

    public void registReceiveHandler(){
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverChatListItemClickHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverSendFileCheckMessageHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(getHistoryMessageRecordHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receivePersonMessageNotifyDateHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveNotifyDataMessageHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiverIndividualCallFromMsgItemHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveDownloadProgressHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveDownloadFinishHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveGetHistoryLiveUrlsHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveHiKvisionUrlHandler);
    }

    public void unregistReceiveHandler(){
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverChatListItemClickHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverSendFileCheckMessageHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(getHistoryMessageRecordHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receivePersonMessageNotifyDateHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveNotifyDataMessageHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiverIndividualCallFromMsgItemHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveDownloadProgressHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveDownloadFinishHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveGetHistoryLiveUrlsHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveHiKvisionUrlHandler);
    }

    public void onResume(){
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverReplayIndividualChatVoiceHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveMultimediaMessageCompleteHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveHistoryMultimediaFailHandler);
    }

    public void onPause(){
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverReplayIndividualChatVoiceHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMultimediaMessageCompleteHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveHistoryMultimediaFailHandler);
    }

    private boolean refreshing;
    private int tempPage = 1;
    private GetHistoryMessageRecordHandler getHistoryMessageRecordHandler = messageRecord -> {
        //加上同步，防止更新消息时又来新的消息，导致错乱
        synchronized (BaseMessagePresenter.this) {
            //更新未读消息和聊天界面
            if (messageRecord.isEmpty()) {
                mHandler.post(() -> {
                    if (data.size() != 0) {
                        stopRefreshAndToast("没有更多消息了");
                    }
                    refreshing = false;
                });
            } else {
                //                messageRecord.remove(0);
                setData(messageRecord, true);
            }
            mHandler.post(() -> {
                getView().getLogger().info("GetHistoryMessageRecordHandler"+data.size());
                if (data.size() > 0) {
                    if (tempPage == 1) {
                        getView().setListSelection(data.size() - 1);
                    } else {
                        if (data.size() > messageRecord.size()) {
                            getView().setListSelection(messageRecord.size());
                        }
                    }
                }
                getView().notifyDataSetChanged(data);
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
                getView().stopReFreshing();
            } else {
                stopRefreshAndToast(resultDes);
            }
            refreshing = false;
        } else {

        }
        getView().getLogger().info("消息数量："+data.size());
        getView().notifyDataSetChanged(data);
    };

    private long lastVersion;
    /**
     * 接收消息
     **/
    private ReceiveNotifyDataMessageHandler mReceiveNotifyDataMessageHandler = new ReceiveNotifyDataMessageHandler() {
        @Override
        public void handler(final TerminalMessage terminalMessage) {
            getView().getLogger().info("接收到消息-----》" + terminalMessage.toString());
            mHandler.post(() -> {
                //判断列表中是否存在相同版本号的消息
                if (data.contains(terminalMessage)) {
                    return;
                }
                //                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_PERSONAGE.getCode()) {//个人消息
                //                    newsBarGroupName.setText(HandleIdUtil.handleName(TerminalMessageUtil.getTitleName(terminalMessage)));
                //                }

                if (getView().isGroup()) {//组会话界面
                    if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_PERSONAGE.getCode())//个人消息屏蔽
                    {
                        return;
                    }
                    if (terminalMessage.messageToId != getView().getUserId())//其它组的屏蔽
                    {
                        return;
                    }
                } else {//个人会话界面
                    if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode())//组消息屏蔽
                    {
                        return;
                    }
                    if (terminalMessage.messageFromId == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)) {//自己发的
                        if (terminalMessage.messageToId != getView().getUserId()) {
                            return;
                        }
                    } else {//接收的
                        if (terminalMessage.messageFromId != getView().getUserId())//其它人的屏蔽
                        {
                            return;
                        }
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
                            Iterator<TerminalMessage> iterator = data.iterator();
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
                        Iterator<TerminalMessage> it = data.iterator();
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
                                data.add(terminalMessage);
                                getView().notifyDataSetChanged(data);
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
                        data.add(terminalMessage);
                        getView().notifyDataSetChanged(data);
                        lastVersion = terminalMessage.messageVersion;
                    }

                    //                    Collections.sort(chatMessageList);
                    getView().setListSelection(data.size() - 1);


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
     * 点击个呼条目
     */
    private ReceiverIndividualCallFromMsgItemHandler mReceiverIndividualCallFromMsgItemHandler = () -> {
        goToChooseDevices(ChooseDevicesDialog.TYPE_CALL_PRIVATE);
    };

    /**
     * 点击个呼录音条目
     */
    private ReceiverReplayIndividualChatVoiceHandler mReceiverReplayIndividualChatVoiceHandler = new ReceiverReplayIndividualChatVoiceHandler() {

        @Override
        public void handler(final TerminalMessage terminalMessage, int postion,int type) {
            mposition = postion;
            isReject=false;
            mHandler.post(() -> {
                if (PadApplication.getPadApplication().getIndividualState() == IndividualCallState.IDLE &&
                        PadApplication.getPadApplication().getGroupSpeakState() == GroupCallSpeakState.IDLE &&
                        PadApplication.getPadApplication().getGroupListenenState() == GroupCallListenState.IDLE) {//不是在组呼也不是在个呼中，可以播放录音

                    if (lastPosition == mposition) {//点击同一个条目
                        if (PadApplication.getPadApplication().isPlayVoice) {
                            logger.error("点击同一条目，停止录音，停止动画");
                            if(type == PlayType.PLAY_PRIVATE_CALL.getCode()||type == PlayType.PLAY_GROUP_CALL.getCode()){
                                MyTerminalFactory.getSDK().getTerminalMessageManager().stopMultimediaMessage();
                            }else{
                                MediaManager.release();
                            }
                            PadApplication.getPadApplication().isPlayVoice = false;
                            isSameItem = true;
                            getView().refreshPersonContactsAdapter(mposition,data,PadApplication.getPadApplication().isPlayVoice,isSameItem);
//                            temporaryAdapter.notifyDataSetChanged();
                        } else {
                            executorService.execute(() -> {
                                if (mposition < data.size() && mposition >= 0) {
                                    try {
                                        if(type == PlayType.PLAY_PRIVATE_CALL.getCode()||type == PlayType.PLAY_GROUP_CALL.getCode()){
                                            mHandler.post(() -> {
                                                terminalMessage.isDownLoadAudio = true;
                                                getView().notifyItemChanged(postion);
                                            });
                                            MyTerminalFactory.getSDK().getTerminalMessageManager().playMultimediaMessage(data.get(mposition), audioPlayComplateHandler);
                                        }else{
                                            downloadRecordFileOrPlay(terminalMessage, onCompletionListener);
                                        }
                                    } catch (IndexOutOfBoundsException e) {
                                        logger.warn("mPosition出现异常，其中mposition=" + mposition + "，mTerminalMessageList.size()=" + data.size(), e);
                                    }
                                }
                            });
                        }
                    } else {//点击不同条目
                        if (PadApplication.getPadApplication().isPlayVoice) {
                            MyTerminalFactory.getSDK().getTerminalMessageManager().stopMultimediaMessage();
                            MediaManager.release();
                        }
                        //播放当前的
                        executorService.execute(() -> {
                            if (mposition < data.size() && mposition >= 0) {
                                try {
                                    if(type == PlayType.PLAY_PRIVATE_CALL.getCode()||type == PlayType.PLAY_GROUP_CALL.getCode()){
                                        mHandler.post(() -> {
                                            terminalMessage.isDownLoadAudio = true;
                                            getView().notifyItemChanged(postion);
                                        });
                                        MyTerminalFactory.getSDK().getTerminalMessageManager().playMultimediaMessage(data.get(mposition), audioPlayComplateHandler);
                                    }else{
                                        downloadRecordFileOrPlay(terminalMessage, onCompletionListener);
                                    }
                                } catch (IndexOutOfBoundsException e) {
                                    logger.warn("mPosition出现异常，其中mposition=" + mposition + "，mTerminalMessageList.size()=" + data.size(), e);
                                }
                            }
                        });
                    }
                } else {
                    getView().showMsg(R.string.text_can_not_play_audio_now);
                }
            });
        }
    };

    /**
     * 开始播放或停止播放的回调
     */
    private ReceiveMultimediaMessageCompleteHandler receiveMultimediaMessageCompleteHandler = (resultCode, resultDes, message) -> {
        logger.info("ReceiveMultimediaMessageCompleteHandler   "+resultCode+"/"+resultDes);
        mHandler.post(() -> {
            if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                if (lastPosition == mposition) {//点击同一个条目
                    isSameItem = true;
                    PadApplication.getPadApplication().isPlayVoice = !PadApplication.getPadApplication().isPlayVoice;
                } else {//点击不同条目
                    isSameItem = false;
                    PadApplication.getPadApplication().isPlayVoice = true;
                }
                if(message!=null&&message.messageId!=0){
                    message.isDownLoadAudio = false;
                }
                Collections.sort(data);
                getView().refreshPersonContactsAdapter(mposition,data,PadApplication.getPadApplication().isPlayVoice,isSameItem);
                lastPosition = mposition;
            } else {
                logger.info("开始播放或停止播放的回调" + resultDes);
                getView().showMsg(resultDes);
                if(message!=null){
                    message.isDownLoadAudio = false;
                    getView().notifyDataSetChanged();
                }
            }
        });
    };

    /**
     * 音频播放失败
     **/
    private ReceiveHistoryMultimediaFailHandler receiveHistoryMultimediaFailHandler = resultCode -> {
        if (resultCode == TerminalErrorCode.STOP_PLAY_RECORD.getErrorCode()) {
            PadApplication.getPadApplication().isPlayVoice = false;
            isSameItem = true;
            getView().refreshPersonContactsAdapter(mposition, data, false, true);
//            temporaryAdapter.notifyDataSetChanged();
        } else {
            logger.info("音频播放失败了！！errorCode=" + resultCode);
            getView().showMsg(R.string.text_play_recorder_fail_has_no_get_recorder_data_please_try_later);
        }
    };

    /***  下载进度更新 **/
    private ReceiveDownloadProgressHandler mReceiveDownloadProgressHandler = new ReceiveDownloadProgressHandler() {

        @Override
        public void handler(final float percent, TerminalMessage terminalMessage) {
            mHandler.post(() -> {
                getView().downloadProgress(percent,terminalMessage);
            });
        }
    };

    /**
     * 文件等下载完成的监听handler
     */
    private ReceiveDownloadFinishHandler mReceiveDownloadFinishHandler = new ReceiveDownloadFinishHandler() {
        @Override
        public void handler(final TerminalMessage terminalMessage, final boolean success) {
            mHandler.post(() -> {
                getView().downloadFinish(terminalMessage,success);
            });
        }
    };



    /**
     * 会话界面列表条目点击事件
     **/

    private ReceiverChatListItemClickHandler mReceiverChatListItemClickHandler = new ReceiverChatListItemClickHandler() {
        @Override
        public void handler(final TerminalMessage terminalMessage, boolean isReceiver) {
            getView().chatListItemClick(terminalMessage,isReceiver);
        }
    };

    /**
     * 选择相片、打开相机、选择文件、发送位置、上报图像、请求上报图像
     */
    public ReceiverSendFileCheckMessageHandler mReceiverSendFileCheckMessageHandler = new ReceiverSendFileCheckMessageHandler() {
        @Override
        public void handler(int msgType, final boolean showOrHidden, int userId) {
            Observable.just(msgType)
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(msgType1 -> {
//                        if (showOrHidden) {
                            switch (msgType1) {
                                case ReceiverSendFileCheckMessageHandler.PHOTO_ALBUM://从相册中选择相片
//                                    if (ContextCompat.checkSelfPermission(ChatBaseActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                                        ActivityCompat.requestPermissions(ChatBaseActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSIONS_REQUEST_CODE);
//                                    } else {
//                                        setViewVisibility(fl_fragment_container, View.VISIBLE);
//                                        getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fl_fragment_container, new ImageSelectorFragment()).commit();
//                                    }
                                    break;
                                case ReceiverSendFileCheckMessageHandler.CAMERA://调用相机拍照
//                                    autoObtainCameraPermission();
                                    break;
                                case ReceiverSendFileCheckMessageHandler.FILE://选取文件
//                                    if (ContextCompat.checkSelfPermission(ChatBaseActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                                        ActivityCompat.requestPermissions(ChatBaseActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSIONS_REQUEST_CODE);
//                                    } else {
//                                        setViewVisibility(fl_fragment_container, View.VISIBLE);
//                                        getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fl_fragment_container, new FileMainFragment()).commit();
//                                    }
                                    break;
                                case ReceiverSendFileCheckMessageHandler.POST_BACK_VIDEO://上报图像
//                                    postVideo();
                                    break;
                                case ReceiverSendFileCheckMessageHandler.REQUEST_VIDEO://请求图像
                                    requestVideo();
                                    break;
                                case ReceiverSendFileCheckMessageHandler.NFC://NFC
//                                    checkNFC(userId,true);
                                    break;
                                case ReceiverSendFileCheckMessageHandler.QR_CODE://二维码
//                                    showQRDialog();
//                                    goToScan();
                                    break;
                            }
//                        } else {
//                            setViewVisibility(fl_fragment_container, View.GONE);
//                        }
                    });
        }
    };

    /**
     * 获取历史上报图像列表
     */
    private ReceiveGetHistoryLiveUrlsHandler mReceiveGetHistoryLiveUrlsHandler = new ReceiveGetHistoryLiveUrlsHandler() {
        @Override
        public void handler(int code,List<String> liveUrl,String name,int memberId) {
            mHandler.post(() -> {
               if(code == 0){
                   MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveGoWatchLiveHandler.class,liveUrl,name,memberId);
               }else{
                   getView().showMsg(R.string.text_get_video_info_fail);
               }
            });
        }
    };

    /**
     * 收到OutGB28181的播放地址
     **/
    private ReceiveHiKvisionUrlHandler receiveHiKvisionUrlHandler = (success, result,deviceId) -> {
            if(success){
                List<String> liveUrls = new ArrayList<>();
                liveUrls.add(result);
                MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveGetHistoryLiveUrlsHandler.class, BaseCommonCode.SUCCESS_CODE,liveUrls,deviceId,0);
            }else{
                ToastUtil.showToast(MyTerminalFactory.getSDK().application,result);
            }
    };


    /**
     * 选择设备进行相应的操作
     * @param type
     */
    public void goToChooseDevices(int type){
        mHandler.post(() -> getView().showProgressDialog());
        TerminalFactory.getSDK().getThreadPool().execute(() -> {
            Account account = DataUtil.getAccountByMemberNo(getView().getUserId(),true);
            mHandler.post(() -> {
                getView().dismissProgressDialog();
                if(account == null){
                    getView().showMsg(R.string.text_has_no_found_this_user);
                    return;
                }
                getView().chooseDevicesDialog(type,account);
            });
        });
    }

    /**
     * 请求个呼
     */
    public void activeIndividualCall(Member member) {
        PadApplication.getPadApplication().isCallState = true;
        boolean network = MyTerminalFactory.getSDK().hasNetwork();
        if (network) {
            if(member!=null){
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCurrentGroupIndividualCallHandler.class, member);
            }else {
                getView().showMsg(R.string.text_get_member_info_fail);
            }
        } else {
            getView().showMsg(R.string.text_network_connection_abnormal_please_check_the_network);
        }
    }

    /**
     * 拨打电话
     */
    public void goToCall(Member member) {
        if(android.text.TextUtils.isEmpty(member.getPhone())){
            ToastUtils.showShort(R.string.text_has_no_member_phone_number);
            return;
        }
        if(member.getUniqueNo() == 0){
            //普通电话
            getView().callPhone(member.getPhone());
        }else{
            if(MyTerminalFactory.getSDK().getParam(Params.VOIP_SUCCESS,false)){
                getView().goToVoIpActivity(member);
            }else {
                getView().showMsg(R.string.text_voip_regist_fail_please_check_server_configure);
            }
        }
    }

    /**
     * 请求图像
     * @param member
     */
    public void goToPullLive(Member member) {
        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverRequestVideoHandler.class, member);
    }


    /**
     * 上报图像
     * @param member
     */
    public void goToPushLive(Member member) {
        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverActivePushVideoHandler.class,
                MyDataUtil.getPushInviteMemberData(member.getUniqueNo(), ReceiveObjectMode.MEMBER.toString()),false);
    }



    private boolean isContainMessage(TerminalMessage terminalMessage) {
        for (TerminalMessage mTerminalMessage : data) {
            if (terminalMessage.messageVersion == mTerminalMessage.messageVersion) {
                return true;
            }
        }
        return false;
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

    /**
     * 停止刷新
     */
    private void stopRefreshAndToast(final String messge) {
        mHandler.post(() -> {
            getView().stopReFreshing();
            ToastUtils.showShort(messge);
        });
    }

    /**
     * 设置数据
     *
     * @param groupMessageRecord
     */
    private void setData(List<TerminalMessage> groupMessageRecord, boolean isStopRefresh) {
        if (!getView().isGroup()){
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
        groupMessageRecord2.addAll(data);
        stopRefresh(groupMessageRecord, groupMessageRecord2, groupMessageRecord.size(), isStopRefresh);
        tempGetMessage = groupMessageRecord.get(0);
        if (isStopRefresh) {
            refreshing = false;
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

    /**
     * 停止刷新
     */
    private void stopRefresh(final List<TerminalMessage> groupMessageRecord1, final List<TerminalMessage> groupMessageRecord2, final int position, boolean isStopRefresh) {
        mHandler.post(() -> {
            data.clear();
            data.addAll(groupMessageRecord1);
            data.addAll(groupMessageRecord2);

            getView().notifyItemRangeInserted(0, position);

            if (tempPage == 1) {
                getView().smoothScrollBy(0,0);
            } else {
                getView().smoothScrollBy(0, -DensityUtil.dip2px(getView().getContext(),30));
            }
            //            groupCallList.scrollBy(0, -DensityUtil.dip2px(ChatBaseActivity.this, 30));
            if (isStopRefresh) {
                getView().stopReFreshing();
            }
        });
    }


    /**
     * 录音播放完成的消息
     */
    private IAudioPlayComplateHandler audioPlayComplateHandler = new IAudioPlayComplateHandler() {
        @Override
        public void handle() {
            mHandler.post(() -> {
                PadApplication.getPadApplication().isPlayVoice = false;
                isSameItem = true;
//                    logger.error("录音播放完成的消息：" + chatMessageList.get(mposition).toString());
                getView().refreshPersonContactsAdapter(mposition, data, PadApplication.getPadApplication().isPlayVoice, isSameItem);
                getView().setSmoothScrollToPosition(mposition);
                getView().notifyDataSetChanged();
            });
        }
    };

    /**
     * 录音播放完成的消息
     */
    private MediaPlayer.OnCompletionListener onCompletionListener = mediaPlayer -> {
        mHandler.post(() -> {
            //                    logger.error("播放完成的回调触发了========> "+lastPosition+"/"+mposition+"/"+chatMessageList.size());
            PadApplication.getPadApplication().isPlayVoice = false;
            isSameItem = true;
            data.get(mposition).messageBody.put(JsonParam.UNREAD, false);
            getView().refreshPersonContactsAdapter(mposition, data, PadApplication.getPadApplication().isPlayVoice, isSameItem);
//            temporaryAdapter.notifyDataSetChanged();
        });
        autoPlay(mposition+1);
    };

    //自动播放下一条语音
    private void autoPlay(int index){
        if(index<data.size()){//不是最后一条消息，自动播放
            //不是语音消息跳过执行下一条
            if (data.get(index).messageType != MessageType.AUDIO.getCode()&&!isReject) {
                index = index + 1;
                autoPlay(index);
            }else {
                if (data.get(index).messageBody.containsKey(JsonParam.UNREAD) &&
                        data.get(index).messageBody.getBooleanValue(JsonParam.UNREAD) && !MediaManager.isPlaying() && !isReject) {
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverReplayIndividualChatVoiceHandler.class, data.get(index), index, PlayType.PLAY_AUDIO.getCode());
                } else {
                    logger.error("点击消息以下的未读消息已播放完成");
                }
            }
        }else {
            logger.debug("最后一条消息已播放完成");
        }
    }

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
                    Request request = new Request.Builder()
                            .url(terminalMessage.messagePath)
                            .build();
                    mOkHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
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
                                resultDes[0] = getContext().getString(R.string.text_get_audio_success_ready_to_play);
                                logger.info("URL获得音频文件，播放录音");
                                MediaManager.playSound(terminalMessage.messagePath, onCompletionListener);
                            } else {
                                resultCode[0] = TerminalErrorCode.SERVER_NOT_RESPONSE.getErrorCode();
                                resultDes[0] = getContext().getString(R.string.text_audio_history_not_find_or_server_no_response);
                                logger.error("历史音频未找到，或服务器无响应！");
                            }
                            TerminalFactory.getSDK().notifyReceiveHandler(ReceiveMultimediaMessageCompleteHandler.class, resultCode[0], resultDes[0],terminalMessage);
                        }
                    });
                } else {
                    resultCode[0] = BaseCommonCode.SUCCESS_CODE;
                    resultDes[0] = getContext().getString(R.string.text_get_audio_success_from_local_ready_to_play);
                    logger.info("从本地音频文件，播放录音");
                    MediaManager.playSound(terminalMessage.messagePath, onCompletionListener);
                    TerminalFactory.getSDK().notifyReceiveHandler(ReceiveMultimediaMessageCompleteHandler.class, resultCode[0], resultDes[0],terminalMessage);
                }
            } else {
                resultCode[0] = TerminalErrorCode.PARAMETER_ERROR.getErrorCode();
                resultDes[0] = getContext().getString(R.string.text_get_audio_history_paramter_wrongful);
                logger.error("获取历史音频参数不合法！");
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiveMultimediaMessageCompleteHandler.class, resultCode[0], resultDes[0],terminalMessage);
            }
        } catch (Exception e) {
            resultDes[0] = getContext().getString(R.string.text_get_audio_messge_fail);
            resultCode[0] = TerminalErrorCode.PARAMETER_ERROR.getErrorCode();
            logger.error("获得语音消息失败！", e);
            TerminalFactory.getSDK().notifyReceiveHandler(ReceiveMultimediaMessageCompleteHandler.class, resultCode[0], resultDes[0],terminalMessage);
        }
    }

    /**
     * 将列表中的terminalMessage替换成新的terminalMessage
     **/
    public void replaceMessage(TerminalMessage newTerminalMessage){
        boolean has = false;
        Iterator<TerminalMessage> it = data.iterator();
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
            data.add(newTerminalMessage);
        }
        Collections.sort(data);
        getView().notifyDataSetChanged();
        if (!has) {
            getView().setListSelection(data.size() - 1);
            lastVersion = newTerminalMessage.messageVersion;
        }
    }

    public void requestVideo() {
//        if(type == 0){
            goToChooseDevices(ChooseDevicesDialog.TYPE_PULL_LIVE);
//        }else {
//            showProgressDialog();
//            TerminalFactory.getSDK().getThreadPool().execute(()->{
//                Account account = DataUtil.getAccountByMemberNo(userId,true);
//                myHandler.post(this::dismissProgressDialog);
//                Member member = null;
//                if(account != null){
//                    List<Member> members = account.getMembers();
//                    for(Member next : members){
//                        if(next.getType() == type){
//                            member = next;
//                            break;
//                        }
//                    }
//                    if(member !=null){
//                        goToPullLive(member);
//                    }
//                }
//            });
//
//        }
    }

}

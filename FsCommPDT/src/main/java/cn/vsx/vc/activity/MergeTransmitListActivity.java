package cn.vsx.vc.activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.zectec.imageandfileselector.bean.ImageBean;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.TerminalErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.audio.IAudioPlayComplateHandler;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallState;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.GetMessagesByIdsHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveDownloadFinishHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveDownloadProgressHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHistoryMultimediaFailHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMultimediaMessageCompleteHandler;
import cn.vsx.hamster.terminalsdk.tools.MessageComparator;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.MergeTransmitListAdapter;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.model.PlayType;
import cn.vsx.vc.receiveHandle.OnBackListener;
import cn.vsx.vc.receiveHandle.ReceiverReplayGroupMergeTransmitVoiceHandler;
import cn.vsx.vc.record.MediaManager;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.ToastUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import ptt.terminalsdk.context.MyTerminalFactory;

public class MergeTransmitListActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, BaseQuickAdapter.RequestLoadMoreListener, View.OnClickListener {


    TextView barTitle;

    SwipeRefreshLayout layoutSrl;

    RecyclerView contentView;

    FrameLayout fl_fragment_container;

    private MergeTransmitListAdapter adapter;


    private TerminalMessage terminalMessage;
    private String messageIds;
    private boolean isGroup;
    protected int userId;
    protected List<TerminalMessage> chatMessageList = new ArrayList<>();

    private int mposition = -1;
    private int lastPosition = -1;
    private boolean isSameItem = true;
    private ExecutorService executorService = Executors.newFixedThreadPool(1);

    //当前页数
    private int mPage = 1;
    //每页显示条数
    private static final int mPageSize = 10;

    private MessageComparator messageComparator = new MessageComparator();

    protected Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            }
        }
    };

    @Override
    public int getLayoutResId() {
        return R.layout.activity_merge_transmit_list;
    }

    @Override
    public void initView() {
        fl_fragment_container = (FrameLayout) findViewById(R.id.fl_fragment_container);
        contentView = (RecyclerView) findViewById(R.id.contentView);
        layoutSrl = (SwipeRefreshLayout) findViewById(R.id.layout_srl);
        barTitle = (TextView) findViewById(R.id.bar_title);
        Intent intent = getIntent();
        findViewById(R.id.news_bar_back).setOnClickListener(this);
        terminalMessage = (TerminalMessage) intent.getSerializableExtra(Constants.TERMINALMESSAGE);
        isGroup = getIntent().getBooleanExtra(Constants.IS_GROUP, false);
        userId = getIntent().getIntExtra(Constants.USER_ID, 0);
        if (terminalMessage == null || (terminalMessage != null && terminalMessage.messageBody == null)) {
            finish();
            return;
        }

        JSONObject jsonObject = terminalMessage.messageBody;
        messageIds = getMessageIds(jsonObject);

        barTitle.setText(jsonObject.containsKey(JsonParam.CONTENT) ? jsonObject.getString(JsonParam.CONTENT) : "消息记录");

        adapter = new MergeTransmitListAdapter(chatMessageList, this, isGroup, userId);
        adapter.setFragment_contener(fl_fragment_container);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        contentView.setLayoutManager(linearLayoutManager);
        contentView.setAdapter(adapter);
        layoutSrl.setColorSchemeResources(R.color.colorPrimary);
        layoutSrl.setProgressViewOffset(false, 0, (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources()
                        .getDisplayMetrics()));
        layoutSrl.setOnRefreshListener(this);

    }

    @Override
    public void initListener() {
        MyTerminalFactory.getSDK().registReceiveHandler(getMessagesByIdsHandler);
    }

    @Override
    public void initData() {
        loadData(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveDownloadProgressHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveDownloadFinishHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveMultimediaMessageCompleteHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveHistoryMultimediaFailHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverReplayGroupMergeTransmitVoiceHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(getMessagesByIdsHandler);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveDownloadProgressHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveDownloadFinishHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMultimediaMessageCompleteHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveHistoryMultimediaFailHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverReplayGroupMergeTransmitVoiceHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(getMessagesByIdsHandler);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopRecord();
    }

    @Override
    public void doOtherDestroy() {
        handler.removeCallbacksAndMessages(null);
    }


    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.news_bar_back) {
            finish();
        }
    }


    @Override
    public void onRefresh() {
        mPage = 1;
        contentView.scrollToPosition(0);
        loadData(true);
    }

    @Override
    public void onLoadMoreRequested() {
        mPage++;
        loadData(false);
    }

    /**
     * 加载数据
     *
     * @param isRefresh
     */
    private void loadData(boolean isRefresh) {
        TerminalFactory.getSDK().getThreadPool().execute(() -> {
            MyTerminalFactory.getSDK().getTerminalMessageManager().getMessageListByIds(messageIds);
        });
    }

    /**
     * 获取消息列表
     */
    private GetMessagesByIdsHandler getMessagesByIdsHandler = (resultCode, resultDesc, memberList) -> {
        handler.post(() -> layoutSrl.setRefreshing(false));
        if (resultCode == BaseCommonCode.SUCCESS_CODE && !memberList.isEmpty()) {
            chatMessageList.clear();
            setMessagePath(memberList);
            chatMessageList.addAll(memberList);
            Collections.sort(chatMessageList, messageComparator);
            handler.post(() -> adapter.notifyDataSetChanged());
        } else {
            if (mPage > 1) {
                mPage = mPage - 1;
            }
            handler.post(() -> ToastUtil.showToast(MergeTransmitListActivity.this, resultDesc));
        }
    };

    /**
     * 点击消息的组呼条目，播放组呼录音
     **/
    private ReceiverReplayGroupMergeTransmitVoiceHandler mReceiverReplayGroupMergeTransmitVoiceHandler = new ReceiverReplayGroupMergeTransmitVoiceHandler() {
        @Override
        public void handler(final TerminalMessage terminalMessage, int postion, int type) {
            mposition = postion;
            handler.post(() -> {
                if (MyApplication.instance.getIndividualState() == IndividualCallState.IDLE &&
                        MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.IDLE &&
                        MyApplication.instance.getGroupListenenState() == GroupCallListenState.IDLE) {//不是在组呼也不是在个呼中，可以播放录音

                    if (lastPosition == mposition) {//点击同一个条目
                        if (MyApplication.instance.isPlayVoice) {
                            MyTerminalFactory.getSDK().getTerminalMessageManager().stopMultimediaMessage();
                            if (type == PlayType.PLAY_PRIVATE_CALL.getCode() || type == PlayType.PLAY_GROUP_CALL.getCode()) {
                                MyTerminalFactory.getSDK().getTerminalMessageManager().stopMultimediaMessage();
                            } else {
                                MediaManager.release();
                            }
                            MyApplication.instance.isPlayVoice = false;
                            isSameItem = true;
                            adapter.refreshPersonContactsAdapter(mposition, chatMessageList, MyApplication.instance.isPlayVoice, isSameItem);
                        } else {
                            executorService.execute(() -> {
                                if (mposition < chatMessageList.size() && mposition >= 0) {
                                    try {
                                        if (type == PlayType.PLAY_PRIVATE_CALL.getCode() || type == PlayType.PLAY_GROUP_CALL.getCode()) {
                                            handler.post(() -> {
                                                terminalMessage.isDownLoadAudio = true;
                                                adapter.notifyItemChanged(postion);
                                            });
                                            MyTerminalFactory.getSDK().getTerminalMessageManager().playMultimediaMessage(chatMessageList.get(mposition), audioPlayComplateHandler);
                                        } else {
                                            downloadRecordFileOrPlay(terminalMessage, onCompletionListener);
                                        }
                                    } catch (IndexOutOfBoundsException e) {
                                        logger.warn("mPosition出现异常，其中mposition=" + mposition + "，mTerminalMessageList.size()=" + chatMessageList.size(), e);
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
                        executorService.execute(() -> {
                            if (mposition < chatMessageList.size() && mposition >= 0) {
                                try {
                                    if (type == PlayType.PLAY_PRIVATE_CALL.getCode() || type == PlayType.PLAY_GROUP_CALL.getCode()) {
                                        handler.post(() -> {
                                            terminalMessage.isDownLoadAudio = true;
                                            adapter.notifyItemChanged(postion);
                                        });
                                        MyTerminalFactory.getSDK().getTerminalMessageManager().playMultimediaMessage(chatMessageList.get(mposition), audioPlayComplateHandler);
                                    } else {
                                        downloadRecordFileOrPlay(terminalMessage, onCompletionListener);
                                    }
                                    MyTerminalFactory.getSDK().getTerminalMessageManager().playMultimediaMessage(chatMessageList.get(mposition), audioPlayComplateHandler);
                                } catch (IndexOutOfBoundsException e) {
                                    logger.warn("mPosition出现异常，其中mposition=" + mposition + "，mTerminalMessageList.size()=" + chatMessageList.size(), e);
                                }
                            }
                        });
                    }

                } else {
                    ToastUtil.showToast(MergeTransmitListActivity.this, getString(R.string.text_can_not_play_recording_now));
                }
            });
        }
    };

    /**
     * 下载录音文件并播放
     */
    public void downloadRecordFileOrPlay(final TerminalMessage terminalMessage, final MediaPlayer.OnCompletionListener onCompletionListener) {
        TerminalFactory.getSDK().getThreadPool().execute(() -> {
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
//                        url = TerminalFactory.getSDK().getServiceBusManager().getUrl(url);
                        Request request = new Request.Builder()
                                .url(url)
                                .build();
                        mOkHttpClient.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
//                                TerminalFactory.getSDK().getServiceBusManager().addErrorCount();
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
                                TerminalFactory.getSDK().notifyReceiveHandler(ReceiveMultimediaMessageCompleteHandler.class, resultCode[0], resultDes[0], terminalMessage);
                            }
                        });
                    } else {
                        resultCode[0] = BaseCommonCode.SUCCESS_CODE;
                        resultDes[0] = getString(R.string.text_get_audio_success_from_local_ready_to_play);
                        logger.info("从本地音频文件，播放录音");
                        MediaManager.playSound(terminalMessage.messagePath, onCompletionListener);
                        TerminalFactory.getSDK().notifyReceiveHandler(ReceiveMultimediaMessageCompleteHandler.class, resultCode[0], resultDes[0], terminalMessage);
                    }
                } else {
                    resultCode[0] = TerminalErrorCode.PARAMETER_ERROR.getErrorCode();
                    resultDes[0] = getString(R.string.text_get_audio_history_paramter_wrongful);
                    logger.error("获取历史音频参数不合法！");
                    TerminalFactory.getSDK().notifyReceiveHandler(ReceiveMultimediaMessageCompleteHandler.class, resultCode[0], resultDes[0], terminalMessage);
                }
            } catch (Exception e) {
                resultDes[0] = getString(R.string.text_get_audio_messge_fail);
                resultCode[0] = TerminalErrorCode.PARAMETER_ERROR.getErrorCode();
                logger.error("获得语音消息失败！", e);
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiveMultimediaMessageCompleteHandler.class, resultCode[0], resultDes[0], terminalMessage);
            }

        });
    }

    /**
     * 开始播放或停止播放的回调
     */
    private ReceiveMultimediaMessageCompleteHandler receiveMultimediaMessageCompleteHandler = (resultCode, resultDes, message) -> {
        logger.info("ReceiveMultimediaMessageCompleteHandler   " + resultCode + "/" + resultDes);
        handler.post(() -> {
            if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                if (lastPosition == mposition) {//点击同一个条目
                    isSameItem = true;
//                    MyApplication.instance.isPlayVoice = !MyApplication.instance.isPlayVoice;
                } else {//点击不同条目
                    isSameItem = false;
                }
                MyApplication.instance.isPlayVoice = true;
                Collections.sort(chatMessageList, messageComparator);
                if (adapter != null) {
                    adapter.refreshPersonContactsAdapter(mposition, chatMessageList, MyApplication.instance.isPlayVoice, isSameItem);
                }
                lastPosition = mposition;
            } else {
                logger.info("开始播放或停止播放的回调" + resultDes);
                ToastUtil.showToast(MergeTransmitListActivity.this, resultDes);
            }
        });
    };


    /***  下载进度更新 **/
    private ReceiveDownloadProgressHandler mReceiveDownloadProgressHandler = new ReceiveDownloadProgressHandler() {

        @Override
        public void handler(final float percent, TerminalMessage terminalMessage) {
            handler.post(() -> {
                if (!checkMessageIsHave(terminalMessage)) {
                    return;
                }
                if (terminalMessage.messageType == MessageType.VIDEO_CLIPS.getCode()) {
                    if (null != adapter.loadingView) {
                        int percentInt = (int) (percent * 100);
                        if (percentInt >= 100) {
                            setViewVisibility(adapter.loadingView, View.GONE);
                            adapter.loadingView = null;
                        } else {
                            setViewVisibility(adapter.loadingView, View.VISIBLE);
                            adapter.loadingView.setProgerss(percentInt);
                        }
                    }
                } else {
                    if (adapter.downloadProgressBar != null
                            && adapter.download_tv_progressBars != null) {
                        int percentInt = (int) (percent * 100);
                        adapter.downloadProgressBar.setProgress(percentInt);
                        setText(adapter.download_tv_progressBars, percentInt + "%");

                        if (percentInt >= 100) {
                            setViewVisibility(adapter.downloadProgressBar, View.GONE);
                            setViewVisibility(adapter.download_tv_progressBars, View.GONE);
                            adapter.downloadProgressBar = null;
                            adapter.download_tv_progressBars = null;
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
                if (!checkMessageIsHave(terminalMessage)) {
                    return;
                }
                if (!success) {
                    return;
                }

                if (terminalMessage.messageType == MessageType.LONG_TEXT.getCode()
                        || terminalMessage.messageType == MessageType.HYPERLINK.getCode()) {
                    replaceMessage(terminalMessage);
                }
                if (terminalMessage.messageType == MessageType.PICTURE.getCode()) {
                    adapter.isDownloadingPicture = false;
                    replaceMessage(terminalMessage);
                    //如果是原图下载完了就打开
                    if (terminalMessage.messageBody.containsKey(JsonParam.ISMICROPICTURE) &&
                            !terminalMessage.messageBody.getBooleanValue(JsonParam.ISMICROPICTURE)) {
                        adapter.openPhotoAfterDownload(terminalMessage);
                    }
                }

                if (terminalMessage.messageType == MessageType.FILE.getCode()) {
                    adapter.openFileAfterDownload(terminalMessage);
                    adapter.isDownloading = false;
                    terminalMessage.messageBody.put(JsonParam.IS_DOWNLOADINF, false);
                    replaceMessage(terminalMessage);
                }
                if (terminalMessage.messageType == MessageType.AUDIO.getCode()) {
                    adapter.isDownloading = false;
                    terminalMessage.messageBody.put(JsonParam.IS_DOWNLOADINF, false);
                    replaceMessage(terminalMessage);
                }
                if (terminalMessage.messageType == MessageType.VIDEO_CLIPS.getCode()) {
                    File file = new File(terminalMessage.messagePath);
                    adapter.openVideo(terminalMessage, file);
                    adapter.isDownloading = false;
                    terminalMessage.messageBody.put(JsonParam.IS_DOWNLOADINF, false);
                    replaceMessage(terminalMessage);
                }
            });
        }
    };


    /**
     * 将列表中的terminalMessage替换成新的terminalMessage
     **/
    private void replaceMessage(TerminalMessage newTerminalMessage) {
//        Iterator<TerminalMessage> it = chatMessageList.iterator();
//        while (it.hasNext()) {
//            TerminalMessage next = it.next();
//            if (next.messageBody.containsKey(JsonParam.TOKEN_ID) && newTerminalMessage.messageBody.containsKey(JsonParam.TOKEN_ID) &&
//                    next.messageBody.getIntValue(JsonParam.TOKEN_ID) == newTerminalMessage.messageBody.getIntValue(JsonParam.TOKEN_ID)) {
//            }
//        }
        if (chatMessageList.contains(newTerminalMessage)) {
            int index = chatMessageList.indexOf(newTerminalMessage);
            TerminalMessage message = chatMessageList.get(index);
            chatMessageList.set(index,newTerminalMessage);

//            Collections.replaceAll(chatMessageList, message, newTerminalMessage);
        }

        Collections.sort(chatMessageList, messageComparator);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
//        if (!has) {
//            if(contentView !=null){
//                contentView.scrollToPosition(chatMessageList.size() - 1);
//            }
//        }
    }

    /**
     * 音频播放失败
     **/
    private ReceiveHistoryMultimediaFailHandler receiveHistoryMultimediaFailHandler = resultCode -> {
        if (resultCode == TerminalErrorCode.STOP_PLAY_RECORD.getErrorCode()) {
            MyApplication.instance.isPlayVoice = false;
            isSameItem = true;
            if (adapter != null) {
                adapter.refreshPersonContactsAdapter(mposition, chatMessageList, false, true);
            }
//            temporaryAdapter.notifyDataSetChanged();
        } else {
            logger.info("音频播放失败了！！errorCode=" + resultCode);
            ToastUtil.showToast(MergeTransmitListActivity.this, getString(R.string.text_play_recorder_fail_has_no_get_recorder_data_please_try_later));
        }
    };

    /**
     * 录音播放完成的消息
     */
    private IAudioPlayComplateHandler audioPlayComplateHandler = () -> handler.post(() -> {
        MyApplication.instance.isPlayVoice = false;
        isSameItem = true;
        chatMessageList.get(mposition).messageBody.put(JsonParam.UNREAD, false);
        if (adapter != null) {
            adapter.refreshPersonContactsAdapter(mposition, chatMessageList, MyApplication.instance.isPlayVoice, isSameItem);
        }
        setSmoothScrollToPosition(mposition);
        autoPlay(mposition + 1);
    });

    /**
     * 录音播放完成的消息
     */
    private MediaPlayer.OnCompletionListener onCompletionListener = mediaPlayer -> {
        handler.post(() -> {
            //                    logger.error("播放完成的回调触发了========> "+lastPosition+"/"+mposition+"/"+chatMessageList.size());
            MyApplication.instance.isPlayVoice = false;
            isSameItem = true;
            chatMessageList.get(mposition).messageBody.put(JsonParam.UNREAD, false);
            adapter.refreshPersonContactsAdapter(mposition, chatMessageList, MyApplication.instance.isPlayVoice, isSameItem);
//            temporaryAdapter.notifyDataSetChanged();
        });
        autoPlay(mposition + 1);
    };

    //自动播放下一条语音
    private void autoPlay(int index) {
//        logger.debug("自动播放:第"+index+"条");
        //不是最后一条消息，自动播放
        if (index < chatMessageList.size()) {
            //不是语音消息跳过执行下一条
            if (chatMessageList.get(index).messageType != MessageType.AUDIO.getCode() && chatMessageList.get(index).messageType != MessageType.GROUP_CALL.getCode()) {
                index = index + 1;
                autoPlay(index);
            } else {
                if (chatMessageList.get(index).messageBody.containsKey(JsonParam.UNREAD) &&
                        chatMessageList.get(index).messageBody.getBooleanValue(JsonParam.UNREAD)
                        && MyTerminalFactory.getSDK().getParam(Params.IS_PLAY_END, false)) {
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverReplayGroupMergeTransmitVoiceHandler.class, index,
                            (chatMessageList.get(index).messageType == MessageType.AUDIO.getCode())?PlayType.PLAY_AUDIO.getCode():PlayType.PLAY_GROUP_CALL.getCode());
                }
            }
        } else {
            logger.debug("最后一条消息已播放完成");
        }
    }

    public void setSmoothScrollToPosition(int position) {
        if (contentView != null) {
            contentView.smoothScrollToPosition(position);
        }
    }

    /**
     * 获取消息id的字符串
     *
     * @param jsonObject
     * @return
     */
    private String getMessageIds(JSONObject jsonObject) {
        StringBuffer ids = new StringBuffer();
        if (jsonObject.containsKey(JsonParam.MESSAGE_ID_LIST)) {
            JSONArray jsonArray = jsonObject.getJSONArray(JsonParam.MESSAGE_ID_LIST);
            if (jsonArray != null && jsonArray.size() > 0) {
                int size = jsonArray.size();
                for (int i = 0; i < size; i++) {
                    ids.append(String.valueOf(jsonArray.get(i)));
                    if (i != (size - 1)) {
                        ids.append(",");
                    }
                }
            }
        }
        return ids.toString();
    }

    /**
     * 设置messagePath
     *
     * @param memberList
     */
    private void setMessagePath(List<TerminalMessage> memberList) {
        for (TerminalMessage message : memberList) {
//            TerminalFactory.getSDK().getTerminalMessageManager().setMessagePath(message, true);
//            TerminalFactory.getSDK().getThreadPool().execute(() -> {
                TerminalFactory.getSDK().getTerminalMessageManager().downloadOrSetMessage(message, false);
//            });
        }
    }

    /**
     * 检查该消息是否存在
     *
     * @param terminalMessage
     * @return
     */
    private boolean checkMessageIsHave(TerminalMessage terminalMessage) {
        boolean isHave = false;
        if (terminalMessage != null) {
            for (TerminalMessage message : chatMessageList) {
                if (message.messageId == terminalMessage.messageId) {
                    isHave = true;
                    break;
                }
            }
        }
        return isHave;
    }

    public void setBackListener(OnBackListener backListener) {
        this.backListener = backListener;
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
                super.onBackPressed();
            } else if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
                fl_fragment_container.setVisibility(View.GONE);
                getSupportFragmentManager().popBackStack();
            } else {
                super.onBackPressed();
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
     * 停止播放组呼录音
     */
    public void stopRecord() {
        MyTerminalFactory.getSDK().getTerminalMessageManager().stopMultimediaMessage();
        MediaManager.release();
        MyApplication.instance.isPlayVoice = false;
    }

    public void openPhoto(List<ImageBean> mImgList, int currentPos) {


    }

}

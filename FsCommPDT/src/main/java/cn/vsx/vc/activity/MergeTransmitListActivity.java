package cn.vsx.vc.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.vsx.hamster.common.MessageCategory;
import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.manager.audio.IAudioPlayComplateHandler;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallState;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.GetMessagesByIdsHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveDownloadFinishHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.MergeTransmitListAdapter;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.receiveHandle.ReceiverReplayGroupMergeTransmitVoiceHandler;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

public class MergeTransmitListActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, BaseQuickAdapter.RequestLoadMoreListener {


    @Bind(R.id.bar_title)
    TextView barTitle;
    @Bind(R.id.layout_srl)
    SwipeRefreshLayout layoutSrl;
    @Bind(R.id.contentView)
    RecyclerView contentView;
    @Bind(R.id.fl_fragment_container)
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

    protected Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_merge_transmit_list;
    }

    @Override
    public void initView() {
        Intent intent = getIntent();
        terminalMessage = (TerminalMessage) intent.getSerializableExtra(Constants.TERMINALMESSAGE);
        isGroup = getIntent().getBooleanExtra(Constants.IS_GROUP, false);
        userId = getIntent().getIntExtra(Constants.USER_ID, 0);
        if(terminalMessage == null || (terminalMessage != null && terminalMessage.messageBody == null)){
            finish();
            return;
        }

        JSONObject jsonObject = terminalMessage.messageBody;
        messageIds = getMessageIds(jsonObject);

        barTitle.setText(jsonObject.containsKey(JsonParam.CONTENT)?jsonObject.getString(JsonParam.CONTENT):"消息记录");

        adapter = new MergeTransmitListAdapter(chatMessageList,this,isGroup,userId);
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
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveDownloadFinishHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiverReplayGroupMergeTransmitVoiceHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(getMessagesByIdsHandler);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveDownloadFinishHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiverReplayGroupMergeTransmitVoiceHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(getMessagesByIdsHandler);
    }

    @Override
    public void doOtherDestroy() {
        handler.removeCallbacksAndMessages(null);
    }

    @OnClick({R.id.news_bar_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.news_bar_back:
                finish();
                break;
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
        MyTerminalFactory.getSDK().getTerminalMessageManager().getMessageListByIds(messageIds);
    }
    /**
     * 获取组内直播历史列表
     */
    private GetMessagesByIdsHandler getMessagesByIdsHandler = (resultCode, resultDesc,memberList) -> {
        handler.post(() -> {
            layoutSrl.setRefreshing(false);
            if (resultCode == BaseCommonCode.SUCCESS_CODE && !memberList.isEmpty()) {
                chatMessageList.clear();
                chatMessageList.addAll(memberList);
                adapter.notifyDataSetChanged();
            } else {
                if (mPage > 1) {
                    mPage = mPage - 1;
                }
                ToastUtil.showToast(MergeTransmitListActivity.this, resultDesc);
            }
        });
    };

    /**
     * 点击消息的组呼条目，播放组呼录音
     **/
    private ReceiverReplayGroupMergeTransmitVoiceHandler mReceiverReplayGroupMergeTransmitVoiceHandler = new ReceiverReplayGroupMergeTransmitVoiceHandler() {
        @Override
        public void handler(int postion) {
            mposition = postion;

            handler.post(() -> {
                if (MyApplication.instance.getIndividualState() == IndividualCallState.IDLE &&
                        MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.IDLE &&
                        MyApplication.instance.getGroupListenenState() == GroupCallListenState.IDLE) {//不是在组呼也不是在个呼中，可以播放录音

                    if (lastPosition == mposition) {//点击同一个条目
                        if (MyApplication.instance.isPlayVoice) {
                            MyApplication.instance.isPlayVoice = false;
                            isSameItem = true;
                            adapter.refreshPersonContactsAdapter(mposition, chatMessageList, MyApplication.instance.isPlayVoice, isSameItem);
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
                    ToastUtil.showToast(MergeTransmitListActivity.this, getString(R.string.text_can_not_play_recording_now));
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
                    replaceMessage(terminalMessage);
                }

                if (terminalMessage.messageType == MessageType.FILE.getCode()) {
                    terminalMessage.messageBody.put(JsonParam.IS_DOWNLOADINF, false);
                    replaceMessage(terminalMessage);
                }
                if (terminalMessage.messageType == MessageType.AUDIO.getCode()) {
                    adapter.isDownloading = false;
                    terminalMessage.messageBody.put(JsonParam.IS_DOWNLOADINF, false);
                    replaceMessage(terminalMessage);
                }
                if (terminalMessage.messageType == MessageType.VIDEO_CLIPS.getCode()) {
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
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        if(has){
            //如果包含这条消息
            if (terminalMessage.messageType == MessageType.PICTURE.getCode()) {
                //如果是原图下载完了就打开
                adapter.isDownloadingPicture = false;
                if (terminalMessage.messageBody.containsKey(JsonParam.ISMICROPICTURE) &&
                        !terminalMessage.messageBody.getBooleanValue(JsonParam.ISMICROPICTURE)) {
                    adapter.openPhotoAfterDownload(terminalMessage);
                }
            }
            if (terminalMessage.messageType == MessageType.FILE.getCode()) {
                adapter.openFileAfterDownload(terminalMessage);
                adapter.isDownloading = false;
            }
            if (terminalMessage.messageType == MessageType.VIDEO_CLIPS.getCode()) {
                File file = new File(terminalMessage.messagePath);
                adapter.openVideo(terminalMessage, file);
                adapter.isDownloading = false;
            }
        }
    }

    /**
     * 录音播放完成的消息
     */
    private IAudioPlayComplateHandler audioPlayComplateHandler = () -> handler.post(() -> {
        MyApplication.instance.isPlayVoice = false;
        isSameItem = true;
        adapter.refreshPersonContactsAdapter(mposition, chatMessageList, MyApplication.instance.isPlayVoice, isSameItem);
        setSmoothScrollToPosition(mposition);
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
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverReplayGroupMergeTransmitVoiceHandler.class, index);
                }
            }
        }else {
            logger.debug("最后一条消息已播放完成");
        }
    }

    public void setSmoothScrollToPosition(int position) {
        contentView.smoothScrollToPosition(position);
    }

    /**
     * 获取消息id的字符串
     * @param jsonObject
     * @return
     */
    private String getMessageIds(JSONObject jsonObject) {
        StringBuffer ids = new StringBuffer();
        if(jsonObject.containsKey(JsonParam.MESSAGE_ID_LIST)){
            JSONArray jsonArray = jsonObject.getJSONArray(JsonParam.MESSAGE_ID_LIST);
            if(jsonArray!=null && jsonArray.size()>0){
                int size = jsonArray.size();
                for (int i = 0; i < size; i++) {
                    ids.append(String.valueOf(jsonArray.get(i)));
                    if(i != (size-1)){
                        ids.append(",");
                    }
                }
            }
        }
        return ids.toString();
    }

}

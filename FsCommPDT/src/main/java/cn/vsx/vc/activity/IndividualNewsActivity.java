package cn.vsx.vc.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.Bind;
import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.CallMode;
import cn.vsx.hamster.common.MemberChangeType;
import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.TerminalErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallState;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveAnswerIndividualCallTimeoutHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCurrentGroupIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMultimediaMessageCompleteHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyIndividualCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyLivingIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyMemberChangeHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseStartLiveHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveServerConnectionEstablishedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiverReplayIndividualChatVoiceHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.ItemAdapter;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.receiveHandle.ReceiverActivePushVideoHandler;
import cn.vsx.vc.receiveHandle.ReceiverCloseKeyBoardHandler;
import cn.vsx.vc.receiveHandle.ReceiverIndividualCallFromMsgItemHandler;
import cn.vsx.vc.receiveHandle.ReceiverRequestVideoHandler;
import cn.vsx.vc.record.AudioRecordButton;
import cn.vsx.vc.record.MediaManager;
import cn.vsx.vc.utils.CallPhoneUtil;
import cn.vsx.vc.utils.DataUtil;
import cn.vsx.vc.utils.InputMethodUtil;
import cn.vsx.vc.utils.ToastUtil;
import cn.vsx.vc.view.FixedRecyclerView;
import cn.vsx.vc.view.FunctionHidePlus;
import cn.vsx.vc.view.VolumeViewLayout;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import ptt.terminalsdk.context.MyTerminalFactory;


/**
 * Created by zckj on 2017/3/15.
 */

public class IndividualNewsActivity extends ChatBaseActivity implements View.OnClickListener {
    @Bind(R.id.news_bar_return)
    ImageView newsBarReturn;
    @Bind(R.id.individual_news_phone)
    ImageView individualNewsPhone;
    @Bind(R.id.individual_news_info)
    ImageView individualNewsInfo;
    @Bind(R.id.individual_news_help)
    ImageView individual_news_help;
    @Bind(R.id.noNetWork)
    LinearLayout noNetWork;
    @Bind(R.id.volume_layout)
    VolumeViewLayout volumeViewLayout;
    @Bind(R.id.tv_chat_name)
    TextView newsBarGroupName;
    @Bind(R.id.btn_ptt)
    Button ptt;
    @Bind(R.id.funcation)
    FunctionHidePlus funcation;
    @Bind(R.id.btn_record)
    AudioRecordButton record;
    //    @Bind(R.id.rl_include_listview)
    //    RelativeLayout rl_include_listview;

    @Bind(R.id.sfl_call_list)
    SwipeRefreshLayout sflCallList;
    @Bind(R.id.group_call_list)
    FixedRecyclerView groupCallList;

    @Bind(R.id.fl_fragment_container)
    FrameLayout fl_fragment_container;
    @Bind(R.id.group_call_news_et)
    EditText groupCallNewsEt;
    @Bind(R.id.group_call_news_keyboard)
    ImageView group_call_news_keyboard;
    @Bind(R.id.iv_call)
    ImageView ivCall;
    private static int VOIP=0;
    private static int TELEPHONE=1;

    public static boolean isForeground = false;
    public static int mFromId;
    private Member member;
    private String mPhoneNo;

    public static void startCurrentActivity(Context context, int userId, String userName) {
        Intent intent = new Intent(context, IndividualNewsActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("userName", userName);
        intent.putExtra("isGroup", false);
        context.startActivity(intent);
    }

    @Override
    public int getLayoutResId() {
        setSatusBarTransparent();
        return R.layout.activity_individual_news;
    }

    @Override
    public void initView() {
        sflCallList.setColorSchemeResources(R.color.colorPrimary);
        sflCallList.setProgressViewOffset(false, 0, (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources()
                        .getDisplayMetrics()));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        groupCallList.setLayoutManager(linearLayoutManager);
        //        super.rl_include_listview = rl_include_listview;
        super.newsBarGroupName = newsBarGroupName;
        super.sflCallList = sflCallList;
        super.groupCallList = groupCallList;
        super.fl_fragment_container = fl_fragment_container;
        super.groupCallNewsEt = groupCallNewsEt;
        super.funcation = funcation;
        super.ptt = ptt;
//        setStatusBarColor();
        groupCallList.setVerticalScrollBarEnabled(false);
        record.setAudioPauseListener(new AudioRecordButton.AudioPauseListener(){
            @Override
            public void onPause(){
                sendRecord();
            }
        });
    }

    @Override
    public void initListener() {
        newsBarReturn.setOnClickListener(this);
        individualNewsPhone.setOnClickListener(this);
        individualNewsInfo.setOnClickListener(this);
        individual_news_help.setOnClickListener(this);
        ivCall.setOnClickListener(this);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveServerConnectionEstablishedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseStartLiveHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyIndividualCallIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyLivingIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyMemberChangeHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverIndividualCallFromMsgItemHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiverCloseKeyBoardHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverReplayIndividualChatVoiceHandler);
        //        MyTerminalFactory.getSDK().registReceiveHandler(receiveHistoryMultimediaFailHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveMultimediaMessageCompleteHandler);
        super.initListener();
    }

    @Override
    public void initData() {
        super.initData();
        mFromId = userId;
        member = DataUtil.getMemberByMemberNo(userId);
        mPhoneNo = member.phone;
        logger.info("userId：" + userId);
        logger.info("member：" + member.toString());
        funcation.setFunction(false, userId);
    }


    @Override
    public void postVideo() {
        //        if (!ActivityCollector.isActivityExist(VideoLiveActivity.class)) {
        //            Intent intent = new Intent(this, VideoLiveActivity.class);
        //            intent.setAction("InitiativeVideoLive");
        //            intent.putExtra("userId", userId);
        //            intent.putExtra("userName", userName);
        //            intent.putExtra("number", 1);
        //            startActivity(intent);
        //        }

        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverActivePushVideoHandler.class, userId);
    }

    @Override
    public void requestVideo() {
        //        if (!ActivityCollector.isActivityExist(VideoLiveActivity.class)) {
        //            Intent intent = new Intent(this, VideoLiveActivity.class);
        //            intent.setAction("RequestOtherLive");
        //            intent.putExtra("userId", userId);
        //            intent.putExtra("userName", userName);
        //            intent.putExtra("number", 2);
        //            startActivity(intent);
        //        }
        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverRequestVideoHandler.class, new Member(userId, userName));
    }

    @Override
    public void doOtherDestroy() {
        handler.removeCallbacksAndMessages(null);
        record.cancel();
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveServerConnectionEstablishedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseStartLiveHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverIndividualCallFromMsgItemHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverReplayIndividualChatVoiceHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyIndividualCallIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyLivingIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMultimediaMessageCompleteHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyMemberChangeHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiverCloseKeyBoardHandler);
        if (volumeViewLayout != null) {
            volumeViewLayout.unRegistLintener();
        }
        MediaManager.release();
        super.doOtherDestroy();
    }


    protected void receiveIndividualCall() {
        funcation.hideKeyboard(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isForeground = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isForeground = false;
        record.onPause();
    }

    /**
     * 请求个呼
     */
    private void activeIndividualCall() {
        MyApplication.instance.isCallState = true;
        Member member = DataUtil.getMemberByMemberNo(userId);
        //        Member member = new Member(userId, HandleIdUtil.handleName(userName));
        boolean network = MyTerminalFactory.getSDK().hasNetwork();
        if (network) {

            if(member!=null){
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCurrentGroupIndividualCallHandler.class, member);
            }else {
                ToastUtil.showToast(IndividualNewsActivity.this,"获取成员信息失败");
            }
        } else {
            ToastUtil.showToast(this, "网络连接异常，请检查网络！");
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
                                resultDes[0] = "获取音频成功，准备播放";
                                logger.info("URL获得音频文件，播放录音");
                                MediaManager.playSound(terminalMessage.messagePath, onCompletionListener);
                            } else {
                                resultCode[0] = TerminalErrorCode.SERVER_NOT_RESPONSE.getErrorCode();
                                resultDes[0] = "历史音频未找到，或服务器无响应！";
                                logger.error("历史音频未找到，或服务器无响应！");
                            }
                            TerminalFactory.getSDK().notifyReceiveHandler(ReceiveMultimediaMessageCompleteHandler.class, resultCode[0], resultDes[0]);
                        }
                    });
                } else {
                    resultCode[0] = BaseCommonCode.SUCCESS_CODE;
                    resultDes[0] = "从本地获取音频成功，准备播放";
                    logger.info("从本地音频文件，播放录音");
                    MediaManager.playSound(terminalMessage.messagePath, onCompletionListener);
                    TerminalFactory.getSDK().notifyReceiveHandler(ReceiveMultimediaMessageCompleteHandler.class, resultCode[0], resultDes[0]);
                }
            } else {
                resultCode[0] = TerminalErrorCode.PARAMETER_ERROR.getErrorCode();
                resultDes[0] = "获取历史音频参数不合法！";
                logger.error("获取历史音频参数不合法！");
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiveMultimediaMessageCompleteHandler.class, resultCode[0], resultDes[0]);
            }
        } catch (Exception e) {
            resultDes[0] = "获得语音消息失败！";
            resultCode[0] = TerminalErrorCode.PARAMETER_ERROR.getErrorCode();
            logger.error("获得语音消息失败！", e);
            TerminalFactory.getSDK().notifyReceiveHandler(ReceiveMultimediaMessageCompleteHandler.class, resultCode[0], resultDes[0]);
        }


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.news_bar_return:
                onBackPressed();
                break;
            case R.id.individual_news_phone:

                hideKey();
                if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_CALL_PRIVATE.name())){
                    ToastUtil.showToast(this,"没有个呼功能权限");
                }else {
                    activeIndividualCall();
                }

                break;
            case R.id.individual_news_info:
                Intent intent = new Intent(MyApplication.instance, UserInfoActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("userName", userName);
                startActivity(intent);
                break;
            case R.id.individual_news_help:
                //                Intent intent = new Intent(this, HelpActivity.class);
                //                intent.setAction("5");
                //                startActivity(intent);
                break;
            case R.id.iv_call:
                hideKey();
                if (!TextUtils.isEmpty(member.phone)) {

                    ItemAdapter adapter = new ItemAdapter(IndividualNewsActivity.this,ItemAdapter.iniDatas());
                    AlertDialog.Builder builder = new AlertDialog.Builder(IndividualNewsActivity.this);
                    //设置标题
                    builder.setTitle("拨打电话");
                    builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int position) {
                            if(position==VOIP){//voip电话


                                if(MyTerminalFactory.getSDK().getParam(Params.VOIP_SUCCESS,false)){
                                    Intent intent = new Intent(IndividualNewsActivity.this, VoipPhoneActivity.class);
                                    intent.putExtra("member",member);
                                    IndividualNewsActivity.this.startActivity(intent);
                                }else {
                                    ToastUtil.showToast(IndividualNewsActivity.this,"voip注册失败，请检查服务器配置");
                                }
                            }
                            else if(position==TELEPHONE){//普通电话

                                CallPhoneUtil.callPhone(IndividualNewsActivity.this, member.phone);

                            }

                        }
                    });
                    builder.create();
                    builder.show();
                }else {
                    ToastUtil.showToast(IndividualNewsActivity.this,"暂无该用户电话号码");
                }
                break;
        }
    }

    /**
     * 点击个呼条目
     */
    private ReceiverIndividualCallFromMsgItemHandler mReceiverIndividualCallFromMsgItemHandler = new ReceiverIndividualCallFromMsgItemHandler() {
        @Override
        public void handler() {
            activeIndividualCall();
        }
    };

    private int mposition = -1;
    private int lastPosition = -1;
    private boolean isSameItem = true;
    private Handler myHandler = new Handler();
    private ExecutorService executorService = Executors.newFixedThreadPool(1);
    /**
     * 点击个呼录音条目
     */
    private ReceiverReplayIndividualChatVoiceHandler mReceiverReplayIndividualChatVoiceHandler = new ReceiverReplayIndividualChatVoiceHandler() {

        @Override
        public void handler(final TerminalMessage terminalMessage, int postion) {
            mposition = postion;
            isReject=false;
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (MyApplication.instance.getIndividualState() == IndividualCallState.IDLE &&
                            MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.IDLE &&
                            MyApplication.instance.getGroupListenenState() == GroupCallListenState.IDLE) {//不是在组呼也不是在个呼中，可以播放录音

                        if (lastPosition == mposition) {//点击同一个条目
                            if (MyApplication.instance.isPlayVoice) {
                                logger.error("点击同一条目，停止录音，停止动画");
                                MediaManager.release();
                                MyApplication.instance.isPlayVoice = false;
                                isSameItem = true;
                                temporaryAdapter.refreshPersonContactsAdapter(mposition, chatMessageList, MyApplication.instance.isPlayVoice, isSameItem);
                                temporaryAdapter.notifyDataSetChanged();
                            } else {
                                executorService.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mposition < chatMessageList.size() && mposition >= 0) {
                                            try {
                                                downloadRecordFileOrPlay(terminalMessage, onCompletionListener);
                                            } catch (IndexOutOfBoundsException e) {
                                                logger.warn("mPosition出现异常，其中mposition=" + mposition + "，mTerminalMessageList.size()=" + chatMessageList.size(), e);
                                            }
                                        }
                                    }
                                });
                            }
                        } else {//点击不同条目
                            //播放当前的
                            executorService.execute(new Runnable() {
                                public void run() {
                                    if (mposition < chatMessageList.size() && mposition >= 0) {
                                        try {
                                            downloadRecordFileOrPlay(terminalMessage, onCompletionListener);
                                        } catch (IndexOutOfBoundsException e) {
                                            logger.warn("mPosition出现异常，其中mposition=" + mposition + "，mTerminalMessageList.size()=" + chatMessageList.size(), e);
                                        }
                                    }
                                }
                            });
                        }

                    } else {
                        ToastUtil.showToast(IndividualNewsActivity.this, "当前不可播放录音");
                    }
                }
            });
        }
    };
    /**
     * 录音播放完成的消息
     */
    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    //                    logger.error("播放完成的回调触发了========> "+lastPosition+"/"+mposition+"/"+chatMessageList.size());
                    MyApplication.instance.isPlayVoice = false;
                    isSameItem = true;
                    chatMessageList.get(mposition).messageBody.put(JsonParam.UNREAD, false);
                    temporaryAdapter.refreshPersonContactsAdapter(mposition, chatMessageList, MyApplication.instance.isPlayVoice, isSameItem);
                    temporaryAdapter.notifyDataSetChanged();
                }
            });
            autoPlay(mposition+1);
        }
    };
    private boolean isReject=false;
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
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverReplayIndividualChatVoiceHandler.class, chatMessageList.get(index), index);
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
    private ReceiveMultimediaMessageCompleteHandler receiveMultimediaMessageCompleteHandler = new ReceiveMultimediaMessageCompleteHandler() {
        @Override
        public void handler(final int resultCode, final String resultDes) {
            logger.info("ReceiveMultimediaMessageCompleteHandler   "+resultCode+"/"+resultDes);
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                        if (lastPosition == mposition) {//点击同一个条目
                            isSameItem = true;
                            MyApplication.instance.isPlayVoice = !MyApplication.instance.isPlayVoice;
                        } else {//点击不同条目
                            isSameItem = false;
                            MyApplication.instance.isPlayVoice = false;
                        }
                        Collections.sort(chatMessageList);
                        if (temporaryAdapter != null) {
                            temporaryAdapter.refreshPersonContactsAdapter(mposition, chatMessageList, MyApplication.instance.isPlayVoice, isSameItem);
                            //                            temporaryAdapter.notifyDataSetChanged();
                        }
                        lastPosition = mposition;
                    } else {
                        logger.info("开始播放或停止播放的回调" + resultDes);
                        ToastUtil.showToast(IndividualNewsActivity.this, resultDes);
                    }
                }
            });
        }
    };


    /**
     * 网络连接状态
     */
    private ReceiveServerConnectionEstablishedHandler receiveServerConnectionEstablishedHandler = new ReceiveServerConnectionEstablishedHandler() {

        @Override
        public void handler(final boolean connected) {
            logger.info("个人会话页面收到服务是否连接的通知" + connected);
            IndividualNewsActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!connected) {
                        noNetWork.setVisibility(View.VISIBLE);
                    } else {
                        noNetWork.setVisibility(View.GONE);
                    }
                }
            });
        }
    };

    /**更新所有成员列表*/
    private ReceiveNotifyMemberChangeHandler receiveNotifyMemberChangeHandler = new ReceiveNotifyMemberChangeHandler() {
        @Override
        public void handler(final MemberChangeType memberChangeType) {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    funcation.setFunction(false,userId);
                }
            });
        }
    };

    /**
     * 被动方组呼来了
     */
    private ReceiveGroupCallIncommingHandler receiveGroupCallIncommingHandler = new ReceiveGroupCallIncommingHandler() {

        @Override
        public void handler(int memberId, final String memberName, int groupId,
                            String version, CallMode currentCallMode) {
            logger.info("触发了被动方组呼来了receiveGroupCallIncommingHandler:" + currentCallMode);
            speakingId = groupId;
            speakingName = memberName;
            if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_LISTEN.name())) {
                ToastUtil.showToast(IndividualNewsActivity.this, "没有组呼听的功能权限");
            }
        }
    };

    /**
     * 被动方个呼来了，选择接听或挂断
     */
    private ReceiveNotifyIndividualCallIncommingHandler receiveNotifyIndividualCallIncommingHandler = new ReceiveNotifyIndividualCallIncommingHandler() {
        @Override
        public void handler(final String mainMemberName, final int mainMemberId, int individualCallType) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    isReject=true;
                    MediaManager.release();
                    MyApplication.instance.isPlayVoice=true;
                    TerminalFactory.getSDK().notifyReceiveHandler(ReceiveMultimediaMessageCompleteHandler.class, 0, "");
                }
            });

        }
    };

    /**
     * 被动方个呼答复超时
     */
    private ReceiveAnswerIndividualCallTimeoutHandler receiveAnswerIndividualCallTimeoutHandler = new ReceiveAnswerIndividualCallTimeoutHandler() {
        @Override
        public void handler() {

        }
    };

    /**收到别人请求我开启直播的通知**/
    private ReceiveNotifyLivingIncommingHandler receiveNotifyLivingIncommingHandler = new ReceiveNotifyLivingIncommingHandler() {
        @Override
        public void handler(final String mainMemberName, final int mainMemberId) {
            logger.info("ReceiveNotifyLivingIncommingHandler:"+mainMemberName+"/"+mainMemberId);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    isReject=true;
                    MediaManager.release();
                    MyApplication.instance.isPlayVoice=true;
                    TerminalFactory.getSDK().notifyReceiveHandler(ReceiveMultimediaMessageCompleteHandler.class, 0, "");
                }
            });
        }
    };

    private ReceiveUpdateConfigHandler receiveUpdateConfigHandler = new ReceiveUpdateConfigHandler() {
        @Override
        public void handler() {
            IndividualNewsActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    funcation.setFunction(false, userId);
                }
            });
        }
    };
    private ReceiveResponseStartLiveHandler receiveResponseStartLiveHandler = new ReceiveResponseStartLiveHandler() {

        @Override
        public void handler(int resultCode, String resultDesc) {

            PromptManager.getInstance().stopRing();
        }
    };

    private ReceiverCloseKeyBoardHandler receiverCloseKeyBoardHandler = new ReceiverCloseKeyBoardHandler() {
        @Override
        public void handler() {
            logger.info("sjl_收到来自服务的关闭键盘handler");
            InputMethodUtil.hideInputMethod(IndividualNewsActivity.this, groupCallNewsEt);
        }
    };


}

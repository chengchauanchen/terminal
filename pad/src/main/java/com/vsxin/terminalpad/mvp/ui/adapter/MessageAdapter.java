package com.vsxin.terminalpad.mvp.ui.adapter;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.ixiaoma.xiaomabus.architecture.mvp.refresh.adapter.BaseRecycleViewAdapter;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.app.PadApplication;
import com.vsxin.terminalpad.mvp.entity.MediaBean;
import com.vsxin.terminalpad.mvp.entity.PlayType;
import com.vsxin.terminalpad.mvp.ui.adapter.holder.ChatViewHolder;
import com.vsxin.terminalpad.mvp.ui.fragment.VideoPreviewItemFragment;
import com.vsxin.terminalpad.receiveHandler.ReceiveGetHistoryLiveUrlsHandler2;
import com.vsxin.terminalpad.receiveHandler.ReceiverChatListItemClickHandler;
import com.vsxin.terminalpad.receiveHandler.ReceiverIndividualCallFromMsgItemHandler;
import com.vsxin.terminalpad.utils.DensityUtil;
import com.vsxin.terminalpad.utils.FragmentManage;
import com.zectec.imageandfileselector.adapter.FaceRecognitionAdapter;
import com.zectec.imageandfileselector.bean.FaceRecognitionBean;
import com.zectec.imageandfileselector.bean.ImageBean;
import com.zectec.imageandfileselector.fragment.ImagePreviewItemFragment;
import com.zectec.imageandfileselector.receivehandler.ReceiverSendFileCheckMessageHandler;
import com.zectec.imageandfileselector.receivehandler.ReceiverSendFileHandler;
import com.zectec.imageandfileselector.utils.DateUtils;
import com.zectec.imageandfileselector.utils.FileIcons;
import com.zectec.imageandfileselector.utils.FileUtil;
import com.zectec.imageandfileselector.utils.OpenFileUtils;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;
import com.zectec.imageandfileselector.utils.PhotoUtils;
import com.zectec.imageandfileselector.view.LoadingCircleView;

import org.apache.log4j.Logger;
import org.ddpush.im.common.v1.handler.PushMessageSendResultHandler;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.MessageSendStateEnum;
import cn.vsx.hamster.common.MessageStatus;
import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.Remark;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.common.util.NoCodec;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.SignalServerErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiverReplayIndividualChatVoiceHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.TerminalMessageUtil;
import cn.vsx.hamster.terminalsdk.tools.Util;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * 作者：xuxiaolong
 * 版本：1.0
 * 创建日期：2019/8/3
 * 描述：
 * 修订历史：
 */
public class MessageAdapter  extends BaseRecycleViewAdapter<TerminalMessage,ChatViewHolder>{

    private static final int MESSAGE_SHORT_TEXT_RECEIVED = 0;//短文本
    private static final int MESSAGE_LONG_TEXT_RECEIVED = 1;//长文本
    private static final int MESSAGE_IMAGE_RECEIVED = 2;//图片
    private static final int MESSAGE_VOICE_RECEIVED = 3;//录音
    private static final int MESSAGE_VEDIO_RECEIVED = 4;//小视频
    private static final int MESSAGE_FILE_RECEIVED = 5;//文件
    private static final int MESSAGE_LOCATION_RECEIVED = 6;//位置
    private static final int MESSAGE_AFFICHE_RECEIVED = 7;//公告
    private static final int MESSAGE_WARNING_INSTANCE_RECEIVED = 8;//警情
    private static final int MESSAGE_PRIVATE_CALL_RECEIVED = 9;//个呼
    private static final int MESSAGE_VIDEO_LIVE_RECEIVED = 10;//图像记录
    private static final int MESSAGE_GROUP_CALL_RECEIVED = 11;//组呼
    private static final int MESSAGE_HYPERLINK_RECEIVED = 12;//超链接
    private static final int MESSAGE_GB28181_RECODE_RECEIVED = 13;//视频平台
    private static final int MESSAGE_MERGE_TRANSMIT_RECEIVED = 14;//合并转发

    private static final int MESSAGE_SHORT_TEXT_SEND = 15;//短文本
    private static final int MESSAGE_LONG_TEXT_SEND = 16;//长文本
    private static final int MESSAGE_IMAGE_SEND = 17;//图片
    private static final int MESSAGE_VOICE_SEND = 18;//录音
    private static final int MESSAGE_VEDIO_SEND = 19;//小视频
    private static final int MESSAGE_FILE_SEND = 20;//文件
    private static final int MESSAGE_LOCATION_SEND = 21;//位置
    private static final int MESSAGE_AFFICHE_SEND = 22;//公告
    private static final int MESSAGE_WARNING_INSTANCE_SEND = 23;//警情
    private static final int MESSAGE_PRIVATE_CALL_SEND = 24;//个呼
    private static final int MESSAGE_VIDEO_LIVE_SEND = 25;//图像记录
    private static final int MESSAGE_GROUP_CALL_SEND = 26;//组呼
    private static final int MESSAGE_HYPERLINK_SEND = 27;//超链接
    private static final int MESSAGE_GB28181_RECODE_SEND = 28;//视频平台
    private static final int MESSAGE_MERGE_TRANSMIT_SEND = 29;//合并转发
    private static final int MESSAGE_AFFICHE= 30;//公告

    private static final int MIN_CLICK_DELAY_TIME = 1000;
    private long lastClickTime = 0;

//    FrameLayout fragment_contener;

    private Logger logger = Logger.getLogger(getClass());
    private boolean isGroupChat;
    private LayoutInflater inflater;
    private FragmentActivity activity;

    private int mposition = -1;
    private boolean isPlaying;
    private boolean isSameItem;
    public boolean isDownloading;
    public boolean isDownloadingPicture;
    public boolean isEnable = true;
    public List<TerminalMessage> uploadMessages = new ArrayList<>();
    public LoadingCircleView loadingView;
    public ProgressBar downloadProgressBar;//正在下载的条目对应的ProgressBar
    public TextView download_tv_progressBars;//正在下载的条目对应的tv_progressBars
    public Map<Integer, Integer> progressPercentMap = new HashMap<>();
    public String liveTheme;
    private boolean mIsLongClick = false;//文本消息是否长按
    List<ImageBean> mImgList = new ArrayList<>();
    List<String> mImgUrlList = new ArrayList<>();
    List<TerminalMessage> unReadVoiceList = new ArrayList<>();
    private boolean upload;//是否正在上传
    private boolean isForWardMore;//是否合并转发
    private Handler myHandler = new Handler(Looper.getMainLooper());

    public MessageAdapter(FragmentActivity activity) {
        super(activity);
        this.activity = activity;
        inflater = activity.getLayoutInflater();
    }

    public void refreshPersonContactsAdapter(int mposition,List<TerminalMessage> terminalMessageList, boolean isPlaying, boolean isSameItem) {
        this.mposition = mposition;
        this.datas = terminalMessageList;
        this.isPlaying = isPlaying;
        this.isSameItem = isSameItem;
        Collections.sort(terminalMessageList);
        notifyDataSetChanged();
    }


    public void setIsGroup(boolean isGroup) {
        this.isGroupChat = isGroup;
    }

//    public void setFragment_contener(FrameLayout fragment_contener) {
//        this.fragment_contener = fragment_contener;
//    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }

    private void setText(TextView textView, String content) {
        if (textView != null) {
            textView.setText(content);
        }
    }

    private void setViewVisibility(View view, int visiable) {
        if (view != null) {
            view.setVisibility(visiable);
        }
    }

    private void setProgress(ProgressBar progressBar, int progress) {
        if(progressBar!=null){
            progressBar.setProgress(progress);
        }
    }

    private void setViewChecked(CheckBox view, boolean isChecked) {
        if (view != null) {
            view.setChecked(isChecked);
        }
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ChatViewHolder holder = null;
        switch (viewType) {
            case MESSAGE_LONG_TEXT_RECEIVED:
            case MESSAGE_SHORT_TEXT_RECEIVED:
                holder =  new ChatViewHolder.TextReceivedHolder(getViewByType(viewType, parent),true);
                break;
            case MESSAGE_LONG_TEXT_SEND:
            case MESSAGE_SHORT_TEXT_SEND:
                holder =   new ChatViewHolder.TextSendHolder(getViewByType(viewType, parent),false);
                break;
            case MESSAGE_IMAGE_RECEIVED:
                holder =   new ChatViewHolder.ImageReceivedHolder(getViewByType(viewType, parent),true);
                break;
            case MESSAGE_IMAGE_SEND:
                holder =   new ChatViewHolder.ImageSendHolder(getViewByType(viewType, parent),false);
                break;
            case MESSAGE_GROUP_CALL_RECEIVED:
            case MESSAGE_VOICE_RECEIVED:
                holder =   new ChatViewHolder.VoiceReceivedHolder(getViewByType(viewType, parent),true);
                break;
            case MESSAGE_GROUP_CALL_SEND:
            case MESSAGE_VOICE_SEND:
                holder =   new ChatViewHolder.VoiceSendHolder(getViewByType(viewType, parent),false);
                break;
            case MESSAGE_FILE_RECEIVED:
                holder =   new ChatViewHolder.FileReceivedHolder(getViewByType(viewType, parent),true);
                break;
            case MESSAGE_FILE_SEND:
                holder =   new ChatViewHolder.FileSendHolder(getViewByType(viewType, parent),false);
                break;
            case MESSAGE_VEDIO_RECEIVED:
                holder =   new ChatViewHolder.VideoReceivedHolder(getViewByType(viewType, parent),true);
                break;
            case MESSAGE_VEDIO_SEND:
                holder =   new ChatViewHolder.VideoSendHolder(getViewByType(viewType, parent),false);
                break;
            case MESSAGE_LOCATION_RECEIVED:
                holder =   new ChatViewHolder.LocationReceivedHolder(getViewByType(viewType, parent),true);
                break;
            case MESSAGE_LOCATION_SEND:
                holder =   new ChatViewHolder.LocationSendHolder(getViewByType(viewType, parent),false);
                break;
            case MESSAGE_VIDEO_LIVE_RECEIVED:
            case MESSAGE_GB28181_RECODE_RECEIVED:
                holder =   new ChatViewHolder.LiveReceivedHolder(getViewByType(viewType, parent),true);
                break;
            case MESSAGE_VIDEO_LIVE_SEND:
            case MESSAGE_GB28181_RECODE_SEND:
                holder =   new ChatViewHolder.LiveSendHolder(getViewByType(viewType, parent),false);
                break;
            case MESSAGE_WARNING_INSTANCE_RECEIVED:
            case MESSAGE_WARNING_INSTANCE_SEND:
                holder = new ChatViewHolder.WarningHolder(getViewByType(viewType,parent));
                break;
            case MESSAGE_PRIVATE_CALL_RECEIVED:
                holder =   new ChatViewHolder.PrivateCallReceivedHolder(getViewByType(viewType, parent),true);
                break;
            case MESSAGE_PRIVATE_CALL_SEND:
                holder =   new ChatViewHolder.PrivateCallSendHolder(getViewByType(viewType, parent),false);
                break;
            case MESSAGE_HYPERLINK_RECEIVED:
                holder =   new ChatViewHolder.HyperlinkReceivedHolder(getViewByType(viewType, parent),true);
                break;
            case MESSAGE_HYPERLINK_SEND:
                holder =   new ChatViewHolder.HyperlinkSendHolder(getViewByType(viewType, parent),false);
                break;
            case MESSAGE_MERGE_TRANSMIT_RECEIVED:
                holder =   new ChatViewHolder.TextMergeTransmitHolder(getViewByType(viewType, parent),true);
                break;
            case MESSAGE_MERGE_TRANSMIT_SEND:
                holder =   new ChatViewHolder.TextMergeTransmitHolder(getViewByType(viewType, parent),false);
                break;
            case MESSAGE_AFFICHE:
                holder = new ChatViewHolder.AfficheHolder(getViewByType(viewType,parent));
                break;
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(ChatViewHolder holder, int position) {
        if(holder!=null){
            final TerminalMessage terminalMessage = datas.get(position);
            final int viewType = getItemViewType(position);
            //消息撤回
            if(terminalMessage.messageType == MessageType.WARNING_INSTANCE.getCode()){
                setWarningData(terminalMessage,position,holder);
            }else if(terminalMessage.messageType == MessageType.AFFICHE.getCode()){//公告
                JSONObject messageBody = terminalMessage.messageBody;
                String noticeContent = messageBody.getString("noticeContent");
                String msg = String.format(activity.getString(R.string.join_temp_group), noticeContent);
                SpannableString spanString = new SpannableString(msg);
                //再构造一个改变字体颜色的Span
                ForegroundColorSpan span = new ForegroundColorSpan(activity.getResources().getColor(R.color.grey_7f));
                //将这个Span应用于指定范围的字体
                spanString.setSpan(span, msg.length()-5, msg.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                holder.tv_bubble.setText(spanString);
                handlerTime(terminalMessage,position,holder);
            }else if(MessageStatus.valueOf(terminalMessage.messageStatus).getCode() == MessageStatus.MESSAGE_RECALL.getCode()){//撤回
                withDrawView(terminalMessage,holder);
            }else{
                setViewVisibility(holder.timeStamp, View.VISIBLE);
                setViewVisibility(holder.reMain, View.VISIBLE);
                setData(position, terminalMessage, viewType, holder);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(datas.get(position).messageType == MessageType.AFFICHE.getCode()){
            return MESSAGE_AFFICHE;
        }
        if (isReceiver(datas.get(position))) {//接收
            if (datas.get(position).messageType == MessageType.SHORT_TEXT.getCode()) {
                return MESSAGE_SHORT_TEXT_RECEIVED;
            }
            if (datas.get(position).messageType == MessageType.LONG_TEXT.getCode()) {
                return MESSAGE_LONG_TEXT_RECEIVED;
            }
            if (datas.get(position).messageType == MessageType.PICTURE.getCode()) {
                return MESSAGE_IMAGE_RECEIVED;
            }
            if (datas.get(position).messageType == MessageType.AUDIO.getCode()) {
                return MESSAGE_VOICE_RECEIVED;
            }
            if (datas.get(position).messageType == MessageType.VIDEO_CLIPS.getCode()) {
                return MESSAGE_VEDIO_RECEIVED;
            }
            if (datas.get(position).messageType == MessageType.FILE.getCode()) {
                return MESSAGE_FILE_RECEIVED;
            }
            if (datas.get(position).messageType == MessageType.POSITION.getCode()) {
                return MESSAGE_LOCATION_RECEIVED;
            }
            if (datas.get(position).messageType == MessageType.AFFICHE.getCode()) {
                return MESSAGE_AFFICHE_RECEIVED;
            }
            if (datas.get(position).messageType == MessageType.WARNING_INSTANCE.getCode()) {
                return MESSAGE_WARNING_INSTANCE_RECEIVED;
            }
            if (datas.get(position).messageType == MessageType.PRIVATE_CALL.getCode()) {
                return MESSAGE_PRIVATE_CALL_RECEIVED;
            }
            if (datas.get(position).messageType == MessageType.VIDEO_LIVE.getCode()) {
                return MESSAGE_VIDEO_LIVE_RECEIVED;
            }
            if (datas.get(position).messageType == MessageType.GROUP_CALL.getCode()) {
                return MESSAGE_GROUP_CALL_RECEIVED;
            }
            if (datas.get(position).messageType == MessageType.HYPERLINK.getCode()) {
                return MESSAGE_HYPERLINK_RECEIVED;
            }
            if(datas.get(position).messageType == MessageType.GB28181_RECORD.getCode()
                    ||datas.get(position).messageType == MessageType.OUTER_GB28181_RECORD.getCode()){
                return MESSAGE_GB28181_RECODE_RECEIVED;
            }
            if(datas.get(position).messageType == MessageType.MERGE_TRANSMIT.getCode()){
                return MESSAGE_MERGE_TRANSMIT_RECEIVED;
            }
        } else {//发送
            if (datas.get(position).messageType == MessageType.SHORT_TEXT.getCode()) {
                return MESSAGE_SHORT_TEXT_SEND;
            }
            if (datas.get(position).messageType == MessageType.LONG_TEXT.getCode()) {
                return MESSAGE_LONG_TEXT_SEND;
            }
            if (datas.get(position).messageType == MessageType.PICTURE.getCode()) {
                return MESSAGE_IMAGE_SEND;
            }
            if (datas.get(position).messageType == MessageType.AUDIO.getCode()) {
                return MESSAGE_VOICE_SEND;
            }
            if (datas.get(position).messageType == MessageType.VIDEO_CLIPS.getCode()) {
                return MESSAGE_VEDIO_SEND;
            }
            if (datas.get(position).messageType == MessageType.FILE.getCode()) {
                return MESSAGE_FILE_SEND;
            }
            if (datas.get(position).messageType == MessageType.POSITION.getCode()) {
                return MESSAGE_LOCATION_SEND;
            }
            if (datas.get(position).messageType == MessageType.AFFICHE.getCode()) {
                return MESSAGE_AFFICHE_SEND;
            }
            if (datas.get(position).messageType == MessageType.WARNING_INSTANCE.getCode()) {
                return MESSAGE_WARNING_INSTANCE_SEND;
            }
            if (datas.get(position).messageType == MessageType.PRIVATE_CALL.getCode()) {
                return MESSAGE_PRIVATE_CALL_SEND;
            }
            if (datas.get(position).messageType == MessageType.VIDEO_LIVE.getCode()) {
                return MESSAGE_VIDEO_LIVE_SEND;
            }
            if (datas.get(position).messageType == MessageType.GROUP_CALL.getCode()) {
                return MESSAGE_GROUP_CALL_SEND;
            }
            if (datas.get(position).messageType == MessageType.HYPERLINK.getCode()) {
                return MESSAGE_HYPERLINK_SEND;
            }
            if(datas.get(position).messageType == MessageType.GB28181_RECORD.getCode()
                    ||datas.get(position).messageType == MessageType.OUTER_GB28181_RECORD.getCode()){
                return MESSAGE_GB28181_RECODE_SEND;
            }
            if(datas.get(position).messageType == MessageType.MERGE_TRANSMIT.getCode()){
                return MESSAGE_MERGE_TRANSMIT_SEND;
            }
        }

        return MESSAGE_SHORT_TEXT_SEND;
    }

    private void setData(int position, TerminalMessage terminalMessage, int viewType, ChatViewHolder holder) {
        handleData(holder, viewType, terminalMessage, position);
        setListener(viewType, holder, terminalMessage, position);
        aboutSend(holder, terminalMessage, viewType);
        if (position == datas.size() - 1) {
            holder.placeHolder.setVisibility(View.VISIBLE);
        } else {
            holder.placeHolder.setVisibility(View.GONE);
        }
        //合并转发
        forwardMore(terminalMessage,holder);


    }

    /**
     * 合并转发
     * @param terminalMessage
     * @param holder
     */
    private void forwardMore(TerminalMessage terminalMessage, ChatViewHolder holder) {
        if(isForWardMore){
            if(terminalMessage.messageType == MessageType.PRIVATE_CALL.getCode() ||
                    terminalMessage.messageType == MessageType.WARNING_INSTANCE.getCode()||
                    terminalMessage.messageType == MessageType.VIDEO_LIVE.getCode()||
                    terminalMessage.messageType == MessageType.GB28181_RECORD.getCode() ||
                    terminalMessage.messageType == MessageType.OUTER_GB28181_RECORD.getCode()){
                setViewVisibility(holder.cbForward,View.GONE);
            }else{
                if(!isReceiver(terminalMessage)){
                    if(terminalMessage.messageBody.containsKey(JsonParam.SEND_STATE)&&
                            terminalMessage.messageBody.getString(JsonParam.SEND_STATE).equals(MessageSendStateEnum.SEND_SUCCESS.toString())){
                        setViewVisibility(holder.cbForward,View.VISIBLE);
                        setViewChecked(holder.cbForward,terminalMessage.isForward);
                    }else{
                        setViewVisibility(holder.cbForward,View.GONE);
                    }
                }else{
                    setViewVisibility(holder.cbForward,View.VISIBLE);
                    setViewChecked(holder.cbForward,terminalMessage.isForward);
                }
            }
        }else{
            setViewVisibility(holder.cbForward,View.GONE);
        }
    }

    /**
     * 设置警情UI
     */
    private void setWarningData(TerminalMessage terminalMessage, int positon ,ChatViewHolder holder){
        if(terminalMessage.messageBody.containsKey(JsonParam.DETAIL) && terminalMessage.messageBody.getBooleanValue(JsonParam.DETAIL)){
            JSONObject messageBody = terminalMessage.messageBody;
            handlerTime(terminalMessage,positon,holder);
            holder.mTvAperson.setText(messageBody.getString(JsonParam.APERSON));
            holder.mTvApersonPhone.setText(messageBody.getString(JsonParam.APERSON_PHONE));
            holder.mTvRecePerson.setText(messageBody.getString(JsonParam.RECVPERSON));
            holder.mTvRecePersonPhone.setText(messageBody.getString(JsonParam.RECVPHONE));
            holder.mTvAlarmTime.setText(messageBody.getString(JsonParam.ALARM_TIME));
            holder.mTvAddress.setText(messageBody.getString(JsonParam.ADDRESS));
            holder.mTvSummary.setText(messageBody.getString(JsonParam.SUMMARY));
        }
    }
    /**
     * 设置撤回UI
     * @param terminalMessage
     */
    private void withDrawView(TerminalMessage terminalMessage, ChatViewHolder holder) {
        setText(holder.timeStamp, String.format(activity.getString(R.string.with_draw_content),isReceiver(terminalMessage)?terminalMessage.messageFromName:"您"));
        setViewVisibility(holder.timeStamp, View.VISIBLE);
        setViewVisibility(holder.reMain, View.GONE);
    }

    private void aboutSend(ChatViewHolder holder, TerminalMessage terminalMessage, int viewType) {
        //        logger.error("============="+terminalMessage);
        //发送中状态
        if (terminalMessage.messageBody.containsKey(JsonParam.SEND_STATE)) {
            if (terminalMessage.messageBody.getString(JsonParam.SEND_STATE).equals(MessageSendStateEnum.SEND_PRE.toString())) {
                if (viewType == MESSAGE_LOCATION_SEND && terminalMessage.messageBody.containsKey(JsonParam.ACTUAL_SEND) &&
                        !terminalMessage.messageBody.getBooleanValue(JsonParam.ACTUAL_SEND)) {
                } else {
                    //这里put是将原来的MessageSendStateEnum.SEND_PRE改成MessageSendStateEnum.SENDING
                    terminalMessage.messageBody.put(JsonParam.SEND_STATE, MessageSendStateEnum.SENDING);
                    sendMessage(holder, terminalMessage, viewType);
                }
                setSendingView(holder, terminalMessage, viewType);
            } else if (terminalMessage.messageBody.getString(JsonParam.SEND_STATE).equals(MessageSendStateEnum.SENDING.toString())) {
                setSendingView(holder, terminalMessage, viewType);
            } else if (terminalMessage.messageBody.getString(JsonParam.SEND_STATE).equals(MessageSendStateEnum.SEND_FAIL.toString())) {
                setSendResultView(holder, viewType, View.VISIBLE);
            } else if (terminalMessage.messageBody.getString(JsonParam.SEND_STATE).equals(MessageSendStateEnum.SEND_SUCCESS.toString())) {
                setSendResultView(holder, viewType, View.GONE);
            }
        } else {
            logger.error("*******没有发送状态的标记，这是不应该的*******");
        }
    }

    private void setSendResultView(ChatViewHolder holder, int viewType, int visible) {
        setViewVisibility(holder.ivMsgStatus, visible);
        setViewVisibility(holder.progressBar, View.GONE);
        if (viewType == MESSAGE_IMAGE_SEND || viewType == MESSAGE_FILE_SEND) {
            setViewVisibility(holder.tv_progress, View.GONE);
            setViewVisibility(holder.progress_bar_uploading, View.GONE);
            setViewVisibility(holder.tv_progress_uploading, View.GONE);
        }
    }

    private void setSendingView(ChatViewHolder holder, TerminalMessage terminalMessage, int viewType) {
        setViewVisibility(holder.ivMsgStatus, View.GONE);
        setViewVisibility(holder.progressBar, View.VISIBLE);
        if (viewType == MESSAGE_IMAGE_SEND || viewType == MESSAGE_FILE_SEND) {
            int tokenId = terminalMessage.messageBody.getIntValue(JsonParam.TOKEN_ID);
            setViewVisibility(holder.tv_progress, View.VISIBLE);
            if (progressPercentMap.containsKey(tokenId)) {
                int percent = progressPercentMap.get(tokenId);
                setProgress(holder.progressBar, percent);
                setText(holder.tv_progress, percent + "%");
            }
        }
        if (viewType == MESSAGE_LOCATION_SEND && terminalMessage.messageBody.containsKey(JsonParam.ACTUAL_SEND) &&
                !terminalMessage.messageBody.getBooleanValue(JsonParam.ACTUAL_SEND)) {
            if (terminalMessage.messageBody.containsKey(JsonParam.GET_LOCATION_FAIL) &&
                    terminalMessage.messageBody.getBooleanValue(JsonParam.GET_LOCATION_FAIL)) {
                setViewVisibility(holder.ivMsgStatus, View.VISIBLE);
                setViewVisibility(holder.progressBar, View.GONE);
            }
            return;//不是真的发送位置
        }
    }

    public void uploadFileDelay() {
        //        new Thread().start();
        MyTerminalFactory.getSDK().getThreadPool().execute(() -> {
            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int count = uploadMessages.size();
            for (int i = 0; i < count; i++) {
                uploadNextFile();
            }
        });
    }

    /**
     * 上传下一个文件
     **/
    public void uploadNextFile() {
        if (uploadMessages.size() == 0) {
            return;
        }
        TerminalMessage terminalMessage1 = (TerminalMessage) uploadMessages.get(0).clone();

        uploadMessages.remove(0);
        terminalMessage1.messageToId = setToIds(terminalMessage1).get(0);
        terminalMessage1.messageBody.put(JsonParam.SEND_STATE, MessageSendStateEnum.SENDING);
        File file = new File(terminalMessage1.messagePath);
        upload = true;
        //        File file1 = new File(terminalMessage1.messageBody.getString("pictureUrl"));
        if (terminalMessage1.messageType == MessageType.PICTURE.getCode()) {
            if(file.length()<=0){
                ToastUtil.showToast(activity,activity.getString(R.string.text_image_is_empty_can_not_send));
                return;
            }
            MyTerminalFactory.getSDK().upload(MyTerminalFactory.getSDK().getParam(Params.IMAGE_UPLOAD_URL, ""), file, terminalMessage1, true);
        } else if (terminalMessage1.messageType == MessageType.FILE.getCode()) {
            if(file.length()<=0){
                ToastUtil.showToast(activity,activity.getString(R.string.text_file_is_empty_can_not_send));
                return;
            }
            MyTerminalFactory.getSDK().upload(MyTerminalFactory.getSDK().getParam(Params.FILE_UPLOAD_URL, ""), file, terminalMessage1, true);
        } else if (terminalMessage1.messageType == MessageType.AUDIO.getCode()) {
            if(file.length()<=0){
                ToastUtil.showToast(activity,activity.getString(R.string.text_audio_is_too_short));
                return;
            }
            MyTerminalFactory.getSDK().upload(MyTerminalFactory.getSDK().getParam(Params.FILE_UPLOAD_URL, ""), file, terminalMessage1, true);
        } else if (terminalMessage1.messageType == MessageType.VIDEO_CLIPS.getCode()) {
            if(file.length()<=0){
                ToastUtil.showToast(activity,activity.getString(R.string.text_video_is_too_short));
                return;
            }
            MyTerminalFactory.getSDK().upload(MyTerminalFactory.getSDK().getParam(Params.FILE_UPLOAD_URL, ""), file, terminalMessage1, true);
            //            MyTerminalFactory.getSDK().upload(MyTerminalFactory.getSDK().getParam(Params.FILE_UPLOAD_URL, ""),file1,terminalMessage1,true);//上传视频第一帧图片
        }
    }

    private void sendMessage(ChatViewHolder holder, TerminalMessage terminalMessage, int viewType) {
        switch (viewType) {
            case MESSAGE_SHORT_TEXT_SEND:
                sendShortTextMessage(terminalMessage);
                break;
            case MESSAGE_LONG_TEXT_SEND:
                uploadLongText(holder, terminalMessage);
                break;
            case MESSAGE_IMAGE_SEND:
                break;
            case MESSAGE_FILE_SEND:
                break;
            case MESSAGE_LOCATION_SEND:
                sendLocationMessage(terminalMessage);
                break;
            case MESSAGE_VIDEO_LIVE_SEND:
                sendVideoLiveMessage(terminalMessage);
                break;

        }
    }

    private void setListener(final int viewType, final ChatViewHolder holder, final TerminalMessage terminalMessage, final int position) {
        //长按消息条目
//        holder.reBubble.setOnLongClickListener(v -> {
//
//            if (terminalMessage.messageType == MessageType.AFFICHE.getCode() ||
//                    terminalMessage.messageType == MessageType.CALL_RECORD.getCode()||
//                    terminalMessage.messageType == MessageType.PRIVATE_CALL.getCode() ||
//                    terminalMessage.messageType == MessageType.VIDEO_LIVE.getCode() ||
//                    terminalMessage.messageType == MessageType.GB28181_RECORD.getCode() ||
//                    terminalMessage.messageType == MessageType.OUTER_GB28181_RECORD.getCode() ||
//                    terminalMessage.messageType == MessageType.MULTI_PATH_VIDEO_PUSH.getCode() ||
//                    terminalMessage.messageType == MessageType.VIDEO_ENTRANCE.getCode()){
//                logger.info("不能转发和撤回");
//                return false;
//            }
//            if (!isEnable){
//                return false;
//            }
//            if(upload){
//                ToastUtil.showToast(activity,activity.getString(R.string.text_in_upload_can_not_forward));
//                return false;
//            }
//
////            new TranspondDialog(activity, terminalMessage).showView();
//            if (!terminalMessage.messageBody.containsKey(JsonParam.TOKEN_ID)){
//                terminalMessage.messageBody.put(JsonParam.TOKEN_ID, MyTerminalFactory.getSDK().getMessageSeq());
//            }
//            transponMessage = (TerminalMessage) terminalMessage.clone();
//            return false;
//        });
        //个呼单独处理
        if(terminalMessage.messageType == MessageType.PRIVATE_CALL.getCode()){
            holder.llCallTempt.setOnClickListener(new View.OnClickListener() {//点击消息条目
                @Override
                public void onClick(View v) {
                    if (!isEnable){
                        return;
                    }
                    long currentTime = Calendar.getInstance().getTimeInMillis();

                    if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {//防止频繁点击操作<1秒
                        lastClickTime = currentTime;
                        privateCallClick(terminalMessage);
                    }
                    InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(holder.reBubble.getWindowToken(), 0);
                }
            });
            holder.llCallListener.setOnClickListener(new View.OnClickListener() {//点击消息条目
                @Override
                public void onClick(View v) {
                    if (!isEnable) {
                        return;
                    }
                    long currentTime = Calendar.getInstance().getTimeInMillis();

                    if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {//防止频繁点击操作<1秒
                        lastClickTime = currentTime;
                        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverReplayIndividualChatVoiceHandler.class, terminalMessage, position, PlayType.PLAY_PRIVATE_CALL.getCode());
                    }
                    InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(holder.reBubble.getWindowToken(), 0);
                }
            });
        }else{
            holder.reBubble.setOnClickListener(v -> {
                if (!isEnable) {
                    return;
                }
                long currentTime = Calendar.getInstance().getTimeInMillis();

                if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {//防止频繁点击操作<1秒
                    lastClickTime = currentTime;
                    fileItemClick(holder, terminalMessage, viewType);
                    photoItemClick(holder, terminalMessage, viewType);
                    videoItemClick(holder, terminalMessage, viewType);
                    privateCallClick(terminalMessage);
                    locationItemClick(terminalMessage);
                    liveItemClick(terminalMessage, viewType);
                    individualNewsRecordItemClick(terminalMessage, position);
                    groupCallItemClick(terminalMessage, position);
                    gb28181ItemClick(terminalMessage, viewType);
                    mergeTransmit(terminalMessage,viewType);
                }
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(holder.reBubble.getWindowToken(), 0);
            });
        }

        if (holder.live_bubble != null) {
            holder.live_bubble.setOnClickListener(v -> {
                if (terminalMessage.resultCode == SignalServerErrorCode.MEMBER_REFUSE.getErrorCode() || terminalMessage.messageBody.getInteger(JsonParam.REMARK) == Remark.STOP_ASK_VIDEO_LIVE || terminalMessage.resultCode == SignalServerErrorCode.VIDEO_LIVE_WAITE_TIMEOUT.getErrorCode()) {
                    if (terminalMessage.messageFromId != MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)) {
                        return;
                    } else {
                        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverSendFileCheckMessageHandler.class, ReceiverSendFileCheckMessageHandler.REQUEST_VIDEO, true, 0);
                    }
                }
            });
        }
        if (holder.ivAvatar != null) {
            holder.ivAvatar.setOnClickListener(v -> {
                if (PadApplication.getPadApplication().getGroupSpeakState() != GroupCallSpeakState.IDLE) {
                    return;
                } else {
                    // TODO: 2019/8/20 跳转到个人页面 
//                    Intent intent = new Intent(activity, UserInfoActivity.class);
//                    intent.putExtra("userId", terminalMessage.messageFromId);
//                    intent.putExtra("userName", terminalMessage.messageFromName);
//                    activity.startActivity(intent);
                }
            });

        }

        /**  点击失败按钮重新发送  **/
        if (!isReceiver(terminalMessage) && holder.ivMsgStatus != null) {
            holder.ivMsgStatus.setOnClickListener(v -> {
                if (!isEnable) {
                    return;
                }
                int messageType = terminalMessage.messageType;
                terminalMessage.resultCode = -1;
                setViewVisibility(holder.ivMsgStatus, View.GONE);
                if (messageType == MessageType.SHORT_TEXT.getCode()) {//短文本
                    sendShortTextMessage(terminalMessage);
                } else if (messageType == MessageType.LONG_TEXT.getCode()) {//长文本
                    uploadLongText(holder, terminalMessage);
                } else if (messageType == MessageType.POSITION.getCode()) {//定位
                    //                        sendLocationMessage(terminalMessage);
                    //这里将之前的元素移除(发送失败的位置)，重新调取发送位置的方法
                    datas.remove(position);
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverSendFileHandler.class, ReceiverSendFileHandler.LOCATION);
                } else if (messageType == MessageType.PICTURE.getCode()
                        || messageType == MessageType.FILE.getCode()
                        ||messageType == MessageType.VIDEO_CLIPS.getCode()
                        ||messageType == MessageType.AUDIO.getCode()) {//图片、文件
                    terminalMessage.messageBody.put(JsonParam.SEND_STATE, MessageSendStateEnum.SENDING);
                    progressPercentMap.put(terminalMessage.messageBody.getIntValue(JsonParam.TOKEN_ID), 0);
                    setProgress(holder.progressBar, 0);
                    setText(holder.tv_progress, "0%");
                    setViewVisibility(holder.progressBar, View.VISIBLE);
                    setViewVisibility(holder.tv_progress, View.VISIBLE);
                    uploadMessages.add(terminalMessage);

                    uploadFileDelay();
                }
            });
        }
        if (viewType == MESSAGE_SHORT_TEXT_RECEIVED || viewType == MESSAGE_SHORT_TEXT_SEND) {
            holder.tvContent.setOnTouchListener((view, motionEvent) -> {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    return mIsLongClick;
                } else if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    mIsLongClick = false;
                    return false;
                }
                return false;
            });
            holder.tvContent.setOnLongClickListener(view -> {
                if(upload){
                    ToastUtil.showToast(activity,activity.getString(R.string.text_in_upload_can_not_forward));
                    return false;
                }
                mIsLongClick = true;
                //处理长按事件
                transponMessage = (TerminalMessage) terminalMessage.clone();
//                new TranspondDialog(activity, terminalMessage).showView();
                return true;
            });
        }
        //合并转发  是否选择
        holder.cbForward.setOnCheckedChangeListener((buttonView, isChecked) -> {
            terminalMessage.isForward = isChecked;
        });
    }


    /**
     * 是否是接收的消息
     */
    private boolean isReceiver(TerminalMessage terminalMessage) {
        return (terminalMessage.messageFromId != MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0));
    }

    /**
     * 设置数据
     */
    private void handleData(ChatViewHolder holder, int viewType, final TerminalMessage terminalMessage, int position) {
        JSONObject messageBody = terminalMessage.messageBody;
        if (messageBody == null) {
            logger.error("messageBody 空了" + messageBody);
            return;
        }

        handlerTime(terminalMessage, position, holder);
        handlerAvatar(terminalMessage, position, holder);

        if (isGroupChat && isReceiver(terminalMessage)) {
            setViewVisibility(holder.tvNick, View.VISIBLE);
        } else if (!isGroupChat && isReceiver(terminalMessage)) {
            setViewVisibility(holder.tvNick, View.GONE);
        }

        setText(holder.tvNick, TerminalMessageUtil.getMemberName(terminalMessage));

        /**  短文本  */
        if (terminalMessage.messageType == MessageType.SHORT_TEXT.getCode()) {
            setText(holder.tvContent, messageBody.getString(JsonParam.CONTENT));
        }
        /**  长文本  */
        if (terminalMessage.messageType == MessageType.LONG_TEXT.getCode()) {
            setLongText(terminalMessage, holder);
        }
        /**  图片  */
        if (terminalMessage.messageType == MessageType.PICTURE.getCode()) {
            if (terminalMessage.messagePath.startsWith("http")) {
                PhotoUtils.getInstance().loadNetBitmap(activity, terminalMessage.messagePath, holder.ivContent, holder.tv_progress, holder.progressBar);
            } else {
                PhotoUtils.getInstance().loadLocalBitmap(activity, terminalMessage.messagePath, holder.ivContent);
            }
        }
        /** 小视频 */
        if (terminalMessage.messageType == MessageType.VIDEO_CLIPS.getCode()) {
            long videoTime = 0;
            try{
                videoTime = messageBody.getLong(JsonParam.VIDEO_TIME );

            }catch(Exception e){
                e.printStackTrace();
            }
            if(videoTime >0){
                setText(holder.tvDuration, videoTime/1000 + "''");
            }
            if(terminalMessage.messageBody.containsKey(JsonParam.PICTURE_THUMB_URL)){
                String pictureThumbUrl = terminalMessage.messageBody.getString(JsonParam.PICTURE_THUMB_URL);
                if(pictureThumbUrl.startsWith("http")){
                    PhotoUtils.getInstance().loadNetBitmap2(activity, pictureThumbUrl, holder.ivContent);
                }else {
                    PhotoUtils.getInstance().loadLocalBitmap(activity, pictureThumbUrl, holder.ivContent);
                }
            }

        }
        /**  文件  */
        if (terminalMessage.messageType == MessageType.FILE.getCode()) {
            handleFileData(terminalMessage, holder, viewType);
        }
        /**  组呼--录音条目  */
        if (terminalMessage.messageType == MessageType.GROUP_CALL.getCode()
                || terminalMessage.messageType == MessageType.AUDIO.getCode()) {
            if(messageBody.containsKey(JsonParam.END_TIME) && messageBody.containsKey(JsonParam.START_TIME)){

                double length = (messageBody.getLong(JsonParam.END_TIME) - messageBody.getLong(JsonParam.START_TIME)) / 1000.00;
                BigDecimal b = new BigDecimal(new Double(length).toString());
                long voiceLength = b.setScale(0, BigDecimal.ROUND_HALF_UP).longValue();
                voiceLength = voiceLength < 1 ? 1 : voiceLength;
                voiceLength = voiceLength > 60 ? 60 : voiceLength;
                setText(holder.tvDuration, voiceLength + "''");
            }
            playGroupVoice(position, holder,terminalMessage);
            if (isReceiver(terminalMessage)) {
                if (messageBody.containsKey(JsonParam.UNREAD) &&
                        messageBody.getBooleanValue(JsonParam.UNREAD)) {
                    setViewVisibility(holder.ivUnread, View.VISIBLE);
                } else {
                    setViewVisibility(holder.ivUnread, View.INVISIBLE);
                }
            }
        }
        /**  个呼条目  */
        if (terminalMessage.messageType == MessageType.PRIVATE_CALL.getCode()) {
            handlerPrivateCallData(terminalMessage, holder,position);
        }
        /**  直播接收条目  */
        if (viewType == MESSAGE_VIDEO_LIVE_RECEIVED || viewType == MESSAGE_VIDEO_LIVE_SEND) {
            handlerLiveData(terminalMessage, holder);
        }
        if(viewType == MESSAGE_GB28181_RECODE_RECEIVED || viewType == MESSAGE_GB28181_RECODE_SEND){
            handleGB28181Data(terminalMessage, holder);
        }
        /**  定位条目 */
        if (terminalMessage.messageType == MessageType.POSITION.getCode()) {
            setText(holder.tvContent, "位置");
        }

        if (terminalMessage.messageType == MessageType.HYPERLINK.getCode()) {
            handleHyperlinkData(terminalMessage, holder);
        }
        //合并转发
        if(terminalMessage.messageType == MessageType.MERGE_TRANSMIT.getCode()){
            handleMergeTransmitData(terminalMessage, holder);
        }

        if (terminalMessage.messageType == MessageType.PRIVATE_CALL.getCode()
                || terminalMessage.messageType == MessageType.VIDEO_LIVE.getCode()) {
            return;
        }
        /**  消息是否发送失败 **/
        if (!isReceiver(terminalMessage)) {//发送方
            if (terminalMessage.resultCode != 0) {
                setViewVisibility(holder.ivMsgStatus, View.VISIBLE);
                setViewVisibility(holder.progressBar, View.GONE);
                setViewVisibility(holder.tv_progress, View.GONE);
            } else {
                setViewVisibility(holder.ivMsgStatus, View.GONE);
            }
        }

        /***  组或者用户被删除显示提示语 ***/
        if (position == datas.size() - 1 && !isReceiver(terminalMessage)) {
            if (terminalMessage.resultCode == SignalServerErrorCode.NO_GROUP_AUTHORITY.getErrorCode()) {//组被删除
                setViewVisibility(holder.tv_error_delete, View.VISIBLE);
                setText(holder.tv_error_delete, activity.getString(R.string.text_member_has_no_authority_in_this_group));
            } else if (terminalMessage.resultCode == SignalServerErrorCode.MEMBER_NOT_HAVE_THIS_MEMBER.getErrorCode()) {//用户被删除
                setViewVisibility(holder.tv_error_delete, View.VISIBLE);
                setText(holder.tv_error_delete, activity.getString(R.string.text_the_user_does_not_exist));
            } else {
                setViewVisibility(holder.tv_error_delete, View.GONE);
            }
        }
    }

    private void handleFileData(TerminalMessage terminalMessage, ChatViewHolder holder, int viewType) {
        JSONObject messageBody = terminalMessage.messageBody;
        String fileName = messageBody.getString(JsonParam.FILE_NAME);
        int res = FileIcons.smallIcon(fileName);
        holder.iv_temp.setImageDrawable(activity.getResources().getDrawable(res));
        setText(holder.tvContent, messageBody.getString(JsonParam.FILE_NAME));
        String fileSize = FileUtil.getFileSzie(messageBody.getLong(JsonParam.FILE_SIZE));
        setText(holder.tvFileSize, fileSize);
        /***  当前条目正在下载就显示进度条， 否则隐藏 **/
        if (viewType == MESSAGE_FILE_RECEIVED) {
            if (messageBody.containsKey(JsonParam.IS_DOWNLOADINF)
                    && messageBody.getBooleanValue(JsonParam.IS_DOWNLOADINF)) {
                setViewVisibility(holder.progressBar, View.VISIBLE);
                setViewVisibility(holder.tv_progress, View.VISIBLE);
            } else {
                setViewVisibility(holder.progressBar, View.GONE);
                setViewVisibility(holder.tv_progress, View.GONE);
            }
        }
    }


    /**
     * 设置人脸识别数据
     ***/
    private void handleHyperlinkData(TerminalMessage terminalMessage, ChatViewHolder holder) {
        int code = terminalMessage.messageBody.getIntValue(JsonParam.CODE);
        if (code == 0) {
            setViewVisibility(holder.lv_face_pair, View.VISIBLE);
            setViewVisibility(holder.tv_error_msg, View.GONE);
            File file = new File(terminalMessage.messagePath);
            if (file.exists() && file.length() > 0) {
                List<FaceRecognitionBean> list = new ArrayList<>();
                String arrayData = FileUtil.getStringFromFile(file);
                JSONObject object = JSONObject.parseObject(arrayData);
                JSONArray jsonArray = object.getJSONArray(JsonParam.DATA);
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject data = jsonArray.getJSONObject(i);
                    FaceRecognitionBean faceRecognitionBean = new FaceRecognitionBean();
                    faceRecognitionBean.setTitle(data.getString(JsonParam.TITLE));
                    faceRecognitionBean.setMatcheDegree(data.getString(JsonParam.DESCRIBE));
                    faceRecognitionBean.setPictureUrl(data.getString(JsonParam.PICTURE_URL));
                    faceRecognitionBean.setDetailedHtml(data.getString(JsonParam.PARTICULAR_HTML));
                    faceRecognitionBean.setContent(data.getString(JsonParam.CONTENT));
                    list.add(faceRecognitionBean);
                }
                FaceRecognitionAdapter adapter = new FaceRecognitionAdapter(activity, list);
                holder.lv_face_pair.setAdapter(adapter);
                setListViewHeightOnChildren(holder);
            } else {
                setViewVisibility(holder.lv_face_pair, View.GONE);
                setViewVisibility(holder.tv_error_msg, View.VISIBLE);
                setText(holder.tv_error_msg, activity.getString(R.string.text_unrecognized_face));
            }

        } else {
            String errorMsg = terminalMessage.messageBody.getString(JsonParam.MSG);
            setViewVisibility(holder.lv_face_pair, View.GONE);
            setViewVisibility(holder.tv_error_msg, View.VISIBLE);
            setText(holder.tv_error_msg, errorMsg);
        }
    }
    /**
     * 合并转发
     */
    private void handleMergeTransmitData(TerminalMessage terminalMessage, ChatViewHolder holder) {
        JSONObject messageBody = terminalMessage.messageBody;
        //标题
        if(messageBody.containsKey(JsonParam.CONTENT)){
            holder.tv_title.setText(messageBody.getString(JsonParam.CONTENT));
        }else{
            holder.tv_title.setText(activity.getString(R.string.chat_record));
        }
        //内容
        if(messageBody.containsKey(JsonParam.NOTE_LIST)){
            JSONArray jsonArray = messageBody.getJSONArray(JsonParam.NOTE_LIST);
            StringBuffer sb = new StringBuffer();
            if(jsonArray!=null&&jsonArray.size()>0){
                int size = (jsonArray.size()>3)?3:jsonArray.size();
                for(int i = 0; i < size; i++){
                    sb.append(jsonArray.get(i));
                    if(i != (size-1)){
                        sb.append("\n");
                    }
                }
            }
            holder.tv_content.setText(sb.toString());
        }else{
            holder.tv_content.setText(activity.getString(R.string.chat_record));
        }
    }

    /**
     * 显示全部条目
     **/
    private void setListViewHeightOnChildren(ChatViewHolder holder) {
        ListAdapter listAdapter = holder.lv_face_pair.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View item = listAdapter.getView(i, null, holder.lv_face_pair);
            item.measure(0, 0);
            totalHeight += item.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = holder.lv_face_pair.getLayoutParams();
        params.height = totalHeight + (holder.lv_face_pair.getDividerHeight() * listAdapter.getCount());
        holder.lv_face_pair.setLayoutParams(params);
    }

    private void handleGB28181Data(TerminalMessage terminalMessage, ChatViewHolder holder){
        JSONObject messageBody = terminalMessage.messageBody;
        //设置默认上报视频者，防止数组下标越界异常
        if(messageBody.containsKey(JsonParam.DEVICE_NAME)){
            setText(holder.tvContent, messageBody.getString(JsonParam.DEVICE_NAME));
        }
        holder.live_bubble.setVisibility(View.GONE);
        if(null != holder.ll_botoom_to_watch){
            holder.ll_botoom_to_watch.setVisibility(View.GONE);
        }
        holder.iv_image.setImageResource(R.drawable.law_recoder_image);
    }
    /***  设置图像观看数据 **/
    private void handlerLiveData(TerminalMessage terminalMessage, ChatViewHolder holder) {
        JSONObject messageBody = terminalMessage.messageBody;
        String liver = messageBody.getString(JsonParam.LIVER);
        //        int liverNo = Util.stringToInt(messageBody.getString(JsonParam.LIVERNO));
        long uniqueNo = MyTerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO, 0l);
        long liverUniqueNo = 0L;
        String[] split = null;
        if(!TextUtils.isEmpty(liver)){
            split = liver.split("_");
            liverUniqueNo = Util.stringToLong(split[0]);
        }
        //设置默认上报视频者，防止数组下标越界异常
        //        int memberNo = terminalMessage.messageFromId;
        //        if(split.length>0){
        //            memberNo = Integer.valueOf(split[0]);
        //        }
        //上报主题，如果没有就取上报者的名字
        liveTheme = messageBody.getString(JsonParam.TITLE);
        if(TextUtils.isEmpty(liveTheme)){
            if(split!=null){
                if(split.length>1){
                    String memberName = split[1];
                    liveTheme = String.format(activity.getString(R.string.current_push_member),memberName);
                    setText(holder.tvContent, liveTheme);
                }else {
                    long finalLiverUniqueNo = liverUniqueNo;
                    TerminalFactory.getSDK().getThreadPool().execute(() -> {
                        Member member = cn.vsx.hamster.terminalsdk.tools.DataUtil.getMemberByUniqueNo(finalLiverUniqueNo,true);
                        String name = (member!=null)?member.getName():terminalMessage.messageFromName;
                        myHandler.post(() -> {
                            setText(holder.tvContent, String.format(activity.getString(R.string.current_push_member),name));
                        });
                    });
                }
            }
        }

        if(!messageBody.containsKey(JsonParam.REMARK)){
            setViewVisibility(holder.reBubble, View.VISIBLE);
            setViewVisibility(holder.live_bubble, View.GONE);
            return;
        }
        if (messageBody.getInteger(JsonParam.REMARK) == Remark.INFORM_TO_WATCH_LIVE) {//被邀请去观看图像
            setViewVisibility(holder.reBubble, View.VISIBLE);
            setViewVisibility(holder.ll_botoom_to_watch, View.VISIBLE);
            setViewVisibility(holder.tv_watch_time, View.GONE);
            setViewVisibility(holder.live_bubble, View.GONE);
        } else if (messageBody.getInteger(JsonParam.REMARK) == Remark.LIVE_WATCHING_END) {//结束观看图像
            //            setViewVisibility(holder.ll_botoom_to_watch, View.GONE);
            setViewVisibility(holder.reBubble, View.GONE);
            //            setViewVisibility(holder.tv_watch_time, View.VISIBLE);
            setViewVisibility(holder.live_bubble, View.VISIBLE);

            if (terminalMessage.resultCode == SignalServerErrorCode.MEMBER_REFUSE.getErrorCode()) {//拒绝
                setText(holder.live_tv_chatcontent, activity.getString(R.string.refused));
            } else if (terminalMessage.resultCode == 0) {
                setViewVisibility(holder.reBubble, View.VISIBLE);
                setViewVisibility(holder.ll_botoom_to_watch, View.GONE);
                setViewVisibility(holder.tv_watch_time, View.VISIBLE);
                setViewVisibility(holder.live_bubble, View.GONE);
                if (messageBody.containsKey(JsonParam.END_WATCH_TIME) && messageBody.containsKey(JsonParam.START_WATCH_TIME)) {
                    long watchTime = (messageBody.getLong(JsonParam.END_WATCH_TIME) - messageBody.getLong(JsonParam.START_WATCH_TIME)) / 1000;
                    watchTime = watchTime < 1 ? 1 : watchTime;
                    setText(holder.tv_watch_time, String.format(activity.getString(R.string.text_finish_watching_time),getCallLength(watchTime)));
                } else {
                    setText(holder.tv_watch_time, activity.getString(R.string.text_finish_watching));
                }
            }
        } else if (messageBody.getInteger(JsonParam.REMARK) == Remark.ASK_VIDEO_LIVE) {
            logger.info("sjl_JsonParam.REMARK:" + messageBody.getInteger(JsonParam.REMARK) + ",REQUIRE_VIDEO_LIVE：" + Remark.ASK_VIDEO_LIVE);
            setViewVisibility(holder.reBubble, View.GONE);
            setViewVisibility(holder.live_bubble, View.VISIBLE);

            if (!Util.isEmpty(liver)) {
                if (terminalMessage.resultCode == SignalServerErrorCode.VIDEO_LIVE_WAITE_TIMEOUT.getErrorCode()) {//超时

                    if (liverUniqueNo == uniqueNo) {
                        setText(holder.live_tv_chatcontent, activity.getString(R.string.text_not_accepted));
                    } else {
                        setText(holder.live_tv_chatcontent, activity.getString(R.string.other_no_answer));
                    }

                } else if (terminalMessage.resultCode == SignalServerErrorCode.SLAVE_BUSY.getErrorCode()) {
                    if (liverUniqueNo == uniqueNo) {
                        setText(holder.live_tv_chatcontent, activity.getString(R.string.text_not_accepted));
                    } else {
                        setText(holder.live_tv_chatcontent, activity.getString(R.string.text_busy_party));
                    }
                } else if (terminalMessage.resultCode == SignalServerErrorCode.MEMBER_IS_KILLED.getErrorCode()) {//主叫或被叫被遥弊
                    if (liverUniqueNo == uniqueNo) {
                        setText(holder.live_tv_chatcontent, activity.getString(R.string.text_not_accepted));
                    } else {
                        setText(holder.live_tv_chatcontent, activity.getString(R.string.other_no_answer));
                    }
                } else if (terminalMessage.resultCode == SignalServerErrorCode.CALLED_MEMBER_OFFLINE.getErrorCode()) {
                    if (liverUniqueNo == uniqueNo) {
                        setText(holder.live_tv_chatcontent, activity.getString(R.string.text_not_accepted));
                    } else {
                        setText(holder.live_tv_chatcontent, SignalServerErrorCode.CALLED_MEMBER_OFFLINE.getErrorDiscribe());
                    }
                } else if (terminalMessage.resultCode == SignalServerErrorCode.TERMINAL_OFFLINE_LOGOUT.getErrorCode()) {
                    if (liverUniqueNo == uniqueNo) {
                        setText(holder.live_tv_chatcontent, activity.getString(R.string.text_not_accepted));
                    } else {
                        setText(holder.live_tv_chatcontent, activity.getString(R.string.text_the_other_party_is_not_online));
                    }
                } else if (terminalMessage.resultCode == SignalServerErrorCode.MEMBER_REFUSE.getErrorCode()) {
                    if (liverUniqueNo == uniqueNo) {
                        setText(holder.live_tv_chatcontent, activity.getString(R.string.refused));
                    } else {
                        setText(holder.live_tv_chatcontent, activity.getString(R.string.text_the_other_party_has_refus));
                    }

                } else if (liverUniqueNo == uniqueNo) {
                    setText(holder.live_tv_chatcontent, activity.getString(R.string.text_not_accepted));
                }
            } else {
                setText(holder.live_tv_chatcontent, activity.getString(R.string.refused));
            }
        } else if (messageBody.getInteger(JsonParam.REMARK) == Remark.STOP_ASK_VIDEO_LIVE) {
            setViewVisibility(holder.reBubble, View.GONE);
            setViewVisibility(holder.live_bubble, View.VISIBLE);
            if (terminalMessage.resultCode == SignalServerErrorCode.STOP_ASK_VIDEO_LIVE.getErrorCode()) {
                if ((liverUniqueNo == uniqueNo)) {
                    setText(holder.live_tv_chatcontent, activity.getString(R.string.text_not_accepted));
                } else {
                    setText(holder.live_tv_chatcontent, activity.getString(R.string.canceled));
                }
            }
        }
    }

    /***  设置个呼数据 **/
    private void handlerPrivateCallData(TerminalMessage terminalMessage, ChatViewHolder holder,int position) {
        JSONObject messageBody = terminalMessage.messageBody;
        logger.info("sjl_:" + terminalMessage.resultCode + "," + SignalServerErrorCode.INDIVIDUAL_CALL_WAITE_TIMEOUT.getErrorCode());
        holder.vLine.setVisibility(View.GONE);
        holder.llCallListener.setVisibility(View.GONE);
        if (terminalMessage.resultCode == 0) {
            if (!messageBody.containsKey(JsonParam.CALLID) || messageBody.getLong(JsonParam.CALLID) == 0) {//主叫接通前挂断
                if (isReceiver(terminalMessage)) {
                    setText(holder.tvContent, activity.getString(R.string.other_cancel));
                } else {
                    setText(holder.tvContent, activity.getString(R.string.canceled));
                }
            } else {//正常通话
                long callLength = (messageBody.getLong(JsonParam.END_TIME) - messageBody.getLong(JsonParam.START_TIME)) / 1000;
                callLength = callLength < 1 ? 1 : callLength;
                setText(holder.tvContent, String.format(activity.getString(R.string.text_call_time),getCallLength(callLength)));
                holder.vLine.setVisibility(View.VISIBLE);
                holder.llCallListener.setVisibility(View.VISIBLE);
                playGroupVoice(position, holder,terminalMessage);
            }
        } else if (terminalMessage.resultCode == SignalServerErrorCode.INDIVIDUAL_CALL_WAITE_TIMEOUT.getErrorCode()) {//请求个呼超时
            if (isReceiver(terminalMessage)) {
                setText(holder.tvContent, activity.getString(R.string.no_answer));
            } else {
                setText(holder.tvContent, activity.getString(R.string.other_no_answer));
            }
        } else if (terminalMessage.resultCode == SignalServerErrorCode.SLAVE_BUSY.getErrorCode()) {//被叫繁忙
            if (isReceiver(terminalMessage)) {
                setText(holder.tvContent, activity.getString(R.string.no_answer));
            } else {
                setText(holder.tvContent, activity.getString(R.string.text_busy_party));
            }
        } else if (terminalMessage.resultCode == SignalServerErrorCode.MEMBER_REFUSE.getErrorCode()) {//被叫拒绝
            if (isReceiver(terminalMessage)) {
                setText(holder.tvContent, activity.getString(R.string.refused));
            } else {
                setText(holder.tvContent, activity.getString(R.string.text_the_other_party_has_refus));
            }
        } else if (terminalMessage.resultCode == SignalServerErrorCode.TERMINAL_OFFLINE_LOGOUT.getErrorCode()) {//主叫掉线
            if (isReceiver(terminalMessage)) {
                setText(holder.tvContent, activity.getString(R.string.no_answer));
            } else {
                setText(holder.tvContent, activity.getString(R.string.canceled));
            }
        } else if (terminalMessage.resultCode == SignalServerErrorCode.MEMBER_IS_KILLED.getErrorCode()) {//主叫或被叫被遥弊
            if (isReceiver(terminalMessage)) {
                setText(holder.tvContent, activity.getString(R.string.other_cancel));
            } else {
                setText(holder.tvContent, activity.getString(R.string.canceled));
            }
        } else {//被呼叫方不在线
            if (isReceiver(terminalMessage)) {
                setText(holder.tvContent, activity.getString(R.string.no_answer));
            } else {
                setText(holder.tvContent, activity.getString(R.string.text_the_other_party_is_not_online));
            }
        }
    }

    /**
     * 设置头像显示
     */
    private void handlerAvatar(TerminalMessage terminalMessage, int position, ChatViewHolder holder) {
        if(activity!=null&&!activity.isDestroyed()){
            Glide.with(activity)
                    .load(R.mipmap.img_police)
//                    .asBitmap()
//                    .placeholder(com.zectec.imageandfileselector.R.drawable.user_photo)//加载中显示的图片
//                    .error(com.zectec.imageandfileselector.R.drawable.user_photo)//加载失败时显示的图片
                    .into(holder.ivAvatar);
        }
    }

    /**
     * 设置消息时间显示
     */
    private void handlerTime(TerminalMessage terminalMessage, int position, ChatViewHolder holder) {
        if (terminalMessage.sendTime > 0) {
            long messageTime = terminalMessage.sendTime;
            if (position == 0) {
                setText(holder.timeStamp, DateUtils.getNewChatTime(messageTime));
                setViewVisibility(holder.timeStamp, View.VISIBLE);
            } else {
                // 两条消息时间离得如果稍长，显示时间
                long currentTime = terminalMessage.sendTime;
                long lastTime = 0L;
                if (datas.get(position - 1).sendTime > 0) {
                    lastTime = datas.get(position - 1).sendTime;

                } else {
                    lastTime = currentTime;
                }
                handlerTime2(holder, currentTime, lastTime);
            }
        } else {
            long currentTime = System.currentTimeMillis();
            if (position == 0) {
                setText(holder.timeStamp, DateUtils.getNewChatTime(currentTime));
                setViewVisibility(holder.timeStamp, View.VISIBLE);
            } else {
                long lastTime = 0L;
                if (datas.get(position - 1).sendTime > 0) {
                    lastTime = datas.get(position - 1).sendTime;
                } else {
                    lastTime = currentTime;
                }
                handlerTime2(holder, currentTime, lastTime);
            }

        }
    }

    private void handlerTime2(ChatViewHolder holder, long currentTime, long lastTime) {
        if (currentTime - lastTime <= 0) {
            setViewVisibility(holder.timeStamp, View.GONE);
        } else {
            if (DateUtils.isCloseEnough(currentTime, lastTime)) {
                setViewVisibility(holder.timeStamp, View.GONE);
            } else {
                setText(holder.timeStamp, DateUtils.getNewChatTime(currentTime));
                setViewVisibility(holder.timeStamp, View.VISIBLE);
            }
        }
    }

    private String getCallLength(long time) {
        int hours = (int) Math.floor(time / 3600);
        time -= hours * 3600;
        int minutes = (int) Math.floor(time / 60);
        time -= minutes * 60;
        int sec = (int) time;

        String timeStr = "";
        if (hours != 0) {
            if (hours >= 10) {
                timeStr = hours + ":" + minutes + ":" + sec + "";
            } else{
                timeStr = "0" + hours + ":" + minutes + ":" + sec + "";
            }
        } else if (minutes != 0) {
            if(minutes >= 10){
                if(sec >= 10){
                    timeStr = minutes + ":" + sec ;
                }else {
                    timeStr = minutes + ":" + "0"+sec ;
                }
            }else {
                if(sec >= 10){
                    timeStr = "0"+minutes + ":" + sec ;
                }else {
                    timeStr = "0"+minutes + ":" + "0"+sec ;
                }
            }
        } else {
            timeStr = +sec + "秒";
        }

        return timeStr;
    }

    /**
     * 显示长文本
     **/
    private void setLongText(TerminalMessage terminalMessage, ChatViewHolder holder) {
        String path = terminalMessage.messagePath;
        File file = new File(path);
        if (!file.exists()) {
            MyTerminalFactory.getSDK().getTerminalMessageManager().setMessagePath(terminalMessage, false);
            //下载这条消息 并 监听进度
            MyTerminalFactory.getSDK().download(terminalMessage, true);
        }else {
            String content = FileUtil.getStringFromFile(file);
            if (TextUtils.isEmpty(content)) {
                content = activity.getString(R.string.text_get_no_content);
            }
            setText(holder.tvContent, content);
        }
    }

    /***  播放组呼录音相关改变 **/
    private void playGroupVoice(int position, ChatViewHolder holder, TerminalMessage terminalMessage) {
        if (holder.iv_voice_image_anim != null){
            if(terminalMessage.messageType == MessageType.PRIVATE_CALL.getCode()&&terminalMessage.isDownLoadAudio){
                //显示下载中的UI
                setViewVisibility(holder.ivVoice, View.VISIBLE);
                setViewVisibility(holder.iv_voice_image_anim, View.GONE);
                if(holder.tvStatus!=null){
                    holder.tvStatus.setText(R.string.down_loading);
                }
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.ivVoice.getLayoutParams();
                lp.width = DensityUtil.dip2px(activity,25);
                lp.height = DensityUtil.dip2px(activity,25);
                holder.ivVoice.setLayoutParams(lp);
//                Glide.with(PadApplication.getPadApplication().getApplicationContext()).load(R.drawable.gif_download).asGif().dontAnimate().into(holder.ivVoice);

            }else{
                //显示播放的UI
                if(holder.tvStatus!=null){
                    holder.tvStatus.setText(R.string.play_audio);
                }
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.ivVoice.getLayoutParams();
                lp.width = DensityUtil.dip2px(activity,15);
                lp.height = DensityUtil.dip2px(activity,25);
                holder.ivVoice.setLayoutParams(lp);
                holder.ivVoice.setImageResource(isReceiver(terminalMessage)?R.drawable.sound_item:R.drawable.sound_item_right);
                if(MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_LISTEN.name())) {
                    AnimationDrawable animationDrawable = (AnimationDrawable) holder.iv_voice_image_anim.getBackground();
                    if (mposition == position) {
                        setUnread(position);
                        if (isSameItem) {
                            if (isPlaying) {
                                setViewVisibility(holder.ivVoice, View.GONE);
                                setViewVisibility(holder.iv_voice_image_anim, View.VISIBLE);
                                animationDrawable.start();
                            } else {
                                animationDrawable.stop();
                                setViewVisibility(holder.iv_voice_image_anim, View.GONE);
                                setViewVisibility(holder.ivVoice, View.VISIBLE);
                            }
                        } else {//不同条目
                            setViewVisibility(holder.ivVoice, View.GONE);
                            setViewVisibility(holder.iv_voice_image_anim, View.VISIBLE);
                            animationDrawable.start();
                        }
                    } else {
                        animationDrawable.stop();
                        setViewVisibility(holder.iv_voice_image_anim, View.GONE);
                        setViewVisibility(holder.ivVoice, View.VISIBLE);
                    }
                }else {
                    if(terminalMessage.messageType == MessageType.GROUP_CALL.getCode()){
                        setViewVisibility(holder.iv_voice_image_anim, View.GONE);
                        setViewVisibility(holder.ivVoice, View.VISIBLE);
                        if (isReceiver(terminalMessage)) {
                            holder.ivVoice.setImageResource(R.drawable.iv_voice_image_off);
                        }else{
                            holder.ivVoice.setImageResource(R.drawable.iv_voice_image_off_self);
                        }
                    }
                }
            }

        }
    }

    /**
     * 点击播放之后改变数据库状态
     **/
    private void setUnread(int position) {
        TerminalMessage terminalMessage = datas.get(position);
        if (terminalMessage.messageBody.containsKey(JsonParam.UNREAD) &&
                terminalMessage.messageBody.getBooleanValue(JsonParam.UNREAD)) {
            terminalMessage.messageBody.put(JsonParam.UNREAD, false);
            MyTerminalFactory.getSDK().getSQLiteDBManager().updateTerminalMessage(terminalMessage);
        }
    }

    /**
     * 发送短文本消息到信令
     */
    private void sendShortTextMessage(TerminalMessage terminalMessage) {
        List<Integer> toIds = setToIds(terminalMessage);
        sendShortTextMessage2(terminalMessage, toIds);
    }

    private void sendShortTextMessage2(TerminalMessage terminalMessage, List<Integer> toIds) {
        TerminalMessage terminalMessage1 = (TerminalMessage) terminalMessage.clone();
        String msg = terminalMessage.messageBody.getString(JsonParam.CONTENT);
        terminalMessage1.messageToId = toIds.get(0);
        MyTerminalFactory.getSDK().getTerminalMessageManager().uploadDataByDDPUSH("", terminalMessage1);
    }

    /**
     * 上传长文本消息
     */
    private void uploadLongText(ChatViewHolder chatViewHolder, TerminalMessage terminalMessage) {
        File file = new File(terminalMessage.messagePath);
        terminalMessage.messageToId = setToIds(terminalMessage).get(0);
        upload = true;
        MyTerminalFactory.getSDK().upload(MyTerminalFactory.getSDK().getParam(Params.FILE_UPLOAD_URL), file, terminalMessage, false);
    }

    /**
     * 发送定位到信令
     */
    private void sendLocationMessage(TerminalMessage terminalMessage) {
        List<Integer> toIds = setToIds(terminalMessage);
        sendLocationMessage2(terminalMessage, toIds);
    }

    /***  转发定位消息 **/
    private void transponLocationMessage(TerminalMessage terminalMessage, List<Integer> list,List<Long> toUniqueNos,PushMessageSendResultHandler pushMessageSendResultHandler) {
        TerminalMessage terminalMessage1 = (TerminalMessage) terminalMessage.clone();
        MyTerminalFactory.getSDK().getTerminalMessageManager().uploadDataByDDPUSH(terminalMessage.messageUrl, terminalMessage1.messageType,terminalMessage1.messageBody.toJSONString(),list,toUniqueNos,pushMessageSendResultHandler);
    }

    private void sendLocationMessage2(TerminalMessage terminalMessage, List<Integer> toIds) {
        TerminalMessage terminalMessage1 = (TerminalMessage) terminalMessage.clone();
        terminalMessage1.messageToId = toIds.get(0);
        MyTerminalFactory.getSDK().getTerminalMessageManager().uploadDataByDDPUSH(terminalMessage.messageUrl, terminalMessage1);
    }

    /**
     * 发送上报图像信令
     */
    private void sendVideoLiveMessage(TerminalMessage terminalMessage) {
        List<Integer> toIds = setToIds(terminalMessage);
        TerminalMessage terminalMessage1 = (TerminalMessage) terminalMessage.clone();
        terminalMessage1.messageToId = toIds.get(0);
        upload = true;
        MyTerminalFactory.getSDK().getTerminalMessageManager().uploadDataByDDPUSH("", terminalMessage1);
    }


    /**
     * 将长文本相关发送到信令服务
     **/
    public void sendLongTxtMessage(TerminalMessage terminalMessage) {
        List<Integer> toIds = setToIds(terminalMessage);
        sendLongTxtMessage2(terminalMessage, toIds);
    }

    /**
     * 转发长文本相关发送到信令服务
     **/
    public void transponLongTxtMessage(TerminalMessage terminalMessage,  List<Integer> list,List<Long> toUniqueNos,PushMessageSendResultHandler pushMessageSendResultHandler) {
        TerminalMessage terminalMessage1 = (TerminalMessage) terminalMessage.clone();
        MyTerminalFactory.getSDK().getTerminalMessageManager().uploadDataByDDPUSH(terminalMessage.messageUrl, terminalMessage1.messageType,terminalMessage1.messageBody.toJSONString(),list,toUniqueNos,pushMessageSendResultHandler);
    }

    public void sendLongTxtMessage2(TerminalMessage terminalMessage, List<Integer> toIds) {
        TerminalMessage terminalMessage1 = (TerminalMessage) terminalMessage.clone();
        terminalMessage1.messageToId = toIds.get(0);
        MyTerminalFactory.getSDK().getTerminalMessageManager().uploadDataByDDPUSH(terminalMessage.messageUrl, terminalMessage1);
    }

    /**
     * 将图片相关发送到信令服务
     **/
    public void sendPhotoMessage(TerminalMessage terminalMessage) {
        List<Integer> toIds = setToIds(terminalMessage);
        sendPhotoMessage2(terminalMessage, toIds);
    }

    /**
     * 转发图片消息
     **/
    private void transponPhotoMessage(TerminalMessage terminalMessage, List<Integer> list ,List<Long> toUniqueNos,PushMessageSendResultHandler pushMessageSendResultHandler) {
        if (terminalMessage.resultCode != 0) {//发送失败的文件消息进行转发
            terminalMessage.messageBody.put(JsonParam.SEND_STATE, MessageSendStateEnum.SENDING);
            terminalMessage.messageBody.put(JsonParam.ISMICROPICTURE, true);
            File file = new File(terminalMessage.messagePath);
            upload = true;
            MyTerminalFactory.getSDK().upload(MyTerminalFactory.getSDK().getParam(Params.IMAGE_UPLOAD_URL, ""), file, terminalMessage, false);
        } else {
            TerminalMessage terminalMessage1 = (TerminalMessage) terminalMessage.clone();
            MyTerminalFactory.getSDK().getTerminalMessageManager().uploadDataByDDPUSH("", terminalMessage1.messageType,terminalMessage1.messageBody.toJSONString(),list,toUniqueNos,pushMessageSendResultHandler);
        }
    }

    public void sendPhotoMessage2(TerminalMessage terminalMessage, List<Integer> toIds) {
        TerminalMessage terminalMessage1 = (TerminalMessage) terminalMessage.clone();
        terminalMessage1.messageToId = toIds.get(0);
        MyTerminalFactory.getSDK().getTerminalMessageManager().uploadDataByDDPUSH("", terminalMessage1);
    }

    /**
     * 将文件相关发送到信令服务
     **/
    public void sendFileMessage(TerminalMessage terminalMessage) {
        List<Integer> toIds = setToIds(terminalMessage);
        sendFileMessage2(terminalMessage, toIds);
    }

    /***  转发文件消息 **/
    private void transponFileMessage(TerminalMessage terminalMessage, List<Integer> list,List<Long> toUniqueNos,PushMessageSendResultHandler pushMessageSendResultHandler) {
        if (terminalMessage.resultCode != 0) {//发送失败的文件消息进行转发
            terminalMessage.messageBody.put(JsonParam.SEND_STATE, MessageSendStateEnum.SENDING);
            File file = new File(terminalMessage.messagePath);
            upload = true;
            MyTerminalFactory.getSDK().upload(MyTerminalFactory.getSDK().getParam(Params.FILE_UPLOAD_URL, ""), file, terminalMessage, false);
        } else {
            TerminalMessage terminalMessage1 = (TerminalMessage) terminalMessage.clone();
            MyTerminalFactory.getSDK().getTerminalMessageManager().uploadDataByDDPUSH(terminalMessage.messageUrl, terminalMessage1.messageType,terminalMessage1.messageBody.toJSONString(),list,toUniqueNos,pushMessageSendResultHandler);
        }
    }

    public void sendFileMessage2(TerminalMessage terminalMessage, List<Integer> toIds) {
        TerminalMessage terminalMessage1 = (TerminalMessage) terminalMessage.clone();
        terminalMessage1.messageToId = toIds.get(0);
        MyTerminalFactory.getSDK().getTerminalMessageManager().uploadDataByDDPUSH(terminalMessage.messageUrl, terminalMessage1);
    }

    /**
     * 将直播相关发送到信令服务
     **/
    private void sendLiveMessage(TerminalMessage terminalMessage) {
        List<Integer> toIds = setToIds(terminalMessage);
        sendLiveMessage2(terminalMessage, toIds);
    }

    /***   转发直播消息  **/
    private void transponLiveMessage(TerminalMessage terminalMessage, List<Integer> list,List<Long> toUniqueNos,PushMessageSendResultHandler pushMessageSendResultHandler) {
        TerminalMessage terminalMessage1 = (TerminalMessage) terminalMessage.clone();
        MyTerminalFactory.getSDK().getTerminalMessageManager().uploadDataByDDPUSH(terminalMessage.messageUrl, terminalMessage1.messageType,terminalMessage1.messageBody.toJSONString(),list,toUniqueNos,pushMessageSendResultHandler);
    }

    private void sendLiveMessage2(TerminalMessage terminalMessage, List<Integer> toIds) {
        TerminalMessage terminalMessage1 = (TerminalMessage) terminalMessage.clone();
        terminalMessage1.messageToId = toIds.get(0);

        MyTerminalFactory.getSDK().getTerminalMessageManager().uploadDataByDDPUSH(terminalMessage.messageUrl, terminalMessage1);
    }

    /**
     * 将组呼相关发送到信令服务
     **/
    private void sendGroupCallMessage(TerminalMessage terminalMessage) {
        List<Integer> toIds = setToIds(terminalMessage);
        sendGroupCallMessage2(terminalMessage, toIds);
    }

    /**
     * 转发组呼或录音消息
     **/
    private void transponGroupCallMessage(TerminalMessage terminalMessage, List<Integer> list,List<Long> toUniqueNos,PushMessageSendResultHandler pushMessageSendResultHandler) {
        TerminalMessage terminalMessage1 = (TerminalMessage) terminalMessage.clone();
        MyTerminalFactory.getSDK().getTerminalMessageManager().uploadDataByDDPUSH(terminalMessage.messageUrl, terminalMessage1.messageType,terminalMessage1.messageBody.toJSONString(),list,toUniqueNos,pushMessageSendResultHandler);
    }

    private void sendGroupCallMessage2(TerminalMessage terminalMessage, List<Integer> toIds) {
        TerminalMessage terminalMessage1 = (TerminalMessage) terminalMessage.clone();
        terminalMessage1.messageToId = toIds.get(0);
        MyTerminalFactory.getSDK().getTerminalMessageManager().uploadDataByDDPUSH("", terminalMessage1);
    }

    /**
     * 文件条目点击
     */
    private void fileItemClick(ChatViewHolder chatViewHolder, TerminalMessage terminalMessage, int type) {
        if (terminalMessage.messageType == MessageType.FILE.getCode()){
            openFile(terminalMessage, chatViewHolder);
        }
    }

    /**
     * 图片条目条目点击
     */
    private void photoItemClick(ChatViewHolder chatViewHolder, TerminalMessage terminalMessage, int type) {
        if (terminalMessage.messageType == MessageType.PICTURE.getCode()) {
            openPhoto(terminalMessage, chatViewHolder);
        }
    }

    /**
     * 点击视频条目点击
     */
    private void videoItemClick(ChatViewHolder chatViewHolder, TerminalMessage terminalMessage, int type) {
        if (terminalMessage.messageType == MessageType.VIDEO_CLIPS.getCode()) {
            if(MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_ACCEPT.name())){
                File file = new File(terminalMessage.messagePath);
                if (!file.exists()) {
                    // TODO: 2019/1/17 下载视频
                    isDownloading = true;
                    MyTerminalFactory.getSDK().getTerminalMessageManager().setMessagePath(terminalMessage, false);
                    //                    downloadProgressBar = chatViewHolder.progressBar;
                    //                    download_tv_progressBars = chatViewHolder.tv_progress;
                    //                    setProgress(downloadProgressBar, 0);
                    //                    setText(download_tv_progressBars, "0%");
                    //                    setViewVisibility(downloadProgressBar, View.VISIBLE);
                    //                    setViewVisibility(download_tv_progressBars, View.VISIBLE);
                    loadingView = chatViewHolder.loadingView;
                    loadingView.setProgerss(0);
                    //下载这条消息 并 监听进度
                    MyTerminalFactory.getSDK().download(terminalMessage, true);
                }else {
                    openVideo(terminalMessage,file);
                }
            }else {
                ToastUtil.showToast(activity,activity.getString(R.string.text_has_no_image_watch_authority));
            }
        }

    }

    /**
     * 点击个呼历史条目发送个呼
     */
    private void privateCallClick(TerminalMessage terminalMessage) {
        if(terminalMessage.messageType == MessageType.PRIVATE_CALL.getCode()){
            if (MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_CALL_PRIVATE.name())) {
                if (terminalMessage.messageType == MessageType.PRIVATE_CALL.getCode()) {

                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverIndividualCallFromMsgItemHandler.class);

                }
            } else {
                ToastUtil.showToast(activity, activity.getString(R.string.text_no_call_permission));
            }
        }

    }

    /**
     * 点击定位消息进入定位界面
     **/
    private void locationItemClick(TerminalMessage terminalMessage) {

        if (terminalMessage.messageType == MessageType.POSITION.getCode()) {
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverChatListItemClickHandler.class,
                    terminalMessage, isReceiver(terminalMessage));
        }

    }

    private void gb28181ItemClick(TerminalMessage terminalMessage, int viewType){
        if (terminalMessage.messageType == MessageType.GB28181_RECORD.getCode()) {
            if (MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_ACCEPT.name())){
                if(terminalMessage.messageBody.containsKey(JsonParam.GB28181_RTSP_URL)){
                    //gb28181Url = terminalMessage.messageBody.getString(JsonParam.GB28181_RTSP_URL);
                    String deviceId = terminalMessage.messageBody.getString(JsonParam.GB28181_RTSP_URL);
                    String gateWayUrl = TerminalFactory.getSDK().getParam(Params.GATE_WAY_URL);
                    List<MediaBean> liveUrls = new ArrayList<>();
                    MediaBean mediaBean = new MediaBean();
                    mediaBean.setUrl(gateWayUrl);
                    mediaBean.setStartTime("视频1");
                    liveUrls.add(mediaBean);
                    MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveGetHistoryLiveUrlsHandler2.class, BaseCommonCode.SUCCESS_CODE,liveUrls,deviceId,0);
                }else{
                    ToastUtil.showToast(activity, activity.getString(R.string.text_pull_data_error));
                }
            }else {
                ToastUtil.showToast(activity, activity.getString(R.string.text_has_no_image_receiver_authority));
            }
        } else if (terminalMessage.messageType == MessageType.OUTER_GB28181_RECORD.getCode()) {
            if (MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_ACCEPT.name())){
                if(terminalMessage.messageBody.containsKey(JsonParam.DEVICE_ID)){
                    final String deviceId = terminalMessage.messageBody.getString(JsonParam.DEVICE_ID);
                    TerminalFactory.getSDK().getDataManager().getHikvisionUrl(terminalMessage);
                }else{
                    ToastUtil.showToast(activity, activity.getString(R.string.text_pull_data_error));
                }
            }else {
                ToastUtil.showToast(activity, activity.getString(R.string.text_has_no_image_receiver_authority));
            }
        }
    }


    /**
     * 合并转发的点击事件
     * @param terminalMessage
     * @param viewType
     */
    private void mergeTransmit(TerminalMessage terminalMessage, int viewType) {
        if (terminalMessage.messageType == MessageType.MERGE_TRANSMIT.getCode()) {
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverChatListItemClickHandler.class,
                    terminalMessage, isReceiver(terminalMessage));
        }
    }



    /**
     * 点击图像接收条目
     **/
    private void liveItemClick(TerminalMessage terminalMessage, int viewType) {

        if(terminalMessage.messageType == MessageType.VIDEO_LIVE.getCode()){
            if (MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_ACCEPT.name())) {
                if ((viewType == MESSAGE_VIDEO_LIVE_RECEIVED || viewType == MESSAGE_VIDEO_LIVE_SEND) && terminalMessage.messageBody.getIntValue("remark") != 1) {
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverChatListItemClickHandler.class,
                            terminalMessage, isReceiver(terminalMessage));


                }
            } else {
                ToastUtil.showToast(activity, activity.getString(R.string.text_has_no_image_receiver_authority));
            }
        }

    }

    /**
     * 点击播放组呼或者录音
     **/
    public void groupCallItemClick(TerminalMessage terminalMessage, int position) {
        if(terminalMessage.messageType == MessageType.GROUP_CALL.getCode()){
            if (MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_LISTEN.name())) {
                if (PadApplication.getPadApplication().isPlayVoice) {
                    MyTerminalFactory.getSDK().getTerminalMessageManager().stopMultimediaMessage();
                }
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverReplayIndividualChatVoiceHandler.class, terminalMessage, position, PlayType.PLAY_GROUP_CALL.getCode());
                //                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverReplayGroupChatVoiceHandler.class, position);
            } else {
                ToastUtil.showToast(activity, activity.getString(R.string.text_has_no_group_call_listener_authority));
            }
        }

    }

    /**
     * 点击播放个呼录音
     **/
    public void individualNewsRecordItemClick(TerminalMessage terminalMessage, int position) {
        if (terminalMessage.messageType == MessageType.AUDIO.getCode()) {
            logger.error("个呼录音点击事件--->" + position);
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverReplayIndividualChatVoiceHandler.class, terminalMessage, position, PlayType.PLAY_AUDIO.getCode());
        }
    }



    private View getViewByType(int viewType, ViewGroup parent) {
        switch (viewType) {
            case MESSAGE_LONG_TEXT_RECEIVED:
            case MESSAGE_SHORT_TEXT_RECEIVED:
                return inflater.inflate(R.layout.row_received_message, parent, false);
            case MESSAGE_LONG_TEXT_SEND:
            case MESSAGE_SHORT_TEXT_SEND:
                return inflater.inflate(R.layout.row_sent_message, parent, false);
            case MESSAGE_IMAGE_RECEIVED:
                return inflater.inflate(R.layout.row_received_picture, parent, false);
            case MESSAGE_IMAGE_SEND:
                return inflater.inflate(R.layout.row_sent_picture, parent, false);
            case MESSAGE_GROUP_CALL_RECEIVED:
            case MESSAGE_VOICE_RECEIVED:
                return inflater.inflate(R.layout.row_received_voice, parent, false);
            case MESSAGE_GROUP_CALL_SEND:
            case MESSAGE_VOICE_SEND:
                return inflater.inflate(R.layout.row_sent_voice, parent, false);
            case MESSAGE_FILE_RECEIVED:
                return inflater.inflate(R.layout.row_received_file, parent, false);
            case MESSAGE_FILE_SEND:
                return inflater.inflate(R.layout.row_sent_file, parent, false);
            case MESSAGE_VEDIO_RECEIVED:
                return inflater.inflate(R.layout.row_received_video, parent, false);
            case MESSAGE_VEDIO_SEND:
                return inflater.inflate(R.layout.row_sent_video, parent, false);
            case MESSAGE_LOCATION_RECEIVED:
                return inflater.inflate(R.layout.row_received_location, parent, false);
            case MESSAGE_LOCATION_SEND:
                return inflater.inflate(R.layout.row_sent_location, parent, false);

            case MESSAGE_VIDEO_LIVE_RECEIVED:
            case MESSAGE_GB28181_RECODE_RECEIVED:
                return inflater.inflate(R.layout.row_receiver_live, parent, false);

            case MESSAGE_VIDEO_LIVE_SEND:
            case MESSAGE_GB28181_RECODE_SEND:
                return inflater.inflate(R.layout.row_send_live, parent, false);
            case MESSAGE_WARNING_INSTANCE_RECEIVED:
            case MESSAGE_WARNING_INSTANCE_SEND:
                return inflater.inflate(R.layout.layout_warning_message,parent,false);
            case MESSAGE_PRIVATE_CALL_RECEIVED:
                return inflater.inflate(R.layout.row_receiver_private_call, parent, false);
            case MESSAGE_PRIVATE_CALL_SEND:
                return inflater.inflate(R.layout.row_send_private_call, parent, false);
            case MESSAGE_HYPERLINK_RECEIVED:
                return inflater.inflate(R.layout.row_received_face, parent, false);
            case MESSAGE_HYPERLINK_SEND:
                return inflater.inflate(R.layout.row_received_face, parent, false);
            case MESSAGE_MERGE_TRANSMIT_RECEIVED:
                return inflater.inflate(R.layout.row_receiver_merge_transmit, parent, false);
            case MESSAGE_MERGE_TRANSMIT_SEND:
                return inflater.inflate(R.layout.row_send_merge_transmit, parent, false);
            case MESSAGE_AFFICHE:
                return inflater.inflate(R.layout.layout_affiche_message, parent, false);
            default:
                return inflater.inflate(R.layout.row_sent_message, parent, false);
        }
    }

    public void openPhoto(TerminalMessage terminalMessage, ChatViewHolder chatViewHolder) {
        //加载原图
        File file = new File(terminalMessage.messagePath);
        mImgList = findImages();
        logger.error("adapter ---getCount():" + getItemCount());
        logger.error("mImgList.size():" + mImgList.size());
        int currentPos = mImgUrlList.indexOf(terminalMessage.messagePath);
        logger.info("图片列表位置：" + currentPos+"路径："+terminalMessage.messagePath);
        if (!isDownloadingPicture){
            if(terminalMessage.messageBody.containsKey(JsonParam.ISMICROPICTURE) && terminalMessage.messageBody.getBooleanValue(JsonParam.ISMICROPICTURE)||
                    !file.exists()){
                logger.error("哎呀---------->本地图片被删了或为缩略图，重新去服务器下载！");
                isDownloadingPicture = true;
                MyTerminalFactory.getSDK().getTerminalMessageManager().setMessagePath(terminalMessage, false);
                downloadProgressBar = chatViewHolder.progressBar;
                download_tv_progressBars = chatViewHolder.tv_progress;
                //                setProgress(downloadProgressBar, 0);
                //                setText(download_tv_progressBars, "0%");
                //                setViewVisibility(downloadProgressBar, View.VISIBLE);
                setViewVisibility(download_tv_progressBars, View.VISIBLE);
                MyTerminalFactory.getSDK().download(terminalMessage, true);
            }else {
//                setViewVisibility(fragment_contener, View.VISIBLE);
                ImagePreviewItemFragment imagePreviewItemFragment = ImagePreviewItemFragment.getInstance(mImgList, currentPos);
//
//                imagePreviewItemFragment.setFragment_contener(fragment_contener);
//                activity.getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fl_fragment_container, imagePreviewItemFragment).commit();
                FragmentManage.startFragment(activity, imagePreviewItemFragment);
            }
        }
    }

    /**
     * 打开文件
     */
    public void openFile(TerminalMessage terminalMessage, ChatViewHolder chatViewHolder) {
        File file;
        if (!TextUtils.isEmpty(terminalMessage.messagePath)) {
            file = new File(terminalMessage.messagePath);
        } else {
            ToastUtil.showToast(activity, activity.getString(R.string.text_file_path_is_empty_can_not_opened));
            return;
        }
        /**  如果文件不存在就下载 **/
        if (!file.exists()) {
            if (isDownloading) {
                //                ToastUtil.showToast(activity, "有其他文件正在下载！");
                return;
            }
            /**  当本地文件不存在并且url是本地路径时---进行url切换  ***/
            if (!terminalMessage.messagePath.startsWith("http")){
                MyTerminalFactory.getSDK().getTerminalMessageManager().setMessagePath(terminalMessage, false);
            }
            downloadProgressBar = chatViewHolder.progressBar;
            download_tv_progressBars = chatViewHolder.tv_progress;
            setProgress(downloadProgressBar, 0);
            setText(download_tv_progressBars, "0%");
            setViewVisibility(downloadProgressBar, View.VISIBLE);
            setViewVisibility(download_tv_progressBars, View.VISIBLE);
            terminalMessage.messageBody.put(JsonParam.IS_DOWNLOADINF, true);
            isDownloading = true;
            logger.error("转换网络路径");
            MyTerminalFactory.getSDK().download(terminalMessage, true);

            return;
        }
        //打开文件
        if (terminalMessage.messageType != MessageType.VIDEO_CLIPS.getCode()) {
            OpenFileUtils.openFile(file, activity);
        }

    }




    /**
     * 下载完原图之后打开预览
     **/
    public void openPhotoAfterDownload(TerminalMessage terminalMessage) {
        mImgList = findImages();

        int currentPos = mImgUrlList.indexOf(terminalMessage.messagePath);
        logger.info("图片列表位置：" + currentPos+"  路径："+terminalMessage.messagePath);
        File file = new File(terminalMessage.messagePath);
        if (!file.exists()) {
            //下载图片
            return;
        }
//        setViewVisibility(fragment_contener, View.VISIBLE);
        //        ImagePreviewItemFragment imagePreviewItemFragment = ImagePreviewItemFragment.getInstance(terminalMessage.messagePath, isReceiver(terminalMessage));
        ImagePreviewItemFragment imagePreviewItemFragment = ImagePreviewItemFragment.getInstance(mImgList, currentPos);

//        imagePreviewItemFragment.setFragment_contener(fragment_contener);
//        activity.getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fl_fragment_container, imagePreviewItemFragment).commit();

        FragmentManage.startFragment(activity, imagePreviewItemFragment);
    }

    public void openVideo(TerminalMessage terminalMessage,File file){
        if(terminalMessage.messageType == MessageType.VIDEO_CLIPS.getCode()){
            if(file.exists()){
//                setViewVisibility(fragment_contener, View.VISIBLE);
                VideoPreviewItemFragment videoPreviewItemFragment = VideoPreviewItemFragment.newInstance(file.getAbsolutePath());
//                videoPreviewItemFragment.setFragment_contener(fragment_contener);
//                activity.getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fl_fragment_container, videoPreviewItemFragment).commit();

                FragmentManage.startFragment(activity, videoPreviewItemFragment);
            }else {
                ToastUtil.showToast(activity,activity.getString(R.string.text_down_load_video_fail));
            }
        }
    }

    /***  下载完后打开文件 **/
    public void openFileAfterDownload(TerminalMessage terminalMessage) {
        File file = new File(terminalMessage.messagePath);
        if (!file.exists()) {
            //            ToastUtil.showToast(activity, "下载失败!");
            // TODO: 2019/1/17 下载视频

            return;
        }
        if(terminalMessage.messageType == MessageType.VIDEO_CLIPS.getCode()){
//            FragmentManage.startFragment(activity,);
//            setViewVisibility(fragment_contener, View.VISIBLE);
//            VideoPreviewItemFragment videoPreviewItemFragment = VideoPreviewItemFragment.newInstance(file.getAbsolutePath());
//            videoPreviewItemFragment.setFragment_contener(fragment_contener);
//            FragmentManage.startFragment(activity,videoPreviewItemFragment);
//            activity.getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fl_fragment_container, videoPreviewItemFragment).commit();
        }else if(terminalMessage.messageType == MessageType.FILE.getCode()){
            OpenFileUtils.openFile(file,activity);
        }
    }

    public TerminalMessage transponMessage;//需要转发的消息
    public boolean isTranspon;//是否转发了

    /**
     * 清空转发选择的状态
     */
    public void clearForWardState(){
        for (TerminalMessage message: datas) {
            message.isForward = false;
        }
        isForWardMore = false;
        notifyDataSetChanged();
    }

    /**
     * 打开转发选择的状态
     */
    public void openForWardState(){
        for (TerminalMessage message: datas) {
            message.isForward = false;
        }
        isForWardMore = true;
        notifyDataSetChanged();
    }

    /**
     * 获取消息的内容
     * @param message
     * @return
     */
    public String getMessageContent(TerminalMessage message) {
        String noticeContent = "";
        //短文
        if(message.messageType ==  MessageType.SHORT_TEXT.getCode()) {
            String content = message.messageBody.getString(JsonParam.CONTENT);
            noticeContent=message.messageFromName+":"+content;
        }
        //长文
        if(message.messageType ==  MessageType.LONG_TEXT.getCode()) {
            String path = message.messagePath;
            File file = new File(path);
            if (!file.exists()) {
                MyTerminalFactory.getSDK().getTerminalMessageManager().setMessagePath(message, false);
                MyTerminalFactory.getSDK().download(message, true);
            }
            String content = FileUtil.getStringFromFile(file);
            noticeContent=String.format(activity.getString(R.string.text_message_list_text_),message.messageFromName,content);
        }
        //图片
        if(message.messageType ==  MessageType.PICTURE.getCode()) {
            noticeContent=String.format(activity.getString(R.string.text_message_list_picture_),message.messageFromName);
        }
        //录音
        if(message.messageType ==  MessageType.AUDIO.getCode()) {
            noticeContent=String.format(activity.getString(R.string.text_message_list_voice_),message.messageFromName);
        }
        //小视频
        if(message.messageType ==  MessageType.VIDEO_CLIPS.getCode()) {
            noticeContent=String.format(activity.getString(R.string.text_message_list_video_),message.messageFromName);
        }
        //文件
        if(message.messageType ==  MessageType.FILE.getCode()) {
            noticeContent=String.format(activity.getString(R.string.text_message_list_file_),message.messageFromName);
        }
        //位置
        if(message.messageType ==  MessageType.POSITION.getCode()) {
            noticeContent=String.format(activity.getString(R.string.text_message_list_location_),message.messageFromName);
        }
        //公告
        if(message.messageType ==  MessageType.AFFICHE.getCode()) {
            noticeContent=String.format(activity.getString(R.string.text_message_list_notice_),message.messageFromName);
        }
        //警情
        if(message.messageType ==  MessageType.WARNING_INSTANCE.getCode()) {
            noticeContent=String.format(activity.getString(R.string.text_message_list_warning_),message.messageFromName);
        }
        //个呼
        if(message.messageType ==  MessageType.PRIVATE_CALL.getCode()) {
            noticeContent=String.format(activity.getString(R.string.text_message_list_personal_call_),message.messageFromName);
        }
        //图像记录
        if(message.messageType ==  MessageType.VIDEO_LIVE.getCode()||
                message.messageType ==  MessageType.GB28181_RECORD.getCode() ||
                message.messageType ==  MessageType.OUTER_GB28181_RECORD.getCode()) {
            noticeContent=String.format(activity.getString(R.string.text_message_list_image_),message.messageFromName);
        }
        //组呼
        if(message.messageType ==  MessageType.GROUP_CALL.getCode()) {
            noticeContent=String.format(activity.getString(R.string.text_message_list_group_call_),message.messageFromName);
        }
        //超链接
        if(message.messageType ==  MessageType.HYPERLINK.getCode()) {
            noticeContent=String.format(activity.getString(R.string.text_message_list_face_recognition_),message.messageFromName);
        }
        //合并转发
        if(message.messageType ==  MessageType.MERGE_TRANSMIT.getCode()) {
            noticeContent=String.format(activity.getString(R.string.text_message_list_merge_transmit_),message.messageFromName);
        }
        return noticeContent;
    }


    @NonNull
    private List<Integer> setToIds(TerminalMessage terminalMessage) {
        List<Integer> toIds = new ArrayList<>();
        if (isGroupChat) {
            toIds.add(NoCodec.encodeGroupNo(terminalMessage.messageToId));
        } else {
            toIds.add(NoCodec.encodeMemberNo(terminalMessage.messageToId));
        }
        return toIds;
    }

    private List<Integer> toIds = new ArrayList<>();

    private List<Integer> setToIdsWhenTranspon(TerminalMessage terminalMessage, boolean isGroup) {
        toIds.clear();
        if (isGroup) {
            toIds.add(NoCodec.encodeGroupNo(terminalMessage.messageToId));
        } else {
            toIds.add(NoCodec.encodeMemberNo(terminalMessage.messageToId));
        }
        return toIds;
    }

    private void addTokenId(JSONObject messageBody, Map<String, String> map) {
        if (messageBody.containsKey(JsonParam.TOKEN_ID)) {
            map.put(JsonParam.TOKEN_ID, messageBody.getIntValue(JsonParam.TOKEN_ID) + "");
        }
    }


    private List<ImageBean> findImages() {
        mImgUrlList.clear();
        mImgList.clear();
        for (TerminalMessage msg : datas) {
            if (msg.messageType == MessageType.PICTURE.getCode()) {
                ImageBean bean = new ImageBean();
                bean.setPath(msg.messagePath);
                //                bean.setReceive(isReceiver(msg));
                mImgList.add(bean);
                mImgUrlList.add(msg.messagePath);
            }
        }
        notifyDataSetChanged();
        return mImgList;
    }

    public void setUploadFinished(){
        upload = false;
    }

    /**
     * 设置是否合并转发
     * @param isForWardMore
     */
    public void setIsForWardMore(boolean isForWardMore){
        this.isForWardMore = isForWardMore;
    }

    /**
     * 获取是否在合并转发
     * @return
     */
    public boolean isForWardMore(){
        return isForWardMore;
    }

    /**
     * 设置头像显示
     */
    //    private void handlerAvatar(TerminalMessage terminalMessage, int position, ChatViewHolder holder) {
    //        String terminalMemberMode = null;
    //        if(MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) == terminalMessage.messageFromId){
    //            terminalMemberMode =  MyTerminalFactory.getSDK().getParam(Params.TERMINAL_MEMBER_MODE, "");
    //        }else{
    //            terminalMemberMode = DataUtil.getMemberByMemberNo(terminalMessage.messageFromId).getTerminalMemberMode();
    //        }
    //        if(DataUtil.checkTerminalTypeIsCar(terminalMemberMode)){
    //            //车辆
    //            if(activity!=null&&!activity.isDestroyed()) {
    //                Glide.with(activity).load(DataUtil.getTerminalTypeHeadImage(terminalMemberMode)).asBitmap().into(holder.ivAvatar);
    //            }
    //
    //        }else{
    //            //人员
    //            if(activity!=null&&!activity.isDestroyed()) {
    //                Glide.with(activity)
    //                        .load(DataUtil.getMemberByMemberNo(terminalMessage.messageFromId).avatarUrl)
    //                        .asBitmap()
    //                        .placeholder(R.drawable.user_photo)//加载中显示的图片
    //                        .error(R.drawable.user_photo)//加载失败时显示的图片
    //                        .into(holder.ivAvatar);
    //            }
    //        }
    //    }
}

package com.vsxin.terminalpad.mvp.ui.adapter;

import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.app.PadApplication;
import com.vsxin.terminalpad.mvp.entity.PlayType;
import com.vsxin.terminalpad.mvp.ui.adapter.holder.ChatViewHolder;
import com.vsxin.terminalpad.mvp.ui.adapter.holder.MergeTransmitViewHolder;
import com.vsxin.terminalpad.mvp.ui.fragment.LocationFragment;
import com.vsxin.terminalpad.mvp.ui.fragment.MergeTransmitListFragment;
import com.vsxin.terminalpad.mvp.ui.fragment.VideoPreviewItemFragment;
import com.vsxin.terminalpad.receiveHandler.ReceiveGoWatchRTSPHandler;
import com.vsxin.terminalpad.receiveHandler.ReceiverIndividualCallFromMsgItemHandler;
import com.vsxin.terminalpad.receiveHandler.ReceiverReplayGroupMergeTransmitVoiceHandler;
import com.vsxin.terminalpad.utils.BitmapUtil;
import com.vsxin.terminalpad.utils.Constants;
import com.vsxin.terminalpad.utils.FragmentManage;
import com.vsxin.terminalpad.utils.LiveUtil;
import com.zectec.imageandfileselector.adapter.FaceRecognitionAdapter;
import com.zectec.imageandfileselector.bean.FaceRecognitionBean;
import com.zectec.imageandfileselector.bean.FileInfo;
import com.zectec.imageandfileselector.bean.ImageBean;
import com.zectec.imageandfileselector.fragment.ImagePreviewFragment;
import com.zectec.imageandfileselector.fragment.ImagePreviewItemFragment;
import com.zectec.imageandfileselector.utils.DateUtils;
import com.zectec.imageandfileselector.utils.FileIcons;
import com.zectec.imageandfileselector.utils.FileUtil;
import com.zectec.imageandfileselector.utils.OpenFileUtils;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;
import com.zectec.imageandfileselector.utils.PhotoUtils;
import com.zectec.imageandfileselector.view.LoadingCircleView;

import org.apache.log4j.Logger;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.Remark;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.common.util.NoCodec;
import cn.vsx.hamster.errcode.module.SignalServerErrorCode;
import cn.vsx.hamster.errcode.module.TerminalErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.SignatureUtil;
import cn.vsx.hamster.terminalsdk.tools.Util;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * Created by zckj on 2017/3/22.
 */

public class MergeTransmitListAdapter extends RecyclerView.Adapter<MergeTransmitViewHolder> {
//    private final int VIEW_TYPE = 28;

    private static final int MESSAGE_SHORT_TEXT = 0;//短文本
    private static final int MESSAGE_LONG_TEXT = 1;//长文本
    private static final int MESSAGE_IMAGE = 2;//图片
    private static final int MESSAGE_VOICE = 3;//录音
    private static final int MESSAGE_VEDIO = 4;//小视频
    private static final int MESSAGE_FILE = 5;//文件
    private static final int MESSAGE_LOCATION = 6;//位置
    private static final int MESSAGE_AFFICHE = 7;//公告
    private static final int MESSAGE_WARNING_INSTANCE = 8;//警情
    private static final int MESSAGE_PRIVATE_CALL = 9;//个呼
    private static final int MESSAGE_VIDEO_LIVE = 10;//图像记录
    private static final int MESSAGE_GROUP_CALL = 11;//组呼
    private static final int MESSAGE_HYPERLINK = 12;//超链接
    private static final int MESSAGE_GB28181_RECODE = 13;//视频平台
    private static final int MESSAGE_MERGE_TRANSMIT = 14;//合并转发


    public static final int MIN_CLICK_DELAY_TIME = 1000;
    private long lastClickTime = 0;

//    FrameLayout fragment_contener;

//    private HashMap<Integer, String> idNameMap;

    private Logger logger = Logger.getLogger(getClass());
    List<TerminalMessage> chatMessageList;
    private boolean isGroupChat;
    private LayoutInflater inflater;
    private FragmentActivity activity;

    private int mposition = -1;
    private boolean isPlaying;
    private boolean isSameItem;
    public boolean isDownloading;
    public boolean isDownloadingPicture;
    public boolean isEnable = true;
    public LoadingCircleView loadingView;
    public ProgressBar downloadProgressBar;//正在下载的条目对应的ProgressBar
    public TextView download_tv_progressBars;//正在下载的条目对应的tv_progressBars
    public String liveTheme;
    private boolean mIsLongClick = false;//文本消息是否长按
    List<ImageBean> mImgList = new ArrayList<>();
    List<String> mImgUrlList = new ArrayList<>();
    //是否是组消息
    private boolean isGroup;
    protected int userId;

    public MergeTransmitListAdapter(List<TerminalMessage> chatMessageList, FragmentActivity activity, boolean isGroup, int userId) {
        this.chatMessageList = chatMessageList;
        this.activity = activity;
        this.isGroup = isGroup;
        this.userId = userId;
        inflater = activity.getLayoutInflater();
    }

    public void refreshPersonContactsAdapter(int mposition, List<TerminalMessage> terminalMessageList, boolean isPlaying, boolean isSameItem) {
        this.mposition = mposition;
        this.chatMessageList = terminalMessageList;
        this.isPlaying = isPlaying;
        this.isSameItem = isSameItem;
//        Collections.sort(terminalMessageList);
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
        progressBar.setProgress(progress);
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
        return chatMessageList.size();
    }

    @Override
    public MergeTransmitViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MergeTransmitViewHolder holder = null;
        switch (viewType) {
            case MESSAGE_LONG_TEXT:
            case MESSAGE_SHORT_TEXT:
                holder =  new MergeTransmitViewHolder.TextHolder(getViewByType(viewType, parent));
                break;
            case MESSAGE_IMAGE:
                holder =   new MergeTransmitViewHolder.ImageHolder(getViewByType(viewType, parent));
                break;
            case MESSAGE_GROUP_CALL:
            case MESSAGE_VOICE:
                holder =   new MergeTransmitViewHolder.VoiceHolder(getViewByType(viewType, parent));
                break;
            case MESSAGE_FILE:
                holder =   new MergeTransmitViewHolder.FileHolder(getViewByType(viewType, parent));
                break;
            case MESSAGE_VEDIO:
                holder =   new MergeTransmitViewHolder.VideoHolder(getViewByType(viewType, parent));
                break;
            case MESSAGE_LOCATION:
                holder =   new MergeTransmitViewHolder.LocationHolder(getViewByType(viewType, parent));
                break;
            case MESSAGE_WARNING_INSTANCE:
            case MESSAGE_VIDEO_LIVE:
            case MESSAGE_GB28181_RECODE:
                holder =   new MergeTransmitViewHolder.LiveHolder(getViewByType(viewType, parent));
                break;
            case MESSAGE_PRIVATE_CALL:
                holder =   new MergeTransmitViewHolder.PrivateCallHolder(getViewByType(viewType, parent));
                break;
            case MESSAGE_HYPERLINK:
                holder =   new MergeTransmitViewHolder.HyperlinkHolder(getViewByType(viewType, parent));
                break;
            case MESSAGE_MERGE_TRANSMIT:
                holder =   new MergeTransmitViewHolder.MergeTransmitHolder(getViewByType(viewType, parent));
                break;
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(MergeTransmitViewHolder holder, int position) {
        if(holder!=null){
            final TerminalMessage terminalMessage = chatMessageList.get(position);
            final int viewType = getItemViewType(position);
            setData(position, terminalMessage, viewType, holder);
        }
    }

    @Override
    public int getItemViewType(int position) {
            if (chatMessageList.get(position).messageType == MessageType.SHORT_TEXT.getCode()) {
                return MESSAGE_SHORT_TEXT;
            } else if (chatMessageList.get(position).messageType == MessageType.LONG_TEXT.getCode()) {
                return MESSAGE_LONG_TEXT;
            }else if (chatMessageList.get(position).messageType == MessageType.PICTURE.getCode()) {
                return MESSAGE_IMAGE;
            }else if (chatMessageList.get(position).messageType == MessageType.AUDIO.getCode()) {
                return MESSAGE_VOICE;
            }else if (chatMessageList.get(position).messageType == MessageType.VIDEO_CLIPS.getCode()) {
                return MESSAGE_VEDIO;
            }else if (chatMessageList.get(position).messageType == MessageType.FILE.getCode()) {
                return MESSAGE_FILE;
            }else if (chatMessageList.get(position).messageType == MessageType.POSITION.getCode()) {
                return MESSAGE_LOCATION;
            }else if (chatMessageList.get(position).messageType == MessageType.AFFICHE.getCode()) {
                return MESSAGE_AFFICHE;
            }else if (chatMessageList.get(position).messageType == MessageType.WARNING_INSTANCE.getCode()) {
                return MESSAGE_WARNING_INSTANCE;
            }else if (chatMessageList.get(position).messageType == MessageType.PRIVATE_CALL.getCode()) {
                return MESSAGE_PRIVATE_CALL;
            }else if (chatMessageList.get(position).messageType == MessageType.VIDEO_LIVE.getCode()) {
                return MESSAGE_VIDEO_LIVE;
            }else if (chatMessageList.get(position).messageType == MessageType.GROUP_CALL.getCode()) {
                return MESSAGE_GROUP_CALL;
            }else if (chatMessageList.get(position).messageType == MessageType.HYPERLINK.getCode()) {
                return MESSAGE_HYPERLINK;
            }else if(chatMessageList.get(position).messageType == MessageType.GB28181_RECORD.getCode()){
                return MESSAGE_GB28181_RECODE;
            }else if(chatMessageList.get(position).messageType == MessageType.MERGE_TRANSMIT.getCode()){
                return MESSAGE_MERGE_TRANSMIT;
            }else {
                return MESSAGE_SHORT_TEXT;
            }
    }

    private void setData(int position, TerminalMessage terminalMessage, int viewType, MergeTransmitViewHolder holder) {
        handleData(holder, viewType, terminalMessage, position);
        setListener(viewType, holder, terminalMessage, position);
        if (position == chatMessageList.size() - 1) {
            holder.placeHolder.setVisibility(View.VISIBLE);
        } else {
            holder.placeHolder.setVisibility(View.GONE);
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
//        if (terminalMessage.messageBody.containsKey(JsonParam.SEND_STATE)) {
//            if (terminalMessage.messageBody.getString(JsonParam.SEND_STATE).equals(MessageSendStateEnum.SEND_PRE.toString())) {
//                if (viewType == MESSAGE_LOCATION_SEND && terminalMessage.messageBody.containsKey(JsonParam.ACTUAL_SEND) &&
//                        !terminalMessage.messageBody.getBooleanValue(JsonParam.ACTUAL_SEND)) {
//                } else {
//                    terminalMessage.messageBody.put(JsonParam.SEND_STATE, MessageSendStateEnum.SENDING);
//                    sendMessage(holder, terminalMessage, viewType);
//                }
//                setSendingView(holder, terminalMessage, viewType);
//            } else if (terminalMessage.messageBody.getString(JsonParam.SEND_STATE).equals(MessageSendStateEnum.SENDING.toString())) {
//                setSendingView(holder, terminalMessage, viewType);
//            } else if (terminalMessage.messageBody.getString(JsonParam.SEND_STATE).equals(MessageSendStateEnum.SEND_FAIL.toString())) {
//                setSendResultView(holder, viewType, View.VISIBLE);
//            } else if (terminalMessage.messageBody.getString(JsonParam.SEND_STATE).equals(MessageSendStateEnum.SEND_SUCCESS.toString())) {
//                setSendResultView(holder, viewType, View.GONE);
//            }
//        } else {
//            logger.error("*******没有发送状态的标记，这是不应该的*******");
//        }
    }

    private void setListener(final int viewType, final MergeTransmitViewHolder holder, final TerminalMessage terminalMessage, final int position) {
        //长按消息条目
//        holder.reBubble.setOnLongClickListener(v -> {
//            if (!isEnable) {
//                return false;
//            }
//            if (
////                    terminalMessage.messageType == MessageType.PRIVATE_CALL.getCode() ||
////                    terminalMessage.messageType == MessageType.VIDEO_LIVE.getCode() ||
////                    terminalMessage.messageType == MessageType.GROUP_CALL.getCode() ||
////                    terminalMessage.messageType == MessageType.AUDIO.getCode()||
//                    PadApplication.getPadApplication().getGroupSpeakState() != GroupCallSpeakState.IDLE) {
//                return false;
//            }
//            new TranspondNewDialog(activity, terminalMessage, terminalMessage1 -> onCopy(terminalMessage1)).showView();
//            if (!terminalMessage.messageBody.containsKey(JsonParam.TOKEN_ID)) {
//                terminalMessage.messageBody.put(JsonParam.TOKEN_ID, MyTerminalFactory.getSDK().getMessageSeq());
//            }
//            transponMessage = (TerminalMessage) terminalMessage.clone();
//            return false;
//        });
        //点击消息条目
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
//                privateCallClick(terminalMessage);
                locationItemClick(terminalMessage);
//                liveItemClick(terminalMessage, viewType);
                individualNewsRecordItemClick(terminalMessage, position);
                groupCallItemClick(terminalMessage, position);
//                gb28181ItemClick(terminalMessage, viewType);
                mergeTransmit(terminalMessage,viewType);
            }
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(holder.reBubble.getWindowToken(), 0);
        });
        if (holder.live_bubble != null) {
            holder.live_bubble.setOnClickListener(v -> {
                if (terminalMessage.resultCode == SignalServerErrorCode.MEMBER_REFUSE.getErrorCode() || terminalMessage.messageBody.getInteger(JsonParam.REMARK) == Remark.STOP_ASK_VIDEO_LIVE || terminalMessage.resultCode == SignalServerErrorCode.VIDEO_LIVE_WAITE_TIMEOUT.getErrorCode()) {
                    if (terminalMessage.messageFromId != MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)) {
                        return;
                    } else {
//                        MergeTransmitListActivity activity = ActivityCollector.getActivity(MergeTransmitListActivity.class);
//                        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverSendFileCheckMessageHandler.class, ReceiverSendFileCheckMessageHandler.REQUEST_VIDEO, true, activity.getChatTargetId());
                    }
                }
            });
        }
        if (holder.ivAvatar != null) {
            holder.ivAvatar.setOnClickListener(v -> {
                if (PadApplication.getPadApplication().getGroupSpeakState() != GroupCallSpeakState.IDLE) {
                    return;
                } else {
//                    Intent intent = new Intent(activity, UserInfoActivity.class);
//                    intent.putExtra("userId", terminalMessage.messageFromId);
//                    intent.putExtra("userName", terminalMessage.messageFromName);
//                    activity.startActivity(intent);
                }
            });

        }
        if (viewType == MESSAGE_SHORT_TEXT) {
            holder.tvContent.setOnTouchListener((view, motionEvent) -> {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    return mIsLongClick;
                } else if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    mIsLongClick = false;
                    return false;
                }
                return false;
            });
        }

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
    private void handleData(MergeTransmitViewHolder holder, int viewType, final TerminalMessage terminalMessage, int position) {
        JSONObject messageBody = terminalMessage.messageBody;
        if (messageBody == null) {
            logger.error("messageBody 空了" + messageBody);
            return;
        }

        handlerTime(terminalMessage, holder);
        handlerAvatar(terminalMessage, holder,position);
        setText(holder.tvNick, terminalMessage.messageFromName);

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
            double length = (messageBody.getLong(JsonParam.END_TIME) - messageBody.getLong(JsonParam.START_TIME)) / 1000.00;
            BigDecimal b = new BigDecimal(new Double(length).toString());
            long voiceLength = b.setScale(0, BigDecimal.ROUND_HALF_UP).longValue();
            voiceLength = voiceLength < 1 ? 1 : voiceLength;
            voiceLength = voiceLength > 60 ? 60 : voiceLength;
            setText(holder.tvDuration, voiceLength + "''");
            playGroupVoice(position, holder,terminalMessage);
//            if (isReceiver(terminalMessage)) {
//                if (messageBody.containsKey(JsonParam.UNREAD) &&
//                        messageBody.getBooleanValue(JsonParam.UNREAD)) {
//                    setViewVisibility(holder.ivUnread, View.VISIBLE);
//                } else {
//                    setViewVisibility(holder.ivUnread, View.INVISIBLE);
//                }
//            }
        }
        /**  个呼条目  */
        if (terminalMessage.messageType == MessageType.PRIVATE_CALL.getCode()) {
            handlerPrivateCallData(terminalMessage, holder);
        }
        /**  直播接收条目  */
        if (viewType == MESSAGE_VIDEO_LIVE) {
            handlerLiveData(terminalMessage, holder);
        }
        if(viewType == MESSAGE_GB28181_RECODE){
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
        if (position == chatMessageList.size() - 1 && !isReceiver(terminalMessage)) {
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

    private void handleFileData(TerminalMessage terminalMessage, MergeTransmitViewHolder holder, int viewType) {
        JSONObject messageBody = terminalMessage.messageBody;
        String fileName = messageBody.getString(JsonParam.FILE_NAME);
        int res = FileIcons.smallIcon(fileName);
        holder.iv_temp.setImageDrawable(activity.getResources().getDrawable(res));
        setText(holder.tvContent, messageBody.getString(JsonParam.FILE_NAME));
        String fileSize = FileUtil.getFileSzie(messageBody.getLong(JsonParam.FILE_SIZE));
        setText(holder.tvFileSize, fileSize);
        /***  当前条目正在下载就显示进度条， 否则隐藏 **/
        if (viewType == MESSAGE_FILE) {
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
    private void handleHyperlinkData(TerminalMessage terminalMessage, MergeTransmitViewHolder holder) {
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
    private void handleMergeTransmitData(TerminalMessage terminalMessage, MergeTransmitViewHolder holder) {
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
            CharSequence charSequence;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                charSequence =Html.fromHtml(sb.toString(),Html.FROM_HTML_MODE_LEGACY);
            } else {
                charSequence = Html.fromHtml(sb.toString()); }
            holder.tv_content.setText(charSequence);
        }else{
            holder.tv_content.setText(activity.getString(R.string.chat_record));
        }
    }

    /**
     * 显示全部条目
     **/
    private void setListViewHeightOnChildren(MergeTransmitViewHolder holder) {
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

    private void handleGB28181Data(TerminalMessage terminalMessage, MergeTransmitViewHolder holder){
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
    private void handlerLiveData(TerminalMessage terminalMessage, MergeTransmitViewHolder holder) {
        JSONObject messageBody = terminalMessage.messageBody;
        String liver = messageBody.getString(JsonParam.LIVER);
        int liverNo = Util.stringToInt(messageBody.getString(JsonParam.LIVERNO));
        String[] split = liver.split("_");
//        //设置默认上报视频者，防止数组下标越界异常
//        int memberNo = terminalMessage.messageFromId;
//        if(split.length>0){
//            memberNo = Integer.valueOf(split[0]);
//        }
        //上报主题，如果没有就取上报者的名字
        liveTheme = messageBody.getString(JsonParam.TITLE);
        if(TextUtils.isEmpty(liveTheme)){
            if(split.length>1){
                String memberName = split[1];
                liveTheme = String.format(activity.getString(R.string.current_push_member),memberName);
                setText(holder.tvContent, liveTheme);
            }else {
                TerminalFactory.getSDK().getThreadPool().execute(() -> {
                    Account account = cn.vsx.hamster.terminalsdk.tools.DataUtil.getAccountByMemberNo(liverNo,true);
                    String name = (account!=null)?account.getName():terminalMessage.messageFromName;
                    new Handler().post(() -> {
                        setText(holder.tvContent, String.format(activity.getString(R.string.current_push_member),name));
                    });
                });
            }
        }

        if(!messageBody.containsKey(JsonParam.REMARK)){
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

                    if (liverNo == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)) {
                        setText(holder.live_tv_chatcontent, activity.getString(R.string.text_not_accepted));
                    } else {
                        setText(holder.live_tv_chatcontent, activity.getString(R.string.other_no_answer));
                    }

                } else if (terminalMessage.resultCode == SignalServerErrorCode.SLAVE_BUSY.getErrorCode()) {
                    if ((liverNo == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0))) {
                        setText(holder.live_tv_chatcontent, activity.getString(R.string.text_not_accepted));
                    } else {
                        setText(holder.live_tv_chatcontent, activity.getString(R.string.text_busy_party));
                    }
                } else if (terminalMessage.resultCode == SignalServerErrorCode.MEMBER_IS_KILLED.getErrorCode()) {//主叫或被叫被遥弊
                    if ((liverNo == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0))) {
                        setText(holder.live_tv_chatcontent, activity.getString(R.string.text_not_accepted));
                    } else {
                        setText(holder.live_tv_chatcontent, activity.getString(R.string.other_no_answer));
                    }
                } else if (terminalMessage.resultCode == SignalServerErrorCode.CALLED_MEMBER_OFFLINE.getErrorCode()) {
                    if ((liverNo == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0))) {
                        setText(holder.live_tv_chatcontent, activity.getString(R.string.text_not_accepted));
                    } else {
                        setText(holder.live_tv_chatcontent, SignalServerErrorCode.CALLED_MEMBER_OFFLINE.getErrorDiscribe());
                    }
                } else if (terminalMessage.resultCode == SignalServerErrorCode.TERMINAL_OFFLINE_LOGOUT.getErrorCode()) {
                    if ((liverNo == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0))) {
                        setText(holder.live_tv_chatcontent, activity.getString(R.string.text_not_accepted));
                    } else {
                        setText(holder.live_tv_chatcontent, activity.getString(R.string.text_the_other_party_is_not_online));
                    }
                } else if (terminalMessage.resultCode == SignalServerErrorCode.MEMBER_REFUSE.getErrorCode()) {
                    if (liverNo == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)) {
                        setText(holder.live_tv_chatcontent, activity.getString(R.string.refused));
                    } else {
                        setText(holder.live_tv_chatcontent, activity.getString(R.string.text_the_other_party_has_refus));
                    }

                } else if (liverNo == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)) {
                    setText(holder.live_tv_chatcontent, activity.getString(R.string.text_not_accepted));
                }
            } else {
                setText(holder.live_tv_chatcontent, activity.getString(R.string.refused));
            }
        } else if (messageBody.getInteger(JsonParam.REMARK) == Remark.STOP_ASK_VIDEO_LIVE) {
            setViewVisibility(holder.reBubble, View.GONE);
            setViewVisibility(holder.live_bubble, View.VISIBLE);
            if (terminalMessage.resultCode == SignalServerErrorCode.STOP_ASK_VIDEO_LIVE.getErrorCode()) {
                if ((liverNo == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0))) {
                    setText(holder.live_tv_chatcontent, activity.getString(R.string.text_not_accepted));
                } else {
                    setText(holder.live_tv_chatcontent, activity.getString(R.string.canceled));
                }
            }
        }
    }

    /***  设置个呼数据 **/
    private void handlerPrivateCallData(TerminalMessage terminalMessage, MergeTransmitViewHolder holder) {
        JSONObject messageBody = terminalMessage.messageBody;
        logger.info("sjl_:" + terminalMessage.resultCode + "," + SignalServerErrorCode.INDIVIDUAL_CALL_WAITE_TIMEOUT.getErrorCode());
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
    private void handlerAvatar(TerminalMessage terminalMessage, MergeTransmitViewHolder holder, int position) {
        int drawable = BitmapUtil.getUserPhoto();
        Glide.with(activity)
                .load(drawable)
                .asBitmap()
                .placeholder(drawable)//加载中显示的图片
                .error(drawable)//加载失败时显示的图片
                .into(holder.ivAvatar);
        holder.ivAvatar.setVisibility(View.VISIBLE);
        if(position > 0 && position < chatMessageList.size()){
            TerminalMessage message = chatMessageList.get(position-1);
           if(terminalMessage!=null&&message!=null&&terminalMessage.messageFromId == message.messageFromId){
             holder.ivAvatar.setVisibility(View.INVISIBLE);
           }
        }
    }

    /**
     * 设置消息时间显示
     */
    private void handlerTime(TerminalMessage terminalMessage , MergeTransmitViewHolder holder) {
        long currentTime = 0;
        if (terminalMessage.sendTime > 0) {
             currentTime = terminalMessage.sendTime;
        } else {
             currentTime = System.currentTimeMillis();
        }
        setText(holder.timeStamp, DateUtils.getNewChatTime(currentTime));
        setViewVisibility(holder.timeStamp, View.VISIBLE);
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
    private void setLongText(TerminalMessage terminalMessage, MergeTransmitViewHolder holder) {
        String path = terminalMessage.messagePath;
        File file = new File(path);
        if (!file.exists()) {
            MyTerminalFactory.getSDK().getTerminalMessageManager().setMessagePath(terminalMessage, false);
            MyTerminalFactory.getSDK().download(terminalMessage, true);
        }
        String content = FileUtil.getStringFromFile(file);
        if (TextUtils.isEmpty(content)) {
            content = activity.getString(R.string.text_get_no_content);
        }
        setText(holder.tvContent, content);
    }

    /***  播放组呼录音相关改变 **/
    private void playGroupVoice(int position, MergeTransmitViewHolder holder, TerminalMessage terminalMessage) {
        if(holder.iv_voice_image_anim != null){
            if (MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_LISTEN.name())) {
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

    /**
     * 点击播放之后改变数据库状态
     **/
    private void setUnread(int position) {
        TerminalMessage terminalMessage = chatMessageList.get(position);
        if (terminalMessage.messageBody.containsKey(JsonParam.UNREAD) &&
                terminalMessage.messageBody.getBooleanValue(JsonParam.UNREAD)) {
            terminalMessage.messageBody.put(JsonParam.UNREAD, false);
            MyTerminalFactory.getSDK().getSQLiteDBManager().updateTerminalMessage(terminalMessage);
        }
    }
    /**
     * 文件条目点击
     */
    private void fileItemClick(MergeTransmitViewHolder chatViewHolder, TerminalMessage terminalMessage, int type) {
        if (terminalMessage.messageType == MessageType.FILE.getCode()){
            openFile(terminalMessage, chatViewHolder);
        }
    }

    /**
     * 图片条目条目点击
     */
    private void photoItemClick(MergeTransmitViewHolder chatViewHolder, TerminalMessage terminalMessage, int type) {
        if (terminalMessage.messageType == MessageType.PICTURE.getCode()) {
            openPhoto(terminalMessage, chatViewHolder);
        }
    }

    /**
     * 点击视频条目点击
     */
    private void videoItemClick(MergeTransmitViewHolder chatViewHolder, TerminalMessage terminalMessage, int type) {
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
            onListItemClick(terminalMessage, isReceiver(terminalMessage));
        }
    }

    private void gb28181ItemClick(TerminalMessage terminalMessage, int viewType){
        if(terminalMessage.messageType == MessageType.GB28181_RECORD.getCode()){
            if (MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_ACCEPT.name())){
                if(viewType == MESSAGE_GB28181_RECODE){
                    MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveGoWatchRTSPHandler.class,terminalMessage);
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
            onListItemClick(terminalMessage, isReceiver(terminalMessage));
        }
    }


    /**
     * 点击图像接收条目
     **/
    private void liveItemClick(TerminalMessage terminalMessage, int viewType) {
        if(terminalMessage.messageType == MessageType.VIDEO_LIVE.getCode()){
            if (MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_ACCEPT.name())) {
                if (viewType == MESSAGE_VIDEO_LIVE && terminalMessage.messageBody.getIntValue("remark") != 1) {
                    onListItemClick(terminalMessage, isReceiver(terminalMessage));
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
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverReplayGroupMergeTransmitVoiceHandler.class,terminalMessage,position, PlayType.PLAY_GROUP_CALL.getCode());
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
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverReplayGroupMergeTransmitVoiceHandler.class,terminalMessage, position,PlayType.PLAY_AUDIO.getCode());
        }
    }

    private View getViewByType(int viewType, ViewGroup parent) {
        switch (viewType) {
            case MESSAGE_LONG_TEXT:
            case MESSAGE_SHORT_TEXT:
                return inflater.inflate(R.layout.row_merge_transmit_message, parent, false);
            case MESSAGE_IMAGE:
                return inflater.inflate(R.layout.row_merge_transmit_picture, parent, false);
            case MESSAGE_GROUP_CALL:
            case MESSAGE_VOICE:
                return inflater.inflate(R.layout.row_merge_transmit_voice, parent, false);
            case MESSAGE_FILE:
                return inflater.inflate(R.layout.row_merge_transmit_file, parent, false);
            case MESSAGE_VEDIO:
                return inflater.inflate(R.layout.row_merge_transmit_video, parent, false);
            case MESSAGE_LOCATION:
                return inflater.inflate(R.layout.row_merge_transmit_location, parent, false);
            case MESSAGE_WARNING_INSTANCE:
            case MESSAGE_VIDEO_LIVE:
            case MESSAGE_GB28181_RECODE:
                return inflater.inflate(R.layout.row_merge_transmit_live, parent, false);
            case MESSAGE_PRIVATE_CALL:
                return inflater.inflate(R.layout.row_merge_transmit_private_call, parent, false);
            case MESSAGE_HYPERLINK:
                return inflater.inflate(R.layout.row_merge_transmit_face, parent, false);
            case MESSAGE_MERGE_TRANSMIT:
                return inflater.inflate(R.layout.row_merge_transmit_merge_transmit, parent, false);
            default:
                return inflater.inflate(R.layout.row_merge_transmit_message, parent, false);
        }
    }

    public void openPhoto(TerminalMessage terminalMessage, MergeTransmitViewHolder chatViewHolder) {
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
//                activity.openPhoto
//                setViewVisibility(fragment_contener, View.VISIBLE);
                ImagePreviewItemFragment imagePreviewItemFragment = ImagePreviewItemFragment.getInstance(mImgList, currentPos);

//                imagePreviewItemFragment.setFragment_contener(fragment_contener);
//                activity.getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fl_fragment_container, imagePreviewItemFragment).commitAllowingStateLoss();
                FragmentManage.startFragment(activity, imagePreviewItemFragment);
            }
        }
    }

    /**
     * 打开文件
     */
    public void openFile(TerminalMessage terminalMessage, MergeTransmitViewHolder chatViewHolder) {
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
//        activity.getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fl_fragment_container, imagePreviewItemFragment).commitAllowingStateLoss();

        FragmentManage.startFragment(activity, imagePreviewItemFragment);
    }

    public void openVideo(TerminalMessage terminalMessage, File file){
        if(terminalMessage.messageType == MessageType.VIDEO_CLIPS.getCode()){
            if(file.exists()){
//                setViewVisibility(fragment_contener, View.VISIBLE);
                VideoPreviewItemFragment videoPreviewItemFragment = VideoPreviewItemFragment.newInstance(file.getAbsolutePath());
//                videoPreviewItemFragment.setFragment_contener(fragment_contener);
//                activity.getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fl_fragment_container, videoPreviewItemFragment).commitAllowingStateLoss();

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
//            setViewVisibility(fragment_contener, View.VISIBLE);
            VideoPreviewItemFragment videoPreviewItemFragment = VideoPreviewItemFragment.newInstance(file.getAbsolutePath());
//            videoPreviewItemFragment.setFragment_contener(fragment_contener);
//            activity.getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fl_fragment_container, videoPreviewItemFragment).commitAllowingStateLoss();

            FragmentManage.startFragment(activity, videoPreviewItemFragment);
        }else if(terminalMessage.messageType == MessageType.FILE.getCode()){
            OpenFileUtils.openFile(file,activity);
        }
    }

    public TerminalMessage transponMessage;//需要转发的消息

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
        for (TerminalMessage msg : chatMessageList) {
            if (msg.messageType == MessageType.PICTURE.getCode()) {
                ImageBean bean = new ImageBean();
                bean.setPath(msg.messagePath);
//                bean.setReceive(isReceiver(msg));
                mImgList.add(bean);
                mImgUrlList.add(msg.messagePath);
            }
        }
//        notifyDataSetChanged();
        return mImgList;
    }

    /**
     * 复制
     * @param terminalMessage
     */
    private void onCopy(TerminalMessage terminalMessage){
        ClipboardManager cmb = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        if (terminalMessage.messageType == 1) {
            cmb.setText(terminalMessage.messageBody.getString(JsonParam.CONTENT));
        } else if (terminalMessage.messageType == 2) {
            logger.info("sjl_:" + terminalMessage.messageType + "," + terminalMessage.messagePath);
            String path = terminalMessage.messagePath;
            File file = new File(path);
            String content = FileUtil.getStringFromFile(file);
            cmb.setText(content);
        }
        ToastUtil.showToast(activity.getString(R.string.text_replication_success), activity);
    }

    /**
     * 会话界面列表条目点击事件(定位，图片预览，观看图像，合并转发)
     **/

    private void onListItemClick(final TerminalMessage terminalMessage, boolean isReceiver){

        /**  进入定位界面 **/
        if (terminalMessage.messageType == MessageType.POSITION.getCode()) {
            if (terminalMessage.messageBody.containsKey(JsonParam.LONGITUDE) &&
                    terminalMessage.messageBody.containsKey(JsonParam.LATITUDE)) {
//                setViewVisibility(fragment_contener, View.VISIBLE);
                double longitude = terminalMessage.messageBody.getDouble(JsonParam.LONGITUDE);
                double altitude = terminalMessage.messageBody.getDouble(JsonParam.LATITUDE);
                //http://192.168.1.96:7007/mapLocationl.html?lng=117.68&lat=39.456
                String url = TerminalFactory.getSDK().getParam(Params.LOCATION_URL, "") + "?lng=" + longitude + "&lat=" + altitude;
                if (org.apache.http.util.TextUtils.isEmpty(TerminalFactory.getSDK().getParam(Params.LOCATION_URL, ""))) {
                    ToastUtil.showToast(activity, activity.getString(R.string.text_please_go_to_the_management_background_configuration_location_url));
                } else {
                    LocationFragment locationFragment = LocationFragment.getInstance(url, "", true);
//                    locationFragment.setFragment_contener(fragment_contener);
//                    activity.getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fl_fragment_container, locationFragment).commitAllowingStateLoss();
                    FragmentManage.startFragment(activity, locationFragment);
                }
            } else {
//                setViewVisibility(fragment_contener, View.VISIBLE);
                String url = TerminalFactory.getSDK().getParam(Params.LOCATION_URL, "");
                if (org.apache.http.util.TextUtils.isEmpty(TerminalFactory.getSDK().getParam(Params.LOCATION_URL, ""))) {
                    ToastUtil.showToast(activity, activity.getString(R.string.text_please_go_to_the_management_background_configuration_location_url));
                } else {
                    LocationFragment locationFragment = LocationFragment.getInstance(url, "", true);
//                    locationFragment.setFragment_contener(fragment_contener);
//                    activity.getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fl_fragment_container, locationFragment).commitAllowingStateLoss();
                    FragmentManage.startFragment(activity, locationFragment);
                }
            }
        }

        /**  进入图片预览界面  **/
        if (terminalMessage.messageType == MessageType.PICTURE.getCode()) {
//            setViewVisibility(fragment_contener, View.VISIBLE);
            FileInfo fileInfo = new FileInfo();
            fileInfo.setFilePath(terminalMessage.messagePath);
            List<FileInfo> images = new ArrayList<>();
            images.add(fileInfo);
//            activity.getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fl_fragment_container, new ImagePreviewFragment(images)).commitAllowingStateLoss();
            FragmentManage.startFragment(activity, new ImagePreviewFragment(images));
        }

        /**  上报图像  **/
        if (terminalMessage.messageType == MessageType.VIDEO_LIVE.getCode()) {
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
                    Long endChatTime = jsonObject.getLong("endChatTime");
                    if (living) {
                        int resultCode = LiveUtil.requestToWatchLiving(terminalMessage);
                        if(resultCode !=0){
                            ToastUtil.livingFailToast(activity, resultCode, TerminalErrorCode.LIVING_PLAYING.getErrorCode());
                        }
                    } else {
                        // TODO: 2018/8/7
                        LiveUtil.getHistoryLiveUrls(terminalMessage);
                    }
                }
            });
        }

        if (terminalMessage.messageType == MessageType.AUDIO.getCode()) {
            logger.debug("点击了录音消息！");
        }
        /**  跳转到合并转发  **/
        if (terminalMessage.messageType == MessageType.MERGE_TRANSMIT.getCode()) {
            MergeTransmitListFragment fragment = new MergeTransmitListFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.TERMINALMESSAGE,terminalMessage);
            bundle.putBoolean(Constants.IS_GROUP,isGroup);
            bundle.putInt(Constants.USER_ID,userId);
            fragment.setArguments(bundle);
//            fragment.setFragment_contener(fragment_contener);
//            activity.getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fl_fragment_container, fragment).commit();

            FragmentManage.startFragment(activity, fragment);
        }
    }
}

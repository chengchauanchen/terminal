package cn.vsx.vc.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zectec.imageandfileselector.view.BubbleImageView;
import com.zectec.imageandfileselector.view.LoadingCircleView;

import cn.vsx.vc.R;

/**
 * Created by dragon on 2017/11/15.
 */

public class ChatViewHolder extends RecyclerView.ViewHolder{
    public TextView timeStamp;//消息时间
    public RelativeLayout reBubble;//消息的主体
    public ImageView ivAvatar;//头像
    public CheckBox cbForward;//合并转发


    public TextView tvNick;//昵称
    public RelativeLayout reMain;
    public ImageView ivMsgStatus;//消息发送状态
    public TextView tvContent;//文本消息,位置消息（位置）,文件消息（名字）
    public BubbleImageView ivContent;//图片消息（缩略图）,位置消息（位置图片）,文件消息（文件图标）
    //语音消息
    public TextView tvDuration;//语音长度
    public ImageView ivUnread;
    public ImageView ivVoice;//语音图标
    public ImageView iv_voice_image_anim;//播放组呼录音的图标动画
    //活动消息
    public TextView tvMonth;
    public TextView tvDay;
    public TextView tvTitle;
    public TextView tvPlace;
    //发送消息
    public ProgressBar progressBar;
    public TextView tv_progress;

    public ProgressBar progress_bar_uploading;
    public TextView tv_progress_uploading;

    //文件消息
    public TextView tvFileSize;
    public ImageView iv_temp;//文件类型图标
    //消息状态
    public TextView tv_ack_msg;
    //送达显示
    public TextView tv_delivered;
    //小视频
    public LoadingCircleView loadingView;

    /**  图像  ***/
    public LinearLayout ll_botoom_to_watch;//去观看图像
    public TextView tv_watch_time;//观看图像结束之后显示观看时间
    public RelativeLayout live_bubble;
    public TextView live_tv_chatcontent;
    public ImageView iv_image;
    /**  人脸识别  **/
    public ListView lv_face_pair;
    public TextView tv_error_msg;

    public TextView tv_error_delete;
    public View placeHolder;

    //合并转发
    public TextView tv_title;//标题
    public TextView tv_content;//标题

    public ChatViewHolder(View itemView, boolean isReceiver) {
        super(itemView);
        reBubble = (RelativeLayout) itemView.findViewById(R.id.bubble);
        ivAvatar = (ImageView) itemView.findViewById(R.id.iv_userhead);
        timeStamp = (TextView) itemView.findViewById(R.id.timestamp);
        reMain = (RelativeLayout) itemView.findViewById(R.id.re_main);

        cbForward = (CheckBox) itemView.findViewById(R.id.cb_forward);
        placeHolder = itemView.findViewById(R.id.placeholder);
        if (isReceiver) {
            tvNick = (TextView) itemView.findViewById(R.id.tv_userid);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
        } else {
            tv_ack_msg = (TextView) itemView.findViewById(R.id.tv_ack_msg);
            tv_delivered = (TextView) itemView.findViewById(R.id.tv_delivered);

            ivMsgStatus = (ImageView) itemView.findViewById(R.id.msg_status);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
            tv_error_delete = (TextView) itemView.findViewById(R.id.tv_error);
        }
    }
    //文本
    public void textFindViewById(View itemView,boolean isReceiver){
        tvContent = (TextView) itemView.findViewById(R.id.tv_chatcontent);
    }
    //图片
    public void imageFindViewById(View itemView,boolean isReceiver){
        ivContent = (BubbleImageView) itemView.findViewById(R.id.bubbleImage);
        progressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
        tv_progress = (TextView) itemView.findViewById(R.id.tv_progress);
        if (!isReceiver) {
            progress_bar_uploading = (ProgressBar) itemView.findViewById(R.id.progress_bar_uploading);
            tv_progress_uploading = (TextView) itemView.findViewById(R.id.tv_progress_uploading);
        }
    }
    //小视频
    public void videoFindViewById(View itemView,boolean isReceiver){
        ivContent = (BubbleImageView) itemView.findViewById(R.id.iv_content);
//        progressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
//        tv_progress = (TextView) itemView.findViewById(R.id.tv_progress);
        tvDuration = (TextView) itemView.findViewById(R.id.tv_voice_length);
        loadingView = (LoadingCircleView) itemView.findViewById(R.id.loading_view);
    }
    //个呼
    public void privateCallFindViewById(View itemView,boolean isReceiver){
        tvContent = (TextView) itemView.findViewById(R.id.tv_chatcontent);
    }

    //组呼---录音
    public void groupCallFindViewById(View itemView,boolean isReceiver){
        tvDuration = (TextView) itemView.findViewById(R.id.tv_voice_length);
        ivVoice = (ImageView) itemView.findViewById(R.id.iv_voice);
        iv_voice_image_anim = (ImageView) itemView.findViewById(R.id.iv_voice_image_anim);
    }

    //组呼---录音（接收）
    public void groupCallReceivedFindViewById(View itemView,boolean isReceiver){
        ivUnread = (ImageView) itemView.findViewById(R.id.iv_unread_voice);
    }

    //定位
    public void locationFindViewById(View itemView,boolean isReceiver){
        tvContent = (TextView) itemView.findViewById(R.id.tv_chatcontent);
    }

    //文件
    public void fileFindViewById(View itemView,boolean isReceiver){
        iv_temp = (ImageView) itemView.findViewById(R.id.iv_temp);
        progressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
        tvFileSize = (TextView) itemView.findViewById(R.id.tv_file_size);
        tvContent = (TextView) itemView.findViewById(R.id.tv_chatcontent);
        tv_progress = (TextView) itemView.findViewById(R.id.tv_progress);

    }
    //文件（发送）
    public void fileSendFindViewById(View itemView,boolean isReceiver){
        progress_bar_uploading = (ProgressBar) itemView.findViewById(R.id.progress_bar_uploading);
        tv_progress_uploading = (TextView) itemView.findViewById(R.id.tv_progress_uploading);

    }

    //图像
    public void videoLiveFindViewById(View itemView,boolean isReceiver){
        tvContent = (TextView) itemView.findViewById(R.id.tv_chatcontent);
        live_bubble = (RelativeLayout) itemView.findViewById(R.id.live_bubble);
        live_tv_chatcontent = (TextView) itemView.findViewById(R.id.live_tv_chatcontent);
        iv_image = (ImageView) itemView.findViewById(R.id.iv_image);
    }

    //图像（接收）
    public void videoLiveReceivedFindViewById(View itemView,boolean isReceiver){
        ll_botoom_to_watch = (LinearLayout) itemView.findViewById(R.id.ll_botoom_to_watch);
        tv_watch_time = (TextView) itemView.findViewById(R.id.tv_watch_time);
    }

    //超链接
    public void hyperlinkFindViewById(View itemView,boolean isReceiver){
        lv_face_pair = (ListView) itemView.findViewById(R.id.lv_face_pair);
        tv_error_msg = (TextView) itemView.findViewById(R.id.tv_error_msg);
    }
    //合并转发
    public void mergeTransmitFindViewById(View itemView,boolean isReceiver){
        tv_title = (TextView) itemView.findViewById(R.id.tv_title);
        tv_content = (TextView) itemView.findViewById(R.id.tv_content);
    }

    public static class TextReceivedHolder extends ChatViewHolder {
        public TextReceivedHolder(View itemView,boolean isReceiver) {
            super(itemView,isReceiver);
            textFindViewById(itemView,isReceiver);
        }
    }
    public static class TextSendHolder extends ChatViewHolder {
        public TextSendHolder(View itemView,boolean isReceiver) {
            super(itemView,isReceiver);
            textFindViewById(itemView,isReceiver);
        }
    }
    public static class ImageReceivedHolder extends ChatViewHolder {
        public ImageReceivedHolder(View itemView,boolean isReceiver) {
            super(itemView,isReceiver);
            imageFindViewById(itemView,isReceiver);
        }
    }
    public static class ImageSendHolder extends ChatViewHolder {
        public ImageSendHolder(View itemView,boolean isReceiver) {
            super(itemView,isReceiver);
            imageFindViewById(itemView,isReceiver);
        }
    }

    public static class VideoReceivedHolder extends ChatViewHolder {
        public VideoReceivedHolder(View itemView,boolean isReceiver) {
            super(itemView,isReceiver);
            videoFindViewById(itemView,isReceiver);
        }
    }
    public static class VideoSendHolder extends ChatViewHolder {
        public VideoSendHolder(View itemView,boolean isReceiver) {
            super(itemView,isReceiver);
            videoFindViewById(itemView,isReceiver);
        }
    }
    public static class PrivateCallReceivedHolder extends ChatViewHolder {
        public PrivateCallReceivedHolder(View itemView,boolean isReceiver) {
            super(itemView,isReceiver);
            privateCallFindViewById(itemView,isReceiver);
        }
    }
    public static class PrivateCallSendHolder extends ChatViewHolder {
        public PrivateCallSendHolder(View itemView,boolean isReceiver) {
            super(itemView,isReceiver);
            privateCallFindViewById(itemView,isReceiver);
        }
    }

    public static class VoiceReceivedHolder extends VoiceSendHolder {
        public VoiceReceivedHolder(View itemView,boolean isReceiver) {
            super(itemView,isReceiver);
            groupCallReceivedFindViewById(itemView,isReceiver);
        }
    }
    public static class VoiceSendHolder extends ChatViewHolder {
        public VoiceSendHolder(View itemView,boolean isReceiver) {
            super(itemView,isReceiver);
            groupCallFindViewById(itemView,isReceiver);
        }
    }
    public static class LocationReceivedHolder extends ChatViewHolder {
        public LocationReceivedHolder(View itemView,boolean isReceiver) {
            super(itemView,isReceiver);
            locationFindViewById(itemView,isReceiver);
        }
    }
    public static class LocationSendHolder extends ChatViewHolder {
        public LocationSendHolder(View itemView,boolean isReceiver) {
            super(itemView,isReceiver);
            locationFindViewById(itemView,isReceiver);
        }
    }
    public static class FileReceivedHolder extends ChatViewHolder {
        public FileReceivedHolder(View itemView,boolean isReceiver) {
            super(itemView,isReceiver);
            fileFindViewById(itemView,isReceiver);
        }
    }
    public static class FileSendHolder extends FileReceivedHolder {
        public FileSendHolder(View itemView,boolean isReceiver) {
            super(itemView,isReceiver);
            fileSendFindViewById(itemView,isReceiver);
        }
    }

    public static class LiveReceivedHolder extends LiveSendHolder {
        public LiveReceivedHolder(View itemView,boolean isReceiver) {
            super(itemView,isReceiver);
            videoLiveReceivedFindViewById(itemView,isReceiver);
        }
    }
    public static class LiveSendHolder extends ChatViewHolder {
        public LiveSendHolder(View itemView,boolean isReceiver) {
            super(itemView,isReceiver);
            videoLiveFindViewById(itemView,isReceiver);
        }
    }

    public static class HyperlinkReceivedHolder extends ChatViewHolder {
        public HyperlinkReceivedHolder(View itemView,boolean isReceiver) {
            super(itemView,isReceiver);
            hyperlinkFindViewById(itemView,isReceiver);
        }
    }
    public static class HyperlinkSendHolder extends ChatViewHolder {
        public HyperlinkSendHolder(View itemView,boolean isReceiver) {
            super(itemView,isReceiver);
            hyperlinkFindViewById(itemView,isReceiver);
        }
    }

    public static class TextMergeTransmitHolder extends ChatViewHolder {
        public TextMergeTransmitHolder(View itemView,boolean isReceiver) {
            super(itemView,isReceiver);
            mergeTransmitFindViewById(itemView,isReceiver);
        }
    }
}

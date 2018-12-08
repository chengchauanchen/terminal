package cn.vsx.vc.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zectec.imageandfileselector.view.BubbleImageView;

/**
 * Created by dragon on 2017/11/15.
 */

public class ChatViewHolder {
    public TextView timeStamp;//消息时间
    public RelativeLayout reBubble;//消息的主体
    public ImageView ivAvatar;//头像

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

    /**  图像  ***/
    public LinearLayout ll_botoom_to_watch;//去观看图像
    public TextView tv_watch_time;//观看图像结束之后显示观看时间
    public RelativeLayout live_bubble;
    public TextView live_tv_chatcontent;
    /**  人脸识别  **/
    public ListView lv_face_pair;
    public TextView tv_error_msg;

    public TextView tv_error_delete;
    public View placeHolder;

    public static class TextReceivedHolder extends ChatViewHolder {}
    public static class TextSendHolder extends ChatViewHolder {}
    public static class ImageReceivedHolder extends ChatViewHolder {}
    public static class ImageSendHolder extends ChatViewHolder {}
    public static class VoiceReceivedHolder extends ChatViewHolder {}
    public static class VoiceSendHolder extends ChatViewHolder {}
    public static class FileReceivedHolder extends ChatViewHolder {}
    public static class FileSendHolder extends ChatViewHolder {}
    public static class VideoReceivedHolder extends ChatViewHolder {}
    public static class VideoSendHolder extends ChatViewHolder {}
    public static class LocationReceivedHolder extends ChatViewHolder {}
    public static class LocationSendHolder extends ChatViewHolder {}
    public static class LiveReceivedHolder extends ChatViewHolder {}
    public static class LiveSendHolder extends ChatViewHolder {}
    public static class PrivateCallReceivedHolder extends ChatViewHolder {}
    public static class PrivateCallSendHolder extends ChatViewHolder {}
    public static class HyperlinkReceivedHolder extends ChatViewHolder {}
    public static class HyperlinkSendHolder extends ChatViewHolder {}
}

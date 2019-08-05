package com.vsxin.terminalpad.mvp.ui.adapter.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vsxin.terminalpad.R;
import com.zectec.imageandfileselector.view.BubbleImageView;
import com.zectec.imageandfileselector.view.LoadingCircleView;


/**
 * Created by dragon on 2017/11/15.
 */

public class MergeTransmitViewHolder extends RecyclerView.ViewHolder {
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

    //发送消息
    public ProgressBar progressBar;
    public TextView tv_progress;

    //文件消息
    public TextView tvFileSize;
    public ImageView iv_temp;//文件类型图标
    //小视频
    public LoadingCircleView loadingView;

    /**
     * 图像
     ***/
    public LinearLayout ll_botoom_to_watch;//去观看图像
    public TextView tv_watch_time;//观看图像结束之后显示观看时间
    public RelativeLayout live_bubble;
    public TextView live_tv_chatcontent;
    public ImageView iv_image;
    /**
     * 人脸识别
     **/
    public ListView lv_face_pair;
    public TextView tv_error_msg;

    public TextView tv_error_delete;
    public View placeHolder;

    //合并转发
    public TextView tv_title;//标题
    public TextView tv_content;//标题

    public MergeTransmitViewHolder(View itemView) {
        super(itemView);
        reMain = (RelativeLayout) itemView.findViewById(R.id.re_main);
        ivAvatar = (ImageView) itemView.findViewById(R.id.iv_userhead);
        timeStamp = (TextView) itemView.findViewById(R.id.timestamp);
        tvNick = (TextView) itemView.findViewById(R.id.tv_userid);
        reBubble = (RelativeLayout) itemView.findViewById(R.id.bubble);
        placeHolder = itemView.findViewById(R.id.placeholder);
    }

    //文本
    public void textFindViewById(View itemView) {
        tvContent = (TextView) itemView.findViewById(R.id.tv_chatcontent);
    }

    //图片
    public void imageFindViewById(View itemView) {
        ivContent = (BubbleImageView) itemView.findViewById(R.id.bubbleImage);
        progressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
        tv_progress = (TextView) itemView.findViewById(R.id.tv_progress);
    }

    //小视频
    public void videoFindViewById(View itemView) {
        ivContent = (BubbleImageView) itemView.findViewById(R.id.iv_content);
        tvDuration = (TextView) itemView.findViewById(R.id.tv_voice_length);
        loadingView = (LoadingCircleView) itemView.findViewById(R.id.loading_view);
    }

    //个呼
    public void privateCallFindViewById(View itemView) {
        tvContent = (TextView) itemView.findViewById(R.id.tv_chatcontent);
    }

    //组呼---录音
    public void groupCallFindViewById(View itemView) {
        tvDuration = (TextView) itemView.findViewById(R.id.tv_voice_length);
        ivVoice = (ImageView) itemView.findViewById(R.id.iv_voice);
        iv_voice_image_anim = (ImageView) itemView.findViewById(R.id.iv_voice_image_anim);
        ivUnread = (ImageView) itemView.findViewById(R.id.iv_unread_voice);
    }


    //定位
    public void locationFindViewById(View itemView) {
        tvContent = (TextView) itemView.findViewById(R.id.tv_chatcontent);
    }

    //文件
    public void fileFindViewById(View itemView) {
        iv_temp = (ImageView) itemView.findViewById(R.id.iv_temp);
        progressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
        tvFileSize = (TextView) itemView.findViewById(R.id.tv_file_size);
        tvContent = (TextView) itemView.findViewById(R.id.tv_chatcontent);
        tv_progress = (TextView) itemView.findViewById(R.id.tv_progress);

    }

    //图像
    public void videoLiveFindViewById(View itemView) {
        tvContent = (TextView) itemView.findViewById(R.id.tv_chatcontent);
        live_bubble = (RelativeLayout) itemView.findViewById(R.id.live_bubble);
        live_tv_chatcontent = (TextView) itemView.findViewById(R.id.live_tv_chatcontent);
        iv_image = (ImageView) itemView.findViewById(R.id.iv_image);
        ll_botoom_to_watch = (LinearLayout) itemView.findViewById(R.id.ll_botoom_to_watch);
        tv_watch_time = (TextView) itemView.findViewById(R.id.tv_watch_time);
    }

    //超链接
    public void hyperlinkFindViewById(View itemView) {
        lv_face_pair = (ListView) itemView.findViewById(R.id.lv_face_pair);
        tv_error_msg = (TextView) itemView.findViewById(R.id.tv_error_msg);
    }

    //合并转发
    public void mergeTransmitFindViewById(View itemView) {
        tv_title = (TextView) itemView.findViewById(R.id.tv_title);
        tv_content = (TextView) itemView.findViewById(R.id.tv_content);
    }

    public static class TextHolder extends MergeTransmitViewHolder {
        public TextHolder(View itemView) {
            super(itemView);
            textFindViewById(itemView);
        }
    }

    public static class ImageHolder extends MergeTransmitViewHolder {
        public ImageHolder(View itemView) {
            super(itemView);
            imageFindViewById(itemView);
        }
    }


    public static class VoiceHolder extends MergeTransmitViewHolder {
        public VoiceHolder(View itemView) {
            super(itemView);
            groupCallFindViewById(itemView);
        }
    }

    public static class FileHolder extends MergeTransmitViewHolder {
        public FileHolder(View itemView) {
            super(itemView);
            fileFindViewById(itemView);
        }
    }

    public static class VideoHolder extends MergeTransmitViewHolder {
        public VideoHolder(View itemView) {
            super(itemView);
            videoFindViewById(itemView);
        }
    }
    public static class LocationHolder extends MergeTransmitViewHolder {
        public LocationHolder(View itemView) {
            super(itemView);
            locationFindViewById(itemView);
        }
    }

    public static class LiveHolder extends MergeTransmitViewHolder {
        public LiveHolder(View itemView) {
            super(itemView);
            videoLiveFindViewById(itemView);
        }
    }

    public static class PrivateCallHolder extends MergeTransmitViewHolder {
        public PrivateCallHolder(View itemView) {
            super(itemView);
            privateCallFindViewById(itemView);
        }
    }

    public static class HyperlinkHolder extends MergeTransmitViewHolder {
        public HyperlinkHolder(View itemView) {
            super(itemView);
            hyperlinkFindViewById(itemView);
        }
    }

    public static class MergeTransmitHolder extends MergeTransmitViewHolder {
        public MergeTransmitHolder(View itemView) {
            super(itemView);
            mergeTransmitFindViewById(itemView);
        }
    }
}

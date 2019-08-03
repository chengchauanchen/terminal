package com.vsxin.terminalpad.mvp.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ixiaoma.xiaomabus.architecture.mvp.refresh.adapter.BaseRecycleViewAdapter;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.utils.HandleIdUtil;
import com.zectec.imageandfileselector.utils.FileUtil;

import org.apache.log4j.Logger;

import java.io.File;

import cn.vsx.hamster.common.MessageCategory;
import cn.vsx.hamster.common.MessageStatus;
import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.tools.DateUtils;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.TerminalMessageUtil;
import cn.vsx.hamster.terminalsdk.tools.Util;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * @author qzw
 * <p>
 * app模块-消息模块
 */
public class MessageAdapter extends BaseRecycleViewAdapter<TerminalMessage,MessageAdapter.ViewHolder>{


    public Logger logger = Logger.getLogger(getClass());

    public MessageAdapter (Context context ) {
        super(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = View.inflate(mContext, R.layout.fragment_news_listview_item, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){
        TerminalMessage terminalMessage = datas.get(position);
        boolean isReceiver = terminalMessage.messageFromId != MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);

        //设置会话信息
        //        if (terminalMessage.messageId == 0) {
        //            viewHolder.tv_last_msg.setText("");
        //        } else {
        setMsg(holder, terminalMessage);
        //        }
        //设置名字
        if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_PERSONAGE.getCode()) {//个人消息，显示人名
            if (TerminalMessageUtil.isLiveMessage(terminalMessage)) {//自己是直播方
                holder.tv_user_name.setText(mContext.getString(R.string.text_image_assistant));
            }else if(terminalMessage.messageType == MessageType.CALL_RECORD.getCode()){//电话助手目前不属于消息
                holder.tv_user_name.setText(mContext.getString(R.string.text_telephone_assistant));
            }else if(terminalMessage.messageType == MessageType.WARNING_INSTANCE.getCode()){//警情
                holder.tv_user_name.setText(mContext.getString(R.string.text_to_warning));
            }
            else {
                if (isReceiver) {//接受消息，显示对方名字
                    holder.tv_user_name.setText(HandleIdUtil.handleName(terminalMessage.messageFromName));
                } else {//自己发送的也显示对方名字
                    holder.tv_user_name.setText(HandleIdUtil.handleName(terminalMessage.messageToName));
                }
            }
            //            viewHolder.tv_current_group.setVisibility(View.GONE);
        } else if (TerminalMessageUtil.isGroupMessage(terminalMessage)){//组消息，显示组名
            //如果是合成作战组，上面显示合成作战组
            if (TerminalMessageUtil.isCombatGroup(terminalMessage)){
//                if(isNewsFragment){
                    holder.tv_user_name.setText(mContext.getString(R.string.text_to_help_combat));
//                }else {
//                    holder.tv_user_name.setText(terminalMessage.messageToName);
//                }
            } else if(terminalMessage.messageType == MessageType.WARNING_INSTANCE.getCode()){
                holder.tv_user_name.setText(mContext.getString(R.string.text_to_warning));
            }else {
                holder.tv_user_name.setText(terminalMessage.messageToName);
            }
        }
        //设置最后一条消息时间
        if (terminalMessage.sendTime != 0) {
            holder.tv_last_msg_time.setText(DateUtils.getNewChatTime(terminalMessage.sendTime));
        } else {
            holder.tv_last_msg_time.setText("");
            holder.tv_last_msg.setText("");
        }
        if(TerminalMessageUtil.isGroupMessage(terminalMessage)){
            holder.iv_user_photo.setBackgroundResource(R.drawable.group);
        }else {
            holder.iv_user_photo.setBackgroundResource(R.drawable.user_photo);
        }

        //设置未读消息条目
        if (terminalMessage.unReadCount > 0){
            holder.tv_unread_msg_num.setVisibility(View.VISIBLE);
            if(terminalMessage.unReadCount < 100) {
                holder.tv_unread_msg_num.setText(""+terminalMessage.unReadCount);
            }
            else {
                holder.tv_unread_msg_num.setText("99+");
            }
        }else {
            holder.tv_unread_msg_num.setVisibility(View.GONE);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount(){
        return datas.size();
    }

    private void setMsg(final ViewHolder viewHolder, TerminalMessage terminalMessage) {
        setLastMessage(viewHolder, terminalMessage);

    }

    private void setLastMessage(ViewHolder viewHolder, TerminalMessage terminalMessage){
        int userId =0;
        //组会话userId= terminalMessage.messageToId
        if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){//组会话
            userId = terminalMessage.messageToId;
        }else if(terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_PERSONAGE.getCode()){//个人会话
            //个人会话userId需要判断
            boolean isReceiver = terminalMessage.messageFromId != MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);//是否为别人发的消息
            if(isReceiver){
                userId = terminalMessage.messageFromId;
            }else {
                userId = terminalMessage.messageToId;
            }
        }
        String unsendMessage = mContext.getSharedPreferences("unsendMessage", Context.MODE_PRIVATE).getString(String.valueOf(userId),"");
        if(!Util.isEmpty(unsendMessage)){
            String text = String.format("<font color='red'>[草稿]</font>%s",unsendMessage);
            viewHolder.tv_last_msg.setText(Html.fromHtml(text));
        }else {
            if(terminalMessage.messageType ==  MessageType.SHORT_TEXT.getCode()) {
                String content = terminalMessage.messageBody.getString(JsonParam.CONTENT);
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    viewHolder.tv_last_msg.setText(terminalMessage.messageFromName+":"+content);
                }else {
                    viewHolder.tv_last_msg.setText(content);
                }
            }
            if(terminalMessage.messageType ==  MessageType.LONG_TEXT.getCode()) {
                String path = terminalMessage.messagePath;
                File file = new File(path);
                if (!file.exists()) {
                    MyTerminalFactory.getSDK().getTerminalMessageManager().setMessagePath(terminalMessage, false);
                    MyTerminalFactory.getSDK().download(terminalMessage, true);
                }
                String content = FileUtil.getStringFromFile(file);
                logger.info("长文本： path:"+path+"    content:"+content);
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    viewHolder.tv_last_msg.setText(String.format(mContext.getString(R.string.text_message_list_text_),terminalMessage.messageFromName,content));
                } else {
                    viewHolder.tv_last_msg.setText(content);
                }
            }
            if(terminalMessage.messageType ==  MessageType.PICTURE.getCode()) {
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    viewHolder.tv_last_msg.setText(String.format(mContext.getString(R.string.text_message_list_picture_),terminalMessage.messageFromName));
                } else {
                    viewHolder.tv_last_msg.setText(R.string.text_message_list_picture);
                }
            }
            if(terminalMessage.messageType ==  MessageType.AUDIO.getCode()) {
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    viewHolder.tv_last_msg.setText(String.format(mContext.getString(R.string.text_message_list_voice_),terminalMessage.messageFromName));
                } else {
                    viewHolder.tv_last_msg.setText(R.string.text_message_list_voice);
                }
            }
            if(terminalMessage.messageType ==  MessageType.VIDEO_CLIPS.getCode()) {
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    viewHolder.tv_last_msg.setText(String.format(mContext.getString(R.string.text_message_list_video_),terminalMessage.messageFromName));
                } else {
                    viewHolder.tv_last_msg.setText(R.string.text_message_list_video);
                }
            }
            if(terminalMessage.messageType ==  MessageType.CALL_RECORD.getCode()) {
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    viewHolder.tv_last_msg.setText(String.format(mContext.getString(R.string.text_message_list_call_record_),terminalMessage.messageFromName));
                } else {
                    viewHolder.tv_last_msg.setText(R.string.text_message_list_call_record);
                }
            }
            if(terminalMessage.messageType ==  MessageType.FILE.getCode()) {
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    viewHolder.tv_last_msg.setText(String.format(mContext.getString(R.string.text_message_list_file_),terminalMessage.messageFromName));
                } else {
                    viewHolder.tv_last_msg.setText(R.string.text_message_list_file);
                }
            }
            if(terminalMessage.messageType ==  MessageType.POSITION.getCode()) {
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    viewHolder.tv_last_msg.setText(String.format(mContext.getString(R.string.text_message_list_location_),terminalMessage.messageFromName));
                } else {
                    viewHolder.tv_last_msg.setText(R.string.text_message_list_location);
                }
            }
            if(terminalMessage.messageType ==  MessageType.AFFICHE.getCode()) {
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    viewHolder.tv_last_msg.setText(String.format(mContext.getString(R.string.text_message_list_notice_),terminalMessage.messageFromName));
                } else {
                    viewHolder.tv_last_msg.setText(R.string.text_message_list_notice);
                }
            }
            if(terminalMessage.messageType ==  MessageType.WARNING_INSTANCE.getCode()) {
                if(TerminalMessageUtil.hasWarningDetail(terminalMessage)){
                    viewHolder.tv_last_msg.setText(terminalMessage.messageBody.getString(JsonParam.SUMMARY));
                }else {
                    viewHolder.tv_last_msg.setText(R.string.text_message_list_warning);
                }
            }
            if(terminalMessage.messageType ==  MessageType.PRIVATE_CALL.getCode()) {
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    viewHolder.tv_last_msg.setText(String.format(mContext.getString(R.string.text_message_list_personal_call_),terminalMessage.messageFromName));
                } else {
                    viewHolder.tv_last_msg.setText(R.string.text_message_list_personal_call);
                }
            }
            if(terminalMessage.messageType ==  MessageType.VIDEO_LIVE.getCode()) {
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    viewHolder.tv_last_msg.setText(String.format(mContext.getString(R.string.text_message_list_image_),terminalMessage.messageFromName));
                } else {
                    viewHolder.tv_last_msg.setText(R.string.text_message_list_image);
                }
            }
            if(terminalMessage.messageType ==  MessageType.GROUP_CALL.getCode()) {
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    viewHolder.tv_last_msg.setText(String.format(mContext.getString(R.string.text_message_list_group_call_),terminalMessage.messageFromName));
                } else {
                    viewHolder.tv_last_msg.setText(R.string.text_message_list_group_call);
                }
            }
            if(terminalMessage.messageType ==  MessageType.AUDIO.getCode()) {
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    viewHolder.tv_last_msg.setText(String.format(mContext.getString(R.string.text_message_list_sound_recording_),terminalMessage.messageFromName));
                } else {
                    viewHolder.tv_last_msg.setText(R.string.text_message_list_sound_recording);
                }
            }
            if(terminalMessage.messageType ==  MessageType.HYPERLINK.getCode()) {//人脸识别
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    viewHolder.tv_last_msg.setText(String.format(mContext.getString(R.string.text_message_list_face_recognition_),terminalMessage.messageFromName));
                } else {
                    viewHolder.tv_last_msg.setText(R.string.text_message_list_face_recognition);
                }
            }
            if(terminalMessage.messageType ==  MessageType.MERGE_TRANSMIT.getCode()) {
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    viewHolder.tv_last_msg.setText(String.format(mContext.getString(R.string.text_message_list_merge_transmit_),terminalMessage.messageFromName));
                }else{
                    viewHolder.tv_last_msg.setText(R.string.text_message_list_merge_transmit);
                }
            }
            //消息撤回
            if(MessageStatus.valueOf(terminalMessage.messageStatus).getCode() == MessageStatus.MESSAGE_RECALL.getCode()){
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    viewHolder.tv_last_msg.setText(String.format(mContext.getString(R.string.text_message_list_with_draw_),terminalMessage.messageFromName));
                }else{
                    viewHolder.tv_last_msg.setText(R.string.text_message_list_with_draw);
                }
            }if(terminalMessage.messageType == MessageType.GB28181_RECORD.getCode()){
                viewHolder.tv_last_msg.setText(R.string.text_message_LTE_live);
            }else if(terminalMessage.messageType == MessageType.OUTER_GB28181_RECORD.getCode()){
                viewHolder.tv_last_msg.setText(R.string.text_message_city_live);
            }
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        ImageView iv_user_photo;

        TextView tv_unread_msg_num;

        TextView tv_user_name;

        TextView tv_last_msg;

        TextView tv_last_msg_time;

        public ViewHolder (View view) {
            super(view);
            iv_user_photo = view.findViewById(R.id.iv_user_photo);
            tv_unread_msg_num = view.findViewById(R.id.tv_unread_msg_num);
            tv_user_name = view.findViewById(R.id.tv_user_name);
            tv_last_msg = view.findViewById(R.id.tv_last_msg);
            tv_last_msg_time = view.findViewById(R.id.tv_last_msg_time);
        }
    }
}

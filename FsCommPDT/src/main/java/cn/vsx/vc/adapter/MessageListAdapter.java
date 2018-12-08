package cn.vsx.vc.adapter;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zectec.imageandfileselector.utils.DateUtils;
import com.zectec.imageandfileselector.utils.FileUtil;

import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.vsx.hamster.common.MessageCategory;
import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.Util;
import cn.vsx.vc.R;
import cn.vsx.vc.utils.HandleIdUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 *  消息列表的adapter
 * Created by gt358 on 2017/9/8.
 */

public class MessageListAdapter extends BaseAdapter {
    private HashMap<Integer, String> idNameMap;
    private Context context;
    private List<TerminalMessage> messageList = new ArrayList<>();
    public Logger logger = Logger.getLogger(getClass());

    public MessageListAdapter (Context context, List<TerminalMessage> messageList, HashMap<Integer, String> idNameMap) {
        this.context = context;
        this.messageList = messageList;
        this.idNameMap = idNameMap;
    }

    @Override
    public int getCount() {
        return messageList.size();
    }

    @Override
    public Object getItem(int position) {
        return messageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.fragment_news_listview_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        TerminalMessage terminalMessage = messageList.get(position);
        boolean isReceiver = terminalMessage.messageFromId != MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);

        //设置会话信息
//        if (terminalMessage.messageId == 0) {
//            viewHolder.tv_last_msg.setText("");
//        } else {
        setMsg(viewHolder, terminalMessage);
        //        }
        //设置名字
        if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_PERSONAGE.getCode()) {//个人消息，显示人名
            if (terminalMessage.messageType == MessageType.VIDEO_LIVE.getCode() &&
                    terminalMessage.messageFromId == terminalMessage.messageToId &&
                    terminalMessage.messageFromId == TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)) {//自己是直播方
                viewHolder.tv_user_name.setText("图像助手");
            }else if(terminalMessage.messageType == MessageType.CALL_RECORD.getCode()){//电话助手目前不属于消息
                viewHolder.tv_user_name.setText("电话助手");
            }
            else {
                if (isReceiver) {//接受消息，显示对方名字
                    viewHolder.tv_user_name.setText(HandleIdUtil.handleName(idNameMap.get(terminalMessage.messageFromId)));
                } else {//自己发送的也显示对方名字
                    viewHolder.tv_user_name.setText(HandleIdUtil.handleName(idNameMap.get(terminalMessage.messageToId)));
                }
            }
            viewHolder.tv_current_group.setVisibility(View.GONE);
        } else if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){//组消息，显示组名
            viewHolder.tv_user_name.setText(idNameMap.get(terminalMessage.messageToId));
            if (terminalMessage.messageToId == TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)) {
                viewHolder.tv_current_group.setVisibility(View.VISIBLE);
            } else {
                viewHolder.tv_current_group.setVisibility(View.GONE);
            }
        }
        //设置最后一条消息时间
        if (terminalMessage.sendTime != 0) {
//            viewHolder.tv_last_msg_time.setText(DateUtils.getTimestampString(new Date(terminalMessage.sendTime)));
            viewHolder.tv_last_msg_time.setText(DateUtils.getNewChatTime(terminalMessage.sendTime));
        } else {
            viewHolder.tv_last_msg_time.setText("");
            viewHolder.tv_last_msg.setText("");
        }

        if(terminalMessage.messageFromId == terminalMessage.messageToId &&
                terminalMessage.messageFromId == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) &&
                terminalMessage.messageType == MessageType.VIDEO_LIVE.getCode()) {/**  图像助手 **/
            viewHolder.iv_user_photo.setBackgroundResource(R.drawable.video_photo);
        }else if(terminalMessage.messageType == MessageType.CALL_RECORD.getCode()){/** 电话助手 **/
            viewHolder.iv_user_photo.setBackgroundResource(R.drawable.call_photo);
        }
        else if(terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
            viewHolder.iv_user_photo.setBackgroundResource(R.drawable.group_photo);
        }else {
            if (terminalMessage.messageType == MessageType.HYPERLINK.getCode()) {
                viewHolder.iv_user_photo.setBackgroundResource(R.drawable.face_recognition_photo);
            } else {
                viewHolder.iv_user_photo.setBackgroundResource(R.drawable.user_photo);
            }
        }

        //设置未读消息条目
        if (terminalMessage.unReadCount > 0){
            viewHolder.tv_unread_msg_num.setVisibility(View.VISIBLE);
            if(terminalMessage.unReadCount < 100) {
                viewHolder.tv_unread_msg_num.setText(""+terminalMessage.unReadCount);
//                viewHolder.tv_unread_msg_num.setTextSize(context.getResources().getDimensionPixelSize(R.dimen.x5));
//                viewHolder.tv_unread_msg_num.setTextSize(DensityUtil.sp2px(context, 4));
            }
            else {
                viewHolder.tv_unread_msg_num.setText("99+");
//                viewHolder.tv_unread_msg_num.setTextSize(context.getResources().getDimensionPixelSize(R.dimen.x4));
//                viewHolder.tv_unread_msg_num.setTextSize(DensityUtil.sp2px(context, 3));
            }
        }else {
            viewHolder.tv_unread_msg_num.setVisibility(View.GONE);
        }

        return convertView;
    }

    private void setMsg(final ViewHolder viewHolder, TerminalMessage terminalMessage) {
        terminalMessage.messageFromName = idNameMap.get(terminalMessage.messageFromId);
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
        String unsendMessage = context.getSharedPreferences("unsendMessage", Context.MODE_PRIVATE).getString(String.valueOf(userId),"");
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
                    viewHolder.tv_last_msg.setText(terminalMessage.messageFromName+":"+content);
                } else {
                    viewHolder.tv_last_msg.setText(content);
                }
            }
            if(terminalMessage.messageType ==  MessageType.PICTURE.getCode()) {
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    viewHolder.tv_last_msg.setText(terminalMessage.messageFromName+":"+"[图片]");
                } else {
                    viewHolder.tv_last_msg.setText("[图片]");
                }
            }
            if(terminalMessage.messageType ==  MessageType.AUDIO.getCode()) {
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    viewHolder.tv_last_msg.setText(terminalMessage.messageFromName+":"+"[语音]");
                } else {
                    viewHolder.tv_last_msg.setText("[语音]");
                }
            }
            if(terminalMessage.messageType ==  MessageType.VIDEO_CLIPS.getCode()) {
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    viewHolder.tv_last_msg.setText(terminalMessage.messageFromName+":"+"[视频]");
                } else {
                    viewHolder.tv_last_msg.setText("[视频]");
                }
            }
            if(terminalMessage.messageType ==  MessageType.CALL_RECORD.getCode()) {
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    viewHolder.tv_last_msg.setText(terminalMessage.messageFromName+":"+"[通话记录]");
                } else {
                    viewHolder.tv_last_msg.setText("[通话记录]");
                }
            }
            if(terminalMessage.messageType ==  MessageType.FILE.getCode()) {
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    viewHolder.tv_last_msg.setText(terminalMessage.messageFromName+":"+"[文件]");
                } else {
                    viewHolder.tv_last_msg.setText("[文件]");
                }
            }
            if(terminalMessage.messageType ==  MessageType.POSITION.getCode()) {
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    viewHolder.tv_last_msg.setText(terminalMessage.messageFromName+":"+"[位置]");
                } else {
                    viewHolder.tv_last_msg.setText("[位置]");
                }
            }
            if(terminalMessage.messageType ==  MessageType.AFFICHE.getCode()) {
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    viewHolder.tv_last_msg.setText(terminalMessage.messageFromName+":"+"[公告]");
                } else {
                    viewHolder.tv_last_msg.setText("[公告]");
                }
            }
            if(terminalMessage.messageType ==  MessageType.WARNING_INSTANCE.getCode()) {
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    viewHolder.tv_last_msg.setText(terminalMessage.messageFromName+":"+"[警情]");
                } else {
                    viewHolder.tv_last_msg.setText("[警情]");
                }
            }
            if(terminalMessage.messageType ==  MessageType.PRIVATE_CALL.getCode()) {
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    viewHolder.tv_last_msg.setText(terminalMessage.messageFromName+":"+"[个呼]");
                } else {
                    viewHolder.tv_last_msg.setText("[个呼]");
                }
            }
            if(terminalMessage.messageType ==  MessageType.VIDEO_LIVE.getCode()) {
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    viewHolder.tv_last_msg.setText(terminalMessage.messageFromName+":"+"[图像]");
                } else {
                    viewHolder.tv_last_msg.setText("[图像]");
                }
            }
            if(terminalMessage.messageType ==  MessageType.GROUP_CALL.getCode()) {
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    viewHolder.tv_last_msg.setText(terminalMessage.messageFromName+":"+"[组呼]");
                } else {
                    viewHolder.tv_last_msg.setText("[组呼]");
                }
            }
            if(terminalMessage.messageType ==  MessageType.AUDIO.getCode()) {
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    viewHolder.tv_last_msg.setText(terminalMessage.messageFromName+":"+"[录音]");
                } else {
                    viewHolder.tv_last_msg.setText("[录音]");
                }
            }
            if(terminalMessage.messageType ==  MessageType.HYPERLINK.getCode()) {//人脸识别
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    viewHolder.tv_last_msg.setText(terminalMessage.messageFromName+":"+"[人脸识别]");
                } else {
                    viewHolder.tv_last_msg.setText("[人脸识别]");
                }
            }
        }
    }

    public static class ViewHolder {
        @Bind(R.id.iv_user_photo)
        ImageView iv_user_photo;
        @Bind(R.id.tv_unread_msg_num)
        TextView tv_unread_msg_num;
        @Bind(R.id.tv_user_name)
        TextView tv_user_name;
        @Bind(R.id.tv_current_group)
        TextView tv_current_group;
        @Bind(R.id.tv_last_msg)
        TextView tv_last_msg;
        @Bind(R.id.tv_last_msg_time)
        TextView tv_last_msg_time;

        public ViewHolder (View view) {
            ButterKnife.bind(this, view);
        }
    }

}

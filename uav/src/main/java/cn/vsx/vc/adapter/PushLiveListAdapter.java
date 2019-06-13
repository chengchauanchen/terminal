package cn.vsx.vc.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.zectec.imageandfileselector.utils.DateUtils;

import org.apache.http.util.TextUtils;

import java.util.List;



import cn.vsx.hamster.common.Remark;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.vc.R;

/**
 * Created by gt358 on 2017/9/30.
 */

public class PushLiveListAdapter extends BaseAdapter {

    private Context context;
    private List<TerminalMessage> mLiveMessageList;

    public PushLiveListAdapter(Context context, List<TerminalMessage> mLiveMessageList) {
        this.context = context;
        this.mLiveMessageList = mLiveMessageList;
    }

    @Override
    public int getCount() {
        return mLiveMessageList.size();
    }

    @Override
    public Object getItem(int position) {
        return mLiveMessageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TerminalMessage terminalMessage = mLiveMessageList.get(position);
        ViewHolder holder = null;
        if(convertView == null) {
            convertView = View.inflate(context, R.layout.item_push_live_message, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        handlerTime(terminalMessage, position, holder);
        handlerLiveData(terminalMessage, holder);
        return convertView;
    }

    /***  设置图像观看数据 **/
    private void handlerLiveData (TerminalMessage terminalMessage, ViewHolder holder) {
        JSONObject messageBody = terminalMessage.messageBody;
        int resultCode = terminalMessage.resultCode;
        if(resultCode == 0) {//上报图像成功
            holder.ll_watch_success.setVisibility(View.VISIBLE);
            holder.ll_push_fail.setVisibility(View.GONE);
            if(messageBody.containsKey(JsonParam.REMARK)&&messageBody.getInteger(JsonParam.REMARK) == Remark.ACTIVE_VIDEO_LIVE) {//自己主动上报图像
                holder.tv_push_type.setText(R.string.text_personal_image_reporting);
                String title = messageBody.getString(JsonParam.TITLE);
                if(TextUtils.isEmpty(title)) {
                    holder.tv_title.setText(R.string.text_personal_image_reporting);
                }
                else {
                    holder.tv_title.setText(title);
                }
            }
            else if(messageBody.containsKey(JsonParam.REMARK)&&messageBody.getInteger(JsonParam.REMARK) == Remark.ASK_VIDEO_LIVE) {//别人邀请我上报图像

                holder.tv_push_type.setText(String.format(context.getString(R.string.text_people_request_report_name),messageBody.getString(JsonParam.QUERY_MEMBER_NAME)));
                String title = messageBody.getString(JsonParam.TITLE);
                if(TextUtils.isEmpty(title)) {
                    holder.tv_title.setText(String.format(context.getString(R.string.text_people_request_report_name),messageBody.getString(JsonParam.QUERY_MEMBER_NAME)));
                }
                else {
                    holder.tv_title.setText(title);
                }
            }
            if(messageBody.containsKey(JsonParam.END_TIME) && messageBody.containsKey(JsonParam.START_TIME)) {
                long liveTime = (messageBody.getLong(JsonParam.END_TIME) - messageBody.getLong(JsonParam.START_TIME))/1000;
                if(liveTime < 1) {
                    liveTime = 1;
                }
                holder.tv_watch_time.setText(getCallLength(liveTime));
            }
            else {
                holder.tv_watch_time.setText("- -");
            }

        }
    }

    private String getCallLength (long time) {
        int hours = (int) (time / 3600);
        time -=  hours*3600;
        int minutes = (int) (time / 60);
        time -= minutes*60;
        int sec = (int) time;

        String timeStr = "";
        if(hours != 0)
            timeStr = hours + "小时" + minutes + "分" + sec + "秒";
        else if (minutes != 0)
            timeStr = minutes + "分" + sec + "秒";
        else
            timeStr = sec + "秒";

        return timeStr;
    }

    /**  设置消息时间显示 */
    private void handlerTime (TerminalMessage terminalMessage, int position, ViewHolder holder) {
        if(terminalMessage.sendTime > 0) {
            long messageTime = terminalMessage.sendTime;
            if (position == 0) {
                holder.tv_message_time.setText(DateUtils.getNewChatTime(messageTime));
                holder.tv_message_time.setVisibility(View.VISIBLE);
            } else {
                // 两条消息时间离得如果稍长，显示时间
                long currentTime = terminalMessage.sendTime;
                long lastTime =0L;
                if(mLiveMessageList.get(position - 1).sendTime > 0) {
                    lastTime = mLiveMessageList.get(position - 1).sendTime;

                }
                else {
                    lastTime = currentTime;
                }
                handlerTime2(holder, currentTime, lastTime);
            }
        }
        else {
            long currentTime = System.currentTimeMillis();
            if (position == 0) {
                holder.tv_message_time.setText(DateUtils.getNewChatTime(currentTime));
                holder.tv_message_time.setVisibility(View.VISIBLE);
            }
            else {
                long lastTime =0L;
                if(mLiveMessageList.get(position - 1).sendTime > 0) {
                    lastTime = mLiveMessageList.get(position - 1).sendTime;
                }
                else {
                    lastTime = currentTime;
                }
                handlerTime2(holder, currentTime, lastTime);
            }

        }
    }

    private void handlerTime2 (ViewHolder holder, long currentTime, long lastTime) {
        if(currentTime - lastTime <= 0) {
            holder.tv_message_time.setVisibility(View.GONE);
        }
        else {
            if (DateUtils.isCloseEnough(currentTime,lastTime)) {
                holder.tv_message_time.setVisibility(View.GONE);
            } else {
                holder.tv_message_time.setText(DateUtils.getNewChatTime(currentTime));
                holder.tv_message_time.setVisibility(View.VISIBLE);
            }
        }
    }

    class ViewHolder {


        TextView tv_message_time;
        LinearLayout ll_watch_success;
        TextView tv_push_type;
        TextView tv_title;
        TextView tv_watch_time;
        LinearLayout ll_push_fail;
        TextView tv_push_type_fail;
        TextView tv_push_state;
        public ViewHolder (View view) {
            tv_message_time = view.findViewById(R.id.tv_message_time);
            ll_watch_success = view.findViewById(R.id.ll_watch_success);
            tv_push_type = view.findViewById(R.id.tv_push_type);
            tv_title = view.findViewById(R.id.tv_title);
            tv_watch_time = view.findViewById(R.id.tv_watch_time);
            ll_push_fail = view.findViewById(R.id.ll_push_fail);
            tv_push_type_fail = view.findViewById(R.id.tv_push_type_fail);
            tv_push_state = view.findViewById(R.id.tv_push_state);

        }


    }
}

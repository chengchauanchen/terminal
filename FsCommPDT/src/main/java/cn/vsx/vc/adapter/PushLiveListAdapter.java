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

import butterknife.Bind;
import butterknife.ButterKnife;
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
            if(messageBody.getInteger(JsonParam.REMARK) == Remark.ACTIVE_VIDEO_LIVE) {//自己主动上报图像
                holder.tv_push_type.setText("个人上报图像");
                String title = messageBody.getString(JsonParam.TITLE);
                if(TextUtils.isEmpty(title)) {
                    holder.tv_title.setText("个人上报图像");
                }
                else {
                    holder.tv_title.setText(title);
                }
            }
            else if(messageBody.getInteger(JsonParam.REMARK) == Remark.ASK_VIDEO_LIVE) {//别人邀请我上报图像
                holder.tv_push_type.setText(messageBody.getString(JsonParam.QUERY_MEMBER_NAME)+"请求上报图像");
                String title = messageBody.getString(JsonParam.TITLE);
                if(TextUtils.isEmpty(title)) {
                    holder.tv_title.setText(messageBody.getString(JsonParam.QUERY_MEMBER_NAME)+"请求上报图像");
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
//        else  {
//            holder.ll_watch_success.setVisibility(View.GONE);
//            holder.ll_push_fail.setVisibility(View.VISIBLE);
//            if(messageBody.getInteger(JsonParam.REMARK) == Remark.ACTIVE_VIDEO_LIVE) {
//                holder.tv_push_type_fail.setText("自己上报图像");
//                Log.i("sjl_:看看被遥毙走的哪里","1111111111111111");
//                if(terminalMessage.resultCode == SignalServerErrorCode.MEMBER_REFUSE.getErrorCode()) {
//                    holder.tv_push_state.setText("对方已拒绝");
//                }
//                else {
//                    holder.tv_push_state.setText("对方无应答");
//                }
//
//            }
//            else if(messageBody.getInteger(JsonParam.REMARK) == Remark.REQUIRE_VIDEO_LIVE) {
//                Log.i("sjl_:看看被遥毙走的哪里","222222222222222222");
//                holder.tv_push_type_fail.setText(messageBody.getString(JsonParam.ASK_MEMBER_NAME)+"请求您上报图像");
//                if(terminalMessage.resultCode == SignalServerErrorCode.MEMBER_REFUSE.getErrorCode()) {
//                    holder.tv_push_state.setText("已拒绝");
//                }
//                else {
//                    holder.tv_push_state.setText("对方无应答");
//                }
//            }
//        }

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
//                holder.tv_message_time.setText(DateUtils.getTimestampString(new Date(messageTime)));
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
//                holder.tv_message_time.setText(DateUtils.getTimestampString(new Date(currentTime)));
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
//                String timeStr = DateUtils.getTimestampString(new Date(currentTime));
//                holder.tv_message_time.setText(timeStr);
                holder.tv_message_time.setText(DateUtils.getNewChatTime(currentTime));
                holder.tv_message_time.setVisibility(View.VISIBLE);
            }
        }
    }

    class ViewHolder {
        public ViewHolder (View view) {
            ButterKnife.bind(this, view);
        }

        @Bind(R.id.tv_message_time)
        TextView tv_message_time;

        @Bind(R.id.ll_watch_success)
        LinearLayout ll_watch_success;
        @Bind(R.id.tv_push_type)
        TextView tv_push_type;
        @Bind(R.id.tv_title)
        TextView tv_title;
        @Bind(R.id.tv_watch_time)
        TextView tv_watch_time;


        @Bind(R.id.ll_push_fail)
        LinearLayout ll_push_fail;
        @Bind(R.id.tv_push_type_fail)
        TextView tv_push_type_fail;
        @Bind(R.id.tv_push_state)
        TextView tv_push_state;
    }
}

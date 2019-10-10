package com.vsxin.terminalpad.mvp.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.ixiaoma.xiaomabus.architecture.mvp.refresh.adapter.BaseRecycleViewAdapter;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.utils.BitmapUtil;
import com.vsxin.terminalpad.utils.StringUtil;

import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;

public class GroupVideoLiveListAdapter extends BaseRecycleViewAdapter<TerminalMessage, GroupVideoLiveListAdapter.ViewHolder> {

    //是否是组内正在上报的列表
    private boolean isGroupVideoLiving;
    private OnItemClickListerner onItemClickListerner;

    public GroupVideoLiveListAdapter(Context mContext, boolean isGroupVideoLiving) {
        super(mContext);
        this.isGroupVideoLiving = isGroupVideoLiving;
    }

    @NonNull
    @Override
    public GroupVideoLiveListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_group_video_live, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TerminalMessage item = getDatas().get(position);
        if (item != null) {
            item.messageUrl = "";
            //图标
            holder.iv_user_photo.setImageResource(BitmapUtil.getPadDeviceImageResourceByType(
                    TerminalMemberType.valueOf(
                            TextUtils.isEmpty(item.terminalMemberType) ? TerminalMemberType.TERMINAL_PHONE.toString() : item.terminalMemberType)
                            .getCode()
            ));
            JSONObject messageBody = item.messageBody;
            if (messageBody != null && !TextUtils.isEmpty(messageBody.toJSONString())) {
                //姓名
                if (messageBody.containsKey(JsonParam.BACKUP)) {
                    String backUp = messageBody.getString(JsonParam.BACKUP);
                    if (!TextUtils.isEmpty(backUp) && backUp.contains("_")) {
                        String[] split = backUp.split("_");
                        if (split.length > 1) {
                            holder.tv_user_name.setText(split[1]);
                        } else {
                            holder.tv_user_name.setText("");
                        }
                    } else {
                        holder.tv_user_name.setText("");
                    }
                }
                //警号
                if (messageBody.containsKey(JsonParam.LIVERNO)) {
                    holder.tv_user_number.setText(String.valueOf(messageBody.getIntValue(JsonParam.LIVERNO)));
                } else {
                    holder.tv_user_number.setText("");
                }
            }
            //时间
            holder.tv_time.setVisibility(!isGroupVideoLiving ? View.VISIBLE : View.GONE);
            holder.tv_time.setText(StringUtil.stringToDate(item.sendTime));

            //如果是历史上报,则不显示分析
            holder.iv_forward.setVisibility(isGroupVideoLiving ? View.VISIBLE : View.GONE);
            //点击：观看
            holder.iv_watch.setOnClickListener(v -> {
                if (onItemClickListerner != null) {
                    onItemClickListerner.goToWatch(item);
                }
            });
            //点击：转发
            holder.iv_forward.setOnClickListener(v -> {
                if (onItemClickListerner != null) {
                    onItemClickListerner.goToForward(item);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return getDatas().size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView iv_user_photo;
        ImageView iv_watch;
        ImageView iv_forward;
        TextView tv_user_name;
        TextView tv_user_number;
        TextView tv_time;

        ViewHolder(View itemView) {
            super(itemView);
            iv_user_photo = itemView.findViewById(R.id.iv_user_photo);
            iv_watch = itemView.findViewById(R.id.iv_watch);
            iv_forward = itemView.findViewById(R.id.iv_forward);
            tv_user_name = itemView.findViewById(R.id.tv_user_name);
            tv_user_number = itemView.findViewById(R.id.tv_user_number);
            tv_time = itemView.findViewById(R.id.tv_time);
        }
    }

    public void setOnItemClickListerner(OnItemClickListerner onItemClickListerner) {
        this.onItemClickListerner = onItemClickListerner;
    }

    public interface OnItemClickListerner {
        void goToWatch(TerminalMessage item);

        void goToForward(TerminalMessage item);
    }
}

package com.vsxin.terminalpad.mvp.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ixiaoma.xiaomabus.architecture.mvp.refresh.adapter.BaseRecycleViewAdapter;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.entity.NoticeBean;
import com.vsxin.terminalpad.mvp.ui.adapter.NoticeAdapter.CallViewHolder;
import com.vsxin.terminalpad.mvp.ui.adapter.NoticeAdapter.LiveViewHolder;

import cn.vsx.hamster.terminalsdk.model.TerminalMessage;

/**
 * @author qzw
 * <p>
 * app模块-消息模块
 */
public class MessageAdapter extends BaseRecycleViewAdapter<TerminalMessage,MessageAdapter.ViewHolder> {


    public MessageAdapter(Context mContext) {
        super(mContext);
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_message_item, parent, false);
        return new ViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        TerminalMessage terminalMessage = getDatas().get(position);

        holder.tv_name.setText(terminalMessage.messageFromName);
        holder.tv_message_time.setText(terminalMessage.sendTime+"");
        if(terminalMessage.messageBody!=null){
            holder.tv_message_content.setText(terminalMessage.messageBody.getString("content"));
        }
    }

    @Override
    public int getItemCount() {
        return getDatas().size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView tv_name;
        private final TextView tv_message_time;
        private final TextView tv_message_content;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_message_time = itemView.findViewById(R.id.tv_message_time);
            tv_message_content = itemView.findViewById(R.id.tv_message_content);
        }
    }
}

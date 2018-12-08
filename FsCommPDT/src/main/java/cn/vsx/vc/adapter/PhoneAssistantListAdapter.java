package cn.vsx.vc.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.terminalsdk.model.CallRecord;
import cn.vsx.vc.R;
import cn.vsx.vc.view.RoundProgressBar;

public class PhoneAssistantListAdapter extends BaseAdapter {
    private List<CallRecord> list;
    private Activity activity;
    public PhoneAssistantListAdapter(List<CallRecord> list , Activity activity){
        this.list=list;
        this.activity=activity;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(activity, R.layout.item_phone_assistant, null);
            holder.memberName  =  convertView.findViewById(R.id.member_name);
            holder.callRecords = convertView.findViewById(R.id.call_records);
            holder.phone = convertView.findViewById(R.id.phone);
            holder.playRecord =convertView.findViewById(R.id.play_record);
            holder.progressBar =convertView.findViewById(R.id.progress_bar);
            holder.time=convertView.findViewById(R.id.time);
            holder.status=convertView.findViewById(R.id.status);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();

        }

        holder.memberName.setText(list.get(position).getMemberName());
        holder.callRecords.setText(list.get(position).getCallRecords());
        holder.phone.setText(list.get(position).getPhone());
        holder.time.setText(list.get(position).getTime());
        if(list.get(position).getCallRecords().equals("1")||list.get(position).getCallRecords().equals("2")){
            holder.playRecord.setVisibility(View.GONE);
            holder.progressBar.setVisibility(View.GONE);
            holder.status.setVisibility(View.GONE);
            holder.callRecords.setText("未接通");
        }else {
            holder.playRecord.setVisibility(View.VISIBLE);
            holder.status.setVisibility(View.VISIBLE);
            holder.progressBar.setVisibility(View.GONE);
//            holder.playRecord.setIndeterminateDrawable(activity.getResources().getDrawable(R.drawable.progressbar_white));
            holder.status.setImageResource(R.drawable.undownload);
        }
        CallRecord callRecord= (CallRecord) getItem(position);
        if(callRecord.isDownLoad()){
            holder.playRecord.setProgress(holder.progressBar.getMax());
            if(callRecord.isPlaying){
                holder.status.setImageResource(R.drawable.downloading);
                holder.progressBar.setVisibility(View.VISIBLE);
            }else {
                holder.status.setImageResource(R.drawable.downloaded);
            }
        }else {
            holder.playRecord.setProgress(0);
            holder.status.setImageResource(R.drawable.undownload);
            holder.progressBar.setVisibility(View.GONE);
        }



        return convertView;
    }

    static class ViewHolder {
        TextView memberName;
        TextView callRecords;
        TextView phone;
        RoundProgressBar playRecord;
        ProgressBar progressBar;
        TextView time;
        ImageView status;
    }


}

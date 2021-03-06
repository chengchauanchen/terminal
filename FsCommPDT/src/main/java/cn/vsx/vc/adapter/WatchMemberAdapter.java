package cn.vsx.vc.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;



import cn.vsx.hamster.terminalsdk.model.VideoMember;
import cn.vsx.vc.R;

/**
 * Created by zckj on 2017/6/14.
 */

public class WatchMemberAdapter extends BaseAdapter {

    private Context context;
    private List<VideoMember> list;

    public WatchMemberAdapter(Context context, List<VideoMember> list) {
        this.context = context;
        this.list = list;
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
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.watch_live_member_listview_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        VideoMember watchMember = list.get(position);
        viewHolder.tvWatchItemName.setText(watchMember.name);
        if(position == 0){
            viewHolder.tvWatchItemLiving.setVisibility(View.VISIBLE);
        }else {
            viewHolder.tvWatchItemLiving.setVisibility(View.VISIBLE);
        }
        viewHolder.tvWatchItemTime.setText(watchMember.getEnterTime());
        return convertView;
    }

    static class ViewHolder {

        ImageView ivWatchItemPhoto;

        TextView tvWatchItemName;

        TextView tvWatchItemLiving;

        TextView tvWatchItemTime;

        ViewHolder(View view) {
            ivWatchItemPhoto = view.findViewById(R.id.iv_watch_item_photo);
            tvWatchItemName = view.findViewById(R.id.tv_watch_item_name);
            tvWatchItemLiving = view.findViewById(R.id.tv_watch_item_living);
            tvWatchItemTime = view.findViewById(R.id.tv_watch_item_time);
        }
    }

}

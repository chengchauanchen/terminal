package cn.vsx.vc.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;



import cn.vsx.hamster.terminalsdk.model.VideoMember;
import cn.vsx.vc.R;

/**
 * Created by zckj on 2017/6/13.
 */

public class MemberEnterAdapter extends BaseAdapter {
    private Context context;
    private List<VideoMember> list;

    public MemberEnterAdapter(Context context, List<VideoMember> list) {
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
            convertView = View.inflate(context, R.layout.live_activity_listivew_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        VideoMember watchMember = list.get(position);
        String name = watchMember.getName();
        if (name.length() > 4){
            name = name.substring(0, 4)+"...";
        }
        if (watchMember.joinOrExit) {
            viewHolder.tvLiveMemberEnter.setText(String.format(context.getString(R.string.text_watch_live_enter_name),name));
        }else {
            viewHolder.tvLiveMemberEnter.setText(String.format(context.getString(R.string.text_watch_live_leave_name),name));
        }
        if(position % 4 == 0){
            viewHolder.llLiveMemberEnterBg.setBackground(context.getResources().getDrawable(R.drawable.live_member_enter_blue_shape));
        }else if(position % 4 == 1){
            viewHolder.llLiveMemberEnterBg.setBackground(context.getResources().getDrawable(R.drawable.live_member_enter_yellow_shape));
        }else if(position % 4 == 2){
            viewHolder.llLiveMemberEnterBg.setBackground(context.getResources().getDrawable(R.drawable.live_member_enter_green_shape));
        }else if(position % 4 == 3){
            viewHolder.llLiveMemberEnterBg.setBackground(context.getResources().getDrawable(R.drawable.live_member_enter_orange_shape));
        }

        return convertView;
    }

    static class ViewHolder {

        TextView tvLiveMemberEnter;

        LinearLayout llLiveMemberEnterBg;

        ViewHolder(View view) {
            tvLiveMemberEnter = view.findViewById(R.id.tv_live_member_enter);
            llLiveMemberEnterBg = view.findViewById(R.id.ll_live_member_enter_bg);
        }
    }

}

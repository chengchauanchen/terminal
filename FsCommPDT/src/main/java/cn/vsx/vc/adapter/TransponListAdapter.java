package cn.vsx.vc.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.vc.R;
import cn.vsx.vc.model.ChatMember;
import cn.vsx.vc.utils.BitmapUtil;

/**
 * Created by gt358 on 2017/9/15.
 */

public class TransponListAdapter extends BaseAdapter {

    private List<ChatMember> chatLists = new ArrayList<>();
    private Context context;

    public TransponListAdapter(List<ChatMember> chatLists, Context context) {
        this.chatLists = chatLists;
        this.context = context;
    }

    @Override
    public int getCount() {
        return chatLists.size();
    }

    @Override
    public Object getItem(int position) {
        return chatLists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null) {
            convertView = View.inflate(context, R.layout.item_transpon, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        if(chatLists.get(position).isGroup()){
            holder.iv_user_logo.setImageResource(R.drawable.group_photo);
        }else{
            holder.iv_user_logo.setImageResource(BitmapUtil.getUserPhoto());
        }
        holder.tv_user_name.setText(chatLists.get(position).getName());

        return convertView;
    }

    class ViewHolder {
        ImageView iv_user_logo;
        TextView tv_user_name;
        ViewHolder (View view) {
            iv_user_logo = view.findViewById(R.id.iv_user_logo);
            tv_user_name = view.findViewById(R.id.tv_user_name);
        }

    }
}

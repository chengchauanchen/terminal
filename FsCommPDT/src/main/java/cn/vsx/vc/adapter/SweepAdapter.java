package cn.vsx.vc.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.vsx.vc.R;

/**
 * Created by Administrator on 2017/3/16 0016.
 */

public class SweepAdapter extends BaseAdapter {
    private Context context;
    private ViewHolder holder;
    private List<String> list;
    private int itemPosition;

    public SweepAdapter(Context context, List<String> list) {
        this.context = context;
        this.list= list;
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_sweep, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.groupName.setText(list.get(position));
        holder.groupDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list.remove(position);
                notifyDataSetChanged();
            }
        });
        itemPosition = position;
        return convertView;
    }


    static class ViewHolder {
        @Bind(R.id.group_name)
        TextView groupName;
        @Bind(R.id.group_del)
        ImageView groupDel;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}

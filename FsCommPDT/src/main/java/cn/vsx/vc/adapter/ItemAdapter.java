package cn.vsx.vc.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.vc.R;

public class ItemAdapter extends BaseAdapter {
    public List<DataHolder> list;
    LayoutInflater inflater;

    public ItemAdapter(Context context, List<DataHolder> list) {
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public DataHolder getItem(int i) {
        if (i == getCount() || list == null) {
            return null;
        }
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.tem_layout, null);
            holder.icon = (ImageView) convertView.findViewById(R.id.imageview);
            holder.title = (TextView) convertView.findViewById(R.id.textview);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.icon.setImageResource(getItem(position).imgId);
        holder.title.setText(getItem(position).title);
        return convertView;
    }

    public class ViewHolder {
        public ImageView icon;
        public TextView title;
    }

    public static class DataHolder {
        public String title;
        public int imgId;

        //数据实体
        public DataHolder(String title, int imgId) {
            this.title = title;
            this.imgId = imgId;
        }
    }

    public static List<ItemAdapter.DataHolder> iniDatas() {
        List<ItemAdapter.DataHolder> list = new ArrayList<ItemAdapter.DataHolder>();
        ItemAdapter.DataHolder dataHolder1 = new ItemAdapter.DataHolder("录音电话", R.drawable.tel);
        ItemAdapter.DataHolder dataHolder2 = new ItemAdapter.DataHolder("普通电话", R.drawable.tel);
        list.add(dataHolder1);
        list.add(dataHolder2);
        return list;
    }
}

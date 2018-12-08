package cn.vsx.vc.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.vsx.vc.R;
import cn.vsx.vc.view.CallStatePopuwindow;
//import cn.zectec.ptt.activity.IndividualNewsActivity;
//import cn.zectec.ptt.view.CallStatePopuwindow;

/**
 * Created by Administrator on 2017/3/20 0020.
 */

public class PersonSearchAdapter extends BaseAdapter {
    private Activity context;
    private String[] strings;
    private ViewHolder holder;

    public PersonSearchAdapter(Activity context, String[] strings) {
        this.context = context;
        this.strings = strings;
    }

    @Override
    public int getCount() {
        return strings.length;
    }

    @Override
    public Object getItem(int position) {
        return strings[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_sreach_contacts_listview, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.catagory.setVisibility(View.GONE);
        holder.userName.setText(strings[position]);
        holder.messageTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                context.startActivity(new Intent(context, IndividualNewsActivity.class));
            }
        });
        holder.callTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CallStatePopuwindow popuwindow = new CallStatePopuwindow(context);
                popuwindow.showPersonCall(v, (Activity) context);
            }
        });
        holder.addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return convertView;
    }

    static class ViewHolder {
        @Bind(R.id.catagory)
        RelativeLayout catagory;
        @Bind(R.id.user_logo)
        ImageView userLogo;
        @Bind(R.id.user_name)
        TextView userName;
        @Bind(R.id.message_to)
        ImageView messageTo;
        @Bind(R.id.call_to)
        ImageView callTo;
        @Bind(R.id.add_friend)
        ImageView addFriend;
        @Bind(R.id.item_background)
        RelativeLayout itemBackground;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}

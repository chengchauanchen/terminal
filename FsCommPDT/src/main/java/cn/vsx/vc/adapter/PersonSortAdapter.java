package cn.vsx.vc.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;


import java.util.List;

import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.vc.R;
//import cn.zectec.ptt.activity.IndividualNewsActivity;
import cn.vsx.vc.activity.IndividualNewsActivity;
import cn.vsx.vc.view.CallStatePopuwindow;
//import cn.zectec.ptt.view.CallStatePopuwindow;

public class PersonSortAdapter extends BaseAdapter implements SectionIndexer {
    private List<Member> personContacts;
    private Context mContext;

    public PersonSortAdapter(Context mContext, List<Member> list) {
        this.mContext = mContext;
        this.personContacts = list;
    }

    /**
     * 当ListView数据发生变化时,调用此方法来更新ListView
     *
     * @param list
     */
    public void updateListView(List<Member> list) {
        this.personContacts = list;
        notifyDataSetChanged();
    }

    public int getCount() {
        return this.personContacts.size();
    }

    public Object getItem(int position) {
        return personContacts.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View view, ViewGroup arg2) {
        ViewHolder viewHolder = null;
        final Member mContent = personContacts.get(position);
        if (view == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.fragment_person_item, null);
            viewHolder.userName = (TextView) view.findViewById(R.id.user_name);
            viewHolder.userId = (TextView) view.findViewById(R.id.user_id);
            viewHolder.userLogo = (ImageView) view.findViewById(R.id.user_logo);
            viewHolder.messageTo = (LinearLayout) view.findViewById(R.id.message_to);
            viewHolder.callTo = (LinearLayout) view.findViewById(R.id.call_to);
            view.setTag(viewHolder);
            viewHolder.tvLetter = (TextView) view.findViewById(R.id.tv_catagory);
            viewHolder.line = (RelativeLayout) view.findViewById(R.id.lay_line);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        int section = getSectionForPosition(position);

        if (position == getPositionForSection(section)) {
            viewHolder.tvLetter.setVisibility(View.VISIBLE);
            viewHolder.tvLetter.setText(mContent.pinyin.charAt(0) + "");
            viewHolder.line.setVisibility(View.GONE);
        } else {
            viewHolder.tvLetter.setVisibility(View.GONE);
            viewHolder.line.setVisibility(View.VISIBLE);
        }

        viewHolder.userName.setText(this.personContacts.get(position).getName());

        //跳转到消息界面
        viewHolder.messageTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, IndividualNewsActivity.class);
                mContext.startActivity(intent);
            }
        });
        //跳转到个呼
        viewHolder.callTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CallStatePopuwindow popuwindow = new CallStatePopuwindow(mContext);
                popuwindow.showPersonCall(v, (Activity) mContext);
            }
        });
        return view;

    }


    final static class ViewHolder {
        TextView tvLetter;
        TextView userName;
        TextView userId;
        ImageView userLogo;
        LinearLayout messageTo;
        LinearLayout callTo;
        RelativeLayout line;
    }

    public int getSectionForPosition(int position) {
        return personContacts.get(position).pinyin.charAt(0);
    }

    public int getPositionForSection(int section) {
        for (int i = 0; i < getCount(); i++) {
            String sortStr = personContacts.get(i).pinyin;
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public Object[] getSections() {
        return null;
    }
}
package cn.vsx.vc.adapter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.vsx.vc.R;
import cn.vsx.vc.model.ShouTaiBean;
import cn.vsx.vc.model.ShouTaiBean.BuMenBean;
import cn.vsx.vc.view.CustomExpandableListView;

/**
 * Created by jamie on 2017/10/28.
 */

public class HandPlatformSimpleExpandableListViewAdapter extends BaseExpandableListAdapter {
    private List<ShouTaiBean> memberList;
    private Activity activity;

    public HandPlatformSimpleExpandableListViewAdapter(List<ShouTaiBean> memberList, Activity activity) {
        this.memberList = memberList;
        this.activity = activity;
    }
    @Override
    public int getGroupCount() {
        if (memberList == null){
            return 0;
        }
        return memberList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return memberList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return memberList.get(groupPosition).bumenList;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ViewHolderShiJu viewHolderShiJu = null;
        if (convertView == null){
            convertView = View.inflate(activity,R.layout.shoutai_item_shiju,null);
            viewHolderShiJu = new ViewHolderShiJu(convertView);
            convertView.setTag(viewHolderShiJu);
        }else {
            viewHolderShiJu = (ViewHolderShiJu) convertView.getTag();
        }
        viewHolderShiJu.shoutai_shuju_name.setText(memberList.get(groupPosition).shijuName);
        int memberNumber = 0;
        for (BuMenBean b : memberList.get(groupPosition).bumenList){
            if (b.memberList != null)
                memberNumber += b.memberList.size();
        }
        viewHolderShiJu.shoutai_group_size.setText("("+memberNumber+")");
        if (isExpanded) {
            viewHolderShiJu.is_shoutai_shiju.setBackgroundResource(R.drawable.new_folder_open);

        } else {
            viewHolderShiJu.is_shoutai_shiju.setBackgroundResource(R.drawable.new_folder_close);
        }
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        return getGenericExpandableListView(groupPosition, childPosition);
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
    public static class ViewHolderShiJu {
        @Bind(R.id.shoutai_shuju_name)
        TextView shoutai_shuju_name;
        @Bind(R.id.shoutai_group_size)
        TextView shoutai_group_size;
        @Bind(R.id.is_shoutai_shiju)
        ImageView is_shoutai_shiju;

        public ViewHolderShiJu(View rootView) {
            ButterKnife.bind(this, rootView);
        }
    }
    public static class ViewHolderBuMen {
        @Bind(R.id.shoutai_bumen_name)
        TextView shoutai_bumen_name;

        public ViewHolderBuMen(View rootView) {
            ButterKnife.bind(this, rootView);
        }
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public ExpandableListView getGenericExpandableListView(int groupPosition, int childPosition){
        CustomExpandableListView view = new CustomExpandableListView(activity);
        List<BuMenBean> bumens = memberList.get(groupPosition).bumenList;
        HandPlatformAdapter adapter = new HandPlatformAdapter(bumens, activity);
        view.setAdapter(adapter);

        view.setPadding(20,0,0,0);
        return view;
    }

}

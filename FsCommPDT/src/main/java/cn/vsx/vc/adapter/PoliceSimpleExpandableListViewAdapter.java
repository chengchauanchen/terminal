package cn.vsx.vc.adapter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.vc.R;
import cn.vsx.vc.model.ShouTaiBean;
import cn.vsx.vc.model.ShouTaiBean.BuMenBean;
import cn.vsx.vc.view.CustomExpandableListView;

/**
 * Created by jamie on 2017/10/28.
 */

public class PoliceSimpleExpandableListViewAdapter extends BaseExpandableListAdapter {
    private Logger logger = Logger.getLogger(getClass());
    private Activity activity;
    private List<ShouTaiBean> shouTaiBeenList;
    private List<BuMenBean> bumenList;

    public PoliceSimpleExpandableListViewAdapter(List<ShouTaiBean> shouTaiBeenList, Activity activity) {
        this.shouTaiBeenList = shouTaiBeenList;
        this.activity = activity;
    }
    @Override
    public int getGroupCount() {
        if (shouTaiBeenList == null){
            return 0;
        }
        return shouTaiBeenList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return shouTaiBeenList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return shouTaiBeenList.get(groupPosition).bumenList;
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
            convertView = View.inflate(activity,R.layout.jingwutong_item_shiju,null);
            viewHolderShiJu = new ViewHolderShiJu(convertView);
            convertView.setTag(viewHolderShiJu);
        }else {
            viewHolderShiJu = (ViewHolderShiJu) convertView.getTag();
        }
        viewHolderShiJu.shijuName.setText(shouTaiBeenList.get(groupPosition).shijuName);

        bumenList = shouTaiBeenList.get(groupPosition).bumenList;
        int memberNumber = 0;
        Iterator<BuMenBean> iterator = bumenList.iterator();
        while (iterator.hasNext()){
            List<Member> memberList = iterator.next().memberList;
            Iterator<Member> iterator1 = memberList.iterator();
            while (iterator1.hasNext()){
                Member member = iterator1.next();
                if (member.getType() == TerminalMemberType.TERMINAL_TEST.getCode()){
                    iterator1.remove();
                    logger.error("------------移除的隐形人----------->"+member);
                }else {
                    memberNumber++;
                }
            }
        }
        viewHolderShiJu.shuju_group_size.setText("("+memberNumber+")");
        if (isExpanded) {
            viewHolderShiJu.is_jingwutong_shiju.setBackgroundResource(R.drawable.new_folder_open);
        } else {
            viewHolderShiJu.is_jingwutong_shiju.setBackgroundResource(R.drawable.new_folder_close);
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
        @Bind(R.id.shuju_name)
        TextView shijuName;
        @Bind(R.id.shuju_group_size)
        TextView shuju_group_size;
        @Bind(R.id.is_jingwutong_shiju)
        ImageView is_jingwutong_shiju;

        public ViewHolderShiJu(View rootView) {
            ButterKnife.bind(this, rootView);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public ExpandableListView getGenericExpandableListView(int groupPosition, int childPosition){
        AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        CustomExpandableListView view = new CustomExpandableListView(activity);

        // 加载适配器
        PoliceAffairsAdapter adapter = new PoliceAffairsAdapter(bumenList, activity);

        view.setAdapter(adapter);
        adapter.notifyDataSetChanged();


        view.setPadding(20,0,0,0);
        return view;
    }
}

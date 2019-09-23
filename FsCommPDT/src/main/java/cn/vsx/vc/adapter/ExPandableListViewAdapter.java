package cn.vsx.vc.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.vc.R;
import cn.vsx.vc.model.Car;
import cn.vsx.vc.model.HongHuBean;

/**
 * @author qzw
 * <p>
 * 东湖选择车 折叠列表
 */
public class ExPandableListViewAdapter extends BaseExpandableListAdapter {
    // 定义一个Context
    private Context context;
    // 定义一个LayoutInflater
    private LayoutInflater mInflater;
    // 定义一个List来保存列表数据
    private List<HongHuBean> datas = new ArrayList<>();

    // 定义一个构造方法
    public ExPandableListViewAdapter(Context context, List<HongHuBean> datas) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.datas.clear();
        this.datas.addAll(datas);
    }

    // 刷新数据
    public void notifyData(List<HongHuBean> datas) {
        this.datas.clear();
        this.datas.addAll(datas);
        this.notifyDataSetChanged();
    }

    public List<HongHuBean> getDatas() {
        return datas;
    }

    // 获取一级列表的个数
    @Override
    public int getGroupCount() {
        return datas.size();
    }

    // 获取二级列表的数量
    @Override
    public int getChildrenCount(int groupPosition) {
        if(datas.get(groupPosition).getCar()!=null){
            return datas.get(groupPosition).getCar().size();
        }else{
            return 0;
        }
    }

    // 获取一级列表的数据
    @Override
    public HongHuBean getGroup(int groupPosition) {
        return datas.get(groupPosition);
    }

    // 获取二级列表的内容
    @Override
    public Car getChild(int groupPosition, int childPosition) {
        return datas.get(groupPosition).getCar().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    // 获取一级列表的ID
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    //父级 view
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        FatherViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_donghu_father_layout, parent, false);
            viewHolder = new FatherViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (FatherViewHolder) convertView.getTag();
        }

        HongHuBean group = getGroup(groupPosition);

        //折叠状态
        viewHolder.iv_arrow.setImageResource(isExpanded ? R.drawable.ico_bindingcar_packup : R.drawable.ico_bindingcar_unfold);

        int whiteRid = context.getResources().getColor(R.color.white);
        int white04Rid = context.getResources().getColor(R.color.color_FAFAFA);
        viewHolder.ll_content.setBackgroundColor(isExpanded ? white04Rid : whiteRid);

        viewHolder.tv_content.setText(group.getDeptName());
        viewHolder.iv_folder.setImageResource(R.drawable.ico_bindingcar_folder);
        return convertView;
    }

    //子级 view
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_donghu_child_layout, parent, false);
            viewHolder = new ChildViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ChildViewHolder) convertView.getTag();
        }
        Car child = getChild(groupPosition, childPosition);
        viewHolder.tv_content.setText(child.getUniqueNo());
        viewHolder.iv_device.setImageResource(R.drawable.ico_bindingcar_car);
        viewHolder.iv_checkbox.setImageResource(child.isCheck() ? R.drawable.ico_bindingcar_selected : R.drawable.ico_bindingcar_unselect);
        return convertView;
    }

    //指定位置相应的组视图
    @Override
    public boolean hasStableIds() {
        return true;
    }

    //ExPandableListView二级列表点击没有反应 当选择子节点的时候，调用该方法(点击二级列表)
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    // 定义一个 父级列表的view类
    static class FatherViewHolder {
        LinearLayout ll_content;
        ImageView iv_folder;
        TextView tv_content;
        ImageView iv_arrow;

        public FatherViewHolder(View view) {
            ll_content = view.findViewById(R.id.ll_content);
            iv_folder = view.findViewById(R.id.iv_folder);
            tv_content = view.findViewById(R.id.tv_content);
            iv_arrow = view.findViewById(R.id.iv_arrow);
        }
    }

    // 保存二级列表的视图类
    static class ChildViewHolder {
        ImageView iv_device;
        TextView tv_content;
        //CheckBox checkbox;
        ImageView iv_checkbox;

        public ChildViewHolder(View view) {
            iv_device = view.findViewById(R.id.iv_device);
            tv_content = view.findViewById(R.id.tv_content);
            //checkbox = view.findViewById(R.id.checkbox);
            iv_checkbox = view.findViewById(R.id.iv_checkbox);
        }
    }
}

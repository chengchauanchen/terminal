package cn.vsx.vc.adapter;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.tools.Util;
import cn.vsx.vc.R;
import cn.vsx.vc.utils.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * 作者：xuxiaolong
 * 版本：1.0
 * 创建日期：2018/6/28
 * 描述：扫描组搜索adapter
 * 修订历史：
 */

public class ScanGroupSearchAdapter extends BaseAdapter{

    private List<Group> searchGroupList;
    private String keyWords;
    private LayoutInflater inflater;
    private Context context;
    private List<Integer>MemberIds = new ArrayList<>();
    private List<Integer> scanGroupList = MyTerminalFactory.getSDK().getConfigManager().getScanGroups();
    public ScanGroupSearchAdapter (Context context, List<Group> searchGroupList) {
        this.context = context;
        this.searchGroupList = searchGroupList;
        this.inflater = LayoutInflater.from(context);
    }

    public void setFilterKeyWords(String keyWords) {
        this.keyWords = keyWords;
    }

    @Override
    public int getCount(){
        return searchGroupList.size();
    }

    @Override
    public Object getItem(int position){
        return searchGroupList.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent){
        ViewHolder viewHolder;
        if(convertView == null){
            convertView = inflater.inflate(R.layout.item_group_sweep_add,parent,false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        //设置名字
        String name = searchGroupList.get(position).name;
        if (!Util.isEmpty(name) && !Util.isEmpty(keyWords) && name.toLowerCase().contains(keyWords.toLowerCase())) {

            int index = name.toLowerCase().indexOf(keyWords.toLowerCase());

            int len = keyWords.length();

            Spanned temp = Html.fromHtml(name.substring(0, index)
                    + "<u><font color=#eb403a>"
                    + name.substring(index, index + len) + "</font></u>"
                    + name.substring(index + len, name.length()));

            viewHolder.tvName.setText(temp);
        } else {
            viewHolder.tvName.setText(name);
        }
        //设置已经添加过的显示与隐藏
        if(scanGroupList.contains(searchGroupList.get(position).getId())){
            viewHolder.tvAddScanGroup.setVisibility(View.GONE);
            viewHolder.ivScanGroup.setVisibility(View.VISIBLE);
        }else{
            viewHolder.tvAddScanGroup.setVisibility(View.VISIBLE);
            viewHolder.ivScanGroup.setVisibility(View.GONE);
        }

        //设置扫描组点击事件
        viewHolder.tvAddScanGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MyTerminalFactory.getSDK().getConfigManager().getScanGroups().size()>=10){
                    ToastUtil.showToast(context,"扫描组不能超过10个");
                    return;
                }
                MemberIds.clear();
                MemberIds.add(searchGroupList.get(position).getNo());
                MyTerminalFactory.getSDK().getGroupScanManager().setScanGroupList(MemberIds,true);
            }
        });

        return convertView;
    }




    class ViewHolder {
        @Bind(R.id.tv_name)
        TextView tvName;
        @Bind(R.id.tv_add_scan_group)
        TextView tvAddScanGroup;
        @Bind(R.id.iv_scan_group)
        ImageView ivScanGroup;

        private ViewHolder(View rootView) {
            ButterKnife.bind(this, rootView);
        }
    }
}

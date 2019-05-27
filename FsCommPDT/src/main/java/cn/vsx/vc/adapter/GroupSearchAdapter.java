package cn.vsx.vc.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.Util;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.GroupCallNewsActivity;
import cn.vsx.vc.receiveHandle.ReceiverMonitorViewClickHandler;

/**
 * 通讯录组搜索adapter
 * Created by gt358 on 2017/10/21.
 */

public class GroupSearchAdapter extends BaseAdapter {

    private Context context;
    private List<Group> searchGroupList;
    private String keyWords;
    private OnItemBtnClickListener mItemBtnClickListener;
    private LayoutInflater inflater;

    public GroupSearchAdapter (Context context, List<Group> searchGroupList) {
        this.context = context;
        this.searchGroupList = searchGroupList;
        this.inflater = LayoutInflater.from(context);
    }

    public void setFilterKeyWords(String keyWords) {
        this.keyWords = keyWords;
    }

    @Override
    public int getCount() {
        return searchGroupList.size();
    }

    @Override
    public Object getItem(int position) {
        return searchGroupList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolderGroup viewHolderGroup;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_group_search, parent,false);
            viewHolderGroup = new ViewHolderGroup(convertView);
            convertView.setTag(viewHolderGroup);
        } else {
            viewHolderGroup = (ViewHolderGroup) convertView.getTag();
        }

        String name = searchGroupList.get(position).name;
        if (!Util.isEmpty(name) && !Util.isEmpty(keyWords) && name.toLowerCase().contains(keyWords.toLowerCase())) {

            int index = name.toLowerCase().indexOf(keyWords.toLowerCase());

            int len = keyWords.length();

            Spanned temp = Html.fromHtml(name.substring(0, index)
                    + "<u><font color=#eb403a>"
                    + name.substring(index, index + len) + "</font></u>"
                    + name.substring(index + len, name.length()));

            viewHolderGroup.groupChildName.setText(temp);
        } else {
            viewHolderGroup.groupChildName.setText(name);
        }
        if(checkIsMonitorGroup(searchGroupList.get(position).getNo())){
            viewHolderGroup.iv_monitor.setImageResource(R.drawable.monitor_open);
        }else {
            viewHolderGroup.iv_monitor.setImageResource(R.drawable.monitor_close_blue);
        }
        //如果是当前组
        viewHolderGroup.iv_monitor.setOnClickListener(v -> {
            TerminalFactory.getSDK().notifyReceiveHandler(ReceiverMonitorViewClickHandler.class,searchGroupList.get(position).getNo());
        });

        //会话按钮点击事件
        viewHolderGroup.toGroup.setOnClickListener(view -> {
            Intent intent = new Intent(context, GroupCallNewsActivity.class);
            intent.putExtra("isGroup", true);
            intent.putExtra("userId", searchGroupList.get(position).id);//组id
            intent.putExtra("userName", searchGroupList.get(position).name);
            intent.putExtra("speakingId",searchGroupList.get(position).id);
            intent.putExtra("speakingName",searchGroupList.get(position).name);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            if (mItemBtnClickListener!=null){
                mItemBtnClickListener.onItemBtnClick();
            }

        });

        if(position == searchGroupList.size()-1){
            viewHolderGroup.line.setVisibility(View.GONE);
        }else {
            viewHolderGroup.line.setVisibility(View.VISIBLE);
        }
        return convertView;
    }


    public void setOnItemBtnClick(OnItemBtnClickListener listener){
        this.mItemBtnClickListener=listener;
    }

    public interface OnItemBtnClickListener{
        void onItemBtnClick();
    }

    public static class ViewHolderGroup {

        LinearLayout ll_group;

        TextView groupChildName;

        TextView isCurrentGroupTv;

        ImageView iv_monitor ;

        ImageView toGroup;

        View line;

        public ViewHolderGroup(View rootView) {
            ll_group = rootView.findViewById(R.id.ll_group);
            groupChildName = rootView.findViewById(R.id.group_child_name);
            isCurrentGroupTv = rootView.findViewById(R.id.is_current_group_tv);
            iv_monitor = rootView.findViewById(R.id.iv_monitor);
            toGroup = rootView.findViewById(R.id.to_group);
            line = rootView.findViewById(R.id.lay_line);
        }
    }

    private boolean checkIsMonitorGroup(int groupNo){
        if(TerminalFactory.getSDK().getConfigManager().getMonitorGroupNo().contains(groupNo)){
            return true;
        }
        if(TerminalFactory.getSDK().getConfigManager().getTempMonitorGroupNos().contains(groupNo)){
            return true;
        }
        if(TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID,0) == groupNo){
            return true;
        }
        if(TerminalFactory.getSDK().getParam(Params.MAIN_GROUP_ID,0) == groupNo){
            return true;
        }
        return false;
    }

}

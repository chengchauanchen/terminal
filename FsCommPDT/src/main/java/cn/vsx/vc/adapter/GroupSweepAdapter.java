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

import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.utils.DataUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * Created by weishixin on 2018/1/15.
 */

public class GroupSweepAdapter extends BaseAdapter {


    private Context context;
    private List<Integer> list;
    private List<Integer> memberIds = new ArrayList<>();
    public GroupSweepAdapter(Context context,List<Integer> list){
        this.context=context;
        this.list=list;
    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, final ViewGroup viewGroup) {
        ViewHoder viewHoder=null;
        if(view ==null){
            viewHoder = new ViewHoder();
            view = LayoutInflater.from(context).inflate(R.layout.item_group_sweep, null);
            viewHoder.tv_sweep_group =  view.findViewById(R.id.tv_sweep_group);
            viewHoder.tv_new_sweep = view.findViewById(R.id.tv_new_sweep);
            viewHoder.iv_delete = view.findViewById(R.id.iv_delete);
            view.setTag(viewHoder);
        }else {
            viewHoder=(ViewHoder) view.getTag();
        }
            viewHoder.tv_sweep_group.setText(DataUtil.getGroupName(list.get(i)));
            viewHoder.iv_delete.setOnClickListener(view1 -> {
                memberIds.clear();
                memberIds.add(list.get(i));
                MyTerminalFactory.getSDK().getGroupScanManager().setScanGroupList(memberIds,false);
            });
            if(list.get(i)== MyTerminalFactory.getSDK().getParam(Params.MAIN_GROUP_ID, 0)){
                viewHoder.tv_new_sweep.setVisibility(View.VISIBLE);
                viewHoder.iv_delete.setVisibility(View.GONE);
            }else {
                viewHoder.tv_new_sweep.setVisibility(View.GONE);
                viewHoder.iv_delete.setVisibility(View.VISIBLE);
            }
        return view;
    }
    class ViewHoder {
        TextView tv_sweep_group;
        ImageView iv_delete;
        TextView tv_new_sweep;
    }
}

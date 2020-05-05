package cn.vsx.vc.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.common.GroupType;
import cn.vsx.hamster.common.ResponseGroupType;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/7/12
 * 描述：
 * 修订历史：
 */
public class MonitorGroupListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int COMMON = 0;
    private static final int SPECIL = 1;
    private static final int REMOVE = 2;
    private List<Group> data;
    private final LayoutInflater inflater;

    public MonitorGroupListAdapter(List<Group> data, Context context){
        this.data = data;
        inflater = LayoutInflater.from(context);
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        if(viewType == COMMON){
            View view = inflater.inflate(R.layout.common_monitor_list_item,parent,false);
            return new CommonViewHolder(view);
        }else if (viewType == SPECIL){
            View view = inflater.inflate(R.layout.specil_monitor_list_item,parent,false);
            return new SpecilViewHolder(view);
        }else {
            View view = inflater.inflate(R.layout.remove_list_item,parent,false);
            return new RemoveViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position){
        Group group = data.get(position);
        if(group.getNo() == TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID,0)){
            return SPECIL;
        }
//        if(GroupType.TEMPORARY.toString().equals(group.getGroupType())){
//            return SPECIL;
//        }
        if(ResponseGroupType.RESPONSE_TRUE.toString().equals(group.getResponseGroupType())){
            return SPECIL;
        }
        if (group.isRemove()){
            return REMOVE;
        }
        return COMMON;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position){
        int itemViewType = getItemViewType(position);
        Group group = data.get(position);
        if(itemViewType == COMMON){
            CommonViewHolder commonViewHolder = (CommonViewHolder) holder;
            commonViewHolder.mTvName.setText(group.getName());
            commonViewHolder.mIvDelete.setOnClickListener(v -> {
                List<Integer> monitorGroups = new ArrayList<>();
                monitorGroups.add(group.getNo());
                TerminalFactory.getSDK().getGroupManager().setMonitorGroup(monitorGroups,false);
            });
        }else if(itemViewType == SPECIL){
            SpecilViewHolder specilViewHolder = (SpecilViewHolder) holder;
            specilViewHolder.mTvName.setText(group.getName());
            if(group.getNo() == TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID,0)){
                specilViewHolder.mTvDescribe.setText(R.string.current_group_cannot_remove);
            }else if(GroupType.TEMPORARY.toString().equals(group.getGroupType())){
                specilViewHolder.mTvDescribe.setText(R.string.temp_group_cannot_remove);
            }else if(ResponseGroupType.RESPONSE_TRUE.toString().equals(group.getResponseGroupType())){
                specilViewHolder.mTvDescribe.setText(R.string.response_group_cannot_remove);
            }
        }else{
            RemoveViewHolder removeViewHolder = (RemoveViewHolder) holder;
            removeViewHolder.mTvName.setText(group.getName());
            removeViewHolder.mTvDescribe.setText(R.string.already_remove);
        }
    }

    @Override
    public int getItemCount(){
        return data.size();
    }

    class CommonViewHolder extends RecyclerView.ViewHolder{

        private TextView mTvName;
        private ImageView mIvDelete;
        private View mBottomDiver;

        public CommonViewHolder(View itemView){
            super(itemView);
            mTvName = (TextView) itemView.findViewById(R.id.tv_name);
            mIvDelete = (ImageView) itemView.findViewById(R.id.iv_delete);
            mBottomDiver = (View) itemView.findViewById(R.id.bottom_diver);
        }
    }


    class SpecilViewHolder extends RecyclerView.ViewHolder{

        private TextView mTvName;
        private TextView mTvDescribe;
        private View mBottomDiver;

        public SpecilViewHolder(View itemView){
            super(itemView);
            mTvName = (TextView) itemView.findViewById(R.id.tv_name);
            mTvDescribe = (TextView) itemView.findViewById(R.id.tv_describe);
            mBottomDiver = (View) itemView.findViewById(R.id.bottom_diver);
        }
    }

    private class RemoveViewHolder extends RecyclerView.ViewHolder {
        private TextView mTvName;
        private TextView mTvDescribe;
        private View mBottomDiver;

        public RemoveViewHolder(View itemView){
            super(itemView);
            mTvName = (TextView) itemView.findViewById(R.id.tv_name);
            mTvDescribe = (TextView) itemView.findViewById(R.id.tv_describe);
            mBottomDiver = (View) itemView.findViewById(R.id.bottom_diver);
        }
    }
}

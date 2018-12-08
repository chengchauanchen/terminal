package cn.vsx.vc.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.vc.R;
import cn.vsx.vc.model.GroupItemBean;
import cn.vsx.vc.utils.Constants;
import ptt.terminalsdk.context.MyTerminalFactory;

import static cn.vsx.vc.utils.Constants.TYPE_GROUP;


/**
 * Created by XX on 2018/4/11.
 */

public class GroupScanAddAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<GroupItemBean> mDatas;
    private ItemClickListener listener;
    List<Integer> memberIds=new ArrayList<>();
    List<Integer> scanGroups = MyTerminalFactory.getSDK().getConfigManager().getScanGroups();

    public GroupScanAddAdapter(Context context, List<GroupItemBean> datas) {
        this.mContext=context;
        this.mDatas=datas;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType== Constants.TYPE_FOLDER){
            View view= LayoutInflater.from(mContext).inflate(R.layout.item_department,parent,false);
            return new FolderViewHolder(view);
        }else {
            View view= LayoutInflater.from(mContext).inflate(R.layout.item_group_sweep_add,parent,false);
            return new GroupViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {

        if (getItemViewType(position)==Constants.TYPE_FOLDER){
            FolderViewHolder holder= (FolderViewHolder) viewHolder;
            holder.tvFolder.setText(mDatas.get(position).getName());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener!=null) {
                        listener.onItemClick(view, position, Constants.TYPE_FOLDER);
                    }
                }
            });
        }else {
            final GroupViewHolder holder= (GroupViewHolder) viewHolder;
            final Group group= (Group) mDatas.get(position).getBean();
            holder.tvName.setText(group.getName()+"");
            if (scanGroups.contains(group.getNo())){
                holder.tvAddScanGroup.setVisibility(View.INVISIBLE);
                holder.ivScanGroup.setVisibility(View.VISIBLE);
            }else {
                holder.tvAddScanGroup.setVisibility(View.VISIBLE);
                holder.ivScanGroup.setVisibility(View.INVISIBLE);
            }
            //添加扫描组点击事件
            holder.tvAddScanGroup.setOnClickListener(new View.OnClickListener() {

                public static final int MIN_CLICK_TIME=1000;
                private long lastTime=0;
                private long cTime=System.currentTimeMillis();

                @Override
                public void onClick(View view) {
                    if(cTime-lastTime>MIN_CLICK_TIME){
                        if(scanGroups.size()>=10){
                            Toast.makeText(mContext,"已超出扫描组最大添加数量",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        memberIds.clear();
                        memberIds.add(group.getNo());
                        MyTerminalFactory.getSDK().getGroupScanManager().setScanGroupList(memberIds,true);
                        lastTime=cTime;
                    }

                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    @Override
    public int getItemViewType(int position) {
        GroupItemBean bean=mDatas.get(position);
        if (bean.getType()==TYPE_GROUP){
            return Constants.TYPE_GROUP;
        }else {
            return Constants.TYPE_FOLDER;
        }
    }




    class FolderViewHolder extends RecyclerView.ViewHolder{
        @Bind(R.id.tv_department)
        TextView tvFolder;

        public FolderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class GroupViewHolder extends RecyclerView.ViewHolder{
        @Bind(R.id.tv_name)
        TextView  tvName;
        @Bind(R.id.tv_add_scan_group)
        TextView tvAddScanGroup;
        @Bind(R.id.iv_scan_group)
        ImageView ivScanGroup;

        public GroupViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }
    }

    public void setOnItemClickListener(ItemClickListener listener){
        this.listener=listener;
    }
    public interface ItemClickListener{
        void onItemClick(View view, int postion, int type);
    }

}

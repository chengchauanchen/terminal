package cn.vsx.vc.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.vc.R;
import cn.vsx.vc.model.CatalogBean;

/**
 * 选择设备
 */

public class ChooseDevicesAdapter extends RecyclerView.Adapter<ChooseDevicesAdapter.MyViewHolder> {
    private Context mContext;
    private List<Member> list;
    private ItemClickListener mItemClickListener;

    public ChooseDevicesAdapter(Context context, List<Member> list,ItemClickListener mItemClickListener) {
        this.mContext=context;
        this.list=list;
        this.mItemClickListener = mItemClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.item_choose_devices,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

//        holder.ivIcon.setImageResource();
//        holder.tvContext.setText("");
        holder.llAll.setOnClickListener(view -> {
            if (mItemClickListener!=null){
                mItemClickListener.onItemClick(view,position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface ItemClickListener{
        void onItemClick(View view, int position);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llAll;
        ImageView ivIcon;
        TextView tvContext;
        public MyViewHolder(View itemView) {
            super(itemView);
            llAll=  itemView.findViewById(R.id.ll_all);
            ivIcon=  itemView.findViewById(R.id.iv_icon);
            tvContext=  itemView.findViewById(R.id.tv_context);
        }
    }
}

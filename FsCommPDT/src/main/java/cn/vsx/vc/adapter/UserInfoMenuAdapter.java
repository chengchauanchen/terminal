package cn.vsx.vc.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.vsx.vc.R;
import cn.vsx.vc.model.Picture;

/**
 * Created by XX on 2018/3/23.
 */

public class UserInfoMenuAdapter  extends RecyclerView.Adapter<UserInfoMenuAdapter.MyViewHolder>{
    private List<Picture> mDatas;
    private Context mContext;
    private OnItemClickListener mItemClickListener;

    public UserInfoMenuAdapter(Context context,List<Picture> pictures) {
        mContext=context;
        this.mDatas=pictures;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.item_user_info_menu,null);
        MyViewHolder holder=new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.ivMenu.setImageResource(mDatas.get(position).getImageId());
        holder.tvMenu.setText(mDatas.get(position).getTitle());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mItemClickListener!=null){
                    mItemClickListener.onItemClick(position,mDatas.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.mItemClickListener=onItemClickListener;
    }

   public interface OnItemClickListener{
        void onItemClick(int postion,Picture picture);
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView ivMenu;
        TextView tvMenu;

        public MyViewHolder(View itemView) {
            super(itemView);
            ivMenu= (ImageView) itemView.findViewById(R.id.iv_menu);
            tvMenu= (TextView) itemView.findViewById(R.id.tv_menu);
        }
    }

}


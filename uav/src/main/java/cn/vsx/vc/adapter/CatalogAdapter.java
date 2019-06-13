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
import cn.vsx.vc.model.CatalogBean;

/**
 * Created by XX on 2018/4/11.
 */

public class CatalogAdapter extends RecyclerView.Adapter<CatalogAdapter.MyViewHolder> {
    private Context mContext;
    private List<CatalogBean> mCatalogs;
    private ItemClickListener mItemClickListener;

    public CatalogAdapter(Context context, List<CatalogBean> catalogs) {
        this.mContext=context;
        this.mCatalogs=catalogs;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.item_catalog,parent,false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        if (position==0){
            holder.iv.setVisibility(View.GONE);
        }else {
            holder.iv.setVisibility(View.VISIBLE);
        }

        holder.tvCatalog.setText(mCatalogs.get(position).getName());

        holder.itemView.setOnClickListener(view -> {
            if (mItemClickListener!=null){
                mItemClickListener.onItemClick(view,position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mCatalogs.size();
    }

    public void setOnItemClick(ItemClickListener listener){
        this.mItemClickListener=listener;
    }

    public interface ItemClickListener{
        void onItemClick(View view, int position);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView iv;
        TextView tvCatalog;
        public MyViewHolder(View itemView) {
            super(itemView);
            iv=  itemView.findViewById(R.id.iv);
            tvCatalog=  itemView.findViewById(R.id.tv_catalog);
        }
    }
}

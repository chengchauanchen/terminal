package cn.vsx.vc.adapter;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.vc.R;

/**
 * Created by Administrator on 2017/3/14 0014.
 * 群组adapter
 */

public class AppListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;

    //显示在recyclerview上的所有数据
    private List<PackageInfo> data = new ArrayList<>();

    private OnItemClickListener onItemClickListener;

    public AppListAdapter(Context context, List<PackageInfo> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_app_list, parent, false);
        return new AppListHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        AppListHolder aHolder = (AppListHolder) holder;
        PackageInfo bean = data.get(position);
        if(bean!=null){
            aHolder.ivImage.setImageDrawable(bean.applicationInfo.loadIcon(context.getPackageManager()));
            aHolder.tvImage.setText(bean.applicationInfo.loadLabel(context.getPackageManager()).toString());
            aHolder.itemView.setOnClickListener(view -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onClick(bean);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class AppListHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvImage;

        AppListHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            tvImage = itemView.findViewById(R.id.tv_image);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onClick(PackageInfo bean);
    }
}

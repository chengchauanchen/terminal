package cn.vsx.vc.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.vc.R;
import cn.vsx.vc.model.SetLivingStopTimeBean;

/**
 * Created by Administrator on 2017/3/14 0014.
 * 群组adapter
 */

public class SetLivingStopTimeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;

    //显示在recyclerview上的所有数据
    private List<SetLivingStopTimeBean> data = new ArrayList<>();

    private OnItemClickListener onItemClickListener;

    public SetLivingStopTimeAdapter(Context context, List<SetLivingStopTimeBean> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_living_stop_time_set, parent, false);
        return new LivingStopTimeHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        LivingStopTimeHolder lHolder = (LivingStopTimeHolder) holder;
        SetLivingStopTimeBean bean = data.get(position);
        if(bean!=null){
            lHolder.tvTime.setText((bean.getTime() == 0)?context.getString(R.string.text_living_stop_time_no_limit):String.format(context.getString(R.string.text_living_stop_time), bean.getTime()));
            if(bean.isChecked()){
                lHolder.tvTime.setTextColor(context.getResources().getColor(R.color.white));
                lHolder.tvTime.setBackgroundResource(R.drawable.bg_bind_button_blue_white);
            }else{
                lHolder.tvTime.setTextColor(context.getResources().getColor(R.color.blue_42));
                lHolder.tvTime.setBackgroundResource(R.drawable.bg_bind_button_blue);
            }
            lHolder.tvTime.setOnClickListener(view -> {
                if (onItemClickListener != null&&!data.get(position).isChecked()) {
                    onItemClickListener.onClick(bean);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class LivingStopTimeHolder extends RecyclerView.ViewHolder {
        TextView tvTime;

        LivingStopTimeHolder(View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onClick(SetLivingStopTimeBean bean);
    }
}

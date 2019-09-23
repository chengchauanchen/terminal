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

import cn.vsx.vc.R;
import cn.vsx.vc.model.BindBean;
import cn.vsx.vc.model.TerminalType;
import cn.vsx.vc.utils.HongHuUtils;

/**
 * 作者：xuxiaolong
 * 版本：1.0
 * 创建日期：2019/1/4
 * 描述：
 * 修订历史：
 */
public class BindEquipmentListAdapter extends RecyclerView.Adapter<BindEquipmentListAdapter.ViewHolder> {

    //显示的数据
    private List<BindBean> datas;
    private Context context;
    private LayoutInflater inflater;

    public BindEquipmentListAdapter(Context context, List<BindBean> warningRecords) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.datas = warningRecords;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.item_bind_equipment_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BindBean bindBean = datas.get(position);

        switch (bindBean.getEquipmentType()){
            case TerminalType.TERMINAL_PATROL://船
                holder.iv_equipment.setImageResource(R.drawable.ico_unbinding_1);
                break;
            case TerminalType.TERMINAL_CAR://车
                holder.iv_equipment.setImageResource(R.drawable.ico_unbinding_2);
                break;
            case TerminalType.TERMINAL_PDT://电台
                holder.iv_equipment.setImageResource(R.drawable.ico_unbinding_4);
                break;
            case TerminalType.TERMINAL_LTE://LTE
                holder.iv_equipment.setImageResource(R.drawable.ico_unbinding_3);
                break;
            default:
                holder.iv_equipment.setImageResource(R.drawable.ico_unbinding_1);
                break;
        }
        holder.tv_content.setText(bindBean.getEquipmentNo());
        holder.tv_unbind.setOnClickListener(v -> {
            HongHuUtils.unBindDevice(bindBean.getId(),position);
        });
    }


    // 获取条目数量，之所以要加1是因为增加了一条footView
    @Override
    public int getItemCount() {
        return datas.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout ll_root_view;
        private ImageView iv_equipment;
        private TextView tv_content;
        private TextView tv_unbind;

        private ViewHolder(View itemView) {
            super(itemView);
            ll_root_view = itemView.findViewById(R.id.ll_root_view);
            iv_equipment = itemView.findViewById(R.id.iv_equipment);
            tv_content = itemView.findViewById(R.id.tv_content);
            tv_unbind = itemView.findViewById(R.id.tv_unbind);
        }
    }


}

package cn.vsx.vc.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.vc.R;
import cn.vsx.vc.model.BindBean;
import cn.vsx.vc.model.Boat;
import cn.vsx.vc.model.Car;
import cn.vsx.vc.model.DongHuTerminalType;
import cn.vsx.vc.model.HongHuBean;
import cn.vsx.vc.utils.HongHuUtils;

/**
 * 作者：xuxiaolong
 * 版本：1.0
 * 创建日期：2019/1/4
 * 描述：
 * 修订历史：
 */
public class BindShipListAdapter extends RecyclerView.Adapter<BindShipListAdapter.ViewHolder> {

    //显示的数据
    private List<Boat> datas = new ArrayList<>();
    private Context context;
    private LayoutInflater inflater;

    public BindShipListAdapter(Context context, List<Boat> boats) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        datas.clear();
        this.datas.addAll(boats);
    }

    public List<Boat> getDatas() {
        return datas;
    }

    public void notifyData(List<Boat> boats){
        datas.clear();
        this.datas.addAll(boats);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.item_bind_ship_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Boat boat = datas.get(position);
        holder.tv_content.setText(boat.getUniqueNo());
        holder.iv_checkbox.setImageResource(boat.isCheck() ? R.drawable.ico_bindingcar_selected : R.drawable.ico_bindingcar_unselect);
        holder.ll_content.setOnClickListener(v -> {
            boolean isCheck = boat.isCheck();
            List<Boat> boatList = setNoChecked(datas);
            boatList.get(position).setCheck(!isCheck);
            notifyData(boatList);
        });
    }

    // 获取条目数量，之所以要加1是因为增加了一条footView
    @Override
    public int getItemCount() {
        return datas.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout ll_content;
        private ImageView iv_device;
        private TextView tv_content;
        private ImageView iv_checkbox;

        private ViewHolder(View itemView) {
            super(itemView);
            ll_content = itemView.findViewById(R.id.ll_content);
            iv_device = itemView.findViewById(R.id.iv_device);
            tv_content = itemView.findViewById(R.id.tv_content);
            iv_checkbox = itemView.findViewById(R.id.iv_checkbox);
        }
    }


    private List<Boat> setNoChecked(List<Boat> boats){
        List<Boat> huBeans = new ArrayList<>();
        huBeans.addAll(boats);
        for (Boat boat : huBeans){
            boat.setCheck(false);
        }
        return huBeans;
    }
}

package com.vsxin.terminalpad.mvp.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.location.LLSInterface;
import com.ixiaoma.xiaomabus.architecture.mvp.refresh.adapter.BaseRecycleViewAdapter;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.constant.MemberTypeEnum;
import com.vsxin.terminalpad.mvp.contract.presenter.LayerMapPresenter;

import cn.vsx.hamster.terminalsdk.model.TerminalMessage;

/**
 * @author qzw
 * <p>
 * app模块-消息模块
 */
public class LayerMapAdapter extends BaseRecycleViewAdapter<MemberTypeEnum, LayerMapAdapter.ViewHolder> {

    private LayerMapPresenter layerMapPresenter;

    public LayerMapAdapter(Context mContext, LayerMapPresenter layerMapPresenter) {
        super(mContext);
        this.layerMapPresenter = layerMapPresenter;
    }

    @NonNull
    @Override
    public LayerMapAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_layer_map_item, parent, false);
        return new ViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(@NonNull LayerMapAdapter.ViewHolder holder, int position) {
        MemberTypeEnum memberTypeEnum = getDatas().get(position);

        holder.checkbox.setChecked(memberTypeEnum.isCheck());
        holder.iv_layer.setImageResource(memberTypeEnum.getResId());
        holder.tv_type_name.setText(memberTypeEnum.getName());

        holder.ll_row.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.checkbox.setChecked(!memberTypeEnum.isCheck());
                memberTypeEnum.setCheck(!memberTypeEnum.isCheck());
                layerMapPresenter.drawMapLayer(memberTypeEnum.getType(),memberTypeEnum.isCheck());
            }
        });

    }

    @Override
    public int getItemCount() {
        return getDatas().size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        private final LinearLayout ll_row;
        private final ImageView iv_layer;
        private final CheckBox checkbox;
        private final TextView tv_type_name;

        public ViewHolder(View itemView) {
            super(itemView);
            ll_row = itemView.findViewById(R.id.ll_row);
            checkbox = itemView.findViewById(R.id.checkbox);
            iv_layer = itemView.findViewById(R.id.iv_layer);
            tv_type_name = itemView.findViewById(R.id.tv_type_name);
        }
    }
}

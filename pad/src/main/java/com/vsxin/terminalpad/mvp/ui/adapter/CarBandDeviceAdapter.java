package com.vsxin.terminalpad.mvp.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ixiaoma.xiaomabus.architecture.mvp.refresh.adapter.BaseRecycleViewAdapter;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.entity.DeviceBean;
import com.vsxin.terminalpad.mvp.entity.PersonnelBean;
import com.vsxin.terminalpad.mvp.entity.TerminalBean;
import com.vsxin.terminalpad.utils.TerminalUtils;

/**
 * @author qzw
 * <p>
 * 点击地图 警车 图层 ----警车绑定的设备列表
 */
public class CarBandDeviceAdapter extends BaseRecycleViewAdapter<DeviceBean, CarBandDeviceAdapter.ViewHolder> {

    public CarBandDeviceAdapter(Context mContext) {
        super(mContext);
    }

    @NonNull
    @Override
    public CarBandDeviceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_car_boat_item, parent, false);
        return new ViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(@NonNull CarBandDeviceAdapter.ViewHolder holder, int position) {
        DeviceBean deviceBean = getDatas().get(position);
        holder.tv_type.setVisibility(View.GONE);
        //显示分类 view
        if (position == 0) {
            holder.tv_type.setVisibility(View.VISIBLE);
        } else {
            DeviceBean deviceBean1 = getDatas().get(position - 1);
            if (deviceBean1.isPolice() != deviceBean.isPolice()) {
                holder.tv_type.setVisibility(View.VISIBLE);
            } else {
                holder.tv_type.setVisibility(View.GONE);
            }
        }

        if (deviceBean.isPolice()) {
            PersonnelBean personnel = deviceBean.getPersonnel();
            holder.tv_type.setText("民警：");
            holder.iv_device.setImageResource(R.mipmap.img_police);
            holder.tv_device_name.setText(personnel.getPersonnelName());
        } else {
            holder.tv_type.setText("装备：");
            TerminalBean terminal = deviceBean.getTerminal();
            int resId = TerminalUtils.getImageForTerminalType(terminal.getTerminalType());
            holder.iv_device.setImageResource(resId);
            String terminalName = TerminalUtils.getNameForTerminalType(terminal.getTerminalType());
            holder.tv_device_name.setText(terminalName);
        }
    }

    @Override
    public int getItemCount() {
        return getDatas().size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final LinearLayout ll_row;
        private final ImageView iv_device;
        private final TextView tv_device_name;
        private final ImageView iv_call_phone;
        private final ImageView iv_message;
        private final ImageView iv_push_video;
        private final ImageView iv_individual_call;
        //装备/民警
        private final TextView tv_type;

        public ViewHolder(View itemView) {
            super(itemView);
            ll_row = itemView.findViewById(R.id.ll_row);
            iv_device = itemView.findViewById(R.id.iv_device);//设备图标
            tv_device_name = itemView.findViewById(R.id.tv_device_name);//设备名称
            iv_call_phone = itemView.findViewById(R.id.iv_call_phone);//打电话
            iv_message = itemView.findViewById(R.id.iv_message);//消息
            iv_push_video = itemView.findViewById(R.id.iv_push_video);//拉视频
            iv_individual_call = itemView.findViewById(R.id.iv_individual_call);//个呼
            tv_type = itemView.findViewById(R.id.tv_type);//个呼
        }
    }
}
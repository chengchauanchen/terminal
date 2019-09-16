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
import com.vsxin.terminalpad.mvp.entity.NoticeBean;

/**
 * @author qzw
 * <p>
 * 点击地图 警察 图层 ----警察绑定的设备列表
 */
public class CarBoatBandDeviceAdapter extends BaseRecycleViewAdapter<DeviceBean, CarBoatBandDeviceAdapter.ViewHolder> {

    public CarBoatBandDeviceAdapter(Context mContext) {
        super(mContext);
    }

    @NonNull
    @Override
    public CarBoatBandDeviceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_car_boat_item, parent, false);
        return new ViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(@NonNull CarBoatBandDeviceAdapter.ViewHolder holder, int position) {
        DeviceBean deviceBean = getDatas().get(position);
        holder.tv_type.setVisibility(View.GONE);
        if(deviceBean.isPolice()){
            holder.tv_type.setText("民警：");
        }else{
            holder.tv_type.setText("装备：");
        }
        if(position==0){
            holder.tv_type.setVisibility(View.VISIBLE);
        }else{
            DeviceBean deviceBean1 = getDatas().get(position - 1);
            if(deviceBean1.isPolice()!=deviceBean.isPolice()){
                holder.tv_type.setVisibility(View.VISIBLE);
            }else{
                holder.tv_type.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return getDatas().size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

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

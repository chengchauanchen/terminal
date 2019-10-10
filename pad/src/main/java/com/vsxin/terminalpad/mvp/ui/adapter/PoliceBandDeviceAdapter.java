package com.vsxin.terminalpad.mvp.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ixiaoma.xiaomabus.architecture.mvp.refresh.adapter.BaseRecycleViewAdapter;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.manager.PullLiveManager;
import com.vsxin.terminalpad.manager.StartCallManager;
import com.vsxin.terminalpad.mvp.contract.constant.TerminalEnum;
import com.vsxin.terminalpad.mvp.entity.PersonnelBean;
import com.vsxin.terminalpad.mvp.entity.TerminalBean;
import com.vsxin.terminalpad.utils.NumberUtil;
import com.vsxin.terminalpad.utils.TerminalUtils;

import ptt.terminalsdk.tools.ToastUtil;

/**
 * @author qzw
 * <p>
 * 点击地图 警察 图层 ----警察绑定的设备列表
 */
public class PoliceBandDeviceAdapter extends BaseRecycleViewAdapter<TerminalBean, PoliceBandDeviceAdapter.ViewHolder> {

    private final StartCallManager startCallManager;

    public PoliceBandDeviceAdapter(Context mContext) {
        super(mContext);
        //呼警务通
        startCallManager = new StartCallManager(getContext());
    }

    @NonNull
    @Override
    public PoliceBandDeviceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_police_device_item, parent, false);
        return new ViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(@NonNull PoliceBandDeviceAdapter.ViewHolder holder, int position) {
        TerminalBean terminalBean = getDatas().get(position);
        int resId = TerminalUtils.getImageForTerminalType(terminalBean.getTerminalType());
        holder.iv_device.setImageResource(resId);

        holder.iv_call_phone.setVisibility(View.GONE);
        holder.iv_message.setVisibility(View.GONE);
        holder.iv_push_video.setVisibility(View.GONE);
        holder.iv_individual_call.setVisibility(View.GONE);
        //根据装备类型,显示操作按钮
        String terminalType = terminalBean.getTerminalType();
        ImageView[] imageRid = {holder.iv_call_phone,holder.iv_message,holder.iv_push_video,holder.iv_individual_call};
        TerminalUtils.showOperate(imageRid,terminalType);

        try{
            TerminalEnum terminalEnum = TerminalEnum.valueOf(terminalType);
            if(terminalEnum!=null){
                holder.tv_device_name.setText(terminalEnum.getDes());
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        holder.iv_individual_call.setOnClickListener(v -> {
            TerminalEnum  terminalEnum = TerminalEnum.valueOf(terminalBean.getTerminalType());
            StartCallManager startCallManager = new StartCallManager(getContext());
            startCallManager.startIndividualCall(terminalEnum.getDes(), terminalBean);
        });

        holder.iv_push_video.setOnClickListener(v -> {
            //拉视频
            PullLiveManager liveManager = new PullLiveManager(getContext());
            String terminalUniqueNo = TerminalUtils.getPullLiveUniqueNo(terminalBean);
            liveManager.pullVideo(terminalBean.getAccount(), TerminalEnum.valueOf(terminalType), terminalUniqueNo);
        });
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

        public ViewHolder(View itemView) {
            super(itemView);
            ll_row = itemView.findViewById(R.id.ll_row);
            iv_device = itemView.findViewById(R.id.iv_device);//设备图标
            tv_device_name = itemView.findViewById(R.id.tv_device_name);//设备名称
            iv_call_phone = itemView.findViewById(R.id.iv_call_phone);//打电话
            iv_message = itemView.findViewById(R.id.iv_message);//消息
            iv_push_video = itemView.findViewById(R.id.iv_push_video);//拉视频
            iv_individual_call = itemView.findViewById(R.id.iv_individual_call);//个呼
        }
    }
}

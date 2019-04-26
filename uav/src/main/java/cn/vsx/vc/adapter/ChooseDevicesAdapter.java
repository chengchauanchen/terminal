package cn.vsx.vc.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.vc.R;
import cn.vsx.vc.dialog.ChooseDevicesDialog;
import cn.vsx.vc.model.CatalogBean;

/**
 * 选择设备
 */

public class ChooseDevicesAdapter extends RecyclerView.Adapter<ChooseDevicesAdapter.MyViewHolder> {
    private Context mContext;
    private List<Member> list;
    private ChooseDevicesDialog dialog;
    private ItemClickListener mItemClickListener;

    //是否是打电话
    private boolean isCallPhone ;

    public ChooseDevicesAdapter(Context context, ChooseDevicesDialog dialog, List<Member> list, ItemClickListener mItemClickListener, boolean isCallPhone) {
        this.mContext = context;
        this.list = list;
        this.dialog = dialog;
        this.mItemClickListener = mItemClickListener;
        this.isCallPhone = isCallPhone;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_choose_devices, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        if (list != null && !list.isEmpty() && list.get(position) != null) {
            Member member = list.get(position);
            holder.ivIcon.setImageResource(getImageResource(member.type));
            if(isCallPhone){
                //拨打电话（语音电话和普通电话）
                holder.tvContext.setText(getCallPhoneName(member.getUniqueNo()));
            }else{
                holder.tvContext.setText(getTypeName(member.type));
            }
            holder.llAll.setOnClickListener(view -> {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(dialog,list.get(position));
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface ItemClickListener {
        void onItemClick( ChooseDevicesDialog dialog,Member member);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llAll;
        ImageView ivIcon;
        TextView tvContext;

        public MyViewHolder(View itemView) {
            super(itemView);
            llAll = itemView.findViewById(R.id.ll_all);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvContext = itemView.findViewById(R.id.tv_context);
        }
    }

    /**
     * 根据类型获取图标
     * @param type
     * @return
     */
    private int getImageResource(int type) {
        if (type == TerminalMemberType.TERMINAL_PC.getCode()) {
            return R.drawable.icon_pc_device;
        } else if (type == TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.getCode()) {
            return R.drawable.icon_record_device;
        } else if (type == TerminalMemberType.TERMINAL_UAV.getCode()) {
            return R.drawable.icon_uav_device;
        } else {
            return R.drawable.icon_phone_device;
        }
    }

    /**
     * 根据类型获取类型名称
     * @param type
     * @return
     */
    private String getTypeName(int type) {
        return subName(TerminalMemberType.getInstanceByCode(type).getValue());
    }

    /**
     * 截取类型名称
     * @param name
     * @return
     */
    private String  subName(String name){
        if(!TextUtils.isEmpty(name)){
            String content = mContext.getString(R.string.text_terminal);
            if(name.contains(content)){
                int index = name.lastIndexOf(content);
                return name.substring(0,index);
            }else{
                return name;
            }
        }
        return "";
    }

    /**
     * 获取拨打电话显示的文字信息
     * @param uniqueNo
     * @return
     */
    private String getCallPhoneName(long uniqueNo){
        return (uniqueNo<=0)?mContext.getString(R.string.text_normal_telephone)
                :mContext.getString(R.string.text_recording_telephone);
    }
}

package com.vsxin.terminalpad.mvp.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.entity.ContactItemBean;
import com.vsxin.terminalpad.utils.BitmapUtil;
import com.vsxin.terminalpad.utils.HandleIdUtil;

import java.util.List;

import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.Member;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/4/17
 * 描述：
 * 修订历史：
 */
public class SelectedItemListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private List<ContactItemBean> mData;
    private Context mContext;
    private ItemClickListener itemClickListener;

    public SelectedItemListAdapter(Context mContext, List<ContactItemBean> mData ){
        this.mContext = mContext;
        this.mData = mData;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            View view = LayoutInflater.from(mContext).inflate(R.layout.layout_item_selected, parent, false);
            return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position){
        ContactItemBean contactItemBean = mData.get(position);
        UserViewHolder userViewHolder = (UserViewHolder) holder;
        if(contactItemBean.getBean() instanceof Member){
            Member member = (Member) contactItemBean.getBean();
            if(member != null){
                userViewHolder.ivLogo.setImageResource(BitmapUtil.getPadDeviceImageResourceByType(member.getType()));
                userViewHolder.tvName.setText(member.getName());
                userViewHolder.tvId.setText(HandleIdUtil.handleId(member.getNo()));
                userViewHolder.ivDelete.setOnClickListener(v -> {
                    if(null != itemClickListener){
                        itemClickListener.itemClick(position);
                    }
                });
            }
        }else if(contactItemBean.getBean() instanceof Group){
            Group group = (Group) contactItemBean.getBean();
            if(group != null){
                userViewHolder.ivLogo.setImageResource(R.drawable.group_photo);
                userViewHolder.tvName.setText(group.getName());
                userViewHolder.tvId.setVisibility(View.GONE);
            }
        }else if (contactItemBean.getBean() instanceof Account){
            Account account = (Account) contactItemBean.getBean();
            if(account != null){
                userViewHolder.ivLogo.setImageResource(BitmapUtil.getUserPhoto());
                userViewHolder.tvName.setText(account.getName());
                userViewHolder.tvId.setText(HandleIdUtil.handleId(account.getNo()));
                userViewHolder.ivDelete.setOnClickListener(v -> {
                    if(null != itemClickListener){
                        itemClickListener.itemClick(position);
                    }
                });
            }
        }
        userViewHolder.ivDelete.setOnClickListener(v -> {
            if(null != itemClickListener){
                itemClickListener.itemClick(position);
            }
        });
    }

    @Override
    public int getItemCount(){
        return mData.size();
    }


    class UserViewHolder extends RecyclerView.ViewHolder{

        ImageView ivLogo;

        TextView tvName;

        TextView tvId;

        ImageView ivDelete;

        public UserViewHolder(View itemView){
            super(itemView);
            ivLogo = itemView.findViewById(R.id.shoutai_user_logo);
            tvName = itemView.findViewById(R.id.shoutai_tv_member_name);
            tvId = itemView.findViewById(R.id.shoutai_tv_member_id);
            ivDelete = itemView.findViewById(R.id.iv_delete);
        }
    }

    public void setItemClickListener(ItemClickListener itemClickListener){
        this.itemClickListener =itemClickListener;
    }

    public interface ItemClickListener{
        void itemClick(int position);
    }
}

package cn.vsx.vc.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.vsx.hamster.terminalsdk.model.Department;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.vc.R;
import cn.vsx.vc.model.ContactItemBean;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.HandleIdUtil;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/4/17
 * 描述：
 * 修订历史：
 */
public class SelectedListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private List<Member> mData;
    private Context mContext;
    private ItemClickListener itemClickListener;

    public SelectedListAdapter(Context mContext, List<Member> mData ){
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
            UserViewHolder userViewHolder = (UserViewHolder) holder;
            Member member =  mData.get(position);
            if(member != null){
                userViewHolder.tvName.setText(member.getName());
                userViewHolder.tvId.setText(HandleIdUtil.handleId(member.getNo()));
                userViewHolder.ivDelete.setOnClickListener(v -> {
                    if(null != itemClickListener){
                        itemClickListener.itemClick(position);
                    }
                });
            }
    }

    @Override
    public int getItemCount(){
        return mData.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder{
        @Bind(R.id.shoutai_user_logo)
        ImageView ivLogo;
        @Bind(R.id.shoutai_tv_member_name)
        TextView tvName;
        @Bind(R.id.shoutai_tv_member_id)
        TextView tvId;
        @Bind(R.id.iv_delete)
        ImageView ivDelete;

        public UserViewHolder(View itemView){
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void setItemClickListener(ItemClickListener itemClickListener){
        this.itemClickListener =itemClickListener;
    }

    public interface ItemClickListener{
        void itemClick(int position);
    }
}

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

import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Department;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.vc.R;
import cn.vsx.vc.model.ContactItemBean;
import cn.vsx.vc.utils.BitmapUtil;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.HandleIdUtil;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/4/17
 * 描述：
 * 修订历史：
 */
public class AccountListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private List<ContactItemBean> mData;
    private Context mContext;
    private LayoutInflater mInflater;
    private ItemClickListener itemClickListener;

    public AccountListAdapter(Context mContext, List<ContactItemBean> mData ){
        this.mContext = mContext;
        this.mData = mData;
        mInflater = LayoutInflater.from(mContext);
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        if(viewType == Constants.TYPE_ACCOUNT){
            View view = LayoutInflater.from(mContext).inflate(R.layout.layout_item_user, parent, false);
            return new UserViewHolder(view);
        }else if(viewType == Constants.TYPE_DEPARTMENT){
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_department, parent, false);
            return new DepartmentViewHolder(view);
        }else if(viewType == Constants.TYPE_GROUP){
            View view = LayoutInflater.from(mContext).inflate(R.layout.layout_item_group_check, parent, false);
            return new GroupViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position){
        if(getItemViewType(position) == Constants.TYPE_ACCOUNT){
            UserViewHolder userViewHolder = (UserViewHolder) holder;
            Account account = (Account) mData.get(position).getBean();
            if (account != null) {
                userViewHolder.ivLogo.setImageResource(BitmapUtil.getUserPhoto());
                userViewHolder.tvName.setText(account.getName());
                userViewHolder.tvId.setText(HandleIdUtil.handleId(account.getNo()));
                userViewHolder.checkbox.setChecked(account.isChecked());
                userViewHolder.checkbox.setOnClickListener(v -> {
                    if(null != itemClickListener){
                        itemClickListener.itemClick(Constants.TYPE_ACCOUNT,position);
                    }
                });
            }
        }else if(getItemViewType(position) == Constants.TYPE_DEPARTMENT){
            DepartmentViewHolder departmentViewHolder = (DepartmentViewHolder) holder;
            Department department = (Department) mData.get(position).getBean();
            departmentViewHolder.tvDepartment.setText(department.getName());
            departmentViewHolder.itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    if(itemClickListener !=null){
                        itemClickListener.itemClick(Constants.TYPE_DEPARTMENT,position);
                    }
                }
            });
        }else if(getItemViewType(position) == Constants.TYPE_GROUP){
            GroupViewHolder groupViewHolder = (GroupViewHolder) holder;
            Group group = (Group) mData.get(position).getBean();
            groupViewHolder.tvName.setText(group.getName());
            groupViewHolder.checkbox.setChecked(group.isChecked());
            groupViewHolder.checkbox.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    if(null != itemClickListener){
                        itemClickListener.itemClick(Constants.TYPE_GROUP,position);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount(){
        return mData.size();
    }

    @Override
    public int getItemViewType(int position){
        ContactItemBean contactItemBean = mData.get(position);
        if(contactItemBean.getType() == Constants.TYPE_ACCOUNT){
            return Constants.TYPE_ACCOUNT;
        }else if(contactItemBean.getType() == Constants.TYPE_DEPARTMENT){
            return Constants.TYPE_DEPARTMENT;
        }else if(contactItemBean.getType() == Constants.TYPE_GROUP){
            return Constants.TYPE_GROUP;
        }
        return -1;
    }

    class GroupViewHolder extends RecyclerView.ViewHolder{

        ImageView ivLogo;

        TextView tvName;

        CheckBox checkbox;
        public GroupViewHolder(View itemView){
            super(itemView);
            ivLogo = itemView.findViewById(R.id.shoutai_user_logo);
            tvName = itemView.findViewById(R.id.shoutai_tv_member_name);
            CheckBox checkbox = itemView.findViewById(R.id.checkbox);
        }
    }

    class UserViewHolder extends RecyclerView.ViewHolder{

        ImageView ivLogo;

        TextView tvName;

        TextView tvId;

        CheckBox checkbox;

        public UserViewHolder(View itemView){
            super(itemView);
            ivLogo = itemView.findViewById(R.id.shoutai_user_logo);
            tvName = itemView.findViewById(R.id.shoutai_tv_member_name);
            tvId = itemView.findViewById(R.id.shoutai_tv_member_id);
            checkbox = itemView.findViewById(R.id.checkbox);
        }
    }

    class DepartmentViewHolder extends RecyclerView.ViewHolder{
        TextView tvDepartment;

        public DepartmentViewHolder(View itemView){
            super(itemView);
            tvDepartment = itemView.findViewById(R.id.tv_department);
        }
    }

    public void setItemClickListener(ItemClickListener itemClickListener){
        this.itemClickListener =itemClickListener;
    }

    public interface ItemClickListener{
        void itemClick(int type, int position);
    }
}

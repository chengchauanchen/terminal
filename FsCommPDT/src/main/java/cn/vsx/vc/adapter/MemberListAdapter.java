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


import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.terminalsdk.model.Department;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.Member;
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
public class MemberListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private List<ContactItemBean> mData;
    private Context mContext;
    private LayoutInflater mInflater;
    private ItemClickListener itemClickListener;

    public MemberListAdapter(Context mContext,List<ContactItemBean> mData ){
        this.mContext = mContext;
        this.mData = mData;
        mInflater = LayoutInflater.from(mContext);
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        if(viewType == Constants.TYPE_USER){
            View view = LayoutInflater.from(mContext).inflate(R.layout.layout_item_user, parent, false);
            return new UserViewHolder(view);
        }else if(viewType == Constants.TYPE_FOLDER){
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
        if(getItemViewType(position) == Constants.TYPE_USER){
            UserViewHolder userViewHolder = (UserViewHolder) holder;
            Member member = (Member) mData.get(position).getBean();
            if (member != null) {
                userViewHolder.ivLogo.setImageResource(BitmapUtil.getImageResourceByType(member.getType()));

                userViewHolder.tvName.setText(member.getName());
                userViewHolder.tvId.setText(HandleIdUtil.handleId(member.getNo()));
                userViewHolder.tvId.setVisibility(View.VISIBLE);
                if(member.getType() == TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.getCode()){
                    userViewHolder.ivRBindedLogo.setVisibility(member.isBind()?View.VISIBLE:View.GONE);
                    if(!member.isBind()){
                        userViewHolder.tvName.setText(HandleIdUtil.handleId(member.getNo()));
                        userViewHolder.tvId.setVisibility(View.GONE);
                    }
                }else{
                    userViewHolder.ivRBindedLogo.setVisibility(View.GONE);
                }
                userViewHolder.checkbox.setChecked(member.isChecked());
                userViewHolder.checkbox.setOnClickListener(v -> {
                    if(null != itemClickListener){
                        itemClickListener.itemClick(Constants.TYPE_USER,position);
                    }
                });
            }
        }else if(getItemViewType(position) == Constants.TYPE_FOLDER){
            DepartmentViewHolder departmentViewHolder = (DepartmentViewHolder) holder;
            Department department = (Department) mData.get(position).getBean();
            departmentViewHolder.tvDepartment.setText(department.getName());
            departmentViewHolder.itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    if(itemClickListener !=null){
                        itemClickListener.itemClick(Constants.TYPE_FOLDER,position);
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
        if(contactItemBean.getType() == Constants.TYPE_USER){
            return Constants.TYPE_USER;
        }else if(contactItemBean.getType() == Constants.TYPE_FOLDER){
            return Constants.TYPE_FOLDER;
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
            checkbox = itemView.findViewById(R.id.checkbox);
        }
    }

    class UserViewHolder extends RecyclerView.ViewHolder{

        ImageView ivLogo;
        ImageView ivRBindedLogo;

        TextView tvName;

        TextView tvId;

        CheckBox checkbox;

        public UserViewHolder(View itemView){
            super(itemView);
            ivLogo = itemView.findViewById(R.id.shoutai_user_logo);
            ivRBindedLogo = itemView.findViewById(R.id.recorder_binded_logo);
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

package cn.vsx.vc.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Department;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.GroupAndDepartment;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.utils.Constants;

/**
 * Created by Administrator on 2017/3/14 0014.
 * 群组adapter
 */

public class GroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private Context context;

    //显示在recyclerview上的所有数据
    private List<GroupAndDepartment>allGroupAndDepartment = new ArrayList<>();

    private int width = 0;
    private OnItemClickListener onItemClickListener;
    public GroupAdapter(Context context, List<GroupAndDepartment>allGroupAndDepartment){
        this.context = context;
        this.allGroupAndDepartment = allGroupAndDepartment;
        width = ScreenUtils.getScreenWidth()/4;
    }



    @Override
    public int getItemViewType(int position) {
        GroupAndDepartment groupAndDepartment = allGroupAndDepartment.get(position);
        if(groupAndDepartment.getType() == Constants.TYPE_FOLDER){
            return Constants.TYPE_FOLDER;
        }else{
            return Constants.TYPE_GROUP;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        if(viewType == Constants.TYPE_FOLDER){
            View view = LayoutInflater.from(context).inflate(R.layout.item_department,parent,false);
            return new DepartmentViewHolder(view);
        }else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_group,parent,false);
            return new GroupViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position){
        if(getItemViewType(position)== Constants.TYPE_FOLDER){
            DepartmentViewHolder departmentViewHolder = (DepartmentViewHolder) holder;
            final Department department = (Department) allGroupAndDepartment.get(position).getBean();
            departmentViewHolder.tvFolderName.setText(department.getName());

            departmentViewHolder.itemView.setOnClickListener(view -> {
                if(onItemClickListener !=null){
                    if(department.getId() ==-1){
                        onItemClickListener.onFolderClick(view,department.getId(),department.getName(),true);
                    }else {
                        onItemClickListener.onFolderClick(view,department.getId(),department.getName(),false);
                    }
                }
            });
        }else{
            GroupViewHolder groupViewHolder = (GroupViewHolder) holder;
            final Group group = (Group) allGroupAndDepartment.get(position).getBean();
            groupViewHolder.tvGroupName.setText(group.getName());
            int currentGroupId = TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);
            if(currentGroupId == group.getNo()){
                groupViewHolder.ivGroup.setImageResource(R.drawable.ico_group_pickup);
                groupViewHolder.itemView.setOnClickListener(null);
            }else{
                groupViewHolder.ivGroup.setImageResource(R.drawable.ico_group);
                groupViewHolder.itemView.setOnClickListener(view -> {
                    if(onItemClickListener!=null){
                        onItemClickListener.onGroupClick(group);
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount(){
        return allGroupAndDepartment.size();
    }

    class DepartmentViewHolder extends RecyclerView.ViewHolder{
        ImageView ivFolder;
        TextView  tvFolderName;
        DepartmentViewHolder(View rootView){
            super(rootView);
            ivFolder = itemView.findViewById(R.id.iv_folder);
            tvFolderName = itemView.findViewById(R.id.tv_folder_name);
        }
    }

    class GroupViewHolder extends RecyclerView.ViewHolder{
        ImageView ivGroup;
        TextView  tvGroupName;
        GroupViewHolder(View itemView) {
            super(itemView);
            ivGroup = itemView.findViewById(R.id.iv_group);
            tvGroupName = itemView.findViewById(R.id.tv_group_name);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }
    public interface OnItemClickListener{
        void onFolderClick(View view, int depId, String name, boolean isTempGroup);
        void onGroupClick(Group group);
    }
}

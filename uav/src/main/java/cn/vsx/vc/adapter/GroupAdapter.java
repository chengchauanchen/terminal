package cn.vsx.vc.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.vsx.hamster.common.GroupType;
import cn.vsx.hamster.terminalsdk.model.Department;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.GroupAndDepartment;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.GroupCallNewsActivity;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.model.CatalogBean;
import cn.vsx.vc.receiveHandle.ReceiverShowGroupFragmentHandler;
import cn.vsx.vc.utils.Constants;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * Created by Administrator on 2017/3/14 0014.
 * 群组adapter
 */

public class GroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private Context context;

    //显示在recyclerview上的所有数据
    private List<GroupAndDepartment>allGroupAndDepartment = new ArrayList<>();

    //临时组数据
    private List<Group>tempGroup = new ArrayList<>();

    //上面标题数据
    private List<CatalogBean> mTempCatalogList=new ArrayList<>();
    private List<CatalogBean> mCatalogList=new ArrayList<>();

    private CatalogItemClickListener catalogItemClickListener;
    private FolderClickListener folderClickListener;
    private long lastSearchTime;
    public GroupAdapter(Context context,List<GroupAndDepartment>allGroupAndDepartment,
                        List<Group>tempGroup,List<CatalogBean> mTempCatalogList,
                        List<CatalogBean> mCatalogList){
        this.context = context;
        this.allGroupAndDepartment = allGroupAndDepartment;
        this.tempGroup = tempGroup;
        this.mTempCatalogList = mTempCatalogList;
        this.mCatalogList = mCatalogList;
    }



    @Override
    public int getItemViewType(int position) {
        GroupAndDepartment groupAndDepartment = allGroupAndDepartment.get(position);
        if(groupAndDepartment.getType() == Constants.TYPE_TITLE ||
                groupAndDepartment.getType() == Constants.TYPE_TEMP_TITLE){
            return Constants.TYPE_TITLE;
        }else if(groupAndDepartment.getType() == Constants.TYPE_FOLDER){
            return Constants.TYPE_FOLDER;
        }else{
            return Constants.TYPE_GROUP;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        if(viewType == Constants.TYPE_TITLE){
            View view = LayoutInflater.from(context).inflate(R.layout.group_adapter_parent_layout,parent,false);
            return new TitleViewHolder(view);
        }else if(viewType == Constants.TYPE_FOLDER){
            View view = LayoutInflater.from(context).inflate(R.layout.item_department,parent,false);
            return new DepartmentViewHolder(view);
        }else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_group,parent,false);
            return new GroupViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position){
        if (getItemViewType(position)==Constants.TYPE_TITLE){
            TitleViewHolder titleViewHolder = (TitleViewHolder) holder;
            if(allGroupAndDepartment.get(position).getType() == Constants.TYPE_TEMP_TITLE){
                titleViewHolder.iv_search.setVisibility(View.GONE);
            }else {
                titleViewHolder.iv_search.setVisibility(View.VISIBLE);
            }
            titleViewHolder.parent_recyclerview.setLayoutManager(new LinearLayoutManager(context, OrientationHelper.HORIZONTAL,false));
            if(allGroupAndDepartment.get(position).getType() == Constants.TYPE_TEMP_TITLE){
                GroupCatalogAdapter mCatalogAdapter=new GroupCatalogAdapter(context,mTempCatalogList);
                titleViewHolder.parent_recyclerview.setAdapter(mCatalogAdapter);
                mCatalogAdapter.setOnItemClick((view, position1) -> {
                    if(catalogItemClickListener !=null){
                        catalogItemClickListener.onCatalogItemClick(view,true, position1);
                    }
                });
            }else {
                GroupCatalogAdapter mCatalogAdapter=new GroupCatalogAdapter(context,mCatalogList);
                titleViewHolder.parent_recyclerview.setAdapter(mCatalogAdapter);
                mCatalogAdapter.setOnItemClick((view, position12) -> {
                    if(catalogItemClickListener !=null){
                        catalogItemClickListener.onCatalogItemClick(view,false, position12);
                    }
                });
            }
            titleViewHolder.iv_search.setOnClickListener(v -> {
                if(System.currentTimeMillis() - lastSearchTime<1000){
                    return;
                }
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverShowGroupFragmentHandler.class, null,false);
                lastSearchTime = System.currentTimeMillis();
            });

        }else if(getItemViewType(position)==Constants.TYPE_FOLDER){
            DepartmentViewHolder departmentViewHolder = (DepartmentViewHolder) holder;
            final Department department = (Department) allGroupAndDepartment.get(position).getBean();
            departmentViewHolder.tvFolder.setText(department.getName());

            departmentViewHolder.itemView.setOnClickListener(view -> {
                if(folderClickListener !=null){
                    if(department.getId() ==-1){
                        folderClickListener.onFolderClick(view,department.getId(),department.getName(),true);
                    }else {
                        folderClickListener.onFolderClick(view,department.getId(),department.getName(),false);
                    }
                }
            });
        }else{
            GroupViewHolder groupViewHolder = (GroupViewHolder) holder;
            final Group group = (Group) allGroupAndDepartment.get(position).getBean();
            groupViewHolder.tvName.setText(group.getName());
            if (group.getNo()== MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)){
                groupViewHolder.tvChangeGroup.setVisibility(View.INVISIBLE);
                groupViewHolder.ivCurrentGroup.setVisibility(View.VISIBLE);
            }else {
                groupViewHolder.tvChangeGroup.setVisibility(View.VISIBLE);
                groupViewHolder.ivMessage.setVisibility(View.VISIBLE);
                groupViewHolder.ivCurrentGroup.setVisibility(View.INVISIBLE);
            }
            if(group.getGroupType().equals(GroupType.RESPONSE.toString())){
                groupViewHolder.iv_group_logo.setImageResource(R.drawable.response_group_photo);
                TextViewCompat.setTextAppearance(groupViewHolder.tvName, R.style.group_name_color);
                groupViewHolder.iv_response_group_icon.setVisibility(View.VISIBLE);
            }else {
                groupViewHolder.iv_group_logo.setImageResource(R.drawable.group_photo);
                TextViewCompat.setTextAppearance(groupViewHolder.tvName, R.style.normal_group_color);
                groupViewHolder.iv_response_group_icon.setVisibility(View.GONE);
            }

            groupViewHolder.ivMessage.setOnClickListener(view -> {
                Intent intent = new Intent(context, GroupCallNewsActivity.class);
                intent.putExtra("isGroup", true);
                intent.putExtra("userId", group.getNo());//组id
                intent.putExtra("userName", group.getName());
                intent.putExtra("speakingId",group.getId());
                intent.putExtra("speakingName",group.getName());
                context.startActivity(intent);
            });

            groupViewHolder.tvChangeGroup.setOnClickListener(view -> {
                if(MyApplication.instance.isLocked){
                    ToastUtil.showToast(context, context.getString(R.string.group_locked_can_not_change_group));
                }else if(MyApplication.instance.isMiniLive){
                    ToastUtil.showToast(context, context.getString(R.string.text_small_window_mode_can_not_change_group));
                }else {
                    MyTerminalFactory.getSDK().getGroupManager().changeGroup(group.getNo());
                }

            });
            if(!tempGroup.isEmpty()){
                if(position == tempGroup.size()){
                    groupViewHolder.placeholder.setVisibility(View.VISIBLE);
                }else {
                    groupViewHolder.placeholder.setVisibility(View.GONE);
                }
            }else {
                groupViewHolder.placeholder.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount(){
        return allGroupAndDepartment.size();
    }

    class TitleViewHolder extends RecyclerView.ViewHolder{
        @Bind(R.id.parent_recyclerview)
        RecyclerView parent_recyclerview;
        @Bind(R.id.iv_search)
        ImageView iv_search;

        TitleViewHolder(View rootView){
            super(rootView);
            ButterKnife.bind(this, itemView);
        }
    }

    class DepartmentViewHolder extends RecyclerView.ViewHolder{
        @Bind(R.id.tv_department)
        TextView tvFolder;

        DepartmentViewHolder(View rootView){
            super(rootView);
            ButterKnife.bind(this, itemView);
        }
    }

    class GroupViewHolder extends RecyclerView.ViewHolder{
        @Bind(R.id.tv_name)
        TextView  tvName;
        @Bind(R.id.tv_change_group)
        TextView tvChangeGroup;
        @Bind(R.id.iv_message)
        ImageView ivMessage;
        @Bind(R.id.iv_current_group)
        ImageView ivCurrentGroup;
        @Bind(R.id.placeholder)
        View placeholder;
        @Bind(R.id.iv_group_logo)
        ImageView iv_group_logo;
        @Bind(R.id.iv_response_group_icon)
        ImageView iv_response_group_icon;

        GroupViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }
    }

    public void setCatalogItemClickListener(CatalogItemClickListener catalogItemClickListener){
        this.catalogItemClickListener = catalogItemClickListener;
    }

    public interface CatalogItemClickListener{
        void onCatalogItemClick(View view,boolean isTempGroup,int position);
    }

    public void setFolderClickListener(FolderClickListener folderClickListener){
        this.folderClickListener = folderClickListener;
    }
    public interface FolderClickListener{
        void onFolderClick(View view,int depId,String name,boolean isTempGroup);
    }
}

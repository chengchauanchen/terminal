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

import com.blankj.utilcode.util.ToastUtils;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.common.ResponseGroupType;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Department;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.GroupAndDepartment;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.GroupCallNewsActivity;
import cn.vsx.vc.model.CatalogBean;
import cn.vsx.vc.receiveHandle.ReceiveSwitchMainFrgamentHandler;
import cn.vsx.vc.receiveHandle.ReceiverMonitorViewClickHandler;
import cn.vsx.vc.receiveHandle.ReceiverShowGroupFragmentHandler;
import cn.vsx.vc.utils.Constants;
import ptt.terminalsdk.context.MyTerminalFactory;

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
    private MonitorOnClickListener monitorOnClickListener;
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
//                groupViewHolder.ivCurrentGroup.setVisibility(View.VISIBLE);
            }else {
                groupViewHolder.ivMessage.setVisibility(View.VISIBLE);
//                groupViewHolder.ivCurrentGroup.setVisibility(View.INVISIBLE);
            }
            if(ResponseGroupType.RESPONSE_TRUE.toString().equals(group.getResponseGroupType())){
                groupViewHolder.iv_group_logo.setImageResource(R.drawable.response_group_photo);
                TextViewCompat.setTextAppearance(groupViewHolder.tvName, R.style.group_name_color);
                groupViewHolder.iv_response_group_icon.setVisibility(View.VISIBLE);
            }else {
                groupViewHolder.iv_group_logo.setImageResource(R.drawable.group_photo);
                TextViewCompat.setTextAppearance(groupViewHolder.tvName, R.style.normal_group_color);
                groupViewHolder.iv_response_group_icon.setVisibility(View.GONE);
            }
            if(checkIsMonitorGroup(group)){
                groupViewHolder.ivMonitor.setImageResource(R.drawable.monitor_open);
            }else {
                groupViewHolder.ivMonitor.setImageResource(R.drawable.monitor_close_blue);
            }

            if(TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID,0) == group.getNo()){
                groupViewHolder.tv_change_group.setVisibility(View.GONE);
                groupViewHolder.iv_current_group.setVisibility(View.VISIBLE);
            }else {
                groupViewHolder.tv_change_group.setVisibility(View.VISIBLE);
                groupViewHolder.iv_current_group.setVisibility(View.GONE);
            }

            groupViewHolder.tv_change_group.setOnClickListener(view ->{
                if(TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID,0) != group.getNo()){
                    TerminalFactory.getSDK().getGroupManager().changeGroup(group.getNo());
                }
            });
            groupViewHolder.ivMessage.setOnClickListener(view -> {
                Intent intent = new Intent(context, GroupCallNewsActivity.class);
                intent.putExtra("isGroup", true);
                intent.putExtra("uniqueNo",group.getUniqueNo());
                intent.putExtra("userId", group.getNo());//组id
                intent.putExtra("userName", group.getName());
                intent.putExtra("speakingId",group.getId());
                intent.putExtra("speakingName",group.getName());
                context.startActivity(intent);
            });

            groupViewHolder.ivMonitor.setOnClickListener(view -> {
                if(TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID,0) == group.getNo()){
                    ToastUtils.showShort(R.string.current_group_cannot_cancel_monitor);
                }else {
                    TerminalFactory.getSDK().notifyReceiveHandler(ReceiverMonitorViewClickHandler.class,group.getNo());
                }
            });
            groupViewHolder.tvName.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    TerminalFactory.getSDK().notifyReceiveHandler(ReceiveSwitchMainFrgamentHandler.class,0);
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

    private boolean checkIsMonitorGroup(Group group){
        if(ResponseGroupType.RESPONSE_TRUE.toString().equals(group.getResponseGroupType())){
            return true;
        }
        if(TerminalFactory.getSDK().getConfigManager().getMonitorGroupNo().contains(group.getNo())){
            return true;
        }
        if(TerminalFactory.getSDK().getConfigManager().getTempMonitorGroupNos().contains(group.getNo())){
            return true;
        }
        if(TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID,0) == group.getNo()){
            return true;
        }

        return false;
    }

    @Override
    public int getItemCount(){
        return allGroupAndDepartment.size();
    }

    class TitleViewHolder extends RecyclerView.ViewHolder{
        RecyclerView parent_recyclerview;
        ImageView iv_search;

        TitleViewHolder(View rootView){
            super(rootView);
            parent_recyclerview = rootView.findViewById(R.id.parent_recyclerview);
            iv_search = rootView.findViewById(R.id.iv_search);
        }
    }

    class DepartmentViewHolder extends RecyclerView.ViewHolder{
        TextView tvFolder;

        DepartmentViewHolder(View rootView){
            super(rootView);
            tvFolder = rootView.findViewById(R.id.tv_department);
        }
    }

    class GroupViewHolder extends RecyclerView.ViewHolder{

        TextView  tvName;

        ImageView ivMonitor;

        ImageView ivMessage;

        View placeholder;

        ImageView iv_group_logo;

        ImageView iv_response_group_icon;

        TextView tv_change_group;

        ImageView iv_current_group;

        GroupViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);

            ivMonitor = itemView.findViewById(R.id.iv_monitor);

            ivMessage = itemView.findViewById(R.id.iv_message);

            placeholder = itemView.findViewById(R.id.placeholder);

            iv_group_logo = itemView.findViewById(R.id.iv_group_logo);
            iv_response_group_icon = itemView.findViewById(R.id.iv_response_group_icon);
            tv_change_group = itemView.findViewById(R.id.tv_change_group);
            iv_current_group = itemView.findViewById(R.id.iv_current_group);
        }
    }

    public void setCatalogItemClickListener(CatalogItemClickListener catalogItemClickListener){
        this.catalogItemClickListener = catalogItemClickListener;
    }

    public interface CatalogItemClickListener{
        void onCatalogItemClick(View view, boolean isTempGroup, int position);
    }

    public void setFolderClickListener(FolderClickListener folderClickListener){
        this.folderClickListener = folderClickListener;
    }
    public interface FolderClickListener{
        void onFolderClick(View view, int depId, String name, boolean isTempGroup);
    }

    public void setMonitorOnClickListener(MonitorOnClickListener monitorOnClickListener){
        this.monitorOnClickListener = monitorOnClickListener;
    }

    public interface MonitorOnClickListener{
        void onMonitorClick(int groupNo);
    }
}

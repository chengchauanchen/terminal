package cn.vsx.vc.fragment;

import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import butterknife.Bind;
import cn.vsx.hamster.common.GroupType;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.SignalServerErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.GroupAndDepartment;
import cn.vsx.hamster.terminalsdk.model.GroupBean;
import cn.vsx.hamster.terminalsdk.model.GroupResponse;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveChangeGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseChangeTempGroupProcessingStateHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseGroupActiveHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateFoldersAndGroupsHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.NewMainActivity;
import cn.vsx.vc.adapter.GroupAdapter;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.model.GroupCatalogBean;
import cn.vsx.vc.utils.CommonGroupUtil;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.DataUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * 群组
 */
public class NewGroupFragment extends BaseFragment{

    @Bind(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.group_recyclerView)
    RecyclerView groupRecyclerView;

    private NewMainActivity mActivity;
    private GroupAdapter groupAdapter;
    private Handler myHandler = new Handler();
    private List<GroupBean> groupBeans = new ArrayList<>();

    //显示在recyclerview上的所有数据
    private List<GroupAndDepartment> allGroupAndDepartment = new ArrayList<>();

    //临时组数据
    private List<Group> tempGroup = new ArrayList<>();

    //上面标题数据
    private List<GroupCatalogBean> mTempCatalogList = new ArrayList<>();
    private List<GroupCatalogBean> mCatalogList = new ArrayList<>();
    private HashMap<Integer, String> idNameMap = TerminalFactory.getSDK().getSerializable(Params.ID_NAME_MAP, new HashMap<Integer, String>());
    //响应组在列表中的位置
    private int responseGroupPosition;
    /**
     * 响应组是否显示
     */
    private ReceiveResponseGroupActiveHandler receiveResponseGroupActiveHandler = new ReceiveResponseGroupActiveHandler(){
        @Override
        public void handler(boolean isActive,int responseGroupId){
            logger.info("ReceiveResponseGroupActiveHandler" + "isActive:"+isActive+",responseGroupPosition:"+responseGroupPosition);
            if(isActive){
                //显示响应组
                Group responseGroup = DataUtil.getGroupByGroupNo(responseGroupId);
                idNameMap.put(responseGroup.getNo(),responseGroup.getName());
                idNameMap.putAll(TerminalFactory.getSDK().getSerializable(Params.ID_NAME_MAP, new HashMap<Integer, String>()));
                TerminalFactory.getSDK().putSerializable(Params.ID_NAME_MAP, idNameMap);
                final GroupAndDepartment<Group> groupAndDepartment = new GroupAndDepartment<>();
                groupAndDepartment.setType(Constants.TYPE_GROUP);
                groupAndDepartment.setBean(responseGroup);
                myHandler.post(new Runnable(){
                    @Override
                    public void run(){
                        allGroupAndDepartment.add(responseGroupPosition,groupAndDepartment);
                        groupAdapter.notifyDataSetChanged();
                        //groupAdapter.notifyItemInserted(responseGroupPosition);
                    }
                });


            }else{
                //隐藏响应组
                if(allGroupAndDepartment.get(responseGroupPosition).getType() == Constants.TYPE_GROUP){
                    Group responseGroup = (Group) allGroupAndDepartment.get(responseGroupPosition).getBean();
                    if(responseGroup.getId() == responseGroupId){
                        idNameMap.remove(responseGroupId);
                        TerminalFactory.getSDK().putSerializable(Params.ID_NAME_MAP, idNameMap);
                        myHandler.post(new Runnable(){
                            @Override
                            public void run(){
                                allGroupAndDepartment.remove(responseGroupPosition);
                                groupAdapter.notifyDataSetChanged();
                                //groupAdapter.notifyItemRemoved(responseGroupPosition);
                            }
                        });

                    }
                }
            }
        }
    };
    /**
     * 更新配置信息
     */
    private ReceiveUpdateConfigHandler receiveUpdateConfigHandler = new ReceiveUpdateConfigHandler(){
        @Override
        public void handler(){//更新当前组
            CommonGroupUtil.setCatchGroupIdList(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0));
            myHandler.post(new Runnable(){
                @Override
                public void run(){
                    GroupResponse mGroupResponse = MyTerminalFactory.getSDK().getConfigManager().getAllGroupInfo();
                    if(mGroupResponse == null){
                        return;
                    }
                    groupBeans.clear();
                    groupBeans.addAll(mGroupResponse.getMemberGroups());
                    mTempCatalogList.clear();
                    mCatalogList.clear();
                    updateData();
                }
            });
        }
    };

    /**
     * 转组消息
     */
    private ReceiveChangeGroupHandler receiveChangeGroupHandler = new ReceiveChangeGroupHandler(){

        @Override
        public void handler(int errorCode, String errorDesc){
            logger.info("收到转组消息："+errorCode+"/"+errorDesc);
            if(errorCode == 0 || errorCode == SignalServerErrorCode.INVALID_SWITCH_GROUP.getErrorCode()){
                CommonGroupUtil.setCatchGroupIdList(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0));
                MyTerminalFactory.getSDK().putParam(Params.IS_CHANGE_GROUP_SUCCEED, true);
                myHandler.post(new Runnable(){
                    @Override
                    public void run(){
                        GroupResponse mGroupResponse = MyTerminalFactory.getSDK().getConfigManager().getAllGroupInfo();
                        if(mGroupResponse == null){
                            return;
                        }
                        groupAdapter.notifyDataSetChanged();
                    }
                });
            }else{
                MyTerminalFactory.getSDK().putParam(Params.IS_CHANGE_GROUP_SUCCEED, false);
                ToastUtil.showToast(MyApplication.instance, errorDesc);
            }
        }
    };


    /**
     * 更新文件夹和组列表数据
     */
    private ReceiveUpdateFoldersAndGroupsHandler receiveUpdateFoldersAndGroupsHandler = new ReceiveUpdateFoldersAndGroupsHandler(){

        @Override
        public void handler(){
            myHandler.post(new Runnable(){
                @Override
                public void run(){
                    GroupResponse mGroupResponse = MyTerminalFactory.getSDK().getConfigManager().getAllGroupInfo();
                    if(mGroupResponse == null){
                        return;
                    }
                    groupBeans.clear();
                    groupBeans.addAll(mGroupResponse.getMemberGroups());
                    mTempCatalogList.clear();
                    mCatalogList.clear();
                    updateData();
                }
            });
        }
    };

    // 警情临时组处理完成，终端需要切到主组，刷新通讯录
    private ReceiveResponseChangeTempGroupProcessingStateHandler receiveResponseChangeTempGroupProcessingStateHandler = new ReceiveResponseChangeTempGroupProcessingStateHandler(){

        @Override
        public void handler(int resultCode,String resultDesc){
            if(resultCode == BaseCommonCode.SUCCESS_CODE){
                TerminalFactory.getSDK().getConfigManager().updateAllGroups();
            }
        }
    };

    @Override
    public int getContentViewId(){
        return R.layout.fragment_all_group;
    }

    @Override
    public void initView(){
        groupRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        groupAdapter = new GroupAdapter(getContext(), allGroupAndDepartment, tempGroup, mTempCatalogList, mCatalogList);
        groupRecyclerView.setAdapter(groupAdapter);
    }

    @Override
    public void initData(){
        mActivity = (NewMainActivity) getActivity();
        GroupResponse mGroupResponse = MyTerminalFactory.getSDK().getConfigManager().getAllGroupInfo();
        if(mGroupResponse == null){
            return;
        }
        groupBeans.clear();
        groupBeans.addAll(mGroupResponse.getMemberGroups());

        mTempCatalogList.clear();
        mCatalogList.clear();
        updateData();

    }

    public void updateData(){
        responseGroupPosition = 0;
        allGroupAndDepartment.clear();
        tempGroup.clear();
        //先添加临时组
        for(GroupBean groupBean : groupBeans){
            if(groupBean.getId() == -1){
                //先添加上面的标题
                GroupAndDepartment<GroupBean> groupAndDepartment = new GroupAndDepartment<>();
                groupAndDepartment.setType(Constants.TYPE_TEMP_TITLE);
                groupAndDepartment.setBean(groupBean);
                allGroupAndDepartment.add(groupAndDepartment);
                responseGroupPosition++;
                //上面标题添加数据
                GroupCatalogBean catalog = new GroupCatalogBean();
                catalog.setName(groupBean.getName());
                catalog.setBean(groupBean);
                mTempCatalogList.add(catalog);
                List<Group> groups = groupBean.getGroups();
                //再添加组
                for(Group group : groups){
                    tempGroup.add(group);
                    GroupAndDepartment<Group> tempGroup = new GroupAndDepartment<>();
                    tempGroup.setType(Constants.TYPE_GROUP);
                    tempGroup.setBean(group);
                    allGroupAndDepartment.add(tempGroup);
                    responseGroupPosition++;
                }
                if(tempGroup.isEmpty()){
                    allGroupAndDepartment.remove(0);
                    responseGroupPosition = 0;
                }
                break;
            }

        }

        //再添加部门组
        for(GroupBean groupBean : groupBeans){
            if(groupBean.getId() != -1){
                //先添加上面的标题
                GroupAndDepartment<GroupBean> groupAndDepartment = new GroupAndDepartment<>();
                groupAndDepartment.setType(Constants.TYPE_TITLE);
                groupAndDepartment.setBean(groupBean);
                allGroupAndDepartment.add(groupAndDepartment);
                responseGroupPosition++;
                //上面标题添加数据
                GroupCatalogBean catalog = new GroupCatalogBean();
                catalog.setName(groupBean.getName());
                catalog.setBean(groupBean);
                mCatalogList.add(catalog);
                List<Group> groups = groupBean.getGroups();
                //再添加组
                for(Group group : groups){
                    //响应组不显示
                    if(group.getGroupType()!= GroupType.RESPONSE){
                        GroupAndDepartment<Group> tempGroup = new GroupAndDepartment<>();
                        tempGroup.setType(Constants.TYPE_GROUP);
                        tempGroup.setBean(group);
                        allGroupAndDepartment.add(tempGroup);
                    }
                }
                //最后添加子部门
                List<GroupBean> children = groupBean.getChildren();
                for(GroupBean child : children){
                    GroupAndDepartment<GroupBean> department = new GroupAndDepartment<>();
                    department.setType(Constants.TYPE_FOLDER);
                    department.setBean(child);
                    allGroupAndDepartment.add(department);
                }
            }
        }

        if(groupAdapter != null){
            groupAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void initListener(){
        MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseGroupActiveHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveChangeGroupHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateFoldersAndGroupsHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseChangeTempGroupProcessingStateHandler);
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.white);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorPrimaryDark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh(){
                TerminalFactory.getSDK().getConfigManager().updateAllGroups();
                myHandler.postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        // 加载完数据设置为不刷新状态，将下拉进度收起来
                        swipeRefreshLayout.setRefreshing(false);
                        // 加载完数据设置为不刷新状态，将下拉进度收起来
                    }
                }, 1200);
            }
        });

        groupAdapter.setCatalogItemClickListener(new GroupAdapter.CatalogItemClickListener(){
            @Override
            public void onCatalogItemClick(View view, boolean isTempGroup, int position){
                GroupBean groupBean;
                if(isTempGroup){
                    groupBean = mTempCatalogList.get(position).getBean();
                }else{
                    groupBean = mCatalogList.get(position).getBean();
                }
                //去掉之前的数据
                Iterator<GroupBean> iterator = groupBeans.iterator();
                while(iterator.hasNext()){
                    GroupBean next = iterator.next();
                    //临时组只有一级，所以这里只会是部门，先去掉之后再添加新的，如果以后临时组有变动再改
                    if(next.getId() != -1){
                        iterator.remove();
                        break;
                    }
                }
                //添加新的数据
                groupBeans.add(groupBean);
                mTempCatalogList.clear();
                List<GroupCatalogBean> groupCatalogBeans = new ArrayList<>();
                groupCatalogBeans.addAll(mCatalogList.subList(0, position));
                mCatalogList.clear();
                mCatalogList.addAll(groupCatalogBeans);
                updateData();
            }
        });
        groupAdapter.setFolderClickListener(new GroupAdapter.FolderClickListener(){
            @Override
            public void onFolderClick(View view, int position, boolean isTempGroup){
                GroupBean groupBean = (GroupBean) allGroupAndDepartment.get(position).getBean();
                //去掉之前的数据
                Iterator<GroupBean> iterator = groupBeans.iterator();
                while(iterator.hasNext()){
                    GroupBean next = iterator.next();
                    if(next.getId() == groupBean.getParentId()){
                        if(isTempGroup){
                            mCatalogList.clear();
                        }else{
                            mTempCatalogList.clear();
                        }
                        iterator.remove();
                        break;
                    }
                }
                //添加新的数据
                groupBeans.add(groupBean);
                updateData();
            }
        });
    }

    @Override
    public void onDestroy(){
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseGroupActiveHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveChangeGroupHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateFoldersAndGroupsHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseChangeTempGroupProcessingStateHandler);
        super.onDestroy();
    }

    /**
     * 返回操作
     */
    public void onBack(){
        if(mCatalogList.size() > 1){

            GroupBean groupBean = mCatalogList.get(mCatalogList.size() - 2).getBean();

            //去掉最后两个
            mCatalogList.remove(mCatalogList.size() - 1);
            mCatalogList.remove(mCatalogList.size() - 1);

            List<GroupCatalogBean> catalogBeanList = new ArrayList<>();
            catalogBeanList.addAll(mCatalogList);

            mTempCatalogList.clear();
            mCatalogList.clear();
            mCatalogList.addAll(catalogBeanList);

            //去掉之前的数据
            Iterator<GroupBean> iterator = groupBeans.iterator();
            while(iterator.hasNext()){
                GroupBean next = iterator.next();
                //临时组只有一级，所以这里只会是部门，先去掉之后再添加新的，如果以后临时组有变动再改
                if(next.getId() != -1){
                    iterator.remove();
                    break;
                }
            }
            groupBeans.add(groupBean);
            updateData();

        }else{
            mActivity.exit();
        }
    }
}

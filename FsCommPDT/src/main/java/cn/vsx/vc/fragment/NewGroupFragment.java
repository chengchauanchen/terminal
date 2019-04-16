package cn.vsx.vc.fragment;

import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.Bind;
import cn.vsx.hamster.common.UserType;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.SignalServerErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Department;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.GroupAndDepartment;
import cn.vsx.hamster.terminalsdk.model.MemberGroupResponse;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveChangeGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseChangeTempGroupProcessingStateHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseGroupActiveHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivegUpdateGroupHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.NewMainActivity;
import cn.vsx.vc.adapter.GroupAdapter;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.model.GroupCatalogBean;
import cn.vsx.vc.utils.CommonGroupUtil;
import cn.vsx.vc.utils.Constants;
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

    //显示在recyclerview上的所有数据
    private List<GroupAndDepartment> datas = new ArrayList<>();
    //临时组的数据
    private List<GroupAndDepartment> tempGroupDatas = new ArrayList<>();
    //普通部门组的数据
    private List<GroupAndDepartment> commonGroupDatas = new ArrayList<>();
    //点进子部门之前的数据
    private List<GroupAndDepartment> lastGroupDatas = new ArrayList<>();

    //临时组数据
    private List<Group> tempGroup = new ArrayList<>();

    private List<GroupCatalogBean> catalogNames = new ArrayList<>();
    private List<GroupCatalogBean> tempCatalogNames = new ArrayList<>();
    //    private HashMap<Integer, String> idNameMap = TerminalFactory.getSDK().getSerializable(Params.ID_NAME_MAP, new HashMap<>());
    private boolean tempGroupUpdateCompleted;
    private boolean groupUpdateCompleted;
    private ReceivegUpdateGroupHandler receivegUpdateGroupHandler = new ReceivegUpdateGroupHandler(){
        @Override
        public void handler(int depId, String depName, List<Department> departments, List<Group> groups){
            updateData( depId,  depName, departments, groups);
        }
    };

    private boolean updateCompleted(){
        return tempGroupUpdateCompleted && groupUpdateCompleted;
    }

    private void sortList(){
        datas.clear();
        datas.addAll(tempGroupDatas);
        datas.addAll(commonGroupDatas);
        myHandler.post(new Runnable(){
            @Override
            public void run(){
                if(groupAdapter !=null){
                    groupAdapter.notifyDataSetChanged();
                    groupRecyclerView.scrollToPosition(0);
                }
            }
        });

    }

    /**
     * 响应组是否显示
     */
    private ReceiveResponseGroupActiveHandler receiveResponseGroupActiveHandler = new ReceiveResponseGroupActiveHandler(){
        @Override
        public void handler(boolean isActive, int responseGroupId){
            logger.info("ReceiveResponseGroupActiveHandler---" + "isActive:" + isActive);
            if(TerminalFactory.getSDK().getParam(Params.USER_TYPE, "").equals(UserType.USER_HIGH.toString())){
                return;
            }
            if(isActive){
            }else{
                //普通用户响应组时间到了，并且当前组是响应组，需要切换到之前的组
                if(TerminalFactory.getSDK().getParam(Params.USER_TYPE).equals(UserType.USER_NORMAL.toString()) && TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0) == responseGroupId){
                    if(TerminalFactory.getSDK().getParam(Params.OLD_CURRENT_GROUP_ID, 0) != 0){
                        TerminalFactory.getSDK().getGroupManager().changeGroup(TerminalFactory.getSDK().getParam(Params.OLD_CURRENT_GROUP_ID, 0));
                    }else{
                        TerminalFactory.getSDK().getGroupManager().changeGroup(TerminalFactory.getSDK().getParam(Params.MAIN_GROUP_ID, 0));
                    }
                }
                myHandler.post(() -> {
                    groupAdapter.notifyDataSetChanged();
                });
            }
        }
    };
    /**
     * 更新配置信息
     */
    private ReceiveUpdateConfigHandler receiveUpdateConfigHandler = () -> {//更新当前组
        CommonGroupUtil.setCatchGroupIdList(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0));
        myHandler.post(() -> {
            //            GroupResponse mGroupResponse = MyTerminalFactory.getSDK().getConfigManager().getAllGroupInfo();
            //            if(mGroupResponse == null){
            //                return;
            //            }
            //            groupBeans.clear();
            //            groupBeans.addAll(mGroupResponse.getMemberGroups());
            //            mTempCatalogList.clear();
            //            mCatalogList.clear();
            //            updateData();
        });
    };

    /**
     * 转组消息
     */
    private ReceiveChangeGroupHandler receiveChangeGroupHandler = new ReceiveChangeGroupHandler(){

        @Override
        public void handler(int errorCode, String errorDesc){
            logger.info("收到转组消息：" + errorCode + "/" + errorDesc);
            if(errorCode == 0 || errorCode == SignalServerErrorCode.INVALID_SWITCH_GROUP.getErrorCode()){
                CommonGroupUtil.setCatchGroupIdList(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0));
                //                myHandler.post(() -> {
                //                    GroupResponse mGroupResponse = MyTerminalFactory.getSDK().getConfigManager().getAllGroupInfo();
                //                    if(mGroupResponse == null){
                //                        return;
                //                    }
                //                    groupAdapter.notifyDataSetChanged();
                //                });
            }else{
                ToastUtil.showToast(MyApplication.instance, errorDesc);
            }
        }
    };

    // 警情临时组处理完成，终端需要切到主组，刷新通讯录
    private ReceiveResponseChangeTempGroupProcessingStateHandler receiveResponseChangeTempGroupProcessingStateHandler = (resultCode, resultDesc) -> {
        if(resultCode == BaseCommonCode.SUCCESS_CODE){
            TerminalFactory.getSDK().getConfigManager().updateAllGroups();
        }
    };

    @Override
    public int getContentViewId(){
        return R.layout.fragment_all_group;
    }

    @Override
    public void initView(){
        groupRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        groupAdapter = new GroupAdapter(getContext(), datas, tempGroup, tempCatalogNames, catalogNames);
        groupRecyclerView.setAdapter(groupAdapter);
    }

    @Override
    public void initData(){
        mActivity = (NewMainActivity) getActivity();

        GroupCatalogBean groupCatalogBean = new GroupCatalogBean(TerminalFactory.getSDK().getParam(Params.DEP_NAME,""),TerminalFactory.getSDK().getParam(Params.DEP_ID,0));
        catalogNames.add(groupCatalogBean);
        MemberGroupResponse tempGroupResponse = TerminalFactory.getSDK().getConfigManager().getTempGroupResponse();
        if(null != tempGroupResponse){
            List<Department> deptList = tempGroupResponse.getDeptList();
            List<Group> groupList = tempGroupResponse.getGroupList();
            updateData(-1,"临时组",deptList,groupList);
        }

        MemberGroupResponse depGroupResponse = TerminalFactory.getSDK().getConfigManager().getDepGroupResponse();
        if(null != depGroupResponse){
            List<Department> deptList = depGroupResponse.getDeptList();
            List<Group> groupList = depGroupResponse.getGroupList();
            updateData(TerminalFactory.getSDK().getParam(Params.DEP_ID,0),TerminalFactory.getSDK().getParam(Params.DEP_NAME,""),deptList,groupList);
        }
    }

    public synchronized void updateData(int depId, String depName, List<Department> departments, List<Group> groups){
        if(depId == -1){
            tempGroup.clear();
            tempGroup.addAll(groups);
            GroupCatalogBean groupCatalogBean = new GroupCatalogBean(depName,depId);
            tempCatalogNames.clear();
            tempCatalogNames.add(groupCatalogBean);
            tempGroupDatas.clear();
            if(!groups.isEmpty()){
                //添加临时组
                GroupAndDepartment<Object> tempTitle = new GroupAndDepartment<>();
                tempTitle.setType(Constants.TYPE_TEMP_TITLE);
                tempTitle.setBean(new Object());
                tempGroupDatas.add(tempTitle);
                for(Group group : groups){
                    GroupAndDepartment<Group> groupAndDepartment = new GroupAndDepartment<>();
                    groupAndDepartment.setType(Constants.TYPE_TEMP_GROUP);
                    groupAndDepartment.setBean(group);
                    tempGroupDatas.add(groupAndDepartment);
                }
            }
            tempGroupUpdateCompleted = true;
        }else{
            //请求一个添加一个部门标题
            commonGroupDatas.clear();
            //先把标题去掉重新添加
            Iterator<GroupAndDepartment> commonGroupIterator = commonGroupDatas.iterator();
            while(commonGroupIterator.hasNext()){
                GroupAndDepartment next = commonGroupIterator.next();
                if(next.getType() == Constants.TYPE_TITLE){
                    commonGroupIterator.remove();
                    break;
                }
            }
            //部门标题
            GroupAndDepartment<Object> Title = new GroupAndDepartment<>();
            Title.setType(Constants.TYPE_TITLE);
            Title.setBean(new Object());
            commonGroupDatas.add(Title);
            //添加组
            for(Group group : groups){
                GroupAndDepartment<Group> groupAndDepartment = new GroupAndDepartment<>();
                groupAndDepartment.setType(Constants.TYPE_GROUP);
                groupAndDepartment.setBean(group);
                commonGroupDatas.add(groupAndDepartment);
            }
            //添加部门
            for(Department department : departments){
                GroupAndDepartment<Department> groupAndDepartment = new GroupAndDepartment<>();
                groupAndDepartment.setType(Constants.TYPE_FOLDER);
                groupAndDepartment.setBean(department);
                commonGroupDatas.add(groupAndDepartment);
            }
            groupUpdateCompleted = true;
        }
        if(updateCompleted()){
            //请求完成排序
            sortList();
        }
    }

    @Override
    public void initListener(){
        MyTerminalFactory.getSDK().registReceiveHandler(receivegUpdateGroupHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseGroupActiveHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveChangeGroupHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseChangeTempGroupProcessingStateHandler);
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.white);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorPrimaryDark);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            groupUpdateCompleted = false;
            tempGroupUpdateCompleted = false;
            catalogNames.clear();
            tempCatalogNames.clear();

            GroupCatalogBean groupCatalogBean = new GroupCatalogBean(TerminalFactory.getSDK().getParam(Params.DEP_NAME,""),TerminalFactory.getSDK().getParam(Params.DEP_ID,0));
            catalogNames.add(groupCatalogBean);
            TerminalFactory.getSDK().getConfigManager().updateAllGroups();
            myHandler.postDelayed(() -> {
                // 加载完数据设置为不刷新状态，将下拉进度收起来
                swipeRefreshLayout.setRefreshing(false);
                // 加载完数据设置为不刷新状态，将下拉进度收起来
            }, 1200);
        });

        groupAdapter.setCatalogItemClickListener((view, isTempGroup, position) -> {
            if(position == catalogNames.size()-2){
                catalogNames.remove(catalogNames.size()-1);
                datas.removeAll(commonGroupDatas);
                commonGroupDatas.clear();
                commonGroupDatas.addAll(lastGroupDatas);
                datas.addAll(lastGroupDatas);
                if(groupAdapter !=null){
                    groupAdapter.notifyDataSetChanged();
                }
                groupRecyclerView.scrollToPosition(0);
            }else {
                //重新请求
                synchronized(NewGroupFragment.this){
                    List<GroupCatalogBean> groupCatalogBeans = new ArrayList<>(catalogNames.subList(0, position + 1));
                    catalogNames.clear();
                    catalogNames.addAll(groupCatalogBeans);
                }
                TerminalFactory.getSDK().getConfigManager().updateGroup(catalogNames.get(position).getId(),catalogNames.get(position).getName());
            }
        });
        groupAdapter.setFolderClickListener((view, depId, name, isTempGroup) -> {
            saveLastGroupData();
            tempGroupUpdateCompleted = true;
            groupUpdateCompleted = false;
            GroupCatalogBean groupCatalogBean = new GroupCatalogBean(name,depId);
            catalogNames.add(groupCatalogBean);
            TerminalFactory.getSDK().getConfigManager().updateGroup(depId,name);
        });
    }

    private void saveLastGroupData(){
        lastGroupDatas.clear();
        for(GroupAndDepartment commonGroupData : commonGroupDatas){
            lastGroupDatas.add((GroupAndDepartment) commonGroupData.clone());
        }
    }

    @Override
    public void onDestroy(){
        MyTerminalFactory.getSDK().unregistReceiveHandler(receivegUpdateGroupHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseGroupActiveHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveChangeGroupHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseChangeTempGroupProcessingStateHandler);
        super.onDestroy();
    }

    /**
     * 返回操作
     */
    public void onBack(){
        if(catalogNames.size() > 1){
            //返回到上一级
            catalogNames.remove(catalogNames.size()-1);
            datas.removeAll(commonGroupDatas);
            commonGroupDatas.clear();
            commonGroupDatas.addAll(lastGroupDatas);
            datas.addAll(lastGroupDatas);
            if(groupAdapter !=null){
                groupAdapter.notifyDataSetChanged();
            }
            groupRecyclerView.scrollToPosition(0);

        }else{
            mActivity.exit();
        }
    }
}

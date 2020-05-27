package cn.vsx.vc.fragment;

import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;

import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.common.GroupChangeType;
import cn.vsx.hamster.common.ResponseGroupType;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.SignalServerErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Department;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.GroupAndDepartment;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveChangeGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyAboutGroupChangeMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseChangeTempGroupProcessingStateHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSetMonitorGroupListHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSetMonitorGroupViewHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateMonitorGroupViewHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivegUpdateGroupHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.NewMainActivity;
import cn.vsx.vc.adapter.GroupAdapter;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.model.CatalogBean;
import cn.vsx.vc.receiveHandle.ReceiverMonitorViewClickHandler;
import cn.vsx.vc.utils.CommonGroupUtil;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.WuTieUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * 群组
 */
public class NewGroupFragment extends BaseFragment{


    SwipeRefreshLayout swipeRefreshLayout;

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
    private SparseBooleanArray currentMonitorGroup = new SparseBooleanArray();
    private int monitorGroupNo;
    private List<CatalogBean> catalogNames = new ArrayList<>();
    private List<CatalogBean> tempCatalogNames = new ArrayList<>();
    //    private HashMap<Integer, String> idNameMap = TerminalFactory.getSDK().getSerializable(Params.ID_NAME_MAP, new HashMap<>());
    private boolean tempGroupUpdateCompleted;
    private boolean groupUpdateCompleted;
    private boolean isHidden = false;
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

    private ReceiveSetMonitorGroupListHandler receiveSetMonitorGroupListHandler = new ReceiveSetMonitorGroupListHandler(){
        @Override
        public void handler(int errorCode, String errorDesc){
            if(errorCode == BaseCommonCode.SUCCESS_CODE){
                //加入监听
                if(currentMonitorGroup.get(monitorGroupNo)){
//                    TerminalFactory.getSDK().getGroupManager().changeGroup(monitorGroupNo);
                    if(null != DataUtil.getTempGroupByGroupNo(monitorGroupNo)&&!TerminalFactory.getSDK().getConfigManager().getTempMonitorGroupNos().contains(monitorGroupNo)){
                        TerminalFactory.getSDK().getConfigManager().getTempMonitorGroupNos().add(monitorGroupNo);
                        TerminalFactory.getSDK().getConfigManager().updateTempMonitorGroup();
                    }
                }else {
                    //移除监听
                    if(monitorGroupNo == TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID,0)){
                        //当前组被移除监听了切换到主组去
                        if(monitorGroupNo != TerminalFactory.getSDK().getParam(Params.MAIN_GROUP_ID,0)){
//                            TerminalFactory.getSDK().getGroupManager().changeGroup(TerminalFactory.getSDK().getParam(Params.MAIN_GROUP_ID,0));
                        }
                    }
                    if(TerminalFactory.getSDK().getConfigManager().getTempMonitorGroupNos().contains(monitorGroupNo)){
                        TerminalFactory.getSDK().getConfigManager().getTempMonitorGroupNos().remove((Integer)monitorGroupNo);
                        TerminalFactory.getSDK().getConfigManager().updateTempMonitorGroup();
                    }

                }
                if(groupAdapter !=null){
                    myHandler.post(()->{
                        groupAdapter.notifyDataSetChanged();
                    });
                }
                //更新对讲页面
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiveSetMonitorGroupViewHandler.class);
                monitorGroupNo = 0;
            }else {
                monitorGroupNo = 0;
                ToastUtil.showToast(getContext(),errorDesc);
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
                myHandler.post(new Runnable(){
                    @Override
                    public void run(){
                        CommonGroupUtil.setCatchGroupIdList(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0));
                        groupAdapter.notifyDataSetChanged();
                    }
                });
            }else{
                ToastUtil.showToast(MyApplication.instance, errorDesc);
            }
        }
    };

    private ReceiveNotifyAboutGroupChangeMessageHandler receiveNotifyAboutGroupChangeMessageHandler = new ReceiveNotifyAboutGroupChangeMessageHandler(){
        @Override
        public void handler(int groupId, int groupChangeType, String groupChangeDesc){
            if(groupChangeType == GroupChangeType.MODIFY_RESPONSE_GROUP_TYPE.getCode()){
                for(GroupAndDepartment data : datas){
                    if(data.getBean() instanceof Group){
                        Group group = (Group) data.getBean();
                        if(group.getNo() == groupId){
                            JSONObject jsonObject = JSONObject.parseObject(groupChangeDesc);
                            int responseGroupType = jsonObject.getIntValue("responseGroupType");
                            ResponseGroupType instanceByCode = ResponseGroupType.getInstanceByCode(responseGroupType);
                            if(null !=instanceByCode){
                                group.setResponseGroupType(instanceByCode.toString());
                            }
                            break;
                        }
                    }
                }
                myHandler.post(()-> groupAdapter.notifyDataSetChanged());
            }
        }
    };

    // 警情临时组处理完成，终端需要切到主组，刷新通讯录
    private ReceiveResponseChangeTempGroupProcessingStateHandler receiveResponseChangeTempGroupProcessingStateHandler = (resultCode, resultDesc) -> {
        if(resultCode == BaseCommonCode.SUCCESS_CODE){
            TerminalFactory.getSDK().getConfigManager().updateAllGroups();
        }
    };

    /**
     * 更新组数据通知
     */
    private ReceiveUpdateMonitorGroupViewHandler receiveUpdateMonitorGroupViewHandler = new ReceiveUpdateMonitorGroupViewHandler(){
        @Override
        public void handler(){
            myHandler.post(() -> {
                groupUpdateCompleted = false;
                tempGroupUpdateCompleted = false;
                catalogNames.clear();
                tempCatalogNames.clear();

                catalogNames.add(WuTieUtil.addRootDetpCatalogNames());
                TerminalFactory.getSDK().getConfigManager().updateAllGroups();
            });
        }
    };

    @Override
    public int getContentViewId(){
        return R.layout.fragment_all_group;
    }

    @Override
    public void initView(){
        swipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipeRefreshLayout);
        groupRecyclerView = (RecyclerView) mRootView.findViewById(R.id.group_recyclerView);
        groupRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        groupAdapter = new GroupAdapter(getContext(), datas, tempGroup, tempCatalogNames, catalogNames);
        groupRecyclerView.setAdapter(groupAdapter);
    }

    @Override
    public void initData(){
        mActivity = (NewMainActivity) getActivity();
        TerminalFactory.getSDK().getConfigManager().updateAllGroups();
        catalogNames.add(WuTieUtil.addRootDetpCatalogNames());
    }

    public synchronized void updateData(int depId, String depName, List<Department> departments, List<Group> groups){
        if(depId == -1){
            tempGroup.clear();
            tempGroup.addAll(groups);
            CatalogBean groupCatalogBean = new CatalogBean(depName,depId);
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
            //部门标题
            GroupAndDepartment<Object> Title = new GroupAndDepartment<>();
            Title.setType(Constants.TYPE_TITLE);
            Title.setBean(new Object());
            commonGroupDatas.add(Title);
            //过滤根部门中的组
            List<Group> groupList = WuTieUtil.filterRootDeptment(depId,groups);
            //添加组
            for(Group group : groupList){
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
        MyTerminalFactory.getSDK().registReceiveHandler(receiverMonitorViewClickHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyAboutGroupChangeMessageHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveSetMonitorGroupListHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receivegUpdateGroupHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveChangeGroupHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseChangeTempGroupProcessingStateHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateMonitorGroupViewHandler);
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.white);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorPrimaryDark);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            groupUpdateCompleted = false;
            tempGroupUpdateCompleted = false;
            catalogNames.clear();
            tempCatalogNames.clear();

            catalogNames.add(WuTieUtil.addRootDetpCatalogNames());
            TerminalFactory.getSDK().getConfigManager().updateAllGroups();
            TerminalFactory.getSDK().getConfigManager().updateAllGroupInfo(true);
            myHandler.postDelayed(() -> {
                // 加载完数据设置为不刷新状态，将下拉进度收起来
                swipeRefreshLayout.setRefreshing(false);
                // 加载完数据设置为不刷新状态，将下拉进度收起来
            }, 1200);
        });

        groupAdapter.setCatalogItemClickListener((view, isTempGroup, position) -> {
//            if(position == catalogNames.size()-2){
//                catalogNames.remove(catalogNames.size()-1);
//                datas.removeAll(commonGroupDatas);
//                commonGroupDatas.clear();
//                commonGroupDatas.addAll(lastGroupDatas);
//                datas.addAll(lastGroupDatas);
//                if(groupAdapter !=null){
//                    groupAdapter.notifyDataSetChanged();
//                }
//                groupRecyclerView.scrollToPosition(0);
//            }else {
                //重新请求
               if(position>=0 && position < catalogNames.size()){
                   synchronized(NewGroupFragment.this){
                       List<CatalogBean> groupCatalogBeans = new ArrayList<>(catalogNames.subList(0, position + 1));
                       catalogNames.clear();
                       catalogNames.addAll(groupCatalogBeans);
                   }
                   TerminalFactory.getSDK().getConfigManager().updateGroup(catalogNames.get(position).getId(),catalogNames.get(position).getName());
                }
//            }
        });
        groupAdapter.setFolderClickListener((view, depId, name, isTempGroup) -> {
            saveLastGroupData();
            tempGroupUpdateCompleted = true;
            groupUpdateCompleted = false;
            CatalogBean groupCatalogBean = new CatalogBean(name,depId);
            if(!catalogNames.contains(groupCatalogBean)){
                catalogNames.add(groupCatalogBean);
            }
            TerminalFactory.getSDK().getConfigManager().updateGroup(depId,name);
        });
    }

    private ReceiverMonitorViewClickHandler receiverMonitorViewClickHandler = new ReceiverMonitorViewClickHandler(){
        @Override
        public void handler(int groupNo){
            //响应组不能取消监听
            Group group = TerminalFactory.getSDK().getGroupByGroupNo(groupNo);
            if(ResponseGroupType.RESPONSE_TRUE.toString().equals(group.getResponseGroupType())){
                ToastUtils.showShort(R.string.response_group_cannot_cancel_monitor);
                return;
            }
            List<Integer> monitorGroups = new ArrayList<>();
            monitorGroups.add(groupNo);
            NewGroupFragment.this.monitorGroupNo = groupNo;

            //如果是当前组，取消当前组
            if(TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID,0) == groupNo){
                ToastUtils.showShort(R.string.current_group_cannot_cancel_monitor);
                //既是当前组又是主组不能取消监听
//                if(TerminalFactory.getSDK().getParam(Params.MAIN_GROUP_ID,0) == groupNo){
//                    ToastUtils.showShort(R.string.main_group_cannot_cancel_monitor);
//                }else {
//                    currentMonitorGroup.put(groupNo,false);
//                    MyTerminalFactory.getSDK().getGroupManager().setMonitorGroup(monitorGroups,false);
//                }

            }else {
                if(null != DataUtil.getTempGroupByGroupNo(groupNo)){
                    //是临时组
                    if(TerminalFactory.getSDK().getConfigManager().getTempMonitorGroupNos().contains(groupNo)){
                        currentMonitorGroup.put(groupNo,false);
                        MyTerminalFactory.getSDK().getGroupManager().setMonitorGroup(monitorGroups,false);
                    }else {
                        currentMonitorGroup.put(groupNo,true);
                        MyTerminalFactory.getSDK().getGroupManager().setMonitorGroup(monitorGroups,true);
                    }
                }else {
                    //不是临时组
                    if(TerminalFactory.getSDK().getConfigManager().getMonitorGroupNo().contains(groupNo)){
                        if(groupNo == TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID,0)){
                            //弹窗提示
                        }
                        currentMonitorGroup.put(groupNo,false);
                        MyTerminalFactory.getSDK().getGroupManager().setMonitorGroup(monitorGroups,false);
                    }else {
                        //判断有没有超过5个监听组
                        if(TerminalFactory.getSDK().getConfigManager().getMonitorGroupNo().size()>=5){
                            ToastUtil.showToast(getContext(),getResources().getString(R.string.monitor_more_than_five));
                        }else {
                            currentMonitorGroup.put(groupNo,true);
                            MyTerminalFactory.getSDK().getGroupManager().setMonitorGroup(monitorGroups,true);
                        }
                    }
                }
            }
        }
    };

    private void saveLastGroupData(){
        lastGroupDatas.clear();
        for(GroupAndDepartment commonGroupData : commonGroupDatas){
            lastGroupDatas.add((GroupAndDepartment) commonGroupData.clone());
        }
    }

    @Override
    public void onDestroy(){
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiverMonitorViewClickHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyAboutGroupChangeMessageHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveSetMonitorGroupListHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receivegUpdateGroupHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveChangeGroupHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseChangeTempGroupProcessingStateHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateMonitorGroupViewHandler);
        super.onDestroy();
    }

    /**
     * 返回操作
     */
    public void onBack(){
        if(catalogNames.size() > 1){
            //返回到上一级
            catalogNames.remove(catalogNames.size()-1);
//            datas.removeAll(commonGroupDatas);
//            commonGroupDatas.clear();
//            commonGroupDatas.addAll(lastGroupDatas);
//            datas.addAll(lastGroupDatas);
            tempGroupUpdateCompleted = true;
            groupUpdateCompleted = false;
            if(groupAdapter !=null){
                groupAdapter.notifyDataSetChanged();
            }
            int position = catalogNames.size()-1;
            TerminalFactory.getSDK().getConfigManager().updateGroup(catalogNames.get(position).getId(),catalogNames.get(position).getName());
            groupRecyclerView.scrollToPosition(0);

        }else{
            mActivity.exit();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        logger.info("NewGroupFragment---onHiddenChanged---hidden:"+hidden);
        isHidden = hidden;
    }

    /**
     * 获取是否是显示或者隐藏的状态
     * @return
     */
    public boolean getHiddenState(){
        return isHidden;
    }
}

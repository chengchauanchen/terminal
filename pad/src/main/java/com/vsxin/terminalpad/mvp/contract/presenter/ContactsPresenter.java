package com.vsxin.terminalpad.mvp.contract.presenter;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.View;

import com.alibaba.fastjson.JSONObject;
import com.ixiaoma.xiaomabus.architecture.mvp.refresh.RefreshPresenter;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.view.IContactsView;
import com.vsxin.terminalpad.mvp.entity.CatalogBean;
import com.vsxin.terminalpad.receiveHandler.ReceiverMonitorViewClickHandler;
import com.vsxin.terminalpad.utils.CommonGroupUtil;
import com.vsxin.terminalpad.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
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
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * @author qzw
 * <p>
 * app模块-通讯录模块
 */
public class ContactsPresenter extends RefreshPresenter<GroupAndDepartment, IContactsView> {
    private static final String TAG = "ContactsPresenter";
    /**
     * 会话列表list,线程安全的
     */
    private List<GroupAndDepartment> datas = Collections.synchronizedList(new ArrayList<>());
    private List<GroupAndDepartment> tempGroupDatas = Collections.synchronizedList(new ArrayList<>());
    private List<GroupAndDepartment> commonGroupDatas = Collections.synchronizedList(new ArrayList<>());

    private boolean tempGroupUpdateCompleted;
    private boolean groupUpdateCompleted;

    private List<CatalogBean> catalogNames = new ArrayList<>();
    private List<CatalogBean> tempCatalogNames = new ArrayList<>();

    private int monitorGroupNo;
    private SparseBooleanArray currentMonitorGroup = new SparseBooleanArray();
    //临时组数据
    private List<Group> tempGroup = new ArrayList<>();

    public ContactsPresenter(Context mContext) {
        super(mContext);
    }

    private boolean updateCompleted() {
        return tempGroupUpdateCompleted && groupUpdateCompleted;
    }


    /**
     * 加载数据
     */
    public void initData() {
        TerminalFactory.getSDK().getConfigManager().updateAllGroups();
        CatalogBean groupCatalogBean = new CatalogBean(TerminalFactory.getSDK().getParam(Params.DEP_NAME, ""), TerminalFactory.getSDK().getParam(Params.DEP_ID, 0));
        catalogNames.add(groupCatalogBean);
    }

    private ReceiverMonitorViewClickHandler receiverMonitorViewClickHandler = new ReceiverMonitorViewClickHandler() {
        @Override
        public void handler(int groupNo) {
            //响应组不能取消监听
            Group group = TerminalFactory.getSDK().getGroupByGroupNo(groupNo);
            if (ResponseGroupType.RESPONSE_TRUE.toString().equals(group.getResponseGroupType())) {
                getView().showMsg(R.string.response_group_cannot_cancel_monitor);
                return;
            }
            List<Integer> monitorGroups = new ArrayList<>();
            monitorGroups.add(groupNo);
            ContactsPresenter.this.monitorGroupNo = groupNo;

            //如果是当前组，取消当前组
            if (TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0) == groupNo) {
                getView().showMsg(R.string.current_group_cannot_cancel_monitor);
                //既是当前组又是主组不能取消监听
//                if(TerminalFactory.getSDK().getParam(Params.MAIN_GROUP_ID,0) == groupNo){
//                    ToastUtils.showShort(R.string.main_group_cannot_cancel_monitor);
//                }else {
//                    currentMonitorGroup.put(groupNo,false);
//                    MyTerminalFactory.getSDK().getGroupManager().setMonitorGroup(monitorGroups,false);
//                }

            } else {
                if (null != DataUtil.getTempGroupByGroupNo(groupNo)) {
                    //是临时组
                    if (TerminalFactory.getSDK().getConfigManager().getTempMonitorGroupNos().contains(groupNo)) {
                        currentMonitorGroup.put(groupNo, false);
                        MyTerminalFactory.getSDK().getGroupManager().setMonitorGroup(monitorGroups, false);
                    } else {
                        currentMonitorGroup.put(groupNo, true);
                        MyTerminalFactory.getSDK().getGroupManager().setMonitorGroup(monitorGroups, true);
                    }
                } else {
                    //不是临时组
                    if (TerminalFactory.getSDK().getConfigManager().getMonitorGroupNo().contains(groupNo)) {
                        if (groupNo == TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)) {
                            //弹窗提示
                        }
                        currentMonitorGroup.put(groupNo, false);
                        MyTerminalFactory.getSDK().getGroupManager().setMonitorGroup(monitorGroups, false);
                    } else {
                        //判断有没有超过5个监听组
                        if (TerminalFactory.getSDK().getConfigManager().getMonitorGroupNo().size() >= 5) {
                            getView().showMsg(R.string.monitor_more_than_five);
                        } else {
                            currentMonitorGroup.put(groupNo, true);
                            MyTerminalFactory.getSDK().getGroupManager().setMonitorGroup(monitorGroups, true);
                        }
                    }
                }
            }
        }
    };

    private ReceiveNotifyAboutGroupChangeMessageHandler receiveNotifyAboutGroupChangeMessageHandler = new ReceiveNotifyAboutGroupChangeMessageHandler() {
        @Override
        public void handler(int groupId, int groupChangeType, String groupChangeDesc) {
            if (groupChangeType == GroupChangeType.MODIFY_RESPONSE_GROUP_TYPE.getCode()) {
                for (GroupAndDepartment data : datas) {
                    if (data.getBean() instanceof Group) {
                        Group group = (Group) data.getBean();
                        if (group.getNo() == groupId) {
                            JSONObject jsonObject = JSONObject.parseObject(groupChangeDesc);
                            int responseGroupType = jsonObject.getIntValue("responseGroupType");
                            ResponseGroupType instanceByCode = ResponseGroupType.getInstanceByCode(responseGroupType);
                            if (null != instanceByCode) {
                                group.setResponseGroupType(instanceByCode.toString());
                            }
                            break;
                        }
                    }
                }
                getView().getAdapter().setCatalogList(tempGroup,tempCatalogNames,catalogNames);
                getView().notifyDataSetChanged(datas, false);
            }
        }
    };

    private ReceiveSetMonitorGroupListHandler receiveSetMonitorGroupListHandler = new ReceiveSetMonitorGroupListHandler() {
        @Override
        public void handler(int errorCode, String errorDesc) {
            if (errorCode == BaseCommonCode.SUCCESS_CODE) {
                //加入监听
                if (currentMonitorGroup.get(monitorGroupNo)) {
//                    TerminalFactory.getSDK().getGroupManager().changeGroup(monitorGroupNo);
                    if (null != DataUtil.getTempGroupByGroupNo(monitorGroupNo) && !TerminalFactory.getSDK().getConfigManager().getTempMonitorGroupNos().contains(monitorGroupNo)) {
                        TerminalFactory.getSDK().getConfigManager().getTempMonitorGroupNos().add(monitorGroupNo);
                        TerminalFactory.getSDK().getConfigManager().updateTempMonitorGroup();
                    }
                } else {
                    //移除监听
                    if (monitorGroupNo == TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)) {
                        //当前组被移除监听了切换到主组去
                        if (monitorGroupNo != TerminalFactory.getSDK().getParam(Params.MAIN_GROUP_ID, 0)) {
//                            TerminalFactory.getSDK().getGroupManager().changeGroup(TerminalFactory.getSDK().getParam(Params.MAIN_GROUP_ID,0));
                        }
                    }
                    if (TerminalFactory.getSDK().getConfigManager().getTempMonitorGroupNos().contains(monitorGroupNo)) {
                        TerminalFactory.getSDK().getConfigManager().getTempMonitorGroupNos().remove((Integer) monitorGroupNo);
                        TerminalFactory.getSDK().getConfigManager().updateTempMonitorGroup();
                    }

                }
                getView().getAdapter().setCatalogList(tempGroup,tempCatalogNames,catalogNames);
                getView().notifyDataSetChanged(datas, false);
                //更新对讲页面
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiveSetMonitorGroupViewHandler.class);
                monitorGroupNo = 0;
            } else {
                monitorGroupNo = 0;
                ToastUtil.showToast(getContext(), errorDesc);
            }
        }
    };

    private ReceivegUpdateGroupHandler receivegUpdateGroupHandler = new ReceivegUpdateGroupHandler() {
        @Override
        public void handler(int depId, String depName, List<Department> departments, List<Group> groups) {
            updateData(depId, depName, departments, groups);
        }
    };

    /**
     * 切组后的消息回调
     */
    private ReceiveChangeGroupHandler receiveChangeGroupHandler = new ReceiveChangeGroupHandler() {
        @Override
        public void handler(final int errorCode, String errorDesc) {
            if(errorCode == 0 || errorCode == SignalServerErrorCode.INVALID_SWITCH_GROUP.getErrorCode()){
                CommonGroupUtil.setCatchGroupIdList(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0));
                getView().notifyDataSetChanged(datas,false);
            }else{
                getView().showMsg(errorDesc);
            }
        }
    };

    /**
     * 更新配置信息
     */
    private ReceiveUpdateConfigHandler receiveUpdateConfigHandler = () -> {//更新当前组
        CommonGroupUtil.setCatchGroupIdList(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0));
    };

    // 警情临时组处理完成，终端需要切到主组，刷新通讯录
    private ReceiveResponseChangeTempGroupProcessingStateHandler receiveResponseChangeTempGroupProcessingStateHandler = (resultCode, resultDesc) -> {
        if (resultCode == BaseCommonCode.SUCCESS_CODE) {
            TerminalFactory.getSDK().getConfigManager().updateAllGroups();
        }
    };

    /**
     * 更新组数据通知
     */
    private ReceiveUpdateMonitorGroupViewHandler receiveUpdateMonitorGroupViewHandler = new ReceiveUpdateMonitorGroupViewHandler() {
        @Override
        public void handler() {
            groupUpdateCompleted = false;
            tempGroupUpdateCompleted = false;
            catalogNames.clear();
            tempCatalogNames.clear();

            CatalogBean groupCatalogBean = new CatalogBean(TerminalFactory.getSDK().getParam(Params.DEP_NAME, ""), TerminalFactory.getSDK().getParam(Params.DEP_ID, 0));
            catalogNames.add(groupCatalogBean);
            TerminalFactory.getSDK().getConfigManager().updateAllGroups();
        }
    };

    /**
     * 下拉刷新
     */
    public void onRefresh(){
        groupUpdateCompleted = false;
        tempGroupUpdateCompleted = false;
        catalogNames.clear();
        tempCatalogNames.clear();

        CatalogBean groupCatalogBean = new CatalogBean(TerminalFactory.getSDK().getParam(Params.DEP_NAME,""),TerminalFactory.getSDK().getParam(Params.DEP_ID,0));
        catalogNames.add(groupCatalogBean);
        TerminalFactory.getSDK().getConfigManager().updateAllGroups();
        TerminalFactory.getSDK().getConfigManager().updateAllGroupInfo(false);
    }


    public synchronized void updateData(int depId, String depName, List<Department> departments, List<Group> groups) {
        if (depId == -1) {
            tempGroup.clear();
            tempGroup.addAll(groups);
            CatalogBean groupCatalogBean = new CatalogBean(depName, depId);
            tempCatalogNames.clear();
            tempCatalogNames.add(groupCatalogBean);
            tempGroupDatas.clear();
            if (!groups.isEmpty()) {
                //添加临时组
                GroupAndDepartment<Object> tempTitle = new GroupAndDepartment<>();
                tempTitle.setType(Constants.TYPE_TEMP_TITLE);
                tempTitle.setBean(new Object());
                tempGroupDatas.add(tempTitle);
                for (Group group : groups) {
                    GroupAndDepartment<Group> groupAndDepartment = new GroupAndDepartment<>();
                    groupAndDepartment.setType(Constants.TYPE_TEMP_GROUP);
                    groupAndDepartment.setBean(group);
                    tempGroupDatas.add(groupAndDepartment);
                }
            }
            tempGroupUpdateCompleted = true;
        } else {
            //请求一个添加一个部门标题
            commonGroupDatas.clear();
            //部门标题
            GroupAndDepartment<Object> Title = new GroupAndDepartment<>();
            Title.setType(Constants.TYPE_TITLE);
            Title.setBean(new Object());
            commonGroupDatas.add(Title);
            //添加组
            for (Group group : groups) {
                GroupAndDepartment<Group> groupAndDepartment = new GroupAndDepartment<>();
                groupAndDepartment.setType(Constants.TYPE_GROUP);
                groupAndDepartment.setBean(group);
                commonGroupDatas.add(groupAndDepartment);
            }
            //添加部门
            for (Department department : departments) {
                GroupAndDepartment<Department> groupAndDepartment = new GroupAndDepartment<>();
                groupAndDepartment.setType(Constants.TYPE_FOLDER);
                groupAndDepartment.setBean(department);
                commonGroupDatas.add(groupAndDepartment);
            }
            groupUpdateCompleted = true;
        }
        if (updateCompleted()) {
            //请求完成排序
            sortList();
        }
    }

    private void sortList() {
        datas.clear();
        datas.addAll(tempGroupDatas);
        datas.addAll(commonGroupDatas);
        getView().getAdapter().setCatalogList(tempGroup,tempCatalogNames,catalogNames);
        getView().notifyDataSetChanged(datas, true);
    }

    public void onCatalogItemClick(View view, boolean isTempGroup, int position){
        //重新请求
        if(position>=0 && position < catalogNames.size()){
            synchronized(ContactsPresenter.this){
                List<CatalogBean> groupCatalogBeans = new ArrayList<>(catalogNames.subList(0, position + 1));
                catalogNames.clear();
                catalogNames.addAll(groupCatalogBeans);
            }
            TerminalFactory.getSDK().getConfigManager().updateGroup(catalogNames.get(position).getId(),catalogNames.get(position).getName());
        }
    }
    public void onFolderClickListener(View view, int depId, String name, boolean isTempGroup){
        tempGroupUpdateCompleted = true;
        groupUpdateCompleted = false;
        CatalogBean groupCatalogBean = new CatalogBean(name,depId);
        if(!catalogNames.contains(groupCatalogBean)){
            catalogNames.add(groupCatalogBean);
        }
        TerminalFactory.getSDK().getConfigManager().updateGroup(depId,name);
    }

    public void registReceiveHandler() {
        MyTerminalFactory.getSDK().registReceiveHandler(receiverMonitorViewClickHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyAboutGroupChangeMessageHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveSetMonitorGroupListHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receivegUpdateGroupHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveChangeGroupHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseChangeTempGroupProcessingStateHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateMonitorGroupViewHandler);
    }

    public void unRegistReceiveHandler() {
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiverMonitorViewClickHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyAboutGroupChangeMessageHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveSetMonitorGroupListHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receivegUpdateGroupHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveChangeGroupHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseChangeTempGroupProcessingStateHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateMonitorGroupViewHandler);
    }
}

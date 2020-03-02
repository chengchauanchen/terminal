package cn.vsx.vc.search;

import android.os.Handler;
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
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.fragment.BaseFragment;
import cn.vsx.vc.receiveHandle.ReceiverMonitorViewClickHandler;
import cn.vsx.vc.utils.CommonGroupUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * @author Administrator
 */
public abstract class BaseSearchFragment extends BaseFragment {

    public SearchAdapter searchAdapter;
    public List<Object> datas = new ArrayList<>();

    /*--------------------------------业务监听--------------------------------------*/

    private Handler myHandler = new Handler();

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

    public void unregistReceiveHandler() {
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiverMonitorViewClickHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyAboutGroupChangeMessageHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveSetMonitorGroupListHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receivegUpdateGroupHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveChangeGroupHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseChangeTempGroupProcessingStateHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateMonitorGroupViewHandler);

    }

    /**
     * 转组消息
     */
    private ReceiveChangeGroupHandler receiveChangeGroupHandler = new ReceiveChangeGroupHandler() {

        @Override
        public void handler(int errorCode, String errorDesc) {
//            if(isHidden){
//                return;
//            }
            logger.info("收到转组消息：" + errorCode + "/" + errorDesc);
            if (errorCode == 0 || errorCode == SignalServerErrorCode.INVALID_SWITCH_GROUP.getErrorCode()) {
                myHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        CommonGroupUtil.setCatchGroupIdList(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0));
                        if (BaseSearchFragment.this instanceof SearchTabFragment){
                            changeGroupSuccess();
                        }
                        searchAdapter.notifyDataSetChanged();
                    }
                });
            } else {
                ToastUtil.showToast(MyApplication.instance, errorDesc);
            }
        }
    };

    public void changeGroupSuccess(){

    };

    private SparseBooleanArray currentMonitorGroup = new SparseBooleanArray();
    private int monitorGroupNo;

    private ReceiverMonitorViewClickHandler receiverMonitorViewClickHandler = new ReceiverMonitorViewClickHandler(){
        @Override
        public void handler(int groupNo){

            if(isHidden){
                return;
            }

            //响应组不能取消监听
            Group group = TerminalFactory.getSDK().getGroupByGroupNo(groupNo);
            if(ResponseGroupType.RESPONSE_TRUE.toString().equals(group.getResponseGroupType())){
                ToastUtils.showShort(R.string.response_group_cannot_cancel_monitor);
                return;
            }
            List<Integer> monitorGroups = new ArrayList<>();
            monitorGroups.add(groupNo);
            BaseSearchFragment.this.monitorGroupNo = groupNo;

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
                            logger.info(getResources().getString(R.string.monitor_more_than_five));
                            ToastUtil.toast(getActivity(),getResources().getString(R.string.monitor_more_than_five));
                        }else {
                            currentMonitorGroup.put(groupNo,true);
                            MyTerminalFactory.getSDK().getGroupManager().setMonitorGroup(monitorGroups,true);
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
                for (Object obj:datas){
                    if(obj instanceof  Group){
                        Group group  =  (Group)obj;
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
                myHandler.post(() -> searchAdapter.notifyDataSetChanged());
            }
        }
    };


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
                    }else if(TerminalFactory.getSDK().getConfigManager().getMonitorGroupNo().contains(monitorGroupNo)){
                        TerminalFactory.getSDK().getConfigManager().getMonitorGroupNo().remove((Integer)monitorGroupNo);
                        TerminalFactory.getSDK().getConfigManager().saveMonitorGroup();
                    }
                }
                if(searchAdapter !=null){
                    myHandler.post(()->{
                        searchAdapter.notifyDataSetChanged();
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

    private ReceivegUpdateGroupHandler receivegUpdateGroupHandler = new ReceivegUpdateGroupHandler(){
        @Override
        public void handler(int depId, String depName, List<Department> departments, List<Group> groups){
            //更新临时组的数据
            // updateData( depId,  depName, departments, groups);
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
//                groupUpdateCompleted = false;
//                tempGroupUpdateCompleted = false;
//                catalogNames.clear();
//                tempCatalogNames.clear();
//
//                catalogNames.add(WuTieUtil.addRootDetpCatalogNames());
//                TerminalFactory.getSDK().getConfigManager().updateAllGroups();
            });
        }
    };

    protected boolean isHidden = false;

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (this instanceof SearchTabFragment){
            logger.info("SearchTabFragment hidden:"+hidden);
        }else if(this instanceof SearchTabGroupFragment){
            logger.info("SearchTabGroupFragment hidden:"+hidden);
        }
        isHidden=hidden;
    }
}

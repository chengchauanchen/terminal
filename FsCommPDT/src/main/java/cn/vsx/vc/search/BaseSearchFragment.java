package cn.vsx.vc.search;

import android.os.Handler;

import com.alibaba.fastjson.JSONObject;

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
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateMonitorGroupViewHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivegUpdateGroupHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.fragment.BaseFragment;
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
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyAboutGroupChangeMessageHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receivegUpdateGroupHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveChangeGroupHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseChangeTempGroupProcessingStateHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateMonitorGroupViewHandler);
    }

    public void unregistReceiveHandler() {
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyAboutGroupChangeMessageHandler);
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
}

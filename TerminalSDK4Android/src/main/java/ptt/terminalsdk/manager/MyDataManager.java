package ptt.terminalsdk.manager;

import com.alibaba.fastjson.JSONObject;
import com.allen.library.manage.RxUrlManager;
import com.allen.library.observer.CommonObserver;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.vsx.hamster.common.RequestDataType;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.data.DataManager;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import io.reactivex.schedulers.Schedulers;
import ptt.terminalsdk.bean.DepData;
import ptt.terminalsdk.bean.GroupBean;
import ptt.terminalsdk.manager.http.AppUrlConfig;
import ptt.terminalsdk.manager.http.api.ApiManager;
import ptt.terminalsdk.receiveHandler.ReceiveUpdateDepGroupHandler;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/7/5
 * 描述：
 * 修订历史：
 */
public class MyDataManager extends DataManager{

    private Logger logger = Logger.getLogger(getClass());
    private static List<GroupBean> depAllGroup = new ArrayList<>();

    @Override
    protected void setUrl(JSONObject resultJsonObject){
        super.setUrl(resultJsonObject);
        //认证成功之后配置BaseUrl
        AppUrlConfig.setFileServerUrl("http://" + TerminalFactory.getSDK().getParam(Params.FILE_SERVER_IP) + ":" + TerminalFactory.getSDK().getParam(Params.FILE_SERVER_PORT, 0));
        RxUrlManager.getInstance().setMultipleUrl(AppUrlConfig.getAllUrl());
    }

    /**
     * 更新当前组部门下的所有组
     */
    public void updateDepAllGroup(){
        int currentGroupNo = TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);
        Group currentGroup = DataUtil.getGroupByGroupNo(currentGroupNo);
        logger.info("updateDepAllGroup---"+currentGroup);
        //查询所在部门的组
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("type", RequestDataType.DEPARTMENTS_GROUPS_DATA.toString());
        paramsMap.put("memberId", String.valueOf(TerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO, 0L)));
        paramsMap.put("id", String.valueOf(currentGroup.getDeptId()));
        ApiManager.getFileServerApi()
                .getDeptData(paramsMap)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new CommonObserver<DepData>(){
                    @Override
                    protected void onError(String errorMsg){
                        logger.info("请求当前组部门下的所有组失败"+errorMsg);
                    }

                    @Override
                    protected void onSuccess(DepData depData){
                        List<GroupBean> groupList = depData.getMemberGroups().getGroupList();
                        depAllGroup.clear();
                        depAllGroup.addAll(groupList);
                        TerminalFactory.getSDK().notifyReceiveHandler(ReceiveUpdateDepGroupHandler.class,groupList);
                    }
                });
    }

    public static List<GroupBean> getDepAllGroup(){
        return depAllGroup;
    }
}

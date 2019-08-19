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
import cn.vsx.hamster.terminalsdk.tools.Params;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ptt.terminalsdk.BuildConfig;
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
    private boolean uavVoiceOpen = true;

    @Override
    protected void setUrl(JSONObject resultJsonObject){
        super.setUrl(resultJsonObject);
        // TODO: 2019/8/5 海康摄像头地址暂时写在build.gradle 里
        TerminalFactory.getSDK().putParam(Params.HIKVISION_CAMERADATA_URL, BuildConfig.HIKVISIONCAMERADATAURL);
        //认证成功之后配置BaseUrl
        AppUrlConfig.setFileServerUrl("http://" + TerminalFactory.getSDK().getParam(Params.FILE_SERVER_IP) + ":" + TerminalFactory.getSDK().getParam(Params.FILE_SERVER_PORT, 0));
        RxUrlManager.getInstance().setMultipleUrl(AppUrlConfig.getAllUrl());
    }

    /**
     * 更新当前组部门下的所有组
     */
    @Override
    public void updateDepAllGroup(){
        //查询所在部门的组
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("type", RequestDataType.DEPARTMENTS_GROUPS_DATA.toString());
        paramsMap.put("memberId", String.valueOf(TerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO, 0L)));
        paramsMap.put("id", String.valueOf(TerminalFactory.getSDK().getParam(Params.CURRENT_DEP_ID,0L)));

        ApiManager.getFileServerApi()
                .getDeptData(paramsMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CommonObserver<DepData>(){
                    @Override
                    protected void onError(String errorMsg){
                        logger.info("请求当前组部门下的所有组失败"+errorMsg);
                    }

                    @Override
                    protected void onSuccess(DepData depData){
                        logger.info("请求当前组部门下的所有组:"+depData);
                        if(depData != null && depData.getMemberGroups() !=null){
                            List<GroupBean> groupList = depData.getMemberGroups().getGroupList();
                            depAllGroup.clear();
                            depAllGroup.addAll(groupList);
                            TerminalFactory.getSDK().notifyReceiveHandler(ReceiveUpdateDepGroupHandler.class,groupList);
                        }else {
                            logger.info("请求当前组部门下的所有组数据为null");
                        }
                    }
                });

    }

    public static List<GroupBean> getDepAllGroup(){
        return depAllGroup;
    }

    /**
     * 在账号改变的时候，删除相关的数据,比如（sp中会存储之前的当前组信息，改变账号的时候需要删除）
     */
    @Override
    public void clearDataByAccountChanged(){
        logger.info("在账号改变的时候，删除相关的数据:");
        //删除之前账号的当前组
        TerminalFactory.getSDK().putParam(Params.CURRENT_GROUP_ID, 0);
    }

    @Override
    public boolean isUavVoiceOpen(){
        return uavVoiceOpen;
    }

    @Override
    public void setUavVoiceOpen(boolean uavVoiceOpen){
        this.uavVoiceOpen = uavVoiceOpen;
    }
}

package com.vsxin.terminalpad.js;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.Gson;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.manager.PullLiveManager;
import com.vsxin.terminalpad.mvp.contract.constant.TerminalEnum;
import com.vsxin.terminalpad.mvp.entity.CarBean;
import com.vsxin.terminalpad.mvp.entity.PatrolBean;
import com.vsxin.terminalpad.mvp.entity.PersonnelBean;
import com.vsxin.terminalpad.mvp.entity.TerminalBean;
import com.vsxin.terminalpad.mvp.ui.fragment.CarOrPatrolInfoFragment;
import com.vsxin.terminalpad.mvp.ui.fragment.GroupMessageFragment;
import com.vsxin.terminalpad.mvp.ui.fragment.PoliceInfoFragment;
import com.vsxin.terminalpad.mvp.ui.fragment.TerminalInfoFragment;
import com.vsxin.terminalpad.receiveHandler.ReceiverMonitorViewClickHandler;
import com.vsxin.terminalpad.utils.FragmentManage;
import com.vsxin.terminalpad.utils.NumberUtil;

import java.util.logging.Logger;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.tools.ToastUtil;

import static com.alibaba.fastjson.JSON.parseObject;

/**
 * @author 地图web与原生交互
 */
public class TerminalPadJs {
    private Logger logger = Logger.getLogger(this.getClass().getName());

    private Context context;

    public TerminalPadJs(Context context) {
        this.context = context;
    }

    /**
     * 点击单个气泡，打开成员详情
     *
     * //32010000001320000114
     *
     * @param memberInfo
     */
    @JavascriptInterface
    public void memberInfo(String memberInfo) {
        JSONObject jsonObject = parseObject(memberInfo);
        if (jsonObject == null) {
            Log.i("气泡-memberInfo:", "jsonObject==null");
            return;
        }
        //String type = jsonObject.getString("type");
        String terminalType = jsonObject.getString("terminalType");
//        if (!TextUtils.isEmpty(type)) {//TODO 由于李翔穿给我的Type与接口返回的不一致,
//            terminalType = type;
//        }
        Log.i("气泡-memberInfo:", memberInfo);
        try {
            TerminalEnum terminalEnum = TerminalEnum.valueOf(terminalType);

            if (terminalEnum == null) {
                return;
            }
            //List<BindBean> bindBeans = gson.fromJson(deviceJson, new TypeToken<List<BindBean>>() {}.getType());

            switch (terminalEnum){
                case TERMINAL_PATROL://船
                    PatrolBean patrolBean = new Gson().fromJson(memberInfo, PatrolBean.class);
                    CarOrPatrolInfoFragment.startCarBoatInfoFragment((FragmentActivity) context, patrolBean, terminalEnum);
                    break;
                case TERMINAL_CAR://车
                    CarBean carBean = new Gson().fromJson(memberInfo, CarBean.class);
                    CarOrPatrolInfoFragment.startCarBoatInfoFragment((FragmentActivity) context, carBean, terminalEnum);
                    break;
                case TERMINAL_PERSONNEL://民警
                    PersonnelBean personnelBean = new Gson().fromJson(memberInfo, PersonnelBean.class);
                    PoliceInfoFragment.startPoliceInfoFragment((FragmentActivity) context, personnelBean, terminalEnum);
                    break;
                default://其它终端设备
                    TerminalBean terminalBean = new Gson().fromJson(memberInfo, TerminalBean.class);
                    TerminalInfoFragment.startTerminalInfoFragment((FragmentActivity) context, terminalBean, terminalEnum);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("数据解析异常");
        }
    }

    @JavascriptInterface
    public void playCityCamera(String gb28181 ) {
        logger.info(gb28181);
        //Todo 点击地图 城市摄像头观看视频监控
        PullLiveManager liveManager = new PullLiveManager(context);
        liveManager.pullVideo("1", TerminalEnum.TERMINAL_CAMERA, gb28181);
    }

    /**
     * 切组
     *
     * @param groupNoStr
     */
    @JavascriptInterface
    public void changeGroup(String groupNoStr) {
        int groupNo = NumberUtil.strToInt(groupNoStr);
        if (TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0) != groupNo) {
            TerminalFactory.getSDK().getGroupManager().changeGroup(groupNo);
        }
        Group group = TerminalFactory.getSDK().getGroupByGroupNo(groupNo);
        FragmentManage.startFragment((FragmentActivity) context, GroupMessageFragment.newInstance(groupNo, group.getName(),0));
    }

    /**
     * 监听或取消监听
     *
     * @param groupNoStr
     */
    @JavascriptInterface
    public void monitor(String groupNoStr) {
        int groupNo = NumberUtil.strToInt(groupNoStr);
        if (TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0) == groupNo) {
            ToastUtil.showToast(R.string.current_group_cannot_cancel_monitor);
        } else {
            TerminalFactory.getSDK().notifyReceiveHandler(ReceiverMonitorViewClickHandler.class, groupNo);
        }
    }
}

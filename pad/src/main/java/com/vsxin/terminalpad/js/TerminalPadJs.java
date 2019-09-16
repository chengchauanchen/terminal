package com.vsxin.terminalpad.js;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.google.gson.Gson;
import com.vsxin.terminalpad.mvp.contract.constant.TerminalEnum;
import com.vsxin.terminalpad.mvp.contract.constant.TerminalType;
import com.vsxin.terminalpad.mvp.entity.CarBean;
import com.vsxin.terminalpad.mvp.entity.PatrolBean;
import com.vsxin.terminalpad.mvp.entity.PersonnelBean;
import com.vsxin.terminalpad.mvp.entity.TerminalBean;
import com.vsxin.terminalpad.mvp.ui.fragment.CarOrPatrolInfoFragment;
import com.vsxin.terminalpad.mvp.ui.fragment.PoliceInfoFragment;
import com.vsxin.terminalpad.mvp.ui.fragment.TerminalInfoFragment;

import java.util.logging.Logger;

/**
 * @author 地图web与原生交互
 */
public class TerminalPadJs {
    private Logger logger = Logger.getLogger(this.getClass().getName());

    private Context context;

    public TerminalPadJs(Context context) {
        this.context = context;
    }


    public static final int PHONE = 1;//警务通
    public static final int LTE = 2;//LTE
    public static final int CAMERA = 3;//摄像头
    public static final int HAND = 4;//电台
    public static final int CAR = 5;//警车
    public static final int VIDEO = 6;//执法仪
    public static final int UAV = 7;//无人机
    public static final int POLICE = 8;//警员
    public static final int PATROL = 9;//巡逻船
    public static final int BALL = 10;//布控球


    /**
     * 点击单个气泡，打开成员详情
     *
     * @param memberInfo
     */
    @JavascriptInterface
    public void memberInfo(String memberInfo, String terminalType) {
        Log.i("气泡-memberInfo:", memberInfo);
        TerminalEnum terminalEnum = null;

        /**
         *     //上图终端
         *     TERMINAL_PHONE("警务通终端", "TERMINAL_PHONE", R.mipmap.img_phone),
         *     TERMINAL_BODY_WORN_CAMERA("执法记录仪", "TERMINAL_BODY_WORN_CAMERA", R.mipmap.img_recorder),
         *     TERMINAL_UAV("无人机", "TERMINAL_UAV", R.mipmap.img_uav),
         *     TERMINAL_PDT("PDT终端(350M)", "TERMINAL_PDT", R.mipmap.img_hand),
         *     TERMINAL_LTE("LTE终端", "TERMINAL_LTE", R.mipmap.img_lte),
         *     TERMINAL_BULL("布控球", "TERMINAL_BULL", R.mipmap.img_lte_cammera),
         *     TERMINAL_CAMERA("摄像头", "TERMINAL_CAMERA", R.mipmap.img_cammera),
         *     TERMINAL_PATROL("船", "patrol", R.mipmap.img_boat),
         *     TERMINAL_CAR("警车", "car", R.mipmap.call_connected),
         *     TERMINAL_PERSONNEL("民警", "personnel", R.mipmap.img_police);
         */


        switch (terminalType) {
            case TerminalType.TERMINAL_PHONE://警务通终端
                terminalEnum = TerminalEnum.TERMINAL_PHONE;
                break;
            case TerminalType.TERMINAL_BODY_WORN_CAMERA://执法记录仪
                terminalEnum = TerminalEnum.TERMINAL_BODY_WORN_CAMERA;
                break;
            case TerminalType.TERMINAL_UAV://无人机
                terminalEnum = TerminalEnum.TERMINAL_UAV;
                break;
            case TerminalType.TERMINAL_PDT://PDT终端(350M)
                terminalEnum = TerminalEnum.TERMINAL_PDT;
                break;
            case TerminalType.TERMINAL_LTE://LTE终端
                terminalEnum = TerminalEnum.TERMINAL_LTE;
                break;
            case TerminalType.TERMINAL_BULL://布控球
                terminalEnum = TerminalEnum.TERMINAL_BULL;
                break;
            case TerminalType.TERMINAL_CAMERA://摄像头
                terminalEnum = TerminalEnum.TERMINAL_CAMERA;
                break;

            //载具
            case TerminalType.TERMINAL_PATROL://船
                terminalEnum = TerminalEnum.TERMINAL_PATROL;
                break;
            case TerminalType.TERMINAL_CAR://警车
                terminalEnum = TerminalEnum.TERMINAL_CAR;
                break;
            case TerminalType.TERMINAL_PERSONNEL://民警
                terminalEnum = TerminalEnum.TERMINAL_PERSONNEL;
                break;
            default:
                terminalEnum = null;
                break;
        }

        Gson gson = new Gson();

        try {
            if(terminalEnum==null){
                return;
            }
            //List<BindBean> bindBeans = gson.fromJson(deviceJson, new TypeToken<List<BindBean>>() {}.getType());

            if(terminalType ==TerminalType.TERMINAL_PATROL){//船
                PatrolBean patrolBean = new Gson().fromJson(memberInfo, PatrolBean.class);
                CarOrPatrolInfoFragment.startCarBoatInfoFragment((FragmentActivity) context,patrolBean,terminalEnum);
            }else if(terminalType ==TerminalType.TERMINAL_CAR){//警车
                CarBean carBean = new Gson().fromJson(memberInfo, CarBean.class);
                CarOrPatrolInfoFragment.startCarBoatInfoFragment((FragmentActivity) context,carBean,terminalEnum);
            }else if(terminalType ==TerminalType.TERMINAL_PERSONNEL){//民警
                PersonnelBean personnelBean = new Gson().fromJson(memberInfo, PersonnelBean.class);
                PoliceInfoFragment.startPoliceInfoFragment((FragmentActivity) context,personnelBean,terminalEnum);
            }else{//终端设备
                TerminalBean terminalBean = new Gson().fromJson(memberInfo, TerminalBean.class);
                TerminalInfoFragment.startTerminalInfoFragment((FragmentActivity) context, terminalBean, terminalEnum);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("数据解析异常");
        }

    }
}

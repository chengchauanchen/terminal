package com.vsxin.terminalpad.utils;

import android.view.View;
import android.widget.ImageView;

import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.manager.operation.OperationConstants;
import com.vsxin.terminalpad.mvp.contract.constant.TerminalEnum;
import com.vsxin.terminalpad.mvp.entity.TerminalBean;

import java.util.HashMap;
import java.util.Map;

/**
 * 上图设备 工具类
 */
public class TerminalUtils {

    //个呼:警务通终端,PDT终端(350M)
    //视频:警务通终端,执法记录仪,无人机,LTE终端,布控球,摄像头


    /**
     * 获取个呼UniqueNo
     *
     * @param terminalBean
     * @return
     */
    public static String getIndividualCallUniqueNo(TerminalBean terminalBean) {
        String uniqueNo = "";
        TerminalEnum terminalEnum = TerminalEnum.valueOf(terminalBean.getTerminalType());

        switch (terminalEnum) {
            case TERMINAL_PHONE://警务通
                uniqueNo = terminalBean.getTerminalUniqueNo();
                break;
            case TERMINAL_PDT://手台
                uniqueNo = terminalBean.getPdtNo();
                break;
            default:
                break;
        }
        return uniqueNo;
    }

    /**
     * 该类型设备是否允许个呼
     *
     * @return
     */
    public static Boolean isAllowIndividualCall(TerminalBean terminalBean) {
        Boolean isAllow = false;
        TerminalEnum terminalEnum = TerminalEnum.valueOf(terminalBean.getTerminalType());
        switch (terminalEnum) {
            case TERMINAL_PHONE://警务通
                isAllow = true;
                break;
            case TERMINAL_PDT://手台
                isAllow = true;
                break;
            default:
                break;
        }
        return isAllow;
    }

    /**
     * 该类型设备是否允许拉视频
     *
     * @return
     */
    public static Boolean isAllowPullLive(TerminalBean terminalBean) {
        Boolean isAllow = false;
        TerminalEnum terminalEnum = TerminalEnum.valueOf(terminalBean.getTerminalType());

        switch (terminalEnum) {
            case TERMINAL_PHONE://警务通
                isAllow = true;
                break;
            case TERMINAL_BODY_WORN_CAMERA://执法仪
                isAllow = true;
                break;
            case TERMINAL_UAV://无人机
                isAllow = true;
                break;
            case TERMINAL_LTE://lte
                isAllow = true;
                break;
            case TERMINAL_BALL://不控球
                isAllow = true;
                break;
            case TERMINAL_CAMERA://城市摄像头
                isAllow = true;
                break;
            default:
                isAllow = false;
                break;
        }
        return isAllow;
    }

    /**
     * 获取可以视频上报设备的UniqueNo
     *
     * @return
     */
    public static String getPullLiveUniqueNo(TerminalBean terminalBean) {
        String uniqueNo = "";
        TerminalEnum terminalEnum = TerminalEnum.valueOf(terminalBean.getTerminalType());

        switch (terminalEnum) {
            case TERMINAL_PHONE://警务通
                uniqueNo = terminalBean.getTerminalUniqueNo();
                break;
            case TERMINAL_BODY_WORN_CAMERA://执法仪
                uniqueNo = terminalBean.getTerminalUniqueNo();
                break;
            case TERMINAL_UAV://无人机
                uniqueNo = terminalBean.getTerminalUniqueNo();
                break;
            case TERMINAL_LTE://lte
                uniqueNo = terminalBean.getGb28181No();
                break;
            case TERMINAL_BALL://不控球
                uniqueNo = terminalBean.getGb28181No();
                break;
            case TERMINAL_CAMERA://城市摄像头
                uniqueNo = terminalBean.getGb28181No();
                break;
            default:
                break;
        }
        return uniqueNo;
    }


    public static void showOperate(ImageView[] imageRid, String terminalType) {
        Map<String, Boolean> operates = getOperationForTerminalType(terminalType);
        for (String key : operates.keySet()) {//keySet获取map集合key的集合  然后在遍历key即可
            Boolean value = operates.get(key);
            switch (key) {
                case OperationConstants.CALL_PHONE:
                    imageRid[0].setVisibility(value ? View.VISIBLE : View.GONE);
                    break;
                case OperationConstants.MESSAGE:
                    imageRid[1].setVisibility(value ? View.VISIBLE : View.GONE);
                    break;
                case OperationConstants.LIVE:
                    imageRid[2].setVisibility(value ? View.VISIBLE : View.GONE);
                    break;
                case OperationConstants.INDIVIDUAL_CALL:
                    imageRid[3].setVisibility(value ? View.VISIBLE : View.GONE);
                    break;
                default:
                    break;
            }
        }
    }

    public static Map<String, Boolean> getOperationForTerminalType(String terminalType) {
        Map<String, Boolean> operatShow = new HashMap<>();
        TerminalEnum terminalEnum = TerminalEnum.valueOf(terminalType);

        switch (terminalEnum) {
            case TERMINAL_PHONE://警务通终端 直播\个呼
                operatShow.put(OperationConstants.CALL_PHONE, false);//打电话
                operatShow.put(OperationConstants.MESSAGE, false);//消息
                operatShow.put(OperationConstants.LIVE, true);//直播
                operatShow.put(OperationConstants.INDIVIDUAL_CALL, true);//个呼
                break;
            case TERMINAL_BODY_WORN_CAMERA://执法记录仪 直播
                operatShow.put(OperationConstants.CALL_PHONE, false);
                operatShow.put(OperationConstants.MESSAGE, false);
                operatShow.put(OperationConstants.LIVE, true);
                operatShow.put(OperationConstants.INDIVIDUAL_CALL, false);
                break;
            case TERMINAL_UAV://无人机 直播
                operatShow.put(OperationConstants.CALL_PHONE, false);
                operatShow.put(OperationConstants.MESSAGE, false);
                operatShow.put(OperationConstants.LIVE, true);
                operatShow.put(OperationConstants.INDIVIDUAL_CALL, false);
                break;
            case TERMINAL_PDT://PDT终端(350M) 个呼
                operatShow.put(OperationConstants.CALL_PHONE, false);
                operatShow.put(OperationConstants.MESSAGE, false);
                operatShow.put(OperationConstants.LIVE, false);
                operatShow.put(OperationConstants.INDIVIDUAL_CALL, true);
                break;
            case TERMINAL_DDT://交管电台
                operatShow.put(OperationConstants.CALL_PHONE, false);
                operatShow.put(OperationConstants.MESSAGE, false);
                operatShow.put(OperationConstants.LIVE, false);
                operatShow.put(OperationConstants.INDIVIDUAL_CALL, false);//暂时还不通,先掩个呼藏按钮
                break;
            case TERMINAL_PDT_CAR://350M车载台 个呼
                operatShow.put(OperationConstants.CALL_PHONE, false);
                operatShow.put(OperationConstants.MESSAGE, false);
                operatShow.put(OperationConstants.LIVE, false);
                operatShow.put(OperationConstants.INDIVIDUAL_CALL, true);
                break;
            case TERMINAL_LTE://LTE终端 直播
                operatShow.put(OperationConstants.CALL_PHONE, false);
                operatShow.put(OperationConstants.MESSAGE, false);
                operatShow.put(OperationConstants.LIVE, true);
                operatShow.put(OperationConstants.INDIVIDUAL_CALL, false);
                break;
            case TERMINAL_BALL://布控球 直播
                operatShow.put(OperationConstants.CALL_PHONE, false);
                operatShow.put(OperationConstants.MESSAGE, false);
                operatShow.put(OperationConstants.LIVE, true);
                operatShow.put(OperationConstants.INDIVIDUAL_CALL, false);
                break;
            case TERMINAL_CAMERA://摄像头 直播
                operatShow.put(OperationConstants.CALL_PHONE, false);
                operatShow.put(OperationConstants.MESSAGE, false);
                operatShow.put(OperationConstants.LIVE, true);
                operatShow.put(OperationConstants.INDIVIDUAL_CALL, false);
                break;
            case TERMINAL_PERSONNEL://民警 直播 个呼
                operatShow.put(OperationConstants.CALL_PHONE, false);
                operatShow.put(OperationConstants.MESSAGE, false);
                operatShow.put(OperationConstants.LIVE, true);
                operatShow.put(OperationConstants.INDIVIDUAL_CALL, true);
                break;
            default:
                break;
        }
        return operatShow;
    }

    /**
     * 通过终端设备类型 获取 对应图标
     *
     * @param terminalType
     * @return
     */
    public static int getDialogImageForTerminalType(String terminalType) {
        int imgRid = 0;
        TerminalEnum terminalEnum = TerminalEnum.valueOf(terminalType);

        switch (terminalEnum) {
            case TERMINAL_PHONE://警务通终端
                imgRid = R.mipmap.ic_phone_dialog;
                break;
            case TERMINAL_BODY_WORN_CAMERA://执法记录仪
                imgRid = R.mipmap.ic_zfy_dialog;
                break;
            case TERMINAL_UAV://无人机
                imgRid = R.mipmap.ic_uav_dialog;
                break;
            case TERMINAL_PDT://PDT终端(350M)
                imgRid = R.mipmap.ic_pdt_dialog;
                break;
            case TERMINAL_LTE://lte
                imgRid = R.mipmap.ic_lte_dialog;
                break;
            default:
                break;
        }
        return imgRid;
    }

    /**
     * 通过终端设备类型 获取 对应图标
     *
     * @param terminalType
     * @return
     */
    public static int getImageForTerminalType(String terminalType) {
        TerminalEnum terminalEnum = TerminalEnum.valueOf(terminalType);

//        switch (terminalEnum) {
//            case TERMINAL_PHONE://警务通终端
//                terminalEnum = TerminalEnum.TERMINAL_PHONE;
//                break;
//            case TERMINAL_BODY_WORN_CAMERA://执法记录仪
//                terminalEnum = TerminalEnum.TERMINAL_BODY_WORN_CAMERA;
//                break;
//            case TERMINAL_UAV://无人机
//                terminalEnum = TerminalEnum.TERMINAL_UAV;
//                break;
//            case TERMINAL_PDT://PDT终端(350M)
//                terminalEnum = TerminalEnum.TERMINAL_PDT;
//                break;
//            case TERMINAL_LTE://LTE终端
//                terminalEnum = TerminalEnum.TERMINAL_LTE;
//                break;
//            case TERMINAL_BALL://布控球
//                terminalEnum = TerminalEnum.TERMINAL_BALL;
//                break;
//            case TERMINAL_CAMERA://摄像头
//                terminalEnum = TerminalEnum.TERMINAL_CAMERA;
//                break;
//
//            //载具
//            case TERMINAL_PATROL://船
//                terminalEnum = TerminalEnum.TERMINAL_PATROL;
//                break;
//            case TERMINAL_CAR://警车
//                terminalEnum = TerminalEnum.TERMINAL_CAR;
//                break;
//            case TERMINAL_PERSONNEL://民警
//                terminalEnum = TerminalEnum.TERMINAL_PERSONNEL;
//                break;
//            default:
//                terminalEnum = null;
//                break;
//        }
        return terminalEnum == null ? TerminalEnum.TERMINAL_PHONE.getRid() : terminalEnum.getRid();
    }

    /**
     * 通过终端设备类型 获取 对应名称
     *
     * @param terminalType
     * @return
     */
    public static String getNameForTerminalType(String terminalType) {
        TerminalEnum terminalEnum = TerminalEnum.valueOf(terminalType);
//        switch (terminalType) {
//            case TerminalType.TERMINAL_PHONE://警务通终端
//                terminalEnum = TerminalEnum.TERMINAL_PHONE;
//                break;
//            case TerminalType.TERMINAL_BODY_WORN_CAMERA://执法记录仪
//                terminalEnum = TerminalEnum.TERMINAL_BODY_WORN_CAMERA;
//                break;
//            case TerminalType.TERMINAL_UAV://无人机
//                terminalEnum = TerminalEnum.TERMINAL_UAV;
//                break;
//            case TerminalType.TERMINAL_PDT://PDT终端(350M)
//                terminalEnum = TerminalEnum.TERMINAL_PDT;
//                break;
//            case TerminalType.TERMINAL_LTE://LTE终端
//                terminalEnum = TerminalEnum.TERMINAL_LTE;
//                break;
//            case TerminalType.TERMINAL_BULL://布控球
//                terminalEnum = TerminalEnum.TERMINAL_BULL;
//                break;
//            case TerminalType.TERMINAL_CAMERA://摄像头
//                terminalEnum = TerminalEnum.TERMINAL_CAMERA;
//                break;
//
//            //载具
//            case TerminalType.TERMINAL_PATROL://船
//                terminalEnum = TerminalEnum.TERMINAL_PATROL;
//                break;
//            case TerminalType.TERMINAL_CAR://警车
//                terminalEnum = TerminalEnum.TERMINAL_CAR;
//                break;
//            case TerminalType.TERMINAL_PERSONNEL://民警
//                terminalEnum = TerminalEnum.TERMINAL_PERSONNEL;
//                break;
//            default:
//                terminalEnum = null;
//                break;
//        }
        return terminalEnum == null ? TerminalEnum.TERMINAL_PHONE.getDes() : terminalEnum.getDes();
    }

}

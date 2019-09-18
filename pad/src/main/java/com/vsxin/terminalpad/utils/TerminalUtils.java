package com.vsxin.terminalpad.utils;

import com.vsxin.terminalpad.mvp.contract.constant.TerminalEnum;
import com.vsxin.terminalpad.mvp.contract.constant.TerminalType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 上图设备 工具类
 */
public class TerminalUtils {

    public static final String CALL_PHONE = "CALL_PHONE";
    public static final String MESSAGE = "MESSAGE";
    public static final String LIVE = "LIVE";
    public static final String INDIVIDUAL_CALL = "INDIVIDUAL_CALL";


    public static void showOperate(int[] imageRid,String terminalType){




    }


    public static Map<String,Boolean> getOperationForTerminalType(String terminalType){
        Map<String,Boolean> operatShow = new HashMap<>();
        switch (terminalType) {
            case TerminalType.TERMINAL_PHONE://警务通终端
                operatShow.put(CALL_PHONE,true);
                operatShow.put(MESSAGE,true);
                operatShow.put(LIVE,true);
                operatShow.put(INDIVIDUAL_CALL,true);
                break;
            case TerminalType.TERMINAL_BODY_WORN_CAMERA://执法记录仪
                operatShow.put(CALL_PHONE,false);
                operatShow.put(MESSAGE,false);
                operatShow.put(LIVE,true);
                operatShow.put(INDIVIDUAL_CALL,false);
                break;
            case TerminalType.TERMINAL_UAV://无人机
                operatShow.put(CALL_PHONE,false);
                operatShow.put(MESSAGE,false);
                operatShow.put(LIVE,true);
                operatShow.put(INDIVIDUAL_CALL,false);
                break;
            case TerminalType.TERMINAL_PDT://PDT终端(350M)
                operatShow.put(CALL_PHONE,false);
                operatShow.put(MESSAGE,false);
                operatShow.put(LIVE,false);
                operatShow.put(INDIVIDUAL_CALL,true);
                break;
            case TerminalType.TERMINAL_LTE://LTE终端
                operatShow.put(CALL_PHONE,false);
                operatShow.put(MESSAGE,false);
                operatShow.put(LIVE,true);
                operatShow.put(INDIVIDUAL_CALL,true);
                break;
            case TerminalType.TERMINAL_BULL://布控球
                operatShow.put(CALL_PHONE,false);
                operatShow.put(MESSAGE,false);
                operatShow.put(LIVE,true);
                operatShow.put(INDIVIDUAL_CALL,false);
                break;
            case TerminalType.TERMINAL_CAMERA://摄像头
                operatShow.put(CALL_PHONE,false);
                operatShow.put(MESSAGE,false);
                operatShow.put(LIVE,true);
                operatShow.put(INDIVIDUAL_CALL,false);
                break;
            case TerminalType.TERMINAL_PERSONNEL://民警
                operatShow.put(CALL_PHONE,true);
                operatShow.put(MESSAGE,true);
                operatShow.put(LIVE,true);
                operatShow.put(INDIVIDUAL_CALL,true);
                break;
            default:
                break;
        }
        return operatShow;
    }


    /**
     * 通过终端设备类型 获取 对应图标
     * @param terminalType
     * @return
     */
    public static int getImageForTerminalType(String terminalType){
        TerminalEnum terminalEnum = null;
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
        return terminalEnum==null ? TerminalEnum.TERMINAL_PHONE.getRid():terminalEnum.getRid();
    }

    /**
     * 通过终端设备类型 获取 对应名称
     * @param terminalType
     * @return
     */
    public static String getNameForTerminalType(String terminalType){
        TerminalEnum terminalEnum = null;
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
        return terminalEnum==null ? TerminalEnum.TERMINAL_PHONE.getDes():terminalEnum.getDes();
    }

}

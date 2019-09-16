package com.vsxin.terminalpad.utils;

import com.vsxin.terminalpad.mvp.contract.constant.TerminalEnum;
import com.vsxin.terminalpad.mvp.contract.constant.TerminalType;

/**
 * 上图设备 工具类
 */
public class TerminalUtils {

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

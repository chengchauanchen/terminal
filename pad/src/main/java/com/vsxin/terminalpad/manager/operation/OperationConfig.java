package com.vsxin.terminalpad.manager.operation;

import com.vsxin.terminalpad.mvp.contract.constant.TerminalEnum;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 功能配置文件
 * 功能按钮    ----->  终端设备
 * 一个功能    ----->  多个终端
 */
public class OperationConfig {

    /**
     * 个呼
     */
    public static String[] INDIVIDUAL_CALL_CONFIG = new String[]{
            TerminalEnum.TERMINAL_PHONE.toString(),//警务通
            TerminalEnum.TERMINAL_UAV.toString(),//无人机
            TerminalEnum.TERMINAL_PDT.toString(),//PDT终端(350M)
            TerminalEnum.TERMINAL_LTE.toString(),//LTE终端
            TerminalEnum.TERMINAL_PERSONNEL.toString(),//民警
    };

    /**
     * 消息
     */
    public static String[] MESSAGE_PAGE_CONFIG = new String[]{
            TerminalEnum.TERMINAL_PHONE.toString(),//警务通
            TerminalEnum.TERMINAL_UAV.toString(),//民警
            TerminalEnum.TERMINAL_PERSONNEL.toString(),//民警
    };

    /**
     * 打电话
     */
    public static String[] PHONE_CALL_CONFIG = new String[]{
            TerminalEnum.TERMINAL_PHONE.toString(),//警务通
            TerminalEnum.TERMINAL_PERSONNEL.toString(),//民警
    };

    /**
     * 拉视频
     */
    public static String[] PULL_LIVE_CONFIG = new String[]{
            TerminalEnum.TERMINAL_PHONE.toString(),//警务通
            TerminalEnum.TERMINAL_BODY_WORN_CAMERA.toString(),//执法仪
            TerminalEnum.TERMINAL_UAV.toString(),//无人机
            TerminalEnum.TERMINAL_LTE.toString(),//LTE
            TerminalEnum.TERMINAL_BALL.toString(),//布控球
            TerminalEnum.TERMINAL_CAMERA.toString(),//城市摄像头
    };

    /**
     * 判断设备是否可以拥有 打电话、消息、拉视频、个呼 功能
     * @param TerminalType
     * @return
     */
    public static Map<OperationEnum,Boolean> getOperationConfig(String TerminalType) {
        Map<OperationEnum,Boolean> operationEnumMap = new HashMap<>();
        boolean phoneCallContains = isContains(PHONE_CALL_CONFIG, TerminalType);
        boolean messagePageContains = isContains(MESSAGE_PAGE_CONFIG, TerminalType);
        boolean pullLiveContains = isContains(PULL_LIVE_CONFIG, TerminalType);
        boolean individualCallContains = isContains(INDIVIDUAL_CALL_CONFIG, TerminalType);
        operationEnumMap.put(OperationEnum.CALL_PHONE,phoneCallContains);
        operationEnumMap.put(OperationEnum.MESSAGE,messagePageContains);
        operationEnumMap.put(OperationEnum.LIVE,pullLiveContains);
        operationEnumMap.put(OperationEnum.INDIVIDUAL_CALL,individualCallContains);
        return operationEnumMap;
    }

    /**
     *
     * @param arr
     * @param targetValue
     * @return
     */
    public static boolean isContains(String[] arr, String targetValue) {
        return Arrays.asList(arr).contains(targetValue);
    }

}

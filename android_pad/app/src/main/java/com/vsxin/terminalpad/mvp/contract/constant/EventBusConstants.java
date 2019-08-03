package com.vsxin.terminalpad.mvp.contract.constant;


public class EventBusConstants {

    public static final int NFC_STEP1_TO_STEP2 = 100;//识别卡成功，展示卡数据
    public static final int NFC_STEP1_TO_STEP3 = 101;//识别卡失败，不支持此卡充值
    public static final int NFC_STEP2_TO_STEP4 = 102;//付款成功，进入写卡
}

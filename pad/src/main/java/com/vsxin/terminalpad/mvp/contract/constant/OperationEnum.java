package com.vsxin.terminalpad.mvp.contract.constant;

import com.vsxin.terminalpad.utils.TerminalUtils;

/**
 * @author qzw
 *  操作类型 个呼,消息,打电话,直播
 */
public enum OperationEnum {
    /**
     * 通知类型 个呼、直播
     */
    CALL_PHONE("打电话", TerminalUtils.CALL_PHONE),
    MESSAGE("消息", TerminalUtils.MESSAGE),
    LIVE("请求图像", TerminalUtils.LIVE),
    INDIVIDUAL_CALL("个呼", TerminalUtils.INDIVIDUAL_CALL);

    private String remarks;
    private String type;

    OperationEnum(String remarks, String type) {
        this.remarks = remarks;
        this.type = type;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "remarks:"+remarks+",type:"+type;
    }}



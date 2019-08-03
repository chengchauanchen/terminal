package com.vsxin.terminalpad.mvp.contract.constant;

/**
 * @author qzw
 *  通知类型 个呼、直播
 */
public enum NoticeTypeEnum {
    /**
     * 通知类型 个呼、直播
     */
    CALL("个呼", 1),
    LIVE("直播", 2);

    private String remarks;
    private int type;

    NoticeTypeEnum(String remarks, int type) {
        this.remarks = remarks;
        this.type = type;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "remarks:"+remarks+",type:"+type;
    }}



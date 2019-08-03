package com.vsxin.terminalpad.mvp.contract.constant;

/**
 * @author qzw
 *  通知类型 被动 or 主动
 */
public enum NoticeInOrOutEnum {
    /**
     * 通知类型 被动 or 主动
     */
    IN("被动", 1),
    OUT("主动", 2);

    private String remarks;
    private int type;

    NoticeInOrOutEnum(String remarks, int type) {
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



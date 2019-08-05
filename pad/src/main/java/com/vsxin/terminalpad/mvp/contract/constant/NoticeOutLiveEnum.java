package com.vsxin.terminalpad.mvp.contract.constant;

/**
 * @author qzw
 *
 * 直播 主动 上报\观看
 *
 */
public enum NoticeOutLiveEnum {
    /**
     * 主动 上报\观看
     */
    LIVE_OUT_REPORT("主动 上报", 1),
    LIVE_OUT_INVITE("主动 邀请他人直播 邀请中", 2),
    LIVE_OUT_INVITE_REFUSE("主动 邀请他人直播 邀请被拒绝", 3),
    LIVE_OUT_INVITE_AGREE("主动 邀请他人直播 邀请同意", 4),
    LIVE_OUT_WATCH("主动 邀请他人直播 正在观看", 5),
    LIVE_OUT_END("主动 邀请他人直播 结束观看", 6),
    LIVE_OUT_TIME_OUT("超时", 7);

    private String remarks;
    private int state;

    NoticeOutLiveEnum(String remarks, int state) {
        this.remarks = remarks;
        this.state = state;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "remarks:"+remarks+",state:"+state;
    }}



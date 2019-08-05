package com.vsxin.terminalpad.mvp.contract.constant;

/**
 * @author qzw
 *
 * 直播 被动 上报\观看
 *
 */
public enum NoticeInLiveEnum {
    /**
     * 被动 上报\观看
     */
    LIVE_IN_INVITE("被动 他人请求我直播 等待接听", 1),
    LIVE_IN_INVITE_REFUSE("被动 他人请求我直播 拒绝", 2),
    LIVE_IN_INVITE_AGREE("被动 他人请求我直播 同意", 3),
    LIVE_IN_WATCH("主动 他人直播邀请我观看", 4),
    LIVE_IN_END("通话结束", 5),
    LIVE_IN_TIME_OUT("超时", 6);

    private String remarks;
    private int state;

    NoticeInLiveEnum(String remarks, int state) {
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



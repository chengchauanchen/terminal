package com.vsxin.terminalpad.mvp.contract.constant;

import com.vsxin.terminalpad.R;

/**
 * @author qzw
 *
 * 个呼 被动 打进来
 */
public enum NoticeInCallEnum {
    /**
     * 被动 打进来
     */
    CALL_IN_WAIT("等待接听", 1, R.mipmap.call_connected),
    CALL_IN_CONNECT("正在通话中", 2,R.mipmap.call_connected),
    CALL_IN_END("通话结束", 3,R.mipmap.call_connected),
    CALL_IN_TIME_OUT("超时未接听", 4,R.mipmap.call_no_connect),
    CALL_IN_REFUSE("拒接接听", 5,R.mipmap.call_no_connect);

    private String remarks;
    private int state;
    private int resId;//资源id,主要用于电话小图标显示

    NoticeInCallEnum(String remarks, int state,int resId) {
        this.remarks = remarks;
        this.state = state;
        this.resId = resId;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
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



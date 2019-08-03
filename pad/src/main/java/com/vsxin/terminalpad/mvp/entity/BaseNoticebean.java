package com.vsxin.terminalpad.mvp.entity;

import com.ixiaoma.xiaomabus.architecture.bean.BaseBean;

public class BaseNoticebean extends BaseBean {
    private Long startTime;//开始时间
    private Long stopTime;//结束时间

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getStopTime() {
        return stopTime;
    }

    public void setStopTime(Long stopTime) {
        this.stopTime = stopTime;
    }
}

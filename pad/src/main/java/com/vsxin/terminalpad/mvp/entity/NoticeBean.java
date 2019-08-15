package com.vsxin.terminalpad.mvp.entity;

import com.vsxin.terminalpad.mvp.contract.constant.NoticeInCallEnum;
import com.vsxin.terminalpad.mvp.contract.constant.NoticeInLiveEnum;
import com.vsxin.terminalpad.mvp.contract.constant.NoticeInOrOutEnum;
import com.vsxin.terminalpad.mvp.contract.constant.NoticeOutCallEnum;
import com.vsxin.terminalpad.mvp.contract.constant.NoticeOutLiveEnum;
import com.vsxin.terminalpad.mvp.contract.constant.NoticeTypeEnum;

/**
 * @author qzw
 *
 * 个人 代办/已办 通知
 *
 * 包括: 个呼、直播
 */
public class NoticeBean extends BaseBean {

    private String memberName;//成员名称
    private int memberId;//成员id

    private NoticeTypeEnum noticeType;//通知类型：个呼，直播
    private NoticeInOrOutEnum inOrOut;//被动 or 主动

    private NoticeInCallEnum inCall;//被动 个呼
    private NoticeOutCallEnum outCall;//主动 个呼

    private NoticeInLiveEnum inLive;//被动 上报/观看
    private NoticeOutLiveEnum outLive;//主动 上报/观看

    private boolean isForce;//是否强制上报

    private Long startTime;//开始时间
    private Long stopTime;//结束时间

    public boolean isForce() {
        return isForce;
    }

    public void setForce(boolean force) {
        isForce = force;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public NoticeTypeEnum getNoticeType() {
        return noticeType;
    }

    public void setNoticeType(NoticeTypeEnum noticeType) {
        this.noticeType = noticeType;
    }

    public NoticeInOrOutEnum getInOrOut() {
        return inOrOut;
    }

    public void setInOrOut(NoticeInOrOutEnum inOrOut) {
        this.inOrOut = inOrOut;
    }

    public NoticeInCallEnum getInCall() {
        return inCall;
    }

    public void setInCall(NoticeInCallEnum inCall) {
        this.inCall = inCall;
    }

    public NoticeOutCallEnum getOutCall() {
        return outCall;
    }

    public void setOutCall(NoticeOutCallEnum outCall) {
        this.outCall = outCall;
    }

    public NoticeInLiveEnum getInLive() {
        return inLive;
    }

    public void setInLive(NoticeInLiveEnum inLive) {
        this.inLive = inLive;
    }

    public NoticeOutLiveEnum getOutLive() {
        return outLive;
    }

    public void setOutLive(NoticeOutLiveEnum outLive) {
        this.outLive = outLive;
    }

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

package com.xuchongyang.easyphone.callback;

import org.linphone.core.LinphoneCall;

/**
 * Created by Mark Xu on 17/3/13.
 * 状态回调
 */

public abstract class PhoneCallback {
    /**
     * 来电状态
     * @param linphoneCall
     */
    public void incomingCall(LinphoneCall linphoneCall) {}

    /**
     * 呼叫初始化
     */
    public void outgoingInit(LinphoneCall linphoneCall) {}

    /**
     * 电话接通
     */
    public void callConnected(LinphoneCall linphoneCall) {}

    /**
     * 电话挂断
     */
    public void callEnd(LinphoneCall linphoneCall) {}

    /**
     * 释放通话
     */
    public void callReleased(LinphoneCall linphoneCall) {}

    /**
     * 连接失败
     */
    public void error(LinphoneCall linphoneCall) {}
}

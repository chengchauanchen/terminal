package com.vsxin.terminalpad.mvp.entity;

/**
 * 直播 通知 bean
 *
 * 2.直播(a.主动直播,b.主动邀请他人直播,c.他人请求我直播,d.他人直播邀请我观看)
 *
 *     被动
 *     c.他人请求我直播：等待直播(接收/拒绝),正在直播,结束直播
 *     d.他人直播邀请我观看:收到邀请(点击观看--直播正在进行中,直播已结束),
 *
 *     主动
 *     a.主动直播????
 *          我正在直播？？
 *     b.主动邀请他人直播：正在邀请中，对方拒绝，对方同意，正在观看，结束观看(主动退出、对方下播)

 *
 *
 */
public class LiveNoticeBean extends BaseNoticebean{

    public static int LIVE_IN = 1;//被动 上报\观看
    public static int LIVE_OUT = 2;//主动 上报\观看

    //被动接收
    public static int LIVE_IN_INVITE = 3;//被动 他人请求我直播 等待接听
    public static int LIVE_IN_INVITE_REFUSE = 4;//被动 他人请求我直播 拒绝
    public static int LIVE_IN_INVITE_AGREE = 5;//被动 他人请求我直播 同意
    public static int LIVE_IN_WATCH = 6;//主动 他人直播邀请我观看 收到邀请(点击观看--直播正在进行中,直播已结束)
    public static int LIVE_IN_END = 7;//通话结束

    //主动发起
    public static int LIVE_OUT_REPORT = 8;//主动 上报
    public static int LIVE_OUT_INVITE = 9;//主动 邀请他人直播 邀请中
    public static int LIVE_OUT_INVITE_REFUSE = 10;//主动 邀请他人直播 邀请被拒绝
    public static int LIVE_OUT_INVITE_AGREE = 11;//主动 邀请他人直播 邀请同意
    public static int LIVE_OUT_WATCH = 12;//主动 邀请他人直播 正在观看
    public static int LIVE_OUT_END = 13;//主动 邀请他人直播 结束观看

    private int liveState;//直播状态 1.被动接收，2.主动发起
    private int liveInState;//被动接收
    private int liveOutState;//主动发起

}

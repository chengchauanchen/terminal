package com.vsxin.terminalpad.mvp.contract.constant;

public class NoticeStateConstants {

    /*************************个呼*************************/

    public static int NOTICE_CALL = 0;//通知 个呼类型
    public static int CALL_IN = 1;//被动 打进来
    public static int CALL_OUT = 2;//主动 拨出去
    //被动 打进来
    public static int CALL_IN_WAIT = 3;//等待接听
    public static int CALL_IN_CONNECT = 4;//正在通话中
    public static int CALL_IN_END = 5;//通话结束
    //主动 拨出去
    public static int CALL_OUT_WAIT = 6;//等待接听
    public static int CALL_OUT_CONNECT = 7;//正在通话中
    public static int CALL_OUT_END = 8;//通话结束

    /**************************直播***********************/
    public static int NOTICE_LIVE = 22;//通知 直播类型
    public static int LIVE_IN = 9;//被动 上报\观看
    public static int LIVE_OUT = 10;//主动 上报\观看

    //被动接收
    public static int LIVE_IN_INVITE = 11;//被动 他人请求我直播 等待接听
    public static int LIVE_IN_INVITE_REFUSE = 12;//被动 他人请求我直播 拒绝
    public static int LIVE_IN_INVITE_AGREE = 13;//被动 他人请求我直播 同意
    public static int LIVE_IN_WATCH = 14;//主动 他人直播邀请我观看 收到邀请(点击观看--直播正在进行中,直播已结束)
    public static int LIVE_IN_END = 15;//通话结束

    //主动发起
    public static int LIVE_OUT_REPORT = 16;//主动 上报
    public static int LIVE_OUT_INVITE = 17;//主动 邀请他人直播 邀请中
    public static int LIVE_OUT_INVITE_REFUSE = 18;//主动 邀请他人直播 邀请被拒绝
    public static int LIVE_OUT_INVITE_AGREE = 19;//主动 邀请他人直播 邀请同意
    public static int LIVE_OUT_WATCH = 20;//主动 邀请他人直播 正在观看
    public static int LIVE_OUT_END = 21;//主动 邀请他人直播 结束观看
}

package com.vsxin.terminalpad.mvp.entity;

/**
 * 个呼 通知 bean
 *
 * 1.个呼(a.打进来，b.拨出去)
 *       a.打进来(等待接听(接听/拒绝),正在通话中(通话时长),通话结束(自己\对方 挂断))
 *       b.拨出去(等待接听(取消,对方未接听(超时、挂断)),正在通话中,通话结束(自己\对方 挂断))
 *
 *
 */
public class IndividualCallNoticeBean extends BaseNoticebean{

    public static int CALL_IN = 1;//打进来
    public static int CALL_OUT = 2;//拨出去
    //打进来
    public static int CALL_IN_WAIT = 3;//等待接听
    public static int CALL_IN_CONNECT = 4;//正在通话中
    public static int call_in_end = 5;//通话结束
    //拨出去
    public static int CALL_OUT_WAIT = 6;//等待接听
    public static int CALL_OUT_CONNECT = 7;//正在通话中
    public static int CALL_OUT_END = 8;//通话结束

    private int callState;//个呼状态 1.打进来，1.拨出去
    private int callInState;//打进来状态
    private int callOutState;//拨出去状态

}

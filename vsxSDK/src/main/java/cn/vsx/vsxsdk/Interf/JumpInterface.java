package cn.vsx.vsxsdk.Interf;

import java.util.List;

public interface JumpInterface {

    /**
     * 发起上报
     */
    void activeStartLive();
    /**
     * 自己上报，邀请别人来观看
     */
    void activeStartLive(String memberNo);

    /**
     * 自己上报，邀请别人来观看
     * @param type 终端类型 1：手机   6 PC
     */
    void activeStartLive(String memberNo,int type);

    /**
     * 请求别人上报
     * @param memberNo
     * 默认是手机
     */
    void requestOtherLive(String memberNo);

    /**
     * 请求别人上报
     * @param memberNo
     * @param type 终端类型 1：手机   6 PC
     */
    void requestOtherLive(String memberNo,int type);

    /**
     * 发起个呼
     * @param memberNo
     */
    void activeIndividualCall(String memberNo);

    /**
     * 发起个呼
     * @param memberNo
     * @param type 终端类型 1：手机   6 PC
     */
    void activeIndividualCall(String memberNo,int type);

    /**
     * 跳转到个人会话
     * @param memberNo
     */
    void jumpPersonChatActivity(String memberNo);

    /**
     * 跳转到组会话
     * @param groupNo
     */
    void jumpGroupChatActivity(String groupNo);

    void voipCall (String phoneNo, String appkey);

    void createTemporaryGroup(String appkey);

    void changeGroup (String groupNo,String appkey);

    void monitorGroup (String groupNo,String appkey);

    void pushVideoLive (List<String > numbers , List<String> groups , String appkey);
}

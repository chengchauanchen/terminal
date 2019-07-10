package cn.vsx.vsxsdk.Interf;

import android.content.Context;

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
     *
     * @param type 终端类型 1：手机   6 PC
     */
    void activeStartLive(String memberNo, int type);

    /**
     * 请求别人上报
     *
     * @param memberNo 默认是手机
     */
    void requestOtherLive(String memberNo);

    /**
     * 请求别人上报
     *
     * @param memberNo
     * @param type     终端类型 1：手机   6 PC
     */
    void requestOtherLive(String memberNo, int type);

    /**
     * 发起个呼
     *
     * @param memberNo
     */
    void activeIndividualCall(String memberNo);

    /**
     * 发起个呼
     *
     * @param memberNo
     * @param type     终端类型 1：手机   6 PC
     */
    void activeIndividualCall(String memberNo, int type);

    /**
     * 跳转到个人会话
     *
     * @param memberNo
     */
    void jumpPersonChatActivity(String memberNo);

    /**
     * 跳转到组会话
     *
     * @param groupNo
     */
    void jumpGroupChatActivity(String groupNo);

    /**
     * 跳转到组会话
     *
     * @param groupName 组名
     */
    void jumpGroupChatActivityForName(String groupName);

    /**
     * voip电话
     *
     * @param phoneNo
     */
    void voipCall(String phoneNo);

    /**
     * 创建临时组
     */
    void createTemporaryGroup();

    /**
     * 切换组
     *
     * @param groupNo
     */
    void changeGroup(String groupNo);


    /**
     * 组监听
     *
     * @param groupNo
     */
    void monitorGroup(String groupNo);

    /**
     * 上报视频
     *
     * @param members
     * @param groups
     */
    void pushVideoLive(List<String> members, List<String> groups);

    /**
     * 启动融合通信app
     */
    void launchedVSXApp(Context context);

    /**
     * 注册连接jump的广播
     * @param context
     */
    void registerConnectJumpReceiver(Context context);

    /**
     * 解绑连接jump的广播
     * @param context
     */
    void unregisterConnectJumpReceiver(Context context);
}

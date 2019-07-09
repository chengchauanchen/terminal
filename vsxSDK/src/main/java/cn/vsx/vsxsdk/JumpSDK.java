package cn.vsx.vsxsdk;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.util.List;

import cn.vsx.vsxsdk.Interf.JumpInterface;
import cn.vsx.vsxsdk.constant.CommandEnum;
import cn.vsx.vsxsdk.utils.GsonUtils;

public class JumpSDK implements JumpInterface {


    /**
     * 注册连接jump的广播
     * @param context
     */
    @Override
    public void registerConnectJumpReceiver(Context context) {
        VsxSDK.getInstance().getRegisterBroadcastReceiver().register(context);
    }

    /**
     * 解绑连接jump的广播
     * @param context
     */
    @Override
    public void unregisterConnectJumpReceiver(Context context) {
        VsxSDK.getInstance().getRegisterBroadcastReceiver().unregisterReceiver(context);
    }

    /**
     * 启动融合通信app
     */
    @Override
    public void launchedVSXApp(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vsxin://project.release.com/jump"));
        context.startActivity(intent);
    }

    /**
     * 发起上报
     */
    @Override
    public void activeStartLive() {
        try {
            String json = GsonUtils.getEmptySendGson();
            VsxSDK.getInstance().getIJump().jumpPage(json, CommandEnum.SelfLive.getType());
        } catch (Exception e) {
            Log.e("JumpSDK", "发起上报失败");
        }
    }

    /**
     * 自己上报，邀请别人来观看
     */
    @Override
    public void activeStartLive(String memberNo) {
        try {
            String json = GsonUtils.getMemberNoToGson(memberNo);
            VsxSDK.getInstance().getIJump().jumpPage(json, CommandEnum.SelfLive.getType());//;
        } catch (Exception e) {
            Log.e("JumpSDK", "自己上报，邀请别人来观看 失败");
        }
    }

    /**
     * 自己上报，邀请别人来观看
     *
     * @param type 终端类型 1：手机   6 PC
     */
    @Override
    public void activeStartLive(String memberNo, int type) {
        try {
            String json = GsonUtils.getMemberTerminalTypeToGson(memberNo, type);
            VsxSDK.getInstance().getIJump().jumpPage(json, CommandEnum.SelfLive.getType());//;
        } catch (Exception e) {
            Log.e("JumpSDK", "自己上报，邀请别人来观看 失败");
        }
    }

    /**
     * 请求别人上报
     *
     * @param memberNo 默认是手机
     */
    @Override
    public void requestOtherLive(String memberNo) {
        try {
            String json = GsonUtils.getMemberNoToGson(memberNo);
            VsxSDK.getInstance().getIJump().jumpPage(json, CommandEnum.OtherLive.getType());//;
        } catch (Exception e) {
            Log.e("JumpSDK", "请求别人上报失败");
        }
    }

    /**
     * 请求别人上报
     *
     * @param memberNo
     * @param type     终端类型 1：手机   6 PC
     */
    @Override
    public void requestOtherLive(String memberNo, int type) {
        try {
            String json = GsonUtils.getMemberTerminalTypeToGson(memberNo, type);
            VsxSDK.getInstance().getIJump().jumpPage(json, CommandEnum.OtherLive.getType());//;
        } catch (Exception e) {
            Log.e("JumpSDK", "请求别人上报");
        }
    }

    /**
     * 发起个呼
     *
     * @param memberNo
     */
    @Override
    public void activeIndividualCall(String memberNo) {
        try {
            String json = GsonUtils.getMemberNoToGson(memberNo);
            VsxSDK.getInstance().getIJump().jumpPage(json, CommandEnum.IndividualCall.getType());//;
        } catch (Exception e) {
            Log.e("JumpSDK", "发起个呼失败");
        }
    }

    /**
     * 发起个呼
     *
     * @param memberNo
     * @param type     终端类型 1：手机   6 PC
     */
    @Override
    public void activeIndividualCall(String memberNo, int type) {
        try {
            String json = GsonUtils.getMemberTerminalTypeToGson(memberNo, type);
            VsxSDK.getInstance().getIJump().jumpPage(json, CommandEnum.IndividualCall.getType());//;
        } catch (Exception e) {
            Log.e("JumpSDK", "发起个呼失败");
        }
    }

    /**
     * 跳转到个人会话
     *
     * @param memberNo
     */
    @Override
    public void jumpPersonChatActivity(String memberNo) {
        try {
            String json = GsonUtils.getMemberNoToGson(memberNo);
            VsxSDK.getInstance().getIJump().jumpPage(json, CommandEnum.PersonChat.getType());//;
        } catch (Exception e) {
            Log.e("JumpSDK", "跳转到个人会话失败");
        }
    }

    /**
     * 跳转到组会话
     *
     * @param groupNo
     */
    @Override
    public void jumpGroupChatActivity(String groupNo) {
        try {
            String json = GsonUtils.getGroupNoToGson(groupNo);
            VsxSDK.getInstance().getIJump().jumpPage(json, CommandEnum.GroupChat.getType());//;
        } catch (Exception e) {
            Log.e("JumpSDK", "跳转到组会话失败");
        }
    }

    /**
     * voip电话
     *
     * @param phoneNo
     */
    @Override
    public void voipCall(String phoneNo) {
        try {
            String json = GsonUtils.getPhoneNoToGson(phoneNo);
            VsxSDK.getInstance().getIJump().jumpPage(json, CommandEnum.VoipCall.getType());
        } catch (Exception e) {
            Log.e("JumpSDK", "voip电话失败");
        }
    }

    /**
     * 创建临时组
     */
    @Override
    public void createTemporaryGroup() {
        try {
            String json = GsonUtils.getEmptySendGson();
            VsxSDK.getInstance().getIJump().jumpPage(json, CommandEnum.CreateTempGroup.getType());
        } catch (Exception e) {
            Log.e("JumpSDK", "创建临时组失败");
        }
    }

    /**
     * 切换组
     *
     * @param groupNo
     */
    @Override
    public void changeGroup(String groupNo) {
        String json = GsonUtils.getGroupNoToGson(groupNo);

    }

    /**
     * 组监听
     *
     * @param groupNo
     */
    @Override
    public void monitorGroup(String groupNo) {
        String json = GsonUtils.getGroupNoToGson(groupNo);

    }

    /**
     * 上报视频
     *
     * @param members
     * @param groups
     */
    @Override
    public void pushVideoLive(List<String> members, List<String> groups) {
        try {
            String json = GsonUtils.getMembersGroupsToGson(members, groups);
            VsxSDK.getInstance().getIJump().jumpPage(json, CommandEnum.pushVideoLive.getType());
        } catch (Exception e) {
            Log.e("JumpSDK", "上报视频失败");
        }
    }
}

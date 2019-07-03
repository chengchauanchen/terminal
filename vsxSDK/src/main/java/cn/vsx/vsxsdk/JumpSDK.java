package cn.vsx.vsxsdk;

import android.util.Log;

import java.util.List;

import cn.vsx.vsxsdk.Interf.JumpInterface;
import cn.vsx.vsxsdk.constant.CommandEnum;
import cn.vsx.vsxsdk.utils.GsonUtils;


public class JumpSDK implements JumpInterface {

    /**
     * 发起上报
     */
    @Override
    public void activeStartLive() {
        try {
            String json = GsonUtils.getEmptySendGson();
            VsxSDK.getInstance().getIJump().jumpPage(json, CommandEnum.SelfLive.getType());
        } catch (Exception e) {
            Log.e("JumpSDK", e.toString());
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
            Log.e("JumpSDK", e.toString());
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
            Log.e("JumpSDK", e.toString());
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
            Log.e("JumpSDK", e.toString());
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
            Log.e("JumpSDK", e.toString());
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
            Log.e("JumpSDK", e.toString());
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
            Log.e("JumpSDK", e.toString());
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
            Log.e("JumpSDK", e.toString());
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
            Log.e("JumpSDK", e.toString());
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
            Log.e("JumpSDK", e.toString());
        }
    }

    /**
     * 创建临时组
     *
     * @param appkey
     */
    @Override
    public void createTemporaryGroup() {
        try {
            String json = GsonUtils.getEmptySendGson();
            VsxSDK.getInstance().getIJump().jumpPage(json, CommandEnum.CreateTempGroup.getType());
        } catch (Exception e) {
            Log.e("JumpSDK", e.toString());
        }
    }

    /**
     * 切换组
     *
     * @param groupNo
     * @param appkey
     */
    @Override
    public void changeGroup(String groupNo) {
        String json = GsonUtils.getGroupNoToGson(groupNo);

    }

    /**
     * 组监听
     *
     * @param groupNo
     * @param appkey
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
     * @param appkey
     */
    @Override
    public void pushVideoLive(List<String> members, List<String> groups) {
        try {
            String json = GsonUtils.getMembersGroupsToGson(members, groups);
            VsxSDK.getInstance().getIJump().jumpPage(json, CommandEnum.pushVideoLive.getType());
        } catch (Exception e) {
            Log.e("JumpSDK", e.toString());
        }
    }
}

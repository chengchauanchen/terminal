package cn.vsx.vsxsdk;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.vsxsdk.Interf.JumpInterface;
import cn.vsx.vsxsdk.constant.CommandEnum;
import cn.vsx.vsxsdk.utils.GsonUtils;
import cn.vsx.vsxsdk.utils.download.DownLoadApkUtils;

public class JumpSDK implements JumpInterface {

    private Context context;

    public JumpSDK(Context context) {
        this.context = context;
    }

    @Override
    public void autoDownloadApk(Context activity) {
        DownLoadApkUtils.getInstance(activity).startDownLoadApk(activity);
    }

    @Override
    public void addMemberToTempGroup(List<String> nos,String businessId) {
        String json = GsonUtils.getAddMembersToTempGroupGson(nos,businessId);
        try {
            VsxSDK.getInstance().getIJump().jumpPage(json, CommandEnum.AddMemberToGroup.getType());
        } catch (Exception e) {
            Log.e("JumpSDK", "添加成员失败");
            doCacheCommandAndLaunchedVSXApp(json, CommandEnum.AddMemberToGroup.getType());
        }
    }

    @Override
    public void sendStartAppBroadcast(Context context) {
        VsxSDK.getInstance().getRegisterBroadcastReceiver().sendStartAppBroadcast(context);
    }

    @Override
    public void startVsxService(Context context){
        Intent intent = new Intent();
        //在intent里面加   component（“广播接收者的包名”，“广播接收者路径”）
        intent.setComponent(new ComponentName("cn.vsx.vc","ptt.terminalsdk.context.OnlineService"));
        context.startService(intent);
        Log.e("--vsxSDK--","启动惟实信Service");
    }



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
        Toast.makeText(context, "正在开启融合通信", Toast.LENGTH_SHORT).show();
        startIntent(context,"vsxin://project.release.com/jump");
    }

    private void startIntent(Context context,String url){
        //PackageManager packageManager = context.getPackageManager();
        String packageName = "cn.vsx.vc";//要打开应用的包名

        //这个方法在没main 入口得时候会取到null
//        Intent launchIntentForPackage = packageManager.getLaunchIntentForPackage(packageName);
        if(isAvilible(context,packageName)){
            //scheme
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }else{
            //Toast.makeText(context, "手机未安装融合通信APP,请前往警务平台应用商店下载", Toast.LENGTH_LONG).show();
            Toast.makeText(context, "手机未安装融合通信APP,正在下载", Toast.LENGTH_LONG).show();
            autoDownloadApk(context);
        }
    }

    /**
     * 判断包是否安装
     * @param context
     * @param packageName
     * @return
     */
    private boolean isAvilible(Context context, String packageName){
        final PackageManager packageManager = context.getPackageManager();//获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);//获取所有已安装程序的包信息
        List<String> pName = new ArrayList<String>();//用于存储所有已安装程序的包名
        //从pinfo中将包名字逐一取出，压入pName list中
        if(pinfo != null){
            for(int i = 0; i < pinfo.size(); i++){
                String pn = pinfo.get(i).packageName;
                pName.add(pn);
            }
        }
        return pName.contains(packageName);//判断pName中是否有目标程序的包名，有TRUE，没有FALSE
    }


    /**
     * 发起上报
     */
    @Override
    public void activeStartLive() {
        String json = GsonUtils.getEmptySendGson();
        try {
            VsxSDK.getInstance().getIJump().jumpPage(json, CommandEnum.SelfLive.getType());
        } catch (Exception e) {
            Log.e("JumpSDK", "发起上报失败");
            doCacheCommandAndLaunchedVSXApp(json, CommandEnum.SelfLive.getType());
        }
    }

    /**
     * 自己上报，邀请别人来观看
     */
    @Override
    public void activeStartLive(String memberNo) {
        String json = GsonUtils.getMemberNoToGson(memberNo);
        try {
            VsxSDK.getInstance().getIJump().jumpPage(json, CommandEnum.SelfLive.getType());//;
        } catch (Exception e) {
            Log.e("JumpSDK", "自己上报，邀请别人来观看 失败");
            doCacheCommandAndLaunchedVSXApp(json, CommandEnum.SelfLive.getType());
        }
    }

    /**
     * 自己上报，邀请别人来观看
     *
     * @param type 终端类型 1：手机   6 PC
     */
    @Override
    public void activeStartLive(String memberNo, int type) {
        String json = GsonUtils.getMemberTerminalTypeToGson(memberNo, type);
        try {
            VsxSDK.getInstance().getIJump().jumpPage(json, CommandEnum.SelfLive.getType());//;
        } catch (Exception e) {
            Log.e("JumpSDK", "自己上报，邀请别人来观看 失败");
            doCacheCommandAndLaunchedVSXApp(json, CommandEnum.SelfLive.getType());
        }
    }

    /**
     * 请求别人上报
     *
     * @param memberNo 默认是手机
     */
    @Override
    public void requestOtherLive(String memberNo) {
        String json = GsonUtils.getMemberNoToGson(memberNo);
        try {
            VsxSDK.getInstance().getIJump().jumpPage(json, CommandEnum.OtherLive.getType());//;
        } catch (Exception e) {
            Log.e("JumpSDK", "请求别人上报失败");
            doCacheCommandAndLaunchedVSXApp(json, CommandEnum.OtherLive.getType());
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
        String json = GsonUtils.getMemberTerminalTypeToGson(memberNo, type);
        try {
            VsxSDK.getInstance().getIJump().jumpPage(json, CommandEnum.OtherLive.getType());//;
        } catch (Exception e) {
            Log.e("JumpSDK", "请求别人上报");
            doCacheCommandAndLaunchedVSXApp(json, CommandEnum.OtherLive.getType());
        }
    }

    /**
     * 发起个呼
     *
     * @param memberNo
     */
    @Override
    public void activeIndividualCall(String memberNo) {
        String json = GsonUtils.getMemberNoToGson(memberNo);
        try {
            VsxSDK.getInstance().getIJump().jumpPage(json, CommandEnum.IndividualCall.getType());//;
        } catch (Exception e) {
            Log.e("JumpSDK", "发起个呼失败");
            doCacheCommandAndLaunchedVSXApp(json, CommandEnum.IndividualCall.getType());
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
        String json = GsonUtils.getMemberTerminalTypeToGson(memberNo, type);
        try {
            VsxSDK.getInstance().getIJump().jumpPage(json, CommandEnum.IndividualCall.getType());//;
        } catch (Exception e) {
            Log.e("JumpSDK", "发起个呼失败");
            doCacheCommandAndLaunchedVSXApp(json, CommandEnum.IndividualCall.getType());
        }
    }

    /**
     * 跳转到个人会话
     *
     * @param memberNo
     */
    @Override
    public void jumpPersonChatActivity(String memberNo) {
        String json = GsonUtils.getMemberNoToGson(memberNo);
        try {
            VsxSDK.getInstance().getIJump().jumpPage(json, CommandEnum.PersonChat.getType());//;
        } catch (Exception e) {
            Log.e("JumpSDK", "跳转到个人会话失败");
            doCacheCommandAndLaunchedVSXApp(json, CommandEnum.PersonChat.getType());
        }
    }

    /**
     * 跳转到组会话
     *
     * @param groupNo
     */
    @Override
    public void jumpGroupChatActivity(String groupNo) {
        String json = GsonUtils.getGroupNoToGson(groupNo);
        try {
            VsxSDK.getInstance().getIJump().jumpPage(json, CommandEnum.GroupChat.getType());//;
        } catch (Exception e) {
            Log.e("JumpSDK", "跳转到组会话失败");
            doCacheCommandAndLaunchedVSXApp(json, CommandEnum.GroupChat.getType());
        }
    }

    @Override
    public void jumpGroupChatActivityForName(String groupName) {
        String json = GsonUtils.getGroupNameToGson(groupName);
        try {
            VsxSDK.getInstance().getIJump().jumpPage(json, CommandEnum.GroupChat.getType());//;
        } catch (Exception e) {
            Log.e("JumpSDK", "跳转到组会话失败");
            doCacheCommandAndLaunchedVSXApp(json, CommandEnum.GroupChat.getType());
        }
    }

    /**
     * voip电话
     *
     * @param phoneNo
     */
    @Override
    public void voipCall(String phoneNo) {
        String json = GsonUtils.getPhoneNoToGson(phoneNo);
        try {
            VsxSDK.getInstance().getIJump().jumpPage(json, CommandEnum.VoipCall.getType());
        } catch (Exception e) {
            Log.e("JumpSDK", "voip电话失败");
            doCacheCommandAndLaunchedVSXApp(json, CommandEnum.VoipCall.getType());
        }
    }

    /**
     * 创建临时组
     */
    @Override
    public void createTemporaryGroup() {
        String json = GsonUtils.getEmptySendGson();
        try {
            VsxSDK.getInstance().getIJump().jumpPage(json, CommandEnum.CreateTempGroup.getType());
        } catch (Exception e) {
            Log.e("JumpSDK", "创建临时组失败");
            doCacheCommandAndLaunchedVSXApp(json, CommandEnum.CreateTempGroup.getType());
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
        String json = GsonUtils.getMembersGroupsToGson(members, groups);
        try {
            VsxSDK.getInstance().getIJump().jumpPage(json, CommandEnum.pushVideoLive.getType());
        } catch (Exception e) {
            Log.e("JumpSDK", "上报视频失败");
            doCacheCommandAndLaunchedVSXApp(json, CommandEnum.pushVideoLive.getType());
        }
    }

    /**
     * 缓存指令参数,并打开融合通信app
     * @param json
     * @param type
     */
    private void doCacheCommandAndLaunchedVSXApp(String json,int type){
        CommandCache.getInstance().setCommandCacheData(json, type);
        launchedVSXApp(context);
        //如果异常，重新发送连接广播
        //VsxSDK.getInstance().getRegisterBroadcastReceiver().sendMessageActionBroadcast(context);
    }
}

package com.vsxin.terminalpad.app;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsListener;
import com.vsxin.terminalpad.mvp.contract.constant.AppStatusConstants;
import com.vsxin.terminalpad.utils.CommonGroupUtil;
import com.vsxin.terminalpad.utils.SystemUtils;

import cn.vsx.SpecificSDK.SpecificSDK;
import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenStateMachine;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakStateMachine;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallState;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallStateMachine;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePlayingState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePlayingStateMachine;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePushingState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePushingStateMachine;
import ptt.terminalsdk.tools.ToastUtil;
import skin.support.SkinCompatManager;
import skin.support.design.app.SkinMaterialViewInflater;

/**
 * @author qzw
 * pad客户端application
 */
public class PadApplication extends App {

    public static int mAppStatus = AppStatusConstants.FORCE_KILL;//App运行状态，是否被强杀

    public boolean isPttPress = false;//组呼是否按下
    public boolean isPlayVoice = false;//是否正在播放组呼
    public boolean isCallState = false; //记录主动个呼嘟嘟声
    public boolean usbAttached;//外置摄像头是否连接


    /**标记个呼来或者请求图形来，是否做了接受或拒绝的操作，默认是false*/
    public boolean isPrivateCallOrVideoLiveHand = false;

    public static PadApplication getPadApplication() {
        return (PadApplication)getApplication();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //确保只在主进程中初始化
        if(!SystemUtils.isMainProcess(this,getPackageName())){
            return;
        }
        initVsxinSDK();
        registerActivityLifecycle();
        initCatchGroup();
//        SkinCompatManager.withoutActivity(this)                         // 基础控件换肤初始化
//                .addInflater(new SkinMaterialViewInflater())            // material design 控件换肤初始化[可选]
//                .loadSkin();
        //配置c5内容
//        initC5();
    }

    /**
     *  配置c5内容
     */
    private void initC5(){
//搜集本地tbs内核信息并上报服务器，服务器返回结果决定使用哪个内核。

        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {

            @Override
            public void onViewInitFinished(boolean arg0) {
                // TODO Auto-generated method stub
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                Log.d("app", " onViewInitFinished is " + arg0);

                ToastUtil.showToast(" onViewInitFinished is " + arg0);
            }

            @Override
            public void onCoreInitFinished() {
                // TODO Auto-generated method stub
            }
        };
        //x5内核初始化接口
        QbSdk.initX5Environment(getApplicationContext(),  cb);
        QbSdk.setTbsListener(new TbsListener(){
            @Override
            public void onDownloadFinish(int i){
                Log.e("onDownloadFinish", "i:" + i);
            }

            @Override
            public void onInstallFinish(int i){
                Log.e("onInstallFinish", "i:" + i);
            }

            @Override
            public void onDownloadProgress(int i){
                Log.e("onDownloadProgress", "i:" + i);
            }
        });
    }

    /**
     * 初始化vsxSDK 相关
     */
    private void initVsxinSDK(){
        SpecificSDK.init(this,TerminalMemberType.TERMINAL_PAD.toString());
    }

    /**
     * 监听所有Activity生命周期
     */
    private void registerActivityLifecycle(){
        registerActivityLifecycleCallbacks(new SimpleActivityLifecycle());
    }

    /**
     * 初始化常用组
     * 将常用组数据，放到内存中
     */
    private void initCatchGroup(){
        CommonGroupUtil.getCatchGroupIds();
    }

    /**
     * 获取 Manifest中配置得 APKTYPE 值，并存储
     */
    public void setApkType(){
        SpecificSDK.setApkType(this);
    }

    /**
     * 获取 Manifest中配置得 cn.vsx.sdk.API_KEY 值，并存储
     */
    public void setAppKey(){
        SpecificSDK.setAppKey(this);
    }

    public static int getmAppStatus() {
        return mAppStatus;
    }

    public static void setmAppStatus(int aStatus) {
        mAppStatus = aStatus;
    }

    public void setTerminalMemberType(){
        SpecificSDK.setTerminalMemberType(TerminalMemberType.TERMINAL_PAD.toString());
    }

    @Override
    public VideoLivePlayingState getVideoLivePlayingState(){
        VideoLivePlayingStateMachine liveStateMachine = TerminalFactory.getSDK().getLiveManager().getVideoLivePlayingStateMachine();
        if (liveStateMachine != null){
            return liveStateMachine.getCurrentState();
        }
        return null;
    }
    @Override
    public VideoLivePushingState getVideoLivePushingState(){
        VideoLivePushingStateMachine liveStateMachine = TerminalFactory.getSDK().getLiveManager().getVideoLivePushingStateMachine();
        if (liveStateMachine != null){
            return liveStateMachine.getCurrentState();
        }
        return null;
    }
    @Override
    public IndividualCallState getIndividualState(){
        IndividualCallStateMachine individualCallStateMachine = TerminalFactory.getSDK().getIndividualCallManager().getIndividualCallStateMachine();
        if (individualCallStateMachine != null){
            return individualCallStateMachine.getCurrentState();
        }
        return null;
    }

    @Override
    public GroupCallListenState getGroupListenenState(){
        GroupCallListenStateMachine groupCallListenStateMachine = TerminalFactory.getSDK().getGroupCallManager().getGroupCallListenStateMachine();
        if (groupCallListenStateMachine != null){
            return groupCallListenStateMachine.getCurrentState();
        }
        return null;
    }

    @Override
    public GroupCallSpeakState getGroupSpeakState(){
        GroupCallSpeakStateMachine groupCallSpeakStateMachine = TerminalFactory.getSDK().getGroupCallManager().getGroupCallSpeakStateMachine();
        if (groupCallSpeakStateMachine != null){
            return groupCallSpeakStateMachine.getCurrentState();
        }
        return null;
    }

    public String getVersionName(){
        String localVersion = "";
        try{
            PackageInfo packageInfo = getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0);
            localVersion = packageInfo.versionName;
        }catch(PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
        return localVersion;
    }

}

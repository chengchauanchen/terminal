package com.vsxin.terminalpad.app;

import com.vsxin.terminalpad.mvp.contract.constant.AppStatusConstants;
import com.vsxin.terminalpad.utils.CommonGroupUtil;
import com.vsxin.terminalpad.utils.SystemUtils;

import cn.vsx.SpecificSDK.SpecificSDK;
import cn.vsx.SpecificSDK.application.App;
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

/**
 * @author qzw
 * pad客户端application
 */
public class PadApplication extends App {

    public static int mAppStatus = AppStatusConstants.FORCE_KILL;//App运行状态，是否被强杀

    public boolean isPttPress = false;//组呼是否按下

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

        setApkType();
        setAppKey();

    }

    /**
     * 初始化vsxSDK 相关
     */
    private void initVsxinSDK(){
        SpecificSDK.init(this);
        //设置终端类型未 pad
        SpecificSDK.setTerminalMemberType(TerminalMemberType.TERMINAL_PAD.toString());
//        SpecificSDK.setTerminalMemberType(TerminalMemberType.TERMINAL_PHONE.toString());//暂时先模拟警务通
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

}
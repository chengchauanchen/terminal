package com.vsxin.terminalpad.app;

import android.content.Context;
import android.support.multidex.MultiDex;

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
 * 基类Application,主要用于VsxinSDK初始化操作，
 */
public class App extends com.ixiaoma.xiaomabus.architecture.app.App {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static App getApp() {
        return (App)getApplication();
    }


    /*****************************************当前状态获取***************************************************/
    public VideoLivePlayingState getVideoLivePlayingState(){
        VideoLivePlayingStateMachine liveStateMachine = TerminalFactory.getSDK().getLiveManager().getVideoLivePlayingStateMachine();
        if (liveStateMachine != null){
            return liveStateMachine.getCurrentState();
        }
        return null;
    }
    public VideoLivePushingState getVideoLivePushingState(){
        VideoLivePushingStateMachine liveStateMachine = TerminalFactory.getSDK().getLiveManager().getVideoLivePushingStateMachine();
        if (liveStateMachine != null){
            return liveStateMachine.getCurrentState();
        }
        return null;
    }
    public IndividualCallState getIndividualState(){
        IndividualCallStateMachine individualCallStateMachine = TerminalFactory.getSDK().getIndividualCallManager().getIndividualCallStateMachine();
        if (individualCallStateMachine != null){
            return individualCallStateMachine.getCurrentState();
        }
        return null;
    }

    public GroupCallListenState getGroupListenenState(){
        GroupCallListenStateMachine groupCallListenStateMachine = TerminalFactory.getSDK().getGroupCallManager().getGroupCallListenStateMachine();
        if (groupCallListenStateMachine != null){
            return groupCallListenStateMachine.getCurrentState();
        }
        return null;
    }

    public GroupCallSpeakState getGroupSpeakState(){
        GroupCallSpeakStateMachine groupCallSpeakStateMachine = TerminalFactory.getSDK().getGroupCallManager().getGroupCallSpeakStateMachine();
        if (groupCallSpeakStateMachine != null){
            return groupCallSpeakStateMachine.getCurrentState();
        }
        return null;
    }
}

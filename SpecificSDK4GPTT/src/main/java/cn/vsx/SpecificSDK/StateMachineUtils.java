package cn.vsx.SpecificSDK;

import android.util.Log;

import java.util.Map;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenStateMachine;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakStateMachine;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallState;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallStateMachine;
import cn.vsx.hamster.terminalsdk.manager.terminal.TerminalState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePlayingState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePlayingStateMachine;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePushingState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePushingStateMachine;
import cn.vsx.util.StateMachine.IState;
import ptt.terminalsdk.context.MyTerminalFactory;

public class StateMachineUtils {

    /**
     * 重置状态机
     */
    public static void revertStateMachine() {
        Log.e("state","状态机重置了");
        //视频上报
        if(getVideoLivePushingState() != VideoLivePushingState.IDLE){
            MyTerminalFactory.getSDK().getLiveManager().ceaseLiving();
        }
        //视频观看
        if(getVideoLivePlayingState() != VideoLivePlayingState.IDLE){
            MyTerminalFactory.getSDK().getLiveManager().ceaseWatching();
        }
        //个呼
        if(getIndividualState() != IndividualCallState.IDLE){
            MyTerminalFactory.getSDK().getIndividualCallManager().ceaseIndividualCall();
        }
    }

    /**
     * 获取当前状态map集合
     * @return
     */
    public static Map<TerminalState, IState<?>> getCurrentStateMap(){
        return TerminalFactory.getSDK().getTerminalStateManager().getCurrentStateMap();
    }

    /**
     * 视频直播 播放状态
     * @return
     */
    public static VideoLivePlayingState getVideoLivePlayingState(){
        VideoLivePlayingStateMachine liveStateMachine = TerminalFactory.getSDK().getLiveManager().getVideoLivePlayingStateMachine();
        if (liveStateMachine != null){
            return liveStateMachine.getCurrentState();
        }
        return null;
    }

    /**
     * 上报状态
     * @return
     */
    public static VideoLivePushingState getVideoLivePushingState(){
        VideoLivePushingStateMachine liveStateMachine = TerminalFactory.getSDK().getLiveManager().getVideoLivePushingStateMachine();
        if (liveStateMachine != null){
            return liveStateMachine.getCurrentState();
        }
        return null;
    }

    /**
     * 获取个呼状态
     * @return
     */
    public static IndividualCallState getIndividualState(){
        IndividualCallStateMachine individualCallStateMachine = TerminalFactory.getSDK().getIndividualCallManager().getIndividualCallStateMachine();
        if (individualCallStateMachine != null){
            return individualCallStateMachine.getCurrentState();
        }
        return null;
    }

    /**
     * 获取组呼 听 状态
     * @return
     */
    public static GroupCallListenState getGroupListenenState(){
        GroupCallListenStateMachine groupCallListenStateMachine = TerminalFactory.getSDK().getGroupCallManager().getGroupCallListenStateMachine();
        if (groupCallListenStateMachine != null){
            return groupCallListenStateMachine.getCurrentState();
        }
        return null;
    }

    /**
     * 获取组呼 说 状态
     * @return
     */
    public static GroupCallSpeakState getGroupSpeakState(){
        GroupCallSpeakStateMachine groupCallSpeakStateMachine = TerminalFactory.getSDK().getGroupCallManager().getGroupCallSpeakStateMachine();
        if (groupCallSpeakStateMachine != null){
            return groupCallSpeakStateMachine.getCurrentState();
        }
        return null;
    }

}

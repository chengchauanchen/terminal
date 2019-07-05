package cn.vsx.vc.jump.sendMessage;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyDataMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyEmergencyVideoLiveIncommingMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyIndividualCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyLivingIncommingHandler;
import cn.vsx.vc.receiveHandle.ReceiveGoWatchRTSPHandler;
import ptt.terminalsdk.context.MyTerminalFactory;

public class RegistReceiveHandler {

    private static RegistReceiveHandler registReceiveHandler;

    public static RegistReceiveHandler getInstance() {
        if (registReceiveHandler == null) {
            registReceiveHandler = new RegistReceiveHandler();
        }
        return registReceiveHandler;
    }

    /**
     * 注册要 推送给第三方app的消息
     */
    public void registReceiveHandler(){
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveNotifyDataMessageHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyIndividualCallIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGoWatchRTSPHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyLivingIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyEmergencyVideoLiveIncommingMessageHandler);
    }


    /**
     * 解绑 推送给第三方app的消息
     */
    public void unregistReceiveHandler(){
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveNotifyDataMessageHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyIndividualCallIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGoWatchRTSPHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyLivingIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyEmergencyVideoLiveIncommingMessageHandler);
    }

    /**
     * 接收到消息
     */
    protected ReceiveNotifyDataMessageHandler mReceiveNotifyDataMessageHandler = terminalMessage -> {
        String json = new Gson().toJson(terminalMessage);
        ThirdSendMessage.getInstance().sendMessageToThird(json,ThirdMessageType.NOTIFY_DATA_MESSAGE);
    };


    /**
     * 被动方个呼来了，选择接听或挂断
     */
    private ReceiveNotifyIndividualCallIncommingHandler receiveNotifyIndividualCallIncommingHandler = (mainMemberName, mainMemberId, individualCallType) -> {
        Map<String,Object> map = new HashMap<>();
        map.put("mainMemberName",mainMemberName);
        map.put("mainMemberId",mainMemberId);
        map.put("individualCallType",individualCallType);
        String json = new Gson().toJson(map);
        ThirdSendMessage.getInstance().sendMessageToThird(json,ThirdMessageType.NOTIFY_INDIVIDUAL_CALL_IN_COMMING);
    };

    /**
     * 观看上报视频
     */
    private ReceiveGoWatchRTSPHandler receiveGoWatchRTSPHandler = terminalMessage -> {
        String json = new Gson().toJson(terminalMessage);
        ThirdSendMessage.getInstance().sendMessageToThird(json,ThirdMessageType.GO_WATCH_RTSP);
    };

    /**
     * 收到别人请求我开启直播的通知
     */
    private ReceiveNotifyLivingIncommingHandler receiveNotifyLivingIncommingHandler = (mainMemberName, mainMemberId, emergencyType)->{
        Map<String,Object> map = new HashMap<>();
        map.put("mainMemberName",mainMemberName);
        map.put("mainMemberId",mainMemberId);
        map.put("emergencyType",emergencyType);
        String json = new Gson().toJson(map);
        ThirdSendMessage.getInstance().sendMessageToThird(json,ThirdMessageType.NOTIFY_LIVING_IN_COMMING);
    };

    /**
     * 收到强制上报图像的通知
     */
    private ReceiveNotifyEmergencyVideoLiveIncommingMessageHandler receiveNotifyEmergencyVideoLiveIncommingMessageHandler = message ->{
        Map<String,Object> map = new HashMap<>();
        map.put("callId",message.getCallId());
        map.put("emergencyType",message.getEmergencyType());
        map.put("liveMemberId",message.getLiveMemberId());
        map.put("liveUniqueNo",message.getLiveUniqueNo());
        map.put("mainMemberId",message.getMainMemberId());
        map.put("mainMemberName",message.getMainMemberName());
        map.put("mainMemberNameBytes",message.getMainMemberNameBytes());
        map.put("mainUniqueNo",message.getMainUniqueNo());
        map.put("messageFromUnit",message.getMessageFromUnit());
        map.put("serializedSize",message.getSerializedSize());
        map.put("streamMediaServerIp",message.getStreamMediaServerIp());
        map.put("streamMediaServerIpBytes",message.getStreamMediaServerIpBytes());
        map.put("version",message.getVersion());
        String json = new Gson().toJson(map);
        ThirdSendMessage.getInstance().sendMessageToThird(json,ThirdMessageType.NOTIFY_EMERGENCY_VIDEO_LIVE_IN_COMMING);
    };
}

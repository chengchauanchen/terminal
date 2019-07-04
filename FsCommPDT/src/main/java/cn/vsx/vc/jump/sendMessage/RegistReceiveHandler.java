package cn.vsx.vc.jump.sendMessage;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyDataMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyIndividualCallIncommingHandler;
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
    }


    /**
     * 解绑 推送给第三方app的消息
     */
    public void unregistReceiveHandler(){
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveNotifyDataMessageHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyIndividualCallIncommingHandler);
    }


    /**
     * 接收到消息
     */
    @SuppressWarnings("unchecked")
    protected ReceiveNotifyDataMessageHandler mReceiveNotifyDataMessageHandler = terminalMessage -> {
        ThirdSendMessage.getInstance().sendMessageToThird("普通消息:"+terminalMessage.toString());
    };


    /**
     * 被动方个呼来了，选择接听或挂断
     */
    private ReceiveNotifyIndividualCallIncommingHandler receiveNotifyIndividualCallIncommingHandler = (mainMemberName, mainMemberId, individualCallType) -> {
        ThirdSendMessage.getInstance().sendMessageToThird("个呼:"+mainMemberName +","+mainMemberId+","+individualCallType);
    };
}

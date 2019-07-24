package ptt.terminalsdk.service;

import ptt.terminalsdk.PushMessageSendResultHandlerAidl;
import ptt.terminalsdk.ServerConnectionEstablishedHandlerAidl;
import ptt.terminalsdk.ServerMessageReceivedHandlerAidl;

/**
 * 作者：xuxiaolong
 * 版本：1.0
 * 创建日期：2019/7/18
 * 描述：
 * 修订历史：
 */
public interface IConnectionClient{

    void registMessageReceivedHandler(ServerMessageReceivedHandlerAidl handler);

    void unregistMessageReceivedHandler(ServerMessageReceivedHandlerAidl handler);

    void sendMessage(byte[] data,PushMessageSendResultHandlerAidl handler);

    void registServerConnectionEstablishedHandler(ServerConnectionEstablishedHandlerAidl handler);

    void unregistServerConnectionEstablishedHandler(ServerConnectionEstablishedHandlerAidl handler);

    void setUuid(byte[] uuid);

    void setServerIp(String serverIp);

    void setServerPort(int serverPort);

    boolean isStarted();

    void start();

    void stop();

    boolean isConnected();
}

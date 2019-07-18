package ptt.terminalsdk;

import ptt.terminalsdk.ServerMessageReceivedHandlerAidl;
import ptt.terminalsdk.PushMessageSendResultHandlerAidl;
import ptt.terminalsdk.ServerConnectionEstablishedHandlerAidl;

interface IMessageService {
    void registMessageReceivedHandler(ServerMessageReceivedHandlerAidl handler);
    void unregistMessageReceivedHandler(ServerMessageReceivedHandlerAidl handler);
    void sendMessage(in byte[] data, PushMessageSendResultHandlerAidl handler);
    void registServerConnectionEstablishedHandler(ServerConnectionEstablishedHandlerAidl handler);
    void unregistServerConnectionEstablishedHandler(ServerConnectionEstablishedHandlerAidl handler);
    void initConnectionClient(in String protocolType);
}

// ServerMessageReceivedHandler.aidl
package ptt.terminalsdk;

interface ServerMessageReceivedHandlerAidl {
    void handle(in byte[] data, in int offset, in int length) ;
}

// PushMessageSendResultHandler.aidl
package ptt.terminalsdk;

interface PushMessageSendResultHandlerAidl {
    void handler(boolean sendOK, String uuid);
}

// IReceivedVSXMessage.aidl
package cn.vsx.vsxsdk;

// Declare any non-default types here with import statements

interface IReceivedVSXMessage {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
//    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
                                              ////            double aDouble, String aString);

      //
      void receivedMessage(String messageJson,int messageType);

     //我 -> 通知第三方app -> 连接我的JumpService
     void noticeConnectJumpService();
}

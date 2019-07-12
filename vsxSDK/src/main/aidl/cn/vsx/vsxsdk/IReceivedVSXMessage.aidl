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

      //通知连接JumpService
     void noticeConnectJumpService();

     //场景描述：绿之云接到警情后，调用SDK接口,但此时融合通信app没有启动，导致接口调用不起来
     //解决办法：只要调用接口连接失败，就重新启动融合通信，并且将之前的操作缓存起来，待连接成功后，通过次接口，重新执行
     void continueBeforeActions();
}

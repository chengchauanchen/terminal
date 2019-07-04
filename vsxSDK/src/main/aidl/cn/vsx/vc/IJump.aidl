// IJump.aidl
package cn.vsx.vc;

// Declare any non-default types here with import statements

interface IJump {
    //界面跳转
    void jumpPage(String sendJson,int commandType);
    //第三方app->通知我->去试图连接 第三方app的消息接收服务
    void noticeConnectReceivedService();
}

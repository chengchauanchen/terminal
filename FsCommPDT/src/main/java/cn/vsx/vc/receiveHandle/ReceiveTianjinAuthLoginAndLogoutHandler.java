package cn.vsx.vc.receiveHandle;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * 天津统一认证客户端登录、退出的通知
 */
public interface ReceiveTianjinAuthLoginAndLogoutHandler extends ReceiveHandler {
    void handler(boolean isLogin);
}

package cn.vsx.vc.receiveHandle;

import org.linphone.core.LinphoneCall;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/1/21
 * 描述：
 * 修订历史：
 */
public interface ReceiveVoipCallEndHandler extends ReceiveHandler{
    void handle(LinphoneCall linphoneCall);
}

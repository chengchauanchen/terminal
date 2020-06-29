package ptt.terminalsdk.receiveHandler;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * 描述：NFC刷入结果回调
 * 修订历史：
 */
public interface ReceiveNFCWriteResultHandler extends ReceiveHandler{
    void handler(int resultCode, String resultDec);
}

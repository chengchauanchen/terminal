package com.vsxin.terminalpad.receiveHandler;

import java.util.List;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2018/12/17
 * 描述：
 * 修订历史：
 */
public interface ReceiveGetHistoryLiveUrlsHandler extends ReceiveHandler{
    void handler(int code,List<String> liveUrl,String name,int memberId);
}

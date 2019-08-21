package com.vsxin.terminalpad.mvp.contract.view;

import java.util.List;

import cn.vsx.hamster.terminalsdk.model.TerminalMessage;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/8/5
 * 描述：
 * 修订历史：
 */
public interface IGroupMessageView extends IBaseMessageView{
    void change2Speaking();

    void change2Waiting();

    void change2Silence();

    void change2Listening();

    void refreshPtt();

    void changeGroup(final int errorCode, final String errorDesc);

    void getGroupLivingList(List<TerminalMessage> beanList, int resultCode, String resultDesc, boolean forNumber);
}

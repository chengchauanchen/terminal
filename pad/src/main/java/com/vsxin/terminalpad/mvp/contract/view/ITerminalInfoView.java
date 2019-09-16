package com.vsxin.terminalpad.mvp.contract.view;


import com.ixiaoma.xiaomabus.architecture.mvp.IBaseView;

import cn.vsx.hamster.terminalsdk.model.Account;

/**
 * @author qzw
 *
 * 地图聚合气泡点击-单个终端详情页
 */

public interface ITerminalInfoView extends IBaseView {

    void showChooseDevicesDialog(Account account, int type);

}

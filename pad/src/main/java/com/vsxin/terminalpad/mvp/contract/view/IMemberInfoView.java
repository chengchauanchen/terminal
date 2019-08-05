package com.vsxin.terminalpad.mvp.contract.view;


import com.ixiaoma.xiaomabus.architecture.mvp.IBaseView;

import cn.vsx.hamster.terminalsdk.model.Account;

/**
 * @author qzw
 *
 * 地图聚合气泡点击-成员列表页
 */

public interface IMemberInfoView extends IBaseView {

    void showChooseDevicesDialog(Account account, int type);

}

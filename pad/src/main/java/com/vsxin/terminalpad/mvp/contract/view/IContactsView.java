package com.vsxin.terminalpad.mvp.contract.view;


import com.ixiaoma.xiaomabus.architecture.mvp.refresh.IRefreshView;
import com.vsxin.terminalpad.mvp.ui.adapter.ContactsAdapter;

import java.util.List;

import cn.vsx.hamster.terminalsdk.model.GroupAndDepartment;

/**
 * @author qzw
 * <p>
 * app模块-通讯录模块
 */
public interface IContactsView extends IRefreshView<GroupAndDepartment> {
    void notifyDataSetChanged(List<GroupAndDepartment> terminalMessages,boolean toTop);
    void showMsg(String msg);
    void showMsg(int resouce);
    ContactsAdapter getAdapter();
}

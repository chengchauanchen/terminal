package com.vsxin.terminalpad.mvp.contract.view;


import com.ixiaoma.xiaomabus.architecture.mvp.IBaseView;
import com.vsxin.terminalpad.mvp.entity.ContactItemBean;

import java.util.List;

/**
 * Created by PC on 2018/11/1.
 */

public interface ISelectMemberView extends IBaseView {
    void selectedView(List<ContactItemBean> selectedMembers);
    List<ContactItemBean> getSelectedMembers();
    void showMsg(String msg);
    void showMsg(int resouce);
    void removeView();
}

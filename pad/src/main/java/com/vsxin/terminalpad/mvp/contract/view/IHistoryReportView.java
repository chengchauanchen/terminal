package com.vsxin.terminalpad.mvp.contract.view;


import com.ixiaoma.xiaomabus.architecture.mvp.IBaseView;
import com.vsxin.terminalpad.mvp.entity.HistoryMediaBean;
import com.vsxin.terminalpad.mvp.entity.MediaBean;

import java.util.List;

import cn.vsx.hamster.terminalsdk.model.Member;

/**
 * Created by PC on 2018/11/1.
 */

public interface IHistoryReportView extends IBaseView {

    void setHistoryMediaDataSource(List<MediaBean> dataSource, String name, int memberId);
}

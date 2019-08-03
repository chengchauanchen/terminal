package com.vsxin.terminalpad.mvp.contract.view;


import com.ixiaoma.xiaomabus.architecture.mvp.refresh.IRefreshView;
import com.vsxin.terminalpad.mvp.entity.NoticeBean;

import java.util.List;

/**
 * @author qzw
 *
 * 左上角 小地图
 */
public interface INoticeView extends IRefreshView<NoticeBean> {

    void notifyDataSetChanged(List<NoticeBean> noticeBeans);
}

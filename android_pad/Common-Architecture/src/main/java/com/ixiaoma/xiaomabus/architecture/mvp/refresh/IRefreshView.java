package com.ixiaoma.xiaomabus.architecture.mvp.refresh;

import com.ixiaoma.xiaomabus.architecture.mvp.IBaseView;

import java.util.List;

/**
 * Created by win on 2018/5/2.
 */

public interface IRefreshView<T> extends IBaseView {

    void refreshOrLoadMore(List<T> data);
    void onFinish();
    void onSuccess();
    boolean isShowLoadDialog();
    void setIsShowLoadDialog(boolean isShowLoadDialog);
}

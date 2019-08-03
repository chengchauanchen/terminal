package com.ixiaoma.xiaomabus.architecture.mvp.lce;

import com.ixiaoma.xiaomabus.architecture.mvp.IBaseView;

import org.apache.log4j.Logger;

/**
 * Created by win on 2018/4/28.
 */

public interface ILce<D> extends IBaseView {

    //显示loading
    void showLoading();
    //显示内容
    void showContent();
    //显示空
    void showEmpty();
    //显示错误
    void showError();

    /**
     * 绑定数据
     * @param data
     */
    void bindData(D data);

    /**
     * 加载数据
     * @param pullToRefresh
     */
    void loadData(boolean pullToRefresh);


}

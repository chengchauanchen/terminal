package com.ixiaoma.xiaomabus.architecture.mvp;

import android.content.Context;

/**
 * Created by win on 2018/4/28.
 * 基础P接口
 */

public interface IBasePresenter<V extends IBaseView> {

    //绑定View
    void attachView(V view);

    //解绑View
    void detachView();

    //获取View
    V getView();

    //获取上下文
    Context getContext();
}

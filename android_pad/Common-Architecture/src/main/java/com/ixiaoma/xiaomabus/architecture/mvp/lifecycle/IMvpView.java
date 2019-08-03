package com.ixiaoma.xiaomabus.architecture.mvp.lifecycle;

import com.ixiaoma.xiaomabus.architecture.mvp.IBasePresenter;
import com.ixiaoma.xiaomabus.architecture.mvp.IBaseView;

/**
 * Created by win on 2018/3/20.
 *  定义：关于MVP的方法规范
 */

public interface IMvpView<V extends IBaseView, P extends IBasePresenter<V>> {
    //创建P
    P createPresenter();

    //获取P
    P getPresenter();

    //设置P
    void setPresenter(P presenter);

    // 获取V fragment中有getView() 重名了
    V getUI();
}

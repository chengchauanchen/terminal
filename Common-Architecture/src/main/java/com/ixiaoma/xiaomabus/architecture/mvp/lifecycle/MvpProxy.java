package com.ixiaoma.xiaomabus.architecture.mvp.lifecycle;

import android.util.Log;

import com.ixiaoma.xiaomabus.architecture.mvp.IBasePresenter;
import com.ixiaoma.xiaomabus.architecture.mvp.IBaseView;

/**
 * Created by win on 2018/3/20.
 *  mvp 的代理对象 拥有目标对象的引用
 *  具体实现应该是activity或view
 *  主要作用：去除目标对象中一些判空的操作
 */

public class MvpProxy<V extends IBaseView,P extends IBasePresenter<V>> implements IMvpView<V,P> {

    private IMvpView<V,P> mvpView;

    /**
     * @param mvpView 具体的实现类 一般是activity、Fragment
     */
    public MvpProxy(IMvpView<V,P> mvpView){
        this.mvpView = mvpView;
    }

    @Override
    public P createPresenter() {
        P presenter = mvpView.getPresenter();
        if(presenter == null){
            presenter = mvpView.createPresenter();
        }
        if(presenter==null){
            throw new NullPointerException("presenter不能为空");
        }
        mvpView.setPresenter(presenter);
        return presenter;
    }

    @Override
    public P getPresenter() {
        P presenter = mvpView.getPresenter();
        if(presenter==null){
            throw new NullPointerException("presenter不能为空");
        }
        return presenter;
    }

    @Override
    public void setPresenter(P presenter) {
        mvpView.setPresenter(presenter);
    }

    @Override
    public V getUI() {
        V view = mvpView.getUI();
        if (view ==null){
            throw new NullPointerException("view不能为空");
        }
        return view;
    }

    public void attachView(){
        Log.i("MVP","绑定View");
        getPresenter().attachView(getUI());
    }

    public void detachView(){
        Log.i("MVP","解除绑定");
        getPresenter().detachView();
    }
}

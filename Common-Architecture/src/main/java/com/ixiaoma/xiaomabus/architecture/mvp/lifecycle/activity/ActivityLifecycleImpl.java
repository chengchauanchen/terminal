package com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.activity;

import android.os.Bundle;
import android.util.Log;

import com.ixiaoma.xiaomabus.architecture.eventbus.AndroidEventBus;
import com.ixiaoma.xiaomabus.architecture.mvp.IBasePresenter;
import com.ixiaoma.xiaomabus.architecture.mvp.IBaseView;
import com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.IMvpView;
import com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.MvpProxy;

/**
 * Created by win on 2018/3/20。
 * 生命周期的具体实现 --目标对象
 *
 * mvp的具体实现
 *
 */

public class ActivityLifecycleImpl<V extends IBaseView, P extends IBasePresenter<V>> implements IActivityLifecycle<V,P>{

    private IMvpView<V,P> iMvpView; //mvp 目标对象的引用

    private MvpProxy<V,P> proxyMvpView; //mvp 的代理对象

    public ActivityLifecycleImpl(IMvpView<V, P> iMvpView) {
        if(iMvpView ==null){
            throw new NullPointerException("mvp目标对象不能为空");
        }
        this.iMvpView = iMvpView;
    }

    public MvpProxy<V,P> getProxyMvpView() {
        if (proxyMvpView ==null){
            proxyMvpView = new MvpProxy<>(iMvpView);
        }
        return proxyMvpView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("activity","onCreate 执行了");
        getProxyMvpView().createPresenter();
        getProxyMvpView().attachView();
    }

    @Override
    public void onStart() {
        Log.d("activity","onStart 执行了");
    }

    @Override
    public void onPause() {
        Log.d("activity","onPause 执行了");
    }

    @Override
    public void onResume() {
        Log.d("activity","onResume 执行了");
    }

    @Override
    public void onRestart() {
        Log.d("activity","onRestart 执行了");
    }

    @Override
    public void onStop() {
        Log.d("activity","onRestart 执行了");
    }

    @Override
    public void onDestroy() {
        Log.d("activity","onDestroy 执行了");
        getProxyMvpView().detachView();
    }

    @Override
    public void onContentChanged() {
        Log.d("activity","onContentChanged 执行了");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d("activity","onSaveInstanceState 执行了");
    }

    @Override
    public void onAttachedToWindow() {
        Log.d("activity","onAttachedToWindow 执行了");
    }
}

package com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.ixiaoma.xiaomabus.architecture.mvp.BaseActivity;
import com.ixiaoma.xiaomabus.architecture.mvp.IBasePresenter;
import com.ixiaoma.xiaomabus.architecture.mvp.IBaseView;
import com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.IMvpView;

/**
 * Created by win on 2018/3/20.
 * mvp + 生命周期 的基类activity
 *
 * mvp ---> 目标对象
 * 生命周期 ---> 代理对象
 */

public abstract class MvpLifecycleActivity<V extends IBaseView, P extends IBasePresenter<V>> extends BaseActivity implements IBaseView, IMvpView<V,P> {

    private IActivityLifecycle<V,P> iActivityLifecycle;

    private P presenter;

    public IActivityLifecycle<V,P> getiActivityLifecycle() {
        if (iActivityLifecycle ==null){
            iActivityLifecycle = new ActivityLifecycleImpl<>(this);
        }
        return iActivityLifecycle;
    }

    // 具体的activity实现
//    @Override
//    public P createPresenter() {
//        return null;
//    }

    @Override
    public P getPresenter() {
        return this.presenter;
    }

    @Override
    public void setPresenter(P presenter) {
        this.presenter = presenter;
    }

    @Override
    public V getUI() {
        return (V)this;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getiActivityLifecycle().onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getiActivityLifecycle().onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getiActivityLifecycle().onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getiActivityLifecycle().onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        getiActivityLifecycle().onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        getiActivityLifecycle().onStop();
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        getiActivityLifecycle().onContentChanged();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        getiActivityLifecycle().onAttachedToWindow();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getiActivityLifecycle().onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getiActivityLifecycle().onDestroy();
    }
}

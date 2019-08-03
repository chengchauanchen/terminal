package com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ixiaoma.xiaomabus.architecture.eventbus.AndroidEventBus;
import com.ixiaoma.xiaomabus.architecture.mvp.IBasePresenter;
import com.ixiaoma.xiaomabus.architecture.mvp.IBaseView;
import com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.IMvpView;
import com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.MvpProxy;

/**
 * Created by win on 2018/3/20.
 * mvp --> 代理对象
 * 生命周期 --> 目标对象
 */

public class FragmentLifecyleImpl<V extends IBaseView, P extends IBasePresenter<V>> implements IFragmentLifecyle {

    private IMvpView<V,P> iMvpView;
    private MvpProxy<V,P> proxyMvpView;

    public FragmentLifecyleImpl(IMvpView<V, P> iMvpView) {
        if(iMvpView ==null){
            throw new NullPointerException("Fragment不能为空");
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
    public void onCreate(@Nullable Bundle savedInstanceState) {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getProxyMvpView().createPresenter();
        return null;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        getProxyMvpView().attachView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onDestroyView() {

    }

    @Override
    public void onDestroy() {
        getProxyMvpView().detachView();
    }
}

package com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ixiaoma.xiaomabus.architecture.mvp.BaseFragment;
import com.ixiaoma.xiaomabus.architecture.mvp.IBasePresenter;
import com.ixiaoma.xiaomabus.architecture.mvp.IBaseView;
import com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.IMvpView;

/**
 * Created by Administrator on 2017/10/20 0020.
 */

public abstract class MvpLifecyleFragment<V extends IBaseView, P extends IBasePresenter<V>> extends BaseFragment implements IBaseView, IMvpView<V, P> {

    private FragmentLifecyleImpl<V, P> fragmentLifecyleImpl;

    private IFragmentLifecyle<V, P> getFragmentLifecyleImpl() {
        if (fragmentLifecyleImpl == null) {
            fragmentLifecyleImpl = new FragmentLifecyleImpl<>(this);
        }
        return fragmentLifecyleImpl;
    }

    private P presenter;

    @Override
    public P getPresenter() {
        return presenter;
    }

    @Override
    public void setPresenter(P presenter) {
        this.presenter = presenter;
    }

    @Override
    public V getUI() {
        return (V) this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentLifecyleImpl().onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getFragmentLifecyleImpl().onCreateView(inflater, container, savedInstanceState);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getFragmentLifecyleImpl().onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getFragmentLifecyleImpl().onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        getFragmentLifecyleImpl().onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        getFragmentLifecyleImpl().onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        getFragmentLifecyleImpl().onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        getFragmentLifecyleImpl().onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getFragmentLifecyleImpl().onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getFragmentLifecyleImpl().onDestroy();
    }

}

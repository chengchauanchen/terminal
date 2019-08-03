package com.ixiaoma.xiaomabus.architecture.mvp.lifecycle;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

import com.ixiaoma.xiaomabus.architecture.mvp.IBasePresenter;
import com.ixiaoma.xiaomabus.architecture.mvp.IBaseView;
import com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.Fragment.MvpLifecyleFragment;


import org.apache.log4j.Logger;

import butterknife.ButterKnife;

/**
 * Created by win on 2018/4/28.
 */

public abstract class MvpFragment<V extends IBaseView, P extends IBasePresenter<V>> extends MvpLifecyleFragment<V, P> implements  OnTouchListener{

    private View viewContent;//缓存视图

    //当前Fragment是否处于可见状态标志，防止因ViewPager的缓存机制而导致回调函数的触发
    private boolean isFragmentVisible;
    private boolean isInit;

    private Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (viewContent == null) {
            viewContent = inflater.inflate(getLayoutResID(), container, false);
            ButterKnife.bind(this,viewContent);
        }
        initViews(viewContent);
        return viewContent;
    }

    /**
     * 当前fragment可见状态发生变化时会回调该方法
     * 如果当前fragment是第一次加载，等待onCreateView后才会回调该方法，其它情况回调时机跟 {@link #setUserVisibleHint(boolean)}一致
     * 在该回调方法中你可以做一些加载数据操作，甚至是控件的操作.
     *
     * @param isVisible true  不可见 -> 可见
     *                  false 可见  -> 不可见
     */
    protected void onFragmentVisibleChange(boolean isVisible) {
        if (isVisible && !isInit ) {
            initData();
            isInit = true;
        }
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //可见，并且没有加载过
        if (!isInit) {
            onFragmentVisibleChange(true);
        }
//        onFragmentVisibleChange(true);
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            isFragmentVisible = true;
        }
        if (viewContent == null) {
            return;
        }
        //可见，并且没有加载过
        if (!isInit && isFragmentVisible) {
            onFragmentVisibleChange(true);
            return;
        }
        //由可见——>不可见 已经加载过
        if (isFragmentVisible) {
            onFragmentVisibleChange(false);
            isFragmentVisible = false;
        }
    }


    //设置Layout
    protected abstract int getLayoutResID();

    //初始化View
    protected abstract void initViews(View view);

    //初始化数据 只加载一次
    protected abstract void initData();


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return true;
    }

    /**
     * 拦截事件，避免事件泄露
     * 在父类调用，子类的点击事件就失灵了，须在子类中调用
     * @param v
     */
    protected void interceptTouch(View v){
        v.setOnTouchListener(this);
    }
}

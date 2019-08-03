package com.ixiaoma.xiaomabus.architecture.mvp.lifecycle;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import butterknife.Unbinder;

import com.gyf.barlibrary.BarHide;
import com.gyf.barlibrary.ImmersionBar;
import com.ixiaoma.xiaomabus.architecture.R;
import com.ixiaoma.xiaomabus.architecture.mvp.IBasePresenter;
import com.ixiaoma.xiaomabus.architecture.mvp.IBaseView;
import com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.activity.MvpLifecycleActivity;


import org.apache.log4j.Logger;

import butterknife.ButterKnife;

/**
 * Created by win on 2018/4/28.
 */

public abstract class MvpActivity<V extends IBaseView, P extends IBasePresenter<V>> extends MvpLifecycleActivity<V, P> {

    private Unbinder unbinder;
    private boolean isInit;

    private Logger logger = Logger.getLogger(getClass());

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.e("activity", "onCreate 执行了");
        super.onCreate(savedInstanceState);
//        //去掉标题栏
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        //去掉状态栏
//        getWindow().setFlags(
//                WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN
//        );
        if (getLayoutResID() != 0) {
            setContentView(getLayoutResID());
            unbinder = ButterKnife.bind(this);
            initViews(savedInstanceState);
            if (!isInit) {
                this.isInit = true;
                initData();
            }
            //初始化沉浸式
            if (isImmersionBarEnabled()) {
                initImmersionBar();
            }
        } else {
            throw new NullPointerException("请设置布局文件");
        }
    }

    //设置Layout
    protected abstract int getLayoutResID();

    //初始化View
    protected abstract void initViews(Bundle savedInstanceState);

    //初始化数据 只加载一次
    protected abstract void initData();

    /**
     * 是否可以使用沉浸式
     * Is immersion bar enabled boolean.
     *
     * @return the boolean
     */
    protected boolean isImmersionBarEnabled() {
        return true;
    }

    protected void initImmersionBar() {
//        //在BaseActivity里初始化
//        ImmersionBar
//                .with(this)
//                .statusBarColor(R.color.colorPrimary)
//                .fitsSystemWindows(true)//解决状态栏和布局重叠问题
//                .statusBarDarkFont(true)//状态栏字体是深色，不写默认为亮色
//                .init();

        ImmersionBar.with(this).hideBar(BarHide.FLAG_HIDE_STATUS_BAR).init();
    }

    @Override
    protected void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
        // 必须调用该方法，防止内存泄漏
        ImmersionBar.with(this).destroy();
    }
}

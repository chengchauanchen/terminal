package com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.activity;

import android.os.Bundle;

import com.ixiaoma.xiaomabus.architecture.mvp.IBasePresenter;
import com.ixiaoma.xiaomabus.architecture.mvp.IBaseView;

/**
 * Created by win on 2018/3/20.
 * activity生命周期接口
 */

public interface IActivityLifecycle<V extends IBaseView, P extends IBasePresenter<V>> {
    void onCreate(Bundle savedInstanceState);

    void onStart();

    void onPause();

    void onResume();

    void onRestart();

    void onStop();

    void onDestroy();

    void onContentChanged();

    void onSaveInstanceState(Bundle outState);

    void onAttachedToWindow();
}

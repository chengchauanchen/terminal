package com.ixiaoma.xiaomabus.architecture.mvp.lce;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.ixiaoma.xiaomabus.architecture.R;
import com.ixiaoma.xiaomabus.architecture.mvp.IBasePresenter;
import com.ixiaoma.xiaomabus.architecture.mvp.IBaseView;
import com.ixiaoma.xiaomabus.architecture.mvp.lce.statemanager.BuilderLceLayout;
import com.ixiaoma.xiaomabus.architecture.mvp.lce.statemanager.LceLayout;
import com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.MvpFragment;

/**
 * Created by win on 2018/5/2.
 * Lce fragment
 */

public abstract class LceFragment<D,V extends IBaseView,P extends IBasePresenter<V>> extends MvpFragment<V,P> implements ILce<D>{

    private LceLayout progress_activity;
    private D data;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initLceView(view);
    }

    private void initLceView(View view){
        if (progress_activity == null) {
            try {
                progress_activity = view.findViewById(R.id.progress_activity);
            } catch (Exception e) {
                progress_activity = null;
                throw new NullPointerException("progress_activity不能为空");
            }
        }
    }

    public LceLayout getProgressActivity(){
        return progress_activity;
    }

    @Override
    public void showLoading() {
        if (progress_activity != null) {
            progress_activity.showLoading();
        }
    }

    @Override
    public void showContent() {
        if (progress_activity != null) {
            progress_activity.showContent();
        }
    }

    @Override
    public void showEmpty() {
        if (progress_activity != null) {
            progress_activity.showEmpty();
        }
    }

    @Override
    public void showError() {
        if (progress_activity != null) {
            new BuilderLceLayout(progress_activity)
                    .setErrorButtonClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            loadData(true);
                        }
                    })
                    .create().showError();
        }
    }

    @Override
    public void bindData(D data) {
        this.data = data;
    }


//    @Override
//    public void showLoading() {
//        mvpLECView.showLoading();
//    }
//
//    @Override
//    public void showContent() {
//        mvpLECView.showContent();
//    }
//
//    @Override
//    public void showError() {
//        mvpLECView.showError();
//    }
//
//    @Override
//    public void bindData(D data) {
//        mvpLECView.bindData(data);
//    }
//
//    @Override
//    public void loadData(boolean pullToRefresh) {
//        mvpLECView.loadData(pullToRefresh);
//    }
}

package com.ixiaoma.xiaomabus.architecture.mvp.lce;

import android.view.View;

import com.ixiaoma.xiaomabus.architecture.R;
import com.ixiaoma.xiaomabus.architecture.mvp.IBasePresenter;
import com.ixiaoma.xiaomabus.architecture.mvp.IBaseView;
import com.ixiaoma.xiaomabus.architecture.mvp.lce.statemanager.BuilderLceLayout;
import com.ixiaoma.xiaomabus.architecture.mvp.lce.statemanager.LceLayout;
import com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.MvpActivity;

/**
 * Created by win on 2018/4/28.
 */

public abstract class LceActivity<D, V extends IBaseView, P extends IBasePresenter<V>> extends MvpActivity<V, P> implements ILce<D> {

    private LceLayout progress_activity;

    private D data;

    /**
     * onContentChanged()是Activity中的一个回调方法 当Activity的布局改动时，
     * 即setContentView()或者addContentView()方法执行完毕时就会调用该方法，
     * 例如，Activity中各种View的findViewById()方法都可以放到该方法中。
     */
    @Override
    public void onContentChanged() {
        super.onContentChanged();
        initLceView(getWindow().getDecorView());
    }

    public LceLayout getProgressActivity(){
        return progress_activity;
    }


    protected void initLceView(View v) {
        if (progress_activity == null) {
            try {
                progress_activity = v.findViewById(R.id.progress_activity);
            } catch (Exception e) {
                progress_activity = null;
                throw new NullPointerException("progress_activity不能为空");
            }
        }
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

//    private LceImpl<D> lceImpl;
//
//    private LceImpl<D> getLceImpl(){
//        if(lceImpl ==null){
//            lceImpl = new LceImpl<>();
//        }
//        return lceImpl;
//    }
//
//    /**
//     * onContentChanged()是Activity中的一个回调方法 当Activity的布局改动时，
//     * 即setContentView()或者addContentView()方法执行完毕时就会调用该方法，
//     * 例如，Activity中各种View的findViewById()方法都可以放到该方法中。
//     */
//    @Override
//    public void onContentChanged() {
//        super.onContentChanged();
//        initLceView(getWindow().getDecorView());
//    }
//
//    private void initLceView(View view){
//        getLceImpl().initLceView(view);
//    }
//
//
//    @Override
//    public void showLoading() {
//        getLceImpl().showLoading();
//    }
//
//    @Override
//    public void showContent() {
//        getLceImpl().showContent();
//    }
//
//    @Override
//    public void showEmpty() {
//        getLceImpl().showEmpty();
//    }
//
//    @Override
//    public void showError() {
//        getLceImpl().showError();
//    }
//
//    @Override
//    public void bindData(D data) {
//        getLceImpl().bindData(data);
//    }
//
//    @Override
//    public void loadData(boolean pullToRefresh) {
//        getLceImpl().loadData(pullToRefresh);
//    }
}

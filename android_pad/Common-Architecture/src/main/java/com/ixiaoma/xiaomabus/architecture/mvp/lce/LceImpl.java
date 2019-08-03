package com.ixiaoma.xiaomabus.architecture.mvp.lce;

import android.view.View;

import com.ixiaoma.xiaomabus.architecture.R;
import com.ixiaoma.xiaomabus.architecture.mvp.lce.statemanager.BuilderLceLayout;
import com.ixiaoma.xiaomabus.architecture.mvp.lce.statemanager.LceLayout;

import org.apache.log4j.Logger;

/**
 * Created by win on 2018/4/28.
 */

public class LceImpl<D> implements ILce<D> {

    private LceLayout progress_activity;

    private D data;

    public void initLceView(View v){
        if(progress_activity ==null){
            try {
                progress_activity = v.findViewById(R.id.progress_activity);
            }catch (Exception e){
                progress_activity = null;
                throw new NullPointerException("progress_activity不能为空");
            }
        }
    }


    @Override
    public void showLoading() {
        if(progress_activity !=null){
            progress_activity.showLoading();
        }
    }

    @Override
    public void showContent() {
        if(progress_activity !=null){
            progress_activity.showContent();
        }
    }

    @Override
    public void showEmpty() {
        if(progress_activity !=null){
            progress_activity.showEmpty();
        }
    }

    @Override
    public void showError() {
        if(progress_activity !=null){
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

    @Override
    public void loadData(boolean pullToRefresh) {

    }

    @Override
    public Logger getLogger(){
        return null;
    }
}

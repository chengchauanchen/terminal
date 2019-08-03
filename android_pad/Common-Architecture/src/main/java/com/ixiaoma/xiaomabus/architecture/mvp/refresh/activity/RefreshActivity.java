package com.ixiaoma.xiaomabus.architecture.mvp.refresh.activity;

import android.os.Bundle;

import com.ixiaoma.xiaomabus.architecture.R;
import com.ixiaoma.xiaomabus.architecture.mvp.IBasePresenter;
import com.ixiaoma.xiaomabus.architecture.mvp.IBaseView;
import com.ixiaoma.xiaomabus.architecture.mvp.lce.LceActivity;
import com.ixiaoma.xiaomabus.architecture.mvp.refresh.IRefreshView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.List;

/**
 * Created by win on 2018/5/2.
 */

public abstract class RefreshActivity<D,V extends IBaseView,P extends IBasePresenter<V>> extends LceActivity<D,V,P> implements IRefreshView<D> {
    public SmartRefreshLayout refreshLayout;
    private boolean isRefrash = true;
    private boolean isShowLoadDialog;

    @Override
    protected void initViews(Bundle savedInstanceState) {
        initRefreshView();
    }

    //初始化加载数据
    @Override
    protected void initData() {
        refreshLayout.autoRefresh();
    }

    //初始化
    private void initRefreshView(){
        refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshLayout) {
                isRefrash = false;
                loadMore();
            }

            @Override
            public void onRefresh(RefreshLayout refreshLayout) {
                isRefrash = true;
                refresh();
            }
        });
    }


    //刷新数据
    protected abstract void refresh();

    //加载更多
    protected abstract void loadMore();


    @Override
    public boolean isShowLoadDialog() {
        return isShowLoadDialog;
    }

    @Override
    public void setIsShowLoadDialog(boolean isShowLoadDialog) {
        this.isShowLoadDialog = isShowLoadDialog;
    }

    //状态页 重新加载
    @Override
    public void loadData(boolean pullToRefresh) {
        setIsShowLoadDialog(true);
    }

    @Override
    public void bindData(D data) {
        super.bindData(data);
        setIsShowLoadDialog(false);


    }

    @Override
    public void refreshOrLoadMore(List<D> data) {
        //将加载框至为不显示
        setIsShowLoadDialog(false);
    }


    @Override
    public void onFinish() {
        if (isRefrash) {
            refreshLayout.finishRefresh();
        } else {
            refreshLayout.finishLoadMore();
        }
        if(isDataEmpty()){
            showError();
        }
        setIsShowLoadDialog(false);
    }

    @Override
    public void onSuccess() {
        showContent();
    }

    protected boolean isDataEmpty() {
        return false;
    }

}

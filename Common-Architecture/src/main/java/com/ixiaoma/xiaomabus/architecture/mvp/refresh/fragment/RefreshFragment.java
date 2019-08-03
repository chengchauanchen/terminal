package com.ixiaoma.xiaomabus.architecture.mvp.refresh.fragment;

import android.view.View;

import com.ixiaoma.xiaomabus.architecture.R;
import com.ixiaoma.xiaomabus.architecture.mvp.IBasePresenter;
import com.ixiaoma.xiaomabus.architecture.mvp.IBaseView;
import com.ixiaoma.xiaomabus.architecture.mvp.lce.LceFragment;
import com.ixiaoma.xiaomabus.architecture.mvp.refresh.IRefreshView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.List;

/**
 * Created by win on 2018/5/3.
 */

public  abstract class RefreshFragment<D,V extends IBaseView,P extends IBasePresenter<V>> extends LceFragment<D,V,P> implements IRefreshView<D> {
    public SmartRefreshLayout refreshLayout;
    private boolean isRefrash = true;
    private boolean isShowLoadDialog;

    @Override
    protected void initViews(View view) {
        initRefreshView(view);
    }

    //初始化加载数据
    @Override
    protected void initData() {
        refreshLayout.autoRefresh();
    }

    //初始化
    private void initRefreshView(View view){
        refreshLayout = view.findViewById(R.id.refreshLayout);
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
    public void refreshOrLoadMore(List<D> data) {
        //将加载框至为不显示
        setIsShowLoadDialog(false);
    }

    /**
     * 非列表数据 可以重写这个方法 来设置获取的数据
     * @param data
     */
    @Override
    public void bindData(D data) {
        super.bindData(data);
        setIsShowLoadDialog(false);


    }

    @Override
    public void onFinish() {
        setIsShowLoadDialog(false);
        if (isRefrash) {
            refreshLayout.finishRefresh();
        } else {
            refreshLayout.finishLoadMore();
        }
        if(isDataEmpty()){
            showError();
        }
    }

    protected boolean isDataEmpty() {
        return false;
    }

    @Override
    public void onSuccess() {
        showContent();
    }
}

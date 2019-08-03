package com.ixiaoma.xiaomabus.architecture.mvp.refresh.activity;

import android.os.Bundle;

import android.support.v7.widget.RecyclerView;
import com.ixiaoma.xiaomabus.architecture.R;
import com.ixiaoma.xiaomabus.architecture.mvp.IBasePresenter;
import com.ixiaoma.xiaomabus.architecture.mvp.IBaseView;
import com.ixiaoma.xiaomabus.architecture.mvp.lce.LceActivity;
import com.ixiaoma.xiaomabus.architecture.mvp.refresh.IRefreshView;
import com.ixiaoma.xiaomabus.architecture.mvp.refresh.adapter.BaseRecycleViewAdapter;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.List;

/**
 * Created by win on 2018/5/2.
 */

public abstract class RefreshRecycleViewActivity<D,V extends IBaseView,P extends IBasePresenter<V>> extends LceActivity<D,V,P> implements IRefreshView<D> {
    public SmartRefreshLayout refreshLayout;
    public RecyclerView recyclerView;
    public boolean isRefrash = true;
    public BaseRecycleViewAdapter mSuperAdapter;
    private boolean isShowLoadDialog;//网络请求是否显示加载框
    public int page=1;//分页

    @Override
    protected void initViews(Bundle savedInstanceState) {
        initRefreshView();
        if(mSuperAdapter==null){
            mSuperAdapter = createAdapter();
            if(mSuperAdapter==null){
                throw new RuntimeException("请设置adapter");
            }
        }
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        initLceView(getWindow().getDecorView());
    }


    //初始化加载数据
    @Override
    protected void initData() {
        refreshLayout.autoRefresh();
    }

    //初始化
    private void initRefreshView(){
        recyclerView = findViewById(R.id.recyclerView);
        refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshLayout) {
                isRefrash = false;
                page++;
                loadMore();
            }

            @Override
            public void onRefresh(RefreshLayout refreshLayout) {
                isRefrash = true;
                page=1;
                refresh();
            }
        });
    }

    public BaseRecycleViewAdapter getAdapter() {
        return mSuperAdapter;
    }

    //刷新数据
    protected abstract void refresh();

    //加载更多
    protected abstract void loadMore();

    //设置Adpater
    protected abstract BaseRecycleViewAdapter createAdapter();

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
        if (isRefrash) {
            mSuperAdapter.getDatas().clear();
            refreshLayout.finishRefresh();
            if (data.size() == 0) { //获取的数据为空
                refreshLayout.setEnableLoadMore(false);
            } else {
                refreshLayout.setNoMoreData(false);
            }
        } else {
            if (data.size() == 0) {
                refreshLayout.finishLoadMoreWithNoMoreData();
            } else {
                refreshLayout.finishLoadMore();
            }
        }
        mSuperAdapter.getDatas().addAll(data);
        mSuperAdapter.notifyDataSetChanged();

        if(isDataNoEmpty()){
            showEmpty();
        }else{
            if(mSuperAdapter.getDatas().size()>0){
                showContent();
                isShowContent();
            }else{
                showEmpty();
            }
        }
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
        if(isDataNoEmpty()){
            showError();
        }else{
            if(mSuperAdapter!=null && mSuperAdapter.getDatas().size()==0){//数据为空
                showError();
            }
        }
        setIsShowLoadDialog(false);
    }

    //数据是否为空
    protected boolean isDataNoEmpty() {
        return false;
    }

    //显示数据
    protected void isShowContent(){

    }

    @Override
    public void onSuccess() {

    }
}

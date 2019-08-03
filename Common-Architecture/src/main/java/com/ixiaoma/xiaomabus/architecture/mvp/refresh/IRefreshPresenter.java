package com.ixiaoma.xiaomabus.architecture.mvp.refresh;

import android.support.v7.widget.RecyclerView;

import com.ixiaoma.xiaomabus.architecture.mvp.refresh.adapter.BaseRecycleViewAdapter;

/**
 * Created by win on 2018/4/28.
 * 基础P接口
 */

public interface IRefreshPresenter<D> {
    void setAdapter(RecyclerView recyclerView, BaseRecycleViewAdapter adapter);
    void setAdapter(RecyclerView recyclerView, BaseRecycleViewAdapter adapter, RecyclerView.LayoutManager layout);
}

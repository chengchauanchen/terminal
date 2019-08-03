package com.ixiaoma.xiaomabus.architecture.mvp.refresh;


import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.ixiaoma.xiaomabus.architecture.mvp.BasePresenter;
import com.ixiaoma.xiaomabus.architecture.mvp.IBaseView;
import com.ixiaoma.xiaomabus.architecture.mvp.refresh.adapter.BaseRecycleViewAdapter;

/**
 * Created by win on 2018/5/2.
 */

public class RefreshPresenter<T,V extends IBaseView> extends BasePresenter<V> implements IRefreshPresenter<T>{

    private BaseRecycleViewAdapter mSuperAdapter;
    private RecyclerView recyclerView;
    public RefreshPresenter(Context mContext) {
        super(mContext);
    }
    /**
     * 设置adapter
     */
    @Override
    public void setAdapter(RecyclerView recyclerView, BaseRecycleViewAdapter adapter){
        this.mSuperAdapter = adapter;
        this.recyclerView = recyclerView;
        //设置布局管理器 默认是垂直方向item布局，可以设置为水平方向，LinearLayoutManager(this,Horizontal,false);第三个参数表示是否反转
        this.recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        this.recyclerView.setItemAnimator(new DefaultItemAnimator());
        //recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        this.recyclerView.setAdapter(mSuperAdapter);
    }

    /**
     * 设置adapter
     */
    @Override
    public void setAdapter(RecyclerView recyclerView, BaseRecycleViewAdapter adapter, RecyclerView.LayoutManager layout){
        this.mSuperAdapter = adapter;
        this.recyclerView = recyclerView;
        //设置布局管理器 默认是垂直方向item布局，可以设置为水平方向，LinearLayoutManager(this,Horizontal,false);第三个参数表示是否反转
        this.recyclerView.setLayoutManager(layout);
        this.recyclerView.setItemAnimator(new DefaultItemAnimator());
        //recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        this.recyclerView.setAdapter(mSuperAdapter);
    }


}

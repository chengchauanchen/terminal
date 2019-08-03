package com.ixiaoma.xiaomabus.architecture.mvp.refresh.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by penny on 2016/10/18 0018.
 */
public abstract class BaseRecycleViewAdapter<T,VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    private Context mContext;

    public BaseRecycleViewAdapter(Context mContext) {
        this.mContext = mContext;
    }
    public Context getContext() {
        return mContext;
    }

    protected List<T> datas = new ArrayList<T>();
    public List<T> getDatas() {
        if (datas==null)
            datas = new ArrayList<T>();
        return datas;
    }
    public void setDatas(List<T> datas) {
        this.datas = datas;
    }


}


package com.ixiaoma.xiaomabus.architecture.mvp.refresh.adapter;

import android.content.Context;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by penny on 2016/10/18 0018.
 */
public abstract class BaseListViewAdapter<T> extends BaseAdapter {
    protected List<T> datas = new ArrayList<T>();

    private Context mContext;

    public BaseListViewAdapter(Context mContext) {
        this.mContext = mContext;
    }
    public Context getContext() {
        return mContext;
    }
    public List<T> getDatas() {
        if (datas==null)
            datas = new ArrayList<T>();
        return datas;
    }
    public void setDatas(List<T> datas) {
        this.datas = datas;
    }

}


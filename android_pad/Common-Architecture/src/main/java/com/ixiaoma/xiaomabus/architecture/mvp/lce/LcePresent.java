package com.ixiaoma.xiaomabus.architecture.mvp.lce;


import android.content.Context;

import com.ixiaoma.xiaomabus.architecture.mvp.BasePresenter;
import com.ixiaoma.xiaomabus.architecture.mvp.IBaseView;

/**
 * Created by win on 2018/4/28.
 */

public abstract class LcePresent<D,V extends IBaseView> extends BasePresenter<V> implements ILcePresent<D>{

    public LcePresent(Context mContext,D data) {
        super(mContext);
    }

    @Override
    public boolean isDataEmpty(D data) {
        return false;
    }
}

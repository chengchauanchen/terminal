package com.zectec.imageandfileselector.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * Created by CWJ on 2016/10/8.
 */
public abstract class BaseFragment extends Fragment {

    protected View rootView;
    /**
     * 控件是否初始化完成
     */
    private boolean isViewCreated;
    /**
     * 数据是否已加载完毕
     */
    private boolean isLoadDataCompleted;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (null == rootView) {
            rootView = inflater.inflate(getLayoutResource(), container, false);
            rootView.setClickable(true);
        }
        isViewCreated = true;
        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isViewCreated && !isLoadDataCompleted) {
            isLoadDataCompleted = true;
            loadData();
        }
    }

    protected abstract boolean isBindEventBusHere();

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (this.isBindEventBusHere()) {
        }
        if (getUserVisibleHint()) {
            isLoadDataCompleted = true;
            loadData();
        }
    }

    private void loadData() {
        Log.e("BaseFragment", "loadData()");
        initView();
    }

    //获取布局文件
    public abstract int getLayoutResource();

    //初始化view
    public abstract void initView();


    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        if (null != rootView) {
//            ((ViewGroup) rootView.getParent()).removeView(rootView);
//        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        doOtherDestory();
    }

    public void doOtherDestory () {}
}

package com.vsxin.terminalpad.mvp.ui.fragment;

import android.view.View;

import com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.MvpFragment;
import com.ixiaoma.xiaomabus.architecture.mvp.refresh.adapter.BaseRecycleViewAdapter;
import com.ixiaoma.xiaomabus.architecture.mvp.refresh.fragment.RefreshRecycleViewFragment;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.presenter.MainMapPresenter;
import com.vsxin.terminalpad.mvp.contract.presenter.MainPresenter;
import com.vsxin.terminalpad.mvp.contract.presenter.MessagePresenter;
import com.vsxin.terminalpad.mvp.contract.view.IMainMapView;
import com.vsxin.terminalpad.mvp.contract.view.IMainView;
import com.vsxin.terminalpad.mvp.contract.view.IMessageView;
import com.vsxin.terminalpad.mvp.entity.MessageBean;
import com.vsxin.terminalpad.mvp.ui.adapter.NoticeAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author qzw
 * <p>
 * app模块-我模块
 */
public class MeFragment extends MvpFragment<IMainView, MainPresenter> implements IMainView {

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_me;
    }

    @Override
    protected void initViews(View view) {

    }

    @Override
    protected void initData() {
    }

    @Override
    public MainPresenter createPresenter() {
        return new MainPresenter(getContext());
    }
}

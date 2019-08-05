package com.vsxin.terminalpad.mvp.ui.fragment;

import android.view.View;

import com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.MvpFragment;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.presenter.MainPresenter;
import com.vsxin.terminalpad.mvp.contract.view.IMainView;

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

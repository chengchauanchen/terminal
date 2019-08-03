package com.vsxin.terminalpad;

import android.os.Bundle;

import com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.MvpActivity;
import com.vsxin.terminalpad.mvp.contract.presenter.MainPresenter;
import com.vsxin.terminalpad.mvp.contract.view.IMainView;

public class MainActivity extends MvpActivity<IMainView, MainPresenter> implements IMainView {

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {

    }

    @Override
    protected void initData() {

    }

    @Override
    public MainPresenter createPresenter() {
        return new MainPresenter(this);
    }
}

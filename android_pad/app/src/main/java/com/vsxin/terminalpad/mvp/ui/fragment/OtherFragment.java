package com.vsxin.terminalpad.mvp.ui.fragment;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;

import com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.MvpFragment;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.presenter.MainMapPresenter;
import com.vsxin.terminalpad.mvp.contract.presenter.MainPresenter;
import com.vsxin.terminalpad.mvp.contract.view.IMainMapView;
import com.vsxin.terminalpad.mvp.contract.view.IMainView;
import com.vsxin.terminalpad.utils.FragmentManage;

import butterknife.BindView;

/**
 * @author
 *
 * app模块
 */
public class OtherFragment extends MvpFragment<IMainView, MainPresenter> implements IMainView {

    @BindView(R.id.bnt_back)
    Button bnt_back;

    @BindView(R.id.bnt_other)
    Button bnt_other;
    @BindView(R.id.bnt_back_main)
    Button bnt_back_main;

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_other;
    }

    @Override
    protected void initViews(View view) {

        interceptTouch(view);
        bnt_back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManage.finishFragment(getActivity());
            }
        });

        bnt_other.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManage.startFragment(getActivity(),new OtherFragment());
            }
        });
        bnt_back_main.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManage.startVsxFragment(getActivity());
            }
        });
    }

    @Override
    protected void initData() {

    }

    @Override
    public MainPresenter createPresenter() {
        return new MainPresenter(getContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("OtherFragment","执行了onDestroy");
    }
}

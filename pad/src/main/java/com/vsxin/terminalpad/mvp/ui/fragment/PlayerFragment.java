package com.vsxin.terminalpad.mvp.ui.fragment;

import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;

import com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.MvpFragment;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.constant.FragmentTagConstants;
import com.vsxin.terminalpad.mvp.contract.presenter.PlayerPresenter;
import com.vsxin.terminalpad.mvp.contract.view.IPlayerView;
import com.vsxin.terminalpad.mvp.entity.MediaBean;

import java.util.List;

import butterknife.BindView;

/**
 * @author qzw
 * 播放器模块
 * 包含：直播拉流rtsp 和 历史上报记录播放mp4
 */
public class PlayerFragment extends MvpFragment<IPlayerView, PlayerPresenter> implements IPlayerView {

    @BindView(R.id.fl_content)
    FrameLayout fl_content;
    private LiveFragment2 liveFragment;
    private HistoryReportFragment historyReportFragment;

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_player;
    }

    @Override
    protected void initViews(View view) {
        initFragment();
    }

    @Override
    protected void initData() {

    }

    @Override
    public void setHistoryMediaDataSource(List<MediaBean> dataSource, String name, int memberId) {
        historyReportFragment.setHistoryMediaDataSource(dataSource,name,memberId);
    }

    @Override
    public void showFragmentForTag(String tag) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        hideFragments(transaction);
        if(TextUtils.equals(FragmentTagConstants.LIVE,tag)){//直播
            transaction.show(liveFragment);
        }else if(TextUtils.equals(FragmentTagConstants.MP4,tag)){//mp4
            transaction.show(historyReportFragment);
        }
        transaction.commitAllowingStateLoss();
    }

    public void initFragment() {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        if (liveFragment == null) {
            liveFragment = new LiveFragment2();
            transaction.add(R.id.fl_content, liveFragment, FragmentTagConstants.LIVE);
        }
        if (historyReportFragment == null) {
            historyReportFragment = new HistoryReportFragment();
            transaction.add(R.id.fl_content, historyReportFragment, FragmentTagConstants.MP4);
        }
        hideFragments(transaction);
        transaction.commitAllowingStateLoss();
    }

    private void hideFragments(FragmentTransaction transaction) {
        if (liveFragment != null) {
            transaction.hide(liveFragment);
        }
        if (historyReportFragment != null) {
            transaction.hide(historyReportFragment);
        }
    }

    @Override
    public PlayerPresenter createPresenter() {
        return new PlayerPresenter(getContext());
    }
}

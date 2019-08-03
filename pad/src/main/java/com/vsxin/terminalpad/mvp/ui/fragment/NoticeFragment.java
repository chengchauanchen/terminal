package com.vsxin.terminalpad.mvp.ui.fragment;

import android.view.View;

import com.ixiaoma.xiaomabus.architecture.mvp.refresh.adapter.BaseRecycleViewAdapter;
import com.ixiaoma.xiaomabus.architecture.mvp.refresh.fragment.RefreshRecycleViewFragment;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.entity.NoticeBean;
import com.vsxin.terminalpad.mvp.contract.presenter.NoticePresenter;
import com.vsxin.terminalpad.mvp.contract.view.INoticeView;
import com.vsxin.terminalpad.mvp.ui.adapter.NoticeAdapter;

import java.util.List;

/**
 * @author qzw
 * <p>
 * 通知模块
 */
public class NoticeFragment extends RefreshRecycleViewFragment<NoticeBean, INoticeView, NoticePresenter> implements INoticeView {

    private NoticePresenter noticePresenter;

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_notice;
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        getPresenter().registReceiveHandler();
        refreshLayout.setEnableRefresh(false);
        refreshLayout.setEnableLoadMore(false);
        getPresenter().setAdapter(recyclerView, mSuperAdapter);
    }

    @Override
    protected void initData() {

    }

    @Override
    public void notifyDataSetChanged(List<NoticeBean> noticeBeans) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshOrLoadMore(noticeBeans);
            }
        });
    }

    @Override
    protected void refresh() {

    }

    @Override
    protected void loadMore() {

    }

    @Override
    protected BaseRecycleViewAdapter createAdapter() {
        return new NoticeAdapter(getContext(),noticePresenter);
    }

    @Override
    public NoticePresenter createPresenter() {
        noticePresenter = new NoticePresenter(getContext());
        return noticePresenter;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPresenter().unRegistReceiveHandler();
    }
}

package com.vsxin.terminalpad.mvp.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.ixiaoma.xiaomabus.architecture.mvp.refresh.adapter.BaseRecycleViewAdapter;
import com.ixiaoma.xiaomabus.architecture.mvp.refresh.fragment.RefreshRecycleViewFragment;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.presenter.NoticePresenter;
import com.vsxin.terminalpad.mvp.contract.view.INoticeView;
import com.vsxin.terminalpad.mvp.entity.NoticeBean;
import com.vsxin.terminalpad.mvp.ui.adapter.NoticeAdapter;
import com.vsxin.terminalpad.utils.Constants;

import java.util.List;

/**
 * @author qzw
 * <p>
 * 通知模块
 */
public class NoticeFragment extends RefreshRecycleViewFragment<NoticeBean, INoticeView, NoticePresenter> implements INoticeView {

    private NoticePresenter noticePresenter;
    private static final String HDICFragment_TAG = "halfDuplexIndividualCallFragment";

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

    /**
     * 开始半双工个呼
     * @param bean
     */
    @Override
    public void startHalfDuplexIndividualCall(NoticeBean bean) {
        HalfDuplexIndividualCallFragment fragment = new HalfDuplexIndividualCallFragment();
        Bundle args = new Bundle();
        args.putString(Constants.MEMBER_NAME, (bean!=null)?bean.getMemberName():"");
        args.putString(Constants.MEMBER_ID, (bean!=null)?bean.getMemberId()+"":"");
        fragment.setArguments(args);
        FragmentManager supportFragmentManager = getActivity().getSupportFragmentManager();
        //replace 会将上一个Fragment干掉
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fl_layer_member_info, fragment, HDICFragment_TAG);
        fragmentTransaction.commit();
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

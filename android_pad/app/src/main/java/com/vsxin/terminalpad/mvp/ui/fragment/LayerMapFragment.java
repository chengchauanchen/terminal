package com.vsxin.terminalpad.mvp.ui.fragment;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.location.LLSInterface;
import com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.MvpFragment;
import com.ixiaoma.xiaomabus.architecture.mvp.refresh.adapter.BaseRecycleViewAdapter;
import com.ixiaoma.xiaomabus.architecture.mvp.refresh.fragment.RefreshRecycleViewFragment;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.constant.MemberTypeConstans;
import com.vsxin.terminalpad.mvp.contract.constant.MemberTypeEnum;
import com.vsxin.terminalpad.mvp.contract.presenter.LayerMapPresenter;
import com.vsxin.terminalpad.mvp.contract.presenter.MainMapPresenter;
import com.vsxin.terminalpad.mvp.contract.presenter.MainPresenter;
import com.vsxin.terminalpad.mvp.contract.presenter.NoticePresenter;
import com.vsxin.terminalpad.mvp.contract.view.ILayerMapView;
import com.vsxin.terminalpad.mvp.contract.view.IMainMapView;
import com.vsxin.terminalpad.mvp.contract.view.IMainView;
import com.vsxin.terminalpad.mvp.contract.view.INoticeView;
import com.vsxin.terminalpad.mvp.entity.NoticeBean;
import com.vsxin.terminalpad.mvp.ui.activity.MainMapActivity;
import com.vsxin.terminalpad.mvp.ui.adapter.LayerMapAdapter;

import java.util.List;

import butterknife.BindView;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * @author qzw
 * <p>
 * 图层模块
 */
public class LayerMapFragment extends RefreshRecycleViewFragment<MemberTypeEnum, ILayerMapView, LayerMapPresenter> implements ILayerMapView {

    @BindView(R.id.ll_layer_map)
    LinearLayout ll_layer_map;

    @BindView(R.id.ll_type_list)
    LinearLayout ll_type_list;

    @BindView(R.id.ll_all_layer)
    LinearLayout ll_all_layer;

    @BindView(R.id.checkbox)
    CheckBox checkbox;

    private boolean isShowTypeListView = false;
    private LayerMapPresenter layerMapPresenter;

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_layer_map;
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        refreshLayout.setEnableRefresh(false);
        refreshLayout.setEnableLoadMore(false);
        getPresenter().setAdapter(recyclerView, mSuperAdapter);

        ll_layer_map.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_type_list.setVisibility(isShowTypeListView ? View.GONE : View.VISIBLE);
                isShowTypeListView = !isShowTypeListView;
            }
        });

        ll_all_layer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                checkbox.setChecked(!checkbox.isChecked());
                getPresenter().notifyDataSetChanged(checkbox.isChecked());

            }
        });
    }

    @Override
    protected void initData() {
        super.initData();
        List<MemberTypeEnum> memberTypeList = MemberTypeConstans.getMemberTypeList();
        refreshOrLoadMore(memberTypeList);
    }

    @Override
    protected void refresh() {

    }

    @Override
    protected void loadMore() {

    }

    @Override
    protected BaseRecycleViewAdapter createAdapter() {
        return new LayerMapAdapter(getContext(), layerMapPresenter);
    }

    @Override
    public LayerMapPresenter createPresenter() {
        layerMapPresenter = new LayerMapPresenter(getContext());
        return layerMapPresenter;
    }
}

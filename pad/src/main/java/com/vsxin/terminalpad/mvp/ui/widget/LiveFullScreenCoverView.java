package com.vsxin.terminalpad.mvp.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import com.ixiaoma.xiaomabus.architecture.mvp.view.layout.MvpLinearLayout;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.presenter.LiveSmallCoverPresenter;
import com.vsxin.terminalpad.mvp.contract.view.ILiveSmallCoverView;

/**
 * @author qzw
 * <p>
 * 直播小框 浮层view
 */
public class LiveFullScreenCoverView extends MvpLinearLayout<ILiveSmallCoverView, LiveSmallCoverPresenter> implements ILiveSmallCoverView {

    private ImageView iv_break_live;

    private OnClickListener quitLiveClickListener;//退出

    public LiveFullScreenCoverView(Context context) {
        super(context);
        initView();
    }

    public LiveFullScreenCoverView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_live_full_screen_cover, this, true);

        //退出
        iv_break_live = findViewById(R.id.iv_break_live);

        //退出
        iv_break_live.setOnClickListener(v -> {
            if (quitLiveClickListener != null) {
                quitLiveClickListener.onClick(v);
            }
        });
    }

    /**
     * 退出
     *
     * @param quitLiveClickListener
     */
    public void setQuitLiveClickListener(OnClickListener quitLiveClickListener) {
        this.quitLiveClickListener = quitLiveClickListener;
    }

    @Override
    public LiveSmallCoverPresenter createPresenter() {
        return new LiveSmallCoverPresenter(getSuperContext());
    }
}

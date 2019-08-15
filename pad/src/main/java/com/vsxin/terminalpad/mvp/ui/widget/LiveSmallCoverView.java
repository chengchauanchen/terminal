package com.vsxin.terminalpad.mvp.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
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
public class LiveSmallCoverView extends MvpLinearLayout<ILiveSmallCoverView, LiveSmallCoverPresenter> implements ILiveSmallCoverView {

    private TextView tv_member_name;
    private TextView tv_member_no;
    private TextView tv_time;
    private TextView tv_group_call_member;
    private ImageView iv_group_call;
    private ImageView iv_share_live;
    private ImageView iv_full_screen;
    private ImageView iv_break_live;

    public LiveSmallCoverView(Context context) {
        super(context);
    }

    public LiveSmallCoverView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getSuperContext()).inflate(R.layout.view_live_small_cover, this, true);

        //上报人名称
        tv_member_name = findViewById(R.id.tv_member_name);
        //警号
        tv_member_no = findViewById(R.id.tv_member_no);
        //上报计时
        tv_time = findViewById(R.id.tv_time);

        //当前组呼人名称
        tv_group_call_member = findViewById(R.id.tv_group_call_member);

        //组呼按钮
        iv_group_call = findViewById(R.id.iv_group_call);
        //分享
        iv_share_live = findViewById(R.id.iv_share_live);
        //全屏
        iv_full_screen = findViewById(R.id.iv_full_screen);

        //退出
        iv_break_live = findViewById(R.id.iv_break_live);

    }

    @Override
    public LiveSmallCoverPresenter createPresenter() {
        return new LiveSmallCoverPresenter(getSuperContext());
    }
}

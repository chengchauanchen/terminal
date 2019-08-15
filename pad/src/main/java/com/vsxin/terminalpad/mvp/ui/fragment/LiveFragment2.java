package com.vsxin.terminalpad.mvp.ui.fragment;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.MvpFragment;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.presenter.LivePresenter2;
import com.vsxin.terminalpad.mvp.contract.view.ILiveView2;
import com.vsxin.terminalpad.mvp.ui.widget.LivePlayer;
import com.vsxin.terminalpad.receiveHandler.ReceiveStopPullLiveHandler;
import com.vsxin.terminalpad.utils.OperateReceiveHandlerUtilSync;

import butterknife.BindView;


/**
 * @author qzw
 * <p>
 * 直播模块
 */
public class LiveFragment2 extends MvpFragment<ILiveView2, LivePresenter2> implements ILiveView2 {

    @BindView(R.id.rl_live_view)
    RelativeLayout rl_live_view;

    @BindView(R.id.lp_live_player)
    LivePlayer livePlayer;

    @BindView(R.id.iv_break_live)
    ImageView iv_break_live;//退出直播

    @BindView(R.id.iv_max_screen)
    ImageView iv_max_screen;//全屏

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_live2;
    }

    @Override
    protected void initViews(View view) {
        getPresenter().registReceiveHandler();

        //退出
        iv_break_live.setOnClickListener(v -> {
            livePlayer.stopPullLive();
            //OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveStopPullLiveHandler.class);
        });

        //全屏
        iv_max_screen.setOnClickListener(v -> livePlayer.enterFullScreen());
    }

    @Override
    protected void initData() {

    }

    @Override
    public void startPullLive(String rtspURL) {
        livePlayer.startPullLive(rtspURL);
    }

    @Override
    public void stopPullLive() {
        livePlayer.stopPullLive();
    }

    @Override
    public LivePresenter2 createPresenter() {
        return new LivePresenter2(getContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPresenter().unregistReceiveHandler();
    }
}

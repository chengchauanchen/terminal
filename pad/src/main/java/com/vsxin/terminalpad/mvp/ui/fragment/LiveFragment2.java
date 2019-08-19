package com.vsxin.terminalpad.mvp.ui.fragment;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.MvpFragment;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.presenter.LivePresenter2;
import com.vsxin.terminalpad.mvp.contract.view.ILiveView2;
import com.vsxin.terminalpad.mvp.ui.widget.LiveFullScreenCoverView;
import com.vsxin.terminalpad.mvp.ui.widget.LivePlayer;
import com.vsxin.terminalpad.mvp.ui.widget.LiveSmallCoverView;
import com.vsxin.terminalpad.receiveHandler.ReceiveStopPullLiveHandler;
import com.vsxin.terminalpad.utils.OperateReceiveHandlerUtilSync;

import butterknife.BindView;
import cn.vsx.hamster.terminalsdk.model.Member;


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
    private LiveSmallCoverView liveSmallCoverView;
    private LiveFullScreenCoverView liveFullScreenCoverView;

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_live2;
    }

    @Override
    protected void initViews(View view) {
        getPresenter().registReceiveHandler();
    }

    @Override
    protected void initData() {
        liveSmallCoverView = new LiveSmallCoverView(getContext());
        liveFullScreenCoverView = new LiveFullScreenCoverView(getContext());

        livePlayer.setSmallCoverView(liveSmallCoverView);
        livePlayer.setFullScreenCoverView(liveFullScreenCoverView);

        //退出
        liveSmallCoverView.setQuitLiveClickListener(v -> livePlayer.stopPullLive());
        //全屏
        liveSmallCoverView.setFullScreenClickListener(v -> livePlayer.enterFullScreen());
        //关闭全屏
        liveFullScreenCoverView.setQuitLiveClickListener(v -> livePlayer.exitFullScreen());
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
    public void setMemberInfo(Member member) {
        liveSmallCoverView.setMemberInfo(member.getName(),member.getNo()+"");
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

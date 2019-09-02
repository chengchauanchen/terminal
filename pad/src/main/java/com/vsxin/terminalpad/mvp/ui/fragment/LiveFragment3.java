package com.vsxin.terminalpad.mvp.ui.fragment;

import android.view.View;
import android.widget.RelativeLayout;

import com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.MvpFragment;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.presenter.LivePresenter2;
import com.vsxin.terminalpad.mvp.contract.presenter.LivePresenter3;
import com.vsxin.terminalpad.mvp.contract.view.ILiveView2;
import com.vsxin.terminalpad.mvp.contract.view.ILiveView3;
import com.vsxin.terminalpad.mvp.entity.MediaBean;
import com.vsxin.terminalpad.mvp.ui.widget.HistoryReportPlayer;
import com.vsxin.terminalpad.mvp.ui.widget.LiveFullScreenCoverView;
import com.vsxin.terminalpad.mvp.ui.widget.LivePlayer;
import com.vsxin.terminalpad.mvp.ui.widget.LiveSmallCoverView;

import java.util.List;

import butterknife.BindView;
import cn.vsx.hamster.terminalsdk.model.Member;


/**
 * @author qzw
 * <p>
 * 直播模块
 */
public class LiveFragment3 extends MvpFragment<ILiveView3, LivePresenter3> implements ILiveView3 {

    @BindView(R.id.rl_live_view)
    RelativeLayout rl_live_view;

    @BindView(R.id.lp_live_player)
    LivePlayer livePlayer;

    @BindView(R.id.hrp_video_player)
    HistoryReportPlayer hrp_video_player;

    private LiveSmallCoverView liveSmallCoverView;
    private LiveFullScreenCoverView liveFullScreenCoverView;

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_live3;
    }

    @Override
    protected void initViews(View view) {
        getPresenter().registReceiveHandler();
    }

    @Override
    protected void initData() {
        initLivePlayer();
        initHistoryReportPlayer();
    }

    @Override
    public void playerHistory(){
        hrp_video_player.setVisibility(View.VISIBLE);
        livePlayer.setVisibility(View.GONE);
        List<MediaBean> testData = hrp_video_player.getPresenter().getTestData();
        hrp_video_player.setTestData(testData);
        hrp_video_player.play(0);
    }

    private void initHistoryReportPlayer(){

    }

    private void initLivePlayer(){
        liveSmallCoverView = new LiveSmallCoverView(getContext());
        liveFullScreenCoverView = new LiveFullScreenCoverView(getContext());

        livePlayer.setSmallCoverView(liveSmallCoverView);
        livePlayer.addSmallCoverView();//默认添加小屏浮层
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
        hrp_video_player.setVisibility(View.GONE);
        livePlayer.setVisibility(View.VISIBLE);
        livePlayer.startPullLive(rtspURL);
    }

    @Override
    public void stopPullLive() {
        livePlayer.stopPullLive();
    }

    @Override
    public void setMemberInfo(Member member) {
        liveSmallCoverView.setMemberInfo(member.getName(), member.getNo() + "");
    }

    @Override
    public LivePresenter3 createPresenter() {
        return new LivePresenter3(getContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPresenter().unregistReceiveHandler();
    }
}

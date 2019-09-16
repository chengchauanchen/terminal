package com.vsxin.terminalpad.mvp.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.MvpFragment;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.presenter.LivePresenter2;
import com.vsxin.terminalpad.mvp.contract.view.ILiveView2;
import com.vsxin.terminalpad.mvp.entity.InviteMemberExceptList;
import com.vsxin.terminalpad.mvp.entity.InviteMemberLiverMember;
import com.vsxin.terminalpad.mvp.ui.widget.LiveFullScreenCoverView;
import com.vsxin.terminalpad.mvp.ui.widget.LivePlayer;
import com.vsxin.terminalpad.mvp.ui.widget.LiveSmallCoverView;
import com.vsxin.terminalpad.prompt.PromptManager;
import com.vsxin.terminalpad.receiveHandler.ReceiveStopPullLiveHandler;
import com.vsxin.terminalpad.utils.Constants;
import com.vsxin.terminalpad.utils.OperateReceiveHandlerUtilSync;

import java.util.ArrayList;
import java.util.List;

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
        livePlayer.addSmallCoverView();//默认添加小屏浮层
        livePlayer.setFullScreenCoverView(liveFullScreenCoverView);

        //退出
        liveSmallCoverView.setQuitLiveClickListener(v -> livePlayer.stopPullLive());
        //全屏
        liveSmallCoverView.setFullScreenClickListener(v -> livePlayer.enterFullScreen());

        //退出全屏
        liveFullScreenCoverView.setSmallScreenClickListener(v -> livePlayer.exitFullScreen());
        //关闭全屏
        liveFullScreenCoverView.setQuitLiveClickListener(v -> livePlayer.stopPullLive());
    }

    /**
     * 设置分享监听
     * @param shareLiveClickListener
     */
    @Override
    public void setShareLiveClickListener(OnClickListener shareLiveClickListener){
        liveSmallCoverView.setShareLiveClickListener(shareLiveClickListener);
        liveFullScreenCoverView.setShareLiveClickListener(shareLiveClickListener);
    }


    @Override
    public void startPullLive(String rtspURL) {
        if(livePlayer!=null){
            livePlayer.startPullLive(rtspURL);
        }
    }

    @Override
    public void stopPullLive() {
        if(livePlayer!=null && livePlayer.getLiveMode()!=LivePlayer.MODE_DEFAULT_LIVE){
            getLogger().info("stopPullLive");
            livePlayer.stopPullLive();
        }else{
            PromptManager.getInstance().stopRing();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        getLogger().info("hidden:"+hidden);
        if(hidden){
            stopPullLive();
        }
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

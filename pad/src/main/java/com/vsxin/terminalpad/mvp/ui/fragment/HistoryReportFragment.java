package com.vsxin.terminalpad.mvp.ui.fragment;

import android.media.MediaPlayer;
import android.util.Log;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener;
import com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.MvpFragment;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.presenter.HistoryReportPresenter;
import com.vsxin.terminalpad.mvp.contract.view.IHistoryReportView;
import com.vsxin.terminalpad.mvp.entity.HistoryMediaBean;
import com.vsxin.terminalpad.mvp.entity.MediaBean;
import com.vsxin.terminalpad.mvp.ui.widget.CustomMediaPlayer.PlayerListener;
import com.vsxin.terminalpad.mvp.ui.widget.HistoryReportPlayer;
import com.vsxin.terminalpad.mvp.ui.widget.HistoryReportPlayer2;
import com.vsxin.terminalpad.mvp.ui.widget.HistoryReportPlayerCoverView;
import com.vsxin.terminalpad.utils.TimeUtil;
import com.vsxin.terminalpad.utils.Timer;

import java.util.List;

import butterknife.BindView;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * @author qzw
 * 播放历史上报 mp4文件
 */
public class HistoryReportFragment extends MvpFragment<IHistoryReportView, HistoryReportPresenter> implements IHistoryReportView {

    @BindView(R.id.hrp_video_player)
    HistoryReportPlayer2 hrp_video_player;

    private HistoryReportPlayerCoverView historyReportPlayerCoverView;
    private Timer timer;
    private List<HistoryMediaBean> testData;

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_history_report;
    }

    @Override
    protected void initViews(View view) {
        timer = new Timer(() -> {
            updateView();
        });

        hrp_video_player.setListener(new PlayerListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                timer.start();
            }

            @Override
            public void onSeekComplete(MediaPlayer mp) {
                timer.start();
            }

            @Override
            public void onCompletion(MediaPlayer mp) {
                timer.stop();
            }

            @Override
            public void onError(MediaPlayer mp) {
                timer.stop();
            }

            //主动关闭后，不走监听回调了，只能单独搞个回调方法
            @Override
            public void onStop() {
                timer.stop();
            }
        });
    }

    @Override
    protected void initData() {
        historyReportPlayerCoverView = new HistoryReportPlayerCoverView(getContext());
        hrp_video_player.setHistoryReportPlayerCoverView(historyReportPlayerCoverView);
        hrp_video_player.addCoverView();

        //关闭
        historyReportPlayerCoverView.setQuitClickListener(v -> {
            hrp_video_player.stopPlay();
        });

        //选择
        historyReportPlayerCoverView.setChoiceClickListener(v -> {
            ToastUtil.showToast(getContext(), "选择");

            if (hrp_video_player.getCurrentMode() != HistoryReportPlayer.MODE_FULL_SCREEN) {
                hrp_video_player.enterFullScreen();
            } else {
                hrp_video_player.exitFullScreen();
            }

        });

        //暂停/播放
        historyReportPlayerCoverView.setPauseContinueClickListener(v -> {
            if (hrp_video_player.isPlaying()) {
                hrp_video_player.pause();
                historyReportPlayerCoverView.pauseView();
            } else {
                hrp_video_player.continuePlay();
                historyReportPlayerCoverView.playView();
            }
        });

        //播放
        historyReportPlayerCoverView.setPlayerClickListener(v -> {
            if (!hrp_video_player.isPlaying()) {
                hrp_video_player.continuePlay();
                historyReportPlayerCoverView.playView();
            }
        });

        testData = getPresenter().getTestData();
        historyReportPlayerCoverView.setHistoryMedia(testData);
        historyReportPlayerCoverView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (hrp_video_player != null) {
                    hrp_video_player.play(testData.get(position).getUrl());
                }
            }
        });
    }

    private void updateView(){
        int position = hrp_video_player.getCurrentPosition();
        getLogger().info("position:" + position);
        if(position < 0){
            return;
        }
        int sMax = hrp_video_player.getDuration();
        historyReportPlayerCoverView.setSeekBarMaxProgress(sMax);
        historyReportPlayerCoverView.setSeekBarProgress(position);

        historyReportPlayerCoverView.setMaxTime(TimeUtil.getTime(sMax));
        historyReportPlayerCoverView.setCurrentTime(TimeUtil.getTime(position));
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            playerHistory();
        } else {
            stopPlay();
        }
    }

    /**
     * 停止播放
     */
    public void stopPlay() {
        if (hrp_video_player != null && hrp_video_player.isPlaying()) {
            hrp_video_player.stopPlay();
        }
    }

    public void playerHistory() {
        if (hrp_video_player != null) {
            hrp_video_player.play(testData.get(0).getUrl());
        }
    }

    @Override
    public HistoryReportPresenter createPresenter() {
        return new HistoryReportPresenter(getContext());
    }
}

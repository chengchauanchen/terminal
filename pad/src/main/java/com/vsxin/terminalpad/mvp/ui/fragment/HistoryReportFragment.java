package com.vsxin.terminalpad.mvp.ui.fragment;

import android.media.MediaPlayer;
import android.util.Log;
import android.view.View;

import com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.MvpFragment;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.presenter.HistoryReportPresenter;
import com.vsxin.terminalpad.mvp.contract.view.IHistoryReportView;
import com.vsxin.terminalpad.mvp.entity.MediaBean;
import com.vsxin.terminalpad.mvp.ui.widget.HistoryReportPlayer;
import com.vsxin.terminalpad.mvp.ui.widget.HistoryReportPlayer.HistoryReportPlayerListener;
import com.vsxin.terminalpad.mvp.ui.widget.HistoryReportPlayerCoverView;
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
    HistoryReportPlayer hrp_video_player;

    private HistoryReportPlayerCoverView historyReportPlayerCoverView;
    private Timer timer;

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_history_report;
    }

    @Override
    protected void initViews(View view) {
        timer = new Timer(() -> {
            updataView();
        });

        hrp_video_player.setListener(new HistoryReportPlayerListener() {
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
        });
    }

    @Override
    protected void initData() {
        historyReportPlayerCoverView = new HistoryReportPlayerCoverView(getContext());
        hrp_video_player.setHistoryReportPlayerCoverView(historyReportPlayerCoverView);
        hrp_video_player.addCoverView();

        historyReportPlayerCoverView.setQuitClickListener(v -> {
            hrp_video_player.stopPlay();
        });

        historyReportPlayerCoverView.setChoiceClickListener(v -> {
            ToastUtil.showToast(getContext(), "选择");

            if (hrp_video_player.getCurrentMode() != HistoryReportPlayer.MODE_FULL_SCREEN) {
                hrp_video_player.enterFullScreen();
            } else {
                hrp_video_player.exitFullScreen();
            }

        });

        historyReportPlayerCoverView.setPauseContinueClickListener(v -> {
            if (hrp_video_player.isPlaying()) {
                hrp_video_player.pause();
                historyReportPlayerCoverView.pauseView();
            } else {
                hrp_video_player.continuePlay();
                historyReportPlayerCoverView.playView();
            }
        });

        historyReportPlayerCoverView.setPlayerClickListener(v -> {
            if (!hrp_video_player.isPlaying()) {
                hrp_video_player.continuePlay();
                historyReportPlayerCoverView.playView();
            }
        });
    }

    private void updataView(){
        int position = hrp_video_player.getCurrentPosition();
        Log.e("PlayLiveHistoryActivity", "position:" + position);
        if(position < 0){
            return;
        }
        int sMax = hrp_video_player.getDuration();
        historyReportPlayerCoverView.setSeekBarMaxProgress(sMax);
        historyReportPlayerCoverView.setSeekBarProgress(position);
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
            List<MediaBean> testData = hrp_video_player.getPresenter().getTestData();
            hrp_video_player.setTestData(testData);
            hrp_video_player.play(0);
        }
    }

    @Override
    public HistoryReportPresenter createPresenter() {
        return new HistoryReportPresenter(getContext());
    }
}

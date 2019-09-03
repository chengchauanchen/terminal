package com.vsxin.terminalpad.mvp.ui.widget;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener;
import com.ixiaoma.xiaomabus.architecture.mvp.view.layout.MvpLinearLayout;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.presenter.HistoryReportPlayerCoverPresenter;
import com.vsxin.terminalpad.mvp.contract.presenter.LiveSmallCoverPresenter;
import com.vsxin.terminalpad.mvp.contract.view.IHistoryReportPlayerCoverView;
import com.vsxin.terminalpad.mvp.contract.view.ILiveSmallCoverView;
import com.vsxin.terminalpad.mvp.entity.HistoryMediaBean;
import com.vsxin.terminalpad.mvp.ui.adapter.PlayHistoryVideoAdapter;
import com.vsxin.terminalpad.utils.ResUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author qzw
 * <p>
 * 播放历史上报记录  浮层view
 */
public class HistoryReportPlayerCoverView extends MvpLinearLayout<IHistoryReportPlayerCoverView, HistoryReportPlayerCoverPresenter> implements IHistoryReportPlayerCoverView {

    private ImageView iv_close;
    private TextView tv_theme;
    private ImageView iv_pause;
    private LinearLayout ll_seek_bar;
    private ImageView iv_pause_continue;
    private TextView tv_current_time;
    private SeekBar seek_bar;
    private TextView tv_max_time;
    private TextView tv_choice;

    private OnClickListener quitClickListener;//退出
    private OnClickListener pauseContinueClickListener;//暂停/继续
    private OnClickListener playerClickListener;//播放
    private OnClickListener choiceClickListener;//选择视频list
    private LinearLayout ll_list;
    private RecyclerView recyclerview;
    private PlayHistoryVideoAdapter playLiveAdapter;

    private List<HistoryMediaBean> historyMediaBeanList = new ArrayList<>();


    private OnItemClickListener onItemClickListener;

    public HistoryReportPlayerCoverView(Context context) {
        super(context);
        initView();
    }

    public HistoryReportPlayerCoverView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        getLogger().info("initView()");

        LayoutInflater.from(getContext()).inflate(R.layout.view_video_player_cover, this, true);
        //关闭
        iv_close = findViewById(R.id.iv_close);
        //标题
        tv_theme = findViewById(R.id.tv_theme);
        //暂停
        iv_pause = findViewById(R.id.iv_pause);
        //进度条
        ll_seek_bar = findViewById(R.id.ll_seek_bar);
        //暂停/继续
        iv_pause_continue = findViewById(R.id.iv_pause_continue);
        //播放时长
        tv_current_time = findViewById(R.id.tv_current_time);
        //进度条
        seek_bar = findViewById(R.id.seek_bar);
        //视频时长
        tv_max_time = findViewById(R.id.tv_max_time);
        //选择
        tv_choice = findViewById(R.id.tv_choice);

        //历史播放记录列表
        ll_list = findViewById(R.id.ll_list);
        recyclerview = findViewById(R.id.recyclerview);

        //关闭
        iv_close.setOnClickListener(v -> {
            if (quitClickListener != null) {
                quitClickListener.onClick(v);
            }
        });

        //暂停/继续
        iv_pause_continue.setOnClickListener(v -> {
            if (pauseContinueClickListener != null) {
                pauseContinueClickListener.onClick(v);
            }
        });

        //暂停按钮点击播放
        iv_pause.setOnClickListener(v -> {
            if (playerClickListener != null) {
                playerClickListener.onClick(v);
            }
        });

        //选择视频list
        tv_choice.setOnClickListener(v -> {
            if (choiceClickListener != null) {
                choiceClickListener.onClick(v);
            }
        });

        initRecyclerView();
    }

    /**
     * 初始化 RecyclerView
     */
    private void initRecyclerView() {
        recyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        playLiveAdapter = new PlayHistoryVideoAdapter(R.layout.item_play_history_video, historyMediaBeanList);
        recyclerview.setAdapter(playLiveAdapter);
    }

    /**
     * 设置item点击事件
     * @param onItemClickListener
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        playLiveAdapter.setOnItemClickListener(onItemClickListener);
    }

    /**
     * 设置数据源
     * @param historyMediaBeanList
     */
    public void setHistoryMedia(List<HistoryMediaBean> historyMediaBeanList){
        this.historyMediaBeanList.clear();
        this.historyMediaBeanList.addAll(historyMediaBeanList);
        playLiveAdapter.notifyDataSetChanged();
    }


    /**
     * 暂停
     */
    public void pauseView(){
        iv_pause_continue.setImageResource(R.mipmap.on_pause);
        iv_pause.setVisibility(View.VISIBLE);
    }

    /**
     * 播放
     */
    public void playView(){
        iv_pause_continue.setImageResource(R.mipmap.continue_play);
        iv_pause.setVisibility(View.GONE);
    }

    /**
     * 设置最大进度
     * @param progress
     */
    public void setSeekBarMaxProgress(int progress){
        seek_bar.setMax(progress);
    }

    /**
     * 设置实时进度
     * @param progress
     */
    public void setSeekBarProgress(int progress){
        seek_bar.setProgress(progress);
    }

    /**
     * 设置当前播放时长
     * @param currentTime
     */
    public void setCurrentTime(String currentTime){
        tv_current_time.setText(currentTime);
    }

    /**
     * 设置最大时长
     * @param maxTime
     */
    public void setMaxTime(String maxTime){
        tv_max_time.setText(maxTime);
    }

    /**************************设置监听*****************************/

    /**
     * 退出
     * @param quitClickListener
     */
    public void setQuitClickListener(OnClickListener quitClickListener) {
        this.quitClickListener = quitClickListener;
    }

    /**
     * 暂停/继续
     * @param pauseContinueClickListener
     */
    public void setPauseContinueClickListener(OnClickListener pauseContinueClickListener) {
        this.pauseContinueClickListener = pauseContinueClickListener;
    }

    /**
     * 播放
     * @param playerClickListener
     */
    public void setPlayerClickListener(OnClickListener playerClickListener) {
        this.playerClickListener = playerClickListener;
    }

    /**
     * 选择视频list
     * @param choiceClickListener
     */
    public void setChoiceClickListener(OnClickListener choiceClickListener) {
        this.choiceClickListener = choiceClickListener;
    }

    @Override
    public HistoryReportPlayerCoverPresenter createPresenter() {
        return new HistoryReportPlayerCoverPresenter(getSuperContext());
    }
}

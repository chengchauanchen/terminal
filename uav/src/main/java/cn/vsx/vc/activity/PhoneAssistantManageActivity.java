package cn.vsx.vc.activity;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.vsx.hamster.terminalsdk.model.CallRecord;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveDownloadProgressHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetRtspStreamUrlHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyIndividualCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyLivingIncommingHandler;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.PhoneAssistantListAdapter;
import cn.vsx.vc.record.MediaManager;
import cn.vsx.vc.utils.ToastUtil;
import cn.vsx.vc.view.PullToRefreshLayout;
import cn.vsx.vc.view.PullableListView;
import cn.vsx.vc.view.RoundProgressBar;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.voip.DownloadCompleteListener;

/**
 * 电话助手界面
 * Created by 2018/8/2.
 */

public class PhoneAssistantManageActivity extends BaseActivity implements View.OnClickListener {


    private Handler mHandler = new Handler();
    private List<CallRecord> callRecords = new ArrayList<>();
    private PullToRefreshLayout pull_refresh_layout;
    private PullableListView pl_phone_assistant;
    private PhoneAssistantListAdapter phoneAssistantListAdapter;
    private ImageView ivback;
    private boolean RUN = true;
    private final int DOWNLOAD = 0;
    private final int PAUSE = 1;
    private final int COMPLETED = 3;
    private final int PLAY = 2;
    public RoundProgressBar downloadProgressBar;
    public ProgressBar playProgressBar;
    public ImageView status;
    public Thread progressUpdateThread;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case PLAY:
                    playProgressBar.setVisibility(View.VISIBLE);
                    status.setImageResource(R.drawable.downloading);
                    break;
                case PAUSE:
                    status.setImageResource(R.drawable.downloaded);
                    break;
                case COMPLETED:
                    logger.error("播放完成");
                    status.setImageResource(R.drawable.downloaded);
                    playProgressBar.setVisibility(View.GONE);
                    break;
                case DOWNLOAD:
                    downloadProgressBar.setProgress(100);
                    status.setImageResource(R.drawable.downloaded);

                    break;
            }

        }
    };
    private int lastPosition = -1;
    private ProgressBar lastProgressBar;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_phone_assistant;
    }


    @Override
    public void initView() {
        pull_refresh_layout = findViewById(R.id.pull_refresh_layout);
        pl_phone_assistant = findViewById(R.id.pl_phone_assistant);
        ivback = findViewById(R.id.iv_back);
        phoneAssistantListAdapter = new PhoneAssistantListAdapter(callRecords, this);
        pl_phone_assistant.setAdapter(phoneAssistantListAdapter);
    }

    private long lastSearchTime=0;
    private long currentTime=0;

    @Override
    public void initListener() {

        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveDownloadProgressHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveNotifyIndividualCallIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveNotifyLivingIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGetRtspStreamUrlHandler);

        pl_phone_assistant.setOnItemClickListener((parent, view, position, id) -> {

            currentTime=System.currentTimeMillis();
            if(currentTime - lastSearchTime<1000){
                return;
            }
            lastSearchTime=currentTime;

            logger.info("点击了条目 " + position);
            //找到点击ITEM里的控件
            downloadProgressBar = view.findViewById(R.id.play_record);
            status = view.findViewById(R.id.status);
            playProgressBar = view.findViewById(R.id.progress_bar);
            //没有录音的条目点击不处理
            if (callRecords.get(position).getPath() == null || callRecords.get(position).getPath().equals("") ||callRecords.get(position).getCallRecords().equals("1")||callRecords.get(position).getCallRecords().equals("2")) {
                return;
            }
            //通过path判断是否是网络地址需要下载
            if (callRecords.get(position).getPath().startsWith("http")) {
                callRecords.get(position).setDownLoad(false);
            } else {
                callRecords.get(position).setDownLoad(true);
            }

            if (callRecords.get(position).isDownLoad()) {//播放
                if (lastPosition == position) {//是同一条目
                    logger.error("点击了同一条目");
                    if (MediaManager.getMediaPlayer().isPlaying()) {
                        MediaManager.pause();
                        callRecords.get(position).setPlaying(false);
                        //暂停
                        onThreadPause();
                        logger.error("播放暂停");
                        handler.sendEmptyMessage(PAUSE);
                    } else {
                        MediaManager.resume();
                        callRecords.get(position).setPlaying(true);
                        status.setImageResource(R.drawable.downloading);
                        onThreadResume();
                        handler.sendEmptyMessage(PLAY);
                    }

                } else {//不同条目
                    MediaManager.release();
                    onThreadResume();
                    onThreadStop();
                    if (lastPosition >= 0) {
                        callRecords.get(lastPosition).setPlaying(false);
                        lastProgressBar.setVisibility(View.GONE);
                        playProgressBar.setVisibility(View.VISIBLE);
                        phoneAssistantListAdapter.notifyDataSetChanged();
                    }
                    MediaManager.playSound(callRecords.get(position).getPath(), mp -> {
                        callRecords.get(position).setPlaying(false);
                        playProgressBar.setProgress(MediaManager.getMediaPlayer().getDuration());
                        lastPosition = -1;//播放完成重置角标
                        handler.sendEmptyMessage(COMPLETED);
                        handler.post(() -> {
                            playProgressBar.setVisibility(View.GONE);
                            phoneAssistantListAdapter.notifyDataSetChanged();
                        });

                    });
                    callRecords.get(position).setPlaying(true);
                    progressUpdateThread = new ProgressUpdateThread(playProgressBar);
                    progressUpdateThread.start();
                    playProgressBar.setMax(MediaManager.getMediaPlayer().getDuration());
                    handler.sendEmptyMessage(PLAY);
                }
                lastProgressBar = playProgressBar;
                lastPosition = position;//记录上一个条目的角标

            } else {//下载
                if(lastPosition!=-1){
                    if(!callRecords.get(lastPosition).isDownLoad()){
                        ToastUtil.showToast(PhoneAssistantManageActivity.this,getString(R.string.text_loading_audio_please_wait));
                        return;
                    }
                }
                status.setImageResource(R.drawable.download);
                view.setEnabled(false);//下载过程中禁止再次点击
                MyTerminalFactory.getSDK().getVoipCallManager().downloadRecordFile(callRecords.get(position), new DownloadCompleteListener() {
                    @Override
                    public void succeed(CallRecord callRecord) {
                        logger.info("电话录音下载成功");
                        callRecords.get(position).setDownLoad(true);

                        ToastUtil.showToast(PhoneAssistantManageActivity.this, String.format(getString(R.string.text_audio_save_path),callRecord.getPath()));
                        handler.post(() -> {
                            view.setEnabled(true);
                            handler.sendEmptyMessage(DOWNLOAD);
                        });

                    }

                    @Override
                    public void failure() {
                        logger.error("电话录音下载失败");
                        callRecords.get(position).setDownLoad(false);
                        ToastUtil.showToast(PhoneAssistantManageActivity.this, getString(R.string.text_load_audio_fail));
                        mHandler.post(() -> {
                            view.setEnabled(true);
                            downloadProgressBar.setProgress(0);
                            status.setImageResource(R.drawable.undownload);
                        });

                    }
                }, true);
                MyTerminalFactory.getSDK().getSQLiteDBManager().updateCallRecord(callRecords.get(position));//保存通话记录状态的改变
            }

        });
        ivback.setOnClickListener(this);

    }

    @Override
    public void initData() {
        CopyOnWriteArrayList<CallRecord> callRecordList = MyTerminalFactory.getSDK().getSQLiteDBManager().getCallRecords();
        callRecords.addAll(callRecordList);
        for (CallRecord callRecord : callRecords) {
            callRecord.setPlaying(false);
        }
        Collections.sort(callRecords);
        phoneAssistantListAdapter.notifyDataSetChanged();
        pull_refresh_layout.setOnRefreshListener(mOnRefreshListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        logger.info("PhoneAssistantManageActivity.onPause()");
        handler.post(() -> {
            try {
                if (MediaManager.getMediaPlayer() != null) {
                    MediaManager.pause();
                    onThreadPause();
                    status.setImageResource(R.drawable.downloaded);
                }
            }catch (Exception e){
                logger.error(e.toString());
            }

        });
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void doOtherDestroy() {
        onThreadResume();
        onThreadStop();
        //更新最终通话记录列表保存至数据库
        CopyOnWriteArrayList<CallRecord> callRecordList = new CopyOnWriteArrayList<>();
        callRecordList.addAll(callRecords);
        MyTerminalFactory.getSDK().getSQLiteDBManager().addCallRecord(callRecordList);


        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveDownloadProgressHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveNotifyIndividualCallIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveNotifyLivingIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetRtspStreamUrlHandler);


        if (MediaManager.getMediaPlayer() != null) {
            if (MediaManager.getMediaPlayer().isPlaying()) {
                MediaManager.getMediaPlayer().stop();
            }
            MediaManager.release();
        }

    }


    /**
     * 下拉刷新 和 上拉加载更多
     **/
    private PullToRefreshLayout.OnRefreshListener mOnRefreshListener = new PullToRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
            // 下拉刷新操作

            pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
        }

        @Override
        public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {

        }
    };

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
        }
    }

    /***  下载进度更新 **/
    private ReceiveDownloadProgressHandler mReceiveDownloadProgressHandler = new ReceiveDownloadProgressHandler() {

        @Override
        public void handler(final float percent, TerminalMessage terminalMessage) {
            mHandler.post(() -> {
                if (downloadProgressBar != null) {
                    int percentInt = (int) (percent * 100);
                    logger.info("下载进度" + percent);
                    downloadProgressBar.setProgress(percentInt);

                    if (percentInt >= 100) {
                        status.setImageResource(R.drawable.downloaded);
                    }
                }
            });
        }
    };

    //被动方组呼来了
    private ReceiveGroupCallIncommingHandler mReceiveGroupCallIncommingHandler = (memberId, memberName, groupId, groupName, currentCallMode) -> {
        logger.info("被动方组呼来了ReceiveGroupCallIncommingHandler");

                if (MediaManager.getMediaPlayer() != null) {
                    MediaManager.release();
                    lastPosition=-1;
                    onThreadResume();
                    onThreadStop();
                    handler.sendEmptyMessage(COMPLETED);
                }


    };
    //被动方个呼来了
    private ReceiveNotifyIndividualCallIncommingHandler mReceiveNotifyIndividualCallIncommingHandler = (mainMemberName, mainMemberId, individualCallType) -> {
        logger.info("被动方个呼来了ReceiveNotifyIndividualCallIncommingHandler");

                if (MediaManager.getMediaPlayer() != null) {
                    MediaManager.release();
                    handler.sendEmptyMessage(COMPLETED);
                    lastPosition=-1;
                    onThreadResume();
                    onThreadStop();
                }


    };
    //被动方请求视频上报
    private ReceiveNotifyLivingIncommingHandler mReceiveNotifyLivingIncommingHandler = (mainMemberName, mainMemberId, emergencyType) -> {
        logger.info("被动方请求视频上报");

                if (MediaManager.getMediaPlayer() != null) {
                    MediaManager.release();
                    lastPosition=-1;
                    onThreadResume();
                    onThreadStop();
                    handler.sendEmptyMessage(COMPLETED);
                }

    };

    private ReceiveGetRtspStreamUrlHandler receiveGetRtspStreamUrlHandler = (rtspUrl, liveMember, callId) -> {
        logger.info("被动方观看视频上报");

        if (MediaManager.getMediaPlayer() != null) {
            MediaManager.release();
            lastPosition=-1;
            onThreadResume();
            onThreadStop();
            handler.sendEmptyMessage(COMPLETED);
        }
    };


    private class ProgressUpdateThread extends Thread {
        private ProgressBar mProgressBar;
        public ProgressUpdateThread(ProgressBar progressBar){
            mProgressBar=progressBar;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    if (!RUN) {
                        Thread.sleep(Long.MAX_VALUE);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    if (MediaManager.getMediaPlayer() != null && MediaManager.getMediaPlayer().isPlaying()) {
                        int currentPosition = MediaManager.getMediaPlayer().getCurrentPosition();
                        logger.error("播放进度更新===>" + currentPosition);
                        mProgressBar.setProgress(currentPosition);
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.error(e.toString());
                }
            }
        }
    }

    public void onThreadResume() {

        if(progressUpdateThread!=null){
            RUN = true;
            progressUpdateThread.interrupt();
        }

    }

    public void onThreadPause() {
        if(progressUpdateThread!=null){
            RUN = false;
        }

    }

    public void onThreadStop() {
        if (progressUpdateThread != null && progressUpdateThread.isAlive()) {
            progressUpdateThread.interrupt();
            progressUpdateThread = null;

        }

    }
}

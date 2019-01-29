package cn.vsx.vc.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.OnClick;
import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.Remark;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyDataMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyIndividualCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyLivingIncommingHandler;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.ChatBaseActivity;
import cn.vsx.vc.view.CustomerVideoView;
import ptt.terminalsdk.context.MyTerminalFactory;

import static cn.vsx.vc.utils.DataUtil.getTime;

/**
 * 看小视频fragment
 */
public class VideoPreviewItemFragment extends BaseFragment{

    @Bind(R.id.videoView)
    CustomerVideoView videoView;
    @Bind(R.id.tv_current_time)
    TextView tv_current_time;
    @Bind(R.id.seek_bar)
    SeekBar seek_bar;
    @Bind(R.id.tv_max_time)
    TextView tv_max_time;
    @Bind(R.id.ll_seek_bar)
    LinearLayout ll_seek_bar;
    @Bind(R.id.iv_pause)
    ImageView iv_pause;
    @Bind(R.id.iv_pause_continue)
    ImageView iv_pause_continue;

    private static final int UPDATE_PROGRESS = 0;
    private static final int HIDE_SEEK_BAR = 1;
    private FrameLayout fragment_contener;
    private int duration;
    @SuppressWarnings("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch(msg.what){
                case UPDATE_PROGRESS:
                    mHandler.removeMessages(UPDATE_PROGRESS);
                    float currentPosition = videoView.getCurrentPosition();

                    if(currentPosition<0){
                        return;
                    }
                    float sMax = seek_bar.getMax();
                    //播放比例
                    float percent = currentPosition / duration;
                    Log.e("VideoPreviewItemFragmen", "--currentPosition:" + currentPosition+"--duration:"+duration+"--sMax:"+sMax+"--percent:"+percent);
                    if(percent < 1){
                        seek_bar.setProgress((int) (sMax * percent));
                        tv_current_time.setText(getTime((int) currentPosition));
                        mHandler.sendEmptyMessageDelayed(UPDATE_PROGRESS, 1000);
                    }
                    break;

                case HIDE_SEEK_BAR:
                    ll_seek_bar.setVisibility(View.GONE);
                    break;
            }
        }
    };


    public VideoPreviewItemFragment(){
        // Required empty public constructor
    }

    public static VideoPreviewItemFragment newInstance(String filePath){
        VideoPreviewItemFragment fragment = new VideoPreviewItemFragment();
        Bundle bundle = new Bundle();
        bundle.putString("filePath",filePath);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getContentViewId(){
        return R.layout.fragment_video_preview_item;
    }

    @Override
    public void initView(){
        ((ChatBaseActivity) getActivity()).setBackListener(() -> {
            if(null !=getActivity() && !isDetached()){
                if(null != videoView && videoView.isPlaying()){
                    videoView.stopPlayback();
                }
                popBack();
            }
        });
    }

    @Override
    public void initListener(){
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyIndividualCallIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyLivingIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyDataMessageHandler);
    }

    @OnClick(R.id.iv_close)
    public void close(){
        popBack();
    }

    @OnClick({R.id.iv_pause_continue,R.id.iv_pause})
    public void playOrContinue(){
        try{
            if(videoView.isPlaying()){
                videoView.pause();
                iv_pause_continue.setImageResource(R.drawable.on_pause);
                iv_pause.setVisibility(View.VISIBLE);
                mHandler.removeMessages(UPDATE_PROGRESS);
            }else{
                iv_pause_continue.setImageResource(R.drawable.continue_play);
                iv_pause.setVisibility(View.GONE);
                videoView.start();
                mHandler.sendEmptyMessage(UPDATE_PROGRESS);
                mHandler.sendEmptyMessageDelayed(HIDE_SEEK_BAR, 2000);
            }
        }catch(IllegalStateException e){
            e.printStackTrace();
        }
    }

    @Override
    public void initData(){
        String filePath = getArguments().getString("filePath");
        Log.e("文件路径：", filePath);
        videoView.setVideoPath(filePath);
        videoView.start();
        videoView.setOnPreparedListener(mp -> {
            duration = videoView.getDuration();
            tv_max_time.setText(getTime(duration));
            mHandler.sendEmptyMessage(UPDATE_PROGRESS);
        });

        videoView.setOnCompletionListener(mp -> popBack());

        videoView.setOnTouchListener((v, event) -> {
            mHandler.removeMessages(HIDE_SEEK_BAR);
            ll_seek_bar.setVisibility(View.VISIBLE);
            mHandler.sendEmptyMessageDelayed(HIDE_SEEK_BAR,2000);
            return false;
        });

        seek_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
            }

            //拖动条开始拖动的时候调用
            @Override
            public void onStartTrackingTouch(SeekBar seekBar){
            }

            //拖动条停止拖动的时候调用
            @Override
            public void onStopTrackingTouch(SeekBar seekBar){
                float sMax = seek_bar.getMax();
                float progress = seekBar.getProgress();
                //seekBar进度比例
                float percent = (progress / sMax);
                videoView.seekTo((int) (duration * percent));
            }
        });
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        if (videoView != null) {
            videoView.suspend();
        }
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyIndividualCallIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyLivingIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyDataMessageHandler);
        mHandler.removeCallbacksAndMessages(null);

    }

    public void popBack(){
        videoView.stopPlayback();
        mHandler.removeCallbacksAndMessages(null);
        if(null !=fragment_contener){
            fragment_contener.setVisibility(View.GONE);
        }
        if(null != getActivity() && !isDetached()){
            getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
            getActivity().getSupportFragmentManager().popBackStack();
            ((ChatBaseActivity) getActivity()).setBackListener(null);
        }
    }

    public void setFragment_contener(FrameLayout fragmentContener){
        this.fragment_contener = fragmentContener;
    }

    //收到个呼请求，暂停播放
    private ReceiveNotifyIndividualCallIncommingHandler receiveNotifyIndividualCallIncommingHandler = new ReceiveNotifyIndividualCallIncommingHandler(){
        @Override
        public void handler(String mainMemberName, int mainMemberId, int individualCallType){
            if(videoView.isPlaying()){
                videoView.pause();
                mHandler.post(() -> {
                    iv_pause_continue.setImageResource(R.drawable.on_pause);
                    iv_pause.setVisibility(View.VISIBLE);
                    mHandler.removeMessages(UPDATE_PROGRESS);
                });
            }
        }
    };

    private ReceiveNotifyLivingIncommingHandler receiveNotifyLivingIncommingHandler = new ReceiveNotifyLivingIncommingHandler(){
        @Override
        public void handler(String mainMemberName, int mainMemberId){
            if(videoView != null && videoView.isPlaying()){
                videoView.pause();
                iv_pause_continue.setImageResource(R.drawable.on_pause);
                iv_pause.setVisibility(View.VISIBLE);
                mHandler.removeMessages(UPDATE_PROGRESS);
            }
        }
    };

    private ReceiveNotifyDataMessageHandler receiveNotifyDataMessageHandler = new ReceiveNotifyDataMessageHandler(){
        @Override
        public void handler(TerminalMessage terminalMessage){
            if(terminalMessage.messageType == MessageType.WARNING_INSTANCE.getCode() || terminalMessage.messageType == MessageType.VIDEO_LIVE.getCode() && terminalMessage.messageBody.getInteger(JsonParam.REMARK) == Remark.INFORM_TO_WATCH_LIVE){
                if(videoView != null && videoView.isPlaying()){
                    videoView.pause();
                    iv_pause_continue.setImageResource(R.drawable.on_pause);
                    iv_pause.setVisibility(View.VISIBLE);
                }
            }
        }
    };
}

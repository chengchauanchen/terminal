package cn.vsx.vc.fragment;

import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.Remark;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyDataMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyIndividualCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyLivingIncommingHandler;
import cn.vsx.hamster.terminalsdk.tools.DateUtils;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.BaseActivity;
import cn.vsx.vc.receiveHandle.OnBackListener;
import ptt.terminalsdk.context.MyTerminalFactory;


/**
 * 看小视频fragment
 */
public class VideoPreviewItemFragment extends BaseFragment implements TextureView.SurfaceTextureListener, View.OnClickListener{


    RelativeLayout rl_live_general_view;

    TextureView mTextureView;

    TextView tv_current_time;

    SeekBar seek_bar;

    TextView tv_max_time;

    LinearLayout ll_seek_bar;

    ImageView iv_pause;

    ImageView iv_pause_continue;

    private MediaPlayer mMediaPlayer;
    private Surface mSurface;
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
                    float currentPosition = mMediaPlayer.getCurrentPosition();

                    if(currentPosition<0){
                        return;
                    }
                    float sMax = seek_bar.getMax();
                    //播放比例
                    float percent = currentPosition / duration;
                    Log.e("VideoPreviewItemFragmen", "--currentPosition:" + currentPosition+"--duration:"+duration+"--sMax:"+sMax+"--percent:"+percent);
                    if(percent < 1){
                        seek_bar.setProgress((int) (sMax * percent));
                        tv_current_time.setText(DateUtils.getTime((int) currentPosition));
                        mHandler.sendEmptyMessageDelayed(UPDATE_PROGRESS, 1000);
                    }
                    break;

                case HIDE_SEEK_BAR:
                    ll_seek_bar.setVisibility(View.GONE);
                    break;
            }
        }
    };
    private String filePath;

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
        iv_pause_continue = (ImageView) mRootView.findViewById(R.id.iv_pause_continue);
        iv_pause = (ImageView) mRootView.findViewById(R.id.iv_pause);
        ll_seek_bar = (LinearLayout) mRootView.findViewById(R.id.ll_seek_bar);
        tv_max_time = (TextView) mRootView.findViewById(R.id.tv_max_time);
        seek_bar = (SeekBar) mRootView.findViewById(R.id.seek_bar);
        tv_current_time = (TextView) mRootView.findViewById(R.id.tv_current_time);
        mTextureView = (TextureView) mRootView.findViewById(R.id.texture_view);
        rl_live_general_view = (RelativeLayout) mRootView.findViewById(R.id.rl_live_general_view);
        ((BaseActivity) getActivity()).setBackListener(new OnBackListener(){
            @Override
            public void onBack(){
                if(null !=getActivity() && !isDetached()){
                    if(null != mMediaPlayer && mMediaPlayer.isPlaying()){
                        mMediaPlayer.stop();
                    }
                    popBack();
                }
            }
        });
        mRootView.findViewById(R.id.iv_pause).setOnClickListener(this);
        mRootView.findViewById(R.id.iv_pause_continue).setOnClickListener(this);
        mRootView.findViewById(R.id.iv_close).setOnClickListener(this);
    }

    @Override
    public void initListener(){
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyIndividualCallIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyLivingIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyDataMessageHandler);
    }


    public void close(){
        popBack();
    }


    public void playOrContinue(){
        try{
            if(mMediaPlayer.isPlaying()){
                mMediaPlayer.pause();
                iv_pause_continue.setImageResource(R.drawable.on_pause);
                iv_pause.setVisibility(View.VISIBLE);
                mHandler.removeMessages(UPDATE_PROGRESS);
            }else{
                iv_pause_continue.setImageResource(R.drawable.continue_play);
                iv_pause.setVisibility(View.GONE);
                mMediaPlayer.start();
                mHandler.sendEmptyMessage(UPDATE_PROGRESS);
                mHandler.sendEmptyMessageDelayed(HIDE_SEEK_BAR, 2000);
            }
        }catch(IllegalStateException e){
            e.printStackTrace();
        }
    }

    @Override
    public void initData(){
        filePath = getArguments().getString("filePath");
        Log.e("文件路径：", filePath);
        mTextureView.setSurfaceTextureListener(this);


        rl_live_general_view.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                mHandler.removeMessages(HIDE_SEEK_BAR);
                ll_seek_bar.setVisibility(View.VISIBLE);
                mHandler.sendEmptyMessageDelayed(HIDE_SEEK_BAR,2000);
                return false;
            }
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
                mMediaPlayer.seekTo((int) (duration * percent));
            }
        });
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();

        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyIndividualCallIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyLivingIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyDataMessageHandler);
        mHandler.removeCallbacksAndMessages(null);

    }

    public void popBack(){
        if(null != mMediaPlayer && mMediaPlayer.isPlaying()){
            mMediaPlayer.stop();
        }
        mHandler.removeCallbacksAndMessages(null);
        if(null !=fragment_contener){
            fragment_contener.setVisibility(View.GONE);
        }
        if(null != getActivity() && !isDetached()){
            getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
            getActivity().getSupportFragmentManager().popBackStack();
            ((BaseActivity) getActivity()).setBackListener(null);
        }
    }

    public void setFragment_contener(FrameLayout fragmentContener){
        this.fragment_contener = fragmentContener;
    }

    //收到个呼请求，暂停播放
    private ReceiveNotifyIndividualCallIncommingHandler receiveNotifyIndividualCallIncommingHandler = new ReceiveNotifyIndividualCallIncommingHandler(){
        @Override
        public void handler(String mainMemberName, int mainMemberId, int individualCallType){
            if(mMediaPlayer.isPlaying()){
                mMediaPlayer.pause();
                mHandler.post(new Runnable(){
                    @Override
                    public void run(){
                        iv_pause_continue.setImageResource(R.drawable.on_pause);
                        iv_pause.setVisibility(View.VISIBLE);
                        mHandler.removeMessages(UPDATE_PROGRESS);
                    }
                });
            }
        }
    };

    private ReceiveNotifyLivingIncommingHandler receiveNotifyLivingIncommingHandler = new ReceiveNotifyLivingIncommingHandler(){
        @Override
        public void handler(String mainMemberName, int mainMemberId,boolean emergencyType){
            if(mMediaPlayer != null && mMediaPlayer.isPlaying()){
                mMediaPlayer.pause();
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
                if(mMediaPlayer != null && mMediaPlayer.isPlaying()){
                    mMediaPlayer.pause();
                    iv_pause_continue.setImageResource(R.drawable.on_pause);
                    iv_pause.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    private void openMediaPlayer(){
        try {
            File file = new File(filePath);
            if (!file.exists()) {//文件不存在
                Toast.makeText(getActivity(), "文件路径错误", Toast.LENGTH_SHORT).show();
                popBack();
                return;
            }
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(file.getAbsolutePath());
            mMediaPlayer.setSurface(mSurface);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    int videoWidth = mMediaPlayer.getVideoWidth();
                    int videoHeight = mMediaPlayer.getVideoHeight();
                    float ratio = videoWidth * 1.0f/videoHeight;
                    float ratioView = rl_live_general_view.getWidth() * 1.0f/rl_live_general_view.getHeight();
                    // 屏幕比视频的宽高比更小.表示视频是过于宽屏了.
                    if (ratioView - ratio < 0){
                        // 宽为基准.
                        mTextureView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                        mTextureView.getLayoutParams().height = (int) (rl_live_general_view.getWidth() / ratio + 0.5f);

                    }
                    // 视频是竖屏了.
                    else{
                        mTextureView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                        mTextureView.getLayoutParams().width = (int) (rl_live_general_view.getHeight() * ratio + 0.5f);
                    }

                    mMediaPlayer.start();
                    duration = mMediaPlayer.getDuration();
                    tv_max_time.setText(DateUtils.getTime(duration));
                    mHandler.sendEmptyMessage(UPDATE_PROGRESS);
                }
            });
            mMediaPlayer.prepare();
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    popBack();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height){
        mSurface = new Surface(surfaceTexture);
        openMediaPlayer();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height){
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface){
        mSurface = null;
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface){
    }

    @Override
    public void onClick(View v){
        int i = v.getId();
        if(i == R.id.iv_pause){
            playOrContinue();
        }else if(i == R.id.iv_pause_continue){
            playOrContinue();
        }else if(i == R.id.iv_close){
            close();
        }
    }
}

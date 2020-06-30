package cn.vsx.vc.fragment;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ToastUtils;

import org.apache.log4j.Logger;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.vsx.hamster.terminalsdk.tools.DateUtils;
import cn.vsx.vc.R;
import cn.vsx.vc.listener.PlayVideoStateListener;
import cn.vsx.vc.receiveHandle.ReceiverUpdatePlayVideoStateHandler;
import cn.vsx.vc.utils.Constants;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.StringUtil;

/**
 * Created by gt358 on 2017/10/20.
 */

public class FileVideoShowItemFragment extends Fragment implements View.OnClickListener {

    @Bind(R.id.texture_view)
    TextureView mTextureView;
    @Bind(R.id.iv_pause)
    ImageView mIvPause;
    @Bind(R.id.ll_seek_bar)
    LinearLayout mLlSeekBar;
    @Bind(R.id.iv_pause_continue)
    ImageView mIvPauseContinue;
    @Bind(R.id.tv_current_time)
    TextView mTvCurrentTime;
    @Bind(R.id.seek_bar)
    SeekBar mSeekBar;
    @Bind(R.id.tv_max_time)
    TextView mTvMaxTime;
    @Bind(R.id.ll_volume)
    LinearLayout mLlVolume;
    @Bind(R.id.iv_volume)
    ImageView mIvVolume;
    @Bind(R.id.tv_volume)
    TextView mTvVolume;
    @Bind(R.id.tv_quiet_play)
    TextView mTvQuietPlay;


    private String filePath = "";
    private MediaPlayer mediaPlayer;
    private int maxTime;

    private static final int UPDATE_PROGRESS = 0;
    private static final int COMPLETE_PROGRESS = 1;
    private static final int HIDE_SEEK_BAR = 2;
    private static final int RECEIVEVOICECHANGED = 3;
    private static final int UPDATEQUITPLAY = 4;
    private int currentPosition;
    private boolean playFinish;
    private boolean prepared;

    private PlayVideoStateListener mListener;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch(msg.what){
                case UPDATE_PROGRESS:
                    mHandler.removeMessages(UPDATE_PROGRESS);
                    if(null != mediaPlayer){
                        float position = mediaPlayer.getCurrentPosition();
                        Log.e("PlayLiveHistoryActivity", "position:" + position);
                        if(position < 0){
                            return;
                        }
                        float sMax = mSeekBar.getMax();
                        //播放比例
                        float percent = position / maxTime;
                        Log.e("PlayLiveHistoryActivity", "position:" + position + "--percent:" + percent);
                        if(percent < 1){
                            mSeekBar.setProgress((int) (sMax * percent));
                            mTvCurrentTime.setText(DateUtils.getTime((int) position));
                            mHandler.sendEmptyMessageDelayed(UPDATE_PROGRESS, 1000);
                        }
                    }
                    break;
                case COMPLETE_PROGRESS:
                    mSeekBar.setProgress(mSeekBar.getMax());
                    mTvCurrentTime.setText(DateUtils.getTime(maxTime));
                    if(null != mediaPlayer && mediaPlayer.isPlaying()){
                        mediaPlayer.pause();
                    }
                    mIvPauseContinue.setImageResource(R.drawable.on_pause);
                    break;
                case HIDE_SEEK_BAR:
                    mLlSeekBar.setVisibility(View.GONE);
                    break;
                case RECEIVEVOICECHANGED:
                    mLlVolume.setVisibility(View.GONE);
                    break;
                case UPDATEQUITPLAY:
                    removeMessages(UPDATEQUITPLAY);
                    updateQuitPlay();
                    break;
                default:
                    break;
            }
        }
    };
    public Logger logger = Logger.getLogger(getClass());

    public static FileVideoShowItemFragment newInstance() {
        return new FileVideoShowItemFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            filePath = getArguments().getString(Constants.FILE_PATH);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_video_show_item, container, false);
        ButterKnife.bind(this, view);
        initView();
        initListener();
        initData();
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (PlayVideoStateListener) activity;
        } catch (Exception e) {
           e.printStackTrace();
        }
    }

    /**
     * 初始化布局
     */
    private void initView() {
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener(){
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height){
                Log.e("PlayLiveHistoryActivity", "onSurfaceTextureAvailable");
                //设置视屏文件图像的显示参数
                Surface face = new Surface(surface);
                if(mediaPlayer != null){
                    mediaPlayer.setSurface(face);
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height){
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface){
                Log.e("PlayLiveHistoryActivity", "onSurfaceTextureDestroyed");
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface){
            }
        });
        //设置 surfaceView点击监听
        mTextureView.setOnTouchListener((v, event) -> {
            switch(event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    mLlSeekBar.setVisibility(View.VISIBLE);
                    mHandler.removeMessages(HIDE_SEEK_BAR);
                    mHandler.sendEmptyMessageDelayed(HIDE_SEEK_BAR, 2000);
                    break;
            }
            //返回True代表事件已经处理了
            return true;
        });
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
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
                try{
                    float sMax = mSeekBar.getMax();
                    float progress = seekBar.getProgress();
                    //seekBar进度比例
                    float percent = (progress / sMax);
                    mediaPlayer.seekTo((int) (maxTime * percent));
                    mIvPause.setVisibility(View.GONE);
                }catch(Exception e){
                    logger.error(e.toString());
                }
            }
        });
        setTextureViewSize(filePath);
    }
    /**
     * 添加监听
     */
    private void initListener() {
        MyTerminalFactory.getSDK().registReceiveHandler(receiverUpdatePlayVideoStateHandler);
    }

    /**
     * 获取数据
     */
    private void initData() {
        play();
    }

    @Override
    public void onResume(){
        super.onResume();
        if(mediaPlayer != null && prepared){
            mediaPlayer.start();
            mIvPauseContinue.setImageResource(R.drawable.continue_play);
            mIvPause.setVisibility(View.GONE);
            mHandler.sendEmptyMessage(UPDATE_PROGRESS);
            mHandler.postDelayed(this::pauseOrContinue,100);
        }
    }

    @OnClick({R.id.tv_quiet_play, R.id.iv_pause_continue, R.id.iv_pause})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_quiet_play:
                //静音预览
                ToastUtils.showShort(R.string.uav_push_quiet_play);
                break;
            case R.id.iv_pause_continue:
                //继续播放
                pauseOrContinue();
                break;
            case R.id.iv_pause:
                //暂停
                pauseOrContinue();
                break;
                default:break;
        }
    }

    public void pauseOrContinue(){
        try{

            if(null != mediaPlayer && mediaPlayer.isPlaying()){
                mediaPlayer.pause();
                mIvPauseContinue.setImageResource(R.drawable.on_pause);
                mIvPause.setVisibility(View.VISIBLE);
                mHandler.removeMessages(UPDATE_PROGRESS);
            }else{
                if(playFinish){
                    play();
                }else{
                    if(null != mediaPlayer){
                        mediaPlayer.start();
                    }
                }
                mIvPauseContinue.setImageResource(R.drawable.continue_play);
                mIvPause.setVisibility(View.GONE);
                mHandler.sendEmptyMessage(UPDATE_PROGRESS);
                mHandler.sendEmptyMessageDelayed(HIDE_SEEK_BAR, 2000);
            }

        }catch(IllegalStateException e){
            e.printStackTrace();
        }
    }

    private void play(){
        mIvPause.setVisibility(View.GONE);
        mIvPauseContinue.setImageResource(R.drawable.continue_play);

        if(TextUtils.isEmpty(filePath)){
            ToastUtils.showShort("文件路径为空，不能播放");
        }else{
            try{
                initMediaPlayer(filePath);
            }catch(Exception e){
                logger.error(e);
            }
        }
    }

    private void initMediaPlayer(String url) throws IOException {
        logger.info("视频地址："+url);
        if(mediaPlayer == null){
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();
            updateQuitPlay();
        }
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){

            @Override
            public void onPrepared(MediaPlayer mp){
                logger.info("onPrepared");
                if(null != mTextureView.getSurfaceTexture()){
                    Surface face = new Surface(mTextureView.getSurfaceTexture());
                    mediaPlayer.setSurface(face);
                }
                prepared = true;
                playFinish = false;
                maxTime = mediaPlayer.getDuration();
                mediaPlayer.start();
                mTvMaxTime.setText(DateUtils.getTime(maxTime));
                mHandler.sendEmptyMessage(UPDATE_PROGRESS);
            }
        });
        mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener(){
            @Override
            public void onSeekComplete(MediaPlayer mp){
                mp.start();
                mIvPauseContinue.setImageResource(R.drawable.continue_play);
                mHandler.sendEmptyMessage(UPDATE_PROGRESS);
                mHandler.sendEmptyMessageDelayed(HIDE_SEEK_BAR, 2000);
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
            @Override
            public void onCompletion(MediaPlayer mp){
                logger.info("onCompletion");
                mIvPauseContinue.setImageResource(R.drawable.on_pause);
                mIvPause.setVisibility(View.VISIBLE);
                mHandler.sendEmptyMessage(COMPLETE_PROGRESS);
                mHandler.removeMessages(UPDATE_PROGRESS);
                playFinish = true;
                //重新播放
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener(){
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra){
                logger.error("mediaPlayer  onError");
                mIvPauseContinue.setImageResource(R.drawable.on_pause);
                mIvPause.setVisibility(View.VISIBLE);
                mHandler.removeMessages(UPDATE_PROGRESS);
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.stop();
                }
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
                prepared = false;
                return false;
            }
        });
    }

    /**
     * 更新是否静音播放
     */
    private ReceiverUpdatePlayVideoStateHandler receiverUpdatePlayVideoStateHandler = new ReceiverUpdatePlayVideoStateHandler() {
        @Override
        public void handler() {
            mHandler.sendEmptyMessageDelayed(UPDATEQUITPLAY,500);
        }
    };


    /**
     * 更新是否静音播放的状态
     */
    private void updateQuitPlay() {
        if(mListener!=null&&mListener.canQuitPlay()){
            if(mediaPlayer!=null) {
                mediaPlayer.setVolume(0.0f, 0.0f);
            }
            if(mTvQuietPlay!=null){
                mTvQuietPlay.setVisibility(View.VISIBLE);
            }
        }else{
            if(mediaPlayer!=null) {
                mediaPlayer.setVolume(0.5f,0.5f);
            }
            if(mTvQuietPlay!=null){
                mTvQuietPlay.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 设置
     * @param path
     */
    public void setTextureViewSize(String path){
        if(TextUtils.isEmpty(path)){
            return;
        }
        android.media.MediaMetadataRetriever mmr = new android.media.MediaMetadataRetriever();
        try {
            mmr.setDataSource(path);
            int width = StringUtil.stringToInt(mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));//宽
            int height = StringUtil.stringToInt(mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));//高
            logger.info("setTextureViewSize--width:"+width+"--height:"+height);
            int screenWidth = ScreenUtils.getScreenWidth();
            if(mTextureView!=null){
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mTextureView.getLayoutParams();
                lp.width = screenWidth;
                lp.height = screenWidth*height/width;
                mTextureView.setLayoutParams(lp);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            mmr.release();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        try{
            if(mediaPlayer != null && mediaPlayer.isPlaying()){
                currentPosition = mediaPlayer.getCurrentPosition();
                mediaPlayer.pause();
                mIvPauseContinue.setImageResource(R.drawable.on_pause);
                mIvPause.setVisibility(View.VISIBLE);
                mHandler.removeMessages(UPDATE_PROGRESS);
            }
        }catch(IllegalStateException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiverUpdatePlayVideoStateHandler);
        mHandler.removeCallbacksAndMessages(null);
        try{
            if(mediaPlayer != null){
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.stop();
                }
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }catch(IllegalStateException e){
            e.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}

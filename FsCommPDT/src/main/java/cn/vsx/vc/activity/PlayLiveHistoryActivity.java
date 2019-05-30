package cn.vsx.vc.activity;

import android.annotation.SuppressLint;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.Remark;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNetworkChangeHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyDataMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyIndividualCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyLivingIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveVolumeOffCallHandler;
import cn.vsx.hamster.terminalsdk.tools.DateUtils;
import cn.vsx.vc.R;
import cn.vsx.vc.utils.StringUtil;
import cn.vsx.vc.utils.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

public class PlayLiveHistoryActivity extends BaseActivity implements View.OnClickListener{

    private final String TAG = this.getClass().getName();

    TextureView textureView;

    ImageView iv_pause_continue;

    TextView tv_current_time;

    SeekBar seek_bar;

    TextView tv_max_time;

    ImageView iv_close;

    TextView tv_theme;

    ImageView iv_pause;

    LinearLayout ll_seek_bar;

    LinearLayout ll_volume;

    ImageView iv_volume;

    TextView tv_volume;
    private MediaPlayer mediaPlayer;
    private int currentPosition;
    private boolean isNetConnected = true;
    //    private boolean error;
    private static final int UPDATE_PROGRESS = 0;
    private static final int COMPLETE_PROGRESS = 1;
    private static final int HIDE_SEEK_BAR = 2;
    private static final int RECEIVEVOICECHANGED = 3;

    //视频最大时长
    private int maxTime;
    @SuppressWarnings("HandlerLeak")
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
                        float sMax = seek_bar.getMax();
                        //播放比例
                        float percent = position / maxTime;
                        Log.e("PlayLiveHistoryActivity", "position:" + position + "--percent:" + percent);
                        if(percent < 1){
                            seek_bar.setProgress((int) (sMax * percent));
                            tv_current_time.setText(DateUtils.getTime((int) position));
                            mHandler.sendEmptyMessageDelayed(UPDATE_PROGRESS, 1000);
                        }
                    }
                    break;
                case COMPLETE_PROGRESS:
                    seek_bar.setProgress(seek_bar.getMax());
                    tv_current_time.setText(DateUtils.getTime(maxTime));
                    if(null != mediaPlayer && mediaPlayer.isPlaying()){
                        mediaPlayer.pause();
                    }
                    iv_pause_continue.setImageResource(R.drawable.on_pause);
                    break;
                case HIDE_SEEK_BAR:
                    ll_seek_bar.setVisibility(View.GONE);
                    break;
                case RECEIVEVOICECHANGED:
                    ll_volume.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }
    };
    private String url;
    private boolean playFinish;

    @Override
    public int getLayoutResId(){
        return R.layout.activity_play_live_history;
    }

    @Override
    public void setOritation(){
        oritationPort = false;
    }

    @Override
    public void initView(){
        tv_volume = (TextView) findViewById(R.id.tv_volume);
        iv_volume = (ImageView) findViewById(R.id.iv_volume);
        ll_volume = (LinearLayout) findViewById(R.id.ll_volume);
        ll_seek_bar = (LinearLayout) findViewById(R.id.ll_seek_bar);
        iv_pause = (ImageView) findViewById(R.id.iv_pause);
        tv_theme = (TextView) findViewById(R.id.tv_theme);
        iv_close = (ImageView) findViewById(R.id.iv_close);
        tv_max_time = (TextView) findViewById(R.id.tv_max_time);
        seek_bar = (SeekBar) findViewById(R.id.seek_bar);
        tv_current_time = (TextView) findViewById(R.id.tv_current_time);
        iv_pause_continue = (ImageView) findViewById(R.id.iv_pause_continue);
        textureView = (TextureView) findViewById(R.id.texture_view);
        findViewById(R.id.iv_close).setOnClickListener(this);
        findViewById(R.id.iv_pause).setOnClickListener(this);
        findViewById(R.id.iv_pause_continue).setOnClickListener(this);
    }

    @Override
    public void initListener(){
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiveVolumeOffCallHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNetworkChangeHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyIndividualCallIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyLivingIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyDataMessageHandler);
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener(){
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height){
                Log.e("PlayLiveHistoryActivity", "onSurfaceTextureAvailable");
                //设置视屏文件图像的显示参数
                Surface face = new Surface(surface);
                mediaPlayer.setSurface(face);
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
        textureView.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        ll_seek_bar.setVisibility(View.VISIBLE);
                        mHandler.removeMessages(HIDE_SEEK_BAR);
                        mHandler.sendEmptyMessageDelayed(HIDE_SEEK_BAR, 2000);
                        break;
                }
                //返回True代表事件已经处理了
                return true;
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
                try{
                    float sMax = seek_bar.getMax();
                    float progress = seekBar.getProgress();
                    //seekBar进度比例
                    float percent = (progress / sMax);
                    mediaPlayer.seekTo((int) (maxTime * percent));
                    iv_pause.setVisibility(View.GONE);
                }catch(Exception e){
                    logger.error(e.toString());
                }
            }
        });
    }

    @Override
    protected void onPause(){
        super.onPause();
        try{
            if(mediaPlayer != null && mediaPlayer.isPlaying()){
                currentPosition = mediaPlayer.getCurrentPosition();
                mediaPlayer.pause();
                iv_pause_continue.setImageResource(R.drawable.on_pause);
                iv_pause.setVisibility(View.VISIBLE);
                mHandler.removeMessages(UPDATE_PROGRESS);
            }
        }catch(IllegalStateException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();

    }

    private ReceiveVolumeOffCallHandler receiveVolumeOffCallHandler = new ReceiveVolumeOffCallHandler(){
        @SuppressLint("SetTextI18n")
        @Override
        public void handler(boolean isVolumeOff, int status){
            mHandler.removeMessages(RECEIVEVOICECHANGED);
            if(status == 0){
                ll_volume.setVisibility(View.GONE);
            }else if(status == 1){
                ll_volume.setVisibility(View.VISIBLE);
            }
            tv_volume.setText(MyTerminalFactory.getSDK().getAudioProxy().getVolume() + "%");
            mHandler.sendEmptyMessageDelayed(RECEIVEVOICECHANGED, 2000);
        }
    };

    //收到个呼请求，暂停播放
    private ReceiveNotifyIndividualCallIncommingHandler receiveNotifyIndividualCallIncommingHandler = new ReceiveNotifyIndividualCallIncommingHandler(){
        @Override
        public void handler(String mainMemberName, int mainMemberId, int individualCallType){
            if(mediaPlayer != null && mediaPlayer.isPlaying()){
                mediaPlayer.pause();
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
        public void handler(String mainMemberName, int mainMemberId, boolean emergencyType){
            if(mediaPlayer != null && mediaPlayer.isPlaying()){
                mediaPlayer.pause();
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
                if(mediaPlayer != null && mediaPlayer.isPlaying()){
                    mHandler.post(new Runnable(){
                        @Override
                        public void run(){
                            mediaPlayer.pause();
                            iv_pause_continue.setImageResource(R.drawable.on_pause);
                            iv_pause.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        }
    };

    private ReceiveNetworkChangeHandler receiveNetworkChangeHandler = new ReceiveNetworkChangeHandler(){
        @Override
        public void handler(boolean connected){
            isNetConnected = connected;
            mHandler.post(new Runnable(){
                @Override
                public void run(){
                    if(!isNetConnected){
                        if(mediaPlayer != null && mediaPlayer.isPlaying()){
                            mediaPlayer.pause();
                            iv_pause_continue.setImageResource(R.drawable.on_pause);
                            iv_pause.setVisibility(View.VISIBLE);
                            mHandler.removeMessages(UPDATE_PROGRESS);
                        }
                    }else{
                        if(mediaPlayer != null){
                            iv_pause_continue.setImageResource(R.drawable.continue_play);
                            iv_pause.setVisibility(View.GONE);
                            mediaPlayer.start();
                            mHandler.sendEmptyMessage(UPDATE_PROGRESS);
                            mHandler.sendEmptyMessageDelayed(HIDE_SEEK_BAR, 2000);
                        }
                    }
                }
            });
        }
    };

    @Override
    public void initData(){
        url = getIntent().getStringExtra("URL");
        String liveTheme = getIntent().getStringExtra("liveTheme");
        String duration = getIntent().getStringExtra("DURATION");
        maxTime = ((int) (StringUtil.toFloat(duration) * 1000));
        tv_theme.setText(liveTheme);
        play(0);
    }

    @Override
    public void doOtherDestroy(){
        Log.e("PlayLiveHistoryActivity", "doOtherDestroy");
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiveVolumeOffCallHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNetworkChangeHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyIndividualCallIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyLivingIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyDataMessageHandler);
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
        }
    }

    public void pauseOrContinue(){
        try{
            if(isNetConnected){
                if(null != mediaPlayer && mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    iv_pause_continue.setImageResource(R.drawable.on_pause);
                    iv_pause.setVisibility(View.VISIBLE);
                    mHandler.removeMessages(UPDATE_PROGRESS);
                }else{
                    if(playFinish){
                        play(0);
                    }else{
                        if(null != mediaPlayer){
                            mediaPlayer.start();
                        }
                    }
                    iv_pause_continue.setImageResource(R.drawable.continue_play);
                    iv_pause.setVisibility(View.GONE);
                    mHandler.sendEmptyMessage(UPDATE_PROGRESS);
                    mHandler.sendEmptyMessageDelayed(HIDE_SEEK_BAR, 2000);
                }
            }else{
                ToastUtil.showToast(PlayLiveHistoryActivity.this, "网络连接已断开");
            }
        }catch(IllegalStateException e){
            e.printStackTrace();
        }
    }

    public void close(){
        mHandler.removeCallbacksAndMessages(null);
        try{
            if(mediaPlayer != null){
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }catch(IllegalStateException e){
            e.printStackTrace();
        }
        finish();
    }

    private void play(final int msec){
        if(TextUtils.isEmpty(url)){
            ToastUtil.showToast(this, "url为空，不能播放");
        }else{
            try{
                if(mediaPlayer == null){
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.setDataSource(url);
                    mediaPlayer.prepareAsync();
                }
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){

                    @Override
                    public void onPrepared(MediaPlayer mp){
                        if(null != textureView.getSurfaceTexture()){
                            Surface face = new Surface(textureView.getSurfaceTexture());
                            mediaPlayer.setSurface(face);
                        }
                        playFinish = false;
                        mediaPlayer.start();
                        //                        mediaPlayer.seekTo(msec);
                        tv_max_time.setText(DateUtils.getTime(maxTime));
                        mHandler.sendEmptyMessage(UPDATE_PROGRESS);
                    }
                });
                mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener(){
                    @Override
                    public void onSeekComplete(MediaPlayer mp){
                        mp.start();
                        iv_pause_continue.setImageResource(R.drawable.continue_play);
                        mHandler.sendEmptyMessage(UPDATE_PROGRESS);
                        mHandler.sendEmptyMessageDelayed(HIDE_SEEK_BAR, 2000);
                    }
                });
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
                    @Override
                    public void onCompletion(MediaPlayer mp){
                        logger.info("onCompletion");
                        iv_pause_continue.setImageResource(R.drawable.on_pause);
                        iv_pause.setVisibility(View.VISIBLE);
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
                        iv_pause_continue.setImageResource(R.drawable.on_pause);
                        iv_pause.setVisibility(View.VISIBLE);
                        mHandler.removeMessages(UPDATE_PROGRESS);
                        if(mediaPlayer.isPlaying()){
                            mediaPlayer.stop();
                        }
                        mediaPlayer.reset();
                        mediaPlayer.release();
                        mediaPlayer = null;
                        return false;
                    }
                });
            }catch(Exception e){
                logger.error(e);
            }
        }
    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.iv_close:
                close();
                break;
            case R.id.iv_pause:
                pauseOrContinue();
                break;
            case R.id.iv_pause_continue:
                pauseOrContinue();
                break;
        }
    }
}

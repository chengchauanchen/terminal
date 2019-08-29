package cn.vsx.vc.activity;

import android.annotation.SuppressLint;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.Remark;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNetworkChangeHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyDataMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyIndividualCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyLivingIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveVolumeOffCallHandler;
import cn.vsx.hamster.terminalsdk.tools.DateUtils;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.Util;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.PlayLiveAdapter;
import cn.vsx.vc.model.MediaBean;
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
    private LinearLayout ll_list;
    private RecyclerView recyclerview;
    private View tv_choice;
    private MediaPlayer mediaPlayer;
    private int currentPosition;
    private boolean isNetConnected = true;
    private List<MediaBean> mediaBeans = new ArrayList<>();
    private int currentMediaBeanPosition;
    private String liveTheme;
    private boolean playFinish;
    private PlayLiveAdapter playLiveAdapter;
    //    private boolean error;
    private static final int UPDATE_PROGRESS = 0;
    private static final int COMPLETE_PROGRESS = 1;
    private static final int HIDE_SEEK_BAR = 2;
    private static final int RECEIVEVOICECHANGED = 3;
    private static final int GETDATA = 4;

    //视频最大时长
    private int maxTime;
    @SuppressWarnings("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch(msg.what){
                case UPDATE_PROGRESS://更新进度
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
                case COMPLETE_PROGRESS://完成\进度
                    seek_bar.setProgress(seek_bar.getMax());
                    tv_current_time.setText(DateUtils.getTime(maxTime));
                    logger.info("currentMediaBeanPosition:"+currentMediaBeanPosition);
                    if(currentMediaBeanPosition < mediaBeans.size()-1){
                        playNext(currentMediaBeanPosition+1);
                    }else {
                        if(null != mediaPlayer && mediaPlayer.isPlaying()){
                            mediaPlayer.pause();
                        }
                        iv_pause_continue.setImageResource(R.drawable.on_pause);
                    }
                    break;
                case HIDE_SEEK_BAR://隐藏搜索栏
                    ll_seek_bar.setVisibility(View.GONE);
                    break;
                case RECEIVEVOICECHANGED://接收语音已更改
                    ll_volume.setVisibility(View.GONE);
                    break;
                case GETDATA://获取数据
                    if(!mediaBeans.isEmpty()){
                        mediaBeans.get(0).setSelected(true);
                        playLiveAdapter.notifyItemChanged(0);
                        currentMediaBeanPosition = 0;
                        play(currentMediaBeanPosition);
                    }
                    break;
                default:
                    break;
            }
        }
    };

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
        tv_choice = findViewById(R.id.tv_choice);
        ll_list = findViewById(R.id.ll_list);
        recyclerview = findViewById(R.id.recyclerview);
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
        tv_choice.setOnClickListener(v -> ll_list.setVisibility(View.VISIBLE));

        playLiveAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener(){
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position){
                revertMediaPlayer();
                playNext(position);
            }
        });
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener(){
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
        textureView.setOnTouchListener((v, event) -> {
            switch(event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    ll_seek_bar.setVisibility(View.VISIBLE);
                    ll_list.setVisibility(View.GONE);
                    mHandler.removeMessages(HIDE_SEEK_BAR);
                    mHandler.sendEmptyMessageDelayed(HIDE_SEEK_BAR, 2000);
                    break;
            }
            //返回True代表事件已经处理了
            return true;
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

    private void playNext(int position){
        //将上一个播放的不选中
        mediaBeans.get(currentMediaBeanPosition).setSelected(false);
        currentMediaBeanPosition = position;
        mediaBeans.get(currentMediaBeanPosition).setSelected(true);
        playLiveAdapter.notifyDataSetChanged();
        play(position);
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
        if(mediaPlayer != null){
            mediaPlayer.start();
            iv_pause_continue.setImageResource(R.drawable.continue_play);
            iv_pause.setVisibility(View.GONE);
            mHandler.sendEmptyMessage(UPDATE_PROGRESS);
            mHandler.postDelayed(this::pauseOrContinue,100);
        }
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
        final TerminalMessage terminalMessage = (TerminalMessage) getIntent().getSerializableExtra("terminalMessage");
        setLiveTheme(terminalMessage);
        getData(terminalMessage);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        playLiveAdapter = new PlayLiveAdapter(R.layout.item_play_live, mediaBeans);
        recyclerview.setAdapter(playLiveAdapter);
    }

    private void initMediaPlayer(String url) throws IOException{
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
                maxTime = mediaPlayer.getDuration();
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
                ToastUtils.showShort(R.string.text_play_live_fail);
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
    }

    private void getData(TerminalMessage terminalMessage){
        //获取播放url
        MyTerminalFactory.getSDK().getThreadPool().execute(() -> {
            String serverIp = TerminalFactory.getSDK().getParam(Params.MEDIA_HISTORY_SERVER_IP, "");
            String serverPort = TerminalFactory.getSDK().getParam(Params.MEDIA_HISTORY_SERVER_PORT, 0)+"";
            String url = "http://"+serverIp+":"+serverPort+"/api/v1/query_records";
            Map<String,String> paramsMap = new HashMap<>();
            logger.info("消息："+terminalMessage);
            paramsMap.put("id",getCallId(terminalMessage));
            logger.info("获取视频回放url："+url);
            String result = TerminalFactory.getSDK().getHttpClient().sendGet(url, paramsMap);
            //            result = "{\"msg\":\"success\",\"code\":0,\"data\":{\"list\":[{\"name\":\"88045832_6540978884229379386\",\"start_time\":\"20190517154511\",\"duration\":\"13.598\",\"hls\":\"/hls/88045832_6540978884229379386/20190517/20190517154511/88045832_6540978884229379386_record.m3u8\"}]}}";
            logger.info("获取视频回放结果："+result);
            if(!Util.isEmpty(result)){
                JSONObject jsonResult = JSONObject.parseObject(result);
                Integer code = jsonResult.getInteger("code");
                if(code == 0){
                    JSONObject data = jsonResult.getJSONObject("data");
                    JSONArray list = data.getJSONArray("list");
                    if(list.isEmpty()){
                        ToastUtil.showToast(PlayLiveHistoryActivity.this,getString(R.string.text_get_video_info_fail));
                        finish();
                        return;
                    }
                    for(int i = 0; i < list.size(); i++){
                        JSONObject jsonObject = list.getJSONObject(i);
                        String hls = jsonObject.getString("hls");
                        String startTime = jsonObject.getString("start_time");
                        String fileServerIp = MyTerminalFactory.getSDK().getParam(Params.MEDIA_HISTORY_SERVER_IP);
                        String port = MyTerminalFactory.getSDK().getParam(Params.MEDIA_HISTORY_SERVER_PORT,0)+"";
                        String liveUrl = "http://"+fileServerIp+":"+port+hls;
                        logger.info("liveUrl："+liveUrl);
                        MediaBean mediaBean = new MediaBean();
                        mediaBean.setUrl(liveUrl);
                        mediaBean.setStartTime(startTime);
                        mediaBeans.add(mediaBean);
                    }
                    mHandler.sendEmptyMessage(GETDATA);
                }
            }
        });
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
                        if(currentMediaBeanPosition < mediaBeans.size()-1){
                            playNext(currentMediaBeanPosition+1);
                        }else {
                            playNext(0);
                        }

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

    private void close(){
        mHandler.removeCallbacksAndMessages(null);
        destroyMediaPlayer();
        finish();
    }

    private void destroyMediaPlayer(){
        try{
            if(mediaPlayer != null){
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }catch(IllegalStateException e){
            e.printStackTrace();
        }
    }

    /**
     * 还原SeekBar
     */
    private void revertSeekBar(){
        mHandler.removeMessages(UPDATE_PROGRESS);
        mHandler.removeMessages(COMPLETE_PROGRESS);
        seek_bar.setProgress(0);
    }

    /**
     * 还原媒体播放器
     */
    private void revertMediaPlayer(){
        destroyMediaPlayer();
        revertSeekBar();
    }

    private void play(int positoon){
        iv_pause.setVisibility(View.GONE);
        iv_pause_continue.setImageResource(R.drawable.continue_play);
        String url = mediaBeans.get(positoon).getUrl();
        if(TextUtils.isEmpty(url)){
            ToastUtil.showToast(this, "url为空，不能播放");
        }else{
            try{
                initMediaPlayer(url);
            }catch(Exception e){
                logger.error(e);
            }
        }
    }

    @Override
    public void onClick(View v){
        int i = v.getId();
        if(i == R.id.iv_close){
            close();
        }else if(i == R.id.iv_pause){
            pauseOrContinue();
        }else if(i == R.id.iv_pause_continue){
            pauseOrContinue();
        }
    }

    private void setLiveTheme(TerminalMessage terminalMessage){
        JSONObject messageBody = terminalMessage.messageBody;
        String liver = messageBody.getString(JsonParam.LIVER);

        String[] split = liver.split("_");
        //上报主题，如果没有就取上报者的名字
        liveTheme = messageBody.getString(JsonParam.TITLE);
        if(Util.isEmpty(liveTheme)){
            if(split.length>1){
                String memberName = split[1];
                liveTheme = String.format(getString(R.string.text_living_theme_member_name),memberName);
            }else {
                int memberNo = terminalMessage.messageFromId;
                TerminalFactory.getSDK().getThreadPool().execute(() -> {
                    Account account = cn.vsx.hamster.terminalsdk.tools.DataUtil.getAccountByMemberNo(memberNo,true);
                    String name = (account!=null)?account.getName():terminalMessage.messageFromName;
                    liveTheme = String.format(getString(R.string.text_living_theme_member_name),name);
                    mHandler.post(() -> {
                        if(tv_theme !=null){
                            tv_theme.setText(liveTheme);
                        }
                    });
                });
            }
        }
        tv_theme.setText(liveTheme);
    }

    /**
     * 获取callId
     * @param terminalMessage
     * @return
     */
    private String getCallId(TerminalMessage terminalMessage){
        String id = "";
        if(terminalMessage.messageBody!=null){
            if(terminalMessage.messageBody.containsKey(JsonParam.EASYDARWIN_RTSP_URL)){
                String url = terminalMessage.messageBody.getString(JsonParam.EASYDARWIN_RTSP_URL);
                if(!TextUtils.isEmpty(url)&&url.contains("/")&&url.contains(".")){
                    int index = url.lastIndexOf("/");
                    int pointIndex = url.lastIndexOf(".");
                    id = url.substring(index+1,pointIndex);
                }
            }
        }
        return id;
    }
}

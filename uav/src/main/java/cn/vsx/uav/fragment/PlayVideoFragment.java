package cn.vsx.uav.fragment;

import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
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

import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.ToastUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.common.MessageSendStateEnum;
import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.tools.DateUtils;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.uav.R;
import cn.vsx.uav.activity.UavFileListActivity;
import cn.vsx.uav.bean.FileBean;
import cn.vsx.uav.receiveHandler.ReceiveShowPreViewHandler;
import cn.vsx.vc.activity.TransponActivity;
import cn.vsx.vc.fragment.BaseFragment;
import cn.vsx.vc.model.ContactItemBean;
import cn.vsx.vc.model.TransponSelectedBean;
import cn.vsx.vc.model.TransponToBean;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.MyDataUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

import static android.app.Activity.RESULT_OK;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/8/22
 * 描述：
 * 修订历史：
 */
public class PlayVideoFragment extends BaseFragment implements View.OnClickListener{

    private static final String FILE_BEAN = "fileBean";
    private TextureView mTextureView;
    private ImageView mIvClose;
    private TextView mTvFileName;
    private TextView mTvUavForward;
    private ImageView mIvPause;
    private LinearLayout mLlSeekBar;
    private ImageView mIvPauseContinue;
    private TextView mTvCurrentTime;
    private SeekBar mSeekBar;
    private TextView mTvMaxTime;
    private LinearLayout mLlVolume;
    private ImageView mIvVolume;
    private TextView mTvVolume;
    private TextView mTvQuietPlay;
    private FileBean fileBean;
    private MediaPlayer mediaPlayer;
    private int maxTime;

    private static final int UPDATE_PROGRESS = 0;
    private static final int COMPLETE_PROGRESS = 1;
    private static final int HIDE_SEEK_BAR = 2;
    private static final int RECEIVEVOICECHANGED = 3;

    private int currentPosition;
    private boolean playFinish;
    private boolean prepared;
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
                    mIvPauseContinue.setImageResource(cn.vsx.vc.R.drawable.on_pause);
                    break;
                case HIDE_SEEK_BAR:
                    mLlSeekBar.setVisibility(View.GONE);
                    break;
                case RECEIVEVOICECHANGED:
                    mLlVolume.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }
    };

    public static PlayVideoFragment newInstance(FileBean fileBean){
        Bundle args = new Bundle();
        args.putParcelable(FILE_BEAN, fileBean);
        PlayVideoFragment fragment = new PlayVideoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getContentViewId(){
        return R.layout.fragment_play_video;
    }

    @Override
    public void initView(){
        mTextureView = (TextureView) mRootView.findViewById(R.id.texture_view);
        mIvClose = (ImageView) mRootView.findViewById(R.id.iv_close);
        mTvFileName = (TextView) mRootView.findViewById(R.id.tv_file_name);
        mTvUavForward = (TextView) mRootView.findViewById(R.id.tv_uav_forward);
        mIvPause = (ImageView) mRootView.findViewById(R.id.iv_pause);
        mLlSeekBar = (LinearLayout) mRootView.findViewById(R.id.ll_seek_bar);
        mIvPauseContinue = (ImageView) mRootView.findViewById(R.id.iv_pause_continue);
        mTvCurrentTime = (TextView) mRootView.findViewById(R.id.tv_current_time);
        mSeekBar = (SeekBar) mRootView.findViewById(R.id.seek_bar);
        mTvMaxTime = (TextView) mRootView.findViewById(R.id.tv_max_time);
        mLlVolume = (LinearLayout) mRootView.findViewById(R.id.ll_volume);
        mIvVolume = (ImageView) mRootView.findViewById(R.id.iv_volume);
        mTvVolume = (TextView) mRootView.findViewById(R.id.tv_volume);
        mTvQuietPlay = mRootView.findViewById(R.id.tv_quiet_play);
    }

    @Override
    public void initListener(){
        mIvClose.setOnClickListener(this);
        mTvUavForward.setOnClickListener(this);
        mTvQuietPlay.setOnClickListener(this);
        mIvPauseContinue.setOnClickListener(this);
        mIvPause.setOnClickListener(this);

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
    }

    @Override
    public void initData(){
        fileBean = getArguments().getParcelable("fileBean");
        mTvFileName.setText(fileBean.getName());
        play();
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

    @Override
    public void onDestroyView(){
        super.onDestroyView();
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

    @Override
    public void onClick(View v){
        if(v.getId() == R.id.iv_close){
            TerminalFactory.getSDK().notifyReceiveHandler(ReceiveShowPreViewHandler.class,false,fileBean);
        }else if(v.getId() == R.id.tv_uav_forward){
            Intent intent = new Intent(getActivity(), TransponActivity.class);
            intent.putExtra(Constants.TRANSPON_TYPE, Constants.TRANSPON_TYPE_ONE);
            startActivityForResult(intent, UavFileListActivity.CODE_TRANSPON_REQUEST);
        }else if(v.getId() == R.id.tv_quiet_play){
            ToastUtils.showShort(R.string.uav_push_quiet_play);
        }else if(v.getId() == R.id.iv_pause_continue){
            pauseOrContinue();
        }else if(v.getId() == R.id.iv_pause){
            pauseOrContinue();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == UavFileListActivity.CODE_TRANSPON_REQUEST){
            if(resultCode == RESULT_OK){
                //转发返回结果
                TransponSelectedBean bean = (TransponSelectedBean) data.getSerializableExtra(cn.vsx.vc.utils.Constants.TRANSPON_SELECTED_BEAN);
                if(bean != null && bean.getList() != null && !bean.getList().isEmpty()){
                    int type = data.getIntExtra(cn.vsx.vc.utils.Constants.TRANSPON_TYPE, cn.vsx.vc.utils.Constants.TRANSPON_TYPE_ONE);
                    if(type == Constants.TRANSPON_TYPE_ONE){
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put(JsonParam.FILE_NAME, fileBean.getName());
                        jsonObject.put(JsonParam.FILE_SIZE, fileBean.getFileSize());
                        jsonObject.put(JsonParam.SEND_STATE, MessageSendStateEnum.SEND_PRE);
                        jsonObject.put(JsonParam.TOKEN_ID, MyTerminalFactory.getSDK().getMessageSeq());
                        //                                jsonObject.put(JsonParam.DOWN_VERSION_FOR_FAIL, lastVersion);
                        TerminalMessage mTerminalMessage = new TerminalMessage();
                        mTerminalMessage.messageType = MessageType.FILE.getCode();
                        mTerminalMessage.sendTime = System.currentTimeMillis();
                        mTerminalMessage.messagePath = fileBean.getPath();
                        mTerminalMessage.messageFromId = MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
                        mTerminalMessage.messageFromName = MyTerminalFactory.getSDK().getParam(Params.MEMBER_NAME, "");
                        mTerminalMessage.messageFromId = MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
                        mTerminalMessage.messageFromName = MyTerminalFactory.getSDK().getParam(Params.MEMBER_NAME, "");
                        mTerminalMessage.messageBody = jsonObject;
                        transponMessage(mTerminalMessage,bean.getList());
                    }
                }
            }
        }
    }

    public void pauseOrContinue(){
        try{

            if(null != mediaPlayer && mediaPlayer.isPlaying()){
                mediaPlayer.pause();
                mIvPauseContinue.setImageResource(cn.vsx.vc.R.drawable.on_pause);
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
                mIvPauseContinue.setImageResource(cn.vsx.vc.R.drawable.continue_play);
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

        if(TextUtils.isEmpty(fileBean.getPath())){
            ToastUtils.showShort("url为空，不能播放");
        }else{
            try{
                initMediaPlayer(fileBean.getPath());
            }catch(Exception e){
                logger.error(e);
            }
        }
    }

    private void initMediaPlayer(String url) throws IOException{
        logger.info("视频地址："+url);
        if(mediaPlayer == null){
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();
            ToastUtils.showShort(R.string.quiet_play);
            mediaPlayer.setVolume(0f, 0f);
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
                mIvPauseContinue.setImageResource(cn.vsx.vc.R.drawable.continue_play);
                mHandler.sendEmptyMessage(UPDATE_PROGRESS);
                mHandler.sendEmptyMessageDelayed(HIDE_SEEK_BAR, 2000);
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
            @Override
            public void onCompletion(MediaPlayer mp){
                logger.info("onCompletion");
                mIvPauseContinue.setImageResource(cn.vsx.vc.R.drawable.on_pause);
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
                mIvPauseContinue.setImageResource(cn.vsx.vc.R.drawable.on_pause);
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

    public void transponMessage(TerminalMessage transponMessage, ArrayList<ContactItemBean> list) {

        logger.info("转发消息，type:" + transponMessage);
        //单个转发
        List<Integer> toIds = MyDataUtil.getToIdsTranspon(list);
        TransponToBean bean = MyDataUtil.getToNamesTranspon(list);
        List<Long> toUniqueNos = MyDataUtil.getToUniqueNoTranspon(list);
        if(bean!=null){
            transponMessage.messageToId = bean.getNo();
            transponMessage.messageToName = bean.getName();
        }
        transponMessage.messageFromId = MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
        transponMessage.messageFromName = MyTerminalFactory.getSDK().getParam(Params.MEMBER_NAME, "");
        transponMessage.messageBody.put(JsonParam.TOKEN_ID, MyTerminalFactory.getSDK().getMessageSeq());

        if (transponMessage.messageType == MessageType.FILE.getCode()) {
            transponFileMessage(transponMessage, toIds,toUniqueNos);
        }
    }

    /***  转发文件消息 **/
    private void transponFileMessage(TerminalMessage terminalMessage, List<Integer> list,List<Long> toUniqueNos) {
        terminalMessage.messageBody.put(JsonParam.SEND_STATE, MessageSendStateEnum.SENDING);
        File file = new File(terminalMessage.messagePath);
        MyTerminalFactory.getSDK().upload(list,toUniqueNos, file, terminalMessage, false);
    }
}

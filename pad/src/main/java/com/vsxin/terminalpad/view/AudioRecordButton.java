package com.vsxin.terminalpad.view;

import android.Manifest;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.blankj.utilcode.util.ToastUtils;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.manager.AudioManager;
import com.zectec.imageandfileselector.receivehandler.ReceiverSendFileHandler;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import cn.vsx.hamster.common.Authority;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.audio.CheckMyPermission;
import ptt.terminalsdk.tools.ToastUtil;

//录音按钮核心类，包括点击、响应、与弹出对话框交互等操作。
public class AudioRecordButton extends android.support.v7.widget.AppCompatButton implements AudioManager.AudioStageListener {

    //三个对话框的状态常量
    private static final int STATE_NORMAL = 1;
    private static final int STATE_RECORDING = 2;
    private static final int STATE_WANT_TO_CANCEL = 3;

    //垂直方向滑动取消的临界距离
    private static final int DISTANCE_Y_CANCEL = 50;

    //当前状态
    private int mCurrentState = STATE_NORMAL;
    // 正在录音标记
    private boolean isRecording = false;
    //录音对话框
    private DialogManager mDialogManager;
    //核心录音类
    private AudioManager mAudioManager;
    //当前录音时长
    private float mTime = 0;
    //标记是否强制终止
    private boolean isOverTime = false;
    //最大录音时长（单位:s）。def:60s
    private int MAX_RECORD_TIME = 60;
    //手指按下，音频是否已经准备好录制
//    private boolean mReady;
    //上下文
    Context mContext;
    //设置是否允许录音,这个是是否有录音权限
    private boolean mHasRecordPromission = true;
    private AudioPauseListener audioPauseListener;

    public boolean isHasRecordPromission() {
        return mHasRecordPromission;
    }

    public void setHasRecordPromission(boolean hasRecordPromission) {
        this.mHasRecordPromission = hasRecordPromission;
    }

    @Override
    public boolean isInEditMode() {
        return true;
    }

    public AudioRecordButton(Context context) {
        this(context, null);
    }

    public AudioRecordButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;


        //初始化语音对话框
        mDialogManager = new DialogManager(getContext());

        //获取录音保存位置
        String dir = MyTerminalFactory.getSDK().getAudioRecordDirectory();
        //实例化录音核心类
        mAudioManager = AudioManager.getInstance(dir);

        mAudioManager.setOnAudioStageListener(this);
        setTextColor(getResources().getColor(R.color.white));
    }

    public void onPause(){
        mDialogManager.dimissDialog();
        if(isRecording){
            mAudioManager.release();// release释放一个mediarecorder
        }
        if (mListener != null) {// 并且callbackActivity，保存录音

            mListener.onFinished(mTime, mAudioManager.getCurrentFilePath());
        }
        if(audioPauseListener!=null){
            audioPauseListener.onPause();
        }
        reset();
    }

    public interface AudioFinishRecorderListener {
        void onFinished(float seconds, String filePath);
    }

    private AudioFinishRecorderListener mListener;

    public void setAudioFinishRecorderListener(AudioFinishRecorderListener listener) {
        mListener = listener;
    }

    public interface AudioPauseListener{
        void onPause();
    }

    public void setAudioPauseListener(AudioPauseListener listener){
        this.audioPauseListener = listener;
    }

    // 三个状态
    private static final int MSG_COUNT_DOWN = 0;
    private static final int MSG_AUDIO_PREPARED = 0X110;
//    private static final int MSG_VOICE_CHANGE = 0X111;
    private static final int MSG_DIALOG_DIMISS = 0X112;
    //手指状态，是否在按下，只有在按下时才能录制
    private boolean down;
    @SuppressWarnings("handlerLeak")
    private Handler mStateHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_COUNT_DOWN:
                    //最长mMaxRecordTimes
                    if (MAX_RECORD_TIME <= mTime) {
                        reset();// 恢复标志位
                        mDialogManager.dimissDialog();
                        mAudioManager.release();// release释放一个mediarecorder
                        isOverTime = true;//超时
                        mStateHandler.removeMessages(MSG_COUNT_DOWN);
                        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverSendFileHandler.class, ReceiverSendFileHandler.VOICE);
                        if(null !=mListener){
                            mListener.onFinished(mTime, mAudioManager.getCurrentFilePath());
                        }
                    }else {
                        //剩余10s
                        mTime+=0.1f;
                        showRemainedTime();
                        mDialogManager.updateVoiceLevel(mAudioManager.getVoiceLevel(7));
                        mStateHandler.sendEmptyMessageDelayed(MSG_COUNT_DOWN,100);
                    }

                    break;
                case MSG_AUDIO_PREPARED:
                    // 显示应该是在audio end prepare之后回调
                    //如果按下的时间太短，已经抬起手指，这个回调还没执行
                    if(down){

                        isRecording = true;
                        mTime = 0;
                        mStateHandler.sendEmptyMessage(MSG_COUNT_DOWN);
                    }
                    break;

                case MSG_DIALOG_DIMISS:
                    mDialogManager.dimissDialog();
                    break;

            }
        }
    };
    //是否触发过震动
    boolean isShcok;

    private void showRemainedTime() {
        //倒计时
        int remainTime = (int) (MAX_RECORD_TIME - mTime);
        //提醒倒计时
        int mRemainedTime = 10;
        if (remainTime < mRemainedTime) {
            if (!isShcok) {
                isShcok = true;
                doShock();
            }

            mDialogManager.getTipsTxt().setText(String.format(mContext.getString(R.string.text_can_speak_time),remainTime));
        }

    }

    /*
     * 想设置震动大小可以通过改变pattern来设定，如果开启时间太短，震动效果可能感觉不到
     * */
    private void doShock() {
        //震动类
        Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {100, 400, 100, 400};   // 停止 开启 停止 开启
        vibrator.vibrate(pattern, -1);           //重复两次上面的pattern 如果只想震动一次，index设为-1
    }

    // 在这里面发送一个handler的消息
    @Override
    public void wellPrepared() {
        mStateHandler.sendEmptyMessage(MSG_AUDIO_PREPARED);
    }

    private long lastDownTime = 0;
    //手指滑动监听
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!CheckMyPermission.selfPermissionGranted(getContext(), Manifest.permission.RECORD_AUDIO)){
                    ToastUtils.showShort(R.string.no_record_perssion);
                }
                if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_MESSAGE_SEND.name())){
                    ToastUtil.showToast(mContext,mContext.getString(R.string.text_has_no_send_message_authority));
                    reset();
                    return super.onTouchEvent(event);
                }
//                if(MyApplication.instance.isMiniLive){
//                    ToastUtil.showToast(mContext,mContext.getString(R.string.text_small_window_mode_can_not_do_this));
//                    return super.onTouchEvent(event);
//                }
                if(System.currentTimeMillis()-lastDownTime<1000){
                    return super.onTouchEvent(event);
                }
                if(!isEnabled()){
                    ToastUtil.showToast(this.mContext,mContext.getString(R.string.text_the_other_party_not_supported_this_message_type));
                    reset();
                    return super.onTouchEvent(event);
                }
                changeState(STATE_RECORDING);
                //录音前释放资源
                mAudioManager.release();
                down = true;

                if (isHasRecordPromission() ) {
                    mDialogManager.recording();
                    mAudioManager.prepareAudio();
                }
                lastDownTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_MOVE:
                if (isRecording) {
                    // 根据x，y来判断用户是否想要取消
                    if (wantToCancel(x, y)) {
                        changeState(STATE_WANT_TO_CANCEL);
                    }

                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // 首先判断是否有触发onlongclick事件，没有的话直接返回reset
//                changeState(STATE_WANT_TO_CANCEL);
                if(mCurrentState == STATE_WANT_TO_CANCEL){
                    mDialogManager.dimissDialog();
                }
                if (isOverTime) {
                    reset();
                    mDialogManager.dimissDialog();
                    return super.onTouchEvent(event);
                }
                if (!isRecording ||mTime < 1f) {
                    mDialogManager.tooShort();
                    if(isRecording){
                        mAudioManager.cancel();
                    }
                    mStateHandler.sendEmptyMessageDelayed(MSG_DIALOG_DIMISS, 1000);// 持续1s
                } else if (mCurrentState == STATE_RECORDING) {//正常录制结束
                    mDialogManager.dimissDialog();
                    mAudioManager.release();// release释放一个mediarecorder
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverSendFileHandler.class, ReceiverSendFileHandler.VOICE);
                    if (mListener != null) {// 并且callbackActivity，保存录音

                        mListener.onFinished(mTime, mAudioManager.getCurrentFilePath());
                    }


                } else if (mCurrentState == STATE_WANT_TO_CANCEL) {
                    // cancel
                    mDialogManager.dimissDialog();
                    mAudioManager.cancel();
                }
                down = false;
                reset();
                break;
        }

        return super.onTouchEvent(event);
    }

    /**
     * 回复标志位以及状态
     */
    private void reset() {
        mStateHandler.removeMessages(MSG_COUNT_DOWN);
        isRecording = false;
        changeState(STATE_NORMAL);
        mTime = 0;

        isOverTime = false;
        isShcok = false;
    }

    private boolean wantToCancel(int x, int y) {
        if (x < 0 || x > getWidth()) {// 判断是否在左边，右边，上边，下边
            return true;
        }
        if (y < -DISTANCE_Y_CANCEL || y > getHeight() + DISTANCE_Y_CANCEL) {
            return true;
        }

        return false;
    }

    private void changeState(int state) {
        if (mCurrentState != state) {
            mCurrentState = state;
            switch (mCurrentState) {
                case STATE_NORMAL:
                    setText(mContext.getString(R.string.long_click_record));//长按录音
                    break;
                case STATE_RECORDING:
                    setText(R.string.hang_up_finsh);//松开结束
//                    setTextColor(getResources().getColor(R.color.white));
                    if (isRecording) {
                        // 复写dialog.recording();
                        mDialogManager.recording();
                    }
                    break;

                case STATE_WANT_TO_CANCEL:
                    setText(R.string.release_cancel);//松开取消
                    // dialog want to cancel
                    mDialogManager.wantToCancel();
                    mStateHandler.removeMessages(MSG_COUNT_DOWN);
                    break;

            }
        }

    }

    @Override
    public boolean onPreDraw() {
        return false;
    }



    public int getMaxRecordTime() {
        return MAX_RECORD_TIME;
    }

    public void setMaxRecordTime(int maxRecordTime) {
        MAX_RECORD_TIME = maxRecordTime;
    }

    public void cancel(){
        mStateHandler.removeCallbacksAndMessages(null);
        if(isRecording){
            mAudioManager.cancel();
        }
        mDialogManager.dimissDialog();
        reset();
        lastDownTime = 0;
    }


}

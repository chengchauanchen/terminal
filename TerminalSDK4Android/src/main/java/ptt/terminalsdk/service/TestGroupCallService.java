package ptt.terminalsdk.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.SignalServerErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRequestGroupCallConformationHandler;
import ptt.terminalsdk.R;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.receiveHandler.ReceiveTestGroupCallHandler;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2018/12/29
 * 描述：
 * 修订历史：
 */
public class TestGroupCallService extends Service {

    protected Logger logger = Logger.getLogger(this.getClass());
    protected final String TAG = this.getClass().getSimpleName();
    protected WindowManager windowManager;
    protected WindowManager.LayoutParams layoutParams;
    protected WindowManager.LayoutParams layoutParams1;
    protected int screenWidth;
    protected int screenHeight;
    protected PowerManager.WakeLock wakeLock;
    protected View rootView;
    protected boolean dialogAdd;//是否添加了弹窗
    private float downX = 0;
    private float downY = 0;
    private int oddOffsetX = 0;
    private int oddOffsetY = 0;

    private RelativeLayout rlContent;
    private TextView tvClose;
    private EditText etAllCount;
    private EditText etGroupCallTime;
    private EditText etTimeInterval;
    private TextView txResultTitle;
    private TextView txResult;
    private TextView txEnd;
    private Button btStart;
    private Button btEnd;
    //打印log日志
    private StringBuffer stringBuffer = new StringBuffer();
    private int allCount = 1000;
    private int currentCount = 1;
    private int errorCount = 0;
    private long groupCallTime = 5*1000;//每次组呼的时长
    private long groupCallTimeInterval = 1*1000;//上次组呼和下次组呼的间隔时间

    private static final int START_GROUP_CALL = 1;
    private static final int CANCEL_GROUP_CALL = 2;


    @SuppressLint("HandlerLeak")
    protected Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch (msg.what){
                case START_GROUP_CALL:
                    currentCount++;
                    TerminalFactory.getSDK().notifyReceiveHandler(ReceiveTestGroupCallHandler.class,true);
                    break;
                case CANCEL_GROUP_CALL:
                    TerminalFactory.getSDK().notifyReceiveHandler(ReceiveTestGroupCallHandler.class,false);
                    if(currentCount<allCount){
                        removeMessages(START_GROUP_CALL);
                        sendEmptyMessageDelayed(START_GROUP_CALL,groupCallTimeInterval);
                    }
                    break;
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public void onCreate(){
        logger.info(TAG+":onCreate");
        super.onCreate();
        initWakeLock();
        setRootView();
        findView();
        initData();
        initWindow();
        initListener();
    }

    @SuppressLint("InvalidWakeLockTag")
    protected void initWakeLock(){
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if(null != powerManager){
            //noinspection deprecation
            wakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "wakeLock");
        }
    }

    protected void setRootView(){
        rootView = LayoutInflater.from(MyTerminalFactory.getSDK().application).inflate(R.layout.layout_test_group_call_service, null);
    }

    protected void findView(){
        rlContent = rootView.findViewById(R.id.rl_content);
        tvClose = rootView.findViewById(R.id.tv_close);
        etAllCount = rootView.findViewById(R.id.et_all_count);
        etGroupCallTime = rootView.findViewById(R.id.et_group_call_time);
        etTimeInterval = rootView.findViewById(R.id.et_time_interval);
        txResultTitle = rootView.findViewById(R.id.tx_result_title);
        txResult = rootView.findViewById(R.id.tx_result);
        txEnd = rootView.findViewById(R.id.tx_end);
        btStart = rootView.findViewById(R.id.bt_start);
        btEnd = rootView.findViewById(R.id.bt_end);
    }

    protected void initData(){
    }

    protected void initWindow(){
        windowManager = (WindowManager) this.getSystemService(WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_PHONE ,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED, PixelFormat.RGBA_8888);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.gravity = Gravity.END|Gravity.TOP;
        //小窗口type，要让下层view可以获取焦点
        layoutParams.flags = WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        layoutParams1 = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_PHONE ,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED, PixelFormat.RGBA_8888);

        //大窗口
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams1.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams1.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams1.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING;
        layoutParams1.gravity = Gravity.CENTER;
        //大窗口type，下层view不获取焦点
        layoutParams1.flags = WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED|
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

        setScreenWidth();
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void initListener(){
        MyTerminalFactory.getSDK().registReceiveHandler(receiveRequestGroupCallConformationHandler);
//        MyTerminalFactory.getSDK().registReceiveHandler(receiveAnswerIndividualCallTimeoutHandler);
//        MyTerminalFactory.getSDK().registReceiveHandler(receiveStopStartReceiveCallServiceHandler);
        etAllCount.setEnabled(true);
        etGroupCallTime.setEnabled(true);
        etTimeInterval.setEnabled(true);
        btStart.setEnabled(true);
        rlContent.setOnTouchListener(removeOnTouchListener);
        btStart.setOnClickListener(startListener);
        btEnd.setOnClickListener(endListener);
        tvClose.setOnClickListener(mCloseListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
//        KeepLiveManager.getInstance().setServiceForeground(this);
        logger.info(TAG+"---onStartCommand--"+dialogAdd);
        if(!dialogAdd){
            windowManager.addView(rootView, layoutParams);
            dialogAdd = true;
            if(null != intent){
                initView(intent);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    protected void initView(Intent intent){
        wakeLock.acquire(10 * 1000);
        etAllCount.setSelection(etAllCount.getText().length());
        etGroupCallTime.setSelection(etGroupCallTime.getText().length());
        etTimeInterval.setSelection(etTimeInterval.getText().length());
    }

    protected void setScreenWidth(){
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
    }

    @SuppressLint("ClickableViewAccessibility")
    private View.OnTouchListener removeOnTouchListener = (v, event) -> {
        //触摸点到边界屏幕的距离
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                //触摸点到自身边界的距离
                downX = event.getX();
                downY = event.getY();
                oddOffsetX = layoutParams.x;
                oddOffsetY = layoutParams.y;
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();
                //不除以3，拖动的view抖动的有点厉害
                if(Math.abs(downX - moveX) > 5 || Math.abs(downY - moveY) > 5){
                    // 更新浮动窗口位置参数
                    layoutParams.x = (int) (screenWidth - (x + downX));
                    layoutParams.y = (int) (y - downY);
                    windowManager.updateViewLayout(rootView, layoutParams);
                }
                break;
            case MotionEvent.ACTION_UP:
                int newOffsetX = layoutParams.x;
                int newOffsetY = layoutParams.y;
                if(Math.abs(newOffsetX - oddOffsetX) <= 30 && Math.abs(newOffsetY - oddOffsetY) <= 30){
                }
                break;
        }
        return true;
    };
    private View.OnClickListener startListener = v -> mStartListener();
    private View.OnClickListener endListener = v ->{
        mHandler.post(() -> updateUI());
        stopTest("主动停止");
    };
    private View.OnClickListener mCloseListener = v -> mCloseListener();

    /**
     * 主动方请求组呼的消息
     */
    private ReceiveRequestGroupCallConformationHandler receiveRequestGroupCallConformationHandler = new ReceiveRequestGroupCallConformationHandler() {
        @Override
        public void handler(final int methodResult, final String resultDesc, int groupId) {
            if(methodResult != SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode()){
                if (methodResult == BaseCommonCode.SUCCESS_CODE) {
                    checkCount(false);
                } else if (methodResult == SignalServerErrorCode.RESPONSE_GROUP_IS_DISABLED.getErrorCode()) {
                    //响应组为禁用状态，低级用户无法组呼
                    mHandler.post(() -> {
                        updateUI();
                        stopTest(SignalServerErrorCode.RESPONSE_GROUP_IS_DISABLED.getErrorDiscribe());
                    });
                } else if (methodResult == SignalServerErrorCode.CANT_SPEAK_IN_GROUP.getErrorCode()) {
                    //只听组
                    mHandler.post(() -> {
                        updateUI();
                        stopTest(SignalServerErrorCode.RESPONSE_GROUP_IS_DISABLED.getErrorDiscribe());
                    });

                } else if (methodResult == SignalServerErrorCode.TETRA_INVALID_RETURN.getErrorCode()) {
                    //电台
                    mHandler.post(() -> {
                        updateUI();
                        stopTest(SignalServerErrorCode.TETRA_INVALID_RETURN.getErrorDiscribe());
                    });
                } else {
                    errorCount++;
                    checkCount(true);
                }
            }
        }
    };
    /**
     * 开始测试
     */
    private void mStartListener() {
        //初始化数据
        stringBuffer.setLength(0);
        allCount = stringToInt(etAllCount.getText().toString());
        currentCount = 0;
        errorCount = 0;
        groupCallTime = stringToLong(etGroupCallTime.getText().toString())*1000;
        groupCallTimeInterval = stringToLong(etTimeInterval.getText().toString())*1000;
        if(groupCallTime<=0||groupCallTime>60*1000){
            ToastUtil.showToast(this,"请输入大于0秒小于60秒的组呼时长");
            return;
        }
        if(groupCallTimeInterval<=0){
            ToastUtil.showToast(this,"请输入大于0秒组呼间隔时间");
            return;
        }
        //设置UI
        etAllCount.setEnabled(false);
        etGroupCallTime.setEnabled(false);
        etTimeInterval.setEnabled(false);
        btStart.setEnabled(false);
        txEnd.setText("");
        txEnd.setVisibility(View.GONE);
        stringBuffer.append("测试开始时间： "+getCurrentTime()).append("\n");
        stringBuffer.append("测试总次数："+allCount);
        txResultTitle.setText(stringBuffer.toString());
        txResult.setText(" 当前次数："+currentCount+"  失败次数："+errorCount);
        stringBuffer.append("\n");
        //开始组呼
        mHandler.removeCallbacksAndMessages(null);
        mHandler.sendEmptyMessage(START_GROUP_CALL);
    }

    /**
     * 刷新页面
     */
    private void updateUI(){
        stringBuffer.append("当前次数："+currentCount+"  失败次数："+errorCount+" 时间："+getCurrentTime()).append("\n");
        txResult.setText("当前次数："+currentCount+"  失败次数："+errorCount);
    }

    /**
     * 检查次数
     * @param isStart
     */
    private void checkCount(boolean isStart){
        mHandler.post(() -> updateUI());
        if(currentCount >= allCount){
            mHandler.sendEmptyMessageDelayed(CANCEL_GROUP_CALL,groupCallTime);
            stopTest("测试结束");
            return;
        }
        if(isStart){
            mHandler.sendEmptyMessage(START_GROUP_CALL);
        }else {
            mHandler.sendEmptyMessageDelayed(CANCEL_GROUP_CALL,groupCallTime);
        }
    }

    /**
     * 停止测试
     * @param errorDiscribe
     */
    private void stopTest(String errorDiscribe) {
        stringBuffer.append("结束："+errorDiscribe+" 时间："+getCurrentTime()).append("\n");
        stringBuffer.append("测试总次数："+allCount+" 当前次数："+currentCount+"  失败次数："+errorCount+" 成功率："+((float)(currentCount-errorCount)/(float)currentCount)*100+"%").append("\n");
        logger.info("组呼测试结果:\n"+stringBuffer.toString());
        txEnd.setText("测试结束："+errorDiscribe);
        txEnd.setVisibility(View.VISIBLE);
        mHandler.removeMessages(START_GROUP_CALL);
        etAllCount.setEnabled(true);
        etGroupCallTime.setEnabled(true);
        etTimeInterval.setEnabled(true);
        btStart.setEnabled(true);
    }
    /**
     * 停止测试
     */
    private void mCloseListener() {
        stopBusiness();
    }

    private String getCurrentTime(){
         return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    /**
     * String 转int
     * @return
     */
    public static int stringToInt(String string){
        int data = 0;
        if(!TextUtils.isEmpty(string)){
            try{
                data = Integer.valueOf(string);
            }catch (Exception e){
                data =  0 ;
            }
        }
        return data;
    }

    /**
     * String转Long
     * @param data
     * @return
     */
    public static long stringToLong(String data){
        long result = 0L;
        if(!TextUtils.isEmpty(data)){
            try{
                result = Long.valueOf(data);
            }catch (Exception e){
                e.printStackTrace();
                result = 0L;
            }
        }
        return result;
    }
    /**
     * 移除view并停止service
     */
    protected void removeView(){
        logger.info(TAG+"--ReceiverRemoveWindowViewHandler:"+dialogAdd);
        mHandler.removeCallbacksAndMessages(null);
        if(dialogAdd){
            windowManager.removeView(rootView);
            dialogAdd = false;
        }
        stopSelf();
    }


    /**
     * 退出业务状态
     */
    protected void stopBusiness(){
        mHandler.post(this::removeView);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        logger.info(TAG+":onDestroy");
        TerminalFactory.getSDK().notifyReceiveHandler(ReceiveTestGroupCallHandler.class,false);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveRequestGroupCallConformationHandler);
    }

}

package cn.vsx.vc.utils;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import cn.vsx.vc.R;
import cn.vsx.vc.receive.Actions;
import cn.vsx.vc.receive.SendRecvHelper;
import cn.vsx.vc.view.IndividualCallTimerView;

/**
 * Created by jamie on 2017/10/17.
 */

public class CallAbbreviatedWindowService extends Service {
    private Handler handler = new Handler();
    private static final int UPDATE_PIC = 0x100;
    private int statusBarHeight;// 状态栏高度
    private View view;// 透明窗体
    private IndividualCallTimerView individualCallTimerView;
    private TextView tv_waiting;
    private Thread updateThread = null;
    private boolean viewAdded = false;// 透明窗体是否已经显示
    private boolean viewHide = false; // 窗口隐藏
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private MyBinder mBinder = new MyBinder();
    public CallAbbreviatedWindowService() {
        super();
//        createFloatView();
    }

    @Nullable

    @Override
    public IBinder onBind(Intent intent) {
        return  mBinder;
    }
    public class MyBinder extends Binder {

        public void onStartTime(){
            Log.d("sjl_:", "onStartTime: 通话开始");
            individualCallTimerView.start();
        }
        public void onStopTime(){
            Log.d("sjl_:", "onStartTime: 通话结束");
            individualCallTimerView.stop();
        }
        public void showPop(){
            viewHide = false;
            refresh();
        }
        public void hideView(){
            removeView();
        }
        public void change(){
            changeView();
        }

    }
        @Override
    public void onCreate() {
        super.onCreate();
        Log.e("sjl_个呼小窗口服务创建","onCreate");
        createFloatView();


    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.e("sjl_个呼小窗口服务启动","onStart");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        removeView();
    }
    /**
     * 关闭悬浮窗
     */
    public void removeView() {
        if (viewAdded) {
            windowManager.removeView(view);
            viewAdded = false;
        }
    }

    public void createFloatView(){
        Log.e("sjl_个呼小窗口服务创建","进入创建视图的方法");
        view = LayoutInflater.from(this).inflate(R.layout.popupwindow_minimize,null);
        individualCallTimerView = (IndividualCallTimerView) view.findViewById(R.id.popup_ICTV_speaking_time);
        tv_waiting = (TextView) view.findViewById(R.id.tv_waiting);
        windowManager = (WindowManager) this.getSystemService(WINDOW_SERVICE);
               /*
         * LayoutParams.TYPE_SYSTEM_ERROR：保证该悬浮窗所有View的最上层
         * LayoutParams.FLAG_NOT_FOCUSABLE:该浮动窗不会获得焦点，但可以获得拖动
         * PixelFormat.TRANSPARENT：悬浮窗透明
         */
        layoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSPARENT);
         layoutParams.gravity = Gravity.RIGHT|Gravity.TOP; //悬浮窗开始在右上角显示
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("sjl_个呼小窗口服务创建","点击了全屏");
                removeView();
                SendRecvHelper.send(getApplicationContext(), Actions.ACT_SHOW_FULL_SCREEN);
            }
        });
    }
    /**
     * 添加悬浮窗或者更新悬浮窗 如果悬浮窗还没添加则添加 如果已经添加则更新其位置
     */
    private void refresh() {
        // 如果已经添加了就只更新view
        if (viewAdded) {
            windowManager.updateViewLayout(view, layoutParams);
        } else {
            Log.d("sjl_:", "refresh:视图显示 ");
            windowManager.addView(view, layoutParams);
            viewAdded = true;
        }
    }
    private void changeView(){
        tv_waiting.setVisibility(View.GONE);
        individualCallTimerView.setVisibility(View.VISIBLE);
    }



}

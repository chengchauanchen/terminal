package cn.vsx.vsxsdk.dialog;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.vsx.vsxsdk.R;
import java.util.Timer;
import java.util.TimerTask;

public class SdkTemptToast extends Toast {

    private Context context;
    private TextView tvContent;
    private LinearLayout llButton;

    private int type = 1;
    public static final int TYPE_INSTALL = 1;//请重新安装启融合通信
    public static final int TYPE_OPEN = 2;//请开启融合通信

    private long time = 0;
    private  Timer timer;

    public SdkTemptToast(Context context, int type ,long time) {
        super(context);
        this.context = context;
        this.type = type;
        this.time = time;
        initToast();
        setView();
    }

    /**
     * 初始化Toast
     */
    private  void initToast() {
        try {
            // 获取LayoutInflater对象
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // 由layout文件创建一个View对象
            View layout = inflater.inflate(R.layout.dialog_tempt, null);
            tvContent = layout.findViewById(R.id.tv_content);
            llButton = layout.findViewById(R.id.ll_button);
            setView(layout);
            setGravity(Gravity.CENTER, 0, 0);
            setDuration(Toast.LENGTH_LONG);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置控件
     */
    private void setView() {
        switch (type){
            case TYPE_INSTALL:
                //请重新安装启融合通信
                tvContent.setText(context.getString(R.string.text_install));
                llButton.setVisibility(View.GONE);
                break;
            case TYPE_OPEN:
                //请开启融合通信
                tvContent.setText(context.getString(R.string.text_open));
                llButton.setVisibility(View.GONE);
                break;
                default:break;
        }
    }

    /**
     * 设置时间显示
     */
    public void showTime(){
        if(timer!=null){
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                show();
            }
        }, 0, 1000);//每隔三秒调用一次show方法;
        mHandler.sendEmptyMessageDelayed(HANDLER_CODE_DISMISS, time);
    }

    @Override
    public void cancel() {
        try {
            super.cancel();
        } catch (Exception e) {
        }
    }

    @Override
    public void show() {
        try {
            super.show();
        } catch (Exception e) {
        }
    }
    private static final int HANDLER_CODE_DISMISS = 0;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
      @Override
      public void handleMessage(Message msg) {
        super.handleMessage(msg);
        if (msg.what == HANDLER_CODE_DISMISS) {
          mHandler.removeMessages(HANDLER_CODE_DISMISS);
            if(timer!=null){
                timer.cancel();
            }
            cancel();

        }
      }
    };

}

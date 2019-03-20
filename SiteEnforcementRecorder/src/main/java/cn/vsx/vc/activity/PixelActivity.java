package cn.vsx.vc.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

public class PixelActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.e("PixelActivity", "启动1像素透明Activity");
        Window window = getWindow();
        window.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = 0;
        params.y = 0;
        params.height = 1;
        params.width = 1;
        window.setAttributes(params);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("FINISH_PIXELACTIVITY");
        registerReceiver(openLockReceiver, intentFilter);

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.e("PixelActivity", "关闭1像素Activity");
        if(openLockReceiver != null){
            unregisterReceiver(openLockReceiver);
        }
    }

    private BroadcastReceiver openLockReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent){
            String action = intent.getAction();
            if(action !=null && "FINISH_PIXELACTIVITY".equals(action)){
                Log.e("PixelActivity", "收到FINISH_PIXELACTIVITY");
                PixelActivity.this.finish();
            }
        }
    };
}

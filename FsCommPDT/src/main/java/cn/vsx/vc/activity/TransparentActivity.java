package cn.vsx.vc.activity;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import cn.vsx.vc.R;
import cn.vsx.vc.utils.ActivityCollector;

/**
 * 透明activity，用于个呼到来时启动接听界面
 */
public class TransparentActivity extends BaseActivity{

    @Override
    public int getLayoutResId(){
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON|
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        return R.layout.activity_transparent;
    }

    @Override
    public void initView(){
        ActivityCollector.addActivity(this, getClass());
        Window window = getWindow();
        //放在左上角
        window.setGravity(Gravity.START | Gravity.TOP);
        WindowManager.LayoutParams attributes = window.getAttributes();
        //宽高设计为1个像素
        attributes.width = 1;
        attributes.height = 1;
        //起始坐标
        attributes.x = 0;
        attributes.y = 0;
        window.setAttributes(attributes);

    }

    @Override
    public void initListener(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        intentFilter.addAction("FINISH_TRANSPARENT");
        registerReceiver(openLockReceiver, intentFilter);
    }

    @Override
    public void initData(){
        Log.e("TransparentActivity", "启动了TransparentActivity");

        //得到键盘锁管理器对象
//        KeyguardManager km= (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
//        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
//        //解锁
//        kl.disableKeyguard();
    }
    
    @Override
    public void doOtherDestroy(){
        ActivityCollector.removeActivity(this);
        if(openLockReceiver != null){
            unregisterReceiver(openLockReceiver);
        }
    }

    private BroadcastReceiver openLockReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent){
            String action = intent.getAction();
            if(action != null && action.equals(Intent.ACTION_USER_PRESENT)){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
                    if(((KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE)).isKeyguardSecure()){
                        TransparentActivity.this.finish();
                    }
                }
            }else if(action !=null && "FINISH_TRANSPARENT".equals(action)){
                Log.e("TransparentActivity", "收到FINISH_TRANSPARENT");
                TransparentActivity.this.finish();
            }
        }
    };
}

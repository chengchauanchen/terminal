package cn.vsx.vc.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.widget.RelativeLayout;

import cn.vsx.vc.R;

import static cn.vsx.vc.utils.BitmapUtil.computeSampleSize;

/**
 * Created by zckj on 2017/4/1.
 */

public class KilledActivity extends BaseActivity {

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            finish();
        }
    };


    @Override
    public int getLayoutResId(){
        return R.layout.activity_killed;
    }

    @Override
    public void initView(){
    }

    @Override
    public void initListener(){
    }

    @Override
    public void initData(){
        handler.sendEmptyMessageDelayed(0,5000);
    }

    @Override
    public void doOtherDestroy(){
        logger.info("KilledActivity---doOtherDestroy");
    }

    private void setBackground() {
        RelativeLayout relativeLayout =  findViewById(R.id.rl_kill);

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.regist_bg, opts);

        opts.inSampleSize = computeSampleSize(opts, -1, 480 * 640);
        opts.inJustDecodeBounds = false;
        try {
            Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.regist_bg, opts);
            relativeLayout.setBackgroundDrawable(new BitmapDrawable(bmp));
        } catch (OutOfMemoryError err) {
            err.printStackTrace();
        }
    }
}

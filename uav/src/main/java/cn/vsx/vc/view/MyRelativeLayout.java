package cn.vsx.vc.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2018/6/7
 * 描述：
 * 修订历史：
 */

public class MyRelativeLayout extends RelativeLayout{

    private ScreenLockListener screenLockListener;
    private boolean unLocked;
    public MyRelativeLayout(Context context){
        super(context);
    }

    public MyRelativeLayout(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    public MyRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev){
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev){
        return super.dispatchTouchEvent(ev);
    }

    private float downY = 0.0F;
    @Override
    public boolean onTouchEvent(MotionEvent event){
        int action = event.getAction();
        switch(action){
            case MotionEvent.ACTION_DOWN:
                downY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                float rawY = event.getRawY();
                if(!unLocked && downY !=0 && downY-rawY>80){
                    if(null !=screenLockListener){
                        unLocked = true;
                        screenLockListener.onScreenLock();
                    }
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;

    }

    public void setScreenLockListener(ScreenLockListener screenLockListener){
        this.screenLockListener = screenLockListener;
    }

    public interface ScreenLockListener{
        void onScreenLock();
    }
}

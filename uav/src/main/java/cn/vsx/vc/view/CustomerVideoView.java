package cn.vsx.vc.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2018/10/15
 * 描述：
 * 修订历史：
 */

public class CustomerVideoView extends VideoView{
    public CustomerVideoView(Context context) {
        super(context);
    }

    public CustomerVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomerVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 其实就是在这里做了一些处理。
        int width = getDefaultSize(0, widthMeasureSpec);
        int height = getDefaultSize(0, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }
}

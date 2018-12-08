package com.zectec.imageandfileselector.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by gt358 on 2017/9/5.
 */

public class LoadingCircleView extends View {
    private Paint paintBgCircle;
    private Paint paintProgressCircle;


    private float startAngle = -90f;//开始角度

    private float sweepAngle = 0;//结束

    private int progressCirclePadding = 0;//进度圆与背景圆的间距

    public LoadingCircleView(Context context) {
        this(context, null);
    }

    public LoadingCircleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingCircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init () {
        paintBgCircle = new Paint();
        paintBgCircle.setAntiAlias(true);
        paintBgCircle.setColor(Color.WHITE);
        paintBgCircle.setStyle(Paint.Style.FILL);

        paintProgressCircle = new Paint();
        paintProgressCircle.setAntiAlias(true);
        paintProgressCircle.setColor(Color.parseColor("#88ffffff"));
        paintProgressCircle.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(getMeasuredWidth() / 2, getMeasuredWidth() / 2, getMeasuredWidth() / 2, paintBgCircle);
        RectF f = new RectF(progressCirclePadding, progressCirclePadding, getMeasuredWidth() - progressCirclePadding, getMeasuredWidth() - progressCirclePadding);
        canvas.drawArc(f, startAngle, sweepAngle, true, paintProgressCircle);

    }

    public void setProgerss(int progerss) {
        sweepAngle = (float) (360 / 100.0 * progerss);
        invalidate();
    }

}

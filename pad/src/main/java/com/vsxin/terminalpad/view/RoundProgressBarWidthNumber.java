package com.vsxin.terminalpad.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.vsxin.terminalpad.R;

import skin.support.content.res.SkinCompatResources;
import skin.support.widget.SkinCompatProgressBar;
import skin.support.widget.SkinCompatProgressBarHelper;
import skin.support.widget.SkinCompatSupportable;

import static skin.support.widget.SkinCompatHelper.checkResourceId;

/**
 * Created by zckj on 2017/3/16.
 */

public class RoundProgressBarWidthNumber extends SkinCompatProgressBar  implements SkinCompatSupportable {

    private static final int DEFAULT_TEXT_SIZE = 40;
    private static final int DEFAULT_TEXT_COLOR = 0XFF33A6FF;

    private static final int DEFAULT_TEXT_COLOR_NEW = 0XFFFFFFFF;

    private static final int DEFAULT_COLOR_UNREACHED_COLOR = 0xFFFFFFFF;

    private static final int DEFAULT_HEIGHT_REACHED_PROGRESS_BAR = 10;

    //圆弧的宽度
    private static final int DEFAULT_HEIGHT_UNREACHED_PROGRESS_BAR = 10;

    private static final int DEFAULT_SIZE_TEXT_OFFSET = 10;

    /**
     * painter of all drawing things
     */
    protected Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    /**
     * color of progress number
     */
    protected int mTextColor = DEFAULT_TEXT_COLOR;
    /**
     * size of text (sp)
     */
    protected int mTextSize = sp2px(DEFAULT_TEXT_SIZE);

    /**
     * offset of draw progress
     */
    protected int mTextOffset = dp2px(DEFAULT_SIZE_TEXT_OFFSET);

    /**
     * height of reached progress bar
     */
    protected int mReachedProgressBarHeight = dp2px(DEFAULT_HEIGHT_REACHED_PROGRESS_BAR);

    /**
     * color of reached bar
     */
    protected int mReachedBarColor = DEFAULT_TEXT_COLOR;
    /**
     * color of unreached bar
     */
    protected int mUnReachedBarColor = DEFAULT_COLOR_UNREACHED_COLOR;
    /**
     * height of unreached progress bar
     */
    protected int mUnReachedProgressBarHeight = dp2px(DEFAULT_HEIGHT_UNREACHED_PROGRESS_BAR);
    /**
     * view width except padding
     */
    protected int mRealWidth;

    protected boolean mIfDrawText = true;

    protected static final int VISIBLE = 0;

    /**
     * mRadius of view
     */
    private int mRadius = dp2px(80);

    private int mReachedBarColorResID = SkinCompatProgressBarHelper.INVALID_ID;

    public RoundProgressBarWidthNumber(Context context) {
        this(context, null);
    }

    public RoundProgressBarWidthNumber(Context context, AttributeSet attrs) {
        this(context, attrs,-1);

        mReachedProgressBarHeight = (int) (mUnReachedProgressBarHeight * 1.1f);


        TypedArray ta = context.obtainStyledAttributes(attrs,
                R.styleable.RoundProgressBarWidthNumber,0,0);

        mRadius = (int) ta.getDimension(
                R.styleable.RoundProgressBarWidthNumber_radius, mRadius);

        ta.recycle();

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.HorizontalProgressBarWithNumber, -1, 0);

        mReachedBarColor = ta
                .getColor(
                        R.styleable.HorizontalProgressBarWithNumber_progress_reached_color,
                        mTextColor);

        try {
            if (a.hasValue(R.styleable.HorizontalProgressBarWithNumber_progress_reached_color)) {
                mReachedBarColorResID = a.getResourceId(
                        R.styleable.HorizontalProgressBarWithNumber_progress_reached_color, SkinCompatProgressBarHelper.INVALID_ID);
            }
        } finally {
            a.recycle();
        }
        applySkin();

        mTextSize = sp2px(30);

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

    }

    @Override
    public void applySkin() {
        super.applySkin();
        mReachedBarColorResID = checkResourceId(mReachedBarColorResID);
        if (mReachedBarColorResID != SkinCompatProgressBarHelper.INVALID_ID) {
            String typeName = getResources().getResourceTypeName(mReachedBarColorResID);
            if("color".equals(typeName)){
                mReachedBarColor = SkinCompatResources.getInstance().getColor(mReachedBarColorResID);
            }
        }
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec,
                                          int heightMeasureSpec) {
        int heightMode = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        int widthMode = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);

        int paintWidth = Math.min(mReachedProgressBarHeight,
                mUnReachedProgressBarHeight);

        if (heightMode != View.MeasureSpec.EXACTLY) {

            int exceptHeight = (int) (getPaddingTop() + getPaddingBottom()
                    + mRadius * 2 + paintWidth);
            heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(exceptHeight,
                    View.MeasureSpec.EXACTLY);
        }
        if (widthMode != View.MeasureSpec.EXACTLY) {
            int exceptWidth = (int) (getPaddingLeft() + getPaddingRight()
                    + mRadius * 2 + paintWidth);
            widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(exceptWidth,
                    View.MeasureSpec.EXACTLY);
        }

        super.onMeasure(heightMeasureSpec, heightMeasureSpec);

    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {

        String text = getProgress()/10+"s";
        // mPaint.getTextBounds(text, 0, text.length(), mTextBound);
        float textWidth = mPaint.measureText(text);
        float textHeight = (mPaint.descent() + mPaint.ascent()) / 2;

        canvas.save();
        canvas.translate(getPaddingLeft(), getPaddingTop());
        mPaint.setStyle(Paint.Style.STROKE);

        // draw unreaded bar
        mPaint.setColor(mUnReachedBarColor);
        mPaint.setStrokeWidth(mUnReachedProgressBarHeight);
        canvas.drawCircle(mRadius, mRadius, mRadius, mPaint);

        // draw reached bar
        mPaint.setColor(mReachedBarColor);
        mPaint.setStrokeWidth(mReachedProgressBarHeight);
        //设置弧度
        float sweepAngle = getProgress() * 1.0f / getMax() * 360;
        canvas.drawArc(new RectF(0, 0, mRadius * 2, mRadius * 2), -90,
                -sweepAngle, false, mPaint);

        // draw text
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(DEFAULT_TEXT_COLOR_NEW);
        canvas.drawText(text, mRadius - textWidth / 2, mRadius - textHeight,
                mPaint);

        canvas.restore();

    }

    /**
     * dp 2 px
     *
     * @param dpVal
     */
    protected int dp2px(int dpVal)
    {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, getResources().getDisplayMetrics());
    }

    /**
     * sp 2 px
     *
     * @param spVal
     * @return
     */
    protected int sp2px(int spVal)
    {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, getResources().getDisplayMetrics());

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mRealWidth = w - getPaddingRight() - getPaddingLeft();
    }

    /**
     * get the styled attributes
     *
     * @param attrs
     */
    private void obtainStyledAttributes(AttributeSet attrs) {
        // initClient values from custom attributes
        final TypedArray attributes = getContext().obtainStyledAttributes(
                attrs, R.styleable.HorizontalProgressBarWithNumber,0,0);

        mTextColor = attributes
                .getColor(
                        R.styleable.HorizontalProgressBarWithNumber_progress_text_color,
                        DEFAULT_TEXT_COLOR);
        mTextSize = (int) attributes.getDimension(
                R.styleable.HorizontalProgressBarWithNumber_progress_text_size,
                mTextSize);

//        mReachedBarColor = attributes
//                .getColor(
//                        R.styleable.HorizontalProgressBarWithNumber_progress_reached_color,
//                        mTextColor);

        mUnReachedBarColor = attributes
                .getColor(
                        R.styleable.HorizontalProgressBarWithNumber_progress_unreached_color,
                        DEFAULT_COLOR_UNREACHED_COLOR);

        mReachedProgressBarHeight = (int) attributes
                .getDimension(
                        R.styleable.HorizontalProgressBarWithNumber_progress_reached_bar_height,
                        mReachedProgressBarHeight);
        mUnReachedProgressBarHeight = (int) attributes
                .getDimension(
                        R.styleable.HorizontalProgressBarWithNumber_progress_unreached_bar_height,
                        mUnReachedProgressBarHeight);
        mTextOffset = (int) attributes
                .getDimension(
                        R.styleable.HorizontalProgressBarWithNumber_progress_text_offset,
                        mTextOffset);

        int textVisible = attributes
                .getInt(R.styleable.HorizontalProgressBarWithNumber_progress_text_visibility,
                        VISIBLE);
        if (textVisible != VISIBLE) {
            mIfDrawText = false;
        }

        attributes.recycle();
    }

    public RoundProgressBarWidthNumber(Context context, AttributeSet attrs,
                                       int defStyle) {
        super(context, attrs, defStyle);

        setHorizontalScrollBarEnabled(true);

        obtainStyledAttributes(attrs);

        mPaint.setTextSize(mTextSize);
        mPaint.setColor(mTextColor);
    }

}

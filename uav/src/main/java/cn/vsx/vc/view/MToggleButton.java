package cn.vsx.vc.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import cn.vsx.vc.R;
import cn.vsx.vc.utils.BitmapUtil;
import cn.vsx.vc.utils.DensityUtil;
import skin.support.content.res.SkinCompatResources;
import skin.support.widget.SkinCompatHelper;
import skin.support.widget.SkinCompatView;

import static skin.support.widget.SkinCompatHelper.checkResourceId;

public class MToggleButton extends SkinCompatView {

    private Bitmap sildBtn_on, sildBtn_off;
    private Paint paint;
    private boolean currState;
    private Context context;


	Drawable drawable;
	Drawable drawable3;
	Drawable drawable2;
    Drawable drawable4;

	private int drawableID = SkinCompatHelper.INVALID_ID;
	private int drawableID3 = SkinCompatHelper.INVALID_ID;
	private int drawableID2 = SkinCompatHelper.INVALID_ID;
	private int drawableID4 = SkinCompatHelper.INVALID_ID;

	public MToggleButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;

        // 根据属性集合和上下文 来设置id
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MToggleBtn, -1, 0);


		if (ta.hasValue(R.styleable.MToggleBtn_mtogglebutton_background_on)) {
			drawableID = ta.getResourceId(
					R.styleable.MToggleBtn_mtogglebutton_background_on, SkinCompatHelper.INVALID_ID);
		}

		if (ta.hasValue(R.styleable.MToggleBtn_mtogglebutton_background_off)) {
			drawableID3 = ta.getResourceId(
					R.styleable.MToggleBtn_mtogglebutton_background_off, SkinCompatHelper.INVALID_ID);
		}

		if (ta.hasValue(R.styleable.MToggleBtn_mtogglebutton_button_on)) {
			drawableID2 = ta.getResourceId(
					R.styleable.MToggleBtn_mtogglebutton_button_on, SkinCompatHelper.INVALID_ID);
		}

        if (ta.hasValue(R.styleable.MToggleBtn_mtogglebutton_button_off)) {
            drawableID4 = ta.getResourceId(
                    R.styleable.MToggleBtn_mtogglebutton_button_off, SkinCompatHelper.INVALID_ID);
        }

//		 drawable = ta.getDrawable(R.styleable.MToggleBtn_mtogglebutton_background_on);
		applySkin();

		ta.recycle();

		initView();
	}

	@Override
	public void applySkin() {

		drawableID = checkResourceId(drawableID);
		if (drawableID != SkinCompatHelper.INVALID_ID) {
			String typeName = getResources().getResourceTypeName(drawableID);
			if("drawable".equals(typeName)) {
				drawable = SkinCompatResources.getInstance().getDrawable(drawableID);
			}
		}

		drawableID3 = checkResourceId(drawableID3);
		if (drawableID3 != SkinCompatHelper.INVALID_ID) {
			String typeName = getResources().getResourceTypeName(drawableID3);
			if("drawable".equals(typeName)) {
				drawable3 = SkinCompatResources.getInstance().getDrawable(drawableID3);
			}
		}

		drawableID2 = checkResourceId(drawableID2);
		if (drawableID2 != SkinCompatHelper.INVALID_ID) {
			String typeName = getResources().getResourceTypeName(drawableID2);
			if("drawable".equals(typeName)) {
				drawable2 = SkinCompatResources.getInstance().getDrawable(drawableID2);
			}
		}

        drawableID4 = checkResourceId(drawableID4);
        if (drawableID4 != SkinCompatHelper.INVALID_ID) {
            String typeName = getResources().getResourceTypeName(drawableID4);
            if("drawable".equals(typeName)) {
                drawable4 = SkinCompatResources.getInstance().getDrawable(drawableID4);
            }
        }

        Bitmap bitmap = BitmapUtil.drawableToBitmap(drawable);
        bitmap_bg_on = Bitmap.createScaledBitmap(bitmap,DensityUtil.dip2px(context, 55), DensityUtil.dip2px(context, 26), true);

//		 Drawable drawable3 = ta.getDrawable(R.styleable.MToggleBtn_mtogglebutton_background_off);
        Bitmap bitmap3 = BitmapUtil.drawableToBitmap(drawable3);
        bitmap_bg_off = Bitmap.createScaledBitmap(bitmap3,DensityUtil.dip2px(context, 55), DensityUtil.dip2px(context, 26), true);

//		 Drawable drawable2=ta.getDrawable(R.styleable.MToggleBtn_mtogglebutton_button);
        Bitmap bitmap2 = BitmapUtil.drawableToBitmap(drawable2);
        sildBtn_on = Bitmap.createScaledBitmap(bitmap2, DensityUtil.dip2px(context, 22), DensityUtil.dip2px(context, 22), true);


//        Drawable drawable4 = ta.getDrawable(R.styleable.MToggleBtn_mtogglebutton_button_off);
        Bitmap bitmap4 = BitmapUtil.drawableToBitmap(drawable4);
        sildBtn_off = Bitmap.createScaledBitmap(bitmap4, DensityUtil.dip2px(context, 22), DensityUtil.dip2px(context, 22), true);

        // 刷新状态
        flushState();
        super.applySkin();
	}

	public MToggleButton(Context context) {
		super(context);
		this.context = context;
		initView();
	}

    private void initView() {
        sildBtnLeftMax = bitmap_bg_on.getWidth() - sildBtn_on.getWidth() - DensityUtil.dip2px(context, 4);

        paint = new Paint();
        paint.setAntiAlias(true);

        // 添加按钮的监听事件
        setOnClickListener(v -> {
            // 点击时切换按钮的状态，声明一个布尔的变量
            currState = !currState;
            // 设置按钮状态的监听
            if (onBtnClickListener != null) {
                onBtnClickListener.onBtnClick(currState);
            }
            // 刷新状态
            flushState();
        });


    }

    /**
     * 判断是否发生拖动，我们定义:如果按下，移动在15像素之内，抬起，我们认为是点击，按点击的逻辑处理开关 如果，按下并移动超过15
     * 个像素，我们认为是拖动，按拖动的逻辑处理开关
     */

    public void flushState() {
        if (currState) {
            sildBtnLeft = sildBtnLeftMax;
        } else {
            sildBtnLeft = 0;
        }
        // 刷新view
        flushView();
    }

    private void flushView() {
        // 先对sildBtnLeft的值进行判断
        if (sildBtnLeft < 0) {
            sildBtnLeft = 0;
        } else if (sildBtnLeft > sildBtnLeftMax) {
            sildBtnLeft = sildBtnLeftMax;
        }
        // 通知系统 ，刷新页面，会导致 onDraw 方法的执行
        invalidate();
    }

    /**
     * 一个view从创建到显示到屏幕上，要经历的步骤 一、测量控件的大小 onMeasure(int widthMeasureSpec, int
     * heightMeasureSpec); 二、指定位置。 onLayout(boolean changed, int left, int top,
     * int right,int bottom); 三、绘制控件 onDraw(Canvas canvas);
     */

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = bitmap_bg_on.getWidth();
        int height = bitmap_bg_on.getHeight();
        // 设置背景图片与控件的大小相同
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        // TODO Auto-generated method stub
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 绘制背景
        /**
         * 参数二，是图片在左边距, 参数三，是图片在上边距,
         */
        if (currState) {
            canvas.drawBitmap(bitmap_bg_on, 0, 0, paint);
            canvas.drawBitmap(sildBtn_on, sildBtnLeft + DensityUtil.dip2px(context, 2), DensityUtil.dip2px(context, 2), paint);
        } else {
            canvas.drawBitmap(bitmap_bg_off, 0, 0, paint);
            canvas.drawBitmap(sildBtn_off, sildBtnLeft + DensityUtil.dip2px(context, 2), DensityUtil.dip2px(context, 2), paint);
        }
    }

    // 定义按钮距离左边的距离
    private float sildBtnLeft = 0;
    private int sildBtnLeftMax;
    private Bitmap bitmap_bg_on;
    private Bitmap bitmap_bg_off;

    // 开关的监听
    private OnBtnClickListener onBtnClickListener;

    public void setOnBtnClick(OnBtnClickListener onBtnClickListener) {
        this.onBtnClickListener = onBtnClickListener;
    }

    public interface OnBtnClickListener {
        void onBtnClick(boolean currState);
    }

    public void initToggleState(boolean isOpenGroupScan) {
        if (isOpenGroupScan) {
            currState = true;
            sildBtnLeft = sildBtnLeftMax;
        } else {
            currState = false;
            sildBtnLeft = 0;
        }
        // 刷新view
        flushView();
    }
    /**
     * 获取当前的选择状态
     * @return
     */
    public boolean isChecked() {
        return currState;
    }
}

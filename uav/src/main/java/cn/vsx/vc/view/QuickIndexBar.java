package cn.vsx.vc.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class QuickIndexBar extends View {
	private static final String[] LETTERS = new String[] { "A", "B", "C", "D",
			"E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q",
			"R", "S", "T", "U", "V", "W", "X", "Y", "Z" };

	private Paint paint;
	private int mCellWidth;
	private int mHeight;
	private int mCellHeight;
	private float mTextHeight;
	private int currentIndex = -1;

	public interface OnLetterChangeListener {
		void onLetterChange(String letter);

		void onReset();// 手指抬起
	}

	private OnLetterChangeListener onLetterChangeListener;

	public OnLetterChangeListener getOnLetterChangeListener() {
		return onLetterChangeListener;
	}

	public void setOnLetterChangeListener(
			OnLetterChangeListener onLetterChangeListener) {
		this.onLetterChangeListener = onLetterChangeListener;
	}

	public QuickIndexBar(Context context) {
		this(context, null);
	}

	public QuickIndexBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public QuickIndexBar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		paint = new Paint();
		// 抗锯齿
		paint.setAntiAlias(true);
		//字体颜色
		paint.setColor(Color.WHITE);
		//字体大小
		paint.setTextSize(30);
		// 字体加粗
		paint.setTypeface(Typeface.DEFAULT_BOLD);
		// 获取字体高度
		Paint.FontMetrics fontMetrics = paint.getFontMetrics();
		mTextHeight = (float) Math.ceil(fontMetrics.descent
				- fontMetrics.ascent); // ceil 天花板 2.1-->3 flow 地板 2.9----2
	}

	/**
	 * 每次测量完成后 值改变的时候调用
	 * 
	 * @param w
	 * @param h
	 * @param oldw
	 * @param oldh
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mCellWidth = getMeasuredWidth();
		mHeight = getMeasuredHeight();

		mCellHeight = mHeight / LETTERS.length;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		for (int i = 0; i < LETTERS.length; i++) {
			String text = LETTERS[i];

			float mTextWidth = paint.measureText(text);
			float x = mCellWidth * 0.5f - mTextWidth * 0.5f;

			float y = mCellHeight * 0.5f + mTextHeight * 0.5f + mCellHeight * i;
			if (currentIndex == i) {
				paint.setColor(Color.GREEN);
			} else {
				paint.setColor(Color.LTGRAY);
			}

			canvas.drawText(text, x, y, paint);

		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:

			int downY = (int) event.getY();
			currentIndex = downY / mCellHeight;
			// Toast.makeText(getContext(),LETTERS[currentIndex],
			// Toast.LENGTH_SHORT).show();
			if (currentIndex < 0 || currentIndex > LETTERS.length - 1) {

			} else {
				// ToastUtils.show(getContext(), LETTERS[currentIndex]);
				if (onLetterChangeListener != null) {
					onLetterChangeListener
							.onLetterChange(LETTERS[currentIndex]);
				}
			}
			// 重新绘制 ----> ondraw
			invalidate();

			break;
		case MotionEvent.ACTION_MOVE:
			int moveY = (int) event.getY();
			currentIndex = moveY / mCellHeight;
			// Toast.makeText(getContext(),LETTERS[currentIndex],
			// Toast.LENGTH_SHORT).show();
			if (currentIndex < 0 || currentIndex > LETTERS.length - 1) {

			} else {
				// ToastUtils.show(getContext(), LETTERS[currentIndex]);
				if (onLetterChangeListener != null) {
					onLetterChangeListener
							.onLetterChange(LETTERS[currentIndex]);
				}
			}
			// 重新绘制 ----> ondraw
			invalidate();
			break;
		case MotionEvent.ACTION_UP:
			currentIndex = -1;
			// 重新绘制 ----> ondraw
			invalidate();
			if (onLetterChangeListener != null) {
				onLetterChangeListener.onReset();
			}

			break;
		}

		// 父view 没有事件的处理
		return true;
	}
}

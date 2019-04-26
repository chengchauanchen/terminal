package cn.vsx.vc.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.sql.Date;
import java.text.SimpleDateFormat;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.vsx.vc.R;

public class MyListView extends ListView {

	public MyListView(Context context) {
		super(context);
		initView();
	}

	public MyListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	public MyListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView();
	}

	// 总布局
	@Bind(R.id.ll_refresh_header_root)
	LinearLayout ll_refresh_header_root;
	// 头布局
	@Bind(R.id.ll_refresh_header_view)
	LinearLayout ll_refresh_header_view;
	// 刷新头
	@Bind(R.id.pb_refresh_header)
	ProgressBar pb_refresh_header;
	// 刷新脚
//	@Bind(R.id.ll_refresh_footer_root)
//	LinearLayout ll_refresh_footer_root;
	// 箭头
	// 提示信息
	// 日期
	private int measuredHeight;

	/**
	 * 初始化view的方法 1.添加刷新头 2.隐藏刷新头 3.跟着手指一起显示
	 */
	private void initView() {
		sp = getContext().getSharedPreferences("lastTime",getContext().MODE_PRIVATE);
		headerView = View.inflate(getContext(), R.layout.refresh_header,null);
		ButterKnife.bind(this, headerView);
		// 添加刷新头
		addHeaderView(headerView);
		// 隐藏刷新头
		/**
		 * 1.widthMeasureSpec : 宽度的测量标准(尺子) 2.heightMeasureSpec : 高度的测量标准(尺子)
		 * 测量模式: 1. MeasureSpec.AT_MOST 最大值测量 1000 (0-1000) 2.
		 * MeasureSpec.EXACTLY 精确值测量 60 (60) 3.MeasureSpec.UNSPECIFIED
		 * 未指定测量(不指定标准,随意测量) (没有范围) 0 0
		 */
		ll_refresh_header_view.measure(0, 0);
		measuredHeight = ll_refresh_header_view.getMeasuredHeight();
		ll_refresh_header_view.setPadding(0, -measuredHeight, 0, 0);

		// 初始化动画
		initAnimation();
		setVerticalScrollBarEnabled(false);
		setScrollbarFadingEnabled(false);
	}

	private void initAnimation() {
		RotateAnimation down2up = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF,
				0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		down2up.setDuration(200);
		down2up.setFillAfter(true);
		RotateAnimation up2down = new RotateAnimation(-180, -360, Animation.RELATIVE_TO_SELF,
				0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		up2down.setDuration(200);
		up2down.setFillAfter(true);

	}

	private int downY;
	private int moveY;
	// (下拉刷新 / 释放刷新 / 正在刷新)
	public static final int state_down_refresh = 0;
	public static final int state_release_refresh = 1;
	public static final int state_ing_refresh = 2;
	public int currentState = state_down_refresh;// 当前状态:下拉刷新

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		//执行listview中的条目点击事件等等。
		super.onTouchEvent(ev);
		
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downY = (int) ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			// 如果rollView和listview的y坐标重合之后,向下滑动,才改变刷新头的位置
			if (getFirstVisiblePosition() == 0) {

				moveY = (int) ev.getY();

				if (moveY > downY) {
					int disY = moveY - downY;
					int paddingTop = -measuredHeight + disY;
					if(paddingTop <= 0)
						ll_refresh_header_view.setPadding(0, paddingTop, 0, 0);

					/**
					 * 移动逻辑:move 下拉刷新-释放刷新: paddingTop>=0 释放刷新-下拉刷新:
					 * paddingTop<0
					 */

					if (currentState == state_down_refresh && paddingTop >= 0) {
						currentState = state_release_refresh;
						refreshState();
					}
					if (currentState == state_release_refresh && paddingTop < 0) {
						currentState = state_down_refresh;
						refreshState();
					}
					return true;// 如果改变paddingTop显示刷新头,listview就可以不下滑
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			/**
			 * 松手逻辑: up 释放刷新-正在刷新 下拉刷新-状态不变(隐藏刷新头)
			 */
			if (currentState == state_down_refresh) {
				// 隐藏刷新头
				ll_refresh_header_view.setPadding(0, -measuredHeight, 0, 0);
			}
			if (currentState == state_release_refresh) {
				currentState = state_ing_refresh;
				refreshState();
			}
			
			// 每次刷新时就重置一下downY
			downY = 0;
			break;

		default:
			break;
		}
		return true;
	}

	private void refreshState() {
		lastTime = sp.getString("lastTime", "00:00:00");
		switch (currentState) {
		case state_down_refresh:
			break;
		case state_release_refresh:
			break;
		case state_ing_refresh:
			// 隐藏箭头 显示进度条 改变文字
			pb_refresh_header.setVisibility(View.VISIBLE);
			// 清楚动画
			// 完全显示下拉刷新头
			ll_refresh_header_view.setPadding(0, 0, 0, 0);
			// 发送延迟消息模拟刷新
//			handler.sendEmptyMessageDelayed(0, 2000);
			//进行真实的刷新
			if (onRefreshListener!=null) {
				onRefreshListener.onRefresh();
			}
		
			// 设置时间
			// String lastTime =
			// DateFormat.getDateFormat(getContext()).format(currentTimeMillis);
			long currentTimeMillis = System.currentTimeMillis();
			Date date = new Date(currentTimeMillis);
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
			String newTime = simpleDateFormat.format(date);
			sp.edit().putString("lastTime", newTime).commit();

			break;

		default:
			break;
		}

	}

	private OnRefreshListener onRefreshListener;
	public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
		this.onRefreshListener=onRefreshListener;
	}
	//定义一个真实数据的刷新接口
	public interface OnRefreshListener{
		void onRefresh();//刷新当前数据
		void onLoadMore();//加载下一页数据
	}
	
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				refreshFinish();
				break;
			case 1:
//				isLoadMore = false;
//				// 显示脚布局
//				footerView.setPadding(0, 0, 0, 0);
				break;

			default:
				break;
			}
		}
	};

	public boolean isLoadMore = false;

	public void refreshFinish() {
		if (isLoadMore) {
//			// 隐藏脚布局
//			footerView.setPadding(0, -footerHeight, 0, 0);
//			// 隐藏一段时间后，发送消息显示
//			handler.sendEmptyMessageDelayed(1, 200);
		} else {
			// 隐藏进度条 显示箭头 改变文字
			pb_refresh_header.setVisibility(INVISIBLE);
			// 改变默认状态
			currentState = state_down_refresh;
			// 隐藏下拉刷新头
			ll_refresh_header_view.setPadding(0, -measuredHeight, 0, 0);

		}
	}
	
	private String lastTime;
	private SharedPreferences sp;
	private View headerView;
}

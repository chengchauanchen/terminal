package cn.vsx.vc.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

import org.apache.log4j.Logger;

public abstract class BaseViewPager extends ViewPager{
	
	public Logger logger = Logger.getLogger(getClass());
	
	public BaseViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context=context;
	}

	public BaseViewPager(Context context) {
		super(context);
		this.context=context;
	}

	public Context context;

	public boolean canScroll(){
		return true;
	}
	
	/**
	 * 初始化界面
	 */
	public abstract View initView();
	
	/**
	 * 给控件添加监听
	 */
	public abstract void initListener();
	
	/**
	 * 初始化数据 给控件填充内容
	 */
	public abstract void initData();
	/**
	 * viewpager销毁时
	 */
	public abstract void doDestroy();
}

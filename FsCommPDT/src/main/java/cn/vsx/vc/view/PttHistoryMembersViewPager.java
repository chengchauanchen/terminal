package cn.vsx.vc.view;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.AttributeSet;
import android.view.MotionEvent;

import cn.vsx.vc.adapter.PttHistoryMembersAdapter;

public class PttHistoryMembersViewPager extends LazyViewPager {

	public Context context;

	public PttHistoryMembersViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context=context;
	}

	public PttHistoryMembersViewPager(Context context) {
		super(context);
		this.context=context;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		PagerAdapter pagerAdapter = getAdapter();
		if(pagerAdapter instanceof PttHistoryMembersAdapter){
			BaseViewPager bvp = ((PttHistoryMembersAdapter)pagerAdapter).getCurrentBaseViewPager();
			return bvp.canScroll() && super.onInterceptTouchEvent(event);
		}
		return super.onInterceptTouchEvent(event);
	}
	
}

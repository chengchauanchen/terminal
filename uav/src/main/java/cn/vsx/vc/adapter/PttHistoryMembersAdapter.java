package cn.vsx.vc.adapter;

import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import cn.vsx.vc.view.BaseViewPager;

public class PttHistoryMembersAdapter extends ViewPagerAdapter {

	private List<BaseViewPager> baseViewPagers;
	private BaseViewPager currentBaseViewPager;

	public PttHistoryMembersAdapter(List<BaseViewPager> baseViewPagers) {
		super(baseViewPagers);
		this.baseViewPagers = baseViewPagers;
	}

	@Override
	public int getCount() {
		return baseViewPagers.size();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == (View) arg1;
	}

	@Override
	public Object instantiateItem(View container, int position) {
		((ViewPager) container).addView(baseViewPagers.get(position), 0);
		return baseViewPagers.get(position);
	}

	@Override
	public void destroyItem(View container, int position, Object object) {
		((ViewPager) container).removeView(baseViewPagers.get(position));
	}

	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object) {
		super.setPrimaryItem(container, position, object);
		if(position>=0 && position < baseViewPagers.size())currentBaseViewPager = baseViewPagers.get(position);
	}
	
	public BaseViewPager getCurrentBaseViewPager(){
		return currentBaseViewPager;
	}

}

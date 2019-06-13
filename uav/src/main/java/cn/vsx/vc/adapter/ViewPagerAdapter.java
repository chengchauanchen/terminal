package cn.vsx.vc.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import org.apache.log4j.Logger;

import java.util.List;

import cn.vsx.vc.view.BaseViewPager;

public class ViewPagerAdapter extends PagerAdapter{

	private Logger logger = Logger.getLogger(getClass());
	private List<BaseViewPager> pagers;
	public ViewPagerAdapter(List<BaseViewPager>  pagers){
		this.pagers = pagers;
	}
	
	@Override
	public int getCount() {
		return pagers.size();
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		BaseViewPager currentPager = pagers.get(position);
		//初始化viewpager时，调用baseviewpager的抽象方法。
		View view = currentPager.initView();
		currentPager.initListener();
		currentPager.initData();
		container.addView(view);
		return view;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		BaseViewPager currentPager = pagers.get(position);
		logger.info("执行了" + currentPager.getClass().getName() + "的doDestory方法");
		currentPager.doDestroy();
		container.removeView((View) object);
	}
	
	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view==object;
	}

}

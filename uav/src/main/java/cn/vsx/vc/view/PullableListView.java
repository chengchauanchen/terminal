package cn.vsx.vc.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

import skin.support.widget.SkinCompatBackgroundHelper;
import skin.support.widget.SkinCompatSupportable;

public class PullableListView extends ListView implements Pullable,SkinCompatSupportable{

	private SkinCompatBackgroundHelper mBackgroundTintHelper;

	public PullableListView(Context context)
	{
		super(context,null);
	}

	public PullableListView(Context context, AttributeSet attrs)
	{
		super(context, attrs,0);
	}

	public PullableListView(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		mBackgroundTintHelper = new SkinCompatBackgroundHelper(this);
		mBackgroundTintHelper.loadFromAttributes(attrs, defStyle);
	}

	@Override
	public boolean canPullDown(){
		if (getChildCount() == 0){
			// 没有item的时候也可以下拉刷新
			return true;
		} else {
			View childAt = getChildAt(0);
			if (childAt != null && childAt.getTop() >= 0) {
				// 滑到ListView的顶部了
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean canPullUp(){
//		if (getCount() == 0)
//		{
//			// 没有item的时候也可以上拉加载
//			return true;
//		} else if (getLastVisiblePosition() == (getCount() - 1))
//		{
//			// 滑到底部了
//			if (getChildAt(getLastVisiblePosition() - getFirstVisiblePosition()) != null
//					&& getChildAt(
//							getLastVisiblePosition()
//									- getFirstVisiblePosition()).getBottom() <= getMeasuredHeight())
//				return true;
//		}
		return false;
	}



	@Override
	public void applySkin() {
		if (mBackgroundTintHelper != null) {
			mBackgroundTintHelper.applySkin();
		}
	}
}

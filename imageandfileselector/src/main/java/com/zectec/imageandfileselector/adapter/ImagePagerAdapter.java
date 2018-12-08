package com.zectec.imageandfileselector.adapter;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by XX on 2018/1/18.
 */

public class ImagePagerAdapter extends PagerAdapter {

    private List<ImageView> mImageViews;

    public ImagePagerAdapter(List<ImageView> mImageViews) {
        this.mImageViews = mImageViews;
    }

    @Override
    public int getCount() {
        return mImageViews.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        container.removeView(mImageViews.get(position % mImageViews.size()));

    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ((ViewPager)container).addView(mImageViews.get(position % mImageViews.size()), 0);
        return mImageViews.get(position % mImageViews.size());
    }
}
